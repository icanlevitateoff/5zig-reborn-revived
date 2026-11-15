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
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

	// Screens moved into Gui in 26.1 (see MixinGui).
	//? if <26 {
	@Inject(method = "setScreen", at = @At("HEAD"))
	private void the5zig$onSetScreen(Screen screen, CallbackInfo ci) {
		if (The5zigMod.hasBeenInitialized()) {
			The5zigMod.getKeybindingManager().flushTextfields();
		}
	}
	//?}

	/** A "5zig Reborn Revived" section in every crash report. */
	@Inject(method = "fillReport(Lnet/minecraft/CrashReport;)Lnet/minecraft/CrashReport;", at = @At("TAIL"))
	private void the5zig$crashReport(net.minecraft.CrashReport report, CallbackInfoReturnable<net.minecraft.CrashReport> cir) {
		net.minecraft.CrashReportCategory category = report.addCategory("5zig Reborn Revived");
		category.setDetail("Version", io.icanlevitateoff.fivezigrebornrevived.mod.Version.VERSION);
		category.setDetail("Initialized", The5zigMod.hasBeenInitialized());
	}

	@Inject(method = "startAttack", at = @At("HEAD"))
	private void the5zig$onLeftClick(CallbackInfoReturnable<Boolean> cir) {
		if (The5zigMod.hasBeenInitialized()) {
			BytecodeHook.onLeftClickMouse();
		}
	}

	@Inject(method = "startUseItem", at = @At("HEAD"))
	private void the5zig$onRightClick(CallbackInfo ci) {
		if (The5zigMod.hasBeenInitialized()) {
			BytecodeHook.onRightClickMouse();
		}
	}
}
