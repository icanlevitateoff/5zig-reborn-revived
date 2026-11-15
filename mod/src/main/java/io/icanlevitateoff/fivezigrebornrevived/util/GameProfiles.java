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

import com.mojang.authlib.GameProfile;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.UUID;

/**
 * GameProfile accessors that work on both authlib 6 (getName/getId) and authlib 7, where
 * GameProfile became a record (name/id).
 */
public final class GameProfiles {

	private static final MethodHandle NAME = find(String.class, "name", "getName");
	private static final MethodHandle ID = find(UUID.class, "id", "getId");

	private GameProfiles() {
	}

	public static String name(GameProfile profile) {
		if (profile == null) return null;
		try {
			return (String) NAME.invoke(profile);
		} catch (Throwable t) {
			throw new IllegalStateException(t);
		}
	}

	public static UUID id(GameProfile profile) {
		if (profile == null) return null;
		try {
			return (UUID) ID.invoke(profile);
		} catch (Throwable t) {
			throw new IllegalStateException(t);
		}
	}

	private static MethodHandle find(Class<?> type, String... names) {
		for (String n : names) {
			try {
				return MethodHandles.publicLookup().findVirtual(GameProfile.class, n, MethodType.methodType(type));
			} catch (ReflectiveOperationException ignored) {
			}
		}
		throw new IllegalStateException("No GameProfile accessor among " + String.join(", ", names));
	}
}
