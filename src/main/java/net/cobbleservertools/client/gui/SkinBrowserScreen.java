package net.cobbleservertools.client.gui;

import java.util.*;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Responsive skin browser used by the RC23 editor.
 *
 * The list deliberately lives in its own viewport.  Visible row count is
 * derived from the scaled GUI height, and the action bar is always pinned to
 * the bottom of the browser panel so it cannot be pushed off-screen by rows.
 */
public final class SkinBrowserScreen extends Screen {
    private static final int MAX_ROWS = 8;
    private static final int ROW_STEP = 22;
    private static final int ROW_HEIGHT = 20;

    private static final List<String> SKINS = List.of(
        "cobbleservertools:textures/entity/npc/aghata.png",
        "cobbleservertools:textures/entity/npc/alder.png",
        "cobbleservertools:textures/entity/npc/aroma_lady.png",
        "cobbleservertools:textures/entity/npc/attendant.png",
        "cobbleservertools:textures/entity/npc/beauty.png",
        "cobbleservertools:textures/entity/npc/beauty_diamond.png",
        "cobbleservertools:textures/entity/npc/biker.png",
        "cobbleservertools:textures/entity/npc/bill.png",
        "cobbleservertools:textures/entity/npc/bird_keeper.png",
        "cobbleservertools:textures/entity/npc/bird_keeper_hhss.png",
        "cobbleservertools:textures/entity/npc/blaine.png",
        "cobbleservertools:textures/entity/npc/blue.png",
        "cobbleservertools:textures/entity/npc/bro.png",
        "cobbleservertools:textures/entity/npc/brock.png",
        "cobbleservertools:textures/entity/npc/bruno.png",
        "cobbleservertools:textures/entity/npc/bug_catcher.png",
        "cobbleservertools:textures/entity/npc/camper.png",
        "cobbleservertools:textures/entity/npc/channeler.png",
        "cobbleservertools:textures/entity/npc/cientist_1.png",
        "cobbleservertools:textures/entity/npc/cool_trainer_boy.png",
        "cobbleservertools:textures/entity/npc/cool_trainer_girl.png",
        "cobbleservertools:textures/entity/npc/crush_girl.png",
        "cobbleservertools:textures/entity/npc/crush_kin.png",
        "cobbleservertools:textures/entity/npc/cynthia.png",
        "cobbleservertools:textures/entity/npc/dbt.png",
        "cobbleservertools:textures/entity/npc/diantha.png",
        "cobbleservertools:textures/entity/npc/dna_analist.png",
        "cobbleservertools:textures/entity/npc/dna_analist_fe.png",
        "cobbleservertools:textures/entity/npc/dna_analist_fe2.png",
        "cobbleservertools:textures/entity/npc/dna_cipher.png",
        "cobbleservertools:textures/entity/npc/dna_mart.png",
        "cobbleservertools:textures/entity/npc/dna_mysterious.png",
        "cobbleservertools:textures/entity/npc/dna_supervisor.png",
        "cobbleservertools:textures/entity/npc/dna_supervisor_fe.png",
        "cobbleservertools:textures/entity/npc/dna_supervisor_fe2.png",
        "cobbleservertools:textures/entity/npc/eicruz_owner.png",
        "cobbleservertools:textures/entity/npc/engineer.png",
        "cobbleservertools:textures/entity/npc/erika.png",
        "cobbleservertools:textures/entity/npc/fisherman.png",
        "cobbleservertools:textures/entity/npc/gamer.png",
        "cobbleservertools:textures/entity/npc/gardenia.png",
        "cobbleservertools:textures/entity/npc/gentleman.png",
        "cobbleservertools:textures/entity/npc/giovanni.png",
        "cobbleservertools:textures/entity/npc/grandma.png",
        "cobbleservertools:textures/entity/npc/grandpa.png",
        "cobbleservertools:textures/entity/npc/guard.png",
        "cobbleservertools:textures/entity/npc/guard_green.png",
        "cobbleservertools:textures/entity/npc/gym_guide.png",
        "cobbleservertools:textures/entity/npc/hiker.png",
        "cobbleservertools:textures/entity/npc/iris.png",
        "cobbleservertools:textures/entity/npc/jasmine_hhss.png",
        "cobbleservertools:textures/entity/npc/koga.png",
        "cobbleservertools:textures/entity/npc/lance.png",
        "cobbleservertools:textures/entity/npc/lass_diamond.png",
        "cobbleservertools:textures/entity/npc/leon.png",
        "cobbleservertools:textures/entity/npc/lorelei.png",
        "cobbleservertools:textures/entity/npc/lt_surge.png",
        "cobbleservertools:textures/entity/npc/mart.png",
        "cobbleservertools:textures/entity/npc/misty.png",
        "cobbleservertools:textures/entity/npc/mom.png",
        "cobbleservertools:textures/entity/npc/nurse_joy.png",
        "cobbleservertools:textures/entity/npc/opaulim_owner.png",
        "cobbleservertools:textures/entity/npc/painter.png",
        "cobbleservertools:textures/entity/npc/picnicker.png",
        "cobbleservertools:textures/entity/npc/pokemaniac.png",
        "cobbleservertools:textures/entity/npc/prof_oak.png",
        "cobbleservertools:textures/entity/npc/psychic_boy.png",
        "cobbleservertools:textures/entity/npc/psychic_girl.png",
        "cobbleservertools:textures/entity/npc/random_man.png",
        "cobbleservertools:textures/entity/npc/random_woman.png",
        "cobbleservertools:textures/entity/npc/red.png",
        "cobbleservertools:textures/entity/npc/richie_rich.png",
        "cobbleservertools:textures/entity/npc/rocket_boy.png",
        "cobbleservertools:textures/entity/npc/rocket_girl.png",
        "cobbleservertools:textures/entity/npc/sabrina.png",
        "cobbleservertools:textures/entity/npc/sailor.png",
        "cobbleservertools:textures/entity/npc/selphy.png",
        "cobbleservertools:textures/entity/npc/selphy_daisy.png",
        "cobbleservertools:textures/entity/npc/selphy_orange.png",
        "cobbleservertools:textures/entity/npc/sis.png",
        "cobbleservertools:textures/entity/npc/steven.png",
        "cobbleservertools:textures/entity/npc/swimmer.png",
        "cobbleservertools:textures/entity/npc/tamer.png",
        "cobbleservertools:textures/entity/npc/twins.png",
        "cobbleservertools:textures/entity/npc/weird_kid.png",
        "cobbleservertools:textures/entity/npc/yougster.png"
    );

