package net.cobbleservertools.util;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public final class ItemPriceRuntime {
   private static final Map<ResourceLocation, Integer> PRICES = new LinkedHashMap<>();

   private ItemPriceRuntime() {
   }

   public static int getPrice(ResourceLocation var0) {
      return var0 == null ? 0 : PRICES.getOrDefault(var0, 0);
   }

   public static Map<ResourceLocation, Integer> map() {
      return PRICES;
   }

   public static void setPrice(ResourceLocation var0, int var1) {
      if (var0 != null) {
         PRICES.put(var0, Math.max(0, var1));
      }
   }

   public static void removePrice(ResourceLocation var0) {
      if (var0 != null) {
         PRICES.remove(var0);
      }
   }
}
