package net.cobbleservertools.battle;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedData.Factory;

public final class MoneyPersistentState extends SavedData {
   private static final String FILE_ID = "cobbleservertools_money";
   private static final Factory<MoneyPersistentState> FACTORY = new Factory<>(MoneyPersistentState::new, MoneyPersistentState::load, null);
   private final Map<UUID, Integer> balances = new HashMap<>();

   public static MoneyPersistentState get(MinecraftServer var0) {
      return (MoneyPersistentState)var0.overworld().getDataStorage().computeIfAbsent(FACTORY, "cobbleservertools_money");
   }

   public static MoneyPersistentState load(CompoundTag var0, Provider var1) {
      MoneyPersistentState var2 = new MoneyPersistentState();
      CompoundTag var3 = var0.getCompound("balances");

      for (String var5 : var3.getAllKeys()) {
         try {
            var2.balances.put(UUID.fromString(var5), Math.max(0, var3.getInt(var5)));
         } catch (IllegalArgumentException var7) {
         }
      }

      return var2;
   }

   public CompoundTag save(CompoundTag var1, Provider var2) {
      CompoundTag var3 = new CompoundTag();
      this.balances.forEach((var1x, var2x) -> var3.putInt(var1x.toString(), Math.max(0, var2x)));
      var1.put("balances", var3);
      return var1;
   }

   public int get(UUID var1) {
      return this.balances.getOrDefault(var1, 0);
   }

   public boolean contains(UUID var1) {
      return this.balances.containsKey(var1);
   }

   public int set(UUID var1, int var2) {
      int var3 = Math.max(0, var2);
      this.balances.put(var1, var3);
      this.setDirty();
      return var3;
   }

   public int add(UUID var1, int var2) {
      long var3 = (long)this.get(var1) + Math.max(0, var2);
      return this.set(var1, (int)Math.min(2147483647L, var3));
   }

   public boolean subtract(UUID var1, int var2) {
      int var3 = Math.max(0, var2);
      int var4 = this.get(var1);
      if (var4 < var3) {
         return false;
      }

      this.set(var1, var4 - var3);
      return true;
   }
}
