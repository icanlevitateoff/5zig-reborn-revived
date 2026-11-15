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

package io.icanlevitateoff.fivezigrebornrevived.mod.gui.ts.entries;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.icanlevitateoff.fivezigrebornrevived.mod.The5zigMod;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.Gui;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.elements.Row;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.ts.MovableEntry;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.ts.TeamSpeakIcon;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.ts.rows.TeamSpeakClientAvatarRow;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.ts.rows.TeamSpeakClientGroupListRow;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.ts.rows.TeamSpeakClientStatusRow;
import io.icanlevitateoff.fivezigrebornrevived.mod.render.Base64Renderer;
import io.icanlevitateoff.fivezigrebornrevived.mod.util.Vector2i;
import io.icanlevitateoff.fivezigrebornrevived.teamspeak.TeamSpeak;
import io.icanlevitateoff.fivezigrebornrevived.teamspeak.api.Channel;
import io.icanlevitateoff.fivezigrebornrevived.teamspeak.api.Client;
import io.icanlevitateoff.fivezigrebornrevived.teamspeak.api.Group;
import io.icanlevitateoff.fivezigrebornrevived.teamspeak.api.ServerTab;
import eu.the5zig.util.minecraft.ChatColor;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class GuiTeamSpeakClient extends GuiTeamSpeakEntry implements MovableEntry {

	public static final ImmutableMap<Integer, Vector2i> DEFAULT_GROUP_ICONS = ImmutableMap.of(100, new Vector2i(5, 4), 200, new Vector2i(6, 4), 300, new Vector2i(7, 4), 500,
			new Vector2i(8, 4), 600, new Vector2i(9, 4));

	private final Client client;
	private final List<TeamSpeakIcon> icons;
	private final String serverUniqueId;
	private final List<Integer> defaultIcons;

	public GuiTeamSpeakClient(Client client, String serverUniqueId) {
		this.client = client;
		this.icons = new ArrayList<TeamSpeakIcon>(2 + client.getServerGroups().size());
		this.serverUniqueId = serverUniqueId;
		{
			BufferedImage icon = client.getIcon();
			if (icon != null) {
				icons.add(new TeamSpeakIcon(icon, serverUniqueId + "/icon_" + client.getIconId()));
			}
		}
		this.defaultIcons = Lists.newArrayList();

		List<? extends Group> serverGroups = client.getServerGroups();
		for (int i = serverGroups.size() - 1; i >= 0; i--) {
			Group group = serverGroups.get(i);
			if (DEFAULT_GROUP_ICONS.containsKey(group.getIconId())) {
				defaultIcons.add(group.getIconId());
			} else {
				BufferedImage icon = group.getIcon();
				if (icon != null) {
					icons.add(new TeamSpeakIcon(icon, serverUniqueId + "/icon_" + group.getIconId()));
				}
			}
		}
		Group channelGroup = client.getChannelGroup();
		if (channelGroup != null) {
			if (DEFAULT_GROUP_ICONS.containsKey(channelGroup.getIconId())) {
				defaultIcons.add(channelGroup.getIconId());
			} else {
				BufferedImage icon = channelGroup.getIcon();
				if (icon != null) {
					icons.add(new TeamSpeakIcon(icon, serverUniqueId + "/icon_" + channelGroup.getIconId()));
				}
			}
		}
	}

	@Override
	public void render(int x, int y, int width, int height) {
		String name;
		ServerTab selectedTab = TeamSpeak.getClient().getSelectedTab();
		if (selectedTab != null && client.equals(selectedTab.getSelf())) {
			name = ChatColor.BOLD + client.getDisplayName();
		} else {
			name = client.getDisplayName();
		}

		for (int i = 0; i < icons.size(); i++) {
			TeamSpeakIcon icon = icons.get(i);
			Base64Renderer renderer = Base64Renderer.getRenderer(icon.getIcon(), "ts/" + icon.getId());
			renderer.renderImage(x + width - 11 - 10 * (defaultIcons.size() + i), y + 1, 10, 10);
		}
		if (client.getTalkPower() < client.getChannel().getNeededTalkPower()) {
			width -= 10;
		}
		if (client.isPrioritySpeaker()) {
			width -= 10;
		}

		The5zigMod.getVars().drawString(The5zigMod.getVars().shortenToWidth(name, Math.max(4, width - 1 - 14 - 10 * (icons.size() + defaultIcons.size()))), x + 14, y + 2);
	}

	@Override
	public void renderIcons(int x, int y, int width, int height) {
		drawSpeechBubble(client, x, y);
		for (int i = 0, defaultIconsSize = defaultIcons.size(); i < defaultIconsSize; i++) {
			Integer iconId = defaultIcons.get(i);
			Vector2i uv = DEFAULT_GROUP_ICONS.get(iconId);
			Gui.drawModalRectWithCustomSizedTexture(x + width - 11 - 10 * i, y, uv.getX() * 128 / 12, uv.getY() * 128 / 12, 128 / 12, 128 / 12, 2048 / 12, 2048 / 12);
		}
		if (client.getTalkPower() < client.getChannel().getNeededTalkPower()) {
			Gui.drawModalRectWithCustomSizedTexture(x + width - 1 - 10 - 10 * (icons.size() + defaultIcons.size()), y, 6 * 128 / 12, 5 * 128 / 12, 128 / 12, 128 / 12, 2048 / 12, 2048 / 12);
			width -= 10;
		}
		if (client.isPrioritySpeaker()) {
			Gui.drawModalRectWithCustomSizedTexture(x + width - 1 - 10 - 10 * (icons.size() + defaultIcons.size()), y, 3 * 128 / 12, 1 * 128 / 12, 128 / 12, 128 / 12, 2048 / 12, 2048 / 12);
		}
	}

	@Override
	public int getXOffset() {
		return 20 + getParentChannels(client.getChannel()) * 10;
	}

	@Override
	public void onClick(boolean doubleClick) {
		if (doubleClick) {
			ServerTab selectedTab = TeamSpeak.getClient().getSelectedTab();
			if (selectedTab != null) {
				selectedTab.getPrivateChat(client);
			}
		}
	}

	@Override
	public List<? extends Row> getDescription(int width) {
		return ImmutableList.of(row(width, "client.nickname", client.getName()), new TeamSpeakClientGroupListRow(width, client.getServerGroups(), client.getChannelGroup(), serverUniqueId),
				new TeamSpeakClientStatusRow(width, client), new TeamSpeakClientAvatarRow(width, client));
	}

	@Override
	public void renderDragging(int x, int y, int width, int height) {
		String name;
		if (client.equals(TeamSpeak.getClient().getSelectedTab().getSelf())) {
			name = ChatColor.BOLD + client.getDisplayName();
		} else {
			name = client.getDisplayName();
		}
		The5zigMod.getVars().drawString(The5zigMod.getVars().shortenToWidth(name, Math.max(4, width - 1 - 14 - 10 * (icons.size() + defaultIcons.size()))), x + 14, y + 2);
	}

	@Override
	public void renderDraggingIcons(int x, int y, int width, int height) {
		drawSpeechBubble(client, x, y);
	}

	@Override
	public boolean canBeMovedTo(Channel to, DragLocation location) {
		return !to.equals(client.getChannel());
	}

	@Override
	public void moveEntryTo(Channel to, DragLocation location) {
		client.joinChannel(to);
	}

	public Client getClient() {
		return client;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof GuiTeamSpeakClient && ((GuiTeamSpeakClient) obj).client.equals(client);
	}

	public static void drawSpeechBubble(Client client, int x, int y) {
		int u, v;
		if (client.isMuted()) {
			u = 7;
			v = 5;
		} else if (client.isAway()) {
			u = 10;
			v = 0;
		} else if (!client.hasOutputHardware()) {
			u = 12;
			v = 4;
		} else if (client.isOutputMuted()) {
			u = 8;
			v = 6;
		} else if (!client.hasInputHardware()) {
			u = 11;
			v = 4;
		} else if (client.isInputMuted()) {
			u = 6;
			v = 5;
		} else if (client.isChannelCommander()) {
			if (!client.isTalking()) {
				u = 6;
			} else {
				u = 7;
			}
			v = 7;
		} else if (client.isWhispering()) {
			u = 10;
			v = 7;
		} else if (client.isTalking()) {
			u = 9;
			v = 7;
		} else {
			u = 8;
			v = 7;
		}
		Gui.drawModalRectWithCustomSizedTexture(x, y, u * 128 / 12, v * 128 / 12, 128 / 12, 128 / 12, 2048 / 12, 2048 / 12);
	}
}
