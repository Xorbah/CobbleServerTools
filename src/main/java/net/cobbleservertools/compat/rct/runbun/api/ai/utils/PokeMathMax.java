package net.cobbleservertools.compat.rct.runbun.api.ai.utils;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.battles.interpreter.BattleContext;
import com.cobblemon.mod.common.api.battles.interpreter.BattleContext.Type;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.moves.Move;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.moves.categories.DamageCategories;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.api.types.ElementalTypes;
import com.cobblemon.mod.common.api.types.tera.TeraType;
import com.cobblemon.mod.common.api.types.tera.TeraTypes;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.InBattleMove;
import com.cobblemon.mod.common.battles.MoveTarget;
import com.cobblemon.mod.common.battles.Targetable;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleStates;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleEffects.Custom;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleEffects.Field.Gravity;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleEffects.Field.Room;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleEffects.Field.Terrain;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleEffects.Field.Weather;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleEffects.Pokemon.State;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleEffects.Pokemon.Status;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleEffects.Pokemon.Volatile;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleEffects.Side.Screen;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleEffects.Side.Tailwind;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import kotlin.Unit;
import net.cobbleservertools.compat.rct.runbun.ModCommon;

public class PokeMathMax {
   private static final List<String> punchingMoves = RBMoveList.getPunchingMoves();
   private static final Map<UUID, Map<UUID, Integer>> FAINT_COUNTS = new HashMap<>();
   private static final Map<UUID, List<String>> pokemonMoveHistory = new HashMap<>();

