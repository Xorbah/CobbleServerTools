package net.cobbleservertools.world;

import net.cobbleservertools.entity.AbstractCobbleNpcEntity;
import net.cobbleservertools.entity.BattleNpcEntity;
import net.cobbleservertools.entity.RivalNpcEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public final class SpawnFacingManager {
   private static final String TAG_APPLIED = "cobbleservertools_facing_applied";
   private static final String TAG_PRESET_INIT = "cobbleservertools_init_facing";

   private SpawnFacingManager() {
   }

   public static void tick(MinecraftServer var0) {
      for (ServerLevel var2 : var0.getAllLevels()) {
         onLevelTick(var2);
      }
   }

   private static void onLevelTick(ServerLevel var0) {
      if (!var0.players().isEmpty()) {
         for (ServerPlayer var2 : var0.players()) {
            for (BattleNpcEntity var4 : var0.getEntitiesOfClass(
               BattleNpcEntity.class,
               var2.getBoundingBox().inflate(32.0),
               var0x -> var0x.isAlive() && !var0x.getTags().contains("cobbleservertools_facing_applied") && !var0x.getTags().contains("cobbleservertools_init_facing")
            )) {
               applyInitialFacing(var0, var4);
            }
         }

         for (Entity var6 : var0.getAllEntities()) {
            if (var6 instanceof RivalNpcEntity var7
               && var7.isAlive()
               && !var7.getTags().contains("cobbleservertools_facing_applied")
               && !var7.getTags().contains("cobbleservertools_init_facing")) {
               applyInitialFacing(var0, var7);
            }
         }
      }
   }

   private static void applyInitialFacing(ServerLevel var0, AbstractCobbleNpcEntity var1) {
      if (!var1.getTags().contains("cobbleservertools_facing_applied")) {
         if (Math.abs(var1.initialHomeYaw()) >= 0.001F) {
            var1.addTag("cobbleservertools_facing_applied");
         } else {
            ServerPlayer var2 = null;
            double var3 = Double.MAX_VALUE;

            for (ServerPlayer var6 : var0.players()) {
               double var7 = var6.distanceToSqr(var1);
               if (var7 < var3) {
                  var3 = var7;
                  var2 = var6;
               }
            }

            float var9;
            if (var2 != null) {
               var9 = snapToCardinal(Mth.wrapDegrees(var2.getYRot() + 180.0F));
            } else {
               var9 = Mth.wrapDegrees(var1.getYRot());
            }

            var1.setYRot(var9);
            var1.setYBodyRot(var9);
            var1.moveTo(var1.getX(), var1.getY(), var1.getZ(), var9, var1.getXRot());
            var1.initializeHomeYaw(var9);
            var1.addTag("cobbleservertools_facing_applied");
         }
      }
   }

   static float snapToCardinal(float var0) {
      float var1 = Mth.wrapDegrees(var0);
      int var2 = Math.round(var1 / 90.0F);
      return Mth.wrapDegrees(var2 * 90.0F);
   }
}
