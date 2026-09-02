package net.cobbleservertools.compat.rct.runbun.api.ai.utils;

import com.cobblemon.mod.common.api.abilities.Ability;
import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import net.cobbleservertools.compat.rct.runbun.ModCommon;
import net.cobbleservertools.compat.rct.runbun.api.ai.RunBunAI;

public class RBSlotInformation {
   private String id = "";
   private RunBunAI.MoveEvaluation chosenMove;
   private BattlePokemon battlePokemon = null;
   private ActiveBattlePokemon activeBattlePokemon = null;
   private Ability ability = null;
   private String heldItem = "";
   private int turnsForActivePokemon = 1;
   private String moveUsedLastTurn = "";
   private boolean hasUsedMega = false;
   private boolean hasUsedTera = false;
   private Map<Integer, String> moveHistory = new HashMap<>();
   private boolean grounded = false;
   private int perishSongTurns = 0;
   private int encoreTurns = 0;
   private int substituteHP = 0;
   private String lastMoveUsed = null;
   private RBStatStages stages;

   public RBSlotInformation(String id) {
      this.id = id;
   }

   public RBSlotInformation(ActiveBattlePokemon abp) {
      this.battlePokemon = abp.getBattlePokemon();
      this.activeBattlePokemon = abp;
   }

   public String getID() {
      return this.id;
   }

   public void setID(String id) {
      this.id = id;
   }

   public BattlePokemon getBattlePokemon() {
      return this.battlePokemon;
   }

   public void setBattlePokemon(BattlePokemon battlePokemon) {
      this.battlePokemon = battlePokemon;
   }

   public ActiveBattlePokemon getActiveBattlePokemon() {
      return this.activeBattlePokemon;
   }

   public void setActiveBattlePokemon(ActiveBattlePokemon activeBattlePokemon) {
      this.activeBattlePokemon = activeBattlePokemon;
   }

   public boolean isGrounded() {
      return this.grounded;
   }

   public void setGrounded(boolean grounded) {
      this.grounded = grounded;
   }

   public void turnChange() {
      if (this.perishSongTurns > 0) {
         this.perishSongTurns--;
      }

      if (this.encoreTurns > 0) {
         this.encoreTurns--;
      }

      this.turnsForActivePokemon++;
   }

   public RunBunAI.MoveEvaluation getChosenMove() {
      return this.chosenMove;
   }

   public void setChosenMove(RunBunAI.MoveEvaluation move) {
      this.chosenMove = move;
   }

   public int getTurnsForActivePokemon() {
      return this.turnsForActivePokemon;
   }

   public String getMoveUsedLastTurn() {
      return this.moveUsedLastTurn;
   }

   public void setMoveUsedLastTurn(String v) {
      this.moveUsedLastTurn = v;
   }

   public boolean hasUsedMega() {
      return this.hasUsedMega;
   }

   public void setHasUsedMega(boolean hasUsedMega) {
      this.hasUsedMega = hasUsedMega;
   }

   public boolean hasUsedTera() {
      return this.hasUsedTera;
   }

   public void setHasUsedTera(boolean hasUsedTera) {
      this.hasUsedTera = hasUsedTera;
   }

   public Map<Integer, String> getMoveHistory() {
      return this.moveHistory;
   }

   public void setMoveHistory(Map<Integer, String> moveHistory) {
      this.moveHistory = moveHistory;
   }

   public void logMoveHistory(BattlePokemon battlePokemon, boolean countFail) {
      this.moveHistory.forEach((turn, move) -> ModCommon.LOG.info("Move history - turn {}: {}", turn, move));
   }

   public void addToMoveHistory(String moveName) {
      this.moveHistory.put(this.turnsForActivePokemon, moveName);
      this.turnsForActivePokemon++;
   }

   public Ability getAbility() {
      return this.ability;
   }

   public String getHeldItem() {
      return this.heldItem;
   }

   public static String getMoveHistoryName(BattlePokemon battlePokemon, boolean countFail) {
      for (Entry<UUID, BattleMessage> entry : battlePokemon.getActor().getBattle().getMajorBattleActions().entrySet()) {
         BattleMessage msg = entry.getValue();
         String type = msg.getId();
         BattlePokemon mon = msg.battlePokemon(0, battlePokemon.getActor().getBattle());
         if ("move".equals(type)) {
            if (mon != null && mon.equals(battlePokemon)) {
               msg.moveAt(1);
               return msg.moveAt(1).getName();
            }
         } else if (countFail
            && ("miss".equals(type) || "immune".equals(type) || "fail".equals(type))
            && mon != null
            && mon.getUuid().equals(battlePokemon.getUuid())) {
            msg.moveAt(1);
            return msg.moveAt(1).getName();
         }
      }

      return "";
   }

   public static Map<Integer, MoveTemplate> getMovesUsedAgainstPokemon(BattlePokemon battlePokemon, boolean countFail) {
      Map<Integer, MoveTemplate> moveHistory = new HashMap<>();
      int turn = 1;
      PokemonBattle battle = battlePokemon.getActor().getBattle();
      MoveTemplate lastMove = null;

      for (BattleMessage msg : battle.getMajorBattleActions().values()) {
         String type = msg.getId();
         if ("move".equals(type)) {
            lastMove = msg.moveAt(1);
         } else if (lastMove != null && ("-damage".equals(type) || "-immune".equals(type) || countFail && ("-miss".equals(type) || "-fail".equals(type)))) {
            BattlePokemon target = msg.battlePokemon(0, battle);
            if (target != null && target.getUuid().equals(battlePokemon.getUuid())) {
               moveHistory.put(turn, lastMove);
               turn++;
            }
         }
      }

      return moveHistory;
   }

   public static Map<Integer, MoveTemplate> getMoveHistory(BattlePokemon battlePokemon, boolean countFail) {
      Map<Integer, MoveTemplate> moveHistory = new HashMap<>();
      int turn = 1;
      PokemonBattle battle = battlePokemon.getActor().getBattle();

      for (BattleMessage msg : battle.getMajorBattleActions().values()) {
         String type = msg.getId();
         BattlePokemon mon = msg.battlePokemon(0, battle);
         if (mon != null && mon.getUuid().equals(battlePokemon.getUuid())) {
            if ("move".equals(type)) {
               MoveTemplate move = msg.moveAt(1);
               if (move != null) {
                  moveHistory.put(turn, move);
                  turn++;
               }
            } else if (countFail && ("-miss".equals(type) || "-immune".equals(type) || "-fail".equals(type))) {
               MoveTemplate move = msg.moveAt(1);
               if (move != null) {
                  moveHistory.put(turn, move);
                  turn++;
               }
            }
         }
      }

      return moveHistory;
   }
}
