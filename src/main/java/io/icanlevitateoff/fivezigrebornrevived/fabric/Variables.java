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

import io.icanlevitateoff.fivezigrebornrevived.fabric.compat.Draw;
import io.icanlevitateoff.fivezigrebornrevived.fabric.compat.Keys;
import io.icanlevitateoff.fivezigrebornrevived.fabric.compat.Mc;
import io.icanlevitateoff.fivezigrebornrevived.fabric.compat.Ids;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.NativeImage;
import io.icanlevitateoff.fivezigrebornrevived.fabric.gui.*;
import io.icanlevitateoff.fivezigrebornrevived.fabric.mixin.ChatScreenAccessor;
import io.icanlevitateoff.fivezigrebornrevived.fabric.mixin.MinecraftAccessor;
import io.icanlevitateoff.fivezigrebornrevived.fabric.mixin.OptionsAccessor;
import io.icanlevitateoff.fivezigrebornrevived.fabric.net.RawPayload;
import io.icanlevitateoff.fivezigrebornrevived.fabric.util.ChatComponentBuilder;
import io.icanlevitateoff.fivezigrebornrevived.mod.MinecraftFactory;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.Gui;
import eu.the5zig.mod.gui.IOverlay;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.IWrappedGui;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.elements.*;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.ingame.IGui2ndChat;
import eu.the5zig.mod.gui.ingame.ItemStack;
import eu.the5zig.mod.gui.ingame.PotionEffect;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.ingame.PotionEffectImpl;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.ingame.ScoreboardImpl;
import io.icanlevitateoff.fivezigrebornrevived.mod.util.*;
import eu.the5zig.mod.util.*;
import eu.the5zig.mod.util.component.MessageComponent;
import eu.the5zig.util.Callback;
import eu.the5zig.util.minecraft.ChatColor;
import io.netty.buffer.ByteBuf;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.User;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.scores.Objective;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.util.*;

/** The 1.21.8 implementation of core's version abstraction. Looked up by name from MinecraftFactory. */
public class Variables implements IVariables {

	private static final List<PotionEffectImpl> DUMMY_POTIONS = Arrays.asList(
			new PotionEffectImpl("effect.minecraft.jump_boost", 20, "0:01", 1, 10, true, true, 0x22ff4c),
			new PotionEffectImpl("effect.minecraft.speed", 20 * 50, "0:50", 1, 0, true, true, 0x7cafc6));

	private final IGui2ndChat gui2ndChat = new Gui2ndChat();
	private final ResourceManager resourceManager;
	/** Textures created through core (dynamic images, base64 renderers) and their locations. */
	private final Map<AbstractTexture, ResourceLocation> textureLocations = new IdentityHashMap<>();
	private final Map<ResourceLocation, AbstractTexture> textures = new HashMap<>();

	public Variables() {
		Keyboard.init(new Keyboard.KeyboardHandler() {
			@Override
			public boolean isKeyDown(int key) {
				return Mc.isKeyDown(Keys.toNative(key));
			}

			@Override
			public void enableRepeatEvents(boolean repeat) {
				// Key repeat is always delivered since 1.13.
			}
		});
		Mouse.init(new Mouse.MouseHandler() {
			@Override
			public boolean isButtonDown(int button) {
				return Mc.isMouseDown(button);
			}

			@Override
			public int getX() {
				return (int) mc().mouseHandler.xpos();
			}

			@Override
			public int getY() {
				return (int) (mc().getWindow().getHeight() - mc().mouseHandler.ypos());
			}
		});
		Display.init(() -> mc().isWindowActive());
		resourceManager = new ResourceManager(getGameProfile());
	}

	private static Minecraft mc() {
		return Minecraft.getInstance();
	}

	private static long window() {
		return Mc.window();
	}

	private static Font font() {
		return Minecraft.getInstance().font;
	}

	private static Options options() {
		return Minecraft.getInstance().options;
	}

	private static LocalPlayer player() {
		return Minecraft.getInstance().player;
	}

	private static ClientLevel level() {
		return Minecraft.getInstance().level;
	}

	private static Entity camera() {
		Entity camera = Minecraft.getInstance().getCameraEntity();
		return camera != null ? camera : player();
	}

	// ---------------------------------------------------------------- text

	@Override
	public void drawString(String string, int x, int y, Object... format) {
		drawString(string, x, y, 0xffffff, format);
	}

	@Override
	public void drawString(String string, int x, int y) {
		drawString(string, x, y, 0xffffff);
	}

	@Override
	public void drawCenteredString(String string, int x, int y) {
		drawCenteredString(string, x, y, 0xffffff);
	}

	@Override
	public void drawCenteredString(String string, int x, int y, int color) {
		drawString(string, x - getStringWidth(string) / 2, y, color);
	}

	@Override
	public void drawString(String string, int x, int y, int color, Object... format) {
		drawString(String.format(string, format), x, y, color);
	}

	@Override
	public void drawString(String string, int x, int y, int color) {
		drawString(string, x, y, color, true);
	}

	@Override
	public void drawString(String string, int x, int y, int color, boolean withShadow) {
		GuiGraphics g = RenderContext.get();
		if (g == null || string == null) {
			return;
		}
		// Like the old font renderer: a colour without alpha bits means opaque (1.21.6+ skips alpha 0).
		if ((color & 0xFC000000) == 0) {
			color |= 0xFF000000;
		}
		Draw.text(g, font(), string, x, y, color, withShadow);
	}

