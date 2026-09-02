package net.cobbleservertools.config;

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
import java.util.Iterator;
import java.util.Locale;
import net.cobbleservertools.CobbleServerTools;
import net.cobbleservertools.commerce.MartPresetCatalog;
import net.cobbleservertools.entity.data.NpcKind;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.neoforged.fml.loading.FMLPaths;

public final class NpcTemplateCatalog {
   private static final String TEMPLATE_DIR = "templates";

   private NpcTemplateCatalog() {
   }

   public static CompoundTag load(NpcKind var0) {
      return var0 == null ? new CompoundTag() : loadFile(fileName(var0));
   }

   public static CompoundTag loadVendingMachine() {
      return loadFile("vending_machine.json");
   }

   private static CompoundTag loadFile(String var0) {
      Path var1 = FMLPaths.CONFIGDIR.get().resolve("cobbleservertools").resolve("templates").resolve(var0);

      try {
         if (Files.isRegularFile(var1)) {
            try (BufferedReader var15 = Files.newBufferedReader(var1, StandardCharsets.UTF_8)) {
               return parse(var15, var1.toString());
            }
         }
      } catch (IOException | RuntimeException var14) {
         CobbleServerTools.LOGGER.warn("Failed to load NPC template {}: {}", new Object[]{var1, var14.toString()});
      }

      String var2 = "/assets/cobbleservertools/templates/" + var0;

      try (InputStream var3 = NpcTemplateCatalog.class.getResourceAsStream(var2)) {
         if (var3 == null) {
            return new CompoundTag();
         }

         try (InputStreamReader var4 = new InputStreamReader(var3, StandardCharsets.UTF_8)) {
            return parse(var4, var2);
         }
      } catch (IOException | RuntimeException var12) {
         CobbleServerTools.LOGGER.warn("Failed to load bundled NPC template {}: {}", new Object[]{var2, var12.toString()});
         return new CompoundTag();
      }
   }

   private static CompoundTag parse(Reader var0, String var1) {
      JsonElement var2 = JsonParser.parseReader(var0);
      if (!var2.isJsonObject()) {
         throw new IllegalArgumentException("NPC template root must be an object: " + var1);
      } else {
         return parseObject(var2.getAsJsonObject(), var1);
      }
   }

   public static CompoundTag parseObject(JsonObject var0, String var1) {
      if (var0 == null) {
         throw new IllegalArgumentException("NPC template root must not be null: " + var1);
      }

      CompoundTag var2 = new CompoundTag();
      putString(var0, var2, "npcName", "NpcName", true);
      putFloat(var0, var2, "npcSize", "NpcSize");
      putString(var0, var2, "npcType", "NpcType", false);
      putString(var0, var2, "skin", "NpcSkin", false);
      putString(var0, var2, "dialogId", "DialogId", true);
      putBoolean(var0, var2, "seekPlayer", "SeekPlayer");
      putInt(var0, var2, "seekRange", "SeekRange");
      putBoolean(var0, var2, "sitting", "Sitting");
      putBoolean(var0, var2, "lockPosition", "LockPos");
      putBoolean(var0, var2, "lookAtPlayer", "LookAtPlayer");
      putString(var0, var2, "battleType", "BattleType", false);
      putString(var0, var2, "rewardItemId", "RewardItemId", true);
      putString(var0, var2, "onVictoryCommand", "OnVictoryCommand", true);
      putBoolean(var0, var2, "rewardResetAlways", "RewardResetAlways");
      putInt(var0, var2, "moneyReward", "MoneyReward");
      putInt(var0, var2, "rematchCooldownSeconds", "RematchCooldownSeconds");
      putBoolean(var0, var2, "healAfterDialog", "HealAfterDialog");
      putString(var0, var2, "tutorMoveId", "TutorMoveId", true);
      if (var0.has("team")) {
         var2.put("NpcPokemons", parseBattleTeam(array(var0, "team")));
      }

      if (var0.has("teams") && var0.get("teams").isJsonObject()) {
         JsonObject var3 = var0.getAsJsonObject("teams");
         CompoundTag var4 = new CompoundTag();
         var4.put("Fire", parseRivalTeam(array(var3, "fire")));
         var4.put("Water", parseRivalTeam(array(var3, "water")));
         var4.put("Grass", parseRivalTeam(array(var3, "grass")));
         var2.put("RivalTeams", var4);
      }

      putString(var0, var2, "requestSpecies", "tradeRequestId", true);
      putString(var0, var2, "offerSpecies", "tradeOfferId", true);
      putInt(var0, var2, "offerLevel", "tradeOfferLevel");
      putString(var0, var2, "offerGender", "tradeOfferGender", true);
      putBoolean(var0, var2, "unlimitedTrades", "unlimitedTrade");
      String var6 = string(var0, "martPreset");
      if (!var6.isBlank()) {
         CompoundTag var7 = MartPresetCatalog.load(var6);
         var2.merge(var7);
      }

      if (var0.has("shopItems")) {
         var2.put("ShopItems", parseShop(array(var0, "shopItems")));
      }

      if (var0.has("sellItems")) {
         ListTag var8 = new ListTag();
         ListTag var5 = new ListTag();
         parseSell(array(var0, "sellItems"), var8, var5);
         var2.put("SellPrices", var8);
         var2.put("SellDescs", var5);
      }

      return var2;
   }

