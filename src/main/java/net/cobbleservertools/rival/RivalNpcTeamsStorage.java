package net.cobbleservertools.rival;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.cobbleservertools.CobbleServerTools;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

public final class RivalNpcTeamsStorage {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final Type MAP_TYPE = (new TypeToken<Map<UUID, RivalNpcTeamsStorage.Teams>>() {}).getType();
   private static final Map<MinecraftServer, RivalNpcTeamsStorage> INSTANCES = new WeakHashMap<>();
   private final Path filePath;
   private final Map<UUID, RivalNpcTeamsStorage.Teams> data = new LinkedHashMap<>();
   private boolean dirty;

   private RivalNpcTeamsStorage(MinecraftServer var1) {
      this.filePath = var1.getWorldPath(LevelResource.ROOT).resolve("cobbleservertools/rival/teams.json");
      this.readFromDisk();
   }

   public static synchronized RivalNpcTeamsStorage get(MinecraftServer var0) {
      return INSTANCES.computeIfAbsent(var0, RivalNpcTeamsStorage::new);
   }

   public synchronized RivalNpcTeamsStorage.Teams getOrCreate(UUID var1) {
      RivalNpcTeamsStorage.Teams var2 = this.data.computeIfAbsent(var1, var0 -> new RivalNpcTeamsStorage.Teams());
      return deepCopy(var2);
   }

   public synchronized RivalNpcTeamsStorage.Teams get(UUID var1) {
      RivalNpcTeamsStorage.Teams var2 = this.data.get(var1);
      return var2 == null ? null : deepCopy(var2);
   }

   public synchronized void put(UUID var1, RivalNpcTeamsStorage.Teams var2) {
      this.data.put(var1, deepCopy(var2 == null ? new RivalNpcTeamsStorage.Teams() : var2));
      this.dirty = true;
      this.flush();
   }

   public synchronized void flush() {
      if (this.dirty) {
         try {
            Files.createDirectories(this.filePath.getParent());

            try (BufferedWriter var1 = Files.newBufferedWriter(this.filePath)) {
               GSON.toJson(this.data, MAP_TYPE, var1);
            }

            this.dirty = false;
         } catch (Exception var6) {
            CobbleServerTools.LOGGER.error("Could not save rival team storage {}", new Object[]{this.filePath, var6});
         }
      }
   }

   public static synchronized void flushAndRelease(MinecraftServer var0) {
      RivalNpcTeamsStorage var1 = INSTANCES.remove(var0);
      if (var1 != null) {
         var1.flush();
      }
   }

   private void readFromDisk() {
      if (Files.isRegularFile(this.filePath)) {
         try (BufferedReader var1 = Files.newBufferedReader(this.filePath)) {
            Map<UUID, Teams> var2 = GSON.fromJson(var1, MAP_TYPE);
            if (var2 != null) {
               var2.forEach((var1x, var2x) -> this.data.put(var1x, sanitize(var2x)));
            }
         } catch (Exception var6) {
            CobbleServerTools.LOGGER.warn("Could not load rival team storage {}", new Object[]{this.filePath, var6});
         }
      }
   }

   public static RivalNpcTeamsStorage.Teams deepCopy(RivalNpcTeamsStorage.Teams var0) {
      RivalNpcTeamsStorage.Teams var1 = new RivalNpcTeamsStorage.Teams();
      if (var0 == null) {
         return var1;
      }

      var1.fire = deepCopyList(var0.fire);
      var1.water = deepCopyList(var0.water);
      var1.grass = deepCopyList(var0.grass);
      return var1;
   }

   private static RivalNpcTeamsStorage.Teams sanitize(RivalNpcTeamsStorage.Teams var0) {
      return deepCopy(var0);
   }

   private static List<RivalNpcTeamsStorage.PokemonEntry> deepCopyList(List<RivalNpcTeamsStorage.PokemonEntry> var0) {
      if (var0 == null) {
         return new ArrayList<>();
      }

      ArrayList var1 = new ArrayList(Math.min(6, var0.size()));

      for (int var2 = 0; var2 < Math.min(6, var0.size()); var2++) {
         RivalNpcTeamsStorage.PokemonEntry var3 = (RivalNpcTeamsStorage.PokemonEntry)var0.get(var2);
         if (var3 != null && var3.species != null && !var3.species.isBlank()) {
            var1.add(new RivalNpcTeamsStorage.PokemonEntry(var3.species, var3.level, var3.gender, var3.moves));
         }
      }

      return var1;
   }

