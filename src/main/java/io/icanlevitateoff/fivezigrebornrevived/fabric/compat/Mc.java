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

package io.icanlevitateoff.fivezigrebornrevived.fabric.compat;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Small version-dependent Minecraft accessors. */
public final class Mc {

	private Mc() {
	}

	/** The GLFW window handle. */
	public static long window() {
		//? if >=1.21.9 {
		/*return Minecraft.getInstance().getWindow().handle();
		*///?} else {
		return Minecraft.getInstance().getWindow().getWindow();
		//?}
	}

	public static DynamicTexture texture(String label, NativeImage image) {
		//? if >=1.21.5 {
		return new DynamicTexture(() -> label, image);
		//?} else {
		/*return new DynamicTexture(image);
		*///?}
	}

	public static DynamicTexture texture(String label, int width, int height) {
		//? if >=1.21.5 {
		return new DynamicTexture(() -> label, width, height, true);
		//?} else {
		/*return new DynamicTexture(width, height, true);
		*///?}
	}

	/** Main inventory + hotbar, without armor and offhand. */
	public static List<ItemStack> mainItems(Inventory inventory) {
		//? if >=1.21.5 {
		return inventory.getNonEquipmentItems();
		//?} else {
		/*return inventory.items;
		*///?}
	}

	public static int selectedSlot(Inventory inventory) {
		//? if >=1.21.5 {
		return inventory.getSelectedSlot();
		//?} else {
		/*return inventory.selected;
		*///?}
	}

	public static void setSelectedSlot(Inventory inventory, int slot) {
		//? if >=1.21.5 {
		inventory.setSelectedSlot(slot);
		//?} else {
		/*inventory.selected = slot;
		*///?}
	}

	// ---------------------------------------------------------------- screens / HUD (moved into Gui/Hud in 26.1)

	public static Screen screen() {
		//? if >=26 {
		/*return Minecraft.getInstance().gui.screen();
		*///?} else {
		return Minecraft.getInstance().screen;
		//?}
	}

	public static void setScreen(Screen screen) {
		//? if >=26 {
		/*Minecraft.getInstance().gui.setScreen(screen);
		*///?} else {
		Minecraft.getInstance().setScreen(screen);
		//?}
	}

	public static ChatComponent chat() {
		//? if >=26 {
		/*return Minecraft.getInstance().gui.hud.getChat();
		*///?} else {
		return Minecraft.getInstance().gui.getChat();
		//?}
	}

	/** Adds a client-side line to the vanilla chat. */
	public static void addChatMessage(Component message) {
		//? if >=26 {
		/*chat().addClientSystemMessage(message);
		*///?} else {
		chat().addMessage(message);
		//?}
	}

	public static int guiTicks() {
		//? if >=26 {
		/*return Minecraft.getInstance().gui.hud.getGuiTicks();
		*///?} else {
		return Minecraft.getInstance().gui.getGuiTicks();
		//?}
	}

	/** F1 mode. */
	public static boolean hudHidden() {
		//? if >=26 {
		/*return Minecraft.getInstance().gui.hud.isHidden();
		*///?} else {
		return Minecraft.getInstance().options.hideGui;
		//?}
	}

	/** F3 screen. */
	public static boolean debugScreenShown() {
		//? if >=26 {
		/*return Minecraft.getInstance().getDebugOverlay().showDebugScreen();
		*///?} else if >=1.20.2 {
		return Minecraft.getInstance().gui.getDebugOverlay().showDebugScreen();
		//?} else {
		/*return Minecraft.getInstance().options.renderDebug;
		*///?}
	}

	public static boolean isKeyDown(int key) {
		//? if >=26 {
		/*return key > 0 && com.mojang.blaze3d.platform.InputConstants.isKeyDown(key);
		*///?} else if >=1.21.9 {
		/*return key > 0 && com.mojang.blaze3d.platform.InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), key);
		*///?} else {
		// 0 is how unbound 5zig keys are stored; GLFW reports an error for it on every call.
		return key > 0 && com.mojang.blaze3d.platform.InputConstants.isKeyDown(window(), key);
		//?}
	}

	/** @param button core (GLFW) numbering: 0 left, 1 right, 2 middle */
	public static boolean isMouseDown(int button) {
		//? if >=26 {
		/*net.minecraft.client.MouseHandler mouse = Minecraft.getInstance().mouseHandler;
		return switch (button) {
			case 0 -> mouse.isLeftPressed();
			case 1 -> mouse.isRightPressed();
			case 2 -> mouse.isMiddlePressed();
			default -> false;
		};
		*///?} else {
		return org.lwjgl.glfw.GLFW.glfwGetMouseButton(window(), button) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
		//?}
	}

	// ---------------------------------------------------------------- input events (1.21.9+)

	//? if >=26 {
	/*public static int scancode(net.minecraft.client.input.KeyEvent event) {
		return event.keycode();
	}

	public static net.minecraft.client.input.CharacterEvent charEvent(char c) {
		return new net.minecraft.client.input.CharacterEvent(c);
	}
	*///?} else if >=1.21.9 {
	/*public static int scancode(net.minecraft.client.input.KeyEvent event) {
		return event.scancode();
	}

	public static net.minecraft.client.input.CharacterEvent charEvent(char c) {
		return new net.minecraft.client.input.CharacterEvent(c, 0);
	}
	*///?}
}
