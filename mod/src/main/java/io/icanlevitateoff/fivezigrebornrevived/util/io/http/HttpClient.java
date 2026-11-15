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

package io.icanlevitateoff.fivezigrebornrevived.util.io.http;

import com.google.common.base.Preconditions;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Async HTTP GET on top of java.net.http (replaces the old netty-codec-http pipeline,
 * which Minecraft no longer ships).
 */
public class HttpClient {

	public static final int TIMEOUT = 5000;
	private static final String USER_AGENT = "5zig-Reborn";

	private static volatile java.net.http.HttpClient client, insecureClient;

	/** @param executor kept for source compatibility with the old netty signature; may be null. */
	public static void get(String url, Executor executor, HttpResponseCallback callback) {
		get(url, Collections.emptyMap(), TIMEOUT, false, callback);
	}

	public static void get(String url, Map<String, String> headers, int timeout, boolean ignoreInvalidCertificates, HttpResponseCallback callback) {
		Preconditions.checkNotNull(url, "url");
		Preconditions.checkNotNull(callback, "callback");
		HttpRequest.Builder request;
		try {
			request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofMillis(timeout)).GET()
					.header("User-Agent", USER_AGENT);
			for (Map.Entry<String, String> h : headers.entrySet()) {
				request.setHeader(h.getKey(), h.getValue());
			}
		} catch (IllegalArgumentException e) {
			callback.call(null, -1, e);
			return;
		}
		client(ignoreInvalidCertificates).sendAsync(request.build(), HttpResponse.BodyHandlers.ofString())
				.whenComplete((response, error) -> {
					if (error != null) {
						callback.call(null, -1, error);
					} else {
						callback.call(response.body(), response.statusCode(), null);
					}
				});
	}

	private static java.net.http.HttpClient client(boolean insecure) {
		if (!insecure) {
			if (client == null) {
				client = java.net.http.HttpClient.newBuilder().connectTimeout(Duration.ofMillis(TIMEOUT))
						.followRedirects(java.net.http.HttpClient.Redirect.NORMAL).build();
			}
			return client;
		}
		if (insecureClient == null) {
			try {
				SSLContext ctx = SSLContext.getInstance("TLS");
				ctx.init(null, new TrustManager[]{new X509TrustManager() {
					public void checkClientTrusted(X509Certificate[] c, String a) {}
					public void checkServerTrusted(X509Certificate[] c, String a) {}
					public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
				}}, null);
				insecureClient = java.net.http.HttpClient.newBuilder().sslContext(ctx).connectTimeout(Duration.ofMillis(TIMEOUT)).build();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return insecureClient;
	}
}
