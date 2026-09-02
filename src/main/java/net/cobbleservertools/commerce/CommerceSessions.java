package net.cobbleservertools.commerce;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import net.cobbleservertools.entity.AbstractCobbleNpcEntity;
import net.cobbleservertools.entity.MartNpcEntity;
import net.cobbleservertools.entity.MoveTutorNpcEntity;
import net.cobbleservertools.entity.TraderNpcEntity;
import net.cobbleservertools.network.payload.OpenMartPayload;
import net.cobbleservertools.network.payload.OpenTraderPayload;
import net.cobbleservertools.network.payload.OpenTutorPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;

public final class CommerceSessions {
   private static final long LIFETIME_MILLIS = 90000L;
   private static final double MAX_DISTANCE_SQUARED = 4096.0;
   private static final Map<UUID, CommerceSessions.Session> SESSIONS = new HashMap<>();

   private CommerceSessions() {
   }

   public static synchronized void openMart(ServerPlayer var0, MartNpcEntity var1) {
      long var2 = issue(var0, var1, CommerceSessions.Kind.MART);
      PacketDistributor.sendToPlayer(
         var0,
         new OpenMartPayload(var1.getId(), var2, title(var1, "screen.cobbleservertools.poke_shop"), MartService.clientView(var0, var1)),
         new CustomPacketPayload[0]
      );
   }

   public static synchronized void openTrader(ServerPlayer var0, TraderNpcEntity var1) {
      long var2 = issue(var0, var1, CommerceSessions.Kind.TRADER);
      PacketDistributor.sendToPlayer(
         var0,
         new OpenTraderPayload(var1.getId(), var2, title(var1, "cobbleservertools.trader.title"), TraderService.clientView(var0, var1)),
         new CustomPacketPayload[0]
      );
   }

   public static synchronized void openTutor(ServerPlayer var0, MoveTutorNpcEntity var1) {
      long var2 = issue(var0, var1, CommerceSessions.Kind.TUTOR);
      PacketDistributor.sendToPlayer(
         var0,
         new OpenTutorPayload(var1.getId(), var2, title(var1, "cobbleservertools.tutor.title"), MoveTutorService.clientView(var0, var1)),
         new CustomPacketPayload[0]
      );
   }

   public static synchronized <T extends AbstractCobbleNpcEntity> T validate(ServerPlayer var0, int var1, long var2, CommerceSessions.Kind var4, Class<T> var5) {
      cleanup();
      CommerceSessions.Session var6 = SESSIONS.get(var0.getUUID());
      if (var6 != null && var6.entityId == var1 && var6.token == var2 && var6.kind == var4) {
         Entity var7 = var0.serverLevel().getEntity(var1);
         if (var5.isInstance(var7) && !var7.isRemoved() && var7.getUUID().equals(var6.entityUuid) && !(var0.distanceToSqr(var7) > 4096.0)) {
            var6.expiresAt = System.currentTimeMillis() + 90000L;
            return (T)var5.cast(var7);
         } else {
            SESSIONS.remove(var0.getUUID());
            return null;
         }
      } else {
         return null;
      }
   }

   public static synchronized void disconnect(ServerPlayer var0) {
      SESSIONS.remove(var0.getUUID());
   }

   public static synchronized void clear() {
      SESSIONS.clear();
   }

   private static long issue(ServerPlayer var0, AbstractCobbleNpcEntity var1, CommerceSessions.Kind var2) {
      cleanup();
      long var3 = ThreadLocalRandom.current().nextLong();
      SESSIONS.put(var0.getUUID(), new CommerceSessions.Session(var1.getUUID(), var1.getId(), var3, var2, System.currentTimeMillis() + 90000L));
      return var3;
   }

   private static String title(AbstractCobbleNpcEntity var0, String var1) {
      return var0.npcName().isBlank() ? var1 : var0.npcName();
   }

   private static void cleanup() {
      long var0 = System.currentTimeMillis();
      SESSIONS.values().removeIf(var2 -> var2.expiresAt < var0);
   }

   public enum Kind {
      MART,
      TRADER,
      TUTOR;
   }

   private static final class Session {
      final UUID entityUuid;
      final int entityId;
      final long token;
      final CommerceSessions.Kind kind;
      long expiresAt;

      Session(UUID var1, int var2, long var3, CommerceSessions.Kind var5, long var6) {
         this.entityUuid = var1;
         this.entityId = var2;
         this.token = var3;
         this.kind = var5;
         this.expiresAt = var6;
      }
   }
}
