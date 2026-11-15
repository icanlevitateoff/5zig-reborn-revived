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

package io.icanlevitateoff.fivezigrebornrevived.mod.gui;

import eu.the5zig.util.minecraft.ChatColor;
import io.icanlevitateoff.fivezigrebornrevived.mod.I18n;
import io.icanlevitateoff.fivezigrebornrevived.mod.The5zigMod;
import io.icanlevitateoff.fivezigrebornrevived.mod.account.MicrosoftAuth;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.elements.IButton;
import io.icanlevitateoff.fivezigrebornrevived.util.BrowseUrl;

import java.net.URL;

/**
 * Microsoft sign-in with a device code: shows the code and the link, waits in the background
 * until the player has signed in on any device, then adds the account.
 */
public class GuiMicrosoftLogin extends Gui {

	private volatile MicrosoftAuth.DeviceCode code;
	private volatile String status;
	private volatile boolean error;
	private volatile boolean cancelled;
	private Thread worker;

	public GuiMicrosoftLogin(Gui lastScreen) {
		super(lastScreen);
	}

	@Override
	public void initGui() {
		addButton(The5zigMod.getVars().createButton(1, getWidth() / 2 - 155, getHeight() / 2 + 30, 150, 20, I18n.translate("account_manager.microsoft.open")));
		addButton(The5zigMod.getVars().createButton(2, getWidth() / 2 + 5, getHeight() / 2 + 30, 150, 20, I18n.translate("account_manager.microsoft.copy")));
		addButton(The5zigMod.getVars().createButton(200, getWidth() / 2 - 100, getHeight() - 35, The5zigMod.getVars().translate("gui.cancel")));
		if (worker == null) {
			start();
		}
	}

	private void start() {
		status = I18n.translate("account_manager.microsoft.requesting");
		worker = new Thread(() -> {
			try {
				code = MicrosoftAuth.requestDeviceCode();
				status = I18n.translate("account_manager.microsoft.waiting");
				MicrosoftAuth.Result result = MicrosoftAuth.awaitLogin(code, () -> cancelled);
				if (!cancelled) {
					The5zigMod.getAccountManager().addMicrosoftAccount(result);
					The5zigMod.getVars().runOnMainThread(() -> The5zigMod.getVars().displayScreen(lastScreen));
				}
			} catch (Exception e) {
				if (!cancelled) {
					The5zigMod.logger.warn("Microsoft login failed: " + e.getMessage());
					error = true;
					status = e instanceof MicrosoftAuth.AuthException ? e.getMessage() : I18n.translate("account_manager.microsoft.network_error");
				}
			}
		}, "5zig Microsoft Login");
		worker.setDaemon(true);
		worker.start();
	}

	@Override
	protected void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		int y = getHeight() / 2 - 50;
		MicrosoftAuth.DeviceCode current = code;
		if (current != null && !error) {
			drawCenteredString(I18n.translate("account_manager.microsoft.step1"), getWidth() / 2, y);
			drawCenteredString(ChatColor.AQUA + current.verificationUri, getWidth() / 2, y + 12);
			drawCenteredString(I18n.translate("account_manager.microsoft.step2"), getWidth() / 2, y + 30);
			The5zigMod.getVars().drawString(ChatColor.BOLD + current.userCode, getWidth() / 2 - The5zigMod.getVars().getStringWidth(ChatColor.BOLD + current.userCode) / 2, y + 42);
		}
		String text = status;
		if (text != null) {
			int line = 0;
			for (String part : The5zigMod.getVars().splitStringToWidth((error ? ChatColor.RED : ChatColor.GRAY) + text, getWidth() - 40)) {
				drawCenteredString(part, getWidth() / 2, getHeight() / 2 + 60 + line++ * 11);
			}
		}
	}

	@Override
	protected void actionPerformed(IButton button) {
		MicrosoftAuth.DeviceCode current = code;
		if (button.getId() == 1 && current != null) {
			try {
				BrowseUrl.get().openURL(new URL(current.verificationUri));
			} catch (Exception e) {
				status = current.verificationUri;
			}
		} else if (button.getId() == 2 && current != null) {
			The5zigMod.getVars().setClipboard(current.userCode);
			status = I18n.translate("account_manager.microsoft.copied");
		} else if (button.getId() == 200) {
			cancelled = true;
			The5zigMod.getVars().displayScreen(lastScreen);
		}
	}

	@Override
	public String getTitleKey() {
		return "account_manager.microsoft.title";
	}
}
