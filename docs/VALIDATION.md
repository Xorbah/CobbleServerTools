# Verification status

## Cobblemon 1.8 migration — 2026-09-06

RC29 compiles directly against the official `Cobblemon-neoforge-1.8.0+1.21.1.jar`. The migration updates the model-widget constructor and dynamic species-feature collection types changed by 1.8. The full Gradle build, artifact identity checks, variant discovery tests, and isolated Pokérus compatibility tests pass. A live Minecraft client/server acceptance test has not been run.

## Roaming workflow update — 2026-09-03

The RC28.2 full Gradle build, identity checks, dynamic form tests, and optional Pokérus contract tests pass. The responsive roaming editor, live area visualization, server-backed preset round-trip, and direct-in-front placement still require an in-game client/server acceptance test.

## Pokérus update — 2026-09-02

The optional integration was checked against the public method signatures and implementation in the supplied `pokerus-neoforge-1.7.3-1.1.0.jar`. Regression tests exercise an absent sidemod with no API classes on the classpath, the matching API contract, disabled requests, rejected infection, API exceptions, and an installed mod with missing API classes. These isolated tests do not replace an in-game test with the sidemod and its required dependencies.

## RC28 reconstruction

Verified on 2026-09-01 using Java 21, Gradle 8.14.3, NeoForge 21.1.235, and Cobblemon 1.7.3.

- Full `gradle build`: PASS.
- Compilation of all 137 Java source files against the actual dependencies: PASS.
- RC27 form/species-feature variant regression test: PASS.
- RC28 dynamic discovery regression test: PASS.
- Source/resource namespace scan and bundled JSON parsing: PASS.
- Packaged JAR namespace/content scan, entrypoint, and license checks: PASS.
- ASM comparison of public/protected members, superclasses, and interfaces for 175 named runtime classes against the supplied RC28 reference, with the requested namespace/name mappings: PASS, zero missing interfaces. Anonymous class numbering and synthetic methods are excluded.

The form tests use isolated compile stubs; they do not replace live game testing. No Minecraft client/server gameplay session was run. NPC editing, spawning, commerce, networking, optional RCT battles, and world persistence still need an in-game acceptance test. The interface comparison checks linkage shape, not complete behavioral equivalence.

The source was reconstructed with Vineflower 1.12.0 and repaired for erased generic types and conflicting decompiler-local variable names. Required notices were preserved. The build still reports inherited deprecation/unchecked warnings; Gradle 9 is not supported by this build configuration.

The original RC28 binary is retained in Git history. The new artifact is compiled from the renamed sources and uses a new mod identity. There is no automatic old-world migration.
