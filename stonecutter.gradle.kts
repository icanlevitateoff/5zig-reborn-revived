plugins {
    id("dev.kikugie.stonecutter")
    // One artifact, two plugin ids: -remap for obfuscated Minecraft (<= 1.21.11), plain for 26.x.
    id("net.fabricmc.fabric-loom-remap") version "1.19.4" apply false
    id("net.fabricmc.fabric-loom") version "1.19.4" apply false
}

stonecutter active "1.21.8" /* [SC] DO NOT EDIT */

