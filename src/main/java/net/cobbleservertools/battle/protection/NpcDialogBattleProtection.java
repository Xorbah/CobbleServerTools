package net.cobbleservertools.battle.protection;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class NpcDialogBattleProtection {
   public static final String TAG = "cobbleservertools_dialog_battle_protected";
   public static final int DIALOG_TIMEOUT_TICKS = 40;
   public static final int ENGAGE_TIMEOUT_TICKS = 10;
   private static final Map<UUID, NpcDialogBattleProtection.Window> WINDOWS = new HashMap<>();

   private NpcDialogBattleProtection() {
   }

   public static synchronized void beginEngage(ServerPlayer var0) {
      touchEngage(var0);
   }

   public static synchronized void touchEngage(ServerPlayer var0) {
      if (var0 != null) {
         NpcDialogBattleProtection.Window var1 = WINDOWS.computeIfAbsent(var0.getUUID(), var0x -> new NpcDialogBattleProtection.Window());
         var1.engageUntil = gameTime(var0) + 10L;
         var0.addTag("cobbleservertools_dialog_battle_protected");
      }
   }

   public static synchronized void endEngage(ServerPlayer var0) {
      NpcDialogBattleProtection.Window var1 = var0 == null ? null : WINDOWS.get(var0.getUUID());
      if (var1 != null) {
         var1.engageUntil = 0L;
         updateTag(var0, var1);
      }
   }

   public static synchronized void beginDialog(ServerPlayer var0) {
      touchDialog(var0);
   }

   public static synchronized void touchDialog(ServerPlayer var0) {
      if (var0 != null) {
         NpcDialogBattleProtection.Window var1 = WINDOWS.computeIfAbsent(var0.getUUID(), var0x -> new NpcDialogBattleProtection.Window());
         var1.dialogUntil = gameTime(var0) + 40L;
         var0.addTag("cobbleservertools_dialog_battle_protected");
      }
   }

   public static synchronized void endDialog(ServerPlayer var0) {
      NpcDialogBattleProtection.Window var1 = var0 == null ? null : WINDOWS.get(var0.getUUID());
      if (var1 != null) {
         var1.dialogUntil = 0L;
         updateTag(var0, var1);
      }
   }

   public static synchronized boolean isProtected(ServerPlayer var0) {
      if (var0 == null) {
         return false;
      }

      NpcDialogBattleProtection.Window var1 = WINDOWS.get(var0.getUUID());
      if (var1 == null) {
         return false;
      }

      long var2 = gameTime(var0);
      return var1.engageUntil > var2 || var1.dialogUntil > var2;
   }

   public static synchronized void clear(ServerPlayer var0) {
      if (var0 != null) {
         WINDOWS.remove(var0.getUUID());
         var0.removeTag("cobbleservertools_dialog_battle_protected");
      }
   }

   public static synchronized void tick(MinecraftServer var0) {
      if (var0 != null) {
         for (ServerPlayer var2 : var0.getPlayerList().getPlayers()) {
            NpcDialogBattleProtection.Window var3 = WINDOWS.get(var2.getUUID());
            if (var3 != null) {
               updateTag(var2, var3);
            }
         }
      }
   }

   public static synchronized void clearAll(MinecraftServer var0) {
      if (var0 != null) {
         for (ServerPlayer var2 : var0.getPlayerList().getPlayers()) {
            var2.removeTag("cobbleservertools_dialog_battle_protected");
         }
      }

      WINDOWS.clear();
   }

   private static void updateTag(ServerPlayer var0, NpcDialogBattleProtection.Window var1) {
      long var2 = gameTime(var0);
      if (var1.engageUntil <= var2 && var1.dialogUntil <= var2) {
         var0.removeTag("cobbleservertools_dialog_battle_protected");
         WINDOWS.remove(var0.getUUID());
      } else {
         var0.addTag("cobbleservertools_dialog_battle_protected");
      }
   }

   private static long gameTime(ServerPlayer var0) {
      return var0.serverLevel().getGameTime();
   }

   private static final class Window {
      long engageUntil;
      long dialogUntil;
   }
}
