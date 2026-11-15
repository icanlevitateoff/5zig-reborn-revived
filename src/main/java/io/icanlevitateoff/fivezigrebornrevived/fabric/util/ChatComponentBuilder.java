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

package io.icanlevitateoff.fivezigrebornrevived.fabric.util;

import eu.the5zig.mod.util.component.MessageComponent;
import eu.the5zig.mod.util.component.style.MessageAction;
import eu.the5zig.mod.util.component.style.MessageStyle;
import eu.the5zig.util.minecraft.ChatColor;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.net.URI;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts between core's legacy (section-sign formatted) strings / {@link MessageComponent}s and
 * vanilla {@link Component}s.
 */
public final class ChatComponentBuilder {

	private static final Pattern URL = Pattern.compile("^(?:(https?)://)?([-\\w_\\.]{2,}\\.[a-z]{2,4})(/\\S*)?$");

	private ChatComponentBuilder() {
	}

	// ---- legacy string -> Component

	public static MutableComponent fromLegacyText(String message) {
		MutableComponent base = Component.empty();
		if (message == null) {
			return base;
		}
		Style style = Style.EMPTY;
		StringBuilder builder = new StringBuilder();
		Matcher matcher = URL.matcher(message);
		for (int i = 0; i < message.length(); i++) {
			char c = message.charAt(i);
			if (c == ChatColor.COLOR_CHAR && i + 1 < message.length()) {
				ChatFormatting format = ChatFormatting.getByCode(Character.toLowerCase(message.charAt(++i)));
				if (format == null) {
					continue;
				}
				flush(base, builder, style);
				if (format == ChatFormatting.RESET) {
					style = Style.EMPTY;
				} else if (isColor(format)) {
					style = Style.EMPTY.withColor(format);
				} else {
					style = style.applyFormat(format);
				}
				continue;
			}
			int end = message.indexOf(' ', i);
			if (end == -1) {
				end = message.length();
			}
			if (matcher.region(i, end).find()) {
				flush(base, builder, style);
				String url = message.substring(i, end);
				String target = url.startsWith("http") ? url : "http://" + url;
				Style urlStyle = style;
				try {
					urlStyle = style.withClickEvent(openUrl(target));
				} catch (IllegalArgumentException ignored) {
				}
				base.append(Component.literal(url).setStyle(urlStyle));
				i = end - 1;
				continue;
			}
			builder.append(c);
		}
		flush(base, builder, style);
		return base;
	}

	private static void flush(MutableComponent base, StringBuilder builder, Style style) {
		if (builder.length() > 0) {
			base.append(Component.literal(builder.toString()).setStyle(style));
			builder.setLength(0);
		}
	}

	// ---- Component -> legacy string