   private static double damage(
      Move move,
      boolean physical,
      boolean multiTarget,
      boolean parentalBond,
      boolean glaiveRush,
      boolean burn,
      boolean zmove,
      boolean reflect,
      boolean lightscreen,
      BattlePokemon attacker,
      BattlePokemon defender,
      RBStatStages statStages,
      ActiveBattlePokemon activeBattlePokemon,
      boolean predictTera,
      boolean isAttacker
   ) {
      if (attacker != null && defender != null) {
         int attackerLevel = attacker.getEffectedPokemon().getLevel();
         ElementalType moveType = move.getName().equals("hiddenpower") ? RBTypeChart.getHiddenPowerType(attacker) : move.getType();
         double movePower = move.getPower();
         boolean attackerHasStatus = false;
         if (attacker.getContextManager().get(Type.STATUS) != null) {
            attackerHasStatus = attacker.getContextManager().get(Type.STATUS).isEmpty();
         }

         double attackerEffectiveAttack = calcAttackWithStatChanges(physical, attacker, statStages.getStatMap(attacker));
         Pokemon attackerEp = BattleStates.getTransformationOrEffected(attacker);
         ElementalType attackerPrimaryType = attackerEp.getPrimaryType();
         ElementalType attackerSecondaryType = attackerEp.getSecondaryType() != null ? attackerEp.getSecondaryType() : attackerPrimaryType;
         TeraType attackerTeraType = attacker.getEffectedPokemon().getTeraType();
         String attackerAbility = attackerEp.getAbility().getName();
         String attackerHeldItem = attacker.getHeldItemManager().showdownId(attacker) != null ? attacker.getHeldItemManager().showdownId(attacker) : "";
         double attackerCurrentHPPercent = (double)attacker.getHealth() / attacker.getMaxHealth() * 100.0;
         String attackerName = attacker.getName().toString().toLowerCase();
         boolean isAttackerPoisoned = Status.tox(attacker) || Status.psn(attacker);
         boolean isAttackerParalysed = Status.par(attacker);
         boolean isAttackerBurned = Status.brn(attacker);
         double defenderEffectiveDefence = calcDefenseWithStatChanges(physical, defender, statStages.getStatMap(defender));
         String defenderHeldItem = defender.getHeldItemManager().showdownId(defender) != null ? defender.getHeldItemManager().showdownId(defender) : "";
         String defenderAbility = defender.getEffectedPokemon().getAbility().getName();
         boolean isDefenderFullHP = defender.getHealth() == defender.getMaxHealth();
         String defenderName = defender.getName().toString().toLowerCase();
         PokemonBattle battle = attacker.getActor().battle;
         Collection<BattleContext> weatherContexts = battle.getContextManager().get(Type.WEATHER);
         boolean sun = weatherContexts != null
            && weatherContexts.stream().anyMatch(c -> c.getId().equals("harshsunlight") || c.getId().equals("extremelyharshsunlight"));
         boolean rain = weatherContexts != null && weatherContexts.stream().anyMatch(c -> c.getId().equals("rain") || c.getId().equals("heavyrain"));
         boolean snow = weatherContexts != null && weatherContexts.stream().anyMatch(c -> c.getId().equals("snow"));
         boolean hail = weatherContexts != null && weatherContexts.stream().anyMatch(c -> c.getId().equals("hail"));
         if (attacker != null && defender != null) {
            if (hasAbility("megasol", attacker)) {
               sun = true;
               rain = false;
               snow = false;
               hail = false;
            }

            double targets = 1.0;
            double PB = 1.0;
            double weather = 1.0;
            double GR = 1.0;
            double critical = 1.0;
            double stab = 1.0;
            double otherDamage = 1.0;
            double typeDamage = 1.0;
            double ability = 1.0;
            double heldItem = 1.0;
            double isBurn = 1.0;
            boolean isNeutralizingGas = false;

            for (ActiveBattlePokemon abp : RBBattleSlots.getAllActivePokemon(activeBattlePokemon)) {
               if (abp.getBattlePokemon() != null && abp.getBattlePokemon().getEffectedPokemon().getAbility().getName().equals("neutralizinggas")) {
                  isNeutralizingGas = true;
                  break;
               }
            }

            boolean isAttackerAbilitySuppressed = isSuppressed(attacker, isNeutralizingGas);
            boolean isDefenderAbilitySuppressed = isSuppressed(defender, isNeutralizingGas);
            if (RBMoveList.getFixedDamagingMoves().contains(move.getName())) {
               switch (move.getName()) {
                  case "superfang":
                     return defender.getHealth() / 2;
                  case "dragonrage":
                     return 40.0;
                  case "nightshade":
                  case "seismictoss":
                     return attackerLevel;
                  case "flail":
                  case "reversal":
                     movePower = getFlailPower(attacker);
                     break;
                  case "return":
                     movePower = attacker.getEntity().getFriendship() / 2.5;
                     break;
                  case "frustration":
                     movePower = (255 - attacker.getEntity().getFriendship()) / 2.5;
                     break;
                  case "sonicboom":
                     return 20.0;
                  case "endeavor":
                     double endeavorHP = defender.getHealth() - attacker.getHealth();
                     return endeavorHP <= 0.0 ? 0.0 : endeavorHP;
                  case "lowkick":
                  case "grassknot":
                  case "punishment":
                  default:
                     break;
                  case "gyroball":
                     movePower = Math.min(150.0, 25 * defender.getEffectedPokemon().getSpeed() / attacker.getEffectedPokemon().getSpeed() + 1);
                     break;
                  case "trumpcard":
                     movePower = getTrumpCardPower(move.getCurrentPp());
                     break;
                  case "crushgrip":
                  case "wringout":
                     movePower = 120.0 * (defender.getHealth() / defender.getMaxHealth());
               }
            }

            if (multiTarget) {
               targets *= 0.75;
            }

            if (parentalBond && !isAttackerAbilitySuppressed) {
               PB *= 0.25;
            }

            if (glaiveRush) {
               GR *= 2.0;
            }

            if (hasAbility("steelworker", attacker) && moveType.equals(ElementalTypes.STEEL)) {
               attackerEffectiveAttack *= 1.5;
            } else if (hasAbility("transistor", attacker) && moveType.equals(ElementalTypes.ELECTRIC)) {
               attackerEffectiveAttack *= 1.5;
            } else if (hasAbility("dragonsmaw", attacker) && moveType.equals(ElementalTypes.DRAGON)) {
               attackerEffectiveAttack *= 1.5;
            }

            if (move.getName().equals("revelationdance")) {
               if (attackerPrimaryType != null) {
                  typeDamage = RBTypeChart.getEffectiveness(attackerPrimaryType, defender);
               }
            } else if (move.getName().equals("terablast")) {
               physical = calcAttackWithStatChanges(true, attacker, statStages.getStatMap(attacker))
                  > calcAttackWithStatChanges(false, attacker, statStages.getStatMap(attacker));
               typeDamage = RBTypeChart.getTeraBlastEffectiveness(attacker, defender);
               if (attackerTeraType.equals(TeraTypes.getSTELLAR())) {
                  movePower = 100.0;
               }
            } else if (isAttacker) {
               typeDamage = RBTypeChart.getEffectiveness(moveType, defender, false);
            } else {
               typeDamage = RBTypeChart.getEffectiveness(moveType, defender, predictTera);
            }

            if (Weather.strongwinds(attacker)
               && defender.getEffectedPokemon().getPrimaryType().equals(ElementalTypes.FLYING)
               && defender.getEffectedPokemon().getSecondaryType() == null
               && (moveType == ElementalTypes.ELECTRIC || moveType == ElementalTypes.ICE || moveType == ElementalTypes.ROCK)) {
               typeDamage = 1.0;
            }

            if (burn && physical && !attackerAbility.equals("guts")) {
               isBurn *= 0.5;
            }

            if (attackerHasStatus && attackerAbility.equals("guts") && !isAttackerAbilitySuppressed) {
               ability *= 1.5;
            }

            if (Terrain.grassyterrain(attacker)) {
               if (move.getName().equals("terrainpulse")) {
                  movePower = 100.0;
                  moveType = ElementalTypes.GRASS;
               }

               if (moveType.equals(ElementalTypes.GRASS)) {
                  otherDamage *= 1.3;
               }
            } else if (Terrain.psychicterrain(attacker)) {
               if (move.getName().equals("terrainpulse")) {
                  movePower = 100.0;
                  moveType = ElementalTypes.PSYCHIC;
               }

               if (moveType.equals(ElementalTypes.PSYCHIC)) {
                  otherDamage *= 1.3;
               }
            } else if (Terrain.electricterrain(attacker)) {
               if (move.getName().equals("terrainpulse")) {
                  movePower = 100.0;
                  moveType = ElementalTypes.ELECTRIC;
               }

               if (moveType.equals(ElementalTypes.ELECTRIC)) {
                  otherDamage *= 1.3;
               }
            } else if (Terrain.mistyterrain(attacker) && move.getName().equals("terrainpulse")) {
               movePower = 100.0;
               moveType = ElementalTypes.FAIRY;
            }

            if (doesOurMoveCrit(move, attacker, defender)) {
               critical = 1.5;
               if (attackerAbility.equals("sniper") && !isAttackerAbilitySuppressed) {
                  critical = 2.25;
               }
            } else {
               critical = 1.0;
            }

            if (!isDefenderAbilitySuppressed) {
               switch (defenderAbility) {
                  case "filter":
                  case "solidrock":
                  case "prismarmor":
                     if (stab > 1.0) {
                        ability *= 0.75;
                     }
                     break;
                  case "heatproof":
                  case "waterbubble":
                     if (moveType.equals(ElementalTypes.FIRE)) {
                        ability *= 0.5;
                     }
                     break;
                  case "thickfat":
                     if (moveType.equals(ElementalTypes.FIRE) || moveType.equals(ElementalTypes.WATER)) {
                        ability *= 0.5;
                     }
                     break;
                  case "fluffy":
                     if (moveType.equals(ElementalTypes.FIRE)) {
                        ability *= 2.0;
                     }

                     if (RBMoveList.getContactMoves().contains(move.getName())) {
                        ability *= 0.5;
                     }
                     break;
                  case "multiscale":
                     if (isDefenderFullHP) {
                        ability *= 0.5;
                     }
                     break;
                  case "shadowshield":
                     if (isDefenderFullHP && !RBMoveList.getDirectDamageMoves().contains(move.getName())) {
                        ability *= 0.5;
                     }
                     break;
                  case "dryskin":
                     if (moveType.equals(ElementalTypes.FIRE)) {
                        ability *= 1.25;
                     }
                     break;
                  case "icescales":
                     if (!physical && !RBMoveList.getDirectDamageMoves().contains(move.getName())) {
                        ability *= 0.5;
                     }
                     break;
                  case "purifyingsalt":
                     if (moveType.equals(ElementalTypes.GHOST)) {
                        attackerEffectiveAttack *= 0.5;
                     }
                     break;
                  case "flashfire":
                     if (moveType.equals(ElementalTypes.FIRE)) {
                        ability *= 0.0;
                     }
                     break;
                  case "windpower":
                     if (RBMoveList.getWindMoves().contains(move.getName())) {
                        ability *= 0.0;
                     }
                     break;
                  case "vesselofruin":
                     if (!physical) {
                        attackerEffectiveAttack *= 0.75;
                     }
                  case "unaware":
                  default:
                     break;
                  case "terrashell":
                     if (isDefenderFullHP && typeDamage >= 1.0) {
                        typeDamage = 0.5;
                     }
                     break;
                  case "marvelscale":
                     if (Status.any(defender) && physical) {
                        defenderEffectiveDefence *= 1.5;
                     }
                     break;
                  case "terashell":
                     if (isDefenderFullHP && typeDamage > 0.5) {
                        typeDamage = 0.5;
                     }
               }
            }

            if (!isAttackerAbilitySuppressed) {
               switch (attackerAbility) {
                  case "tintedlens":
                     if (typeDamage < 1.0) {
                        ability *= 2.0;
                     }
                     break;
                  case "aerilate":
                     if (moveType.equals(ElementalTypes.NORMAL)) {
                        moveType = ElementalTypes.FLYING;
                        ability *= 1.3;
                     }
                     break;
                  case "galvanize":
                     if (moveType.equals(ElementalTypes.NORMAL)) {
                        moveType = ElementalTypes.ELECTRIC;
                        ability *= 1.3;
                     }
                     break;
                  case "pixilate":
                     if (moveType.equals(ElementalTypes.NORMAL)) {
                        moveType = ElementalTypes.FAIRY;
                        ability *= 1.3;
                     }
                     break;
                  case "refrigerate":
                     if (moveType.equals(ElementalTypes.NORMAL)) {
                        moveType = ElementalTypes.ICE;
                        ability *= 1.3;
                     }
                     break;
                  case "defeatist":
                     if ((double)attacker.getHealth() / attacker.getMaxHealth() <= 0.5) {
                        movePower *= 0.5;
                     }
                     break;
                  case "flashfire":
                     if (hasBeenHitByType(attacker, ElementalTypes.FIRE)) {
                        ability *= 1.5;
                     }
                     break;
                  case "windpower":
                     if (lastUsedMoveOfType(attacker, ElementalTypes.FLYING) < mostRecentHitOfType(attacker, ElementalTypes.FLYING)
                        && moveType.equals(ElementalTypes.ELECTRIC)) {
                        movePower *= 2.0;
                     }
                     break;
                  case "toxicboost":
                     if ((Status.psn(attacker) || Status.tox(attacker)) && physical) {
                        ability *= 1.5;
                     }
                     break;
                  case "toughclaws":
                     if (RBMoveList.getContactMoves().contains(move.getName())) {
                        ability *= 1.3;
                     }
                     break;
                  case "torrent":
                     if (moveType.equals(ElementalTypes.WATER) && attackerCurrentHPPercent <= 33.0) {
                        ability *= 1.5;
                     }
                     break;
                  case "technician":
                     if (movePower <= 60.0) {
                        movePower *= 1.5;
                     }
                     break;
                  case "swarm":
                     if (moveType.equals(ElementalTypes.BUG) && attackerCurrentHPPercent <= 33.0) {
                        ability *= 1.5;
                     }
                     break;
                  case "supremeoverlord":
                     double deadCount = numberOfFaintedPokemon(attacker);
                     if (deadCount > 0.0) {
                        attackerEffectiveAttack *= 1.0 + 0.1 * deadCount;
                     }
                     break;
                  case "strongjaw":
                     if (RBMoveList.getBitingMoves().contains(move.getName())) {
                        ability *= 1.5;
                     }
                     break;
                  case "steelyspirit":
                     if (moveType.equals(ElementalTypes.STEEL)) {
                        ability *= 1.5;
                     }
                     break;
                  case "solarpower":
                     if (sun && !physical) {
                        attackerEffectiveAttack *= 1.5;
                     }
                     break;
                  case "gorillatactics":
                  case "hustle":
                     if (physical) {
                        attackerEffectiveAttack *= 1.5;
                     }
                     break;
                  case "grasspelt":
                     if (Terrain.grassyterrain(defender)) {
                        defenderEffectiveDefence *= 1.5;
                     }
                     break;
                  case "hadronengine":
                     if (Terrain.electricterrain(attacker) && !physical) {
                        attackerEffectiveAttack *= 1.333252F;
                     }
                     break;
                  case "hugepower":
                     if (physical) {
                        attackerEffectiveAttack *= 2.0;
                     }
                     break;
                  case "liquidvoice":
                     if (RBMoveList.getSoundMoves().contains(move.getName())) {
                        moveType = ElementalTypes.WATER;
                     }
                     break;
                  case "neuroforce":
                     if (typeDamage > 1.0) {
                        ability *= 1.25;
                     }
                     break;
                  case "normalize":
                     moveType = ElementalTypes.NORMAL;
                     break;
                  case "orichalcumpulse":
                     if (Weather.harshsunlight(attacker) && physical) {
                        attackerEffectiveAttack *= 1.333252F;
                     }
                     break;
                  case "overgrow":
                     if ((double)attacker.getHealth() / attacker.getMaxHealth() <= 0.3333333333333333 && moveType.equals(ElementalTypes.GRASS)) {
                        movePower *= 1.5;
                     }
                     break;
                  case "blaze":
                     if ((double)attacker.getHealth() / attacker.getMaxHealth() <= 0.3333333333333333 && moveType.equals(ElementalTypes.FIRE)) {
                        movePower *= 1.5;
                     }
                     break;
                  case "punkrock":
                     if (RBMoveList.getSoundMoves().contains(move.getName())) {
                        movePower *= 1.3000488F;
                     }
                     break;
                  case "reckless":
                     if (RBMoveList.getRecoilMoves().contains(move.getName())) {
                        movePower *= 1.2;
                     }
                     break;
                  case "rivalry":
                     if (attacker.getEffectedPokemon().getGender().toString().equalsIgnoreCase("female")
                        || attacker.getEffectedPokemon().getGender().toString().equalsIgnoreCase("male")) {
                        if (attacker.getEffectedPokemon().getGender().equals(defender.getEffectedPokemon().getGender())) {
                           movePower *= 0.75;
                        } else {
                           movePower *= 1.25;
                        }
                     }
                     break;
                  case "rockypayload":
                     if (moveType.equals(ElementalTypes.ROCK)) {
                        attackerEffectiveAttack *= 1.5;
                     }
                     break;
                  case "sandforce":
                     if (Weather.sandstorm(attacker)
                        && (moveType.equals(ElementalTypes.ROCK) || moveType.equals(ElementalTypes.GROUND) || moveType.equals(ElementalTypes.STEEL))) {
                        movePower *= 1.3;
                     }
                     break;
                  case "sharpness":
                     if (RBMoveList.getSlicingMoves().contains(move.getName())) {
                        movePower *= 1.5;
                     }
                  case "skilllink":
                  default:
                     break;
                  case "ironfist":
                     if (punchingMoves.contains(move.getName())) {
                        ability *= 1.2;
                     }
                     break;
                  case "slowstart":
                     int age = BattleStates.get(attacker.actor.battle).getPokemonState(attacker).age(Custom.TURN);
                     if (age <= 5) {
                        ability /= 2.0;
                     }
                     break;
                  case "flareboost":
                     if (burn && !physical) {
                        ability *= 1.5;
                     }
               }
            }

            boolean isAttacking = false;
            boolean isDefending = false;

            for (ActiveBattlePokemon abp : RBBattleSlots.getAllies(activeBattlePokemon)) {
               if (abp.getBattlePokemon() != null) {
                  if (attacker.equals(abp.getBattlePokemon())) {
                     isAttacking = true;
                  } else if (defender.equals(abp.getBattlePokemon())) {
                     isDefending = true;
                  }
               }
            }

            for (ActiveBattlePokemon abp : RBBattleSlots.getAllies(activeBattlePokemon)) {
               if (abp.getBattlePokemon() != null && !isSuppressed(abp.getBattlePokemon(), isNeutralizingGas)) {
                  String abpAbility = abp.getBattlePokemon().getEffectedPokemon().getAbility().getName();
                  if (isAttacking && abpAbility != null) {
                     switch (abpAbility) {
                        case "battery":
                           if (!abp.getBattlePokemon().getUuid().equals(attacker.getUuid())) {
                              ability *= 1.3;
                           }
                           break;
                        case "steelyspirit":
                           if (!abp.getBattlePokemon().getUuid().equals(attacker.getUuid()) && moveType.equals(ElementalTypes.STEEL)) {
                              ability *= 1.5;
                           }
                           break;
                        case "minus":
                           if (attackerAbility.equals("plus") && !physical) {
                              attackerEffectiveAttack *= 1.5;
                           }
                           break;
                        case "plus":
                           if (attackerAbility.equals("minus") && !physical) {
                              attackerEffectiveAttack *= 1.5;
                           }
                           break;
                        case "powerspot":
                           if (!attacker.getUuid().equals(abp.getBattlePokemon().getUuid())) {
                              movePower *= 1.3;
                           }
                     }
                  }

                  if (isDefending && abpAbility.equals("friendguard") && !defender.equals(abp.getBattlePokemon())) {
                     ability *= 0.75;
                  }
               }
            }

            for (ActiveBattlePokemon abp : RBBattleSlots.getAllActivePokemon(activeBattlePokemon)) {
               if (abp.getBattlePokemon() != null && !isSuppressed(abp.getBattlePokemon(), isNeutralizingGas)) {
                  String abpAbility = abp.getBattlePokemon().getEffectedPokemon().getAbility().getName();
                  boolean isWonderRoom = false;
                  Collection<BattleContext> roomContexts = attacker.getContextManager().get(Type.ROOM);
                  if (roomContexts != null) {
                     for (BattleContext room : roomContexts) {
                        if (room.getId().equalsIgnoreCase("wonderroom")) {
                           isWonderRoom = true;
                           break;
                        }
                     }
                  }

                  if (abpAbility.equalsIgnoreCase("beadsofruin") && !abp.getBattlePokemon().getUuid().equals(defender.getUuid())) {
                     boolean affectsThisHit = !isWonderRoom && !physical || isWonderRoom && physical;
                     if (affectsThisHit) {
                        defenderEffectiveDefence *= 0.75;
                     }
                  }

                  if (abpAbility.equals("swordofruin") && !abp.getBattlePokemon().getUuid().equals(defender.getUuid())) {
                     boolean affectsThisHit = !isWonderRoom && physical || isWonderRoom && !physical;
                     if (affectsThisHit) {
                        defenderEffectiveDefence *= 0.75;
                     }
                  }

                  if (abpAbility.equals("vesselofruin") && !abp.getBattlePokemon().getUuid().equals(attacker.getUuid()) && !physical) {
                     attackerEffectiveAttack *= 0.75;
                  }

                  if (abpAbility.equals("tabletofruin")
                     && !abp.getBattlePokemon().getUuid().equals(attacker.getUuid())
                     && physical
                     && !defenderAbility.equals("tabletofruin")) {
                     attackerEffectiveAttack *= 0.75;
                  }
               }
            }

            if (moveType == ElementalTypes.DARK) {
               ability *= auraAbilityCalculation(battle, "dark");
            } else if (moveType == ElementalTypes.FAIRY) {
               ability *= auraAbilityCalculation(battle, "fairy");
            }

            switch (attackerHeldItem) {
               case "expertbelt":
                  if (RBTypeChart.getEffectiveness(moveType, defender) >= 2.0) {
                     heldItem *= 1.2;
                  }
                  break;
               case "choiceband":
                  if (physical) {
                     heldItem *= 1.5;
                  }
                  break;
               case "choicespecs":
                  if (!physical) {
                     heldItem *= 1.5;
                  }
                  break;
               case "muscleband":
                  if (physical) {
                     heldItem *= 1.1;
                  }
                  break;
               case "wiseglasses":
                  if (!physical) {
                     heldItem *= 1.1;
                  }
                  break;
               case "blackbelt":
               case "fistplate":
                  if (moveType.equals(ElementalTypes.FIGHTING)) {
                     heldItem *= 1.2;
                  }
                  break;
               case "blackglasses":
               case "dreadplate":
                  if (moveType.equals(ElementalTypes.DARK)) {
                     heldItem *= 1.2;
                  }
                  break;
               case "charcoalstick":
               case "flameplate":
                  if (moveType.equals(ElementalTypes.FIRE)) {
                     heldItem *= 1.2;
                  }
                  break;
               case "dragonfang":
               case "dracoplate":
                  if (moveType.equals(ElementalTypes.DRAGON)) {
                     heldItem *= 1.2;
                  }
                  break;
               case "hardstone":
               case "rockincense":
               case "stoneplate":
                  if (moveType.equals(ElementalTypes.ROCK)) {
                     heldItem *= 1.2;
                  }
                  break;
               case "magnet":
               case "zapplate":
                  if (moveType.equals(ElementalTypes.ELECTRIC)) {
                     heldItem *= 1.2;
                  }
                  break;
               case "metalcoat":
               case "ironplate":
                  if (moveType.equals(ElementalTypes.STEEL)) {
                     heldItem *= 1.2;
                  }
                  break;
               case "miracleseed":
               case "roseincense":
               case "meadowplate":
                  if (moveType.equals(ElementalTypes.GRASS)) {
                     heldItem *= 1.2;
                  }
                  break;
               case "mysticwater":
               case "seaincense":
               case "waveincense":
               case "splashplate":
                  if (moveType.equals(ElementalTypes.WATER)) {
                     heldItem *= 1.2;
                  }
                  break;
               case "nevermeltice":
               case "icicleplate":
                  if (moveType.equals(ElementalTypes.ICE)) {
                     heldItem *= 1.2;
                  }
                  break;
               case "poisonbarb":
               case "toxicplate":
                  if (moveType.equals(ElementalTypes.POISON)) {
                     heldItem *= 1.2;
                  }
                  break;
               case "sharpbeak":
               case "skyplate":
                  if (moveType.equals(ElementalTypes.FLYING)) {
                     heldItem *= 1.2;
                  }
                  break;
               case "silkscarf":
                  if (moveType.equals(ElementalTypes.NORMAL)) {
                     heldItem *= 1.2;
                  }
                  break;
               case "silverpowder":
               case "insectplate":
                  if (moveType.equals(ElementalTypes.BUG)) {
                     heldItem *= 1.2;
                  }
                  break;
               case "softsand":
               case "earthplate":
                  if (moveType.equals(ElementalTypes.GROUND)) {
                     heldItem *= 1.2;
                  }
                  break;
               case "spelltag":
               case "spookyplate":
                  if (moveType.equals(ElementalTypes.GHOST)) {
                     heldItem *= 1.2;
                  }
                  break;
               case "twistedspoon":
               case "oddincense":
               case "mindplate":
                  if (moveType.equals(ElementalTypes.PSYCHIC)) {
                     heldItem *= 1.2;
                  }
                  break;
               case "lifeorb":
                  heldItem *= 1.3;
                  break;
               case "souldew":
                  if ((attackerName.equals("latias") || attackerName.equals("latios"))
                     && (moveType.equals(ElementalTypes.PSYCHIC) || moveType.equals(ElementalTypes.DRAGON))) {
                     heldItem *= 1.2;
                  }
                  break;
               case "adamantorb":
                  if (attackerName.equals("dialga") && (moveType.equals(ElementalTypes.STEEL) || moveType.equals(ElementalTypes.DRAGON))) {
                     heldItem *= 1.2;
                  }
                  break;
               case "lustrousorb":
               case "lustrousglobe":
                  if (attackerName.equals("palkia") && (moveType.equals(ElementalTypes.WATER) || moveType.equals(ElementalTypes.DRAGON))) {
                     heldItem *= 1.2;
                  }
                  break;
               case "griseousorb":
               case "griseouscore":
                  if (attackerName.equals("giratina") && (moveType.equals(ElementalTypes.GHOST) || moveType.equals(ElementalTypes.DRAGON))) {
                     heldItem *= 1.2;
                  }
                  break;
               case "fightinggem":
                  if (moveType.equals(ElementalTypes.FIGHTING)) {
                     heldItem *= 1.3;
                  }
                  break;
               case "normalgem":
                  if (moveType.equals(ElementalTypes.NORMAL)) {
                     heldItem *= 1.3;
                  }
                  break;
               case "firegem":
                  if (moveType.equals(ElementalTypes.FIRE)) {
                     heldItem *= 1.3;
                  }
                  break;
               case "watergem":
                  if (moveType.equals(ElementalTypes.WATER)) {
                     heldItem *= 1.3;
                  }
                  break;
               case "grassgem":
                  if (moveType.equals(ElementalTypes.GRASS)) {
                     heldItem *= 1.3;
                  }
                  break;
               case "icegem":
                  if (moveType.equals(ElementalTypes.ICE)) {
                     heldItem *= 1.3;
                  }
                  break;
               case "poisongem":
                  if (moveType.equals(ElementalTypes.POISON)) {
                     heldItem *= 1.3;
                  }
                  break;
               case "groundgem":
                  if (moveType.equals(ElementalTypes.GROUND)) {
                     heldItem *= 1.3;
                  }
                  break;
               case "flyinggem":
                  if (moveType.equals(ElementalTypes.FLYING)) {
                     heldItem *= 1.3;
                  }
                  break;
               case "psychicgem":
                  if (moveType.equals(ElementalTypes.PSYCHIC)) {
                     heldItem *= 1.3;
                  }
                  break;
               case "buggem":
                  if (moveType.equals(ElementalTypes.BUG)) {
                     heldItem *= 1.3;
                  }
                  break;
               case "rockgem":
                  if (moveType.equals(ElementalTypes.ROCK)) {
                     heldItem *= 1.3;
                  }
                  break;
               case "ghostgem":
                  if (moveType.equals(ElementalTypes.GHOST)) {
                     heldItem *= 1.3;
                  }
                  break;
               case "dragongem":
                  if (moveType.equals(ElementalTypes.DRAGON)) {
                     heldItem *= 1.3;
                  }
                  break;
               case "darkgem":
                  if (moveType.equals(ElementalTypes.DARK)) {
                     heldItem *= 1.3;
                  }
                  break;
               case "steelgem":
                  if (moveType.equals(ElementalTypes.STEEL)) {
                     heldItem *= 1.3;
                  }
                  break;
               case "fairygem":
                  if (moveType.equals(ElementalTypes.FAIRY)) {
                     heldItem *= 1.3;
                  }
                  break;
               case "punchingglove":
                  if (punchingMoves.contains(move.getName())) {
                     heldItem *= 1.1;
                  }
                  break;
               case "deepseatooth":
                  if (attackerName.equals("clamperl") && !physical) {
                     attackerEffectiveAttack *= 2.0;
                  }
                  break;
               case "lightball":
                  if (attackerName.contains("pikachu")) {
                     attackerEffectiveAttack *= 2.0;
                  }
                  break;
               case "thickclub":
                  if ((attackerName.equals("cubone") || attackerName.contains("marowak")) && physical) {
                     attackerEffectiveAttack *= 2.0;
                  }
            }

            if (!defenderHeldItem.equals("utilityumbrella") && !attackerHeldItem.equals("utilityumbrella") && move.getName().equals("weatherball")) {
               if (battle != null) {
                  typeDamage = RBTypeChart.getEffectiveness(weatherBallType(battle), defender);
               } else {
                  typeDamage = RBTypeChart.getEffectiveness(ElementalTypes.NORMAL, defender);
               }
            }

            switch (defenderHeldItem) {
               case "chilanberry":
                  if (moveType.equals(ElementalTypes.NORMAL)) {
                     heldItem /= 2.0;
                  }
                  break;
               case "occaberry":
                  if (typeDamage >= 2.0 && moveType.equals(ElementalTypes.FIRE)) {
                     heldItem /= 2.0;
                  }
                  break;
               case "passhoberry":
                  if (typeDamage >= 2.0 && moveType.equals(ElementalTypes.WATER)) {
                     heldItem /= 2.0;
                  }
                  break;
               case "wacanberry":
                  if (typeDamage >= 2.0 && moveType.equals(ElementalTypes.ELECTRIC)) {
                     heldItem /= 2.0;
                  }
                  break;
               case "rindoberry":
                  if (typeDamage >= 2.0 && moveType.equals(ElementalTypes.GRASS)) {
                     heldItem /= 2.0;
                  }
                  break;
               case "yacheberry":
                  if (typeDamage >= 2.0 && moveType.equals(ElementalTypes.ICE)) {
                     heldItem /= 2.0;
                  }
                  break;
               case "chopleberry":
                  if (typeDamage >= 2.0 && moveType.equals(ElementalTypes.FIGHTING)) {
                     heldItem /= 2.0;
                  }
                  break;
               case "kebiaberry":
                  if (typeDamage >= 2.0 && moveType.equals(ElementalTypes.POISON)) {
                     heldItem /= 2.0;
                  }
                  break;
               case "shucaberry":
                  if (typeDamage >= 2.0 && moveType.equals(ElementalTypes.GROUND)) {
                     heldItem /= 2.0;
                  }
                  break;
               case "cobaberry":
                  if (typeDamage >= 2.0 && moveType.equals(ElementalTypes.FLYING)) {
                     heldItem /= 2.0;
                  }
                  break;
               case "payapaberry":
                  if (typeDamage >= 2.0 && moveType.equals(ElementalTypes.PSYCHIC)) {
                     heldItem /= 2.0;
                  }
                  break;
               case "tangaberry":
                  if (typeDamage >= 2.0 && moveType.equals(ElementalTypes.BUG)) {
                     heldItem /= 2.0;
                  }
                  break;
               case "chartiberry":
                  if (typeDamage >= 2.0 && moveType.equals(ElementalTypes.ROCK)) {
                     heldItem /= 2.0;
                  }
                  break;
               case "kasibberry":
                  if (typeDamage >= 2.0 && moveType.equals(ElementalTypes.GHOST)) {
                     heldItem /= 2.0;
                  }
                  break;
               case "habanberry":
                  if (typeDamage >= 2.0 && moveType.equals(ElementalTypes.DRAGON)) {
                     heldItem /= 2.0;
                  }
                  break;
               case "colburberry":
                  if (typeDamage >= 2.0 && moveType.equals(ElementalTypes.DARK)) {
                     heldItem /= 2.0;
                  }
                  break;
               case "babiriberry":
                  if (typeDamage >= 2.0 && moveType.equals(ElementalTypes.STEEL)) {
                     heldItem /= 2.0;
                  }
                  break;
               case "roseliberry":
                  if (typeDamage >= 2.0 && moveType.equals(ElementalTypes.FAIRY)) {
                     heldItem /= 2.0;
                  }
                  break;
               case "deepseascale":
                  if (defenderName.equals("clamperl") && !physical) {
                     defenderEffectiveDefence *= 2.0;
                  }
                  break;
               case "eviolite":
                  if (defender.getOriginalPokemon().getEvolutions().iterator().hasNext()) {
                     defenderEffectiveDefence *= 1.5;
                  }
                  break;
               case "assaultvest":
                  if (!physical) {
                     defenderEffectiveDefence *= 1.5;
                  }
                  break;
               case "metalpowder":
                  if (defenderName.equals("ditto") && physical) {
                     defenderEffectiveDefence *= 1.5;
                  }
            }

            if (!defenderHeldItem.equals("utilityumbrella") && !attackerHeldItem.equals("utilityumbrella")) {
               if (sun && moveType.equals(ElementalTypes.FIRE) || rain && moveType.equals(ElementalTypes.WATER)) {
                  weather *= 1.5;
               }

               if (sun && moveType.equals(ElementalTypes.WATER) || rain && moveType.equals(ElementalTypes.FIRE)) {
                  weather *= 0.5;
               }

               if ((snow || hail) && (move.getName().equals("solarblade") || move.getName().equals("solarbeam"))) {
                  weather *= 0.5;
               }

               ElementalType secondary = defender.getEffectedPokemon().getSecondaryType();
               if (physical
                  && snow
                  && (defender.getEffectedPokemon().getPrimaryType().equals(ElementalTypes.ICE) || secondary != null && secondary.equals(ElementalTypes.ICE))) {
                  defenderEffectiveDefence *= 1.5;
               }
            }

            boolean terastal = BattleStates.get(attacker.actor.battle).getPokemonState(attacker).has(Custom.TERA);
            boolean adapt = attackerAbility.equals("adaptability");
            if ((!terastal || attackerTeraType == null) && !predictTera) {
               if (moveType.equals(attackerPrimaryType) || moveType.equals(attackerSecondaryType)) {
                  stab = adapt ? 2.0 : 1.5;
               }
            } else if (moveType.equals(attackerPrimaryType)
               || moveType.equals(attackerSecondaryType)
               || moveType.getName().equalsIgnoreCase(attackerTeraType.showdownId())) {
               boolean isTeraStab = moveType.getName().equalsIgnoreCase(attackerTeraType.showdownId());
               boolean isRegularStab = moveType.getName().toLowerCase().equals(attackerPrimaryType.getName())
                  || moveType.getName().toLowerCase().equals(attackerSecondaryType.getName());
               stab = isTeraStab && !adapt ? 2.0 : (isRegularStab && adapt && isTeraStab ? 2.25 : 1.5);
            }

            switch (move.getName()) {
               case "weatherball":
                  if (battle != null) {
                     movePower = !weatherBallType(battle).equals(ElementalTypes.NORMAL) ? 100.0 : 50.0;
                  } else {
                     movePower = 50.0;
                  }
                  break;
               case "venoshock":
                  movePower = !Status.psn(defender) && !Status.tox(defender) ? 65.0 : 130.0;
                  break;
               case "smellingsalts":
                  movePower = Status.par(defender) ? 120.0 : 60.0;
                  break;
               case "hex":
                  movePower = Status.any(defender) ? 100.0 : 50.0;
                  break;
               case "acrobatics":
                  movePower = !hasAHeldItem(attacker) && !attackerHeldItem.equals("flyingem") ? 55.0 : 110.0;
                  break;
               case "knockoff":
                  movePower = !defenderHeldItem.isEmpty() ? 65.0 : 20.0;
                  break;
               case "barbbarrage":
                  movePower = !Status.psn(defender) && !Status.tox(defender) ? 60.0 : 120.0;
                  break;
               case "boltbeak":
                  movePower = getEffectiveSpeed(attacker, statStages.getStatMap(attacker)) > getEffectiveSpeed(defender, statStages.getStatMap(defender))
                     ? 170.0
                     : 85.0;
                  break;
               case "brine":
                  movePower = (double)defender.getHealth() / defender.getMaxHealth() <= 0.5 ? 130.0 : 65.0;
                  break;
               case "collisioncourse":
                  otherDamage *= typeDamage > 1.0 ? 1.33 : 1.0;
                  break;
               case "earthquake":
               case "bulldoze":
               case "magnitude":
                  movePower *= Terrain.grassyterrain(defender) ? 0.5 : 1.0;
                  break;
               case "electrodrift":
                  otherDamage *= typeDamage > 1.0 ? 1.33 : 1.0;
                  break;
               case "expandingforce":
                  movePower *= Terrain.psychicterrain(attacker) && !State.raised(attacker) ? 1.5 : 1.0;
                  break;
               case "facade":
                  movePower = !isAttackerPoisoned && !isAttackerParalysed && !isAttackerBurned ? 70.0 : 140.0;
                  break;
               case "gravapple":
                  movePower = Gravity.gravity(attacker) ? 120.0 : 80.0;
                  break;
               case "mistyexplosion":
                  movePower = Terrain.mistyterrain(attacker) ? 150.0 : 100.0;
                  break;
               case "payback":
                  movePower = getEffectiveSpeed(attacker, statStages.getStatMap(attacker)) < getEffectiveSpeed(defender, statStages.getStatMap(defender))
                     ? 100.0
                     : 50.0;
                  break;
               case "risingvoltage":
                  movePower = Terrain.electricterrain(attacker) ? 140.0 : 70.0;
            }

            if (lightscreen && !physical) {
               if (!hasAbility("infiltrator", attacker)) {
                  otherDamage *= 0.66;
               }
            } else if (reflect && physical && !hasAbility("infiltrator", attacker)) {
               otherDamage *= 0.66;
            }

            double multipower = multiHitPower(move, attacker);
            if (multipower != movePower) {
               movePower = multipower;
            }

            if (move.getName().equalsIgnoreCase("lastrespects")) {
               movePower = lastRespectsMultiplier(attacker) * 50.0;
            }

            if (move.getName().equalsIgnoreCase("storedpower")) {
               movePower = totalStatStages(statStages.getStatMap(attacker)) * 20.0;
            }

            double baseDamage = (2 * attackerLevel / 5.0 + 2.0) * movePower * attackerEffectiveAttack / defenderEffectiveDefence / 50.0 + 2.0;
            return baseDamage * targets * PB * weather * GR * critical * stab * typeDamage * otherDamage * isBurn * heldItem * ability * targets;
         } else {
            ModCommon.LOG.info("Attack or Defender is null when checking dmg");
            return 0.0;
         }
      } else {
         return 0.0;
      }
   }

