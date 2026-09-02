package net.cobbleservertools.client.gui;

import net.cobbleservertools.network.payload.OpenTutorPayload;
import net.cobbleservertools.network.payload.TutorActionPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;

public final class MoveTutorScreen extends Screen {
   private static final int PANEL_WIDTH = 390;
   private static final int PANEL_HEIGHT = 216;
   private final OpenTutorPayload payload;
   private final CompoundTag view;

   public MoveTutorScreen(OpenTutorPayload var1) {
      super(CommerceScreenSupport.title(var1.title(), "cobbleservertools.tutor.title"));
      this.payload = var1;
      this.view = var1.view();
   }

   protected void init() {
      int var1 = Math.min(390, this.width - 20);
      int var2 = (this.width - var1) / 2;
      int var3 = Math.max(8, (this.height - 216) / 2);
      ListTag var4 = this.view.getList("Party", 10);

      for (int var5 = 0; var5 < var4.size(); var5++) {
         CompoundTag var6 = var4.getCompound(var5);
         int var7 = var6.getInt("Slot");
         int var8 = var2 + 18 + var5 % 2 * 178;
         int var9 = var3 + 72 + var5 / 2 * 31;
         String var10 = "#" + (var7 + 1) + " " + var6.getString("Species") + " Lv." + var6.getInt("Level");
         this.addRenderableWidget(Button.builder(Component.literal(var10), var2x -> this.submit(var7)).bounds(var8, var9, 168, 24).build());
      }

      this.addRenderableWidget(
         Button.builder(Component.translatable("gui.done", new Object[0]), var1x -> this.onClose()).bounds(var2 + var1 - 84, var3 + 216 - 28, 68, 20).build()
      );
   }

   private void submit(int var1) {
      PacketDistributor.sendToServer(new TutorActionPayload(this.payload.entityId(), this.payload.sessionToken(), var1), new CustomPacketPayload[0]);
   }

   public void renderBackground(GuiGraphics var1, int var2, int var3, float var4) {
      var1.fill(0, 0, this.width, this.height, 1996488704);
      int var5 = Math.min(390, this.width - 20);
      int var6 = (this.width - var5) / 2;
      int var7 = Math.max(8, (this.height - 216) / 2);
      var1.fill(var6, var7, var6 + var5, var7 + 216, -233103319);
      var1.fill(var6, var7, var6 + var5, var7 + 2, -1653151);
      var1.fill(var6, var7 + 216 - 2, var6 + var5, var7 + 216, -1653151);
   }

   public void render(GuiGraphics var1, int var2, int var3, float var4) {
      super.render(var1, var2, var3, var4);
      int var5 = Math.min(390, this.width - 20);
      int var6 = (this.width - var5) / 2;
      int var7 = Math.max(8, (this.height - 216) / 2);
      var1.drawCenteredString(this.font, this.title, this.width / 2, var7 + 10, -1653151);
      var1.drawString(this.font, Component.translatable("cobbleservertools.tutor.teaching", new Object[0]), var6 + 18, var7 + 37, -6511697, false);
      var1.drawString(this.font, Component.literal(this.view.getString("MoveId")), var6 + 18, var7 + 51, -1, false);
   }

   public boolean isPauseScreen() {
      return false;
   }
}