	@Override
	public List<String> splitStringToWidth(String string, int maxWidth) {
		Validate.isTrue(maxWidth > 0);
		if (string == null) {
			return Collections.emptyList();
		}
		if (string.isEmpty()) {
			return Collections.singletonList("");
		}
		List<String> lines = new ArrayList<>();
		for (String paragraph : string.split("\n", -1)) {
			wrapLegacy(paragraph, maxWidth, lines);
		}
		return lines;
	}

	/** Word-wraps a legacy formatted string, carrying active formatting codes over to the next line. */
	private void wrapLegacy(String text, int maxWidth, List<String> out) {
		String carry = "";
		while (true) {
			String line = carry + text;
			if (getStringWidth(line) <= maxWidth) {
				out.add(line);
				return;
			}
			int cut = text.length();
			while (cut > 1 && getStringWidth(carry + text.substring(0, cut)) > maxWidth) {
				cut--;
			}
			int space = text.lastIndexOf(' ', cut);
			if (space > 0) {
				cut = space;
			}
			if (cut > 0 && text.charAt(cut - 1) == ChatColor.COLOR_CHAR) {
				cut--;
			}
			cut = Math.max(1, cut);
			String head = carry + text.substring(0, cut);
			out.add(head);
			carry = lastFormat(head);
			text = text.substring(cut);
			if (text.startsWith(" ")) {
				text = text.substring(1);
			}
			if (text.isEmpty()) {
				return;
			}
		}
	}

	private static String lastFormat(String s) {
		StringBuilder codes = new StringBuilder();
		for (int i = 0; i < s.length() - 1; i++) {
			if (s.charAt(i) == ChatColor.COLOR_CHAR) {
				char code = Character.toLowerCase(s.charAt(i + 1));
				ChatColor c = ChatColor.getByChar(code);
				if (c != null) {
					if ("0123456789abcdefr".indexOf(code) >= 0) {
						codes.setLength(0);
					}
					if (c != ChatColor.RESET) {
						codes.append(c);
					}
				}
			}
		}
		return codes.toString();
	}

	@Override
	public int getStringWidth(String string) {
		return string == null ? 0 : font().width(string);
	}

	@Override
	public String shortenToWidth(String string, int width) {
		if (StringUtils.isEmpty(string)) {
			return string;
		}
		Validate.isTrue(width > 0);
		boolean changed = false;
		if (getStringWidth(string) > width) {
			while (getStringWidth(string + "...") > width && !string.isEmpty()) {
				string = string.substring(0, string.length() - 1);
				changed = true;
			}
		}
		return changed ? string + "..." : string;
	}

	@Override
	public int getFontHeight() {
		return font().lineHeight;
	}

	@Override
	public String translate(String location, Object... values) {
		return I18n.get(location, values);
	}

	// ---------------------------------------------------------------- widgets

	@Override
	public IButton createButton(int id, int x, int y, String label) {
		return new Button(id, x, y, label);
	}

	@Override
	public IButton createButton(int id, int x, int y, String label, boolean enabled) {
		return new Button(id, x, y, label, enabled);
	}

	@Override
	public IButton createButton(int id, int x, int y, int width, int height, String label) {
		return new Button(id, x, y, width, height, label);
	}

	@Override
	public IButton createButton(int id, int x, int y, int width, int height, String label, boolean enabled) {
		return new Button(id, x, y, width, height, label, enabled);
	}

	@Override
	public IButton createStringButton(int id, int x, int y, String label) {
		return new StringButton(id, x, y, label);
	}

	@Override
	public IButton createStringButton(int id, int x, int y, int width, int height, String label) {
		return new StringButton(id, x, y, width, height, label);
	}

	@Override
	public IButton createAudioButton(int id, int x, int y, AudioCallback callback) {
		return new AudioButton(id, x, y, callback);
	}

	@Override
	public IButton createIconButton(IResourceLocation resourceLocation, int u, int v, int id, int x, int y) {
		return new IconButton(resourceLocation, u, v, id, x, y);
	}

	@Override
	public ITextfield createTextfield(int id, int x, int y, int width, int height) {
		return new Textfield(id, x, y, width, height);
	}

	@Override
	public ITextfield createTextfield(int id, int x, int y, int width, int height, int maxStringLength) {
		return new Textfield(id, x, y, width, height, maxStringLength);
	}

	@Override
	public IPlaceholderTextfield createTextfield(String placeholder, int id, int x, int y, int width, int height) {
		return new PlaceholderTextfield(placeholder, id, x, y, width, height);
	}

	@Override
	public IPlaceholderTextfield createTextfield(String placeholder, int id, int x, int y, int width, int height, int maxStringLength) {
		return new PlaceholderTextfield(placeholder, id, x, y, width, height, maxStringLength);
	}

	@Override
	public IWrappedTextfield createWrappedTextfield(Object handle) {
		return new WrappedTextfield((net.minecraft.client.gui.components.EditBox) handle);
	}

	@Override
	public <E extends Row> IGuiList<E> createGuiList(Clickable<E> clickable, int width, int height, int top, int bottom, int left, int right, List<E> rows) {
		return new GuiList<>(clickable, width, height, top, bottom, left, right, rows);
	}

	@Override
	public <E extends Row> IGuiList<E> createGuiListChat(int width, int height, int top, int bottom, int left, int right, int scrollx, List<E> rows, GuiListChatCallback callback) {
		return new GuiListChat<>(width, height, top, bottom, left, right, scrollx, rows, callback);
	}

