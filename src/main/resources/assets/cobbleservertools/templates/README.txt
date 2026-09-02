CobbleServerTools configuration field templates
=======================================

RC17 no longer auto-applies these files when an NPC is spawned.
New NPCs now spawn as harmless blank shells and open the searchable Preset Library.

These files remain useful as field/schema examples when authoring reusable presets under:
  config/cobbleservertools/presets/<type>/

To create a preset, use the metadata wrapper documented in:
  config/cobbleservertools/presets/README.txt
and place the relevant fields from one of these templates inside "configuration".

Supported preset type folders:
  battle, rival, dialog, trader, mart, move_tutor, vending

The existing config/cobbleservertools/templates/ files are never overwritten, so older server edits
remain available as references even after upgrading.
