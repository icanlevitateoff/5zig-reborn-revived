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

import com.mojang.authlib.GameProfile;
import io.icanlevitateoff.fivezigrebornrevived.mod.MinecraftFactory;
import io.icanlevitateoff.fivezigrebornrevived.mod.The5zigMod;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class MixinAbstractClientPlayer {

	@Inject(method = "<init>", at = @At("RETURN"))
	private void the5zig$init(ClientLevel level, GameProfile profile, CallbackInfo ci) {
		if (The5zigMod.hasBeenInitialized()) {
			MinecraftFactory.getVars().getResourceManager().loadPlayerTextures(profile);
		}
	}

	private ResourceLocation the5zig$capeLocation() {
		if (!The5zigMod.hasBeenInitialized()) {
			return null;
		}
		Object cape = MinecraftFactory.getVars().getResourceManager().getCapeLocation(((Player) (Object) this).getGameProfile());
		return cape instanceof ResourceLocation location ? location : null;
	}

	/** 5zig capes take priority over vanilla ones (also used for the elytra). */
	//? if >=1.21.9 {
	/*@Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
	private void the5zig$cape(CallbackInfoReturnable<net.minecraft.world.entity.player.PlayerSkin> cir) {
		ResourceLocation location = the5zig$capeLocation();
		if (location != null) {
			net.minecraft.world.entity.player.PlayerSkin skin = cir.getReturnValue();
			net.minecraft.core.ClientAsset.ResourceTexture texture = new net.minecraft.core.ClientAsset.ResourceTexture(location, location);
			cir.setReturnValue(new net.minecraft.world.entity.player.PlayerSkin(skin.body(), texture, texture, skin.model(), skin.secure()));
		}
	}
	*///?} else if >=1.20.2 {
	@Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
	private void the5zig$cape(CallbackInfoReturnable<net.minecraft.client.resources.PlayerSkin> cir) {
		ResourceLocation location = the5zig$capeLocation();
		if (location != null) {
			net.minecraft.client.resources.PlayerSkin skin = cir.getReturnValue();
			cir.setReturnValue(new net.minecraft.client.resources.PlayerSkin(skin.texture(), skin.textureUrl(), location, location, skin.model(), skin.secure()));
		}
	}
	//?} else {
	/*@Inject(method = {"getCloakTextureLocation", "getElytraTextureLocation"}, at = @At("RETURN"), cancellable = true)
	private void the5zig$cape(CallbackInfoReturnable<ResourceLocation> cir) {
		ResourceLocation location = the5zig$capeLocation();
		if (location != null) {
			cir.setReturnValue(location);
		}
	}
	*///?}

	//? if >=1.21.2 {
	@Inject(method = "getFieldOfViewModifier", at = @At("HEAD"), cancellable = true)
	private void the5zig$staticFov(boolean firstPerson, float fovEffectScale, CallbackInfoReturnable<Float> cir) {
	//?} else {
	/*@Inject(method = "getFieldOfViewModifier", at = @At("HEAD"), cancellable = true)
	private void the5zig$staticFov(CallbackInfoReturnable<Float> cir) {
	*///?}
		if (The5zigMod.hasBeenInitialized() && The5zigMod.getConfig().getBool("staticFov")) {
			cir.setReturnValue(1.0F);
		}
	}
}
