package net.cobbleservertools.client.gui;

import java.util.*;
import java.util.function.Consumer;
import com.cobblemon.mod.common.client.gui.summary.widgets.ModelWidget;
import com.cobblemon.mod.common.pokemon.RenderablePokemon;
import net.cobbleservertools.network.payload.UpdateNpcProfilePayload;
import net.cobbleservertools.roaming.SizeVariationCompat;
import net.cobbleservertools.roaming.PokerusCompat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Three-page editor for server-authoritative roaming Pokemon definitions.
 * RC27 adds synchronized species-feature/aspect variants alongside formal forms.
 */
public final class RoamingPokemonEditorScreen extends Screen {
    private final Screen parent;
    private int page = 0;
    private String species = "suicune", nature = "hardy", form = "", variantAspects = "", variantProperties = "";
    private final String[] moves = {"", "", "", ""};
    private boolean shiny, pokerus;
    private EditBox level, size, koDelay, migrate, dwell, cry, dimension, rx, rz, radius;
    private final EditBox[] iv = new EditBox[6], ev = new EditBox[6];
    private final List<String> regions = new ArrayList<>();

    // Client-only preview state. ModelWidget is Cobblemon's own animated summary renderer.
    private ModelWidget pokemonPreview;
    private int previewX, previewY, previewW, previewH;

