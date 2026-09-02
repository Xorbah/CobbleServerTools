package net.cobbleservertools.entity;

import net.cobbleservertools.commerce.NpcServiceConfig;
import net.cobbleservertools.dialog.NpcDialogPurpose;
import net.cobbleservertools.entity.data.BattleType;
import net.cobbleservertools.entity.data.NpcKind;
import net.cobbleservertools.entity.data.NpcProfile;
import net.cobbleservertools.entity.data.NpcType;
import net.cobbleservertools.network.CobbleServerToolsNetworking;
import net.cobbleservertools.util.NpcPermissions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public abstract class AbstractCobbleNpcEntity extends PathfinderMob {
   private static final EntityDataAccessor<String> NPC_NAME = SynchedEntityData.defineId(AbstractCobbleNpcEntity.class, EntityDataSerializers.STRING);
   private static final EntityDataAccessor<Float> NPC_SIZE = SynchedEntityData.defineId(AbstractCobbleNpcEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Integer> NPC_TYPE = SynchedEntityData.defineId(AbstractCobbleNpcEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<String> NPC_SKIN = SynchedEntityData.defineId(AbstractCobbleNpcEntity.class, EntityDataSerializers.STRING);
   private static final EntityDataAccessor<Boolean> SITTING = SynchedEntityData.defineId(AbstractCobbleNpcEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> SEEK_PLAYER = SynchedEntityData.defineId(AbstractCobbleNpcEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> LOCK_POSITION = SynchedEntityData.defineId(AbstractCobbleNpcEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> LOOK_AT_PLAYER = SynchedEntityData.defineId(AbstractCobbleNpcEntity.class, EntityDataSerializers.BOOLEAN);
   private NpcProfile profile = new NpcProfile();

   protected AbstractCobbleNpcEntity(EntityType<? extends AbstractCobbleNpcEntity> var1, Level var2) {
      super(var1, var2);
   }

   public boolean requiresCustomPersistence() {
      return true;
   }

   public abstract NpcKind npcKind();

   protected void registerGoals() {
   }

   protected void defineSynchedData(Builder var1) {
      super.defineSynchedData(var1);
      var1.define(NPC_NAME, "");
      var1.define(NPC_SIZE, 0.9375F);
      var1.define(NPC_TYPE, NpcType.WIDE.ordinal());
      var1.define(NPC_SKIN, "minecraft:textures/entity/player/wide/steve.png");
      var1.define(SITTING, false);
      var1.define(SEEK_PLAYER, true);
      var1.define(LOCK_POSITION, false);
      var1.define(LOOK_AT_PLAYER, false);
   }

   public void addAdditionalSaveData(CompoundTag var1) {
      super.addAdditionalSaveData(var1);
      var1.merge(this.profile.writeFull(this.npcKind()));
   }

   public void readAdditionalSaveData(CompoundTag var1) {
      super.readAdditionalSaveData(var1);
      this.profile.readFull(var1);
      this.copyProfileToSyncedData();
   }

   protected InteractionResult mobInteract(Player var1, InteractionHand var2) {
      if (var2 != InteractionHand.MAIN_HAND) {
         return InteractionResult.PASS;
      }

      if (this.level().isClientSide) {
         return InteractionResult.SUCCESS;
      }

      if (var1 instanceof ServerPlayer var3) {
         if (var3.isShiftKeyDown() && NpcPermissions.canModify(var3)) {
            CobbleServerToolsNetworking.sendSnapshot(var3, this, true);
            return InteractionResult.SUCCESS;
         }

         if (this.isBlankNpc()) {
            if (NpcPermissions.canModify(var3)) {
               CobbleServerToolsNetworking.openPresetBrowser(var3, this);
            } else {
               var3.displayClientMessage(Component.literal("This NPC has not been configured."), true);
            }

            return InteractionResult.SUCCESS;
         } else {
            return this.interactWithPlayer(var3);
         }
      } else {
         return InteractionResult.PASS;
      }
   }

   protected InteractionResult interactWithPlayer(ServerPlayer var1) {
      CobbleServerToolsNetworking.sendSnapshot(var1, this, false);
      var1.displayClientMessage(Component.translatable("message.cobbleservertools.behavior_pending", new Object[]{this.npcKind().serializedName()}), true);
      return InteractionResult.SUCCESS;
   }

   public CompoundTag createVisibleSnapshot() {
      return this.profile.writeVisible(this.npcKind());
   }

   public CompoundTag createEditorSnapshot() {
      CompoundTag var1 = this.profile.writeEditable(this.npcKind());
      var1.merge(NpcServiceConfig.editorView(this));
      var1.putString("PresetId", this.presetId());
      var1.putString("PresetBrowserType", this.getTags().contains("cobbleservertools_vending_backing") ? "vending" : this.npcKind().serializedName());
      var1.putBoolean("BlankNpc", this.isBlankNpc());
      return var1;
   }

   public void applyOperatorUpdate(CompoundTag var1) {
      this.profile.applyEditableTag(var1);
      NpcServiceConfig.apply(this, var1);
      this.copyProfileToSyncedData();
   }

   public void initializeBlankNpc(double var1, double var3, double var5, float var7) {
      NpcProfile var8 = new NpcProfile();
      CompoundTag var9 = new CompoundTag();
      var9.putString("NpcName", "");
      var9.putString("DialogId", "");
      var9.putString("NpcSkin", "minecraft:textures/entity/player/wide/steve.png");
      var9.putFloat("NpcSize", 0.9375F);
      var9.putString("NpcType", "WIDE");
      var9.putBoolean("SeekPlayer", false);
      var9.putInt("SeekRange", 8);
      var9.putBoolean("Sitting", false);
      var9.putBoolean("LockPos", false);
      var9.putBoolean("LookAtPlayer", false);
      var9.putString("BattleType", "NOT_SET");
      var9.putString("RewardItemId", "");
      var9.putString("OnVictoryCommand", "");
      var9.putBoolean("RewardResetAlways", false);
      var9.putInt("MoneyReward", 0);
      var9.putInt("RematchCooldownSeconds", 0);
      var9.putBoolean("HealAfterDialog", false);
      var9.putString("TutorMoveId", "");
      var9.putDouble("HomeX", var1);
      var9.putDouble("HomeY", var3);
      var9.putDouble("HomeZ", var5);
      var9.putFloat("HomeYaw", var7);
      var8.applyEditableTag(var9);
      this.profile = var8;
      NpcServiceConfig.clearForPreset(this);
      CompoundTag var10 = this.profile.compatibilityData();
      var10.putBoolean("CobbleServerToolsBlankNpc", true);
      var10.putString("CobbleServerToolsPresetId", "");
      this.profile.replaceCompatibilityData(var10);
      this.copyProfileToSyncedData();
   }

   public void applyPresetConfiguration(CompoundTag var1, String var2) {
      Double var3 = this.profile.homeX();
      Double var4 = this.profile.homeY();
      Double var5 = this.profile.homeZ();
      float var6 = this.profile.homeYaw();
      NpcProfile var7 = new NpcProfile();
      CompoundTag var8 = new CompoundTag();
      var8.putString("NpcName", "");
      var8.putString("DialogId", "");
      var8.putString("NpcSkin", "minecraft:textures/entity/player/wide/steve.png");
      var8.putFloat("NpcSize", 0.9375F);
      var8.putString("NpcType", "WIDE");
      var8.putBoolean("SeekPlayer", false);
      var8.putInt("SeekRange", 8);
      var8.putBoolean("Sitting", false);
      var8.putBoolean("LockPos", false);
      var8.putBoolean("LookAtPlayer", false);
      var8.putString("BattleType", "NOT_SET");
      var8.putString("RewardItemId", "");
      var8.putString("OnVictoryCommand", "");
      var8.putBoolean("RewardResetAlways", false);
      var8.putInt("MoneyReward", 0);
      var8.putInt("RematchCooldownSeconds", 0);
      var8.putBoolean("HealAfterDialog", false);
      var8.putString("TutorMoveId", "");
      if (var3 != null) {
         var8.putDouble("HomeX", var3);
      }

      if (var4 != null) {
         var8.putDouble("HomeY", var4);
      }

      if (var5 != null) {
         var8.putDouble("HomeZ", var5);
      }

      var8.putFloat("HomeYaw", var6);
      var7.applyEditableTag(var8);
      var7.applyEditableTag(var1);
      this.profile = var7;
      NpcServiceConfig.clearForPreset(this);
      NpcServiceConfig.apply(this, var1);
      CompoundTag var9 = this.profile.compatibilityData();
      var9.putBoolean("CobbleServerToolsBlankNpc", false);
      var9.putString("CobbleServerToolsPresetId", var2 == null ? "" : var2);
      this.profile.replaceCompatibilityData(var9);
      this.copyProfileToSyncedData();
   }

   public void markManualConfigured() {
      CompoundTag var1 = this.profile.compatibilityData();
      var1.putBoolean("CobbleServerToolsBlankNpc", false);
      var1.putString("CobbleServerToolsPresetId", "manual");
      this.profile.replaceCompatibilityData(var1);
   }

   public boolean isBlankNpc() {
      return this.profile.compatibilityData().getBoolean("CobbleServerToolsBlankNpc");
   }

   public String presetId() {
      return this.profile.compatibilityData().getString("CobbleServerToolsPresetId");
   }

   public void applyClientSnapshot(CompoundTag var1) {
      this.profile.applyEditableTag(var1);
      this.copyProfileToSyncedData();
   }

   public String npcName() {
      return (String)this.entityData.get(NPC_NAME);
   }

   public float npcSize() {
      return (Float)this.entityData.get(NPC_SIZE);
   }

   public NpcType npcType() {
      int var1 = (Integer)this.entityData.get(NPC_TYPE);
      return var1 >= 0 && var1 < NpcType.values().length ? NpcType.values()[var1] : NpcType.WIDE;
   }

   public String npcSkin() {
      return (String)this.entityData.get(NPC_SKIN);
   }

   public boolean isNpcSitting() {
      return (Boolean)this.entityData.get(SITTING);
   }

   public boolean seeksPlayer() {
      return (Boolean)this.entityData.get(SEEK_PLAYER);
   }

   public int seekRange() {
      return this.profile.seekRange();
   }

   protected Double configuredHomeX() {
      return this.profile.homeX();
   }

   protected Double configuredHomeY() {
      return this.profile.homeY();
   }

   protected Double configuredHomeZ() {
      return this.profile.homeZ();
   }

   protected float configuredHomeYaw() {
      return this.profile.homeYaw();
   }

   public boolean locksPosition() {
      return (Boolean)this.entityData.get(LOCK_POSITION);
   }

   public boolean looksAtPlayer() {
      return (Boolean)this.entityData.get(LOOK_AT_PLAYER);
   }

   public String dialogId() {
      return this.profile.dialogId();
   }

   public boolean healsAfterDialog() {
      return this.profile.healAfterDialog();
   }

   public CompoundTag compatibilityData() {
      return this.profile.compatibilityData();
   }

   public void replaceCompatibilityData(CompoundTag var1) {
      this.profile.replaceCompatibilityData(var1);
   }

   public String tutorMoveId() {
      return this.profile.tutorMoveId();
   }

   public String rewardItemId() {
      return this.profile.rewardItemId();
   }

   public String onVictoryCommand() {
      return this.profile.onVictoryCommand();
   }

   public boolean rewardResetAlways() {
      return this.profile.rewardResetAlways();
   }

   public int moneyReward() {
      return this.profile.moneyReward();
   }

   public int rematchCooldownSeconds() {
      return this.profile.rematchCooldownSeconds();
   }

   public BattleType battleType() {
      return this.profile.battleType();
   }

   public void onDialogCompleted(ServerPlayer var1, NpcDialogPurpose var2, Boolean var3) {
   }

   private void copyProfileToSyncedData() {
      this.entityData.set(NPC_NAME, this.profile.npcName());
      this.entityData.set(NPC_SIZE, this.profile.npcSize());
      this.entityData.set(NPC_TYPE, this.profile.npcType().ordinal());
      this.entityData.set(NPC_SKIN, this.profile.npcSkin());
      this.entityData.set(SITTING, this.profile.sitting());
      this.entityData.set(SEEK_PLAYER, this.profile.seekPlayer());
      this.entityData.set(LOCK_POSITION, this.profile.lockPos());
      this.entityData.set(LOOK_AT_PLAYER, this.profile.lookAtPlayer());
      this.setCustomName(this.profile.npcName().isBlank() ? null : Component.literal(this.profile.npcName()));
      this.setCustomNameVisible(!this.profile.npcName().isBlank());
   }

   public float initialHomeYaw() {
      return this.configuredHomeYaw();
   }

   public void initializeHomeYaw(float var1) {
      CompoundTag var2 = new CompoundTag();
      var2.putFloat("HomeYaw", var1);
      this.applyOperatorUpdate(var2);
   }
}
