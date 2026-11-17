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

package io.icanlevitateoff.fivezigrebornrevived.fabric.integration;

import io.icanlevitateoff.fivezigrebornrevived.mod.gui.GuiSettings;
import net.minecraft.client.gui.screens.Screen;

/** Opens 5zig's own menus from plain Minecraft screens (vanilla Options, Mod Menu, Cloth Config). */
public final class FiveZigScreens {

	private FiveZigScreens() {
	}

	/** 5zig's main settings menu; Done and Escape go back to {@code parent}. */
	public static GuiSettings settings(Screen parent) {
		GuiSettings settings = new GuiSettings(null, "main");
		settings.returnScreen = parent;
		return settings;
	}

	/** {@link #settings(Screen)} as a Minecraft screen, for APIs that want one. */
	public static Screen settingsScreen(Screen parent) {
		return (Screen) settings(parent).getHandle();
	}
}
