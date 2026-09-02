package net.cobbleservertools.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.neoforged.fml.loading.FMLPaths;

public final class ShinyNotificationConfig {
   private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("cobbleservertools").resolve("shiny_notification.json");
   private static final Pattern ENABLED = Pattern.compile("\\\"enabled\\\"\\s*:\\s*(true|false)", 2);
   private static boolean enabled = true;
   private static boolean loaded;

   private ShinyNotificationConfig() {
   }

   public static synchronized void ensureLoaded() {
      if (!loaded) {
         loaded = true;
         loadOrCreate();
      }
   }

   public static synchronized boolean isEnabled() {
      ensureLoaded();
      return enabled;
   }

   public static synchronized void setEnabled(boolean var0) throws IOException {
      ensureLoaded();
      enabled = var0;
      save();
   }

   private static void loadOrCreate() {
      try {
         if (!Files.exists(CONFIG_PATH)) {
            Files.createDirectories(CONFIG_PATH.getParent());
            save();
            return;
         }

         Matcher var0 = ENABLED.matcher(Files.readString(CONFIG_PATH));
         if (var0.find()) {
            enabled = Boolean.parseBoolean(var0.group(1));
         }
      } catch (Exception var3) {
         enabled = true;

         try {
            Files.createDirectories(CONFIG_PATH.getParent());
            save();
         } catch (IOException var2) {
         }
      }
   }

   private static void save() throws IOException {
      Files.createDirectories(CONFIG_PATH.getParent());
      Files.writeString(CONFIG_PATH, "{\n  \"enabled\": " + enabled + "\n}\n");
   }
}
