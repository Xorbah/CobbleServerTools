package net.cobbleservertools.event;

import net.cobbleservertools.registry.KRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

public final class CobbleServerToolsModEvents {
   private CobbleServerToolsModEvents() {
   }

   public static void registerEntityAttributes(EntityAttributeCreationEvent var0) {
      AttributeSupplier var1 = mobileNpcAttributes().build();
      var0.put((EntityType)KRegistries.BATTLE_NPC.get(), var1);
      var0.put((EntityType)KRegistries.RIVAL_NPC.get(), mobileNpcAttributes().build());
      var0.put((EntityType)KRegistries.DIALOG_NPC.get(), mobileNpcAttributes().build());
      var0.put((EntityType)KRegistries.TRADER_NPC.get(), mobileNpcAttributes().build());
      var0.put((EntityType)KRegistries.MOVE_TUTOR_NPC.get(), mobileNpcAttributes().build());
      var0.put((EntityType)KRegistries.MART_NPC.get(), stationaryNpcAttributes().build());
   }

   private static Builder mobileNpcAttributes() {
      return Mob.createMobAttributes()
         .add(Attributes.MAX_HEALTH, 20.0)
         .add(Attributes.MOVEMENT_SPEED, 0.25)
         .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
         .add(Attributes.FOLLOW_RANGE, 16.0);
   }

   private static Builder stationaryNpcAttributes() {
      return Mob.createMobAttributes()
         .add(Attributes.MAX_HEALTH, 20.0)
         .add(Attributes.MOVEMENT_SPEED, 0.0)
         .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
         .add(Attributes.FOLLOW_RANGE, 16.0);
   }
}
