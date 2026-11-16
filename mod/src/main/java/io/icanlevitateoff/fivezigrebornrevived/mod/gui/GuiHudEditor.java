/*
 * Copyright (c) 2026 IcanLevitate
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

package io.icanlevitateoff.fivezigrebornrevived.mod.gui;

import io.icanlevitateoff.fivezigrebornrevived.mod.I18n;
import io.icanlevitateoff.fivezigrebornrevived.mod.The5zigMod;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.elements.IButton;
import io.icanlevitateoff.fivezigrebornrevived.mod.modules.AnchorPoint;
import io.icanlevitateoff.fivezigrebornrevived.mod.modules.Module;
import io.icanlevitateoff.fivezigrebornrevived.mod.modules.ModuleLocation;
import io.icanlevitateoff.fivezigrebornrevived.mod.util.GLUtil;
import io.icanlevitateoff.fivezigrebornrevived.mod.util.Keyboard;
import io.icanlevitateoff.fivezigrebornrevived.mod.util.Mouse;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * HUD layout editor: shows every module that would be on screen, and lets you drag modules
 * around, resize them with the corner handle or the mouse wheel, and open their settings with a
 * right click. Moving a module makes its position custom, anchored to the nearest screen edge
 * or center, so it stays in place when the window size changes. Edges snap to the screen and
 * to other modules (hold Shift to place freely); arrow keys nudge the selected module.
 */
public class GuiHudEditor extends Gui {

	private static final int SNAP = 4;
	private static final int HANDLE = 5;
	private static final float MIN_SCALE = 0.5f, MAX_SCALE = 3.0f, SCALE_STEP = 0.05f;

	/** Box of a module in HUD coordinates (screen / global HUD scale). */
	private static final class Box {
		final Module module;
		int left, top, width, height;
		int renderX, renderY;

		Box(Module module) {
			this.module = module;
		}

		boolean contains(int x, int y) {
			return x >= left && x <= left + width && y >= top && y <= top + height;
		}

		boolean onHandle(int x, int y) {
			return x >= left + width - HANDLE && x <= left + width + 1 && y >= top + height - HANDLE && y <= top + height + 1;
		}
	}

	private final List<Box> boxes = new ArrayList<>();
	private Module selected;
	private Module dragging;
	private boolean resizing;
	private int grabX, grabY;
	private int startLeft, startTop, startWidth;
	private float startScale;
	private int mouseX, mouseY;
	private Integer guideX, guideY;
	private boolean changed;

	public GuiHudEditor(Gui lastScreen) {
		super(lastScreen);
	}

	/**
	 * True while the editor is the current screen; the normal HUD then leaves the modules to it.
	 * (Asked from the screen itself: a "closed" callback does not run for every way of leaving it.)
	 */
	public static boolean isOpen() {
		return The5zigMod.getVars().getCurrentScreen() instanceof GuiHudEditor;
	}

	@Override
	public void initGui() {
		addButton(The5zigMod.getVars().createButton(1, getWidth() / 2 - 105, getHeight() - 26, 100, 20, I18n.translate("hud_editor.modules")));
		addButton(The5zigMod.getVars().createButton(200, getWidth() / 2 + 5, getHeight() - 26, 100, 20, The5zigMod.getVars().translate("gui.done")));
	}

	@Override
	protected void actionPerformed(IButton button) {
		if (button.getId() == 1) {
			save();
			The5zigMod.getVars().displayScreen(new GuiModules(this));
		} else if (button.getId() == 200) {
			save();
			The5zigMod.getVars().displayScreen(lastScreen);
		}
	}

	@Override
	protected void onEscapeType() {
		save();
		The5zigMod.getVars().displayScreen(lastScreen);
	}

	@Override
	protected void guiClosed() {
		save();
	}

	private void save() {
		if (changed) {
			The5zigMod.getModuleMaster().save();
			changed = false;
		}
	}

	private static float hudScale() {
		return The5zigMod.getConfig().getFloat("scale");
	}

	private int hudWidth() {
		return (int) (getWidth() / hudScale());
	}

	private int hudHeight() {
		return (int) (getHeight() / hudScale());
	}

	// ---- layout -------------------------------------------------------------------------------

