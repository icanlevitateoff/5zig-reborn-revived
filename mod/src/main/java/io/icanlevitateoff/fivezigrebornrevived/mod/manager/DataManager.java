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

package io.icanlevitateoff.fivezigrebornrevived.mod.manager;

import io.icanlevitateoff.fivezigrebornrevived.util.GameProfiles;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import io.icanlevitateoff.fivezigrebornrevived.mod.The5zigMod;
import io.icanlevitateoff.fivezigrebornrevived.mod.chat.ChatBackgroundManager;
import io.icanlevitateoff.fivezigrebornrevived.mod.chat.NetworkStats;
import io.icanlevitateoff.fivezigrebornrevived.mod.chat.entity.Friend;
import io.icanlevitateoff.fivezigrebornrevived.mod.chat.entity.Profile;
import io.icanlevitateoff.fivezigrebornrevived.mod.chat.entity.Rank;
import io.icanlevitateoff.fivezigrebornrevived.mod.chat.network.filetransfer.FileTransferManager;
import io.icanlevitateoff.fivezigrebornrevived.mod.chat.network.packets.PacketFriendStatus;
import io.icanlevitateoff.fivezigrebornrevived.mod.config.items.SelectColorItem;
import eu.the5zig.mod.event.ServerQuitEvent;
import io.icanlevitateoff.fivezigrebornrevived.mod.listener.ChatSearchManager;
import io.icanlevitateoff.fivezigrebornrevived.mod.listener.CrossHairDistanceListener;
import io.icanlevitateoff.fivezigrebornrevived.mod.listener.TeamSpeakListener;
import io.icanlevitateoff.fivezigrebornrevived.mod.listener.TeamSpeakReconnectListener;
import io.icanlevitateoff.fivezigrebornrevived.mod.manager.einslive.EinsLiveManager;
import io.icanlevitateoff.fivezigrebornrevived.mod.manager.iloveradio.ILoveRadioManager;
import io.icanlevitateoff.fivezigrebornrevived.mod.manager.itunes.ITunesManager;
import io.icanlevitateoff.fivezigrebornrevived.mod.manager.keyboard.KeyboardManager;
import io.icanlevitateoff.fivezigrebornrevived.mod.manager.media.MediaManager;
import io.icanlevitateoff.fivezigrebornrevived.mod.render.BracketsFormatting;
import io.icanlevitateoff.fivezigrebornrevived.mod.render.SnowRenderer;
import io.icanlevitateoff.fivezigrebornrevived.mod.server.GameServer;
import io.icanlevitateoff.fivezigrebornrevived.mod.server.Server;
import eu.the5zig.mod.server.ServerInstance;
import io.icanlevitateoff.fivezigrebornrevived.mod.server.ServerInstanceRegistry;
import io.icanlevitateoff.fivezigrebornrevived.mod.server.bergwerk.ServerInstanceBergwerk;
import io.icanlevitateoff.fivezigrebornrevived.mod.server.cytooxien.ServerInstanceCytooxien;
import io.icanlevitateoff.fivezigrebornrevived.mod.server.gomme.ServerInstanceGommeHD;
import io.icanlevitateoff.fivezigrebornrevived.mod.server.hypixel.ServerInstanceHypixel;
import io.icanlevitateoff.fivezigrebornrevived.mod.server.mineplex.ServerInstanceMineplex;
import io.icanlevitateoff.fivezigrebornrevived.mod.server.octc.ServerInstanceOCC;
import io.icanlevitateoff.fivezigrebornrevived.mod.server.playminity.ServerInstancePlayMinity;
import io.icanlevitateoff.fivezigrebornrevived.mod.server.simplehg.ServerInstanceSimpleHG;
import io.icanlevitateoff.fivezigrebornrevived.mod.server.timolia.ServerInstanceTimolia;
import io.icanlevitateoff.fivezigrebornrevived.mod.server.venicraft.ServerInstanceVenicraft;
import io.icanlevitateoff.fivezigrebornrevived.mod.util.TabList;
import io.icanlevitateoff.fivezigrebornrevived.teamspeak.TeamSpeak;
import io.icanlevitateoff.fivezigrebornrevived.teamspeak.event.TeamSpeakEventDispatcher;
import io.icanlevitateoff.fivezigrebornrevived.util.Utils;
import eu.the5zig.util.minecraft.ChatColor;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class DataManager {

	public TabList tabList;

	private Server server;
	private Profile profile;
	private final String session = The5zigMod.getVars().getSession();
	private final String username = The5zigMod.getVars().getUsername();
	private final UUID uuid;
	private final GameProfile gameProfile;

	private CoordinateClipboard coordinatesClipboard = new CoordinateClipboard(null);
	private DeathLocation deathLocation = null;

	// Network
	private final NetworkStats networkStats = new NetworkStats();
	private final ChatTypingManager chatTypingManager = new ChatTypingManager();
	private final AFKManager afkManager = new AFKManager();
	private final ChatBackgroundManager chatBackgroundManager = new ChatBackgroundManager();

	private final FPSCalculator fpsCalculator = new FPSCalculator();
	private final CPSManager cpsManager = new CPSManager();
	private final SpeedCalculator speedCalculator = new SpeedCalculator();

	private final FileTransferManager fileTransferManager = new FileTransferManager();

	private final AutoReconnectManager autoReconnectManager = new AutoReconnectManager();
	private final SearchManager searchManager = new SearchManager();
	private final CrossHairDistanceListener crossHairDistanceListener = new CrossHairDistanceListener();
	private final WeatherManager weatherManager = new WeatherManager();
	private final SnowRenderer snowRenderer = new SnowRenderer();

	private final ServerInstanceRegistry serverInstanceRegistry = new ServerInstanceRegistry();

	private final MediaManager mediaManager = new MediaManager();
	private final ILoveRadioManager iLoveRadioManager = new ILoveRadioManager();
	private final EinsLiveManager einsLiveManager = new EinsLiveManager();
	private final ITunesManager iTunesManager = new ITunesManager();
	private final KeyboardManager keyboardManager = new KeyboardManager();
	private final TimerManager timerManager = new TimerManager();
	private final ChatSearchManager chatSearchManager = new ChatSearchManager();

	private final ServerSettings serverSettings = new ServerSettings();

	private boolean tsRequiresAuth = false;
	private final TeamSpeakReconnectListener teamSpeakReconnectListener;

	/** Everything this instance registered with the event listener, removed again by {@link #close()}. */
	private List<Object> registeredListeners = Collections.emptyList();
	private TeamSpeakListener teamSpeakListener;

	/** Creates a DataManager and remembers the event listeners it (and its managers) registered. */
	public static DataManager create() {
		DataManager[] created = new DataManager[1];
		List<Object> registered = The5zigMod.getListener().recordRegistrations(() -> created[0] = new DataManager());
		created[0].registeredListeners = registered;
		return created[0];
	}

	/**
	 * Unregisters everything this instance registered. Called when an account switch replaces it;
	 * without this every manager (AFK, timers, search, server stats...) ran twice after a switch.
	 */
	public void close() {
		for (Object listener : registeredListeners) {
			try {
				The5zigMod.getListener().unregisterListener(listener);
			} catch (RuntimeException e) {
				The5zigMod.logger.warn("Could not unregister " + listener.getClass().getName(), e);
			}
		}
		registeredListeners = Collections.emptyList();
		if (teamSpeakListener != null) {
			TeamSpeakEventDispatcher.unregisterListener(teamSpeakListener);
			TeamSpeak.getClient().removeDisconnectListener(teamSpeakListener);
			teamSpeakListener = null;
		}
	}

	private DataManager() {
		gameProfile = The5zigMod.getVars().getGameProfile();
		UUID id = GameProfiles.id(gameProfile);
		uuid = id == null ? UUID.randomUUID() : id;
		profile = new Profile(0, Lists.newArrayList(Rank.USER), System.currentTimeMillis(), "Hey there, I'm using " + The5zigMod.NAME + "!", Friend.OnlineStatus.ONLINE, true, true, true, false, true, ChatColor.RESET);

		registerServerInstance(new ServerInstanceTimolia());
		registerServerInstance(new ServerInstanceGommeHD());
		registerServerInstance(new ServerInstancePlayMinity());
		registerServerInstance(new ServerInstanceBergwerk());
		registerServerInstance(new ServerInstanceMineplex());
		registerServerInstance(new ServerInstanceHypixel());
		registerServerInstance(new ServerInstanceVenicraft());
		registerServerInstance(new ServerInstanceCytooxien());
		registerServerInstance(new ServerInstanceSimpleHG());
		registerServerInstance(new ServerInstanceOCC());

		The5zigMod.getListener().registerListener(teamSpeakReconnectListener = new TeamSpeakReconnectListener());
		teamSpeakListener = new TeamSpeakListener();
		TeamSpeakEventDispatcher.registerListener(teamSpeakListener);
		TeamSpeak.getClient().addDisconnectListener(teamSpeakListener);
		TeamSpeak.setDebugMode(The5zigMod.DEBUG);
		TeamSpeak.getClient().setAutoReconnect(false);
	}

	public UUID getUniqueId() {
		return uuid;
	}

	public String getColoredName() {
		return profile.getRank().get(0).getColorCode() + username;
	}

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public String getSession() {
		return session;
	}

	public String getUsername() {
		return username;
	}

	public String getUniqueIdWithoutDashes() {
		return Utils.getUUIDWithoutDashes(uuid);
	}

	public GameProfile getGameProfile() {
		return gameProfile;
	}

	public void resetServer() {
		The5zigMod.getListener().fireEvent(new ServerQuitEvent());
		server = null;
		tabList = null;
		deathLocation = null;
		The5zigMod.getServerAPIBackend().reset();
		The5zigMod.getVars().resetServer();
		The5zigMod.getVars().get2ndChat().clear();
		if (The5zigMod.getNetworkManager().isConnected()) {
			The5zigMod.getNetworkManager().sendPacket(new PacketFriendStatus(PacketFriendStatus.FriendStatus.SERVER, ""));
		}
	}

	public Profile getProfile() {
		return profile;
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
	}

	public CoordinateClipboard getCoordinatesClipboard() {
		return coordinatesClipboard;
	}

	public void setCoordinatesClipboard(CoordinateClipboard coordinatesClipboard) {
		this.coordinatesClipboard = coordinatesClipboard;
	}

	public DeathLocation getDeathLocation() {
		return deathLocation;
	}

	public void setDeathLocation(DeathLocation deathLocation) {
		this.deathLocation = deathLocation;
	}

	public NetworkStats getNetworkStats() {
		return networkStats;
	}

	public ChatTypingManager getChatTypingManager() {
		return chatTypingManager;
	}

	public AFKManager getAfkManager() {
		return afkManager;
	}

	public ChatBackgroundManager getChatBackgroundManager() {
		return chatBackgroundManager;
	}

	public FPSCalculator getFpsCalculator() {
		return fpsCalculator;
	}

	public CPSManager getCpsManager() {
		return cpsManager;
	}

	public SpeedCalculator getSpeedCalculator() {
		return speedCalculator;
	}

	public FileTransferManager getFileTransferManager() {
		return fileTransferManager;
	}

	public AutoReconnectManager getAutoReconnectManager() {
		return autoReconnectManager;
	}

	public SearchManager getSearchManager() {
		return searchManager;
	}

	public CrossHairDistanceListener getCrossHairDistanceListener() {
		return crossHairDistanceListener;
	}

	public WeatherManager getWeatherManager() {
		return weatherManager;
	}

	public SnowRenderer getSnowRenderer() {
		return snowRenderer;
	}

	public void registerServerInstance(ServerInstance serverInstance) {
		serverInstanceRegistry.registerServerInstance(serverInstance);
	}

	public ServerInstanceRegistry getServerInstanceRegistry() {
		return serverInstanceRegistry;
	}

	public void updateCurrentLobby() {
		Server server = The5zigMod.getDataManager().getServer();
		if (The5zigMod.getDataManager().getServer() != null) {
			The5zigMod.getNetworkManager().sendPacket(new PacketFriendStatus(PacketFriendStatus.FriendStatus.SERVER,
					The5zigMod.getDataManager().getServer().getHost() + ":" + The5zigMod.getDataManager().getServer().getPort()));
			if (server instanceof GameServer) {
				The5zigMod.getNetworkManager().sendPacket(new PacketFriendStatus(PacketFriendStatus.FriendStatus.LOBBY, ((GameServer) server).getLobbyString()));
			} else if (The5zigMod.getServerAPIBackend().getLobby() != null) {
				The5zigMod.getNetworkManager().sendPacket(new PacketFriendStatus(PacketFriendStatus.FriendStatus.LOBBY, The5zigMod.getServerAPIBackend().getLobby()));
			}
		}
	}

	public MediaManager getMediaManager() {
		return mediaManager;
	}

	public ILoveRadioManager getiLoveRadioManager() {
		return iLoveRadioManager;
	}

	public EinsLiveManager getEinsLiveManager() {
		return einsLiveManager;
	}

	public ITunesManager getITunesManager() {
		return iTunesManager;
	}

	public KeyboardManager getKeyboardManager() {
		return keyboardManager;
	}

	public TimerManager getTimerManager() {
		return timerManager;
	}

	public ChatSearchManager getChatSearchManager() {
		return chatSearchManager;
	}

	public Object getChatComponentWithTime(Object originalChatComponent) {
		ChatColor mainColor = The5zigMod.getConfig().get("chatTimePrefixColor", SelectColorItem.class).get();
		ChatColor bracketsColor = The5zigMod.getConfig().get("chatTimePrefixBracketsColor", SelectColorItem.class).get();
		BracketsFormatting bracketsFormatting = The5zigMod.getConfig().getEnum("chatTimePrefixBracketsFormatting", BracketsFormatting.class);
		String timeFormat = The5zigMod.getConfig().getString("chatTimePrefixTimeFormat");
		try {
			String prefix = bracketsColor + bracketsFormatting.getFirst() + mainColor + new SimpleDateFormat(timeFormat).format(new Date()) +
					bracketsColor + bracketsFormatting.getLast() + "§r ";
			return The5zigMod.getVars().getChatComponentWithPrefix(prefix, originalChatComponent);
		} catch (Exception e) {
			The5zigMod.logger.warn("Failed to parse time format " + timeFormat, e);
			return originalChatComponent;
		}
	}

	public boolean isTsRequiresAuth() {
		return tsRequiresAuth;
	}

	public void setTsRequiresAuth(boolean tsRequiresAuth) {
		this.tsRequiresAuth = tsRequiresAuth;
	}

	public TeamSpeakReconnectListener getTeamSpeakReconnectListener() {
		return teamSpeakReconnectListener;
	}

	public ServerSettings getServerSettings() {
		return serverSettings;
	}
}