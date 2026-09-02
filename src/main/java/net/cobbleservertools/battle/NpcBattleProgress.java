package net.cobbleservertools.battle;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Consumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

public final class NpcBattleProgress {
   private final Set<UUID> winners = new HashSet<>();
   private final Map<UUID, Long> availableAt = new HashMap<>();

   public void read(CompoundTag var1) {
      this.winners.clear();
      this.availableAt.clear();
      ListTag var2 = var1.getList("Winners", 8);

      for (int var3 = 0; var3 < var2.size(); var3++) {
         parseUuid(var2.getString(var3), this.winners::add);
      }

      ListTag var9 = var1.getList("RematchCooldowns", 10);

      for (int var4 = 0; var4 < var9.size(); var4++) {
         CompoundTag var5 = var9.getCompound(var4);
         String var6 = var5.contains("PlayerUuid") ? var5.getString("PlayerUuid") : var5.getString("Player");
         long var7 = var5.contains("UnlockAt") ? var5.getLong("UnlockAt") : var5.getLong("AvailableAt");
         parseUuid(var6, var3x -> this.availableAt.put(var3x, var7));
      }

      if (var1.contains("RematchCooldowns", 10)) {
         CompoundTag var10 = var1.getCompound("RematchCooldowns");

         for (String var12 : var10.getAllKeys()) {
            parseUuid(var12, var3x -> this.availableAt.put(var3x, var10.getLong(var12)));
         }
      }
   }

   public void write(CompoundTag var1) {
      ListTag var2 = new ListTag();
      this.winners.stream().sorted().forEach(var1x -> var2.add(StringTag.valueOf(var1x.toString())));
      var1.put("Winners", var2);
      ListTag var3 = new ListTag();
      this.availableAt.entrySet().stream().sorted(Entry.comparingByKey()).forEach(var1x -> {
         CompoundTag var2x = new CompoundTag();
         var2x.putString("PlayerUuid", var1x.getKey().toString());
         var2x.putLong("UnlockAt", var1x.getValue());
         var3.add(var2x);
      });
      var1.put("RematchCooldowns", var3);
   }

   public boolean hasActiveWin(UUID var1, long var2) {
      this.expire(var1, var2);
      return this.winners.contains(var1);
   }

   public boolean markWon(UUID var1, int var2, long var3) {
      boolean var5 = !this.winners.contains(var1);
      this.winners.add(var1);
      if (var2 > 0) {
         this.availableAt.put(var1, var3 + var2 * 1000L);
      } else {
         this.availableAt.remove(var1);
      }

      return var5;
   }

   public long remainingMillis(UUID var1, long var2) {
      this.expire(var1, var2);
      return Math.max(0L, this.availableAt.getOrDefault(var1, 0L) - var2);
   }

   public void clear(UUID var1) {
      this.winners.remove(var1);
      this.availableAt.remove(var1);
   }

   private void expire(UUID var1, long var2) {
      Long var4 = this.availableAt.get(var1);
      if (var4 != null && var4 <= var2) {
         this.clear(var1);
      }
   }

   private static void parseUuid(String var0, Consumer<UUID> var1) {
      try {
         var1.accept(UUID.fromString(var0));
      } catch (IllegalArgumentException var3) {
      }
   }
}