	@Override
	public IFileSelector createFileSelector(File currentDir, int width, int height, int left, int right, int top, int bottom, Callback<File> callback) {
		return new FileSelector(currentDir, width, height, left, right, top, bottom, callback);
	}

	@Override
	public IButton createSlider(int id, int x, int y, SliderCallback sliderCallback) {
		return new Slider(id, x, y, sliderCallback);
	}

	@Override
	public IButton createSlider(int id, int x, int y, int width, int height, SliderCallback sliderCallback) {
		return new Slider(id, x, y, width, height, sliderCallback);
	}

	@Override
	public IColorSelector createColorSelector(int id, int x, int y, int width, int height, String label, ColorSelectorCallback callback) {
		return new ColorSelector(id, x, y, width, height, label, callback);
	}

	@Override
	public IOverlay newOverlay() {
		return new Overlay();
	}

	@Override
	public void updateOverlayCount(int count) {
		Overlay.updateOverlayCount(count);
	}

	@Override
	public void renderOverlay() {
		Overlay.renderAll();
	}

	@Override
	public IWrappedGui createWrappedGui(Object lastScreen) {
		return new WrappedGui((Screen) lastScreen);
	}

	@Override
	public void crash(Throwable cause, String description) {
		net.minecraft.CrashReport report = net.minecraft.CrashReport.forThrowable(cause, description);
		throw new net.minecraft.ReportedException(report);
	}

	@Override
	public IKeybinding createKeybinding(String description, int keyCode, String category) {
		return new Keybinding(description, keyCode, category);
	}

	@Override
	public void registerKeybindings(List<IKeybinding> keybindings) {
		KeyMapping[] custom = new KeyMapping[keybindings.size()];
		for (int i = 0; i < custom.length; i++) {
			custom[i] = (KeyMapping) keybindings.get(i);
		}
		Options options = options();
		((OptionsAccessor) options).the5zig$setKeyMappings(ArrayUtils.addAll(options.keyMappings, custom));
		options.load();
		//? if <26 {
		// Older versions saved the number-pad defaults as key 0, which GLFW rejects: use the default again.
		boolean fixed = false;
		for (KeyMapping mapping : custom) {
			if ("key.keyboard.0".equals(mapping.saveString())) {
				mapping.setKey(mapping.getDefaultKey());
				fixed = true;
			}
		}
		if (fixed) {
			options.save();
		}
		//?}
		KeyMapping.resetMapping();
	}

	// ---------------------------------------------------------------- chat

	@Override
	public IGui2ndChat get2ndChat() {
		return gui2ndChat;
	}

	@Override
	public boolean isChatOpened() {
		return Mc.screen() instanceof ChatScreen;
	}

	@Override
	public String getChatBoxText() {
		return isChatOpened() ? ((ChatScreenAccessor) Mc.screen()).the5zig$getInput().getValue() : null;
	}

	@Override
	public void typeInChatGUI(String text) {
		if (!isChatOpened()) {
			//? if >=1.21.9 {
			/*displayScreen(new ChatScreen(text, false));
			*///?} else {
			displayScreen(new ChatScreen(text));
			//?}
			return;
		}
		net.minecraft.client.gui.components.EditBox input = ((ChatScreenAccessor) Mc.screen()).the5zig$getInput();
		input.setValue(input.getValue() + text);
	}

	@Override
	public Object getChatComponentWithPrefix(String prefix, Object originalChatComponent) {
		return ChatComponentBuilder.fromLegacyText(prefix).append((Component) originalChatComponent);
	}

	@Override
	public boolean isSignGUIOpened() {
		return Mc.screen() instanceof AbstractSignEditScreen;
	}

	@Override
	public void messagePlayer(String message) {
		messagePlayer(ChatComponentBuilder.fromLegacyText(message));
	}

	public void messagePlayer(Component component) {
		String legacy = ChatComponentBuilder.toLegacy(component).replace(ChatColor.RESET.toString(), "");
		if (!MinecraftFactory.getClassProxyCallback().shouldCancelChatMessage(legacy, component)) {
			Mc.addChatMessage(component);
		}
	}

	@Override
	public void sendMessage(String message) {
		if (player() == null) {
			return;
		}
		if (message.startsWith("/")) {
			player().connection.sendCommand(message.substring(1));
		} else {
			player().connection.sendChat(message);
		}
	}

	@Override
	public void sendChatComponent(MessageComponent component, boolean secondChat) {
		Component result = ChatComponentBuilder.fromInterface(component);
		if (secondChat) {
			gui2ndChat.printChatMessage(result);
		} else {
			Mc.addChatMessage(result);
		}
	}

	// ---------------------------------------------------------------- sound

	@Override
	public void playSound(String sound, float pitch) {
		playSound("minecraft", sound, pitch);
	}

	@Override
	public void playSound(String domain, String sound, float pitch) {
		SoundEvent event = SoundEvent.createVariableRangeEvent(Ids.of(domain, sound));
		mc().getSoundManager().play(SimpleSoundInstance.forUI(event, pitch));
	}

	// ---------------------------------------------------------------- server / connection

	@Override
	public void resetServer() {
		// The current server lives on the connection since 1.20.2; nothing to clear.
	}

	@Override
	public String getServer() {
		ServerData data = mc().getCurrentServer();
		return data == null ? null : data.ip;
	}

