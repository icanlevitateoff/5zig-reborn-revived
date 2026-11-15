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

package io.icanlevitateoff.fivezigrebornrevived.mod.listener;

import io.icanlevitateoff.fivezigrebornrevived.mod.The5zigMod;
import io.icanlevitateoff.fivezigrebornrevived.mod.config.JoinText;
import eu.the5zig.mod.event.EventHandler;
import eu.the5zig.mod.event.ServerJoinEvent;
import eu.the5zig.mod.event.TickEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Sends the join auto-texts of a server.
 *
 * <p>The texts run in list order, in steps. A text with "wait for server switch" on starts a new
 * step: it waits until every earlier text has been sent and the proxy (Velocity, BungeeCord)
 * has then moved you to another server. Delays are in milliseconds and count from the start of
 * their step, which is the join for the first step and the server switch for the others, so
 * texts without the option behave as before.
 *
 * <p>Texts are only started by a real connection (through the connect screen). Since 1.20.2 a
 * proxy switch also looks like a fresh join to the rest of the mod; here it only continues a
 * step that is waiting, instead of sending everything again.
 */
public class JoinTextListener {

	/** Give up on a step if no server switch comes within this time, or after being offline this long. */
	private static final long TIMEOUT_MS = 60_000;

	private static JoinTextListener instance;

	/** The texts still to send, as steps; null when nothing is running. */
	private List<List<JoinText>> steps;
	/** When the current step started, or -1 while waiting for a server switch. */
	private long stepStart;
	private long waitingSince;
	private long offlineSince;
	/** Set when a connection is started from the connect screen; the next join plans the texts. */
	private boolean newConnection;

	public JoinTextListener() {
		instance = this;
	}

	/** Called when the player connects to a server (not when a proxy moves them). */
	public static void onNewConnection() {
		JoinTextListener listener = instance;
		if (listener != null) {
			listener.newConnection = true;
			listener.steps = null;
		}
	}

	/** Called when the game receives a new "join game" packet, which proxies send on every server switch. */
	public static void onServerSwitch() {
		JoinTextListener listener = instance;
		if (listener != null && listener.steps != null && listener.stepStart < 0) {
			listener.stepStart = System.currentTimeMillis();
		}
	}

	@EventHandler
	public void onServerJoin(ServerJoinEvent event) {
		if (!newConnection) {
			onServerSwitch(); // a proxy switch since 1.20.2 (the client goes through configuration again)
			return;
		}
		newConnection = false;
		List<List<JoinText>> plan = new ArrayList<>();
		for (JoinText text : The5zigMod.getJoinTextConfiguration().getConfigInstance().getTexts()) {
			if (text.getMessage() == null || text.getMessage().isEmpty()) {
				continue;
			}
			if (text.getServer() != null && (text.getServerPattern() == null || !text.getServerPattern().matcher(event.getHost()).matches())) {
				continue;
			}
			// The first text has nothing to wait for: the join itself starts it.
			if (plan.isEmpty() || text.isWaitForSwitch()) {
				plan.add(new ArrayList<>());
			}
			plan.get(plan.size() - 1).add(text);
		}
		steps = plan.isEmpty() ? null : plan;
		stepStart = System.currentTimeMillis();
	}

	@EventHandler
	public void onTick(TickEvent event) {
		if (steps == null) {
			return;
		}
		long now = System.currentTimeMillis();
		if (!The5zigMod.getVars().hasNetworkManager()) {
			// Not in game: disconnected, or in the middle of a proxy switch. Wait, but not forever.
			if (offlineSince == 0) {
				offlineSince = now;
			} else if (now - offlineSince > TIMEOUT_MS) {
				steps = null;
			}
			return;
		}
		offlineSince = 0;
		if (stepStart < 0) {
			if (now - waitingSince > TIMEOUT_MS) {
				The5zigMod.logger.info("Join texts: no server switch within " + TIMEOUT_MS / 1000 + " s, the rest is not sent.");
				steps = null;
			}
			return;
		}
		List<JoinText> step = steps.get(0);
		long elapsed = now - stepStart;
		// Within a step each text has its own delay, as before; list order breaks ties.
		for (java.util.Iterator<JoinText> it = step.iterator(); it.hasNext(); ) {
			JoinText text = it.next();
			if (elapsed >= text.getDelayMs()) {
				it.remove();
				The5zigMod.getListener().doSendChatMessage(text.getMessage());
			}
		}
		if (step.isEmpty()) {
			steps.remove(0);
			if (steps.isEmpty()) {
				steps = null;
			} else {
				stepStart = -1;
				waitingSince = now;
			}
		}
	}
}
