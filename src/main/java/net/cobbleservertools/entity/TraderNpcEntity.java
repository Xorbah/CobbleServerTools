package net.cobbleservertools.entity;

import net.cobbleservertools.commerce.CommerceSessions;
import net.cobbleservertools.dialog.NpcDialogPurpose;
import net.cobbleservertools.dialog.NpcDialogSessions;
import net.cobbleservertools.entity.data.NpcKind;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public final class TraderNpcEntity extends AbstractCobbleNpcEntity {
   public TraderNpcEntity(EntityType<? extends TraderNpcEntity> var1, Level var2) {
      super(var1, var2);
   }

   @Override
   public NpcKind npcKind() {
      return NpcKind.TRADER;
   }

   @Override
   protected InteractionResult interactWithPlayer(ServerPlayer var1) {
      if (!NpcDialogSessions.openIfPresent(var1, this, this.dialogId(), NpcDialogPurpose.TRADER_OPEN)) {
         CommerceSessions.openTrader(var1, this);
      }

      return InteractionResult.SUCCESS;
   }

   @Override
   public void onDialogCompleted(ServerPlayer var1, NpcDialogPurpose var2, Boolean var3) {
      if (var2 == NpcDialogPurpose.TRADER_OPEN && !Boolean.FALSE.equals(var3)) {
         CommerceSessions.openTrader(var1, this);
      }
   }
}
