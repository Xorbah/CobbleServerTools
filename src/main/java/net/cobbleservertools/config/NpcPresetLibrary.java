package net.cobbleservertools.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Stream;
import net.cobbleservertools.CobbleServerTools;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.neoforged.fml.loading.FMLPaths;

public final class NpcPresetLibrary {
   public static final int PAGE_SIZE = 8;
   private static final int MAX_PRESETS = 10000;
   private static volatile List<NpcPresetLibrary.Preset> presets = List.of();
   private static volatile boolean loaded;

   private NpcPresetLibrary() {
   }

   public static synchronized void reload() {
      Path var0 = presetRoot();
      ArrayList<Preset> var1 = new ArrayList<>();

      try {
         Files.createDirectories(var0);

         try (Stream<Path> var2 = Files.walk(var0)) {
            var2.filter(var0x -> Files.isRegularFile(var0x))
               .filter(var0x -> var0x.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".json"))
               .sorted()
               .limit(10000L)
               .forEach(var1x -> {
                  try {
                     NpcPresetLibrary.Preset var2x = parse(var1x);
                     if (var2x != null) {
                        var1.add(var2x);
                     }
                  } catch (Exception var3) {
                     CobbleServerTools.LOGGER.warn("Failed to load CobbleServerTools NPC preset {}: {}", new Object[]{var1x, var3.toString()});
                  }
               });
         }
      } catch (IOException var7) {
         CobbleServerTools.LOGGER.warn("Failed to scan CobbleServerTools NPC preset directory {}: {}", new Object[]{var0, var7.toString()});
      }

      var1.sort(
         Comparator.comparing(NpcPresetLibrary.Preset::displayName, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(NpcPresetLibrary.Preset::id, String.CASE_INSENSITIVE_ORDER)
      );
      presets = List.copyOf(var1);
      loaded = true;
      CobbleServerTools.LOGGER.info("Indexed {} CobbleServerTools NPC presets from {}", new Object[]{presets.size(), var0});
   }

   public static void ensureLoaded() {
      if (!loaded) {
         reload();
      }
   }

   public static NpcPresetLibrary.Preset find(String var0) {
      ensureLoaded();
      String var1 = normalizeId(var0);
      if (var1.isBlank()) {
         return null;
      }

      for (NpcPresetLibrary.Preset var3 : presets) {
         if (var3.id().equals(var1)) {
            return var3;
         }
      }

      return null;
   }

   public static NpcPresetLibrary.SearchResult search(NpcPresetLibrary.SearchQuery var0) {
      ensureLoaded();
      NpcPresetLibrary.SearchQuery var1 = var0 == null ? NpcPresetLibrary.SearchQuery.empty() : var0.normalized();
      ArrayList<Preset> var2 = new ArrayList<>();

      for (NpcPresetLibrary.Preset var4 : presets) {
         if (typeMatches(var4.npcType(), var1.npcType())) {
            var2.add(var4);
         }
      }

      NpcPresetLibrary.Facets var13 = facets(var2);
      ArrayList<Preset> var14 = new ArrayList<>();
      String[] var5 = var1.query().isBlank() ? new String[0] : var1.query().split("\\s+");

      for (NpcPresetLibrary.Preset var7 : var2) {
         if ((var1.region().isBlank() || var7.region().equals(var1.region()))
            && (var1.role().isBlank() || var7.role().equals(var1.role()))
            && (var1.location().isBlank() || var7.location().equals(var1.location()))
            && (var1.trainerClass().isBlank() || var7.trainerClass().equals(var1.trainerClass()))
            && (var1.tag().isBlank() || var7.tags().contains(var1.tag()))) {
            boolean var8 = true;

            for (String var12 : var5) {
               if (!var12.isBlank() && !var7.searchText().contains(var12)) {
                  var8 = false;
                  break;
               }
            }

            if (var8) {
               var14.add(var7);
            }
         }
      }

      int var15 = Math.max(1, (var14.size() + 8 - 1) / 8);
      int var16 = Math.max(0, Math.min(var1.page(), var15 - 1));
      int var17 = Math.min(var14.size(), var16 * 8);
      int var18 = Math.min(var14.size(), var17 + 8);
      return new NpcPresetLibrary.SearchResult(List.copyOf(var14.subList(var17, var18)), var14.size(), var16, var15, var13);
   }

