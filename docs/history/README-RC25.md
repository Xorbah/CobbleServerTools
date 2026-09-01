# KantoNPCs NeoForge 1.21.1 — RC25 roaming Pokémon variants/forms

RC25 is built directly over RC24.

## Added
- Page 1 of the Roaming Pokémon creator now has a **Variant/Form** selector next to Level.
- The selector opens a dedicated fixed-height, mouse-wheel-scrollable browser.
- Forms are discovered dynamically from the currently selected Cobblemon species, so addon/datapack species synchronized into Cobblemon can expose their own forms.
- The browser includes `Default / Standard` plus every non-standard form exposed by Cobblemon's `Species.forms` list.
- The selected form updates a live 3D Cobblemon `ModelWidget` preview before confirmation.
- An optional **custom aspects** field supports visual/model variants that are aspect-driven instead of being a formal FormData entry.
- Changing the base species resets the previous form/aspect selection to prevent an incompatible form from leaking to another species.

## Persistence / spawning
New roaming profile keys:
- `RoamingForm`
- `RoamingAspects`

Roamer world persistence now stores `form` and `aspects` alongside species, nature, moves, IVs/EVs, etc.

Spawn construction includes `form=<form id>`.  Variant aspects are also merged into the created Pokémon's `forcedAspects` at runtime, which reinforces form-required aspects and supports custom addon/resource-pack variants.

## Compatibility
- Existing RC22/RC23/RC24 roaming definitions remain valid; missing form/aspect fields default to the standard form.
- Existing skin browser and live roaming preview behavior remain intact.
- No hard dependency on a specific list of Pokémon forms is introduced.

## Validation
- Java 21/classfile 65 compilation passed.
- RC25 JAR ZIP integrity passed.
- Metadata updated to `1.0.5-neoforge-port.6-rc25`.
- Bytecode checked for `RoamingForm`, `RoamingAspects`, `PokemonProperties.parse`, variant browser construction, and server-side `setForcedAspects` compatibility path.

A complete NeoForge + Cobblemon client launch remains the final runtime validation step.
