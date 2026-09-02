package net.cobbleservertools.client.gui;

import java.util.ArrayList;
import java.util.List;
import net.cobbleservertools.network.payload.UpdateNpcProfilePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;

public final class NpcMartInventoryEditorScreen extends Screen {
   private static final int ROWS = 6;
   private final int entityId;
   private final CompoundTag original;
   private final List<NpcMartInventoryEditorScreen.Row> rows = new ArrayList<>();
   private final EditBox[] buyBoxes = new EditBox[6];
   private final EditBox[] sellBoxes = new EditBox[6];
   private int page;

   public NpcMartInventoryEditorScreen(int var1, CompoundTag var2) {
      super(Component.literal("Mart / Vending Inventory"));
      this.entityId = var1;
      this.original = var2 == null ? new CompoundTag() : var2.copy();
      this.read();
   }

   protected void init() {
      int var1 = Math.min(650, this.width - 18);
      int var2 = (this.width - var1) / 2;
      int var3 = Math.max(8, (this.height - 330) / 2);

      for (int var4 = 0; var4 < 6; var4++) {
         int var5 = this.page * 6 + var4;
         int var6 = var5;
         int var7 = var3 + 62 + var4 * 34;
         String var8 = var5 < this.rows.size() ? this.rows.get(var5).item : "";
         Button var9 = Button.builder(
               Component.literal(var5 < this.rows.size() ? NpcCreatorCatalog.pretty(var8) + "  [" + var8 + "]" : "[ Empty ]"), var2x -> {
                  this.capture();
                  this.ensure(var6);
                  NpcMartInventoryEditorScreen.Row var3x = this.rows.get(var6);
                  this.minecraft.setScreen(new SearchableCatalogScreen(this, NpcCreatorCatalog.Kind.ITEM, "", var3x.item, var2xx -> {
                     var3x.item = var2xx;
                     var2x.setMessage(Component.literal(var2xx.isBlank() ? "[ Empty ]" : NpcCreatorCatalog.pretty(var2xx) + "  [" + var2xx + "]"));
                  }));
               }
            )
            .bounds(var2 + 20, var7, var1 - 250, 20)
            .build();
         this.addRenderableWidget(var9);
         this.buyBoxes[var4] = this.edit(var2 + var1 - 220, var7, 68, Integer.toString(var5 < this.rows.size() ? this.rows.get(var5).buy : 0), 8);
         this.sellBoxes[var4] = this.edit(var2 + var1 - 144, var7, 68, Integer.toString(var5 < this.rows.size() ? this.rows.get(var5).sell : 0), 8);
         this.addRenderableWidget(Button.builder(Component.literal("X"), var2x -> {
            this.capture();
            if (var6 < this.rows.size()) {
               this.rows.remove(var6);
            }

            this.minecraft.setScreen(new NpcMartInventoryEditorScreen(this.entityId, this.toSnapshot()));
         }).bounds(var2 + var1 - 66, var7, 36, 20).build());
      }

      this.addRenderableWidget(Button.builder(Component.literal("< Prev"), var1x -> {
         this.capture();
         if (this.page > 0) {
            this.page--;
            this.reopen();
         }
      }).bounds(var2 + 20, var3 + 274, 72, 20).build());
      this.addRenderableWidget(Button.builder(Component.literal("Add Item"), var1x -> {
         this.capture();
         this.rows.add(new NpcMartInventoryEditorScreen.Row());
         this.page = (this.rows.size() - 1) / 6;
         this.reopen();
      }).bounds(var2 + 100, var3 + 274, 76, 20).build());
      this.addRenderableWidget(Button.builder(Component.literal("Next >"), var1x -> {
         this.capture();
         if ((this.page + 1) * 6 < Math.max(this.rows.size(), 7)) {
            this.page++;
            this.reopen();
         }
      }).bounds(var2 + 184, var3 + 274, 72, 20).build());
      this.addRenderableWidget(Button.builder(Component.literal("Save Inventory"), var1x -> this.save()).bounds(var2 + var1 - 230, var3 + 274, 104, 20).build());
      this.addRenderableWidget(
         Button.builder(Component.literal("Cancel"), var1x -> this.minecraft.setScreen(null)).bounds(var2 + var1 - 116, var3 + 274, 86, 20).build()
      );
   }

   private EditBox edit(int var1, int var2, int var3, String var4, int var5) {
      EditBox var6 = new EditBox(this.font, var1, var2, var3, 20, Component.literal("price"));
      var6.setMaxLength(var5);
      var6.setValue(var4);
      var6.setFilter(var0 -> var0.matches("\\d{0,8}"));
      this.addRenderableWidget(var6);
      return var6;
   }

   private void capture() {
      for (int var1 = 0; var1 < 6; var1++) {
         int var2 = this.page * 6 + var1;
         if (var2 < this.rows.size()) {
            this.rows.get(var2).buy = Math.max(0, this.num(this.buyBoxes[var1].getValue()));
            this.rows.get(var2).sell = Math.max(0, this.num(this.sellBoxes[var1].getValue()));
         }
      }
   }

