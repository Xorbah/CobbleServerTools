package net.cobbleservertools.commerce;

import com.cobblemon.mod.common.pokemon.Pokemon;
import java.lang.reflect.Method;
import net.cobbleservertools.battle.CobblemonPartyService;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;

public final class PokemonPartySnapshot {
   private PokemonPartySnapshot() {
   }

   public static ListTag create(ServerPlayer var0) {
      ListTag var1 = new ListTag();
      int var2 = 0;

      for (Pokemon var4 : CobblemonPartyService.partySlots(var0)) {
         if (var4 != null) {
            CompoundTag var5 = new CompoundTag();
            var5.putInt("Slot", var2);
            var5.putString("Species", speciesName(var4));
            var5.putInt("Level", var4.getLevel());
            var5.putString("Gender", value(invoke(var4, "getGender")));
            var5.putBoolean("Shiny", Boolean.TRUE.equals(invoke(var4, "getShiny")));
            var1.add(var5);
         }

         var2++;
      }

      return var1;
   }

   private static String speciesName(Pokemon var0) {
      Object var1 = invoke(var0, "getSpecies");
      if (var1 == null) {
         return "Unknown";
      }

      Object var2 = invoke(var1, "getName");
      return var2 == null ? var1.toString() : var2.toString();
   }

   private static Object invoke(Object var0, String var1) {
      try {
         Method var2 = var0.getClass().getMethod(var1);
         return var2.invoke(var0);
      } catch (ReflectiveOperationException | RuntimeException var3) {
         return null;
      }
   }

   private static String value(Object var0) {
      return var0 == null ? "" : var0.toString();
   }
}
