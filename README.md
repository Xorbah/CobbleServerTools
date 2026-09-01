# CobbleServerTools

CobbleServerTools is a NeoForge 1.21.1 server/NPC toolkit for Cobblemon, evolved from the KantoNPCs NeoForge recreation work.

> **Current status:** active reconstruction / release-candidate development. The latest preserved build in this repository is **RC28**.

## Compatibility

- Minecraft Java Edition **1.21.1**
- **NeoForge** 21.1.x
- Cobblemon **1.7.3**
- Java **21**

The repository/project name is now **CobbleServerTools**. For compatibility with existing worlds, NBT, resource locations, configs, and packets, the internal mod ID/package namespace is intentionally still `kantonpcs` / `net.crulim.kantonpcs` for now. A future migration can change the internal ID with explicit data-fix and compatibility handling rather than silently breaking existing saves.

## Current feature set

The current reconstruction includes the recovered KantoNPCs NPC/editor systems plus the newer CobbleServerTools work, including:

- Manual NPC editing and advanced editor recovery.
- Scrollable catalog/list handling.
- Scrollable NPC skin browser with live preview.
- Battle/Rival NPC editing work preserved from the NeoForge reconstruction.
- **Roaming Pokémon NPCs** inspired by Generation II roaming encounters.
  - Configurable species, level, nature, moves, IVs, EVs, shiny state, Pokérus state, and optional size scaling.
  - Configurable roaming regions, migration behavior, cry/reveal timing, and KO return delay.
  - Capture retires a roamer; KO allows it to return after its configured cooldown.
  - Live animated Cobblemon model preview in the editor.
  - Formal alternate-form selection.
  - Datapack/mod-added form discovery from Cobblemon's synchronized species state.
  - Aspect/species-feature variant discovery for packs that use resolver aspects instead of only formal `FormData` entries.
  - Runtime refresh of detected forms/variants so datapack reloads do not require hard-coded compatibility updates.
- Optional Cobblemon Size Variation compatibility.

## Dynamic forms and variants

RC27/RC28 deliberately distinguish **forms** from **variants**:

- **Forms** are Cobblemon `FormData` entries (for example Alolan Vulpix).
- **Variants** may instead be species-feature/aspect choices that select alternate models or textures through Cobblemon model resolvers (for example the supplied Fat Pikachu datapack pattern).

The roaming editor queries Cobblemon's live synchronized species/form/feature data rather than maintaining addon-specific hard-coded lists. Compatible datapacks and mods that register their additions through Cobblemon's normal species/form/feature APIs can therefore appear dynamically.

## Repository layout

- `src/main/java/` — latest preserved Java source overlay for the RC28 work.
- `tools/tests/` — Java regression/contract tests used while reconstructing form/variant support.
- `scripts/legacy-patch-build/` — RC25–RC28 bytecode-overlay build scripts used during reconstruction.
- `docs/history/` — milestone notes for RC22 through RC28.
- `docs/audits/` — validation/audit notes from the variant work.
- `dist/` — latest compiled test build.

## Important source-state note

The recent RC builds were produced as **source/bytecode overlays on top of the last recovered full KantoNPCs JAR**, because the earlier complete native NeoForge workspace was not preserved as one standalone Gradle tree. This repository therefore preserves the latest source work and build history without pretending the current tree is already a clean one-command Gradle build.

The next repository milestone should be to fold the recovered base implementation and these overlays into a normal standalone NeoForge project, then remove the legacy patch-build scripts.

## Latest build

`dist/CobbleServerTools-1.21.1-rc28.jar`

This is the same functional RC28 build previously produced under the KantoNPCs filename; only the repository/distribution filename here uses the new CobbleServerTools project name. The internal mod ID remains `kantonpcs` for compatibility.

