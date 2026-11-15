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

public class The5zigRichPresence {
    private String text, state, smallImage;

    static The5zigRichPresence getDefault() {
        The5zigRichPresence presence = new The5zigRichPresence();
        presence.text = "Menu";
        presence.state = "Menu";
        return presence;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setSmallImage(String smallImage) {
        this.smallImage = smallImage;
    }

    /** The "activity" object of Discord's SET_ACTIVITY command. */
    JsonObject toJson(long startSeconds) {
        JsonObject activity = new JsonObject();
        if (text != null) {
            activity.addProperty("details", text);
        }
        if (state != null) {
            activity.addProperty("state", state);
        }
        JsonObject timestamps = new JsonObject();
        timestamps.addProperty("start", startSeconds);
        activity.add("timestamps", timestamps);
        JsonObject assets = new JsonObject();
        assets.addProperty("large_image", DiscordRPCManager.IMG_LOGO);
        if (smallImage != null) {
            assets.addProperty("small_image", smallImage);
        }
        activity.add("assets", assets);
        return activity;
    }
}
