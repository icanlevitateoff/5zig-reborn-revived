/*
 * Copyright (c) 2019-2020 5zig Reborn
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

package io.icanlevitateoff.fivezigrebornrevived.mod.account;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.function.BooleanSupplier;

/**
 * Microsoft account login for the account switcher: OAuth device code flow, then
 * Xbox Live → XSTS → Minecraft services.
 *
 * <p>The Azure application (client) ID is set at build time ({@code auth.azure_client_id} in
 * gradle.properties). Mojang only accepts Azure apps it has approved for Minecraft services; an
 * unapproved app fails at {@link #loginWithXbox} with "Invalid app registration".</p>
 */
public final class MicrosoftAuth {

	private static final String TENANT = "consumers";
	private static final String SCOPE = "XboxLive.signin offline_access";
	private static final String DEVICE_CODE_URL = "https://login.microsoftonline.com/" + TENANT + "/oauth2/v2.0/devicecode";
	private static final String TOKEN_URL = "https://login.microsoftonline.com/" + TENANT + "/oauth2/v2.0/token";
	private static final String XBL_URL = "https://user.auth.xboxlive.com/user/authenticate";
	private static final String XSTS_URL = "https://xsts.auth.xboxlive.com/xsts/authorize";
	private static final String MC_LOGIN_URL = "https://api.minecraftservices.com/authentication/login_with_xbox";
	private static final String MC_PROFILE_URL = "https://api.minecraftservices.com/minecraft/profile";

	private static final String CLIENT_ID = loadClientId();

	private MicrosoftAuth() {
	}

	/** @return true if this build has an Azure client ID (Microsoft login is possible at all). */
	public static boolean isConfigured() {
		return CLIENT_ID != null && !CLIENT_ID.isEmpty();
	}

	private static String loadClientId() {
		try (InputStream in = MicrosoftAuth.class.getResourceAsStream("/fivezigrebornrevived-auth.properties")) {
			if (in == null) {
				return null;
			}
			Properties properties = new Properties();
			properties.load(in);
			String id = properties.getProperty("azureClientId", "").trim();
			return id.startsWith("${") ? null : id;
		} catch (IOException e) {
			return null;
		}
	}

	// ------------------------------------------------------------------ device code flow

	/** What the player has to do: open {@link #verificationUri} and enter {@link #userCode}. */
	public static final class DeviceCode {
		public final String userCode;
		public final String verificationUri;
		final String deviceCode;
		final int intervalSeconds;
		final long expiresAt;

		DeviceCode(String userCode, String verificationUri, String deviceCode, int intervalSeconds, long expiresAt) {
			this.userCode = userCode;
			this.verificationUri = verificationUri;
			this.deviceCode = deviceCode;
			this.intervalSeconds = intervalSeconds;
			this.expiresAt = expiresAt;
		}
	}

	/** A logged-in Minecraft account. */
	public static final class Result {
		public final String name;
		/** Undashed UUID, as Minecraft services return it. */
		public final String uuid;
		public final String accessToken;
		/** Microsoft refresh token: store it (encrypted) to log in again without a code. */
		public final String refreshToken;

		Result(String name, String uuid, String accessToken, String refreshToken) {
			this.name = name;
			this.uuid = uuid;
			this.accessToken = accessToken;
			this.refreshToken = refreshToken;
		}
	}

	public static final class AuthException extends Exception {
		public AuthException(String message) {
			super(message);
		}
	}

	public static DeviceCode requestDeviceCode() throws IOException, AuthException {
		requireConfigured();
		JsonObject json = postForm(DEVICE_CODE_URL, Map.of("client_id", CLIENT_ID, "scope", SCOPE));
		if (json.has("error")) {
			throw new AuthException(describeOAuthError(json));
		}
		return new DeviceCode(json.get("user_code").getAsString(), json.get("verification_uri").getAsString(),
				json.get("device_code").getAsString(), json.has("interval") ? json.get("interval").getAsInt() : 5,
				System.currentTimeMillis() + json.get("expires_in").getAsLong() * 1000L);
	}

