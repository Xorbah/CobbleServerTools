package net.cobbleservertools.client.gui;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.cobbleservertools.network.payload.MartActionPayload;
import net.cobbleservertools.network.payload.OpenMartPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;

public final class MartScreen extends Screen {
   private static final int PANEL_WIDTH = 420;
   private static final int PANEL_HEIGHT = 244;
   private static final int ROWS_PER_PAGE = 6;
   private static final long RESUME_TTL_MILLIS = 5000L;
   private static final Map<Integer, MartScreen.ResumeState> RESUME = new ConcurrentHashMap<>();
   private final OpenMartPayload payload;
   private final CompoundTag view;
   private final MartScreen.Mode mode;
   private final int page;
   private final int quantity;
   private final boolean returningToMenu;
   private int goodbyeTicks;
   private static final ThreadLocal<MartScreen.ResumeState> PENDING = new ThreadLocal<>();

   public MartScreen(OpenMartPayload var1) {
      this(var1, initialMode(var1), initialPage(var1), initialQuantity(var1), false);
   }

   private MartScreen(OpenMartPayload var1, MartScreen.Mode var2, int var3, int var4, boolean var5) {
      super(CommerceScreenSupport.title(var1.title(), "screen.cobbleservertools.poke_shop"));
      this.payload = var1;
      this.view = var1.view();
      this.mode = var2;
      this.page = Math.max(0, var3);
      this.quantity = Math.max(1, Math.min(64, var4));
      this.returningToMenu = var5;
   }

   private static MartScreen.Mode initialMode(OpenMartPayload var0) {
      MartScreen.ResumeState var1 = RESUME.remove(var0.entityId());
      if (var1 != null && System.currentTimeMillis() - var1.createdAt <= 5000L) {
         PENDING.set(var1);
         return var1.mode;
      } else {
         return MartScreen.Mode.MENU;
      }
   }

   private static int initialPage(OpenMartPayload var0) {
      MartScreen.ResumeState var1 = PENDING.get();
      return var1 == null ? 0 : var1.page;
   }

   private static int initialQuantity(OpenMartPayload var0) {
      MartScreen.ResumeState var1 = PENDING.get();
      PENDING.remove();
      return var1 == null ? 1 : var1.quantity;
   }

   protected void init() {
      if (this.mode == MartScreen.Mode.MENU) {
         this.initMenu();
      } else if (this.mode != MartScreen.Mode.GOODBYE) {
         this.initInventory();
      }
   }

   private void initMenu() {
      int var1 = Math.min(390, this.width - 24);
      int var2 = (this.width - var1) / 2;
      int var3 = Math.max(16, (this.height - 150) / 2);
      int var4 = var2 + var1 - 104;
      int var5 = var3 + 18;
      this.addRenderableWidget(
         Button.builder(Component.literal("BUY"), var1x -> this.switchTo(MartScreen.Mode.BUY, 0, this.quantity, false)).bounds(var4, var5, 88, 20).build()
      );
      this.addRenderableWidget(
         Button.builder(Component.literal("SELL"), var1x -> this.switchTo(MartScreen.Mode.SELL, 0, this.quantity, false))
            .bounds(var4, var5 + 26, 88, 20)
            .build()
      );
      this.addRenderableWidget(Button.builder(Component.literal("LEAVE"), var1x -> this.sayGoodbye()).bounds(var4, var5 + 52, 88, 20).build());
   }

