# CobbleServerTools

Server tools and NPC editors for Minecraft 1.21.1, NeoForge 21.1.235+, and Cobblemon 1.8.0.

- Mod ID and resource namespace: `cobbleservertools`
- Java package: `net.cobbleservertools`
- Version: `1.0.0-rc29`
- Java: 21

Includes battle, rival, dialog, move-tutor, mart, trader, and roaming Pokémon NPCs; skin and preset browsers; and dynamic form/variant discovery.

## Build

Run `./gradlew build` on Linux/macOS or `gradlew.bat build` on Windows. Artifacts are generated in `build/libs/`. Dependencies are downloaded by Gradle; do not copy compile stubs into a runtime installation.

The latest artifact is [dist/CobbleServerTools-1.0.0-rc29.jar](dist/CobbleServerTools-1.0.0-rc29.jar). Install only one version. See [verification status](docs/VALIDATION.md) before installing it.

## Optional Pokérus integration

The roaming Pokémon creation screen shows the Pokérus toggle only when timinc's `pokerus` sidemod is installed on the client. Enabling it calls that sidemod's infection API when the server creates the Pokémon, including its normal strain, duration, marks, and aspects. Install the sidemod and its required dependencies on the server as well. Without the sidemod, the option is hidden and the server does not apply any fake infection flag.

The integration targets the supplied Pokérus API contract. The old `pokerus-neoforge-1.7.3-1.1.0.jar` itself declares Cobblemon 1.7.3 and must not be installed in a Cobblemon 1.8 pack; install a Cobblemon 1.8-compatible Pokérus release when one is available. The sidemod is not bundled with CobbleServerTools.

## Compatibility warning

This version changes the mod ID, Java packages, registry and resource identifiers, network channels, configuration directory, preset metadata, and saved-data keys. It is **not a drop-in update for existing worlds**. Back up your world and configuration first. Test with a new world and matching client/server builds. No automatic migration or legacy aliases are included.

NPC dialogue referring to the Pokémon region Kanto and identifiers belonging to external mods are gameplay content, not this mod's branding, and remain unchanged.

## Source reconstruction

The source baseline was reconstructed from the supplied RC28 runtime with Vineflower, retaining the nine available RC28 overlay sources. This includes runtime-patched initialization, networking, and editor behavior absent from the older source checkpoint. Decompiled source is reconstructed code, not a claim of original authorship. Build and runtime verification status is recorded in `docs/VALIDATION.md`.

Required copyright and third-party notices remain in `LICENSE` and `src/main/resources/META-INF/cobbleservertools-third-party/`. Historical snapshots remain available in Git history; history has not been rewritten.
