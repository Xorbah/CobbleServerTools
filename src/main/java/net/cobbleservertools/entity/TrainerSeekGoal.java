package net.cobbleservertools.entity;

import com.cobblemon.mod.common.battles.BattleRegistry;
import java.util.EnumSet;
import java.util.List;
import net.cobbleservertools.battle.CobblemonPartyService;
import net.cobbleservertools.battle.NpcBattleService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

final class TrainerSeekGoal extends Goal {
   static final long CHASE_LIFETIME_TICKS = 200L;
   static final double ARRIVAL_DISTANCE_SQUARED = 0.7225;
   static final double PATH_SPEED = 1.0;
   static final double FACING_DOT_THRESHOLD = 0.98;
   private final AbstractBattleNpcEntity npc;
   private int repathTicks;
   private double lastDistanceSquared = Double.MAX_VALUE;
   private int noProgressTicks;

   TrainerSeekGoal(AbstractBattleNpcEntity var1) {
      this.npc = var1;
      this.setFlags(EnumSet.of(Flag.MOVE));
   }

   public boolean canUse() {
      if (this.npc.isRemoved() || this.npc.isBusy() || this.npc.isChasingPlayer() || this.npc.isReturningHome()) {
         return false;
      } else if (this.npc.hasProtectedInteraction()) {
         return false;
      } else if (this.npc.isEngageCooldownActive()) {
         return false;
      } else {
         double var1 = Math.max(1.0, this.npc.seekRange());
         AABB var3 = new AABB(
            this.npc.getX() - var1, this.npc.getY() - 3.0, this.npc.getZ() - var1, this.npc.getX() + var1, this.npc.getY() + 3.0, this.npc.getZ() + var1
         );
         List var4 = this.npc.level().getEntitiesOfClass(ServerPlayer.class, var3, this::isValidTarget);
         if (var4.isEmpty()) {
            return false;
         } else {
            ServerPlayer var5 = (ServerPlayer)var4.get(0);
            if (!this.npc.seeksPlayer()) {
               this.npc.openAutomaticChallenge(var5);
               return false;
            } else {
               this.npc.beginSeek(var5, 200L);
               return true;
            }
         }
      }
   }

   public boolean canContinueToUse() {
      return this.npc.shouldContinueSeek();
   }

   public void start() {
      this.repathTicks = 0;
      this.lastDistanceSquared = Double.MAX_VALUE;
      this.noProgressTicks = 0;
      this.moveTowardCurrentTarget();
   }

   public void stop() {
      if (!this.npc.isChasingPlayer()) {
         this.npc.getNavigation().stop();
      }
   }

   public void tick() {
      this.npc.pulseSeekProtection();
      if (this.npc.seekExpired()) {
         this.npc.abortSeek(true);
      } else {
         ServerPlayer var1 = this.npc.seekTarget();
         if (var1 == null || !var1.isAlive() || var1.isRemoved()) {
            this.npc.dropLiveSeekTarget();
            Vec3 var5 = this.npc.lastKnownSeekPosition();
            if (var5 != null && !(this.npc.distanceToSqr(var5) <= 1.0)) {
               if (--this.repathTicks <= 0 || this.npc.getNavigation().isDone()) {
                  this.npc.moveSeekNavigation(var5);
                  this.repathTicks = 5;
               }
            } else {
               this.npc.abortSeek(true);
            }
         } else if (BattleRegistry.getBattleByParticipatingPlayer(var1) != null) {
            this.npc.abortSeek(true);
         } else if (this.npc.hasActiveWin(var1.getUUID(), System.currentTimeMillis())) {
            this.npc.abortSeek(false);
            NpcBattleService.interact(var1, this.npc);
         } else {
            this.npc.rememberTargetPosition(var1.position());
            Vec3 var2 = this.npc.computeSeekStopPosition(var1);
            double var3 = this.npc.distanceToSqr(var2);
            if (var3 <= 0.7225) {
               this.npc.completeSeekArrival(var1);
            } else {
               if (var3 + 1.0E-4 < this.lastDistanceSquared) {
                  this.noProgressTicks = Math.max(0, this.noProgressTicks - 1);
               } else {
                  this.noProgressTicks++;
               }

               this.lastDistanceSquared = var3;
               if (--this.repathTicks <= 0 || this.npc.getNavigation().isDone() || this.noProgressTicks > 40 && var3 > 9.0) {
                  this.npc.moveSeekNavigation(var2);
                  this.repathTicks = 5;
                  if (this.noProgressTicks > 40) {
                     this.noProgressTicks = 0;
                  }
               }
            }
         }
      }
   }

   private void moveTowardCurrentTarget() {
      ServerPlayer var1 = this.npc.seekTarget();
      if (var1 != null) {
         this.npc.moveSeekNavigation(this.npc.computeSeekStopPosition(var1));
      }
   }

   private boolean isValidTarget(ServerPlayer var1) {
      if (!var1.isAlive() || var1.isRemoved()) {
         return false;
      } else if (!this.isPlayerInLineOfSight(var1)) {
         return false;
      } else if (this.npc.hasActiveWin(var1.getUUID(), System.currentTimeMillis())) {
         return false;
      } else {
         return CobblemonPartyService.healthyBattleParty(var1).isEmpty() ? false : BattleRegistry.getBattleByParticipatingPlayer(var1) == null;
      }
   }

   private boolean isPlayerInLineOfSight(ServerPlayer var1) {
      Vec3 var2 = this.npc.getLookAngle();
      Vec3 var3 = new Vec3(var2.x, 0.0, var2.z);
      Vec3 var4 = var1.position().subtract(this.npc.position());
      Vec3 var5 = new Vec3(var4.x, 0.0, var4.z);
      if (var3.lengthSqr() < 1.0E-6 || var5.lengthSqr() < 1.0E-6) {
         return false;
      }

      if (var3.normalize().dot(var5.normalize()) <= 0.98) {
         return false;
      }

      Vec3 var6 = this.npc.position().add(0.0, this.npc.getEyeHeight(), 0.0);
      Vec3 var7 = var1.position().add(0.0, var1.getEyeHeight(), 0.0);
      if (this.rayMisses(var6, var7)) {
         return true;
      }

      Vec3 var8 = var1.position().add(0.0, var1.getBbHeight() * 0.5, 0.0);
      if (this.rayMisses(var6, var8)) {
         return true;
      }

      Vec3 var9 = var1.position().add(0.0, 0.1, 0.0);
      return this.rayMisses(var6, var9);
   }

   private boolean rayMisses(Vec3 var1, Vec3 var2) {
      BlockHitResult var3 = this.npc.level().clip(new ClipContext(var1, var2, Block.COLLIDER, Fluid.NONE, this.npc));
      return var3.getType() == Type.MISS;
   }
}
