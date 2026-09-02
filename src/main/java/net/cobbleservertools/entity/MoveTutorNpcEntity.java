package net.cobbleservertools.entity;

import net.cobbleservertools.commerce.CommerceSessions;
import net.cobbleservertools.dialog.NpcDialogPurpose;
import net.cobbleservertools.dialog.NpcDialogSessions;
import net.cobbleservertools.entity.data.NpcKind;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public final class MoveTutorNpcEntity extends AbstractCobbleNpcEntity {
   public MoveTutorNpcEntity(EntityType<? extends MoveTutorNpcEntity> var1, Level var2) {
      super(var1, var2);
   }

   @Override
   public NpcKind npcKind() {
      return NpcKind.MOVE_TUTOR;
   }

   @Override
   protected InteractionResult interactWithPlayer(ServerPlayer var1) {
      if (!NpcDialogSessions.openIfPresent(var1, this, this.dialogId(), NpcDialogPurpose.TUTOR_OPEN)) {
         CommerceSessions.openTutor(var1, this);
      }

      return InteractionResult.SUCCESS;
   }

   @Override
   public void onDialogCompleted(ServerPlayer var1, NpcDialogPurpose var2, Boolean var3) {
      if (var2 == NpcDialogPurpose.TUTOR_OPEN && !Boolean.FALSE.equals(var3)) {
         CommerceSessions.openTutor(var1, this);
      }
   }
}
