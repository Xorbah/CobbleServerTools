package net.cobbleservertools.battle;

import net.cobbleservertools.CobbleServerTools;
import net.cobbleservertools.entity.AbstractBattleNpcEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class NpcRewardService {
   private NpcRewardService() {
   }

   public static void award(ServerPlayer var0, AbstractBattleNpcEntity var1, boolean var2, boolean var3) {
      boolean var4 = var1.rewardResetAlways();
      if (var2 || var4) {
         giveItems(var0, var1.rewardItemId());
         int var5 = Math.max(0, var1.moneyReward());
         int var6 = var3 ? (var5 > 1073741823 ? Integer.MAX_VALUE : var5 * 2) : var5;
         if (var6 > 0) {
            PlayerMoneyService.add(var0, var6);
            String var7 = var3 ? "message.cobbleservertools.battle.money_reward_received_amulet_coin" : "message.cobbleservertools.battle.money_reward_received";
            var0.displayClientMessage(Component.translatable(var7, new Object[]{var6}), false);
         }
      }

      runVictoryCommands(var0, var1.onVictoryCommand());
   }

   private static void giveItems(ServerPlayer var0, String var1) {
      if (var1 != null && !var1.isBlank()) {
         String var2 = var1.trim();

         try {
            if (var2.indexOf(91) >= 0) {
               runServerCommand(var0, "give " + var0.getScoreboardName() + " " + var2);
               return;
            }

            for (String var6 : var2.split(",")) {
               giveOne(var0, var6.trim());
            }
         } catch (RuntimeException var7) {
            CobbleServerTools.LOGGER.warn("Failed to deliver NPC item reward to {}", new Object[]{var0.getScoreboardName(), var7});
         }
      }
   }

   private static void giveOne(ServerPlayer var0, String var1) {
      if (!var1.isBlank()) {
         String[] var2 = var1.split("\\s+", 2);
         ResourceLocation var3 = resolveItemIdentifier(var2[0]);
         if (var3 == null) {
            CobbleServerTools.LOGGER.warn("Ignoring invalid NPC reward item '{}'", new Object[]{var2[0]});
         } else {
            Item var4 = (Item)BuiltInRegistries.ITEM.get(var3);
            if (var4 != null && var4 != Items.AIR) {
               int var5 = 1;
               if (var2.length == 2) {
                  try {
                     var5 = Math.max(1, Math.min(64, Integer.parseInt(var2[1])));
                  } catch (NumberFormatException var7) {
                     var5 = 1;
                  }
               }

               ItemStack var6 = new ItemStack(var4, var5);
               if (!var0.getInventory().add(var6)) {
                  var0.drop(var6, false);
               }
            }
         }
      }
   }

   private static ResourceLocation resolveItemIdentifier(String var0) {
      if (var0 != null && !var0.isBlank()) {
         String var1 = var0.trim();
         if (var1.indexOf(58) < 0) {
            for (ResourceLocation var3 : BuiltInRegistries.ITEM.keySet()) {
               if (var3.getPath().equals(var1)) {
                  return var3;
               }
            }

            return null;
         } else {
            ResourceLocation var2 = ResourceLocation.tryParse(var1);
            return var2 != null && BuiltInRegistries.ITEM.containsKey(var2) ? var2 : null;
         }
      } else {
         return null;
      }
   }

   private static void runVictoryCommands(ServerPlayer var0, String var1) {
      if (var1 != null && !var1.isBlank()) {
         for (String var5 : var1.split(",")) {
            String var6 = var5 == null ? "" : var5.trim();
            if (var6.startsWith("/")) {
               var6 = var6.substring(1).trim();
            }

            if (!var6.isBlank()) {
               try {
                  runServerCommand(var0, "execute as " + var0.getScoreboardName() + " at @s run " + var6);
               } catch (RuntimeException var8) {
                  CobbleServerTools.LOGGER.warn("Failed to execute NPC victory command for {}", new Object[]{var0.getScoreboardName(), var8});
               }
            }
         }
      }
   }

   private static void runServerCommand(ServerPlayer var0, String var1) {
      var0.getServer().getCommands().performPrefixedCommand(var0.getServer().createCommandSourceStack().withSuppressedOutput().withPermission(4), var1);
   }
}
