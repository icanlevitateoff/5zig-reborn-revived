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

package io.icanlevitateoff.fivezigrebornrevived.mod.account;

import io.icanlevitateoff.fivezigrebornrevived.util.GameProfiles;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.mojang.authlib.exceptions.AuthenticationException;
import io.icanlevitateoff.fivezigrebornrevived.mod.I18n;
import io.icanlevitateoff.fivezigrebornrevived.mod.The5zigMod;
import eu.the5zig.mod.event.AccountSwitchEvent;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by RoccoDev on 2019-08-25.
 */
public class AccountManager {

    private static ExecutorService POOL;

    private static Gson gson;
    private static Type accountsType;

    private boolean encrypted;
    private String password;

    private boolean isNewManager;

    private ArrayList<Account> accounts;

    static {
        gson = new Gson();
        accountsType = new TypeToken<List<Account>>(){}.getType();

        POOL = Executors.newFixedThreadPool(1);
    }

    public AccountManager() {
        isNewManager = !new File(The5zigMod.getModDirectory(), "accounts.enc").exists();
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isNewManager() {
        return isNewManager;
    }

    public ArrayList<Account> getAccounts() {
        return accounts;
    }

    /**
     * Adds (or updates) a Microsoft account after a successful device-code login and switches to it.
     */
    public void addMicrosoftAccount(MicrosoftAuth.Result result) {
        Account account = accounts.stream().filter(a -> result.uuid.equals(a.getUuid())).findFirst().orElse(null);
        if (account == null) {
            account = new Account(null, null, result.uuid, result.name, "msa");
            accounts.add(account);
        }
        account.setName(result.name);
        account.setRefreshToken(result.refreshToken);
        applyMicrosoft(account, result);
    }

    /**
     * Offline accounts may only be added once the manager holds a Microsoft account (one that
     * owns Minecraft, since adding it checked the profile).
     */
    public boolean hasAuthenticatedAccount() {
        return accounts != null && accounts.stream().anyMatch(a -> a.isMicrosoft() && a.getRefreshToken() != null);
    }

    public void addOfflineAccount(String username) {
        if (!hasAuthenticatedAccount()) {
            The5zigMod.getOverlayMessage().displayMessageAndSplit(I18n.translate("account_manager.offline_requires_microsoft"));
            return;
        }
        Account account = new Account(null, null, username, username, "legacy");
        accounts.add(account);

        The5zigMod.getVars().setSession(username, username, "0", "legacy");
        selectOnly(account);
        updateSettings();
        try {
            save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Switches to the account. Offline accounts switch immediately; Microsoft accounts log in again
     * with their stored refresh token in the background, so this returns before that finishes.
     */
    public void login(Account account) throws AuthenticationException {
        if (account.isMicrosoft()) {
            if (account.getRefreshToken() == null) {
                throw new AuthenticationException("This Microsoft account has no saved login. Remove it and add it again.");
            }
            The5zigMod.getOverlayMessage().displayMessage("Microsoft login", "Logging in as " + account.getName() + "...");
            POOL.execute(() -> {
                try {
                    MicrosoftAuth.Result result = MicrosoftAuth.refresh(account.getRefreshToken());
                    account.setRefreshToken(result.refreshToken);
                    applyMicrosoft(account, result);
                } catch (Exception e) {
                    The5zigMod.logger.warn("Microsoft login failed for " + account.getName() + ": " + e.getMessage());
                    The5zigMod.getOverlayMessage().displayMessageAndSplit("Login failed: " + e.getMessage());
                }
            });
            return;
        }
        if (!"legacy".equals(account.getAccountType())) {
            throw new AuthenticationException("Mojang accounts no longer exist. Remove this account and add it as a Microsoft account.");
        }
        The5zigMod.getVars().setSession(account.getName(), account.getName(), "0", "legacy");
        selectOnly(account);
        updateSettings();
        The5zigMod.getOverlayMessage().displayMessage("Login Success", "Logged in as " + account.getName());
    }

    private void applyMicrosoft(Account account, MicrosoftAuth.Result result) {
        The5zigMod.getVars().runOnMainThread(() -> {
            The5zigMod.getVars().setSession(result.name, result.uuid, result.accessToken, "msa");
            selectOnly(account);
            updateSettings();
            save();
            The5zigMod.getOverlayMessage().displayMessage("Login Success", "Logged in as " + result.name);
        });
    }

    private void selectOnly(Account account) {
        for (Account other : accounts) {
            other.setSelected(other == account);
        }
    }

    public void save() {
        JsonObject json = new JsonObject();

        json.add("accounts", gson.toJsonTree(accounts, accountsType));

        POOL.execute(() -> {
            try {
                AccountsFileEncryption.encrypt(password, json);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void updateSettings() {
        The5zigMod.reloadDataManager();
        The5zigMod.getNetworkManager().reloadNow();
        The5zigMod.getListener().fireEvent(new AccountSwitchEvent());
        if(The5zigMod.getVars().getServer() != null) {
            String host = The5zigMod.getVars().getServer();
            int port = 25565;
            if(host.contains(":")) {
                String[] data = host.split(":");
                host = data[0];
                port = Integer.parseInt(data[1]);
            }
            The5zigMod.getVars().joinServer(host, port);
        }
    }

    public void read() throws Exception {
        if(isNewManager) {
            create();
            return;
        }

        JsonObject json = AccountsFileEncryption.decrypt(password);
        accounts = gson.fromJson(json.getAsJsonArray("accounts"), accountsType);

        Optional<Account> self = accounts.stream().filter(account -> {
            if(GameProfiles.id(The5zigMod.getDataManager().getGameProfile()) != null) {
                return GameProfiles.id(The5zigMod.getDataManager().getGameProfile()).toString().equals(account.getUuid());
            }
            return false;
        }).findAny();
        self.ifPresent(account -> account.setSelected(true));
    }

    private void create() throws Exception {
        accounts = new ArrayList<>();
        new File(The5zigMod.getModDirectory(), "accounts.enc").createNewFile();
        save();
    }

}
