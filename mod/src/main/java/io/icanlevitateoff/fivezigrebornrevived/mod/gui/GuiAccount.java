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

import eu.the5zig.util.minecraft.ChatColor;
import io.icanlevitateoff.fivezigrebornrevived.mod.I18n;
import io.icanlevitateoff.fivezigrebornrevived.mod.The5zigMod;
import io.icanlevitateoff.fivezigrebornrevived.mod.account.Account;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.elements.IButton;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.elements.ITextfield;

/**
 * Created by RoccoDev on 2019-08-26.
 */
public class GuiAccount extends Gui {

    private ITextfield userField;
    private Account account;

    public GuiAccount(Gui lastScreen) {
        super(lastScreen);
    }

    public GuiAccount(Account account, Gui lastScreen) {
        this(lastScreen);
        this.account = account;
    }

    @Override
    public void initGui() {
        addTextField(userField = The5zigMod.getVars().createTextfield(1, getWidth() / 2 - 150,
                getHeight() / 3, 300, 20));
        userField.callSetMaxStringLength(16);
        addButton(The5zigMod.getVars().createButton(201, getWidth() / 2 - 100, getHeight() - 35, The5zigMod.getVars().translate("gui.done")));
        if (account != null) {
            userField.callSetText(account.getName());
        }
    }

    @Override
    protected void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawCenteredString(I18n.translate("account_manager.offline_name"), getWidth() / 2, getHeight() / 3 - 10);
        drawCenteredString(ChatColor.GRAY + I18n.translate("account_manager.offline_hint"), getWidth() / 2, getHeight() / 3 + 28);
    }

    @Override
    protected void actionPerformed(IButton button) {
        if (button.getId() == 201) {
            String user = userField.callGetText();
            if (user != null && !user.isEmpty()) {
                if (account != null) {
                    account.setName(user);
                    The5zigMod.getAccountManager().save();
                } else {
                    The5zigMod.getAccountManager().addOfflineAccount(user);
                }
            }
            The5zigMod.getVars().displayScreen(lastScreen);
        }
    }

    @Override
    public String getTitleKey() {
        return "account_manager.add_offline_title";
    }
}