   private static ListTag parseBattleTeam(JsonArray var0) {
      ListTag var1 = new ListTag();
      int var2 = 0;

      for (JsonElement var4 : var0) {
         if (var2 >= 6 || !var4.isJsonObject()) {
            break;
         }

         JsonObject var5 = var4.getAsJsonObject();
         String var6 = string(var5, "species");
         String var7 = string(var5, "properties");
         if (!var6.isBlank() || !var7.isBlank()) {
            if (var6.isBlank()) {
               var6 = var7;
            }

            if (var7.isBlank()) {
               var7 = var6;
            }

            CompoundTag var8 = new CompoundTag();
            var8.putString("Species", var6);
            var8.putString("Properties", var7);
            var8.putInt("Level", integer(var5, "level", 1));
            var8.putString("Gender", string(var5, "gender"));
            ListTag var9 = new ListTag();
            Iterator var10 = array(var5, "moves").iterator();

            while (true) {
               if (var10.hasNext()) {
                  JsonElement var11 = (JsonElement)var10.next();
                  if (var9.size() < 4) {
                     String var12 = var11.isJsonPrimitive() ? var11.getAsString().trim() : "";
                     if (!var12.isBlank()) {
                        CompoundTag var13 = new CompoundTag();
                        var13.putString("MoveId", var12);
                        var9.add(var13);
                     }
                     continue;
                  }
               }

               var8.put("Moves", var9);
               var1.add(var8);
               var2++;
               break;
            }
         }
      }

      return var1;
   }

   private static ListTag parseRivalTeam(JsonArray var0) {
      ListTag var1 = new ListTag();
      int var2 = 0;

      for (JsonElement var4 : var0) {
         if (var2 >= 6 || !var4.isJsonObject()) {
            break;
         }

         JsonObject var5 = var4.getAsJsonObject();
         String var6 = string(var5, "species");
         if (!var6.isBlank()) {
            CompoundTag var7 = new CompoundTag();
            var7.putString("Species", var6);
            var7.putInt("Level", integer(var5, "level", 1));
            var7.putString("Gender", string(var5, "gender"));
            ListTag var8 = new ListTag();
            Iterator var9 = array(var5, "moves").iterator();

            while (true) {
               if (var9.hasNext()) {
                  JsonElement var10 = (JsonElement)var9.next();
                  if (var8.size() < 4) {
                     String var11 = var10.isJsonPrimitive() ? var10.getAsString().trim() : "";
                     if (!var11.isBlank()) {
                        var8.add(StringTag.valueOf(var11));
                     }
                     continue;
                  }
               }

               var7.put("Moves", var8);
               var1.add(var7);
               var2++;
               break;
            }
         }
      }

      return var1;
   }

   private static ListTag parseShop(JsonArray var0) {
      ListTag var1 = new ListTag();

      for (JsonElement var3 : var0) {
         if (var3.isJsonObject()) {
            JsonObject var4 = var3.getAsJsonObject();
            String var5 = string(var4, "item");
            int var6 = integer(var4, "price", 0);
            if (!var5.isBlank() && var6 > 0) {
               CompoundTag var7 = new CompoundTag();
               var7.putString("ItemId", var5);
               var7.putString("Item", var5);
               var7.putInt("Price", var6);
               var7.putString("Desc", string(var4, "description"));
               String var8 = string(var4, "costItem");
               int var9 = integer(var4, "costCount", 0);
               if (!var8.isBlank() && var9 > 0) {
                  var7.putString("CostItem", var8);
                  var7.putInt("CostCount", var9);
               }

               var1.add(var7);
            }
         }
      }

      return var1;
   }

   private static void parseSell(JsonArray var0, ListTag var1, ListTag var2) {
      for (JsonElement var4 : var0) {
         if (var4.isJsonObject()) {
            JsonObject var5 = var4.getAsJsonObject();
            String var6 = string(var5, "item");
            int var7 = integer(var5, "price", 0);
            if (!var6.isBlank() && var7 > 0) {
               CompoundTag var8 = new CompoundTag();
               var8.putString("ItemId", var6);
               var8.putInt("Price", var7);
               var1.add(var8);
               String var9 = string(var5, "description");
               if (!var9.isBlank()) {
                  CompoundTag var10 = new CompoundTag();
                  var10.putString("ItemId", var6);
                  var10.putString("Desc", var9);
                  var2.add(var10);
               }
            }
         }
      }
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

   private static float decimal(JsonObject var0, String var1, float var2) {
      String var3 = string(var0, var1);
      if (var3.isBlank()) {
         return var2;
      }

      try {
         return Float.parseFloat(var3);
      } catch (NumberFormatException var5) {
         return var2;
      }
   }

   private static void putString(JsonObject var0, CompoundTag var1, String var2, String var3, boolean var4) {
      if (var0.has(var2)) {
         String var5 = string(var0, var2);
         if (var4 || !var5.isBlank()) {
            var1.putString(var3, var5);
         }
      }
   }

   private static void putBoolean(JsonObject var0, CompoundTag var1, String var2, String var3) {
      if (var0.has(var2)) {
         String var4 = string(var0, var2).toLowerCase(Locale.ROOT);
         if ("true".equals(var4) || "false".equals(var4)) {
            var1.putBoolean(var3, Boolean.parseBoolean(var4));
         }
      }
   }

   private static void putInt(JsonObject var0, CompoundTag var1, String var2, String var3) {
      if (var0.has(var2)) {
         var1.putInt(var3, integer(var0, var2, 0));
      }
   }

   private static void putFloat(JsonObject var0, CompoundTag var1, String var2, String var3) {
      if (var0.has(var2)) {
         var1.putFloat(var3, decimal(var0, var2, 0.9375F));
      }
   }

   private static String fileName(NpcKind var0) {
      return switch (var0) {
         case BATTLE -> "battle_npc.json";
         case RIVAL -> "rival_npc.json";
         case DIALOG -> "dialog_npc.json";
         case TRADER -> "trader_npc.json";
         case MART -> "mart_npc.json";
         case MOVE_TUTOR -> "move_tutor_npc.json";
      };
   }
}
