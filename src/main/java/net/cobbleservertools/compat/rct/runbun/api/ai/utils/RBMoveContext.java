package net.cobbleservertools.compat.rct.runbun.api.ai.utils;

import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.cobbleservertools.compat.rct.runbun.api.ai.RunBunAI;

public class RBMoveContext {
   RunBunAI.MoveEvaluation evaluation;
   BattlePokemon battlePokemon;
   List<RunBunAI.MoveEvaluation> killingMoves;
   int maxDamage;
   RBSlotInformation selfInfo;
   RBSlotInformation partnerInfo;
   Map<Stats, Integer> aiStages = new HashMap<>();
   Map<Stats, Integer> oppStages = new HashMap<>();
   RBBattleSlots battleSlots;
   boolean isFaster;
   boolean npcIsOHKO = false;
   boolean npcIs2OHKO = false;
   boolean npcIs3OHKO = false;
   boolean npcIsOHKOWithSS = false;
   boolean npcIsOHKOWithBD = false;
   boolean oppHasSpecialMove = false;
   boolean oppHasPhysicalMove = false;
   boolean isDoubles;

   public RBMoveContext(
      RunBunAI.MoveEvaluation evaluation,
      BattlePokemon battlePokemon,
      List<RunBunAI.MoveEvaluation> killingMoves,
      int maxDamage,
      RBSlotInformation selfInfo,
      RBSlotInformation partnerInfo,
      Map<Stats, Integer> aiStages,
      Map<Stats, Integer> oppStages,
      RBBattleSlots battleSlots,
      boolean isFaster,
      boolean npcIsOHKO,
      boolean npcIs2OHKO,
      boolean npcIs3OHKO,
      boolean npcIsOHKOWithSS,
      boolean npcIsOHKOWithBD,
      boolean oppHasSpecialMove,
      boolean oppHasPhysicalMove,
      boolean isDoubles
   ) {
      this.evaluation = evaluation;
      this.killingMoves = killingMoves;
      this.maxDamage = maxDamage;
      this.battlePokemon = battlePokemon;
      this.aiStages = aiStages;
      this.oppStages = oppStages;
      this.selfInfo = selfInfo;
      this.partnerInfo = partnerInfo;
      this.battleSlots = battleSlots;
      this.isFaster = isFaster;
      this.npcIs2OHKO = npcIs2OHKO;
      this.npcIsOHKO = npcIsOHKO;
      this.npcIs3OHKO = npcIs3OHKO;
      this.npcIsOHKOWithSS = npcIsOHKOWithSS;
      this.npcIsOHKOWithBD = npcIsOHKOWithBD;
      this.oppHasPhysicalMove = oppHasPhysicalMove;
      this.oppHasSpecialMove = oppHasSpecialMove;
      this.isDoubles = isDoubles;
   }
}
