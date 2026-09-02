package net.cobbleservertools.compat.rct.runbun.api.ai.utils;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import java.util.ArrayList;
import java.util.List;

public class RBBattleSlots {
   public RBSlotInformation[] slots = null;

   public RBBattleSlots() {
   }

   public RBBattleSlots(ActiveBattlePokemon activeBattlePokemon) {
      if (this.slots == null) {
         int size = 0;

         for (ActiveBattlePokemon abp : activeBattlePokemon.getAllActivePokemon()) {
            size++;
         }

         this.slots = new RBSlotInformation[size];

         for (ActiveBattlePokemon abp : activeBattlePokemon.getAllActivePokemon()) {
            this.slots[getSlot(abp, abp.getBattle())] = new RBSlotInformation(abp);
         }
      }
   }

   public RBSlotInformation getSlot(int idx) {
      return this.slots[idx];
   }

   public RBSlotInformation getBPInfo(BattlePokemon bp) {
      if (bp == null) {
         return null;
      }

      for (RBSlotInformation i : this.slots) {
         if (i != null && i.getBattlePokemon() == bp) {
            return i;
         }
      }

      return null;
   }

   public static int getSlot(ActiveBattlePokemon target, PokemonBattle battle) {
      int index = 0;

      for (ActiveBattlePokemon abp : battle.getActivePokemon()) {
         if (abp != null) {
            if (abp.equals(target)) {
               return index;
            }

            index++;
         }
      }

      return -1;
   }

   public static List<ActiveBattlePokemon> getOpponents(ActiveBattlePokemon source) {
      List<ActiveBattlePokemon> opponents = new ArrayList<>();

      for (ActiveBattlePokemon abp : source.getBattle().getActivePokemon()) {
         if (!abp.equals(source) && !source.isAllied(abp)) {
            opponents.add(abp);
         }
      }

      return opponents;
   }

   public static List<ActiveBattlePokemon> getAllies(ActiveBattlePokemon source) {
      List<ActiveBattlePokemon> allies = new ArrayList<>();

      for (ActiveBattlePokemon abp : source.getBattle().getActivePokemon()) {
         if (!abp.equals(source) && source.isAllied(abp)) {
            allies.add(abp);
         }
      }

      return allies;
   }

   public static List<ActiveBattlePokemon> getAllActivePokemon(ActiveBattlePokemon source) {
      List<ActiveBattlePokemon> all = new ArrayList<>();

      for (ActiveBattlePokemon abp : source.getBattle().getActivePokemon()) {
         if (abp != null) {
            all.add(abp);
         }
      }

      return all;
   }

   public List<RBSlotInformation> getAllySlotInfos(ActiveBattlePokemon source) {
      List<RBSlotInformation> result = new ArrayList<>();

      for (RBSlotInformation slot : this.slots) {
         if (slot != null) {
            ActiveBattlePokemon abp = slot.getActiveBattlePokemon();
            if (abp != null && (abp.equals(source) || source.isAllied(abp))) {
               result.add(slot);
            }
         }
      }

      return result;
   }

   public List<RBSlotInformation> getOpponentSlotInfos(ActiveBattlePokemon source) {
      List<RBSlotInformation> result = new ArrayList<>();

      for (RBSlotInformation slot : this.slots) {
         if (slot != null) {
            ActiveBattlePokemon abp = slot.getActiveBattlePokemon();
            if (abp != null && !abp.equals(source) && !source.isAllied(abp)) {
               result.add(slot);
            }
         }
      }

      return result;
   }
}
