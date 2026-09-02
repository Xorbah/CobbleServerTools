package net.cobbleservertools.entity;

import net.cobbleservertools.entity.data.NpcKind;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public final class BattleNpcEntity extends AbstractBattleNpcEntity {
   public BattleNpcEntity(EntityType<? extends BattleNpcEntity> var1, Level var2) {
      super(var1, var2);
   }

   @Override
   public NpcKind npcKind() {
      return NpcKind.BATTLE;
   }
}
