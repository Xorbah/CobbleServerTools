package net.cobbleservertools.entity;

import net.cobbleservertools.commerce.CommerceSessions;
import net.cobbleservertools.commerce.MartPresetCatalog;
import net.cobbleservertools.dialog.NpcDialogPurpose;
import net.cobbleservertools.dialog.NpcDialogSessions;
import net.cobbleservertools.entity.data.NpcKind;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public final class MartNpcEntity extends AbstractCobbleNpcEntity {
   public MartNpcEntity(EntityType<? extends MartNpcEntity> var1, Level var2) {
      super(var1, var2);
   }

   @Override
   public NpcKind npcKind() {
      return NpcKind.MART;
   }

   @Override
   protected InteractionResult interactWithPlayer(ServerPlayer var1) {
      this.ensureDefaultStock();
      if (!NpcDialogSessions.openIfPresent(var1, this, this.dialogId(), NpcDialogPurpose.MART_OPEN)) {
         CommerceSessions.openMart(var1, this);
      }

      return InteractionResult.SUCCESS;
   }

   private void ensureDefaultStock() {
      CompoundTag var1 = this.compatibilityData();
      if (var1.getList("ShopItems", 10).isEmpty()) {
         CompoundTag var2 = MartPresetCatalog.load("viridian_mart");
         if (!var2.getAllKeys().isEmpty()) {
            this.applyOperatorUpdate(var2);
         }
      }
   }

   @Override
   public void onDialogCompleted(ServerPlayer var1, NpcDialogPurpose var2, Boolean var3) {
      if (var2 == NpcDialogPurpose.MART_OPEN && !Boolean.FALSE.equals(var3)) {
         CommerceSessions.openMart(var1, this);
      }
   }
}
