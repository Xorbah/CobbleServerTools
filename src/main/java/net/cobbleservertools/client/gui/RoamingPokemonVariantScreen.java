package net.cobbleservertools.client.gui;

import java.util.*;
import java.util.function.Consumer;
import com.cobblemon.mod.common.client.gui.summary.widgets.ModelWidget;
import com.cobblemon.mod.common.pokemon.RenderablePokemon;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Searchable, scrollable form/variant browser for roaming Pokémon. */
public final class RoamingPokemonVariantScreen extends Screen {
    private static final int ROW_STEP = 22;
    private static final int ROW_HEIGHT = 20;
    private static final int MAX_ROWS = 7;

    public static final class Selection {
        private final String form;
        private final String aspects;
        private final String variantProperties;
        public Selection(String form, String aspects, String variantProperties) {
            this.form = form == null ? "" : form;
            this.aspects = aspects == null ? "" : aspects;
            this.variantProperties = variantProperties == null ? "" : variantProperties;
        }
        public String form() { return form; }
        public String aspects() { return aspects; }
        public String variantProperties() { return variantProperties; }
    }

    private final Screen parent;
    private final String species;
    private final boolean shiny;
    private final Consumer<Selection> callback;
    private String selectedForm;
    private String selectedRequiredAspects = "";
    private String selectedVariantProperties = "";
    private String startingCustomAspects;
    private final String startingVariantProperties;

    private EditBox search;
    private EditBox customAspects;
    private final List<Button> rows = new ArrayList<>();
    private List<RoamingPokemonVariantSupport.FormChoice> all = new ArrayList<>();
    private List<RoamingPokemonVariantSupport.FormChoice> filtered = new ArrayList<>();
    private int offset;

    private int panelLeft, panelTop, panelWidth, panelHeight;
    private int listX, listY, listWidth, listHeight, actionY, infoY, visibleRows;
    private int previewX, previewY, previewW, previewH;
    private boolean showPreview;
    private ModelWidget preview;
    private int discoveryTicks;
    private String discoveryFingerprint = "";

    public RoamingPokemonVariantScreen(Screen parent, String species, String currentForm,
                                       String currentAspects, String currentVariantProperties, boolean shiny,
                                       Consumer<Selection> callback) {
        super(Component.literal("Pokemon Variant / Form"));
        this.parent = parent;
        this.species = species == null || species.isBlank() ? "suicune" : species;
        this.selectedForm = currentForm == null ? "" : currentForm;
        this.startingCustomAspects = currentAspects == null ? "" : currentAspects;
        this.startingVariantProperties = currentVariantProperties == null ? "" : currentVariantProperties;
        this.selectedVariantProperties = this.startingVariantProperties;
        this.shiny = shiny;
        this.callback = callback;
    }

    private void layout() {
        panelWidth = Math.min(600, Math.max(250, width - 24));
        panelHeight = Math.min(340, Math.max(210, height - 16));
        panelLeft = (width - panelWidth) / 2;
        panelTop = Math.max(8, (height - panelHeight) / 2);
        showPreview = panelWidth >= 455;
        int reserve = showPreview ? 178 : 20;
        listX = panelLeft + 14;
        listWidth = Math.max(150, panelWidth - reserve - 14);
        listY = panelTop + 82;
        actionY = panelTop + panelHeight - 28;
        infoY = actionY - 13;
        int available = Math.max(ROW_STEP, infoY - listY - 5);
        visibleRows = Math.max(1, Math.min(MAX_ROWS, available / ROW_STEP));
        listHeight = visibleRows * ROW_STEP;
        previewX = panelLeft + panelWidth - 154;
        previewY = panelTop + 54;
        previewW = 138;
        previewH = Math.max(90, actionY - previewY - 8);
    }

