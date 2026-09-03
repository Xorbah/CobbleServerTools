package net.cobbleservertools.network;

import net.cobbleservertools.CobbleServerTools;
import net.cobbleservertools.battle.protection.NpcDialogBattleProtection;
import net.cobbleservertools.battle.protection.NpcPlayerFreezeService;
import net.cobbleservertools.commerce.CommerceSessions;
import net.cobbleservertools.commerce.MartService;
import net.cobbleservertools.commerce.MoveTutorService;
import net.cobbleservertools.commerce.TraderService;
import net.cobbleservertools.config.NpcPresetLibrary;
import net.cobbleservertools.dialog.NpcDialogSessions;
import net.cobbleservertools.entity.AbstractBattleNpcEntity;
import net.cobbleservertools.entity.AbstractCobbleNpcEntity;
import net.cobbleservertools.entity.MartNpcEntity;
import net.cobbleservertools.entity.MoveTutorNpcEntity;
import net.cobbleservertools.entity.TraderNpcEntity;
import net.cobbleservertools.network.payload.ApplyNpcPresetPayload;
import net.cobbleservertools.network.payload.BattleDialogProtectionPayload;
import net.cobbleservertools.network.payload.CloseFreezeOverlayPayload;
import net.cobbleservertools.network.payload.CloseNpcDialogPayload;
import net.cobbleservertools.network.payload.CommerceResultPayload;
import net.cobbleservertools.network.payload.FreezeOverlayTimeoutPayload;
import net.cobbleservertools.network.payload.MartActionPayload;
import net.cobbleservertools.network.payload.NpcDialogChoicePayload;
import net.cobbleservertools.network.payload.NpcPresetSearchRequestPayload;
import net.cobbleservertools.network.payload.NpcPresetSearchResultsPayload;
import net.cobbleservertools.network.payload.NpcSnapshotPayload;
import net.cobbleservertools.network.payload.OpenFreezeOverlayPayload;
import net.cobbleservertools.network.payload.OpenMartPayload;
import net.cobbleservertools.network.payload.OpenNpcDialogPayload;
import net.cobbleservertools.network.payload.OpenNpcPresetBrowserPayload;
import net.cobbleservertools.network.payload.OpenTraderPayload;
import net.cobbleservertools.network.payload.OpenTutorPayload;
import net.cobbleservertools.network.payload.RequestNpcSnapshotPayload;
import net.cobbleservertools.network.payload.SyncPlayerMoneyPayload;
import net.cobbleservertools.network.payload.TraderActionPayload;
import net.cobbleservertools.network.payload.TutorActionPayload;
import net.cobbleservertools.network.payload.UpdateNpcProfilePayload;
import net.cobbleservertools.network.payload.RoamingPresetListPayload;
import net.cobbleservertools.roaming.RoamingPokemonManager;
import net.cobbleservertools.util.NpcPermissions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class CobbleServerToolsNetworking {
   public static final String PROTOCOL_VERSION = "7";
   private static final double MAX_EDITOR_DISTANCE_SQUARED = 4096.0;

   private CobbleServerToolsNetworking() {
   }

   public static void registerPayloads(RegisterPayloadHandlersEvent var0) {
      PayloadRegistrar var1 = var0.registrar("7");
      var1.playToClient(NpcSnapshotPayload.TYPE, NpcSnapshotPayload.STREAM_CODEC, ClientPayloadBridge::handleSnapshot);
      var1.playToClient(OpenNpcDialogPayload.TYPE, OpenNpcDialogPayload.STREAM_CODEC, ClientPayloadBridge::handleDialog);
      var1.playToClient(OpenMartPayload.TYPE, OpenMartPayload.STREAM_CODEC, ClientPayloadBridge::handleMart);
      var1.playToClient(OpenTraderPayload.TYPE, OpenTraderPayload.STREAM_CODEC, ClientPayloadBridge::handleTrader);
      var1.playToClient(OpenTutorPayload.TYPE, OpenTutorPayload.STREAM_CODEC, ClientPayloadBridge::handleTutor);
      var1.playToClient(CommerceResultPayload.TYPE, CommerceResultPayload.STREAM_CODEC, ClientPayloadBridge::handleResult);
      var1.playToClient(OpenFreezeOverlayPayload.TYPE, OpenFreezeOverlayPayload.STREAM_CODEC, ClientPayloadBridge::handleFreezeOpen);
      var1.playToClient(CloseFreezeOverlayPayload.TYPE, CloseFreezeOverlayPayload.STREAM_CODEC, ClientPayloadBridge::handleFreezeClose);
      var1.playToClient(SyncPlayerMoneyPayload.TYPE, SyncPlayerMoneyPayload.STREAM_CODEC, ClientPayloadBridge::handleMoney);
      var1.playToClient(OpenNpcPresetBrowserPayload.TYPE, OpenNpcPresetBrowserPayload.STREAM_CODEC, ClientPayloadBridge::handlePresetOpen);
      var1.playToClient(NpcPresetSearchResultsPayload.TYPE, NpcPresetSearchResultsPayload.STREAM_CODEC, ClientPayloadBridge::handlePresetResults);
      var1.playToClient(RoamingPresetListPayload.TYPE, RoamingPresetListPayload.STREAM_CODEC, ClientPayloadBridge::handleRoamingPresets);
      var1.playToServer(RequestNpcSnapshotPayload.TYPE, RequestNpcSnapshotPayload.STREAM_CODEC, CobbleServerToolsNetworking::handleSnapshotRequest);
      var1.playToServer(UpdateNpcProfilePayload.TYPE, UpdateNpcProfilePayload.STREAM_CODEC, CobbleServerToolsNetworking::handleProfileUpdate);
      var1.playToServer(NpcDialogChoicePayload.TYPE, NpcDialogChoicePayload.STREAM_CODEC, CobbleServerToolsNetworking::handleDialogChoice);
      var1.playToServer(CloseNpcDialogPayload.TYPE, CloseNpcDialogPayload.STREAM_CODEC, CobbleServerToolsNetworking::handleDialogClose);
      var1.playToServer(MartActionPayload.TYPE, MartActionPayload.STREAM_CODEC, CobbleServerToolsNetworking::handleMartAction);
      var1.playToServer(TraderActionPayload.TYPE, TraderActionPayload.STREAM_CODEC, CobbleServerToolsNetworking::handleTraderAction);
      var1.playToServer(TutorActionPayload.TYPE, TutorActionPayload.STREAM_CODEC, CobbleServerToolsNetworking::handleTutorAction);
      var1.playToServer(FreezeOverlayTimeoutPayload.TYPE, FreezeOverlayTimeoutPayload.STREAM_CODEC, CobbleServerToolsNetworking::handleFreezeTimeout);
      var1.playToServer(BattleDialogProtectionPayload.TYPE, BattleDialogProtectionPayload.STREAM_CODEC, CobbleServerToolsNetworking::handleBattleDialogProtection);
      var1.playToServer(NpcPresetSearchRequestPayload.TYPE, NpcPresetSearchRequestPayload.STREAM_CODEC, CobbleServerToolsNetworking::handlePresetSearch);
      var1.playToServer(ApplyNpcPresetPayload.TYPE, ApplyNpcPresetPayload.STREAM_CODEC, CobbleServerToolsNetworking::handlePresetApply);
      CobbleServerTools.LOGGER.info("Registered phase-5 release-candidate profile, dialogue, battle, and commerce payloads", new Object[0]);
   }

   public static void syncMoney(ServerPlayer var0, int var1) {
      PacketDistributor.sendToPlayer(var0, new SyncPlayerMoneyPayload(Math.max(0, var1)), new CustomPacketPayload[0]);
   }

   public static void openFreezeOverlay(ServerPlayer var0) {
      PacketDistributor.sendToPlayer(var0, new OpenFreezeOverlayPayload(), new CustomPacketPayload[0]);
   }

   public static void closeFreezeOverlay(ServerPlayer var0) {
      PacketDistributor.sendToPlayer(var0, new CloseFreezeOverlayPayload(), new CustomPacketPayload[0]);
   }

   public static void sendSnapshot(ServerPlayer var0, AbstractCobbleNpcEntity var1, boolean var2) {
      PacketDistributor.sendToPlayer(
         var0, new NpcSnapshotPayload(var1.getId(), var2, var2 ? var1.createEditorSnapshot() : var1.createVisibleSnapshot()), new CustomPacketPayload[0]
      );
   }

   public static void broadcastVisibleSnapshot(AbstractCobbleNpcEntity var0) {
      PacketDistributor.sendToPlayersTrackingEntityAndSelf(
         var0, new NpcSnapshotPayload(var0.getId(), false, var0.createVisibleSnapshot()), new CustomPacketPayload[0]
      );
   }

   public static void openPresetBrowser(ServerPlayer var0, AbstractCobbleNpcEntity var1) {
      if (NpcPermissions.canModify(var0) && var1 != null && !var1.isRemoved()) {
         String var2 = browserTypeFor(var1);
         NpcPresetLibrary.ensureLoaded();
         PacketDistributor.sendToPlayer(var0, new OpenNpcPresetBrowserPayload(var1.getId(), var2, var1.presetId()), new CustomPacketPayload[0]);
      }
   }

   private static void handleSnapshotRequest(RequestNpcSnapshotPayload var0, IPayloadContext var1) {
      var1.enqueueWork(() -> {
         if (var1.player() instanceof ServerPlayer var2) {
            AbstractCobbleNpcEntity var4 = findAccessibleNpc(var2, var0.entityId());
            if (var4 != null) {
               sendSnapshot(var2, var4, NpcPermissions.canModify(var2));
            }
         }
      });
   }

   private static void handleProfileUpdate(UpdateNpcProfilePayload var0, IPayloadContext var1) {
      RoamingPokemonManager.tryHandleProfileUpdate(var1, var0);
      var1.enqueueWork(() -> {
         if (var1.player() instanceof ServerPlayer var2 && NpcPermissions.canModify(var2)) {
            AbstractCobbleNpcEntity var4 = findAccessibleNpc(var2, var0.entityId());
            if (var4 != null) {
               var4.applyOperatorUpdate(var0.profile());
               var4.markManualConfigured();
               broadcastVisibleSnapshot(var4);
               sendSnapshot(var2, var4, false);
            }
         } else {
            CobbleServerTools.LOGGER.warn("Rejected unauthorized NPC profile update from {}", new Object[]{var1.player().getScoreboardName()});
         }
      });
   }

   private static void handleDialogChoice(NpcDialogChoicePayload var0, IPayloadContext var1) {
      var1.enqueueWork(() -> {
         if (var1.player() instanceof ServerPlayer var2) {
            NpcDialogSessions.choose(var2, var0.entityId(), var0.sessionToken(), var0.accepted());
         }
      });
   }

   private static void handleDialogClose(CloseNpcDialogPayload var0, IPayloadContext var1) {
      var1.enqueueWork(() -> {
         if (var1.player() instanceof ServerPlayer var2) {
            NpcDialogSessions.close(var2, var0.entityId(), var0.sessionToken(), var0.completed());
         }
      });
   }

   private static void handleMartAction(MartActionPayload var0, IPayloadContext var1) {
      var1.enqueueWork(
         () -> {
            if (var1.player() instanceof ServerPlayer var2) {
               MartNpcEntity var5 = CommerceSessions.validate(var2, var0.entityId(), var0.sessionToken(), CommerceSessions.Kind.MART, MartNpcEntity.class);
               if (var5 == null) {
                  sendResult(var2, false, "message.cobbleservertools.commerce.session_expired");
               } else {
                  MartService.Result var4 = var0.action() == MartActionPayload.Action.SELL
                     ? MartService.sell(var2, var5, var0.target(), var0.quantity())
                     : MartService.buy(var2, var5, var0.target(), var0.quantity());
                  if (var4.literal()) {
                     var2.displayClientMessage(Component.literal(var4.translationKey()), false);
                  } else {
                     sendResult(var2, var4.success(), var4.translationKey());
                  }

                  CommerceSessions.openMart(var2, var5);
               }
            }
         }
      );
   }

   private static void handleTraderAction(TraderActionPayload var0, IPayloadContext var1) {
      var1.enqueueWork(() -> {
         if (var1.player() instanceof ServerPlayer var2) {
            TraderNpcEntity var5 = CommerceSessions.validate(var2, var0.entityId(), var0.sessionToken(), CommerceSessions.Kind.TRADER, TraderNpcEntity.class);
            if (var5 == null) {
               sendResult(var2, false, "message.cobbleservertools.commerce.session_expired");
            } else {
               TraderService.Result var4 = TraderService.trade(var2, var5, var0.partySlot());
               sendResult(var2, var4.success(), var4.translationKey());
               CommerceSessions.openTrader(var2, var5);
            }
         }
      });
   }

   private static void handleTutorAction(TutorActionPayload var0, IPayloadContext var1) {
      var1.enqueueWork(
         () -> {
            if (var1.player() instanceof ServerPlayer var2) {
               MoveTutorNpcEntity var5 = CommerceSessions.validate(
                  var2, var0.entityId(), var0.sessionToken(), CommerceSessions.Kind.TUTOR, MoveTutorNpcEntity.class
               );
               if (var5 == null) {
                  sendResult(var2, false, "message.cobbleservertools.commerce.session_expired");
               } else {
                  MoveTutorService.Result var4 = MoveTutorService.teach(var2, var5, var0.partySlot());
                  sendResult(var2, var4.success(), var4.translationKey());
                  CommerceSessions.openTutor(var2, var5);
               }
            }
         }
      );
   }

   private static void handleFreezeTimeout(FreezeOverlayTimeoutPayload var0, IPayloadContext var1) {
      var1.enqueueWork(
         () -> {
            if (var1.player() instanceof ServerPlayer var1x) {
               NpcPlayerFreezeService.unfreeze(var1x);
               NpcDialogBattleProtection.clear(var1x);
               closeFreezeOverlay(var1x);

               for (AbstractBattleNpcEntity var3 : var1x.serverLevel()
                  .getEntitiesOfClass(AbstractBattleNpcEntity.class, var1x.getBoundingBox().inflate(64.0), var0xx -> true)) {
                  var3.onFreezeOverlayTimeout(var1x);
               }
            }
         }
      );
   }

   private static void handleBattleDialogProtection(BattleDialogProtectionPayload var0, IPayloadContext var1) {
      var1.enqueueWork(() -> {
         if (var1.player() instanceof ServerPlayer var2) {
            if (var2.serverLevel().getEntity(var0.npcUuid()) instanceof AbstractBattleNpcEntity var4 && !(var2.distanceToSqr(var4) > 4096.0)) {
               switch (var0.action()) {
                  case 0:
                  case 1:
                     NpcDialogBattleProtection.touchDialog(var2);
                     break;
                  case 2:
                     NpcDialogBattleProtection.endDialog(var2);
               }
            }
         }
      });
   }

   private static void handlePresetSearch(NpcPresetSearchRequestPayload var0, IPayloadContext var1) {
      var1.enqueueWork(
         () -> {
            if (var1.player() instanceof ServerPlayer var2 && NpcPermissions.canModify(var2)) {
               AbstractCobbleNpcEntity var8 = findAccessibleNpc(var2, var0.entityId());
               if (var8 != null) {
                  String var4 = browserTypeFor(var8);
                  if (var0.reload()) {
                     NpcPresetLibrary.reload();
                  }

                  NpcPresetLibrary.SearchQuery var5 = new NpcPresetLibrary.SearchQuery(
                     var4, var0.query(), var0.region(), var0.role(), var0.location(), var0.trainerClass(), var0.tag(), var0.page()
                  );
                  NpcPresetLibrary.SearchResult var6 = NpcPresetLibrary.search(var5);
                  CompoundTag var7 = NpcPresetLibrary.searchResultTag(var6);
                  var7.putString("CurrentPresetId", var8.presetId());
                  PacketDistributor.sendToPlayer(
                     var2,
                     new NpcPresetSearchResultsPayload(
                        var8.getId(),
                        var4,
                        var5.normalized().query(),
                        var5.normalized().region(),
                        var5.normalized().role(),
                        var5.normalized().location(),
                        var5.normalized().trainerClass(),
                        var5.normalized().tag(),
                        var7
                     ),
                     new CustomPacketPayload[0]
                  );
               }
            }
         }
      );
   }

   private static void handlePresetApply(ApplyNpcPresetPayload var0, IPayloadContext var1) {
      var1.enqueueWork(() -> {
         if (var1.player() instanceof ServerPlayer var2 && NpcPermissions.canModify(var2)) {
            AbstractCobbleNpcEntity var6 = findAccessibleNpc(var2, var0.entityId());
            if (var6 != null) {
               String var4 = browserTypeFor(var6);
               NpcPresetLibrary.Preset var5 = NpcPresetLibrary.find(var0.presetId());
               if (var5 != null && var5.npcType().equals(var4)) {
                  var6.applyPresetConfiguration(var5.configuration(), var5.id());
                  broadcastVisibleSnapshot(var6);
                  var2.displayClientMessage(Component.literal("Applied NPC preset: " + var5.displayName()), true);
               } else {
                  var2.displayClientMessage(Component.literal("Preset is missing or does not match this NPC type."), true);
               }
            }
         }
      });
   }

   private static String browserTypeFor(AbstractCobbleNpcEntity var0) {
      return var0.getTags().contains("cobbleservertools_vending_backing") ? "vending" : var0.npcKind().serializedName();
   }

   private static void sendResult(ServerPlayer var0, boolean var1, String var2) {
      PacketDistributor.sendToPlayer(var0, new CommerceResultPayload(var1, var2), new CustomPacketPayload[0]);
   }

   private static AbstractCobbleNpcEntity findAccessibleNpc(ServerPlayer var0, int var1) {
      if (!(var0.serverLevel().getEntity(var1) instanceof AbstractCobbleNpcEntity var3 && !var3.isRemoved())) {
         return null;
      } else {
         return var0.distanceToSqr(var3) > 4096.0 ? null : var3;
      }
   }
}
