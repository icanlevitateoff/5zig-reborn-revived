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

package io.icanlevitateoff.fivezigrebornrevived.mod.manager.spotify;

import io.icanlevitateoff.fivezigrebornrevived.util.io.http.HttpClient;
import io.icanlevitateoff.fivezigrebornrevived.util.io.http.HttpResponseCallback;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

public class SpotifyHttpClient {

	private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36";

	public static void get(String url, Map<String, String> headers, int timeout, boolean ignoreInvalidCertificates, Executor executor, HttpResponseCallback callback) {
		Map<String, String> all = new HashMap<>(headers);
		all.putIfAbsent("User-Agent", USER_AGENT);
		HttpClient.get(url, all, timeout, ignoreInvalidCertificates, callback);
	}
}
