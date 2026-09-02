package net.cobbleservertools.entity;

import net.cobbleservertools.entity.data.NpcKind;
import net.cobbleservertools.rival.RivalNpcTeamsStorage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public final class RivalNpcEntity extends AbstractBattleNpcEntity {
   public RivalNpcEntity(EntityType<? extends RivalNpcEntity> var1, Level var2) {
      super(var1, var2);
   }

   @Override
   public NpcKind npcKind() {
      return NpcKind.RIVAL;
   }

   public RivalNpcTeamsStorage.Teams rivalTeams() {
      MinecraftServer var1 = this.level() instanceof ServerLevel var2 ? var2.getServer() : null;
      return var1 == null ? new RivalNpcTeamsStorage.Teams() : RivalNpcTeamsStorage.get(var1).getOrCreate(this.getUUID());
   }

   public void setRivalTeams(RivalNpcTeamsStorage.Teams var1) {
      MinecraftServer var2 = this.level() instanceof ServerLevel var3 ? var3.getServer() : null;
      if (var2 != null) {
         RivalNpcTeamsStorage.get(var2).put(this.getUUID(), var1);
      }
   }
}
