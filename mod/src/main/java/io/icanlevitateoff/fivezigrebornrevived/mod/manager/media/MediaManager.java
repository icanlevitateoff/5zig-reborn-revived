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

import io.icanlevitateoff.fivezigrebornrevived.mod.The5zigMod;
import io.icanlevitateoff.fivezigrebornrevived.util.Utils;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Base64;

/**
 * Shows what any media player is playing (the "Now Playing" module) and lets keybindings
 * play/pause and skip. Linux uses MPRIS (Spotify, tidal-hifi, browsers, ...), Windows the
 * system media session (Spotify, TIDAL, browsers, ...), macOS Spotify and Apple Music.
 *
 * <p>Players are only polled while something asks for the status (the module being drawn), so
 * nothing runs in the background for people who do not use it.
 */
public class MediaManager {

	public static final int COLOR_ACCENT = 0xFF1ED760;
	public static final int COLOR_SECOND = 0xFF404040;
	public static final int COLOR_BACKGROUND = 0xFF282828;

	private static final long POLL_INTERVAL = 1000;
	/** Stop polling (and stop helper processes) this long after the status was last asked for. */
	private static final long IDLE_AFTER = 10_000;
	private static final int ART_SIZE = 128;
	private static final long MAX_ART_BYTES = 8 * 1024 * 1024;

	private final MediaSource source;
	private volatile MediaStatus status;
	private volatile String problem;
	private volatile long lastDemand;
	private volatile String artBase64;
	private volatile String artId;
	private String artKey;
	private int artCounter;
	private Thread thread;

	public MediaManager() {
		switch (Utils.getPlatform()) {
			case WINDOWS:
				source = new WindowsMediaSource();
				break;
			case MAC:
				source = new MacMediaSource();
				break;
			default:
				source = new MprisSource();
				break;
		}
	}

	/** The current status, or null when nothing is playing. Keeps the player polling. */
	public MediaStatus getStatus() {
		demand();
		return status;
	}

	/** Why media cannot be read on this computer, or null. */
	public String getProblem() {
		return problem;
	}

	/** Base64 PNG of the current cover art, or null. */
	public String getArtBase64() {
		return artBase64;
	}

	/** Changes whenever the cover art does. */
	public String getArtId() {
		return artId;
	}

	public void playPause() {
		demand();
		source.playPause();
	}

	public void next() {
		demand();
		source.next();
	}

	public void previous() {
		demand();
		source.previous();
	}

	private synchronized void demand() {
		lastDemand = System.currentTimeMillis();
		if (thread == null || !thread.isAlive()) {
			thread = new Thread(this::run, "5zig Now Playing");
			thread.setDaemon(true);
			thread.start();
		}
	}

	private void run() {
		try {
			while (System.currentTimeMillis() - lastDemand < IDLE_AFTER) {
				long start = System.currentTimeMillis();
				try {
					MediaStatus next = source.poll();
					problem = null;
					status = next;
					updateArt(next);
				} catch (MediaSource.MediaException e) {
					if (problem == null || !problem.equals(e.getMessage())) {
						The5zigMod.logger.info("Now Playing: " + e.getMessage());
					}
					problem = e.getMessage();
					status = null;
					Thread.sleep(5000);
				} catch (RuntimeException e) {
					The5zigMod.logger.warn("Now Playing failed", e);
					status = null;
				}
				long sleep = POLL_INTERVAL - (System.currentTimeMillis() - start);
				if (sleep > 0) {
					Thread.sleep(sleep);
				}
			}
		} catch (InterruptedException ignored) {
		} finally {
			source.stop();
			status = null;
		}
	}

	private void updateArt(MediaStatus next) {
		if (next == null) {
			artKey = null;
			setArt(null);
			return;
		}
		if (source instanceof WindowsMediaSource) {
			if (((WindowsMediaSource) source).takeArtChanged()) {
				setArt(toBase64Png(source.pollArt()));
			}
			return;
		}
		String key = next.artUrl == null ? next.trackKey() : next.artUrl;
		if (key.equals(artKey)) {
			return;
		}
		artKey = key;
		setArt(next.artUrl == null ? null : toBase64Png(load(next.artUrl)));
	}

	private void setArt(String base64) {
		if (base64 == null ? artBase64 == null : base64.equals(artBase64)) {
			return;
		}
		artBase64 = base64;
		artId = base64 == null ? null : "media/art_" + (artCounter++);
	}

	/** Loads cover art from an http(s) or file URL given by the player. */
	private static byte[] load(String url) {
		try {
			URI uri = URI.create(url);
			String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(java.util.Locale.ROOT);
			if (scheme.equals("file")) {
				Path path = Paths.get(uri);
				if (!Files.isRegularFile(path) || Files.size(path) > MAX_ART_BYTES) {
					return null;
				}
				return Files.readAllBytes(path);
			}
			if (!scheme.equals("https") && !scheme.equals("http")) {
				return null;
			}
			HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5))
					.followRedirects(HttpClient.Redirect.NORMAL).build();
			HttpRequest request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(10)).GET().build();
			HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
			if (response.statusCode() != 200) {
				response.body().close();
				return null;
			}
			try (InputStream in = response.body()) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte[] buffer = new byte[8192];
				int n;
				while ((n = in.read(buffer)) != -1) {
					out.write(buffer, 0, n);
					if (out.size() > MAX_ART_BYTES) {
						return null;
					}
				}
				return out.toByteArray();
			}
		} catch (IOException | IllegalArgumentException e) {
			return null;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return null;
		}
	}

	/** Scales the image to the module's size and encodes it the way Base64Renderer expects. */
	private static String toBase64Png(byte[] bytes) {
		if (bytes == null || bytes.length == 0) {
			return null;
		}
		try {
			BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
			if (image == null) {
				return null;
			}
			BufferedImage scaled = new BufferedImage(ART_SIZE, ART_SIZE, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = scaled.createGraphics();
			try {
				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g.drawImage(image, 0, 0, ART_SIZE, ART_SIZE, null);
			} finally {
				g.dispose();
			}
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ImageIO.write(scaled, "PNG", out);
			return Base64.getEncoder().encodeToString(out.toByteArray());
		} catch (IOException | RuntimeException e) {
			return null;
		}
	}
}