   private void ensure(int var1) {
      while (this.rows.size() <= var1) {
         this.rows.add(new NpcMartInventoryEditorScreen.Row());
      }
   }

   private int num(String var1) {
      try {
         return Integer.parseInt(var1);
      } catch (Exception var3) {
         return 0;
      }
   }

   private void reopen() {
      this.minecraft.setScreen(this);
   }

   private CompoundTag toSnapshot() {
      CompoundTag var1 = this.original.copy();
      this.writeInto(var1);
      return var1;
   }

   private void save() {
      this.capture();
      CompoundTag var1 = new CompoundTag();
      this.writeInto(var1);
      PacketDistributor.sendToServer(new UpdateNpcProfilePayload(this.entityId, var1), new CustomPacketPayload[0]);
      this.minecraft.setScreen(null);
   }

   private void writeInto(CompoundTag var1) {
      ListTag var2 = new ListTag();
      ListTag var3 = new ListTag();
      ListTag var4 = new ListTag();

      for (NpcMartInventoryEditorScreen.Row var6 : this.rows) {
         if (var6.item != null && !var6.item.isBlank()) {
            CompoundTag var7 = new CompoundTag();
            var7.putString("ItemId", var6.item);
            var7.putString("Item", var6.item);
            var7.putInt("Price", var6.buy);
            var7.putString("Desc", var6.desc);
            var7.putString("CostItem", "");
            var7.putInt("CostCount", 0);
            var2.add(var7);
            CompoundTag var8 = new CompoundTag();
            var8.putString("ItemId", var6.item);
            var8.putString("Item", var6.item);
            var8.putInt("Price", var6.sell);
            var3.add(var8);
            CompoundTag var9 = new CompoundTag();
            var9.putString("ItemId", var6.item);
            var9.putString("Item", var6.item);
            var9.putString("Desc", var6.desc);
            var4.add(var9);
         }
      }

      var1.put("ShopItems", var2);
      var1.put("SellPrices", var3);
      var1.put("SellDescs", var4);
   }

   private void read() {
      ListTag var1 = this.original.getList("ShopItems", 10);
      ListTag var2 = this.original.getList("SellPrices", 10);

      for (int var3 = 0; var3 < var1.size(); var3++) {
         CompoundTag var4 = var1.getCompound(var3);
         NpcMartInventoryEditorScreen.Row var5 = new NpcMartInventoryEditorScreen.Row();
         var5.item = first(var4.getString("ItemId"), var4.getString("Item"));
         var5.buy = var4.getInt("Price");
         var5.desc = var4.getString("Desc");

         for (int var6 = 0; var6 < var2.size(); var6++) {
            CompoundTag var7 = var2.getCompound(var6);
            if (var5.item.equals(first(var7.getString("ItemId"), var7.getString("Item")))) {
               var5.sell = var7.getInt("Price");
               break;
            }
         }

         this.rows.add(var5);
      }
   }

   private static String first(String var0, String var1) {
      return var0 != null && !var0.isBlank() ? var0 : (var1 == null ? "" : var1);
   }

   public void renderBackground(GuiGraphics var1, int var2, int var3, float var4) {
      var1.fill(0, 0, this.width, this.height, -2013265920);
      int var5 = Math.min(650, this.width - 18);
      int var6 = (this.width - var5) / 2;
      int var7 = Math.max(8, (this.height - 330) / 2);
      var1.fill(var6, var7, var6 + var5, var7 + 306, -233103319);
      var1.fill(var6, var7, var6 + var5, var7 + 2, -1653151);
   }

   public void render(GuiGraphics var1, int var2, int var3, float var4) {
      super.render(var1, var2, var3, var4);
      int var5 = Math.min(650, this.width - 18);
      int var6 = (this.width - var5) / 2;
      int var7 = Math.max(8, (this.height - 330) / 2);
      var1.drawCenteredString(this.font, this.title, this.width / 2, var7 + 10, -1653151);
      var1.drawString(this.font, Component.literal("Item (searches every registered mod item)"), var6 + 20, var7 + 40, -1, false);
      var1.drawString(this.font, Component.literal("Buy"), var6 + var5 - 216, var7 + 40, -1, false);
      var1.drawString(this.font, Component.literal("Sell"), var6 + var5 - 140, var7 + 40, -1, false);
      var1.drawString(
         this.font, Component.literal("Page " + (this.page + 1) + " • " + this.rows.size() + " configured entries"), var6 + 270, var7 + 280, -4669236, false
      );
   }

   public boolean isPauseScreen() {
      return false;
   }

   private static final class Row {
      String item = "";
      String desc = "";
      int buy;
      int sell;

      NpcMartInventoryEditorScreen.Row copy() {
         NpcMartInventoryEditorScreen.Row var1 = new NpcMartInventoryEditorScreen.Row();
         var1.item = this.item;
         var1.desc = this.desc;
         var1.buy = this.buy;
         var1.sell = this.sell;
         return var1;
      }
   }
}
