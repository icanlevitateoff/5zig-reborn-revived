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

package io.icanlevitateoff.fivezigrebornrevived.fabric;

import io.icanlevitateoff.fivezigrebornrevived.fabric.util.ChatComponentBuilder;
import io.icanlevitateoff.fivezigrebornrevived.mod.util.IServerData;
import io.icanlevitateoff.fivezigrebornrevived.mod.util.IServerPinger;
import eu.the5zig.util.minecraft.ChatColor;
import net.minecraft.client.multiplayer.ServerStatusPinger;

import java.net.UnknownHostException;

public class ServerPinger implements IServerPinger {

	private final ServerStatusPinger handle = new ServerStatusPinger();

	@Override
	public IServerData createServerData(String serverIP) {
		return new ServerDataWrapper(serverIP);
	}

	@Override
	public void ping(IServerData iServerData) {
		ServerDataWrapper serverData = (ServerDataWrapper) iServerData;
		try {
			//? if >=26 {
			/*handle.pingServer(serverData, () -> {
			}, () -> {
			}, net.minecraft.server.network.EventLoopGroupHolder.remote(net.minecraft.client.Minecraft.getInstance().options.useNativeTransport()));
			*///?} else if >=1.20.5 {
			handle.pingServer(serverData, () -> {
			}, () -> {
			});
			//?} else {
			/*handle.pingServer(serverData, () -> {
			});
			*///?}
		} catch (UnknownHostException e) {
			fail(serverData, "Can't resolve hostname");
		} catch (Exception e) {
			fail(serverData, "Can't connect to server");
		}
	}

	private static void fail(ServerDataWrapper serverData, String message) {
		serverData.ping = -1;
		serverData.motd = ChatComponentBuilder.fromLegacyText(ChatColor.DARK_RED + message);
	}

	@Override
	public void callPingPendingNetworks() {
		handle.tick();
	}

	@Override
	public void callClearPendingNetworks() {
		handle.removeAll();
	}
}
