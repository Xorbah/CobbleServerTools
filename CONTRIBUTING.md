# Contributing

CobbleServerTools currently contains a reconstructed source overlay rather than a fully normalized standalone Gradle workspace.

For changes to the current RC28 overlay:

1. Target Java 21 and Minecraft/NeoForge 1.21.1.
2. Preserve the internal `kantonpcs` mod ID unless a migration explicitly includes save/resource compatibility handling.
3. Keep new Cobblemon integration data-driven where possible; do not hard-code addon names for forms or variants that Cobblemon can expose through synchronized species/form/feature APIs.
4. Add or update regression tests under `tools/tests/` for form/variant discovery changes.
5. Do not commit generated `.class` files, stub output directories, IDE state, or Gradle build output.

A future normalization milestone should convert this repository into a standard NeoForge Gradle project and retire the legacy patch-build workflow.
