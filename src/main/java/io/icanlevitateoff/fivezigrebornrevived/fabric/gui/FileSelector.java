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
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.elements.IFileSelector;
import eu.the5zig.util.Callback;
import net.minecraft.client.gui.GuiGraphics;
import org.apache.commons.io.FilenameUtils;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/** Grid of files/folders; double-click opens a folder or picks a file. */
public class FileSelector implements IFileSelector {

	private static final int BOX_WIDTH = 100, SLOT_HEIGHT = 16, SCROLLBAR_WIDTH = 6;

	private final Callback<File> callback;
	private final List<String> allowedExtensions = new ArrayList<>();
	private final List<File> files = Lists.newArrayList();
	private final int left, right, top, bottom, columnCount;
	private File currentDir;
	private int selectedFile = -1;
	private float scroll;
	private int mouseX, mouseY;
	private long lastClicked;

	public FileSelector(File currentDir, int width, int height, int left, int right, int top, int bottom, Callback<File> callback) {
		this.left = left;
		this.right = right;
		this.top = top;
		this.bottom = bottom;
		this.callback = callback;
		this.columnCount = Math.max(1, (right - left - SCROLLBAR_WIDTH) / BOX_WIDTH);
		updateDir(currentDir);
	}

	@Override
	public File getCurrentDir() {
		return currentDir;
	}

	@Override
	public File getSelectedFile() {
		return selectedFile >= 0 && selectedFile < files.size() ? files.get(selectedFile) : null;
	}

	@Override
	public void goUp() {
		if (currentDir != null) {
			updateDir(currentDir.getParentFile());
		}
	}

	@Override
	public void updateDir(File dir) {
		currentDir = dir;
		files.clear();
		scroll = 0;
		File[] list = dir == null ? File.listRoots() : dir.listFiles();
		if (list != null) {
			for (File file : list) {
				if (dir != null && file.isHidden()) {
					continue;
				}
				if (isExtensionAllowed(file)) {
					files.add(file);
				}
			}
		}
		files.sort(Comparator.comparing((File f) -> !f.isDirectory()).thenComparing(f -> f.getName().toLowerCase()));
		selectedFile = files.isEmpty() ? -1 : 0;
	}

	@Override
	public void setAllowedExtensions(String... extensions) {
		allowedExtensions.addAll(Arrays.asList(extensions));
		updateDir(currentDir);
	}

	private boolean isExtensionAllowed(File file) {
		return allowedExtensions.isEmpty() || file.isDirectory() || allowedExtensions.contains(FilenameUtils.getExtension(file.getName()));
	}

	private String displayName(File file) {
		String name = currentDir == null ? FileSystemView.getFileSystemView().getSystemDisplayName(file) : file.getName();
		if (name == null || name.isEmpty()) {
			name = file.getPath();
		}
		return file.isDirectory() ? "[" + name + "]" : name;
	}

	private int contentHeight() {
		return (int) Math.ceil(files.size() / (double) columnCount) * SLOT_HEIGHT + 8;
	}

	private int maxScroll() {
		return Math.max(0, contentHeight() - (bottom - top));
	}

	private int idAt(int mouseX, int mouseY) {
		if (mouseY < top || mouseY > bottom) {
			return -1;
		}
		int x = mouseX - left;
		int y = mouseY - top + (int) scroll - 4;
		if (x < 0 || y < 0) {
			return -1;
		}
		int idX = x / BOX_WIDTH;
		int id = idX + (y / SLOT_HEIGHT) * columnCount;
		return idX < columnCount && id < files.size() ? id : -1;
	}

	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		double wheel = GuiHandle.consumeScroll();
		if (wheel != 0 && mouseX >= left && mouseX <= right && mouseY >= top && mouseY <= bottom) {
			scroll -= (float) (wheel * SLOT_HEIGHT);
		}
		scroll = Math.max(0, Math.min(scroll, maxScroll()));
		GuiGraphics g = RenderContext.get();
		if (g == null) {
			return;
		}
		g.fill(left, top, right, bottom, 0x80000000);
		g.enableScissor(left, top, right, bottom);
		int hovered = idAt(mouseX, mouseY);
		for (int i = 0; i < files.size(); i++) {
			int x = left + (i % columnCount) * BOX_WIDTH;
			int y = top + 4 + (i / columnCount) * SLOT_HEIGHT - (int) scroll;
			if (y + SLOT_HEIGHT < top || y > bottom) {
				continue;
			}
			if (i == selectedFile) {
				g.fill(x + 1, y - 1, x + BOX_WIDTH - 1, y + SLOT_HEIGHT - 3, 0xFF808080);
				g.fill(x + 2, y, x + BOX_WIDTH - 2, y + SLOT_HEIGHT - 4, 0xFF000000);
			} else if (i == hovered) {
				g.fill(x + 1, y - 1, x + BOX_WIDTH - 1, y + SLOT_HEIGHT - 3, 0x40FFFFFF);
			}
			File file = files.get(i);
			String name = MinecraftFactory.getVars().shortenToWidth(displayName(file), BOX_WIDTH - 8);
			MinecraftFactory.getVars().drawString(name, x + 4, y + 2, file.isDirectory() ? 0xFFFFFFA0 : 0xFFFFFFFF);
		}
		g.disableScissor();
		int max = maxScroll();
		if (max > 0) {
			int barX = right - SCROLLBAR_WIDTH;
			int h = bottom - top;
			int thumb = Math.max(32, Math.min(h - 8, h * h / contentHeight()));
			int thumbY = top + (int) scroll * (h - thumb) / max;
			g.fill(barX, top, right, bottom, 0xFF000000);
			g.fill(barX, thumbY, right, thumbY + thumb, 0xFFC0C0C0);
		}
		if (hovered >= 0) {
			String full = displayName(files.get(hovered));
			if (MinecraftFactory.getVars().getStringWidth(full) > BOX_WIDTH - 8) {
				MinecraftFactory.getVars().getCurrentScreen().drawHoveringText(MinecraftFactory.getVars().splitStringToWidth(full, 200), mouseX, mouseY);
			}
		}
	}

	/** Called by the hosting GUI on a left click. */
	@Override
	public void handleMouseInput() {
		int id = idAt(mouseX, mouseY);
		if (id < 0) {
			return;
		}
		boolean doubleClick = id == selectedFile && System.currentTimeMillis() - lastClicked < 250L;
		selectedFile = id;
		lastClicked = System.currentTimeMillis();
		File file = getSelectedFile();
		if (doubleClick && file != null) {
			if (file.isDirectory()) {
				updateDir(file);
			} else {
				callback.call(file);
			}
		}
	}
}
