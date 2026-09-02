package net.cobbleservertools.dialog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import net.cobbleservertools.CobbleServerTools;
import net.cobbleservertools.battle.protection.NpcDialogBattleProtection;
import net.cobbleservertools.entity.AbstractBattleNpcEntity;
import net.cobbleservertools.entity.AbstractCobbleNpcEntity;
import net.cobbleservertools.network.payload.OpenNpcDialogPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;

public final class NpcDialogSessions {
   private static final long LIFETIME_MILLIS = 120000L;
   private static final double MAX_DISTANCE_SQUARED = 4096.0;
   private static final int MAX_SESSIONS = 4096;
   private static final Map<UUID, NpcDialogSessions.Session> SESSIONS = new HashMap<>();

   private NpcDialogSessions() {
   }

   public static synchronized boolean open(ServerPlayer var0, AbstractCobbleNpcEntity var1, String var2, NpcDialogPurpose var3) {
      return openInternal(var0, var1, var2, var3, true);
   }

   public static synchronized boolean openIfPresent(ServerPlayer var0, AbstractCobbleNpcEntity var1, String var2, NpcDialogPurpose var3) {
      return openInternal(var0, var1, var2, var3, false);
   }

   private static boolean openInternal(ServerPlayer var0, AbstractCobbleNpcEntity var1, String var2, NpcDialogPurpose var3, boolean var4) {
      cleanupExpired();
      String var5 = var0.clientInformation().language();
      NpcDialog var6 = NpcDialogCatalog.find(var5, var2).orElse(null);
      if (var6 != null && !var6.main().isEmpty()) {
         if (SESSIONS.size() >= 4096) {
            cleanupExpired();
            if (SESSIONS.size() >= 4096) {
               SESSIONS.clear();
            }
         }

         long var7 = ThreadLocalRandom.current().nextLong();
         NpcDialogSessions.Session var9 = new NpcDialogSessions.Session(
            var1.getUUID(),
            var1.getId(),
            var7,
            var3,
            NpcDialogText.apply(var6.yes(), var0),
            NpcDialogText.apply(var6.no(), var0),
            NpcDialogText.apply(var6.cancel(), var0),
            System.currentTimeMillis() + 120000L,
            var6.hasChoices(),
            null,
            false
         );
         SESSIONS.put(var0.getUUID(), var9);
         if (var3 == NpcDialogPurpose.BATTLE_START && var1 instanceof AbstractBattleNpcEntity) {
            NpcDialogBattleProtection.beginDialog(var0);
         }

         send(var0, var1, var7, NpcDialogText.apply(var6.main(), var0), var6.hasChoices());
         return true;
      } else {
         if (var4) {
            var0.displayClientMessage(Component.translatable("message.cobbleservertools.dialog_missing", new Object[]{var2}), false);
            CobbleServerTools.LOGGER.warn("Missing dialogue '{}' for locale {} on NPC {}", new Object[]{var2, var5, var1.getUUID()});
         }

         return false;
      }
   }

   public static synchronized void choose(ServerPlayer var0, int var1, long var2, boolean var4) {
      NpcDialogSessions.Session var5 = validate(var0, var1, var2);
      if (var5 != null && var5.choiceRequired() && !var5.resolved()) {
         NpcDialogSessions.Session var6 = var5.withResolution(var4);
         SESSIONS.put(var0.getUUID(), var6);
         if (var0.serverLevel().getEntity(var1) instanceof AbstractCobbleNpcEntity var8) {
            send(var0, var8, var2, var4 ? var5.yes() : var5.no(), false);
         } else {
            SESSIONS.remove(var0.getUUID());
         }
      }
   }

   public static synchronized void close(ServerPlayer var0, int var1, long var2, boolean var4) {
      NpcDialogSessions.Session var5 = validate(var0, var1, var2);
      if (var5 != null) {
         Entity var6 = var0.serverLevel().getEntity(var1);
         SESSIONS.remove(var0.getUUID());
         if (var5.purpose() == NpcDialogPurpose.BATTLE_START) {
            NpcDialogBattleProtection.endDialog(var0);
         }

         if (var4 && (!var5.choiceRequired() || var5.resolved()) && var6 instanceof AbstractCobbleNpcEntity var7) {
            var7.onDialogCompleted(var0, var5.purpose(), var5.answer());
         }
      }
   }

   public static synchronized boolean hasActiveSession(ServerPlayer var0, AbstractCobbleNpcEntity var1, NpcDialogPurpose var2) {
      cleanupExpired();
      if (var0 != null && var1 != null && var2 != null) {
         NpcDialogSessions.Session var3 = SESSIONS.get(var0.getUUID());
         return var3 != null && var3.entityId() == var1.getId() && var1.getUUID().equals(var3.entityUuid()) && var3.purpose() == var2;
      } else {
         return false;
      }
   }

   public static synchronized void disconnect(ServerPlayer var0) {
      SESSIONS.remove(var0.getUUID());
      NpcDialogBattleProtection.clear(var0);
   }

   public static synchronized void clear() {
      SESSIONS.clear();
   }

   private static NpcDialogSessions.Session validate(ServerPlayer var0, int var1, long var2) {
      cleanupExpired();
      NpcDialogSessions.Session var4 = SESSIONS.get(var0.getUUID());
      if (var4 != null && var4.entityId() == var1 && var4.token() == var2) {
         if (var0.serverLevel().getEntity(var1) instanceof AbstractCobbleNpcEntity var6
            && !var6.isRemoved()
            && var6.getUUID().equals(var4.entityUuid())
            && !(var0.distanceToSqr(var6) > 4096.0)) {
            return var4;
         } else {
            SESSIONS.remove(var0.getUUID());
            return null;
         }
      } else {
         return null;
      }
   }

   private static void send(ServerPlayer var0, AbstractCobbleNpcEntity var1, long var2, List<String> var4, boolean var5) {
      String var6 = var1.npcName().isBlank() ? "NPC" : var1.npcName();
      PacketDistributor.sendToPlayer(var0, new OpenNpcDialogPayload(var1.getId(), var2, var6, var4, var5), new CustomPacketPayload[0]);
   }

   private static void cleanupExpired() {
      long var0 = System.currentTimeMillis();
      SESSIONS.values().removeIf(var2 -> var2.expiresAt() < var0);
   }

   private record Session(
      UUID entityUuid,
      int entityId,
      long token,
      NpcDialogPurpose purpose,
      List<String> yes,
      List<String> no,
      List<String> cancel,
      long expiresAt,
      boolean choiceRequired,
      Boolean answer,
      boolean resolved
   ) {
      NpcDialogSessions.Session withResolution(boolean var1) {
         return new NpcDialogSessions.Session(
            this.entityUuid,
            this.entityId,
            this.token,
            this.purpose,
            this.yes,
            this.no,
            this.cancel,
            System.currentTimeMillis() + 120000L,
            this.choiceRequired,
            var1,
            true
         );
      }
   }
}
