package net.cobbleservertools.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;

public final class RctTrainerBagEditorScreen extends Screen {
   private static final int MAX_ROWS = 10;
   private final Screen parent;
   private final Consumer<ListTag> callback;
   private final List<RctTrainerBagEditorScreen.Entry> entries = new ArrayList<>();
   private final List<EditBox> counts = new ArrayList<>();

   public RctTrainerBagEditorScreen(Screen var1, ListTag var2, Consumer<ListTag> var3) {
      super(Component.literal("RCT Trainer Bag"));
      this.parent = var1;
      this.callback = var3;
      if (var2 != null) {
         for (int var4 = 0; var4 < var2.size() && this.entries.size() < 10; var4++) {
            CompoundTag var5 = var2.getCompound(var4);
            String var6 = var5.getString("ItemId");
            int var7 = var5.getInt("Count");
            if (!var6.isBlank() && var7 > 0) {
               this.entries.add(new RctTrainerBagEditorScreen.Entry(var6, var7));
            }
         }
      }
   }

   protected void init() {
      int var1 = Math.min(590, this.width - 20);
      int var2 = (this.width - var1) / 2;
      int var3 = Math.max(7, (this.height - 390) / 2);
      this.counts.clear();

      for (int var4 = 0; var4 < this.entries.size(); var4++) {
         int var5 = var4;
         RctTrainerBagEditorScreen.Entry var6 = this.entries.get(var4);
         int var7 = var3 + 48 + var4 * 28;
         Button var8 = Button.builder(
               Component.literal(display(var6.itemId)),
               var3x -> {
                  this.syncCounts();
                  this.minecraft
                     .setScreen(
                        new SearchableCatalogScreen(this, NpcCreatorCatalog.Kind.ITEM, "", var6.itemId, var2xx -> this.entries.get(var5).itemId = var2xx)
                     );
               }
            )
            .bounds(var2 + 14, var7, var1 - 150, 20)
            .build();
         this.addRenderableWidget(var8);
         EditBox var9 = new EditBox(this.font, var2 + var1 - 128, var7, 64, 20, Component.literal("Count"));
         var9.setMaxLength(4);
         var9.setValue(Integer.toString(var6.count));
         this.counts.add(var9);
         this.addRenderableWidget(var9);
         this.addRenderableWidget(Button.builder(Component.literal("X"), var2x -> {
            this.syncCounts();
            if (var5 >= 0 && var5 < this.entries.size()) {
               this.entries.remove(var5);
            }

            this.minecraft.setScreen(this);
         }).bounds(var2 + var1 - 56, var7, 42, 20).build());
      }

      int var10 = var3 + 48 + Math.max(1, this.entries.size()) * 28 + 8;
      Button var11 = Button.builder(Component.literal("+ Add battle item"), var1x -> {
         this.syncCounts();
         if (this.entries.size() < 10) {
            int var2x = this.entries.size();
            this.entries.add(new RctTrainerBagEditorScreen.Entry("", 1));
            this.minecraft.setScreen(new SearchableCatalogScreen(this, NpcCreatorCatalog.Kind.ITEM, "", "", var2xx -> {
               if (var2x < this.entries.size()) {
                  this.entries.get(var2x).itemId = var2xx;
               }
            }));
         }
      }).bounds(var2 + 14, var10, 150, 20).build();
      var11.active = this.entries.size() < 10;
      this.addRenderableWidget(var11);
      this.addRenderableWidget(Button.builder(Component.literal("Done"), var1x -> this.done()).bounds(var2 + var1 - 174, var3 + 352, 76, 20).build());
      this.addRenderableWidget(
         Button.builder(Component.literal("Cancel"), var1x -> this.minecraft.setScreen(this.parent)).bounds(var2 + var1 - 90, var3 + 352, 76, 20).build()
      );
   }

   private void syncCounts() {
      for (int var1 = 0; var1 < this.counts.size() && var1 < this.entries.size(); var1++) {
         this.entries.get(var1).count = clamp(parseInt(this.counts.get(var1).getValue(), this.entries.get(var1).count), 1, 999);
      }
   }

   private void done() {
      this.syncCounts();
      ListTag var1 = new ListTag();

      for (RctTrainerBagEditorScreen.Entry var3 : this.entries) {
         if (var3.itemId != null && !var3.itemId.isBlank()) {
            CompoundTag var4 = new CompoundTag();
            var4.putString("ItemId", var3.itemId.trim());
            var4.putInt("Count", clamp(var3.count, 1, 999));
            var1.add(var4);
         }
      }

      this.callback.accept(var1);
      this.minecraft.setScreen(this.parent);
   }

   private static int parseInt(String var0, int var1) {
      try {
         return Integer.parseInt(var0.trim());
      } catch (Exception var3) {
         return var1;
      }
   }

   private static int clamp(int var0, int var1, int var2) {
      return Math.max(var1, Math.min(var2, var0));
   }

   private static String display(String var0) {
      return var0 != null && !var0.isBlank() ? NpcCreatorCatalog.pretty(var0) + "  [" + var0 + "]" : "Select item...";
   }

   public void renderBackground(GuiGraphics var1, int var2, int var3, float var4) {
      var1.fill(0, 0, this.width, this.height, -2013265920);
      int var5 = Math.min(590, this.width - 20);
      int var6 = (this.width - var5) / 2;
      int var7 = Math.max(7, (this.height - 390) / 2);
      var1.fill(var6, var7, var6 + var5, var7 + 382, -233103319);
      var1.fill(var6, var7, var6 + var5, var7 + 2, -1653151);
   }

   public void render(GuiGraphics var1, int var2, int var3, float var4) {
      super.render(var1, var2, var3, var4);
      int var5 = Math.min(590, this.width - 20);
      int var6 = (this.width - var5) / 2;
      int var7 = Math.max(7, (this.height - 390) / 2);
      var1.drawCenteredString(this.font, this.title, this.width / 2, var7 + 12, -1653151);
      var1.drawString(this.font, Component.literal("Items here are consumed virtually by RCT AI during each battle."), var6 + 14, var7 + 28, -4669236, false);
      var1.drawString(this.font, Component.literal("Quantity"), var6 + var5 - 128, var7 + 38, -1, false);
   }

   public boolean isPauseScreen() {
      return false;
   }

   private static final class Entry {
      String itemId;
      int count;

      Entry(String var1, int var2) {
         this.itemId = var1 == null ? "" : var1;
         this.count = Math.max(1, var2);
      }
   }
}
