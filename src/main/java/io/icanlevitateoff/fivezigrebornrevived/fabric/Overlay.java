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

package io.icanlevitateoff.fivezigrebornrevived.fabric;

import io.icanlevitateoff.fivezigrebornrevived.fabric.compat.Draw;
import io.icanlevitateoff.fivezigrebornrevived.mod.MinecraftFactory;
import eu.the5zig.mod.gui.IOverlay;
import eu.the5zig.util.minecraft.ChatColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;

import java.util.List;

/** 5zig overlay messages, shown as vanilla toasts. */
public class Overlay implements IOverlay {

	public static void updateOverlayCount(int count) {
	}

	public static void renderAll() {
	}

	@Override
	public void displayMessage(String title, String subtitle) {
		FiveZigToast.create(ChatColor.YELLOW + title, subtitle, null);
	}

	@Override
	public void displayMessage(String title, String subtitle, Object uniqueReference) {
		FiveZigToast.create(ChatColor.YELLOW + title, subtitle, uniqueReference);
	}

	@Override
	public void displayMessage(String message) {
		displayMessage("5zig Reborn Revived", message);
	}

	@Override
	public void displayMessage(String message, Object uniqueReference) {
		displayMessage("5zig Reborn Revived", message, uniqueReference);
	}

	@Override
	public void displayMessageAndSplit(String message) {
		displayMessageAndSplit(message, null);
	}

	@Override
	public void displayMessageAndSplit(String message, Object uniqueReference) {
		List<String> split = MinecraftFactory.getVars().splitStringToWidth(message, MinecraftFactory.getClassProxyCallback().getOverlayTexture() == 2 ? 142 : 150);
		String title = split.size() > 0 ? split.get(0) : null;
		String subTitle = split.size() > 1 ? split.get(1) : null;
		displayMessage(title, subTitle, uniqueReference);
	}

	private static final class FiveZigToast implements Toast {

		private static final long DISPLAY_TIME = 5000L;

		private final Object uniqueReference;
		private String title;
		private String subTitle;
		private long startedTime = -1;
		private Visibility visibility = Visibility.SHOW;

		private FiveZigToast(Object uniqueReference, String title, String subTitle) {
			this.uniqueReference = uniqueReference;
			this.title = title;
			this.subTitle = subTitle;
		}

		static void create(String title, String subTitle, Object uniqueReference) {
			Minecraft mc = Minecraft.getInstance();
			mc.execute(() -> {
				//? if >=26 {
				/*net.minecraft.client.gui.components.toasts.ToastManager toasts = mc.gui.toastManager();
				*///?} else if >=1.21.2 {
				net.minecraft.client.gui.components.toasts.ToastManager toasts = mc.getToastManager();
				//?} else {
				/*net.minecraft.client.gui.components.toasts.ToastComponent toasts = mc.getToasts();
				*///?}
				FiveZigToast existing = uniqueReference == null ? null : toasts.getToast(FiveZigToast.class, uniqueReference);
				if (existing == null) {
					toasts.addToast(new FiveZigToast(uniqueReference, title, subTitle));
				} else {
					existing.title = title;
					existing.subTitle = subTitle;
					existing.startedTime = -1;
				}
			});
		}

		//? if >=1.21.2 {
		@Override
		public Visibility getWantedVisibility() {
			return visibility;
		}

		@Override
		public void update(net.minecraft.client.gui.components.toasts.ToastManager manager, long time) {
			if (startedTime < 0) {
				startedTime = time;
			}
			visibility = time - startedTime < DISPLAY_TIME * manager.getNotificationDisplayTimeMultiplier() ? Visibility.SHOW : Visibility.HIDE;
		}

		//?}

		//? if >=26 {
		/*@Override
		public void extractRenderState(GuiGraphics g, Font font, long time) {
			draw(g, font);
		}
		*///?} else if >=1.21.2 {
		@Override
		public void render(GuiGraphics g, Font font, long time) {
			draw(g, font);
		}
		//?} else {
		/*@Override
		public Visibility render(GuiGraphics g, net.minecraft.client.gui.components.toasts.ToastComponent component, long time) {
			if (startedTime < 0) {
				startedTime = time;
			}
			draw(g, component.getMinecraft().font);
			return time - startedTime < DISPLAY_TIME * component.getNotificationDisplayTimeMultiplier() ? Visibility.SHOW : Visibility.HIDE;
		}
		*///?}

		private void draw(GuiGraphics g, Font font) {
			int style = MinecraftFactory.getClassProxyCallback().getOverlayTexture();
			Draw.toastBackground(g, style, width(), height());
			int x = style == 2 ? 16 : 6;
			if (title != null) {
				Draw.text(g, font, title, x, 7, 0xFFFFFFFF, true);
			}
			if (subTitle != null) {
				Draw.text(g, font, subTitle, x, 18, 0xFFFFFFFF, true);
			}
		}

		@Override
		public Object getToken() {
			return uniqueReference == null ? NO_TOKEN : uniqueReference;
		}
	}
}
