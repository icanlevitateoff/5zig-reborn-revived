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

package io.icanlevitateoff.fivezigrebornrevived.fabric.mixin;

import io.icanlevitateoff.fivezigrebornrevived.mod.MinecraftFactory;
import io.icanlevitateoff.fivezigrebornrevived.mod.The5zigMod;
import net.minecraft.client.KeyboardHandler;
import io.icanlevitateoff.fivezigrebornrevived.fabric.compat.Mc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Feeds raw key presses to core's key handling (replaces the old GLFW key callback wrapper). */
@Mixin(KeyboardHandler.class)
public abstract class MixinKeyboardHandler {

	//? if >=1.21.9 {
	/*@Inject(method = "keyPress", at = @At("HEAD"))
	private void the5zig$onKeyPress(long window, int action, net.minecraft.client.input.KeyEvent event, CallbackInfo ci) {
		the5zig$key(window, event.key(), action);
	}
	*///?} else {
	@Inject(method = "keyPress", at = @At("HEAD"))
	private void the5zig$onKeyPress(long window, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
		the5zig$key(window, key, action);
	}
	//?}

	private void the5zig$key(long window, int key, int action) {
		if (action == com.mojang.blaze3d.platform.InputConstants.PRESS && window == Mc.window() && The5zigMod.hasBeenInitialized()) {
			MinecraftFactory.getClassProxyCallback().fireKeyPressEvent(io.icanlevitateoff.fivezigrebornrevived.fabric.compat.Keys.toCore(key));
		}
	}
}
