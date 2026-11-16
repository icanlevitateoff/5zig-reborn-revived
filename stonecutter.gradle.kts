plugins {
    id("dev.kikugie.stonecutter")
    // One artifact, two plugin ids: -remap for obfuscated Minecraft (<= 1.21.11), plain for 26.x.
    id("net.fabricmc.fabric-loom-remap") version "1.19.4" apply false
    id("net.fabricmc.fabric-loom") version "1.19.4" apply false
    // Uploads to CurseForge + GitHub: ./gradlew publishMods (see "Publishing" in FABRIC_PORT_NOTES.md).
    id("me.modmuss50.mod-publish-plugin") version "2.2.1"
}

stonecutter active "1.21.8" /* [SC] DO NOT EDIT */


// The root project creates the GitHub release (tag v<mod.version>); every version node then
// uploads its jar to CurseForge and attaches it to that release (publishMods block in build.gradle.kts).
// Tokens: curseforgeToken / githubToken in ~/.gradle/gradle.properties (never in this repo), or the
// CURSEFORGE_TOKEN / GITHUB_TOKEN environment variables. A platform whose token is missing, or every
// platform with -Ppublish.dryRun, only does a dry run: the upload is written to build/publishMods.
val modVersion = property("mod.version").toString()
val releaseType = when (val t = property("publish.type").toString().lowercase()) {
    "release", "stable" -> me.modmuss50.mpp.ReleaseType.STABLE
    "beta" -> me.modmuss50.mpp.ReleaseType.BETA
    "alpha" -> me.modmuss50.mpp.ReleaseType.ALPHA
    else -> throw GradleException("publish.type must be release, beta or alpha, not '$t'")
}
val githubToken = providers.gradleProperty("githubToken").orElse(providers.environmentVariable("GITHUB_TOKEN"))

publishMods {
    version = modVersion
    displayName = "v$modVersion"
    changelog = providers.fileContents(layout.projectDirectory.file("release-notes/v$modVersion.md")).asText
    type = releaseType
    dryRun = hasProperty("publish.dryRun")

    github {
        accessToken = githubToken
        dryRun = !githubToken.isPresent || hasProperty("publish.dryRun")
        repository = "icanlevitateoff/5zig-reborn-revived"
        commitish = "main"
        tagName = "v$modVersion"
        // The jars come from the version nodes (parent = this task).
        allowEmptyFiles = true
    }
}

if (!githubToken.isPresent) logger.warn("5zig: no githubToken, the GitHub release is a dry run only")
if (!providers.gradleProperty("curseforgeToken").orElse(providers.environmentVariable("CURSEFORGE_TOKEN")).isPresent)
    logger.warn("5zig: no curseforgeToken, CurseForge uploads are a dry run only")
