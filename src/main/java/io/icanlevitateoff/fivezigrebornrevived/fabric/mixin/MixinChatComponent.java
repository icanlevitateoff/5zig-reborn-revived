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
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public abstract class MixinChatComponent {

	@Inject(method = "scrollChat", at = @At("HEAD"))
	private void the5zig$scroll(int amount, CallbackInfo ci) {
		if (The5zigMod.hasBeenInitialized()) {
			The5zigMod.getVars().get2ndChat().scroll(amount);
		}
	}

	@Inject(method = "clearMessages", at = @At("HEAD"))
	private void the5zig$clear(boolean clearHistory, CallbackInfo ci) {
		if (The5zigMod.hasBeenInitialized()) {
			The5zigMod.getVars().get2ndChat().clear();
		}
	}

	//? if >=26 {
	/*@ModifyVariable(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V",
			at = @At("HEAD"), argsOnly = true, ordinal = 0)
	*///?} else {
	@ModifyVariable(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
			at = @At("HEAD"), argsOnly = true, ordinal = 0)
	//?}
	private Component the5zig$timePrefix(Component message) {
		if (The5zigMod.hasBeenInitialized() && The5zigMod.getConfig().getBool("chatTimePrefixEnabled")) {
			return (Component) The5zigMod.getDataManager().getChatComponentWithTime(message);
		}
		return message;
	}

	/** Option "transparentChatBackground": no backdrop behind chat lines (only the chat's own read). */
	//? if >=26 {
	/*@com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "extractRenderState(Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;"))
	*///?} else {
	@com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "render",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;"))
	//?}
	private Object the5zig$backgroundOpacity(net.minecraft.client.OptionInstance<?> option, com.llamalad7.mixinextras.injector.wrapoperation.Operation<Object> original) {
		if (The5zigMod.hasBeenInitialized() && The5zigMod.getConfig().getBool("transparentChatBackground")
				&& option == net.minecraft.client.Minecraft.getInstance().options.textBackgroundOpacity()) {
			return 0.0D;
		}
		return original.call(option);
	}

	//? if >=1.20.5 {
	@ModifyConstant(method = {"addMessageToDisplayQueue", "addMessageToQueue"}, constant = @Constant(intValue = 100))
	//?} else {
	/*@ModifyConstant(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;ILnet/minecraft/client/GuiMessageTag;Z)V", constant = @Constant(intValue = 100))
	*///?}
	private int the5zig$maxLines(int vanilla) {
		return The5zigMod.hasBeenInitialized() ? MinecraftFactory.getClassProxyCallback().getMaxChatLines() : vanilla;
	}
}
