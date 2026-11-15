import org.apache.tools.ant.filters.EscapeUnicode

import net.fabricmc.loom.api.LoomGradleExtensionAPI

plugins {
    java
}

fun prop(name: String) = property(name).toString()

val mcVersion = stonecutter.current.version
val javaVersion = prop("java").toInt()
// 26.x ships unobfuscated: no mappings, no remapping, plain (non-"mod") dependency configurations.
val unobfuscated = stonecutter.eval(mcVersion, ">=26")

apply(plugin = if (unobfuscated) "net.fabricmc.fabric-loom" else "net.fabricmc.fabric-loom-remap")
val loom = extensions.getByType<LoomGradleExtensionAPI>()

// Renames in 26.x that are cleaner as reversible text replacements than as //? blocks.
// Generated from tools/replacements.txt; keep the two in sync.
stonecutter {
    replacements {
        regex(eval(current.version, ">=26")) {
            replace("\\bResourceLocation\\b", "Identifier")
            reverse("\\bIdentifier\\b", "ResourceLocation")
        }
        regex(eval(current.version, ">=26")) {
            replace("\\bnet\\.minecraft\\.Util\\b", "net.minecraft.util.Util")
            reverse("\\bnet\\.minecraft\\.util\\.Util\\b", "net.minecraft.Util")
        }
        regex(eval(current.version, ">=26")) {
            replace("\\bnet\\.minecraft\\.client\\.GuiMessageTag\\b", "net.minecraft.client.multiplayer.chat.GuiMessageTag")
            reverse("\\bnet\\.minecraft\\.client\\.multiplayer\\.chat\\.GuiMessageTag\\b", "net.minecraft.client.GuiMessageTag")
        }
        regex(eval(current.version, ">=26")) {
            replace("\\bGuiGraphics\\b", "GuiGraphicsExtractor")
            reverse("\\bGuiGraphicsExtractor\\b", "GuiGraphics")
        }
        regex(eval(current.version, ">=26")) {
            replace("\\bVanillaHudElements\\.STATUS_EFFECTS\\b", "VanillaHudElements.MOB_EFFECTS")
            reverse("\\bVanillaHudElements\\.MOB_EFFECTS\\b", "VanillaHudElements.STATUS_EFFECTS")
        }
        regex(eval(current.version, ">=26")) {
            replace("\\bvanilla\\.render\\(g, delta\\)", "vanilla.extractRenderState(g, delta)")
            reverse("\\bvanilla\\.extractRenderState\\(g, delta\\)", "vanilla.render(g, delta)")
        }
        regex(eval(current.version, ">=26")) {
            replace("\\bScreenEvents\\.afterRender\\b", "ScreenEvents.afterExtract")
            reverse("\\bScreenEvents\\.afterExtract\\b", "ScreenEvents.afterRender")
        }
        regex(eval(current.version, ">=26")) {
            replace("\\bScreens\\.getButtons\\b", "Screens.getWidgets")
            reverse("\\bScreens\\.getWidgets\\b", "Screens.getButtons")
        }
        regex(eval(current.version, ">=26")) {
            replace("\\bclient\\.keybinding\\.v1\\b", "client.keymapping.v1")
            reverse("\\bclient\\.keymapping\\.v1\\b", "client.keybinding.v1")
        }
        regex(eval(current.version, ">=26")) {
            replace("\\bKeyBindingHelper\\b", "KeyMappingHelper")
            reverse("\\bKeyMappingHelper\\b", "KeyBindingHelper")
        }
        regex(eval(current.version, ">=26")) {
            replace("\\bPayloadTypeRegistry\\.playC2S\\(\\)", "PayloadTypeRegistry.serverboundPlay()")
            reverse("\\bPayloadTypeRegistry\\.serverboundPlay\\(\\)", "PayloadTypeRegistry.playC2S()")
        }
        regex(eval(current.version, ">=26")) {
            replace("\\bPayloadTypeRegistry\\.playS2C\\(\\)", "PayloadTypeRegistry.clientboundPlay()")
            reverse("\\bPayloadTypeRegistry\\.clientboundPlay\\(\\)", "PayloadTypeRegistry.playS2C()")
        }
        regex(eval(current.version, ">=26")) {
            replace("\\bInputConstants\\.Type\\.KEYSYM\\b", "InputConstants.Type.KEYBOARD")
            reverse("\\bInputConstants\\.Type\\.KEYBOARD\\b", "InputConstants.Type.KEYSYM")
        }
        regex(eval(current.version, ">=26")) {
            replace("\"renderPingIcon\"", "\"extractPingIcon\"")
            reverse("\"extractPingIcon\"", "\"renderPingIcon\"")
        }
        regex(eval(current.version, ">=26")) {
            replace("@Inject\\(method = \"renderBackground\"", "@Inject(method = \"extractBackground\"")
            reverse("@Inject\\(method = \"extractBackground\"", "@Inject(method = \"renderBackground\"")
        }
        regex(eval(current.version, ">=26")) {
            replace("\\bnet\\.minecraft\\.client\\.GuiMessage\\b", "net.minecraft.client.multiplayer.chat.GuiMessage")
            reverse("\\bnet\\.minecraft\\.client\\.multiplayer\\.chat\\.GuiMessage\\b", "net.minecraft.client.GuiMessage")
        }
    }
}

