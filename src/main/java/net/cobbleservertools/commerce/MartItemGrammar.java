package net.cobbleservertools.commerce;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MartItemGrammar {
   private MartItemGrammar() {
   }

   public static List<MartItemGrammar.Entry> productEntries(String var0) {
      ArrayList var1 = new ArrayList();

      for (String var3 : splitTopLevel(var0, ',')) {
         MartItemGrammar.Entry var4 = parseCount(var3, 1);
         if (!var4.expression().isBlank()) {
            var1.add(var4);
         }
      }

      return var1;
   }

   public static List<MartItemGrammar.CostGroup> costGroups(String var0, int var1) {
      ArrayList var2 = new ArrayList();

      for (String var4 : splitTopLevel(var0, ',')) {
         ArrayList var5 = new ArrayList();

         for (String var7 : splitTopLevel(var4, '|')) {
            MartItemGrammar.Entry var8 = parseCount(var7, Math.max(1, var1));
            if (!var8.expression().isBlank()) {
               var5.add(var8);
            }
         }

         if (!var5.isEmpty()) {
            var2.add(new MartItemGrammar.CostGroup(var5));
         }
      }

      return var2;
   }

   public static String canonicalCosts(List<MartItemGrammar.CostGroup> var0) {
      ArrayList var1 = new ArrayList();

      for (MartItemGrammar.CostGroup var3 : var0) {
         ArrayList var4 = new ArrayList();

         for (MartItemGrammar.Entry var6 : var3.alternatives()) {
            var4.add(var6.expression() + (var6.count() == 1 ? "" : " x" + var6.count()));
         }

         if (!var4.isEmpty()) {
            var1.add(String.join(" | ", var4));
         }
      }

      return String.join(", ", var1);
   }

   public static String baseItemId(String var0) {
      if (var0 == null) {
         return "";
      }

      String var1 = var0.trim();
      if (var1.startsWith("/give ")) {
         String[] var2 = var1.split("\\s+");
         if (var2.length >= 3) {
            var1 = var2[2];
         }
      }

      if (var1.startsWith("item:")) {
         var1 = var1.substring(5);
      }

      int var3 = firstPositive(var1.indexOf(91), var1.indexOf(123), var1.indexOf(40), var1.indexOf(32));
      if (var3 >= 0) {
         var1 = var1.substring(0, var3);
      }

      return var1.trim().toLowerCase(Locale.ROOT);
   }

   private static MartItemGrammar.Entry parseCount(String var0, int var1) {
      String var2 = var0 == null ? "" : var0.trim();
      if (var2.isBlank()) {
         return new MartItemGrammar.Entry("", Math.max(1, var1));
      }

      int var3 = 0;
      boolean var4 = false;
      boolean var5 = false;

      for (int var6 = var2.length() - 1; var6 >= 0; var6--) {
         char var7 = var2.charAt(var6);
         if (var5) {
            var5 = false;
         } else if (var7 == '\\') {
            var5 = true;
         } else if (var7 == '"') {
            var4 = !var4;
         } else if (!var4) {
            if (var7 == ']' || var7 == '}' || var7 == ')') {
               var3++;
            } else if (var7 != '[' && var7 != '{' && var7 != '(') {
               if (var3 == 0 && (var7 == 'x' || var7 == 'X') && var6 > 0) {
                  int var8 = var6 + 1;

                  while (var8 < var2.length() && Character.isWhitespace(var2.charAt(var8))) {
                     var8++;
                  }

                  if (var8 < var2.length()) {
                     String var9 = var2.substring(var8).trim();
                     if (var9.matches("\\d+")) {
                        String var10 = var2.substring(0, var6).trim();

                        try {
                           return new MartItemGrammar.Entry(var10, Math.max(1, Integer.parseInt(var9)));
                        } catch (NumberFormatException var12) {
                           return new MartItemGrammar.Entry(var10, Math.max(1, var1));
                        }
                     }
                  }
               }
            } else {
               var3 = Math.max(0, var3 - 1);
            }
         }
      }

      return new MartItemGrammar.Entry(var2, Math.max(1, var1));
   }

   public static List<String> splitTopLevel(String var0, char var1) {
      ArrayList var2 = new ArrayList();
      if (var0 != null && !var0.isBlank()) {
         int var3 = 0;
         int var4 = 0;
         int var5 = 0;
         boolean var6 = false;
         boolean var7 = false;
         int var8 = 0;

         for (int var9 = 0; var9 < var0.length(); var9++) {
            char var10 = var0.charAt(var9);
            if (var7) {
               var7 = false;
            } else if (var10 == '\\') {
               var7 = true;
            } else if (var10 == '"') {
               var6 = !var6;
            } else if (!var6) {
               switch (var10) {
                  case '(':
                     var5++;
                     break;
                  case ')':
                     var5 = Math.max(0, var5 - 1);
                     break;
                  case '[':
                     var3++;
                     break;
                  case ']':
                     var3 = Math.max(0, var3 - 1);
                     break;
                  case '{':
                     var4++;
                     break;
                  case '}':
                     var4 = Math.max(0, var4 - 1);
                     break;
                  default:
                     if (var10 == var1 && var3 == 0 && var4 == 0 && var5 == 0) {
                        var2.add(var0.substring(var8, var9).trim());
                        var8 = var9 + 1;
                     }
               }
            }
         }

         var2.add(var0.substring(var8).trim());
         return var2;
      } else {
         return var2;
      }
   }

   private static int firstPositive(int... var0) {
      int var1 = -1;

      for (int var5 : var0) {
         if (var5 >= 0 && (var1 < 0 || var5 < var1)) {
            var1 = var5;
         }
      }

      return var1;
   }

   public record CostGroup(List<MartItemGrammar.Entry> alternatives) {
      public CostGroup(List<MartItemGrammar.Entry> alternatives) {
         alternatives = List.copyOf(alternatives == null ? List.of() : alternatives);
         this.alternatives = alternatives;
      }
   }

   public record Entry(String expression, int count) {
      public Entry(String expression, int count) {
         expression = expression == null ? "" : expression.trim();
         count = Math.max(1, count);
         this.expression = expression;
         this.count = count;
      }
   }
}
