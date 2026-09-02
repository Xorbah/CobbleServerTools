package net.cobbleservertools;

import com.mojang.logging.LogUtils;
import net.cobbleservertools.config.PremierBallBonusConfig;
import net.cobbleservertools.config.ShinyNotificationConfig;
import net.cobbleservertools.event.CobbleServerToolsGameEvents;
import net.cobbleservertools.event.CobbleServerToolsModEvents;
import net.cobbleservertools.network.CobbleServerToolsNetworking;
import net.cobbleservertools.registry.KRegistries;
import net.cobbleservertools.util.FirstRunBootstrap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod("cobbleservertools")
public final class CobbleServerTools {
   public static final String MOD_ID = "cobbleservertools";
   public static final Logger LOGGER = LogUtils.getLogger();

   public CobbleServerTools(IEventBus var1, ModContainer var2) {
      FirstRunBootstrap.ensureDefaults();
      ShinyNotificationConfig.ensureLoaded();
      PremierBallBonusConfig.ensureLoaded();
      KRegistries.register(var1);
      var1.addListener(CobbleServerToolsNetworking::registerPayloads);
      var1.addListener(CobbleServerToolsModEvents::registerEntityAttributes);
      NeoForge.EVENT_BUS.register(new CobbleServerToolsGameEvents());
      LOGGER.info("CobbleServerTools initialized; config directory={}", FMLPaths.CONFIGDIR.get().resolve(MOD_ID));
   }
}