	/**
	 * Waits until the player has entered the code, then completes the login.
	 *
	 * @param cancelled polled between attempts; return true to stop
	 */
	public static Result awaitLogin(DeviceCode code, BooleanSupplier cancelled) throws IOException, AuthException, InterruptedException {
		long interval = code.intervalSeconds * 1000L;
		while (System.currentTimeMillis() < code.expiresAt) {
			Thread.sleep(interval);
			if (cancelled.getAsBoolean()) {
				throw new AuthException("Cancelled.");
			}
			JsonObject json = postForm(TOKEN_URL, Map.of("client_id", CLIENT_ID,
					"grant_type", "urn:ietf:params:oauth:grant-type:device_code", "device_code", code.deviceCode));
			if (!json.has("error")) {
				return completeLogin(json.get("access_token").getAsString(), json.get("refresh_token").getAsString());
			}
			switch (json.get("error").getAsString()) {
				case "authorization_pending":
					break;
				case "slow_down":
					interval += 5000;
					break;
				default:
					throw new AuthException(describeOAuthError(json));
			}
		}
		throw new AuthException("The code expired. Start the login again.");
	}

	/** Logs in again from a stored refresh token (no code needed). */
	public static Result refresh(String refreshToken) throws IOException, AuthException {
		requireConfigured();
		JsonObject json = postForm(TOKEN_URL, Map.of("client_id", CLIENT_ID, "grant_type", "refresh_token",
				"refresh_token", refreshToken, "scope", SCOPE));
		if (json.has("error")) {
			throw new AuthException("Microsoft login expired, add the account again (" + describeOAuthError(json) + ")");
		}
		String newRefresh = json.has("refresh_token") ? json.get("refresh_token").getAsString() : refreshToken;
		return completeLogin(json.get("access_token").getAsString(), newRefresh);
	}

	// ------------------------------------------------------------------ Xbox → Minecraft

	private static Result completeLogin(String msAccessToken, String refreshToken) throws IOException, AuthException {
		// Xbox Live user token
		JsonObject xblRequest = new JsonObject();
		JsonObject xblProps = new JsonObject();
		xblProps.addProperty("AuthMethod", "RPS");
		xblProps.addProperty("SiteName", "user.auth.xboxlive.com");
		xblProps.addProperty("RpsTicket", "d=" + msAccessToken);
		xblRequest.add("Properties", xblProps);
		xblRequest.addProperty("RelyingParty", "http://auth.xboxlive.com");
		xblRequest.addProperty("TokenType", "JWT");
		HttpResult xbl = postJson(XBL_URL, xblRequest, null);
		if (xbl.code != 200) {
			throw new AuthException("Xbox Live sign-in failed (HTTP " + xbl.code + ").");
		}
		String xblToken = xbl.json.get("Token").getAsString();
		String userHash = xbl.json.getAsJsonObject("DisplayClaims").getAsJsonArray("xui").get(0).getAsJsonObject().get("uhs").getAsString();

		// XSTS token for Minecraft services
		JsonObject xstsRequest = new JsonObject();
		JsonObject xstsProps = new JsonObject();
		xstsProps.addProperty("SandboxId", "RETAIL");
		JsonArray userTokens = new JsonArray();
		userTokens.add(xblToken);
		xstsProps.add("UserTokens", userTokens);
		xstsRequest.add("Properties", xstsProps);
		xstsRequest.addProperty("RelyingParty", "rp://api.minecraftservices.com/");
		xstsRequest.addProperty("TokenType", "JWT");
		HttpResult xsts = postJson(XSTS_URL, xstsRequest, null);
		if (xsts.code == 401 && xsts.json != null && xsts.json.has("XErr")) {
			throw new AuthException(describeXErr(xsts.json.get("XErr").getAsLong()));
		}
		if (xsts.code != 200) {
			throw new AuthException("Xbox authorisation failed (HTTP " + xsts.code + ").");
		}
		String xstsToken = xsts.json.get("Token").getAsString();

		String mcToken = loginWithXbox(userHash, xstsToken);

		// Profile (also proves the account owns Minecraft)
		HttpResult profile = get(MC_PROFILE_URL, mcToken);
		if (profile.code == 404) {
			throw new AuthException("This Microsoft account does not own Minecraft: Java Edition.");
		}
		if (profile.code != 200) {
			throw new AuthException("Could not load the Minecraft profile (HTTP " + profile.code + ").");
		}
		return new Result(profile.json.get("name").getAsString(), profile.json.get("id").getAsString(), mcToken, refreshToken);
	}

