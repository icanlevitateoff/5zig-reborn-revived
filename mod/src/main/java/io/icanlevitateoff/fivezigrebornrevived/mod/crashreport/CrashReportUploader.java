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

package io.icanlevitateoff.fivezigrebornrevived.mod.crashreport;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.icanlevitateoff.fivezigrebornrevived.mod.I18n;
import io.icanlevitateoff.fivezigrebornrevived.mod.The5zigMod;
import io.icanlevitateoff.fivezigrebornrevived.mod.Version;
import eu.the5zig.util.minecraft.ChatColor;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.Duration;

/**
 * Uploads the crash report of a game crash to mclo.gs, so it can be shared as a link (for example
 * in a GitHub crash issue). Only the crash report is uploaded, not the game log. The link is added
 * to the end of the saved crash report and shown, and copied, the next time the game starts.
 * Turned off with the "reportCrashes" setting.
 */
public final class CrashReportUploader {

	private static final String API = "https://api.mclo.gs/1/log";
	/** mclo.gs accepts 10 MiB; stay well under it. */
	private static final int MAX_CHARS = 4 * 1024 * 1024;
	private static final String LAST_UPLOAD_FILE = "last-crash-upload.txt";

	/** Set when the client itself is crashing, so crash reports of other kinds are not uploaded. */
	private static volatile boolean clientCrashing;
	private static volatile boolean uploaded;

	private CrashReportUploader() {
	}

	public static void onClientCrash() {
		clientCrashing = true;
	}

	/** Called after a crash report was written. Blocks for at most a few seconds. */
	public static void onCrashReportSaved(File file) {
		if (!clientCrashing || uploaded || file == null || !file.isFile() || !enabled()) {
			return;
		}
		uploaded = true;
		try {
			String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
			if (content.length() > MAX_CHARS) {
				content = content.substring(0, MAX_CHARS);
			}
			String url = upload(content);
			Files.write(file.toPath(), ("\n// Uploaded by 5zig Reborn Revived: " + url + "\n").getBytes(StandardCharsets.UTF_8),
					StandardOpenOption.APPEND);
			File modDir = The5zigMod.getModDirectory();
			if (modDir != null) {
				Files.write(new File(modDir, LAST_UPLOAD_FILE).toPath(), url.getBytes(StandardCharsets.UTF_8));
			}
			log("Crash report uploaded to " + url);
		} catch (Throwable t) {
			log("Could not upload the crash report: " + t);
		}
	}

	/** Shows (and copies) the link of a crash report uploaded during the previous session. */
	public static void announcePrevious() {
		File modDir = The5zigMod.getModDirectory();
		if (modDir == null) {
			return;
		}
		File file = new File(modDir, LAST_UPLOAD_FILE);
		if (!file.isFile()) {
			return;
		}
		try {
			String url = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8).trim();
			Files.delete(file.toPath());
			if (!url.startsWith("https://")) {
				return;
			}
			The5zigMod.getVars().runOnMainThread(() -> The5zigMod.getVars().setClipboard(url));
			The5zigMod.getOverlayMessage().displayMessageAndSplit(ChatColor.YELLOW + I18n.translate("crash_report.uploaded", url));
		} catch (IOException e) {
			The5zigMod.logger.warn("Could not read the last crash report link", e);
		}
	}

	private static boolean enabled() {
		try {
			return The5zigMod.getConfig() == null || The5zigMod.getConfig().getBool("reportCrashes");
		} catch (Throwable t) {
			return true;
		}
	}

	private static String upload(String content) throws IOException, InterruptedException {
		JsonObject body = new JsonObject();
		body.addProperty("content", content);
		body.addProperty("source", Version.getNameAndVersion());
		HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
		HttpRequest request = HttpRequest.newBuilder(URI.create(API))
				.timeout(Duration.ofSeconds(10))
				.header("Content-Type", "application/json")
				.header("User-Agent", Version.getNameAndVersion())
				.POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
				.build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
		if (!json.has("success") || !json.get("success").getAsBoolean() || !json.has("url")) {
			throw new IOException("mclo.gs answered " + response.statusCode() + ": "
					+ (json.has("error") ? json.get("error").getAsString() : response.body()));
		}
		return json.get("url").getAsString();
	}

	private static void log(String message) {
		if (The5zigMod.logger != null) {
			The5zigMod.logger.info(message);
		} else {
			System.out.println("[5zig] " + message);
		}
	}
}
