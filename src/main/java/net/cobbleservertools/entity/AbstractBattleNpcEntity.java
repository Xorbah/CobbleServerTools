package net.cobbleservertools.entity;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.BattleRegistry;
import java.util.UUID;
import net.cobbleservertools.battle.NpcBattleProgress;
import net.cobbleservertools.battle.NpcBattleService;
import net.cobbleservertools.battle.protection.NpcDialogBattleProtection;
import net.cobbleservertools.battle.protection.NpcPlayerFreezeService;
import net.cobbleservertools.dialog.NpcDialogPurpose;
import net.cobbleservertools.dialog.NpcDialogSessions;
import net.cobbleservertools.network.CobbleServerToolsNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractBattleNpcEntity extends AbstractCobbleNpcEntity {
   public static final String BATTLE_PROTECTION_TAG = "cobbleservertools_dialog_battle_protected";
   private static final long ENGAGE_COOLDOWN_MILLIS = 1000L;
   private static final long RETURN_HOME_COOLDOWN_MILLIS = 16000L;
   private static final long POST_BATTLE_COOLDOWN_MILLIS = 4000L;
   private static final int IDLE_RETURN_TICKS = 220;
   private static final double HOME_ARRIVAL_DISTANCE_SQUARED = 0.1;
   private static final double IDLE_HOME_DISTANCE_SQUARED = 2.25;
   private static final double BATTLE_FORFEIT_DISTANCE_SQUARED = 1024.0;
   private final NpcBattleProgress progress = new NpcBattleProgress();
   private UUID currentOpponent;
   private boolean startingBattle;
   private ServerPlayer seekTarget;
   private boolean chasingPlayer;
   private boolean chasingToLastKnown;
   private Vec3 lastKnownTargetPos;
   private long chaseExpireTick;
   private long nextEngageAtMs;
   private Vec3 fallbackHome;
   private float fallbackHomeYaw;
   private boolean returningHome;
   private boolean postBattleReturnCooldownPending;
   private int idleAwayTicks;
   private int battleWatchTicks;

   protected AbstractBattleNpcEntity(EntityType<? extends AbstractBattleNpcEntity> var1, Level var2) {
      super(var1, var2);
   }

   @Override
   protected void registerGoals() {
      super.registerGoals();
      this.goalSelector.addGoal(2, new TrainerSeekGoal(this));
   }

   public void tick() {
      super.tick();
      if (!this.level().isClientSide) {
         if (this.fallbackHome == null) {
            this.fallbackHome = this.position();
            this.fallbackHomeYaw = this.getYRot();
         }

         this.tickReturnHome();
         this.tickIdleRecovery();
         this.tickBattleDistanceGuard();
      }
   }

   @Override
   public void addAdditionalSaveData(CompoundTag var1) {
      super.addAdditionalSaveData(var1);
      this.progress.write(var1);
   }

   @Override
   public void readAdditionalSaveData(CompoundTag var1) {
      super.readAdditionalSaveData(var1);
      this.progress.read(var1);
      this.currentOpponent = null;
      this.startingBattle = false;
      this.seekTarget = null;
      this.chasingPlayer = false;
      this.chasingToLastKnown = false;
      this.lastKnownTargetPos = null;
      this.chaseExpireTick = 0L;
      this.returningHome = false;
      this.postBattleReturnCooldownPending = false;
      this.idleAwayTicks = 0;
      this.battleWatchTicks = 0;
   }

   @Override
   protected InteractionResult interactWithPlayer(ServerPlayer var1) {
      return NpcBattleService.interact(var1, this);
   }

   @Override
   public void onDialogCompleted(ServerPlayer var1, NpcDialogPurpose var2, Boolean var3) {
      if (var2 == NpcDialogPurpose.BATTLE_START) {
         if (!Boolean.FALSE.equals(var3)) {
            NpcBattleService.start(var1, this);
         } else {
            this.beginReturnHome(false);
         }
      }
   }

   public boolean tryReserve(ServerPlayer var1) {
      if (this.currentOpponent == null && !this.startingBattle) {
         this.startingBattle = true;
         this.currentOpponent = var1.getUUID();
         return true;
      } else {
         return false;
      }
   }

   public void markStarted(ServerPlayer var1) {
      this.startingBattle = false;
      this.currentOpponent = var1.getUUID();
      this.chasingPlayer = false;
      this.chasingToLastKnown = false;
      this.seekTarget = null;
      this.lastKnownTargetPos = null;
      this.chaseExpireTick = 0L;
      this.clearSeekProtection(var1);
      NpcDialogBattleProtection.clear(var1);
      NpcPlayerFreezeService.unfreeze(var1);
      CobbleServerToolsNetworking.closeFreezeOverlay(var1);
   }

   public void release(ServerPlayer var1) {
      if (var1 == null || var1.getUUID().equals(this.currentOpponent)) {
         this.currentOpponent = null;
         this.startingBattle = false;
         if (!this.isRemoved()) {
            this.beginReturnHome(true);
         }
      }
   }

   public UUID currentOpponent() {
      return this.currentOpponent;
   }

   public boolean isBusy() {
      return this.currentOpponent != null || this.startingBattle;
   }

   public boolean hasActiveWin(UUID var1, long var2) {
      return this.progress.hasActiveWin(var1, var2);
   }

   public long rematchRemainingMillis(UUID var1, long var2) {
      return this.progress.remainingMillis(var1, var2);
   }

   public boolean markWon(UUID var1, long var2) {
      return this.progress.markWon(var1, this.rematchCooldownSeconds(), var2);
   }

   public void clearWin(UUID var1) {
      this.progress.clear(var1);
   }

   public boolean openStartDialog(ServerPlayer var1) {
      return NpcDialogSessions.openIfPresent(var1, this, this.dialogId(), NpcDialogPurpose.BATTLE_START);
   }

   boolean isChasingPlayer() {
      return this.chasingPlayer;
   }

   boolean isReturningHome() {
      return this.returningHome;
   }

   ServerPlayer seekTarget() {
      return this.seekTarget;
   }

   Vec3 lastKnownSeekPosition() {
      return this.lastKnownTargetPos;
   }

   boolean isEngageCooldownActive() {
      return System.currentTimeMillis() < this.nextEngageAtMs;
   }

   boolean hasProtectedInteraction() {
      return this.seekTarget != null && NpcDialogBattleProtection.isProtected(this.seekTarget);
   }

   void beginSeek(ServerPlayer var1, long var2) {
      if (var1 != null) {
         this.ensureFallbackHome();
         this.seekTarget = var1;
         this.chasingPlayer = true;
         this.chasingToLastKnown = false;
         this.lastKnownTargetPos = var1.position();
         this.chaseExpireTick = this.level().getGameTime() + Math.max(1L, var2);
         this.idleAwayTicks = 0;
         this.returningHome = false;
         this.beginSeekProtection(var1);
      }
   }

   boolean shouldContinueSeek() {
      if (!this.chasingPlayer || this.isBusy()) {
         return false;
      } else if (this.seekExpired()) {
         return false;
      } else if (this.seekTarget != null && this.seekTarget.isAlive() && !this.seekTarget.isRemoved()) {
         return true;
      } else if (this.lastKnownTargetPos != null) {
         this.chasingToLastKnown = true;
         return true;
      } else {
         return false;
      }
   }

   boolean seekExpired() {
      return this.chaseExpireTick > 0L && this.level().getGameTime() >= this.chaseExpireTick;
   }

   void rememberTargetPosition(Vec3 var1) {
      if (var1 != null) {
         this.lastKnownTargetPos = var1;
      }
   }

   void dropLiveSeekTarget() {
      if (this.seekTarget != null) {
         this.clearSeekProtection(this.seekTarget);
         this.seekTarget = null;
      }

      this.chasingToLastKnown = this.lastKnownTargetPos != null;
   }

   void moveSeekNavigation(Vec3 var1) {
      if (var1 != null) {
         this.getNavigation().moveTo(var1.x, var1.y, var1.z, 1.0);
      }
   }

   Vec3 computeSeekStopPosition(ServerPlayer var1) {
      if (var1 == null) {
         return this.position();
      }

      BlockPos var2 = var1.blockPosition();
      BlockPos var3 = this.blockPosition();
      int var4 = var3.getX() - var2.getX();
      int var5 = var3.getZ() - var2.getZ();
      int var6 = 0;
      int var7 = 0;
      if (Math.abs(var4) >= Math.abs(var5)) {
         var6 = Integer.compare(var4, 0);
      } else {
         var7 = Integer.compare(var5, 0);
      }

      BlockPos[] var8 = new BlockPos[]{var2.offset(var6, 0, var7), var2.offset(1, 0, 0), var2.offset(-1, 0, 0), var2.offset(0, 0, 1), var2.offset(0, 0, -1)};

      for (BlockPos var12 : var8) {
         if (!var12.equals(var2) && this.isValidSeekStop(var12)) {
            return Vec3.atCenterOf(var12);
         }
      }

      return var1.position();
   }

   private boolean isValidSeekStop(BlockPos var1) {
      return this.level().getBlockState(var1).getCollisionShape(this.level(), var1).isEmpty()
         && this.level().getBlockState(var1.below()).isSolidRender(this.level(), var1.below());
   }

   void completeSeekArrival(ServerPlayer var1) {
      if (var1 == null) {
         this.abortSeek(true);
      } else {
         this.getNavigation().stop();
         this.chasingPlayer = false;
         this.chasingToLastKnown = false;
         this.seekTarget = null;
         this.lastKnownTargetPos = null;
         this.chaseExpireTick = 0L;
         NpcDialogBattleProtection.endEngage(var1);
         NpcDialogBattleProtection.beginDialog(var1);
         NpcPlayerFreezeService.unfreeze(var1);
         CobbleServerToolsNetworking.closeFreezeOverlay(var1);
         this.nextEngageAtMs = Math.max(this.nextEngageAtMs, System.currentTimeMillis() + 1000L);
         NpcBattleService.interact(var1, this);
      }
   }

   void openAutomaticChallenge(ServerPlayer var1) {
      if (var1 != null && !this.isBusy() && !this.isEngageCooldownActive()) {
         this.nextEngageAtMs = System.currentTimeMillis() + 1000L;
         NpcDialogBattleProtection.beginDialog(var1);
         NpcBattleService.interact(var1, this);
      }
   }

   void pulseSeekProtection() {
      ServerPlayer var1 = this.seekTarget;
      if (var1 != null) {
         NpcDialogBattleProtection.touchEngage(var1);
         NpcPlayerFreezeService.freeze(var1);
      }
   }

   private void beginSeekProtection(ServerPlayer var1) {
      NpcDialogBattleProtection.beginEngage(var1);
      NpcPlayerFreezeService.freeze(var1);
      CobbleServerToolsNetworking.openFreezeOverlay(var1);
   }

   private void clearSeekProtection(ServerPlayer var1) {
      if (var1 != null) {
         NpcDialogBattleProtection.endEngage(var1);
         NpcPlayerFreezeService.unfreeze(var1);
         CobbleServerToolsNetworking.closeFreezeOverlay(var1);
      }
   }

   void abortSeek(boolean var1) {
      ServerPlayer var2 = this.seekTarget;
      if (var2 != null) {
         this.clearSeekProtection(var2);
      }

      this.seekTarget = null;
      this.chasingPlayer = false;
      this.chasingToLastKnown = false;
      this.lastKnownTargetPos = null;
      this.chaseExpireTick = 0L;
      this.getNavigation().stop();
      if (var1) {
         this.nextEngageAtMs = Math.max(this.nextEngageAtMs, System.currentTimeMillis() + 1000L);
      }

      this.beginReturnHome(false);
   }

   public void onFreezeOverlayTimeout(ServerPlayer var1) {
      if (var1 != null) {
         if (this.seekTarget != null && var1.getUUID().equals(this.seekTarget.getUUID())) {
            this.clearSeekProtection(var1);
            this.seekTarget = null;
            this.chasingPlayer = false;
            this.chasingToLastKnown = false;
            this.lastKnownTargetPos = null;
            this.chaseExpireTick = 0L;
            this.getNavigation().stop();
            this.nextEngageAtMs = System.currentTimeMillis() + 16000L;
            this.beginReturnHome(false);
         } else {
            NpcDialogBattleProtection.clear(var1);
            NpcPlayerFreezeService.unfreeze(var1);
            CobbleServerToolsNetworking.closeFreezeOverlay(var1);
         }
      }
   }

   private void ensureFallbackHome() {
      if (this.fallbackHome == null) {
         this.fallbackHome = this.position();
         this.fallbackHomeYaw = this.getYRot();
      }
   }

   private Vec3 homePosition() {
      Double var1 = this.configuredHomeX();
      Double var2 = this.configuredHomeY();
      Double var3 = this.configuredHomeZ();
      if (var1 != null && var2 != null && var3 != null) {
         return new Vec3(var1, var2, var3);
      }

      this.ensureFallbackHome();
      return this.fallbackHome;
   }

   private float homeYaw() {
      Double var1 = this.configuredHomeX();
      Double var2 = this.configuredHomeY();
      Double var3 = this.configuredHomeZ();
      return var1 != null && var2 != null && var3 != null ? this.configuredHomeYaw() : this.fallbackHomeYaw;
   }

   private void beginReturnHome(boolean var1) {
      if (!this.level().isClientSide && !this.isRemoved()) {
         Vec3 var2 = this.homePosition();
         if (var2 != null) {
            this.returningHome = true;
            this.postBattleReturnCooldownPending = var1;
            this.nextEngageAtMs = Math.max(this.nextEngageAtMs, System.currentTimeMillis() + 16000L);
            if (!this.getNavigation().moveTo(var2.x, var2.y, var2.z, 1.0)) {
               this.finishReturnHome(var2);
            }
         }
      }
   }

   private void tickReturnHome() {
      if (this.returningHome) {
         Vec3 var1 = this.homePosition();
         if (var1 == null) {
            this.returningHome = false;
         } else {
            if (this.distanceToSqr(var1) <= 0.1 || this.getNavigation().isDone()) {
               this.finishReturnHome(var1);
            }
         }
      }
   }

   private void finishReturnHome(Vec3 var1) {
      this.getNavigation().stop();
      this.setDeltaMovement(Vec3.ZERO);
      this.setPos(var1.x, var1.y, var1.z);
      this.setYRot(this.homeYaw());
      this.returningHome = false;
      this.idleAwayTicks = 0;
      if (this.postBattleReturnCooldownPending) {
         this.postBattleReturnCooldownPending = false;
         this.nextEngageAtMs = System.currentTimeMillis() + 4000L;
      }
   }

   private void tickIdleRecovery() {
      if (!this.returningHome && !this.chasingPlayer && !this.isBusy()) {
         Vec3 var1 = this.homePosition();
         if (var1 == null || this.distanceToSqr(var1) <= 2.25) {
            this.idleAwayTicks = 0;
         } else if (!this.getNavigation().isDone()) {
            this.idleAwayTicks = 0;
         } else {
            if (++this.idleAwayTicks >= 220) {
               this.idleAwayTicks = 0;
               this.beginReturnHome(false);
            }
         }
      } else {
         this.idleAwayTicks = 0;
      }
   }

   private void tickBattleDistanceGuard() {
      if (++this.battleWatchTicks >= 20) {
         this.battleWatchTicks = 0;
         if (this.currentOpponent != null && !this.startingBattle && this.level() instanceof ServerLevel var1) {
            ServerPlayer var4 = var1.getServer().getPlayerList().getPlayer(this.currentOpponent);
            if (var4 == null) {
               this.release(null);
            } else {
               if ((!var4.isAlive() || var4.level() != this.level() || this.distanceToSqr(var4) > 1024.0)
                  && !NpcBattleService.forcePlayerForfeit(var4, this.npcName().isBlank() ? "the trainer" : this.npcName())) {
                  PokemonBattle var3 = BattleRegistry.getBattleByParticipatingPlayer(var4);
                  if (var3 != null) {
                     var3.stop();
                  } else {
                     this.release(var4);
                  }
               }
            }
         }
      }
   }
}
