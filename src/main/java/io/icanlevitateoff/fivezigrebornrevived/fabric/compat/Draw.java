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

package io.icanlevitateoff.fivezigrebornrevived.fabric.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.effect.MobEffect;

import java.util.List;

/**
 * Version-dependent GUI drawing. Everything else in the Fabric layer draws through here, so the
 * rendering API changes (1.20.2 sprites, 1.21.2 render types, 1.21.6 render pipelines and the 2D
 * pose stack) stay in one file.
 */
public final class Draw {

	private Draw() {
	}

	// ---------------------------------------------------------------- pose

	public static void push(GuiGraphics g) {
		//? if >=1.21.6 {
		g.pose().pushMatrix();
		//?} else {
		/*g.pose().pushPose();
		*///?}
	}

	public static void pop(GuiGraphics g) {
		//? if >=1.21.6 {
		g.pose().popMatrix();
		//?} else {
		/*g.pose().popPose();
		*///?}
	}

	public static void translate(GuiGraphics g, float x, float y) {
		//? if >=1.21.6 {
		g.pose().translate(x, y);
		//?} else {
		/*g.pose().translate(x, y, 0.0F);
		*///?}
	}

	public static void scale(GuiGraphics g, float x, float y) {
		//? if >=1.21.6 {
		g.pose().scale(x, y);
		//?} else {
		/*g.pose().scale(x, y, 1.0F);
		*///?}
	}

	public static void rotate(GuiGraphics g, float degrees) {
		//? if >=1.21.6 {
		g.pose().rotate((float) Math.toRadians(degrees));
		//?} else {
		/*g.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(degrees));
		*///?}
	}

	// ---------------------------------------------------------------- text (renamed in 26.1)

	public static void text(GuiGraphics g, Font font, String text, int x, int y, int color, boolean shadow) {
		//? if >=26 {
		/*g.text(font, text, x, y, color, shadow);
		*///?} else {
		g.drawString(font, text, x, y, color, shadow);
		//?}
	}

	public static void text(GuiGraphics g, Font font, Component text, int x, int y, int color, boolean shadow) {
		//? if >=26 {
		/*g.text(font, text, x, y, color, shadow);
		*///?} else {
		g.drawString(font, text, x, y, color, shadow);
		//?}
	}

	public static void text(GuiGraphics g, Font font, FormattedCharSequence text, int x, int y, int color, boolean shadow) {
		//? if >=26 {
		/*g.text(font, text, x, y, color, shadow);
		*///?} else {
		g.drawString(font, text, x, y, color, shadow);
		//?}
	}

	public static void centeredText(GuiGraphics g, Font font, String text, int x, int y, int color) {
		//? if >=26 {
		/*g.centeredText(font, text, x, y, color);
		*///?} else {
		g.drawCenteredString(font, text, x, y, color);
		//?}
	}

	public static void centeredText(GuiGraphics g, Font font, Component text, int x, int y, int color) {
		//? if >=26 {
		/*g.centeredText(font, text, x, y, color);
		*///?} else {
		g.drawCenteredString(font, text, x, y, color);
		//?}
	}

	public static void item(GuiGraphics g, ItemStack stack, int x, int y) {
		//? if >=26 {
		/*g.item(stack, x, y);
		g.itemDecorations(Minecraft.getInstance().font, stack, x, y);
		*///?} else {
		g.renderItem(stack, x, y);
		g.renderItemDecorations(Minecraft.getInstance().font, stack, x, y);
		//?}
	}

	public static void widget(GuiGraphics g, AbstractWidget widget, int mouseX, int mouseY, float partialTick) {
		//? if >=26 {
		/*widget.extractRenderState(g, mouseX, mouseY, partialTick);
		*///?} else {
		widget.render(g, mouseX, mouseY, partialTick);
		//?}
	}

	/** Hover tooltip for a chat style (show-text hover events). */
	public static void hoverEffect(GuiGraphics g, Font font, Style style, int x, int y) {
		//? if >=26 {
		/*if (style.getHoverEvent() instanceof HoverEvent.ShowText text) {
			g.setTooltipForNextFrame(font, text.value(), x, y);
		}
		*///?} else {
		g.renderComponentHoverEffect(font, style, x, y);
		//?}
	}

	/** The style of the character at the given pixel offset of a line, or null. */
	public static Style styleAtWidth(Font font, FormattedCharSequence line, int x) {
		//? if >=26 {
		/*Style[] found = {null};
		float[] advance = {0};
		line.accept((index, style, codepoint) -> {
			advance[0] += font.width(FormattedCharSequence.forward(new String(Character.toChars(codepoint)), style));
			if (advance[0] > x) {
				found[0] = style;
				return false;
			}
			return true;
		});
		return found[0];
		*///?} else {
		return font.getSplitter().componentStyleAtWidth(line, x);
		//?}
	}

	// ---------------------------------------------------------------- textures

