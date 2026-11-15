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

package io.icanlevitateoff.fivezigrebornrevived.util;

/** The current account name in chat filters, highlight words and chat search: %player% or %plr%. */
public final class PlayerPlaceholder {

	public static final String[] TOKENS = {"%player%", "%plr%"};

	private PlayerPlaceholder() {
	}

	public static boolean contains(String text) {
		if (text == null) {
			return false;
		}
		for (String token : TOKENS) {
			if (text.contains(token)) {
				return true;
			}
		}
		return false;
	}

	/** @param name the value to substitute (already escaped if the text is a regex) */
	public static String expand(String text, String name) {
		if (text == null) {
			return null;
		}
		for (String token : TOKENS) {
			text = text.replace(token, name == null ? "" : name);
		}
		return text;
	}
}
