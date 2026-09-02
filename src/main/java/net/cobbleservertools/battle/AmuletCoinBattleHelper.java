package net.cobbleservertools.battle;

import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import java.util.List;
import java.util.Set;
import net.cobbleservertools.registry.KRegistries;
import net.minecraft.world.item.ItemStack;

public final class AmuletCoinBattleHelper {
   private AmuletCoinBattleHelper() {
   }

   public static boolean playerHasParticipatingAmuletCoin(PlayerBattleActor var0) {
      if (var0 == null) {
         return false;
      }

      List<BattlePokemon> var1 = var0.getPokemonList();
      if (var1 != null && !var1.isEmpty()) {
         for (BattlePokemon var3 : var1) {
            if (var3 != null && hasAmuletCoin(var3) && participated(var0, var3)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static boolean hasAmuletCoin(BattlePokemon var0) {
      try {
         if (var0.getEffectedPokemon() == null) {
            return false;
         }

         ItemStack var1 = var0.getEffectedPokemon().heldItem();
         return var1 != null && !var1.isEmpty() && var1.getItem() == KRegistries.AMULET_COIN.get();
      } catch (RuntimeException var2) {
         return false;
      }
   }

   private static boolean participated(PlayerBattleActor var0, BattlePokemon var1) {
      try {
         if (var1.isSentOut()) {
            return true;
         }
      } catch (RuntimeException var7) {
      }

      try {
         Set var2 = var1.getFacedOpponents();
         if (var2 != null && !var2.isEmpty()) {
            return true;
         }
      } catch (RuntimeException var6) {
      }

      try {
         List<ActiveBattlePokemon> var9 = var0.getActivePokemon();
         if (var9 != null) {
            for (ActiveBattlePokemon var4 : var9) {
               BattlePokemon var5 = var4 == null ? null : var4.getBattlePokemon();
               if (var5 == var1 || var5 != null && var5.getUuid().equals(var1.getUuid())) {
                  return true;
               }
            }
         }
      } catch (RuntimeException var8) {
      }

      return false;
   }
}
