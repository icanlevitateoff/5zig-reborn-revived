# 5zig Reborn Revived
A port of 5-Zig Reborn to modern Fabric versions, with extra features.

A Fabric revival of The 5zig Mod by IcanLevitate, for Minecraft 1.20.1, 1.20.4, 1.21.1, 1.21.3–1.21.11 and 26.3.
Based on [The 5zig Mod](https://github.com/5zig-reborn/The-5zig-Mod) by 5zig and the 5zig Reborn team
(RoccoDev, zortax and contributors).

## Installing
Download the jar for your Minecraft version from [Releases](https://github.com/icanlevitateoff/5zig-reborn-revived/releases)
and put it in your `mods` folder. Requires **Fabric Loader 0.17.0+** and **Fabric API**.

| Minecraft | Jar |
|---|---|
| 1.20.1 | `5zig-Reborn-Revived-1.20.1-v<version>.jar` |
| 1.20.3 / 1.20.4 | `5zig-Reborn-Revived-1.20.4-v<version>.jar` |
| 1.21 / 1.21.1 | `5zig-Reborn-Revived-1.21.1-v<version>.jar` |
| 1.21.2 / 1.21.3 | `5zig-Reborn-Revived-1.21.3-v<version>.jar` |
| 1.21.4 – 1.21.11 | `5zig-Reborn-Revived-<minecraft version>-v<version>.jar` |
| 26.3 | `5zig-Reborn-Revived-26.3-v<version>.jar` |

## What works
- HUD modules, 5zig settings and keybinds
- Second chat, chat filters (`%player%` / `%plr%` = your username), time-stamped chat
- Chat search, highlight words, transparent chat background
- Ping on the tab list and on the server list
- Account switcher with Microsoft accounts (sign in with a code at microsoft.com/link) and offline accounts
- HUD layout editor (Right Shift, or "Edit HUD Layout" in Modules): drag modules, snap to edges and other
  modules, resize with the corner or mouse wheel, right click for module settings
- Confirm-disconnect, auto-reconnect, last-server button
- Join auto-texts with delays in milliseconds, optionally waiting for a proxy (Velocity/BungeeCord) server
  switch, e.g. `/server survival` and then `/home` once you are there
- Discord Rich Presence (menu / server you play on)
- "Now Playing" module and media keybinds (play/pause, next, previous) for any player: on Linux every
  MPRIS player (Spotify, tidal-hifi, browsers, ...), on Windows whatever shows in the Windows media
  controls (Spotify, TIDAL, browsers, ...), on macOS Spotify and Apple Music. If Minecraft runs as a
  Flatpak (e.g. Prism Launcher from Flathub), allow it to see players once:
  `flatpak override --user --talk-name=org.mpris.MediaPlayer2.* org.prismlauncher.PrismLauncher`
- 5zig section in crash reports; with "Upload Crash Reports" on (the default), a crash report is uploaded
  to [mclo.gs](https://mclo.gs) and the link is shown and copied the next time the game starts

Tested in-game on 1.20.1, 1.21.4, 1.21.8, 1.21.9 and 26.3. The other versions build from the same code
paths but have not all been launched yet.

## What's missing
- **Online features**: the original 5zig Reborn backend is offline, so the friends/chat network and capes
  do not work. A custom backend may come later. Downloading plugins from the old plugin store, and plugins
  updating themselves, were removed; install plugins by putting their jar in `the5zigmod/plugins`.
- **Reward tags** above players: not ported (they need the backend).
- **Battery module** shows no data (Windows-only); the Windows iTunes integration was removed.
- Forge, NeoForge and versions older than 1.20.1 are not supported.

## Building
```
./gradlew build
```
Needs only a JDK to start Gradle; the build provisions Java 17/21/25 per Minecraft version itself.
Jars land in `versions/<minecraft version>/build/libs/5zig-Reborn-Revived-<minecraft version>-v<version>.jar`.
`./gradlew :1.21.8:runClient` starts a dev client (muted, offline user `5ZRRDEVTEST`; use offline-mode
servers for multiplayer testing). See [FABRIC_PORT_NOTES.md](FABRIC_PORT_NOTES.md) for how the
multi-version setup works.

## Microsoft login (account switcher)
Microsoft sign-in needs an Azure application (client) ID, set at build time:
```
# gradle.properties
auth.azure_client_id=<your Azure application (client) ID>
```
(or the `FIVEZIG_AZURE_CLIENT_ID` environment variable). Builds without it hide nothing but show
"Microsoft login is not available in this build" when you press the button.

The Azure app must be registered for **personal Microsoft accounts**, with **"Allow public client
flows"** enabled, and Mojang must approve it for Minecraft services
([form](https://aka.ms/mce-reviewappid)); until then Microsoft sign-in works but the Minecraft step
fails with "not approved by Mojang". Refresh tokens are stored in the password-encrypted accounts file.

## Making a release
1. Bump `mod.version` in `gradle.properties` (e.g. `0.1.1` → `0.1.2`).
2. Run `./gradlew build` and check it ends with `BUILD SUCCESSFUL`; all 12 jars should be in
   `versions/*/build/libs/` with the new version in their names (ignore the `-sources` jars).
3. Commit and push to `main`.
4. On GitHub: **Releases → Draft a new release** → choose tag `v<version>` ("create new tag on publish",
   target `main`), title `5zig Reborn Revived v<version>`.
5. Release notes: only the changes since the previous release, e.g.
   ```
   ## Changes
   - Fixed …
   ```
6. Attach the 12 jars and publish. Tick "pre-release" if some versions have not been tested in-game.

## License
GPLv3, like the original project. See [LICENSE](LICENSE).
