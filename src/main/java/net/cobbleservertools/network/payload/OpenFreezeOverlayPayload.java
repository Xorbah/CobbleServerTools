package net.cobbleservertools.network.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record OpenFreezeOverlayPayload() implements CustomPacketPayload {
   public static final Type<OpenFreezeOverlayPayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("cobbleservertools", "open_freeze_overlay"));
   public static final StreamCodec<RegistryFriendlyByteBuf, OpenFreezeOverlayPayload> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, OpenFreezeOverlayPayload>() {
      public OpenFreezeOverlayPayload decode(RegistryFriendlyByteBuf var1) {
         return new OpenFreezeOverlayPayload();
      }

      public void encode(RegistryFriendlyByteBuf var1, OpenFreezeOverlayPayload var2) {
      }
   };

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
