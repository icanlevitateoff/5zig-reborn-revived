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

import io.icanlevitateoff.fivezigrebornrevived.mod.BytecodeHook;
import io.icanlevitateoff.fivezigrebornrevived.mod.The5zigMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public abstract class MixinScreen {

	/** Option: no dimmed/blurred world behind in-game screens. */
	@Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
	//? if >=1.20.2 {
	private void the5zig$worldBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
	//?} else {
	/*private void the5zig$worldBackground(GuiGraphics graphics, CallbackInfo ci) {
	*///?}
		if (The5zigMod.hasBeenInitialized() && Minecraft.getInstance().level != null && !The5zigMod.shouldDrawWorldBackground()) {
			ci.cancel();
		}
	}

	//? if <1.21.9 {
	@Inject(method = "hasControlDown", at = @At("HEAD"), cancellable = true)
	private static void the5zig$ctrl(CallbackInfoReturnable<Boolean> cir) {
		if (The5zigMod.hasBeenInitialized()) {
			cir.setReturnValue(BytecodeHook.isCtrlKeyDown(Minecraft.ON_OSX));
		}
	}
	//?}
}
