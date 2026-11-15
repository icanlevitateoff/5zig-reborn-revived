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

package io.icanlevitateoff.fivezigrebornrevived.mod.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.the5zig.util.minecraft.ChatColor;
import io.icanlevitateoff.fivezigrebornrevived.mod.I18n;
import io.icanlevitateoff.fivezigrebornrevived.mod.The5zigMod;
import io.icanlevitateoff.fivezigrebornrevived.mod.Version;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Checks the GitHub releases of 5zig Reborn Revived. The mod version is the part after "-v" in
 * {@link Version#VERSION} ("1.21.8-v0.1.2" -> 0.1.2, "1.21.8" being the Minecraft version the jar
 * was built for). An update is only reported when a newer release has a jar for that same build.
 */
public class Updater implements Runnable {

    public static final String RELEASES_PAGE = "https://github.com/icanlevitateoff/5zig-reborn-revived/releases";
    private static final String RELEASES_API = "https://api.github.com/repos/icanlevitateoff/5zig-reborn-revived/releases?per_page=30";

    private Updater() {
    }

    public static void check() {
        Thread thread = new Thread(new Updater(), "5zig Update Check");
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void run() {
        int split = Version.VERSION.lastIndexOf("-v");
        if (split <= 0) {
            The5zigMod.logger.info("Not checking for updates (development build " + Version.VERSION + ").");
            return;
        }
        String build = Version.VERSION.substring(0, split);
        String current = Version.VERSION.substring(split + 2);
        try {
            String latest = null;
            for (JsonElement element : fetchReleases()) {
                JsonObject release = element.getAsJsonObject();
                if (release.get("draft").getAsBoolean() || !release.has("tag_name")) {
                    continue;
                }
                String tag = release.get("tag_name").getAsString();
                String version = tag.startsWith("v") ? tag.substring(1) : tag;
                if (!version.matches("\\d+(\\.\\d+)*") || !hasJarFor(release, build, version)) {
                    continue;
                }
                if (latest == null || compare(version, latest) > 0) {
                    latest = version;
                }
            }
            if (latest != null && compare(latest, current) > 0) {
                Version.UPDATE = "stable";
                The5zigMod.logger.info("5zig Reborn Revived " + latest + " is available (running " + current + "): " + RELEASES_PAGE);
                The5zigMod.getOverlayMessage().displayMessageAndSplit(ChatColor.YELLOW + I18n.translate("update.1"));
                The5zigMod.getOverlayMessage().displayMessageAndSplit(ChatColor.YELLOW + I18n.translate("update.2"));
            } else {
                The5zigMod.logger.info("5zig Reborn Revived is up to date (" + current + ").");
            }
        } catch (Exception e) {
            The5zigMod.logger.warn("Could not check for updates: " + e);
        }
    }

    private static JsonArray fetchReleases() throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(RELEASES_API).openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);
        connection.addRequestProperty("Accept", "application/vnd.github+json");
        connection.addRequestProperty("User-Agent", "5zig-Reborn-Revived/" + Version.VERSION);
        try (Reader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonArray();
        } finally {
            connection.disconnect();
        }
    }

    /** True if the release has the jar for this Minecraft build, e.g. 5zig-Reborn-Revived-1.21.8-v0.1.3.jar. */
    private static boolean hasJarFor(JsonObject release, String build, String version) {
        String expected = "5zig-Reborn-Revived-" + build + "-v" + version + ".jar";
        if (!release.has("assets")) {
            return false;
        }
        for (JsonElement asset : release.getAsJsonArray("assets")) {
            if (expected.equals(asset.getAsJsonObject().get("name").getAsString())) {
                return true;
            }
        }
        return false;
    }

    /** Numeric comparison of dotted versions: 0.1.10 > 0.1.9, 0.2 > 0.1.9. */
    static int compare(String a, String b) {
        String[] x = a.split("\\."), y = b.split("\\.");
        for (int i = 0; i < Math.max(x.length, y.length); i++) {
            int p = i < x.length ? Integer.parseInt(x[i]) : 0;
            int q = i < y.length ? Integer.parseInt(y[i]) : 0;
            if (p != q) {
                return Integer.compare(p, q);
            }
        }
        return 0;
    }

    public enum UpdateType {

        ALWAYS, SAME_VERSION, NEVER

    }

}
