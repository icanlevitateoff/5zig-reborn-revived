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

import io.icanlevitateoff.fivezigrebornrevived.util.GameProfiles;
import io.icanlevitateoff.fivezigrebornrevived.fabric.compat.Ids;
import io.icanlevitateoff.fivezigrebornrevived.fabric.compat.Mc;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.NativeImage;
import io.icanlevitateoff.fivezigrebornrevived.mod.MinecraftFactory;
import io.icanlevitateoff.fivezigrebornrevived.mod.Version;
import io.icanlevitateoff.fivezigrebornrevived.mod.api.rewards.RewardsCache;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.ingame.resource.IResourceManager;
import eu.the5zig.mod.util.NetworkPlayerInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** Downloads 5zig capes (and reward tags) from the 5zig Reborn texture server. */
public class ResourceManager implements IResourceManager {

	private static final String BASE_URL = "https://textures.5zigreborn.eu/profile/";
	private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(
			new ThreadFactoryBuilder().setNameFormat("5zig Texture Downloader #%d").setDaemon(true).build());
	private static final Gson GSON = new Gson();
	private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

	/** Cape location per player; an empty entry means "looked up, no cape". */
	private final Cache<UUID, PlayerResource> resources = CacheBuilder.newBuilder().expireAfterAccess(3, TimeUnit.MINUTES).build();
	private final GameProfile playerProfile;

	private static final class PlayerResource {
		volatile ResourceLocation cape;
	}

	private static final class TextureData {
		String d;
		String r;
	}

	public ResourceManager(GameProfile playerProfile) {
		this.playerProfile = playerProfile;
	}

	@Override
	public void loadPlayerTextures(GameProfile profile) {
		if (profile == null || GameProfiles.id(profile) == null || resources.getIfPresent(GameProfiles.id(profile)) != null) {
			return;
		}
		PlayerResource resource = new PlayerResource();
		resources.put(GameProfiles.id(profile), resource);
		EXECUTOR.execute(() -> download(profile, resource));
	}

	private void download(GameProfile profile, PlayerResource resource) {
		try {
			HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + GameProfiles.id(profile)))
					.header("User-Agent", "5zig/" + Version.VERSION).timeout(Duration.ofSeconds(10)).GET().build();
			HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() != 200) {
				return;
			}
			TextureData data = GSON.fromJson(response.body().lines().findFirst().orElse(""), TextureData.class);
			if (data == null) {
				return;
			}
			if (data.r != null) {
				RewardsCache.putReward(GameProfiles.id(profile).toString(), data.r);
			}
			if (data.d != null && !data.d.isEmpty()) {
				byte[] png = Base64.getDecoder().decode(data.d);
				ResourceLocation location = Ids.of("the5zigmod", "capes/" + GameProfiles.id(profile).toString().replace("-", ""));
				Minecraft.getInstance().execute(() -> {
					try {
						NativeImage image = NativeImage.read(png);
						Minecraft.getInstance().getTextureManager().register(location, Mc.texture(location.toString(), image));
						resource.cape = location;
					} catch (Exception e) {
						MinecraftFactory.getClassProxyCallback().getLogger().error("Could not parse cape for " + GameProfiles.id(profile), e);
					}
				});
			}
		} catch (Exception e) {
			MinecraftFactory.getClassProxyCallback().getLogger().debug("Couldn't download 5zig textures for " + GameProfiles.name(profile), e);
		}
	}

	@Override
	public void updateOwnPlayerTextures() {
		if (playerProfile != null && GameProfiles.id(playerProfile) != null) {
			resources.invalidate(GameProfiles.id(playerProfile));
		}
		loadPlayerTextures(MinecraftFactory.getVars().getGameProfile());
	}

	@Override
	public Object getOwnCapeLocation() {
		return getCapeLocation(playerProfile);
	}

	@Override
	public void cleanupTextures() {
		for (PlayerResource resource : resources.asMap().values()) {
			if (resource.cape != null) {
				Minecraft.getInstance().getTextureManager().release(resource.cape);
			}
		}
		resources.invalidateAll();
	}

	/** @param player a GameProfile, a vanilla PlayerInfo or a core NetworkPlayerInfo */
	@Override
	public Object getCapeLocation(Object player) {
		GameProfile profile = player instanceof GameProfile p ? p
				: player instanceof PlayerInfo info ? info.getProfile()
				: player instanceof NetworkPlayerInfo info ? info.getGameProfile() : null;
		if (profile == null || GameProfiles.id(profile) == null) {
			return null;
		}
		PlayerResource resource = resources.getIfPresent(GameProfiles.id(profile));
		return resource == null ? null : resource.cape;
	}
}
