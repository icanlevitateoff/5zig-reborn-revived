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
import eu.the5zig.mod.event.PacketEvent;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Exposes raw packets to plugins through core's PacketEvent. */
@Mixin(Connection.class)
public abstract class MixinConnection {

	//? if >=1.21.6 {
	@Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V", at = @At("HEAD"))
	private void the5zig$send(Packet<?> packet, io.netty.channel.ChannelFutureListener listener, boolean flush, CallbackInfo ci) {
	//?} else if >=1.20.2 {
	/*@Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;Z)V", at = @At("HEAD"))
	private void the5zig$send(Packet<?> packet, net.minecraft.network.PacketSendListener listener, boolean flush, CallbackInfo ci) {
	*///?} else {
	/*@Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V", at = @At("HEAD"))
	private void the5zig$send(Packet<?> packet, net.minecraft.network.PacketSendListener listener, CallbackInfo ci) {
	*///?}
		if (The5zigMod.hasBeenInitialized()) {
			The5zigMod.getListener().fireEvent(new PacketEvent(packet, false));
		}
	}

	@Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"))
	private void the5zig$receive(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
		if (The5zigMod.hasBeenInitialized()) {
			The5zigMod.getListener().fireEvent(new PacketEvent(packet, true));
		}
	}
}