   public static int damage(
      BattlePokemon attacker,
      BattlePokemon defender,
      Move move,
      ActiveBattlePokemon activeBattlePokemon,
      boolean predictTera,
      boolean isAttacker,
      RBStatStages stages
   ) {
      String damageCategory = move.getDamageCategory().getName();
      if (attacker != null && defender != null) {
         boolean reflect = Screen.reflect(defender);
         boolean lightscreen = Screen.lightscreen(defender);
         boolean glaiveRush = move.getName().equalsIgnoreCase("glaiverush");
         boolean parentalBond = attacker.getEffectedPokemon().getAbility().getName().equals("parentalbond");
         if (damageCategory.equals(DamageCategories.INSTANCE.getSTATUS().getName())) {
            return 0;
         }

         boolean isPhysicalMove = damageCategory.equals(DamageCategories.INSTANCE.getPHYSICAL().getName());
         boolean isAttackerBurned = Status.brn(attacker);
         return (int)Math.ceil(
            damage(
               move,
               isPhysicalMove,
               spreadMultiplier(attacker, move) < 1.0,
               parentalBond,
               glaiveRush,
               isAttackerBurned,
               false,
               reflect,
               lightscreen,
               attacker,
               defender,
               stages,
               activeBattlePokemon,
               predictTera,
               isAttacker
            )
         );
      } else {
         return 0;
      }
   }

