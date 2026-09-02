package net.cobbleservertools.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import net.cobbleservertools.CobbleServerTools;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public final class ItemPriceLoader implements ResourceManagerReloadListener {
   private static final ResourceLocation DATA_PATH = ResourceLocation.fromNamespaceAndPath("cobbleservertools", "npc/item_sell_prices.json");
   private static final Type MAP_TYPE = (new TypeToken<Map<String, Integer>>() {}).getType();
   private static volatile Map<ResourceLocation, Integer> prices = Map.of();

   public void onResourceManagerReload(ResourceManager var1) {
      LinkedHashMap<String, Integer> var2 = new LinkedHashMap<>();

      try {
         Resource var3 = (Resource)var1.getResource(DATA_PATH).orElse(null);
         if (var3 == null) {
            CobbleServerTools.LOGGER.warn("[ItemPriceLoader] missing resource npc/item_sell_prices.json", new Object[0]);
         } else {
            try (InputStreamReader var4 = new InputStreamReader(var3.open(), StandardCharsets.UTF_8)) {
               Map<String, Integer> var5 = new Gson().fromJson(var4, MAP_TYPE);
               if (var5 != null) {
                  var2.putAll(var5);
               }
            }
         }
      } catch (Exception var9) {
         CobbleServerTools.LOGGER.warn("[ItemPriceLoader] parse error", new Object[]{var9});
      }

      LinkedHashMap<ResourceLocation, Integer> var10 = new LinkedHashMap<>();
      var2.forEach((var1x, var2x) -> {
         try {
            if (var1x == null) {
               return;
            }

            String[] var3x = var1x.split(":", -1);
            if (var3x.length != 2) {
               throw new IllegalArgumentException("Expected namespace:path");
            }

            ResourceLocation var4x = ResourceLocation.fromNamespaceAndPath(var3x[0].toLowerCase(Locale.ROOT), var3x[1].toLowerCase(Locale.ROOT));
            var10.put(var4x, var2x == null ? 0 : var2x);
         } catch (RuntimeException var5x) {
            CobbleServerTools.LOGGER.warn("[ItemPriceLoader] invalid item id {}", new Object[]{var1x});
         }
      });
      prices = Map.copyOf(var10);
      CobbleServerTools.LOGGER.info("[ItemPriceLoader] loaded {} entries", new Object[]{prices.size()});
   }

   public static int getPrice(ResourceLocation var0) {
      return var0 == null ? 0 : prices.getOrDefault(var0, 0);
   }

   public static Map<ResourceLocation, Integer> map() {
      return prices;
   }
}
