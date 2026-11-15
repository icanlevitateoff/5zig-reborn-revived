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

/**
 * Key and mouse-button codes as core sees them (GLFW numbering, which every saved 5zig keybinding
 * uses) versus what Minecraft uses natively. Identical until 26.1, where Minecraft moved to SDL3
 * scancodes and SDL mouse-button numbers.
 */
public final class Keys {

	private Keys() {
	}

	//? if >=26 {
	/*// GLFW code, SDL scancode pairs, matched by InputConstants.KEY_* name (generated from 1.21.8 / 26.3).
	private static final int[] PAIRS = {
			32, 44, 39, 52, 44, 54, 45, 45, 46, 55, 47, 56,
			48, 39, 49, 30, 50, 31, 51, 32, 52, 33, 53, 34,
			54, 35, 55, 36, 56, 37, 57, 38, 59, 51, 61, 46,
			65, 4, 66, 5, 67, 6, 68, 7, 69, 8, 70, 9,
			71, 10, 72, 11, 73, 12, 74, 13, 75, 14, 76, 15,
			77, 16, 78, 17, 79, 18, 80, 19, 81, 20, 82, 21,
			83, 22, 84, 23, 85, 24, 86, 25, 87, 26, 88, 27,
			89, 28, 90, 29, 91, 47, 92, 49, 93, 48, 96, 53,
			256, 41, 257, 40, 258, 43, 259, 42, 260, 73, 261, 76,
			262, 79, 263, 80, 264, 81, 265, 82, 266, 75, 267, 78,
			268, 74, 269, 77, 280, 57, 281, 71, 282, 83, 283, 70,
			284, 72, 290, 58, 291, 59, 292, 60, 293, 61, 294, 62,
			295, 63, 296, 64, 297, 65, 298, 66, 299, 67, 300, 68,
			301, 69, 302, 104, 303, 105, 304, 106, 305, 107, 306, 108,
			307, 109, 308, 110, 309, 111, 310, 112, 311, 113, 312, 114,
			313, 115, 320, 98, 321, 89, 322, 90, 323, 91, 324, 92,
			325, 93, 326, 94, 327, 95, 328, 96, 329, 97, 330, 220,
			332, 85, 333, 86, 334, 87, 335, 88, 336, 103, 340, 225, 341, 224,
			342, 226, 344, 229, 345, 228, 346, 230,
			343, 227, 347, 231 // KEY_LWIN/KEY_RWIN became KEY_LGUI/KEY_RGUI
	};
	private static final java.util.Map<Integer, Integer> TO_NATIVE = new java.util.HashMap<>();
	private static final java.util.Map<Integer, Integer> TO_CORE = new java.util.HashMap<>();

	static {
		for (int i = 0; i < PAIRS.length; i += 2) {
			TO_NATIVE.put(PAIRS[i], PAIRS[i + 1]);
			TO_CORE.put(PAIRS[i + 1], PAIRS[i]);
		}
	}

	// "No key" is -1 for GLFW (and core) but scancode 0 for SDL; Minecraft indexes its key-state
	// buffer with the native code, so anything unmapped must become 0, never negative.
	public static int toNative(int coreKey) {
		return TO_NATIVE.getOrDefault(coreKey, 0);
	}

	public static int toCore(int nativeKey) {
		return TO_CORE.getOrDefault(nativeKey, -1);
	}

	// GLFW: 0 left, 1 right, 2 middle. SDL: 1 left, 2 middle, 3 right.
	public static int mouseToNative(int coreButton) {
		return switch (coreButton) {
			case 0 -> 1;
			case 1 -> 3;
			case 2 -> 2;
			default -> coreButton + 1;
		};
	}

	public static int mouseToCore(int nativeButton) {
		return switch (nativeButton) {
			case 1 -> 0;
			case 3 -> 1;
			case 2 -> 2;
			default -> nativeButton - 1;
		};
	}
	*///?} else {
	public static int toNative(int coreKey) {
		return coreKey;
	}

	public static int toCore(int nativeKey) {
		return nativeKey;
	}

	public static int mouseToNative(int coreButton) {
		return coreButton;
	}

	public static int mouseToCore(int nativeButton) {
		return nativeButton;
	}
	//?}
}