	@Override
	public List<NetworkPlayerInfo> getServerPlayers() {
		List<NetworkPlayerInfo> result = Lists.newArrayList();
		if (mc().getConnection() != null) {
			for (PlayerInfo info : mc().getConnection().getOnlinePlayers()) {
				result.add(new WrappedNetworkPlayerInfo(info));
			}
		}
		return result;
	}

	@Override
	public boolean isPlayerListShown() {
		return options().keyPlayerList.isDown() && (!mc().isLocalServer() || getServerPlayers().size() > 1);
	}

	@Override
	public void joinServer(String host, int port) {
		//? if >=1.20.2 {
		ServerData data = new ServerData(host, io.icanlevitateoff.fivezigrebornrevived.util.HostAndPort.format(host, port), ServerData.Type.OTHER);
		//?} else {
		/*ServerData data = new ServerData(host, io.icanlevitateoff.fivezigrebornrevived.util.HostAndPort.format(host, port), false);
		*///?}
		joinServer(Mc.screen(), data);
	}

	@Override
	public void joinServer(Object parentScreen, Object serverData) {
		if (!(serverData instanceof ServerData data)) {
			return;
		}
		MinecraftFactory.getClassProxyCallback().resetServer();
		//? if >=1.20.5 {
		ConnectScreen.startConnecting((Screen) parentScreen, mc(), ServerAddress.parseString(data.ip), data, false, null);
		//?} else {
		/*ConnectScreen.startConnecting((Screen) parentScreen, mc(), ServerAddress.parseString(data.ip), data, false);
		*///?}
	}

	@Override
	public void disconnectFromWorld() {
		boolean singleplayer = mc().isLocalServer();
		ServerData server = mc().getCurrentServer();
		//? if >=1.20.2 {
		boolean realms = server != null && server.isRealm();
		//?} else {
		/*boolean realms = mc().isConnectedToRealms();
		*///?}
		//? if >=1.21.6 {
		if (level() != null) {
			level().disconnect(ClientLevel.DEFAULT_QUIT_MESSAGE);
		}
		if (singleplayer) {
			mc().disconnectWithSavingScreen();
		} else {
			mc().disconnectWithProgressScreen();
		}
		//?} else if >=1.20.5 {
		/*if (level() != null) {
			level().disconnect();
		}
		net.minecraft.client.gui.screens.Screen saving = new net.minecraft.client.gui.screens.GenericMessageScreen(Component.translatable("menu.savingLevel"));
		if (singleplayer) {
			mc().disconnect(saving);
		} else {
			mc().disconnect();
		}
		*///?} else if >=1.20.2 {
		/*if (level() != null) {
			level().disconnect();
		}
		net.minecraft.client.gui.screens.Screen saving = new net.minecraft.client.gui.screens.GenericDirtMessageScreen(Component.translatable("menu.savingLevel"));
		if (singleplayer) {
			mc().disconnect(saving);
		} else {
			mc().disconnect();
		}
		*///?} else {
		/*if (level() != null) {
			level().disconnect();
		}
		net.minecraft.client.gui.screens.Screen saving = new net.minecraft.client.gui.screens.GenericDirtMessageScreen(Component.translatable("menu.savingLevel"));
		if (singleplayer) {
			mc().clearLevel(saving);
		} else {
			mc().clearLevel();
		}
		*///?}
		TitleScreen title = new TitleScreen();
		if (singleplayer) {
			displayScreen(title);
		} else if (realms) {
			displayScreen(new com.mojang.realmsclient.RealmsMainScreen(title));
		} else {
			displayScreen(new JoinMultiplayerScreen(title));
		}
	}

	@Override
	public IServerPinger getServerPinger() {
		return new ServerPinger();
	}

	@Override
	public boolean hasNetworkManager() {
		return mc().getConnection() != null;
	}

	@Override
	public void sendCustomPayload(String channel, ByteBuf payload) {
		if (hasNetworkManager()) {
			RawPayload.send(channel, payload);
		}
	}

	@Override
	public boolean isLocalWorld() {
		return hasNetworkManager() && mc().getConnection().getConnection().isMemoryConnection();
	}

	@Override
	public void joinServerSession(String serverHash) throws Exception {
		User user = mc().getUser();
		//? if >=1.21.9 {
		/*mc().services().sessionService().joinServer(user.getProfileId(), user.getAccessToken(), serverHash);
		*///?} else if >=1.20.2 {
		mc().getMinecraftSessionService().joinServer(user.getProfileId(), user.getAccessToken(), serverHash);
		//?} else {
		/*mc().getMinecraftSessionService().joinServer(user.getGameProfile(), user.getAccessToken(), serverHash);
		*///?}
	}

	// ---------------------------------------------------------------- screens

	@Override
	public void displayScreen(Gui gui) {
		displayScreen(gui == null ? null : gui.getHandle());
	}

	@Override
	public void displayScreen(Object gui) {
		Minecraft mc = mc();
		if (mc.isSameThread()) {
			Mc.setScreen((Screen) gui);
		} else {
			mc.execute(() -> Mc.setScreen((Screen) gui));
		}
	}

	@Override
	public Gui getCurrentScreen() {
		return Mc.screen() instanceof GuiHandle handle ? handle.getChild() : null;
	}

	@Override
	public Object getMinecraftScreen() {
		return Mc.screen();
	}

	@Override
	public void setIngameFocus() {
		mc().mouseHandler.grabMouse();
	}

