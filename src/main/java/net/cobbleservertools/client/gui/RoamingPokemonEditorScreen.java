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
    private boolean spawnInFront;
    private String levelValue="50", sizeValue="1.0", koValue="1800", migrateValue="600", dwellValue="20", cryValue="12";
    private String dimensionValue="minecraft:overworld", xValue="0", zValue="0", radiusValue="128";
    private final String[] ivValues={"31","31","31","31","31","31"}, evValues={"0","0","0","0","0","0"};
    private int regionScroll;
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

        level = box(l + 220, t, 70, "Level", levelValue);

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
            size = box(l + 320, t + 70, 120, "Size scale", sizeValue);
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
                    true, false, 15
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
        int l = width / 2 - 190, t = 38, step = Math.max(22, Math.min(34, (height - 82) / 6));
        String[] s = {"HP", "Atk", "Def", "SpA", "SpD", "Spe"};
        for (int i = 0; i < 6; i++) {
            int y = t + i * step;
            iv[i] = box(l + 70, y, 90, "IV " + s[i], ivValues[i]);
            ev[i] = box(l + 250, y, 90, "EV " + s[i], evValues[i]);
        }
    }

    private void initRoaming() {
        int l = width / 2 - 220, t = 36;
        dimension = box(l, t, 190, "Dimension", dimensionValue);
        rx = box(l + 200, t, 70, "Center X", xValue);
        rz = box(l + 280, t, 70, "Center Z", zValue);
        radius = box(l + 360, t, 80, "Radius", radiusValue);
        addRenderableWidget(Button.builder(Component.literal("Add roaming area"), b -> {
            String r = dimension.getValue() + "," + rx.getValue() + "," + rz.getValue() + "," + radius.getValue();
            if (regions.size() < 16) regions.add(r);
            capture(); regionScroll=Math.max(0,regions.size()-5);
            minecraft.setScreen(this);
        }).bounds(l, t + 30, 150, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Remove last area"), b -> {
            if (!regions.isEmpty()) regions.remove(regions.size() - 1);
            capture(); regionScroll=Math.min(regionScroll,Math.max(0,regions.size()-5));
            minecraft.setScreen(this);
        }).bounds(l + 160, t + 30, 150, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Use my current position"), b -> usePlayerPosition(false))
            .bounds(l + 320, t + 30, 120, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Spawn directly in front: " + yes(spawnInFront)), b -> {
            spawnInFront = !spawnInFront; b.setMessage(Component.literal("Spawn directly in front: " + yes(spawnInFront)));
            if (spawnInFront) usePlayerPosition(true);
        }).bounds(l, t + 58, 218, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Load saved preset"), b -> requestPresets())
            .bounds(l + 228, t + 58, 212, 20).build());
        int timingY = Math.min(t + 190, height - 54);
        koDelay = box(l, timingY, 100, "KO return sec", koValue);
        migrate = box(l + 110, timingY, 100, "Migrate sec", migrateValue);
        dwell = box(l + 220, timingY, 100, "Reveal sec", dwellValue);
        cry = box(l + 330, timingY, 100, "Cry sec", cryValue);
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
        if (level != null) levelValue = level.getValue();
        if (size != null) sizeValue = size.getValue();
        for (int i=0;i<6;i++) { if(iv[i]!=null)ivValues[i]=iv[i].getValue(); if(ev[i]!=null)evValues[i]=ev[i].getValue(); }
        if (dimension != null) dimensionValue=dimension.getValue();
        if (rx != null) xValue=rx.getValue();
        if (rz != null) zValue=rz.getValue();
        if (radius != null) radiusValue=radius.getValue();
        if (koDelay != null) koValue=koDelay.getValue();
        if (migrate != null) migrateValue=migrate.getValue();
        if (dwell != null) dwellValue=dwell.getValue();
        if (cry != null) cryValue=cry.getValue();
    }

    private void usePlayerPosition(boolean inFront) {
        if (minecraft.player == null || minecraft.level == null) return;
        double x=minecraft.player.getX(), z=minecraft.player.getZ();
        if(inFront){double yaw=Math.toRadians(minecraft.player.getYRot());x-=Math.sin(yaw)*3;z+=Math.cos(yaw)*3;}
        dimension.setValue(minecraft.level.dimension().location().toString());
        rx.setValue(String.format(Locale.ROOT,"%.1f",x)); rz.setValue(String.format(Locale.ROOT,"%.1f",z));
        capture();
    }

    private void requestPresets() {
        capture(); CompoundTag request=new CompoundTag(); request.putString("RoamingAction","request_presets");
        PacketDistributor.sendToServer(new UpdateNpcProfilePayload(Integer.MIN_VALUE,request));
    }

    public void loadPreset(CompoundTag t) {
        species=t.getString("RoamingSpecies"); form=t.getString("RoamingForm"); variantAspects=t.getString("RoamingAspects");
        variantProperties=t.getString("RoamingVariantProperties"); nature=t.getString("RoamingNature"); shiny=t.getBoolean("RoamingShiny"); pokerus=t.getBoolean("RoamingPokerus")&&PokerusCompat.isLoaded();
        levelValue=Integer.toString(t.getInt("RoamingLevel")); sizeValue=Float.toString(t.getFloat("RoamingSize"));
        for(int i=0;i<6;i++){ivValues[i]=Integer.toString(t.getInt("RoamingIV"+i));evValues[i]=Integer.toString(t.getInt("RoamingEV"+i));}
        for(int i=0;i<4;i++)moves[i]=t.getString("RoamingMove"+(i+1));
        regions.clear(); for(String value:t.getString("RoamingRegions").split(";"))if(!value.isBlank())regions.add(value);
        koValue=Integer.toString(t.getInt("RoamingKoRespawnSeconds"));migrateValue=Integer.toString(t.getInt("RoamingMigrationSeconds"));
        dwellValue=Integer.toString(t.getInt("RoamingRevealSeconds"));cryValue=Integer.toString(t.getInt("RoamingCrySeconds"));
        regionScroll=0; spawnInFront=false; page=0;
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
        t.putBoolean("RoamingSpawnInFront", spawnInFront);
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

    @Override public boolean mouseScrolled(double x,double y,double sx,double sy){
        if(page==2&&regions.size()>5){int old=regionScroll;if(sy>0)regionScroll=Math.max(0,regionScroll-1);if(sy<0)regionScroll=Math.min(regions.size()-5,regionScroll+1);return old!=regionScroll||super.mouseScrolled(x,y,sx,sy);}return super.mouseScrolled(x,y,sx,sy);
    }

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
            for (int i = regionScroll; i < Math.min(regionScroll+5, regions.size()); i++) {
                String line=(i+1)+". "+regions.get(i);if(line.length()>38)line=line.substring(0,35)+"…";
                g.drawString(font,line,width/2-220,yy+(i-regionScroll)*16,0xB0B0B0);
            }
            if (regions.isEmpty())
                g.drawString(font, "Add at least one area before saving.", width / 2 - 220, yy, 0xFF8080);
            if(regions.size()>5)g.drawString(font,"Scroll areas  "+(regionScroll+1)+"–"+Math.min(regions.size(),regionScroll+5)+" / "+regions.size(),width/2-220,yy+82,0xAAAAAA);
            drawAreaPreview(g);
        }
    }

    private void drawAreaPreview(GuiGraphics g){
        int left=width/2+45,top=96,w=172,h=82;g.fill(left,top,left+w,top+h,0xD0202020);g.renderOutline(left,top,w,h,0xFF777777);
        double cx=parse(rx,xValue),cz=parse(rz,zValue),rad=Math.max(1,parse(radius,radiusValue));
        int centerX=left+w/2,centerY=top+h/2;int rr=(int)Math.min(34,Math.max(4,rad/4));g.renderOutline(centerX-rr,centerY-rr,rr*2,rr*2,0xFF55AAFF);g.fill(centerX-2,centerY-2,centerX+3,centerY+3,0xFFFFFF55);
        if(minecraft.player!=null){double scale=rr/rad;int px=centerX+(int)((minecraft.player.getX()-cx)*scale),pz=centerY+(int)((minecraft.player.getZ()-cz)*scale);if(px>=left&&px<left+w&&pz>=top&&pz<top+h)g.fill(px-2,pz-2,px+3,pz+3,0xFF55FF55);}
        g.drawCenteredString(font,"Live area: X "+Math.round(cx)+" Z "+Math.round(cz)+" r="+Math.round(rad),left+w/2,top+4,0xFFFFFF);
        g.drawString(font,"yellow=center  green=you",left+8,top+h-12,0xAAAAAA);
    }
    private static double parse(EditBox box,String fallback){try{return Double.parseDouble((box==null?fallback:box.getValue()).trim());}catch(Exception e){return 0;}}

    @Override
    public boolean isPauseScreen() { return false; }
}
