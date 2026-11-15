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

import io.icanlevitateoff.fivezigrebornrevived.fabric.compat.Mc;
import io.icanlevitateoff.fivezigrebornrevived.fabric.compat.Ids;
import io.icanlevitateoff.fivezigrebornrevived.fabric.mixin.DisconnectedScreenAccessor;
import io.icanlevitateoff.fivezigrebornrevived.fabric.net.RawPayload;
import io.icanlevitateoff.fivezigrebornrevived.fabric.util.ChatComponentBuilder;
import io.icanlevitateoff.fivezigrebornrevived.mod.BytecodeHook;
import io.icanlevitateoff.fivezigrebornrevived.mod.MinecraftFactory;
import io.icanlevitateoff.fivezigrebornrevived.mod.The5zigMod;
import io.icanlevitateoff.fivezigrebornrevived.mod.Version;
import io.icanlevitateoff.fivezigrebornrevived.mod.api.PayloadUtils;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.GuiSettings;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.GuiYesNo;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.YesNoCallback;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
//? if >=1.21.6 {
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
//?}
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
//? if >=1.21 {
import net.minecraft.client.gui.screens.options.OptionsScreen;
//?} else {
/*import net.minecraft.client.gui.screens.OptionsScreen;
*///?}
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Fabric entrypoint. Wires core into the game through Fabric API events where one exists
 * (lifecycle, ticks, chat, HUD layers, screen buttons); the rest are mixins in
 * {@code io.icanlevitateoff.fivezigrebornrevived.fabric.mixin}.
 */
public class FiveZigClient implements ClientModInitializer {

	private static ResourceLocation id(String path) {
		return Ids.of("the5zigmod", path);
	}

	private static boolean ready() {
		return The5zigMod.hasBeenInitialized();
	}

