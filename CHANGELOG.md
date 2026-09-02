# Changelog

## 1.0.0-rc28 — CobbleServerTools identity

- Reconstructed the complete RC28 runtime source baseline, including preset management, permissions, networking, and optional battle-AI integration.
- Changed display name to CobbleServerTools, mod ID to `cobbleservertools`, and Java package to `net.cobbleservertools`.
- Renamed mod-owned resources, registry IDs, packets, commands, configuration/storage paths, NBT keys, and UI labels.
- Added a complete Gradle build, wrapper, form-discovery regression tests, and source/JAR identity checks.
- Preserved required copyright and third-party notices.

Breaking change: old worlds/configurations are not migrated automatically. Back up your data and test with a new world and matching client/server builds.
