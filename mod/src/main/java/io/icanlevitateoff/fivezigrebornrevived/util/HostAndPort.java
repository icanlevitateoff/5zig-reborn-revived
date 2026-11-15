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

package io.icanlevitateoff.fivezigrebornrevived.util;

/**
 * A server address split into host and port. Understands {@code host}, {@code host:port},
 * {@code [ipv6]:port} and a bare IPv6 address (which has no port).
 */
public final class HostAndPort {

	public static final int DEFAULT_PORT = 25565;

	public final String host;
	public final int port;

	private HostAndPort(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public static HostAndPort parse(String address) {
		String s = address == null ? "" : address.trim();
		String host = s;
		String portPart = null;
		if (s.startsWith("[")) {
			int end = s.indexOf(']');
			if (end > 0) {
				host = s.substring(1, end);
				if (s.length() > end + 1 && s.charAt(end + 1) == ':') {
					portPart = s.substring(end + 2);
				}
			}
		} else {
			int colon = s.indexOf(':');
			if (colon >= 0 && colon == s.lastIndexOf(':')) {
				host = s.substring(0, colon);
				portPart = s.substring(colon + 1);
			}
		}
		int port = DEFAULT_PORT;
		if (portPart != null) {
			try {
				int parsed = Integer.parseInt(portPart.trim());
				if (parsed > 0 && parsed <= 65535) {
					port = parsed;
				}
			} catch (NumberFormatException ignored) {
			}
		}
		return new HostAndPort(host, port);
	}

	/** {@code host:port}, with IPv6 hosts in brackets so the result can be parsed again. */
	public static String format(String host, int port) {
		return (host.indexOf(':') >= 0 && !host.startsWith("[") ? "[" + host + "]" : host) + ":" + port;
	}

	/** Like {@link #format}, but leaves out the default port. */
	public String toDisplayString() {
		return port == DEFAULT_PORT ? (host.indexOf(':') >= 0 ? "[" + host + "]" : host) : format(host, port);
	}
}
