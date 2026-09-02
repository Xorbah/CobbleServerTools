package net.cobbleservertools.entity.data;

import java.util.Locale;

public enum BattleType {
   NOT_SET("Not Set"),
   EQUAL("Equal Boss"),
   COMMON("Common Boss"),
   UNCOMMON("Uncommon Boss"),
   RARE("Rare Boss"),
   ULTRA_RARE("Ultra Rare Boss"),
   MYTHICAL("Mythical Boss"),
   LEGENDARY("Legendary Boss"),
   MASTER("Master Boss");

   private final String displayName;

   BattleType(String nullxx) {
      this.displayName = nullxx;
   }

   public String displayName() {
      return this.displayName;
   }

   public static BattleType parse(String var0) {
      if (var0 != null && !var0.isBlank()) {
         try {
            return valueOf(var0.trim().toUpperCase(Locale.ROOT).replace(' ', '_'));
         } catch (IllegalArgumentException var2) {
            return NOT_SET;
         }
      } else {
         return NOT_SET;
      }
   }
}
