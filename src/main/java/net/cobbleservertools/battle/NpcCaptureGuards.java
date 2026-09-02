package net.cobbleservertools.battle;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.properties.UncatchableProperty;

public final class NpcCaptureGuards {
   private NpcCaptureGuards() {
   }

   public static void applyUncatchable(Pokemon var0) {
      if (var0 != null) {
         try {
            UncatchableProperty.INSTANCE.uncatchable().apply(var0);
         } catch (RuntimeException var2) {
         }
      }
   }

   public static void clearUncatchable(Iterable<Pokemon> var0) {
      if (var0 != null) {
         for (Pokemon var2 : var0) {
            if (var2 != null) {
               try {
                  UncatchableProperty.INSTANCE.catchable().apply(var2);
               } catch (RuntimeException var4) {
               }
            }
         }
      }
   }
}
