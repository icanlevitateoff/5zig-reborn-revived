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

import io.icanlevitateoff.fivezigrebornrevived.fabric.RenderContext;
import io.icanlevitateoff.fivezigrebornrevived.mod.The5zigMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class MixinChatScreen {

	//? if >=26 {
	/*@Inject(method = "extractRenderState", at = @At("HEAD"))
	*///?} else {
	@Inject(method = "render", at = @At("HEAD"))
	//?}
	private void the5zig$render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
		if (The5zigMod.hasBeenInitialized()) {
			RenderContext.with(graphics, () -> The5zigMod.getVars().get2ndChat().drawComponentHover(mouseX, mouseY));
		}
	}

	//? if >=1.21.9 {
	/*@Inject(method = "keyPressed", at = @At("HEAD"))
	private void the5zig$keyPressed(net.minecraft.client.input.KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
		the5zig$key(event.key());
	}

	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private void the5zig$mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
		the5zig$click(event.x(), event.y(), io.icanlevitateoff.fivezigrebornrevived.fabric.compat.Keys.mouseToCore(event.button()), cir);
	}
	*///?} else {
	@Inject(method = "keyPressed", at = @At("HEAD"))
	private void the5zig$keyPressed(int key, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
		the5zig$key(key);
	}

	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private void the5zig$mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		the5zig$click(mouseX, mouseY, button, cir);
	}
	//?}

	private void the5zig$key(int key) {
		if (The5zigMod.hasBeenInitialized()) {
			The5zigMod.getVars().get2ndChat().keyTyped(io.icanlevitateoff.fivezigrebornrevived.fabric.compat.Keys.toCore(key));
		}
	}

	private void the5zig$click(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		if (!The5zigMod.hasBeenInitialized()) {
			return;
		}
		if (The5zigMod.getRenderer().getChatSymbolsRenderer().mouseClicked((int) mouseX, (int) mouseY)
				|| The5zigMod.getVars().get2ndChat().mouseClicked((int) mouseX, (int) mouseY, button)) {
			cir.setReturnValue(true);
		}
	}
}