	// ---------------------------------------------------------------- options

	@Override
	public void setFOV(float fov) {
		options().fov().set((int) fov);
	}

	@Override
	public float getFOV() {
		return options().fov().get();
	}

	@Override
	public void setSmoothCamera(boolean smoothCamera) {
		options().smoothCamera = smoothCamera;
	}

	@Override
	public boolean isSmoothCamera() {
		return options().smoothCamera;
	}

	@Override
	public boolean isFancyGraphicsEnabled() {
		//? if >=1.21.11 {
		/*return options().graphicsPreset().get() != net.minecraft.client.GraphicsPreset.FAST;
		*///?} else {
		return Minecraft.useFancyGraphics();
		//?}
	}

	@Override
	public String[] getHotbarKeys() {
		String[] result = new String[9];
		KeyMapping[] hotbar = options().keyHotbarSlots;
		for (int i = 0; i < Math.min(result.length, hotbar.length); i++) {
			result[i] = hotbar[i].getTranslatedKeyMessage().getString();
		}
		return result;
	}

	@Override
	public String getKeyDisplayStringShort(int key) {
		if (key < 0) {
			return "M" + (key + 101);
		}
		return InputConstants.Type.KEYSYM.getOrCreate(Keys.toNative(key)).getDisplayName().getString();
	}

	// ---------------------------------------------------------------- session

	@Override
	public String getSession() {
		return mc().getUser().getAccessToken();
	}

	@Override
	public String getUsername() {
		return mc().getUser().getName();
	}

	@Override
	public Proxy getProxy() {
		return mc().getProxy();
	}

	@Override
	public GameProfile getGameProfile() {
		//? if >=1.20.2 {
		return mc().getGameProfile();
		//?} else {
		/*return mc().getUser().getGameProfile();
		*///?}
	}

	@Override
	public void runOnMainThread(Runnable task) {
		if (mc().isSameThread()) {
			task.run();
		} else {
			mc().execute(task);
		}
	}

	@Override
	public void setClipboard(String text) {
		mc().keyboardHandler.setClipboard(text);
	}

	@Override
	public void setSession(String name, String uuid, String token, String userType) {
		UUID id;
		try {
			String dashed = uuid.length() == 32 ? uuid.replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5") : uuid;
			id = UUID.fromString(dashed);
		} catch (IllegalArgumentException | NullPointerException e) {
			id = UUIDUtil.createOfflinePlayerUUID(name);
		}
		//? if >=1.21.9 {
		/*((MinecraftAccessor) mc()).the5zig$setUser(new User(name, id, token, Optional.empty(), Optional.empty()));
		*///?} else if >=1.20.2 {
		User.Type type = "msa".equalsIgnoreCase(userType) ? User.Type.MSA : User.Type.LEGACY;
		((MinecraftAccessor) mc()).the5zig$setUser(new User(name, id, token, Optional.empty(), Optional.empty(), type));
		//?} else {
		/*User.Type type = "msa".equalsIgnoreCase(userType) ? User.Type.MSA : User.Type.LEGACY;
		((MinecraftAccessor) mc()).the5zig$setUser(new User(name, id.toString().replace("-", ""), token, Optional.empty(), Optional.empty(), type));
		*///?}
		refreshChatKeys("msa".equalsIgnoreCase(userType) ? token : null);
	}

	/**
	 * Vanilla creates the chat signing keys and the user API service once, for the account the game
	 * started with. After switching account they have to match the new one, or servers enforcing
	 * secure chat reject messages.
	 */
	private void refreshChatKeys(String accessToken) {
		MinecraftAccessor accessor = (MinecraftAccessor) mc();
		if (accessToken == null) {
			accessor.the5zig$setProfileKeyPairManager(net.minecraft.client.multiplayer.ProfileKeyPairManager.EMPTY_KEY_MANAGER);
			return;
		}
		try {
			//? if >=26 {
			/*com.mojang.authlib.minecraft.UserApiService api = com.mojang.authlib.services.MinecraftServicesDiscoveryService.create(mc().getProxy()).createUserApiService(accessToken);
			*///?} else {
			com.mojang.authlib.minecraft.UserApiService api = new com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService(mc().getProxy()).createUserApiService(accessToken);
			//?}
			accessor.the5zig$setUserApiService(api);
			accessor.the5zig$setProfileKeyPairManager(net.minecraft.client.multiplayer.ProfileKeyPairManager.create(api, mc().getUser(), mc().gameDirectory.toPath()));
		} catch (Exception e) {
			io.icanlevitateoff.fivezigrebornrevived.mod.The5zigMod.logger.warn("Could not set up chat signing for the new account: " + e);
			accessor.the5zig$setProfileKeyPairManager(net.minecraft.client.multiplayer.ProfileKeyPairManager.EMPTY_KEY_MANAGER);
		}
	}


	// ---------------------------------------------------------------- player / world state

	@Override
	public long getSystemTime() {
		return System.currentTimeMillis();
	}

	@Override
	public boolean isSpectatingSelf() {
		return camera() instanceof Player;
	}

	@Override
	public PlayerGameMode getGameMode() {
		GameType type = mc().gameMode == null ? GameType.SURVIVAL : mc().gameMode.getPlayerMode();
		return PlayerGameMode.values()[Math.min(type.getId(), PlayerGameMode.values().length - 1)];
	}

