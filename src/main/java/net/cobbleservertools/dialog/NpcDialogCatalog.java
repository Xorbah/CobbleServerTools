package net.cobbleservertools.dialog;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import net.cobbleservertools.CobbleServerTools;
import net.neoforged.fml.loading.FMLPaths;

public final class NpcDialogCatalog {
   private static final int MAX_LINES = 128;
   private static final int MAX_LINE_LENGTH = 512;
   private static final Map<String, Map<String, NpcDialog>> BY_LOCALE = new HashMap<>();

   private NpcDialogCatalog() {
   }

   public static synchronized void reload() {
      BY_LOCALE.clear();
      loadLocale("en_us");
      loadLocale("pt_br");
      CobbleServerTools.LOGGER.info("Loaded {} CobbleServerTools dialogue locales", new Object[]{BY_LOCALE.size()});
   }

   public static synchronized Optional<NpcDialog> find(String var0, String var1) {
      String var2 = normalizeLocale(var0);
      String var3 = var1 == null ? "" : var1.trim();
      if (var3.isEmpty()) {
         return Optional.empty();
      }

      Map var4 = BY_LOCALE.computeIfAbsent(var2, NpcDialogCatalog::readLocale);
      NpcDialog var5 = (NpcDialog)var4.get(var3);
      if (var5 == null && !"en_us".equals(var2)) {
         var5 = BY_LOCALE.computeIfAbsent("en_us", NpcDialogCatalog::readLocale).get(var3);
      }

      return Optional.ofNullable(var5);
   }

   public static synchronized int localeSize(String var0) {
      return BY_LOCALE.computeIfAbsent(normalizeLocale(var0), NpcDialogCatalog::readLocale).size();
   }

   private static void loadLocale(String var0) {
      Map var1 = readLocale(var0);
      if (!var1.isEmpty()) {
         BY_LOCALE.put(var0, var1);
         CobbleServerTools.LOGGER.info("Loaded {} dialogues for {}", new Object[]{var1.size(), var0});
      }
   }

   private static Map<String, NpcDialog> readLocale(String var0) {
      Path var1 = FMLPaths.CONFIGDIR.get().resolve("cobbleservertools").resolve("dialogs").resolve(var0 + "_arrays.json");

      try {
         if (Files.isRegularFile(var1)) {
            try (BufferedReader var15 = Files.newBufferedReader(var1, StandardCharsets.UTF_8)) {
               return parse(var15, var1.toString());
            }
         }
      } catch (IOException | RuntimeException var14) {
         CobbleServerTools.LOGGER.error("Failed to load dialogue override {}", new Object[]{var1, var14});
      }

      String var2 = "/assets/cobbleservertools/dialogs/" + var0 + "_arrays.json";

      try (InputStream var3 = NpcDialogCatalog.class.getResourceAsStream(var2)) {
         if (var3 == null) {
            CobbleServerTools.LOGGER.warn("Bundled dialogue resource {} is missing", new Object[]{var2});
            return Map.of();
         }

         try (InputStreamReader var4 = new InputStreamReader(var3, StandardCharsets.UTF_8)) {
            return parse(var4, var2);
         }
      } catch (IOException | RuntimeException var12) {
         CobbleServerTools.LOGGER.error("Failed to load bundled dialogues {}", new Object[]{var2, var12});
         return Map.of();
      }
   }

   private static Map<String, NpcDialog> parse(Reader var0, String var1) {
      JsonElement var2 = JsonParser.parseReader(var0);
      if (!var2.isJsonArray()) {
         throw new IllegalArgumentException("Dialogue root must be an array: " + var1);
      }

      HashMap var3 = new HashMap();

      for (JsonElement var5 : var2.getAsJsonArray()) {
         if (var5.isJsonObject()) {
            JsonObject var6 = var5.getAsJsonObject();
            JsonObject var7 = var6.has("answers") && var6.get("answers").isJsonObject() ? var6.getAsJsonObject("answers") : null;

            for (Entry var9 : var6.entrySet()) {
               if (!"answers".equals(var9.getKey())) {
                  NpcDialog var10 = parseDialog((JsonElement)var9.getValue(), var7);
                  if (!var10.isEmpty()) {
                     var3.put((String)var9.getKey(), var10);
                  }
               }
            }
         }
      }

      return Map.copyOf(var3);
   }

   private static NpcDialog parseDialog(JsonElement var0, JsonObject var1) {
      List var2 = List.of();
      List var3 = List.of();
      List var4 = List.of();
      List var5 = List.of();
      if (var0.isJsonArray()) {
         var2 = lines(var0.getAsJsonArray());
      } else if (var0.isJsonObject()) {
         JsonObject var6 = var0.getAsJsonObject();
         var2 = firstLines(var6, "start", "main", "lines", "dialog");
         var3 = firstLines(var6, "end", "yes");
         var4 = firstLines(var6, "post", "no");
         var5 = firstLines(var6, "cancel");
      }

      if (var1 != null) {
         List var9 = answerLines(var1, "yes");
         List var7 = answerLines(var1, "no");
         List var8 = answerLines(var1, "cancel");
         if (!var9.isEmpty()) {
            var3 = var9;
         }

         if (!var7.isEmpty()) {
            var4 = var7;
         }

         if (!var8.isEmpty()) {
            var5 = var8;
         }
      }

      return new NpcDialog(var2, var3, var4, var5);
   }

   private static List<String> answerLines(JsonObject var0, String var1) {
      if (!var0.has(var1)) {
         return List.of();
      } else {
         JsonElement var2 = var0.get(var1);
         if (var2.isJsonArray()) {
            return lines(var2.getAsJsonArray());
         } else {
            return !var2.isJsonObject() ? List.of() : firstLines(var2.getAsJsonObject(), "nextdialog", "nextDialog", "lines", "dialog", "text");
         }
      }
   }

   private static List<String> firstLines(JsonObject var0, String... var1) {
      for (String var5 : var1) {
         if (var0.has(var5)) {
            JsonElement var6 = var0.get(var5);
            if (var6.isJsonArray()) {
               return lines(var6.getAsJsonArray());
            }

            if (var6.isJsonPrimitive() && var6.getAsJsonPrimitive().isString()) {
               return sanitize(List.of(var6.getAsString()));
            }
         }
      }

      return List.of();
   }

   private static List<String> lines(JsonArray var0) {
      ArrayList var1 = new ArrayList();

      for (JsonElement var3 : var0) {
         if (var3.isJsonPrimitive() && var3.getAsJsonPrimitive().isString()) {
            var1.add(var3.getAsString());
         }
      }

      return sanitize(var1);
   }

   private static List<String> sanitize(List<String> var0) {
      ArrayList var1 = new ArrayList(Math.min(var0.size(), 128));

      for (String var3 : var0) {
         if (var1.size() >= 128) {
            break;
         }

         String var4 = var3 == null ? "" : var3.replace('\u0000', ' ').strip();
         if (var4.length() > 512) {
            var4 = var4.substring(0, 512);
         }

         var1.add(var4);
      }

      return List.copyOf(var1);
   }

   private static String normalizeLocale(String var0) {
      if (var0 != null && !var0.isBlank()) {
         String var1 = var0.toLowerCase(Locale.ROOT).replace('-', '_');
         return var1.matches("[a-z]{2}_[a-z]{2}") ? var1 : "en_us";
      } else {
         return "en_us";
      }
   }
}