   public static CompoundTag searchResultTag(NpcPresetLibrary.SearchResult var0) {
      CompoundTag var1 = new CompoundTag();
      var1.putInt("Total", var0.total());
      var1.putInt("Page", var0.page());
      var1.putInt("Pages", var0.pages());
      ListTag var2 = new ListTag();

      for (NpcPresetLibrary.Preset var4 : var0.entries()) {
         CompoundTag var5 = new CompoundTag();
         var5.putString("Id", var4.id());
         var5.putString("DisplayName", var4.displayName());
         var5.putString("NpcType", var4.npcType());
         var5.putString("Region", var4.region());
         var5.putString("Role", var4.role());
         var5.putString("Location", var4.location());
         var5.putString("TrainerClass", var4.trainerClass());
         var5.putString("Tags", String.join(", ", var4.tags()));
         var2.add(var5);
      }

      var1.put("Results", var2);
      var1.put("Regions", strings(var0.facets().regions()));
      var1.put("Roles", strings(var0.facets().roles()));
      var1.put("Locations", strings(var0.facets().locations()));
      var1.put("TrainerClasses", strings(var0.facets().trainerClasses()));
      var1.put("Tags", strings(var0.facets().tags()));
      return var1;
   }

   private static ListTag strings(List<String> var0) {
      ListTag var1 = new ListTag();

      for (String var3 : var0) {
         var1.add(StringTag.valueOf(var3));
      }

      return var1;
   }

   private static NpcPresetLibrary.Preset parse(Path var0) throws IOException {
      try (BufferedReader var1 = Files.newBufferedReader(var0, StandardCharsets.UTF_8)) {
         JsonElement var2 = JsonParser.parseReader(var1);
         if (!var2.isJsonObject()) {
            throw new IllegalArgumentException("root must be a JSON object");
         }

         JsonObject var3 = var2.getAsJsonObject();
         String var4 = var0.getFileName().toString().replaceFirst("(?i)\\.json$", "");
         String var5 = normalizeId(string(var3, "id"));
         if (var5.isBlank()) {
            var5 = normalizeId(var4);
         }

         if (var5.isBlank()) {
            throw new IllegalArgumentException("missing/invalid id");
         }

         String var6 = string(var3, "displayName");
         if (var6.isBlank()) {
            var6 = var5;
         }

         String var7 = normalizeType(string(var3, "npcType"));
         if (var7.isBlank()) {
            var7 = inferType(var0);
         }

         if (var7.isBlank()) {
            throw new IllegalArgumentException("missing/invalid npcType");
         }

         JsonObject var8 = var3.has("metadata") && var3.get("metadata").isJsonObject() ? var3.getAsJsonObject("metadata") : new JsonObject();
         String var9 = normalizeFacet(string(var8, "region"));
         String var10 = normalizeFacet(string(var8, "role"));
         String var11 = normalizeFacet(string(var8, "location"));
         String var12 = normalizeFacet(string(var8, "trainerClass"));
         LinkedHashSet<String> var13 = new LinkedHashSet<>();

         for (JsonElement var15 : array(var8, "tags")) {
            if (var15.isJsonPrimitive()) {
               String var16 = normalizeFacet(var15.getAsString());
               if (!var16.isBlank()) {
                  var13.add(var16);
               }
            }
         }

         JsonObject var21 = var3.has("configuration") && var3.get("configuration").isJsonObject() ? var3.getAsJsonObject("configuration") : var3;
         CompoundTag var22 = NpcTemplateCatalog.parseObject(var21, var0.toString());
         StringBuilder var23 = new StringBuilder();
         appendSearch(var23, var5);
         appendSearch(var23, var6);
         appendSearch(var23, var7);
         appendSearch(var23, var9);
         appendSearch(var23, var10);
         appendSearch(var23, var11);
         appendSearch(var23, var12);

         for (String var18 : var13) {
            appendSearch(var23, var18);
         }

         collectPrimitives(var21, var23);
         return new NpcPresetLibrary.Preset(var5, var6, var7, var9, var10, var11, var12, Set.copyOf(var13), var23.toString().toLowerCase(Locale.ROOT), var22);
      }
   }

   private static void collectPrimitives(JsonElement var0, StringBuilder var1) {
      if (var0 != null) {
         if (var0.isJsonPrimitive()) {
            appendSearch(var1, var0.getAsString());
         } else if (var0.isJsonArray()) {
            for (JsonElement var3 : var0.getAsJsonArray()) {
               collectPrimitives(var3, var1);
            }
         } else if (var0.isJsonObject()) {
            for (Entry var5 : var0.getAsJsonObject().entrySet()) {
               appendSearch(var1, (String)var5.getKey());
               collectPrimitives((JsonElement)var5.getValue(), var1);
            }
         }
      }
   }

   private static NpcPresetLibrary.Facets facets(List<NpcPresetLibrary.Preset> var0) {
      LinkedHashSet<String> var1 = new LinkedHashSet<>();
      LinkedHashSet<String> var2 = new LinkedHashSet<>();
      LinkedHashSet<String> var3 = new LinkedHashSet<>();
      LinkedHashSet<String> var4 = new LinkedHashSet<>();
      LinkedHashSet<String> var5 = new LinkedHashSet<>();

      for (NpcPresetLibrary.Preset var7 : var0) {
         if (!var7.region().isBlank()) {
            var1.add(var7.region());
         }

         if (!var7.role().isBlank()) {
            var2.add(var7.role());
         }

         if (!var7.location().isBlank()) {
            var3.add(var7.location());
         }

         if (!var7.trainerClass().isBlank()) {
            var4.add(var7.trainerClass());
         }

         var5.addAll(var7.tags());
      }

      return new NpcPresetLibrary.Facets(sorted(var1), sorted(var2), sorted(var3), sorted(var4), sorted(var5));
   }

