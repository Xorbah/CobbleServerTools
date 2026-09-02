package net.cobbleservertools.commerce;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;

public record MartCatalog(List<MartOffer> offers, Map<ResourceLocation, Integer> sellPrices, Map<ResourceLocation, String> sellDescriptions) {
   public static final int MAX_OFFERS = 256;
   public static final int MAX_SELL_ITEMS = 512;

   public MartCatalog(List<MartOffer> offers, Map<ResourceLocation, Integer> sellPrices, Map<ResourceLocation, String> sellDescriptions) {
      offers = List.copyOf(offers == null ? List.of() : offers.subList(0, Math.min(offers.size(), 256)));
      sellPrices = Map.copyOf(sellPrices == null ? Map.of() : sellPrices);
      sellDescriptions = Map.copyOf(sellDescriptions == null ? Map.of() : sellDescriptions);
      this.offers = offers;
      this.sellPrices = sellPrices;
      this.sellDescriptions = sellDescriptions;
   }

   public static MartCatalog read(CompoundTag var0) {
      ArrayList var1 = new ArrayList();
      ListTag var2 = var0.getList("ShopItems", 10);

      for (int var3 = 0; var3 < Math.min(var2.size(), 256); var3++) {
         MartOffer var4 = MartOffer.read(var2.getCompound(var3));
         if (!var4.productKey().isBlank()) {
            var1.add(var4);
         }
      }

      LinkedHashMap var10 = new LinkedHashMap();
      ListTag var11 = var0.getList("SellPrices", 10);

      for (int var5 = 0; var5 < Math.min(var11.size(), 512); var5++) {
         CompoundTag var6 = var11.getCompound(var5);
         ResourceLocation var7 = ResourceLocation.tryParse(first(var6.getString("ItemId"), var6.getString("Item")));
         if (var7 != null && var6.getInt("Price") > 0) {
            var10.put(var7, Math.min(1000000000, var6.getInt("Price")));
         }
      }

      LinkedHashMap var12 = new LinkedHashMap();
      ListTag var13 = var0.getList("SellDescs", 10);

      for (int var14 = 0; var14 < Math.min(var13.size(), 512); var14++) {
         CompoundTag var8 = var13.getCompound(var14);
         ResourceLocation var9 = ResourceLocation.tryParse(first(var8.getString("ItemId"), var8.getString("Item")));
         if (var9 != null) {
            var12.put(var9, var8.getString("Desc"));
         }
      }

      return new MartCatalog(var1, var10, var12);
   }

   public void writeInto(CompoundTag var1) {
      ListTag var2 = new ListTag();

      for (MartOffer var4 : this.offers) {
         var2.add(var4.write());
      }

      var1.put("ShopItems", var2);
      ListTag var5 = new ListTag();
      this.sellPrices.forEach((var1x, var2x) -> {
         CompoundTag var3 = new CompoundTag();
         var3.putString("ItemId", var1x.toString());
         var3.putString("Item", var1x.toString());
         var3.putInt("Price", var2x);
         var5.add(var3);
      });
      var1.put("SellPrices", var5);
      ListTag var6 = new ListTag();
      this.sellDescriptions.forEach((var1x, var2x) -> {
         CompoundTag var3 = new CompoundTag();
         var3.putString("ItemId", var1x.toString());
         var3.putString("Item", var1x.toString());
         var3.putString("Desc", var2x);
         var6.add(var3);
      });
      var1.put("SellDescs", var6);
   }

   public CompoundTag clientView(int var1) {
      CompoundTag var2 = new CompoundTag();
      var2.putInt("Money", var1);
      this.writeInto(var2);
      return var2;
   }

   private static String first(String var0, String var1) {
      return var0 != null && !var0.isBlank() ? var0 : (var1 == null ? "" : var1);
   }
}
