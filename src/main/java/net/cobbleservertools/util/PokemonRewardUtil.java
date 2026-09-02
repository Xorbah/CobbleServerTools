package net.cobbleservertools.util;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.storage.PokemonStoreManager;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Optional;
import net.cobbleservertools.battle.CobblemonPartyService;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class PokemonRewardUtil {
   private PokemonRewardUtil() {
   }

   public static boolean isPokemonSpec(String var0) {
      PokemonRewardUtil.ParsedPokemonSpec var1 = PokemonRewardUtil.ParsedPokemonSpec.parse(var0);
      if (var1 != null && !var1.speciesKey.isEmpty()) {
         try {
            return PokemonSpecies.getByName(var1.speciesKey) != null;
         } catch (Throwable var3) {
            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean givePokemonSpecToPlayer(ServerPlayer var0, String var1) {
      return givePokemonSpecToPlayerWithResult(var0, var1, false).success();
   }

   public static boolean givePokemonSpecToPlayer(ServerPlayer var0, String var1, boolean var2) {
      return givePokemonSpecToPlayerWithResult(var0, var1, var2).success();
   }

   public static PokemonRewardUtil.Delivery givePokemonSpecToPlayerWithResult(ServerPlayer var0, String var1, boolean var2) {
      if (var0 == null) {
         return new PokemonRewardUtil.Delivery(false, null);
      }

      PokemonRewardUtil.ParsedPokemonSpec var3 = PokemonRewardUtil.ParsedPokemonSpec.parse(var1);
      if (var3 != null && !var3.speciesKey.isEmpty()) {
         Species var4;
         try {
            var4 = PokemonSpecies.getByName(var3.speciesKey);
         } catch (Throwable var10) {
            var4 = null;
         }

         if (var4 == null) {
            return new PokemonRewardUtil.Delivery(false, null);
         }

         Pokemon var5 = createPokemonFromSpecies(var4, var3.level, var3.gender);
         if (var5 == null) {
            return new PokemonRewardUtil.Delivery(false, null);
         }

         try {
            var5.setShiny(var2);
         } catch (Throwable var9) {
         }

         boolean var6 = tryAddToPartyOrPc(var0, var5);
         if (var6) {
            try {
               var5.setShiny(var2);
            } catch (Throwable var8) {
            }
         }

         return new PokemonRewardUtil.Delivery(var6, var6 ? var5 : null);
      } else {
         return new PokemonRewardUtil.Delivery(false, null);
      }
   }

   private static Pokemon createPokemonFromSpecies(Species var0, int var1, String var2) {
      if (var0 == null) {
         return null;
      }

      int var3 = Math.max(1, Math.min(100, var1));

      Pokemon var4;
      try {
         var4 = var0.create(var3);
      } catch (Throwable var12) {
         var4 = null;
      }

      if (var4 == null) {
         try {
            var4 = new Pokemon();
            var4.setSpecies(var0);
            var4.setLevel(var3);
         } catch (Throwable var11) {
            return null;
         }
      } else {
         try {
            var4.setLevel(var3);
         } catch (Throwable var10) {
         }
      }

      Gender var5 = explicitGender(var2);
      if (var5 != null) {
         try {
            var4.setGender(var5);
         } catch (Throwable var9) {
         }
      }

      try {
         var4.setShiny(false);
      } catch (Throwable var8) {
      }

      try {
         var4.heal();
      } catch (Throwable var7) {
      }

      bestEffortInitPokemonDefaults(var4);
      return var4;
   }

   private static Gender explicitGender(String var0) {
      String var1 = var0 == null ? "" : var0.trim().toLowerCase(Locale.ROOT);

      return switch (var1) {
         case "male", "m" -> Gender.MALE;
         case "female", "f" -> Gender.FEMALE;
         case "genderless", "none" -> Gender.GENDERLESS;
         default -> null;
      };
   }

   private static void bestEffortInitPokemonDefaults(Pokemon var0) {
      for (String var4 : new String[]{
         "initialize",
         "initialise",
         "initializeMoveset",
         "initialiseMoveset",
         "initializeMoves",
         "initialiseMoves",
         "recalculateMoves",
         "recalculateMoveset",
         "refreshMoves",
         "refreshAbility",
         "refresh"
      }) {
         try {
            var0.getClass().getMethod(var4).invoke(var0);
         } catch (ReflectiveOperationException | RuntimeException var6) {
         }
      }
   }

   private static boolean tryAddToPartyOrPc(ServerPlayer var0, Pokemon var1) {
      PlayerPartyStore var2 = CobblemonPartyService.store(var0);
      if (tryAddPokemonToStore(var2, var1)) {
         return true;
      }

      try {
         PokemonStoreManager var3 = Cobblemon.INSTANCE.getStorage();
         Object var4 = null;

         for (String var8 : new String[]{"getPC", "getPc"}) {
            try {
               var4 = var3.getClass().getMethod(var8, ServerPlayer.class).invoke(var3, var0);
               break;
            } catch (ReflectiveOperationException var10) {
            }
         }

         return tryAddPokemonToStore(var4, var1);
      } catch (RuntimeException var11) {
         return false;
      }
   }

   private static boolean tryAddPokemonToStore(Object var0, Pokemon var1) {
      if (var0 != null && var1 != null) {
         for (String var5 : new String[]{"add", "addPokemon", "addToParty", "addToPC", "store", "storePokemon"}) {
            if (invokeAddLike(var0, var5, var1)) {
               return true;
            }
         }

         try {
            Method var12 = var0.getClass().getMethod("size");
            Method var13 = var0.getClass().getMethod("get", int.class);
            if (!(var12.invoke(var0) instanceof Number var15)) {
               return false;
            }

            Method var6 = null;

            for (Method var10 : var0.getClass().getMethods()) {
               if (var10.getName().equals("set")
                  && var10.getParameterCount() == 2
                  && var10.getParameterTypes()[0] == int.class
                  && var10.getParameterTypes()[1].isAssignableFrom(var1.getClass())) {
                  var6 = var10;
                  break;
               }
            }

            if (var6 == null) {
               return false;
            }

            int var16 = Math.max(0, var15.intValue());

            for (int var17 = 0; var17 < var16; var17++) {
               Object var18 = var13.invoke(var0, var17);
               if (isPokemonSlotEmpty(var18)) {
                  var6.invoke(var0, var17, var1);
                  return true;
               }
            }
         } catch (ReflectiveOperationException | RuntimeException var11) {
         }

         return false;
      } else {
         return false;
      }
   }

   private static boolean invokeAddLike(Object var0, String var1, Pokemon var2) {
      for (Method var6 : var0.getClass().getMethods()) {
         if (var6.getName().equals(var1) && var6.getParameterCount() == 1) {
            Class var7 = var6.getParameterTypes()[0];
            if (var7.isAssignableFrom(var2.getClass())) {
               try {
                  return !(var6.invoke(var0, var2) instanceof Boolean var9 && !var9);
               } catch (ReflectiveOperationException | RuntimeException var10) {
               }
            }
         }
      }

      return false;
   }

   private static boolean isPokemonSlotEmpty(Object var0) {
      if (var0 == null) {
         return true;
      } else if (var0 instanceof Optional var4) {
         return var4.isEmpty();
      } else {
         try {
            if (var0.getClass().getMethod("isEmpty").invoke(var0) instanceof Boolean var2) {
               return var2;
            }
         } catch (ReflectiveOperationException | RuntimeException var3) {
         }

         return false;
      }
   }

   private static String normalizeSpeciesKey(String var0) {
      if (var0 == null) {
         return "";
      }

      String var1 = var0.trim().toLowerCase(Locale.ROOT);
      int var2 = var1.indexOf(58);
      if (var2 >= 0) {
         var1 = var1.substring(var2 + 1);
      }

      return var1.replace("-", "").replace("_", "").replace(" ", "");
   }

   public record Delivery(boolean success, Pokemon pokemon) {
   }

   static final class ParsedPokemonSpec {
      final String speciesKey;
      final int level;
      final String gender;

      private ParsedPokemonSpec(String var1, int var2, String var3) {
         this.speciesKey = var1;
         this.level = var2;
         this.gender = var3;
      }

      static PokemonRewardUtil.ParsedPokemonSpec parse(String var0) {
         String var1 = var0 == null ? "" : var0.trim();
         if (!var1.isEmpty() && var1.indexOf(44) < 0 && var1.indexOf(91) < 0) {
            String[] var2 = var1.split("\\s+");
            if (var2.length != 0 && !var2[0].isBlank()) {
               String var3 = var2[0].trim();
               ResourceLocation var4 = ResourceLocation.tryParse(var3);
               String var5 = var4 == null ? var3 : var4.getPath();
               var5 = PokemonRewardUtil.normalizeSpeciesKey(var5);
               if (var5.isEmpty()) {
                  return null;
               }

               int var6 = 5;
               String var7 = "random";

               for (int var8 = 1; var8 < var2.length; var8++) {
                  String var9 = var2[var8];
                  int var10 = var9.indexOf(61);
                  if (var10 > 0 && var10 < var9.length() - 1) {
                     String var11 = var9.substring(0, var10).trim().toLowerCase(Locale.ROOT);
                     String var12 = var9.substring(var10 + 1).trim();
                     if (var11.equals("lvl") || var11.equals("level") || var11.equals("lv")) {
                        try {
                           var6 = Integer.parseInt(var12);
                        } catch (NumberFormatException var14) {
                        }
                     } else if (var11.equals("gender") || var11.equals("sex")) {
                        var7 = var12;
                     }
                  }
               }

               return new PokemonRewardUtil.ParsedPokemonSpec(var5, var6, var7);
            } else {
               return null;
            }
         } else {
            return null;
         }
      }
   }
}
