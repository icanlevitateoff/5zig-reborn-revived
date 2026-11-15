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
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.elements.ITextfield;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

/** Core text field backed by a vanilla {@link EditBox}, driven entirely by core. */
public class Textfield implements ITextfield {

	protected final EditBox box;
	private final int id;
	private int maxLength;
	private boolean formatterInstalled;
	private boolean password;

	public Textfield(int id, int x, int y, int width, int height, int maxStringLength) {
		this.box = new EditBox(Minecraft.getInstance().font, x, y, width, height, Component.empty());
		this.id = id;
		callSetMaxStringLength(maxStringLength);
	}

	public Textfield(int id, int x, int y, int width, int height) {
		this(id, x, y, width, height, 32);
	}

	public EditBox getHandle() {
		return box;
	}

	@Override
	public int callGetId() {
		return id;
	}

	@Override
	public void setSelected(boolean selected) {
		box.setFocused(selected);
	}

	@Override
	public boolean callIsFocused() {
		return box.isFocused();
	}

	@Override
	public void callSetFocused(boolean focused) {
		setSelected(focused);
	}

	@Override
	public boolean isBackgroundDrawing() {
		return true; // never turned off
	}

	@Override
	public int getX() {
		return box.getX();
	}

	@Override
	public void setX(int x) {
		box.setX(x);
	}

	@Override
	public int getY() {
		return box.getY();
	}

	@Override
	public void setY(int y) {
		box.setY(y);
	}

	@Override
	public int getWidth() {
		return box.getWidth();
	}

	@Override
	public int getHeight() {
		return box.getHeight();
	}

	@Override
	public int callGetMaxStringLength() {
		return maxLength;
	}

	@Override
	public void callSetMaxStringLength(int length) {
		this.maxLength = length;
		box.setMaxLength(length);
	}

	@Override
	public String callGetText() {
		return box.getValue();
	}

	@Override
	public void callSetText(String string) {
		box.setValue(string == null ? "" : string);
	}

	@Override
	public void callMouseClicked(int x, int y, int button) {
		boolean inside = x >= getX() && x < getX() + getWidth() && y >= getY() && y < getY() + getHeight();
		box.setFocused(inside);
		if (inside) {
			//? if >=1.21.9 {
			/*box.mouseClicked(new net.minecraft.client.input.MouseButtonEvent(x, y, new net.minecraft.client.input.MouseButtonInfo(io.icanlevitateoff.fivezigrebornrevived.fabric.compat.Keys.mouseToNative(button), 0)), false);
			*///?} else {
			box.mouseClicked(x, y, button);
			//?}
		}
	}

	@Override
	public boolean callKeyTyped(char character, int key) {
		//? if >=1.21.9 {
		/*return box.isFocused() && character != 0 && box.charTyped(io.icanlevitateoff.fivezigrebornrevived.fabric.compat.Mc.charEvent(character));
		*///?} else {
		return box.isFocused() && character != 0 && box.charTyped(character, 0);
		//?}
	}

	@Override
	public boolean keyPressed(int key, int scanCode, int modifiers) {
		//? if >=1.21.9 {
		/*return box.isFocused() && box.keyPressed(new net.minecraft.client.input.KeyEvent(io.icanlevitateoff.fivezigrebornrevived.fabric.compat.Keys.toNative(key), scanCode, modifiers));
		*///?} else {
		return box.isFocused() && box.keyPressed(key, scanCode, modifiers);
		//?}
	}

	@Override
	public void callTick() {
	}

	@Override
	public void callDraw() {
		GuiGraphics g = RenderContext.get();
		if (g != null) {
			Draw.widget(g, box, -1, -1, 0);
		}
	}

	@Override
	public void setIsPassword(boolean b) {
		this.password = b;
		//? if >=1.21.9 {
		/*// Formatters are a list now; one installed formatter reads the flag.
		if (!formatterInstalled) {
			formatterInstalled = true;
			box.addFormatter((text, offset) -> password ? FormattedCharSequence.forward("*".repeat(text.length()), Style.EMPTY) : null);
		}
		*///?} else {
		if (b) {
			box.setFormatter((text, offset) -> FormattedCharSequence.forward("*".repeat(text.length()), Style.EMPTY));
		} else {
			box.setFormatter((text, offset) -> FormattedCharSequence.forward(text, Style.EMPTY));
		}
		//?}
	}
}
