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

package io.icanlevitateoff.fivezigrebornrevived.mod.manager.media;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/** Runs short helper commands (gdbus, osascript, powershell) without a shell. */
final class Processes {

	private Processes() {
	}

	/** Runs the command and returns its standard output, or throws if it fails or takes too long. */
	static String run(long timeoutMillis, String... command) throws IOException {
		Process process = new ProcessBuilder(command).start();
		try {
			process.getOutputStream().close();
			// Read both pipes in the background so a hung or chatty command cannot block us.
			byte[][] out = new byte[1][];
			Thread reader = new Thread(() -> out[0] = readAll(process.getInputStream()), "5zig media stdout");
			Thread drain = new Thread(() -> readAll(process.getErrorStream()), "5zig media stderr");
			reader.setDaemon(true);
			drain.setDaemon(true);
			reader.start();
			drain.start();
			if (!process.waitFor(timeoutMillis, TimeUnit.MILLISECONDS)) {
				throw new IOException(command[0] + " timed out");
			}
			reader.join(1000);
			if (process.exitValue() != 0) {
				throw new IOException(command[0] + " exited with " + process.exitValue());
			}
			return out[0] == null ? "" : new String(out[0], StandardCharsets.UTF_8);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IOException("interrupted");
		} finally {
			process.destroyForcibly();
		}
	}

	/** Runs the command in the background, ignoring its result (for play/pause and skipping). */
	static void fire(String... command) {
		Thread thread = new Thread(() -> {
			try {
				run(5000, command);
			} catch (IOException ignored) {
			}
		}, "5zig media control");
		thread.setDaemon(true);
		thread.start();
	}

	static boolean exists(String program) {
		String path = System.getenv("PATH");
		if (path == null) {
			return false;
		}
		for (String dir : path.split(java.io.File.pathSeparator)) {
			if (new java.io.File(dir, program).canExecute()) {
				return true;
			}
		}
		return false;
	}

	private static byte[] readAll(InputStream in) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[8192];
		try {
			int n;
			while ((n = in.read(buffer)) != -1) {
				if (out.size() < 16 * 1024 * 1024) {
					out.write(buffer, 0, n);
				}
			}
		} catch (IOException ignored) {
		}
		return out.toByteArray();
	}
}
