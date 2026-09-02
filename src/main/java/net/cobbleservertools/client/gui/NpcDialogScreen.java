package net.cobbleservertools.client.gui;

import java.util.ArrayList;
import java.util.List;
import net.cobbleservertools.client.protection.ClientNpcDialogBattleProtection;
import net.cobbleservertools.network.payload.CloseNpcDialogPayload;
import net.cobbleservertools.network.payload.NpcDialogChoicePayload;
import net.cobbleservertools.network.payload.OpenNpcDialogPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;

public final class NpcDialogScreen extends Screen {
   private static final int PANEL_WIDTH = 330;
   private static final int PANEL_HEIGHT = 150;
   private static final int LINE_HEIGHT = 11;
   private static final int LINES_PER_PAGE = 8;
   private final OpenNpcDialogPayload payload;
   private final List<FormattedCharSequence> wrapped = new ArrayList<>();
   private int page;
   private Button nextButton;
   private Button yesButton;
   private Button noButton;
   private Button closeButton;
   private boolean waitingForServer;
   private boolean notifiedClose;
   private final ClientNpcDialogBattleProtection battleProtection = new ClientNpcDialogBattleProtection();

   public NpcDialogScreen(OpenNpcDialogPayload var1) {
      super(Component.literal(var1.title()));
      this.payload = var1;
   }

   protected void init() {
      this.battleProtection.openForEntity(this.payload.entityId());
      this.wrapped.clear();
      int var1 = Math.min(302, this.width - 50);

      for (String var3 : this.payload.lines()) {
         List var4 = this.font.split(Component.literal(var3), var1);
         if (var4.isEmpty()) {
            this.wrapped.add(FormattedCharSequence.EMPTY);
         } else {
            this.wrapped.addAll(var4);
         }
      }

      if (this.wrapped.isEmpty()) {
         this.wrapped.add(FormattedCharSequence.EMPTY);
      }

      int var5 = (this.width - Math.min(330, this.width - 20)) / 2;
      int var6 = this.height - 150 - 18;
      int var7 = Math.min(330, this.width - 20);
      this.nextButton = (Button)this.addRenderableWidget(
         Button.builder(Component.translatable("gui.cobbleservertools.next", new Object[0]), var1x -> this.nextPage())
            .bounds(var5 + var7 - 78, var6 + 150 - 27, 66, 20)
            .build()
      );
      this.yesButton = (Button)this.addRenderableWidget(
         Button.builder(Component.translatable("gui.yes", new Object[0]), var1x -> this.answer(true))
            .bounds(var5 + var7 / 2 - 72, var6 + 150 - 27, 66, 20)
            .build()
      );
      this.noButton = (Button)this.addRenderableWidget(
         Button.builder(Component.translatable("gui.no", new Object[0]), var1x -> this.answer(false))
            .bounds(var5 + var7 / 2 + 6, var6 + 150 - 27, 66, 20)
            .build()
      );
      this.closeButton = (Button)this.addRenderableWidget(
         Button.builder(Component.translatable("gui.done", new Object[0]), var1x -> this.finish()).bounds(var5 + var7 - 78, var6 + 150 - 27, 66, 20).build()
      );
      this.refreshButtons();
   }

   private void nextPage() {
      int var1 = this.pageCount();
      if (this.page + 1 < var1) {
         this.page++;
      }

      this.refreshButtons();
   }

   private void answer(boolean var1) {
      if (!this.waitingForServer) {
         this.waitingForServer = true;
         this.refreshButtons();
         PacketDistributor.sendToServer(new NpcDialogChoicePayload(this.payload.entityId(), this.payload.sessionToken(), var1), new CustomPacketPayload[0]);
      }
   }

   private void finish() {
      this.notifyClose(true);
      if (this.minecraft != null) {
         this.minecraft.setScreen(null);
      }
   }

   private void refreshButtons() {
      if (this.nextButton != null) {
         boolean var1 = this.page + 1 >= this.pageCount();
         this.nextButton.visible = !var1;
         this.nextButton.active = !this.waitingForServer;
         this.yesButton.visible = var1 && this.payload.choices();
         this.noButton.visible = var1 && this.payload.choices();
         this.yesButton.active = !this.waitingForServer;
         this.noButton.active = !this.waitingForServer;
         this.closeButton.visible = var1 && !this.payload.choices();
         this.closeButton.active = !this.waitingForServer;
      }
   }

   private int pageCount() {
      return Math.max(1, (this.wrapped.size() + 8 - 1) / 8);
   }

   public void tick() {
      super.tick();
      this.battleProtection.tick();
   }

   public void renderBackground(GuiGraphics var1, int var2, int var3, float var4) {
      var1.fill(0, 0, this.width, this.height, 1426063360);
      int var5 = Math.min(330, this.width - 20);
      int var6 = (this.width - var5) / 2;
      int var7 = this.height - 150 - 18;
      var1.fill(var6, var7, var6 + var5, var7 + 150, -300541148);
      var1.fill(var6, var7, var6 + var5, var7 + 2, -1653151);
      var1.fill(var6, var7 + 150 - 2, var6 + var5, var7 + 150, -1653151);
      var1.fill(var6, var7, var6 + 2, var7 + 150, -1653151);
      var1.fill(var6 + var5 - 2, var7, var6 + var5, var7 + 150, -1653151);
   }

   public void render(GuiGraphics var1, int var2, int var3, float var4) {
      super.render(var1, var2, var3, var4);
      int var5 = Math.min(330, this.width - 20);
      int var6 = (this.width - var5) / 2;
      int var7 = this.height - 150 - 18;
      var1.drawString(this.font, this.title, var6 + 14, var7 + 11, -1653151, false);
      int var8 = this.page * 8;
      int var9 = Math.min(var8 + 8, this.wrapped.size());
      int var10 = var7 + 29;

      for (int var11 = var8; var11 < var9; var11++) {
         var1.drawString(this.font, this.wrapped.get(var11), var6 + 14, var10, -1, false);
         var10 += 11;
      }

      var1.drawString(this.font, Component.literal(this.page + 1 + "/" + this.pageCount()), var6 + 14, var7 + 150 - 22, -6511697, false);
   }

   public void onClose() {
      this.notifyClose(false);
      super.onClose();
   }

   private void notifyClose(boolean var1) {
      if (!this.notifiedClose) {
         this.notifiedClose = true;
         this.battleProtection.close();
         PacketDistributor.sendToServer(new CloseNpcDialogPayload(this.payload.entityId(), this.payload.sessionToken(), var1), new CustomPacketPayload[0]);
      }
   }

   public boolean isPauseScreen() {
      return false;
   }
}
