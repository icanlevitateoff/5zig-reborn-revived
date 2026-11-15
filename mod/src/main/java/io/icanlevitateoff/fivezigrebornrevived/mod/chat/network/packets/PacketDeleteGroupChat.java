/*
 * Copyright (c) 2019-2020 5zig Reborn
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

package io.icanlevitateoff.fivezigrebornrevived.mod.chat.network.packets;

import io.icanlevitateoff.fivezigrebornrevived.mod.The5zigMod;
import io.icanlevitateoff.fivezigrebornrevived.mod.chat.entity.Group;
import io.icanlevitateoff.fivezigrebornrevived.mod.chat.network.util.PacketUtil;
import io.netty.buffer.ByteBuf;

import java.io.IOException;

/**
 * Created by 5zig.
 * All rights reserved © 2015
 */
public class PacketDeleteGroupChat implements Packet {

	private int groupId;

	public PacketDeleteGroupChat(int groupId) {
		this.groupId = groupId;
	}

	public PacketDeleteGroupChat() {
	}

	@Override
	public void read(ByteBuf buffer) throws IOException {
		this.groupId = PacketBuffer.readVarIntFromBuffer(buffer);
	}

	@Override
	public void write(ByteBuf buffer) throws IOException {
		PacketBuffer.writeVarIntToBuffer(buffer, groupId);
	}

	@Override
	public void handle() {
		PacketUtil.ensureMainThread(this);
		Group group = The5zigMod.getGroupChatManager().getGroup(groupId);
		if (group == null)
			return;
		The5zigMod.getGroupChatManager().removeGroup(group);
	}
}
