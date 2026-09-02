package net.cobbleservertools.battle;

import net.cobbleservertools.network.CobbleServerToolsNetworking;
import net.minecraft.server.level.ServerPlayer;

public final class PlayerMoneyService {
   private static final String LEGACY_CACHE_KEY = "money";

   private PlayerMoneyService() {
   }

   public static int get(ServerPlayer var0) {
      MoneyPersistentState var1 = MoneyPersistentState.get(var0.getServer());
      if (!var1.contains(var0.getUUID()) && var0.getPersistentData().contains("money")) {
         var1.set(var0.getUUID(), Math.max(0, var0.getPersistentData().getInt("money")));
      }

      int var2 = var1.get(var0.getUUID());
      var0.getPersistentData().putInt("money", var2);
      return var2;
   }

   public static int set(ServerPlayer var0, int var1) {
      int var2 = MoneyPersistentState.get(var0.getServer()).set(var0.getUUID(), var1);
      var0.getPersistentData().putInt("money", var2);
      CobbleServerToolsNetworking.syncMoney(var0, var2);
      return var2;
   }

   public static int add(ServerPlayer var0, int var1) {
      int var2 = MoneyPersistentState.get(var0.getServer()).add(var0.getUUID(), var1);
      var0.getPersistentData().putInt("money", var2);
      CobbleServerToolsNetworking.syncMoney(var0, var2);
      return var2;
   }

   public static boolean subtract(ServerPlayer var0, int var1) {
      MoneyPersistentState var2 = MoneyPersistentState.get(var0.getServer());
      boolean var3 = var2.subtract(var0.getUUID(), var1);
      int var4 = var2.get(var0.getUUID());
      var0.getPersistentData().putInt("money", var4);
      CobbleServerToolsNetworking.syncMoney(var0, var4);
      return var3;
   }

   public static boolean hasAccount(ServerPlayer var0) {
      return MoneyPersistentState.get(var0.getServer()).contains(var0.getUUID());
   }

   public static void copy(ServerPlayer var0, ServerPlayer var1) {
      int var2 = get(var1);
      var1.getPersistentData().putInt("money", var2);
      CobbleServerToolsNetworking.syncMoney(var1, var2);
   }
}