	private static boolean visible(Module module) {
		if (!module.isEnabled() || module.getMaxWidth(true) <= 0) {
			return false;
		}
		// Server-specific modules only while on that server.
		return module.getServer() == null || module.isShouldRender();
	}

	/** Places every visible module the way the HUD does, using the preview (dummy) contents. */
	private void layout() {
		boxes.clear();
		int width = hudWidth(), height = hudHeight();
		Map<ModuleLocation, Integer> totals = new EnumMap<>(ModuleLocation.class);
		List<Module> modules = new ArrayList<>();
		for (Module module : The5zigMod.getModuleMaster().getModules()) {
			if (visible(module)) {
				modules.add(module);
				if (module.getLocation() != ModuleLocation.CUSTOM) {
					totals.merge(module.getLocation(), module.getTotalHeight(true), Integer::sum);
				}
			}
		}
		Map<ModuleLocation, Integer> offsets = new EnumMap<>(ModuleLocation.class);
		offsets.put(ModuleLocation.TOP_LEFT, 2);
		offsets.put(ModuleLocation.TOP_RIGHT, 2);
		offsets.put(ModuleLocation.BOTTOM_LEFT, height - totals.getOrDefault(ModuleLocation.BOTTOM_LEFT, 0) + 6);
		offsets.put(ModuleLocation.BOTTOM_RIGHT, height - totals.getOrDefault(ModuleLocation.BOTTOM_RIGHT, 0) + 6);
		offsets.put(ModuleLocation.CENTER_LEFT, height / 2 - totals.getOrDefault(ModuleLocation.CENTER_LEFT, 0) / 2);
		offsets.put(ModuleLocation.CENTER_RIGHT, height / 2 - totals.getOrDefault(ModuleLocation.CENTER_RIGHT, 0) / 2);

		for (Module module : modules) {
			Box box = new Box(module);
			box.width = module.getMaxWidth(true);
			box.height = module.getTotalHeight(true) - (int) (6 * module.getScale());
			if (module.getLocation() == ModuleLocation.CUSTOM) {
				AnchorPoint anchor = anchorOf(module);
				box.renderX = (int) (module.getLocationX() * width);
				box.renderY = (int) (module.getLocationY() * height);
				box.left = box.renderX - horizontalPart(anchor) * box.width / 2;
				box.top = box.renderY - verticalPart(anchor) * box.height / 2;
			} else {
				boolean right = module.getLocation() == ModuleLocation.TOP_RIGHT || module.getLocation() == ModuleLocation.CENTER_RIGHT
						|| module.getLocation() == ModuleLocation.BOTTOM_RIGHT;
				int y = offsets.get(module.getLocation());
				offsets.put(module.getLocation(), y + module.getTotalHeight(true));
				box.renderX = right ? width - box.width - 2 : 2;
				box.renderY = y;
				box.left = box.renderX;
				box.top = y;
			}
			boxes.add(box);
		}
	}

	private static AnchorPoint anchorOf(Module module) {
		return module.getAnchorPoint() == null ? AnchorPoint.TOP_LEFT : module.getAnchorPoint();
	}

	/** 0 = left, 1 = center, 2 = right part of the anchor. */
	private static int horizontalPart(AnchorPoint anchor) {
		return anchor.ordinal() % 3;
	}

	/** 0 = top, 1 = center, 2 = bottom part of the anchor. */
	private static int verticalPart(AnchorPoint anchor) {
		return anchor.ordinal() / 3;
	}

	/**
	 * Gives the module a custom position whose box starts at (left, top), anchored to the third
	 * of the screen its center is in, so it keeps its distance to that edge on other screen sizes.
	 */
	private void place(Module module, int width, int height, int left, int top) {
		int screenWidth = hudWidth(), screenHeight = hudHeight();
		int h = Math.min(2, Math.max(0, (left + width / 2) * 3 / Math.max(1, screenWidth)));
		int v = Math.min(2, Math.max(0, (top + height / 2) * 3 / Math.max(1, screenHeight)));
		AnchorPoint anchor = AnchorPoint.values()[v * 3 + h];
		int anchorX = left + h * width / 2;
		int anchorY = top + v * height / 2;
		module.setLocation(ModuleLocation.CUSTOM);
		module.setAnchorPoint(anchor);
		// +0.5 so that (int) (location * size) gives back exactly this pixel.
		module.setLocationX((anchorX + 0.5f) / screenWidth);
		module.setLocationY((anchorY + 0.5f) / screenHeight);
		changed = true;
	}

