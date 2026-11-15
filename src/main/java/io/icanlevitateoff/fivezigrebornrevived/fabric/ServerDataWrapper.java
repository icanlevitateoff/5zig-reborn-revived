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
import net.minecraft.client.multiplayer.ServerData;

import java.util.Base64;

public class ServerDataWrapper extends ServerData implements IServerData {

	public ServerDataWrapper(String serverIP) {
		//? if >=1.20.2 {
		super(serverIP, serverIP, Type.OTHER);
		//?} else {
		/*super(serverIP, serverIP, false);
		*///?}
	}

	@Override
	public String getServerName() {
		return name;
	}

	@Override
	public String getServerIP() {
		return ip;
	}

	@Override
	public String getPopulationInfo() {
		return status == null ? "" : ChatComponentBuilder.toLegacy(status);
	}

	@Override
	public String getMOTD() {
		return motd == null ? "" : ChatComponentBuilder.toLegacy(motd);
	}

	@Override
	public long getPing() {
		return ping;
	}

	@Override
	public String getServerIcon() {
		byte[] icon = getIconBytes();
		return icon == null ? null : Base64.getEncoder().encodeToString(icon);
	}
}
