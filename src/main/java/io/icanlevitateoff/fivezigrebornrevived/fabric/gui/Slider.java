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
import io.icanlevitateoff.fivezigrebornrevived.mod.util.SliderCallback;
import io.icanlevitateoff.fivezigrebornrevived.util.Utils;
import net.minecraft.client.gui.GuiGraphics;

public class Slider extends Button {

	private final String str;
	private final SliderCallback sliderItem;
	public float value;
	public boolean dragging;

	public Slider(int id, int x, int y, SliderCallback sliderItem) {
		this(id, x, y, 150, 20, sliderItem);
	}

	public Slider(int id, int x, int y, int width, int height, SliderCallback sliderItem) {
		super(id, x, y, width, height, "");
		this.sliderItem = sliderItem;
		this.str = sliderItem.translate();
		this.value = normalizeValue(sliderItem.get());
		setLabel(str);
	}

	@Override
	public void setLabel(String label) {
		if (sliderItem == null) {
			super.setLabel(label);
			return;
		}
		this.value = normalizeValue(sliderItem.get());
		super.setLabel(label + ": " + getValue());
	}

	@Override
	public void draw(int mouseX, int mouseY) {
		if (!isVisible()) {
			return;
		}
		setHovered(isMouseOver(mouseX, mouseY));
		if (dragging) {
			updateValue(mouseX);
		}
		GuiGraphics g = RenderContext.get();
		if (g == null) {
			return;
		}
		Draw.sliderTrack(g, x, y, width, height);
		Draw.sliderHandle(g, x + (int) (value * (width - 8)), y, 8, height, isHovered() || dragging);
		drawLabel(isEnabled() ? 0xFFFFFFFF : 0xFFA0A0A0);
	}

	private void updateValue(int mouseX) {
		value = Utils.clamp((mouseX - (x + 4)) / (float) (width - 8), 0f, 1f);
		float v = denormalizeValue(value);
		sliderItem.set(v);
		setLabel(str);
	}

	public float denormalizeValue(float value) {
		return snapToStepClamp(sliderItem.getMinValue() + (sliderItem.getMaxValue() - sliderItem.getMinValue()) * Utils.clamp(value, 0.0F, 1.0F));
	}

	public float normalizeValue(float value) {
		return Utils.clamp((snapToStepClamp(value) - sliderItem.getMinValue()) / (sliderItem.getMaxValue() - sliderItem.getMinValue()), 0.0F, 1.0F);
	}

	public float snapToStepClamp(float value) {
		if (sliderItem.getSteps() != -1) {
			value = sliderItem.getSteps() * (float) Math.round(value / sliderItem.getSteps());
		}
		return Utils.clamp(value, sliderItem.getMinValue(), sliderItem.getMaxValue());
	}

	@Override
	public boolean mouseClicked(int mouseX, int mouseY) {
		if (super.mouseClicked(mouseX, mouseY)) {
			updateValue(mouseX);
			dragging = true;
			return true;
		}
		return false;
	}

	@Override
	public void callMouseReleased(int mouseX, int mouseY) {
		if (dragging) {
			playClickSound();
			sliderItem.action();
		}
		dragging = false;
	}

	private String getValue() {
		String customValue = sliderItem.getCustomValue(value);
		if (customValue != null) {
			return customValue;
		}
		return Math.round(value * (sliderItem.getMaxValue() - sliderItem.getMinValue()) + sliderItem.getMinValue()) + sliderItem.getSuffix();
	}
}