	private Box boxOf(Module module) {
		for (Box box : boxes) {
			if (box.module == module) {
				return box;
			}
		}
		return null;
	}

	private Box boxAt(int x, int y) {
		// Topmost first: later modules are drawn over earlier ones.
		for (int i = boxes.size() - 1; i >= 0; i--) {
			if (boxes.get(i).contains(x, y)) {
				return boxes.get(i);
			}
		}
		return null;
	}

	// ---- input --------------------------------------------------------------------------------

	@Override
	protected void mouseClicked(int x, int y, int button) {
		if (y >= getHeight() - 28 && Math.abs(x - getWidth() / 2) <= 106) {
			return; // the buttons
		}
		int mx = (int) (x / hudScale()), my = (int) (y / hudScale());
		Box box = boxAt(mx, my);
		if (selected != null) {
			Box current = boxOf(selected);
			if (current != null && current.onHandle(mx, my)) {
				box = current;
			}
		}
		if (box == null) {
			selected = null;
			return;
		}
		selected = box.module;
		if (button == 1) {
			save();
			The5zigMod.getVars().displayScreen(new GuiModuleSettings(this, box.module));
			return;
		}
		if (button != 0) {
			return;
		}
		dragging = box.module;
		resizing = box.onHandle(mx, my);
		grabX = mx - box.left;
		grabY = my - box.top;
		startLeft = box.left;
		startTop = box.top;
		startWidth = Math.max(1, box.width);
		startScale = box.module.getScale();
	}

	@Override
	protected void mouseReleased(int x, int y, int state) {
		dragging = null;
		resizing = false;
		guideX = null;
		guideY = null;
	}

	@Override
	protected void mouseScrolled(double amount) {
		Box box = boxAt(mouseX, mouseY);
		if (box != null && dragging == null) {
			selected = box.module;
			rescale(box, box.module.getScale() + (amount > 0 ? SCALE_STEP : -SCALE_STEP), box.left, box.top);
		}
	}

