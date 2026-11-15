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

package io.icanlevitateoff.fivezigrebornrevived.mod;

import eu.the5zig.mod.*;

import eu.the5zig.util.minecraft.ChatColor;

import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Manifest;

public class Version {

    public static final String VERSION;
    public static final String MCVERSION;
    public static final int LANGVERSION = 44;
    public static final int PROTOCOL = 7;
    public static final int APIVERSION = 4;
    public static final String BETA;
    public static String UPDATE;

    public static String getVersionDisplay() {
        return ChatColor.GOLD + getNameAndVersion();
    }

    /** e.g. "5zig Reborn Revived 1.21.8-v0.1.0" */
    public static String getNameAndVersion() {
        return "5zig Reborn Revived " + Version.VERSION;
    }

    static {
        String version = "DEV", mcVersion = "unknown", beta = null;

        // Fabric knows both versions, also in a dev run (fabric.mod.json is expanded at build time).
        try {
            version = net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer("fivezigrebornrevived")
                    .map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse(version);
            mcVersion = net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer("minecraft")
                    .map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse(mcVersion);
        } catch (Throwable ignored) {
            // not running under Fabric: the jar manifest below still has them
        }

        try {
            Enumeration<URL> resources = Version.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
            while (resources.hasMoreElements()) {
                Manifest manifest = new Manifest(resources.nextElement().openStream());
                if (manifest.getMainAttributes().getValue("5zig-Version") != null) {
                    if ("DEV".equals(version)) {
                        version = manifest.getMainAttributes().getValue("5zig-Version");
                    }
                    if ("unknown".equals(mcVersion)) {
                        mcVersion = manifest.getMainAttributes().getValue("Minecraft-Version");
                    }
                    beta = manifest.getMainAttributes().getValue("5zig-Build");
                }
            }
        } catch (Exception ex) {
            System.out.println("Corrupted file/Dev env");
        }

        VERSION = version;
        MCVERSION = mcVersion;
        BETA = beta;
    }

}