	/** Draws part of a texture, tinted with an ARGB colour. */
	public static void blit(GuiGraphics g, ResourceLocation texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, int argb) {
		//? if >=1.21.6 {
		g.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, textureWidth, textureHeight, argb);
		//?} else if >=1.21.2 {
		/*g.blit(net.minecraft.client.renderer.RenderType::guiTextured, texture, x, y, u, v, width, height, textureWidth, textureHeight, argb);
		*///?} else {
		/*tint(argb);
		g.blit(texture, x, y, u, v, width, height, textureWidth, textureHeight);
		untint();
		*///?}
	}

	//? if <1.21.2 {
	/*private static void tint(int argb) {
		com.mojang.blaze3d.systems.RenderSystem.enableBlend();
		com.mojang.blaze3d.systems.RenderSystem.setShaderColor(((argb >> 16) & 0xFF) / 255f, ((argb >> 8) & 0xFF) / 255f, (argb & 0xFF) / 255f, ((argb >>> 24) & 0xFF) / 255f);
	}

	private static void untint() {
		com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
	}
	*///?}

	/** Draws a GUI atlas sprite (1.20.2+). */
	public static void sprite(GuiGraphics g, ResourceLocation sprite, int x, int y, int width, int height) {
		//? if >=1.21.6 {
		g.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height);
		//?} else if >=1.21.2 {
		/*g.blitSprite(net.minecraft.client.renderer.RenderType::guiTextured, sprite, x, y, width, height);
		*///?} else if >=1.20.2 {
		/*g.blitSprite(sprite, x, y, width, height);
		*///?} else {
		/*throw new UnsupportedOperationException("GUI sprites need 1.20.2+");
		*///?}
	}

	// ---------------------------------------------------------------- vanilla widget looks

	public static void button(GuiGraphics g, int x, int y, int width, int height, boolean enabled, boolean hovered) {
		//? if >=1.20.2 {
		sprite(g, Ids.vanilla(!enabled ? "widget/button_disabled" : hovered ? "widget/button_highlighted" : "widget/button"), x, y, width, height);
		//?} else {
		/*int row = !enabled ? 0 : hovered ? 2 : 1;
		g.blitNineSliced(Ids.vanilla("textures/gui/widgets.png"), x, y, width, height, 20, 4, 200, 20, 0, 46 + row * 20);
		*///?}
	}

	public static void sliderTrack(GuiGraphics g, int x, int y, int width, int height) {
		//? if >=1.20.2 {
		sprite(g, Ids.vanilla("widget/slider"), x, y, width, height);
		//?} else {
		/*g.blitNineSliced(Ids.vanilla("textures/gui/slider.png"), x, y, width, height, 20, 4, 200, 20, 0, 0);
		*///?}
	}

	public static void sliderHandle(GuiGraphics g, int x, int y, int width, int height, boolean hovered) {
		//? if >=1.20.2 {
		sprite(g, Ids.vanilla(hovered ? "widget/slider_handle_highlighted" : "widget/slider_handle"), x, y, width, height);
		//?} else {
		/*g.blitNineSliced(Ids.vanilla("textures/gui/slider.png"), x, y, width, height, 20, 4, 200, 20, 0, hovered ? 60 : 40);
		*///?}
	}

	/** @param style 0 advancement, 1 recipe, 2 system, 3 tutorial (the old toasts.png rows) */
	public static void toastBackground(GuiGraphics g, int style, int width, int height) {
		style = Math.max(0, Math.min(3, style));
		//? if >=1.20.2 {
		String[] sprites = {"toast/advancement", "toast/recipe", "toast/system", "toast/tutorial"};
		sprite(g, Ids.vanilla(sprites[style]), 0, 0, width, height);
		//?} else {
		/*g.blit(Ids.vanilla("textures/gui/toasts.png"), 0, 0, 0, style * 32, width, height);
		*///?}
	}

	/** The dirt / menu-list background used behind list GUIs. */
	public static void menuBackground(GuiGraphics g, int x, int y, int width, int height) {
		//? if >=26 {
		/*Screen.extractMenuBackgroundTexture(g, Screen.MENU_BACKGROUND, x, y, 0, 0, width, height);
		*///?} else if >=1.21 {
		Screen.renderMenuBackgroundTexture(g, Screen.MENU_BACKGROUND, x, y, 0, 0, width, height);
		//?} else {
		/*tint(0xFF404040);
		g.blit(Screen.BACKGROUND_LOCATION, x, y, 0, 0.0F, 0.0F, width, height, 32, 32);
		untint();
		*///?}
	}

	public static void tooltip(GuiGraphics g, Font font, List<FormattedCharSequence> lines, int x, int y) {
		//? if >=1.21.6 {
		g.setTooltipForNextFrame(font, lines, x, y);
		//?} else {
		/*g.renderTooltip(font, lines, x, y);
		*///?}
	}

	public static void mobEffect(GuiGraphics g, MobEffect effect, int x, int y) {
		//? if >=26 {
		/*sprite(g, net.minecraft.client.gui.Hud.getMobEffectSprite(net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect)), x, y, 18, 18);
		*///?} else if >=1.21.6 {
		sprite(g, net.minecraft.client.gui.Gui.getMobEffectSprite(net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect)), x, y, 18, 18);
		//?} else if >=1.21.2 {
		/*g.blitSprite(net.minecraft.client.renderer.RenderType::guiTextured, Minecraft.getInstance().getMobEffectTextures().get(net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect)), x, y, 18, 18);
		*///?} else if >=1.20.5 {
		/*g.blit(x, y, 0, 18, 18, Minecraft.getInstance().getMobEffectTextures().get(net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect)));
		*///?} else {
		/*g.blit(x, y, 0, 18, 18, Minecraft.getInstance().getMobEffectTextures().get(effect));
		*///?}
	}
}
