package net.cobbleservertools.client.gui;

import net.cobbleservertools.network.payload.OpenTraderPayload;
import net.cobbleservertools.network.payload.TraderActionPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;

public final class TraderScreen extends Screen {
   private static final int PANEL_WIDTH = 390;
   private static final int PANEL_HEIGHT = 226;
   private final OpenTraderPayload payload;
   private final CompoundTag view;

   public TraderScreen(OpenTraderPayload var1) {
      super(CommerceScreenSupport.title(var1.title(), "cobbleservertools.trader.title"));
      this.payload = var1;
      this.view = var1.view();
   }

   protected void init() {
      int var1 = Math.min(390, this.width - 20);
      int var2 = (this.width - var1) / 2;
      int var3 = Math.max(8, (this.height - 226) / 2);
      ListTag var4 = this.view.getList("Party", 10);
      boolean var5 = this.view.getBoolean("AlreadyTraded");

      for (int var6 = 0; var6 < var4.size(); var6++) {
         CompoundTag var7 = var4.getCompound(var6);
         int var8 = var7.getInt("Slot");
         int var9 = var2 + 18 + var6 % 2 * 178;
         int var10 = var3 + 82 + var6 / 2 * 31;
         String var11 = "#" + (var8 + 1) + " " + var7.getString("Species") + " Lv." + var7.getInt("Level");
         Button var12 = Button.builder(Component.literal(var11), var2x -> this.submit(var8)).bounds(var9, var10, 168, 24).build();
         var12.active = !var5;
         this.addRenderableWidget(var12);
      }

      this.addRenderableWidget(
         Button.builder(Component.translatable("gui.done", new Object[0]), var1x -> this.onClose()).bounds(var2 + var1 - 84, var3 + 226 - 28, 68, 20).build()
      );
   }

   private void submit(int var1) {
      PacketDistributor.sendToServer(new TraderActionPayload(this.payload.entityId(), this.payload.sessionToken(), var1), new CustomPacketPayload[0]);
   }

   public void renderBackground(GuiGraphics var1, int var2, int var3, float var4) {
      var1.fill(0, 0, this.width, this.height, 1996488704);
      int var5 = Math.min(390, this.width - 20);
      int var6 = (this.width - var5) / 2;
      int var7 = Math.max(8, (this.height - 226) / 2);
      var1.fill(var6, var7, var6 + var5, var7 + 226, -233103319);
      var1.fill(var6, var7, var6 + var5, var7 + 2, -1653151);
      var1.fill(var6, var7 + 226 - 2, var6 + var5, var7 + 226, -1653151);
   }

   public void render(GuiGraphics var1, int var2, int var3, float var4) {
      super.render(var1, var2, var3, var4);
      int var5 = Math.min(390, this.width - 20);
      int var6 = (this.width - var5) / 2;
      int var7 = Math.max(8, (this.height - 226) / 2);
      var1.drawCenteredString(this.font, this.title, this.width / 2, var7 + 10, -1653151);
      var1.drawString(this.font, Component.translatable("cobbleservertools.trader.looking_for", new Object[0]), var6 + 18, var7 + 35, -6511697, false);
      var1.drawString(this.font, Component.literal(this.view.getString("RequestSpecies")), var6 + 18, var7 + 48, -1, false);
      var1.drawString(this.font, Component.translatable("cobbleservertools.trader.given_cobblemon", new Object[0]), var6 + 205, var7 + 35, -6511697, false);
      var1.drawString(
         this.font, Component.literal(this.view.getString("OfferSpecies") + " Lv." + this.view.getInt("OfferLevel")), var6 + 205, var7 + 48, -1, false
      );
      if (this.view.getBoolean("AlreadyTraded")) {
         var1.drawCenteredString(
            this.font, Component.translatable("cobbleservertools.trader.you_have_already_traded_with_npc", new Object[0]), this.width / 2, var7 + 67, -35210
         );
      }
   }

   public boolean isPauseScreen() {
      return false;
   }
}
