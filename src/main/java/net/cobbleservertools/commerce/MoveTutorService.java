package net.cobbleservertools.commerce;

import com.cobblemon.mod.common.api.moves.BenchedMove;
import com.cobblemon.mod.common.api.moves.Move;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.api.pokemon.moves.LearnsetQuery;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import net.cobbleservertools.battle.CobblemonPartyService;
import net.cobbleservertools.entity.MoveTutorNpcEntity;
import net.cobbleservertools.util.NpcRewardFieldUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class MoveTutorService {
   private MoveTutorService() {
   }

   public static CompoundTag clientView(ServerPlayer var0, MoveTutorNpcEntity var1) {
      CompoundTag var2 = new CompoundTag();
      var2.putString("MoveId", configuredMove(var1));
      var2.put("Party", PokemonPartySnapshot.create(var0));
      return var2;
   }

   public static MoveTutorService.Result teach(ServerPlayer var0, MoveTutorNpcEntity var1, int var2) {
      String var3 = configuredMove(var1);
      if (var3.isBlank()) {
         return MoveTutorService.Result.fail("cobbleservertools.tutor.move_tutor_without_config");
      }

      MoveTemplate var4 = resolve(var3);
      if (var4 == null) {
         return MoveTutorService.Result.fail("cobbleservertools.tutor.move_tutor_without_config");
      }

      PlayerPartyStore var5 = CobblemonPartyService.store(var0);
      if (var5 == null) {
         return MoveTutorService.Result.fail("cobbleservertools.tutor.party_not_found");
      }

      if (var2 >= 0 && var2 < 6) {
         Pokemon var6 = var5.get(var2);
         if (var6 == null) {
            return MoveTutorService.Result.fail("cobbleservertools.tutor.no_cobblemon_in_selected_slot");
         }

         if (!LearnsetQuery.Companion.getANY().canLearn(var4, var6.getForm().getMoves())) {
            return MoveTutorService.Result.fail("cobbleservertools.tutor.cobblemon_cannot_learn_move");
         }

         for (Move var8 : var6.getMoveSet().getMoves()) {
            if (var8 != null && var8.getTemplate() == var4) {
               return MoveTutorService.Result.fail("cobbleservertools.tutor.cobblemon_already_knows_move");
            }
         }

         for (BenchedMove var14 : var6.getBenchedMoves()) {
            if (var14 != null && var14.getMoveTemplate() == var4) {
               return MoveTutorService.Result.fail("cobbleservertools.tutor.cobblemon_already_knows_move");
            }
         }

         if (var6.getMoveSet().hasSpace()) {
            var6.getMoveSet().add(var4.create());
         } else {
            var6.getBenchedMoves().add(new BenchedMove(var4, 0));
         }

         CompoundTag var13 = var1.compatibilityData();
         UUID var15 = var0.getUUID();
         boolean var9 = var1.rewardResetAlways();
         Set var10 = readUuidSet(var13, "Winners", "winners");
         boolean var11 = !var10.contains(var15);
         var10.add(var15);
         if (var11 || var9) {
            NpcRewardFieldUtil.deliverRewardField(var0, var1.rewardItemId(), false);
            NpcRewardFieldUtil.runConfiguredRewardCommand(var0, var1.onVictoryCommand());
         }

         if (var9) {
            var10.remove(var15);
         }

         writeUuidSet(var13, "Winners", var10);
         writeUuidSet(var13, "winners", var10);
         var1.replaceCompatibilityData(var13);
         return MoveTutorService.Result.ok("message.cobbleservertools.tutor.move_learned");
      } else {
         return MoveTutorService.Result.fail("cobbleservertools.tutor.no_cobblemon_in_selected_slot");
      }
   }

   private static Set<UUID> readUuidSet(CompoundTag var0, String... var1) {
      HashSet var2 = new HashSet();

      for (String var6 : var1) {
         ListTag var7 = var0.getList(var6, 8);

         for (int var8 = 0; var8 < var7.size(); var8++) {
            try {
               var2.add(UUID.fromString(var7.getString(var8)));
            } catch (IllegalArgumentException var10) {
            }
         }
      }

      return var2;
   }

   private static void writeUuidSet(CompoundTag var0, String var1, Set<UUID> var2) {
      ListTag var3 = new ListTag();

      for (UUID var5 : var2) {
         var3.add(StringTag.valueOf(var5.toString()));
      }

      var0.put(var1, var3);
   }

   private static String configuredMove(MoveTutorNpcEntity var0) {
      CompoundTag var1 = var0.compatibilityData();
      String var2 = var0.tutorMoveId();
      if (var2 == null || var2.isBlank()) {
         var2 = var1.getString("tutorMoveId");
      }

      if (var2 == null || var2.isBlank()) {
         var2 = var1.getString("MoveId");
      }

      return var2 == null ? "" : var2;
   }

   private static MoveTemplate resolve(String var0) {
      String var1 = var0.trim();
      int var2 = var1.indexOf(58);
      if (var2 >= 0 && var2 < var1.length() - 1) {
         var1 = var1.substring(var2 + 1);
      }

      var1 = var1.toLowerCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
      MoveTemplate var3 = Moves.getByName(var1);
      if (var3 == null) {
         var3 = Moves.getByName("cobblemon:" + var1);
      }

      if (var3 == null && var1.equals("thunderbolt")) {
         var3 = Moves.getByName("thunder_bolt");
      }

      return var3;
   }

   public record Result(boolean success, String translationKey) {
      static MoveTutorService.Result ok(String var0) {
         return new MoveTutorService.Result(true, var0);
      }

      static MoveTutorService.Result fail(String var0) {
         return new MoveTutorService.Result(false, var0);
      }

      public Component message(Object... var1) {
         return Component.translatable(this.translationKey, var1);
      }
   }
}
