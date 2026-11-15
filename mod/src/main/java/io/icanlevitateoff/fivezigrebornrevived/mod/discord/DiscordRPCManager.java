/*
 * Copyright (c) 2019-2020 5zig Reborn
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


package io.icanlevitateoff.fivezigrebornrevived.mod.discord;

import com.google.gson.JsonObject;
import io.icanlevitateoff.fivezigrebornrevived.mod.The5zigMod;
import eu.the5zig.mod.event.EventHandler;
import eu.the5zig.mod.event.ServerQuitEvent;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Discord Rich Presence. All talking to Discord happens on one background thread, so a slow or
 * missing Discord never affects the game. If Discord is not running, the next presence update
 * tries again (at most every 30 seconds), so starting Discord later still works.
 */
public class DiscordRPCManager {

    /** The "5zig Reborn Revived" Discord application (its name is what Discord shows as "Playing ..."). */
    public static final long CLIENT_ID = 1571655305882894446L;
    /** Art asset key for the large image; it has to be uploaded to the application under Rich Presence → Art Assets. */
    public static final String IMG_LOGO = "logo";

    private static final long RETRY_MS = 30_000;

    private final ExecutorService thread = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "5zig Discord");
        t.setDaemon(true);
        return t;
    });
    private final long startedAt = System.currentTimeMillis() / 1000;

    private volatile boolean enabled;
    private DiscordIpc ipc;
    private long lastAttempt;
    private The5zigRichPresence current;

    public void init() {
        if (!The5zigMod.getConfig().getBool("discord")) {
            return;
        }
        enabled = true;
        The5zigMod.getListener().registerListener(this);
        setDefault();
    }

    public void clearPresence() {
        submit(null);
    }

    public void close() {
        thread.execute(this::disconnect);
    }

    public void disable() {
        enabled = false;
        thread.execute(() -> {
            if (ipc != null) {
                try {
                    sendActivity(null);
                } catch (IOException ignored) {
                }
            }
            disconnect();
        });
    }

    public void setDefault() {
        setPresence(The5zigRichPresence.getDefault());
    }

    public void setPresence(The5zigRichPresence presence) {
        submit(presence);
    }

    private void submit(The5zigRichPresence presence) {
        if (!enabled) {
            return;
        }
        thread.execute(() -> {
            current = presence;
            try {
                if (ipc == null && !connect()) {
                    return;
                }
                sendActivity(presence);
            } catch (IOException | RuntimeException e) {
                The5zigMod.logger.debug("Discord presence update failed: " + e.getMessage());
                disconnect();
            }
        });
    }

    /** Connects and does the handshake; false if Discord is not there (tried again later). */
    private boolean connect() throws IOException {
        long now = System.currentTimeMillis();
        if (now - lastAttempt < RETRY_MS) {
            return false;
        }
        lastAttempt = now;
        try {
            ipc = DiscordIpc.open();
        } catch (IOException e) {
            The5zigMod.logger.debug("Discord not found: " + e.getMessage());
            return false;
        }
        JsonObject handshake = new JsonObject();
        handshake.addProperty("v", 1);
        handshake.addProperty("client_id", Long.toString(CLIENT_ID));
        ipc.send(DiscordIpc.OP_HANDSHAKE, handshake);
        JsonObject ready = ipc.read();
        if (ready.get("__op").getAsInt() == DiscordIpc.OP_CLOSE || !"READY".equals(str(ready, "evt"))) {
            String reason = str(ready, "message");
            disconnect();
            throw new IOException("Discord refused the connection" + (reason == null ? "" : ": " + reason));
        }
        String user = null;
        if (ready.has("data") && ready.getAsJsonObject("data").has("user")) {
            user = str(ready.getAsJsonObject("data").getAsJsonObject("user"), "username");
        }
        The5zigMod.logger.info("Connected to Discord" + (user == null ? "." : " as " + user + "."));
        return true;
    }

    private void sendActivity(The5zigRichPresence presence) throws IOException {
        JsonObject args = new JsonObject();
        args.addProperty("pid", ProcessHandle.current().pid());
        if (presence != null) {
            args.add("activity", presence.toJson(startedAt));
        }
        JsonObject frame = new JsonObject();
        frame.addProperty("cmd", "SET_ACTIVITY");
        frame.add("args", args);
        frame.addProperty("nonce", UUID.randomUUID().toString());
        ipc.send(DiscordIpc.OP_FRAME, frame);
        JsonObject reply = ipc.read();
        if ("ERROR".equals(str(reply, "evt"))) {
            The5zigMod.logger.debug("Discord rejected the presence: " + reply);
        }
    }

    private void disconnect() {
        if (ipc != null) {
            ipc.close();
            ipc = null;
        }
    }

    private static String str(JsonObject json, String key) {
        return json != null && json.has(key) && json.get(key).isJsonPrimitive() ? json.get(key).getAsString() : null;
    }

    @EventHandler
    public void onDisconnect(ServerQuitEvent event) {
        setDefault();
    }
}
