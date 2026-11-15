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

package io.icanlevitateoff.fivezigrebornrevived.fabric.gui;

import io.icanlevitateoff.fivezigrebornrevived.fabric.compat.Draw;
import io.icanlevitateoff.fivezigrebornrevived.fabric.RenderContext;
import io.icanlevitateoff.fivezigrebornrevived.mod.MinecraftFactory;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.elements.IButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

/**
 * Standalone button: core drives drawing, clicks and ticking itself, so this is not a vanilla
 * widget (that would also need focus/narration plumbing core knows nothing about).
 */
public class Button implements IButton {

	private final int id;
	protected int x, y, width, height;
	private String label;
	private boolean enabled = true;
	private boolean visible = true;
	private boolean hovered;
	private int disabledTicks;

	public Button(int id, int x, int y, String label) {
		this(id, x, y, 200, 20, label);
	}

	public Button(int id, int x, int y, String label, boolean enabled) {
		this(id, x, y, 200, 20, label, enabled);
	}

	public Button(int id, int x, int y, int width, int height, String label) {
		this(id, x, y, width, height, label, true);
	}

	public Button(int id, int x, int y, int width, int height, String label, boolean enabled) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.label = label;
		this.enabled = enabled;
	}

	@Override
	public void draw(int mouseX, int mouseY) {
		if (!visible) {
			return;
		}
		hovered = isMouseOver(mouseX, mouseY);
		GuiGraphics g = RenderContext.get();
		if (g == null) {
			return;
		}
		Draw.button(g, x, y, width, height, enabled, hovered);
		renderBg(mouseX, mouseY);
		drawLabel(enabled ? 0xFFFFFFFF : 0xFFA0A0A0);
	}

	/** Hook for subclasses that draw on top of the button background (sliders). */
	protected void renderBg(int mouseX, int mouseY) {
	}

	protected void drawLabel(int color) {
		if (label == null || label.isEmpty()) {
			return;
		}
		String text = MinecraftFactory.getVars().shortenToWidth(label, Math.max(1, width - 6));
		MinecraftFactory.getVars().drawCenteredString(text, x + width / 2, y + (height - 8) / 2, color);
	}

	protected boolean isMouseOver(int mouseX, int mouseY) {
		return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
	}

	@Override
	public boolean mouseClicked(int mouseX, int mouseY) {
		return enabled && visible && isMouseOver(mouseX, mouseY);
	}

	@Override
	public void callMouseReleased(int mouseX, int mouseY) {
	}

	@Override
	public void playClickSound() {
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}

	@Override
	public void tick() {
		if (disabledTicks > 0) {
			disabledTicks--;
			if (disabledTicks == 0) {
				setEnabled(true);
			}
		}
		if (disabledTicks < 0) {
			disabledTicks = 1;
		}
	}

	@Override
	public void setTicksDisabled(int ticks) {
		setEnabled(false);
		disabledTicks = ticks;
	}

	@Override
	public void guiClosed() {
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public int callGetWidth() {
		return width;
	}

	@Override
	public void callSetWidth(int width) {
		this.width = width;
	}

	@Override
	public int callGetHeight() {
		return height;
	}

	@Override
	public void callSetHeight(int height) {
		this.height = height;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	@Override
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	@Override
	public boolean isHovered() {
		return hovered;
	}

	@Override
	public void setHovered(boolean hovered) {
		this.hovered = hovered;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public void setX(int x) {
		this.x = x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public void setY(int y) {
		this.y = y;
	}
}
