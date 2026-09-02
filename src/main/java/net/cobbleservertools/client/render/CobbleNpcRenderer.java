package net.cobbleservertools.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.cobbleservertools.entity.AbstractCobbleNpcEntity;
import net.cobbleservertools.entity.data.NpcType;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public final class CobbleNpcRenderer<T extends AbstractCobbleNpcEntity> extends LivingEntityRenderer<T, CobbleNpcPlayerModel<T>> {
   private final CobbleNpcPlayerModel<T> wideModel = (CobbleNpcPlayerModel<T>)this.model;
   private final CobbleNpcPlayerModel<T> slimModel;

   public CobbleNpcRenderer(Context var1) {
      super(var1, new CobbleNpcPlayerModel(var1.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
      this.slimModel = new CobbleNpcPlayerModel<>(var1.bakeLayer(ModelLayers.PLAYER_SLIM), true);
   }

   public void render(T var1, float var2, float var3, PoseStack var4, MultiBufferSource var5, int var6) {
      this.model = var1.npcType() == NpcType.SLIM ? this.slimModel : this.wideModel;
      super.render(var1, var2, var3, var4, var5, var6);
   }

   protected void scale(T var1, PoseStack var2, float var3) {
      float var4 = var1.npcSize();
      var2.scale(var4, var4, var4);
   }

   public ResourceLocation getTextureLocation(T var1) {
      return NpcSkinResolver.resolve(var1.npcSkin());
   }
}
