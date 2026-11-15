/*
 * Copyright (c) 2019-2020 5zig Reborn
 * Copyright (c) 2026 IcanLevitate
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

package io.icanlevitateoff.fivezigrebornrevived.fabric.mixin;

import io.icanlevitateoff.fivezigrebornrevived.fabric.compat.Draw;
import io.icanlevitateoff.fivezigrebornrevived.mod.The5zigMod;
import eu.the5zig.util.minecraft.ChatColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Option: show the ping in ms instead of the signal-bars icon. */
@Mixin(PlayerTabOverlay.class)
public abstract class MixinPlayerTabOverlay {

	@Inject(method = "renderPingIcon", at = @At("HEAD"), cancellable = true)
	private void the5zig$ping(GuiGraphics graphics, int width, int x, int y, PlayerInfo info, CallbackInfo ci) {
		if (!The5zigMod.hasBeenInitialized() || !The5zigMod.getConfig().getBool("pingOnTab")) {
			return;
		}
		int ping = info.getLatency();
		String display = ping <= 0 ? "?" : Integer.toString(ping);
		int color = 0xFF000000 | ChatColor.getPingColor(ping);
		Draw.push(graphics);
		Draw.scale(graphics, 0.5F, 0.5F);
		Draw.centeredText(graphics, Minecraft.getInstance().font, display, (x + width) * 2 - 11, y * 2 + 3, color);
		Draw.pop(graphics);
		ci.cancel();
	}
}
