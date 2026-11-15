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

import com.google.common.base.Strings;
import io.icanlevitateoff.fivezigrebornrevived.mod.MinecraftFactory;
import io.icanlevitateoff.fivezigrebornrevived.mod.The5zigMod;
import io.icanlevitateoff.fivezigrebornrevived.util.GameProfiles;
import io.icanlevitateoff.fivezigrebornrevived.util.PlayerPlaceholder;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Chat search and highlight words: matching text in a vanilla chat line is recoloured (highlight
 * colour, underlined) at draw time, so it follows the live search text.
 */
public final class ChatHighlight {

	private ChatHighlight() {
	}

	public static FormattedCharSequence apply(FormattedCharSequence line) {
		if (line == null || !The5zigMod.hasBeenInitialized()) {
			return line;
		}
		String search = MinecraftFactory.getClassProxyCallback().getChatSearchText();
		boolean wholeWords = Strings.isNullOrEmpty(search);
		List<String> words = wholeWords ? MinecraftFactory.getClassProxyCallback().getHighlightWords() : Collections.singletonList(search);
		if (words == null || words.isEmpty()) {
			return line;
		}

		List<Style> styles = new ArrayList<>();
		List<Integer> codepoints = new ArrayList<>();
		line.accept((index, style, codepoint) -> {
			styles.add(style);
			codepoints.add(codepoint);
			return true;
		});
		// One char per codepoint keeps string indices and codepoint indices aligned.
		StringBuilder plain = new StringBuilder(codepoints.size());
		for (int cp : codepoints) {
			plain.append(Character.isBmpCodePoint(cp) ? Character.toLowerCase((char) cp) : '\uFFFD');
		}
		String text = plain.toString();

		boolean[] marked = new boolean[codepoints.size()];
		boolean any = false;
		String player = GameProfiles.name(MinecraftFactory.getVars().getGameProfile());
		for (String word : words) {
			if (Strings.isNullOrEmpty(word)) {
				continue;
			}
			String needle = PlayerPlaceholder.expand(word, player).toLowerCase(Locale.ROOT);
			if (needle.isEmpty()) {
				continue;
			}
			for (int at = text.indexOf(needle); at != -1; at = text.indexOf(needle, at + 1)) {
				int end = at + needle.length();
				if (wholeWords && ((at > 0 && Character.isLetterOrDigit(text.charAt(at - 1)))
						|| (end < text.length() && Character.isLetterOrDigit(text.charAt(end))))) {
					continue;
				}
				for (int i = at; i < end; i++) {
					marked[i] = true;
				}
				any = true;
			}
		}
		if (!any) {
			return line;
		}

		TextColor color = TextColor.fromRgb(MinecraftFactory.getClassProxyCallback().getHighlightWordsColor() & 0xFFFFFF);
		return sink -> {
			for (int i = 0; i < codepoints.size(); i++) {
				Style style = marked[i] ? styles.get(i).withColor(color).withUnderlined(true) : styles.get(i);
				if (!sink.accept(i, style, codepoints.get(i))) {
					return false;
				}
			}
			return true;
		};
	}
}
