# KantoNPCs NeoForge 1.21.1 — RC28 dynamic form discovery

RC28 makes the Roaming Pokemon Variant/Form browser continuously use Cobblemon's live synchronized species state rather than treating the form catalog as a one-time snapshot.

- Formal forms are rediscovered from the selected species every time the browser opens.
- Datapack/mod species-feature variants are rediscovered from the live `SpeciesFeatures` assignment view.
- The browser polls once per second while open and refreshes only when the discovered catalog fingerprint changes.
- The Refresh Detected button now performs a real rediscovery rather than only refiltering the existing list.
- If a reload removes the currently selected third-party form, the editor safely falls back to Default rather than retaining a dead form/property.
- No pack names, species names, or third-party mod IDs are hard-coded into the runtime discovery path.

This means datapacks using species additions/features and mods that inject forms/features through Cobblemon's synchronized species APIs are picked up without a KantoNPCs update.
