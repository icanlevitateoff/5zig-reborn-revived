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

import com.llamalad7.mixinextras.sugar.Local;
import eu.the5zig.util.minecraft.ChatColor;
import io.icanlevitateoff.fivezigrebornrevived.fabric.compat.Draw;
import io.icanlevitateoff.fivezigrebornrevived.mod.The5zigMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/**
 * Option "ping_on_serverlist": the server list shows the ping in ms instead of the signal bars.
 * The bars are hidden by zeroing their draw size, the number is drawn in their place, and the
 * player count is moved left to make room. Overload-agnostic (@ModifyArgs on name-only targets), so
 * one mixin covers every GUI drawing API.
 */
@Mixin(ServerSelectionList.OnlineServerEntry.class)
public abstract class MixinServerListEntry {

	@Shadow
	@Final
	private ServerData serverData;

	@Unique
	private GuiGraphics the5zig$graphics;
	@Unique
	private String the5zig$ping;
	@Unique
	private int the5zig$shift;

	//? if >=26 {
	/*@Inject(method = "extractContent", at = @At("HEAD"))
	*///?} else if >=1.21.9 {
	/*@Inject(method = "renderContent", at = @At("HEAD"))
	*///?} else {
	@Inject(method = "render", at = @At("HEAD"))
	//?}
	private void the5zig$prepare(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics graphics) {
		the5zig$graphics = graphics;
		the5zig$ping = null;
		the5zig$shift = 0;
		if (!The5zigMod.hasBeenInitialized() || !The5zigMod.getConfig().getBool("ping_on_serverlist") || serverData.ping < 0) {
			return;
		}
		//? if >=1.20.5 {
		boolean ok = serverData.state() == ServerData.State.SUCCESSFUL;
		//?} else {
		/*boolean ok = serverData.pinged;
		*///?}
		if (ok) {
			the5zig$ping = serverData.ping + "ms";
			// The bars are 10px wide; anything wider pushes the player count left.
			the5zig$shift = Math.max(0, Minecraft.getInstance().font.width(the5zig$ping) - 10);
		}
	}

	//? if >=26 {
	/*@ModifyArgs(method = "extractContent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite"))
	*///?} else if >=1.21.9 {
	/*@ModifyArgs(method = "renderContent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite"))
	*///?} else if >=1.20.2 {
	@ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite"))
	//?} else {
	/*@ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit"))
	*///?}
	private void the5zig$replaceBars(Args args) {
		if (the5zig$ping == null || the5zig$graphics == null) {
			return;
		}
		int i = 0;
		while (i < args.size() && !(args.get(i) instanceof ResourceLocation)) {
			i++;
		}
		if (i >= args.size()) {
			return;
		}
		ResourceLocation id = args.get(i);
		int x = args.<Integer>get(i + 1);
		int y = args.<Integer>get(i + 2);
		//? if >=1.20.2 {
		if (!id.getPath().startsWith("server_list/ping_")) {
			return;
		}
		args.set(i + 3, 0);
		args.set(i + 4, 0);
		//?} else {
		/*// blit(icons.png, x, y, u, v, w, h, texW, texH): the signal bars live at u=0, v=176..208.
		float u = args.<Float>get(i + 3);
		float v = args.<Float>get(i + 4);
		if (!id.getPath().equals("textures/gui/icons.png") || u != 0 || v < 176 || v >= 216) {
			return;
		}
		args.set(i + 5, 0);
		args.set(i + 6, 0);
		*///?}
		int width = Minecraft.getInstance().font.width(the5zig$ping);
		Draw.text(the5zig$graphics, Minecraft.getInstance().font, the5zig$ping, x + 10 - width, y, 0xFF000000 | ChatColor.getPingColor(serverData.ping), true);
	}

	//? if >=26 {
	/*@ModifyArgs(method = "extractContent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;text"))
	*///?} else if >=1.21.9 {
	/*@ModifyArgs(method = "renderContent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString"))
	*///?} else {
	@ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString"))
	//?}
	private void the5zig$shiftPlayerCount(Args args) {
		// The only Component drawn on the first line is the player count (name is a String, MOTD
		// lines are FormattedCharSequences).
		if (the5zig$shift > 0 && args.get(1) instanceof Component) {
			args.set(2, args.<Integer>get(2) - the5zig$shift);
		}
	}
}