    public RoamingPokemonEditorScreen(Screen parent) {
        super(Component.literal("Create Roaming Pokemon"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        pokemonPreview = null;
        if (page == 0) initPokemon();
        else if (page == 1) initStats();
        else initRoaming();
        nav();
    }

    private void nav() {
        int y = height - 30;
        addRenderableWidget(Button.builder(Component.literal("< Back"), b -> {
            if (page > 0) {
                capture();
                page--;
                minecraft.setScreen(this);
            } else minecraft.setScreen(parent);
        }).bounds(width / 2 - 160, y, 90, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Page " + (page + 1) + " / 3"), b -> {})
            .bounds(width / 2 - 60, y, 120, 20).build());

        addRenderableWidget(Button.builder(Component.literal(page < 2 ? "Next >" : "Save Roamer"), b -> {
            if (page < 2) {
                capture();
                page++;
                minecraft.setScreen(this);
            } else {
                capture();
                save();
            }
        }).bounds(width / 2 + 70, y, 110, 20).build());
    }

    private void initPokemon() {
        int l = width / 2 - 220, t = 36;

        addRenderableWidget(Button.builder(Component.literal("Pokemon: " + species), b ->
            pick(NpcCreatorCatalog.Kind.POKEMON, species, v -> {
                species = v;
                form = "";
                variantAspects = "";
                variantProperties = "";
                refreshPokemonPreview();
            })
        ).bounds(l, t, 210, 20).build());

        level = box(l + 220, t, 70, "Level", "50");

        addRenderableWidget(Button.builder(
            Component.literal("Variant/Form: " + RoamingPokemonVariantSupport.displayName(form, variantAspects, variantProperties)), b -> {
                capture();
                minecraft.setScreen(new RoamingPokemonVariantScreen(
                    this, species, form, variantAspects, variantProperties, shiny, selection -> {
                        form = selection.form();
                        variantAspects = selection.aspects();
                        variantProperties = selection.variantProperties();
                        refreshPokemonPreview();
                    }
                ));
            }).bounds(l + 300, t, 140, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Nature: " + nature), b ->
            pick(NpcCreatorCatalog.Kind.NATURE, nature, v -> nature = v)
        ).bounds(l, t + 30, 210, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Shiny: " + yes(shiny)), b -> {
            shiny = !shiny;
            b.setMessage(Component.literal("Shiny: " + yes(shiny)));
            refreshPokemonPreview();
        }).bounds(l + 220, t + 30, 100, 20).build());

        if (PokerusCompat.isLoaded()) {
            addRenderableWidget(Button.builder(Component.literal("Pokérus: " + yes(pokerus)), b -> {
                pokerus = !pokerus;
                b.setMessage(Component.literal("Pokérus: " + yes(pokerus)));
            }).bounds(l + 330, t + 30, 110, 20).build());
        } else {
            pokerus = false;
        }

        for (int i = 0; i < 4; i++) {
            final int n = i;
            addRenderableWidget(Button.builder(Component.literal("Move " + (i + 1) + ": " + (moves[i].isBlank() ? "auto" : moves[i])), b ->
                pick(NpcCreatorCatalog.Kind.MOVE, moves[n], v -> moves[n] = v)
            ).bounds(l, t + 70 + i * 28, 310, 20).build());
        }

        if (SizeVariationCompat.isLoaded()) {
            size = box(l + 320, t + 70, 120, "Size scale", "1.0");
        } else {
            addRenderableWidget(Button.builder(Component.literal("Size: base game"), b -> {})
                .bounds(l + 320, t + 70, 120, 20).build());
        }

        // Dedicated preview panel in the otherwise unused right-side column beneath size.
        // It is responsive: on very short GUI heights we omit it rather than overlap navigation.
        previewX = l + 320;
        previewY = t + 102;
        previewW = 120;
        int available = (height - 56) - previewY;
        previewH = Math.min(136, available);
        if (previewH >= 72) {
            try {
                RenderablePokemon renderable = buildPreviewPokemon();
                int widgetY = previewY + 16;
                int widgetH = Math.max(48, previewH - 20);
                pokemonPreview = new ModelWidget(
                    previewX + 2, widgetY,
                    previewW - 4, widgetH,
                    renderable,
                    2.15F, 325F, -10.0D,
                    true, false
                );
                addRenderableWidget(pokemonPreview);
            } catch (Throwable ignored) {
                pokemonPreview = null;
            }
        }
    }

    private RenderablePokemon buildPreviewPokemon() {
        RenderablePokemon pokemon = RoamingPokemonVariantSupport.buildRenderable(
            species == null || species.isBlank() ? "suicune" : species,
            form, variantAspects, shiny
        );
        if (pokemon == null) throw new IllegalStateException("Unable to resolve Cobblemon renderable for " + species);
        return pokemon;
    }

    /** Update the existing Cobblemon ModelWidget immediately after species/shiny changes. */
    private void refreshPokemonPreview() {
        if (pokemonPreview == null) return;
        try {
            pokemonPreview.setPokemon(buildPreviewPokemon());
        } catch (Throwable ignored) {
            // Keep the editor usable even if a datapack species/model is temporarily unavailable.
        }
    }

    private void initStats() {
        int l = width / 2 - 190, t = 48;
        String[] s = {"HP", "Atk", "Def", "SpA", "SpD", "Spe"};
        for (int i = 0; i < 6; i++) {
            int y = t + i * 34;
            iv[i] = box(l + 70, y, 90, "IV " + s[i], "31");
            ev[i] = box(l + 250, y, 90, "EV " + s[i], "0");
        }
    }

    private void initRoaming() {
        int l = width / 2 - 220, t = 36;
        dimension = box(l, t, 190, "Dimension", "minecraft:overworld");
        rx = box(l + 200, t, 70, "Center X", "0");
        rz = box(l + 280, t, 70, "Center Z", "0");
        radius = box(l + 360, t, 80, "Radius", "128");
        addRenderableWidget(Button.builder(Component.literal("Add roaming area"), b -> {
            String r = dimension.getValue() + "," + rx.getValue() + "," + rz.getValue() + "," + radius.getValue();
            if (regions.size() < 16) regions.add(r);
            minecraft.setScreen(this);
        }).bounds(l, t + 30, 150, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Remove last area"), b -> {
            if (!regions.isEmpty()) regions.remove(regions.size() - 1);
            minecraft.setScreen(this);
        }).bounds(l + 160, t + 30, 150, 20).build());
        koDelay = box(l, t + 190, 100, "KO return sec", "1800");
        migrate = box(l + 110, t + 190, 100, "Migrate sec", "600");
        dwell = box(l + 220, t + 190, 100, "Reveal sec", "20");
        cry = box(l + 330, t + 190, 100, "Cry sec", "12");
    }

    private EditBox box(int x, int y, int w, String hint, String def) {
        EditBox b = new EditBox(font, x, y, w, 20, Component.literal(hint));
        b.setMaxLength(64);
        b.setHint(Component.literal(hint));
        b.setValue(def);
        addRenderableWidget(b);
        return b;
    }

    private void pick(NpcCreatorCatalog.Kind k, String current, Consumer<String> cb) {
        capture();
        minecraft.setScreen(new SearchableCatalogScreen(this, k, "", current, cb));
    }

    private void capture() {
        // Existing RC22 persistence model retained. The preview itself owns no server state.
    }

    private void save() {
        CompoundTag t = new CompoundTag();
        t.putString("NpcKind", "roaming_pokemon");
        t.putString("RoamingSpecies", species);
        t.putString("RoamingForm", form == null ? "" : form);
        t.putString("RoamingAspects", variantAspects == null ? "" : variantAspects);
        t.putString("RoamingVariantProperties", variantProperties == null ? "" : variantProperties);
        t.putInt("RoamingLevel", ival(level, 50, 1, 100));
        t.putString("RoamingNature", nature);
        for (int i = 0; i < 4; i++) t.putString("RoamingMove" + (i + 1), moves[i]);
        t.putBoolean("RoamingShiny", shiny);
        t.putBoolean("RoamingPokerus", pokerus && PokerusCompat.isLoaded());
        t.putFloat("RoamingSize", fval(size, 1f, .1f, 10f));
        for (int i = 0; i < 6; i++) {
            t.putInt("RoamingIV" + i, ival(iv[i], 31, 0, 31));
            t.putInt("RoamingEV" + i, ival(ev[i], 0, 0, 252));
        }
        t.putString("RoamingRegions", String.join(";", regions));
        t.putInt("RoamingKoRespawnSeconds", ival(koDelay, 1800, 1, 604800));
        t.putInt("RoamingMigrationSeconds", ival(migrate, 600, 30, 86400));
        t.putInt("RoamingRevealSeconds", ival(dwell, 20, 1, 3600));
        t.putInt("RoamingCrySeconds", ival(cry, 12, 2, 600));
        PacketDistributor.sendToServer(new UpdateNpcProfilePayload(Integer.MIN_VALUE, t));
        minecraft.setScreen(parent);
    }

    private static int ival(EditBox b, int d, int lo, int hi) {
        try { return Math.max(lo, Math.min(hi, Integer.parseInt(b.getValue().trim()))); }
        catch (Exception e) { return d; }
    }

    private static float fval(EditBox b, float d, float lo, float hi) {
        try { return Math.max(lo, Math.min(hi, Float.parseFloat(b.getValue().trim()))); }
        catch (Exception e) { return d; }
    }

    private static String yes(boolean b) { return b ? "ON" : "OFF"; }

    @Override
    public void renderBackground(GuiGraphics g, int x, int y, float p) {
        g.fill(0, 0, width, height, 0xC0101010);
    }

    @Override
    public void render(GuiGraphics g, int x, int y, float p) {
        // Draw panel backing before child widgets/models are rendered by Screen.render.
        if (page == 0 && previewH >= 72) {
            g.fill(previewX - 2, previewY - 2, previewX + previewW + 2, previewY + previewH + 2, 0xFF9A9A9A);
            g.fill(previewX, previewY, previewX + previewW, previewY + previewH, 0xD0101010);
            g.drawCenteredString(font, "Live Pokemon Preview", previewX + previewW / 2, previewY + 5, 0xFFFFFF);
        }

        super.render(g, x, y, p);
        g.drawCenteredString(font, "Create Roaming Pokemon", width / 2, 14, 0xFFFFFF);

        if (page == 0 && previewH >= 72 && pokemonPreview == null) {
            g.drawCenteredString(font, "Preview unavailable", previewX + previewW / 2, previewY + previewH / 2, 0xFF8080);
        }

        if (page == 1) {
            g.drawString(font, "IVs", width / 2 - 120, 32, 0xFFFFFF);
            g.drawString(font, "EVs", width / 2 + 60, 32, 0xFFFFFF);
        }
        if (page == 2) {
            int yy = 104;
            g.drawString(font, "Roaming areas (migration rotates between these):", width / 2 - 220, 86, 0xFFFFFF);
            for (int i = 0; i < Math.min(5, regions.size()); i++)
                g.drawString(font, (i + 1) + ". " + regions.get(i), width / 2 - 220, yy + i * 16, 0xB0B0B0);
            if (regions.isEmpty())
                g.drawString(font, "Add at least one area before saving.", width / 2 - 220, yy, 0xFF8080);
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
