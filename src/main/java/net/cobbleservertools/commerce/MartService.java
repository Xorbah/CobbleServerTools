package net.cobbleservertools.commerce;

import com.cobblemon.mod.common.pokemon.Pokemon;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import net.cobbleservertools.CobbleServerTools;
import net.cobbleservertools.battle.PlayerMoneyService;
import net.cobbleservertools.config.PremierBallBonusConfig;
import net.cobbleservertools.entity.MartNpcEntity;
import net.cobbleservertools.util.NpcRewardFieldUtil;
import net.cobbleservertools.util.PokemonRewardUtil;
import net.cobbleservertools.util.ShinyNotificationUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class MartService {
   public static final int MAX_QUANTITY = 64;
   private static final int COBBLESERVERTOOLS_SHINY_ODDS_DENOMINATOR = 8192;
   private static final ResourceLocation POKE_BALL_ID = ResourceLocation.fromNamespaceAndPath("cobblemon", "poke_ball");
   private static final ResourceLocation PREMIER_BALL_ID = ResourceLocation.fromNamespaceAndPath("cobblemon", "premier_ball");

   private MartService() {
   }

   public static CompoundTag clientView(ServerPlayer var0, MartNpcEntity var1) {
      MartCatalog var2 = MartCatalog.read(var1.compatibilityData());
      CompoundTag var3 = var2.clientView(PlayerMoneyService.get(var0));
      ListTag var4 = new ListTag();
      Inventory var5 = var0.getInventory();

      for (int var6 = 0; var6 < var5.getContainerSize(); var6++) {
         ItemStack var7 = var5.getItem(var6);
         if (!var7.isEmpty()) {
            ResourceLocation var8 = BuiltInRegistries.ITEM.getKey(var7.getItem());
            int var9 = var2.sellPrices().getOrDefault(var8, 0);
            if (var9 > 0) {
               CompoundTag var10 = new CompoundTag();
               var10.putInt("Slot", var6);
               var10.putString("ItemId", var8.toString());
               var10.putInt("Count", var7.getCount());
               var10.putInt("Price", var9);
               var10.putString("Desc", var2.sellDescriptions().getOrDefault(var8, ""));
               var4.add(var10);
            }
         }
      }

      var3.put("SellInventory", var4);
      return var3;
   }

   public static MartService.Result buy(ServerPlayer var0, MartNpcEntity var1, int var2, int var3) {
      MartCatalog var4 = MartCatalog.read(var1.compatibilityData());
      if (var2 >= 0 && var2 < var4.offers().size()) {
         int var5 = Math.max(1, Math.min(64, var3));
         MartOffer var6 = var4.offers().get(var2);
         List<MartItemGrammar.Entry> var7 = MartItemGrammar.productEntries(var6.productKey());
         if (var7.isEmpty()) {
            return MartService.Result.fail("message.cobbleservertools.shop.invalid_product");
         }

         List<MartItemGrammar.CostGroup> var8 = MartItemGrammar.costGroups(var6.costKey(), var6.costCount());
         boolean var9 = !var8.isEmpty();
         LinkedHashMap<Item, Integer> var10 = new LinkedHashMap<>();
         if (var9) {
            List<MartCostPlanner.Choice> var21 = chooseCosts(var0.getInventory(), var8, var5);
            if (var21.size() != var8.size()) {
               return MartService.Result.fail("message.cobbleservertools.shop.not_enough_items");
            }

            for (MartCostPlanner.Choice var13 : var21) {
               ResourceLocation var14 = resolveItemIdentifier(MartItemGrammar.baseItemId(var13.entry().expression()));
               if (var14 == null) {
                  return MartService.Result.fail("message.cobbleservertools.shop.invalid_cost");
               }

               Item var15 = (Item)BuiltInRegistries.ITEM.get(var14);
               int var16 = safeMultiply(Math.max(1, var13.entry().count()), var5);
               if (var15 == Items.AIR || var16 <= 0) {
                  return MartService.Result.fail("message.cobbleservertools.shop.invalid_cost");
               }

               var10.merge(var15, var16, Integer::sum);
            }
         } else {
            int var11 = safeMultiply(Math.max(0, var6.price()), var5);
            if (var11 < 0) {
               return MartService.Result.fail("message.cobbleservertools.shop.invalid_quantity");
            }

            if (PlayerMoneyService.get(var0) < var11) {
               return MartService.Result.fail("message.cobbleservertools.buy_item.not_enough_money");
            }
         }

         ArrayList<ItemStack> var22 = new ArrayList<>();
         ArrayList<PokemonRewardEntry> var23 = new ArrayList<>();
         ArrayList<GiveCommandRewardEntry> var24 = new ArrayList<>();
         int var25 = 0;

         for (MartItemGrammar.Entry var28 : var7) {
            int var17 = safeMultiply(Math.max(1, var28.count()), var5);
            if (var17 <= 0) {
               return MartService.Result.fail("message.cobbleservertools.shop.invalid_quantity");
            }

            MartService.GiveCommandRewardEntry var18 = MartService.GiveCommandRewardEntry.parse(var28.expression(), var28.count());
            if (var18 != null) {
               var24.add(var18);
            } else {
               ResourceLocation var19 = resolveFirstIdentifier(var28.expression());
               if (var19 != null && BuiltInRegistries.ITEM.containsKey(var19)) {
                  Item var20 = (Item)BuiltInRegistries.ITEM.get(var19);
                  if (var20 != null && var20 != Items.AIR) {
                     if (POKE_BALL_ID.equals(var19)) {
                        var25 = safeAdd(var25, var17);
                     }

                     addItemRewardStacks(var22, var20, var17);
                     continue;
                  }
               }

               var23.add(new MartService.PokemonRewardEntry(var28.expression(), var17));
            }
         }

         boolean var27 = tryAddPremierBallBonus(var22, var25);
         if (!var22.isEmpty() && !canInsertAll(var0.getInventory(), var22, var9 ? var10 : Map.of())) {
            return MartService.Result.fail("message.cobbleservertools.buy_item.inventory_full");
         }

         for (MartService.PokemonRewardEntry var33 : var23) {
            if (!givePokemonToPlayer(var0, var33.spec(), var33.count())) {
               return MartService.Result.failLiteral("Party/PC is full.");
            }
         }

         if (var9) {
            removeItems(var0.getInventory(), var10);
         } else {
            int var30 = safeMultiply(Math.max(0, var6.price()), var5);
            if (var30 < 0 || !PlayerMoneyService.subtract(var0, var30)) {
               return MartService.Result.fail("message.cobbleservertools.buy_item.not_enough_money");
            }
         }

         for (ItemStack var34 : var22) {
            giveStack(var0, var34.copy());
         }

         for (MartService.GiveCommandRewardEntry var35 : var24) {
            executeGiveCommandReward(var0, var35, var5);
         }

         if (var27) {
            var0.displayClientMessage(Component.translatable("message.cobbleservertools.buy_item.premier_ball_bonus", new Object[0]), false);
         }

         return MartService.Result.ok("message.cobbleservertools.buy_item.success");
      } else {
         return MartService.Result.fail("message.cobbleservertools.shop.invalid_offer");
      }
   }

   public static MartService.Result sell(ServerPlayer var0, MartNpcEntity var1, int var2, int var3) {
      Inventory var4 = var0.getInventory();
      if (var2 >= 0 && var2 < var4.getContainerSize()) {
         ItemStack var5 = var4.getItem(var2);
         if (var5.isEmpty()) {
            return MartService.Result.fail("message.cobbleservertools.sell_item.not_found");
         }

         ResourceLocation var6 = BuiltInRegistries.ITEM.getKey(var5.getItem());
         int var7 = MartCatalog.read(var1.compatibilityData()).sellPrices().getOrDefault(var6, 0);
         if (var7 <= 0) {
            return MartService.Result.fail("message.cobbleservertools.shop.cannot_sell");
         }

         int var8 = Math.max(1, Math.min(Math.min(64, var3), var5.getCount()));
         int var9 = safeMultiply(var8, var7);
         if (var9 < 0) {
            return MartService.Result.fail("message.cobbleservertools.shop.invalid_quantity");
         }

         var5.shrink(var8);
         PlayerMoneyService.add(var0, var9);
         return MartService.Result.ok("message.cobbleservertools.sell_item.success");
      } else {
         return MartService.Result.fail("message.cobbleservertools.sell_item.not_found");
      }
   }

   private static List<MartCostPlanner.Choice> chooseCosts(Inventory var0, List<MartItemGrammar.CostGroup> var1, int var2) {
      HashMap<String, Integer> var3 = new HashMap<>();

      for (int var4 = 0; var4 < var0.getContainerSize(); var4++) {
         ItemStack var5 = var0.getItem(var4);
         if (!var5.isEmpty()) {
            ResourceLocation var6 = BuiltInRegistries.ITEM.getKey(var5.getItem());
            var3.merge(var6.toString(), var5.getCount(), Integer::sum);
         }
      }

      return MartCostPlanner.plan(var1, var3, var2, var0x -> {
         ResourceLocation var1x = resolveItemIdentifier(var0x);
         return var1x == null ? "" : var1x.toString();
      });
   }

   private static boolean tryAddPremierBallBonus(List<ItemStack> var0, int var1) {
      if (var1 < 10 || !PremierBallBonusConfig.isEnabled()) {
         return false;
      } else if (!BuiltInRegistries.ITEM.containsKey(PREMIER_BALL_ID)) {
         return false;
      } else {
         Item var2 = (Item)BuiltInRegistries.ITEM.get(PREMIER_BALL_ID);
         if (var2 != null && var2 != Items.AIR) {
            var0.add(new ItemStack(var2, 1));
            return true;
         } else {
            return false;
         }
      }
   }

   private static boolean givePokemonToPlayer(ServerPlayer var0, String var1, int var2) {
      if (var2 <= 0) {
         return true;
      }

      String var3 = normalizeMartPokemonSpec(var1);
      if (var3.isBlank()) {
         return false;
      }

      for (int var4 = 0; var4 < var2; var4++) {
         boolean var5 = ThreadLocalRandom.current().nextInt(8192) == 0;
         PokemonRewardUtil.Delivery var6 = PokemonRewardUtil.givePokemonSpecToPlayerWithResult(var0, var3, var5);
         if (!var6.success()) {
            return false;
         }

         Pokemon var7 = var6.pokemon();
         if (var5 && var7 != null) {
            ShinyNotificationUtil.broadcastMartPurchase(var0, var7);
            CobbleServerTools.LOGGER
               .info(
                  "[CobbleServerTools] MartNPC rolled shiny purchase: player={}, species={}, deliveredToPartyOrPc=true", new Object[]{var0.getScoreboardName(), var3}
               );
         }
      }

      return true;
   }

   private static String normalizeMartPokemonSpec(String var0) {
      String var1 = var0 == null ? "" : var0.trim();
      if (var1.isBlank()) {
         return "";
      }

      String[] var2 = var1.split("\\s+");
      if (var2.length == 0) {
         return "";
      }

      String var3 = var2[0].trim().toLowerCase(Locale.ROOT);
      if (var3.startsWith("pokemon:")) {
         var3 = var3.substring("pokemon:".length());
      } else if (var3.startsWith("poke:")) {
         var3 = var3.substring("poke:".length());
      } else if (var3.startsWith("cobblemon:")) {
         var3 = var3.substring("cobblemon:".length());
      } else if (var3.startsWith("cobblemon") && var3.length() > "cobblemon".length()) {
         var3 = var3.substring("cobblemon".length());
      }

      int var4 = var3.indexOf(58);
      if (var4 >= 0 && var4 < var3.length() - 1) {
         var3 = var3.substring(var4 + 1);
      }

      StringBuilder var5 = new StringBuilder(var3);

      for (int var6 = 1; var6 < var2.length; var6++) {
         var5.append(' ').append(var2[var6]);
      }

      return var5.toString();
   }

   private static void addItemRewardStacks(List<ItemStack> var0, Item var1, int var2) {
      int var3 = var2;

      while (var3 > 0) {
         int var4 = Math.max(1, new ItemStack(var1).getMaxStackSize());
         int var5 = Math.min(var3, var4);
         var0.add(new ItemStack(var1, var5));
         var3 -= var5;
      }
   }

   private static boolean canInsertAll(Inventory var0, List<ItemStack> var1, Map<Item, Integer> var2) {
      ArrayList<SimSlot> var3 = new ArrayList<>(var0.getContainerSize());

      for (int var4 = 0; var4 < var0.getContainerSize(); var4++) {
         ItemStack var5 = var0.getItem(var4);
         var3.add(var5.isEmpty() ? new MartService.SimSlot(null, 0, 0) : new MartService.SimSlot(var5.getItem(), var5.getCount(), var5.getMaxStackSize()));
      }

      for (Entry var14 : var2.entrySet()) {
         int var6 = (Integer)var14.getValue();

         for (MartService.SimSlot var8 : var3) {
            if (var6 <= 0) {
               break;
            }

            if (var8.item == var14.getKey() && var8.count > 0) {
               int var9 = Math.min(var6, var8.count);
               var8.count -= var9;
               var6 -= var9;
               if (var8.count == 0) {
                  var8.item = null;
                  var8.max = 0;
               }
            }
         }

         if (var6 > 0) {
            return false;
         }
      }

      for (ItemStack var15 : var1) {
         int var16 = var15.getCount();
         Item var17 = var15.getItem();
         int var18 = Math.max(1, var15.getMaxStackSize());

         for (MartService.SimSlot var10 : var3) {
            if (var16 <= 0) {
               break;
            }

            if (var10.item == var17 && var10.count < var10.max) {
               int var11 = Math.min(var16, var10.max - var10.count);
               var10.count += var11;
               var16 -= var11;
            }
         }

         for (MartService.SimSlot var21 : var3) {
            if (var16 <= 0) {
               break;
            }

            if (var21.item == null || var21.count <= 0) {
               int var22 = Math.min(var16, var18);
               var21.item = var17;
               var21.count = var22;
               var21.max = var18;
               var16 -= var22;
            }
         }

         if (var16 > 0) {
            return false;
         }
      }

      return true;
   }

   private static void removeItems(Inventory var0, Map<Item, Integer> var1) {
      for (Entry var3 : var1.entrySet()) {
         int var4 = (Integer)var3.getValue();

         for (int var5 = 0; var5 < var0.getContainerSize() && var4 > 0; var5++) {
            ItemStack var6 = var0.getItem(var5);
            if (!var6.isEmpty() && var6.is((Item)var3.getKey())) {
               int var7 = Math.min(var4, var6.getCount());
               var6.shrink(var7);
               var4 -= var7;
            }
         }
      }
   }

   private static void giveStack(ServerPlayer var0, ItemStack var1) {
      boolean var2 = var0.getInventory().add(var1);
      if (!var2 || !var1.isEmpty()) {
         var0.drop(var1, false);
      }
   }

   private static MartService.GiveCommandRewardEntry parseGiveCommandReward(String var0, int var1) {
      String var2 = var0 == null ? "" : var0.trim();
      if (var2.startsWith("/")) {
         var2 = var2.substring(1).trim();
      }

      List var3 = splitCommandTokens(var2);
      if (var3.size() >= 3) {
         String var4 = ((String)var3.get(0)).toLowerCase(Locale.ROOT);
         if (var4.equals("give") || var4.equals("minecraft:give")) {
            String var5 = ((String)var3.get(2)).trim();
            ResourceLocation var6 = resolveItemIdentifier(MartItemGrammar.baseItemId(var5));
            if (var6 != null && (Item)BuiltInRegistries.ITEM.get(var6) != Items.AIR) {
               int var7 = 1;
               if (var3.size() >= 4) {
                  try {
                     var7 = Math.max(1, Integer.parseInt((String)var3.get(3)));
                  } catch (NumberFormatException var9) {
                  }
               }

               int var8 = safeMultiply(var7, Math.max(1, var1));
               return var8 <= 0 ? null : new MartService.GiveCommandRewardEntry(var5, var8);
            } else {
               return null;
            }
         }
      }

      if (var0 != null && var0.indexOf(91) >= 0) {
         ResourceLocation var10 = resolveItemIdentifier(MartItemGrammar.baseItemId(var0));
         if (var10 != null && (Item)BuiltInRegistries.ITEM.get(var10) != Items.AIR) {
            return new MartService.GiveCommandRewardEntry(var0.trim(), Math.max(1, var1));
         }
      }

      return null;
   }

   private static List<String> splitCommandTokens(String var0) {
      ArrayList var1 = new ArrayList();
      if (var0 != null && !var0.isBlank()) {
         StringBuilder var2 = new StringBuilder();
         int var3 = 0;
         int var4 = 0;
         int var5 = 0;
         boolean var6 = false;
         boolean var7 = false;
         boolean var8 = false;

         for (int var9 = 0; var9 < var0.length(); var9++) {
            char var10 = var0.charAt(var9);
            if (var8) {
               var2.append(var10);
               var8 = false;
            } else if (var10 == '\\') {
               var2.append(var10);
               var8 = true;
            } else if (var10 == '\'' && !var7) {
               var6 = !var6;
               var2.append(var10);
            } else if (var10 == '"' && !var6) {
               var7 = !var7;
               var2.append(var10);
            } else {
               if (!var6 && !var7) {
                  if (var10 == '[') {
                     var3++;
                  } else if (var10 == ']') {
                     var3 = Math.max(0, var3 - 1);
                  } else if (var10 == '{') {
                     var4++;
                  } else if (var10 == '}') {
                     var4 = Math.max(0, var4 - 1);
                  } else if (var10 == '(') {
                     var5++;
                  } else if (var10 == ')') {
                     var5 = Math.max(0, var5 - 1);
                  }

                  if (Character.isWhitespace(var10) && var3 == 0 && var4 == 0 && var5 == 0) {
                     if (!var2.isEmpty()) {
                        var1.add(var2.toString());
                        var2.setLength(0);
                     }
                     continue;
                  }
               }

               var2.append(var10);
            }
         }

         if (!var2.isEmpty()) {
            var1.add(var2.toString());
         }

         return var1;
      } else {
         return var1;
      }
   }

   private static void executeGiveCommandReward(ServerPlayer var0, MartService.GiveCommandRewardEntry var1, int var2) {
      int var3 = safeMultiply(var1.count(), Math.max(1, var2));
      if (var3 > 0 && var0.getServer() != null) {
         String var4 = "give " + var0.getScoreboardName() + " " + var1.itemSpec() + " " + var3;
         var0.getServer().getCommands().performPrefixedCommand(var0.getServer().createCommandSourceStack().withSuppressedOutput().withPermission(4), var4);
      }
   }

   private static ResourceLocation resolveFirstIdentifier(String var0) {
      return resolveItemIdentifier(MartItemGrammar.baseItemId(var0));
   }

   private static ResourceLocation resolveItemIdentifier(String var0) {
      return NpcRewardFieldUtil.resolveItemIdentifier(var0);
   }

   private static int safeMultiply(int var0, int var1) {
      long var2 = (long)var0 * var1;
      return var2 <= 2147483647L && var2 >= 0L ? (int)var2 : -1;
   }

   private static int safeAdd(int var0, int var1) {
      long var2 = (long)var0 + var1;
      return var2 <= 2147483647L && var2 >= 0L ? (int)var2 : Integer.MAX_VALUE;
   }

   private record GiveCommandRewardEntry(String itemSpec, int count) {
      static MartService.GiveCommandRewardEntry parse(String var0, int var1) {
         return MartService.parseGiveCommandReward(var0, var1);
      }
   }

   private record PokemonRewardEntry(String spec, int count) {
   }

   public record Result(boolean success, String translationKey, boolean literal) {
      static MartService.Result ok(String var0) {
         return new MartService.Result(true, var0, false);
      }

      static MartService.Result fail(String var0) {
         return new MartService.Result(false, var0, false);
      }

      static MartService.Result failLiteral(String var0) {
         return new MartService.Result(false, var0, true);
      }
   }

   private static final class SimSlot {
      private Item item;
      private int count;
      private int max;

      private SimSlot(Item var1, int var2, int var3) {
         this.item = var1;
         this.count = var2;
         this.max = var3;
      }
   }
}