	/** Flattens a component tree to a legacy formatted string (the old getFormattedText()). */
	public static String toLegacy(Component component) {
		if (component == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		component.visit((style, text) -> {
			if (!text.isEmpty()) {
				sb.append(formatCodes(style)).append(text).append(ChatColor.RESET);
			}
			return Optional.empty();
		}, Style.EMPTY);
		return sb.toString();
	}

	private static boolean isColor(ChatFormatting format) {
		//? if >=26 {
		/*return format.ordinal() < 16; // the 16 colours come first
		*///?} else {
		return format.isColor();
		//?}
	}

	private static ChatFormatting legacyColor(TextColor color) {
		//? if >=26 {
		/*for (ChatFormatting format : ChatFormatting.values()) {
			if (isColor(format) && color.equals(TextColor.fromLegacyFormat(format))) {
				return format;
			}
		}
		return null;
		*///?} else {
		return ChatFormatting.getByName(color.serialize());
		//?}
	}

	public static String formatCodes(Style style) {
		StringBuilder sb = new StringBuilder();
		TextColor color = style.getColor();
		if (color != null) {
			ChatFormatting legacy = legacyColor(color);
			if (legacy != null) {
				sb.append(ChatColor.COLOR_CHAR).append(legacy.toString().charAt(1));
			}
		}
		if (style.isBold()) sb.append(ChatColor.BOLD);
		if (style.isItalic()) sb.append(ChatColor.ITALIC);
		if (style.isUnderlined()) sb.append(ChatColor.UNDERLINE);
		if (style.isStrikethrough()) sb.append(ChatColor.STRIKETHROUGH);
		if (style.isObfuscated()) sb.append(ChatColor.MAGIC);
		return sb.toString();
	}

	// ---- MessageComponent <-> Component

	public static MutableComponent fromInterface(MessageComponent api) {
		MutableComponent text = fromLegacyText(api.getText());
		Style style = Style.EMPTY;
		MessageAction click = api.getStyle() == null ? null : api.getStyle().getOnClick();
		if (click != null) {
			ClickEvent event = toClickEvent(click);
			if (event != null) {
				style = style.withClickEvent(event);
			}
		}
		MessageAction hover = api.getStyle() == null ? null : api.getStyle().getOnHover();
		if (hover != null && hover.getAction() == MessageAction.Action.SHOW_TEXT && hover.getComponent() != null) {
			//? if >=1.21.5 {
			style = style.withHoverEvent(new HoverEvent.ShowText(fromInterface(hover.getComponent())));
			//?} else {
			/*style = style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, fromInterface(hover.getComponent())));
			*///?}
		}
		text.setStyle(style);
		for (MessageComponent sibling : api.getSiblings()) {
			text.append(fromInterface(sibling));
		}
		return text;
	}

	private static ClickEvent toClickEvent(MessageAction click) {
		String value = click.getString();
		if (value == null) {
			return null;
		}
		try {
			switch (click.getAction()) {
				case OPEN_URL:
					return openUrl(value);
				//? if >=1.21.5 {
				case OPEN_FILE:
					return new ClickEvent.OpenFile(value);
				case RUN_COMMAND:
					return new ClickEvent.RunCommand(value);
				case SUGGEST_COMMAND:
					return new ClickEvent.SuggestCommand(value);
				//?} else {
				/*case OPEN_FILE:
					return new ClickEvent(ClickEvent.Action.OPEN_FILE, value);
				case RUN_COMMAND:
					return new ClickEvent(ClickEvent.Action.RUN_COMMAND, value);
				case SUGGEST_COMMAND:
					return new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, value);
				*///?}
				default:
					return null;
			}
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	private static ClickEvent openUrl(String url) {
		//? if >=1.21.5 {
		return new ClickEvent.OpenUrl(URI.create(url));
		//?} else {
		/*URI.create(url); // same validation as the newer record
		return new ClickEvent(ClickEvent.Action.OPEN_URL, url);
		*///?}
	}

	public static MessageComponent toInterface(Component mc) {
		MessageComponent base = new MessageComponent(formatCodes(mc.getStyle()) + mc.plainCopy().getString());
		Style style = mc.getStyle();
		MessageAction click = null;
		ClickEvent ce = style.getClickEvent();
		MessageAction hover = null;
		//? if >=1.21.5 {
		if (ce instanceof ClickEvent.OpenUrl e) {
			click = new MessageAction(MessageAction.Action.OPEN_URL, e.uri().toString());
		} else if (ce instanceof ClickEvent.OpenFile e) {
			click = new MessageAction(MessageAction.Action.OPEN_FILE, e.path());
		} else if (ce instanceof ClickEvent.RunCommand e) {
			click = new MessageAction(MessageAction.Action.RUN_COMMAND, e.command());
		} else if (ce instanceof ClickEvent.SuggestCommand e) {
			click = new MessageAction(MessageAction.Action.SUGGEST_COMMAND, e.command());
		}
		if (style.getHoverEvent() instanceof HoverEvent.ShowText e) {
			hover = new MessageAction(MessageAction.Action.SHOW_TEXT, toInterface(e.value()));
		}
		//?} else {
		/*if (ce != null) {
			MessageAction.Action action = switch (ce.getAction()) {
				case OPEN_URL -> MessageAction.Action.OPEN_URL;
				case OPEN_FILE -> MessageAction.Action.OPEN_FILE;
				case RUN_COMMAND -> MessageAction.Action.RUN_COMMAND;
				case SUGGEST_COMMAND -> MessageAction.Action.SUGGEST_COMMAND;
				default -> null;
			};
			if (action != null) {
				click = new MessageAction(action, ce.getValue());
			}
		}
		Component hoverText = style.getHoverEvent() == null ? null : style.getHoverEvent().getValue(HoverEvent.Action.SHOW_TEXT);
		if (hoverText != null) {
			hover = new MessageAction(MessageAction.Action.SHOW_TEXT, toInterface(hoverText));
		}
		*///?}
		base.setStyle(new MessageStyle(hover, click));
		for (Component sibling : mc.getSiblings()) {
			base.getSiblings().add(toInterface(sibling));
		}
		return base;
	}
}
