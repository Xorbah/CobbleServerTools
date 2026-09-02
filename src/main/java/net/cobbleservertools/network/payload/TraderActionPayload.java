package net.cobbleservertools.network.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record TraderActionPayload(int entityId, long sessionToken, int partySlot) implements CustomPacketPayload {
   public static final Type<TraderActionPayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("cobbleservertools", "trader_action"));
   public static final StreamCodec<RegistryFriendlyByteBuf, TraderActionPayload> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, TraderActionPayload>() {
      public TraderActionPayload decode(RegistryFriendlyByteBuf var1) {
         return new TraderActionPayload(var1.readVarInt(), var1.readLong(), var1.readVarInt());
      }

      public void encode(RegistryFriendlyByteBuf var1, TraderActionPayload var2) {
         var1.writeVarInt(var2.entityId());
         var1.writeLong(var2.sessionToken());
         var1.writeVarInt(var2.partySlot());
      }
   };

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
