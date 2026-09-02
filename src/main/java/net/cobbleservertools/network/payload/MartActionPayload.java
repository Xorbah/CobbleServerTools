package net.cobbleservertools.network.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record MartActionPayload(int entityId, long sessionToken, MartActionPayload.Action action, int target, int quantity) implements CustomPacketPayload {
   public static final Type<MartActionPayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("cobbleservertools", "mart_action"));
   public static final StreamCodec<RegistryFriendlyByteBuf, MartActionPayload> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, MartActionPayload>() {
      public MartActionPayload decode(RegistryFriendlyByteBuf var1) {
         int var2 = var1.readVarInt();
         long var3 = var1.readLong();
         MartActionPayload.Action var5 = var1.readVarInt() == 1 ? MartActionPayload.Action.SELL : MartActionPayload.Action.BUY;
         int var6 = var1.readVarInt();
         int var7 = var1.readVarInt();
         return new MartActionPayload(var2, var3, var5, var6, var7);
      }

      public void encode(RegistryFriendlyByteBuf var1, MartActionPayload var2) {
         var1.writeVarInt(var2.entityId());
         var1.writeLong(var2.sessionToken());
         var1.writeVarInt(var2.action() == MartActionPayload.Action.SELL ? 1 : 0);
         var1.writeVarInt(var2.target());
         var1.writeVarInt(var2.quantity());
      }
   };

   public MartActionPayload(int entityId, long sessionToken, MartActionPayload.Action action, int target, int quantity) {
      action = action == null ? MartActionPayload.Action.BUY : action;
      this.entityId = entityId;
      this.sessionToken = sessionToken;
      this.action = action;
      this.target = target;
      this.quantity = quantity;
   }

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public enum Action {
      BUY,
      SELL;
   }
}
