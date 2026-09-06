# Changelog

## 1.0.0-rc29

- Migrated the build and runtime dependency contract to Cobblemon 1.8.0 for Minecraft 1.21.1 NeoForge.
- Updated Cobblemon model previews for the new explicit block-light constructor parameter.
- Updated dynamic species-feature discovery for Cobblemon 1.8's covariant choice/aspect collections.
- Aligned the development KotlinForForge dependency with Cobblemon 1.8.0's 5.10.0 toolchain.

## 1.0.0-rc28.2

- Added a live top-down roaming-area editor with current-position capture and a direct-in-front spawn option.
- Generated roaming Pokémon are reusable server-backed presets and can be loaded from a scrollable preset browser.
- Roaming area and preset lists scroll when their contents exceed the GUI; editor layouts stay inside short GUI borders.
- Fixed roaming editor values being lost while changing pages.

## 1.0.0-rc28.1 — optional Pokérus integration

- Hide the roaming Pokémon Pokérus toggle unless the `pokerus` sidemod is installed.
- Use the sidemod's infection API and verify its active-infection state instead of writing a custom NBT flag.
- Ignore disabled requests and absent sidemods safely; log incompatible API or rejected-infection failures.
- Add isolated absent, present, and unavailable-API regression tests.

## 1.0.0-rc28 — CobbleServerTools identity

- Reconstructed the complete RC28 runtime source baseline, including preset management, permissions, networking, and optional battle-AI integration.
- Changed display name to CobbleServerTools, mod ID to `cobbleservertools`, and Java package to `net.cobbleservertools`.
- Renamed mod-owned resources, registry IDs, packets, commands, configuration/storage paths, NBT keys, and UI labels.
- Added a complete Gradle build, wrapper, form-discovery regression tests, and source/JAR identity checks.
- Preserved required copyright and third-party notices.

Breaking change: old worlds/configurations are not migrated automatically. Back up your data and test with a new world and matching client/server builds.
