package net.cobbleservertools.client.network;

import net.cobbleservertools.client.gui.FreezeOverlayScreen;
import net.cobbleservertools.client.gui.MartScreen;
import net.cobbleservertools.client.gui.MoveTutorScreen;
import net.cobbleservertools.client.gui.NpcDialogScreen;
import net.cobbleservertools.client.gui.NpcPresetBrowserScreen;
import net.cobbleservertools.client.gui.NpcProfileEditorScreen;
import net.cobbleservertools.client.gui.TraderScreen;
import net.cobbleservertools.client.state.ClientMoneyState;
import net.cobbleservertools.entity.AbstractCobbleNpcEntity;
import net.cobbleservertools.network.ClientPayloadBridge;
import net.cobbleservertools.network.payload.CommerceResultPayload;
import net.cobbleservertools.network.payload.NpcSnapshotPayload;
import net.cobbleservertools.network.payload.OpenMartPayload;
import net.cobbleservertools.network.payload.OpenNpcDialogPayload;
import net.cobbleservertools.network.payload.OpenTraderPayload;
import net.cobbleservertools.network.payload.OpenTutorPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class CobbleServerToolsClientPayloads {
   private CobbleServerToolsClientPayloads() {
   }

   public static void install() {
      ClientPayloadBridge.install(
         CobbleServerToolsClientPayloads::handleSnapshot,
         CobbleServerToolsClientPayloads::handleDialog,
         CobbleServerToolsClientPayloads::handleMart,
         CobbleServerToolsClientPayloads::handleTrader,
         CobbleServerToolsClientPayloads::handleTutor,
         CobbleServerToolsClientPayloads::handleResult
      );
      ClientPayloadBridge.installMoneyHandler(var0 -> ClientMoneyState.set(var0.money()));
      ClientPayloadBridge.installFreezeHandlers(var0 -> {
         Minecraft var1 = Minecraft.getInstance();
         if (!(var1.screen instanceof FreezeOverlayScreen)) {
            var1.setScreen(new FreezeOverlayScreen());
         }
      }, var0 -> {
         Minecraft var1 = Minecraft.getInstance();
         if (var1.screen instanceof FreezeOverlayScreen) {
            var1.setScreen(null);
         }
      });
      ClientPayloadBridge.installPresetHandlers(
         var0 -> Minecraft.getInstance().setScreen(new NpcPresetBrowserScreen(var0)),
         var0 -> Minecraft.getInstance().setScreen(new NpcPresetBrowserScreen(var0))
      );
   }

   private static void handleSnapshot(NpcSnapshotPayload var0) {
      Minecraft var1 = Minecraft.getInstance();
      if (var1.level != null) {
         if (var1.level.getEntity(var0.entityId()) instanceof AbstractCobbleNpcEntity var2) {
            var2.applyClientSnapshot(var0.profile());
            if (var0.editorView()) {
               var1.setScreen(new NpcProfileEditorScreen(var0.entityId(), var0.profile()));
            }
         }
      }
   }

   private static void handleDialog(OpenNpcDialogPayload var0) {
      Minecraft.getInstance().setScreen(new NpcDialogScreen(var0));
   }

   private static void handleMart(OpenMartPayload var0) {
      Minecraft.getInstance().setScreen(new MartScreen(var0));
   }

   private static void handleTrader(OpenTraderPayload var0) {
      Minecraft.getInstance().setScreen(new TraderScreen(var0));
   }

   private static void handleTutor(OpenTutorPayload var0) {
      Minecraft.getInstance().setScreen(new MoveTutorScreen(var0));
   }

   private static void handleResult(CommerceResultPayload var0) {
      Minecraft var1 = Minecraft.getInstance();
      if (var1.player != null) {
         var1.player
            .displayClientMessage(
               Component.translatable(var0.translationKey(), new Object[0]).withStyle(var0.success() ? ChatFormatting.GREEN : ChatFormatting.RED), false
            );
      }
   }
}
