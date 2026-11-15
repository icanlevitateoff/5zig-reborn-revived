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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Windows: reads the "now playing" session that Windows shows in its media flyout (System Media
 * Transport Controls), which Spotify, TIDAL, browsers and most players feed. A Windows PowerShell
 * helper keeps running while the media module is in use and prints one JSON line per second.
 */
final class WindowsMediaSource implements MediaSource {

	private static final String PRELUDE = String.join("\n",
			"$ErrorActionPreference = 'Stop'",
			"[Console]::OutputEncoding = [System.Text.Encoding]::UTF8",
			"Add-Type -AssemblyName System.Runtime.WindowsRuntime",
			"$asTask = ([System.WindowsRuntimeSystemExtensions].GetMethods() | Where-Object { $_.Name -eq 'AsTask' -and $_.GetParameters().Count -eq 1 -and $_.GetParameters()[0].ParameterType.Name -eq 'IAsyncOperation`1' })[0]",
			"function Await($op, [Type]$type) { $t = $asTask.MakeGenericMethod($type).Invoke($null, @($op)); [void]$t.Wait(5000); $t.Result }",
			"$null = [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager, Windows.Media.Control, ContentType = WindowsRuntime]",
			"$null = [Windows.Storage.Streams.DataReader, Windows.Storage.Streams, ContentType = WindowsRuntime]",
			"$mgr = Await ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager]::RequestAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager])",
			"");

	private static final String LOOP = PRELUDE + String.join("\n",
			"$lastKey = $null",
			"while ($true) {",
			"  $out = @{}",
			"  try {",
			"    $s = $mgr.GetCurrentSession()",
			"    if ($s -ne $null) {",
			"      $p = Await ($s.TryGetMediaPropertiesAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionMediaProperties])",
			"      $tl = $s.GetTimelineProperties()",
			"      $info = $s.GetPlaybackInfo()",
			"      $out.player = $s.SourceAppUserModelId",
			"      $out.title = $p.Title",
			"      $out.artist = $p.Artist",
			"      $out.album = $p.AlbumTitle",
			"      $out.length = [long]($tl.EndTime - $tl.StartTime).TotalMilliseconds",
			"      $out.playing = ($info.PlaybackStatus.ToString() -eq 'Playing')",
			"      $pos = $tl.Position.TotalMilliseconds",
			"      if ($out.playing) { $pos += ([DateTimeOffset]::Now - $tl.LastUpdatedTime).TotalMilliseconds }",
			"      $out.position = [long]$pos",
			"      $key = \"$($out.player)|$($out.title)|$($out.artist)|$($out.album)\"",
			"      if ($key -ne $lastKey) {",
			"        $lastKey = $key",
			"        $out.artChanged = $true",
			"        if ($p.Thumbnail -ne $null) {",
			"          try {",
			"            $st = Await ($p.Thumbnail.OpenReadAsync()) ([Windows.Storage.Streams.IRandomAccessStreamWithContentType])",
			"            if ($st.Size -gt 0 -and $st.Size -lt 8MB) {",
			"              $r = [Windows.Storage.Streams.DataReader]::new($st.GetInputStreamAt(0))",
			"              $n = Await ($r.LoadAsync([uint32]$st.Size)) ([uint32])",
			"              $b = New-Object byte[] $n",
			"              $r.ReadBytes($b)",
			"              $out.art = [Convert]::ToBase64String($b)",
			"            }",
			"          } catch { }",
			"        }",
			"      }",
			"    } else { $lastKey = $null }",
			"  } catch { $out = @{ error = $_.Exception.Message } }",
			"  [Console]::Out.WriteLine(($out | ConvertTo-Json -Compress))",
			"  [Console]::Out.Flush()",
			"  Start-Sleep -Milliseconds 1000",
			"}");

	private static String command(String method) {
		return PRELUDE + String.join("\n",
				"$s = $mgr.GetCurrentSession()",
				"if ($s -ne $null) { $null = Await ($s." + method + "()) ([bool]) }");
	}

	private volatile Process helper;
	private volatile BufferedReader helperOut;
	private volatile byte[] pendingArt;
	private volatile boolean artChanged;

