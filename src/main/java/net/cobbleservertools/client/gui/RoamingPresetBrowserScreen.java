package net.cobbleservertools.client.gui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;

/** Scrollable picker for definitions automatically saved after roamer creation. */
public final class RoamingPresetBrowserScreen extends Screen {
    private final RoamingPokemonEditorScreen parent;
    private final List<CompoundTag> presets = new ArrayList<>();
    private int offset;
    private int visibleRows;

    public RoamingPresetBrowserScreen(RoamingPokemonEditorScreen parent, CompoundTag data) {
        super(Component.literal("Roaming Pokémon Presets"));
        this.parent = parent;
        ListTag list = data.getList("Presets", 10);
        for (int i = 0; i < list.size(); i++) presets.add(list.getCompound(i).copy());
    }

    @Override protected void init() {
        visibleRows = Math.max(1, Math.min(10, (height - 82) / 24));
        offset = Math.min(offset, Math.max(0, presets.size() - visibleRows));
        int left = width / 2 - Math.min(210, width / 2 - 12);
        int w = Math.min(420, width - 24);
        for (int row = 0; row < visibleRows && offset + row < presets.size(); row++) {
            CompoundTag preset = presets.get(offset + row);
            String label = preset.getString("PresetName");
            if (label.isBlank()) label = preset.getString("RoamingSpecies");
            final CompoundTag selected = preset;
            addRenderableWidget(Button.builder(Component.literal(label), b -> {
                parent.loadPreset(selected);
                minecraft.setScreen(parent);
            }).bounds(left, 38 + row * 24, w, 20).build());
        }
        addRenderableWidget(Button.builder(Component.literal("Cancel"), b -> minecraft.setScreen(parent))
            .bounds(width / 2 - 50, height - 28, 100, 20).build());
    }

    @Override public boolean mouseScrolled(double x, double y, double sx, double sy) {
        int old = offset;
        if (sy > 0) offset = Math.max(0, offset - 1);
        if (sy < 0) offset = Math.min(Math.max(0, presets.size() - visibleRows), offset + 1);
        if (old != offset) { rebuildWidgets(); return true; }
        return super.mouseScrolled(x, y, sx, sy);
    }

    @Override public void renderBackground(GuiGraphics g, int x, int y, float p) { g.fill(0, 0, width, height, 0xE0101010); }
    @Override public void render(GuiGraphics g, int x, int y, float p) {
        super.render(g, x, y, p);
        g.drawCenteredString(font, title, width / 2, 14, 0xFFFFFF);
        if (presets.isEmpty()) g.drawCenteredString(font, "No roaming presets have been generated yet.", width / 2, 54, 0xAAAAAA);
        else g.drawCenteredString(font, (offset + 1) + "–" + Math.min(presets.size(), offset + visibleRows) + " / " + presets.size(), width / 2, height - 42, 0xAAAAAA);
    }
    @Override public boolean isPauseScreen() { return false; }
}
