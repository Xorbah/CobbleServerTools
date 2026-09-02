package net.cobbleservertools.compat.rct.runbun.api.ai.utils;

import com.cobblemon.mod.common.api.moves.Move;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.api.types.ElementalTypes;
import com.cobblemon.mod.common.api.types.tera.TeraType;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.InBattleMove;
import com.cobblemon.mod.common.battles.ShowdownActionRequest;
import com.cobblemon.mod.common.battles.ShowdownMoveset;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.pokemon.IVs;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.gitlab.srcmc.rctapi.ModCommon;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleStates;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleEffects.Custom;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleEffects.Pokemon.State;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RBTypeChart {
   private static final Map<ElementalType, Map<ElementalType, Double>> CHART = new HashMap<>();
   private static final Map<ElementalType, Double> EMPTY_MAP = Map.of();
   private static final Map<String, Move> MOVE_MAP = new HashMap<>();
   private static final double SUPER_EFFECTIVE = 2.0;
   private static final double NOT_VERY_EFFECTIVE = 0.5;
   private static final double IMMUNE = 0.0;
   private static final String STELLAR_TYPE_ID = "stellar";
   private static final ElementalType[] HIDDENPOWER_TYPES = new ElementalType[]{
      ElementalTypes.FIGHTING,
      ElementalTypes.FLYING,
      ElementalTypes.POISON,
      ElementalTypes.GROUND,
      ElementalTypes.ROCK,
      ElementalTypes.BUG,
      ElementalTypes.GHOST,
      ElementalTypes.STEEL,
      ElementalTypes.FIRE,
      ElementalTypes.WATER,
      ElementalTypes.GRASS,
      ElementalTypes.ELECTRIC,
      ElementalTypes.PSYCHIC,
      ElementalTypes.ICE,
      ElementalTypes.DRAGON,
      ElementalTypes.DARK
   };
   public static final int NORMAL = 2;
   public static final int FIGHTING = 4;
   public static final int FLYING = 8;
   public static final int POISON = 16;
   public static final int GROUND = 32;
   public static final int ROCK = 64;
   public static final int BUG = 128;
   public static final int GHOST = 256;
   public static final int STEEL = 512;
   public static final int FIRE = 1024;
   public static final int WATER = 2048;
   public static final int GRASS = 4096;
   public static final int ELECTRIC = 8192;
   public static final int PSYCHIC = 16384;
   public static final int ICE = 32768;
   public static final int DRAGON = 65536;
   public static final int DARK = 131072;
   public static final int FAIRY = 262144;
   private static final Map<ElementalType, Integer> TYPE_MASKS = Map.ofEntries(
      Map.entry(ElementalTypes.NORMAL, 2),
      Map.entry(ElementalTypes.FIGHTING, 4),
      Map.entry(ElementalTypes.FLYING, 8),
      Map.entry(ElementalTypes.POISON, 16),
      Map.entry(ElementalTypes.GROUND, 32),
      Map.entry(ElementalTypes.ROCK, 64),
      Map.entry(ElementalTypes.BUG, 128),
      Map.entry(ElementalTypes.GHOST, 256),
      Map.entry(ElementalTypes.STEEL, 512),
      Map.entry(ElementalTypes.FIRE, 1024),
      Map.entry(ElementalTypes.WATER, 2048),
      Map.entry(ElementalTypes.GRASS, 4096),
      Map.entry(ElementalTypes.ELECTRIC, 8192),
      Map.entry(ElementalTypes.PSYCHIC, 16384),
      Map.entry(ElementalTypes.ICE, 32768),
      Map.entry(ElementalTypes.DRAGON, 65536),
      Map.entry(ElementalTypes.DARK, 131072),
      Map.entry(ElementalTypes.FAIRY, 262144)
   );

   public static double getEffectiveness(InBattleMove move, BattlePokemon defender) {
      return getEffectiveness(getMove(move).getType(), defender);
   }

   public static double getEffectiveness(ElementalType attackerType, BattlePokemon defender) {
      String defenderAbilityId = BattleStates.getTransformationOrEffected(defender).getAbility().getName();
      List<ElementalType> defenderTypes = getEffectiveTypes(defender, false);
      double e = 1.0;
      boolean isDefenderTera = BattleStates.get(defender.actor.battle).getPokemonState(defender).has(Custom.TERA);
      if (isDefenderTera) {
         ElementalType temp = PokeMathMax.teraToElementalType(defender);
         if (temp == null) {
            return 1.0;
         }

         defenderTypes = new ArrayList<>(List.of(temp));
      }

      if (defenderTypes.isEmpty()) {
         return 1.0;
      }

      for (ElementalType dt : defenderTypes) {
         e *= getEffectiveness(attackerType, dt, defender);
      }

      if (e < 2.0 && defenderAbilityId.equals("wonderguard")) {
         e = 0.0;
      }

      return e;
   }

   public static double getEffectiveness(ElementalType attackerType, BattlePokemon defender, boolean predictTera) {
      String defenderAbilityId = BattleStates.getTransformationOrEffected(defender).getAbility().getName();
      List<ElementalType> defenderTypes = getEffectiveTypes(defender, false);
      double e = 1.0;
      boolean isDefenderTera = BattleStates.get(defender.actor.battle).getPokemonState(defender).has(Custom.TERA);
      if (isDefenderTera || predictTera) {
         ElementalType types = PokeMathMax.teraToElementalType(defender);
         if (types != null) {
            defenderTypes = new ArrayList<>(List.of(types));
         } else if (defender.getEffectedPokemon().getSecondaryType() != null) {
            defenderTypes = new ArrayList<>(List.of(defender.getEffectedPokemon().getPrimaryType(), defender.getEffectedPokemon().getSecondaryType()));
         } else {
            defenderTypes = new ArrayList<>(List.of(defender.getEffectedPokemon().getPrimaryType()));
         }
      }

      if (defenderTypes.isEmpty()) {
         return getEffectiveness(attackerType, defender);
      }

      for (ElementalType dt : defenderTypes) {
         if (dt != null) {
            e *= getEffectiveness(attackerType, dt, defender);
         }
      }

      if (e < 2.0 && defenderAbilityId.equals("wonderguard")) {
         e = 0.0;
      }

      return e;
   }

   public static double getTeraBlastEffectiveness(BattlePokemon attacker, BattlePokemon defender) {
      boolean isAttackerTera = BattleStates.get(attacker.actor.battle).getPokemonState(attacker).has(Custom.TERA);
      boolean isDefenderTera = BattleStates.get(defender.actor.battle).getPokemonState(defender).has(Custom.TERA);
      if (isAttackerTera) {
         ElementalType attackerType = PokeMathMax.teraToElementalType(attacker);
         if (attackerType != null) {
            return getEffectiveness(attackerType, defender, isDefenderTera);
         } else {
            return isDefenderTera ? 2.0 : 1.0;
         }
      } else {
         return getEffectiveness(ElementalTypes.NORMAL, defender, isDefenderTera);
      }
   }

   public static double getEffectiveness(ElementalType attackerType, ElementalType defenderType, BattlePokemon defender) {
      String defenderAbilityId = BattleStates.getTransformationOrEffected(defender).getAbility().getName();
      if (attackerType.equals(ElementalTypes.WATER)) {
         if (defenderAbilityId.equals("stormdrain") || defenderAbilityId.equals("waterabsorb") || defenderAbilityId.equals("dryskin")) {
            return 0.0;
         }
      } else if (attackerType.equals(ElementalTypes.ELECTRIC)) {
         if (defenderAbilityId.equals("voltabsorb") || defenderAbilityId.equals("lightningrod") || defenderAbilityId.equals("motordrive")) {
            return 0.0;
         }
      } else if (attackerType.equals(ElementalTypes.GROUND)) {
         if (defenderAbilityId.equals("eartheater")) {
            return 0.0;
         }

         if (State.raised(defender)) {
            return 0.0;
         }

         if (defenderType.equals(ElementalTypes.FLYING)) {
            return 1.0;
         }
      } else if (attackerType.equals(ElementalTypes.FIRE)) {
         if (defenderAbilityId.equals("wellbakedbody") || defenderAbilityId.equals("flashfire")) {
            return 0.0;
         }
      } else if (attackerType.equals(ElementalTypes.GRASS) && defenderAbilityId.equals("sapsipper")) {
         return 0.0;
      }

      return CHART.getOrDefault(defenderType, EMPTY_MAP).getOrDefault(attackerType, 1.0);
   }

   private static List<ElementalType> getEffectiveTypes(BattlePokemon p, boolean offensive) {
      Pokemon ep = BattleStates.getTransformationOrEffected(p);
      if (!offensive && BattleStates.get(p.actor.battle).getPokemonState(p).has(Custom.TERA)) {
         TeraType teraType = ep.getTeraType();
         if (teraType == null) {
            ModCommon.LOG.warn(String.format("Missing tera type for %s", ep.getDisplayName(false).getString()));
         } else {
            String teraId = teraType.showdownId();
            if (!"stellar".equals(teraId)) {
               ElementalType eleType = ElementalTypes.get(teraId);
               if (eleType != null) {
                  return List.of(eleType);
               }

               ModCommon.LOG.warn(String.format("Unknown tera type for %s: %s", ep.getDisplayName(false).getString(), teraId));
            }
         }
      }

      return ep.getSecondaryType() != null ? List.of(ep.getPrimaryType(), ep.getSecondaryType()) : List.of(ep.getPrimaryType());
   }

   public static Move getMove(InBattleMove move) {
      return MOVE_MAP.computeIfAbsent(move.id, key -> {
         MoveTemplate m = Moves.getByName(key);
         if (m == null) {
            if (!key.equals("recharge")) {
               ModCommon.LOG.warn("Failed to create move template for '" + key + "'");
            }

            m = Moves.getExceptional();
         }

         return m.create();
      });
   }

   public static InBattleMove getInBattleMove(Move move, BattlePokemon battlePokemon) {
      List<ActiveBattlePokemon> actorSlots = battlePokemon.getActor().getActivePokemon();
      int slot = -1;

      for (int i = 0; i < actorSlots.size(); i++) {
         if (actorSlots.get(i).getBattlePokemon() == battlePokemon) {
            slot = i;
            break;
         }
      }

      if (slot == -1) {
         return null;
      } else {
         ShowdownActionRequest request = battlePokemon.getActor().getRequest();
         if (request != null && request.getActive() != null && slot < request.getActive().size()) {
            ShowdownMoveset moveset = (ShowdownMoveset)request.getActive().get(slot);
            return moveset == null ? null : moveset.moves.stream().filter(ibm -> ibm.id.equals(move.getName())).findFirst().orElse(null);
         } else {
            return null;
         }
      }
   }

   public static ElementalType getHiddenPowerType(BattlePokemon pkmn) {
      IVs ivs = pkmn.getEffectedPokemon().getIvs();
      return HIDDENPOWER_TYPES[(
            (ivs.get(Stats.HP) & 1)
               + (ivs.get(Stats.ATTACK) & 1) * 2
               + (ivs.get(Stats.DEFENCE) & 1) * 4
               + (ivs.get(Stats.SPEED) & 1) * 8
               + (ivs.get(Stats.SPECIAL_ATTACK) & 1) * 16
               + (ivs.get(Stats.SPECIAL_DEFENCE) & 1) * 32
         )
         * 15
         / 63];
   }

   public static boolean is(BattlePokemon pkmn, int type) {
      Pokemon ep = BattleStates.getTransformationOrEffected(pkmn);
      int p = ep.getPrimaryType() != null ? TYPE_MASKS.getOrDefault(ep.getPrimaryType(), 0) : 0;
      int s = ep.getSecondaryType() != null ? TYPE_MASKS.getOrDefault(ep.getSecondaryType(), 0) : p;
      return (type & (p | s)) != 0;
   }

   static {
      ElementalType NORMAL = ElementalTypes.NORMAL;
      ElementalType FIGHTING = ElementalTypes.FIGHTING;
      ElementalType FLYING = ElementalTypes.FLYING;
      ElementalType POISON = ElementalTypes.POISON;
      ElementalType GROUND = ElementalTypes.GROUND;
      ElementalType ROCK = ElementalTypes.ROCK;
      ElementalType BUG = ElementalTypes.BUG;
      ElementalType GHOST = ElementalTypes.GHOST;
      ElementalType STEEL = ElementalTypes.STEEL;
      ElementalType FIRE = ElementalTypes.FIRE;
      ElementalType WATER = ElementalTypes.WATER;
      ElementalType GRASS = ElementalTypes.GRASS;
      ElementalType ELECTRIC = ElementalTypes.ELECTRIC;
      ElementalType PSYCHIC = ElementalTypes.PSYCHIC;
      ElementalType ICE = ElementalTypes.ICE;
      ElementalType DRAGON = ElementalTypes.DRAGON;
      ElementalType DARK = ElementalTypes.DARK;
      ElementalType FAIRY = ElementalTypes.FAIRY;
      CHART.put(NORMAL, Map.of(FIGHTING, 2.0, GHOST, 0.0));
      CHART.put(FIGHTING, Map.of(FLYING, 2.0, ROCK, 0.5, BUG, 0.5, PSYCHIC, 2.0, DARK, 0.5, FAIRY, 2.0));
      CHART.put(FLYING, Map.of(FIGHTING, 0.5, GROUND, 0.0, ROCK, 2.0, BUG, 0.5, GRASS, 0.5, ELECTRIC, 2.0, ICE, 2.0));
      CHART.put(POISON, Map.of(FIGHTING, 0.5, POISON, 0.5, GROUND, 2.0, BUG, 0.5, GRASS, 0.5, PSYCHIC, 2.0, FAIRY, 0.5));
      CHART.put(GROUND, Map.of(POISON, 0.5, ROCK, 0.5, WATER, 2.0, GRASS, 2.0, ELECTRIC, 0.0, ICE, 2.0));
      CHART.put(ROCK, Map.of(NORMAL, 0.5, FIGHTING, 2.0, FLYING, 0.5, POISON, 0.5, GROUND, 2.0, STEEL, 2.0, FIRE, 0.5, WATER, 2.0, GRASS, 2.0));
      CHART.put(BUG, Map.of(FIGHTING, 0.5, FLYING, 2.0, GROUND, 0.5, ROCK, 2.0, FIRE, 2.0, GRASS, 0.5));
      CHART.put(GHOST, Map.of(NORMAL, 0.0, FIGHTING, 0.0, POISON, 0.5, BUG, 0.5, GHOST, 2.0, DARK, 2.0));
      HashMap<ElementalType, Double> steelMap = new HashMap<>(
         Map.of(NORMAL, 0.5, FIGHTING, 2.0, FLYING, 0.5, POISON, 0.0, GROUND, 2.0, ROCK, 0.5, BUG, 0.5, STEEL, 0.5, FIRE, 2.0, GRASS, 0.5)
      );
      steelMap.put(PSYCHIC, 0.5);
      steelMap.put(ICE, 0.5);
      steelMap.put(DRAGON, 0.5);
      steelMap.put(FAIRY, 0.5);
      CHART.put(STEEL, steelMap);
      CHART.put(FIRE, Map.of(GROUND, 2.0, ROCK, 2.0, BUG, 0.5, STEEL, 0.5, FIRE, 0.5, WATER, 2.0, GRASS, 0.5, ICE, 0.5, FAIRY, 0.5));
      CHART.put(WATER, Map.of(STEEL, 0.5, FIRE, 0.5, WATER, 0.5, GRASS, 2.0, ELECTRIC, 2.0, ICE, 0.5));
      CHART.put(GRASS, Map.of(FLYING, 2.0, POISON, 2.0, GROUND, 0.5, BUG, 2.0, FIRE, 2.0, WATER, 0.5, GRASS, 0.5, ELECTRIC, 0.5, ICE, 2.0));
      CHART.put(ELECTRIC, Map.of(FLYING, 0.5, GROUND, 2.0, STEEL, 0.5, ELECTRIC, 0.5));
      CHART.put(PSYCHIC, Map.of(FIGHTING, 0.5, BUG, 2.0, GHOST, 2.0, PSYCHIC, 0.5, DARK, 2.0));
      CHART.put(ICE, Map.of(FIGHTING, 2.0, ROCK, 2.0, STEEL, 2.0, FIRE, 2.0, ICE, 0.5));
      CHART.put(DRAGON, Map.of(FIRE, 0.5, WATER, 0.5, GRASS, 0.5, ELECTRIC, 0.5, ICE, 2.0, DRAGON, 2.0, FAIRY, 2.0));
      CHART.put(DARK, Map.of(FIGHTING, 2.0, BUG, 2.0, GHOST, 0.5, PSYCHIC, 0.0, DARK, 0.5, FAIRY, 2.0));
      CHART.put(FAIRY, Map.of(FIGHTING, 0.5, POISON, 2.0, BUG, 0.5, STEEL, 2.0, DRAGON, 0.0, DARK, 0.5));
   }
}
