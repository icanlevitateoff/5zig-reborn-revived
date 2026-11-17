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

package io.icanlevitateoff.fivezigrebornrevived.fabric.integration;

import eu.the5zig.util.minecraft.ChatColor;
import io.icanlevitateoff.fivezigrebornrevived.mod.I18n;
import io.icanlevitateoff.fivezigrebornrevived.mod.The5zigMod;
import io.icanlevitateoff.fivezigrebornrevived.mod.config.ConfigNew;
import io.icanlevitateoff.fivezigrebornrevived.mod.config.items.*;
import io.icanlevitateoff.fivezigrebornrevived.mod.MinecraftFactory;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.*;
import java.util.function.Consumer;

/**
 * 5zig's settings as a Cloth Config screen, built from the same config items as 5zig's own menu.
 * <p>
 * Every category reachable from the main menu becomes a tab. Items that open their own screens
 * (modules, HUD layout, accounts, ...) cannot be shown by Cloth Config, so a button at the top left
 * opens 5zig's full menu instead. Values are only written when Save is pressed, and each changed item
 * runs the same action as in 5zig's own menu.
 */
public final class ClothConfigScreen {

	private ClothConfigScreen() {
	}

	public static Screen create(Screen parent) {
		ConfigNew config = The5zigMod.getConfig();
		ConfigBuilder builder = ConfigBuilder.create()
				.setParentScreen(parent)
				.setTitle(Component.literal(The5zigMod.NAME))
				.setSavingRunnable(config::save);
		ConfigEntryBuilder entries = builder.entryBuilder();

		builder.setAfterInitConsumer(screen -> Screens.getButtons(screen).add(Button.builder(
				Component.literal(I18n.translate("config.cloth.full_menu")),
				b -> MinecraftFactory.getVars().displayScreen(FiveZigScreens.settings(parent))).bounds(4, 4, 110, 20).build()));

		// Breadth-first from "main", so tabs appear in the same order as 5zig's menu.
		Map<String, String> titles = new LinkedHashMap<>();
		titles.put("main", I18n.translate("config.cloth.general"));
		Deque<String> queue = new ArrayDeque<>();
		queue.add("main");
		while (!queue.isEmpty()) {
			String category = queue.poll();
			ConfigCategory tab = builder.getOrCreateCategory(Component.literal(titles.get(category)));
			if ("main".equals(category)) {
				tab.addEntry(entries.startTextDescription(Component.literal(I18n.translate("config.cloth.full_menu.desc"))).build());
			}
			for (ConfigItem item : config.getItems(category)) {
				if (item instanceof DisplayCategoryItem) {
					String next = ((DisplayCategoryItem) item).getDisplayCategory();
					if (!titles.containsKey(next)) {
						titles.put(next, item.translateName());
						queue.add(next);
					}
					continue;
				}
				if (item instanceof NonConfigItem || item.isRestricted()) {
					continue;
				}
				AbstractConfigListEntry<?> entry = entry(entries, item);
				if (entry != null) {
					tab.addEntry(entry);
				}
			}
		}
		return builder.build();
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static AbstractConfigListEntry<?> entry(ConfigEntryBuilder entries, ConfigItem item) {
		Component name = Component.literal(item.translateName());
		Component[] tooltip = tooltip(item);
		if (item instanceof BoolItem) {
			BoolItem bool = (BoolItem) item;
			return entries.startBooleanToggle(name, bool.get())
					.setDefaultValue(bool.getDefaultValue())
					.setYesNoTextSupplier(value -> Component.literal(bool.translateValueOf(value)))
					.setTooltip(tooltip)
					.setSaveConsumer(save(bool))
					.build();
		}
		if (item instanceof SliderItem) {
			SliderItem slider = (SliderItem) item;
			float min = slider.getMinValue();
			float range = slider.getMaxValue() - min;
			// Cloth only has integer sliders: slide over positions and map them onto the item's range.
			int positions = slider.getSteps() > 0 ? Math.max(1, Math.round(range / slider.getSteps())) : Math.max(100, Math.round(range));
			int current = Math.round((slider.get() - min) / range * positions);
			return entries.startIntSlider(name, current, 0, positions)
					.setDefaultValue(Math.round((slider.getDefaultValue() - min) / range * positions))
					.setTextGetter(position -> Component.literal(sliderText(slider, position / (float) positions)))
					.setTooltip(tooltip)
					// Only write when moved: mapping back can differ from the stored value by a rounding error.
					.setSaveConsumer(position -> {
						if (position != current) {
							apply(slider, min + range * position / positions);
						}
					})
					.build();
		}
		if (item instanceof EnumItem) {
			EnumItem enumItem = (EnumItem) item;
			return entries.startSelector(name, enumItem.getEnumClass().getEnumConstants(), (Enum) enumItem.get())
					.setDefaultValue((Enum) enumItem.getDefaultValue())
					.setNameProvider(value -> Component.literal(enumItem.translateValueOf((Enum) value)))
					.setTooltip(tooltip)
					.setSaveConsumer(save(enumItem))
					.build();
		}
		if (item instanceof SelectColorItem || item instanceof ColorFormattingItem) {
			ConfigItem<ChatColor> color = (ConfigItem<ChatColor>) item;
			ChatColor[] values = item instanceof ColorFormattingItem
					? ColorFormattingItem.getFormattings().toArray(new ChatColor[0])
					: Arrays.stream(ChatColor.values()).filter(c -> Character.digit(c.getCode(), 16) >= 0).toArray(ChatColor[]::new);
			return entries.startSelector(name, values, color.get())
					.setDefaultValue(color.getDefaultValue())
					.setNameProvider(value -> Component.literal(value + color.translateValueOf(value)))
					.setTooltip(tooltip)
					.setSaveConsumer(save(color))
					.build();
		}
		if (item instanceof HexColorItem) {
			HexColorItem hex = (HexColorItem) item;
			return entries.startColorField(name, parseHex(hex.get(), 0xFFFFFF))
					.setDefaultValue(parseHex(hex.getDefaultValue(), 0xFFFFFF))
					.setTooltip(tooltip)
					.setSaveConsumer(rgb -> apply(hex, String.format(Locale.ROOT, "0x%06x", rgb & 0xFFFFFF)))
					.build();
		}
		if (item instanceof StringItem) {
			StringItem string = (StringItem) item;
			return entries.startStrField(name, string.get())
					.setDefaultValue(string.getDefaultValue())
					.setErrorSupplier(value -> value.length() < string.getMinLength() || value.length() > string.getMaxLength()
							? Optional.of(Component.literal(I18n.translate("config.cloth.length", string.getMinLength(), string.getMaxLength())))
							: Optional.empty())
					.setTooltip(tooltip)
					.setSaveConsumer(save(string))
					.build();
		}
		if (item instanceof StringListItem) {
			StringListItem list = (StringListItem) item;
			return entries.startStrList(name, new ArrayList<>(list.get()))
					.setDefaultValue(list.getDefaultValue())
					.setTooltip(tooltip)
					.setSaveConsumer(value -> apply(list, new ArrayList<>(value)))
					.build();
		}
		if (item instanceof IntItem) {
			IntItem integer = (IntItem) item;
			return entries.startIntField(name, integer.get())
					.setDefaultValue(integer.getDefaultValue())
					.setTooltip(tooltip)
					.setSaveConsumer(save(integer))
					.build();
		}
		if (item instanceof FloatItem) {
			FloatItem number = (FloatItem) item;
			return entries.startFloatField(name, number.get())
					.setDefaultValue(number.getDefaultValue())
					.setTooltip(tooltip)
					.setSaveConsumer(save(number))
					.build();
		}
		return null; // anything else is only editable in 5zig's own menu
	}

	/** The same value text as 5zig's own slider ({@code normalized} is 0..1). */
	private static String sliderText(SliderItem slider, float normalized) {
		String custom = slider.getCustomValue(normalized);
		if (custom != null) {
			return custom;
		}
		String suffix = slider.getSuffix() == null ? "" : slider.getSuffix();
		return Math.round(normalized * (slider.getMaxValue() - slider.getMinValue()) + slider.getMinValue()) + suffix;
	}

	private static Component[] tooltip(ConfigItem<?> item) {
		if (!item.isDescriptionLocalized()) {
			return new Component[0];
		}
		return Arrays.stream(item.translateDescription().split("\\n|\n")).map(Component::literal).toArray(Component[]::new);
	}

	private static int parseHex(String value, int fallback) {
		try {
			return Integer.parseInt(value.substring(2), 16);
		} catch (RuntimeException e) {
			return fallback;
		}
	}

	private static <T> Consumer<T> save(ConfigItem<T> item) {
		return value -> apply(item, value);
	}

	/** Like a change in 5zig's own menu: set, then run the item's action. Unchanged values are left alone. */
	private static <T> void apply(ConfigItem<T> item, T value) {
		if (Objects.equals(item.get(), value)) {
			return;
		}
		item.set(value);
		item.action();
	}
}
