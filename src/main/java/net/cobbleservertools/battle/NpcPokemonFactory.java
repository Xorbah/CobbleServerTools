package net.cobbleservertools.battle;

import com.cobblemon.mod.common.api.moves.MoveSet;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.cobbleservertools.CobbleServerTools;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public final class NpcPokemonFactory {
   private static final String FALLBACK_SPECIES = "charmander";

   private NpcPokemonFactory() {
   }

   public static List<Pokemon> createTeam(CompoundTag var0) {
      ArrayList var1 = new ArrayList(6);
      ListTag var2 = var0.getList("NpcPokemons", 10);

      for (int var3 = 0; var3 < Math.min(6, var2.size()); var3++) {
         CompoundTag var4 = var2.getCompound(var3);
         String var5 = var4.contains("Properties", 8) ? var4.getString("Properties") : var4.getString("Species");
         int var6 = var4.contains("Level") ? Math.max(1, var4.getInt("Level")) : 1;
         String var7 = var4.getString("Gender");
         List var8 = readMoves(var4);
         Pokemon var9 = create(var5, var6, var7, var8);
         if (var9 != null) {
            var1.add(var9);
         }
      }

      if (var1.isEmpty()) {
         ListTag var10 = var0.getList("NpcPokemonProperties", 8);

         for (int var11 = 0; var11 < Math.min(6, var10.size()); var11++) {
            Pokemon var12 = create(var10.getString(var11), 1, "", List.of());
            if (var12 != null) {
               var1.add(var12);
            }
         }
      }

      return var1;
   }

   public static Pokemon create(String var0, int var1, String var2, List<String> var3) {
      int var4 = Math.max(1, Math.min(100, var1));
      Pokemon var5 = createFromProperties(var0, var4, var2);
      if (var5 == null) {
         var5 = createSimple("charmander", var4, var2);
      }

      if (var5 == null) {
         return null;
      }

      applyMoves(var5, var3);
      var5.heal();
      return var5;
   }

   private static Pokemon createFromProperties(String var0, int var1, String var2) {
      String var3 = var0 == null ? "" : var0.trim();
      if (var3.isBlank()) {
         return null;
      }

      if (!var3.contains("=") && !var3.contains(" ")) {
         return createSimple(var3, var1, var2);
      }

      try {
         Class var4 = Class.forName("com.cobblemon.mod.common.api.pokemon.PokemonProperties");

         Object var5;
         try {
            Method var6 = var4.getMethod("parse", String.class);
            var5 = var6.invoke(null, normalize(var3));
         } catch (NoSuchMethodException var14) {
            Field var7 = var4.getField("Companion");
            Object var8 = var7.get(null);
            var5 = var8.getClass().getMethod("parse", String.class).invoke(var8, normalize(var3));
         }

         if (var5 == null) {
            return null;
         }

         invokeIfPresent(var5, "setLevel", Integer.class, var1);
         Gender var17 = parseGender(var2);
         if (var17 != null) {
            invokeIfPresent(var5, "setGender", Gender.class, var17);
         }

         Object var18;
         try {
            var18 = var5.getClass().getMethod("create").invoke(var5);
         } catch (NoSuchMethodException var15) {
            Method var9 = null;

            for (Method var13 : var5.getClass().getMethods()) {
               if (var13.getName().equals("create") && var13.getParameterCount() == 1) {
                  var9 = var13;
                  break;
               }
            }

            var18 = var9 == null ? null : var9.invoke(var5);
         }

         return var18 instanceof Pokemon var19 ? var19 : null;
      } catch (ReflectiveOperationException | RuntimeException var16) {
         CobbleServerTools.LOGGER.warn("Could not parse NPC Pokemon properties '{}'", new Object[]{var3});
         return createSimple(firstSpeciesToken(var3), var1, var2);
      }
   }

   private static Pokemon createSimple(String var0, int var1, String var2) {
      Species var3 = PokemonSpecies.getByName(normalizeSpecies(var0));
      if (var3 == null) {
         return null;
      }

      Pokemon var4 = new Pokemon();
      var4.setSpecies(var3);
      var4.setLevel(var1);
      Gender var5 = parseGender(var2);
      if (var5 != null) {
         var4.setGender(var5);
      }

      return var4;
   }

   private static void applyMoves(Pokemon var0, List<String> var1) {
      if (var1 != null && !var1.isEmpty()) {
         try {
            MoveSet var2 = var0.getMoveSet();
            var2.clear();
            int var3 = 0;

            for (String var5 : var1) {
               if (var3 >= 4 || var5 == null || var5.isBlank()) {
                  break;
               }

               MoveTemplate var6 = Moves.getByName(normalizeMove(var5));
               if (var6 != null) {
                  var2.setMove(var3++, var6.create());
               }
            }
         } catch (RuntimeException var7) {
            CobbleServerTools.LOGGER.warn("Could not apply configured moves to an NPC Pokemon", new Object[0]);
         }
      }
   }

   private static List<String> readMoves(CompoundTag var0) {
      ArrayList var1 = new ArrayList(4);
      ListTag var2 = var0.getList("Moves", 10);

      for (int var3 = 0; var3 < Math.min(4, var2.size()); var3++) {
         String var4 = var2.getCompound(var3).getString("MoveId");
         if (!var4.isBlank()) {
            var1.add(var4);
         }
      }

      return var1;
   }

   private static void invokeIfPresent(Object var0, String var1, Class<?> var2, Object var3) {
      try {
         var0.getClass().getMethod(var1, var2).invoke(var0, var3);
      } catch (ReflectiveOperationException var5) {
      }
   }

   private static Gender parseGender(String var0) {
      if (var0 != null && !var0.isBlank()) {
         try {
            return Gender.valueOf(var0.trim().toUpperCase(Locale.ROOT));
         } catch (IllegalArgumentException var2) {
            return null;
         }
      } else {
         return null;
      }
   }

   private static String normalize(String var0) {
      return var0.replaceAll("\\b([a-zA-Z][a-zA-Z0-9_]*)\\s*:\\s*", "$1=").trim();
   }

   private static String normalizeSpecies(String var0) {
      String var1 = firstSpeciesToken(var0).toLowerCase(Locale.ROOT);
      int var2 = var1.indexOf(58);
      if (var2 >= 0) {
         var1 = var1.substring(var2 + 1);
      }

      return var1.replace("-", "").replace("_", "").replace(" ", "");
   }

   private static String firstSpeciesToken(String var0) {
      if (var0 == null) {
         return "charmander";
      }

      for (String var4 : var0.trim().split("\\s+")) {
         if (!var4.contains("=")) {
            return var4;
         }
      }

      return "charmander";
   }

   private static String normalizeMove(String var0) {
      return var0.trim().toLowerCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
   }
}
