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

import io.icanlevitateoff.fivezigrebornrevived.mod.The5zigMod;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Before 1.21.6 the vanilla potion indicator can't be swapped through Fabric's HUD API; from 26.1
 * on, screens are set through Gui instead of Minecraft.
 */
@Mixin(Gui.class)
public abstract class MixinGui {

	//? if >=26 {
	/*@org.spongepowered.asm.mixin.injection.Inject(method = "setScreen", at = @org.spongepowered.asm.mixin.injection.At("HEAD"))
	private void the5zig$onSetScreen(net.minecraft.client.gui.screens.Screen screen, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
		if (The5zigMod.hasBeenInitialized()) {
			The5zigMod.getKeybindingManager().flushTextfields();
		}
	}
	*///?} else if <1.21.6 {
	/*@org.spongepowered.asm.mixin.injection.Inject(method = "renderEffects", at = @org.spongepowered.asm.mixin.injection.At("HEAD"), cancellable = true)
	private void the5zig$hideVanillaEffects(org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
		if (The5zigMod.hasBeenInitialized() && !The5zigMod.getConfig().getBool("showVanillaPotionIndicator")) {
			ci.cancel();
		}
	}
	*///?}
}
