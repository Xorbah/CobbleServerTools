package net.cobbleservertools.entity;

import net.cobbleservertools.battle.CobblemonPartyService;
import net.cobbleservertools.dialog.NpcDialogPurpose;
import net.cobbleservertools.dialog.NpcDialogSessions;
import net.cobbleservertools.entity.data.NpcKind;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public final class DialogNpcEntity extends AbstractCobbleNpcEntity {
   public DialogNpcEntity(EntityType<? extends DialogNpcEntity> var1, Level var2) {
      super(var1, var2);
   }

   @Override
   public NpcKind npcKind() {
      return NpcKind.DIALOG;
   }

   @Override
   protected InteractionResult interactWithPlayer(ServerPlayer var1) {
      return NpcDialogSessions.open(var1, this, this.dialogId(), NpcDialogPurpose.STANDARD) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
   }

   @Override
   public void onDialogCompleted(ServerPlayer var1, NpcDialogPurpose var2, Boolean var3) {
      if (var2 == NpcDialogPurpose.STANDARD && this.healsAfterDialog() && !Boolean.FALSE.equals(var3)) {
         int var4 = CobblemonPartyService.healParty(var1);
         if (var4 > 0) {
            var1.displayClientMessage(Component.translatable("message.cobbleservertools.party_healed", new Object[0]), false);
         }
      }
   }
}
