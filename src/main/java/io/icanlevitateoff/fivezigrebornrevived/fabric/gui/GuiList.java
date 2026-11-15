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

import com.google.common.collect.Lists;
import io.icanlevitateoff.fivezigrebornrevived.fabric.RenderContext;
import io.icanlevitateoff.fivezigrebornrevived.mod.MinecraftFactory;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.Gui;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.elements.Clickable;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.elements.IButton;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.elements.IGuiList;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.elements.Row;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.elements.RowExtended;
import eu.the5zig.util.minecraft.ChatColor;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

/**
 * Scrollable list of core {@link Row}s with variable row heights. Self-contained rather than a
 * vanilla AbstractSelectionList: core owns the rows, the input and the layout.
 */
public class GuiList<E extends Row> implements IGuiList<E> {

	private static final int SCROLLBAR_WIDTH = 6;

	protected final List<E> rows;
	private final Clickable<E> clickable;
	protected final List<Integer> heightMap = Lists.newArrayList();

	private int width, height, top, bottom, left, right;
	private int rowWidth = 95;
	private int headerPadding, bottomPadding;
	private boolean leftbound;
	private int scrollX;
	private boolean drawSelection = true;
	private boolean drawDefaultBackground;
	private Object backgroundTexture;
	private int backgroundWidth, backgroundHeight;
	private String header;

	private float scroll;
	private int selected;
	private long lastClicked;
	private boolean hasSelected;
	private boolean draggingScrollbar;
	private IButton selectedButton;
	protected int mouseX, mouseY;

	public GuiList(Clickable<E> clickable, int width, int height, int top, int bottom, int left, int right, List<E> rows) {
		this.clickable = clickable;
		this.width = width;
		this.height = height;
		this.top = top;
		this.bottom = bottom;
		this.left = left;
		this.right = right;
		this.rows = rows;
	}

	// ---- layout helpers

	protected int rowLeft() {
		return leftbound ? getLeft() + 2 : getLeft() + getWidth() / 2 - getRowWidth() / 2 + 2;
	}

	private int selectionLeft() {
		return leftbound ? getLeft() : getLeft() + getWidth() / 2 - getRowWidth() / 2;
	}

	private int selectionRight() {
		return leftbound ? getLeft() + getRowWidth() : getLeft() + getWidth() / 2 + getRowWidth() / 2;
	}

	/** Screen y of the top of row {@code index}. */
	protected int rowTop(int index) {
		return getTop() + 4 - (int) scroll + heightMap.get(index) + getHeaderPadding();
	}

	protected int getScrollbarPosition() {
		return scrollX > 0 ? scrollX : getWidth() / 2 + 124;
	}

	private int getMaxScroll() {
		return Math.max(0, callGetContentHeight() - (getBottom() - getTop() - 4));
	}

	private boolean isMouseInList(int mouseX, int mouseY) {
		return mouseX >= getLeft() && mouseX <= getRight() && mouseY >= getTop() && mouseY <= getBottom();
	}

	protected boolean isMouseInList() {
		return isMouseInList(mouseX, mouseY);
	}

	// ---- drawing

	@Override
	public void callDrawScreen(int mouseX, int mouseY, float partialTicks) {
		calculateHeightMap();
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		scroll = Math.max(0, Math.min(scroll, getMaxScroll()));
		GuiGraphics g = RenderContext.get();
		if (g == null) {
			return;
		}
		if (backgroundTexture != null) {
			MinecraftFactory.getVars().bindTexture(backgroundTexture);
			Gui.drawModalRectWithCustomSizedTexture(getLeft(), getTop(), 0, 0, getRight() - getLeft(), getBottom() - getTop(), backgroundWidth, backgroundHeight);
		} else if (drawDefaultBackground || MinecraftFactory.getVars().isPlayerNull()) {
			g.fill(getLeft(), getTop(), getRight(), getBottom(), 0x80000000);
		}

		g.enableScissor(getLeft(), getTop(), getRight(), getBottom());
		int x = rowLeft();
		int y = getTop() + 4 - (int) scroll;
		if (header != null) {
			MinecraftFactory.getVars().drawCenteredString(ChatColor.UNDERLINE.toString() + ChatColor.BOLD + header, getLeft() + (getRight() - getLeft()) / 2,
					Math.min(getTop() + 5, y));
		}
		synchronized (rows) {
			for (int i = 0; i < heightMap.size() && i < rows.size(); i++) {
				int rowY = rowTop(i);
				int slotHeight = rows.get(i).getLineHeight() - 4;
				if (rowY > getBottom() || rowY + slotHeight < getTop()) {
					continue;
				}
				if (drawSelection && selected == i) {
					int x1 = selectionLeft(), x2 = selectionRight();
					g.fill(x1, rowY - 2, x2, rowY + slotHeight + 2, 0xFF808080);
					g.fill(x1 + 1, rowY - 1, x2 - 1, rowY + slotHeight + 1, 0xFF000000);
				}
				Row row = rows.get(i);
				row.draw(x, rowY);
				if (row instanceof RowExtended) {
					((RowExtended) row).draw(x, rowY, slotHeight, mouseX, mouseY);
				}
			}
		}
		g.disableScissor();

		int maxScroll = getMaxScroll();
		if (maxScroll > 0) {
			int barX = getScrollbarPosition();
			int listHeight = getBottom() - getTop();
			int thumbHeight = Math.max(32, Math.min(listHeight - 8, listHeight * listHeight / Math.max(1, callGetContentHeight())));
			int thumbY = Math.max(getTop(), (int) scroll * (listHeight - thumbHeight) / maxScroll + getTop());
			g.fill(barX, getTop(), barX + SCROLLBAR_WIDTH, getBottom(), 0xFF000000);
			g.fill(barX, thumbY, barX + SCROLLBAR_WIDTH, thumbY + thumbHeight, 0xFF808080);
			g.fill(barX, thumbY, barX + SCROLLBAR_WIDTH - 1, thumbY + thumbHeight - 1, 0xFFC0C0C0);
		}
		getSelectedRow();
	}

