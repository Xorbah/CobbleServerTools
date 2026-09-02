package net.cobbleservertools.rival.autodetect;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.PokemonStoreManager;
import com.cobblemon.mod.common.pokemon.Pokemon;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.cobbleservertools.battle.CobblemonPartyService;
import net.cobbleservertools.rival.storage.RivalStarterStorage;
import net.minecraft.server.level.ServerPlayer;

public final class RivalStarterAutoDetect {
   private static final Map<String, RivalStarterStorage.Branch> FAMILY = new HashMap<>();

   private RivalStarterAutoDetect() {
   }

   private static void add(RivalStarterStorage.Branch var0, String... var1) {
      for (String var5 : var1) {
         FAMILY.put(var5, var0);
      }
   }

   public static RivalStarterStorage.Branch detect(ServerPlayer var0) {
      for (Pokemon var2 : CobblemonPartyService.party(var0)) {
         RivalStarterStorage.Branch var3 = branchOf(var2);
         if (var3 != null) {
            return var3;
         }
      }

      Object var4 = getPc(var0);
      return var4 != null ? scanObject(var4, Collections.newSetFromMap(new IdentityHashMap<>()), 0) : null;
   }

   public static RivalStarterStorage.Branch branchOf(Pokemon var0) {
      return var0 != null && var0.getSpecies() != null ? FAMILY.get(normalize(var0.getSpecies().getName())) : null;
   }

   private static Object getPc(ServerPlayer var0) {
      try {
         PokemonStoreManager var1 = Cobblemon.INSTANCE.getStorage();

         for (String var5 : new String[]{"getPC", "getPc"}) {
            try {
               return var1.getClass().getMethod(var5, ServerPlayer.class).invoke(var1, var0);
            } catch (ReflectiveOperationException var7) {
            }
         }
      } catch (RuntimeException var8) {
      }

      return null;
   }

   private static RivalStarterStorage.Branch scanObject(Object var0, Set<Object> var1, int var2) {
      if (var0 == null || var2 > 5) {
         return null;
      } else if (var0 instanceof Pokemon var15) {
         return branchOf(var15);
      } else {
         if (!var0.getClass().isPrimitive() && !var1.add(var0)) {
            return null;
         }

         if (var0 instanceof Iterable) {
            for (Object var7 : (Iterable)var0) {
               RivalStarterStorage.Branch var8 = scanObject(var7, var1, var2 + 1);
               if (var8 != null) {
                  return var8;
               }
            }
         } else if (var0 instanceof Map var4) {
            for (Object var21 : var4.values()) {
               RivalStarterStorage.Branch var26 = scanObject(var21, var1, var2 + 1);
               if (var26 != null) {
                  return var26;
               }
            }
         } else if (var0 instanceof Collection) {
            for (Object var22 : (Collection)var0) {
               RivalStarterStorage.Branch var27 = scanObject(var22, var1, var2 + 1);
               if (var27 != null) {
                  return var27;
               }
            }
         } else if (var0.getClass().isArray()) {
            for (int var18 = 0; var18 < Array.getLength(var0); var18++) {
               RivalStarterStorage.Branch var23 = scanObject(Array.get(var0, var18), var1, var2 + 1);
               if (var23 != null) {
                  return var23;
               }
            }
         } else {
            for (String var9 : new String[]{"getSlots", "getBoxes", "getPokemon", "values", "toList"}) {
               try {
                  Method var10 = var0.getClass().getMethod(var9);
                  if (var10.getParameterCount() == 0) {
                     RivalStarterStorage.Branch var11 = scanObject(var10.invoke(var0), var1, var2 + 1);
                     if (var11 != null) {
                        return var11;
                     }
                  }
               } catch (ReflectiveOperationException | RuntimeException var13) {
               }
            }

            try {
               Method var20 = var0.getClass().getMethod("size");
               Method var25 = var0.getClass().getMethod("get", int.class);
               if (var20.invoke(var0) instanceof Number var30) {
                  int var31 = Math.min(512, Math.max(0, var30.intValue()));

                  for (int var32 = 0; var32 < var31; var32++) {
                     RivalStarterStorage.Branch var12 = scanObject(var25.invoke(var0, var32), var1, var2 + 1);
                     if (var12 != null) {
                        return var12;
                     }
                  }
               }
            } catch (ReflectiveOperationException | RuntimeException var14) {
            }
         }

         return null;
      }
   }

   private static String normalize(String var0) {
      if (var0 == null) {
         return "";
      }

      String var1 = var0.toLowerCase(Locale.ROOT).replace("-", "").replace("_", "").replace(" ", "");
      int var2 = var1.indexOf(58);
      return var2 >= 0 ? var1.substring(var2 + 1) : var1;
   }

   static {
      add(
         RivalStarterStorage.Branch.GRASS,
         "bulbasaur",
         "ivysaur",
         "venusaur",
         "chikorita",
         "bayleef",
         "meganium",
         "treecko",
         "grovyle",
         "sceptile",
         "turtwig",
         "grotle",
         "torterra",
         "snivy",
         "servine",
         "serperior",
         "chespin",
         "quilladin",
         "chesnaught",
         "rowlet",
         "dartrix",
         "decidueye",
         "grookey",
         "thwackey",
         "rillaboom",
         "sprigatito",
         "floragato",
         "meowscarada"
      );
      add(
         RivalStarterStorage.Branch.FIRE,
         "charmander",
         "charmeleon",
         "charizard",
         "cyndaquil",
         "quilava",
         "typhlosion",
         "torchic",
         "combusken",
         "blaziken",
         "chimchar",
         "monferno",
         "infernape",
         "tepig",
         "pignite",
         "emboar",
         "fennekin",
         "braixen",
         "delphox",
         "litten",
         "torracat",
         "incineroar",
         "scorbunny",
         "raboot",
         "cinderace",
         "fuecoco",
         "crocalor",
         "skeledirge"
      );
      add(
         RivalStarterStorage.Branch.WATER,
         "squirtle",
         "wartortle",
         "blastoise",
         "totodile",
         "croconaw",
         "feraligatr",
         "mudkip",
         "marshtomp",
         "swampert",
         "piplup",
         "prinplup",
         "empoleon",
         "oshawott",
         "dewott",
         "samurott",
         "froakie",
         "frogadier",
         "greninja",
         "popplio",
         "brionne",
         "primarina",
         "sobble",
         "drizzile",
         "inteleon",
         "quaxly",
         "quaxwell",
         "quaquaval"
      );
   }
}
