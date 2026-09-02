package net.cobbleservertools.event;

import net.cobbleservertools.CobbleServerTools;
import net.cobbleservertools.battle.NpcBattleService;
import net.cobbleservertools.battle.PlayerMoneyService;
import net.cobbleservertools.battle.protection.NpcDialogBattleProtection;
import net.cobbleservertools.battle.protection.NpcPlayerFreezeService;
import net.cobbleservertools.commerce.CommerceSessions;
import net.cobbleservertools.dialog.NpcDialogCatalog;
import net.cobbleservertools.dialog.NpcDialogSessions;
import net.cobbleservertools.entity.AbstractCobbleNpcEntity;
import net.cobbleservertools.network.CobbleServerToolsNetworking;
import net.cobbleservertools.rival.RivalNpcTeamsStorage;
import net.cobbleservertools.rival.storage.RivalStarterStorage;
import net.cobbleservertools.rival.util.RivalStarterService;
import net.cobbleservertools.roaming.RoamingPokemonManager;
import net.cobbleservertools.util.ItemPriceLoader;
import net.cobbleservertools.world.FloatingInWaterManager;
import net.cobbleservertools.world.SpawnFacingManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.StartTracking;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent.Post;

public final class CobbleServerToolsGameEvents {
   @SubscribeEvent
   public void onAddReloadListeners(AddReloadListenerEvent var1) {
      var1.addListener(new ItemPriceLoader());
   }

   @SubscribeEvent
   public void onServerStarting(ServerStartingEvent var1) {
      NpcDialogCatalog.reload();
      CobbleServerTools.LOGGER.info("CobbleServerTools server lifecycle started with phase-5 release-candidate battle, dialogue, and commerce services", new Object[0]);
   }

   @SubscribeEvent
   public void onServerStopped(ServerStoppedEvent var1) {
      NpcDialogSessions.clear();
      CommerceSessions.clear();
      RivalNpcTeamsStorage.flushAndRelease(var1.getServer());
      RivalStarterStorage.flushAndRelease(var1.getServer());
      NpcDialogBattleProtection.clearAll(var1.getServer());
      NpcPlayerFreezeService.clearAll();
      FloatingInWaterManager.clear();
      CobbleServerTools.LOGGER.info("CobbleServerTools server lifecycle stopped", new Object[0]);
   }

   @SubscribeEvent
   public void onPlayerLoggedIn(PlayerLoggedInEvent var1) {
      if (var1.getEntity() instanceof ServerPlayer var2) {
         RivalStarterService.detectAndStore(var2);
         CobbleServerToolsNetworking.syncMoney(var2, PlayerMoneyService.get(var2));
      }
   }

   @SubscribeEvent
   public void onPlayerLoggedOut(PlayerLoggedOutEvent var1) {
      if (var1.getEntity() instanceof ServerPlayer var2) {
         NpcDialogSessions.disconnect(var2);
         CommerceSessions.disconnect(var2);
         NpcBattleService.onPlayerDisconnect(var2);
         NpcDialogBattleProtection.clear(var2);
         NpcPlayerFreezeService.unfreeze(var2);
      }
   }

   @SubscribeEvent
   public void onPlayerClone(Clone var1) {
      if (var1.getOriginal() instanceof ServerPlayer var2 && var1.getEntity() instanceof ServerPlayer var3) {
         PlayerMoneyService.copy(var2, var3);
      }
   }

   @SubscribeEvent
   public void onServerTick(Post var1) {
      RoamingPokemonManager.tickFromEvent(var1);
      NpcDialogBattleProtection.tick(var1.getServer());
      NpcPlayerFreezeService.tick(var1.getServer());
      FloatingInWaterManager.tick(var1.getServer());
      SpawnFacingManager.tick(var1.getServer());
   }

   @SubscribeEvent
   public void onStartTracking(StartTracking var1) {
      if (var1.getEntity() instanceof ServerPlayer var2 && var1.getTarget() instanceof AbstractCobbleNpcEntity var3) {
         CobbleServerToolsNetworking.sendSnapshot(var2, var3, false);
      }
   }
}