	// ---- input

	@Override
	public void callHandleMouseInput() {
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY) {
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		hasSelected = isMouseInList(mouseX, mouseY);
		int barX = getScrollbarPosition();
		draggingScrollbar = hasSelected && getMaxScroll() > 0 && mouseX >= barX && mouseX < barX + SCROLLBAR_WIDTH;
		if (draggingScrollbar) {
			return;
		}
		E item = getHoverItem(mouseX, mouseY);
		if (item != null) {
			int id;
			synchronized (rows) {
				id = rows.indexOf(item);
			}
			selectItem(id);
		}
		if (!hasSelected) {
			return;
		}
		synchronized (rows) {
			for (int i = 0; i < heightMap.size() && i < rows.size(); i++) {
				int rowY = rowTop(i);
				int slotHeight = rows.get(i).getLineHeight() - 4;
				if (rowY <= getBottom() && rowY + slotHeight >= getTop() && rows.get(i) instanceof RowExtended) {
					IButton pressed = ((RowExtended) rows.get(i)).mousePressed(mouseX, mouseY);
					if (pressed != null) {
						if (selectedButton != null && pressed != selectedButton) {
							selectedButton.mouseClicked(mouseX, mouseY);
						}
						selectedButton = pressed;
						return;
					}
				}
			}
		}
	}

	private void selectItem(int id) {
		int last = selected;
		synchronized (rows) {
			if (id < 0 || id >= rows.size()) {
				return;
			}
			boolean doubleClick = id == last && System.currentTimeMillis() - lastClicked < 250L;
			onSelect(id, rows.get(id), doubleClick);
		}
		lastClicked = System.currentTimeMillis();
	}

	@Override
	public void onSelect(int id, E row, boolean doubleClick) {
		setSelectedId(id);
		if (clickable != null && row != null) {
			clickable.onSelect(id, row, doubleClick);
		}
	}

	@Override
	public void mouseReleased(int mouseX, int mouseY, int state) {
		draggingScrollbar = false;
		if (selectedButton != null && state == 0) {
			selectedButton.callMouseReleased(mouseX, mouseY);
			selectedButton = null;
		}
	}