	@Override
	protected void keyPressed(int key, int scanCode, int modifiers) {
		Box box = selected == null ? null : boxOf(selected);
		if (box == null) {
			return;
		}
		int step = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) ? 10 : 1;
		if (key == Keyboard.KEY_LEFT) {
			place(box.module, box.width, box.height, box.left - step, box.top);
		} else if (key == Keyboard.KEY_RIGHT) {
			place(box.module, box.width, box.height, box.left + step, box.top);
		} else if (key == Keyboard.KEY_UP) {
			place(box.module, box.width, box.height, box.left, box.top - step);
		} else if (key == Keyboard.KEY_DOWN) {
			place(box.module, box.width, box.height, box.left, box.top + step);
		} else if (key == Keyboard.KEY_R) {
			rescale(box, 1.0f, box.left, box.top);
		}
	}

	/** Sets the module's size, keeping its top left corner where it is. */
	private void rescale(Box box, float scale, int left, int top) {
		scale = Math.round(Math.max(MIN_SCALE, Math.min(MAX_SCALE, scale)) / SCALE_STEP) * SCALE_STEP;
		Module module = box.module;
		module.setScale(scale);
		int width = module.getMaxWidth(true);
		int height = module.getTotalHeight(true) - (int) (6 * scale);
		place(module, width, height, left, top);
	}

	// ---- drawing ------------------------------------------------------------------------------

	@Override
	protected void drawScreen(int screenX, int screenY, float partialTicks) {
		if (The5zigMod.getVars().isPlayerNull()) {
			drawMenuBackground();
		}
		The5zigMod.getVars().updateScaledResolution();
		float scale = hudScale();
		mouseX = (int) (screenX / scale);
		mouseY = (int) (screenY / scale);
		int width = hudWidth(), height = hudHeight();

		if (dragging != null && !Mouse.isButtonDown(0)) {
			mouseReleased(screenX, screenY, 0);
		}
		layout();
		if (dragging != null) {
			Box box = boxOf(dragging);
			if (box != null) {
				if (resizing) {
					rescale(box, startScale * (mouseX - startLeft) / (float) startWidth, startLeft, startTop);
				} else {
					move(box, width, height);
				}
				layout();
			}
		}

		GLUtil.pushMatrix();
		GLUtil.scale(scale, scale, scale);
		// Center lines, and the snap guides of the current drag.
		if (dragging != null && !resizing) {
			drawRect(width / 2f - 0.5f, 0, width / 2f + 0.5f, height, 0x22ffffff);
			drawRect(0, height / 2f - 0.5f, width, height / 2f + 0.5f, 0x22ffffff);
			if (guideX != null) {
				drawRect(guideX, 0, guideX + 1, height, 0xaa55aaff);
			}
			if (guideY != null) {
				drawRect(0, guideY, width, guideY + 1, 0xaa55aaff);
			}
		}
		Box hovered = dragging == null ? boxAt(mouseX, mouseY) : boxOf(dragging);
		for (Box box : boxes) {
			boolean isSelected = box.module == selected;
			boolean isHovered = hovered != null && box.module == hovered.module;
			drawRect(box.left - 1, box.top - 1, box.left + box.width + 1, box.top + box.height + 1, isHovered ? 0x40ffffff : 0x20ffffff);
			box.module.render(The5zigMod.getRenderer(), box.renderX, box.renderY, true);
			drawRectOutline(box.left - 1, box.top - 1, box.left + box.width + 1, box.top + box.height + 1,
					isSelected ? 0xff55aaff : isHovered ? 0xddffffff : 0x66ffffff);
			if (isSelected || isHovered) {
				drawRect(box.left + box.width - HANDLE + 1, box.top + box.height - HANDLE + 1, box.left + box.width + 1, box.top + box.height + 1,
						isSelected ? 0xff55aaff : 0xddffffff);
			}
		}
		GLUtil.popMatrix();
		io.icanlevitateoff.fivezigrebornrevived.mod.render.DisplayRenderer.largeTextRenderer.flush();

		if (hovered != null) {
			String info = hovered.module.getName() + "  " + Math.round(hovered.module.getScale() * 100) + "%";
			The5zigMod.getVars().drawString(info, 4, getHeight() - 38, 0xffffff);
		}
		drawCenteredString(I18n.translate("hud_editor.help"), getWidth() / 2, 4, 0xaaaaaa);
	}

	/** Moves the dragged box to the mouse, snapping its edges and center to the screen and other modules. */
	private void move(Box box, int width, int height) {
		int left = mouseX - grabX;
		int top = mouseY - grabY;
		guideX = null;
		guideY = null;
		boolean free = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
		if (!free) {
			List<Integer> xs = new ArrayList<>(), ys = new ArrayList<>();
			xs.add(2);
			xs.add(width / 2);
			xs.add(width - 2);
			ys.add(2);
			ys.add(height / 2);
			ys.add(height - 2);
			for (Box other : boxes) {
				if (other.module == box.module) {
					continue;
				}
				xs.add(other.left);
				xs.add(other.left + other.width);
				xs.add(other.left + other.width / 2);
				ys.add(other.top);
				ys.add(other.top + other.height);
				ys.add(other.top + other.height / 2);
			}
			int[] snapX = snap(left, box.width, xs);
			int[] snapY = snap(top, box.height, ys);
			if (snapX != null) {
				left = snapX[0];
				guideX = snapX[1];
			}
			if (snapY != null) {
				top = snapY[0];
				guideY = snapY[1];
			}
		}
		left = Math.max(0, Math.min(width - box.width, left));
		top = Math.max(0, Math.min(height - box.height, top));
		place(box.module, box.width, box.height, left, top);
	}

	/** Closest snap of the start, center or end of a span to a target: {new start, target}, or null. */
	private static int[] snap(int start, int size, List<Integer> targets) {
		int best = SNAP + 1;
		int[] result = null;
		int[] edges = {0, size / 2, size};
		for (int target : targets) {
			for (int edge : edges) {
				int distance = Math.abs(start + edge - target);
				if (distance < best) {
					best = distance;
					result = new int[]{target - edge, target};
				}
			}
		}
		return result;
	}

	@Override
	public String getTitleName() {
		return "";
	}
}
