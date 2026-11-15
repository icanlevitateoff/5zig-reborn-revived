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

package io.icanlevitateoff.fivezigrebornrevived.mod.gui.ts.menu;

import io.icanlevitateoff.fivezigrebornrevived.mod.I18n;
import io.icanlevitateoff.fivezigrebornrevived.mod.The5zigMod;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.GuiYesNo;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.YesNoCallback;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.ts.GuiTeamSpeakCreateChannel;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.ts.GuiTeamSpeakEditChannel;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.ts.entries.GuiTeamSpeakChannel;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.ts.entries.GuiTeamSpeakEntry;
import io.icanlevitateoff.fivezigrebornrevived.mod.util.Vector2i;
import io.icanlevitateoff.fivezigrebornrevived.teamspeak.TeamSpeak;
import io.icanlevitateoff.fivezigrebornrevived.teamspeak.api.Channel;
import io.icanlevitateoff.fivezigrebornrevived.teamspeak.api.ServerTab;

public class GuiTeamSpeakContextMenuChannel extends GuiTeamSpeakContextMenu {

	public GuiTeamSpeakContextMenuChannel() {
		entries.add(new GuiTeamSpeakContextMenuEntry(new Vector2i(5, 2), "channel.join") {
			@Override
			public void onClick(GuiTeamSpeakEntry entry) {
				ServerTab selectedTab = TeamSpeak.getClient().getSelectedTab();
				if (selectedTab != null && selectedTab.getSelf() != null) {
					selectedTab.getSelf().joinChannel(((GuiTeamSpeakChannel) entry).getChannel());
				}
			}
		});
		entries.add(new GuiTeamSpeakContextMenuEntry(new Vector2i(13, 1), "channel.edit") {
			@Override
			public void onClick(GuiTeamSpeakEntry entry) {
				The5zigMod.getVars().displayScreen(new GuiTeamSpeakEditChannel(The5zigMod.getVars().getCurrentScreen(), ((GuiTeamSpeakChannel) entry).getChannel()));
			}
		});
		entries.add(new GuiTeamSpeakContextMenuEntry(new Vector2i(12, 1), "channel.delete") {
			@Override
			public void onClick(final GuiTeamSpeakEntry entry) {
				The5zigMod.getVars().displayScreen(new GuiYesNo(The5zigMod.getVars().getCurrentScreen(), new YesNoCallback() {
					@Override
					public void onDone(boolean yes) {
						if (yes) {
							ServerTab selectedTab = TeamSpeak.getClient().getSelectedTab();
							if (selectedTab == null) {
								return;
							}
							selectedTab.deleteChannel(((GuiTeamSpeakChannel) entry).getChannel(), true);
						}
					}

					@Override
					public String title() {
						return I18n.translate("teamspeak.menu.channel.delete.confirm");
					}
				}));
			}
		});
		entries.add(new GuiTeamSpeakContextMenuEntry(new Vector2i(10, 1), "channel.create_subchannel") {
			@Override
			public void onClick(GuiTeamSpeakEntry entry) {
				final ServerTab selectedTab = TeamSpeak.getClient().getSelectedTab();
				if (selectedTab == null) {
					return;
				}
				final Channel parent = ((GuiTeamSpeakChannel) entry).getChannel();
				The5zigMod.getVars().displayScreen(new GuiTeamSpeakCreateChannel(The5zigMod.getVars().getCurrentScreen(), parent) {
					@Override
					protected void onDone(ChannelResult result) {
						selectedTab.createChannel(result.name, result.password, result.topic, result.description, result.lifespan, result.defaultChannel, result.parentChannel,
								result.orderChannel, result.bottomPosition, result.neededTalkPower, result.codec, result.codecQuality, result.maxClients);
					}
				});
			}
		});
		entries.add(new GuiTeamSpeakContextMenuEntry(new Vector2i(9, 1), "server.create_channel") {
			@Override
			public void onClick(GuiTeamSpeakEntry entry) {
				final ServerTab selectedTab = TeamSpeak.getClient().getSelectedTab();
				if (selectedTab == null) {
					return;
				}
				The5zigMod.getVars().displayScreen(new GuiTeamSpeakCreateChannel(The5zigMod.getVars().getCurrentScreen(), null) {
					@Override
					protected void onDone(ChannelResult result) {
						selectedTab.createChannel(result.name, result.password, result.topic, result.description, result.lifespan, result.defaultChannel, result.parentChannel,
								result.orderChannel, result.bottomPosition, result.neededTalkPower, result.codec, result.codecQuality, result.maxClients);
					}
				});
			}
		});
	}
}
