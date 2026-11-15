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

package io.icanlevitateoff.fivezigrebornrevived.fabric.net;

//? if >=1.20.5 {
import io.icanlevitateoff.fivezigrebornrevived.fabric.compat.Ids;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * Plugin-message payload carrying raw bytes, so core can keep talking "channel + ByteBuf".
 * Since 1.20.5 every channel needs a registered payload type; 5zig's own channels are registered
 * up front, plugin channels on first use.
 */
public record RawPayload(Type<RawPayload> type, byte[] data) implements CustomPacketPayload {

	private static final Map<ResourceLocation, Type<RawPayload>> TYPES = new ConcurrentHashMap<>();
	private static BiConsumer<String, ByteBuf> receiver = (channel, buf) -> {
	};

	public static void init(BiConsumer<String, ByteBuf> onReceive, String... channels) {
		receiver = onReceive;
		for (String channel : channels) {
			register(Ids.parse(channel));
		}
	}

	private static Type<RawPayload> register(ResourceLocation id) {
		return TYPES.computeIfAbsent(id, key -> {
			Type<RawPayload> type = new Type<>(key);
			StreamCodec<FriendlyByteBuf, RawPayload> codec = StreamCodec.of(
					(buf, payload) -> buf.writeBytes(payload.data()),
					buf -> {
						byte[] bytes = new byte[buf.readableBytes()];
						buf.readBytes(bytes);
						return new RawPayload(type, bytes);
					});
			PayloadTypeRegistry.playC2S().register(type, codec);
			PayloadTypeRegistry.playS2C().register(type, codec);
			ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) ->
					receiver.accept(key.toString(), Unpooled.wrappedBuffer(payload.data())));
			return type;
		});
	}

	/** @return false if the channel could not be registered (e.g. invalid name) or we are not connected */
	public static boolean send(String channel, ByteBuf data) {
		Type<RawPayload> type;
		try {
			type = register(Ids.parse(channel));
		} catch (RuntimeException e) {
			return false;
		}
		byte[] bytes = new byte[data.readableBytes()];
		data.getBytes(data.readerIndex(), bytes);
		if (!ClientPlayNetworking.canSend(type)) {
			return false;
		}
		ClientPlayNetworking.send(new RawPayload(type, bytes));
		return true;
	}
}
//?} else {
/*import io.icanlevitateoff.fivezigrebornrevived.fabric.compat.Ids;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

// Before 1.20.5 plugin messages are plain "channel + bytes", which Fabric exposes directly.
public final class RawPayload {

	private static final Set<ResourceLocation> RECEIVING = ConcurrentHashMap.newKeySet();
	private static BiConsumer<String, ByteBuf> receiver = (channel, buf) -> {
	};

	private RawPayload() {
	}

	public static void init(BiConsumer<String, ByteBuf> onReceive, String... channels) {
		receiver = onReceive;
		for (String channel : channels) {
			listen(Ids.parse(channel));
		}
	}

	private static void listen(ResourceLocation id) {
		if (RECEIVING.add(id)) {
			ClientPlayNetworking.registerGlobalReceiver(id, (client, handler, buf, sender) -> {
				byte[] bytes = new byte[buf.readableBytes()];
				buf.readBytes(bytes);
				ByteBuf copy = Unpooled.wrappedBuffer(bytes);
				client.execute(() -> receiver.accept(id.toString(), copy));
			});
		}
	}

	// Returns false if the channel name is invalid or the server doesn't accept it.
	public static boolean send(String channel, ByteBuf data) {
		ResourceLocation id;
		try {
			id = Ids.parse(channel);
		} catch (RuntimeException e) {
			return false;
		}
		listen(id);
		if (!ClientPlayNetworking.canSend(id)) {
			return false;
		}
		ClientPlayNetworking.send(id, new FriendlyByteBuf(data.copy()));
		return true;
	}
}
*///?}
