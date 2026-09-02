package net.cobbleservertools.entity.data;

import java.util.Locale;

public enum NpcType {
   SLIM,
   WIDE;

   public static NpcType parse(String var0) {
      if (var0 != null && !var0.isBlank()) {
         try {
            return valueOf(var0.trim().toUpperCase(Locale.ROOT));
         } catch (IllegalArgumentException var2) {
            return WIDE;
         }
      } else {
         return WIDE;
      }
   }
}
