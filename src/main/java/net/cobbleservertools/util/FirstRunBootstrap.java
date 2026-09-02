package net.cobbleservertools.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.cobbleservertools.CobbleServerTools;
import net.neoforged.fml.loading.FMLPaths;

public final class FirstRunBootstrap {
   private static final Pattern JSON_STRING = Pattern.compile("\\\"((?:\\\\.|[^\\\"\\\\])*)\\\"");
   private static volatile boolean didRun;

   private FirstRunBootstrap() {
   }

   public static synchronized void ensureDefaults() {
      if (!didRun) {
         didRun = true;
         Path var0 = FMLPaths.CONFIGDIR.get().resolve("cobbleservertools");
         Path var1 = var0.resolve("dialogs");
         Path var2 = var0.resolve("choices");
         Path var3 = var0.resolve("descriptions");
         Path var4 = var0.resolve("skins");
         Path var5 = var0.resolve("marts");
         Path var6 = var0.resolve("templates");
         Path var7 = var0.resolve("presets");

         try {
            Files.createDirectories(var1);
            Files.createDirectories(var2);
            Files.createDirectories(var3);
            Files.createDirectories(var4);
            Files.createDirectories(var5);
            Files.createDirectories(var6);
            Files.createDirectories(var7);
            Files.createDirectories(var7.resolve("battle"));
            Files.createDirectories(var7.resolve("rival"));
            Files.createDirectories(var7.resolve("dialog"));
            Files.createDirectories(var7.resolve("trader"));
            Files.createDirectories(var7.resolve("mart"));
            Files.createDirectories(var7.resolve("move_tutor"));
            Files.createDirectories(var7.resolve("vending"));
            copyIfMissing("assets/cobbleservertools/dialogs/en_us_arrays.json", var1.resolve("en_us_arrays.json"));
            copyIfMissing("assets/cobbleservertools/dialogs/pt_br_arrays.json", var1.resolve("pt_br_arrays.json"));
            copyIfMissing("assets/cobbleservertools/descriptions/martnpc_desc_en_us.json", var3.resolve("martnpc_desc_en_us.json"));
            copyIfMissing("assets/cobbleservertools/descriptions/martnpc_desc_pt_br.json", var3.resolve("martnpc_desc_pt_br.json"));
            copyIfMissing("assets/cobbleservertools/marts/viridian_mart.json", var5.resolve("viridian_mart.json"));
            copyIfMissing("assets/cobbleservertools/templates/battle_npc.json", var6.resolve("battle_npc.json"));
            copyIfMissing("assets/cobbleservertools/templates/rival_npc.json", var6.resolve("rival_npc.json"));
            copyIfMissing("assets/cobbleservertools/templates/dialog_npc.json", var6.resolve("dialog_npc.json"));
            copyIfMissing("assets/cobbleservertools/templates/trader_npc.json", var6.resolve("trader_npc.json"));
            copyIfMissing("assets/cobbleservertools/templates/mart_npc.json", var6.resolve("mart_npc.json"));
            copyIfMissing("assets/cobbleservertools/templates/move_tutor_npc.json", var6.resolve("move_tutor_npc.json"));
            copyIfMissing("assets/cobbleservertools/templates/vending_machine.json", var6.resolve("vending_machine.json"));
            copyIfMissing("assets/cobbleservertools/templates/README.txt", var6.resolve("README.txt"));
            copyIfMissing("assets/cobbleservertools/presets/README.txt", var7.resolve("README.txt"));
            copyIfMissing("assets/cobbleservertools/presets/mart/viridian_mart.json", var7.resolve("mart").resolve("viridian_mart.json"));
            int var8 = 0;

            for (String var10 : readSkinIndex()) {
               if (!var10.isBlank()
                  && !var10.contains("..")
                  && !var10.contains("/")
                  && !var10.contains("\\")
                  && copyIfMissing("assets/cobbleservertools/bootstrap/" + var10, var4.resolve(var10))) {
                  var8++;
               }
            }

            CobbleServerTools.LOGGER.info("CobbleServerTools configuration bootstrap ready at {}; copied {} missing bootstrap skins", new Object[]{var0, var8});
         } catch (Exception var11) {
            CobbleServerTools.LOGGER.warn("CobbleServerTools first-run configuration bootstrap failed: {}", new Object[]{var11.toString()});
         }
      }
   }

   private static boolean copyIfMissing(String var0, Path var1) throws IOException {
      if (Files.exists(var1)) {
         return false;
      }

      Path var2 = var1.getParent();
      if (var2 != null) {
         Files.createDirectories(var2);
      }

      ClassLoader var3 = FirstRunBootstrap.class.getClassLoader();

      try (InputStream var4 = var3.getResourceAsStream(var0)) {
         if (var4 == null) {
            CobbleServerTools.LOGGER.warn("CobbleServerTools bootstrap resource is missing: {}", new Object[]{var0});
            return false;
         } else {
            Files.copy(var4, var1);
            return true;
         }
      }
   }

   private static List<String> readSkinIndex() throws IOException {
      String var0 = "assets/cobbleservertools/bootstrap/skins_index.json";
      ClassLoader var1 = FirstRunBootstrap.class.getClassLoader();

      try (InputStream var2 = var1.getResourceAsStream(var0)) {
         if (var2 == null) {
            CobbleServerTools.LOGGER.warn("CobbleServerTools bootstrap skin index is missing: {}", new Object[]{var0});
            return List.of();
         }

         String var3 = new String(var2.readAllBytes(), StandardCharsets.UTF_8);
         Matcher var4 = JSON_STRING.matcher(var3);
         ArrayList var5 = new ArrayList();

         while (var4.find()) {
            var5.add(unescapeJsonString(var4.group(1)));
         }

         return List.copyOf(var5);
      }
   }

   private static String unescapeJsonString(String var0) {
      return var0.replace("\\\"", "\"").replace("\\\\", "\\");
   }
}