   private static List<String> sorted(Set<String> var0) {
      ArrayList<String> var1 = new ArrayList<>(var0);
      var1.sort(String.CASE_INSENSITIVE_ORDER);
      return List.copyOf(var1);
   }

   private static boolean typeMatches(String var0, String var1) {
      return var1.isBlank() || var0.equals(var1);
   }

   private static String inferType(Path var0) {
      Path var1 = var0.getParent();
      return var1 == null ? "" : normalizeType(var1.getFileName().toString());
   }

   private static Path presetRoot() {
      return FMLPaths.CONFIGDIR.get().resolve("cobbleservertools").resolve("presets");
   }

   private static JsonArray array(JsonObject var0, String var1) {
      JsonElement var2 = var0.get(var1);
      return var2 != null && var2.isJsonArray() ? var2.getAsJsonArray() : new JsonArray();
   }

   private static String string(JsonObject var0, String var1) {
      JsonElement var2 = var0.get(var1);
      return var2 != null && var2.isJsonPrimitive() ? var2.getAsString().trim() : "";
   }

   private static void appendSearch(StringBuilder var0, String var1) {
      if (var1 != null && !var1.isBlank()) {
         if (var0.length() > 0) {
            var0.append(' ');
         }

         var0.append(var1.replace('_', ' ').replace('-', ' '));
      }
   }

   private static String normalizeId(String var0) {
      String var1 = var0 == null ? "" : var0.trim().toLowerCase(Locale.ROOT);
      return var1.matches("[a-z0-9_.:-]+") ? var1 : "";
   }

   public static String normalizeType(String var0) {
      String var1 = normalizeFacet(var0);

      return switch (var1) {
         case "movetutor", "move-tutor" -> "move_tutor";
         case "vending_machine", "vending-machine" -> "vending";
         case "battle", "rival", "dialog", "trader", "mart", "move_tutor", "vending" -> var1;
         default -> "";
      };
   }

   private static String normalizeFacet(String var0) {
      if (var0 == null) {
         return "";
      }

      String var1 = var0.trim().toLowerCase(Locale.ROOT).replace(' ', '_');
      return var1.matches("[a-z0-9_.:-]*") ? var1 : "";
   }

   public record Facets(List<String> regions, List<String> roles, List<String> locations, List<String> trainerClasses, List<String> tags) {
   }

   public record Preset(
      String id,
      String displayName,
      String npcType,
      String region,
      String role,
      String location,
      String trainerClass,
      Set<String> tags,
      String searchText,
      CompoundTag configuration
   ) {
      public Preset(
         String id,
         String displayName,
         String npcType,
         String region,
         String role,
         String location,
         String trainerClass,
         Set<String> tags,
         String searchText,
         CompoundTag configuration
      ) {
         configuration = configuration == null ? new CompoundTag() : configuration.copy();
         this.id = id;
         this.displayName = displayName;
         this.npcType = npcType;
         this.region = region;
         this.role = role;
         this.location = location;
         this.trainerClass = trainerClass;
         this.tags = tags;
         this.searchText = searchText;
         this.configuration = configuration;
      }

      public CompoundTag configuration() {
         return this.configuration.copy();
      }
   }

   public record SearchQuery(String npcType, String query, String region, String role, String location, String trainerClass, String tag, int page) {
      public static NpcPresetLibrary.SearchQuery empty() {
         return new NpcPresetLibrary.SearchQuery("", "", "", "", "", "", "", 0);
      }

      public NpcPresetLibrary.SearchQuery normalized() {
         return new NpcPresetLibrary.SearchQuery(
            NpcPresetLibrary.normalizeType(this.npcType),
            normalizeSearch(this.query),
            NpcPresetLibrary.normalizeFacet(this.region),
            NpcPresetLibrary.normalizeFacet(this.role),
            NpcPresetLibrary.normalizeFacet(this.location),
            NpcPresetLibrary.normalizeFacet(this.trainerClass),
            NpcPresetLibrary.normalizeFacet(this.tag),
            Math.max(0, this.page)
         );
      }

      private static String normalizeSearch(String var0) {
         String var1 = var0 == null ? "" : var0.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
         return var1.length() > 128 ? var1.substring(0, 128) : var1;
      }
   }

   public record SearchResult(List<NpcPresetLibrary.Preset> entries, int total, int page, int pages, NpcPresetLibrary.Facets facets) {
   }
}
