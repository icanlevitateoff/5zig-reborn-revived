/*
 * Copyright (c) 2019-2020 5zig Reborn
 * Copyright (c) 2026 IcanLevitate
 * Copyright (c) 2015-2019 5zig
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

package io.icanlevitateoff.fivezigrebornrevived.mod.chat.network;

/**
 * Keeps the 5zig network's Mojang session login away from the game's own.
 * <p>
 * The 5zig network logs in through Mojang's session server, the same endpoint the game uses to join a
 * server, and Mojang rate-limits it per account (HTTP 429). Both at once, e.g. at launch or when the
 * last-server button is used straight away, could get the game's join refused. So the network waits until
 * {@link #QUIET_MS} after launch and after the game last started joining a server.
 * <p>
 * No dependencies on purpose: {@link #onGameConnect()} is called from a mixin that can run before 5zig
 * has started.
 */
public final class SessionGate {

	private static final long QUIET_MS = 20_000;
	private static final long STARTED = System.currentTimeMillis();
	private static volatile long lastGameConnect;

	private SessionGate() {
	}

	/** Called when the game starts connecting to a server (server list, last server, quick play...). */
	public static void onGameConnect() {
		lastGameConnect = System.currentTimeMillis();
	}

	/** Blocks until the game has not started a server join for a while. False if interrupted. */
	public static boolean waitUntilQuiet() {
		while (true) {
			long wait = Math.max(STARTED, lastGameConnect) + QUIET_MS - System.currentTimeMillis();
			if (wait <= 0) {
				return true;
			}
			try {
				Thread.sleep(Math.min(wait, 1000));
			} catch (InterruptedException e) {
				return false;
			}
		}
	}
}
