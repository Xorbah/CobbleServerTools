package net.cobbleservertools.network.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record CloseNpcDialogPayload(int entityId, long sessionToken, boolean completed) implements CustomPacketPayload {
   public static final Type<CloseNpcDialogPayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("cobbleservertools", "close_npc_dialog"));
   public static final StreamCodec<RegistryFriendlyByteBuf, CloseNpcDialogPayload> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, CloseNpcDialogPayload>() {
      public CloseNpcDialogPayload decode(RegistryFriendlyByteBuf var1) {
         return new CloseNpcDialogPayload(var1.readVarInt(), var1.readLong(), var1.readBoolean());
      }

      public void encode(RegistryFriendlyByteBuf var1, CloseNpcDialogPayload var2) {
         var1.writeVarInt(var2.entityId());
         var1.writeLong(var2.sessionToken());
         var1.writeBoolean(var2.completed());
      }
   };

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
