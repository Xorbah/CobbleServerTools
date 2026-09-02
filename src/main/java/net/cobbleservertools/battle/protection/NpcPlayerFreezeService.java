package net.cobbleservertools.battle.protection;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public final class NpcPlayerFreezeService {
   private static final Map<UUID, NpcPlayerFreezeService.Anchor> FROZEN = new HashMap<>();

   private NpcPlayerFreezeService() {
   }

   public static synchronized void freeze(ServerPlayer var0) {
      if (var0 != null) {
         FROZEN.putIfAbsent(var0.getUUID(), new NpcPlayerFreezeService.Anchor(var0.position(), var0.getYRot(), var0.getXRot()));
         var0.setDeltaMovement(Vec3.ZERO);
      }
   }

   public static synchronized void unfreeze(ServerPlayer var0) {
      if (var0 != null) {
         FROZEN.remove(var0.getUUID());
      }
   }

   public static synchronized boolean isFrozen(ServerPlayer var0) {
      return var0 != null && FROZEN.containsKey(var0.getUUID());
   }

   public static synchronized void tick(MinecraftServer var0) {
      if (var0 != null) {
         FROZEN.entrySet().removeIf(var1 -> {
            ServerPlayer var2 = var0.getPlayerList().getPlayer(var1.getKey());
            if (var2 != null && !var2.isRemoved()) {
               NpcPlayerFreezeService.Anchor var3 = var1.getValue();
               var2.setDeltaMovement(Vec3.ZERO);
               var2.moveTo(var3.position.x, var3.position.y, var3.position.z, var3.yaw, var3.pitch);
               return false;
            } else {
               return true;
            }
         });
      }
   }

   public static synchronized void clearAll() {
      FROZEN.clear();
   }

   private record Anchor(Vec3 position, float yaw, float pitch) {
   }
}