	private static String loginWithXbox(String userHash, String xstsToken) throws IOException, AuthException {
		JsonObject request = new JsonObject();
		request.addProperty("identityToken", "XBL3.0 x=" + userHash + ";" + xstsToken);
		HttpResult result = postJson(MC_LOGIN_URL, request, null);
		if (result.code != 200) {
			String detail = result.json != null && result.json.has("errorMessage") ? result.json.get("errorMessage").getAsString() : "HTTP " + result.code;
			if (detail.toLowerCase().contains("invalid app registration")) {
				throw new AuthException("This build's Azure app is not approved by Mojang for Minecraft login yet.");
			}
			throw new AuthException("Minecraft login failed: " + detail);
		}
		return result.json.get("access_token").getAsString();
	}

	private static String describeXErr(long xerr) {
		if (xerr == 2148916233L) {
			return "This Microsoft account has no Xbox profile. Sign in once at minecraft.net or xbox.com, then try again.";
		}
		if (xerr == 2148916235L) {
			return "Xbox Live is not available in this account's country.";
		}
		if (xerr == 2148916236L || xerr == 2148916237L) {
			return "This account needs adult verification on xbox.com.";
		}
		if (xerr == 2148916238L) {
			return "This is a child account: an adult has to add it to a Microsoft family first.";
		}
		return "Xbox authorisation failed (XErr " + xerr + ").";
	}

	private static String describeOAuthError(JsonObject json) {
		String error = json.get("error").getAsString();
		switch (error) {
			case "authorization_declined":
				return "The login was declined.";
			case "expired_token":
				return "The code expired. Start the login again.";
			case "invalid_client":
			case "unauthorized_client":
				return "This build's Azure app is not set up for Microsoft login (" + error + ").";
			default:
				return json.has("error_description") ? json.get("error_description").getAsString().split("\r?\n")[0] : error;
		}
	}

	private static void requireConfigured() throws AuthException {
		if (!isConfigured()) {
			throw new AuthException("Microsoft login is not configured in this build (no Azure client ID).");
		}
	}

	// ------------------------------------------------------------------ HTTP

	private static final class HttpResult {
		final int code;
		final JsonObject json;

		HttpResult(int code, JsonObject json) {
			this.code = code;
			this.json = json;
		}
	}

	private static JsonObject postForm(String url, Map<String, String> form) throws IOException {
		StringJoiner body = new StringJoiner("&");
		for (Map.Entry<String, String> entry : form.entrySet()) {
			body.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "=" + URLEncoder.encode(entry.getValue(), "UTF-8"));
		}
		HttpResult result = send("POST", url, "application/x-www-form-urlencoded", body.toString(), null);
		if (result.json == null) {
			throw new IOException("Unexpected response from Microsoft (HTTP " + result.code + ")");
		}
		return result.json;
	}

	private static HttpResult postJson(String url, JsonObject body, String bearer) throws IOException {
		return send("POST", url, "application/json", body.toString(), bearer);
	}

	private static HttpResult get(String url, String bearer) throws IOException {
		return send("GET", url, null, null, bearer);
	}

	private static HttpResult send(String method, String url, String contentType, String body, String bearer) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		try {
			connection.setRequestMethod(method);
			connection.setConnectTimeout(10000);
			connection.setReadTimeout(15000);
			connection.setRequestProperty("Accept", "application/json");
			if (bearer != null) {
				connection.setRequestProperty("Authorization", "Bearer " + bearer);
			}
			if (body != null) {
				connection.setDoOutput(true);
				connection.setRequestProperty("Content-Type", contentType);
				try (OutputStream out = connection.getOutputStream()) {
					out.write(body.getBytes(StandardCharsets.UTF_8));
				}
			}
			int code = connection.getResponseCode();
			InputStream stream = code >= 400 ? connection.getErrorStream() : connection.getInputStream();
			JsonObject json = null;
			if (stream != null) {
				try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
					json = JsonParser.parseReader(reader).getAsJsonObject();
				} catch (RuntimeException ignored) {
					// empty or non-JSON body
				}
			}
			return new HttpResult(code, json);
		} finally {
			connection.disconnect();
		}
	}
}
