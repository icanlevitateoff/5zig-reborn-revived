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

import io.icanlevitateoff.fivezigrebornrevived.fabric.compat.Ids;
import eu.the5zig.mod.util.IResourceLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * {@link ResourceLocation} is final since 1.21, so core's {@link IResourceLocation} is a wrapper
 * around one instead of a subclass.
 */
public final class IdWrapper implements IResourceLocation {

	private final ResourceLocation handle;

	public IdWrapper(ResourceLocation handle) {
		this.handle = handle;
	}

	public static IdWrapper of(String path) {
		return new IdWrapper(Ids.parse(path));
	}

	public static IdWrapper of(String namespace, String path) {
		return new IdWrapper(Ids.of(namespace, path));
	}

	/** Accepts a wrapper, a vanilla location, or null. */
	public static ResourceLocation unwrap(Object o) {
		if (o instanceof IdWrapper w) {
			return w.handle;
		}
		if (o instanceof ResourceLocation r) {
			return r;
		}
		return null;
	}

	public ResourceLocation handle() {
		return handle;
	}

	@Override
	public String callGetResourcePath() {
		return handle.getPath();
	}

	@Override
	public String callGetResourceDomain() {
		return handle.getNamespace();
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof IdWrapper w && w.handle.equals(handle);
	}

	@Override
	public int hashCode() {
		return handle.hashCode();
	}

	@Override
	public String toString() {
		return handle.toString();
	}
}
