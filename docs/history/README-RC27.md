# KantoNPCs NeoForge 1.21.1 — RC27 form + datapack variant discovery

RC27 expands the roaming Pokemon Variant/Form browser beyond Cobblemon FormData.

## Why

A Cobblemon visual variant does not have to be represented only by a formal form. Datapacks can define synchronized species features, assign those features to a species, mark them as aspects, and use the resulting aspect in a Pokemon model resolver. The attached `Fat Pika and Friends` pack is an example: it defines the `fat` choice feature for Pikachu and maps choices such as `fat`, `balloon`, `bug`, `surfing`, `gojo`, etc. to resolver aspects.

## Changes

- Reads formal forms from `Species#getForms()` as before.
- Also reads the live synchronized `SpeciesFeatures#getFeaturesFor(species)` list.
- Discovers assigned `ChoiceSpeciesFeatureProvider` values where `isAspect` is true.
- Converts each feature choice to its real formatted aspect using `getAllAspects()`.
- Persists the backing Pokemon property (for example `fat=fat`) as `RoamingVariantProperties` so the actual spawned Pokemon retains the datapack feature, not only the visual aspect.
- Still stores/applies resolver aspects so the client preview and in-world model resolve immediately.
- Merges a feature choice with a formal form when they share the same form id or required aspect, avoiding duplicate rows for packs that define both.
- Keeps aspect-only variants visible even if there is no `FormData` entry at all.
- Preserves RC26 Alolan Vulpix/form support.

## Attached Fat Pikachu pack

The supplied pack contains 15 Pikachu FormData additions and one assigned choice feature named `fat` with 16 choices (`no` plus 15 model variants). Resolver files select alternate models/textures using those aspects. RC27 is designed around this exact pattern while remaining generic for other datapacks.

## Validation

`Rc27VariantSupportTest` verifies:

- Alolan Vulpix formal form discovery remains intact.
- A Fat Pikachu-like feature is discovered and merged with a matching formal form.
- An aspect-only resolver variant with no formal form is still discoverable.
- Variant + shiny aspects coexist in the live preview.
- Variant properties are sanitized before being appended to the server-side PokemonProperties string.
