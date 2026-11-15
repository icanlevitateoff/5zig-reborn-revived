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

/** Reads, and controls, the media playing on this computer. One implementation per operating system. */
interface MediaSource {

	/**
	 * Reads what is playing now. Called about once a second from the media thread.
	 *
	 * @return the status, or null when nothing is playing
	 * @throws MediaException when the source cannot work on this computer (the message says why)
	 */
	MediaStatus poll() throws MediaException;

	/** Cover art as image bytes, for sources that cannot give a URL; null otherwise. */
	default byte[] pollArt() {
		return null;
	}

	void playPause();

	void next();

	void previous();

	/** Stops any helper process. The source may be polled again afterwards. */
	default void stop() {
	}

	class MediaException extends Exception {
		public MediaException(String message) {
			super(message);
		}
	}
}
