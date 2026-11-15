# 5zig Reborn Revived: Fabric port (handoff notes)

Targets: Fabric 1.20.1, 1.20.4, 1.21.1, 1.21.3–1.21.10, 26.3 (12 Stonecutter nodes, vcs/active version 1.21.8).
Versioning: `<mc>-v<mod.version>`, e.g. `5zig-Reborn-Revived-1.21.8-v0.1.0.jar`.

## Decisions
- Build: Gradle 9.9.1, Stonecutter 0.7.10, Fabric Loom 1.19.4 (needs a Java 25 Gradle daemon, see
  gradle/gradle-daemon-jvm.properties). `fabric-loom-remap` + Mojang mappings for 1.20.1–1.21.10,
  plain `fabric-loom` (no mappings, no remap) for unobfuscated 26.x.
- Java per node: 17 (1.20.x), 21 (1.21.x), 25 (26.x); the mixin compatibility level follows it.
- Forge/launchwrapper builds dropped. Fabric only. Branch: `fabric-port`.
- Renamed (2026-11-15): mod "5zig Reborn Revived", id `fivezigrebornrevived`,
  packages `io.icanlevitateoff.fivezigrebornrevived.*` for core (mod/) and the Fabric layer (src/).
  The **plugin API (api/) keeps `eu.the5zig.*`** so existing 5zig plugins still load.
  Core reaches the API's package-private fields through three bridge classes that stay in
  `eu.the5zig.mod(.modules|.server)` (`ApiInternals`, `ModuleItemInternals`, `ServerInternals`).
  Old configs storing `eu.the5zig.*` class names are mapped on load (`ServerAdapter`).
- Original credits kept (5zig, RoccoDev, zortax; credits.txt, README, licence headers); IcanLevitate added.
- Asset namespace stays `the5zigmod` (textures, lang, keybind keys).

## Layout
- `mod/src/main/java` core (version independent), `api/` plugin API, `i18n/` translations: plain source dirs.
- `src/main/java/io/icanlevitateoff/fivezigrebornrevived/fabric`: the version layer (Stonecutter-processed):
  - `Variables` (IVariables), `GLUtilHandle`, `RenderContext` (current GuiGraphics + emulated bind/colour state;
    core still draws "bind texture, set colour, draw"), `gui/*` widgets written standalone (not vanilla widgets),
    `GuiHandle` (Screen hosting a core Gui), `Gui2ndChat`, `Overlay` (toasts), `ResourceManager` (capes),
    `net/RawPayload` (plugin channels as raw-byte payloads), `FiveZigClient` (entrypoint).
  - Hooks: Fabric API events for init/tick/chat/HUD layers/screen buttons; mixins for the rest (`fabric/mixin`).
- `compat/`: every version-dependent call goes through these, so most files carry no `//? if` at all:
  - `Ids` (resource ids), `Draw` (pose stack, blits/sprites, text, items, tooltips, widget looks, toast/menu
    backgrounds), `Mc` (screen/chat/HUD accessors that moved into `Gui`/`Hud` in 26.x, key state, textures,
    inventory, input events), `Keys` (key/mouse codes, below).
- `versions/<mc>/gradle.properties`: deps per version (loader 0.20.1, Fabric API, MC range, Java).
- `tools/replacements.txt`: the 26.x text renames (ResourceLocation→Identifier, GuiGraphics→GuiGraphicsExtractor,
  KeyBindingHelper→KeyMappingHelper, …). `build.gradle.kts` has the same list as reversible Stonecutter regex
  replacements; keep them in sync.
- `api/` and `i18n/` are vendored (were upstream submodules). The old Forge layers (`version-specific/`) were
  dropped from the repo; get them from github.com/5zig-reborn/version-support if needed.

## Version notes worth knowing
- **26.x uses SDL3 scancodes** (A=4, Esc=41, left mouse=1). Core and saved 5zig keybinds stay GLFW;
  `compat/Keys` translates at every boundary (generated table matched by `InputConstants.KEY_*` name).
  "No key" is -1 in core but 0 natively; never pass a negative code to vanilla (KeyMapping.setAll crashes).