   public static double calcAttackWithStatChanges(boolean isPhysical, BattlePokemon attacker, Map<Stats, Integer> statStages) {
      double multiplier = 1.0;
      if (statStages == null) {
         return isPhysical
            ? BattleStates.getTransformationOrEffected(attacker).getAttack()
            : BattleStates.getTransformationOrEffected(attacker).getSpecialAttack();
      } else if (isPhysical) {
         double statChange = statStages.getOrDefault(Stats.ATTACK, 0).intValue();
         multiplier = (2.0 + Math.max(statChange, 0.0)) / (2.0 - Math.min(statChange, 0.0));
         int atkStat = BattleStates.getTransformationOrEffected(attacker).getAttack();
         return atkStat * multiplier;
      } else {
         double statChange = statStages.getOrDefault(Stats.SPECIAL_ATTACK, 0).intValue();
         multiplier = (2.0 + Math.max(statChange, 0.0)) / (2.0 - Math.min(statChange, 0.0));
         return BattleStates.getTransformationOrEffected(attacker).getSpecialAttack() * multiplier;
      }
   }

   public static double calcDefenseWithStatChanges(boolean isPhysical, BattlePokemon defender, Map<Stats, Integer> statStages) {
      double multiplier = 1.0;
      double specialDefenseStat = BattleStates.getTransformationOrEffected(defender).getSpecialDefence();
      double defenseStat = BattleStates.getTransformationOrEffected(defender).getDefence();
      if (isPhysical) {
         double statChange = statStages.getOrDefault(Stats.DEFENCE, 0).intValue();
         multiplier = (2.0 + Math.max(statChange, 0.0)) / (2.0 - Math.min(statChange, 0.0));
         return defenseStat * multiplier;
      } else {
         double statChange = statStages.getOrDefault(Stats.SPECIAL_DEFENCE, 0).intValue();
         multiplier = (2.0 + Math.max(statChange, 0.0)) / (2.0 - Math.min(statChange, 0.0));
         return specialDefenseStat * multiplier;
      }
   }

