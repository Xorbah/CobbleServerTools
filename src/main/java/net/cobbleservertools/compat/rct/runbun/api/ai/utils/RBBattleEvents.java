package net.cobbleservertools.compat.rct.runbun.api.ai.utils;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import kotlin.Unit;
import net.cobbleservertools.compat.rct.runbun.ModCommon;
import net.cobbleservertools.compat.rct.runbun.api.ai.RunBunAI;

public class RBBattleEvents {
   public static void register() {
      CobblemonEvents.BATTLE_STARTED_PRE.subscribe(Priority.NORMAL, event -> {
         System.out.println("Battle is about to start! Players: " + event.getBattle().getPlayers());
         RunBunAI.setHasResetDefault(false);
         ModCommon.LOG.info("WE HAVE STARTED A NEW BATTLE AND RESET TO DEFAULT");
         return Unit.INSTANCE;
      });
   }
}
