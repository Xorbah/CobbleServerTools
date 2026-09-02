package net.cobbleservertools.network.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record FreezeOverlayTimeoutPayload() implements CustomPacketPayload {
   public static final Type<FreezeOverlayTimeoutPayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("cobbleservertools", "freeze_overlay_timeout"));
   public static final StreamCodec<RegistryFriendlyByteBuf, FreezeOverlayTimeoutPayload> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, FreezeOverlayTimeoutPayload>() {
      public FreezeOverlayTimeoutPayload decode(RegistryFriendlyByteBuf var1) {
         return new FreezeOverlayTimeoutPayload();
      }

      public void encode(RegistryFriendlyByteBuf var1, FreezeOverlayTimeoutPayload var2) {
      }
   };

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
