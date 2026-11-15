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

/**
 * macOS: asks Spotify and Apple Music through AppleScript. macOS has no public API for whatever
 * app is playing, so other players (such as TIDAL) are not supported here. An app is only asked
 * while it is running, so the script never starts it (or asks where it is).
 */
final class MacMediaSource implements MediaSource {

	private static final long TIMEOUT = 3000;

	private enum App {
		SPOTIFY("Spotify", "Spotify", true),
		MUSIC("Music", "Music", false);

		final String process;
		final String display;
		final boolean hasArtworkUrl;

		App(String process, String display, boolean hasArtworkUrl) {
			this.process = process;
			this.display = display;
			this.hasArtworkUrl = hasArtworkUrl;
		}
	}

	private App lastApp;

	@Override
	public MediaStatus poll() {
		MediaStatus best = null;
		App bestApp = null;
		for (App app : App.values()) {
			if (!running(app)) {
				continue;
			}
			MediaStatus status = read(app);
			if (status == null || !status.hasTrack()) {
				continue;
			}
			if (best == null || (status.playing && !best.playing) || (status.playing == best.playing && app == lastApp)) {
				best = status;
				bestApp = app;
			}
		}
		if (bestApp != null) {
			lastApp = bestApp;
		}
		return best;
	}

	private static boolean running(App app) {
		try {
			Processes.run(TIMEOUT, "pgrep", "-x", app.process);
			return true;
		} catch (IOException e) {
			return false; // pgrep exits with 1 when nothing matches
		}
	}

	private static MediaStatus read(App app) {
		// Spotify reports duration in milliseconds, Music in seconds; positions are seconds in both.
		String duration = app == App.SPOTIFY ? "(duration of t) as integer" : "((duration of t) * 1000) as integer";
		String script = String.join("\n",
				"tell application \"" + app.process + "\"",
				"  if player state is stopped then return \"\"",
				"  set t to current track",
				"  set sep to ASCII character 31",
				"  set art to \"\"",
				app.hasArtworkUrl ? "  try\n    set art to artwork url of t\n  end try" : "",
				"  return (player state as string) & sep & (name of t) & sep & (artist of t) & sep & (album of t) & sep & ("
						+ duration + ") & sep & ((player position * 1000) as integer) & sep & art",
				"end tell");
		String out;
		try {
			out = Processes.run(TIMEOUT, "osascript", "-e", script).trim();
		} catch (IOException e) {
			return null;
		}
		String[] parts = out.split("\u001f", -1);
		if (parts.length < 7) {
			return null;
		}
		return new MediaStatus(app.display, parts[1], parts[2], parts[3], parse(parts[4]), parse(parts[5]),
				parts[0].equals("playing"), parts[6]);
	}

	private static long parse(String s) {
		try {
			return Long.parseLong(s.trim());
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private void tell(String command) {
		App app = lastApp;
		if (app != null) {
			Processes.fire("osascript", "-e", "tell application \"" + app.process + "\" to " + command);
		}
	}

	@Override
	public void playPause() {
		tell("playpause");
	}

	@Override
	public void next() {
		tell("next track");
	}

	@Override
	public void previous() {
		tell("previous track");
	}
}