	@Override
	public boolean callMouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (button != 0 || !draggingScrollbar) {
			return false;
		}
		int listHeight = getBottom() - getTop();
		int thumbHeight = Math.max(32, Math.min(listHeight - 8, listHeight * listHeight / Math.max(1, callGetContentHeight())));
		double ratio = Math.max(1.0, getMaxScroll() / (double) Math.max(1, listHeight - thumbHeight));
		scrollTo((float) (scroll + dragY * ratio));
		return true;
	}

	@Override
	public boolean callMouseScrolled(double amount) {
		if (!isMouseInList(mouseX, mouseY)) {
			return false;
		}
		scrollTo((float) (scroll - amount * 9));
		return true;
	}

	// ---- scrolling / content

	@Override
	public void scrollToBottom() {
		scrollTo(callGetContentHeight());
	}

	@Override
	public float getCurrentScroll() {
		return scroll;
	}

	@Override
	public void scrollTo(float to) {
		scroll = Math.max(0, Math.min(to, getMaxScroll()));
	}

	@Override
	public int callGetContentHeight() {
		int h = bottomPadding + (getHeaderPadding() > 0 ? getHeaderPadding() + 8 : 0);
		for (Row row : Lists.newArrayList(rows)) {
			h += row.getLineHeight();
		}
		return h;
	}

	@Override
	public void calculateHeightMap() {
		heightMap.clear();
		int cur = 0;
		for (Row row : Lists.newArrayList(rows)) {
			heightMap.add(cur);
			cur += row.getLineHeight();
		}
	}

	@Override
	public E getHoverItem(int mouseX, int mouseY) {
		calculateHeightMap();
		if (mouseX < selectionLeft() || mouseX > selectionRight() || !isMouseInList(mouseX, mouseY)) {
			return null;
		}
		synchronized (rows) {
			for (int i = 0; i < heightMap.size() && i < rows.size(); i++) {
				int rowY = rowTop(i);
				if (mouseY >= rowY - 2 && mouseY <= rowY + rows.get(i).getLineHeight() - 2) {
					return rows.get(i);
				}
			}
		}
		return null;
	}

	// ---- selection

	@Override
	public boolean callIsSelected(int id) {
		return selected == id;
	}

	@Override
	public int getSelectedId() {
		synchronized (rows) {
			if (selected < 0 || selected > rows.size()) {
				selected = setSelectedId(0);
			}
		}
		return selected;
	}

	@Override
	public int setSelectedId(int id) {
		synchronized (rows) {
			if (id < 0 || id > rows.size()) {
				id = 0;
			}
		}
		this.selected = id;
		return id;
	}

	@Override
	public E getSelectedRow() {
		synchronized (rows) {
			if (rows.isEmpty()) {
				return null;
			}
			if (selected < 0) {
				selected = 0;
			}
			while (selected >= rows.size()) {
				selected--;
			}
			return rows.get(selected);
		}
	}

	// ---- plain properties

	@Override
	public int callGetRowWidth() {
		return getRowWidth();
	}

	public int getRowWidth() {
		return rowWidth;
	}

	@Override
	public void setRowWidth(int rowWidth) {
		this.rowWidth = rowWidth;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public void setWidth(int width) {
		this.width = width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void setHeight(int height) {
		this.height = height;
	}

	@Override
	public int getHeight(int id) {
		return heightMap.get(id);
	}

	@Override
	public int getTop() {
		return top;
	}

	@Override
	public void setTop(int top) {
		this.top = top;
	}

	@Override
	public int getBottom() {
		return bottom;
	}

	@Override
	public void setBottom(int bottom) {
		this.bottom = bottom;
	}

	@Override
	public int getLeft() {
		return left;
	}

	@Override
	public void setLeft(int left) {
		this.left = left;
	}

	@Override
	public int getRight() {
		return right;
	}

	@Override
	public void setRight(int right) {
		this.right = right;
	}

	@Override
	public int getScrollX() {
		return scrollX;
	}

	@Override
	public void setScrollX(int scrollX) {
		this.scrollX = scrollX;
	}

	@Override
	public boolean isLeftbound() {
		return leftbound;
	}

	@Override
	public void setLeftbound(boolean leftbound) {
		this.leftbound = leftbound;
	}

	@Override
	public boolean isDrawSelection() {
		return drawSelection;
	}

	@Override
	public void setDrawSelection(boolean drawSelection) {
		this.drawSelection = drawSelection;
	}

	@Override
	public int getHeaderPadding() {
		return headerPadding;
	}

	@Override
	public void callSetHeaderPadding(int headerPadding) {
		this.headerPadding = Math.max(0, headerPadding);
	}

	@Override
	public String getHeader() {
		return header;
	}

	@Override
	public void setHeader(String header) {
		this.header = header;
	}

	@Override
	public int getBottomPadding() {
		return bottomPadding;
	}

	@Override
	public void setBottomPadding(int bottomPadding) {
		this.bottomPadding = bottomPadding;
	}

	@Override
	public boolean isDrawDefaultBackground() {
		return drawDefaultBackground;
	}

	@Override
	public void setDrawDefaultBackground(boolean drawDefaultBackground) {
		this.drawDefaultBackground = drawDefaultBackground;
	}

	@Override
	public Object getBackgroundTexture() {
		return backgroundTexture;
	}

	@Override
	public void setBackgroundTexture(Object backgroundTexture, int imageWidth, int imageHeight) {
		this.backgroundTexture = backgroundTexture;
		if (backgroundTexture != null) {
			double w = imageWidth;
			double h = imageHeight;
			int listWidth = getRight() - getLeft();
			int listHeight = getBottom() - getTop();
			while (w > listWidth && h > listHeight) {
				w -= 1;
				h -= h / w;
			}
			while (w < listWidth || h < listHeight) {
				w += 1;
				h += h / w;
			}
			this.backgroundWidth = (int) w;
			this.backgroundHeight = (int) h;
		}
	}

	@Override
	public List<E> getRows() {
		return rows;
	}
}
