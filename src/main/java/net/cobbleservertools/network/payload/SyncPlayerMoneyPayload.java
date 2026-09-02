package net.cobbleservertools.network.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record SyncPlayerMoneyPayload(int money) implements CustomPacketPayload {
   public static final Type<SyncPlayerMoneyPayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("cobbleservertools", "sync_player_money"));
   public static final StreamCodec<RegistryFriendlyByteBuf, SyncPlayerMoneyPayload> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, SyncPlayerMoneyPayload>() {
      public SyncPlayerMoneyPayload decode(RegistryFriendlyByteBuf var1) {
         return new SyncPlayerMoneyPayload(Math.max(0, var1.readVarInt()));
      }

      public void encode(RegistryFriendlyByteBuf var1, SyncPlayerMoneyPayload var2) {
         var1.writeVarInt(Math.max(0, var2.money()));
      }
   };

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
