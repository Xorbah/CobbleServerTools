package net.cobbleservertools.battle;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.battles.BattleFormat;
import com.cobblemon.mod.common.battles.BattleRegistry;
import com.cobblemon.mod.common.battles.BattleSide;
import com.cobblemon.mod.common.battles.BattleStartResult;
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import com.cobblemon.mod.common.pokemon.Pokemon;
import java.util.Iterator;
import java.util.List;
import kotlin.Unit;
import net.cobbleservertools.CobbleServerTools;
import net.cobbleservertools.compat.rct.RctBattleBridge;
import net.cobbleservertools.dialog.NpcDialogPurpose;
import net.cobbleservertools.dialog.NpcDialogSessions;
import net.cobbleservertools.entity.AbstractBattleNpcEntity;
import net.cobbleservertools.entity.RivalNpcEntity;
import net.cobbleservertools.rival.RivalNpcTeamsStorage;
import net.cobbleservertools.rival.storage.RivalStarterStorage;
import net.cobbleservertools.rival.util.RivalStarterService;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

public final class NpcBattleService {
   private static final int TRAINER_AI_SKILL = 5;

   private NpcBattleService() {
   }

   public static InteractionResult interact(ServerPlayer var0, AbstractBattleNpcEntity var1) {
      if (var1.isBusy()) {
         var0.displayClientMessage(Component.translatable("message.cobbleservertools.battle.npc_busy", new Object[0]), true);
         return InteractionResult.FAIL;
      }

      long var2 = System.currentTimeMillis();
      if (var1.hasActiveWin(var0.getUUID(), var2)) {
         long var4 = var1.rematchRemainingMillis(var0.getUUID(), var2);
         if (!NpcDialogSessions.openIfPresent(var0, var1, var1.dialogId() + "_post", NpcDialogPurpose.BATTLE_POST_WIN)) {
            if (var4 > 0L) {
               long var6 = (var4 + 999L) / 1000L;
               var0.displayClientMessage(Component.translatable("message.cobbleservertools.battle.rematch_cooldown", new Object[]{var6}), false);
            } else {
               var0.displayClientMessage(Component.translatable("message.cobbleservertools.battle.already_defeated", new Object[0]), false);
            }
         }

         return InteractionResult.SUCCESS;
      } else {
         if (!var1.openStartDialog(var0)) {
            start(var0, var1);
         }

         return InteractionResult.SUCCESS;
      }
   }

   public static void start(ServerPlayer var0, AbstractBattleNpcEntity var1) {
      if (!var0.isRemoved() && !var1.isRemoved() && !(var0.distanceToSqr(var1) > 1024.0)) {
         if (BattleRegistry.getBattleByParticipatingPlayer(var0) != null) {
            var0.displayClientMessage(Component.translatable("message.cobbleservertools.battle.player_busy", new Object[0]), true);
         } else {
            List var2 = CobblemonPartyService.healthyBattleParty(var0);
            if (var2.isEmpty()) {
               var0.displayClientMessage(Component.translatable("cobbleservertools.battle.you_have_no_cobblemon_to_battle", new Object[0]), false);
            } else {
               List<Pokemon> var3 = NpcPokemonFactory.createTeam(battleTeamData(var0, var1));
               applyBattleTypeLevels(var1, var0, var3);
               if (!RctBattleBridge.handleIfConfigured(var0, var1, var3)) {
                  List var4 = CobblemonPartyService.wrapHealthy(var3);
                  if (var4.isEmpty()) {
                     var0.displayClientMessage(Component.translatable("cobbleservertools.battle.trainers_cobblemon_all_fainted", new Object[0]), false);
                  } else if (!var1.tryReserve(var0)) {
                     var0.displayClientMessage(Component.translatable("message.cobbleservertools.battle.npc_busy", new Object[0]), true);
                  } else {
                     var3.forEach(NpcCaptureGuards::applyUncatchable);
                     PlayerBattleActor var5 = new PlayerBattleActor(var0.getUUID(), var2);
                     NpcBattleActor var6 = new NpcBattleActor(var1, var4, 5);
                     BattleSide var7 = new BattleSide(new BattleActor[]{var5});
                     BattleSide var8 = new BattleSide(new BattleActor[]{var6});

                     BattleStartResult var9;
                     try {
                        var9 = BattleRegistry.startBattle(new BattleFormat(), var7, var8, true);
                     } catch (RuntimeException var11) {
                        NpcCaptureGuards.clearUncatchable(var3);
                        var1.release(var0);
                        CobbleServerTools.LOGGER.error("Cobblemon rejected battle startup for NPC {}", new Object[]{var1.getUUID(), var11});
                        var0.displayClientMessage(Component.translatable("message.cobbleservertools.battle.start_failed", new Object[0]), false);
                        return;
                     }

                     var9.ifSuccessful(var4x -> onStarted(var0, var1, var3, var5, var4x)).ifErrored(var3x -> {
                        NpcCaptureGuards.clearUncatchable(var3);
                        var1.release(var0);
                        var0.displayClientMessage(Component.translatable("message.cobbleservertools.battle.start_failed", new Object[0]), false);
                        CobbleServerTools.LOGGER.warn("Cobblemon battle startup failed for NPC {}: {}", new Object[]{var1.getUUID(), var3x});
                        return Unit.INSTANCE;
                     });
                  }
               }
            }
         }
      }
   }

   private static Unit onStarted(ServerPlayer var0, AbstractBattleNpcEntity var1, List<Pokemon> var2, PlayerBattleActor var3, PokemonBattle var4) {
      var1.markStarted(var0);
      var4.getOnEndHandlers().add(var4x -> {
         finish(var0, var1, var2, var3);
         return Unit.INSTANCE;
      });
      return Unit.INSTANCE;
   }

