# KantoNPCs NeoForge 1.21.1 — RC22 editor + roaming Pokémon overlay

RC22 is built directly over the supplied RC21 runtime JAR. The supplied RC21 source ZIP is a targeted bytecode-repair overlay rather than the full reconstructed source workspace, so this checkpoint follows the same overlay model and contains every new/modified RC22 source plus the ASM patcher used to inject the hooks.

## RC22 changes

### Scrollable editor catalog
- `SearchableCatalogScreen` now responds to the mouse wheel.
- Existing ten-row pagination, Prev/Next buttons, search, namespace filtering, and callbacks are preserved.

### NPC skin browser
- The raw skin resource-path EditBox is hidden from normal editing.
- `NpcProfileEditorScreen` receives a Browse Skins control.
- New `SkinBrowserScreen` exposes all 86 bundled KantoNPCs NPC skins in a mouse-wheel-scrollable list.
- The browser renders a composed front-facing preview from the selected 64x64 skin texture.
- The profile screen also renders a small live head preview.
- The chosen value is still written to the existing `NpcSkin` field, so renderer/save compatibility is preserved.

### Roaming Pokémon NPC
- `NpcPresetBrowserScreen` receives a `Create Roaming Pokemon` entry.
- The editor is divided across three pages:
  1. species / level / nature / moves / shiny / Pokérus / optional size scale;
  2. all six IVs and EVs;
  3. roaming regions and timers.
- Multiple roaming regions are supported as dimension + X/Z center + radius.
- Server-global roaming state is persisted per world in `kantonpcs/roaming_pokemon.properties`.
- Migration rotates between configured roaming areas and broadcasts server-wide migration updates.
- Players inside the active area accumulate dwell time; cries are attempted periodically; after the reveal timer the configured Cobblemon appears near the player.
- Spawn construction uses the existing `NpcPokemonFactory`/Cobblemon PokemonProperties path, including nature, explicit IVs/EVs, moves, shiny state, and scale modifier.
- A caught roamer is retired; a missing/defeated roamer enters the configured KO respawn cooldown and later becomes eligible again.

### Cobblemon Size Variations compatibility
- Runtime detection checks for mod id `cobblemonsizevariation`.
- The size field is exposed only while that mod is present.
- The implementation uses Cobblemon's `scale_modifier`, which is the same underlying `Pokemon.scaleModifier` used by Cobblemon Size Variations 1.4.

## Compatibility notes
- The upstream Cobblemon Size Variations 1.4 source is MIT licensed and shows that it sets `Pokemon.setScaleModifier(float)` and syncs size updates. RC22 intentionally does not create a hard dependency on the addon.
- Cobblemon 1.7.3 does not expose an obvious native Pokérus API in the sources inspected for this patch. RC22 first looks for a runtime Pokérus setter; if unavailable it preserves the flag in Pokémon persistent data as `kantonpcs:pokerus`, so it is not silently lost. A future native/side-mod Pokérus bridge can consume that marker.

## Validation performed
- Java 21 compile of all RC22 additions against descriptor-compatible 1.21.1 stubs and the exact supplied RC21 JAR.
- Patched class disassembly confirms hooks in `NpcProfileEditorScreen`, `NpcPresetBrowserScreen`, `SearchableCatalogScreen`, `KantoNpcsGameEvents`, and `KantoNpcsNetworking`.
- ZIP/JAR integrity test passes.
- All added classes are Java classfile major version 65 (Java 21).
- This sandbox does not contain a complete NeoForge + Cobblemon runtime, so an actual client/server launch remains the next validation gate.
