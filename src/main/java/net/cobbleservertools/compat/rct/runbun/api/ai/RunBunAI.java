package net.cobbleservertools.compat.rct.runbun.api.ai;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.battles.interpreter.BattleContext;
import com.cobblemon.mod.common.api.battles.interpreter.BattleContext.Type;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.moves.Move;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.api.moves.categories.DamageCategories;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.api.types.ElementalTypes;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.BattleSide;
import com.cobblemon.mod.common.battles.InBattleMove;
import com.cobblemon.mod.common.battles.MoveActionResponse;
import com.cobblemon.mod.common.battles.MoveTarget;
import com.cobblemon.mod.common.battles.PassActionResponse;
import com.cobblemon.mod.common.battles.ShowdownActionResponse;
import com.cobblemon.mod.common.battles.ShowdownMoveset;
import com.cobblemon.mod.common.battles.SwitchActionResponse;
import com.cobblemon.mod.common.battles.Targetable;
import com.cobblemon.mod.common.battles.ShowdownMoveset.Gimmick;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleStates;
import com.gitlab.srcmc.rctapi.api.ai.utils.TypeChart;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleEffects.Custom;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleEffects.Field.Room;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleEffects.Field.Terrain;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleEffects.Field.Weather;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleEffects.Pokemon.Status;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleEffects.Pokemon.Volatile;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleEffects.Side.Hazard;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleEffects.Side.Screen;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleEffects.Side.Tailwind;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;
import kotlin.Unit;
import net.cobbleservertools.compat.rct.runbun.ModCommon;
import net.cobbleservertools.compat.rct.runbun.api.ai.config.RunBunAIConfig;
import net.cobbleservertools.compat.rct.runbun.api.ai.utils.PokeMathMax;
import net.cobbleservertools.compat.rct.runbun.api.ai.utils.RBBattleSlots;
import net.cobbleservertools.compat.rct.runbun.api.ai.utils.RBMoveList;
import net.cobbleservertools.compat.rct.runbun.api.ai.utils.RBSlotInformation;
import net.cobbleservertools.compat.rct.runbun.api.ai.utils.RBStatStages;
import net.cobbleservertools.compat.rct.runbun.api.ai.utils.RBTypeChart;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RunBunAI implements BattleAI {
   private boolean canTera;
   private String teraTarget;
   public RBBattleSlots battleSlots = null;
   private RBSlotInformation selfInfo;
   private RBSlotInformation partnerInfo;
   private boolean doubles = false;
   private final Random RANDOM = new Random();
   private static boolean hasResetDefault = false;
   private int battleTurn = 1;
   private boolean hasUsedTera = false;
   private boolean hasUsedMega = false;
   private PokemonBattle pb = null;
   private Map<Integer, String> moveHistoryEnemy = new HashMap<>();
   private Map<Stats, Integer> npcStages = new HashMap<>();
   private static final Map<ElementalType, Map<ElementalType, Double>> typeChart = new HashMap<>();
   private static final List<String> priorityDamageMoves = RBMoveList.getPriorityDamageMoves();
   private static final List<String> abilityStatBooster = RBMoveList.getAbilityStatBooster();
   private static final List<String> highCriticalMoves = RBMoveList.getHighCriticalMoves();
   private static final List<String> trapMoves = RBMoveList.getTrapMoves();
   private static final List<String> speedReductionMoves = RBMoveList.getSpeedReductionMoves();
   private static final List<String> physicalAttackReductionMoves = RBMoveList.getPhysicalAttackReductionMoves();
   private static final List<String> specialAttackReductionMoves = RBMoveList.getSpecialAttackReductionMoves();
   private static final List<String> generalSetupMoves = RBMoveList.getGeneralSetupMoves();
   private static final List<String> ignoreStatDropAbilities = RBMoveList.getIgnoreStatDropAbilities();
   private static final List<String> specialFunctionMoves = RBMoveList.getSpecialFunctionMoves();
   private static final List<String> soundMoves = RBMoveList.getSoundMoves();
   private static final List<String> flinchMoves = RBMoveList.getFlinchMoves();
   private static final List<String> thawingMoves = RBMoveList.getThawingMoves();
   private static final List<String> rechargeMoves = RBMoveList.getRechargeMoves();
   private static final List<String> recoveryMoves = RBMoveList.getRecoveryMoves();
   private static final List<String> megaStones = RBMoveList.getMegaStones();
   private static final List<String> ignoreDamageMoves = RBMoveList.getIgnoreDamageMoves();
   private static final List<String> ignoreSleepAbilities = RBMoveList.getIgnoreSleepAbilities();
   private static final List<String> statusMoves = RBMoveList.getStatusMoves();
   private static final List<String> criticalMoves = RBMoveList.getCriticalMoves();
   private static final List<String> directDamageMoves = RBMoveList.getDirectDamageMoves();
   private static boolean registered = false;
   private UUID lastBattleId = null;
   private RBStatStages battleStatStages = new RBStatStages();

   public RunBunAI(RunBunAIConfig config) {
      ModCommon.LOG.info("[AI] Raw config = {}", config);
      this.canTera = config.canTera();
      this.teraTarget = config.teraTarget();
   }

   public static void setHasResetDefault(boolean hasResetDefault) {
      RunBunAI.hasResetDefault = hasResetDefault;
   }

   public RunBunAI() {
      if (!registered) {
         CobblemonEvents.BATTLE_STARTED_PRE.subscribe(Priority.HIGH, event -> {
            System.out.println("Battle is about to start! Players: " + event.getBattle().getPlayers());
            setHasResetDefault(false);
            return Unit.INSTANCE;
         });
         registered = true;
      }
   }

   public void prepareForBattle(ActiveBattlePokemon activeBattlePokemon) {
      PokemonBattle battle = activeBattlePokemon.getBattle();
      UUID battleId = battle != null ? battle.getBattleId() : null;
      if (!Objects.equals(this.lastBattleId, battleId)) {
         ModCommon.LOG.info("RunBunAI: new battle detected, resetting state {}", battleId);
         this.selfInfo = null;
         this.battleSlots = null;
         this.battleTurn = 1;
         this.hasUsedTera = false;
         this.hasUsedMega = false;
         this.lastBattleId = battleId;
      }
   }

   @NotNull
   public ShowdownActionResponse choose(
      @NotNull ActiveBattlePokemon activeBattlePokemon,
      @Nullable PokemonBattle battle,
      @Nullable BattleSide aiSide,
      @Nullable ShowdownMoveset moveset,
      boolean forceSwitch
   ) {
      this.prepareForBattle(activeBattlePokemon);
      if (this.battleSlots == null) {
         this.battleSlots = new RBBattleSlots(activeBattlePokemon);
      }

      Iterable<ActiveBattlePokemon> actives = activeBattlePokemon.getAllActivePokemon();
      boolean[] assigned = new boolean[this.battleSlots.slots.length];

      for (ActiveBattlePokemon abp : actives) {
         if (abp != null) {
            BattlePokemon bp = abp.getBattlePokemon();
            if (bp != null) {
               this.battleStatStages.statStageContext(bp);
               int idx = RBBattleSlots.getSlot(abp, abp.getBattle());
               if (idx >= 0 && idx < this.battleSlots.slots.length) {
                  RBSlotInformation existing = this.battleSlots.slots[idx];
                  if (existing != null && existing.getBattlePokemon() != null && existing.getBattlePokemon().getUuid().equals(bp.getUuid())) {
                     assigned[idx] = true;
                  } else {
                     if (existing != null && existing.getBattlePokemon() != null) {
                        ModCommon.LOG
                           .warn(
                              "Reassigning slot {} from {} to {}",
                              new Object[]{idx, existing.getBattlePokemon().getName().getString(), bp.getName().getString()}
                           );
                     }

                     this.battleSlots.slots[idx] = new RBSlotInformation(abp);
                     assigned[idx] = true;
                  }
               } else {
                  ModCommon.LOG.warn("Skipping slot assignment: invalid idx {} for {}", idx, bp.getName().getString());
               }
            }
         }
      }

      String npcPokemonName = "";
      boolean isDoubles = RBBattleSlots.getAllActivePokemon(activeBattlePokemon).size() > 2;
      ActiveBattlePokemon npcActiveBP = null;
      int currentBattleSlot = RBBattleSlots.getSlot(activeBattlePokemon, activeBattlePokemon.getBattle());
      if (isDoubles) {
         List<ActiveBattlePokemon> npcAllies = RBBattleSlots.getAllies(activeBattlePokemon);
         if (!npcAllies.isEmpty()) {
            ActiveBattlePokemon NPCPartner = npcAllies.getFirst();
            this.partnerInfo = this.battleSlots.getBPInfo(NPCPartner.getBattlePokemon());
         } else {
            this.partnerInfo = null;
         }

         npcActiveBP = this.partnerInfo != null ? this.partnerInfo.getActiveBattlePokemon() : null;
      }

      BattlePokemon currentBP = activeBattlePokemon.getBattlePokemon();
      if (currentBP != null) {
         this.selfInfo = this.battleSlots.getBPInfo(currentBP);
      } else {
         this.selfInfo = null;
      }

      BattlePokemon battlePokemon = null;
      ModCommon.LOG.info("STARTED SHOWDOWN RESPONSE");
      if (activeBattlePokemon.getBattlePokemon() != null) {
         battlePokemon = activeBattlePokemon.getBattlePokemon();
         this.npcStages = this.battleStatStages.getStatMap(battlePokemon);
      }

      String currentHeldItem = "";
      String currentAbility = "";
      double activePokemonPercentHP = 0.0;
      double activePokemonCurrentHP = 0.0;
      String gimmick = null;
      boolean isAbilitySuppressed = PokeMathMax.isSuppressed(battlePokemon);
      ElementalTypes elementaltypes = ElementalTypes.INSTANCE;
      List<BattlePokemon> NPCParty = activeBattlePokemon.getActor().getPokemonList().stream().toList();
      List<BattlePokemon> aliveParty = activeBattlePokemon.getActor().getPokemonList().stream().filter(BattlePokemon::canBeSentOut).toList();
      Optional<ActiveBattlePokemon> opponentActiveBattlePokemon = StreamSupport.<ActiveBattlePokemon>stream(
            activeBattlePokemon.getAllActivePokemon().spliterator(), false
         )
         .filter(abp -> !abp.isAllied(activeBattlePokemon))
         .findFirst();
      List<ActiveBattlePokemon> allOpponentActiveBattlePokemon = RBBattleSlots.getOpponents(activeBattlePokemon);
      List<ActiveBattlePokemon> allNPCActiveBattlePokemon = RBBattleSlots.getAllies(activeBattlePokemon);
      ActiveBattlePokemon oppActiveBattlePokemon = null;
      if (!allOpponentActiveBattlePokemon.isEmpty()) {
         if (currentBattleSlot < allOpponentActiveBattlePokemon.size()) {
            oppActiveBattlePokemon = allOpponentActiveBattlePokemon.get(currentBattleSlot);
         } else {
            oppActiveBattlePokemon = allOpponentActiveBattlePokemon.getFirst();
         }
      }

      List<BattlePokemon> isAlive = new ArrayList<>();

      for (BattlePokemon saveAliveNPCBattlePokemon : NPCParty) {
         if (saveAliveNPCBattlePokemon.getHealth() > 0) {
            isAlive.add(saveAliveNPCBattlePokemon);
         }
      }

      if (battlePokemon != null) {
         if (this.pb == null) {
            this.pb = activeBattlePokemon.getBattle();
         }

         activePokemonPercentHP = getCurrentPercentHP(battlePokemon);
         activePokemonCurrentHP = battlePokemon.getHealth();
         currentAbility = battlePokemon.getEffectedPokemon().getAbility().getDisplayName();
      }

      List<BattlePokemon> canSwitchTo = new ArrayList<>();

      for (BattlePokemon entry : isAlive.stream().filter(entryx -> !entryx.isSentOut()).filter(entryx -> !entryx.getWillBeSwitchedIn()).toList()) {
         canSwitchTo.addLast(entry);
      }

      Map<BattlePokemon, Integer> switchingScores = new HashMap<>();
      boolean isSwitchMonFaster = false;
      boolean doesSwitchOHKO = false;
      boolean doesOppOHKO = false;
      BattlePokemon nextPokemon = null;
      ModCommon.LOG.info("SCORING SWITCHES");

      for (BattlePokemon possibleSwitch : canSwitchTo) {
         if (possibleSwitch != null) {
            int switchScore = 0;
            List<ActiveBattlePokemon> opponents = allOpponentActiveBattlePokemon;
            BattlePokemon slotOpponent = null;
            if (!opponents.isEmpty()) {
               int index = Math.min(currentBattleSlot, opponents.size() - 1);
               if (opponents.size() == 1) {
                  slotOpponent = opponents.getFirst().getBattlePokemon();
               } else {
                  slotOpponent = opponents.get(index).getBattlePokemon();
               }
            }

            if (slotOpponent != null) {
               if (battlePokemon != null && slotOpponent != null) {
                  if (Room.trickroom(battlePokemon)) {
                     isSwitchMonFaster = PokeMathMax.getEffectiveSpeed(possibleSwitch, this.battleStatStages.getStatMap(possibleSwitch))
                        <= PokeMathMax.getEffectiveSpeed(slotOpponent, this.battleStatStages.getStatMap(slotOpponent));
                  } else {
                     isSwitchMonFaster = PokeMathMax.getEffectiveSpeed(possibleSwitch, this.battleStatStages.getStatMap(possibleSwitch))
                        >= PokeMathMax.getEffectiveSpeed(slotOpponent, this.battleStatStages.getStatMap(slotOpponent));
                  }
               }

               if (possibleSwitch != null && slotOpponent != null) {
                  doesSwitchOHKO = isOHKO(possibleSwitch.getMoveSet().getMoves(), possibleSwitch, slotOpponent, activeBattlePokemon, this.battleStatStages);
               }

               int opponentIndex = currentBattleSlot % 2;
               if (opponentIndex < opponents.size() && slotOpponent != null) {
                  doesOppOHKO = isOHKO(slotOpponent.getMoveSet().getMoves(), slotOpponent, possibleSwitch, opponents.get(opponentIndex), this.battleStatStages);
               }

               if (isSwitchMonFaster && doesSwitchOHKO) {
                  switchScore += 5;
               } else if (!isSwitchMonFaster && !doesOppOHKO && doesSwitchOHKO) {
                  switchScore += 4;
               } else if (isSwitchMonFaster
                  && highestPercentDamageMove(possibleSwitch, slotOpponent, activeBattlePokemon, this.battleStatStages)
                     > highestPercentDamageMove(slotOpponent, possibleSwitch, activeBattlePokemon, this.battleStatStages)) {
                  switchScore += 3;
               } else if (!isSwitchMonFaster
                  && highestPercentDamageMove(possibleSwitch, slotOpponent, activeBattlePokemon, this.battleStatStages)
                     > highestPercentDamageMove(slotOpponent, possibleSwitch, activeBattlePokemon, this.battleStatStages)) {
                  switchScore += 2;
               } else if (isSwitchMonFaster) {
                  switchScore++;
               } else if (!isSwitchMonFaster && doesOppOHKO) {
                  switchScore--;
               }

               if (possibleSwitch.getName().toString().equals("ditto")) {
                  switchScore += 2;
               }

               if (isSwitchMonFaster
                  && !doesOppOHKO
                  && (possibleSwitch.getName().toString().equals("wynaut") || possibleSwitch.getName().toString().equals("wobbuffet"))) {
                  switchScore += 2;
               }

               if (battlePokemon != null) {
                  npcPokemonName = battlePokemon.getEntity().getPokemon().showdownId().toString();
                  String switchAbility = possibleSwitch.getEffectedPokemon().getAbility().getName();
                  ArrayList<String> sunAbilities = new ArrayList<>(List.of("drought", "orichalcumpulse"));
                  boolean sunSetter = !Weather.harshsunlight(battlePokemon) && PokeMathMax.hasAbility(sunAbilities, possibleSwitch);
                  boolean rainSetter = !Weather.rain(battlePokemon) && !Weather.heavyrain(battlePokemon) && switchAbility.equals("drizzle");
                  boolean snowSetter = !Weather.snow(battlePokemon) && switchAbility.equals("snowwarning");
                  boolean sandSetter = !Weather.sandstorm(battlePokemon) && switchAbility.equals("sandstream");
                  if (PokeMathMax.isSuppressed(possibleSwitch)) {
                     continue;
                  }

                  if (sunSetter || rainSetter || snowSetter || sandSetter) {
                     switchScore++;
                  }
               }

               switchingScores.put(possibleSwitch, switchScore);
               String displayP = possibleSwitch.getName().getString();
               ModCommon.LOG.info("Possible Switch:  -> {}  ->  Score: {}", displayP, switchScore);
            }
         }
      }

      int maxSwitchingScore = switchingScores.values().stream().max(Integer::compareTo).orElse(Integer.MIN_VALUE);
      List<BattlePokemon> bestSwitches = switchingScores.entrySet().stream().filter(entry -> entry.getValue() == maxSwitchingScore).map(Entry::getKey).toList();
      if (!bestSwitches.isEmpty()) {
         nextPokemon = bestSwitches.getFirst();
      }

      if (!forceSwitch && !activeBattlePokemon.isGone()) {
         String history = RBSlotInformation.getMoveHistoryName(battlePokemon, true);
         if (!history.isEmpty()) {
            changeTurn(this.selfInfo, history);
         }

         if (moveset == null) {
            return PassActionResponse.INSTANCE;
         }

         if (moveset.moves.size() == 1 && ((InBattleMove)moveset.moves.getFirst()).getId().equals("recharge")) {
            changeTurn(this.selfInfo, "recharge");
            return new MoveActionResponse("recharge", null, gimmick);
         }

         ModCommon.LOG.info("DECIDING MOVE FOR {}", npcPokemonName);
         List<InBattleMove> inBattleMoves = moveset.moves.stream().filter(InBattleMove::canBeUsed).filter(inBattleMove -> {
            List<Targetable> targetList = (List<Targetable>)inBattleMove.getTarget().getTargetList().invoke(activeBattlePokemon);
            return inBattleMove.mustBeUsed() || targetList == null || !targetList.isEmpty();
         }).toList();
         if (inBattleMoves.isEmpty()) {
            changeTurn(this.selfInfo, "struggle");
            return new MoveActionResponse("struggle", null, gimmick);
         }

         if (allOpponentActiveBattlePokemon.isEmpty()) {
            changeTurn(this.selfInfo, "Pass");
            return new MoveActionResponse(inBattleMoves.get(this.RANDOM.nextInt(moveset.moves.size())).id, null, gimmick);
         }

         Map<InBattleMove, Move> moveMap = new HashMap<>();
         IntStream.range(0, inBattleMoves.size())
            .forEach(
               i -> moveMap.put(
                  inBattleMoves.get(i), Moves.all().stream().filter(move -> move.getName().equals(inBattleMoves.get(i).getId())).findFirst().get().create()
               )
            );
         if (battlePokemon.getHeldItemManager().showdownId(battlePokemon) != null) {
            currentHeldItem = battlePokemon.getHeldItemManager().showdownId(battlePokemon) != null
               ? battlePokemon.getHeldItemManager().showdownId(battlePokemon)
               : "";
            if (megaStones.contains(currentHeldItem) && !this.hasUsedMega) {
               gimmick = Gimmick.MEGA_EVOLUTION.getId();
               this.hasUsedMega = true;
               this.selfInfo.setHasUsedMega(true);
            }
         }

         BattlePokemon teraMatch = aliveParty.stream()
            .filter(bp -> bp.getEffectedPokemon().showdownId().equalsIgnoreCase(this.teraTarget))
            .findFirst()
            .orElse(null);
         boolean isTeraAvailable = false;
         if ((this.canTera || !this.teraTarget.isEmpty())
            && !this.selfInfo.hasUsedMega()
            && gimmick == null
            && !this.hasUsedTera
            && activeBattlePokemon != null) {
            if (this.teraTarget.equalsIgnoreCase(activeBattlePokemon.getBattlePokemon().getEffectedPokemon().showdownId())) {
               gimmick = Gimmick.TERASTALLIZATION.getId();
               this.selfInfo.setHasUsedTera(true);
               this.hasUsedTera = true;
            } else if (teraMatch == null) {
               isTeraAvailable = true;
            }
         }

         List<RunBunAI.MoveEvaluation> killingMoves = new ArrayList<>();
         List<RunBunAI.MoveEvaluation> nonKillingPossibleMoves = new ArrayList<>();
         List<RunBunAI.MoveEvaluation> evaluations = new ArrayList<>();

         for (ActiveBattlePokemon target : allOpponentActiveBattlePokemon) {
            BattlePokemon oppBP = target.getBattlePokemon();
            if (!target.isGone()) {
               boolean isFaster = false;
               if (battlePokemon != null) {
                  if (Room.trickroom(battlePokemon)) {
                     isFaster = PokeMathMax.getEffectiveSpeed(battlePokemon, this.battleStatStages.getStatMap(battlePokemon))
                        <= PokeMathMax.getEffectiveSpeed(oppBP, this.battleStatStages.getStatMap(oppBP));
                  } else {
                     isFaster = PokeMathMax.getEffectiveSpeed(battlePokemon, this.battleStatStages.getStatMap(battlePokemon))
                        >= PokeMathMax.getEffectiveSpeed(oppBP, this.battleStatStages.getStatMap(oppBP));
                  }
               }

               boolean dying = isOHKO(oppBP.getMoveSet().getMoves(), oppBP, battlePokemon, activeBattlePokemon, this.battleStatStages);

               for (InBattleMove inBattleMove : moveMap.keySet()) {
                  List<Targetable> validTargets = (List<Targetable>)inBattleMove.getTarget().getTargetList().invoke(activeBattlePokemon);
                  if ((validTargets == null || validTargets.isEmpty() || validTargets.contains(target))
                     && !RBMoveList.getAllyTargetingMoves().contains(inBattleMove.getId())) {
                     Move m = moveMap.get(inBattleMove);
                     int score = 0;
                     boolean willTera = false;
                     if (isTeraAvailable) {
                        if (!isFaster && dying) {
                           boolean Teraohko = isOHKOTera(oppBP.getMoveSet().getMoves(), oppBP, battlePokemon, activeBattlePokemon, false, this.battleStatStages);
                           boolean enemyOHKO = isOHKO(battlePokemon.getMoveSet().getMoves(), battlePokemon, oppBP, activeBattlePokemon, this.battleStatStages);
                           if (!Teraohko && enemyOHKO) {
                              willTera = true;
                           }
                        }

                        if (aliveParty.isEmpty()) {
                           willTera = true;
                        }

                        if (!willTera) {
                           int nonTeraDamage = PokeMathMax.damage(battlePokemon, oppBP, m, activeBattlePokemon, false, true, this.battleStatStages);
                           int TeraDamage = PokeMathMax.damage(battlePokemon, oppBP, m, activeBattlePokemon, true, true, this.battleStatStages);
                           if (oppBP.getHealth() > nonTeraDamage && oppBP.getHealth() <= TeraDamage && isFaster) {
                              willTera = true;
                           }
                        }
                     }

                     int dmg = PokeMathMax.damage(battlePokemon, oppBP, m, activeBattlePokemon, willTera, true, this.battleStatStages);
                     if (ignoreDamageMoves.contains(m.getName()) || trapMoves.contains(m.getName())) {
                        dmg = 0;
                     }

                     boolean isOpTera = BattleStates.get(oppBP.actor.battle).getPokemonState(oppBP).has(Custom.TERA);
                     boolean immune = PokeMathMax.isImmuneCheck(m, currentBP, oppBP, activeBattlePokemon, PokeMathMax.teraToElementalType(oppBP), isOpTera);
                     RunBunAI.MoveEvaluation moveEval = new RunBunAI.MoveEvaluation(
                        m, inBattleMove, target, dmg, immune, score, willTera, isFaster, this.battleStatStages
                     );
                     evaluations.add(moveEval);
                     if (dmg >= target.getBattlePokemon().getHealth()) {
                        killingMoves.add(moveEval);
                     } else {
                        nonKillingPossibleMoves.add(moveEval);
                     }
                  }
               }
            }
         }

         if (isDoubles) {
            for (InBattleMove inBattleMove : moveMap.keySet()) {
               if (RBMoveList.getAllyTargetingMoves().contains(inBattleMove.getId())
                  && this.partnerInfo != null
                  && this.partnerInfo.getActiveBattlePokemon() != null
                  && this.partnerInfo.getBattlePokemon() != null) {
                  boolean isFaster = false;
                  if (battlePokemon != null) {
                     if (Room.trickroom(battlePokemon)) {
                        isFaster = PokeMathMax.getEffectiveSpeed(battlePokemon, this.battleStatStages.getStatMap(battlePokemon))
                           <= PokeMathMax.getEffectiveSpeed(
                              this.partnerInfo.getBattlePokemon(), this.battleStatStages.getStatMap(this.partnerInfo.getBattlePokemon())
                           );
                     } else {
                        isFaster = PokeMathMax.getEffectiveSpeed(battlePokemon, this.battleStatStages.getStatMap(battlePokemon))
                           >= PokeMathMax.getEffectiveSpeed(
                              this.partnerInfo.getBattlePokemon(), this.battleStatStages.getStatMap(this.partnerInfo.getBattlePokemon())
                           );
                     }
                  }

                  RunBunAI.MoveEvaluation allyEval = new RunBunAI.MoveEvaluation(
                     moveMap.get(inBattleMove), inBattleMove, this.partnerInfo.getActiveBattlePokemon(), 0, false, 0, false, isFaster, this.battleStatStages
                  );
                  if (allyEval != null) {
                     evaluations.add(allyEval);
                  }
               }
            }
         }

         boolean npcIsOHKO = false;
         boolean npcIs2OHKO = false;
         boolean npcIs3OHKO = false;
         boolean npcIsOHKOWithSS = false;
         boolean npcIsOHKOWithBD = false;

         for (ActiveBattlePokemon opp : allOpponentActiveBattlePokemon) {
            if (opp != null && !opp.isGone()) {
               List<Move> oppM = opp.getBattlePokemon().getMoveSet().getMoves();
               if (isOHKO(oppM, opp.getBattlePokemon(), battlePokemon, activeBattlePokemon, this.battleStatStages)) {
                  npcIsOHKO = true;
               }

               if (is2HKO(oppM, opp.getBattlePokemon(), battlePokemon, opp, this.battleStatStages)) {
                  npcIs2OHKO = true;
               }

               if (is3HKO(oppM, opp.getBattlePokemon(), battlePokemon, opp, this.battleStatStages)) {
                  npcIs3OHKO = true;
               }

               if (wouldBeOHKOAfterShellSmash(oppM, opp.getBattlePokemon(), battlePokemon, opp, this.battleStatStages)) {
                  npcIsOHKOWithSS = true;
               }

               if (isOHKOAfterBellyDrum(oppM, opp.getBattlePokemon(), battlePokemon, opp, this.battleStatStages)) {
                  npcIsOHKOWithBD = true;
               }
            }
         }

         int maxDamage = 0;
         RunBunAI.MoveEvaluation maxMove = null;
         if (killingMoves.isEmpty()) {
            for (RunBunAI.MoveEvaluation currentMove : evaluations) {
               maxDamage = Math.max(maxDamage, currentMove.getDamage());
               if (maxDamage == currentMove.getDamage()) {
                  maxMove = currentMove;
               }
            }
         }

         for (RunBunAI.MoveEvaluation move : evaluations) {
            Move currentMove = move.getMove();
            int score = 0;
            BattlePokemon opponent = move.getOpponent().getBattlePokemon();
            String getOpponentHeldItem = opponent.getHeldItemManager().showdownId(opponent) != null ? opponent.getHeldItemManager().showdownId(opponent) : "";
            List<Move> oppMoves = opponent != null ? opponent.getMoveSet().getMoves() : Collections.emptyList();
            double oppMaxDamage = highestPercentDamageMove(opponent, activeBattlePokemon.getBattlePokemon(), oppActiveBattlePokemon, this.battleStatStages);
            String opponentAbility = opponent != null ? opponent.getEffectedPokemon().getAbility().getName() : "";
            double oppPercentHP = opponent != null ? (double)opponent.getHealth() / opponent.getMaxHealth() : 0.0;
            boolean isOpponentSuppressed = PokeMathMax.isSuppressed(opponent);
            boolean oppHasSpecialMove = false;
            boolean oppHasPhysicalMove = false;
            String damageCategory = "";
            boolean isFaster = move.isFaster;
            boolean isOPFrozen = Status.frz(opponent);
            boolean isOPSleeping = Status.slp(opponent);
            boolean isFirstTurnOut = this.selfInfo.getTurnsForActivePokemon() == 1;
            String moveUsedLastTurn = this.selfInfo.getMoveHistory().getOrDefault(this.selfInfo.getTurnsForActivePokemon() - 1, "");
            String moveUsed2TurnsAgo = this.selfInfo.getMoveHistory().getOrDefault(this.selfInfo.getTurnsForActivePokemon() - 2, "");
            String moveUsed3TurnsAgo = this.selfInfo.getMoveHistory().getOrDefault(this.selfInfo.getTurnsForActivePokemon() - 3, "");
            Map<Stats, Integer> opponentStages = this.battleStatStages.getStatMap(opponent);

            for (Move opponentMove : oppMoves) {
               damageCategory = opponentMove.getDamageCategory().getName();
               if (damageCategory.equals(DamageCategories.INSTANCE.getPHYSICAL().getName())) {
                  oppHasPhysicalMove = true;
               }

               if (damageCategory.equals(DamageCategories.INSTANCE.getSPECIAL().getName())) {
                  oppHasSpecialMove = true;
               }
            }

            if (killingMoves.contains(move)) {
               double roll = this.RANDOM.nextDouble();
               score = roll > 0.2 ? 6 : 8;
               if (isFaster) {
                  score += 6;
               } else if (priorityDamageMoves.contains(currentMove.getName())) {
                  score += 6;
               } else {
                  score += 3;
               }

               if (currentMove.getName().equals("pursuit")) {
                  score = 10;
               }
            }

            if (!nonKillingPossibleMoves.isEmpty()) {
               if (nonKillingPossibleMoves.contains(move)) {
                  String moveID = currentMove.getName();
                  double roll = this.RANDOM.nextDouble();
                  if (maxMove != null && maxMove.getMove() == currentMove) {
                     roll = this.RANDOM.nextDouble();
                     score += roll > 0.2 ? 6 : 8;
                  }

                  if (trapMoves.contains(moveID)) {
                     score += roll > 0.2 ? 6 : 8;
                  }

                  if (speedReductionMoves.contains(moveID)) {
                     if (move.getDamage() == maxDamage) {
                        roll = this.RANDOM.nextDouble();
                        score += roll > 0.2 ? 6 : 8;
                     } else {
                        score += (!ignoreStatDropAbilities.contains(opponentAbility) || PokeMathMax.isSuppressed(opponent)) && !isFaster ? 6 : 5;
                     }
                  }

                  if (physicalAttackReductionMoves.contains(moveID) || specialAttackReductionMoves.contains(moveID)) {
                     if (move.getDamage() == maxDamage) {
                        roll = this.RANDOM.nextDouble();
                        score += roll > 0.2 ? 6 : 8;
                     } else if (ignoreStatDropAbilities.contains(opponentAbility) && !PokeMathMax.isSuppressed(opponent)) {
                        if (ignoreStatDropAbilities.contains(opponentAbility) && !PokeMathMax.isSuppressed(opponent)) {
                           score += 5;
                        }
                     } else if (specialAttackReductionMoves.contains(moveID) && oppHasSpecialMove) {
                        score += 6;
                     } else if (physicalAttackReductionMoves.contains(moveID) && oppHasPhysicalMove) {
                        score += 6;
                     }
                  }

                  boolean hasFocusSash = "focussash".equals(currentHeldItem)
                     || "sturdy".equals(currentAbility) && activePokemonPercentHP == 100.0 && !isAbilitySuppressed;
                  String isRecharging = moveID;
                  byte isLoafing = -1;
                  switch (isRecharging.hashCode()) {
                     case -2123919667:
                        if (isRecharging.equals("earthquake")) {
                           isLoafing = 26;
                        }
                        break;
                     case -2016783856:
                        if (isRecharging.equals("magnitude")) {
                           isLoafing = 25;
                        }
                        break;
                     case -1987756836:
                        if (isRecharging.equals("grasswhistle")) {
                           isLoafing = 66;
                        }
                        break;
                     case -1940419096:
                        if (isRecharging.equals("stealthrock")) {
                           isLoafing = 8;
                        }
                        break;
                     case -1830275358:
                        if (isRecharging.equals("selfdestruct")) {
                           isLoafing = 48;
                        }
                        break;
                     case -1762966260:
                        if (isRecharging.equals("willowisp")) {
                           isLoafing = 56;
                        }
                        break;
                     case -1722028251:
                        if (isRecharging.equals("sunnyday")) {
                           isLoafing = 39;
                        }
                        break;
                     case -1688483655:
                        if (isRecharging.equals("firstimpression")) {
                           isLoafing = 82;
                        }
                        break;
                     case -1673690389:
                        if (isRecharging.equals("stunspore")) {
                           isLoafing = 52;
                        }
                        break;
                     case -1654232496:
                        if (isRecharging.equals("finalgambit")) {
                           isLoafing = 33;
                        }
                        break;
                     case -1613345534:
                        if (isRecharging.equals("lightscreen")) {
                           isLoafing = 44;
                        }
                        break;
                     case -1589842583:
                        if (isRecharging.equals("burningbulwark")) {
                           isLoafing = 18;
                        }
                        break;
                     case -1554999569:
                        if (isRecharging.equals("fellstinger")) {
                           isLoafing = 6;
                        }
                        break;
                     case -1552694109:
                        if (isRecharging.equals("captivate")) {
                           isLoafing = 76;
                        }
                        break;
                     case -1545448972:
                        if (isRecharging.equals("shadowsneak")) {
                           isLoafing = 22;
                        }
                        break;
                     case -1360065020:
                        if (isRecharging.equals("painsplit")) {
                           isLoafing = 78;
                        }
                        break;
                     case -1335220573:
                        if (isRecharging.equals("detect")) {
                           isLoafing = 16;
                        }
                        break;
                     case -1205218440:
                        if (isRecharging.equals("electricterrain")) {
                           isLoafing = 34;
                        }
                        break;
                     case -1098229980:
                        if (isRecharging.equals("toxicspikes")) {
                           isLoafing = 10;
                        }
                        break;
                     case -1097452470:
                        if (isRecharging.equals("lockon")) {
                           isLoafing = 79;
                        }
                        break;
                     case -1093871806:
                        if (isRecharging.equals("revivalblessing")) {
                           isLoafing = 74;
                        }
                        break;
                     case -1084941351:
                        if (isRecharging.equals("fakeout")) {
                           isLoafing = 31;
                        }
                        break;
                     case -1033953536:
                        if (isRecharging.equals("nuzzle")) {
                           isLoafing = 54;
                        }
                        break;
                     case -1019013069:
                        if (isRecharging.equals("iceshard")) {
                           isLoafing = 24;
                        }
                        break;
                     case -922274813:
                        if (isRecharging.equals("spikyshield")) {
                           isLoafing = 14;
                        }
                        break;
                     case -895946451:
                        if (isRecharging.equals("spikes")) {
                           isLoafing = 9;
                        }
                        break;
                     case -873960490:
                        if (isRecharging.equals("tickle")) {
                           isLoafing = 59;
                        }
                        break;
                     case -814667500:
                        if (isRecharging.equals("blizzard")) {
                           isLoafing = 0;
                        }
                        break;
                     case -760315299:
                        if (isRecharging.equals("aquajet")) {
                           isLoafing = 23;
                        }
                        break;
                     case -759189842:
                        if (isRecharging.equals("trickroom")) {
                           isLoafing = 30;
                        }
                        break;
                     case -741038950:
                        if (isRecharging.equals("substitute")) {
                           isLoafing = 46;
                        }
                        break;
                     case -737413113:
                        if (isRecharging.equals("wakeupslap")) {
                           isLoafing = 87;
                        }
                        break;
                     case -694469544:
                        if (isRecharging.equals("tailwind")) {
                           isLoafing = 29;
                        }
                        break;
                     case -694304609:
                        if (isRecharging.equals("raindance")) {
                           isLoafing = 38;
                        }
                        break;
                     case -668783514:
                        if (isRecharging.equals("zapcannon")) {
                           isLoafing = 55;
                        }
                        break;
                     case -663837379:
                        if (isRecharging.equals("lovelykiss")) {
                           isLoafing = 64;
                        }
                        break;
                     case -641723388:
                        if (isRecharging.equals("shedtail")) {
                           isLoafing = 81;
                        }
                        break;
                     case -628899131:
                        if (isRecharging.equals("batonpass")) {
                           isLoafing = 28;
                        }
                        break;
                     case -486445638:
                        if (isRecharging.equals("futuresight")) {
                           isLoafing = 1;
                        }
                        break;
                     case -419866705:
                        if (isRecharging.equals("imprison")) {
                           isLoafing = 27;
                        }
                        break;
                     case -397641835:
                        if (isRecharging.equals("mindreader")) {
                           isLoafing = 80;
                        }
                        break;
                     case -355666862:
                        if (isRecharging.equals("grassyterrain")) {
                           isLoafing = 36;
                        }
                        break;
                     case -346775423:
                        if (isRecharging.equals("switcheroo")) {
                           isLoafing = 58;
                        }
                        break;
                     case -309012785:
                        if (isRecharging.equals("protect")) {
                           isLoafing = 13;
                        }
                        break;
                     case -265756502:
                        if (isRecharging.equals("roleplay")) {
                           isLoafing = 21;
                        }
                        break;
                     case -241129705:
                        if (isRecharging.equals("suckerpunch")) {
                           isLoafing = 3;
                        }
                        break;
                     case -220368998:
                        if (isRecharging.equals("pursuit")) {
                           isLoafing = 5;
                        }
                        break;
                     case -200045235:
                        if (isRecharging.equals("ruination")) {
                           isLoafing = 73;
                        }
                        break;
                     case -107999143:
                        if (isRecharging.equals("venomdrench")) {
                           isLoafing = 88;
                        }
                        break;
                     case -106082979:
                        if (isRecharging.equals("hypnosis")) {
                           isLoafing = 63;
                        }
                        break;
                     case -18443741:
                        if (isRecharging.equals("snowscape")) {
                           isLoafing = 43;
                        }
                        break;
                     case -11788285:
                        if (isRecharging.equals("sleeptalk")) {
                           isLoafing = 83;
                        }
                        break;
                     case 3194844:
                        if (isRecharging.equals("hail")) {
                           isLoafing = 40;
                        }
                        break;
                     case 3701727:
                        if (isRecharging.equals("yawn")) {
                           isLoafing = 60;
                        }
                        break;
                     case 97520988:
                        if (isRecharging.equals("fling")) {
                           isLoafing = 20;
                        }
                        break;
                     case 98436943:
                        if (isRecharging.equals("glare")) {
                           isLoafing = 53;
                        }
                        break;
                     case 99118746:
                        if (isRecharging.equals("chillyreception")) {
                           isLoafing = 41;
                        }
                        break;
                     case 109592231:
                        if (isRecharging.equals("snore")) {
                           isLoafing = 84;
                        }
                        break;
                     case 109651813:
                        if (isRecharging.equals("spore")) {
                           isLoafing = 65;
                        }
                        break;
                     case 110553911:
                        if (isRecharging.equals("toxic")) {
                           isLoafing = 69;
                        }
                        break;
                     case 110628691:
                        if (isRecharging.equals("trick")) {
                           isLoafing = 57;
                        }
                        break;
                     case 151462041:
                        if (isRecharging.equals("mistyterrain")) {
                           isLoafing = 37;
                        }
                        break;
                     case 205981448:
                        if (isRecharging.equals("relicsong")) {
                           isLoafing = 2;
                        }
                        break;
                     case 239133899:
                        if (isRecharging.equals("stickyweb")) {
                           isLoafing = 11;
                        }
                        break;
                     case 309351808:
                        if (isRecharging.equals("psychicterrain")) {
                           isLoafing = 35;
                        }
                        break;
                     case 333722389:
                        if (isRecharging.equals("explosion")) {
                           isLoafing = 47;
                        }
                        break;
                     case 362458984:
                        if (isRecharging.equals("obstruct")) {
                           isLoafing = 19;
                        }
                        break;
                     case 452665090:
                        if (isRecharging.equals("silktrap")) {
                           isLoafing = 15;
                        }
                        break;
                     case 535075908:
                        if (isRecharging.equals("banefulbunker")) {
                           isLoafing = 17;
                        }
                        break;
                     case 948979769:
                        if (isRecharging.equals("memento")) {
                           isLoafing = 50;
                        }
                        break;
                     case 957830652:
                        if (isRecharging.equals("counter")) {
                           isLoafing = 70;
                        }
                        break;
                     case 972693474:
                        if (isRecharging.equals("dreameater")) {
                           isLoafing = 85;
                        }
                        break;
                     case 1085265597:
                        if (isRecharging.equals("reflect")) {
                           isLoafing = 45;
                        }
                        break;
                     case 1153539009:
                        if (isRecharging.equals("poisonpowder")) {
                           isLoafing = 68;
                        }
                        break;
                     case 1314902610:
                        if (isRecharging.equals("worryseed")) {
                           isLoafing = 75;
                        }
                        break;
                     case 1363430640:
                        if (isRecharging.equals("helpinghand")) {
                           isLoafing = 32;
                        }
                        break;
                     case 1366466271:
                        if (isRecharging.equals("nightmare")) {
                           isLoafing = 86;
                        }
                        break;
                     case 1377108401:
                        if (isRecharging.equals("rollout")) {
                           isLoafing = 7;
                        }
                        break;
                     case 1455096912:
                        if (isRecharging.equals("sleeppowder")) {
                           isLoafing = 62;
                        }
                        break;
                     case 1567689337:
                        if (isRecharging.equals("metalburst")) {
                           isLoafing = 72;
                        }
                        break;
                     case 1663888101:
                        if (isRecharging.equals("sandstorm")) {
                           isLoafing = 42;
                        }
                        break;
                     case 1671308008:
                        if (isRecharging.equals("disable")) {
                           isLoafing = 77;
                        }
                        break;
                     case 1703830225:
                        if (isRecharging.equals("poisongas")) {
                           isLoafing = 67;
                        }
                        break;
                     case 1742050826:
                        if (isRecharging.equals("darkvoid")) {
                           isLoafing = 61;
                        }
                        break;
                     case 1744970206:
                        if (isRecharging.equals("mirrorcoat")) {
                           isLoafing = 71;
                        }
                        break;
                     case 1803630941:
                        if (isRecharging.equals("shelltrap")) {
                           isLoafing = 89;
                        }
                        break;
                     case 1983215481:
                        if (isRecharging.equals("mistyexplosion")) {
                           isLoafing = 49;
                        }
                        break;
                     case 2017242428:
                        if (isRecharging.equals("thunderclap")) {
                           isLoafing = 4;
                        }
                        break;
                     case 2017828317:
                        if (isRecharging.equals("thunderwave")) {
                           isLoafing = 51;
                        }
                        break;
                     case 2085329445:
                        if (isRecharging.equals("kingsshield")) {
                           isLoafing = 12;
                        }
                  }

                  label2971: {
                     switch (isLoafing) {
                        case 0:
                           if (battlePokemon != null) {
                              score += !Weather.snow(battlePokemon) && !Weather.hail(battlePokemon) ? 6 : 7;
                           }
                        case 1:
                           score += isFaster && npcIsOHKO ? 8 : 6;
                           break label2971;
                        case 2:
                           if (battlePokemon != null) {
                              score += "meloetta".equals(battlePokemon.getName().getString()) ? 10 : 0;
                           }
                           break label2971;
                        case 3:
                        case 4:
                           if (moveUsedLastTurn.equals("suckerpunch") || moveUsedLastTurn.equals("thunderclap")) {
                              score += roll < 0.5 ? -20 : 0;
                           }
                           break label2971;
                        case 5:
                           if (oppPercentHP <= 0.2) {
                              score += 10;
                           } else if (oppPercentHP <= 0.4) {
                              roll = this.RANDOM.nextDouble();
                              score += roll < 0.5 ? 8 : 0;
                           }
                           break label2971;
                        case 6:
                           roll = this.RANDOM.nextDouble();
                           int result1 = roll > 0.8 ? 23 : 21;
                           int result2 = roll > 0.8 ? 17 : 15;
                           if (this.npcStages.get(Stats.ATTACK) != 6) {
                              score += isFaster ? result1 : result2;
                           }
                           break label2971;
                        case 7:
                           score += 7;
                           break label2971;
                        case 8:
                           roll = this.RANDOM.nextDouble();
                           if (Hazard.stealthrock(move.getOpponent().getBattlePokemon()) != 0) {
                              score -= 20;
                              break label2971;
                           }

                           if (isFirstTurnOut && Hazard.stealthrock(move.getOpponent().getBattlePokemon()) == 0) {
                              score += roll > 0.75 ? 8 : 9;
                              break label2971;
                           }

                           score += roll > 0.75 ? 6 : 7;
                           break label2971;
                        case 9:
                           roll = this.RANDOM.nextDouble();
                           int spikesCount = Hazard.spikes(move.getOpponent().getBattlePokemon());
                           if (spikesCount == 3) {
                              score -= 20;
                           } else {
                              if (spikesCount > 0) {
                                 score--;
                              }

                              if (isFirstTurnOut) {
                                 score += roll > 0.75 ? 8 : 9;
                              } else {
                                 score += roll > 0.75 ? 6 : 7;
                              }
                           }
                           break label2971;
                        case 10:
                           roll = this.RANDOM.nextDouble();
                           int toxicspikesCount = Hazard.toxicspikes(move.getOpponent().getBattlePokemon());
                           if (toxicspikesCount == 3) {
                              score -= 20;
                           } else {
                              if (toxicspikesCount > 0) {
                                 score--;
                              }

                              if (isFirstTurnOut) {
                                 score += roll > 0.75 ? 8 : 9;
                              } else {
                                 score += roll > 0.75 ? 6 : 7;
                              }
                           }
                           break label2971;
                        case 11:
                           roll = this.RANDOM.nextDouble();
                           if (Hazard.stickyweb(move.getOpponent().getBattlePokemon()) != 0) {
                              score -= 20;
                           } else if (isFirstTurnOut) {
                              score += roll > 0.75 ? 9 : 12;
                           } else {
                              score += roll > 0.75 ? 6 : 9;
                           }
                           break label2971;
                        case 12:
                        case 13:
                        case 14:
                        case 15:
                        case 16:
                        case 17:
                        case 18:
                        case 19:
                           score += 6;
                           if (Volatile.cursed(battlePokemon)
                              || Volatile.yawn(battlePokemon)
                              || Volatile.leech(battlePokemon)
                              || Volatile.attract(battlePokemon)
                              || Status.brn(battlePokemon)
                              || Status.psn(battlePokemon)
                              || Status.tox(battlePokemon)) {
                              if (activePokemonPercentHP <= 25.0) {
                                 score -= 20;
                                 break label2971;
                              }

                              score -= 2;
                           }

                           if (Volatile.cursed(opponent)
                              || Volatile.yawn(opponent)
                              || Volatile.leech(opponent)
                              || Volatile.attract(opponent)
                              || Status.brn(opponent)
                              || Status.psn(opponent)
                              || Status.tox(opponent)) {
                              score++;
                           }

                           if (PokeMathMax.isSandstormFatal(battlePokemon)) {
                              score -= 20;
                           } else if (PokeMathMax.isHailFatal(battlePokemon)) {
                              score -= 20;
                           }

                           if (!isDoubles && isFirstTurnOut) {
                              score--;
                           }

                           if (this.selfInfo.getMoveHistory().isEmpty()) {
                              break label2971;
                           }

                           if (Objects.equals(moveUsed2TurnsAgo, moveID) && Objects.equals(moveUsedLastTurn, moveID)) {
                              score -= 20;
                              break label2971;
                           }

                           if (!Objects.equals(moveUsed2TurnsAgo, moveID) && Objects.equals(moveUsedLastTurn, moveID)) {
                              score += roll > 0.5 ? -20 : 0;
                           }
                           break label2971;
                        case 20:
                           if (Objects.equals(currentHeldItem, "")) {
                              score -= 20;
                           }
                           break label2971;
                        case 21:
                           if (this.partnerInfo == null) {
                              break label2971;
                           }

                           Set<String> roleplayAbilities = Set.of("hugepower", "purepower", "protean", "toughclaws");
                           if (!isAbilitySuppressed && !PokeMathMax.isSuppressed(this.partnerInfo.getBattlePokemon())) {
                              if (!roleplayAbilities.contains(currentAbility) && roleplayAbilities.contains(this.partnerInfo.getAbility().getName())) {
                                 score += 9;
                                 break label2971;
                              }

                              score -= 20;
                              break label2971;
                           }

                           score -= 20;
                           break label2971;
                        case 22:
                        case 23:
                        case 24:
                           if (this.partnerInfo != null
                              && npcActiveBP != null
                              && "weaknesspolicy".equals(this.partnerInfo.getHeldItem())
                              && TypeChart.getEffectiveness(currentMove.getType(), npcActiveBP.getBattlePokemon()) > 1.0) {
                              score = 12;
                           }
                           break label2971;
                        case 25:
                        case 26:
                           score += 6;
                           if (isDoubles && this.partnerInfo != null && this.partnerInfo.getBattlePokemon() != null) {
                              if (PokeMathMax.isImmuneCheck(
                                 move.getMove(),
                                 battlePokemon,
                                 this.partnerInfo.getBattlePokemon(),
                                 activeBattlePokemon,
                                 PokeMathMax.teraToElementalType(this.partnerInfo.getBattlePokemon()),
                                 false
                              )) {
                                 score += 2;
                              } else if (RBTypeChart.getEffectiveness(ElementalTypes.GROUND, this.partnerInfo.getBattlePokemon()) == 2.0) {
                                 score -= 10;
                              } else {
                                 score -= 3;
                              }
                           }
                           break label2971;
                        case 27:
                           int commonMoves = 0;

                           for (Move imprisonSet : opponent.getMoveSet()) {
                              if (battlePokemon.getMoveSet().getMoves().contains(imprisonSet)) {
                                 commonMoves++;
                              }
                           }

                           score += commonMoves > 0 ? 9 : -20;
                           break label2971;
                        case 28:
                           if (aliveParty.isEmpty()) {
                              score -= 20;
                              break label2971;
                           }

                           Map<Stats, Integer> statChanges = this.battleStatStages.getStatMap(battlePokemon);
                           boolean anyPositive = false;

                           for (Integer value : statChanges.values()) {
                              if (value > 0) {
                                 anyPositive = true;
                                 break;
                              }
                           }

                           if (!aliveParty.isEmpty() && (anyPositive || hasVolatile(battlePokemon, "substitute"))) {
                              score += 14;
                           }
                           break label2971;
                        case 29:
                           if (Tailwind.tailwind(battlePokemon)) {
                              score -= 20;
                           }

                           if (isPartySlowerThanOpponent(
                              this.battleSlots.getAllySlotInfos(activeBattlePokemon),
                              this.battleSlots.getOpponentSlotInfos(activeBattlePokemon),
                              this.battleStatStages
                           )) {
                              score += 9;
                           } else {
                              score += 5;
                           }
                           break label2971;
                        case 30:
                           if (Room.trickroom(battlePokemon)) {
                              score -= 20;
                           } else if (isPartySlowerThanOpponent(
                              this.battleSlots.getAllySlotInfos(activeBattlePokemon),
                              this.battleSlots.getOpponentSlotInfos(activeBattlePokemon),
                              this.battleStatStages
                           )) {
                              score += 10;
                           } else {
                              score += 5;
                           }
                           break label2971;
                        case 31:
                           if (PokeMathMax.isSuppressed(opponent) && isFirstTurnOut) {
                              score += 9;
                           } else if ((
                                 !opponentAbility.equals("shielddust") || !opponentAbility.equals("innerfocus") || !getOpponentHeldItem.equals("covertcloak")
                              )
                              && isFirstTurnOut) {
                              score += 9;
                           } else {
                              score -= 20;
                           }
                        case 32:
                        case 74:
                        default:
                           break label2971;
                        case 33:
                           if (isFaster && battlePokemon.getHealth() >= opponent.getHealth()) {
                              score += 8;
                              break label2971;
                           }

                           if (isFaster && npcIsOHKO) {
                              score += 7;
                              break label2971;
                           }

                           score += 6;
                           break label2971;
                        case 34:
                           if (Terrain.electricterrain(battlePokemon)) {
                              break label2971;
                           }

                           if (currentHeldItem != null && currentHeldItem.equals("terrainextender")) {
                              score += 9;
                              break label2971;
                           }

                           score += 8;
                           break label2971;
                        case 35:
                           if (Terrain.psychicterrain(battlePokemon)) {
                              break label2971;
                           }

                           if (currentHeldItem != null && currentHeldItem.equals("terrainextender")) {
                              score += 9;
                              break label2971;
                           }

                           score += 8;
                           break label2971;
                        case 36:
                           if (Terrain.grassyterrain(battlePokemon)) {
                              break label2971;
                           }

                           if (currentHeldItem != null && currentHeldItem.equals("terrainextender")) {
                              score += 9;
                              break label2971;
                           }

                           score += 8;
                           break label2971;
                        case 37:
                           if (Terrain.mistyterrain(battlePokemon)) {
                              break label2971;
                           }

                           if (currentHeldItem != null && currentHeldItem.equals("terrainextender")) {
                              score += 9;
                              break label2971;
                           }

                           score += 8;
                           break label2971;
                        case 38:
                           if (Weather.rain(battlePokemon) || Weather.heavyrain(battlePokemon)) {
                              break label2971;
                           }

                           if (currentHeldItem != null && currentHeldItem.equals("damprock")) {
                              score += 9;
                              break label2971;
                           }

                           score += 8;
                           break label2971;
                        case 39:
                           if (Weather.harshsunlight(battlePokemon) || Weather.extremelyharshsunlight(battlePokemon)) {
                              break label2971;
                           }

                           if (currentHeldItem != null && currentHeldItem.equals("heatrock")) {
                              score += 9;
                              break label2971;
                           }

                           score += 8;
                           break label2971;
                        case 40:
                           if (Weather.hail(battlePokemon)) {
                              break label2971;
                           }

                           if (currentHeldItem != null && currentHeldItem.equals("icyrock")) {
                              score += 9;
                              break label2971;
                           }

                           score += 8;
                           break label2971;
                        case 41:
                           if (Weather.snow(battlePokemon) && (!npcIsOHKO || !isFaster)) {
                              break label2971;
                           }

                           if (currentHeldItem != null && currentHeldItem.equals("icyrock")) {
                              score += 9;
                              break label2971;
                           }

                           score += 6;
                           break label2971;
                        case 42:
                           if (Weather.sandstorm(battlePokemon)) {
                              break label2971;
                           }

                           if (currentHeldItem != null && currentHeldItem.equals("smoothrock")) {
                              score += 9;
                              break label2971;
                           }

                           score += 8;
                           break label2971;
                        case 43:
                           if (Weather.snow(battlePokemon)) {
                              break label2971;
                           }

                           if (currentHeldItem != null && currentHeldItem.equals("terrainextender")) {
                              score += 9;
                              break label2971;
                           }

                           score += 8;
                           break label2971;
                        case 44:
                           if (!Screen.lightscreen(battlePokemon)) {
                              roll = this.RANDOM.nextDouble();
                              score += 6;
                              boolean lightclayLS = currentHeldItem.equals("lightclay");
                              if (lightclayLS) {
                                 if (getIsMoveUp(moveID, this.selfInfo.getMoveHistory(), 8, this.battleTurn)) {
                                    score = -20;
                                 }
                              } else if (getIsMoveUp(moveID, this.selfInfo.getMoveHistory(), 5, this.battleTurn)) {
                                 score = -20;
                              }

                              if (oppHasSpecialMove) {
                                 if (lightclayLS) {
                                    score++;
                                 }

                                 score += roll > 0.5 ? 1 : 0;
                              }
                           } else {
                              score = -20;
                           }
                           break label2971;
                        case 45:
                           if (!Screen.reflect(battlePokemon)) {
                              roll = this.RANDOM.nextDouble();
                              score += 6;
                              boolean lightclayR = currentHeldItem.equals("lightclay");
                              if (lightclayR) {
                                 if (getIsMoveUp(moveID, this.selfInfo.getMoveHistory(), 8, this.battleTurn)) {
                                    score = -20;
                                 }
                              } else if (getIsMoveUp(moveID, this.selfInfo.getMoveHistory(), 5, this.battleTurn)) {
                                 score = -20;
                              }

                              if (oppHasPhysicalMove) {
                                 if (lightclayR) {
                                    score++;
                                 }

                                 score += roll > 0.5 ? 1 : 0;
                              }
                           } else {
                              score = -20;
                           }
                           break label2971;
                        case 46:
                           roll = this.RANDOM.nextDouble();
                           boolean hasSoundMoves = false;
                           score += 6;
                           if (Status.slp(opponent)) {
                              score += 2;
                           }

                           if (Volatile.leech(opponent) && isFaster) {
                              score += 2;
                           }

                           for (Move m : oppMoves) {
                              if (soundMoves.contains(m.getName())) {
                                 hasSoundMoves = true;
                                 break;
                              }
                           }

                           if (hasSoundMoves) {
                              score -= 8;
                           }

                           if (activePokemonPercentHP <= 50.0 || opponentAbility.equals("infiltrator") && !PokeMathMax.isSuppressed(opponent)) {
                              score = -20;
                           }

                           score -= roll > 0.5 ? 1 : 0;
                           break label2971;
                        case 47:
                        case 48:
                        case 49:
                           roll = this.RANDOM.nextDouble();
                           if (aliveParty.isEmpty() && (!allOpponentActiveBattlePokemon.isEmpty() || !aliveParty.isEmpty())) {
                              break label2971;
                           }

                           if (activePokemonPercentHP < 10.0) {
                              score += 10;
                           } else if (activePokemonPercentHP < 33.0) {
                              score += roll > 0.3 ? 8 : 0;
                           } else if (activePokemonPercentHP < 66.0) {
                              score += roll > 0.5 ? 7 : 0;
                           } else {
                              score += roll > 0.95 ? 7 : 0;
                           }

                           if (aliveParty.isEmpty()) {
                              score--;
                           }
                           break label2971;
                        case 50:
                           if (!aliveParty.isEmpty()) {
                              roll = this.RANDOM.nextDouble();
                              if (activePokemonPercentHP < 10.0) {
                                 score += 16;
                              } else if (activePokemonPercentHP < 33.0) {
                                 score += roll > 0.3 ? 14 : 6;
                              } else if (activePokemonPercentHP < 66.0) {
                                 score += roll > 0.5 ? 13 : 6;
                              } else {
                                 score += roll > 0.05 ? 13 : 6;
                              }
                           }
                           break label2971;
                        case 51:
                        case 52:
                        case 53:
                        case 54:
                        case 55:
                           if (!Status.any(opponent)) {
                              roll = this.RANDOM.nextDouble();
                              int paraRoll = roll > 0.5 ? -1 : 0;
                              boolean fasterIfPara = false;
                              boolean hasFlinchMove = evaluations.stream().anyMatch(entry -> flinchMoves.contains(entry.getMove().getName()));
                              if (PokeMathMax.getEffectiveSpeed(opponent, this.battleStatStages.getStatMap(opponent)) / 4.0
                                 < PokeMathMax.getEffectiveSpeed(battlePokemon, this.battleStatStages.getStatMap(battlePokemon))) {
                                 fasterIfPara = true;
                              }

                              if ((isFaster || !fasterIfPara)
                                 && !hasMove(battlePokemon, "hex")
                                 && !hasFlinchMove
                                 && !Volatile.attract(opponent)
                                 && !Volatile.confusion(opponent)) {
                                 score += 7;
                              } else {
                                 score += 8;
                              }

                              score += paraRoll;
                              break label2971;
                           }

                           score -= 20;
                           break label2971;
                        case 56:
                           boolean waterveil = isOpponentSuppressed && opponentAbility.equals("waterveil");
                           boolean waterbubble = isOpponentSuppressed && opponentAbility.equals("waterbubble");
                           boolean guts = isOpponentSuppressed && opponentAbility.equals("guts");
                           boolean thermalexchange = isOpponentSuppressed && opponentAbility.equals("thermalexchange");
                           if (!Status.any(opponent) && !guts && !waterveil && !waterbubble && !thermalexchange) {
                              if (!Status.any(opponent)) {
                                 score += 6;
                                 roll = this.RANDOM.nextDouble();
                                 if (!(roll < 0.37) || (this.partnerInfo == null || npcActiveBP == null) && isDoubles) {
                                    break label2971;
                                 }

                                 if (hasMove(battlePokemon, "hex") || isDoubles && hasMove(npcActiveBP.getBattlePokemon(), "hex")) {
                                    score++;
                                 }

                                 if (oppHasPhysicalMove) {
                                    score++;
                                 }
                                 break label2971;
                              }

                              score -= 20;
                              break label2971;
                           }

                           score -= 20;
                           break label2971;
                        case 57:
                        case 58:
                           if (Objects.equals(getOpponentHeldItem, "")) {
                              score = -20;
                              break label2971;
                           }

                           if (currentHeldItem == null) {
                              break label2971;
                           }

                           if (!currentHeldItem.equals("toxicorb") && !currentHeldItem.equals("flameorb") && !currentHeldItem.equals("blacksludge")) {
                              if (!currentHeldItem.equals("ironball") && !currentHeldItem.equals("laggingtail") && !currentHeldItem.equals("stickybarb")) {
                                 score += 5;
                                 break label2971;
                              }

                              score += 7;
                              break label2971;
                           }

                           roll = this.RANDOM.nextDouble();
                           score += roll > 0.5 ? 6 : 7;
                           break label2971;
                        case 59:
                           if (!PokeMathMax.hasAbility("clearbody", opponent)
                              && (!PokeMathMax.hasAbility("whitesmoke", opponent) || PokeMathMax.hasAbility("moldbreaker", battlePokemon))) {
                              if (opponentStages != null) {
                                 if (-6 == opponentStages.getOrDefault(Stats.ATTACK, 0)) {
                                    score = -20;
                                 } else if (-6 == opponentStages.getOrDefault(Stats.DEFENCE, 0)) {
                                    score = -20;
                                 }
                              } else {
                                 score = 6;
                              }
                              break label2971;
                           }

                           score = -20;
                           break label2971;
                        case 60:
                        case 61:
                        case 62:
                        case 63:
                        case 64:
                        case 65:
                        case 66:
                           score += 6;
                           roll = this.RANDOM.nextDouble();
                           boolean oppPartnerHasFlowerVeil = PokeMathMax.hasAbility(allOpponentActiveBattlePokemon, "flowerveil");
                           boolean oppGrass = opponent.getEffectedPokemon().getPrimaryType() == ElementalTypes.GRASS
                              || opponent.getEffectedPokemon().getSecondaryType() == ElementalTypes.GRASS;
                           if (!Status.any(opponent)) {
                              if (!(roll < 0.25)
                                 || Terrain.mistyterrain(opponent)
                                    && (
                                       !Terrain.mistyterrain(opponent)
                                          || opponent.getEffectedPokemon().getPrimaryType() != ElementalTypes.FLYING
                                             && opponent.getEffectedPokemon().getSecondaryType() != ElementalTypes.FLYING
                                    )
                                 || Terrain.electricterrain(opponent)
                                    && (
                                       !Terrain.electricterrain(opponent)
                                          || opponent.getEffectedPokemon().getPrimaryType() != ElementalTypes.FLYING
                                             && opponent.getEffectedPokemon().getSecondaryType() != ElementalTypes.FLYING
                                    )
                                 || ignoreSleepAbilities.contains(opponentAbility) && !PokeMathMax.isSuppressed(opponent)
                                 || !moveID.equals("hypnosis") && !moveID.equals("spore")
                                 || opponentAbility.equals("magicbounce")
                                 || !moveID.equals("grasswhistle") && !moveID.equals("sing")
                                 || opponentAbility.equals("soundproof")
                                 || opponentAbility.equals("leafguard")
                                    && (!opponentAbility.equals("leafguard") || Weather.harshsunlight(opponent) || Weather.extremelyharshsunlight(opponent))
                                 || isDoubles && (!isDoubles || oppGrass && (oppPartnerHasFlowerVeil || !oppGrass))) {
                                 break label2971;
                              }

                              score++;
                              if (hasMove(battlePokemon, "dreameater")
                                 || hasMove(battlePokemon, "nightmare") && (!hasMove(opponent, "snore") || !hasMove(opponent, "sleeptalk"))) {
                                 score++;
                              }

                              if ((this.partnerInfo != null && npcActiveBP != null || !isDoubles)
                                 && isDoubles
                                 && hasMove(npcActiveBP.getBattlePokemon(), "hex")) {
                                 score++;
                              }
                              break label2971;
                           }

                           score -= 20;
                           break label2971;
                        case 67:
                        case 68:
                        case 69:
                           if (Status.any(opponent)) {
                              score -= 20;
                              break label2971;
                           }

                           score += 6;
                           roll = this.RANDOM.nextDouble();
                           boolean hasDamagingMoves = false;
                           boolean hasCertainMove = false;
                           if (!(roll < 0.38) || !killingMoves.isEmpty() || !(getCurrentPercentHP(opponent) > 20.0)) {
                              break label2971;
                           }

                           for (Move oppMove : oppMoves) {
                              if (oppMove.getPower() > 0.0) {
                                 hasDamagingMoves = true;
                                 break;
                              }
                           }

                           for (RunBunAI.MoveEvaluation ourMoves : nonKillingPossibleMoves) {
                              if (ourMoves.getMove().getName().equals("venomdrench")
                                 || ourMoves.getMove().getName().equals("hex")
                                 || ourMoves.getMove().getName().equals("venoshock")) {
                                 hasCertainMove = true;
                                 break;
                              }
                           }

                           if (hasDamagingMoves && hasCertainMove && currentAbility.equals("merciless") && !isAbilitySuppressed) {
                              score += 2;
                           }
                           break label2971;
                        case 70:
                           boolean hasOnlyPhys = oppHasPhysicalMove && !oppHasSpecialMove;
                           score += 6;
                           if (npcIsOHKO) {
                              score -= 20;
                           }

                           if (hasOnlyPhys && hasFocusSash) {
                              score += 2;
                           }

                           if (!npcIsOHKO && hasOnlyPhys) {
                              score += roll > 0.2 ? 2 : 0;
                           }

                           if (isFaster) {
                              score += roll > 0.75 ? -1 : 0;
                           }

                           if (hasAnyMoveType(opponent, statusMoves) && (!"magicbounce".equals(currentAbility) || isAbilitySuppressed)) {
                              score += roll > 0.75 ? -1 : 0;
                           }
                           break label2971;
                        case 71:
                           boolean hasOnlySpecial = !oppHasPhysicalMove && oppHasSpecialMove;
                           score += 6;
                           if (npcIsOHKO) {
                              score = -20;
                           } else {
                              if (hasOnlySpecial && hasFocusSash) {
                                 score += 2;
                              }

                              if (!npcIsOHKO && hasOnlySpecial) {
                                 score += roll > 0.2 ? 2 : 0;
                              }

                              if (isFaster) {
                                 score += roll > 0.75 ? -1 : 0;
                              }

                              if (hasAnyMoveType(opponent, statusMoves)) {
                                 score += roll > 0.75 ? -1 : 0;
                              }
                           }
                           break label2971;
                        case 72:
                           if (npcIsOHKO) {
                              score = -20;
                              break label2971;
                           }

                           if (isFaster) {
                              score = -20;
                              break label2971;
                           }

                           if (getOpponentHeldItem.equals("shinystone") || PokeMathMax.hasAbility("stall", opponent)) {
                              score = -20;
                              break label2971;
                           }

                           if (currentHeldItem.equals("shinystone") || PokeMathMax.hasAbility("stall", opponent)) {
                              score = roll < 0.75 ? 6 : 8;
                              break label2971;
                           }
                        case 73:
                           break;
                        case 75:
                           List<String> worryseedAbilities = List.of(
                              "truant",
                              "multitype",
                              "stancechange",
                              "schooling",
                              "comatose",
                              "shieldsdown",
                              "disguise",
                              "rkssystem",
                              "battlebond",
                              "powerconstruct",
                              "iceface",
                              "gulpmissile",
                              "asone",
                              "commander",
                              "vitalspirit",
                              "insomnia"
                           );
                           if (worryseedAbilities.contains(opponentAbility)) {
                              score = -20;
                              break label2971;
                           }

                           if (!Status.slp(opponent) || PokeMathMax.hasMove(opponent, "sleeptalk") && PokeMathMax.hasMove(opponent, "snore")) {
                              score = 6;
                              break label2971;
                           }

                           score = -20;
                           break label2971;
                        case 76:
                           List<String> captivateAbilities = List.of("oblivious", "clearbody", "whitesmoke");
                           if (captivateAbilities.contains(opponentAbility) && !PokeMathMax.hasAbility("moldbreaker", battlePokemon)) {
                              score = -20;
                              break label2971;
                           }

                           if (battlePokemon.getEffectedPokemon().getGender().equals(opponent.getEffectedPokemon().getGender())) {
                              score = -20;
                           } else if (opponentStages.getOrDefault(Stats.SPECIAL_ATTACK, 0) <= -6) {
                              score = -20;
                           } else {
                              score = 6;
                           }
                           break label2971;
                        case 77:
                           Map<Integer, MoveTemplate> opponentMoveHistory = RBSlotInformation.getMoveHistory(opponent, false);
                           if (isFaster && !opponentMoveHistory.isEmpty()) {
                              score = 5;
                              break label2971;
                           }

                           if (!isFaster) {
                              score = 6;
                           } else {
                              score = -1;
                           }
                           break label2971;
                        case 78:
                           if (oppPercentHP < 80.0) {
                              score = -5;
                              break label2971;
                           }

                           if (!isFaster) {
                              if (activePokemonPercentHP > 60.0) {
                                 score = -5;
                                 break label2971;
                              }

                              score = 5;
                           }

                           if (activePokemonPercentHP > 40.0) {
                              score = -5;
                           }
                           break label2971;
                        case 79:
                        case 80:
                           if (opponentStages.getOrDefault(Stats.EVASION, 0) >= 3) {
                              score = roll > 0.5 ? 5 : 6;
                           } else {
                              score = roll > 0.25 ? 5 : 6;
                           }
                           break label2971;
                        case 81:
                           if (isFaster) {
                              if (activePokemonPercentHP > 0.5 && maxDamage < oppMaxDamage) {
                                 score += 8;
                                 break label2971;
                              }

                              score -= 20;
                              break label2971;
                           }

                           if (activePokemonCurrentHP - oppMaxDamage > 0.5 && maxDamage < oppMaxDamage) {
                              score += 8;
                              break label2971;
                           }

                           score -= 20;
                           break label2971;
                        case 82:
                           if (!isFirstTurnOut) {
                              score -= 20;
                           } else {
                              score += 10;
                           }
                           break label2971;
                        case 83:
                        case 84:
                           if (Status.slp(battlePokemon)) {
                              score += 14;
                           } else {
                              score -= 20;
                           }
                           break label2971;
                        case 85:
                        case 86:
                           if (!Status.slp(opponent)) {
                              score -= 20;
                           } else if (isFaster) {
                              score += roll > 0.5 ? 8 : 7;
                           } else {
                              score += 6;
                           }
                           break label2971;
                        case 87:
                           if (!Status.slp(opponent)) {
                              break label2971;
                           }

                           if (isFaster && npcIsOHKO) {
                              score -= 20;
                              break label2971;
                           }

                           if (!isFaster) {
                              score += roll > 0.5 ? 8 : 7;
                           }
                           break label2971;
                        case 88:
                           if (Status.psn(opponent) && Status.tox(opponent)) {
                              score += roll > 0.5 ? 8 : 6;
                              break label2971;
                           }

                           score -= 20;
                           break label2971;
                        case 89:
                           if (oppHasPhysicalMove && !oppHasSpecialMove && !npcIsOHKO) {
                              score += 8;
                              break label2971;
                           }

                           if (!oppHasPhysicalMove || npcIsOHKO) {
                              score -= 20;
                           }
                           break label2971;
                     }

                     roll = this.RANDOM.nextDouble();
                     if (opponent.getHealth() / 2 > maxDamage) {
                        score += roll > 0.4 ? 9 : 7;
                     }
                  }

                  boolean opponentRecharging = false;
                  boolean opponentLoafing = false;
                  if (this.selfInfo.getTurnsForActivePokemon() % 2 == 0 && opponentAbility.equals("truant")) {
                     opponentLoafing = !PokeMathMax.isSuppressed(opponent);
                  }

                  if (oppMoves.getFirst().getName().equals("recharge")) {
                     opponentRecharging = true;
                  }

                  if (generalSetupMoves.contains(moveID)) {
                     if (npcIsOHKO) {
                        score -= 20;
                     }

                     if (opponentAbility.equals("unaware") && !PokeMathMax.isSuppressed(opponent)) {
                        score -= 20;
                     }

                     boolean hasThawingMove = false;

                     for (Move m : oppMoves) {
                        if (thawingMoves.contains(m.getName())) {
                           hasThawingMove = true;
                           break;
                        }
                     }

                     if (currentAbility.equals("contrary") && score != 0 && !isAbilitySuppressed) {
                        switch (moveID) {
                           case "overheat":
                           case "leafstorm":
                              score += 6;
                              if ((!isOPFrozen || hasThawingMove) && !isOPSleeping && !opponentLoafing && !opponentRecharging) {
                                 if (!npcIs3OHKO) {
                                    score++;
                                    if (isFaster) {
                                       score++;
                                    }
                                 }
                              } else {
                                 score += 3;
                              }

                              if (!isFaster && npcIs2OHKO) {
                                 score -= 5;
                              }

                              if (this.npcStages.getOrDefault(Stats.SPECIAL_ATTACK, 0) >= 2) {
                                 score--;
                              }
                              break;
                           case "superpower":
                              score += 6;
                              if ((!isOPFrozen || hasThawingMove) && !isOPSleeping && !opponentLoafing && !opponentRecharging) {
                                 if (!npcIs3OHKO) {
                                    score++;
                                    if (isFaster) {
                                       score++;
                                    }
                                 }
                              } else {
                                 score += 3;
                              }

                              if (!isFaster && npcIs2OHKO) {
                                 score -= 5;
                              }

                              if (this.npcStages.getOrDefault(Stats.ATTACK, 0) >= 2) {
                                 score--;
                              }
                        }
                     }

                     switch (moveID) {
                        case "swordsdance":
                        case "howl":
                        case "sharpen":
                        case "meditate":
                        case "honeclaws":
                           score += 6;
                           if (isOPFrozen && !hasThawingMove || isOPSleeping || opponentLoafing || opponentRecharging) {
                              score += 3;
                           }

                           if (!isFaster && npcIs2OHKO) {
                              score -= 5;
                           }
                           break;
                        case "dragondance":
                        case "shiftgear":
                        case "tidyup":
                           score += 6;
                           if (moveID.equals("shiftgear")) {
                              if (fasterAndOHKOAfterBoost(battlePokemon, opponent, 2, 1, 0, activeBattlePokemon, this.battleStatStages)) {
                                 score += 5;
                              }
                           } else if (fasterAndOHKOAfterBoost(battlePokemon, opponent, 1, 1, 0, activeBattlePokemon, this.battleStatStages)) {
                              score += 5;
                           }

                           if (isOPFrozen && !hasThawingMove || isOPSleeping || opponentLoafing || opponentRecharging) {
                              score += 3;
                           }

                           if (!isFaster && npcIs2OHKO) {
                              score -= 5;
                           }

                           if (this.npcStages.getOrDefault(Stats.ATTACK, 0) >= 2 || this.npcStages.getOrDefault(Stats.SPEED, 0) >= 2) {
                              score -= 3;
                           }
                           break;
                        case "acidarmor":
                        case "barrier":
                        case "cottonguard":
                        case "harden":
                        case "irondefense":
                        case "stockpile":
                        case "cosmicpower":
                           roll = this.RANDOM.nextDouble();
                           score += 6;
                           if (!isFaster && npcIs2OHKO) {
                              score -= 5;
                           }

                           if (!(roll > 0.05)) {
                              break;
                           }

                           if (isOPFrozen || isOPSleeping) {
                              score += 2;
                           }

                           if ((moveID.equals("stockpile") || moveID.equals("cosmicpower"))
                              && (this.npcStages.getOrDefault(Stats.SPECIAL_DEFENCE, 0) < 2 || this.npcStages.getOrDefault(Stats.DEFENCE, 0) < 2)) {
                              score += 2;
                           }
                           break;
                        case "coil":
                        case "bulkup":
                        case "calmmind":
                        case "curse":
                           score += 6;
                           if ((oppHasPhysicalMove && !oppHasSpecialMove || !oppHasPhysicalMove && !oppHasSpecialMove) && moveID.equals("calmmind")) {
                              if (isOPFrozen && !hasThawingMove || isOPSleeping || opponentLoafing || opponentRecharging) {
                                 score += 3;
                              }

                              if (!isFaster && npcIs2OHKO) {
                                 score -= 5;
                              }
                           } else if (!oppHasPhysicalMove && oppHasSpecialMove && moveID.equals("calmmind")) {
                              roll = this.RANDOM.nextDouble();
                              if (!isFaster && npcIs2OHKO) {
                                 score -= 5;
                              }

                              if (roll > 0.05 && (isOPFrozen || isOPSleeping)) {
                                 score += 2;
                              }
                           }

                           if ((oppHasSpecialMove && !oppHasPhysicalMove || !oppHasPhysicalMove && !oppHasSpecialMove)
                              && (moveID.equals("coil") || moveID.equals("bulkup") || moveID.equals("curse"))) {
                              if (isOPFrozen && !hasThawingMove || isOPSleeping || opponentLoafing || opponentRecharging) {
                                 score += 3;
                              }

                              if (!isFaster && npcIs2OHKO) {
                                 score -= 5;
                              }
                           } else {
                              if (oppHasSpecialMove
                                 || !oppHasPhysicalMove
                                 || !moveID.equals("coil") && !moveID.equals("bulkup") && !moveID.equals("noretreat") && !moveID.equals("curse")) {
                                 break;
                              }

                              roll = this.RANDOM.nextDouble();
                              if (!isFaster && npcIs2OHKO) {
                                 score -= 5;
                              }

                              if (roll > 0.05 && (isOPFrozen || isOPSleeping)) {
                                 score += 2;
                              }
                           }
                           break;
                        case "quiverdance":
                        case "geomancy":
                           score += 6;
                           if (moveID.equals("geomancy")) {
                              if (fasterAndOHKOAfterBoost(battlePokemon, opponent, 2, 2, 2, activeBattlePokemon, this.battleStatStages)
                                 && "powerherb".equals(currentHeldItem)) {
                                 score += 5;
                              } else {
                                 score -= 20;
                              }
                           } else if (fasterAndOHKOAfterBoost(battlePokemon, opponent, 1, 1, 1, activeBattlePokemon, this.battleStatStages)) {
                              score += 5;
                           }

                           if (isOPFrozen && !hasThawingMove || isOPSleeping || opponentLoafing || opponentRecharging) {
                              score += 3;
                           }

                           if (!isFaster && npcIs2OHKO) {
                              score -= 5;
                           }

                           if (this.npcStages.getOrDefault(Stats.SPECIAL_DEFENCE, 0) >= 2
                              || this.npcStages.getOrDefault(Stats.SPECIAL_ATTACK, 0) >= 2
                              || this.npcStages.getOrDefault(Stats.SPEED, 0) >= 2) {
                              score -= 3;
                           }
                           break;
                        case "noretreat":
                           score += 6;
                           if (fasterAndOHKOAfterBoost(battlePokemon, opponent, 1, 1, 1, activeBattlePokemon, this.battleStatStages)) {
                              score += 5;
                           }

                           if (!isFaster && npcIs2OHKO) {
                              score -= 5;
                           }

                           if (this.npcStages.getOrDefault(Stats.SPECIAL_DEFENCE, 0) >= 2
                              || this.npcStages.getOrDefault(Stats.DEFENCE, 0) >= 2
                              || this.npcStages.getOrDefault(Stats.ATTACK, 0) >= 2
                              || this.npcStages.getOrDefault(Stats.SPECIAL_ATTACK, 0) >= 2
                              || this.npcStages.getOrDefault(Stats.SPEED, 0) >= 2) {
                              score -= 3;
                           }
                           break;
                        case "agility":
                        case "rockpolish":
                        case "autotomize":
                           if (!isFaster) {
                              score += 7;
                           } else {
                              score -= 20;
                           }
                           break;
                        case "tailglow":
                        case "nastyplot":
                        case "workup":
                           score += 6;
                           if ((!isOPFrozen || hasThawingMove) && !isOPSleeping && !opponentLoafing && !opponentRecharging) {
                              if (!npcIs3OHKO) {
                                 score++;
                                 if (isFaster) {
                                    score++;
                                 }
                              }
                           } else {
                              score += 3;
                           }

                           if (!isFaster && npcIs2OHKO) {
                              score -= 5;
                           }

                           if (this.npcStages.getOrDefault(Stats.SPECIAL_ATTACK, 0) >= 2) {
                              score--;
                           }
                           break;
                        case "shellsmash":
                           score += 6;
                           if (isOPFrozen && !hasThawingMove || isOPSleeping || opponentLoafing || opponentRecharging) {
                              score += 3;
                           }

                           if (npcIsOHKOWithSS && (npcIsOHKO || !"whiteherb".equals(currentHeldItem))) {
                              score -= 2;
                           } else {
                              score += 2;
                           }

                           if (this.npcStages.getOrDefault(Stats.ATTACK, 0) >= 1
                              || this.npcStages.getOrDefault(Stats.SPECIAL_ATTACK, 0) >= 1
                              || this.npcStages.getOrDefault(Stats.ATTACK, 0) == 6
                              || this.npcStages.getOrDefault(Stats.SPECIAL_ATTACK, 0) == 6) {
                              score -= 20;
                           }
                           break;
                        case "bellydrum":
                           if (getCurrentPercentHP(currentBP) < 51.0) {
                              score = -10;
                           } else {
                              if ((!isOPFrozen || hasThawingMove) && !isOPSleeping && !opponentLoafing && !opponentRecharging) {
                                 if (!npcIsOHKOWithBD) {
                                    score += 8;
                                 } else {
                                    score += 4;
                                 }
                                 break;
                              }

                              score += 9;
                           }
                           break;
                        case "focusenergy":
                        case "laserfocus":
                           if ("scopelens".equals(currentHeldItem)) {
                              score += 7;
                           } else if (!currentAbility.equals("superluck") && !currentAbility.equals("sniper") || isAbilitySuppressed) {
                              score += 6;
                           }
                           break;
                        case "coaching":
                           roll = this.RANDOM.nextDouble();
                           score += 6;
                           Map<Stats, Integer> partnerStages = this.battleStatStages.getStatMap(this.partnerInfo.getBattlePokemon());
                           if (isDoubles
                              && (
                                 !npcActiveBP.getBattlePokemon().getEffectedPokemon().getAbility().getName().equals("contrary")
                                    || PokeMathMax.isSuppressed(npcActiveBP.getBattlePokemon())
                              )) {
                              if (partnerStages.getOrDefault(Stats.ATTACK, 0) <= 2) {
                                 score += 1 - partnerStages.getOrDefault(Stats.ATTACK, 0);
                              }

                              if (partnerStages.getOrDefault(Stats.DEFENCE, 0) <= 2) {
                                 score += 1 - partnerStages.getOrDefault(Stats.DEFENCE, 0);
                              }

                              score += roll > 0.2 ? 1 : 0;
                              break;
                           }

                           score -= 20;
                           break;
                        case "meteorbeam":
                           if ("powerherb".equals(currentHeldItem)) {
                              score += 9;
                           } else {
                              score -= 20;
                           }
                           break;
                        case "destinybond":
                           roll = this.RANDOM.nextDouble();
                           if (isFaster && npcIsOHKO) {
                              score += roll > 0.19 ? 7 : 6;
                           }

                           if (!isFaster) {
                              score += roll > 0.5 ? 5 : 6;
                           }
                     }
                  }

                  if (recoveryMoves.contains(moveID)) {
                     switch (moveID) {
                        case "junglehealing":
                        case "lifedew":
                           if (shouldRecover(oppMaxDamage, 25, isFaster, battlePokemon, opponent)) {
                              score += 7;
                           } else {
                              score = -20;
                           }
                           break;
                        case "recover":
                        case "slackoff":
                        case "healorder":
                        case "softboiled":
                        case "roost":
                        case "strengthsap":
                           if (shouldRecover(oppMaxDamage, 50, isFaster, battlePokemon, opponent)) {
                              score += 7;
                           } else {
                              score = -20;
                           }
                           break;
                        case "morningsun":
                        case "synthesis":
                        case "moonlight":
                           boolean isSunActive = Weather.harshsunlight(opponent) || Weather.extremelyharshsunlight(opponent);
                           if (isSunActive) {
                              if (shouldRecover(oppMaxDamage, 67, isFaster, battlePokemon, opponent)) {
                                 score += 7;
                              } else {
                                 score = -20;
                              }
                           } else if (shouldRecover(oppMaxDamage, 50, isFaster, battlePokemon, opponent)) {
                              score += 7;
                           } else {
                              score = -20;
                           }
                           break;
                        case "rest":
                           if (!shouldRecover(oppMaxDamage, 100, isFaster, battlePokemon, opponent)) {
                              score = -20;
                           } else {
                              List<Move> NPCmoveSet = battlePokemon.getMoveSet().getMoves();
                              boolean sleepTalkSnore = false;
                              boolean holdingCureSleep = "chestoberry".equals(currentHeldItem) || "lumberry".equals(currentHeldItem);
                              boolean shedSkinEarlyBird = (currentAbility.equals("earlybird") || currentAbility.equals("shedskin")) && !isAbilitySuppressed;
                              boolean hydrationRaining = currentAbility.equals("hydration")
                                 && !isAbilitySuppressed
                                 && (Weather.rain(opponent) || Weather.heavyrain(opponent));
                              if (hasMoveName(battlePokemon, "sleeptalk") || hasMoveName(battlePokemon, "snore")) {
                                 sleepTalkSnore = true;
                              }

                              if (!holdingCureSleep && !sleepTalkSnore && !shedSkinEarlyBird && !hydrationRaining) {
                                 score += 7;
                              } else {
                                 score += 8;
                              }
                           }
                     }
                  }

                  if (priorityDamageMoves.contains(moveID) && !isFaster && npcIsOHKO) {
                     score += 11;
                  }

                  if (abilityStatBooster.contains(battlePokemon.getOriginalPokemon().getAbility().getName()) && !isAbilitySuppressed) {
                     score++;
                  }

                  if (highCriticalMoves.contains(currentMove.getName()) && TypeChart.getEffectiveness(currentMove.getType(), opponent) >= 2.0) {
                     roll = this.RANDOM.nextDouble();
                     score = roll < 0.5 ? score + 1 : score;
                  }

                  if (currentMove.getName().equals("acidspray")) {
                     score += 6;
                  }

                  if (currentMove.getName().equals("taunt")) {
                     boolean hasDefog = hasMoveName(opponent, "defog");
                     boolean hasTrickRoom = hasMoveName(opponent, "trickroom");
                     if (hasTrickRoom && !Room.trickroom(opponent)) {
                        score += 9;
                     } else if (hasDefog && isFaster) {
                        score += 9;
                     } else {
                        score += 5;
                     }
                  }

                  if (currentMove.getName().equals("encore")) {
                     if (isFaster) {
                        score += 7;
                     } else {
                        roll = this.RANDOM.nextDouble();
                        score += roll > 0.5 ? 6 : 5;
                     }
                  }
               }

               if (currentMove.getName().equals("pursuit") && isFaster) {
                  score += 3;
               }
            }

            if (move.isImmune) {
               score = -50;
            }

            move.setScore(score);
            String oppName = "";
            if (move.getOpponent() != null) {
               oppName = move.getOpponent().getBattlePokemon().getName().getString();
            } else {
               oppName = "Fainted Pokemon";
            }

            if (move.score == 0) {
               double percentChange = (double)move.damage / opponent.getMaxHealth();
               if (nonKillingPossibleMoves.contains(move) && move.damage != 0 && percentChange < 0.3) {
                  move.score = -5;
               } else if (nonKillingPossibleMoves.contains(move) && move.damage != 0 && percentChange > 0.3) {
                  move.score = 5;
               } else if (!move.getMove().getDamageCategory().equals(DamageCategories.INSTANCE.getSTATUS()) && move.damage == 0) {
                  move.score = -5;
               } else {
                  move.score = 6;
               }
            }

            ModCommon.LOG
               .info(
                  "{}   ->   Against: {}   ->   Score: {}   ->   Damage: {}  -> Will Tera {}",
                  new Object[]{currentMove.getName(), oppName, move.score, move.damage, move.willTera}
               );
         }

         if (isSwitching(evaluations, aliveParty, battlePokemon, allOpponentActiveBattlePokemon, activeBattlePokemon, this.battleStatStages, isDoubles)
            && !allOpponentActiveBattlePokemon.isEmpty()) {
            BattlePokemon oppBP = allOpponentActiveBattlePokemon.getFirst().getBattlePokemon();
            BattlePokemon validSwitch = bestSwitches.stream()
               .filter(
                  bp -> {
                     boolean fasterNotOHKO = PokeMathMax.getEffectiveSpeed(bp, this.battleStatStages.getStatMap(bp))
                           >= PokeMathMax.getEffectiveSpeed(oppBP, this.battleStatStages.getStatMap(oppBP))
                        && !isOHKO(oppBP.getMoveSet().getMoves(), oppBP, bp, activeBattlePokemon, this.battleStatStages);
                     boolean slowerNot2HKO = PokeMathMax.getEffectiveSpeed(bp, this.battleStatStages.getStatMap(bp))
                           < PokeMathMax.getEffectiveSpeed(oppBP, this.battleStatStages.getStatMap(oppBP))
                        && !is2HKO(oppBP.getMoveSet().getMoves(), oppBP, bp, activeBattlePokemon, this.battleStatStages);
                     return fasterNotOHKO || slowerNot2HKO;
                  }
               )
               .findFirst()
               .orElse(null);
            if (validSwitch != null) {
               nextPokemon = validSwitch;
               nextPokemon.setWillBeSwitchedIn(true);
               ModCommon.LOG.info("SWITCHING INTO NEXT MON");
               return new SwitchActionResponse(nextPokemon.getUuid());
            }
         }

         RunBunAI.MoveEvaluation best = evaluations.stream().max(Comparator.comparingInt(RunBunAI.MoveEvaluation::getScore)).orElse(null);
         List<RunBunAI.MoveEvaluation> bestMoves = evaluations.stream().filter(entry -> entry.score == best.score).toList();
         RunBunAI.MoveEvaluation bestEval;
         if (bestMoves.size() > 1) {
            int randomInt = this.RANDOM.nextInt(bestMoves.size());
            bestEval = bestMoves.get(randomInt);
         } else {
            bestEval = bestMoves.getFirst();
         }

         MoveTarget targetType = bestEval.getMove().getTemplate().getTarget();
         if (bestEval.willTera && !this.selfInfo.hasUsedMega()) {
            gimmick = Gimmick.TERASTALLIZATION.getId();
            this.selfInfo.setHasUsedTera(true);
            this.hasUsedTera = true;
         }

         ModCommon.LOG
            .info(
               "CHOOSEN BEST MOVE  {} -> {} -> {} dmg\n",
               new Object[]{bestEval.getMove().getName(), bestEval.getOpponent().getBattlePokemon().getName().getString(), bestEval.damage}
            );
         List<Targetable> targets = bestEval.inBattleMove.mustBeUsed()
            ? null
            : (List)bestEval.inBattleMove.getTarget().getTargetList().invoke(activeBattlePokemon);
         this.selfInfo.setChosenMove(bestEval);
         return new MoveActionResponse(bestEval.move.getName(), targets == null ? null : bestEval.opponent.getPNX(), gimmick);
      } else {
         if (canSwitchTo.isEmpty()) {
            return PassActionResponse.INSTANCE;
         }

         if (allOpponentActiveBattlePokemon.isEmpty()) {
            if (!bestSwitches.isEmpty()) {
               nextPokemon = bestSwitches.getFirst();
            } else {
               if (canSwitchTo.isEmpty()) {
                  return PassActionResponse.INSTANCE;
               }

               nextPokemon = canSwitchTo.getFirst();
            }

            nextPokemon.setWillBeSwitchedIn(true);
            return new SwitchActionResponse(nextPokemon.getUuid());
         } else {
            if (nextPokemon == null) {
               if (!bestSwitches.isEmpty()) {
                  nextPokemon = bestSwitches.getFirst();
               } else {
                  if (canSwitchTo.isEmpty()) {
                     return PassActionResponse.INSTANCE;
                  }

                  nextPokemon = canSwitchTo.getFirst();
               }
            }

            nextPokemon.setWillBeSwitchedIn(true);
            return new SwitchActionResponse(nextPokemon.getUuid());
         }
      }
   }

   public static int getSpeedStat(ActiveBattlePokemon pkmn, RBStatStages stages) {
      if (pkmn == null) {
         return -1;
      }

      if (pkmn.getBattlePokemon() == null) {
         return -1;
      }

      if (pkmn.isGone()) {
         return -1;
      }

      Map<Stats, Integer> NPCstage = stages != null ? stages.getStatMap(pkmn.getBattlePokemon()) : new HashMap<>();
      return (int)PokeMathMax.getEffectiveSpeed(pkmn.getBattlePokemon(), NPCstage);
   }

   public static boolean hasMoveName(BattlePokemon pokemon, String moveName) {
      if (pokemon == null) {
         ModCommon.LOG.info("Battle Pokemon is null when checking has moves");
         return false;
      }

      for (Move move : pokemon.getMoveSet().getMoves()) {
         if (move.getName().equals(moveName)) {
            return true;
         }
      }

      return false;
   }

   public static boolean shouldRecover(double oppMaxDamage, int recoverAmount, boolean isAIFaster, BattlePokemon AIpokemon, BattlePokemon oppPokemon) {
      if (AIpokemon != null && oppPokemon != null) {
         double maxPercentHPDamage = 0.0;
         if (Status.psn(AIpokemon)) {
            maxPercentHPDamage = (oppMaxDamage + AIpokemon.getMaxHealth() / 16) / AIpokemon.getMaxHealth() * 100.0;
         } else {
            maxPercentHPDamage = oppMaxDamage / AIpokemon.getMaxHealth() * 100.0;
         }

         Random RANDOM = new Random();
         double roll = RANDOM.nextDouble();
         double currentAIPercentHP = getCurrentPercentHP(AIpokemon);
         if (isAIFaster && currentAIPercentHP > 70.0) {
            return false;
         }

         if (Status.tox(AIpokemon)) {
            return false;
         }

         if (oppMaxDamage >= recoverAmount) {
            return false;
         }

         if (isAIFaster) {
            if (maxPercentHPDamage >= currentAIPercentHP && maxPercentHPDamage < currentAIPercentHP + recoverAmount) {
               return true;
            }

            if (maxPercentHPDamage < currentAIPercentHP) {
               if (currentAIPercentHP < 66.0 && currentAIPercentHP > 40.0) {
                  return roll > 0.5;
               }

               return currentAIPercentHP < 40.0;
            }
         } else {
            if (currentAIPercentHP < 50.0) {
               return true;
            }

            if (currentAIPercentHP < 70.0) {
               return roll > 0.25;
            }
         }

         return false;
      } else {
         ModCommon.LOG.info("Battle Pokemon is null when checking recover");
         return false;
      }
   }

   public static boolean isOHKO(List<Move> moves, BattlePokemon attacker, BattlePokemon defender, ActiveBattlePokemon activeBattlePokemon, RBStatStages stages) {
      if (defender != null && attacker != null) {
         int currentHP = defender.getHealth();
         int maxHP = defender.getMaxHealth();
         String abilityId = defender.getEffectedPokemon().getAbility() != null ? defender.getEffectedPokemon().getAbility().getName() : "";
         String itemId = defender.getHeldItemManager() != null ? defender.getHeldItemManager().showdownId(defender) : "";
         boolean atFullHP = currentHP == maxHP;

         for (Move move : moves) {
            int damage = PokeMathMax.damage(attacker, defender, move, activeBattlePokemon, false, false, stages);
            if (damage >= currentHP
               && (
                  RBMoveList.getMultiHit3().contains(move.getName())
                        && RBMoveList.getMultiHit2to5().contains(move.getName())
                        && RBMoveList.getMultiHit2().contains(move.getName())
                     || (!atFullHP || !"sturdy".equals(abilityId) || PokeMathMax.isSuppressed(defender)) && (!atFullHP || !"focussash".equals(itemId))
               )) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static boolean is2HKO(
      List<Move> moves, BattlePokemon attacker, BattlePokemon defender, ActiveBattlePokemon activeBattlePokemon, RBStatStages battleStatStages
   ) {
      if (defender != null && attacker != null) {
         int enemyDamage = 0;
         int currentHP = defender.getHealth();

         for (Move currentMove : moves) {
            enemyDamage = PokeMathMax.damage(attacker, defender, currentMove, activeBattlePokemon, false, false, battleStatStages);
            if (enemyDamage * 2 >= currentHP + PokeMathMax.projectedHPGainNextTurn(defender, attacker)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static boolean is3HKO(
      List<Move> moves, BattlePokemon attacker, BattlePokemon defender, ActiveBattlePokemon activeBattlePokemon, RBStatStages battleStatStages
   ) {
      int enemyDamage = 0;
      if (defender != null && attacker != null) {
         int currentHP = defender.getHealth();

         for (Move currentMove : moves) {
            enemyDamage = PokeMathMax.damage(attacker, defender, currentMove, activeBattlePokemon, false, false, battleStatStages);
            if (enemyDamage * 3 >= currentHP + PokeMathMax.projectedHPGainNextTurn(defender, attacker) * 2.0) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static boolean wouldBeOHKOAfterShellSmash(
      List<Move> moves, BattlePokemon attacker, BattlePokemon defender, ActiveBattlePokemon activeBattlePokemon, RBStatStages stages
   ) {
      if (defender != null && attacker != null) {
         Map<Stats, Integer> simulatedDefenderStages = stages.getStatMap(defender);
         simulatedDefenderStages.put(Stats.DEFENCE, Math.max(-6, Math.min(6, simulatedDefenderStages.getOrDefault(Stats.DEFENCE, 0) - 2)));
         simulatedDefenderStages.put(Stats.SPECIAL_DEFENCE, Math.max(-6, Math.min(6, simulatedDefenderStages.getOrDefault(Stats.SPECIAL_DEFENCE, 0) - 2)));
         boolean result = false;
         int currentHP = defender.getHealth();

         for (Move move : moves) {
            int enemyDamage = PokeMathMax.damage(attacker, defender, move, activeBattlePokemon, false, false, stages);
            if (enemyDamage >= currentHP) {
               result = currentHP != defender.getMaxHealth()
                  || !defender.getEffectedPokemon().getAbility().getDisplayName().equals("sturdy")
                  || PokeMathMax.isSuppressed(defender);
               if (defender.getHeldItemManager().showdownId(defender) != null
                  && currentHP == defender.getMaxHealth()
                  && "focussash".equals(defender.getHeldItemManager().showdownId(defender))) {
                  result = false;
               }
            }
         }

         return result;
      } else {
         return false;
      }
   }

   public static boolean isOHKOAfterBellyDrum(
      List<Move> moves, BattlePokemon attacker, BattlePokemon defender, ActiveBattlePokemon activeBattlePokemon, RBStatStages battleStatStages
   ) {
      if (defender != null && attacker != null) {
         String heldItem = defender.getHeldItemManager().showdownId(defender) != null ? defender.getHeldItemManager().showdownId(defender) : "";
         int enemyDamage = 0;
         int currentHP = defender.getHealth();
         boolean result = false;
         int currentHPAfterBellyDrum = getCurrentHPAfterBellyDrum(defender, heldItem);

         for (Move currentMove : moves) {
            enemyDamage = PokeMathMax.damage(attacker, defender, currentMove, activeBattlePokemon, false, false, battleStatStages);
            if (enemyDamage >= currentHP || enemyDamage >= currentHPAfterBellyDrum) {
               return true;
            }
         }

         return result;
      } else {
         return false;
      }
   }

   private static int getCurrentHPAfterBellyDrum(BattlePokemon defender, String heldItem) {
      if (defender.getHealth() <= defender.getMaxHealth() / 2) {
         return defender.getHealth();
      }

      boolean hasSitrus = false;
      boolean hasPinchBerry = false;
      if (heldItem != null) {
         hasSitrus = "sitrusberry".equals(heldItem);
         hasPinchBerry = "figyberry".equals(heldItem)
            || heldItem.equals("wikiberry")
            || heldItem.equals("magoberry")
            || heldItem.equals("aguavberry")
            || heldItem.equals("iapapaberry");
      }

      int currentHPAfterBellyDrum = defender.getHealth() - defender.getMaxHealth() / 2;
      if (hasSitrus) {
         currentHPAfterBellyDrum += defender.getMaxHealth() / 4;
      }

      if (hasPinchBerry && currentHPAfterBellyDrum <= defender.getMaxHealth() / 4) {
         currentHPAfterBellyDrum += (int)(defender.getMaxHealth() * 0.33);
      }

      return currentHPAfterBellyDrum;
   }

   public static boolean isOHKOTera(
      List<Move> moves,
      BattlePokemon attacker,
      BattlePokemon defender,
      ActiveBattlePokemon activeBattlePokemon,
      boolean isAttacking,
      RBStatStages battleStatStages
   ) {
      if (defender != null && attacker != null) {
         int currentHP = defender.getHealth();
         int maxHP = defender.getMaxHealth();
         String abilityId = defender.getEffectedPokemon().getAbility() != null ? defender.getEffectedPokemon().getAbility().getName() : "";
         String itemId = defender.getHeldItemManager() != null ? defender.getHeldItemManager().showdownId(defender) : "";
         boolean atFullHP = currentHP == maxHP;

         for (Move move : moves) {
            int damage = PokeMathMax.damage(attacker, defender, move, activeBattlePokemon, true, isAttacking, battleStatStages);
            if (damage >= currentHP
               && (
                  RBMoveList.getMultiHit3().contains(move.getName())
                        && RBMoveList.getMultiHit2to5().contains(move.getName())
                        && RBMoveList.getMultiHit2().contains(move.getName())
                     || (!atFullHP || !"sturdy".equals(abilityId) || PokeMathMax.isSuppressed(defender)) && (!atFullHP || !"focussash".equals(itemId))
               )) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static boolean isPartySlowerThanOpponent(List<RBSlotInformation> NPC, List<RBSlotInformation> OPP, RBStatStages stages) {
      if (!NPC.isEmpty() && !OPP.isEmpty()) {
         OptionalInt slowestNPC = NPC.stream()
            .filter(slot -> slot != null && slot.getActiveBattlePokemon() != null)
            .mapToInt(slot -> getSpeedStat(slot.getActiveBattlePokemon(), stages))
            .filter(speed -> speed != -1)
            .min();
         OptionalInt slowestOPP = OPP.stream()
            .filter(slot -> slot != null && slot.getActiveBattlePokemon() != null)
            .mapToInt(slot -> getSpeedStat(slot.getActiveBattlePokemon(), stages))
            .filter(speed -> speed != -1)
            .min();
         return !slowestNPC.isEmpty() && !slowestOPP.isEmpty() ? slowestOPP.getAsInt() > slowestNPC.getAsInt() : false;
      } else {
         return false;
      }
   }

   public static boolean isSwitching(
      List<RunBunAI.MoveEvaluation> evaluations,
      List<BattlePokemon> party,
      BattlePokemon self,
      List<ActiveBattlePokemon> opponents,
      ActiveBattlePokemon activeBattlePokemon,
      RBStatStages battleStatStages,
      boolean isDoubles
   ) {
      if (evaluations.stream().anyMatch(s -> s.score >= 6)) {
         return false;
      }

      long totalMoves = evaluations.size();
      long failCount = evaluations.stream().filter(s -> s.score <= -5).count();
      int badMovesNeeded = isDoubles ? 2 : 1;
      boolean hasLowScore = totalMoves - failCount <= badMovesNeeded;
      Random rng = new Random();
      if (rng.nextDouble() >= 0.75) {
         return false;
      }

      if (Math.ceil(getCurrentPercentHP(self)) <= 50.0) {
         return false;
      }

      for (ActiveBattlePokemon opp : opponents) {
         if (opp != null && !opp.isGone()) {
            BattlePokemon oppBP = opp.getBattlePokemon();
            if (oppBP != null) {
               List<Move> oppMoveSet = oppBP.getMoveSet().getMoves();

               for (BattlePokemon ally : party) {
                  if (ally != null) {
                     boolean fasterNotOHKO = PokeMathMax.getEffectiveSpeed(ally, battleStatStages.getStatMap(ally))
                           >= PokeMathMax.getEffectiveSpeed(oppBP, battleStatStages.getStatMap(oppBP))
                        && !isOHKO(oppMoveSet, oppBP, ally, activeBattlePokemon, battleStatStages);
                     boolean slowerNot2HKO = PokeMathMax.getEffectiveSpeed(ally, battleStatStages.getStatMap(ally))
                           < PokeMathMax.getEffectiveSpeed(oppBP, battleStatStages.getStatMap(oppBP))
                        && !is2HKO(oppMoveSet, oppBP, ally, activeBattlePokemon, battleStatStages);
                     if (fasterNotOHKO || slowerNot2HKO) {
                        return hasLowScore;
                     }
                  }
               }
            }
         }
      }

      return false;
   }

   public static double highestPercentDamageMove(
      BattlePokemon attacker, BattlePokemon defender, ActiveBattlePokemon activeBattlePokemon, RBStatStages battleStatStages
   ) {
      if (attacker != null && defender != null) {
         List<Move> attackerMoves = attacker.getMoveSet().getMoves();
         double highestPercent = 0.0;
         double currentCalc = 0.0;

         for (Move move : attackerMoves) {
            currentCalc = Math.ceil(
               (double)PokeMathMax.damage(attacker, defender, move, activeBattlePokemon, false, false, battleStatStages) / defender.getMaxHealth()
            );
            highestPercent = Math.max(currentCalc, highestPercent);
         }

         return highestPercent;
      } else {
         ModCommon.LOG.info("Battle Pokemon is null when checking highest percent move");
         return 0.0;
      }
   }

   private static double getCurrentPercentHP(BattlePokemon pokemon) {
      return pokemon == null ? 0.0 : (double)pokemon.getHealth() / pokemon.getMaxHealth() * 100.0;
   }

   private static boolean hasMove(BattlePokemon pokemon, String moveID) {
      if (pokemon == null) {
         return false;
      }

      boolean hasMove = false;

      for (Move pkmMove : pokemon.getMoveSet().getMoves()) {
         if (pkmMove.getName().equals(moveID)) {
            hasMove = true;
         }
      }

      return hasMove;
   }

   private static boolean hasAnyMoveType(BattlePokemon pokemon, List<String> list) {
      boolean hasMove = false;
      if (pokemon == null) {
         return false;
      }

      for (Move pkmMove : pokemon.getMoveSet().getMoves()) {
         if (list.contains(pkmMove.getName())) {
            hasMove = true;
            break;
         }
      }

      return hasMove;
   }

   private static boolean fasterAndOHKOAfterBoost(
      BattlePokemon attacker,
      BattlePokemon defender,
      int boostedSpeed,
      int boostedAtk,
      int boostedSpAtk,
      ActiveBattlePokemon activeBattlePokemon,
      RBStatStages battleStatStages
   ) {
      if (defender != null && attacker != null) {
         Map<Stats, Integer> attackerStages = battleStatStages.getStatMap(attacker);
         Map<Stats, Integer> defenderStages = battleStatStages.getStatMap(defender);
         attackerStages.put(Stats.SPEED, Math.max(-6, Math.min(6, attackerStages.getOrDefault(Stats.SPEED, 0) + boostedSpeed)));
         attackerStages.put(Stats.ATTACK, Math.max(-6, Math.min(6, attackerStages.getOrDefault(Stats.ATTACK, 0) + boostedAtk)));
         attackerStages.put(Stats.SPECIAL_ATTACK, Math.max(-6, Math.min(6, attackerStages.getOrDefault(Stats.SPECIAL_ATTACK, 0) + boostedSpAtk)));
         return PokeMathMax.getEffectiveSpeed(attacker, attackerStages) >= PokeMathMax.getEffectiveSpeed(defender, defenderStages)
            && isOHKO(attacker.getMoveSet().getMoves(), attacker, defender, activeBattlePokemon, battleStatStages);
      } else {
         return false;
      }
   }

   public static boolean isSandstormFatal(BattlePokemon battlePokemon) {
      if (battlePokemon == null) {
         return false;
      }

      if (!Weather.sandstorm(battlePokemon)) {
         return false;
      }

      Set<String> immuneTypes = Set.of("rock", "ground", "steel");
      ElementalType primary = battlePokemon.getEffectedPokemon().getPrimaryType();
      ElementalType secondary = battlePokemon.getEffectedPokemon().getSecondaryType();
      boolean isImmune = immuneTypes.contains(battlePokemon.getEffectedPokemon().getPrimaryType().getName().toLowerCase());
      if (secondary != null) {
         isImmune = immuneTypes.contains(secondary.getName().toLowerCase());
      }

      if (isImmune) {
         return false;
      }

      int percentHP = battlePokemon.getMaxHealth() / battlePokemon.getHealth();
      return percentHP <= 8;
   }

   public static int getHazardCount(Map<Integer, String> moveHistory, String move) {
      int count = 0;

      for (Entry<Integer, String> entry : moveHistory.entrySet()) {
         if (entry.getValue().equals(move)) {
            count++;
         }
      }

      return count;
   }

   public static boolean getIsMoveUp(String moveID, Map<Integer, String> moveHistory, int turnDuration, int currentTurn) {
      int lastTurnUsed = -1;
      if (moveHistory.isEmpty()) {
         return false;
      }

      for (Entry<Integer, String> history : moveHistory.entrySet()) {
         if (history.getValue().equals(moveID)) {
            lastTurnUsed = history.getKey();
         }
      }

      return lastTurnUsed == -1 ? false : currentTurn - lastTurnUsed < turnDuration;
   }

   public static void changeTurn(RBSlotInformation info, String move) {
      info.addToMoveHistory(move);
   }

   private static boolean hasVolatile(BattlePokemon pkmn, String id) {
      Collection<BattleContext> ctx = pkmn.getContextManager().get(Type.VOLATILE);
      return ctx != null && ctx.stream().anyMatch(bc -> bc.getId().equals(id));
   }

   public class MoveEvaluation {
      private Move move;
      private ActiveBattlePokemon opponent;
      private int damage;
      private int score;
      private boolean isPhysical;
      private InBattleMove inBattleMove;
      private boolean isImmune;
      private boolean willTera;
      private boolean isFaster;
      private RBStatStages stages;

      public MoveEvaluation(
         Move move,
         InBattleMove inBattleMove,
         ActiveBattlePokemon opponent,
         int damage,
         boolean immune,
         int score,
         boolean willTera,
         boolean isFaster,
         RBStatStages stages
      ) {
         this.move = move;
         this.opponent = opponent;
         this.damage = damage;
         this.score = score;
         this.isPhysical = this.move.getDamageCategory().getName().equals(DamageCategories.INSTANCE.getPHYSICAL().getName());
         this.inBattleMove = inBattleMove;
         this.isImmune = immune;
         this.willTera = willTera;
         this.isFaster = isFaster;
         this.stages = stages;
      }

      public Move getMove() {
         return this.move;
      }

      public ActiveBattlePokemon getOpponent() {
         return this.opponent;
      }

      public int getDamage() {
         return this.damage;
      }

      public int getScore() {
         return this.score;
      }

      public void setMove(Move move) {
         this.move = move;
      }

      public void setOpponent(ActiveBattlePokemon opponent) {
         this.opponent = opponent;
      }

      public void setDamage(int damage) {
         this.damage = damage;
      }

      public void setScore(int score) {
         this.score = score;
      }

      public RBStatStages getStages() {
         return this.stages;
      }
   }
}
