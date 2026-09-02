package net.cobbleservertools.rival.util;

import net.cobbleservertools.rival.autodetect.RivalStarterAutoDetect;
import net.cobbleservertools.rival.storage.RivalStarterStorage;
import net.minecraft.server.level.ServerPlayer;

public final class RivalStarterService {
   private RivalStarterService() {
   }

   public static RivalStarterStorage.Branch getOrDetect(ServerPlayer var0) {
      RivalStarterStorage var1 = RivalStarterStorage.get(var0.getServer());
      RivalStarterStorage.Branch var2 = var1.get(var0.getUUID());
      if (var2 != null) {
         return var2;
      }

      RivalStarterStorage.Branch var3 = RivalStarterAutoDetect.detect(var0);
      if (var3 != null) {
         var1.put(var0.getUUID(), var3);
      }

      return var3;
   }

   public static RivalStarterStorage.Branch detectAndStore(ServerPlayer var0) {
      RivalStarterStorage.Branch var1 = RivalStarterAutoDetect.detect(var0);
      if (var1 != null) {
         RivalStarterStorage.get(var0.getServer()).put(var0.getUUID(), var1);
      }

      return var1;
   }

   public static void set(ServerPlayer var0, RivalStarterStorage.Branch var1) {
      RivalStarterStorage.get(var0.getServer()).put(var0.getUUID(), var1);
   }

   public static void clear(ServerPlayer var0) {
      RivalStarterStorage.get(var0.getServer()).remove(var0.getUUID());
   }
}
