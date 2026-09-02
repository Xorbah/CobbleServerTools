# CobbleServerTools

Server tools and NPC editors for Minecraft 1.21.1, NeoForge 21.1.235+, and Cobblemon 1.7.3.

- Mod ID and resource namespace: `cobbleservertools`
- Java package: `net.cobbleservertools`
- Version: `1.0.0-rc28`
- Java: 21

Includes battle, rival, dialog, move-tutor, mart, trader, and roaming Pokémon NPCs; skin and preset browsers; and dynamic form/variant discovery.

## Build

Run `./gradlew build` on Linux/macOS or `gradlew.bat build` on Windows. Artifacts are generated in `build/libs/`. Dependencies are downloaded by Gradle; do not copy compile stubs into a runtime installation.

The rebuilt artifact is also available at [dist/CobbleServerTools-1.0.0-rc28.jar](dist/CobbleServerTools-1.0.0-rc28.jar). See [verification status](docs/VALIDATION.md) before installing it.

## Compatibility warning

This version changes the mod ID, Java packages, registry and resource identifiers, network channels, configuration directory, preset metadata, and saved-data keys. It is **not a drop-in update for existing worlds**. Back up your world and configuration first. Test with a new world and matching client/server builds. No automatic migration or legacy aliases are included.

NPC dialogue referring to the Pokémon region Kanto and identifiers belonging to external mods are gameplay content, not this mod's branding, and remain unchanged.

## Source reconstruction

The source baseline was reconstructed from the supplied RC28 runtime with Vineflower, retaining the nine available RC28 overlay sources. This includes runtime-patched initialization, networking, and editor behavior absent from the older source checkpoint. Decompiled source is reconstructed code, not a claim of original authorship. Build and runtime verification status is recorded in `docs/VALIDATION.md`.

Required copyright and third-party notices remain in `LICENSE` and `src/main/resources/META-INF/cobbleservertools-third-party/`. Historical snapshots remain available in Git history; history has not been rewritten.
