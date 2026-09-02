package net.cobbleservertools.client.event;

import net.cobbleservertools.client.state.ClientMoneyState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.ScreenEvent.Render.Post;

@EventBusSubscriber(modid = "cobbleservertools", bus = Bus.GAME, value = Dist.CLIENT)
public final class InventoryMoneyPanelEvents {
   private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("cobbleservertools", "textures/gui/shop_quantity.png");

   private InventoryMoneyPanelEvents() {
   }

   @SubscribeEvent
   public static void afterInventoryRender(Post var0) {
      if (var0.getScreen() instanceof InventoryScreen var1) {
         GuiGraphics var7 = var0.getGuiGraphics();
         int var3 = var1.getGuiLeft();
         int var4 = var1.getGuiTop();
         var7.blit(TEXTURE, var3 - 61, var4, 0.0F, 0.0F, 60, 34, 60, 34);
         Minecraft var5 = Minecraft.getInstance();
         int var6 = var3 - 31;
         var7.drawCenteredString(var5.font, Component.translatable("screen.cobbleservertools.moneytext_2", new Object[0]), var6, var4 + 8, 16777215);
         var7.drawCenteredString(var5.font, Component.literal("$" + ClientMoneyState.get()), var6, var4 + 20, 16777215);
      }
   }
}
