package net.cobbleservertools.battle;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.pokemon.Pokemon;
import java.util.ArrayList;
import java.util.List;
import kotlin.Unit;
import net.minecraft.server.level.ServerPlayer;

public final class CobblemonPartyService {
   private CobblemonPartyService() {
   }

   public static PlayerPartyStore store(ServerPlayer var0) {
      try {
         return Cobblemon.INSTANCE.getStorage().getParty(var0);
      } catch (RuntimeException var2) {
         return null;
      }
   }

   public static List<Pokemon> partySlots(ServerPlayer var0) {
      ArrayList var1 = new ArrayList(6);
      PlayerPartyStore var2 = store(var0);

      for (int var3 = 0; var3 < 6; var3++) {
         var1.add(var2 == null ? null : var2.get(var3));
      }

      return var1;
   }

   public static List<Pokemon> party(ServerPlayer var0) {
      ArrayList var1 = new ArrayList(6);

      try {
         PlayerPartyStore var2 = Cobblemon.INSTANCE.getStorage().getParty(var0);

         for (int var3 = 0; var3 < var2.size(); var3++) {
            Pokemon var4 = var2.get(var3);
            if (var4 != null) {
               var1.add(var4);
            }
         }
      } catch (RuntimeException var5) {
      }

      return var1;
   }

   public static List<BattlePokemon> healthyBattleParty(ServerPlayer var0) {
      return wrapHealthy(party(var0));
   }

   public static int highestPartyLevel(ServerPlayer var0) {
      int var1 = 1;

      for (Pokemon var3 : party(var0)) {
         if (var3 != null) {
            var1 = Math.max(var1, var3.getLevel());
         }
      }

      return var1;
   }

   public static List<BattlePokemon> wrapHealthy(List<Pokemon> var0) {
      ArrayList var1 = new ArrayList(var0.size());

      for (Pokemon var3 : var0) {
         if (var3 != null && var3.getCurrentHealth() > 0) {
            var1.add(new BattlePokemon(var3, var3, var0x -> Unit.INSTANCE));
         }
      }

      return var1;
   }

   public static int healParty(ServerPlayer var0) {
      int var1 = 0;

      for (Pokemon var3 : party(var0)) {
         if (var3 != null) {
            var3.heal();
            var1++;
         }
      }

      return var1;
   }
}
