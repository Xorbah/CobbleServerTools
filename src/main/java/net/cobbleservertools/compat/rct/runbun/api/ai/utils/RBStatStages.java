package net.cobbleservertools.compat.rct.runbun.api.ai.utils;

import com.cobblemon.mod.common.api.battles.interpreter.BattleContext;
import com.cobblemon.mod.common.api.battles.interpreter.BattleContext.Type;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleStates;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleEffects.Custom;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.cobbleservertools.compat.rct.runbun.ModCommon;

public class RBStatStages {
   private Map<BattlePokemon, Map<Stats, Integer>> statCache = new HashMap<>();
   public static final Map<String, Stats> conversionStatTable = Map.of(
      "def",
      Stats.DEFENCE,
      "spa",
      Stats.SPECIAL_ATTACK,
      "spd",
      Stats.SPECIAL_DEFENCE,
      "spe",
      Stats.SPEED,
      "atk",
      Stats.ATTACK,
      "eva",
      Stats.EVASION,
      "acc",
      Stats.ACCURACY
   );

   public void statStageContext(BattlePokemon pkmn) {
      this.statCache.put(pkmn, this.initStatMap());
      Map<Stats, Integer> stages = this.statCache.get(pkmn);
      this.applyContexts(pkmn, Type.BOOST, 1, stages);
      this.applyContexts(pkmn, Type.UNBOOST, -1, stages);
      this.statCache.put(pkmn, stages);
   }

   private void applyContexts(BattlePokemon pkmn, Type type, int delta, Map<Stats, Integer> stages) {
      Collection<BattleContext> ctx = pkmn.getContextManager().get(type);
      if (ctx != null) {
         for (BattleContext bc : ctx) {
            if (bc.getId() != null) {
               Stats stat = conversionStatTable.get(bc.getId().toLowerCase());
               if (stat != null) {
                  int newVal = Math.max(-6, Math.min(6, stages.getOrDefault(stat, 0) + delta));
                  stages.put(stat, newVal);
               }
            }
         }
      }
   }

   private Map<Stats, Integer> initStatMap() {
      Map<Stats, Integer> stages = new HashMap<>();

      for (Stats stat : List.of(Stats.ATTACK, Stats.DEFENCE, Stats.SPECIAL_ATTACK, Stats.SPECIAL_DEFENCE, Stats.SPEED, Stats.ACCURACY, Stats.EVASION)) {
         stages.put(stat, 0);
      }

      return stages;
   }

   public Map<Stats, Integer> getStatMap(BattlePokemon battlePokemon) {
      this.statCache.putIfAbsent(battlePokemon, this.initStatMap());
      return this.statCache.getOrDefault(battlePokemon, this.initStatMap());
   }

   public void logStatMap(BattlePokemon battlePokemon) {
      Map<Stats, Integer> map = this.initStatMap();
      if (battlePokemon != null) {
         map = this.getStatMap(battlePokemon);

         for (Stats stat : map.keySet()) {
            ModCommon.LOG.info(stat.getShowdownId() + " IS AT STAGE " + map.getOrDefault(stat, 0));
         }
      }
   }

   public int battleTurn(BattlePokemon battlePokemon) {
      return BattleStates.get(battlePokemon.getActor().getBattle()).getPokemonState(battlePokemon).age(Custom.TURN);
   }
}
