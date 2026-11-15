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

import io.icanlevitateoff.fivezigrebornrevived.fabric.WrappedItemStack;
import io.icanlevitateoff.fivezigrebornrevived.fabric.util.ChatComponentBuilder;
import io.icanlevitateoff.fivezigrebornrevived.mod.BytecodeHook;
import io.icanlevitateoff.fivezigrebornrevived.mod.The5zigMod;
import io.icanlevitateoff.fivezigrebornrevived.mod.util.TabList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Packet hooks. All inject at TAIL (or just after the thread hop): these handlers first bounce from
 * the netty thread to the client thread, and only the client-thread call should reach core.
 */
@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener {

	//? if >=1.21.9 {
	/*private static final String SAME_THREAD = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/PacketProcessor;)V";
	*///?} else {
	private static final String SAME_THREAD = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/util/thread/BlockableEventLoop;)V";
	//?}

	/** A new "join game" packet: the first join, or a proxy (Velocity/BungeeCord) moving you to another server. */
	@Inject(method = "handleLogin", at = @At("TAIL"))
	private void the5zig$login(CallbackInfo ci) {
		if (The5zigMod.hasBeenInitialized()) {
			// Never let the mod break joining: an exception here makes vanilla drop the connection.
			try {
				The5zigMod.getListener().onJoinGame();
			} catch (Throwable t) {
				The5zigMod.logger.error("Error while handling the join", t);
			}
			io.icanlevitateoff.fivezigrebornrevived.mod.listener.JoinTextListener.onServerSwitch();
		}
	}

	@Inject(method = "handleChangeDifficulty", at = @At("TAIL"))
	private void the5zig$difficulty(ClientboundChangeDifficultyPacket packet, CallbackInfo ci) {
		if (The5zigMod.hasBeenInitialized()) {
			The5zigMod.getListener().handleServerDifficulty();
		}
	}

	@Inject(method = "handleTabListCustomisation", at = @At("TAIL"))
	private void the5zig$tabList(ClientboundTabListPacket packet, CallbackInfo ci) {
		if (The5zigMod.hasBeenInitialized()) {
			//? if >=1.20.5 {
			The5zigMod.getListener().onPlayerListHeaderFooter(new TabList(
					ChatComponentBuilder.toLegacy(packet.header()), ChatComponentBuilder.toLegacy(packet.footer())));
			//?} else {
			/*The5zigMod.getListener().onPlayerListHeaderFooter(new TabList(
					ChatComponentBuilder.toLegacy(packet.getHeader()), ChatComponentBuilder.toLegacy(packet.getFooter())));
			*///?}
		}
	}

	@Inject(method = "handleContainerSetSlot", at = @At("TAIL"))
	private void the5zig$setSlot(ClientboundContainerSetSlotPacket packet, CallbackInfo ci) {
		if (The5zigMod.hasBeenInitialized()) {
			BytecodeHook.onSetSlot(packet.getSlot(), packet.getItem().isEmpty() ? null : new WrappedItemStack(packet.getItem()));
		}
	}

	@Inject(method = "setActionBarText", at = @At(value = "INVOKE", target = SAME_THREAD, shift = At.Shift.AFTER), cancellable = true)
	private void the5zig$actionBar(ClientboundSetActionBarTextPacket packet, CallbackInfo ci) {
		//? if >=1.20.5 {
		if (The5zigMod.hasBeenInitialized() && The5zigMod.getListener().onActionBar(ChatComponentBuilder.toLegacy(packet.text()))) {
		//?} else {
		/*if (The5zigMod.hasBeenInitialized() && The5zigMod.getListener().onActionBar(ChatComponentBuilder.toLegacy(packet.getText()))) {
		*///?}
			ci.cancel();
		}
	}

	@Inject(method = "setTitleText", at = @At("TAIL"))
	private void the5zig$title(ClientboundSetTitleTextPacket packet, CallbackInfo ci) {
		if (The5zigMod.hasBeenInitialized()) {
			//? if >=1.20.5 {
			The5zigMod.getListener().onTitle(ChatComponentBuilder.toLegacy(packet.text()), null);
			//?} else {
			/*The5zigMod.getListener().onTitle(ChatComponentBuilder.toLegacy(packet.getText()), null);
			*///?}
		}
	}

	@Inject(method = "setSubtitleText", at = @At("TAIL"))
	private void the5zig$subtitle(ClientboundSetSubtitleTextPacket packet, CallbackInfo ci) {
		if (The5zigMod.hasBeenInitialized()) {
			//? if >=1.20.5 {
			The5zigMod.getListener().onTitle(null, ChatComponentBuilder.toLegacy(packet.text()));
			//?} else {
			/*The5zigMod.getListener().onTitle(null, ChatComponentBuilder.toLegacy(packet.getText()));
			*///?}
		}
	}

	@Inject(method = "handleTitlesClear", at = @At("TAIL"))
	private void the5zig$clearTitles(ClientboundClearTitlesPacket packet, CallbackInfo ci) {
		if (The5zigMod.hasBeenInitialized()) {
			The5zigMod.getListener().onTitle(null, null);
		}
	}

	@Inject(method = "handleMovePlayer", at = @At("TAIL"))
	private void the5zig$teleport(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (The5zigMod.hasBeenInitialized() && player != null) {
			BytecodeHook.onTeleport(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
		}
	}
}
