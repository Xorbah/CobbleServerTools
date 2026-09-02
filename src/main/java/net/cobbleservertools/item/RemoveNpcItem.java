package net.cobbleservertools.item;

import net.cobbleservertools.entity.AbstractCobbleNpcEntity;
import net.cobbleservertools.util.NpcPermissions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;

public final class RemoveNpcItem extends Item {
   public RemoveNpcItem(Properties var1) {
      super(var1);
   }

   public InteractionResult interactLivingEntity(ItemStack var1, Player var2, LivingEntity var3, InteractionHand var4) {
      if (var3 instanceof AbstractCobbleNpcEntity var5) {
         if (var2.level().isClientSide) {
            return InteractionResult.SUCCESS;
         } else if (var2 instanceof ServerPlayer var6 && NpcPermissions.canModify(var6)) {
            String var7 = var5.npcName().isBlank() ? var5.npcKind().serializedName() : var5.npcName();
            var5.discard();
            var6.displayClientMessage(Component.literal("Removed " + var7 + "."), true);
            return InteractionResult.SUCCESS;
         } else {
            return InteractionResult.FAIL;
         }
      } else {
         return InteractionResult.PASS;
      }
   }
}
