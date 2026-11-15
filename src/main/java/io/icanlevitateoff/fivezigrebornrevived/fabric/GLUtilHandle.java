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
import io.icanlevitateoff.fivezigrebornrevived.mod.util.IGLUtil;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Maps core's GL calls onto the 2D GUI pose stack of the current {@link GuiGraphics}.
 * Depth, lighting, fog, alpha test and blend-func calls have no equivalent in the
 * 1.21.6+ GUI pipeline (state is fixed per render pipeline) and are ignored.
 */
public class GLUtilHandle implements IGLUtil {

	@Override
	public void enableBlend() {
	}

	@Override
	public void disableBlend() {
	}

	@Override
	public void scale(float x, float y, float z) {
		GuiGraphics g = RenderContext.get();
		if (g != null) {
			Draw.scale(g, x, y);
		}
	}

	@Override
	public void translate(float x, float y, float z) {
		GuiGraphics g = RenderContext.get();
		if (g != null) {
			Draw.translate(g, x, y);
		}
	}

	@Override
	public void rotate(float angle, float x, float y, float z) {
		GuiGraphics g = RenderContext.get();
		if (g != null && z != 0) {
			Draw.rotate(g, z > 0 ? angle : -angle);
		}
	}

	@Override
	public void color(float r, float g, float b, float a) {
		RenderContext.setColor(r, g, b, a);
	}

	@Override
	public void color(float r, float g, float b) {
		RenderContext.setColor(r, g, b, 1f);
	}

	@Override
	public void pushMatrix() {
		RenderContext.pushPose();
	}

	@Override
	public void popMatrix() {
		RenderContext.popPose();
	}

	@Override
	public void enableScissor(int x1, int y1, int x2, int y2) {
		GuiGraphics g = RenderContext.get();
		if (g != null) {
			g.enableScissor(x1, y1, x2, y2);
		}
	}

	@Override
	public void disableScissor() {
		GuiGraphics g = RenderContext.get();
		if (g != null) {
			g.disableScissor();
		}
	}

	@Override
	public void matrixMode(int mode) {
	}

	@Override
	public void loadIdentity() {
	}

	@Override
	public void clear(int i) {
	}

	@Override
	public void disableDepth() {
	}

	@Override
	public void enableDepth() {
	}

	@Override
	public void depthMask(boolean b) {
	}

	@Override
	public void disableLighting() {
	}

	@Override
	public void enableLighting() {
	}

	@Override
	public void disableFog() {
	}

	@Override
	public void tryBlendFuncSeparate(int i, int i1, int i2, int i3) {
	}

	@Override
	public void disableAlpha() {
	}
}
