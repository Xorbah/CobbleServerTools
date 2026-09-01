# KantoNPCs NeoForge 1.21.1 — RC23 responsive scroll-list hotfix

RC23 is a focused UI hotfix built directly over RC22.  All RC22 roaming-Pokémon, skin-browser, editor, networking, and optional Size Variation work remains intact.

## Problem fixed
At some Minecraft GUI scales the RC22 skin browser had only ~360 scaled pixels of vertical space.  It still created twelve fixed rows and placed `Search / refresh`, `Use selected`, and `Cancel` at a fixed Y coordinate after those rows.  The action bar could therefore fall partially or fully below the visible GUI.

## RC23 behavior
- The skin list now lives inside a bordered, dedicated viewport rather than consuming the screen vertically.
- The viewport shows at most 8 rows and automatically reduces the visible row count on shorter scaled GUIs.
- Mouse-wheel scrolling is accepted only while the pointer is over the list viewport.
- A visible scrollbar shows position within the filtered skin list.
- `Search / refresh`, `Use selected`, and `Cancel` are pinned to the bottom of the browser panel and remain on-screen.
- The search box remains above the list and the live skin preview remains on the right when enough horizontal room exists.
- On narrow windows the preview collapses before the list is allowed to become unusably narrow.
- Existing skin resource IDs, callback semantics, search behavior, and the RC22 profile-editor integration are unchanged.

## Responsive layout validation
The action buttons' lower edge was checked at representative scaled GUI heights:

- height 180 -> 2 rows, action bottom 164
- height 200 -> 3 rows, action bottom 184
- height 240 -> 5 rows, action bottom 224
- height 300 -> 8 rows, action bottom 284
- height 360 -> 8 rows, action bottom 324
- height 480 -> 8 rows, action bottom 384
- height 720 -> 8 rows, action bottom 504

In every case the action bar remains within the screen height.

## Build notes
`BuildRc23.sh` expects the exact RC22 JAR as its base and only replaces `SkinBrowserScreen.class`, then updates the NeoForge metadata/manifest version to RC23.  The older RC22 cumulative build files remain in this overlay for provenance.

## Validation performed
- `SkinBrowserScreen.java` compiled with Java 21 / classfile major version 65.
- RC23 JAR ZIP integrity passes.
- RC23 NeoForge metadata and manifest report `1.0.5-neoforge-port.6-rc23`.
- Entry-by-entry comparison against RC22 confirms only the intended skin-browser class and version metadata differ.
- No complete Minecraft/NeoForge/Cobblemon runtime is available in this sandbox, so live client launch remains the runtime validation gate.
