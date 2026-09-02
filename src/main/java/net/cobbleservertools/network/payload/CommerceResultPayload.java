package net.cobbleservertools.network.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record CommerceResultPayload(boolean success, String translationKey) implements CustomPacketPayload {
   public static final Type<CommerceResultPayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("cobbleservertools", "commerce_result"));
   public static final StreamCodec<RegistryFriendlyByteBuf, CommerceResultPayload> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, CommerceResultPayload>() {
      public CommerceResultPayload decode(RegistryFriendlyByteBuf var1) {
         return new CommerceResultPayload(var1.readBoolean(), var1.readUtf(256));
      }

      public void encode(RegistryFriendlyByteBuf var1, CommerceResultPayload var2) {
         var1.writeBoolean(var2.success());
         var1.writeUtf(var2.translationKey(), 256);
      }
   };

   public CommerceResultPayload(boolean success, String translationKey) {
      translationKey = translationKey == null ? "" : translationKey;
      this.success = success;
      this.translationKey = translationKey;
   }

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
