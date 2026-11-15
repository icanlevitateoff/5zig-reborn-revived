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

package io.icanlevitateoff.fivezigrebornrevived.fabric.compat;

import net.minecraft.resources.ResourceLocation;

/** Resource location construction across versions (constructor before 1.21, factories after). */
public final class Ids {

	private Ids() {
	}

	public static ResourceLocation of(String namespace, String path) {
		//? if >=1.21 {
		return ResourceLocation.fromNamespaceAndPath(namespace, path);
		//?} else {
		/*return new ResourceLocation(namespace, path);
		*///?}
	}

	public static ResourceLocation vanilla(String path) {
		return of("minecraft", path);
	}

	public static ResourceLocation parse(String id) {
		//? if >=1.21 {
		return ResourceLocation.parse(id);
		//?} else {
		/*return new ResourceLocation(id);
		*///?}
	}
}
