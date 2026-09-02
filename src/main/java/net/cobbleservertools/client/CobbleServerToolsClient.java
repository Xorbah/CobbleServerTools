package net.cobbleservertools.client;

import net.cobbleservertools.CobbleServerTools;
import net.cobbleservertools.client.network.CobbleServerToolsClientPayloads;
import net.cobbleservertools.client.render.CobbleNpcRenderer;
import net.cobbleservertools.registry.KRegistries;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers;

@EventBusSubscriber(modid = "cobbleservertools", bus = Bus.MOD, value = Dist.CLIENT)
public final class CobbleServerToolsClient {
   private CobbleServerToolsClient() {
   }

   @SubscribeEvent
   public static void onClientSetup(FMLClientSetupEvent var0) {
      CobbleServerToolsClientPayloads.install();
      CobbleServerTools.LOGGER.info("CobbleServerTools phase-5 release-candidate client bootstrap attached", new Object[0]);
   }

   @SubscribeEvent
   public static void registerRenderers(RegisterRenderers var0) {
      var0.registerEntityRenderer((EntityType)KRegistries.BATTLE_NPC.get(), CobbleNpcRenderer::new);
      var0.registerEntityRenderer((EntityType)KRegistries.RIVAL_NPC.get(), CobbleNpcRenderer::new);
      var0.registerEntityRenderer((EntityType)KRegistries.DIALOG_NPC.get(), CobbleNpcRenderer::new);
      var0.registerEntityRenderer((EntityType)KRegistries.TRADER_NPC.get(), CobbleNpcRenderer::new);
      var0.registerEntityRenderer((EntityType)KRegistries.MART_NPC.get(), CobbleNpcRenderer::new);
      var0.registerEntityRenderer((EntityType)KRegistries.MOVE_TUTOR_NPC.get(), CobbleNpcRenderer::new);
   }
}
