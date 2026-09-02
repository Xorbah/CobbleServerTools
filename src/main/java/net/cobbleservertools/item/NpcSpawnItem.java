package net.cobbleservertools.item;

import java.util.function.Supplier;
import net.cobbleservertools.entity.AbstractCobbleNpcEntity;
import net.cobbleservertools.network.CobbleServerToolsNetworking;
import net.cobbleservertools.util.NpcPermissions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;

public final class NpcSpawnItem<T extends AbstractCobbleNpcEntity> extends Item {
   private final Supplier<EntityType<T>> type;

   public NpcSpawnItem(Properties var1, Supplier<EntityType<T>> var2) {
      super(var1);
      this.type = var2;
   }

   public InteractionResult useOn(UseOnContext var1) {
      if (var1.getLevel().isClientSide) {
         return InteractionResult.SUCCESS;
      }

      if (var1.getLevel() instanceof ServerLevel var2 && var1.getPlayer() instanceof ServerPlayer var3 && NpcPermissions.canModify(var3)) {
         BlockPos var8 = var1.getClickedPos().relative(var1.getClickedFace());
         AbstractCobbleNpcEntity var5 = (AbstractCobbleNpcEntity)this.type.get().spawn(var2, null, var8, MobSpawnType.SPAWN_EGG, true, false);
         if (var5 == null) {
            return InteractionResult.FAIL;
         }

         float var6 = (var3.getYRot() + 180.0F) % 360.0F;
         var5.setYRot(var6);
         var5.setYHeadRot(var6);
         var5.yBodyRot = var6;
         var5.initializeBlankNpc(var8.getX(), var8.getY(), var8.getZ(), var6);
         CobbleServerToolsNetworking.openPresetBrowser(var3, var5);
         if (!var3.getAbilities().instabuild) {
            var1.getItemInHand().shrink(1);
         }

         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.FAIL;
      }
   }
}
