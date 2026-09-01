# KantoNPCs NeoForge 1.21.1 — RC26 roaming form browser fix

RC26 is a targeted correction on top of RC25.

## Fixed

- Replaced RC25's reflective form discovery with direct Cobblemon 1.7.3 `Species` / `FormData` calls.
- Formal alternate forms now come from the live synchronized species form list, including addon/datapack forms.
- Alolan Vulpix is represented as form id `alola` with required aspect `alolan`, matching Cobblemon 1.7.3 data.
- Replaced the invalid preview property-string `aspect=...` approach with direct `RenderablePokemon#setAspects(...)` updates.
- Preserves base generated aspects such as `shiny` while layering form-required and user-entered cosmetic aspects.
- Separates saved formal-form aspects from true custom aspect overrides when reopening the form picker, so changing back to Default actually clears the old form.
- Removed generic `aspect=` tokens from the roaming server property string; formal `form=` remains and forced aspects are still applied after Pokémon creation.

## Validation

`BuildRc26.sh` compiles with Java 21 and runs `Rc26VariantSupportTest`, which checks:

- Vulpix exposes an `alola` form.
- The form carries `alolan` into the preview aspect set.
- Default Vulpix does not accidentally remain Alolan.
- Shiny + Alolan aspects coexist.
- RC25 combined saved aspects are separated correctly on reopen.

The output JAR is additionally checked with `unzip -t` and bytecode inspection.
