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

package io.icanlevitateoff.fivezigrebornrevived.fabric.gui;

import io.icanlevitateoff.fivezigrebornrevived.fabric.compat.Mc;
import io.icanlevitateoff.fivezigrebornrevived.fabric.compat.Draw;
import io.icanlevitateoff.fivezigrebornrevived.fabric.RenderContext;
import io.icanlevitateoff.fivezigrebornrevived.fabric.util.ChatComponentBuilder;
import io.icanlevitateoff.fivezigrebornrevived.mod.MinecraftFactory;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.ingame.IGui2ndChat;
import io.icanlevitateoff.fivezigrebornrevived.mod.util.ClassProxyCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.List;

/** The second chat window drawn on the right-hand side, at the height of the vanilla chat. */
public class Gui2ndChat implements IGui2ndChat {

	private static final int LINE_HEIGHT = 9;
	private static final int BOTTOM_OFFSET = 40;

	/** Full messages (newest first), kept so the chat can be re-wrapped when its width changes. */
	private final List<ChatLine> chatLines = new ArrayList<>();
	/** Wrapped display lines (newest first). */
	private final List<ChatLine> singleChatLines = new ArrayList<>();
	private int scrollPos;
	private boolean isScrolled;

	private record ChatLine(int addedTick, Component component, FormattedCharSequence line) {
	}

	private static ClassProxyCallback cb() {
		return MinecraftFactory.getClassProxyCallback();
	}

	private static Font font() {
		return Minecraft.getInstance().font;
	}

	@Override
	public void draw(int updateCounter) {
		GuiGraphics g = RenderContext.get();
		if (g == null || !cb().is2ndChatVisible() || singleChatLines.isEmpty()) {
			return;
		}
		boolean chatOpened = isChatOpened();
		float opacity = cb().get2ndChatOpacity() * 0.9F + 0.1F;
		float chatScale = getChatScale();
		int width = Mth.ceil(getChatWidth() / chatScale);
		int lineDisplayCount = getLineCount();
		int scaledWidth = MinecraftFactory.getVars().getScaledWidth();
		int scaledHeight = MinecraftFactory.getVars().getScaledHeight();

		Draw.push(g);
		Draw.translate(g, scaledWidth - getChatWidth() - 6.0f * chatScale, scaledHeight - BOTTOM_OFFSET);
		Draw.scale(g, chatScale, chatScale);
		int shown = 0;
		for (int i = 0; i + scrollPos < singleChatLines.size() && i < lineDisplayCount; i++) {
			ChatLine line = singleChatLines.get(i + scrollPos);
			int age = updateCounter - line.addedTick();
			if (age >= 200 && !chatOpened) {
				continue;
			}
			double fade = Mth.clamp((1.0D - age / 200.0D) * 10.0D, 0.0D, 1.0D);
			int alpha = chatOpened ? 255 : (int) (255.0D * fade * fade);
			alpha = (int) (alpha * opacity);
			shown++;
			if (alpha <= 3) {
				continue;
			}
			int y = -i * LINE_HEIGHT;
			if (!cb().isChatBackgroundTransparent()) {
				g.fill(0, y - LINE_HEIGHT, width + Mth.floor(4f * chatScale), y, alpha / 2 << 24);
			}
			int x = cb().is2ndChatTextLeftbound() ? 0 : width - font().width(line.line());
			Draw.text(g, font(), line.line(), x, y - 8, 0xFFFFFF | (alpha << 24), true);
		}
		if (chatOpened && shown > 0) {
			int fontHeight = font().lineHeight;
			int total = singleChatLines.size();
			int visibleHeight = total * fontHeight + total;
			int shownHeight = shown * fontHeight + shown;
			if (visibleHeight != shownHeight) {
				int barOffset = scrollPos * shownHeight / total;
				int barHeight = shownHeight * shownHeight / visibleHeight;
				int alpha = barOffset > 0 ? 170 : 96;
				int color = isScrolled ? 0xCC3333 : 0x3333AA;
				g.fill(width + 3, -barOffset, width + 5, -barOffset - barHeight, color + (alpha << 24));
				g.fill(width + 5, -barOffset, width + 4, -barOffset - barHeight, 0xCCCCCC + (alpha << 24));
			}
		}
		Draw.pop(g);
	}

	@Override
	public void printChatMessage(String message) {
		printChatMessage(ChatComponentBuilder.fromLegacyText(message));
	}

