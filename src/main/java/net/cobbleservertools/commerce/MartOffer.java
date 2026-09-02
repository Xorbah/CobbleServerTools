package net.cobbleservertools.commerce;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public record MartOffer(String productKey, int price, String description, String costKey, int costCount) {
   public static final int MAX_PRICE = 1000000000;
   public static final int MAX_COUNT = 64000;

   public MartOffer(String productKey, int price, String description, String costKey, int costCount) {
      productKey = bounded(productKey, 512);
      price = Math.max(0, Math.min(1000000000, price));
      description = bounded(description, 1024);
      costKey = bounded(costKey, 512);
      costCount = Math.max(0, Math.min(64000, costCount));
      this.productKey = productKey;
      this.price = price;
      this.description = description;
      this.costKey = costKey;
      this.costCount = costCount;
   }

   public boolean barter() {
      return !MartItemGrammar.costGroups(this.costKey, this.costCount).isEmpty();
   }

   public CompoundTag write() {
      CompoundTag var1 = new CompoundTag();
      var1.putString("ItemId", this.productKey);
      var1.putString("Item", this.productKey);
      var1.putInt("Price", this.price);
      var1.putString("Desc", this.description);
      if (this.barter()) {
         var1.putString("CostItem", this.costKey);
         var1.putInt("CostCount", this.costCount);
      }

      return var1;
   }

   public static MartOffer read(CompoundTag var0) {
      String var1 = first(var0.getString("ItemId"), var0.getString("Item"));
      String var2 = first(var0.getString("CostItem"), var0.getString("CostItems"));
      return new MartOffer(var1, var0.getInt("Price"), var0.getString("Desc"), var2, var0.getInt("CostCount"));
   }

   public ResourceLocation simpleProductId() {
      return parseSimpleId(this.productKey);
   }

   public ResourceLocation simpleCostId() {
      return parseSimpleId(this.costKey);
   }

   private static ResourceLocation parseSimpleId(String var0) {
      String var1 = var0 == null ? "" : var0.trim();
      int var2 = var1.indexOf(32);
      if (var2 >= 0) {
         var1 = var1.substring(0, var2);
      }

      if (var1.startsWith("item:")) {
         var1 = var1.substring(5);
      }

      return ResourceLocation.tryParse(var1);
   }

   private static String first(String var0, String var1) {
      return var0 != null && !var0.isBlank() ? var0 : (var1 == null ? "" : var1);
   }

   private static String bounded(String var0, int var1) {
      String var2 = var0 == null ? "" : var0.replace('\u0000', ' ').trim();
      return var2.length() <= var1 ? var2 : var2.substring(0, var1);
   }
}