- `GameProfile` is a record from authlib 7 (1.21.9+); core uses `util/GameProfiles` (method handles).
- Inactive `//? if` branches are `/* … */` blocks: **no `/** */` javadoc inside them** (closes the comment).
- HUD: Fabric HudElementRegistry from 1.21.6; before that one HudRenderCallback + MixinGui hides the
  vanilla potion indicator.
- Plugin channels: raw-byte payload types from 1.20.5, plain channel+FriendlyByteBuf before.

## Status (2026-11-15)
- `./gradlew build` builds all 12 jars into `versions/<mc>/build/libs/`.
- Every node compiles; mixin targets (names, descriptors, parameter counts) were checked against each
  version's Minecraft jar.
- Run in-game: 1.20.1 (title screen), 26.3 (world: HUD toggle, chat filter to second chat), 1.21.8 earlier.
- `runClient` starts muted (sets `soundCategory_master:0.0` in `run/<mc>/options.txt`) as offline user
  `5ZRRDEVTEST`; test multiplayer on offline-mode servers (e.g. pika-network).
- Dev run uses `-D5zig.debug=true` → core points its network at localhost:28499 (expected to fail).
- 5zig Reborn backend appears offline: update check, capes, friends/chat network won't connect.

## Ported since the first pass
- 5zig section in crash reports (`MixinMinecraft.fillReport`); core's CrashReportUtil now goes through
  `IVariables.crash` (it used obfuscated class names before and could not work on Fabric).
- Crash uploads: `MixinMinecraft` marks a client crash (`crash` HEAD), `MixinCrashReport` uploads the
  written report via `CrashReportUploader` (mclo.gs). The old crash-hopper.jar (5zig.net) is gone.
- Now Playing: `mod/manager/media` (MPRIS via gdbus / Windows SMTC via a PowerShell helper / macOS
  AppleScript). The module is still registered as `SPOTIFY` so saved layouts keep it.
- Removed for safety: remote plugin store (PluginSocket on port 31415), plugin self-updates, the 5zig.net
  easter egg, the trust-all HTTPS client.
- Ping in ms on the server list (`MixinServerListEntry`, option "ping_on_serverlist").
- Chat search + highlight words (`MixinGuiMessageLine` + `util/ChatHighlight`): matches are recoloured and
  underlined rather than boxed, so it works on every version without hooking the chat renderer's lambdas.
- Transparent chat background (`MixinChatComponent`, wraps the text-background-opacity read).
- `%player%` (alias `%plr%`) = current account name in chat filters (message), highlight words and chat
  search (`util/PlayerPlaceholder`). Invalid filter patterns are logged once and skipped.
- Version string everywhere: "5zig Reborn Revived <mc>-v<mod.version>", read from Fabric mod metadata.

## Not ported / known gaps
- Reward tags above players: skipped for now. They only display tags pushed by the 5zig backend (offline);
  revisit together with a custom backend. Needs three renderers (pre-1.21.2, render states, 1.21.9+ submit).
- Battery module (Windows-only Kernel32) returns no data; iTunes on Windows removed (JACOB).
- Account switcher: Microsoft accounts via device code (`account/MicrosoftAuth`, `GuiMicrosoftLogin`; client ID from
  `auth.azure_client_id`) and offline accounts. Switching also rebuilds vanilla's chat-signing keys and user API service.

## In-game test checklist (per version)
1. Title screen: version text top-left, "last server" button next to Multiplayer.
2. Options → "The 5zig Mod" opens 5zig settings; lists, sliders, text fields, colour pickers.
3. In a world: HUD modules; F1 hides them; toggle-mod key (M by default).
4. Keybinds under Controls (category "The 5zig Mod"); rebinding survives a restart.
5. Chat: time prefix, second chat (links clickable), chat search, highlight words, transparent background.
6. Multiplayer list with "ping on server list"; tab list with "ping on tab"; own name tag (F5).
7. Pause menu "confirm disconnect"; disconnect screen auto-reconnect countdown.