	@Override
	public void printChatMessage(Object chatComponent) {
		if (!(chatComponent instanceof Component component)) {
			throw new IllegalArgumentException(chatComponent == null ? "null" : chatComponent.getClass().getName());
		}
		LogManager.getLogger().info("[CHAT2] {}", component.getString());
		if (cb().isShowTimeBeforeChatMessage()) {
			component = (Component) cb().getChatComponentWithTime(component);
		}
		int tick = Mc.guiTicks();
		addLines(component, tick);
		chatLines.add(0, new ChatLine(tick, component, null));
		trim(chatLines);
	}

	private void addLines(Component component, int tick) {
		int lineWidth = Mth.floor(getChatWidth() / getChatScale());
		for (FormattedCharSequence line : ComponentRenderUtils.wrapComponents(component, lineWidth, font())) {
			if (isChatOpened() && scrollPos > 0) {
				isScrolled = true;
				scroll(1);
			}
			singleChatLines.add(0, new ChatLine(tick, component, line));
		}
		trim(singleChatLines);
	}

	private static void trim(List<ChatLine> list) {
		while (list.size() > cb().getMaxChatLines()) {
			list.remove(list.size() - 1);
		}
	}

	@Override
	public void clear() {
		singleChatLines.clear();
		chatLines.clear();
		resetScroll();
	}

	@Override
	public void refreshChat() {
		singleChatLines.clear();
		resetScroll();
		for (int i = chatLines.size() - 1; i >= 0; i--) {
			ChatLine line = chatLines.get(i);
			addLines(line.component(), line.addedTick());
		}
	}

	@Override
	public int getLineCount() {
		return getChatHeight() / LINE_HEIGHT;
	}

	@Override
	public void scroll(int scroll) {
		scrollPos += scroll;
		int max = singleChatLines.size() - getLineCount();
		if (scrollPos > max) {
			scrollPos = max;
		}
		if (scrollPos <= 0) {
			scrollPos = 0;
			isScrolled = false;
		}
	}

	@Override
	public void resetScroll() {
		scrollPos = 0;
		isScrolled = false;
	}

	@Override
	public void drawComponentHover(int mouseX, int mouseY) {
		GuiGraphics g = RenderContext.get();
		Style style = getStyleAt(mouseX, mouseY);
		if (g != null && style != null) {
			Draw.hoverEffect(g, font(), style, mouseX, mouseY);
		}
	}

	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int button) {
		if (button != 0) {
			return false;
		}
		Style style = getStyleAt(mouseX, mouseY);
		return style != null && Mc.screen() instanceof ChatScreen screen && GuiHandle.handleStyleClick(screen, style);
	}

	@Override
	public void keyTyped(int code) {
		int page = Mc.chat().getLinesPerPage() - 1;
		if (code == io.icanlevitateoff.fivezigrebornrevived.mod.util.Keyboard.KEY_PRIOR) {
			scroll(page);
		} else if (code == io.icanlevitateoff.fivezigrebornrevived.mod.util.Keyboard.KEY_NEXT) {
			scroll(-page);
		}
	}

	private Style getStyleAt(int mouseX, int mouseY) {
		if (!isChatOpened()) {
			return null;
		}
		float chatScale = getChatScale();
		double x = (mouseX - (MinecraftFactory.getVars().getScaledWidth() - getChatWidth() - 6.0f * chatScale)) / chatScale;
		double y = (MinecraftFactory.getVars().getScaledHeight() - BOTTOM_OFFSET - mouseY) / chatScale;
		if (x < 0 || y < 0) {
			return null;
		}
		int lineIndex = (int) (y / LINE_HEIGHT);
		if (lineIndex >= Math.min(getLineCount(), singleChatLines.size())) {
			return null;
		}
		int index = lineIndex + scrollPos;
		if (index < 0 || index >= singleChatLines.size()) {
			return null;
		}
		ChatLine line = singleChatLines.get(index);
		int width = Mth.ceil(getChatWidth() / chatScale);
		int offset = cb().is2ndChatTextLeftbound() ? 0 : width - font().width(line.line());
		return Draw.styleAtWidth(font(), line.line(), Mth.floor(x) - offset);
	}

	private boolean isChatOpened() {
		return Mc.screen() instanceof ChatScreen;
	}

	private int getChatWidth() {
		return Mth.floor(cb().get2ndChatWidth());
	}

	private int getChatHeight() {
		return Mth.floor(isChatOpened() ? cb().get2ndChatHeightFocused() : cb().get2ndChatHeightUnfocused());
	}

	private float getChatScale() {
		return cb().get2ndChatScale();
	}
}
