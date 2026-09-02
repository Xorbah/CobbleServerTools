package net.cobbleservertools.item;

import net.cobbleservertools.entity.AbstractCobbleNpcEntity;
import net.cobbleservertools.registry.KRegistries;
import net.cobbleservertools.util.NpcPermissions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;

public final class NpcPresetItem extends Item {
   private static final String ROOT = "CobbleNpcPreset";
   private static final String KIND = "Kind";
   private static final String PROFILE = "Profile";

   public NpcPresetItem(Properties var1) {
      super(var1);
   }

   public InteractionResult interactLivingEntity(ItemStack var1, Player var2, LivingEntity var3, InteractionHand var4) {
      if (var3 instanceof AbstractCobbleNpcEntity var5) {
         if (var2.level().isClientSide) {
            return InteractionResult.SUCCESS;
         }

         if (var2 instanceof ServerPlayer var6 && NpcPermissions.canModify(var6)) {
            CompoundTag var7 = new CompoundTag();
            CompoundTag var8 = new CompoundTag();
            var8.putString("Kind", var5.npcKind().serializedName());
            var8.put("Profile", var5.createEditorSnapshot());
            var7.put("CobbleNpcPreset", var8);
            if (var2.isShiftKeyDown()) {
               CustomData.set(DataComponents.CUSTOM_DATA, var1, var7);
               var6.displayClientMessage(Component.literal("NPC preset saved."), true);
            } else {
               ItemStack var9 = new ItemStack(this);
               CustomData.set(DataComponents.CUSTOM_DATA, var9, var7);
               if (!var2.getInventory().add(var9)) {
                  var2.drop(var9, false);
               }

               var6.displayClientMessage(Component.literal("NPC preset copied."), true);
            }

            return InteractionResult.SUCCESS;
         } else {
            return InteractionResult.FAIL;
         }
      } else {
         return InteractionResult.PASS;
      }
   }

   public InteractionResult useOn(UseOnContext var1) {
      if (var1.getLevel().isClientSide) {
         return InteractionResult.SUCCESS;
      }

      if (var1.getLevel() instanceof ServerLevel var2 && var1.getPlayer() instanceof ServerPlayer var3 && NpcPermissions.canModify(var3)) {
         CustomData var13 = (CustomData)var1.getItemInHand().getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
         CompoundTag var5 = var13.copyTag();
         if (!var5.contains("CobbleNpcPreset")) {
            var3.displayClientMessage(Component.literal("Item has no saved preset."), true);
            return InteractionResult.FAIL;
         }

         CompoundTag var6 = var5.getCompound("CobbleNpcPreset");
         EntityType var7 = typeFor(var6.getString("Kind"));
         if (var7 != null && var6.contains("Profile")) {
            BlockPos var8 = var1.getClickedPos().relative(var1.getClickedFace());
            AbstractCobbleNpcEntity var9 = (AbstractCobbleNpcEntity)var7.spawn(var2, null, var8, MobSpawnType.SPAWN_EGG, true, false);
            if (var9 == null) {
               return InteractionResult.FAIL;
            }

            CompoundTag var10 = var6.getCompound("Profile").copy();
            float var11 = (var3.getYRot() + 180.0F) % 360.0F;
            var10.putDouble("HomeX", var8.getX());
            var10.putDouble("HomeY", var8.getY());
            var10.putDouble("HomeZ", var8.getZ());
            var10.putFloat("HomeYaw", var11);
            var10.putDouble("EntityX", var8.getX() + 0.5);
            var10.putDouble("EntityY", var8.getY());
            var10.putDouble("EntityZ", var8.getZ() + 0.5);
            var9.applyOperatorUpdate(var10);
            var9.setYRot(var11);
            var9.setYHeadRot(var11);
            var9.yBodyRot = var11;
            if (!var3.getAbilities().instabuild) {
               var1.getItemInHand().shrink(1);
            }

            return InteractionResult.SUCCESS;
         } else {
            return InteractionResult.FAIL;
         }
      } else {
         return InteractionResult.FAIL;
      }
   }

   private static EntityType<? extends AbstractCobbleNpcEntity> typeFor(String var0) {
      return switch (var0) {
         case "battle" -> (EntityType)KRegistries.BATTLE_NPC.get();
         case "rival" -> (EntityType)KRegistries.RIVAL_NPC.get();
         case "dialog" -> (EntityType)KRegistries.DIALOG_NPC.get();
         case "mart" -> (EntityType)KRegistries.MART_NPC.get();
         case "trader" -> (EntityType)KRegistries.TRADER_NPC.get();
         case "move_tutor", "movetutor" -> (EntityType)KRegistries.MOVE_TUTOR_NPC.get();
         default -> null;
      };
   }
}
