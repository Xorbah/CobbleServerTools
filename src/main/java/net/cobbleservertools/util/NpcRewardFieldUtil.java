package net.cobbleservertools.util;

import java.util.ArrayList;
import java.util.List;
import net.cobbleservertools.CobbleServerTools;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class NpcRewardFieldUtil {
   private NpcRewardFieldUtil() {
   }

   public static boolean isValidRewardField(String var0) {
      String var1 = var0 == null ? "" : var0.trim();
      if (var1.isEmpty()) {
         return true;
      }

      List<String> var2 = splitRewardList(var1);
      if (var2.isEmpty()) {
         return false;
      }

      for (String var4 : var2) {
         String var5 = var4 == null ? "" : var4.trim();
         if (var5.isEmpty()) {
            return false;
         }

         if (!isCommandLikeRewardToken(var5) && !PokemonRewardUtil.isPokemonSpec(var5)) {
            String var6 = var5.indexOf(91) >= 0 ? stripItemComponent(var5) : var5;
            if (var6.isEmpty() || resolveItemIdentifier(var6) == null) {
               return false;
            }
         }
      }

      return true;
   }

   public static boolean deliverRewardField(ServerPlayer var0, String var1, boolean var2) {
      if (var0 == null) {
         return false;
      }

      String var3 = var1 == null ? "" : var1.trim();
      if (var3.isEmpty()) {
         return false;
      }

      boolean var4 = false;

      for (String var6 : splitRewardList(var3)) {
         String var7 = var6 == null ? "" : var6.trim();
         if (!var7.isEmpty()) {
            if (isCommandLikeRewardToken(var7)) {
               runRewardCommand(var0, var7);
               var4 = true;
            } else if (var2 && PokemonRewardUtil.isPokemonSpec(var7)) {
               try {
                  if (PokemonRewardUtil.givePokemonSpecToPlayer(var0, var7)) {
                     var4 = true;
                  }
               } catch (Throwable var9) {
               }
            } else if (giveItemReward(var0, var7)) {
               var4 = true;
            }
         }
      }

      return var4;
   }

   public static String firstResolvedItemId(String var0) {
      for (String var2 : splitRewardList(var0)) {
         String var3 = var2 == null ? "" : var2.trim();
         if (!var3.isEmpty() && !isCommandLikeRewardToken(var3) && !PokemonRewardUtil.isPokemonSpec(var3)) {
            ResourceLocation var4 = resolveItemIdentifier(var3.indexOf(91) >= 0 ? stripItemComponent(var3) : var3);
            if (var4 != null) {
               return var4.toString();
            }
         }
      }

      return "";
   }

   public static List<String> splitRewardList(String var0) {
      String var1 = var0 == null ? "" : var0.trim();
      if (var1.isEmpty()) {
         return List.of();
      }

      ArrayList var2 = new ArrayList();
      StringBuilder var3 = new StringBuilder();
      int var4 = 0;
      int var5 = 0;
      boolean var6 = false;
      boolean var7 = false;
      boolean var8 = false;

      for (int var9 = 0; var9 < var1.length(); var9++) {
         char var10 = var1.charAt(var9);
         if (var8) {
            var3.append(var10);
            var8 = false;
         } else if (var10 == '\\') {
            var3.append(var10);
            var8 = true;
         } else if (var10 == '\'' && !var7) {
            var6 = !var6;
            var3.append(var10);
         } else if (var10 == '"' && !var6) {
            var7 = !var7;
            var3.append(var10);
         } else {
            if (!var6 && !var7) {
               if (var10 == '[') {
                  var4++;
               } else if (var10 == ']') {
                  var4 = Math.max(0, var4 - 1);
               } else if (var10 == '{') {
                  var5++;
               } else if (var10 == '}') {
                  var5 = Math.max(0, var5 - 1);
               }

               if (var10 == ',' && var4 == 0 && var5 == 0) {
                  String var11 = var3.toString().trim();
                  if (!var11.isEmpty()) {
                     var2.add(var11);
                  }

                  var3.setLength(0);
                  continue;
               }
            }

            var3.append(var10);
         }
      }

      String var12 = var3.toString().trim();
      if (!var12.isEmpty()) {
         var2.add(var12);
      }

      return var2;
   }

   public static void runConfiguredRewardCommand(ServerPlayer var0, String var1) {
      if (var0 != null && var1 != null && !var1.isBlank()) {
         for (String var3 : splitRewardList(var1)) {
            String var4 = var3 == null ? "" : var3.trim();
            if (var4.startsWith("/")) {
               var4 = var4.substring(1).trim();
            }

            if (!var4.isBlank()) {
               runServerCommand(var0, "execute as " + var0.getScoreboardName() + " at @s run " + var4);
            }
         }
      }
   }

   public static boolean isCommandLikeRewardToken(String var0) {
      String var1 = var0 == null ? "" : var0.trim();
      if (var1.isEmpty()) {
         return false;
      }

      if (var1.startsWith("/")) {
         return true;
      }

      if (PokemonRewardUtil.isPokemonSpec(var1)) {
         return false;
      }

      int var2 = firstWhitespaceIndex(var1);
      if (var2 < 0) {
         return false;
      }

      String var3 = stripItemComponent(var1.substring(0, var2).trim());
      return resolveItemIdentifier(var3) == null;
   }

   public static ResourceLocation resolveItemIdentifier(String var0) {
      if (var0 == null) {
         return null;
      }

      String var1 = stripItemComponent(var0.trim());
      int var2 = firstWhitespaceIndex(var1);
      if (var2 >= 0) {
         var1 = var1.substring(0, var2);
      }

      if (var1.isEmpty()) {
         return null;
      }

      if (var1.indexOf(58) < 0) {
         ArrayList<ResourceLocation> var6 = new ArrayList<>();

         for (ResourceLocation var5 : BuiltInRegistries.ITEM.keySet()) {
            if (var5.getPath().equals(var1)) {
               var6.add(var5);
            }
         }

         if (var6.isEmpty()) {
            return null;
         }

         for (ResourceLocation var9 : var6) {
            if (var9.getNamespace().equals("minecraft")) {
               return var9;
            }
         }

         for (ResourceLocation var10 : var6) {
            if (var10.getNamespace().equals("cobblemon")) {
               return var10;
            }
         }

         return (ResourceLocation)var6.get(0);
      } else {
         ResourceLocation var3 = ResourceLocation.tryParse(var1);
         return var3 != null && BuiltInRegistries.ITEM.containsKey(var3) ? var3 : null;
      }
   }

   private static boolean giveItemReward(ServerPlayer var0, String var1) {
      String var2 = var1 == null ? "" : var1.trim();
      if (var2.isEmpty()) {
         return false;
      }

      try {
         if (var2.indexOf(91) >= 0) {
            runServerCommand(var0, "give " + var0.getScoreboardName() + " " + var2);
            return true;
         }

         ResourceLocation var3 = resolveItemIdentifier(var2);
         if (var3 == null) {
            return false;
         }

         Item var4 = (Item)BuiltInRegistries.ITEM.get(var3);
         if (var4 != null && var4 != Items.AIR) {
            ItemStack var5 = new ItemStack(var4, 1);
            boolean var6 = var0.getInventory().add(var5);
            if (!var6 || !var5.isEmpty()) {
               var0.drop(var5, false);
            }

            return true;
         } else {
            return false;
         }
      } catch (Throwable var7) {
         CobbleServerTools.LOGGER.warn("Failed to deliver reward item to {}: {}", new Object[]{var0.getScoreboardName(), var7.getMessage()});
         return false;
      }
   }

   private static void runRewardCommand(ServerPlayer var0, String var1) {
      String var2 = var1 == null ? "" : var1.trim();
      if (var2.startsWith("/")) {
         var2 = var2.substring(1).trim();
      }

      if (!var2.isEmpty()) {
         runServerCommand(var0, "execute as " + var0.getScoreboardName() + " at @s run " + var2);
      }
   }

   private static void runServerCommand(ServerPlayer var0, String var1) {
      if (var0.getServer() != null) {
         var0.getServer().getCommands().performPrefixedCommand(var0.getServer().createCommandSourceStack().withSuppressedOutput().withPermission(4), var1);
      }
   }

   private static String stripItemComponent(String var0) {
      if (var0 == null) {
         return "";
      }

      int var1 = var0.indexOf(91);
      return (var1 >= 0 ? var0.substring(0, var1) : var0).trim();
   }

   private static int firstWhitespaceIndex(String var0) {
      for (int var1 = 0; var1 < var0.length(); var1++) {
         if (Character.isWhitespace(var0.charAt(var1))) {
            return var1;
         }
      }

      return -1;
   }
}
