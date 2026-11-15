/*
 * Copyright (c) 2019-2020 5zig Reborn
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

package io.icanlevitateoff.fivezigrebornrevived.mod.config;

import eu.the5zig.mod.config.*;

import io.icanlevitateoff.fivezigrebornrevived.mod.The5zigMod;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.elements.Row;
import io.icanlevitateoff.fivezigrebornrevived.util.Utils;

import java.util.regex.Pattern;

public class JoinText implements Row {

	private transient Pattern serverPattern;

	private String server;
	private String message;
	/** Old setting in game ticks; still written (rounded) so older versions keep a sensible delay. */
	private int delay;
	/** Delay in milliseconds. Missing in files written before it existed: then {@link #delay} (ticks) counts. */
	private Integer delayMs;
	/** Wait for the proxy to move you to another server before sending (see JoinTextListener). */
	private boolean waitForSwitch;

	public JoinText() {
	}

	public JoinText(String server, String message, int delayMs) {
		setServer(server);
		this.message = message;
		setDelayMs(delayMs);
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
		if (server == null) {
			serverPattern = null;
		} else {
			try {
				serverPattern = Utils.compileMatchPattern(server);
			} catch (Exception e) {
				The5zigMod.logger.error("Could not compile pattern: " + server + "!", e);
			}
		}
	}

	public Pattern getServerPattern() {
		return serverPattern;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	/** The delay after joining, in milliseconds. */
	public int getDelayMs() {
		return delayMs != null ? delayMs : delay * 50;
	}

	public void setDelayMs(int delayMs) {
		this.delayMs = Math.max(0, delayMs);
		this.delay = (this.delayMs + 49) / 50;
	}

	public boolean isWaitForSwitch() {
		return waitForSwitch;
	}

	public void setWaitForSwitch(boolean waitForSwitch) {
		this.waitForSwitch = waitForSwitch;
	}

	@Override
	public void draw(int x, int y) {
		The5zigMod.getVars().drawString(The5zigMod.getVars().shortenToWidth(message, 100), x + 2, y + 2);
		The5zigMod.getVars().drawString(":", x + 102, y + 2);

		The5zigMod.getVars().drawString(The5zigMod.getVars().shortenToWidth(server, 100), x + 115, y + 2);
		if (waitForSwitch) {
			The5zigMod.getVars().drawString("\u21aa", x + 218, y + 2, 0xAAAAAA);
		}
	}

	@Override
	public int getLineHeight() {
		return 18;
	}
}
