# KantoNPCs NeoForge 1.21.1 — RC24 roaming Pokémon live-preview hotfix

RC24 is built directly over RC23. All RC22 roaming systems and the RC23 responsive skin-browser fix remain intact.

## Requested fix
The Roaming Pokémon creator now contains a live 3D Pokémon preview on page 1.

- Uses Cobblemon 1.7.3's native `ModelWidget`, the same animated model widget used by Cobblemon's Summary screen.
- Builds a client-side `RenderablePokemon` from the currently selected species.
- The preview updates immediately when a different species is selected.
- Toggling Shiny updates the preview immediately and uses Cobblemon's normal aspect/model pipeline.
- The preview occupies the previously unused right-hand column below the size control, rather than expanding the editor footprint.
- The preview panel is suppressed on extremely short scaled GUIs instead of overlapping the bottom navigation controls.
- Clicking the preview triggers Cobblemon's native cry-animation behavior (`playCryOnClick=true`).
- No temporary Pokémon entity is spawned in the world for the preview.

## Runtime safety
Preview construction is guarded. If a custom/datapack species has no currently resolvable client model, the editor remains usable and displays `Preview unavailable` rather than crashing the entire creator.

## Validation
- Java 21 compilation against descriptor-compatible Minecraft/Cobblemon stubs and the exact RC23 base JAR.
- Added class remains Java classfile major version 65.
- RC24 JAR ZIP integrity checked.
- Metadata/manifest version checked for `1.0.5-neoforge-port.6-rc24`.
- Bytecode disassembly checked for references to Cobblemon `PokemonProperties.asRenderablePokemon`, `ModelWidget.<init>`, and `ModelWidget.setPokemon`.

A full NeoForge + Cobblemon client launch is still the final runtime validation gate.
