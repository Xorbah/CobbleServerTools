package net.cobbleservertools.client.gui;

import com.cobblemon.mod.common.api.abilities.Abilities;
import com.cobblemon.mod.common.api.abilities.AbilityTemplate;
import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.api.pokemon.Natures;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Nature;
import com.cobblemon.mod.common.pokemon.Species;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public final class NpcCreatorCatalog {
   private NpcCreatorCatalog() {
   }

   public static List<NpcCreatorCatalog.Entry> entries(NpcCreatorCatalog.Kind var0, String var1) {
      return switch (var0) {
         case POKEMON -> pokemon();
         case ITEM -> items();
         case MOVE -> moves();
         case ABILITY -> abilities();
         case NATURE -> natures();
         case FORM -> forms(var1);
      };
   }

   public static List<NpcCreatorCatalog.Entry> search(NpcCreatorCatalog.Kind var0, String var1, String var2, String var3) {
      String var4 = normalize(var2);
      String var5 = var3 == null ? "all" : var3.toLowerCase(Locale.ROOT);
      ArrayList var6 = new ArrayList();

      for (NpcCreatorCatalog.Entry var8 : entries(var0, var1)) {
         if ("all".equals(var5) || var8.namespace().equalsIgnoreCase(var5)) {
            boolean var9 = true;
            if (!var4.isBlank()) {
               for (String var13 : var4.split("\\s+")) {
                  if (!var8.haystack().contains(var13)) {
                     var9 = false;
                     break;
                  }
               }
            }

            if (var9) {
               var6.add(var8);
            }
         }
      }

      return var6;
   }

   public static List<String> namespaces(NpcCreatorCatalog.Kind var0, String var1) {
      LinkedHashSet var2 = new LinkedHashSet();
      var2.add("all");

      for (NpcCreatorCatalog.Entry var4 : entries(var0, var1)) {
         if (!var4.namespace().isBlank()) {
            var2.add(var4.namespace());
         }
      }

      return new ArrayList<>(var2);
   }

   private static List<NpcCreatorCatalog.Entry> items() {
      ArrayList var0 = new ArrayList();

      for (ResourceLocation var2 : BuiltInRegistries.ITEM.keySet()) {
         String var3 = var2.toString();
         if (!"minecraft:air".equals(var3)) {
            var0.add(entry(var3, pretty(var2.getPath()), var2.getNamespace(), "item"));
         }
      }

      return sorted(var0);
   }

   private static List<NpcCreatorCatalog.Entry> pokemon() {
      ArrayList var0 = new ArrayList();

      for (Species var3 : PokemonSpecies.getSpecies()) {
         ResourceLocation var4 = var3.getResourceIdentifier();
         if (var4 != null) {
            StringBuilder var5 = new StringBuilder("pokemon species dex ").append(var3.getNationalPokedexNumber()).append(' ');
            var0.add(entry(var4.toString(), var3.getName(), var4.getNamespace(), var5.toString()));
         }
      }

      return sorted(var0);
   }

   private static List<NpcCreatorCatalog.Entry> moves() {
      ArrayList var0 = new ArrayList();

      for (String var2 : Moves.names()) {
         if (var2 != null && !var2.isBlank()) {
            var0.add(entry(var2, pretty(var2), "cobblemon", "move"));
         }
      }

      return sorted(var0);
   }

   private static List<NpcCreatorCatalog.Entry> abilities() {
      ArrayList var0 = new ArrayList();

      for (AbilityTemplate var2 : Abilities.all()) {
         String var3 = var2.getName();
         if (var3 != null && !var3.isBlank()) {
            var0.add(entry(var3, pretty(var3), "cobblemon", "ability"));
         }
      }

      return sorted(var0);
   }

   private static List<NpcCreatorCatalog.Entry> natures() {
      ArrayList var0 = new ArrayList();

      for (Nature var2 : Natures.all()) {
         ResourceLocation var3 = var2.getName();
         if (var3 != null) {
            var0.add(entry(var3.toString(), pretty(var3.getPath()), var3.getNamespace(), "nature"));
         }
      }

      return sorted(var0);
   }

   private static List<NpcCreatorCatalog.Entry> forms(String var0) {
      ArrayList var1 = new ArrayList();
      var1.add(entry("", "Standard / automatic", "", "standard default automatic"));
      if (var0 != null && !var0.isBlank()) {
         ResourceLocation var2 = ResourceLocation.tryParse(var0);
         if (var2 == null) {
            return var1;
         }

         Species var3 = PokemonSpecies.getByIdentifier(var2);
         if (var3 == null) {
            return var1;
         }

         for (FormData var5 : var3.getForms()) {
            String var6 = var5.getName();
            if (var6 != null && !var6.isBlank() && !"standard".equalsIgnoreCase(var6)) {
               var1.add(entry(var6, pretty(var6), var2.getNamespace(), "form " + var3.getName()));
            }
         }

         return sorted(var1);
      } else {
         return var1;
      }
   }

   private static NpcCreatorCatalog.Entry entry(String var0, String var1, String var2, String var3) {
      String var4 = var2 == null ? "" : var2;
      String var5 = normalize(var0 + " " + var1 + " " + var4 + " " + var3);
      return new NpcCreatorCatalog.Entry(var0, var1 != null && !var1.isBlank() ? var1 : var0, var4, var5);
   }

   private static List<NpcCreatorCatalog.Entry> sorted(List<NpcCreatorCatalog.Entry> var0) {
      var0.sort(Comparator.comparing(NpcCreatorCatalog.Entry::label, String.CASE_INSENSITIVE_ORDER).thenComparing(NpcCreatorCatalog.Entry::id));
      return var0;
   }

   public static String pretty(String var0) {
      if (var0 != null && !var0.isBlank()) {
         String var1 = var0;
         int var2 = var1.indexOf(58);
         if (var2 >= 0) {
            var1 = var1.substring(var2 + 1);
         }

         StringBuilder var3 = new StringBuilder();

         for (String var7 : var1.replace('-', '_').split("_")) {
            if (!var7.isBlank()) {
               if (var3.length() > 0) {
                  var3.append(' ');
               }

               var3.append(Character.toUpperCase(var7.charAt(0))).append(var7.substring(1));
            }
         }

         return var3.length() == 0 ? var0 : var3.toString();
      } else {
         return "None";
      }
   }

   private static String normalize(String var0) {
      return var0 == null ? "" : var0.toLowerCase(Locale.ROOT).replace('-', '_').replace(':', ' ').trim();
   }

   public record Entry(String id, String label, String namespace, String haystack) {
      public String display() {
         return this.label.equalsIgnoreCase(this.id) ? this.label : this.label + "  [" + this.id + "]";
      }
   }

   public enum Kind {
      POKEMON,
      ITEM,
      MOVE,
      ABILITY,
      NATURE,
      FORM;
   }
}
