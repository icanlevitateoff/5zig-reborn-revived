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
package io.icanlevitateoff.fivezigrebornrevived.mod.modules.items.system;

import io.icanlevitateoff.fivezigrebornrevived.mod.I18n;
import io.icanlevitateoff.fivezigrebornrevived.mod.The5zigMod;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.Gui;
import io.icanlevitateoff.fivezigrebornrevived.mod.manager.media.MediaManager;
import io.icanlevitateoff.fivezigrebornrevived.mod.manager.media.MediaStatus;
import eu.the5zig.mod.modules.AbstractModuleItem;
import io.icanlevitateoff.fivezigrebornrevived.mod.render.Base64Renderer;
import eu.the5zig.mod.render.RenderLocation;
import io.icanlevitateoff.fivezigrebornrevived.mod.util.GLUtil;
import io.icanlevitateoff.fivezigrebornrevived.mod.util.ScrollingText;
import io.icanlevitateoff.fivezigrebornrevived.util.Utils;

/**
 * "Now Playing": title, artist, cover and progress of whatever media player is playing
 * (Spotify, TIDAL, browsers, ...). Registered as "SPOTIFY" so existing layouts keep it.
 */
public class NowPlaying extends AbstractModuleItem {

	private static final Base64Renderer base64Renderer = new Base64Renderer(The5zigMod.TRACK_LOCATION, 128, 128);

	static {
		base64Renderer.setInterpolateLinear(The5zigMod.isMinecraftVersionAvailable("1.8"));
	}

	private ScrollingText scrollingTrack;
	private ScrollingText scrollingArtist;

	@Override
	public void registerSettings() {
		getProperties().addSetting("showSpotifyAlways", false);
	}

	@Override
	public void render(int x, int y, RenderLocation renderLocation, boolean dummy) {
		Gui.drawRect(x, y, x + getWidth(dummy), y + getHeight(dummy) - 2, MediaManager.COLOR_BACKGROUND);

		MediaManager media = The5zigMod.getDataManager().getMediaManager();
		MediaStatus status = media != null ? media.getStatus() : null;

		int trackLeft = x;
		int trackTop = y;
		int trackRight = x + getWidth(dummy) - 4;
		int albumSize = getHeight(dummy) - 2;
		// Cover art
		String art = status != null ? media.getArtBase64() : null;
		if (art != null) {
			if (!art.equals(base64Renderer.getBase64String())) {
				base64Renderer.setBase64String(art, media.getArtId());
			}
		} else if (base64Renderer.getBase64String() != null) {
			base64Renderer.reset();
		}
		base64Renderer.renderImage(trackLeft, trackTop, albumSize, albumSize);

		// Track info
		int infoLeft = trackLeft + albumSize + 4;
		int infoTop = trackTop + 3;
		int maxInfoWidth = trackRight - infoLeft;
		String trackName;
		String artistName;
		if (status != null) {
			trackName = status.title;
			artistName = status.artist.isEmpty() ? status.player : status.artist;
		} else if (media != null && media.getProblem() != null) {
			trackName = I18n.translate("modules.item.spotify.unavailable");
			artistName = media.getProblem();
		} else {
			trackName = I18n.translate("modules.item.spotify.nothing_playing");
			artistName = null;
		}

		if (trackName != null && (scrollingTrack == null || !trackName.equals(scrollingTrack.getText()))) {
			scrollingTrack = new ScrollingText(trackName, maxInfoWidth, 8, MediaManager.COLOR_BACKGROUND, 0xaaaaaa);
		} else if (scrollingTrack != null && trackName == null) {
			scrollingTrack = null;
		}
		if (artistName != null && (scrollingArtist == null || !artistName.equals(scrollingArtist.getText()))) {
			scrollingArtist = new ScrollingText(artistName, maxInfoWidth, 6, MediaManager.COLOR_BACKGROUND, 0xaaaaaa);
		} else if (scrollingArtist != null && artistName == null) {
			scrollingArtist = null;
		}
		if (scrollingTrack != null) {
			scrollingTrack.setChild(scrollingArtist);
			scrollingTrack.setScale(getRenderSettings().getScale());
			scrollingTrack.render(infoLeft, infoTop);
		}
		if (scrollingArtist != null) {
			scrollingArtist.setParent(scrollingTrack);
			scrollingArtist.setScale(getRenderSettings().getScale());
			scrollingArtist.render(infoLeft, infoTop + 9);
		}

		// Position
		int positionLineTop = y + getHeight(dummy) - 8;
		int positionLineBottom = positionLineTop + 2;
		int positionLineLeft = infoLeft + 11;
		int positionLineRight = trackRight - 11;
		int positionWidth = positionLineRight - positionLineLeft;
		Gui.drawRect(positionLineLeft, positionLineTop, positionLineRight, positionLineBottom, MediaManager.COLOR_SECOND);
		long trackPosition = status != null ? status.currentPositionMs() : 0;
		long trackLength = status != null ? status.lengthMs : 0;
		double trackPercentage = trackLength > 0 ? Math.min(1.0, (double) trackPosition / trackLength) : 0;
		Gui.drawRect(positionLineLeft, positionLineTop, positionLineLeft + (trackPercentage * positionWidth), positionLineBottom, MediaManager.COLOR_ACCENT);
		drawScaledString(Utils.convertToClock(trackPosition), infoLeft, positionLineTop, 0xaaaaaa, 0.35f);
		drawScaledString(trackLength > 0 ? Utils.convertToClock(trackLength) : "--:--", positionLineRight + 2, positionLineTop, 0xaaaaaa, 0.35f);
	}

	private void drawScaledString(String string, int x, int y, int color, float scale) {
		GLUtil.pushMatrix();
		GLUtil.translate(x, y, 1);
		GLUtil.scale(scale, scale, scale);
		The5zigMod.getVars().drawString(string, 0, 0, color);
		GLUtil.popMatrix();
	}

	@Override
	public boolean shouldRender(boolean dummy) {
		if (dummy) {
			return true;
		}
		MediaManager media = The5zigMod.getDataManager().getMediaManager();
		// Asking for the status keeps the players polled while this module is on screen.
		MediaStatus status = media != null ? media.getStatus() : null;
		return ((Boolean) getProperties().getSetting("showSpotifyAlways").get()) || status != null;
	}

	@Override
	public int getWidth(boolean dummy) {
		return 120;
	}

	@Override
	public int getHeight(boolean dummy) {
		return 28;
	}
}
