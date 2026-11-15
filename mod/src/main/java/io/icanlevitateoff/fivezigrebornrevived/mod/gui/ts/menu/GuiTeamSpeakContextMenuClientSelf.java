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
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.CenteredTextfieldCallback;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.GuiCenteredTextfield;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.ts.GuiTeamSpeakClientChannelGroups;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.ts.GuiTeamSpeakClientServerGroups;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.ts.entries.GuiTeamSpeakClient;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.ts.entries.GuiTeamSpeakEntry;
import io.icanlevitateoff.fivezigrebornrevived.mod.util.Vector2i;
import io.icanlevitateoff.fivezigrebornrevived.teamspeak.TeamSpeak;
import io.icanlevitateoff.fivezigrebornrevived.teamspeak.api.Client;
import io.icanlevitateoff.fivezigrebornrevived.teamspeak.api.ServerTab;

public class GuiTeamSpeakContextMenuClientSelf extends GuiTeamSpeakContextMenu {

	public GuiTeamSpeakContextMenuClientSelf() {
		entries.add(new GuiTeamSpeakContextMenuEntry(new Vector2i(4, 1), "client.change_nickname") {
			@Override
			public void onClick(GuiTeamSpeakEntry entry) {
				final ServerTab selectedTab = TeamSpeak.getClient().getSelectedTab();
				if (selectedTab != null && selectedTab.getSelf() != null) {
					The5zigMod.getVars().displayScreen(new GuiCenteredTextfield(The5zigMod.getVars().getCurrentScreen(), new CenteredTextfieldCallback() {
						@Override
						public void onDone(String text) {
							if (text != null) {
								selectedTab.getSelf().setNickName(text);
							}
						}

						@Override
						public String title() {
							return I18n.translate("teamspeak.enter_nickname");
						}
					}, selectedTab.getSelf().getName()));
				}
			}
		});
		entries.add(new GuiTeamSpeakContextMenuEntry(new Vector2i(12, 6), "client.server_groups") {
			@Override
			public void onClick(GuiTeamSpeakEntry entry) {
				final ServerTab selectedTab = TeamSpeak.getClient().getSelectedTab();
				if (selectedTab != null) {
					Client client = ((GuiTeamSpeakClient) entry).getClient();
					The5zigMod.getVars().displayScreen(new GuiTeamSpeakClientServerGroups(The5zigMod.getVars().getCurrentScreen(), client));
				}
			}
		});
		entries.add(new GuiTeamSpeakContextMenuEntry(new Vector2i(9, 6), "client.channel_group") {
			@Override
			public void onClick(GuiTeamSpeakEntry entry) {
				final ServerTab selectedTab = TeamSpeak.getClient().getSelectedTab();
				if (selectedTab != null) {
					Client client = ((GuiTeamSpeakClient) entry).getClient();
					The5zigMod.getVars().displayScreen(new GuiTeamSpeakClientChannelGroups(The5zigMod.getVars().getCurrentScreen(), client));
				}
			}
		});
	}
}
