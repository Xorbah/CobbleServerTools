package net.cobbleservertools.commerce;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import net.cobbleservertools.CobbleServerTools;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.neoforged.fml.loading.FMLPaths;

public final class MartPresetCatalog {
   public static final String DEFAULT_PRESET = "viridian_mart";

   private MartPresetCatalog() {
   }

   public static CompoundTag load(String var0) {
      String var1 = normalizeId(var0);
      if (var1.isBlank()) {
         return new CompoundTag();
      }

      Path var2 = FMLPaths.CONFIGDIR.get().resolve("cobbleservertools").resolve("marts").resolve(var1 + ".json");

      try {
         if (Files.isRegularFile(var2)) {
            try (BufferedReader var16 = Files.newBufferedReader(var2, StandardCharsets.UTF_8)) {
               return parse(var16, var2.toString());
            }
         }
      } catch (IOException | RuntimeException var15) {
         CobbleServerTools.LOGGER.warn("Failed to load Mart preset {}: {}", new Object[]{var2, var15.toString()});
      }

      String var3 = "/assets/cobbleservertools/marts/" + var1 + ".json";

      try (InputStream var4 = MartPresetCatalog.class.getResourceAsStream(var3)) {
         if (var4 == null) {
            return new CompoundTag();
         }

         try (InputStreamReader var5 = new InputStreamReader(var4, StandardCharsets.UTF_8)) {
            return parse(var5, var3);
         }
      } catch (IOException | RuntimeException var13) {
         CobbleServerTools.LOGGER.warn("Failed to load bundled Mart preset {}: {}", new Object[]{var3, var13.toString()});
         return new CompoundTag();
      }
   }

   private static CompoundTag parse(Reader var0, String var1) {
      JsonElement var2 = JsonParser.parseReader(var0);
      if (!var2.isJsonObject()) {
         throw new IllegalArgumentException("Mart preset root must be an object: " + var1);
      }

      JsonObject var3 = var2.getAsJsonObject();
      CompoundTag var4 = new CompoundTag();
      putString(var3, var4, "skin", "NpcSkin");
      putBoolean(var3, var4, "seekPlayer", "SeekPlayer");
      putBoolean(var3, var4, "lockPosition", "LockPos");
      putBoolean(var3, var4, "lookAtPlayer", "LookAtPlayer");
      ListTag var5 = new ListTag();

      for (JsonElement var8 : array(var3, "shopItems")) {
         if (var8.isJsonObject()) {
            CompoundTag var9 = offer(var8.getAsJsonObject());
            if (!var9.getString("ItemId").isBlank() && var9.getInt("Price") > 0) {
               var5.add(var9);
            }
         }
      }

      var4.put("ShopItems", var5);
      ListTag var18 = new ListTag();
      ListTag var19 = new ListTag();

      for (JsonElement var11 : array(var3, "sellItems")) {
         if (var11.isJsonObject()) {
            JsonObject var12 = var11.getAsJsonObject();
            String var13 = string(var12, "item");
            int var14 = integer(var12, "price", 0);
            if (!var13.isBlank() && var14 > 0) {
               CompoundTag var15 = new CompoundTag();
               var15.putString("ItemId", var13);
               var15.putInt("Price", var14);
               var18.add(var15);
               String var16 = string(var12, "description");
               if (!var16.isBlank()) {
                  CompoundTag var17 = new CompoundTag();
                  var17.putString("ItemId", var13);
                  var17.putString("Desc", var16);
                  var19.add(var17);
               }
            }
         }
      }

      var4.put("SellPrices", var18);
      var4.put("SellDescs", var19);
      return var4;
   }

   private static CompoundTag offer(JsonObject var0) {
      CompoundTag var1 = new CompoundTag();
      var1.putString("ItemId", string(var0, "item"));
      var1.putInt("Price", integer(var0, "price", 0));
      var1.putString("Desc", string(var0, "description"));
      return var1;
   }

   private static JsonArray array(JsonObject var0, String var1) {
      JsonElement var2 = var0.get(var1);
      return var2 != null && var2.isJsonArray() ? var2.getAsJsonArray() : new JsonArray();
   }

   private static String string(JsonObject var0, String var1) {
      JsonElement var2 = var0.get(var1);
      return var2 != null && var2.isJsonPrimitive() ? var2.getAsString().trim() : "";
   }

   private static int integer(JsonObject var0, String var1, int var2) {
      String var3 = string(var0, var1);
      if (var3.isBlank()) {
         return var2;
      }

      try {
         return Integer.parseInt(var3);
      } catch (NumberFormatException var5) {
         return var2;
      }
   }

   private static void putString(JsonObject var0, CompoundTag var1, String var2, String var3) {
      String var4 = string(var0, var2);
      if (!var4.isBlank()) {
         var1.putString(var3, var4);
      }
   }

   private static void putBoolean(JsonObject var0, CompoundTag var1, String var2, String var3) {
      String var4 = string(var0, var2).toLowerCase(Locale.ROOT);
      if ("true".equals(var4) || "false".equals(var4)) {
         var1.putBoolean(var3, Boolean.parseBoolean(var4));
      }
   }

   private static String normalizeId(String var0) {
      String var1 = var0 == null ? "" : var0.trim().toLowerCase(Locale.ROOT);
      return var1.matches("[a-z0-9_.-]+") ? var1 : "";
   }
}
