/*
 * Copyright (c) 2026 IcanLevitate
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

package io.icanlevitateoff.fivezigrebornrevived.mod.manager.media;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Picks single values out of the text form of a GVariant dictionary, as gdbus prints it:
 * {@code {'xesam:title': <'Song'>, 'xesam:artist': <['A', 'B']>, 'mpris:length': <int64 2380000>}}.
 */
final class GVariantText {

	private GVariantText() {
	}

	/** The string value of the key, or null. */
	static String string(String text, String key) {
		int i = valueStart(text, key);
		if (i < 0) {
			return null;
		}
		StringBuilder out = new StringBuilder();
		return readString(text, i, out) < 0 ? null : out.toString();
	}

	/** The string-array value of the key (a single string is accepted too), or an empty list. */
	static List<String> strings(String text, String key) {
		int i = valueStart(text, key);
		if (i < 0) {
			return Collections.emptyList();
		}
		List<String> result = new ArrayList<>();
		if (text.charAt(i) != '[') {
			StringBuilder out = new StringBuilder();
			if (readString(text, i, out) >= 0) {
				result.add(out.toString());
			}
			return result;
		}
		i++;
		while (i < text.length()) {
			char c = text.charAt(i);
			if (c == ']') {
				break;
			} else if (c == '\'' || c == '"') {
				StringBuilder out = new StringBuilder();
				i = readString(text, i, out);
				if (i < 0) {
					break;
				}
				result.add(out.toString());
			} else {
				i++;
			}
		}
		return result;
	}

	/** The integer value of the key, or 0. */
	static long number(String text, String key) {
		int i = valueStart(text, key);
		if (i < 0) {
			return 0;
		}
		int end = i;
		if (end < text.length() && (text.charAt(end) == '-' || text.charAt(end) == '+')) {
			end++;
		}
		while (end < text.length() && Character.isDigit(text.charAt(end))) {
			end++;
		}
		try {
			return Long.parseLong(text.substring(i, end));
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	/**
	 * Index of the first character of the value of {@code 'key': <value>}, after any type
	 * annotation such as {@code uint64 } or {@code @as }.
	 */
	private static int valueStart(String text, String key) {
		String needle = "'" + key + "': <";
		int i = text.indexOf(needle);
		if (i < 0) {
			return -1;
		}
		i += needle.length();
		// Skip type annotations: a word made of letters/digits/@ followed by a space.
		while (i < text.length()) {
			int j = i;
			while (j < text.length() && (Character.isLetterOrDigit(text.charAt(j)) || text.charAt(j) == '@')) {
				j++;
			}
			if (j > i && j < text.length() && text.charAt(j) == ' ' && !Character.isDigit(text.charAt(i))) {
				i = j + 1;
			} else {
				break;
			}
		}
		return i < text.length() ? i : -1;
	}

	/** Reads a quoted string starting at {@code i} into {@code out}; returns the index after it, or -1. */
	private static int readString(String text, int i, StringBuilder out) {
		if (i >= text.length()) {
			return -1;
		}
		char quote = text.charAt(i);
		if (quote != '\'' && quote != '"') {
			return -1;
		}
		i++;
		while (i < text.length()) {
			char c = text.charAt(i);
			if (c == quote) {
				return i + 1;
			}
			if (c == '\\' && i + 1 < text.length()) {
				char e = text.charAt(i + 1);
				switch (e) {
					case 'n': out.append('\n'); i += 2; continue;
					case 't': out.append('\t'); i += 2; continue;
					case 'r': out.append('\r'); i += 2; continue;
					case 'u':
					case 'U': {
						int digits = e == 'u' ? 4 : 8;
						if (i + 2 + digits <= text.length()) {
							try {
								out.appendCodePoint(Integer.parseInt(text.substring(i + 2, i + 2 + digits), 16));
								i += 2 + digits;
								continue;
							} catch (IllegalArgumentException ignored) {
							}
						}
						out.append(e);
						i += 2;
						continue;
					}
					default: out.append(e); i += 2; continue;
				}
			}
			out.append(c);
			i++;
		}
		return -1;
	}
}