    private final Screen parent;
    private final Consumer<String> callback;
    private String selected;
    private EditBox search;
    private final List<Button> rows = new ArrayList<>();
    private List<String> filtered = new ArrayList<>();
    private int offset;

    // Calculated every init/resize in scaled-GUI coordinates.
    private int panelWidth;
    private int panelHeight;
    private int panelLeft;
    private int panelTop;
    private int listX;
    private int listY;
    private int listWidth;
    private int listHeight;
    private int actionY;
    private int infoY;
    private int visibleRows;
    private boolean showPreview;

    public SkinBrowserScreen(Screen parent, String current, Consumer<String> callback) {
        super(Component.literal("NPC Skin Browser"));
        this.parent = parent;
        this.selected = current == null ? "" : current;
        this.callback = callback;
    }

    private void calculateLayout() {
        panelWidth = Math.min(560, Math.max(220, width - 24));
        panelHeight = Math.min(304, Math.max(1, height - 16));
        panelLeft = (width - panelWidth) / 2;
        panelTop = Math.max(8, (height - panelHeight) / 2);

        // On narrow windows the preview collapses so the list still remains usable.
        showPreview = panelWidth >= 430;
        int previewReserve = showPreview ? 158 : 24;
        listX = panelLeft + 14;
        listWidth = Math.max(120, panelWidth - previewReserve - 14);

        listY = panelTop + 56;
        actionY = panelTop + panelHeight - 28;
        infoY = actionY - 13;

        int available = Math.max(ROW_STEP, infoY - listY - 5);
        visibleRows = Math.max(1, Math.min(MAX_ROWS, available / ROW_STEP));
        listHeight = visibleRows * ROW_STEP;
    }

