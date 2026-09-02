package net.cobbleservertools.client.render;

import net.cobbleservertools.entity.AbstractCobbleNpcEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;

public final class CobbleNpcPlayerModel<T extends AbstractCobbleNpcEntity> extends PlayerModel<T> {
   public CobbleNpcPlayerModel(ModelPart var1, boolean var2) {
      super(var1, var2);
   }

   public void setupAnim(T var1, float var2, float var3, float var4, float var5, float var6) {
      super.setupAnim(var1, var2, var3, var4, var5, var6);
      if (var1.isNpcSitting()) {
         this.rightArm.xRot += (float) (-Math.PI / 5);
         this.leftArm.xRot += (float) (-Math.PI / 5);
         this.rightLeg.xRot = -1.4137167F;
         this.rightLeg.yRot = (float) (Math.PI / 10);
         this.rightLeg.zRot = 0.07853982F;
         this.leftLeg.xRot = -1.4137167F;
         this.leftLeg.yRot = (float) (-Math.PI / 10);
         this.leftLeg.zRot = -0.07853982F;
      }
   }
}
