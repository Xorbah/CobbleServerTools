package net.cobbleservertools.client.gui;

import net.cobbleservertools.network.payload.FreezeOverlayTimeoutPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;

public final class FreezeOverlayScreen extends Screen {
   public static final int TIMEOUT_TICKS = 160;
   private int ticks;

   public FreezeOverlayScreen() {
      super(Component.empty());
   }

   public void tick() {
      if (++this.ticks >= 160) {
         PacketDistributor.sendToServer(new FreezeOverlayTimeoutPayload(), new CustomPacketPayload[0]);
         if (this.minecraft != null && this.minecraft.screen == this) {
            this.minecraft.setScreen(null);
         }
      }
   }

   public void render(GuiGraphics var1, int var2, int var3, float var4) {
   }

   public void renderBackground(GuiGraphics var1, int var2, int var3, float var4) {
   }

   public void onClose() {
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   public boolean isPauseScreen() {
      return false;
   }

   public boolean keyPressed(int var1, int var2, int var3) {
      return true;
   }

   public boolean keyReleased(int var1, int var2, int var3) {
      return true;
   }

   public boolean charTyped(char var1, int var2) {
      return true;
   }

   public boolean mouseClicked(double var1, double var3, int var5) {
      return true;
   }

   public boolean mouseReleased(double var1, double var3, int var5) {
      return true;
   }

   public boolean mouseDragged(double var1, double var3, int var5, double var6, double var8) {
      return true;
   }

   public boolean mouseScrolled(double var1, double var3, double var5, double var7) {
      return true;
   }
}