   private static void finish(ServerPlayer var0, AbstractBattleNpcEntity var1, List<Pokemon> var2, PlayerBattleActor var3) {
      try {
         boolean var4 = !var2.isEmpty() && var2.stream().allMatch(Pokemon::isFainted);
         if (var4 && !var0.isRemoved()) {
            long var5 = System.currentTimeMillis();
            boolean var7 = var1.markWon(var0.getUUID(), var5);
            boolean var8 = AmuletCoinBattleHelper.playerHasParticipatingAmuletCoin(var3);
            NpcRewardService.award(var0, var1, var7, var8);
            if (var1.rewardResetAlways()) {
               var1.clearWin(var0.getUUID());
            }

            NpcDialogSessions.openIfPresent(var0, var1, var1.dialogId() + "_victory", NpcDialogPurpose.BATTLE_VICTORY);
         }
      } finally {
         NpcCaptureGuards.clearUncatchable(var2);
         Iterator var10 = var2.iterator();

         while (true) {
            if (!var10.hasNext()) {
               var1.release(var0);
            } else {
               Pokemon var11 = (Pokemon)var10.next();
               if (var11 != null) {
                  var11.heal();
               }
            }
         }
      }
   }

   private static CompoundTag battleTeamData(ServerPlayer var0, AbstractBattleNpcEntity var1) {
      CompoundTag var2 = var1.compatibilityData();
      if (var1 instanceof RivalNpcEntity var3) {
         RivalNpcTeamsStorage.Teams var4 = var3.rivalTeams();
         RivalStarterStorage.Branch var5 = RivalStarterService.getOrDetect(var0);

         List var6 = switch (var5 == null ? RivalStarterStorage.Branch.FIRE : var5) {
            case FIRE -> var4.water;
            case WATER -> var4.grass;
            case GRASS -> var4.fire;
         };
         if (var6 == null || var6.isEmpty()) {
            if (var5 == null) {
               var6 = var4.water;
            }

            if (var6 == null || var6.isEmpty()) {
               return var2;
            }
         }

         ListTag var7 = new ListTag();

         for (int var8 = 0; var8 < Math.min(6, var6.size()); var8++) {
            RivalNpcTeamsStorage.PokemonEntry var9 = (RivalNpcTeamsStorage.PokemonEntry)var6.get(var8);
            if (var9 != null && var9.species != null && !var9.species.isBlank()) {
               CompoundTag var10 = new CompoundTag();
               var10.putString("Species", var9.species);
               var10.putString("Properties", var9.species);
               var10.putInt("Level", Math.max(1, Math.min(100, var9.level)));
               var10.putString("Gender", var9.gender == null ? "" : var9.gender);
               ListTag var11 = new ListTag();
               if (var9.moves != null) {
                  for (int var12 = 0; var12 < Math.min(4, var9.moves.size()); var12++) {
                     String var13 = var9.moves.get(var12);
                     if (var13 != null && !var13.isBlank()) {
                        CompoundTag var14 = new CompoundTag();
                        var14.putString("MoveId", var13);
                        var11.add(var14);
                     }
                  }
               }

               var10.put("Moves", var11);
               var7.add(var10);
            }
         }

         if (var7.size() > 0) {
            var2.put("NpcPokemons", var7);
         }

         return var2;
      } else {
         return var2;
      }
   }

   private static void applyBattleTypeLevels(AbstractBattleNpcEntity var0, ServerPlayer var1, List<Pokemon> var2) {
      int var3 = CobblemonPartyService.highestPartyLevel(var1);

      int var4 = switch (var0.battleType()) {
         case EQUAL -> 0;
         case COMMON -> 5;
         case UNCOMMON -> 10;
         case RARE -> 15;
         case ULTRA_RARE -> 20;
         case MYTHICAL -> 25;
         case LEGENDARY -> 30;
         case MASTER -> 40;
         case NOT_SET -> Integer.MIN_VALUE;
      };
      if (var4 != Integer.MIN_VALUE) {
         int var5 = Math.max(1, Math.min(100, var3 + var4));

         for (Pokemon var7 : var2) {
            if (var7 != null) {
               var7.setLevel(var5);
               var7.heal();
            }
         }
      }
   }

   public static boolean forcePlayerForfeit(ServerPlayer var0, String var1) {
      try {
         PokemonBattle var2 = BattleRegistry.getBattleByParticipatingPlayer(var0);
         if (var2 == null) {
            return false;
         } else {
            BattleActor var3 = var2.getActor(var0);
            if (var3 != null && var3.getShowdownId() != null && !var3.getShowdownId().isBlank()) {
               String var4 = var1 != null && !var1.isBlank() ? var1 : "the trainer";
               var0.displayClientMessage(Component.literal("You moved too far from " + var4 + " and forfeited the battle."), false);
               var2.writeShowdownAction(new String[]{">forcelose " + var3.getShowdownId()});
               return true;
            } else {
               var2.stop();
               return true;
            }
         }
      } catch (RuntimeException var5) {
         CobbleServerTools.LOGGER.warn("Failed to force trainer battle forfeit for player {}: {}", new Object[]{var0.getDisplayName().getString(), var5.toString()});
         return false;
      }
   }

   public static void onPlayerDisconnect(ServerPlayer var0) {
      PokemonBattle var1 = BattleRegistry.getBattleByParticipatingPlayer(var0);
      if (var1 != null) {
         var1.stop();
      }
   }
}
