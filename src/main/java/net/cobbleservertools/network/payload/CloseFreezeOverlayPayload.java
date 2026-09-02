package net.cobbleservertools.network.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record CloseFreezeOverlayPayload() implements CustomPacketPayload {
   public static final Type<CloseFreezeOverlayPayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("cobbleservertools", "close_freeze_overlay"));
   public static final StreamCodec<RegistryFriendlyByteBuf, CloseFreezeOverlayPayload> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, CloseFreezeOverlayPayload>() {
      public CloseFreezeOverlayPayload decode(RegistryFriendlyByteBuf var1) {
         return new CloseFreezeOverlayPayload();
      }

      public void encode(RegistryFriendlyByteBuf var1, CloseFreezeOverlayPayload var2) {
      }
   };

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
