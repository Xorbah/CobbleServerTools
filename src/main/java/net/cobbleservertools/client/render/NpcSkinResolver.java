package net.cobbleservertools.client.render;

import net.minecraft.resources.ResourceLocation;

public final class NpcSkinResolver {
   private static final ResourceLocation FALLBACK = ResourceLocation.parse("minecraft:textures/entity/player/wide/steve.png");

   private NpcSkinResolver() {
   }

   public static ResourceLocation resolve(String var0) {
      ResourceLocation var1 = ResourceLocation.tryParse(var0);
      return var1 == null ? FALLBACK : var1;
   }
}