   private void initInventory() {
      int var1 = Math.min(420, this.width - 20);
      int var2 = (this.width - var1) / 2;
      int var3 = Math.max(8, (this.height - 244) / 2);
      this.addRenderableWidget(
         Button.builder(Component.literal("-"), var1x -> this.switchTo(this.mode, this.page, this.quantity - 1, false))
            .bounds(var2 + var1 - 126, var3 + 30, 24, 20)
            .build()
      );
      this.addRenderableWidget(
         Button.builder(Component.literal("+"), var1x -> this.switchTo(this.mode, this.page, this.quantity + 1, false))
            .bounds(var2 + var1 - 54, var3 + 30, 24, 20)
            .build()
      );
      ListTag var4 = this.rows();
      int var5 = this.page * 6;
      int var6 = Math.min(var4.size(), var5 + 6);

      for (int var7 = var5; var7 < var6; var7++) {
         CompoundTag var8 = var4.getCompound(var7);
         int var9 = var3 + 61 + (var7 - var5) * 25;
         int var10 = this.mode == MartScreen.Mode.BUY ? var7 : var8.getInt("Slot");
         MutableComponent var11 = Component.literal(this.mode == MartScreen.Mode.BUY ? "BUY" : "SELL");
         this.addRenderableWidget(Button.builder(var11, var2x -> this.submit(var10)).bounds(var2 + var1 - 70, var9, 54, 20).build());
      }

      if (this.page > 0) {
         this.addRenderableWidget(
            Button.builder(Component.literal("<"), var1x -> this.switchTo(this.mode, this.page - 1, this.quantity, false))
               .bounds(var2 + 14, var3 + 244 - 28, 32, 20)
               .build()
         );
      }

      if ((this.page + 1) * 6 < var4.size()) {
         this.addRenderableWidget(
            Button.builder(Component.literal(">"), var1x -> this.switchTo(this.mode, this.page + 1, this.quantity, false))
               .bounds(var2 + 50, var3 + 244 - 28, 32, 20)
               .build()
         );
      }

      this.addRenderableWidget(
         Button.builder(Component.literal("BACK"), var1x -> this.switchTo(MartScreen.Mode.MENU, 0, this.quantity, true))
            .bounds(var2 + var1 - 86, var3 + 244 - 28, 70, 20)
            .build()
      );
   }

   private void submit(int var1) {
      RESUME.put(this.payload.entityId(), new MartScreen.ResumeState(this.mode, this.page, this.quantity, System.currentTimeMillis()));
      PacketDistributor.sendToServer(
         new MartActionPayload(
            this.payload.entityId(),
            this.payload.sessionToken(),
            this.mode == MartScreen.Mode.BUY ? MartActionPayload.Action.BUY : MartActionPayload.Action.SELL,
            var1,
            this.quantity
         ),
         new CustomPacketPayload[0]
      );
   }

   private void switchTo(MartScreen.Mode var1, int var2, int var3, boolean var4) {
      if (this.minecraft != null) {
         this.minecraft.setScreen(new MartScreen(this.payload, var1, Math.max(0, var2), Math.max(1, Math.min(64, var3)), var4));
      }
   }

   private void sayGoodbye() {
      RESUME.remove(this.payload.entityId());
      this.switchTo(MartScreen.Mode.GOODBYE, 0, 1, false);
   }

   private ListTag rows() {
      return this.view.getList(this.mode == MartScreen.Mode.BUY ? "ShopItems" : "SellInventory", 10);
   }

   public void tick() {
      if (this.mode == MartScreen.Mode.GOODBYE && ++this.goodbyeTicks >= 24) {
         this.onClose();
      }
   }

   public void onClose() {
      RESUME.remove(this.payload.entityId());
      super.onClose();
   }

   public void renderBackground(GuiGraphics var1, int var2, int var3, float var4) {
      var1.fill(0, 0, this.width, this.height, 1996488704);
      if (this.mode != MartScreen.Mode.MENU && this.mode != MartScreen.Mode.GOODBYE) {
         int var8 = Math.min(420, this.width - 20);
         int var9 = (this.width - var8) / 2;
         int var10 = Math.max(8, (this.height - 244) / 2);
         var1.fill(var9, var10, var9 + var8, var10 + 244, -233103319);
         var1.fill(var9, var10, var9 + var8, var10 + 2, -1653151);
         var1.fill(var9, var10 + 244 - 2, var9 + var8, var10 + 244, -1653151);
      } else {
         int var5 = Math.min(390, this.width - 24);
         int var6 = (this.width - var5) / 2;
         int var7 = Math.max(16, (this.height - 150) / 2);
         var1.fill(var6, var7, var6 + var5, var7 + 112, -234024680);
         var1.fill(var6, var7, var6 + var5, var7 + 2, -1);
         var1.fill(var6, var7 + 110, var6 + var5, var7 + 112, -1);
         var1.fill(var6, var7, var6 + 2, var7 + 112, -1);
         var1.fill(var6 + var5 - 2, var7, var6 + var5, var7 + 112, -1);
      }
   }

