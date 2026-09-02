package net.cobbleservertools.client.gui;

import net.minecraft.network.chat.Component;

final class CommerceScreenSupport {
   private CommerceScreenSupport() {
   }

   static Component title(String var0, String var1) {
      String var2 = var0 == null ? "" : var0.trim();
      if (var2.isEmpty()) {
         return Component.translatable(var1, new Object[0]);
      } else {
         return !var2.contains(" ") && var2.contains(".") ? Component.translatable(var2, new Object[0]) : Component.literal(var2);
      }
   }

   static String shortId(String var0) {
      if (var0 == null) {
         return "";
      }

      int var1 = var0.indexOf(58);
      return var1 >= 0 && var1 + 1 < var0.length() ? var0.substring(var1 + 1) : var0;
   }
}