	@Override
	public String getOpenContainerTitle() {
		if (Mc.screen() instanceof AbstractContainerScreen<?> screen && screen.getMenu() instanceof ChestMenu) {
			return ChatComponentBuilder.toLegacy(screen.getTitle());
		}
		return null;
	}

	@Override
	public void closeContainer() {
		if (player() != null) {
			player().closeContainer();
		}
	}

	@Override
	public String getFPS() {
		return Integer.toString(mc().getFps());
	}

	@Override
	public boolean isPlayerNull() {
		return player() == null;
	}

	@Override
	public boolean isTerrainLoading() {
		//? if >=1.21.9 {
		/*return Mc.screen() instanceof LevelLoadingScreen;
		*///?} else {
		return Mc.screen() instanceof LevelLoadingScreen || Mc.screen() instanceof net.minecraft.client.gui.screens.ReceivingLevelScreen;
		//?}
	}

	@Override
	public double getPlayerPosX() {
		return camera().getX();
	}

	@Override
	public double getPlayerPosY() {
		return camera().getY();
	}

	@Override
	public double getPlayerPosZ() {
		return camera().getZ();
	}

	@Override
	public float getPlayerRotationYaw() {
		return camera().getYRot();
	}

	@Override
	public float getPlayerRotationPitch() {
		return camera().getXRot();
	}

	private BlockPos playerPos() {
		return camera().blockPosition();
	}

	@Override
	public int getPlayerChunkX() {
		return playerPos().getX() >> 4;
	}

	@Override
	public int getPlayerChunkY() {
		return playerPos().getY() >> 4;
	}

	@Override
	public int getPlayerChunkZ() {
		return playerPos().getZ() >> 4;
	}

	@Override
	public int getPlayerChunkRelX() {
		return playerPos().getX() & 15;
	}

	@Override
	public int getPlayerChunkRelY() {
		return playerPos().getY() & 15;
	}

	@Override
	public int getPlayerChunkRelZ() {
		return playerPos().getZ() & 15;
	}

	private BlockPos targetBlock() {
		HitResult hit = mc().hitResult;
		return hit instanceof BlockHitResult block && hit.getType() == HitResult.Type.BLOCK ? block.getBlockPos() : null;
	}

	@Override
	public boolean hasTargetBlock() {
		return targetBlock() != null;
	}

	@Override
	public int getTargetBlockX() {
		BlockPos pos = targetBlock();
		return pos == null ? 0 : pos.getX();
	}

	@Override
	public int getTargetBlockY() {
		BlockPos pos = targetBlock();
		return pos == null ? 0 : pos.getY();
	}

	@Override
	public int getTargetBlockZ() {
		BlockPos pos = targetBlock();
		return pos == null ? 0 : pos.getZ();
	}

	@Override
	public IResourceLocation getTargetBlockName() {
		BlockPos pos = targetBlock();
		if (pos == null || level() == null) {
			return null;
		}
		return new IdWrapper(BuiltInRegistries.BLOCK.getKey(level().getBlockState(pos).getBlock()));
	}

	@Override
	public String getBiome() {
		BlockPos pos = playerPos();
		if (level() == null || !level().isLoaded(pos)) {
			return null;
		}
		return level().getBiome(pos).unwrapKey()
				//? if >=1.21.11 {
				/*.map(key -> I18n.get(Util.makeDescriptionId("biome", key.identifier())))
				*///?} else {
				.map(key -> I18n.get(Util.makeDescriptionId("biome", key.location())))
				//?}
				.orElse(null);
	}

	@Override
	public int getLightLevel() {
		return level() == null ? 0 : level().getMaxLocalRawBrightness(playerPos());
	}

	@Override
	public String getEntityCount() {
		if (level() == null) {
			return "No world";
		}
		//? if >=26 {
		/*return String.valueOf(level().getEntityCount());
		*///?} else {
		String[] parts = mc().levelRenderer.getEntityStatistics().split("[ ,]");
		return parts.length > 1 ? parts[1] : "?";
		//?}
	}

	@Override
	public boolean isRidingEntity() {
		return camera().getVehicle() != null;
	}

	private Player foodPlayer() {
		return camera() instanceof Player p ? p : player();
	}

	@Override
	public int getFoodLevel() {
		return foodPlayer().getFoodData().getFoodLevel();
	}

	@Override
	public float getSaturation() {
		return isSpectatingSelf() ? foodPlayer().getFoodData().getSaturationLevel() : 0;
	}

	@Override
	public float getHealth(Object entity) {
		return entity instanceof LivingEntity living ? living.getHealth() : -1;
	}

	@Override
	public float getPlayerHealth() {
		return player().getHealth();
	}

	@Override
	public float getPlayerMaxHealth() {
		return player().getMaxHealth();
	}

	@Override
	public int getPlayerArmor() {
		return player().getArmorValue();
	}

	@Override
	public int getAir() {
		return player().getAirSupply();
	}

	@Override
	public boolean isPlayerInsideWater() {
		return camera().isInWater();
	}

	@Override
	public float getResistanceFactor() {
		float damage = 100 * (25 - player().getArmorValue()) / 25.0F;
		MobEffectInstance resistance = player().getEffect(
				//? if >=1.21.5 {
				MobEffects.RESISTANCE
				//?} else {
				/*MobEffects.DAMAGE_RESISTANCE
				*///?}
		);
		if (resistance != null) {
			damage = damage * (25 - (resistance.getAmplifier() + 1) * 5) / 25.0F;
		}
		return damage <= 0 ? 100 : 100 - damage;
	}