	@Override
	public synchronized MediaStatus poll() throws MediaException {
		try {
			if (helper == null || !helper.isAlive()) {
				start();
			}
			BufferedReader out = helperOut;
			String line = null;
			// Take the newest line the helper has printed; wait for one if there is none yet.
			do {
				String next = out.readLine();
				if (next == null) {
					stop();
					throw new MediaException("the PowerShell helper stopped");
				}
				line = next;
			} while (out.ready());
			return parse(line);
		} catch (IOException e) {
			stop();
			throw new MediaException("could not run PowerShell: " + e.getMessage());
		}
	}

	private MediaStatus parse(String line) throws MediaException {
		JsonElement element = JsonParser.parseString(line);
		if (!element.isJsonObject()) {
			return null;
		}
		JsonObject json = element.getAsJsonObject();
		if (json.has("error")) {
			throw new MediaException(str(json, "error"));
		}
		if (!json.has("title")) {
			return null;
		}
		if (json.has("artChanged") && json.get("artChanged").getAsBoolean()) {
			String art = str(json, "art");
			pendingArt = art == null ? null : Base64.getDecoder().decode(art);
			artChanged = true;
		}
		return new MediaStatus(appName(str(json, "player")), str(json, "title"), str(json, "artist"), str(json, "album"),
				num(json, "length"), num(json, "position"), json.has("playing") && json.get("playing").getAsBoolean(), null);
	}

	@Override
	public byte[] pollArt() {
		return pendingArt;
	}

	/** True once after the helper reported new cover art (which may be none). */
	boolean takeArtChanged() {
		boolean changed = artChanged;
		artChanged = false;
		return changed;
	}

	private void start() throws IOException {
		ProcessBuilder builder = new ProcessBuilder("powershell.exe", "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "Bypass",
				"-EncodedCommand", encode(LOOP));
		builder.redirectError(ProcessBuilder.Redirect.DISCARD);
		helper = builder.start();
		helper.getOutputStream().close();
		helperOut = new BufferedReader(new InputStreamReader(helper.getInputStream(), StandardCharsets.UTF_8));
	}

	@Override
	public void stop() {
		// Not synchronized: destroying the helper is what wakes up a poll() blocked on reading it.
		Process process = helper;
		helper = null;
		if (process != null) {
			process.destroyForcibly();
		}
	}

	private void run(String method) {
		Processes.fire("powershell.exe", "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "Bypass",
				"-EncodedCommand", encode(command(method)));
	}

	@Override
	public void playPause() {
		run("TryTogglePlayPauseAsync");
	}

	@Override
	public void next() {
		run("TrySkipNextAsync");
	}

	@Override
	public void previous() {
		run("TrySkipPreviousAsync");
	}

	/** PowerShell's -EncodedCommand takes Base64 of UTF-16LE. */
	private static String encode(String script) {
		return Base64.getEncoder().encodeToString(script.getBytes(StandardCharsets.UTF_16LE));
	}

	/** "Spotify.exe" / "SpotifyAB.SpotifyMusic_zpdnekdrzrea0!Spotify" / "TIDAL.exe" -> readable name. */
	static String appName(String id) {
		if (id == null || id.isEmpty()) {
			return "Media";
		}
		String name = id;
		int bang = name.lastIndexOf('!');
		if (bang >= 0) {
			name = name.substring(bang + 1);
		}
		if (name.toLowerCase(java.util.Locale.ROOT).endsWith(".exe")) {
			name = name.substring(0, name.length() - 4);
		}
		int dot = name.lastIndexOf('.');
		if (dot >= 0 && dot < name.length() - 1) {
			name = name.substring(dot + 1);
		}
		return name;
	}

	private static String str(JsonObject json, String key) {
		JsonElement e = json.get(key);
		return e == null || e.isJsonNull() ? null : e.getAsString();
	}

	private static long num(JsonObject json, String key) {
		JsonElement e = json.get(key);
		try {
			return e == null || e.isJsonNull() ? 0 : e.getAsLong();
		} catch (NumberFormatException ex) {
			return 0;
		}
	}
}
