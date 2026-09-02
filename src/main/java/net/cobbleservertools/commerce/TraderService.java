package net.cobbleservertools.commerce;

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import net.cobbleservertools.battle.CobblemonPartyService;
import net.cobbleservertools.entity.TraderNpcEntity;
import net.cobbleservertools.util.NpcRewardFieldUtil;
import net.cobbleservertools.util.ShinyNotificationUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class TraderService {
   private static final int COBBLESERVERTOOLS_SHINY_ODDS = 8192;

   private TraderService() {
   }

   public static CompoundTag clientView(ServerPlayer var0, TraderNpcEntity var1) {
      CompoundTag var2 = var1.compatibilityData();
      CompoundTag var3 = new CompoundTag();
      var3.putString("RequestSpecies", get(var2, "tradeRequestId", "requestSpecies", "requestId", "RequestedPokemon"));
      var3.putString("OfferSpecies", get(var2, "tradeOfferId", "offerSpecies", "offerId", "OfferedPokemon"));
      var3.putInt("OfferLevel", positive(var2, "tradeOfferLevel", "offerLevel", "level", 10));
      var3.putString("OfferGender", getOr(var2, "random", "tradeOfferGender", "offerGender", "gender"));
      boolean var4 = bool(var2, "unlimitedTrade", "UnlimitedTrades");
      var3.putBoolean("Unlimited", var4);
      var3.putBoolean("AlreadyTraded", !var4 && readUuidSet(var2, "tradedPlayers", "TradedPlayers").contains(var0.getUUID()));
      var3.put("Party", PokemonPartySnapshot.create(var0));
      return var3;
   }

   public static TraderService.Result trade(ServerPlayer var0, TraderNpcEntity var1, int var2) {
      CompoundTag var3 = var1.compatibilityData();
      boolean var4 = bool(var3, "unlimitedTrade", "UnlimitedTrades");
      Set var5 = readUuidSet(var3, "tradedPlayers", "TradedPlayers");
      if (!var4 && var5.contains(var0.getUUID())) {
         return TraderService.Result.fail("cobbleservertools.trader.you_have_already_traded_with_npc");
      }

      String var6 = normalize(get(var3, "tradeRequestId", "requestSpecies", "requestId", "RequestedPokemon"));
      String var7 = normalize(get(var3, "tradeOfferId", "offerSpecies", "offerId", "OfferedPokemon"));
      if (!var6.isBlank() && !var7.isBlank()) {
         PlayerPartyStore var8 = CobblemonPartyService.store(var0);
         if (var8 != null && var2 >= 0 && var2 < 6) {
            Pokemon var9 = var8.get(var2);
            if (var9 != null && matches(var9, var6)) {
               Species var10 = PokemonSpecies.getByName(var7);
               if (var10 == null) {
                  return TraderService.Result.fail("cobbleservertools.trader.invalid_species");
               }

               int var11 = Math.max(1, Math.min(100, positive(var3, "tradeOfferLevel", "offerLevel", "level", 10)));
               Pokemon var12 = var10.create(var11);
               applyGender(var12, getOr(var3, "random", "tradeOfferGender", "offerGender", "gender"));
               boolean var13 = ThreadLocalRandom.current().nextInt(8192) == 0;
               var12.setShiny(var13);
               var12.heal();
               var8.set(var2, var12);
               if (var13) {
                  ShinyNotificationUtil.broadcastTraderTrade(var0, var12);
               }

               if (!var4) {
                  var5.add(var0.getUUID());
                  writeUuidSet(var3, "tradedPlayers", var5);
                  writeUuidSet(var3, "TradedPlayers", var5);
               }

               UUID var14 = var0.getUUID();
               boolean var15 = var1.rewardResetAlways();
               Set var16 = readUuidSet(var3, "rewardedPlayers", "RewardedPlayers");
               if (var15 || !var16.contains(var14)) {
                  NpcRewardFieldUtil.deliverRewardField(var0, var1.rewardItemId(), false);
                  NpcRewardFieldUtil.runConfiguredRewardCommand(var0, var1.onVictoryCommand());
                  if (!var15) {
                     var16.add(var14);
                  }
               }

               writeUuidSet(var3, "rewardedPlayers", var16);
               writeUuidSet(var3, "RewardedPlayers", var16);
               var1.replaceCompatibilityData(var3);
               return TraderService.Result.ok("message.cobbleservertools.trade.success");
            } else {
               return TraderService.Result.fail("cobbleservertools.trader.selected_cobblemon_not_required");
            }
         } else {
            return TraderService.Result.fail("cobbleservertools.trader.selected_cobblemon_not_required");
         }
      } else {
         return TraderService.Result.fail("cobbleservertools.trader.trader_has_no_species_configured");
      }
   }

   private static boolean matches(Pokemon var0, String var1) {
      Object var2 = invoke(var0, "getSpecies");
      if (var2 == null) {
         return false;
      }

      Object var3 = invoke(var2, "getName");
      return normalize(var3 == null ? var2.toString() : var3.toString()).equals(var1);
   }

   private static void applyGender(Pokemon var0, String var1) {
      String var2 = var1 == null ? "random" : var1.toLowerCase(Locale.ROOT);
      if (var2.equals("male")) {
         var0.setGender(Gender.MALE);
      } else if (var2.equals("female")) {
         var0.setGender(Gender.FEMALE);
      } else if (var2.equals("genderless") || var2.equals("none")) {
         var0.setGender(Gender.GENDERLESS);
      }
   }

   private static Object invoke(Object var0, String var1) {
      try {
         return var0.getClass().getMethod(var1).invoke(var0);
      } catch (ReflectiveOperationException | RuntimeException var3) {
         return null;
      }
   }

   private static String normalize(String var0) {
      String var1 = var0 == null ? "" : var0.trim().toLowerCase(Locale.ROOT);
      int var2 = var1.indexOf(58);
      if (var2 >= 0) {
         var1 = var1.substring(var2 + 1);
      }

      return var1.replace("-", "").replace("_", "").replace(" ", "");
   }

   private static String get(CompoundTag var0, String... var1) {
      for (String var5 : var1) {
         if (var0.contains(var5) && !var0.getString(var5).isBlank()) {
            return var0.getString(var5);
         }
      }

      return "";
   }

   private static String getOr(CompoundTag var0, String var1, String... var2) {
      String var3 = get(var0, var2);
      return var3.isBlank() ? var1 : var3;
   }

   private static int positive(CompoundTag var0, String var1, String var2, String var3, int var4) {
      for (String var8 : new String[]{var1, var2, var3}) {
         if (var0.contains(var8) && var0.getInt(var8) > 0) {
            return var0.getInt(var8);
         }
      }

      return var4;
   }

   private static boolean bool(CompoundTag var0, String... var1) {
      for (String var5 : var1) {
         if (var0.contains(var5)) {
            return var0.getBoolean(var5);
         }
      }

      return false;
   }

   private static Set<UUID> readUuidSet(CompoundTag var0, String... var1) {
      HashSet var2 = new HashSet();

      for (String var6 : var1) {
         ListTag var7 = var0.getList(var6, 8);

         for (int var8 = 0; var8 < var7.size(); var8++) {
            try {
               var2.add(UUID.fromString(var7.getString(var8)));
            } catch (IllegalArgumentException var10) {
            }
         }
      }

      return var2;
   }

   private static void writeUuidSet(CompoundTag var0, String var1, Set<UUID> var2) {
      ListTag var3 = new ListTag();

      for (UUID var5 : var2) {
         var3.add(StringTag.valueOf(var5.toString()));
      }

      var0.put(var1, var3);
   }

   public record Result(boolean success, String translationKey) {
      static TraderService.Result ok(String var0) {
         return new TraderService.Result(true, var0);
      }

      static TraderService.Result fail(String var0) {
         return new TraderService.Result(false, var0);
      }

      public Component message() {
         return Component.translatable(this.translationKey, new Object[0]);
      }
   }
}