   public static boolean doesOurMoveCrit(Move attackingMove, BattlePokemon attackingMon, BattlePokemon defendingMon) {
      String defenderAbility = defendingMon.getEffectedPokemon().getAbility().getName();
      String attackerAbility = attackingMon.getEffectedPokemon().getAbility().getName();
      String attackerName = attackingMon.getEffectedPokemon().getDisplayName(false).toString().toLowerCase(Locale.ROOT);
      String attackerHeldItem = attackingMon.getHeldItemManager().showdownId(attackingMon) != null
         ? attackingMon.getHeldItemManager().showdownId(attackingMon)
         : "";
      int critStage = 0;
      if (!defenderAbility.equalsIgnoreCase("shellarmor") && !defenderAbility.equalsIgnoreCase("battlearmor")) {
         if (RBMoveList.getCriticalMoves().contains(attackingMove.getName().toLowerCase())) {
            return true;
         }

         if (!attackerAbility.equals("merciless") || isSuppressed(attackingMon) || !Status.psn(defendingMon) && !Status.tox(defendingMon)) {
            if (RBMoveList.getHighCriticalMoves().contains(attackingMove.getName().toLowerCase())) {
               critStage++;
            }

            if (attackerHeldItem != null) {
               switch (attackerHeldItem) {
                  case "razorclaw":
                  case "scopelens":
                     critStage++;
                     break;
                  case "leek":
                  case "stick":
                     if (attackerName.equals("sirfetch'd") || attackerName.equals("farfetch'd")) {
                        critStage += 2;
                     }
                     break;
                  case "luckypunch":
                     if (attackerName.equals("chansey")) {
                        critStage += 2;
                     }
               }
            }

            if (attackerAbility.equalsIgnoreCase("superluck")) {
               critStage++;
            }

            return critStage >= 3;
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   public static ElementalType weatherBallType(PokemonBattle battle) {
      Collection<BattleContext> weather = battle.getContextManager().get(Type.WEATHER);
      ElementalType moveType = ElementalTypes.NORMAL;
      if (weather != null && !weather.isEmpty()) {
         BattleContext current = null;

         for (BattleContext w : weather) {
            if (w != null) {
               current = w;
               break;
            }
         }

         if (current == null) {
            return moveType;
         }

         return switch (current.getId()) {
            case "raining", "heavyrain", "rain" -> ElementalTypes.WATER;
            case "sunny", "harshsunlight", "extremelyharshsunlight" -> ElementalTypes.FIRE;
            case "sandstorm" -> ElementalTypes.ROCK;
            case "snow", "hail" -> ElementalTypes.ICE;
            default -> ElementalTypes.NORMAL;
         };
      } else {
         return moveType;
      }
   }

   public static boolean isSuppressed(BattlePokemon battlePokemon, boolean neutralizingGas) {
      if (battlePokemon == null) {
         return false;
      }

      String helditem = battlePokemon.getHeldItemManager().showdownId(battlePokemon) != null
         ? battlePokemon.getHeldItemManager().showdownId(battlePokemon)
         : "";
      return helditem != null && helditem.equals("abilityshield") ? false : neutralizingGas;
   }

   public static boolean isSuppressed(BattlePokemon battlePokemon) {
      if (battlePokemon == null) {
         return false;
      }

      PokemonBattle battle = battlePokemon.getActor().battle;
      if (battle == null) {
         return false;
      }

      Iterable<ActiveBattlePokemon> activePokemon = battle.getActivePokemon();
      String ability = battlePokemon.getEffectedPokemon().getAbility().getName();
      if (RBMoveList.getAbilitiesThatCannotBeSuppressed().contains(ability)) {
         return false;
      }

      String item = battlePokemon.getHeldItemManager().showdownId(battlePokemon);
      if (item != null && "abilityshield".equals(item)) {
         return false;
      }

      for (ActiveBattlePokemon abp : activePokemon) {
         if (abp != null) {
            BattlePokemon bp = abp.getBattlePokemon();
            if (bp != null) {
               String abilityCheck = bp.getEffectedPokemon().getAbility().getName();
               if ("neutralizinggas".equals(abilityCheck)) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   public static boolean ignoreAbilities(BattlePokemon battlePokemon) {
      List<String> ignoreList = List.of("moldbreaker", "teravolt", "turboblaze");
      boolean isSuppressed = isSuppressed(battlePokemon);
      String ability = battlePokemon.getEffectedPokemon().getAbility().getName();
      return !isSuppressed ? ignoreList.contains(ability) : false;
   }

   public static boolean hasBeenHitByType(BattlePokemon battlePokemon, ElementalType element) {
      Map<Integer, MoveTemplate> history = RBSlotInformation.getMovesUsedAgainstPokemon(battlePokemon, false);

      for (MoveTemplate mt : history.values()) {
         if (mt.getElementalType().equals(element)) {
            return true;
         }
      }

      return false;
   }

   public static int mostRecentHitOfType(BattlePokemon battlePokemon, ElementalType element) {
      Map<Integer, MoveTemplate> history = RBSlotInformation.getMovesUsedAgainstPokemon(battlePokemon, false);
      int saved = -1;

      for (int turn : history.keySet()) {
         if (history.get(turn).getElementalType().equals(element)) {
            saved = turn;
         }
      }

      return saved;
   }

   public static int lastUsedMoveOfType(BattlePokemon battlePokemon, ElementalType element) {
      Map<Integer, MoveTemplate> history = RBSlotInformation.getMoveHistory(battlePokemon, false);
      int saved = -1;

      for (int turn : history.keySet()) {
         if (history.get(turn).getElementalType().equals(element)) {
            saved = turn;
         }
      }

      return saved;
   }

   public static boolean isImmuneCheck(
      Move move, BattlePokemon attacker, BattlePokemon defender, ActiveBattlePokemon abp, ElementalType teraType, boolean predictTera
   ) {
      if (attacker != null && defender != null) {
         MoveTarget moveTarget = move.getTemplate().getTarget();
         Set<MoveTarget> bypassTypeImmunity = Set.of(
            MoveTarget.all, MoveTarget.allySide, MoveTarget.allyTeam, MoveTarget.allies, MoveTarget.foeSide, MoveTarget.self, MoveTarget.scripted
         );
         if (bypassTypeImmunity.contains(moveTarget)) {
            return false;
         }

         if (move.getCurrentPp() <= 0) {
            return true;
         }

         ElementalType moveType = move.getType();
         String moveName = move.getName();
         ElementalType primaryType = defender.getEffectedPokemon().getPrimaryType();
         ElementalType secondaryType = defender.getEffectedPokemon().getSecondaryType();
         String heldItem = defender.getHeldItemManager().showdownId(defender);
         String defenderAbilityId = BattleStates.getTransformationOrEffected(defender).getAbility().getName();
         String attackerAbilityId = BattleStates.getTransformationOrEffected(attacker).getAbility().getName();
         List<String> statusMoves = RBMoveList.getNonVolatileMoves();
         List<String> ignoreStatusAbilities = List.of("comatose", "goodasgold");
         List<String> firstTurnMoves = List.of("firstimpression", "fakeout");
         List<String> ignoreDefenderAbilityList = List.of("moldbreaker", "teravolt", "turboblaze");
         boolean ignoreDefenderAbility = hasAbility(ignoreDefenderAbilityList, attacker) && !Objects.equals(heldItem, "abilityshield");
         if (predictTera) {
            primaryType = teraType;
            secondaryType = null;
         }

         Iterable<ActiveBattlePokemon> activePokemon = attacker.getActor().getBattle().getActivePokemon();
         boolean isImmune = moveType == ElementalTypes.NORMAL && (primaryType == ElementalTypes.GHOST || secondaryType == ElementalTypes.GHOST)
            || moveType == ElementalTypes.FIGHTING && (primaryType == ElementalTypes.GHOST || secondaryType == ElementalTypes.GHOST)
            || moveType == ElementalTypes.GHOST && (primaryType == ElementalTypes.NORMAL || secondaryType == ElementalTypes.NORMAL)
            || moveType == ElementalTypes.ELECTRIC && (primaryType == ElementalTypes.GROUND || secondaryType == ElementalTypes.GROUND)
            || moveType == ElementalTypes.GROUND && (primaryType == ElementalTypes.FLYING || secondaryType == ElementalTypes.FLYING)
            || moveType == ElementalTypes.POISON && (primaryType == ElementalTypes.STEEL || secondaryType == ElementalTypes.STEEL)
            || moveType == ElementalTypes.DRAGON && (primaryType == ElementalTypes.FAIRY || secondaryType == ElementalTypes.FAIRY)
            || moveType == ElementalTypes.PSYCHIC && (primaryType == ElementalTypes.DARK || secondaryType == ElementalTypes.DARK);
         if (isImmune && heldItem != null && heldItem.equals("ringtarget")) {
            isImmune = false;
         }

         if (!ignoreDefenderAbility) {
            if (moveType.equals(ElementalTypes.WATER)) {
               if (defenderAbilityId.equals("stormdrain") || defenderAbilityId.equals("waterabsorb") || defenderAbilityId.equals("dryskin")) {
                  return true;
               }
            } else if (moveType.equals(ElementalTypes.ELECTRIC)) {
               if (defenderAbilityId.equals("voltabsorb") || defenderAbilityId.equals("lightningrod") || defenderAbilityId.equals("motordrive")) {
                  return true;
               }
            } else if (moveType.equals(ElementalTypes.GROUND)) {
               if (defenderAbilityId.equals("eartheater")) {
                  return true;
               }
            } else if (moveType.equals(ElementalTypes.FIRE)) {
               if (defenderAbilityId.equals("wellbakedbody") || defenderAbilityId.equals("flashfire")) {
                  return true;
               }
            } else if (moveType.equals(ElementalTypes.GRASS) && defenderAbilityId.equals("sapsipper")) {
               return true;
            }

            if (RBMoveList.ballAndBulletMoves.contains(moveName) && defenderAbilityId.equals("bulletproof")) {
               return true;
            }

            if (statusMoves.contains(moveName) && hasAbility(ignoreStatusAbilities, defender)) {
               return true;
            }

            if (statusMoves.contains(moveName) && Status.any(defender)) {
               return true;
            }

            if (statusMoves.contains(moveName) && hasAbility("hydration", defender) && (Weather.rain(defender) || Weather.heavyrain(defender))) {
               return true;
            }

            if (statusMoves.contains(moveName) && hasAbility("leafguard", defender) && Weather.harshsunlight(defender)) {
               return true;
            }

            if (RBMoveList.getExplosionMoves().contains(moveName) && hasAbility((List<ActiveBattlePokemon>)activePokemon, "damp")) {
               return true;
            }

            ArrayList<String> paraMoves = new ArrayList<>(List.of("thunderwave", "stunspore", "glare", "nuzzle"));
            if (paraMoves.contains(moveName) && hasAbility("limber", defender)) {
               return true;
            }

            if (RBMoveList.getSleepMoves().contains(moveName) && hasAbility(RBMoveList.getIgnoreSleepAbilities(), defender)) {
               return true;
            }

            ArrayList<String> tauntInfatuateMoves = new ArrayList<>(List.of("attract", "taunt"));
            if (tauntInfatuateMoves.contains(moveName) && hasAbility(tauntInfatuateMoves, defender)) {
               return true;
            }

            ArrayList<String> poisonMoves = new ArrayList<>(List.of("poisongas", "toxic", "poisonpowder"));
            ArrayList<String> poiImmuneAbilities = new ArrayList<>(List.of("immunity", "poisonheal"));
            if (hasAbility(poiImmuneAbilities, defender) && poisonMoves.contains(moveName)) {
               return true;
            }

            if (poisonMoves.contains(moveName) && hasAbility(RBBattleSlots.getOpponents(abp), "pastelveil")) {
               return true;
            }

            if (poisonMoves.contains(moveName)) {
               ElementalType defType1 = defender.getEffectedPokemon().getPrimaryType();
               ElementalType defType2 = defender.getEffectedPokemon().getSecondaryType();
               if (hasAbility("corrosion", attacker)) {
                  if (defType1 == ElementalTypes.STEEL
                     || defType1 == ElementalTypes.POISON
                     || defType2 == ElementalTypes.STEEL
                     || defType2 == ElementalTypes.POISON) {
                     isImmune = false;
                  }
               } else if (defType1 == ElementalTypes.STEEL
                  || defType1 == ElementalTypes.POISON
                  || defType2 == ElementalTypes.STEEL
                  || defType2 == ElementalTypes.POISON) {
                  isImmune = true;
               }
            }

            ArrayList<String> confusionMoves = new ArrayList<>(List.of("confuseray", "shadowpanic", "sweetkiss", "teeterdance", "supersonic"));
            if (confusionMoves.contains(moveName) && hasAbility("owntempo", defender)) {
               return true;
            }

            ArrayList<String> forceSwitchMoves = new ArrayList<>(List.of("whirlwind", "roar"));
            if (hasAbility("suctioncups", defender) && forceSwitchMoves.contains(moveName)) {
               return true;
            }

            if (moveType == ElementalTypes.GROUND && !moveName.equals("thousandarrows") && State.raised(defender)) {
               return true;
            }

            ArrayList<String> accLoweringMoves = new ArrayList<>(List.of("smokescreen", "sandattack", "flash", "kinesis"));
            ArrayList<String> accIgnoreAbilities = new ArrayList<>(List.of("illuminate", "keeneye"));
            if (hasAbility(accIgnoreAbilities, defender) && accLoweringMoves.contains(moveName)) {
               return true;
            }

            if (hasAbility("soundproof", defender) && RBMoveList.getSoundMoves().contains(moveName)) {
               return true;
            }

            ArrayList<String> evaLoweringMoves = new ArrayList<>(List.of("minimize", "doubleteam"));
            if (hasAbility("keeneye", defender) && evaLoweringMoves.contains(moveName)) {
               return true;
            }

            if ((hasAbility("scrappy", attacker) || hasAbility("mindseye", attacker))
               && (moveType == ElementalTypes.NORMAL || moveType == ElementalTypes.FIGHTING)
               && (primaryType == ElementalTypes.GHOST || secondaryType == ElementalTypes.GHOST)) {
               isImmune = false;
            }

            if (hasAbility("overcoat", defender) && RBMoveList.getSporePowderMoves().contains(moveName)) {
               return true;
            }

            if (Weather.heavyrain(attacker)) {
               if (moveType == ElementalTypes.FIRE) {
                  isImmune = true;
               }
            } else if (Weather.extremelyharshsunlight(attacker) && moveType == ElementalTypes.WATER) {
               isImmune = true;
            }

            if (abp != null && RBMoveList.getPriorityDamageMoves().contains(moveName)) {
               if (hasAbility(RBBattleSlots.getOpponents(abp), "queenlymajesty")) {
                  return true;
               }

               if (hasAbility(RBBattleSlots.getOpponents(abp), "dazzling")) {
                  return true;
               }

               if (hasAbility(RBBattleSlots.getOpponents(abp), "armortail")) {
                  return true;
               }
            }

            if (hasAbility("wellbakedbody", defender) && moveType.equals(ElementalTypes.FIRE)) {
               return true;
            }

            if (hasAbility("windrider", defender) && RBMoveList.getWindMoves().contains(moveName)) {
               return true;
            }
         }

         if (RBMoveList.getSporePowderMoves().contains(moveName) && hasElementalType(defender, ElementalTypes.GRASS)) {
            return true;
         } else {
            return BattleStates.get(attacker.actor.battle).getPokemonState(attacker).age(Custom.TURN) != 1 && firstTurnMoves.contains(moveName)
               ? true
               : isImmune;
         }
      } else {
         return false;
      }
   }

   public static double multiHitPower(Move move, BattlePokemon attacker) {
      double basePower = move.getPower();
      String attackerHeldItem = attacker.getHeldItemManager().showdownId(attacker) != null ? attacker.getHeldItemManager().showdownId(attacker) : "";
      List<String> moves25 = RBMoveList.getMultiHit2to5();
      List<String> moves2 = RBMoveList.getMultiHit2();
      List<String> moves3 = RBMoveList.getMultiHit3();
      if (moves25.contains(move.getName().toLowerCase())) {
         if ("skilllink".equals(attacker.getEffectedPokemon().getAbility().getName())) {
            return basePower * 5.0;
         } else {
            return "loadeddice".equals(attackerHeldItem) ? basePower * 4.0 : basePower * 3.0;
         }
      } else {
         if (moves2.contains(move.getName().toLowerCase())) {
            return basePower * 2.0;
         }

         if (moves3.contains(move.getName().toLowerCase())) {
            return basePower * 3.0;
         }

         return switch (move.getName().toLowerCase()) {
            case "tripleaxel" -> 120.0;
            case "populationbomb" -> basePower * 10.0;
            case "triplekick" -> 60.0;
            default -> basePower;
         };
      }
   }

   public static ElementalType teraToElementalType(BattlePokemon battlePokemon) {
      TeraType teraType = battlePokemon.getEffectedPokemon().getTeraType();

      for (ElementalType types : ElementalTypes.all()) {
         if (types.getName().equalsIgnoreCase(teraType.getName())) {
            return types;
         }
      }

      return null;
   }

   public static boolean hasAbility(List<ActiveBattlePokemon> checkList, String ability) {
      for (ActiveBattlePokemon abp : checkList) {
         if (abp != null) {
            BattlePokemon battlePokemon = abp.getBattlePokemon();
            if (battlePokemon != null) {
               boolean suppressed = isSuppressed(battlePokemon);
               if (!suppressed && battlePokemon.getEffectedPokemon().getAbility().getName().equals(ability)) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   public static boolean hasAbility(List<String> abilities, BattlePokemon battlePokemon) {
      if (battlePokemon == null) {
         return false;
      }

      String ability = battlePokemon.getEffectedPokemon().getAbility().getName();
      boolean isSuppressed = isSuppressed(battlePokemon);
      return abilities.contains(ability) && !isSuppressed;
   }

   public static boolean hasAbility(String checkAbility, BattlePokemon battlePokemon) {
      if (battlePokemon == null) {
         return false;
      }

      String ability = battlePokemon.getEffectedPokemon().getAbility().getName();
      boolean isSuppressed = isSuppressed(battlePokemon);
      return checkAbility.equals(ability) && !isSuppressed;
   }

   public static double auraAbilityCalculation(PokemonBattle battle, String aura) {
      boolean dark = false;
      boolean fairy = false;
      boolean auraBreak = false;

      for (ActiveBattlePokemon abp : battle.getActivePokemon()) {
         if (abp != null && abp.isAlive()) {
            BattlePokemon bp = abp.getBattlePokemon();
            if (bp != null) {
               String ability = BattleStates.getTransformationOrEffected(bp).getAbility().getName();
               switch (ability) {
                  case "neutralizinggas":
                     return 1.0;
                  case "darkaura":
                     dark = true;
                     break;
                  case "fairyaura":
                     fairy = true;
                     break;
                  case "aurabreak":
                     auraBreak = true;
               }
            }
         }
      }

      boolean auraActive = aura.equals("dark") ? dark : fairy;
      if (!auraActive) {
         return 1.0;
      } else {
         return auraBreak ? 0.75 : 1.33;
      }
   }

   public static double spreadMultiplier(BattlePokemon attacker, Move move) {
      InBattleMove inBattleMove = RBTypeChart.getInBattleMove(move, attacker);
      if (inBattleMove == null) {
         return 1.0;
      }

      MoveTarget target = inBattleMove.getTarget();
      if (target != MoveTarget.allAdjacent && target != MoveTarget.allAdjacentFoes) {
         return 1.0;
      }

      ActiveBattlePokemon active = attacker.getActor().getActivePokemon().stream().filter(abp -> abp.getBattlePokemon() == attacker).findFirst().orElse(null);
      if (active == null) {
         return 1.0;
      }

      List<Targetable> targets = active.getMultiTargetList(target);
      if (targets == null) {
         return 1.0;
      }

      long aliveTargets = targets.stream().filter(t -> t instanceof ActiveBattlePokemon abp && abp.isAlive()).count();
      return aliveTargets >= 2L ? 0.75 : 1.0;
   }

   public static double numberOfFaintedPokemon(BattlePokemon attacker) {
      return attacker.getActor().getPokemonList().stream().filter(p -> p != attacker && p.getHealth() <= 0).count();
   }

   public static double lastRespectsMultiplier(BattlePokemon attacker) {
      Map<UUID, Integer> actorCounts = FAINT_COUNTS.get(attacker.getActor().getBattle().getBattleId());
      if (actorCounts == null) {
         return 1.0;
      }

      int fainted = actorCounts.getOrDefault(attacker.getActor().getUuid(), 0);
      return 1.0 + fainted;
   }

   public static int totalStatStages(Map<Stats, Integer> stages) {
      int count = 0;

      for (int i : stages.values()) {
         count += i;
      }

      return count;
   }

   public static double getEffectiveSpeed(BattlePokemon pokemon, Map<Stats, Integer> stages) {
      double multiplier = 1.0;
      int stage = 0;
      if (pokemon == null) {
         return -1.0;
      }

      String ability = pokemon.getEffectedPokemon().getAbility().getName();
      double currentSpeed = BattleStates.getTransformationOrEffected(pokemon).getSpeed();
      boolean isSuppressed = isSuppressed(pokemon);
      String heldItem = pokemon.getHeldItemManager().showdownId(pokemon) != null ? pokemon.getHeldItemManager().showdownId(pokemon) : "";
      if (ability.equals("stall")) {
         return 0.0;
      }

      if (stages != null && !stages.isEmpty()) {
         stage = stages.getOrDefault(Stats.SPEED, 0);
      }

      multiplier = (double)(2 + Math.max(stage, 0)) / (2 - Math.min(stage, 0));
      if (Status.par(pokemon) && (!ability.equals("quickfeet") || isSuppressed)) {
         multiplier /= 2.0;
      }

      if (Status.any(pokemon) && ability.equals("quickfeet") && !isSuppressed) {
         multiplier *= 2.0;
      }

      boolean isBoostedInSun = ability.equals("chlorophyll") && Weather.harshsunlight(pokemon) && !isSuppressed;
      boolean isBoostedInRain = ability.equals("swiftswim") && Weather.rain(pokemon) && !isSuppressed;
      boolean isBoostedInSand = ability.equals("sandrush") && Weather.sandstorm(pokemon) && !isSuppressed;
      boolean isBoostedInSnow = ability.equals("slushrush") && Weather.snow(pokemon) && !isSuppressed;
      boolean isBoostedInETerrain = ability.equals("surgesurfer") && Terrain.electricterrain(pokemon) && !isSuppressed;
      boolean slowStart = ability.equals("slowstart") && !isSuppressed;
      if (isBoostedInRain || isBoostedInSun || isBoostedInSand || isBoostedInSnow || isBoostedInETerrain) {
         currentSpeed *= 2.0;
      }

      if (slowStart) {
         currentSpeed /= 2.0;
      }

      List<String> halfSpeedItems = new ArrayList<>(
         List.of("ironball", "poweranklet", "powerband", "powerbelt", "powerbracer", "powerlens", "powerweight", "machobrace")
      );
      if (heldItem.equals("choicescarf")) {
         currentSpeed *= 1.5;
      }

      if (heldItem.equals("quickpowder") && pokemon.getName().toString().equalsIgnoreCase("ditto")) {
         currentSpeed *= 2.0;
      }

      if (halfSpeedItems.contains(heldItem.toLowerCase())) {
         currentSpeed /= 2.0;
      }

      if (heldItem.equals("laggingtail")) {
         return Room.trickroom(pokemon) ? 2.147483647E9 : -2.1474836E9F;
      }

      if (Tailwind.tailwind(pokemon)) {
         currentSpeed *= 2.0;
      }

      return currentSpeed * multiplier;
   }

   public static void recordMove(BattlePokemon pokemon, String moveName) {
      pokemonMoveHistory.computeIfAbsent(pokemon.getUuid(), k -> new ArrayList<>()).add(moveName);
   }

   public static void resetMoveHistory(BattlePokemon pokemon) {
      pokemonMoveHistory.remove(pokemon.getUuid());
   }

   public static int getMoveUseCount(BattlePokemon pokemon, String moveName) {
      List<String> history = pokemonMoveHistory.getOrDefault(pokemon.getUuid(), List.of());
      int count = 0;

      for (String move : history) {
         if (move.equals(moveName)) {
            count++;
         }
      }

      return count;
   }

   public static boolean isSandstormFatal(BattlePokemon battlePokemon) {
      if (battlePokemon == null) {
         return false;
      }

      if (!Weather.sandstorm(battlePokemon)) {
         return false;
      }

      String heldItem = battlePokemon.getHeldItemManager().showdownId(battlePokemon) != null
         ? battlePokemon.getHeldItemManager().showdownId(battlePokemon)
         : "";
      String ability = battlePokemon.getEffectedPokemon().getAbility().getName();
      Set<String> immuneAbilities = Set.of("sandveil", "sandrush", "sandforce", "magicguard", "overcoat");
      Set<String> immuneTypes = Set.of("rock", "ground", "steel");
      ElementalType primary = battlePokemon.getEffectedPokemon().getPrimaryType();
      ElementalType secondary = battlePokemon.getEffectedPokemon().getSecondaryType();
      boolean isImmune = immuneTypes.contains(primary.getName().toLowerCase());
      if (secondary != null && !isImmune) {
         isImmune = immuneTypes.contains(secondary.getName().toLowerCase());
      }

      if (heldItem != null && heldItem.equalsIgnoreCase("safetygoggles")) {
         return false;
      }

      if (immuneAbilities.contains(ability)) {
         return false;
      }

      if (isImmune) {
         return false;
      }

      double percentHP = (double)battlePokemon.getHealth() / battlePokemon.getMaxHealth() * 100.0;
      return percentHP <= 6.25;
   }

   public static boolean isHailFatal(BattlePokemon battlePokemon) {
      if (battlePokemon == null) {
         return false;
      }

      String heldItem = battlePokemon.getHeldItemManager().showdownId(battlePokemon) != null
         ? battlePokemon.getHeldItemManager().showdownId(battlePokemon)
         : "";
      String ability = battlePokemon.getEffectedPokemon().getAbility().getName();
      if (!Weather.hail(battlePokemon)) {
         return false;
      }

      Set<String> immuneAbilities = Set.of("icebody", "snowcloak", "magicguard", "overcoat");
      ElementalType primary = battlePokemon.getEffectedPokemon().getPrimaryType();
      ElementalType secondary = battlePokemon.getEffectedPokemon().getSecondaryType();
      boolean isImmune = false;
      if (hasElementalType(battlePokemon, ElementalTypes.ICE)) {
         return false;
      }

      if (heldItem != null && heldItem.equalsIgnoreCase("safetygoggles")) {
         return false;
      }

      if (immuneAbilities.contains(ability)) {
         return false;
      }

      double percentHP = (double)battlePokemon.getHealth() / battlePokemon.getMaxHealth() * 100.0;
      return percentHP <= 6.25;
   }

   public static boolean hasElementalType(BattlePokemon battlePokemon, ElementalType elementalType) {
      if (battlePokemon == null) {
         return false;
      }

      ElementalType primary = battlePokemon.getEffectedPokemon().getPrimaryType();
      ElementalType secondary = battlePokemon.getEffectedPokemon().getSecondaryType();
      return primary != null && primary.equals(elementalType) ? true : secondary != null && secondary.equals(elementalType);
   }

   public static boolean hasAHeldItem(BattlePokemon battlePokemon) {
      if (battlePokemon == null) {
         return false;
      }

      String item = battlePokemon.getHeldItemManager().showdownId(battlePokemon);
      return item != null && !item.equalsIgnoreCase("");
   }

   public static double projectedHPGainNextTurn(BattlePokemon battlePokemon, BattlePokemon opponent) {
      double projectedHpGain = 0.0;
      double maxHp = battlePokemon.getMaxHealth();
      String heldItem = battlePokemon.getHeldItemManager().showdownId(battlePokemon);
      boolean isBigRoot = heldItem.equals("bigroot");
      boolean isPokemonLeeching = Volatile.leech(opponent);
      boolean heldItemHealing = heldItem.equals("leftovers") || heldItem.equals("blacksludge") && hasElementalType(battlePokemon, ElementalTypes.POISON);
      boolean aquaRing = Volatile.aquaring(battlePokemon);
      boolean rainDish = hasAbility("raindish", battlePokemon) && (Weather.rain(battlePokemon) || Weather.heavyrain(battlePokemon));
      boolean iceBody = hasAbility("icebody", battlePokemon) && (Weather.hail(battlePokemon) || Weather.snow(battlePokemon));
      boolean ingrain = Volatile.ingrain(battlePokemon);
      boolean psnHealing = hasAbility("poisonheal", battlePokemon) && (Status.psn(battlePokemon) || Status.tox(battlePokemon));
      boolean drySkin = hasAbility("dryskin", battlePokemon) && (Weather.rain(battlePokemon) || Weather.heavyrain(battlePokemon));
      boolean[] drain = new boolean[]{aquaRing, ingrain, isPokemonLeeching};

      for (boolean b : drain) {
         if (b && isBigRoot) {
            projectedHpGain += maxHp / 12.0;
         } else if (b) {
            projectedHpGain += maxHp / 16.0;
         }
      }

      boolean[] bools = new boolean[]{heldItemHealing, rainDish, iceBody};

      for (boolean b : bools) {
         if (b) {
            projectedHpGain += maxHp / 16.0;
         }
      }

      boolean[] bools2 = new boolean[]{psnHealing, drySkin};

      for (boolean b : bools) {
         if (b) {
            projectedHpGain += maxHp / 8.0;
         }
      }

      return projectedHpGain > maxHp ? maxHp : projectedHpGain;
   }

   public static boolean hasMove(BattlePokemon battlePokemon, String move) {
      for (Move m : battlePokemon.getMoveSet().getMoves()) {
         if (m.getName().equalsIgnoreCase(move)) {
            return true;
         }
      }

      return false;
   }

   public static int getFlailPower(BattlePokemon pokemon) {
      double hpPercent = (double)pokemon.getHealth() / pokemon.getMaxHealth() * 100.0;
      if (hpPercent < 4.2) {
         return 200;
      } else if (hpPercent < 10.4) {
         return 150;
      } else if (hpPercent < 20.8) {
         return 100;
      } else if (hpPercent < 35.4) {
         return 80;
      } else {
         return hpPercent < 68.8 ? 40 : 20;
      }
   }

   public static int getTrumpCardPower(int remainingPp) {
      switch (remainingPp) {
         case 0:
            return 200;
         case 1:
            return 80;
         case 2:
            return 60;
         case 3:
            return 50;
         default:
            return 40;
      }
   }

   static {
      CobblemonEvents.BATTLE_FAINTED.subscribe(Priority.NORMAL, event -> {
         UUID battleId = event.getBattle().getBattleId();
         UUID actorId = event.getKilled().getActor().getUuid();
         FAINT_COUNTS.computeIfAbsent(battleId, k -> new HashMap<>()).merge(actorId, 1, Integer::sum);
         return Unit.INSTANCE;
      });
      CobblemonEvents.BATTLE_VICTORY.subscribe(Priority.NORMAL, event -> {
         FAINT_COUNTS.remove(event.getBattle().getBattleId());
         return Unit.INSTANCE;
      });
      CobblemonEvents.BATTLE_FLED.subscribe(Priority.NORMAL, event -> {
         FAINT_COUNTS.remove(event.getBattle().getBattleId());
         return Unit.INSTANCE;
      });
   }
}
