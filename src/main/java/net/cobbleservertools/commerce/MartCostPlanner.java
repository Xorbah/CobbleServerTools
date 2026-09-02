package net.cobbleservertools.commerce;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class MartCostPlanner {
   private MartCostPlanner() {
   }

   public static List<MartCostPlanner.Choice> plan(List<MartItemGrammar.CostGroup> var0, Map<String, Integer> var1, int var2) {
      return plan(var0, var1, var2, Function.identity());
   }

   public static List<MartCostPlanner.Choice> plan(List<MartItemGrammar.CostGroup> var0, Map<String, Integer> var1, int var2, Function<String, String> var3) {
      ArrayList var4 = new ArrayList();
      HashMap var5 = new HashMap(var1 == null ? Map.of() : var1);
      Function var6 = var3 == null ? Function.identity() : var3;
      return choose(var0 == null ? List.of() : var0, 0, Math.max(1, var2), var5, var4, var6) ? List.copyOf(var4) : List.of();
   }

   private static boolean choose(
      List<MartItemGrammar.CostGroup> var0,
      int var1,
      int var2,
      Map<String, Integer> var3,
      ArrayList<MartCostPlanner.Choice> var4,
      Function<String, String> var5
   ) {
      if (var1 >= var0.size()) {
         return true;
      }

      MartItemGrammar.CostGroup var6 = (MartItemGrammar.CostGroup)var0.get(var1);

      for (MartItemGrammar.Entry var8 : var6.alternatives()) {
         String var9 = MartItemGrammar.baseItemId(var8.expression());
         String var10 = (String)var5.apply(var9);
         if (var10 != null && !var10.isBlank()) {
            long var11 = (long)var8.count() * Math.max(1, var2);
            if (var11 > 0L && var11 <= 2147483647L) {
               int var13 = (int)var11;
               int var14 = var3.getOrDefault(var10, 0);
               if (var14 >= var13) {
                  var3.put(var10, var14 - var13);
                  var4.add(new MartCostPlanner.Choice(var1, var8));
                  if (choose(var0, var1 + 1, var2, var3, var4, var5)) {
                     return true;
                  }

                  var4.remove(var4.size() - 1);
                  var3.put(var10, var14);
               }
            }
         }
      }

      return false;
   }

   public record Choice(int groupIndex, MartItemGrammar.Entry entry) {
   }
}