version = "$mcVersion-v${prop("mod.version")}"
group = "io.icanlevitateoff"
base { archivesName.set("5zig-Reborn-Revived") }

repositories {
    mavenCentral()
    maven("https://jitpack.io") { content { includeGroup("com.github.5zig-reborn") } }
}

// Libraries Minecraft does not ship. Each one is compiled against and nested in the jar (jar-in-jar).
val bundled = listOf(
    "org.hsqldb:hsqldb:2.7.4",
    "com.googlecode.json-simple:json-simple:1.1.1",
    "com.github.5zig-reborn:DiscordIPC:ac1dbc6c73",
    // DiscordIPC talks to Discord over a UNIX socket on Linux/macOS through junixsocket.
    "com.kohlschutter.junixsocket:junixsocket-common:2.0.4",
    "com.kohlschutter.junixsocket:junixsocket-native-common:2.0.4",
    "org.scijava:native-lib-loader:2.0.2",
)

dependencies {
    "minecraft"("com.mojang:minecraft:$mcVersion")
    val modConfig = if (unobfuscated) "implementation" else "modImplementation"
    if (!unobfuscated) {
        "mappings"(loom.officialMojangMappings())
    }
    modConfig("net.fabricmc:fabric-loader:${prop("deps.fabric_loader")}")
    modConfig("net.fabricmc.fabric-api:fabric-api:${prop("deps.fabric_api")}")

    // Minecraft stopped shipping Apache HttpClient in 26.1; core's Spotify integration still uses it.
    val bundledHere = if (unobfuscated) bundled + listOf(
        "org.apache.httpcomponents:httpclient:4.5.14",
        "org.apache.httpcomponents:httpcore:4.4.16",
    ) else bundled
    bundledHere.forEach {
        "implementation"(it) { isTransitive = false }
        "include"(it)
    }
}

// Core (version independent) + plugin API + translations are shared source trees.
// Only the Fabric version layer in src/ is Stonecutter-processed.
sourceSets.main {
    java.srcDir(rootProject.file("mod/src/main/java"))
    java.srcDir(rootProject.file("api/src/main/java"))
    resources.srcDir(rootProject.file("mod/src/main/resources"))
    resources.srcDir(rootProject.file("i18n/resources"))
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion))
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(javaVersion)
    options.encoding = "UTF-8"
}

loom.apply {
    runConfigs.all {
        ideConfigGenerated(true)
        runDir = "../../run/$mcVersion"
    }
    runs.named("client") {
        vmArg("-D5zig.debug=true")
        // Offline dev account (no Mojang login): fixed name so offline-mode test servers see the same player.
        programArgs("--username", "5ZRRDEVTEST")
    }
}

// Dev clients start muted (master volume 0); change it in-game if you want sound.
tasks.matching { it.name == "runClient" }.configureEach {
    val options = rootProject.file("run/$mcVersion/options.txt")
    doFirst {
        options.parentFile.mkdirs()
        val lines = if (options.exists()) options.readLines().filterNot { it.startsWith("soundCategory_master:") } else emptyList()
        options.writeText((lines + "soundCategory_master:0.0").joinToString("\n", postfix = "\n"))
    }
}

val buildNumber: String = providers.exec {
    commandLine("git", "rev-list", "--count", "HEAD")
    isIgnoreExitValue = true
}.standardOutput.asText.map { it.trim().ifEmpty { "0" } }.getOrElse("0")

// Minecraft-side keybind names: legacy .lang -> .json (lower-cased file names).
val convertLang by tasks.registering {
    val src = rootProject.file("i18n/resources/assets/the5zigmod/lang")
    val out = layout.buildDirectory.dir("generated/lang")
    inputs.dir(src)
    outputs.dir(out)
    doLast {
        val dir = out.get().dir("assets/the5zigmod/lang").asFile.apply { mkdirs() }
        fun esc(v: String) = v.replace("\\", "\\\\").replace("\"", "\\\"")
        src.listFiles { f -> f.name.endsWith(".lang") }!!.forEach { f ->
            val json = f.readLines(Charsets.UTF_8)
                .filter { it.contains('=') }
                .joinToString(",\n", "{\n", "\n}\n") {
                    "  \"${esc(it.substringBefore('=').trim())}\": \"${esc(it.substringAfter('=').trim())}\""
                }
            dir.resolve(f.name.lowercase().removeSuffix(".lang") + ".json").writeText(json, Charsets.UTF_8)
        }
    }
}

tasks.processResources {
    val props = mapOf(
        "version" to project.version,
        "minecraft" to prop("deps.minecraft_range"),
        "java" to javaVersion,
        "mixin_java" to if (javaVersion >= 21) "JAVA_21" else "JAVA_17",
    )
    inputs.properties(props)
    filesMatching(listOf("fabric.mod.json", "fivezigrebornrevived.mixins.json")) { expand(props) }
    // Core i18n bundles are read with Properties.load (ISO-8859-1): escape non-ASCII.
    filesMatching("lang/**/*.properties") { filter<EscapeUnicode>() }

    exclude("assets/the5zigmod/lang/*.lang")
    from(convertLang)
}

tasks.jar {
    manifest.attributes(
        "5zig-Version" to project.version,
        "5zig-Build" to buildNumber,
        "Minecraft-Version" to mcVersion,
    )
    from(rootProject.file("LICENSE"))
}
