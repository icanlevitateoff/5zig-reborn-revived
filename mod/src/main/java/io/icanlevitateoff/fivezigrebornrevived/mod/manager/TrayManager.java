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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.icanlevitateoff.fivezigrebornrevived.mod.MinecraftFactory;
import io.icanlevitateoff.fivezigrebornrevived.mod.The5zigMod;
import io.icanlevitateoff.fivezigrebornrevived.util.Utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.concurrent.Executors;

/**
 * Created by 5zig.
 * All rights reserved © 2015
 */
public class TrayManager implements Runnable {

	private boolean isTraySupported;
	private SystemTray tray;
	private TrayIcon trayIcon;
	private Image minecraftIcon;
	private boolean trayInitialized = false;

	public TrayManager() {
		Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("Tray").setDaemon(true).build()).execute(this);
	}

	@Override
	public void run() {
		isTraySupported = Utils.getPlatform() == Utils.Platform.WINDOWS && SystemTray.isSupported();
		if (isTraySupported) {
			Utils.setUI(UIManager.getSystemLookAndFeelClassName());
			tray = SystemTray.getSystemTray();
			loadMinecraftIcon();
			create();
		} else {
			MinecraftFactory.getClassProxyCallback().getLogger().warn("System Tray is NOT Supported!");
		}
	}

	private void loadMinecraftIcon() {
		if (minecraftIcon != null)
			return;
		try {
			minecraftIcon = ImageIO.read(The5zigMod.getVars().getMinecraftIcon());

			trayIcon = new TrayIcon(minecraftIcon, The5zigMod.NAME + " - " + MinecraftFactory.getVars().getUsername());

			PopupMenu popupMenu = new PopupMenu();
			MenuItem about = new MenuItem("About");
			about.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JOptionPane.showMessageDialog(null, "This Tray Icon is used by " + The5zigMod.NAME + " to notify you, when you receive a new Message.",
							"Minecraft " + MinecraftFactory.getClassProxyCallback().getMinecraftVersion(), JOptionPane.PLAIN_MESSAGE, null);
				}
			});
			popupMenu.add(about);
			popupMenu.addSeparator();
			MenuItem disable = new MenuItem("Disable");
			disable.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					MinecraftFactory.getClassProxyCallback().disableTray();

					destroy();
				}
			});
			popupMenu.add(disable);
			trayIcon.setPopupMenu(popupMenu);
		} catch (Exception e) {
			MinecraftFactory.getClassProxyCallback().getLogger().warn("Could not load Minecraft Icon!", e);
		}
	}

	public void create() {
		if (!isTraySupported || trayInitialized || !MinecraftFactory.getClassProxyCallback().isTrayEnabled() || minecraftIcon == null)
			return;

		MinecraftFactory.getClassProxyCallback().getLogger().info("Setting up Tray Icon...");
		try {
			tray.add(trayIcon);
			trayInitialized = true;
		} catch (Exception e) {
			MinecraftFactory.getClassProxyCallback().getLogger().warn("Could not Setup Tray Icon!", e);
		}
	}

	public boolean isTraySupported() {
		return isTraySupported;
	}

	public void displayMessage(String title, String message) {
		if (Utils.getPlatform() == Utils.Platform.MAC) {
			try {
				// Title and message go in as arguments, never into the script text: they can contain
				// chat from other players, and quotes there would otherwise end the AppleScript string.
				new ProcessBuilder("osascript",
						"-e", "on run argv",
						"-e", "display notification (item 1 of argv) with title (item 2 of argv)",
						"-e", "end run",
						String.valueOf(message), String.valueOf(title)).start();
			} catch (IOException e) {
				MinecraftFactory.getClassProxyCallback().getLogger().error("Could not display notification", e);
			}
		} else {
			if (trayInitialized) {
				trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
			}
		}
	}

	public void destroy() {
		if (trayInitialized) {
			tray.remove(trayIcon);
			trayInitialized = false;
		}
	}
}
