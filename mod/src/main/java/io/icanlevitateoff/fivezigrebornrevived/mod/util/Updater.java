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

import eu.the5zig.mod.util.*;

import io.icanlevitateoff.fivezigrebornrevived.mod.I18n;
import io.icanlevitateoff.fivezigrebornrevived.mod.The5zigMod;
import io.icanlevitateoff.fivezigrebornrevived.mod.Version;
import eu.the5zig.util.minecraft.ChatColor;
import org.apache.commons.io.IOUtils;

import java.net.HttpURLConnection;
import java.net.URL;

public class Updater implements Runnable {

    private Updater() {
    }

    public static void check() {
        new Thread(new Updater(), "Update Thread").start();
    }

    @Override
    public void run() {
        try {
            URL url = new URL((The5zigMod.DEBUG ? "http://localhost:8080" : "https://secure.5zigreborn.eu") + "/version?mc=" + Version.MCVERSION + "&new=1");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.addRequestProperty("User-Agent", "5zig/" + Version.VERSION);
            Download download = The5zigMod.gson.fromJson(IOUtils.toString(connection.getInputStream()), Download.class);
            connection.disconnect();
            if (!"DEV".equals(Version.VERSION) && !Version.VERSION.contains("_b") && !Version.VERSION.equals(download.latest)) {
                Version.UPDATE = "stable";
                The5zigMod.logger.info("Found new update of The 5zig Mod (v" + download.latest + ")!");
                The5zigMod.getOverlayMessage().displayMessageAndSplit(ChatColor.YELLOW + I18n.translate("update.1"));
                The5zigMod.getOverlayMessage().displayMessageAndSplit(ChatColor.YELLOW + I18n.translate("update.2"));
            } else {
                The5zigMod.logger.info("The 5zig Mod is up to date!");
            }
        } catch (Exception e) {
            The5zigMod.logger.error("Could not check for latest 5zig Mod Version!", e);
        }
    }

    private static class Download {
        public String latest;
    }

    private static class BetaUpdate {
        public boolean update;
        public boolean beta;
    }

    public enum UpdateType {

        ALWAYS, SAME_VERSION, NEVER

    }

}
