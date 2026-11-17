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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Remembers the server being joined, for auto-reconnect, and marks a new connection for join texts. */
@Mixin(ConnectScreen.class)
public abstract class MixinConnectScreen {

	@Inject(method = "startConnecting", at = @At("HEAD"))
	//? if >=1.20.5 {
	private static void the5zig$connect(Screen parent, Minecraft minecraft, ServerAddress address, ServerData data, boolean quickPlay, net.minecraft.client.multiplayer.TransferState transfer, CallbackInfo ci) {
	//?} else {
	/*private static void the5zig$connect(Screen parent, Minecraft minecraft, ServerAddress address, ServerData data, boolean quickPlay, CallbackInfo ci) {
	*///?}
		// Before the init check: a quick-play join at launch must hold off the 5zig network too.
		io.icanlevitateoff.fivezigrebornrevived.mod.chat.network.SessionGate.onGameConnect();
		if (!The5zigMod.hasBeenInitialized()) {
			return;
		}
		io.icanlevitateoff.fivezigrebornrevived.mod.listener.JoinTextListener.onNewConnection();
		String host = address.getHost();
		boolean fiveZig = "5zig.eu".equalsIgnoreCase(host) || "5zig.net".equalsIgnoreCase(host);
		MinecraftFactory.getClassProxyCallback().setAutoreconnectServerData(fiveZig ? null : data);
	}
}
