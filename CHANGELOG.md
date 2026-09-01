# Changelog

## RC28 — Dynamic form discovery

- Variant/Form browser now uses Cobblemon's live synchronized species state.
- Re-discovers formal forms and species-feature/aspect variants when the browser opens.
- Polls for catalog changes while the browser remains open.
- `Refresh Detected` performs a real rediscovery.
- Removed hard-coded addon/species compatibility from the discovery path.
- Safely falls back to Default when a previously selected third-party form disappears after reload.

## RC27 — Forms + datapack/model variants

- Added discovery of `ChoiceSpeciesFeatureProvider` variants in addition to formal forms.
- Persists backing Pokémon properties for feature-driven variants.
- Merges duplicate form/variant entries when they represent the same appearance.
- Added support for aspect-only model variants with no formal `FormData` entry.
- Validated against the Fat Pikachu datapack pattern.

## RC26 — Form browser/preview correction

- Replaced broken reflective form lookup with Cobblemon's typed species/form API.
- Restored live animated preview for alternate forms.
- Corrected Alolan Vulpix discovery and required-aspect handling.
- Separated formal-form aspects from custom cosmetic aspects.

## RC25 — Initial form/variant controls

- Added Variant/Form controls to the Roaming Pokémon editor.
- Added persistent roaming form/aspect fields.
- Added dedicated variant browser and live preview integration.

## RC24 — Roaming Pokémon live model preview

- Added Cobblemon-native animated model preview to the Roaming Pokémon editor.
- Preview updates for species and shiny state.

## RC23 — Responsive scroll-list UI

- Confined skin selection to a fixed-height scroll viewport.
- Pinned confirmation controls to the bottom of the panel.
- Added responsive row counts for smaller GUI scales.

## RC22 — Roaming Pokémon + editor UX milestone

- Added scrollable catalog behavior.
- Added NPC skin browser with live preview.
- Added initial Roaming Pokémon NPC editor and server-side lifecycle.
- Added optional Size Variation compatibility.
