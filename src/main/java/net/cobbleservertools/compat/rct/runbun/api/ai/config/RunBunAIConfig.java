package net.cobbleservertools.compat.rct.runbun.api.ai.config;

import com.gitlab.srcmc.rctapi.ModCommon;
import com.gitlab.srcmc.rctapi.api.util.JTO;
import net.cobbleservertools.compat.rct.runbun.api.ai.RunBunAI;

public record RunBunAIConfig(Boolean canTera, String teraTarget) {
   private static final boolean DEFAULT_CAN_TERA = false;
   private static final String DEFAULT_TERA_TARGET = "";

   public RunBunAIConfig() {
      this(false, "");
   }

   public RunBunAIConfig {
      if (canTera == null) {
         ModCommon.LOG.info("[CONFIG] canTera={}", canTera);
         canTera = false;
      }

      if (teraTarget == null) {
         teraTarget = "";
      }
   }

   public static void register() {
      JTO.registerParser("rb", RunBunAI::new, RunBunAIConfig::new, RunBunAIConfig.class);
   }
}
