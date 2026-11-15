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

import java.util.Objects;

/** What a media player is playing, as read at {@link #sampledAt}. */
public final class MediaStatus {

	/** Display name of the player, such as "Spotify" or "Tidal-hifi". */
	public final String player;
	public final String title;
	public final String artist;
	public final String album;
	/** Track length in milliseconds, or 0 when the player does not say. */
	public final long lengthMs;
	private final long positionMs;
	public final boolean playing;
	/** Where the cover art can be loaded from (http(s) or file URL), or null. */
	public final String artUrl;
	public final long sampledAt;

	public MediaStatus(String player, String title, String artist, String album, long lengthMs, long positionMs,
	                   boolean playing, String artUrl) {
		this.player = player;
		this.title = title == null ? "" : title;
		this.artist = artist == null ? "" : artist;
		this.album = album == null ? "" : album;
		this.lengthMs = Math.max(0, lengthMs);
		this.positionMs = Math.max(0, positionMs);
		this.playing = playing;
		this.artUrl = artUrl == null || artUrl.isEmpty() ? null : artUrl;
		this.sampledAt = System.currentTimeMillis();
	}

	/** The playing position now, counting on from the last sample while playing. */
	public long currentPositionMs() {
		long position = positionMs + (playing ? System.currentTimeMillis() - sampledAt : 0);
		return lengthMs > 0 ? Math.min(position, lengthMs) : position;
	}

	/** Identifies the track, to notice track changes. */
	public String trackKey() {
		return player + '\u0000' + title + '\u0000' + artist + '\u0000' + album;
	}

	public boolean hasTrack() {
		return !title.isEmpty();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof MediaStatus)) {
			return false;
		}
		MediaStatus other = (MediaStatus) o;
		return trackKey().equals(other.trackKey()) && playing == other.playing && Objects.equals(artUrl, other.artUrl);
	}

	@Override
	public int hashCode() {
		return trackKey().hashCode();
	}
}
