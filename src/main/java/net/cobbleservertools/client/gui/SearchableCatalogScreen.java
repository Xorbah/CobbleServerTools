package net.cobbleservertools.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class SearchableCatalogScreen extends Screen {
   private static final int ROWS = 10;
   private final Screen parent;
   private final NpcCreatorCatalog.Kind kind;
   private final String context;
   private final Consumer<String> callback;
   private final String current;
   private EditBox search;
   private final List<Button> rowButtons = new ArrayList<>();
   private List<NpcCreatorCatalog.Entry> results = List.of();
   private List<String> namespaces = List.of("all");
   private String namespace = "all";
   private int page;

   public SearchableCatalogScreen(Screen var1, NpcCreatorCatalog.Kind var2, String var3, String var4, Consumer<String> var5) {
      super(Component.literal("Select " + NpcCreatorCatalog.pretty(var2.name())));
      this.parent = var1;
      this.kind = var2;
      this.context = var3 == null ? "" : var3;
      this.current = var4 == null ? "" : var4;
      this.callback = var5;
   }

   protected void init() {
      int var1 = Math.min(560, this.width - 24);
      int var2 = (this.width - var1) / 2;
      int var3 = Math.max(10, (this.height - 360) / 2);
      this.search = new EditBox(this.font, var2 + 14, var3 + 36, var1 - 174, 20, Component.literal("Search"));
      this.search.setMaxLength(128);
      this.search.setValue("");
      this.search.setResponder(var1x -> {
         this.page = 0;
         this.refresh();
      });
      this.addRenderableWidget(this.search);
      this.namespaces = NpcCreatorCatalog.namespaces(this.kind, this.context);
      this.addRenderableWidget(Button.builder(Component.literal(this.namespaceLabel()), var1x -> {
         int var2x = this.namespaces.indexOf(this.namespace);
         this.namespace = this.namespaces.get((var2x + 1 + this.namespaces.size()) % this.namespaces.size());
         var1x.setMessage(Component.literal(this.namespaceLabel()));
         this.page = 0;
         this.refresh();
      }).bounds(var2 + var1 - 150, var3 + 36, 136, 20).build());
      this.rowButtons.clear();

      for (int var4 = 0; var4 < 10; var4++) {
         int var5 = var4;
         Button var6 = Button.builder(Component.literal(""), var2x -> this.choose(var5)).bounds(var2 + 14, var3 + 68 + var4 * 24, var1 - 28, 20).build();
         this.rowButtons.add(var6);
         this.addRenderableWidget(var6);
      }

      this.addRenderableWidget(Button.builder(Component.literal("< Prev"), var1x -> {
         if (this.page > 0) {
            this.page--;
            this.refresh();
         }
      }).bounds(var2 + 14, var3 + 316, 70, 20).build());
      this.addRenderableWidget(Button.builder(Component.literal("Next >"), var1x -> {
         int var2x = Math.max(0, (this.results.size() - 1) / 10);
         if (this.page < var2x) {
            this.page++;
            this.refresh();
         }
      }).bounds(var2 + 90, var3 + 316, 70, 20).build());
      this.addRenderableWidget(Button.builder(Component.literal("Clear"), var1x -> {
         this.callback.accept("");
         this.minecraft.setScreen(this.parent);
      }).bounds(var2 + var1 - 160, var3 + 316, 70, 20).build());
      this.addRenderableWidget(
         Button.builder(Component.literal("Cancel"), var1x -> this.minecraft.setScreen(this.parent)).bounds(var2 + var1 - 84, var3 + 316, 70, 20).build()
      );
      this.refresh();
   }

   private String namespaceLabel() {
      return "Mod: " + ("all".equals(this.namespace) ? "All" : this.namespace);
   }

   private void refresh() {
      if (this.search != null) {
         this.results = NpcCreatorCatalog.search(this.kind, this.context, this.search.getValue(), this.namespace);
         int var1 = Math.max(0, (this.results.size() - 1) / 10);
         if (this.page > var1) {
            this.page = var1;
         }

         for (int var2 = 0; var2 < this.rowButtons.size(); var2++) {
            int var3 = this.page * 10 + var2;
            Button var4 = this.rowButtons.get(var2);
            if (var3 < this.results.size()) {
               NpcCreatorCatalog.Entry var5 = this.results.get(var3);
               String var6 = var5.id().equals(this.current) ? "✓ " : "";
               var4.setMessage(Component.literal(truncate(var6 + var5.display(), 76)));
               var4.active = true;
               var4.visible = true;
            } else {
               var4.setMessage(Component.literal(""));
               var4.active = false;
               var4.visible = false;
            }
         }
      }
   }

   private void choose(int var1) {
      int var2 = this.page * 10 + var1;
      if (var2 >= 0 && var2 < this.results.size()) {
         this.callback.accept(this.results.get(var2).id());
         this.minecraft.setScreen(this.parent);
      }
   }

   private static String truncate(String var0, int var1) {
      return var0.length() <= var1 ? var0 : var0.substring(0, Math.max(0, var1 - 3)) + "...";
   }

   public void renderBackground(GuiGraphics var1, int var2, int var3, float var4) {
      var1.fill(0, 0, this.width, this.height, -2013265920);
      int var5 = Math.min(560, this.width - 24);
      int var6 = (this.width - var5) / 2;
      int var7 = Math.max(10, (this.height - 360) / 2);
      var1.fill(var6, var7, var6 + var5, var7 + 346, -233103319);
      var1.fill(var6, var7, var6 + var5, var7 + 2, -1653151);
   }

   public void render(GuiGraphics var1, int var2, int var3, float var4) {
      super.render(var1, var2, var3, var4);
      int var5 = Math.min(560, this.width - 24);
      int var6 = (this.width - var5) / 2;
      int var7 = Math.max(10, (this.height - 360) / 2);
      var1.drawCenteredString(this.font, this.title, this.width / 2, var7 + 12, -1653151);
      var1.drawString(
         this.font,
         Component.literal(this.results.size() + " matches • page " + (this.page + 1) + "/" + (Math.max(0, (this.results.size() - 1) / 10) + 1)),
         var6 + 170,
         var7 + 322,
         -4669236,
         false
      );
   }

   public boolean isPauseScreen() {
      return false;
   }

   public boolean mouseScrolled(double var1, double var3, double var5, double var7) {
      return CatalogScrollHooks.mouseScrolled(this, var7);
   }
}
