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

import io.icanlevitateoff.fivezigrebornrevived.fabric.compat.Draw;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * Core draws in the old immediate-mode style ("bind a texture, set a colour, draw"), with no
 * graphics object passed around. Since 1.21.6 all GUI drawing goes through a {@link GuiGraphics}
 * that only exists during a render call, so the version layer keeps the current one (plus the
 * emulated bind/colour state) here while 5zig code is drawing.
 */
public final class RenderContext {

	private static GuiGraphics graphics;
	private static ResourceLocation boundTexture;
	private static int color = 0xFFFFFFFF;
	private static int poseDepth;

	private RenderContext() {
	}

	/** @return the graphics of the render call in progress, or null if 5zig code runs outside one. */
	public static GuiGraphics get() {
		return graphics;
	}

	/** Runs {@code action} with {@code g} as the current graphics, restoring the previous state afterwards. */
	public static void with(GuiGraphics g, Runnable action) {
		GuiGraphics previous = graphics;
		int previousDepth = poseDepth;
		graphics = g;
		poseDepth = 0;
		try {
			action.run();
		} finally {
			// Unbalanced pushMatrix() calls in core must not leak into vanilla rendering.
			while (poseDepth > 0) {
				Draw.pop(g);
				poseDepth--;
			}
			graphics = previous;
			poseDepth = previousDepth;
		}
	}

	public static ResourceLocation getBoundTexture() {
		return boundTexture;
	}

	public static void bindTexture(ResourceLocation texture) {
		boundTexture = texture;
	}

	/** ARGB tint applied to textured draws (emulates GlStateManager.color). */
	public static int getColor() {
		return color;
	}

	public static void setColor(float r, float g, float b, float a) {
		color = (clamp(a) << 24) | (clamp(r) << 16) | (clamp(g) << 8) | clamp(b);
	}

	private static int clamp(float f) {
		return Math.max(0, Math.min(255, Math.round(f * 255f)));
	}

	static void pushPose() {
		if (graphics != null) {
			Draw.push(graphics);
			poseDepth++;
		}
	}

	static void popPose() {
		if (graphics != null && poseDepth > 0) {
			Draw.pop(graphics);
			poseDepth--;
		}
	}
}
