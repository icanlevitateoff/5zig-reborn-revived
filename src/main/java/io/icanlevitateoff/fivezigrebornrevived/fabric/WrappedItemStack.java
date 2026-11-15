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
import io.icanlevitateoff.fivezigrebornrevived.fabric.util.ChatComponentBuilder;
import io.icanlevitateoff.fivezigrebornrevived.mod.MinecraftFactory;
import eu.the5zig.mod.gui.ingame.ItemStack;
import io.icanlevitateoff.fivezigrebornrevived.mod.util.GLUtil;
import eu.the5zig.util.minecraft.ChatColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
//? if >=1.20.5 {
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.component.ItemAttributeModifiers;
//?}
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.stream.Collectors;

public class WrappedItemStack implements ItemStack {

	private final net.minecraft.world.item.ItemStack item;

	public WrappedItemStack(net.minecraft.world.item.ItemStack item) {
		this.item = item;
	}

	public static String getResourceKey(net.minecraft.world.item.ItemStack item) {
		return "minecraft:" + BuiltInRegistries.ITEM.getKey(item.getItem()).getPath();
	}

	@Override
	public int getAmount() {
		return item.getCount();
	}

	@Override
	public int getMaxDurability() {
		return item.getMaxDamage();
	}

	@Override
	public int getCurrentDurability() {
		return item.getDamageValue();
	}

	@Override
	public String getKey() {
		return getResourceKey(item);
	}

	@Override
	public String getDisplayName() {
		return ChatComponentBuilder.toLegacy(item.getHoverName());
	}

	@Override
	public List<String> getLore() {
		Minecraft mc = Minecraft.getInstance();
		//? if >=1.20.5 {
		List<Component> lines = item.getTooltipLines(Item.TooltipContext.of(mc.level), mc.player, TooltipFlag.Default.NORMAL);
		//?} else {
		/*List<Component> lines = item.getTooltipLines(mc.player, TooltipFlag.Default.NORMAL);
		*///?}
		return lines.stream().map(Component::getString).collect(Collectors.toList());
	}

	private FoodProperties food() {
		//? if >=1.20.5 {
		return item.get(DataComponents.FOOD);
		//?} else {
		/*return item.getItem().getFoodProperties();
		*///?}
	}

	@Override
	public int getHealAmount() {
		FoodProperties food = food();
		//? if >=1.20.5 {
		return food == null ? 0 : food.nutrition();
		//?} else {
		/*return food == null ? 0 : food.getNutrition();
		*///?}
	}

	/** The saturation multiplier (core multiplies it by nutrition * 2). */
	@Override
	public float getSaturationModifier() {
		FoodProperties food = food();
		//? if >=1.20.5 {
		// Since 1.20.5 the component stores the absolute saturation (nutrition * modifier * 2).
		return food == null || food.nutrition() == 0 ? 0 : food.saturation() / (food.nutrition() * 2f);
		//?} else {
		/*return food == null ? 0 : food.getSaturationModifier();
		*///?}
	}

	@Override
	public void render(int x, int y, boolean withGenericAttributes) {
		GuiGraphics g = RenderContext.get();
		if (g == null || item.isEmpty()) {
			return;
		}
		Draw.item(g, item, x, y);
		if (!withGenericAttributes) {
			return;
		}
		double[] attack = {0}, armor = {0};
		boolean[] has = {false, false};
		//? if >=1.20.5 {
		ItemAttributeModifiers modifiers = item.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
		modifiers.forEach(EquipmentSlotGroup.MAINHAND, (attribute, modifier) -> {
			if (attribute.is(Attributes.ATTACK_DAMAGE)) {
				attack[0] += modifier.amount();
				has[0] = true;
			}
		});
		modifiers.forEach(EquipmentSlotGroup.ARMOR, (attribute, modifier) -> {
			if (attribute.is(Attributes.ARMOR)) {
				armor[0] += modifier.amount();
				has[1] = true;
			}
		});
		//?} else {
		/*for (net.minecraft.world.entity.ai.attributes.AttributeModifier modifier : item.getAttributeModifiers(net.minecraft.world.entity.EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE)) {
			attack[0] += modifier.getAmount();
			has[0] = true;
		}
		for (net.minecraft.world.entity.EquipmentSlot slot : new net.minecraft.world.entity.EquipmentSlot[]{net.minecraft.world.entity.EquipmentSlot.HEAD,
				net.minecraft.world.entity.EquipmentSlot.CHEST, net.minecraft.world.entity.EquipmentSlot.LEGS, net.minecraft.world.entity.EquipmentSlot.FEET}) {
			for (net.minecraft.world.entity.ai.attributes.AttributeModifier modifier : item.getAttributeModifiers(slot).get(Attributes.ARMOR)) {
				armor[0] += modifier.getAmount();
				has[1] = true;
			}
		}
		*///?}
		double value = has[0] ? attack[0] + 1 : armor[0];
		if (has[0] || has[1]) {
			GLUtil.pushMatrix();
			GLUtil.translate(x + 8, y + 10, 1);
			GLUtil.scale(0.7f, 0.7f, 0.7f);
			MinecraftFactory.getVars().drawString(ChatColor.BLUE + "+" + Math.round(value), 0, 0);
			GLUtil.popMatrix();
		}
	}
}
