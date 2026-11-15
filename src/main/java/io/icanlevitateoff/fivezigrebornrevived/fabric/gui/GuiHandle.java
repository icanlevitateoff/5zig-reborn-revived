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
import io.icanlevitateoff.fivezigrebornrevived.fabric.compat.Keys;
import io.icanlevitateoff.fivezigrebornrevived.fabric.RenderContext;
import io.icanlevitateoff.fivezigrebornrevived.mod.MinecraftFactory;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.Gui;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.IGuiHandle;
import io.icanlevitateoff.fivezigrebornrevived.mod.util.Keyboard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

/**
 * The vanilla screen hosting a core {@link Gui}. Core looks this class up by name and calls the
 * static draw helpers below reflectively ({@code callDrawModalRectWithCustomSizedTexture},
 * {@code drawRect}, {@code drawGradientRect}).
 */
public class GuiHandle extends Screen implements IGuiHandle {

	private static double pendingScroll;

	private final Gui child;

	public GuiHandle(Gui child) {
		super(Component.literal("5zig Reborn Revived"));
		this.child = child;
	}

	public Gui getChild() {
		return child;
	}

	@Override
	protected void init() {
		child.initGui0();
	}

	//? if >=26 {
	/*@Override
	public void extractRenderState(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		draw(graphics, mouseX, mouseY, partialTicks, () -> super.extractRenderState(graphics, mouseX, mouseY, partialTicks));
	}
	*///?} else {
	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		draw(graphics, mouseX, mouseY, partialTicks, () -> super.render(graphics, mouseX, mouseY, partialTicks));
	}
	//?}

	private void draw(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, Runnable vanilla) {
		RenderContext.with(graphics, () -> {
			//? if >=1.21.6 {
			// The background (blur/panorama/dirt) is drawn by Screen#renderWithTooltip before render().
			child.drawScreen0(mouseX, mouseY, partialTicks);
			pendingScroll = 0;
			vanilla.run();
			//?} else if >=1.20.2 {
			/*// Screen#render draws the background itself, so it has to run before the child.
			vanilla.run();
			child.drawScreen0(mouseX, mouseY, partialTicks);
			pendingScroll = 0;
			*///?} else {
			/*renderBackground(graphics);
			vanilla.run();
			child.drawScreen0(mouseX, mouseY, partialTicks);
			pendingScroll = 0;
			*///?}
		});
	}

	@Override
	public void tick() {
		child.tick0();
	}

	//? if >=1.21.9 {
	/*@Override
	public boolean charTyped(net.minecraft.client.input.CharacterEvent event) {
		for (char c : Character.toChars(event.codepoint())) {
			onChar(c);
		}
		return true;
	}

	@Override
	public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
		if (event.key() == com.mojang.blaze3d.platform.InputConstants.KEY_ESCAPE) {
			return super.keyPressed(event);
		}
		onKey(event.key(), io.icanlevitateoff.fivezigrebornrevived.fabric.compat.Mc.scancode(event), event.modifiers());
		return true;
	}

	@Override
	public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
		onClick(event.x(), event.y(), event.button());
		return true;
	}

	@Override
	public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent event) {
		child.mouseReleased0((int) event.x(), (int) event.y(), Keys.mouseToCore(event.button()));
		return true;
	}

	@Override
	public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent event, double dragX, double dragY) {
		child.mouseDragged0(event.x(), event.y(), Keys.mouseToCore(event.button()), dragX, dragY);
		return true;
	}
	*///?} else {
	@Override
	public boolean charTyped(char c, int modifiers) {
		onChar(c);
		return true;
	}

	@Override
	public boolean keyPressed(int key, int scanCode, int modifiers) {
		if (key == com.mojang.blaze3d.platform.InputConstants.KEY_ESCAPE) {
			return super.keyPressed(key, scanCode, modifiers);
		}
		onKey(key, scanCode, modifiers);
		return true;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		onClick(mouseX, mouseY, button);
		return true;
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		child.mouseReleased0((int) mouseX, (int) mouseY, button);
		return true;
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		child.mouseDragged0(mouseX, mouseY, button, dragX, dragY);
		return true;
	}
	//?}

	//? if >=1.20.2 {
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		onScroll(scrollY);
		return true;
	}
	//?} else {
	/*@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
		onScroll(scrollY);
		return true;
	}
	*///?}

	private void onChar(char c) {
		child.keyTyped0(c, 0);
	}

	private void onKey(int nativeKey, int scanCode, int modifiers) {
		int key = Keys.toCore(nativeKey);
		child.keyPressed0(key, scanCode, modifiers);
		if (key == Keyboard.KEY_RETURN || key == Keyboard.KEY_NUMPADENTER || key == Keyboard.KEY_TAB) {
			// Core reacts to these through keyTyped (enter = confirm, tab = next field).
			child.keyTyped0((char) 0, key);
		}
	}

	private void onClick(double mouseX, double mouseY, int nativeButton) {
		int button = Keys.mouseToCore(nativeButton);
		child.mouseClicked0((int) mouseX, (int) mouseY, button);
		if (button == 0) {
			child.handleMouseInput0();
		}
	}

	private void onScroll(double amount) {
		pendingScroll += amount;
		child.mouseScrolled0(amount);
	}

	@Override
	public void onClose() {
		MinecraftFactory.getVars().displayScreen(child.lastScreen);
		child.guiClosed0();
	}

	@Override
	public boolean isPauseScreen() {
		return true;
	}

	/** Runs the click event of a chat style the way vanilla chat does. */
	public static boolean handleStyleClick(Screen screen, net.minecraft.network.chat.Style style) {
		//? if >=26 {
		/*net.minecraft.network.chat.ClickEvent event = style.getClickEvent();
		if (event == null) {
			return false;
		}
		defaultHandleClickEvent(event, Minecraft.getInstance(), screen);
		return true;
		*///?} else {
		return screen.handleComponentClicked(style);
		//?}
	}

	/** Wheel movement since the last call, for widgets that poll instead of receiving events. */
	public static double consumeScroll() {
		double s = pendingScroll;
		pendingScroll = 0;
		return s;
	}

	// ---- IGuiHandle

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void setResolution(int width, int height) {
		this.width = width;
		this.height = height;
	}

	@Override
	public void callDrawDefaultBackground() {
		// Already drawn once per frame by Screen#renderWithTooltip (blur may only run once per frame).
	}

	@Override
	public void drawMenuBackground() {
	}

	@Override
	public void callDrawTexturedModalRect(int x, int y, int texX, int texY, int width, int height) {
		callDrawModalRectWithCustomSizedTexture(x, y, texX, texY, width, height, 256, 256);
	}

	@Override
	public void callDrawHoveringText(List<String> lines, int x, int y) {
		GuiGraphics g = RenderContext.get();
		if (g == null || lines.isEmpty()) {
			return;
		}
		List<FormattedCharSequence> text = new ArrayList<>(lines.size());
		for (String line : lines) {
			text.add(FormattedCharSequence.forward(line, net.minecraft.network.chat.Style.EMPTY));
		}
		Draw.tooltip(g, Minecraft.getInstance().font, text, x, y);
	}

	// ---- static draw helpers used reflectively by core's Gui

	public static void callDrawModalRectWithCustomSizedTexture(int x, int y, float u, float v, int width, int height, float textureWidth, float textureHeight) {
		GuiGraphics g = RenderContext.get();
		ResourceLocation texture = RenderContext.getBoundTexture();
		if (g == null || texture == null) {
			return;
		}
		Draw.blit(g, texture, x, y, u, v, width, height, (int) textureWidth, (int) textureHeight, RenderContext.getColor());
	}

	public static void drawRect(double left, double top, double right, double bottom, int color) {
		GuiGraphics g = RenderContext.get();
		if (g != null) {
			g.fill((int) Math.min(left, right), (int) Math.min(top, bottom), (int) Math.max(left, right), (int) Math.max(top, bottom), color);
		}
	}

	/**
	 * @param vertical false: startColor at the top, endColor at the bottom (vanilla fillGradient);
	 *                 true: startColor on the left, endColor on the right.
	 */
	public static void drawGradientRect(double left, double top, double right, double bottom, int startColor, int endColor, boolean vertical) {
		GuiGraphics g = RenderContext.get();
		if (g == null) {
			return;
		}
		int x1 = (int) Math.min(left, right), x2 = (int) Math.max(left, right);
		int y1 = (int) Math.min(top, bottom), y2 = (int) Math.max(top, bottom);
		if (!vertical) {
			g.fillGradient(x1, y1, x2, y2, startColor, endColor);
			return;
		}
		int w = Math.max(1, x2 - x1);
		for (int i = 0; i < w; i++) {
			g.fill(x1 + i, y1, x1 + i + 1, y2, lerpColor(startColor, endColor, w == 1 ? 0 : i / (float) (w - 1)));
		}
	}

	private static int lerpColor(int a, int b, float t) {
		int r = 0;
		for (int shift = 0; shift < 32; shift += 8) {
			int ca = (a >>> shift) & 0xFF, cb = (b >>> shift) & 0xFF;
			r |= (Math.round(ca + (cb - ca) * t) & 0xFF) << shift;
		}
		return r;
	}
}
