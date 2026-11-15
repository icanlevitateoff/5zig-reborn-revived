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

import eu.the5zig.mod.gui.*;

import com.mojang.authlib.exceptions.AuthenticationException;
import io.icanlevitateoff.fivezigrebornrevived.mod.I18n;
import io.icanlevitateoff.fivezigrebornrevived.mod.The5zigMod;
import io.icanlevitateoff.fivezigrebornrevived.mod.account.Account;
import io.icanlevitateoff.fivezigrebornrevived.mod.account.MicrosoftAuth;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.elements.Clickable;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.elements.IButton;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.elements.IGuiList;

/**
 * Created by RoccoDev on 2019-08-26.
 */
public class GuiAccountManager extends Gui implements Clickable<Account> {

    private IGuiList<Account> guiList;

    public GuiAccountManager(Gui lastScreen) {
        super(lastScreen);
    }

    @Override
    public void initGui() {
        // Accounts file still encrypted
        if(The5zigMod.getAccountManager().getAccounts() == null) {
            GuiCenteredTextfield field = new GuiCenteredTextfield(this, new CenteredTextfieldCallback() {
                @Override
                public void onDone(String text) {
                    The5zigMod.getAccountManager().setPassword(text);
                    try {
                        The5zigMod.getAccountManager().read();
                    } catch (Exception ignored) {
                        // Password was incorrect.
                    }
                }

                @Override
                public void onCancel(Gui lastScreen) {
                    The5zigMod.getVars().displayScreen(lastScreen.lastScreen);
                }

                @Override
                public String title() {
                    return The5zigMod.getAccountManager().isNewManager()
                            ? I18n.translate("account_manager.create")
                            : I18n.translate("account_manager.enter_password");
                }
            }, 16);
            field.setIsPassword(true);
            The5zigMod.getVars().displayScreen(field);
        }

        addButton(The5zigMod.getVars().createButton(200, 8, 6, 50, 20, I18n.translate("gui.back")));
        guiList = The5zigMod.getVars().createGuiList(this, getWidth(), getHeight(), 32,
                getHeight() - 32, 0, getWidth(), The5zigMod.getAccountManager().getAccounts());
        guiList.setRowWidth(250);
        guiList.setDrawSelection(true);
        addGuiList(guiList);

        int x = getWidth() / 2 - 190;
        addButton(The5zigMod.getVars().createButton(5, x, getHeight() - 27, 74, 20, I18n.translate("account_manager.add_microsoft")));
        IButton offline = The5zigMod.getVars().createButton(1, x + 76, getHeight() - 27, 74, 20, I18n.translate("account_manager.add_offline"));
        offline.setEnabled(The5zigMod.getAccountManager().hasAuthenticatedAccount());
        addButton(offline);
        addButton(The5zigMod.getVars().createButton(2, x + 152, getHeight() - 27, 74, 20, I18n.translate("account_manager.remove")));
        addButton(The5zigMod.getVars().createButton(3, x + 228, getHeight() - 27, 74, 20, I18n.translate("account_manager.edit_btn")));
        addButton(The5zigMod.getVars().createButton(4, x + 304, getHeight() - 27, 76, 20, I18n.translate("account_manager.change_password")));
    }

    @Override
    protected void actionPerformed(IButton button) {
        if(button.getId() == 5) { // Add Microsoft
            if (!MicrosoftAuth.isConfigured()) {
                The5zigMod.getOverlayMessage().displayMessageAndSplit(I18n.translate("account_manager.microsoft.not_configured"));
                return;
            }
            The5zigMod.getVars().displayScreen(new GuiMicrosoftLogin(this));
        }
        else if(button.getId() == 1) { // Add offline (only once a Microsoft account is present)
            if (!The5zigMod.getAccountManager().hasAuthenticatedAccount()) {
                The5zigMod.getOverlayMessage().displayMessageAndSplit(I18n.translate("account_manager.offline_requires_microsoft"));
                return;
            }
            The5zigMod.getVars().displayScreen(new GuiAccount(this));
        }
        else if(button.getId() == 2) { // Remove
            synchronized (guiList.getRows()) {
                int selected = guiList.getSelectedId();
                if(selected < The5zigMod.getAccountManager().getAccounts().size()) {
                    The5zigMod.getAccountManager().getAccounts().remove(selected);
                    guiList.calculateHeightMap();
                }
            }
            The5zigMod.getAccountManager().save();
        }
        else if(button.getId() == 3) { // Edit (offline names only; Microsoft names come from the profile)
            Account selected = guiList.getSelectedRow();
            if (selected == null || selected.isMicrosoft()) {
                return;
            }
            The5zigMod.getVars().displayScreen(new GuiAccount(selected, this));
        }
        else if(button.getId() == 4) { // Change password
            GuiCenteredTextfield field = new GuiCenteredTextfield(this, new CenteredTextfieldCallback() {
                @Override
                public void onDone(String text) {
                    The5zigMod.getAccountManager().setPassword(text);
                    The5zigMod.getAccountManager().save();
                }

                @Override
                public String title() {
                    return I18n.translate("account_manager.password");
                }
            });
            field.setIsPassword(true);
            The5zigMod.getVars().displayScreen(field);
        }
    }

    @Override
    public String getTitleKey() {
        return "account_manager.title";
    }

    @Override
    public void onSelect(int id, Account row, boolean doubleClick) {
        if(!row.isClickable()) return;
        if(doubleClick) {
            try {
                The5zigMod.getAccountManager().login(row);
            } catch (AuthenticationException e) {
                e.printStackTrace();
                The5zigMod.getOverlayMessage().displayMessageAndSplit(e.getMessage());
            }
        }
    }
}
