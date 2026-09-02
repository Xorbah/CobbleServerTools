package net.cobbleservertools.compat.rct;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import com.cobblemon.mod.common.pokemon.Pokemon;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import kotlin.Unit;
import net.cobbleservertools.CobbleServerTools;
import net.cobbleservertools.battle.AmuletCoinBattleHelper;
import net.cobbleservertools.battle.NpcRewardService;
import net.cobbleservertools.dialog.NpcDialogPurpose;
import net.cobbleservertools.dialog.NpcDialogSessions;
import net.cobbleservertools.entity.AbstractBattleNpcEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public final class RctBattleBridge {
   public static final String ENGINE_COBBLESERVERTOOLS = "COBBLESERVERTOOLS";
   public static final String ENGINE_RCT = "RCT";
   public static final String ENGINE_RUN_BUN = "RUN_BUN";
   public static final String FORMAT_SINGLES = "GEN_9_SINGLES";
   public static final String FORMAT_DOUBLES = "GEN_9_DOUBLES";
   public static final String FORMAT_TRIPLES = "GEN_9_TRIPLES";
   private static final String RCT_API = "com.gitlab.srcmc.rctapi.api.RCTApi";
   private static final String TRAINER_PLAYER = "com.gitlab.srcmc.rctapi.api.trainer.TrainerPlayer";
   private static final String TRAINER_NPC = "com.gitlab.srcmc.rctapi.api.trainer.TrainerNPC";
   private static final String TRAINER_BAG = "com.gitlab.srcmc.rctapi.api.trainer.TrainerBag";
   private static final String BATTLE_AI = "com.cobblemon.mod.common.api.battles.model.ai.BattleAI";
   private static final String RCT_AI = "com.gitlab.srcmc.rctapi.api.ai.RCTBattleAI";
   private static final String RUN_BUN_AI = "net.cobbleservertools.compat.rct.runbun.api.ai.RunBunAI";
   private static final String BATTLE_RULES_BUILDER = "com.gitlab.srcmc.rctapi.api.battle.BattleRules$Builder";
   private static final String BATTLE_RULES = "com.gitlab.srcmc.rctapi.api.battle.BattleRules";
   private static final String BATTLE_FORMAT = "com.gitlab.srcmc.rctapi.api.battle.BattleFormat";

   private RctBattleBridge() {
   }

   public static boolean rctAvailable() {
      return classExists("com.gitlab.srcmc.rctapi.api.RCTApi")
         && classExists("com.gitlab.srcmc.rctapi.api.trainer.TrainerNPC")
         && classExists("com.gitlab.srcmc.rctapi.api.ai.RCTBattleAI");
   }

   public static boolean runBunAvailable() {
      return rctAvailable() && classExists("net.cobbleservertools.compat.rct.runbun.api.ai.RunBunAI");
   }

   public static String availabilityLabel() {
      if (runBunAvailable()) {
         return "RCT API + Run & Bun available";
      } else {
         return rctAvailable() ? "RCT API available" : "RCT API not installed";
      }
   }

   public static boolean handleIfConfigured(ServerPlayer var0, AbstractBattleNpcEntity var1, List<Pokemon> var2) {
      CompoundTag var3 = var1.compatibilityData();
      String var4 = normalizeEngine(var3.getString("RctBattleEngine"));
      if ("COBBLESERVERTOOLS".equals(var4)) {
         return false;
      }

      if (!rctAvailable()) {
         var0.displayClientMessage(Component.literal("CobbleServerTools: this trainer is configured for RCT, but RCT API is not installed."), false);
         CobbleServerTools.LOGGER.warn("NPC {} requested RCT battle backend but RCT API is unavailable", new Object[]{var1.getUUID()});
         return true;
      }

      if (var2 == null || var2.isEmpty()) {
         var0.displayClientMessage(Component.literal("CobbleServerTools: trainer has no usable Pokemon."), false);
         return true;
      }

      if (!var1.tryReserve(var0)) {
         var0.displayClientMessage(Component.translatable("message.cobbleservertools.battle.npc_busy", new Object[0]), true);
         return true;
      }

      try {
         Object var5 = newTrainerPlayer(var0);
         Object var14 = newTrainerBag(var3);
         Object var7 = newBattleAi(var4, var0);
         Object var8 = newTrainerNpc(var1, var2, var14, var7);
         Object var9 = newBattleRules(var3);
         Object var10 = battleFormat(var3.getString("RctBattleFormat"));
         UUID var11 = startBattle(var5, var8, var10, var9);
         if (var11 == null) {
            var1.release(var0);
            var0.displayClientMessage(Component.translatable("message.cobbleservertools.battle.start_failed", new Object[0]), false);
            return true;
         }

         PokemonBattle var12 = findBattle(var11, var0);
         var1.markStarted(var0);
         if (var12 != null) {
            var12.getOnEndHandlers().add(var3x -> {
               finish(var0, var1, var2, var3x);
               return Unit.INSTANCE;
            });
         } else {
            CobbleServerTools.LOGGER.warn("RCT battle {} started for NPC {}, but Cobblemon battle lookup returned null", new Object[]{var11, var1.getUUID()});
         }

         CobbleServerTools.LOGGER
            .info("Started {} {} battle for CobbleServerTools NPC {} vs {}", new Object[]{var4, enumName(var10), var1.getUUID(), var0.getGameProfile().getName()});
      } catch (Throwable var13) {
         var1.release(var0);
         Throwable var6 = unwrap(var13);
         CobbleServerTools.LOGGER.error("Failed to start RCT-backed battle for NPC {}", new Object[]{var1.getUUID(), var6});
         var0.displayClientMessage(Component.literal("CobbleServerTools: RCT battle startup failed: " + safeMessage(var6)), false);
      }

      return true;
   }

   private static Object newTrainerPlayer(ServerPlayer var0) throws Exception {
      Class var1 = Class.forName("com.gitlab.srcmc.rctapi.api.trainer.TrainerPlayer");
      return var1.getConstructor(ServerPlayer.class).newInstance(var0);
   }

   private static Object newTrainerBag(CompoundTag var0) throws Exception {
      Class var1 = Class.forName("com.gitlab.srcmc.rctapi.api.trainer.TrainerBag");
      Object var2 = var1.getConstructor().newInstance();
      Method var3 = var1.getMethod("add", String.class, int.class);
      ListTag var4 = var0.getList("RctTrainerBag", 10);

      for (int var5 = 0; var5 < var4.size(); var5++) {
         CompoundTag var6 = var4.getCompound(var5);
         String var7 = var6.getString("ItemId").trim();
         int var8 = Math.max(0, var6.getInt("Count"));
         if (!var7.isBlank() && var8 > 0) {
            try {
               var3.invoke(var2, var7, var8);
            } catch (InvocationTargetException var10) {
               CobbleServerTools.LOGGER.warn("Ignoring invalid RCT trainer-bag entry {} x{}: {}", new Object[]{var7, var8, safeMessage(unwrap(var10))});
            }
         }
      }

      return var2;
   }

   private static Object newBattleAi(String var0, ServerPlayer var1) throws Exception {
      String var2 = "RUN_BUN".equals(var0) && runBunAvailable()
         ? "net.cobbleservertools.compat.rct.runbun.api.ai.RunBunAI"
         : "com.gitlab.srcmc.rctapi.api.ai.RCTBattleAI";
      if ("RUN_BUN".equals(var0) && !runBunAvailable()) {
         var1.displayClientMessage(Component.literal("CobbleServerTools: Run & Bun AI is unavailable; using RCT Battle AI for this battle."), false);
      }

      return Class.forName(var2).getConstructor().newInstance();
   }

   private static Object newTrainerNpc(AbstractBattleNpcEntity var0, List<Pokemon> var1, Object var2, Object var3) throws Exception {
      Class var4 = Class.forName("com.gitlab.srcmc.rctapi.api.trainer.TrainerNPC");
      Class var5 = Class.forName("com.gitlab.srcmc.rctapi.api.trainer.TrainerBag");
      Class var6 = Class.forName("com.cobblemon.mod.common.api.battles.model.ai.BattleAI");
      Pokemon[] var7 = var1.toArray(new Pokemon[0]);
      Constructor var8 = var4.getConstructor(String.class, Pokemon[].class, var5, var6, LivingEntity.class);
      return var8.newInstance(var0.npcName(), var7, var2, var3, var0);
   }

   private static Object newBattleRules(CompoundTag var0) throws Exception {
      Class var1 = Class.forName("com.gitlab.srcmc.rctapi.api.battle.BattleRules$Builder");
      Object var2 = var1.getConstructor().newInstance();
      invokeOptional(var2, "withMaxItemUses", new Class[]{int.class}, var0.contains("RctMaxItemUses") ? var0.getInt("RctMaxItemUses") : -1);
      invokeOptional(var2, "withHealPlayers", new Class[]{boolean.class}, var0.getBoolean("RctHealPlayers"));
      invokeOptional(var2, "withAdjustPlayerLevels", new Class[]{boolean.class}, var0.getBoolean("RctAdjustPlayerLevels"));
      invokeOptional(var2, "withAdjustNPCLevels", new Class[]{boolean.class}, var0.getBoolean("RctAdjustNpcLevels"));
      return var1.getMethod("build").invoke(var2);
   }

   private static Object battleFormat(String var0) throws Exception {
      Class var1 = Class.forName("com.gitlab.srcmc.rctapi.api.battle.BattleFormat");
      String var2 = normalizeFormat(var0);
      return Enum.valueOf(var1.asSubclass(Enum.class), var2);
   }

   private static UUID startBattle(Object var0, Object var1, Object var2, Object var3) throws Exception {
      Class var4 = Class.forName("com.gitlab.srcmc.rctapi.api.RCTApi");
      Object var5 = var4.getMethod("initInstance", String.class).invoke(null, "cobbleservertools");
      Object var6 = var4.getMethod("getBattleManager").invoke(var5);
      Class var7 = Class.forName("com.gitlab.srcmc.rctapi.api.battle.BattleFormat");
      Class var8 = Class.forName("com.gitlab.srcmc.rctapi.api.battle.BattleRules");
      Method var9 = var6.getClass().getMethod("startBattle", List.class, List.class, var7, var8);
      return var9.invoke(var6, List.of(var0), List.of(var1), var2, var3) instanceof UUID var11 ? var11 : null;
   }

   private static PokemonBattle findBattle(UUID var0, ServerPlayer var1) {
      try {
         Class var2 = Class.forName("com.cobblemon.mod.common.battles.BattleRegistry");

         try {
            if (var2.getMethod("getBattle", UUID.class).invoke(null, var0) instanceof PokemonBattle var13) {
               return var13;
            }
         } catch (NoSuchMethodException var10) {
         }

         for (Method var6 : var2.getMethods()) {
            if (var6.getName().equals("getBattleByParticipatingPlayer") && var6.getParameterCount() == 1) {
               Class var7 = var6.getParameterTypes()[0];
               if (var7.isAssignableFrom(var1.getClass()) && var6.invoke(null, var1) instanceof PokemonBattle var9) {
                  return var9;
               }
            }
         }
      } catch (Throwable var11) {
         CobbleServerTools.LOGGER.warn("Unable to look up RCT-backed Cobblemon battle {}", new Object[]{var0, unwrap(var11)});
      }

      return null;
   }

   private static void finish(ServerPlayer var0, AbstractBattleNpcEntity var1, List<Pokemon> var2, PokemonBattle var3) {
      try {
         boolean var4 = didPlayerWin(var3, var0);
         if (var4 && !var0.isRemoved()) {
            long var5 = System.currentTimeMillis();
            boolean var7 = var1.markWon(var0.getUUID(), var5);
            boolean var8 = false;
            if (findPlayerActor(var3, var0) instanceof PlayerBattleActor var10) {
               var8 = AmuletCoinBattleHelper.playerHasParticipatingAmuletCoin(var10);
            }

            NpcRewardService.award(var0, var1, var7, var8);
            if (var1.rewardResetAlways()) {
               var1.clearWin(var0.getUUID());
            }

            NpcDialogSessions.openIfPresent(var0, var1, var1.dialogId() + "_victory", NpcDialogPurpose.BATTLE_VICTORY);
         }
      } catch (Throwable var16) {
         CobbleServerTools.LOGGER.error("Failed to finalize RCT-backed battle for NPC {}", new Object[]{var1.getUUID(), unwrap(var16)});
      } finally {
         Iterator var12 = var2.iterator();

         while (true) {
            if (!var12.hasNext()) {
               var1.release(var0);
            } else {
               Pokemon var13 = (Pokemon)var12.next();
               if (var13 != null) {
                  var13.heal();
               }
            }
         }
      }
   }

   private static boolean didPlayerWin(PokemonBattle var0, ServerPlayer var1) {
      try {
         Object var2 = findPlayerActor(var0, var1);
         return var2 != null && var0.getClass().getMethod("getWinners").invoke(var0) instanceof Collection var4 && var4.contains(var2);
      } catch (Throwable var5) {
         CobbleServerTools.LOGGER.warn("Could not determine RCT battle winner for {}", new Object[]{var1.getUUID(), unwrap(var5)});
         return false;
      }
   }

   private static Object findPlayerActor(PokemonBattle var0, ServerPlayer var1) throws Exception {
      for (Method var5 : var0.getClass().getMethods()) {
         if (var5.getName().equals("getActor") && var5.getParameterCount() == 1) {
            Class var6 = var5.getParameterTypes()[0];
            Object var7 = null;
            if (var6.isAssignableFrom(var1.getClass())) {
               var7 = var1;
            } else if (var6 == UUID.class) {
               var7 = var1.getUUID();
            }

            if (var7 != null) {
               Object var8 = var5.invoke(var0, var7);
               if (var8 != null) {
                  return var8;
               }
            }
         }
      }

      return null;
   }

   private static void invokeOptional(Object var0, String var1, Class<?>[] var2, Object var3) throws Exception {
      try {
         var0.getClass().getMethod(var1, var2).invoke(var0, var3);
      } catch (NoSuchMethodException var5) {
      }
   }

   public static String normalizeEngine(String var0) {
      String var1 = var0 == null ? "" : var0.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');

      return switch (var1) {
         case "RCT" -> "RCT";
         case "RUN_BUN", "RUNANDBUN", "RUN_AND_BUN", "RB" -> "RUN_BUN";
         default -> "COBBLESERVERTOOLS";
      };
   }

   public static String normalizeFormat(String var0) {
      String var1 = var0 == null ? "" : var0.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');

      return switch (var1) {
         case "GEN_9_DOUBLES", "DOUBLES", "DOUBLE" -> "GEN_9_DOUBLES";
         case "GEN_9_TRIPLES", "TRIPLES", "TRIPLE" -> "GEN_9_TRIPLES";
         default -> "GEN_9_SINGLES";
      };
   }

   private static boolean classExists(String var0) {
      try {
         Class.forName(var0, false, RctBattleBridge.class.getClassLoader());
         return true;
      } catch (Throwable var2) {
         return false;
      }
   }

   private static String enumName(Object var0) {
      return var0 instanceof Enum var1 ? var1.name() : String.valueOf(var0);
   }

   private static Throwable unwrap(Throwable var0) {
      Throwable var1 = var0;

      while ((var1 instanceof InvocationTargetException || var1 instanceof ExecutionException) && var1.getCause() != null) {
         var1 = var1.getCause();
      }

      return var1;
   }

   private static String safeMessage(Throwable var0) {
      String var1 = var0 == null ? null : var0.getMessage();
      if (var1 != null && !var1.isBlank()) {
         return var1.length() <= 180 ? var1 : var1.substring(0, 180);
      } else {
         return var0 == null ? "unknown error" : var0.getClass().getSimpleName();
      }
   }
}
