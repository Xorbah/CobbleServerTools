package net.cobbleservertools.commerce;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.cobbleservertools.entity.AbstractBattleNpcEntity;
import net.cobbleservertools.entity.AbstractCobbleNpcEntity;
import net.cobbleservertools.entity.RivalNpcEntity;
import net.cobbleservertools.entity.data.NpcKind;
import net.cobbleservertools.rival.RivalNpcTeamsStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public final class NpcServiceConfig {
   private static final int MAX_TEXT = 1024;

   private NpcServiceConfig() {
   }

   public static CompoundTag editorView(AbstractCobbleNpcEntity var0) {
      CompoundTag var1 = var0.compatibilityData();
      CompoundTag var2 = new CompoundTag();
      if (var0 instanceof RivalNpcEntity var3) {
         var2.put("RivalTeams", RivalNpcTeamsStorage.toNbt(var3.rivalTeams()));
      } else if (var0 instanceof AbstractBattleNpcEntity) {
         copyList(var1, var2, "NpcPokemons", 6);
      }

      if (var0.npcKind() == NpcKind.MART) {
         copyList(var1, var2, "ShopItems", 256);
         copyList(var1, var2, "SellPrices", 512);
         copyList(var1, var2, "SellDescs", 512);
      } else if (var0.npcKind() == NpcKind.TRADER) {
         var2.putString("tradeRequestId", first(var1, "tradeRequestId", "requestSpecies", "requestId", "RequestedPokemon"));
         var2.putString("tradeOfferId", first(var1, "tradeOfferId", "offerSpecies", "offerId", "OfferedPokemon"));
         var2.putInt("tradeOfferLevel", positive(var1, 10, "tradeOfferLevel", "offerLevel", "level"));
         var2.putString("tradeOfferGender", normalizeGender(first(var1, "tradeOfferGender", "offerGender", "gender")));
         var2.putBoolean("unlimitedTrade", bool(var1, "unlimitedTrade", "UnlimitedTrades"));
      } else if (var0.npcKind() == NpcKind.MOVE_TUTOR) {
         var2.putString("TutorMoveId", first(var1, "tutorMoveId", "MoveId"));
      }

      var2.putDouble("EntityX", var0.getX());
      var2.putDouble("EntityY", var0.getY());
      var2.putDouble("EntityZ", var0.getZ());
      return var2;
   }

   public static void clearForPreset(AbstractCobbleNpcEntity var0) {
      if (var0 instanceof RivalNpcEntity var1) {
         var1.setRivalTeams(new RivalNpcTeamsStorage.Teams());
      }

      var0.replaceCompatibilityData(new CompoundTag());
   }

   public static void apply(AbstractCobbleNpcEntity var0, CompoundTag var1) {
      if (var1 != null) {
         CompoundTag var2 = var0.compatibilityData();
         if (var0 instanceof RivalNpcEntity var3 && var1.contains("RivalTeams", 10)) {
            var3.setRivalTeams(sanitizeRivalTeams(RivalNpcTeamsStorage.fromNbt(var1.getCompound("RivalTeams"))));
         } else if (var0 instanceof AbstractBattleNpcEntity && var1.contains("NpcPokemons", 9)) {
            var2.put("NpcPokemons", sanitizeBattleTeam(var1.getList("NpcPokemons", 10)));
         }

         if (var0.npcKind() == NpcKind.MART) {
            if (var1.contains("ShopItems", 9)) {
               var2.put("ShopItems", sanitizeShop(var1.getList("ShopItems", 10)));
            }

            if (var1.contains("SellPrices", 9)) {
               var2.put("SellPrices", sanitizeSellPrices(var1.getList("SellPrices", 10)));
            }

            if (var1.contains("SellDescs", 9)) {
               var2.put("SellDescs", sanitizeSellDescriptions(var1.getList("SellDescs", 10)));
            }
         } else if (var0.npcKind() == NpcKind.TRADER) {
            if (var1.contains("tradeRequestId")) {
               var2.putString("tradeRequestId", species(var1.getString("tradeRequestId")));
            }

            if (var1.contains("tradeOfferId")) {
               var2.putString("tradeOfferId", species(var1.getString("tradeOfferId")));
            }

            if (var1.contains("tradeOfferLevel")) {
               var2.putInt("tradeOfferLevel", clamp(var1.getInt("tradeOfferLevel"), 1, 100));
            }

            if (var1.contains("tradeOfferGender")) {
               var2.putString("tradeOfferGender", normalizeGender(var1.getString("tradeOfferGender")));
            }

            if (var1.contains("unlimitedTrade")) {
               var2.putBoolean("unlimitedTrade", var1.getBoolean("unlimitedTrade"));
            }
         } else if (var0.npcKind() == NpcKind.MOVE_TUTOR) {
            String var9 = bounded(first(var1, "TutorMoveId", "tutorMoveId", "MoveId"), 128).toLowerCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
            if (var9.matches("[a-z0-9_.:-]*")) {
               var2.putString("tutorMoveId", var9);
               var2.putString("TutorMoveId", var9);
            }
         }

         if (var1.contains("EntityX") && var1.contains("EntityY") && var1.contains("EntityZ")) {
            double var10 = var1.getDouble("EntityX");
            double var5 = var1.getDouble("EntityY");
            double var7 = var1.getDouble("EntityZ");
            if (Double.isFinite(var10) && Double.isFinite(var5) && Double.isFinite(var7)) {
               var0.moveTo(var10, var5, var7, var0.getYRot(), var0.getXRot());
               var0.setDeltaMovement(Vec3.ZERO);
               var0.getNavigation().stop();
            }
         }

         var0.replaceCompatibilityData(var2);
      }
   }

   private static ListTag sanitizeBattleTeam(ListTag var0) {
      ListTag var1 = new ListTag();

      for (int var2 = 0; var2 < Math.min(6, var0.size()); var2++) {
         CompoundTag var3 = var0.getCompound(var2);
         String var4 = species(first(var3, "Species", "Properties", "nameID"));
         if (!var4.isBlank()) {
            CompoundTag var5 = new CompoundTag();
            var5.putString("Species", var4);
            var5.putString("Properties", var4);
            var5.putInt("Level", clamp(var3.contains("Level") ? var3.getInt("Level") : 1, 1, 100));
            var5.putString("Gender", normalizeGender(var3.getString("Gender")));
            var5.put("Moves", sanitizeMoveCompounds(var3.getList("Moves", 10)));
            var1.add(var5);
         }
      }

      return var1;
   }

   private static ListTag sanitizeMoveCompounds(ListTag var0) {
      ListTag var1 = new ListTag();

      for (int var2 = 0; var2 < Math.min(4, var0.size()); var2++) {
         String var3 = bounded(var0.getCompound(var2).getString("MoveId"), 128).toLowerCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
         if (var3.matches("[a-z0-9_.:-]+")) {
            CompoundTag var4 = new CompoundTag();
            var4.putString("MoveId", var3);
            var1.add(var4);
         }
      }

      return var1;
   }

   private static RivalNpcTeamsStorage.Teams sanitizeRivalTeams(RivalNpcTeamsStorage.Teams var0) {
      RivalNpcTeamsStorage.Teams var1 = new RivalNpcTeamsStorage.Teams();
      var1.fire = sanitizeRivalList(var0 == null ? null : var0.fire);
      var1.water = sanitizeRivalList(var0 == null ? null : var0.water);
      var1.grass = sanitizeRivalList(var0 == null ? null : var0.grass);
      return var1;
   }

   private static List<RivalNpcTeamsStorage.PokemonEntry> sanitizeRivalList(List<RivalNpcTeamsStorage.PokemonEntry> var0) {
      ArrayList var1 = new ArrayList();
      if (var0 == null) {
         return var1;
      }

      for (int var2 = 0; var2 < Math.min(6, var0.size()); var2++) {
         RivalNpcTeamsStorage.PokemonEntry var3 = (RivalNpcTeamsStorage.PokemonEntry)var0.get(var2);
         if (var3 != null) {
            String var4 = species(var3.species);
            if (!var4.isBlank()) {
               ArrayList var5 = new ArrayList();
               if (var3.moves != null) {
                  for (int var6 = 0; var6 < Math.min(4, var3.moves.size()); var6++) {
                     String var7 = bounded(var3.moves.get(var6), 128).toLowerCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
                     if (var7.matches("[a-z0-9_.:-]+")) {
                        var5.add(var7);
                     }
                  }
               }

               var1.add(new RivalNpcTeamsStorage.PokemonEntry(var4, clamp(var3.level, 1, 100), normalizeGender(var3.gender), var5));
            }
         }
      }

      return var1;
   }

   private static ListTag sanitizeShop(ListTag var0) {
      ListTag var1 = new ListTag();

      for (int var2 = 0; var2 < Math.min(var0.size(), 256); var2++) {
         CompoundTag var3 = var0.getCompound(var2);
         String var4 = shopExpression(first(var3, "ItemId", "Item"));
         if (!MartItemGrammar.productEntries(var4).isEmpty()) {
            CompoundTag var5 = new CompoundTag();
            var5.putString("ItemId", var4);
            var5.putString("Item", var4);
            var5.putInt("Price", clamp(var3.getInt("Price"), 0, 1000000000));
            var5.putString("Desc", bounded(var3.getString("Desc"), 1024));
            String var6 = shopExpression(first(var3, "CostItem", "CostItems"));
            int var7 = clamp(var3.getInt("CostCount"), 0, 64000);
            List var8 = MartItemGrammar.costGroups(var6, var7);
            if (!var8.isEmpty()) {
               var5.putString("CostItem", MartItemGrammar.canonicalCosts(var8));
               var5.putInt("CostCount", ((MartItemGrammar.CostGroup)var8.getFirst()).alternatives().getFirst().count());
            }

            var1.add(var5);
         }
      }

      return var1;
   }

   private static ListTag sanitizeSellPrices(ListTag var0) {
      ListTag var1 = new ListTag();

      for (int var2 = 0; var2 < Math.min(var0.size(), 512); var2++) {
         CompoundTag var3 = var0.getCompound(var2);
         String var4 = item(first(var3, "ItemId", "Item"));
         int var5 = clamp(var3.getInt("Price"), 0, 1000000000);
         if (!var4.isBlank() && var5 > 0) {
            CompoundTag var6 = new CompoundTag();
            var6.putString("ItemId", var4);
            var6.putString("Item", var4);
            var6.putInt("Price", var5);
            var1.add(var6);
         }
      }

      return var1;
   }

   private static ListTag sanitizeSellDescriptions(ListTag var0) {
      ListTag var1 = new ListTag();

      for (int var2 = 0; var2 < Math.min(var0.size(), 512); var2++) {
         CompoundTag var3 = var0.getCompound(var2);
         String var4 = item(first(var3, "ItemId", "Item"));
         if (!var4.isBlank()) {
            CompoundTag var5 = new CompoundTag();
            var5.putString("ItemId", var4);
            var5.putString("Item", var4);
            var5.putString("Desc", bounded(var3.getString("Desc"), 1024));
            var1.add(var5);
         }
      }

      return var1;
   }

   private static void copyList(CompoundTag var0, CompoundTag var1, String var2, int var3) {
      ListTag var4 = var0.getList(var2, 10);
      ListTag var5 = new ListTag();

      for (int var6 = 0; var6 < Math.min(var4.size(), var3); var6++) {
         var5.add(var4.getCompound(var6).copy());
      }

      var1.put(var2, var5);
   }

   private static String shopExpression(String var0) {
      return bounded(var0, 4096);
   }

   private static String item(String var0) {
      String var1 = bounded(var0, 512);
      String var2 = var1;
      int var3 = var2.indexOf(32);
      if (var3 >= 0) {
         var2 = var2.substring(0, var3);
      }

      if (var2.startsWith("item:")) {
         var2 = var2.substring(5);
      }

      if (ResourceLocation.tryParse(var2) == null) {
         return "";
      } else {
         return var3 >= 0 ? var2 + var1.substring(var3) : var2;
      }
   }

   private static String species(String var0) {
      String var1 = bounded(var0, 128).toLowerCase(Locale.ROOT).replace(' ', '_');
      return var1.matches("[a-z0-9_.:-]*") ? var1 : "";
   }

   private static String normalizeGender(String var0) {
      String var1 = var0 == null ? "random" : var0.trim().toLowerCase(Locale.ROOT);

      return switch (var1) {
         case "male", "m" -> "male";
         case "female", "f" -> "female";
         case "genderless", "none", "x" -> "genderless";
         default -> "random";
      };
   }

   private static String first(CompoundTag var0, String... var1) {
      for (String var5 : var1) {
         if (var0.contains(var5) && !var0.getString(var5).isBlank()) {
            return var0.getString(var5);
         }
      }

      return "";
   }

   private static int positive(CompoundTag var0, int var1, String... var2) {
      for (String var6 : var2) {
         if (var0.contains(var6) && var0.getInt(var6) > 0) {
            return var0.getInt(var6);
         }
      }

      return var1;
   }

   private static boolean bool(CompoundTag var0, String... var1) {
      for (String var5 : var1) {
         if (var0.contains(var5)) {
            return var0.getBoolean(var5);
         }
      }

      return false;
   }

   private static String bounded(String var0, int var1) {
      String var2 = var0 == null ? "" : var0.replace('\u0000', ' ').trim();
      return var2.length() <= var1 ? var2 : var2.substring(0, var1);
   }

   private static int clamp(int var0, int var1, int var2) {
      return Math.max(var1, Math.min(var2, var0));
   }
}
