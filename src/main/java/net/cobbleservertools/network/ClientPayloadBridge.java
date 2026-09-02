package net.cobbleservertools.network;

import java.util.Objects;
import java.util.function.Consumer;
import net.cobbleservertools.network.payload.CloseFreezeOverlayPayload;
import net.cobbleservertools.network.payload.CommerceResultPayload;
import net.cobbleservertools.network.payload.NpcPresetSearchResultsPayload;
import net.cobbleservertools.network.payload.NpcSnapshotPayload;
import net.cobbleservertools.network.payload.OpenFreezeOverlayPayload;
import net.cobbleservertools.network.payload.OpenMartPayload;
import net.cobbleservertools.network.payload.OpenNpcDialogPayload;
import net.cobbleservertools.network.payload.OpenNpcPresetBrowserPayload;
import net.cobbleservertools.network.payload.OpenTraderPayload;
import net.cobbleservertools.network.payload.OpenTutorPayload;
import net.cobbleservertools.network.payload.SyncPlayerMoneyPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ClientPayloadBridge {
   private static volatile Consumer<NpcSnapshotPayload> snapshotHandler = var0 -> {};
   private static volatile Consumer<OpenNpcDialogPayload> dialogHandler = var0 -> {};
   private static volatile Consumer<OpenMartPayload> martHandler = var0 -> {};
   private static volatile Consumer<OpenTraderPayload> traderHandler = var0 -> {};
   private static volatile Consumer<OpenTutorPayload> tutorHandler = var0 -> {};
   private static volatile Consumer<CommerceResultPayload> resultHandler = var0 -> {};
   private static volatile Consumer<OpenFreezeOverlayPayload> freezeOpenHandler = var0 -> {};
   private static volatile Consumer<CloseFreezeOverlayPayload> freezeCloseHandler = var0 -> {};
   private static volatile Consumer<SyncPlayerMoneyPayload> moneyHandler = var0 -> {};
   private static volatile Consumer<OpenNpcPresetBrowserPayload> presetOpenHandler = var0 -> {};
   private static volatile Consumer<NpcPresetSearchResultsPayload> presetResultsHandler = var0 -> {};

   private ClientPayloadBridge() {
   }

   public static void install(
      Consumer<NpcSnapshotPayload> var0,
      Consumer<OpenNpcDialogPayload> var1,
      Consumer<OpenMartPayload> var2,
      Consumer<OpenTraderPayload> var3,
      Consumer<OpenTutorPayload> var4,
      Consumer<CommerceResultPayload> var5
   ) {
      snapshotHandler = Objects.requireNonNull(var0, "snapshotConsumer");
      dialogHandler = Objects.requireNonNull(var1, "dialogConsumer");
      martHandler = Objects.requireNonNull(var2, "martConsumer");
      traderHandler = Objects.requireNonNull(var3, "traderConsumer");
      tutorHandler = Objects.requireNonNull(var4, "tutorConsumer");
      resultHandler = Objects.requireNonNull(var5, "resultConsumer");
   }

   public static void installFreezeHandlers(Consumer<OpenFreezeOverlayPayload> var0, Consumer<CloseFreezeOverlayPayload> var1) {
      freezeOpenHandler = Objects.requireNonNull(var0, "open");
      freezeCloseHandler = Objects.requireNonNull(var1, "close");
   }

   public static void installMoneyHandler(Consumer<SyncPlayerMoneyPayload> var0) {
      moneyHandler = Objects.requireNonNull(var0, "money");
   }

   public static void installPresetHandlers(Consumer<OpenNpcPresetBrowserPayload> var0, Consumer<NpcPresetSearchResultsPayload> var1) {
      presetOpenHandler = Objects.requireNonNull(var0, "open");
      presetResultsHandler = Objects.requireNonNull(var1, "results");
   }

   public static void handleSnapshot(NpcSnapshotPayload var0, IPayloadContext var1) {
      var1.enqueueWork(() -> snapshotHandler.accept(var0));
   }

   public static void handleDialog(OpenNpcDialogPayload var0, IPayloadContext var1) {
      var1.enqueueWork(() -> dialogHandler.accept(var0));
   }

   public static void handleMart(OpenMartPayload var0, IPayloadContext var1) {
      var1.enqueueWork(() -> martHandler.accept(var0));
   }

   public static void handleTrader(OpenTraderPayload var0, IPayloadContext var1) {
      var1.enqueueWork(() -> traderHandler.accept(var0));
   }

   public static void handleTutor(OpenTutorPayload var0, IPayloadContext var1) {
      var1.enqueueWork(() -> tutorHandler.accept(var0));
   }

   public static void handleResult(CommerceResultPayload var0, IPayloadContext var1) {
      var1.enqueueWork(() -> resultHandler.accept(var0));
   }

   public static void handleFreezeOpen(OpenFreezeOverlayPayload var0, IPayloadContext var1) {
      var1.enqueueWork(() -> freezeOpenHandler.accept(var0));
   }

   public static void handleFreezeClose(CloseFreezeOverlayPayload var0, IPayloadContext var1) {
      var1.enqueueWork(() -> freezeCloseHandler.accept(var0));
   }

   public static void handleMoney(SyncPlayerMoneyPayload var0, IPayloadContext var1) {
      var1.enqueueWork(() -> moneyHandler.accept(var0));
   }

   public static void handlePresetOpen(OpenNpcPresetBrowserPayload var0, IPayloadContext var1) {
      var1.enqueueWork(() -> presetOpenHandler.accept(var0));
   }

   public static void handlePresetResults(NpcPresetSearchResultsPayload var0, IPayloadContext var1) {
      var1.enqueueWork(() -> presetResultsHandler.accept(var0));
   }
}
