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

import io.icanlevitateoff.fivezigrebornrevived.mod.gui.elements.Row;
import io.icanlevitateoff.fivezigrebornrevived.mod.util.Display;
import io.icanlevitateoff.fivezigrebornrevived.mod.util.GuiListChatCallback;
import io.icanlevitateoff.fivezigrebornrevived.mod.util.Mouse;

import java.util.List;

public class GuiListChat<E extends Row> extends GuiList<E> {

	private final GuiListChatCallback callback;

	public GuiListChat(int width, int height, int top, int bottom, int left, int right, int scrollx, List<E> rows, GuiListChatCallback callback) {
		super(null, width, height, top, bottom, left, right, rows);
		this.callback = callback;
		setBottomPadding(6);
		setLeftbound(true);
		setDrawSelection(false);
		setScrollX(scrollx);
		setDrawDefaultBackground(callback.drawDefaultBackground());
		setBackgroundTexture(callback.getResourceLocation(), callback.getImageWidth(), callback.getImageHeight());
	}

	@Override
	public int getRowWidth() {
		return getRight() - getLeft() - 5;
	}

	@Override
	public void callHandleMouseInput() {
		if (!Display.isActive() || !isMouseInList() || !Mouse.isButtonDown(0)) {
			return;
		}
		int y = mouseY - getTop() - getHeaderPadding() + (int) getCurrentScroll() - 4;
		synchronized (rows) {
			for (int i = 0; i < heightMap.size() && i < rows.size(); i++) {
				int rowStart = heightMap.get(i);
				if (y >= rowStart - 2 && y <= rowStart + rows.get(i).getLineHeight() - 2) {
					callback.chatLineClicked(rows.get(i), mouseX, y, rowStart, getLeft());
					return;
				}
			}
		}
	}
}
