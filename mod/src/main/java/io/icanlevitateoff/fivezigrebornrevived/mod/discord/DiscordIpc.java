/*
 * Copyright (c) 2026 IcanLevitate
 *
 * This file is part of The 5zig Mod
 * The 5zig Mod is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The 5zig Mod is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with The 5zig Mod.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.icanlevitateoff.fivezigrebornrevived.mod.discord;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Minimal client for Discord's local IPC (Rich Presence). Linux and macOS use the UNIX socket
 * {@code discord-ipc-N} (through the JDK, no native library), Windows the named pipe of the same name.
 * Each frame is: opcode (int32 LE), length (int32 LE), JSON.
 */
final class DiscordIpc implements Closeable {

	static final int OP_HANDSHAKE = 0;
	static final int OP_FRAME = 1;
	static final int OP_CLOSE = 2;

	private static final int MAX_FRAME = 1 << 20;

	private SocketChannel unix;
	private RandomAccessFile pipe;

	/** Connects to the first Discord client found, or throws. */
	static DiscordIpc open() throws IOException {
		boolean windows = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
		for (int i = 0; i < 10; i++) {
			if (windows) {
				try {
					DiscordIpc ipc = new DiscordIpc();
					ipc.pipe = new RandomAccessFile("\\\\.\\pipe\\discord-ipc-" + i, "rw");
					return ipc;
				} catch (IOException ignored) {
				}
				continue;
			}
			for (String dir : unixDirectories()) {
				File socket = new File(dir, "discord-ipc-" + i);
				if (!socket.exists()) {
					continue;
				}
				SocketChannel channel = SocketChannel.open(StandardProtocolFamily.UNIX);
				try {
					channel.connect(UnixDomainSocketAddress.of(socket.toPath()));
					DiscordIpc ipc = new DiscordIpc();
					ipc.unix = channel;
					return ipc;
				} catch (IOException e) {
					channel.close();
				}
			}
		}
		throw new IOException("no Discord client is running");
	}

	/** Where Discord (native, Flatpak, Snap, Vesktop) puts its socket. */
	private static List<String> unixDirectories() {
		List<String> bases = new ArrayList<>();
		for (String env : new String[]{"XDG_RUNTIME_DIR", "TMPDIR", "TMP", "TEMP"}) {
			String value = System.getenv(env);
			if (value != null && !value.isEmpty()) {
				bases.add(value);
			}
		}
		bases.add("/tmp");
		List<String> dirs = new ArrayList<>();
		for (String base : bases) {
			dirs.add(base);
			dirs.add(base + "/app/com.discordapp.Discord");
			dirs.add(base + "/app/com.discordapp.DiscordCanary");
			dirs.add(base + "/app/dev.vencord.Vesktop");
			dirs.add(base + "/.flatpak/dev.vencord.Vesktop/xdg-run");
			dirs.add(base + "/snap.discord");
			dirs.add(base + "/snap.discord-canary");
		}
		return dirs;
	}

	synchronized void send(int op, JsonObject json) throws IOException {
		byte[] body = json.toString().getBytes(StandardCharsets.UTF_8);
		ByteBuffer frame = ByteBuffer.allocate(8 + body.length).order(ByteOrder.LITTLE_ENDIAN);
		frame.putInt(op).putInt(body.length).put(body).flip();
		if (unix != null) {
			while (frame.hasRemaining()) {
				unix.write(frame);
			}
		} else {
			pipe.write(frame.array());
		}
	}

	/** Reads one frame; returns its JSON with the opcode added as "__op". */
	JsonObject read() throws IOException {
		ByteBuffer header = readFully(8).order(ByteOrder.LITTLE_ENDIAN);
		int op = header.getInt();
		int length = header.getInt();
		if (length < 0 || length > MAX_FRAME) {
			throw new IOException("bad frame length " + length);
		}
		String text = new String(readFully(length).array(), StandardCharsets.UTF_8);
		JsonObject json = text.isEmpty() ? new JsonObject() : JsonParser.parseString(text).getAsJsonObject();
		json.addProperty("__op", op);
		return json;
	}

	private ByteBuffer readFully(int length) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(length);
		if (unix != null) {
			while (buffer.hasRemaining()) {
				if (unix.read(buffer) < 0) {
					throw new IOException("Discord closed the connection");
				}
			}
		} else {
			pipe.readFully(buffer.array());
		}
		buffer.rewind();
		return buffer;
	}

	@Override
	public void close() {
		try {
			if (unix != null) {
				unix.close();
			}
			if (pipe != null) {
				pipe.close();
			}
		} catch (IOException ignored) {
		}
	}
}