	@Override
	public void onInitializeClient() {
		RawPayload.init(BytecodeHook::onCustomPayload,
				PayloadUtils.API_CHANNEL, PayloadUtils.API_CHANNEL_REGISTER, PayloadUtils.SETTING_CHANNEL);

		ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
			if (!The5zigMod.hasBeenInitialized()) {
				The5zigMod.init();
			}
		});
		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
			if (ready()) {
				The5zigMod.shutdown();
			}
		});
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (!ready()) {
				return;
			}
			The5zigMod.getListener().onTick();
			if (client.player != null && !client.isPaused()) {
				The5zigMod.getGuiIngame().tick();
			}
		});

		registerChat();
		registerHud();
		registerScreens();
	}

	private static void registerChat() {
		ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> !ready() || !onServerMessage(message, overlay));
		ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signed, sender, params, receptionTimestamp) -> !ready() || !onServerMessage(message, false));
		ClientSendMessageEvents.ALLOW_CHAT.register(message -> !ready() || !The5zigMod.getListener().onSendChatMessage(message));
		ClientSendMessageEvents.ALLOW_COMMAND.register(command -> !ready() || !The5zigMod.getListener().onSendChatMessage("/" + command));
	}

	/** @return true if core wants the message hidden */
	static boolean onServerMessage(Component message, boolean actionBar) {
		String legacy = ChatComponentBuilder.toLegacy(message);
		if (actionBar) {
			return The5zigMod.getListener().onActionBar(legacy);
		}
		return The5zigMod.getListener().onServerChat(legacy, ChatComponentBuilder.toInterface(message), message);
	}

	private static void registerHud() {
		//? if >=1.21.6 {
		HudElementRegistry.attachElementBefore(VanillaHudElements.HOTBAR, id("modules"), (g, delta) -> {
			if (ready()) {
				RenderContext.with(g, () -> The5zigMod.getGuiIngame().renderGameOverlay());
			}
		});
		HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR, id("hotbar_overlay"), (g, delta) -> {
			if (ready()) {
				RenderContext.with(g, () -> The5zigMod.getGuiIngame().onRenderHotbar());
			}
		});
		HudElementRegistry.attachElementAfter(VanillaHudElements.FOOD_BAR, id("food_overlay"), (g, delta) -> {
			if (ready() && Minecraft.getInstance().gameMode != null && Minecraft.getInstance().gameMode.canHurtPlayer()) {
				RenderContext.with(g, () -> The5zigMod.getGuiIngame().onRenderFood());
			}
		});
		HudElementRegistry.attachElementAfter(VanillaHudElements.CHAT, id("second_chat"), (g, delta) -> {
			if (ready()) {
				RenderContext.with(g, () -> MinecraftFactory.getVars().get2ndChat().draw(Mc.guiTicks()));
			}
		});
		HudElementRegistry.replaceElement(VanillaHudElements.STATUS_EFFECTS, vanilla -> (g, delta) -> {
			if (!ready() || The5zigMod.getConfig().getBool("showVanillaPotionIndicator")) {
				vanilla.render(g, delta);
			}
		});
			//?} else {
		/*// No layered HUD API before 1.21.6: draw everything once after the vanilla HUD. The vanilla
		// potion indicator is hidden by MixinGui.
		net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback.EVENT.register((g, delta) -> {
			if (!ready() || Mc.hudHidden()) {
				return;
			}
			RenderContext.with(g, () -> {
				The5zigMod.getGuiIngame().renderGameOverlay();
				The5zigMod.getGuiIngame().onRenderHotbar();
				if (Minecraft.getInstance().gameMode != null && Minecraft.getInstance().gameMode.canHurtPlayer()) {
					The5zigMod.getGuiIngame().onRenderFood();
				}
				MinecraftFactory.getVars().get2ndChat().draw(Mc.guiTicks());
			});
		});
		*///?}
	}

	private static void registerScreens() {
		ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
			if (!ready()) {
				return;
			}
			if (screen instanceof TitleScreen) {
				addLastServerButton(screen, width);
				ScreenEvents.afterRender(screen).register((s, g, mouseX, mouseY, delta) ->
						RenderContext.with(g, () -> MinecraftFactory.getVars().drawString(Version.getVersionDisplay(), 2, 2)));
			} else if (screen instanceof OptionsScreen) {
				Screens.getButtons(screen).add(Button.builder(Component.literal(MinecraftFactory.getClassProxyCallback().translate("menu.the5zigMod")),
						b -> MinecraftFactory.getVars().displayScreen(new GuiSettings(null, "main"))).bounds(width / 2 - 155, height / 6 + 18, 150, 20).build());
			} else if (screen instanceof PauseScreen) {
				addDisconnectConfirmation(screen);
			} else if (screen instanceof DisconnectedScreen disconnected) {
				The5zigMod.getDataManager().getAutoReconnectManager().startCountdown(((DisconnectedScreenAccessor) disconnected).the5zig$getParent());
				ScreenEvents.afterRender(screen).register((s, g, mouseX, mouseY, delta) ->
						RenderContext.with(g, () -> MinecraftFactory.getClassProxyCallback().checkAutoreconnectCountdown(s.width, s.height)));
			} else if (screen instanceof DeathScreen) {
				BytecodeHook.onGuiGameOverInit();
			}
		});
	}

	private static boolean hasKey(AbstractWidget widget, String key) {
		return widget.getMessage().getContents() instanceof TranslatableContents t && t.getKey().equals(key);
	}

	private static void addLastServerButton(Screen screen, int width) {
		if (!MinecraftFactory.getClassProxyCallback().isShowLastServer()) {
			return;
		}
		List<AbstractWidget> buttons = Screens.getButtons(screen);
		AbstractWidget multiplayer = buttons.stream().filter(b -> hasKey(b, "menu.multiplayer")).findFirst().orElse(null);
		if (multiplayer == null) {
			return;
		}
		multiplayer.setWidth(98);
		String server = MinecraftFactory.getClassProxyCallback().getLastServer();
		String label;
		if (server != null) {
			String[] parts = server.split(":");
			String host = parts[parts.length - 2];
			int port = Integer.parseInt(parts[parts.length - 1]);
			label = MinecraftFactory.getVars().shortenToWidth(port == 25565 ? host : host + ":" + port, 88);
		} else {
			label = MinecraftFactory.getClassProxyCallback().translate("menu.no_last_server");
		}
		Button lastServer = Button.builder(Component.literal(label), b -> {
			String last = MinecraftFactory.getClassProxyCallback().getLastServer();
			if (last != null) {
				String[] parts = last.split(":");
				MinecraftFactory.getVars().joinServer(parts[parts.length - 2], Integer.parseInt(parts[parts.length - 1]));
			}
		}).bounds(multiplayer.getX() + 102, multiplayer.getY(), 98, 20).build();
		lastServer.active = server != null;
		buttons.add(lastServer);
	}

	private static void addDisconnectConfirmation(Screen screen) {
		if (!The5zigMod.getConfig().getBool("confirmDisconnect")) {
			return;
		}
		List<AbstractWidget> buttons = Screens.getButtons(screen);
		for (int i = 0; i < buttons.size(); i++) {
			AbstractWidget widget = buttons.get(i);
			if (widget instanceof Button original && (hasKey(widget, "menu.disconnect") || hasKey(widget, "menu.returnToMenu"))) {
				Button confirm = Button.builder(original.getMessage(), b -> MinecraftFactory.getVars().displayScreen(new GuiYesNo(
						MinecraftFactory.getVars().createWrappedGui(screen), new YesNoCallback() {
					@Override
					public void onDone(boolean yes) {
						if (yes) {
							//? if >=1.21.9 {
							/*original.onPress(new net.minecraft.client.input.KeyEvent(com.mojang.blaze3d.platform.InputConstants.KEY_RETURN, 0, 0));
							*///?} else {
							original.onPress();
							//?}
						}
					}

					@Override
					public String title() {
						return io.icanlevitateoff.fivezigrebornrevived.mod.I18n.translate("confirm_disconnect.title");
					}
				}))).bounds(original.getX(), original.getY(), original.getWidth(), original.getHeight()).build();
				buttons.remove(i);
				buttons.add(confirm);
				return;
			}
		}
	}
}
