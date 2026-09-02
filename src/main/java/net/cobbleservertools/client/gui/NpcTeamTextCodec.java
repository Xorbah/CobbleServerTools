package net.cobbleservertools.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class NpcTeamTextCodec {
   public static final int MAX_TEAM_SIZE = 6;
   public static final int MAX_MOVES = 4;

   private NpcTeamTextCodec() {
   }

   public static NpcTeamTextCodec.PokemonRow parse(String var0) {
      if (var0 != null && !var0.isBlank()) {
         String[] var1 = var0.split("@", -1);
         String var2 = normalizeId(var1[0]);
         if (var2.isBlank()) {
            return null;
         }

         int var3 = 1;
         if (var1.length > 1 && !var1[1].isBlank()) {
            try {
               var3 = Integer.parseInt(var1[1].trim());
            } catch (NumberFormatException var11) {
               var3 = 1;
            }
         }

         String var4 = var1.length > 2 ? var1[2] : "random";
         ArrayList var5 = new ArrayList(4);
         if (var1.length > 3 && !var1[3].isBlank()) {
            for (String var9 : var1[3].split(",")) {
               String var10 = normalizeId(var9);
               if (!var10.isBlank()) {
                  var5.add(var10);
               }

               if (var5.size() >= 4) {
                  break;
               }
            }
         }

         return new NpcTeamTextCodec.PokemonRow(var2, var3, var4, var5);
      } else {
         return null;
      }
   }

   public static String format(NpcTeamTextCodec.PokemonRow var0) {
      return var0 != null && !var0.species().isBlank() ? var0.species() + "@" + var0.level() + "@" + var0.gender() + "@" + String.join(",", var0.moves()) : "";
   }

   public static String normalizeId(String var0) {
      if (var0 == null) {
         return "";
      }

      String var1 = var0.trim().toLowerCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
      return var1.matches("[a-z0-9_.:-]+") ? var1 : "";
   }

   public static String normalizeGender(String var0) {
      String var1 = var0 == null ? "random" : var0.trim().toLowerCase(Locale.ROOT);

      return switch (var1) {
         case "male", "m" -> "male";
         case "female", "f" -> "female";
         case "genderless", "none", "x" -> "genderless";
         default -> "random";
      };
   }

   public record PokemonRow(String species, int level, String gender, List<String> moves) {
      public PokemonRow(String species, int level, String gender, List<String> moves) {
         species = NpcTeamTextCodec.normalizeId(species);
         level = Math.max(1, Math.min(100, level));
         gender = NpcTeamTextCodec.normalizeGender(gender);
         ArrayList var5 = new ArrayList(4);
         if (moves != null) {
            for (String var7 : moves) {
               String var8 = NpcTeamTextCodec.normalizeId(var7);
               if (!var8.isBlank()) {
                  var5.add(var8);
               }

               if (var5.size() >= 4) {
                  break;
               }
            }
         }

         moves = List.copyOf(var5);
         this.species = species;
         this.level = level;
         this.gender = gender;
         this.moves = moves;
      }
   }
}