	@Override
	public MouseOverObject calculateMouseOverDistance(double maxDistance) {
		return null;
	}

	// ---------------------------------------------------------------- potions

	private Collection<MobEffectInstance> activeEffects() {
		return player() == null ? Collections.emptyList() : player().getActiveEffects();
	}

	private static PotionEffectImpl wrap(MobEffectInstance instance) {
		//? if >=1.20.5 {
		MobEffect effect = instance.getEffect().value();
		//?} else {
		/*MobEffect effect = instance.getEffect();
		*///?}
		//? if >=1.20.3 {
		float tickRate = Minecraft.getInstance().level == null ? 20f : Minecraft.getInstance().level.tickRateManager().tickrate();
		String duration = MobEffectUtil.formatDuration(instance, 1.0F, tickRate).getString();
		//?} else {
		/*String duration = MobEffectUtil.formatDuration(instance, 1.0F).getString();
		*///?}
		return new PotionEffectImpl(effect.getDescriptionId(), instance.getDuration(),
				duration, instance.getAmplifier() + 1,
				BuiltInRegistries.MOB_EFFECT.getId(effect), effect.isBeneficial(), instance.isVisible(), effect.getColor());
	}

	@Override
	public PotionEffect getPotionForVignette() {
		for (MobEffectInstance instance : activeEffects()) {
			return wrap(instance);
		}
		return null;
	}

	@Override
	public List<? extends PotionEffect> getActivePotionEffects() {
		List<PotionEffectImpl> result = new ArrayList<>();
		for (MobEffectInstance instance : activeEffects()) {
			result.add(wrap(instance));
		}
		Collections.sort(result);
		return result;
	}

	@Override
	public List<? extends PotionEffect> getDummyPotionEffects() {
		return DUMMY_POTIONS;
	}

	@Override
	public int getPotionEffectIndicatorHeight() {
		return 0;
	}

	@Override
	public boolean isHungerPotionActive() {
		return player().hasEffect(MobEffects.HUNGER);
	}

	@Override
	public void renderPotionIcon(int index) {
		GuiGraphics g = RenderContext.get();
		MobEffect effect = BuiltInRegistries.MOB_EFFECT.byId(index);
		if (g == null || effect == null) {
			return;
		}
		Draw.mobEffect(g, effect, 0, 0);
	}

	// ---------------------------------------------------------------- items

	private static ItemStack wrap(net.minecraft.world.item.ItemStack stack) {
		return stack == null || stack.isEmpty() ? null : new WrappedItemStack(stack);
	}

	@Override
	public ItemStack getItemInMainHand() {
		return wrap(player().getMainHandItem());
	}

	@Override
	public ItemStack getItemInOffHand() {
		return wrap(player().getOffhandItem());
	}

	/** @param slot 0 = boots, 1 = leggings, 2 = chestplate, 3 = helmet */
	@Override
	public ItemStack getItemInArmorSlot(int slot) {
		EquipmentSlot[] slots = {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};
		return slot < 0 || slot >= slots.length ? null : wrap(player().getItemBySlot(slots[slot]));
	}

	@Override
	public ItemStack getItemByName(String resourceName) {
		return getItemByName(resourceName, 1);
	}

	@Override
	public ItemStack getItemByName(String resourceName, int amount) {
		//? if >=1.21.2 {
		return new WrappedItemStack(new net.minecraft.world.item.ItemStack(BuiltInRegistries.ITEM.getValue(Ids.parse(resourceName)), amount));
		//?} else {
		/*return new WrappedItemStack(new net.minecraft.world.item.ItemStack(BuiltInRegistries.ITEM.get(Ids.parse(resourceName)), amount));
		*///?}
	}

	@Override
	public int getItemCount(String key) {
		int count = 0;
		for (net.minecraft.world.item.ItemStack stack : Mc.mainItems(player().getInventory())) {
			if (!stack.isEmpty() && key.equals(WrappedItemStack.getResourceKey(stack))) {
				count += stack.getCount();
			}
		}
		return count;
	}

	@Override
	public int getSelectedHotbarSlot() {
		return Mc.selectedSlot(player().getInventory());
	}

	@Override
	public void setSelectedHotbarSlot(int slot) {
		Mc.setSelectedSlot(player().getInventory(), slot);
		player().connection.send(new ServerboundSetCarriedItemPacket(slot));
	}

	@Override
	public void onRightClickMouse() {
		((MinecraftAccessor) mc()).the5zig$startUseItem();
	}

	// ---------------------------------------------------------------- window

	@Override
	public void updateScaledResolution() {
	}

	@Override
	public int getWidth() {
		return mc().getWindow().getWidth();
	}

	@Override
	public int getHeight() {
		return mc().getWindow().getHeight();
	}

	@Override
	public int getScaledWidth() {
		return mc().getWindow().getGuiScaledWidth();
	}

	@Override
	public int getScaledHeight() {
		return mc().getWindow().getGuiScaledHeight();
	}

	@Override
	public int getScaleFactor() {
		return (int) mc().getWindow().getGuiScale();
	}

	@Override
	public boolean showDebugScreen() {
		//? if >=1.20.2 {
		return Mc.debugScreenShown();
		//?} else {
		/*return mc().options.renderDebug;
		*///?}
	}

