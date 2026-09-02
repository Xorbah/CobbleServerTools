package net.cobbleservertools.util;

import com.cobblemon.mod.common.pokemon.Pokemon;
import java.util.Locale;
import java.util.Map;
import net.cobbleservertools.config.ShinyNotificationConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

public final class ShinyNotificationUtil {
   private static final Map<String, String> SPECIAL_SPECIES_NAMES = Map.ofEntries(
      Map.entry("mr_mime", "Mr. Mime"),
      Map.entry("farfetchd", "Farfetch'd"),
      Map.entry("sirfetchd", "Sirfetch'd"),
      Map.entry("mr_rime", "Mr. Rime"),
      Map.entry("mime_jr", "Mime Jr."),
      Map.entry("ho_oh", "Ho-Oh"),
      Map.entry("porygon_z", "Porygon-Z"),
      Map.entry("nidoran_f", "Nidoran♀"),
      Map.entry("nidoran_m", "Nidoran♂"),
      Map.entry("flabebe", "Flabébé"),
      Map.entry("type_null", "Type: Null"),
      Map.entry("jangmo_o", "Jangmo-o"),
      Map.entry("hakamo_o", "Hakamo-o"),
      Map.entry("kommo_o", "Kommo-o"),
      Map.entry("wo_chien", "Wo-Chien"),
      Map.entry("chien_pao", "Chien-Pao"),
      Map.entry("ting_lu", "Ting-Lu"),
      Map.entry("chi_yu", "Chi-Yu")
   );

   private ShinyNotificationUtil() {
   }

   public static void broadcastTraderTrade(ServerPlayer var0, Pokemon var1) {
      broadcast(var0, var1, "message.cobbleservertools.shiny_trader_trade");
   }

   public static void broadcastMartPurchase(ServerPlayer var0, Pokemon var1) {
      broadcast(var0, var1, "message.cobbleservertools.shiny_mart_purchase");
   }

   private static void broadcast(ServerPlayer var0, Pokemon var1, String var2) {
      if (var0 != null && var1 != null && var0.getServer() != null && ShinyNotificationConfig.isEnabled()) {
         MutableComponent var3 = Component.literal(var0.getScoreboardName()).withStyle(ChatFormatting.GREEN);
         MutableComponent var4 = Component.literal(resolveSpeciesName(var1).toUpperCase(Locale.ROOT)).withStyle(ChatFormatting.LIGHT_PURPLE);
         MutableComponent var5 = Component.translatable("message.cobbleservertools.shiny_word", new Object[0]).withStyle(ChatFormatting.LIGHT_PURPLE);
         MutableComponent var6 = Component.translatable(var2, new Object[]{var3, var4, var5}).withStyle(ChatFormatting.GRAY);

         for (ServerPlayer var8 : var0.getServer().getPlayerList().getPlayers()) {
            var8.displayClientMessage(var6, false);
         }
      }
   }

   private static String resolveSpeciesName(Pokemon var0) {
      String var1 = "";

      try {
         if (var0.getSpecies() != null && var0.getSpecies().getName() != null) {
            var1 = var0.getSpecies().getName();
         }
      } catch (Throwable var9) {
      }

      if (var1 != null && !var1.isBlank()) {
         String var2 = var1.trim().toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
         String var3 = SPECIAL_SPECIES_NAMES.get(var2);
         if (var3 != null) {
            return var3;
         }

         StringBuilder var4 = new StringBuilder();

         for (String var8 : var2.split("_")) {
            if (!var8.isBlank()) {
               if (!var4.isEmpty()) {
                  var4.append(' ');
               }

               var4.append(Character.toUpperCase(var8.charAt(0)));
               if (var8.length() > 1) {
                  var4.append(var8.substring(1));
               }
            }
         }

         return var4.isEmpty() ? "Pokémon" : var4.toString();
      } else {
         return "Pokémon";
      }
   }
}
