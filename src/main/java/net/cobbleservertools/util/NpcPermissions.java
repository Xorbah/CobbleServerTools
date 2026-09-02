package net.cobbleservertools.util;

import net.minecraft.server.level.ServerPlayer;

public final class NpcPermissions {
   private NpcPermissions() {
   }

   public static boolean canModify(ServerPlayer var0) {
      return var0 != null && (var0.hasPermissions(2) || var0.getAbilities().instabuild);
   }
}
