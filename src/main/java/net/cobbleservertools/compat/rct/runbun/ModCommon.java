package net.cobbleservertools.compat.rct.runbun;

import net.cobbleservertools.compat.rct.runbun.api.ai.config.RunBunAIConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModCommon {
   public static final String MOD_ID = "rbrctai";
   public static final Logger LOG = LoggerFactory.getLogger("rbrctai");

   public static void init() {
      RunBunAIConfig.register();
   }
}
