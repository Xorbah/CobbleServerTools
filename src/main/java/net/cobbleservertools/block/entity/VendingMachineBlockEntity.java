package net.cobbleservertools.block.entity;

import java.util.UUID;
import net.cobbleservertools.commerce.CommerceSessions;
import net.cobbleservertools.entity.MartNpcEntity;
import net.cobbleservertools.network.CobbleServerToolsNetworking;
import net.cobbleservertools.registry.KRegistries;
import net.cobbleservertools.util.NpcPermissions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public final class VendingMachineBlockEntity extends BlockEntity {
   private static final String NBT_NPC_UUID = "NpcUuid";
   private UUID npcUuid;

   public VendingMachineBlockEntity(BlockPos var1, BlockState var2) {
      super((BlockEntityType)KRegistries.VENDING_MACHINE_BLOCK_ENTITY.get(), var1, var2);
   }

   public void openFor(ServerPlayer var1) {
      if (this.level instanceof ServerLevel var2) {
         MartNpcEntity var4 = this.getOrCreateNpc(var2);
         if (var4 != null) {
            if (var1.isShiftKeyDown() && NpcPermissions.canModify(var1)) {
               CobbleServerToolsNetworking.openPresetBrowser(var1, var4);
            } else if (var4.isBlankNpc()) {
               var1.displayClientMessage(Component.literal("This vending machine has not been configured."), true);
            } else {
               CommerceSessions.openMart(var1, var4);
            }
         }
      }
   }

   public void discardNpc() {
      if (this.level instanceof ServerLevel var1 && this.npcUuid != null) {
         Entity var3 = var1.getEntity(this.npcUuid);
         if (var3 != null) {
            var3.discard();
         }

         this.npcUuid = null;
         this.setChanged();
      }
   }

   private MartNpcEntity getOrCreateNpc(ServerLevel var1) {
      if (this.npcUuid != null && var1.getEntity(this.npcUuid) instanceof MartNpcEntity var4) {
         this.anchor(var4);
         return var4;
      }

      MartNpcEntity var2 = (MartNpcEntity)((EntityType)KRegistries.MART_NPC.get()).create(var1);
      if (var2 == null) {
         return null;
      }

      this.anchor(var2);
      var2.setInvisible(true);
      var2.setInvulnerable(true);
      var2.setNoAi(true);
      var2.setNoGravity(true);
      var2.setSilent(true);
      var2.addTag("cobbleservertools_vending_backing");
      var2.initializeBlankNpc(this.getBlockPos().getX(), this.getBlockPos().getY(), this.getBlockPos().getZ(), 0.0F);
      CompoundTag var5 = new CompoundTag();
      var5.putString("NpcName", "");
      var5.putFloat("NpcSize", 0.89F);
      var5.putBoolean("SeekPlayer", false);
      var5.putBoolean("LockPos", true);
      var5.putBoolean("LookAtPlayer", false);
      var2.applyOperatorUpdate(var5);
      if (!var1.addFreshEntity(var2)) {
         return null;
      }

      this.npcUuid = var2.getUUID();
      this.setChanged();
      return var2;
   }

   private void anchor(MartNpcEntity var1) {
      BlockPos var2 = this.getBlockPos();
      var1.moveTo(var2.getX() + 0.5, var2.getY(), var2.getZ() + 0.5, 0.0F, 0.0F);
   }

   protected void saveAdditional(CompoundTag var1, Provider var2) {
      super.saveAdditional(var1, var2);
      if (this.npcUuid != null) {
         var1.putUUID("NpcUuid", this.npcUuid);
      }
   }

   protected void loadAdditional(CompoundTag var1, Provider var2) {
      super.loadAdditional(var1, var2);
      this.npcUuid = var1.hasUUID("NpcUuid") ? var1.getUUID("NpcUuid") : null;
   }
}