    @Override
    protected void init() {
        calculateLayout();

        search = new EditBox(font, listX, panelTop + 28, listWidth, 20, Component.literal("Search skins"));
        search.setMaxLength(64);
        search.setHint(Component.literal("Search 86 bundled NPC skins..."));
        addRenderableWidget(search);

        rows.clear();
        for (int i = 0; i < visibleRows; i++) {
            final int idx = i;
            Button b = Button.builder(Component.literal(""), x -> choose(idx))
                .bounds(listX, listY + i * ROW_STEP, listWidth, ROW_HEIGHT)
                .build();
            rows.add(b);
            addRenderableWidget(b);
        }

        int gap = 6;
        int bw = Math.max(54, (listWidth - gap * 2) / 3);
        int b1x = listX;
        int b2x = b1x + bw + gap;
        int b3x = b2x + bw + gap;
        int b3w = Math.max(54, listX + listWidth - b3x);

        addRenderableWidget(Button.builder(Component.literal("Search / refresh"), b -> {
            offset = 0;
            refresh();
        }).bounds(b1x, actionY, bw, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Use selected"), b -> use())
            .bounds(b2x, actionY, bw, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Cancel"), b -> minecraft.setScreen(parent))
            .bounds(b3x, actionY, b3w, 20).build());

        refresh();
    }

    private void refresh() {
        String q = search == null ? "" : search.getValue().trim().toLowerCase(Locale.ROOT);
        filtered = new ArrayList<>();
        for (String s : SKINS) {
            if (q.isEmpty() || friendly(s).toLowerCase(Locale.ROOT).contains(q)) {
                filtered.add(s);
            }
        }

        int max = Math.max(0, filtered.size() - visibleRows);
        offset = Math.max(0, Math.min(offset, max));
        for (int i = 0; i < rows.size(); i++) {
            int at = offset + i;
            Button b = rows.get(i);
            b.setMessage(Component.literal(at < filtered.size()
                ? ((filtered.get(at).equals(selected) ? "> " : "") + friendly(filtered.get(at)))
                : ""));
            b.active = at < filtered.size();
            b.visible = at < filtered.size();
        }
    }

    private void choose(int row) {
        int at = offset + row;
        if (at >= 0 && at < filtered.size()) {
            selected = filtered.get(at);
            refresh();
        }
    }

    private void use() {
        if (selected != null && !selected.isBlank()) {
            callback.accept(selected);
        }
        minecraft.setScreen(parent);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double sx, double sy) {
        // Scrolling belongs to the list viewport only; scrolling over preview or
        // bottom controls no longer changes selection position unexpectedly.
        if (x < listX - 4 || x > listX + listWidth + 10 || y < listY - 4 || y > listY + listHeight + 4) {
            return false;
        }
        if (sy > 0) {
            offset = Math.max(0, offset - 1);
        } else if (sy < 0) {
            offset = Math.min(Math.max(0, filtered.size() - visibleRows), offset + 1);
        } else {
            return false;
        }
        refresh();
        return true;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mx, int my, float p) {
        g.fill(0, 0, width, height, 0xC0101010);
    }

    private void drawPanelAndListBox(GuiGraphics g) {
        // Browser panel.
        g.fill(panelLeft, panelTop, panelLeft + panelWidth, panelTop + panelHeight, 0xE0181818);
        g.fill(panelLeft, panelTop, panelLeft + panelWidth, panelTop + 1, 0xFFB8B8B8);
        g.fill(panelLeft, panelTop + panelHeight - 1, panelLeft + panelWidth, panelTop + panelHeight, 0xFF555555);
        g.fill(panelLeft, panelTop, panelLeft + 1, panelTop + panelHeight, 0xFF777777);
        g.fill(panelLeft + panelWidth - 1, panelTop, panelLeft + panelWidth, panelTop + panelHeight, 0xFF777777);

        // Dedicated list viewport.  Rows can never escape this rectangle because
        // only visibleRows widgets are created inside it.
        int x1 = listX - 4;
        int y1 = listY - 4;
        int x2 = listX + listWidth + 10;
        int y2 = listY + listHeight + 2;
        g.fill(x1, y1, x2, y2, 0xB0080808);
        g.fill(x1, y1, x2, y1 + 1, 0xFF8A8A8A);
        g.fill(x1, y2 - 1, x2, y2, 0xFF444444);
        g.fill(x1, y1, x1 + 1, y2, 0xFF666666);
        g.fill(x2 - 1, y1, x2, y2, 0xFF666666);
    }

    private void drawScrollbar(GuiGraphics g) {
        int trackX = listX + listWidth + 3;
        int trackY = listY;
        int trackH = Math.max(12, listHeight - 2);
        g.fill(trackX, trackY, trackX + 4, trackY + trackH, 0xFF242424);

        if (filtered.size() <= visibleRows || filtered.isEmpty()) {
            g.fill(trackX, trackY, trackX + 4, trackY + trackH, 0xFF707070);
            return;
        }

        int thumbH = Math.max(14, (int)((long) trackH * visibleRows / filtered.size()));
        int travel = Math.max(0, trackH - thumbH);
        int maxOffset = Math.max(1, filtered.size() - visibleRows);
        int thumbY = trackY + (int)((long) travel * offset / maxOffset);
        g.fill(trackX, thumbY, trackX + 4, thumbY + thumbH, 0xFFAAAAAA);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float p) {
        calculateLayout();
        drawPanelAndListBox(g);
        super.render(g, mx, my, p);

        g.drawCenteredString(font, "NPC Skin Browser", width / 2, panelTop + 8, 0xFFFFFF);
        g.drawString(font, "Mouse wheel scrolls inside the list", listX, infoY, 0xA0A0A0);
        String range = filtered.isEmpty()
            ? "0 / 0"
            : (offset + 1) + "-" + Math.min(filtered.size(), offset + visibleRows) + " / " + filtered.size();
        g.drawString(font, range, Math.max(listX, listX + listWidth - 72), infoY, 0xA0A0A0);
        drawScrollbar(g);

        if (showPreview) {
            int previewX = panelLeft + panelWidth - 137;
            int previewY = panelTop + 62;
            drawPreview(g, previewX, previewY, selected);
        }
    }

    private static void drawPreview(GuiGraphics g, int x, int y, String id) {
        ResourceLocation tex = ResourceLocation.tryParse(id);
        if (tex == null) return;
        g.fill(x - 6, y - 18, x + 110, y + 224, 0x80000000);
        // Front-facing composed player preview.
        blit(g, tex, x + 32, y,       48, 48, 8,  8,  8,  8);
        blit(g, tex, x + 32, y + 48,  48, 72, 20, 20, 8, 12);
        blit(g, tex, x + 8,  y + 48,  24, 72, 44, 20, 4, 12);
        blit(g, tex, x + 80, y + 48,  24, 72, 36, 52, 4, 12);
        blit(g, tex, x + 32, y + 120, 24, 72, 4,  20, 4, 12);
        blit(g, tex, x + 56, y + 120, 24, 72, 20, 52, 4, 12);
    }

    private static void blit(GuiGraphics g, ResourceLocation t, int x, int y, int w, int h,
                             int u, int v, int uw, int uh) {
        g.blit(t, x, y, w, h, (float) u, (float) v, uw, uh, 64, 64);
    }

    private static String friendly(String id) {
        int slash = id.lastIndexOf('/');
        int dot = id.lastIndexOf('.');
        String s = id.substring(slash + 1, dot > slash ? dot : id.length()).replace('_', ' ');
        StringBuilder b = new StringBuilder();
        boolean cap = true;
        for (char c : s.toCharArray()) {
            if (cap && Character.isLetter(c)) {
                b.append(Character.toUpperCase(c));
                cap = false;
            } else {
                b.append(c);
            }
            if (c == ' ') cap = true;
        }
        return b.toString();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
