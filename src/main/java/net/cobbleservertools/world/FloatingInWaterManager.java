package net.cobbleservertools.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.cobbleservertools.entity.BattleNpcEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

public final class FloatingInWaterManager {
   private static final int REFRESH_INTERVAL_TICKS = 40;
   private static final double BASE_DEPTH = 0.55;
   private static final double BOB_HEIGHT_BLOCKS = 0.35;
   private static final double BOB_FREQ = 0.055;
   private static final double KP = 0.5;
   private static final double KD = 0.18;
   private static final double HORIZONTAL_DRAG = 0.9;
   private static final double VY_ABS_LIMIT = 0.28;
   private static final Map<ResourceKey<Level>, FloatingInWaterManager.State> STATE = new ConcurrentHashMap<>();

   private FloatingInWaterManager() {
   }

   public static void tick(MinecraftServer var0) {
      for (ServerLevel var2 : var0.getAllLevels()) {
         onLevelTick(var2);
      }
   }

   public static void clear() {
      STATE.clear();
   }

   private static void onLevelTick(ServerLevel var0) {
      ResourceKey var1 = var0.dimension();
      FloatingInWaterManager.State var2 = STATE.computeIfAbsent(var1, var0x -> new FloatingInWaterManager.State());
      int var3 = Math.floorMod(var1.location().hashCode(), 40);
      if (Math.floorMod(var2.ticks + var3, 40) == 0) {
         var2.npcs.clear();

         for (Entity var5 : var0.getAllEntities()) {
            if (var5 instanceof BattleNpcEntity var6) {
               var2.npcs.add(var6);
            }
         }
      }

      var2.ticks++;
      if (!var2.npcs.isEmpty()) {
         for (int var24 = 0; var24 < var2.npcs.size(); var24++) {
            BattleNpcEntity var25 = var2.npcs.get(var24);

            try {
               if (!var25.isRemoved()
                  && var25.isAlive()
                  && var25.level() == var0
                  && (var25.isInWater() || var0.getFluidState(var25.blockPosition()).is(FluidTags.WATER))) {
                  Double var26 = findTopmostWaterSurfaceY(var0, var25);
                  if (var26 != null) {
                     double var7 = (var25.tickCount + var24 * 13) * 0.055;
                     double var9 = var26 - 0.55 + Math.sin(var7) * 0.35;
                     Vec3 var11 = var25.position();
                     Vec3 var12 = var25.getDeltaMovement();
                     double var13 = var12.x * 0.9;
                     double var15 = var12.z * 0.9;
                     double var17 = var12.y;
                     double var19 = var9 - var11.y;
                     double var21 = 0.5 * var19 - 0.18 * var17;
                     var17 = Math.max(-0.28, Math.min(0.28, var17 + var21));
                     var25.setDeltaMovement(var13, var17, var15);
                     var25.fallDistance = 0.0F;
                     var25.setNoGravity(true);
                  }
               }
            } catch (Throwable var23) {
            }
         }
      }
   }

   private static Double findTopmostWaterSurfaceY(ServerLevel var0, BattleNpcEntity var1) {
      BlockPos var2 = var1.blockPosition();
      Double var3 = null;

      for (int var4 = 0; var4 <= 3; var4++) {
         double var5 = surfaceAt(var0, var2.above(var4));
         if (!Double.isNaN(var5)) {
            var3 = var5;
         }
      }

      return var3;
   }

   private static double surfaceAt(ServerLevel var0, BlockPos var1) {
      FluidState var2 = var0.getFluidState(var1);
      return !var2.is(FluidTags.WATER) ? Double.NaN : var1.getY() + var2.getHeight(var0, var1);
   }

   private static final class State {
      private int ticks;
      private final List<BattleNpcEntity> npcs = new ArrayList<>();
   }
}
