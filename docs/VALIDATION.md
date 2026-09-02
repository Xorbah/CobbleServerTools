# Verification status

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
