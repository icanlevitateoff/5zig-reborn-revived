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

package io.icanlevitateoff.fivezigrebornrevived.fabric;

import io.icanlevitateoff.fivezigrebornrevived.fabric.mixin.KeyMappingAccessor;
import eu.the5zig.mod.util.IKeybinding;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import io.icanlevitateoff.fivezigrebornrevived.fabric.compat.Ids;
import io.icanlevitateoff.fivezigrebornrevived.fabric.compat.Keys;
import net.minecraft.client.KeyMapping;

import java.util.HashMap;
import java.util.Map;

public class Keybinding extends KeyMapping implements IKeybinding {

	//? if >=1.21.9 {
	/*private static final Map<String, KeyMapping.Category> CATEGORIES = new HashMap<>();

	public Keybinding(String description, int keyCode, String category) {
		super(description, Keys.toNative(keyCode), category(category));
	}

	private static KeyMapping.Category category(String name) {
		return CATEGORIES.computeIfAbsent(name, n -> KeyMapping.Category.register(
				Ids.of("fivezigrebornrevived", n.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9_.-]", "_"))));
	}
	*///?} else {
	public Keybinding(String description, int keyCode, String category) {
		super(description, Keys.toNative(keyCode), category);
		Map<String, Integer> order = KeyMappingAccessor.the5zig$getCategorySortOrder();
		if (!order.containsKey(category)) {
			order.put(category, order.values().stream().mapToInt(Integer::intValue).max().orElse(0) + 1);
		}
	}
	//?}

	@Override
	public boolean callIsPressed() {
		return consumeClick();
	}

	@Override
	public int callGetKeyCode() {
		return Keys.toCore(KeyBindingHelper.getBoundKeyOf(this).getValue());
	}
}
