package net.cobbleservertools.battle;

import com.cobblemon.mod.common.api.battles.model.actor.AIBattleActor;
import com.cobblemon.mod.common.api.battles.model.actor.ActorType;
import com.cobblemon.mod.common.api.battles.model.actor.EntityBackedBattleActor;
import com.cobblemon.mod.common.battles.ai.StrongBattleAI;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import java.util.List;
import java.util.UUID;
import net.cobbleservertools.entity.AbstractBattleNpcEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.phys.Vec3;

public final class NpcBattleActor extends AIBattleActor implements EntityBackedBattleActor<AbstractBattleNpcEntity> {
   private final AbstractBattleNpcEntity npc;

   public NpcBattleActor(AbstractBattleNpcEntity var1, List<BattlePokemon> var2, int var3) {
      super(UUID.randomUUID(), var2, new StrongBattleAI(var3));
      this.npc = var1;
   }

   public ActorType getType() {
      return ActorType.NPC;
   }

   public MutableComponent getName() {
      return Component.literal(this.npc.getDisplayName().getString());
   }

   public MutableComponent nameOwned(String var1) {
      return Component.translatable("cobblemon.battle.owned_pokemon", new Object[]{this.getName(), var1});
   }

   public AbstractBattleNpcEntity getEntity() {
      return this.npc;
   }

   public Vec3 getInitialPos() {
      return this.npc.position();
   }
}
