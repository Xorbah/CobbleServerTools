package net.cobbleservertools.network.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record RequestNpcSnapshotPayload(int entityId) implements CustomPacketPayload {
   public static final Type<RequestNpcSnapshotPayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("cobbleservertools", "request_npc_snapshot"));
   public static final StreamCodec<RegistryFriendlyByteBuf, RequestNpcSnapshotPayload> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, RequestNpcSnapshotPayload>() {
      public RequestNpcSnapshotPayload decode(RegistryFriendlyByteBuf var1) {
         return new RequestNpcSnapshotPayload(var1.readVarInt());
      }

      public void encode(RegistryFriendlyByteBuf var1, RequestNpcSnapshotPayload var2) {
         var1.writeVarInt(var2.entityId());
      }
   };

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
