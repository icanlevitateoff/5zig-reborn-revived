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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Linux: reads players through MPRIS on the D-Bus session bus, using the {@code gdbus} tool that
 * comes with GLib. Works with Spotify, tidal-hifi, browsers, VLC and anything else that supports
 * MPRIS. Prefers a player that is playing; otherwise sticks with the last one seen.
 */
final class MprisSource implements MediaSource {

	private static final String PREFIX = "org.mpris.MediaPlayer2.";
	private static final String PATH = "/org/mpris/MediaPlayer2";
	private static final Pattern PLAYER_NAME = Pattern.compile("'(org\\.mpris\\.MediaPlayer2\\.[^']+)'");
	private static final long TIMEOUT = 2000;

	private String lastPlayer;

	@Override
	public MediaStatus poll() throws MediaException {
		if (!Processes.exists("gdbus")) {
			throw new MediaException("gdbus not found (install GLib's tools, e.g. libglib2.0-bin)");
		}
		List<String> players = new ArrayList<>();
		try {
			String names = Processes.run(TIMEOUT, "gdbus", "call", "--session", "--dest", "org.freedesktop.DBus",
					"--object-path", "/org/freedesktop/DBus", "--method", "org.freedesktop.DBus.ListNames");
			Matcher m = PLAYER_NAME.matcher(names);
			while (m.find()) {
				players.add(m.group(1));
			}
		} catch (IOException e) {
			throw new MediaException("could not reach the D-Bus session bus");
		}
		String flatpak = System.getenv("FLATPAK_ID");
		if (players.isEmpty() && flatpak != null) {
			// Flatpak hides other apps' bus names unless the launcher is allowed to see them.
			throw new MediaException("No players found. Flatpak may be hiding them; allow with: flatpak override --user --talk-name=org.mpris.MediaPlayer2.* " + flatpak);
		}
		MediaStatus best = null;
		String bestName = null;
		for (String name : players) {
			MediaStatus status = read(name);
			if (status == null || !status.hasTrack()) {
				continue;
			}
			boolean better = best == null
					|| (status.playing && !best.playing)
					|| (status.playing == best.playing && name.equals(lastPlayer));
			if (better) {
				best = status;
				bestName = name;
			}
		}
		if (bestName != null) {
			lastPlayer = bestName;
		}
		return best;
	}

	private MediaStatus read(String busName) {
		String text;
		try {
			text = Processes.run(TIMEOUT, "gdbus", "call", "--session", "--dest", busName, "--object-path", PATH,
					"--method", "org.freedesktop.DBus.Properties.GetAll", "org.mpris.MediaPlayer2.Player");
		} catch (IOException e) {
			return null;
		}
		String state = GVariantText.string(text, "PlaybackStatus");
		if (state == null || state.equals("Stopped")) {
			return null;
		}
		List<String> artists = GVariantText.strings(text, "xesam:artist");
		long lengthMicros = GVariantText.number(text, "mpris:length");
		long positionMicros = GVariantText.number(text, "Position");
		String artist = String.join(", ", artists).trim();
		if (lengthMicros <= 0 && artist.isEmpty()) {
			return null; // a browser tab that only reports its page title, not media
		}
		return new MediaStatus(playerName(busName), GVariantText.string(text, "xesam:title"), artist,
				GVariantText.string(text, "xesam:album"), lengthMicros / 1000, positionMicros / 1000,
				state.equals("Playing"), GVariantText.string(text, "mpris:artUrl"));
	}

	/** "org.mpris.MediaPlayer2.tidal-hifi" -> "Tidal-hifi"; instance suffixes are dropped. */
	static String playerName(String busName) {
		String name = busName.substring(PREFIX.length());
		int dot = name.indexOf('.');
		if (dot > 0) {
			name = name.substring(0, dot);
		}
		return name.isEmpty() ? name : name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1);
	}

	private void call(String method) {
		String player = lastPlayer;
		if (player != null) {
			Processes.fire("gdbus", "call", "--session", "--dest", player, "--object-path", PATH,
					"--method", "org.mpris.MediaPlayer2.Player." + method);
		}
	}

	@Override
	public void playPause() {
		call("PlayPause");
	}

	@Override
	public void next() {
		call("Next");
	}

	@Override
	public void previous() {
		call("Previous");
	}
}