    @Override
    protected void init() {
        layout();
        rediscover(true);
        RoamingPokemonVariantSupport.FormChoice initial = currentChoice();
        if (initial != null) {
            selectedForm = initial.formId();
            selectedRequiredAspects = initial.requiredAspects();
            selectedVariantProperties = initial.variantProperty();
        } else {
            selectedRequiredAspects = requiredFor(selectedForm, selectedVariantProperties);
        }

        search = new EditBox(font, listX, panelTop + 28, listWidth, 20, Component.literal("Search forms"));
        search.setMaxLength(64);
        search.setHint(Component.literal("Search available forms / variants..."));
        addRenderableWidget(search);

        customAspects = new EditBox(font, listX, panelTop + 54, listWidth, 20, Component.literal("Custom aspects"));
        customAspects.setMaxLength(256);
        customAspects.setHint(Component.literal("Optional custom aspects: radiant-a, pink, etc."));
        // RC25 stored formal required aspects and custom aspects together.  Strip
        // the selected form's requirements back out so switching to Default does
        // not accidentally keep the old form active (e.g. Alolan -> Default).
        customAspects.setValue(RoamingPokemonVariantSupport.customOnly(startingCustomAspects, selectedRequiredAspects));
        addRenderableWidget(customAspects);

        rows.clear();
        for (int i = 0; i < visibleRows; i++) {
            final int row = i;
            Button b = Button.builder(Component.literal(""), x -> choose(row))
                .bounds(listX, listY + i * ROW_STEP, listWidth, ROW_HEIGHT).build();
            rows.add(b);
            addRenderableWidget(b);
        }

        int gap = 6;
        int bw = Math.max(70, (listWidth - gap * 2) / 3);
        int x1 = listX, x2 = x1 + bw + gap, x3 = x2 + bw + gap;
        int w3 = Math.max(60, listX + listWidth - x3);
        addRenderableWidget(Button.builder(Component.literal("Refresh detected"), b -> { offset = 0; rediscover(true); })
            .bounds(x1, actionY, bw, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Use selected"), b -> use())
            .bounds(x2, actionY, bw, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Cancel"), b -> minecraft.setScreen(parent))
            .bounds(x3, actionY, w3, 20).build());

        createPreview();
        refresh();
    }

    /** Re-read Cobblemon's live synchronized form/feature state. */
    private boolean rediscover(boolean force) {
        List<RoamingPokemonVariantSupport.FormChoice> discovered = RoamingPokemonVariantSupport.listForms(species);
        String fingerprint = RoamingPokemonVariantSupport.catalogFingerprint(discovered);
        if (!force && fingerprint.equals(discoveryFingerprint)) return false;

        String oldRequired = selectedRequiredAspects;
        all = discovered;
        discoveryFingerprint = fingerprint;

        RoamingPokemonVariantSupport.FormChoice selected = currentChoice();
        if (selected != null) {
            selectedForm = selected.formId();
            selectedRequiredAspects = selected.requiredAspects();
            selectedVariantProperties = selected.variantProperty();
        } else if ((!selectedForm.isBlank() || !selectedVariantProperties.isBlank()) && !all.isEmpty()) {
            selectedForm = "";
            selectedVariantProperties = "";
            selectedRequiredAspects = "";
            if (customAspects != null && oldRequired != null && !oldRequired.isBlank()) {
                customAspects.setValue(RoamingPokemonVariantSupport.customOnly(customAspects.getValue(), oldRequired));
            }
        } else {
            selectedRequiredAspects = requiredFor(selectedForm, selectedVariantProperties);
        }

        if (search != null) refresh();
        refreshPreview();
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (++discoveryTicks >= 20) {
            discoveryTicks = 0;
            rediscover(false);
        }
    }

    private void refresh() {
        String q = search == null ? "" : search.getValue().trim().toLowerCase(Locale.ROOT);
        filtered = new ArrayList<>();
        for (RoamingPokemonVariantSupport.FormChoice c : all) {
            String hay = (c.label() + " " + c.formId() + " " + c.requiredAspects() + " " + c.variantProperty() + " " + c.kind()).toLowerCase(Locale.ROOT);
            if (q.isBlank() || hay.contains(q)) filtered.add(c);
        }
        int max = Math.max(0, filtered.size() - visibleRows);
        offset = Math.max(0, Math.min(offset, max));
        for (int i = 0; i < rows.size(); i++) {
            int at = offset + i;
            Button b = rows.get(i);
            boolean valid = at < filtered.size();
            b.active = valid; b.visible = valid;
            if (valid) {
                RoamingPokemonVariantSupport.FormChoice c = filtered.get(at);
                String suffix = "  [" + c.kind() + "]";
                boolean selected = sameChoice(c, selectedForm, selectedVariantProperties);
                b.setMessage(Component.literal((selected ? "> " : "") + c.label() + suffix));
            } else b.setMessage(Component.literal(""));
        }
    }

    private void choose(int row) {
        int at = offset + row;
        if (at < 0 || at >= filtered.size()) return;
        RoamingPokemonVariantSupport.FormChoice c = filtered.get(at);
        selectedForm = c.formId();
        selectedRequiredAspects = c.requiredAspects();
        selectedVariantProperties = c.variantProperty();
        refresh();
        refreshPreview();
    }

    private void use() {
        String custom = customAspects == null ? "" : customAspects.getValue();
        String combined = RoamingPokemonVariantSupport.mergeAspects(selectedRequiredAspects, custom);
        callback.accept(new Selection(selectedForm, combined, selectedVariantProperties));
        minecraft.setScreen(parent);
    }

    private void createPreview() {
        preview = null;
        if (!showPreview) return;
        try {
            preview = new ModelWidget(previewX + 2, previewY + 20, previewW - 4, Math.max(60, previewH - 24),
                buildPreview(), 2.0F, 325F, -10.0D, true, false, 15);
            addRenderableWidget(preview);
        } catch (Throwable ignored) { preview = null; }
    }

    private RenderablePokemon buildPreview() {
        String custom = customAspects == null
            ? RoamingPokemonVariantSupport.customOnly(startingCustomAspects, selectedRequiredAspects)
            : customAspects.getValue();
        String aspects = RoamingPokemonVariantSupport.mergeAspects(selectedRequiredAspects, custom);
        RenderablePokemon pokemon = RoamingPokemonVariantSupport.buildRenderable(species, selectedForm, aspects, shiny);
        if (pokemon == null) throw new IllegalStateException("Unable to resolve Cobblemon renderable for " + species);
        return pokemon;
    }

    private void refreshPreview() {
        if (preview == null) return;
        try { preview.setPokemon(buildPreview()); } catch (Throwable ignored) {}
    }

    private String requiredFor(String form, String properties) {
        for (RoamingPokemonVariantSupport.FormChoice c : all)
            if (sameChoice(c, form, properties)) return c.requiredAspects();
        for (RoamingPokemonVariantSupport.FormChoice c : all)
            if (same(c.formId(), form) && (properties == null || properties.isBlank())) return c.requiredAspects();
        return "";
    }

    private RoamingPokemonVariantSupport.FormChoice currentChoice() {
        for (RoamingPokemonVariantSupport.FormChoice c : all)
            if (sameChoice(c, selectedForm, selectedVariantProperties)) return c;
        return null;
    }

    private static boolean sameChoice(RoamingPokemonVariantSupport.FormChoice c, String form, String properties) {
        String p1 = c.variantProperty() == null ? "" : c.variantProperty().trim().toLowerCase(Locale.ROOT);
        String p2 = properties == null ? "" : properties.trim().toLowerCase(Locale.ROOT);
        return same(c.formId(), form) && Objects.equals(p1, p2);
    }

    private static boolean same(String a, String b) {
        return Objects.equals(a == null ? "" : a.toLowerCase(Locale.ROOT), b == null ? "" : b.toLowerCase(Locale.ROOT));
    }

    @Override
    public boolean mouseScrolled(double x, double y, double sx, double sy) {
        if (x < listX - 4 || x > listX + listWidth + 10 || y < listY - 4 || y > listY + listHeight + 4) return false;
        if (sy > 0) offset = Math.max(0, offset - 1);
        else if (sy < 0) offset = Math.min(Math.max(0, filtered.size() - visibleRows), offset + 1);
        else return false;
        refresh(); return true;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mx, int my, float p) { g.fill(0, 0, width, height, 0xC0101010); }

    @Override
    public void render(GuiGraphics g, int mx, int my, float p) {
        layout();
        g.fill(panelLeft, panelTop, panelLeft + panelWidth, panelTop + panelHeight, 0xE0181818);
        int bx1=listX-4, by1=listY-4, bx2=listX+listWidth+10, by2=listY+listHeight+2;
        g.fill(bx1,by1,bx2,by2,0xB0080808);
        if (showPreview) {
            g.fill(previewX-2, previewY-2, previewX+previewW+2, previewY+previewH+2, 0xFF777777);
            g.fill(previewX, previewY, previewX+previewW, previewY+previewH, 0xD0101010);
        }
        super.render(g,mx,my,p);
        g.drawCenteredString(font, "Variant / Form: " + species, width/2, panelTop+8, 0xFFFFFF);
        g.drawString(font, "Live forms/variants from Cobblemon + loaded datapacks/mods", listX, infoY, 0xA0A0A0);
        String range=filtered.isEmpty()?"0 / 0":(offset+1)+"-"+Math.min(filtered.size(),offset+visibleRows)+" / "+filtered.size();
        g.drawString(font, range, Math.max(listX,listX+listWidth-72), infoY, 0xA0A0A0);
        drawScrollbar(g);
        if (showPreview) {
            g.drawCenteredString(font, "Live Preview", previewX+previewW/2, previewY+5, 0xFFFFFF);
            if (preview==null) g.drawCenteredString(font, "Preview unavailable", previewX+previewW/2, previewY+previewH/2, 0xFF8080);
        }
    }

    private void drawScrollbar(GuiGraphics g) {
        int tx=listX+listWidth+3, ty=listY, th=Math.max(12,listHeight-2);
        g.fill(tx,ty,tx+4,ty+th,0xFF242424);
        if(filtered.size()<=visibleRows||filtered.isEmpty()){g.fill(tx,ty,tx+4,ty+th,0xFF707070);return;}
        int thumbH=Math.max(14,(int)((long)th*visibleRows/filtered.size()));
        int travel=Math.max(0,th-thumbH), maxOffset=Math.max(1,filtered.size()-visibleRows);
        int thumbY=ty+(int)((long)travel*offset/maxOffset);
        g.fill(tx,thumbY,tx+4,thumbY+thumbH,0xFFAAAAAA);
    }

    @Override public boolean isPauseScreen(){return false;}
}
