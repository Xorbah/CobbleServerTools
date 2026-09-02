package net.cobbleservertools.rival.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.cobbleservertools.CobbleServerTools;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

public final class RivalStarterStorage {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final Type MAP_TYPE = (new TypeToken<Map<UUID, RivalStarterStorage.Branch>>() {}).getType();
   private static final Map<MinecraftServer, RivalStarterStorage> INSTANCES = new WeakHashMap<>();
   private final Path file;
   private final Map<UUID, RivalStarterStorage.Branch> starters = new LinkedHashMap<>();
   private boolean dirty;

   private RivalStarterStorage(MinecraftServer var1) {
      this.file = var1.getWorldPath(LevelResource.ROOT).resolve("cobbleservertools/rival/starter.json");
      this.load();
   }

   public static synchronized RivalStarterStorage get(MinecraftServer var0) {
      return INSTANCES.computeIfAbsent(var0, RivalStarterStorage::new);
   }

   public synchronized RivalStarterStorage.Branch get(UUID var1) {
      return this.starters.get(var1);
   }

   public synchronized void put(UUID var1, RivalStarterStorage.Branch var2) {
      if (var2 == null) {
         this.starters.remove(var1);
      } else {
         this.starters.put(var1, var2);
      }

      this.dirty = true;
      this.flush();
   }

   public synchronized void remove(UUID var1) {
      if (this.starters.remove(var1) != null) {
         this.dirty = true;
         this.flush();
      }
   }

   public synchronized Map<UUID, RivalStarterStorage.Branch> snapshot() {
      return Map.copyOf(this.starters);
   }

   public synchronized void flush() {
      if (this.dirty) {
         try {
            Files.createDirectories(this.file.getParent());

            try (BufferedWriter var1 = Files.newBufferedWriter(this.file)) {
               GSON.toJson(this.starters, MAP_TYPE, var1);
            }

            this.dirty = false;
         } catch (Exception var6) {
            CobbleServerTools.LOGGER.error("Could not save rival starter storage {}", new Object[]{this.file, var6});
         }
      }
   }

   public static synchronized void flushAndRelease(MinecraftServer var0) {
      RivalStarterStorage var1 = INSTANCES.remove(var0);
      if (var1 != null) {
         var1.flush();
      }
   }

   private void load() {
      if (Files.isRegularFile(this.file)) {
         try (BufferedReader var1 = Files.newBufferedReader(this.file)) {
            Map var2 = (Map)GSON.fromJson(var1, MAP_TYPE);
            if (var2 != null) {
               this.starters.putAll(var2);
            }
         } catch (Exception var6) {
            CobbleServerTools.LOGGER.warn("Could not load rival starter storage {}", new Object[]{this.file, var6});
         }
      }
   }

   public enum Branch {
      FIRE,
      WATER,
      GRASS;
   }
}