	@Override
	public boolean isPlayerSpectating() {
		return !isSpectatingSelf() || (mc().gameMode != null && mc().gameMode.getPlayerMode() == GameType.SPECTATOR);
	}

	@Override
	public boolean shouldDrawHUD() {
		return mc().gameMode != null && mc().gameMode.canHurtPlayer();
	}

	@Override
	public void drawIngameTexturedModalRect(int x, int y, int u, int v, int width, int height) {
		GuiHandle.callDrawModalRectWithCustomSizedTexture(x, y, u, v, width, height, 256, 256);
	}

	@Override
	public void renderTextureOverlay(int x1, int x2, int y1, int y2) {
		GuiGraphics g = RenderContext.get();
		if (g != null) {
			Draw.menuBackground(g, x1, y1, x2, y2 - y1);
		}
	}

	// ---------------------------------------------------------------- textures

	@Override
	public IResourceLocation createResourceLocation(String resourcePath) {
		return IdWrapper.of(resourcePath);
	}

	@Override
	public IResourceLocation createResourceLocation(String resourceDomain, String resourcePath) {
		return IdWrapper.of(resourceDomain, resourcePath);
	}

	private ResourceLocation toLocation(Object o) {
		if (o instanceof AbstractTexture texture) {
			return textureLocations.get(texture);
		}
		return IdWrapper.unwrap(o);
	}

	private void track(ResourceLocation location, AbstractTexture texture) {
		AbstractTexture old = textures.put(location, texture);
		if (old != null) {
			textureLocations.remove(old);
		}
		textureLocations.put(texture, location);
		mc().getTextureManager().register(location, texture);
	}

	@Override
	public Object loadDynamicImage(String name, BufferedImage image) {
		try {
			ByteArrayOutputStream png = new ByteArrayOutputStream();
			ImageIO.write(image, "png", png);
			ResourceLocation location = Ids.of("the5zigmod", "dynamic/" + name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9/._-]", "_"));
			track(location, Mc.texture(location.toString(), NativeImage.read(png.toByteArray())));
			return location;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void bindTexture(Object resourceLocation) {
		RenderContext.bindTexture(toLocation(resourceLocation));
	}

	@Override
	public void deleteTexture(Object resourceLocation) {
		ResourceLocation location = toLocation(resourceLocation);
		if (location != null) {
			AbstractTexture texture = textures.remove(location);
			if (texture != null) {
				textureLocations.remove(texture);
			}
			mc().getTextureManager().release(location);
		}
	}

	@Override
	public Object createDynamicImage(Object resourceLocation, int width, int height) {
		ResourceLocation location = toLocation(resourceLocation);
		DynamicTexture texture = Mc.texture(location.toString(), width, height);
		track(location, texture);
		return texture;
	}

	@Override
	public Object getTexture(Object resourceLocation) {
		return textures.get(toLocation(resourceLocation));
	}

	@Override
	public void fillDynamicImage(Object dynamicImage, BufferedImage image) {
		DynamicTexture texture = (DynamicTexture) dynamicImage;
		NativeImage pixels = texture.getPixels();
		if (pixels == null) {
			return;
		}
		int w = Math.min(image.getWidth(), pixels.getWidth());
		int h = Math.min(image.getHeight(), pixels.getHeight());
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int argb = image.getRGB(x, y);
				//? if >=1.21.2 {
				pixels.setPixel(x, y, argb);
				//?} else {
				/*pixels.setPixelRGBA(x, y, (argb & 0xFF00FF00) | ((argb >> 16) & 0xFF) | ((argb & 0xFF) << 16));
				*///?}
			}
		}
		texture.upload();
	}

	// ---------------------------------------------------------------- misc

	@Override
	public ScoreboardImpl getScoreboard() {
		if (level() == null) {
			return null;
		}
		//? if >=1.20.2 {
		Objective objective = level().getScoreboard().getDisplayObjective(net.minecraft.world.scores.DisplaySlot.SIDEBAR);
		//?} else {
		/*Objective objective = level().getScoreboard().getDisplayObjective(net.minecraft.world.scores.Scoreboard.DISPLAY_SLOT_SIDEBAR);
		*///?}
		if (objective == null) {
			return null;
		}
		HashMap<String, Integer> lines = new HashMap<>();
		//? if >=1.20.3 {
		for (net.minecraft.world.scores.PlayerScoreEntry entry : level().getScoreboard().listPlayerScores(objective)) {
			if (!entry.isHidden()) {
				lines.put(entry.owner(), entry.value());
			}
		}
		//?} else {
		/*for (net.minecraft.world.scores.Score score : level().getScoreboard().getPlayerScores(objective)) {
			if (score.getOwner() != null && !score.getOwner().startsWith("#")) {
				lines.put(score.getOwner(), score.getScore());
			}
		}
		*///?}
		return new ScoreboardImpl(objective.getDisplayName().getString(), lines);
	}

	@Override
	public ResourceManager getResourceManager() {
		return resourceManager;
	}

	@Override
	public ISystemPowerStatus getBatteryStatus() {
		return null;
	}

	@Override
	public InputStream getMinecraftIcon() {
		return null;
	}

	@Override
	public boolean isMainThread() {
		return mc().isSameThread();
	}

	@Override
	public File getMinecraftDataDirectory() {
		return mc().gameDirectory;
	}

	@Override
	public void dispatchKeypresses() {
		// Key presses reach core through MixinKeyboardHandler.
	}

	@Override
	public void shutdown() {
		mc().stop();
	}
}