   public void render(GuiGraphics var1, int var2, int var3, float var4) {
      super.render(var1, var2, var3, var4);
      if (this.mode != MartScreen.Mode.MENU && this.mode != MartScreen.Mode.GOODBYE) {
         int var5 = Math.min(420, this.width - 20);
         int var6 = (this.width - var5) / 2;
         int var7 = Math.max(8, (this.height - 244) / 2);
         var1.drawCenteredString(this.font, this.title, this.width / 2, var7 + 8, -1653151);
         var1.drawString(
            this.font,
            Component.literal(this.mode == MartScreen.Mode.BUY ? "Take your time." : "What would you like to sell?"),
            var6 + 14,
            var7 + 36,
            -1,
            false
         );
         var1.drawString(this.font, Component.literal("₽" + this.view.getInt("Money")), var6 + 184, var7 + 36, -7478387, false);
         var1.drawString(this.font, Component.literal("Qty: " + this.quantity), var6 + var5 - 98, var7 + 36, -1, false);
         ListTag var8 = this.rows();
         int var9 = this.page * 6;
         int var10 = Math.min(var8.size(), var9 + 6);

         for (int var11 = var9; var11 < var10; var11++) {
            CompoundTag var12 = var8.getCompound(var11);
            int var13 = var7 + 64 + (var11 - var9) * 25;
            String var14 = first(var12.getString("ItemId"), var12.getString("Item"));
            int var15 = this.mode == MartScreen.Mode.BUY ? productCount(var14) : var12.getInt("Count");
            int var16 = var12.getInt("Price");
            String var17 = CommerceScreenSupport.shortId(var14) + (var15 > 1 ? " x" + var15 : "");
            var1.drawString(this.font, Component.literal(var17), var6 + 16, var13, -1, false);
            if (this.mode == MartScreen.Mode.BUY && !var12.getString("CostItem").isBlank()) {
               var1.drawString(
                  this.font,
                  Component.literal(var12.getInt("CostCount") + " " + CommerceScreenSupport.shortId(var12.getString("CostItem"))),
                  var6 + 205,
                  var13,
                  -11143,
                  false
               );
            } else {
               var1.drawString(this.font, Component.literal("₽" + var16), var6 + 205, var13, -11143, false);
            }

            String var18 = var12.getString("Desc");
            if (!var18.isBlank()) {
               var1.drawString(this.font, Component.literal(var18), var6 + 16, var13 + 10, -6511697, false);
            }
         }

         var1.drawString(
            this.font, Component.literal(this.page + 1 + "/" + Math.max(1, (var8.size() + 6 - 1) / 6)), var6 + 94, var7 + 244 - 22, -6511697, false
         );
      } else {
         this.renderClerkDialog(var1);
      }
   }

   private void renderClerkDialog(GuiGraphics var1) {
      int var2 = Math.min(390, this.width - 24);
      int var3 = (this.width - var2) / 2;
      int var4 = Math.max(16, (this.height - 150) / 2);
      if (this.mode == MartScreen.Mode.GOODBYE) {
         var1.drawString(this.font, Component.literal("Thank you!"), var3 + 16, var4 + 24, -1, false);
      } else {
         if (this.returningToMenu) {
            var1.drawString(this.font, Component.literal("Is there anything else I can do?"), var3 + 16, var4 + 24, -1, false);
         } else {
            var1.drawString(this.font, Component.literal("Hi there!"), var3 + 16, var4 + 20, -1, false);
            var1.drawString(this.font, Component.literal("May I help you?"), var3 + 16, var4 + 38, -1, false);
         }
      }
   }

   private static int productCount(String var0) {
      if (var0 == null) {
         return 1;
      }

      String[] var1 = var0.trim().split("\\s+");
      if (var1.length < 2) {
         return 1;
      }

      try {
         return Math.max(1, Integer.parseInt(var1[var1.length - 1]));
      } catch (NumberFormatException var3) {
         return 1;
      }
   }

   private static String first(String var0, String var1) {
      return var0 != null && !var0.isBlank() ? var0 : (var1 == null ? "" : var1);
   }

   public boolean isPauseScreen() {
      return false;
   }

   private enum Mode {
      MENU,
      BUY,
      SELL,
      GOODBYE;
   }

   private record ResumeState(MartScreen.Mode mode, int page, int quantity, long createdAt) {
   }
}