   public static CompoundTag toNbt(RivalNpcTeamsStorage.Teams var0) {
      CompoundTag var1 = new CompoundTag();
      var1.put("Fire", writeList(var0 == null ? Collections.emptyList() : var0.fire));
      var1.put("Water", writeList(var0 == null ? Collections.emptyList() : var0.water));
      var1.put("Grass", writeList(var0 == null ? Collections.emptyList() : var0.grass));
      return var1;
   }

   public static RivalNpcTeamsStorage.Teams fromNbt(CompoundTag var0) {
      RivalNpcTeamsStorage.Teams var1 = new RivalNpcTeamsStorage.Teams();
      if (var0 == null) {
         return var1;
      }

      var1.fire = readList(list(var0, "Fire", "fire"));
      var1.water = readList(list(var0, "Water", "water"));
      var1.grass = readList(list(var0, "Grass", "grass"));
      return var1;
   }

   private static ListTag writeList(List<RivalNpcTeamsStorage.PokemonEntry> var0) {
      ListTag var1 = new ListTag();
      if (var0 == null) {
         return var1;
      }

      for (int var2 = 0; var2 < Math.min(6, var0.size()); var2++) {
         RivalNpcTeamsStorage.PokemonEntry var3 = (RivalNpcTeamsStorage.PokemonEntry)var0.get(var2);
         if (var3 != null && var3.species != null && !var3.species.isBlank()) {
            CompoundTag var4 = new CompoundTag();
            var4.putString("Species", var3.species);
            var4.putInt("Level", Math.max(1, Math.min(100, var3.level)));
            var4.putString("Gender", var3.gender == null ? "" : var3.gender);
            ListTag var5 = new ListTag();
            if (var3.moves != null) {
               for (int var6 = 0; var6 < Math.min(4, var3.moves.size()); var6++) {
                  String var7 = var3.moves.get(var6);
                  if (var7 != null && !var7.isBlank()) {
                     var5.add(StringTag.valueOf(var7));
                  }
               }
            }

            var4.put("Moves", var5);
            var1.add(var4);
         }
      }

      return var1;
   }

   private static ListTag list(CompoundTag var0, String var1, String var2) {
      return var0.contains(var1, 9) ? var0.getList(var1, 10) : var0.getList(var2, 10);
   }

   private static String first(String var0, String var1) {
      return var0 != null && !var0.isBlank() ? var0 : (var1 == null ? "" : var1);
   }

   private static List<RivalNpcTeamsStorage.PokemonEntry> readList(ListTag var0) {
      ArrayList var1 = new ArrayList(Math.min(6, var0.size()));

      for (int var2 = 0; var2 < Math.min(6, var0.size()); var2++) {
         CompoundTag var3 = var0.getCompound(var2);
         ArrayList var4 = new ArrayList(4);
         ListTag var5 = var3.contains("Moves", 9) ? var3.getList("Moves", 8) : var3.getList("moves", 8);

         for (int var6 = 0; var6 < Math.min(4, var5.size()); var6++) {
            var4.add(var5.getString(var6));
         }

         String var9 = first(var3.getString("Species"), var3.getString("species"));
         int var7 = var3.contains("Level") ? var3.getInt("Level") : var3.getInt("level");
         String var8 = first(var3.getString("Gender"), var3.getString("gender"));
         if (!var9.isBlank()) {
            var1.add(new RivalNpcTeamsStorage.PokemonEntry(var9, var7, var8, var4));
         }
      }

      return var1;
   }

   public static final class PokemonEntry {
      public String species = "";
      public int level = 1;
      public String gender = "";
      public List<String> moves = new ArrayList<>();

      public PokemonEntry() {
      }

      public PokemonEntry(String var1, int var2, String var3, List<String> var4) {
         this.species = var1 == null ? "" : var1;
         this.level = Math.max(1, Math.min(100, var2));
         this.gender = var3 == null ? "" : var3;
         this.moves = var4 == null ? new ArrayList<>() : new ArrayList<>(var4.subList(0, Math.min(4, var4.size())));
      }
   }

   public static final class Teams {
      public List<RivalNpcTeamsStorage.PokemonEntry> fire = new ArrayList<>();
      public List<RivalNpcTeamsStorage.PokemonEntry> water = new ArrayList<>();
      public List<RivalNpcTeamsStorage.PokemonEntry> grass = new ArrayList<>();
   }
}
