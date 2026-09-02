CobbleServerTools preset library
========================

Place reusable JSON configurations under:
  config/cobbleservertools/presets/<type>/

Supported type folders / npcType values:
  battle, rival, dialog, trader, mart, move_tutor, vending

Preset schema:
{
  "id": "my_pack:unique_id",
  "displayName": "Human-readable name",
  "npcType": "battle",
  "metadata": {
    "region": "kanto",
    "location": "route_1",
    "role": "trainer",
    "trainerClass": "youngster",
    "tags": ["early_game"]
  },
  "configuration": {
    ...same editable fields used by the CobbleServerTools JSON templates...
  }
}

Search indexes ID, display name, metadata, tags, and primitive values inside configuration.
Preset assignment is copy-on-assign: changing the JSON later does not rewrite placed NPCs.
Use the Reload button in the in-game preset browser to rescan files without restarting.
