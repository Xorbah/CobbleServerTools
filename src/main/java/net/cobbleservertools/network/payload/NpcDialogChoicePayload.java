package net.cobbleservertools.network.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record NpcDialogChoicePayload(int entityId, long sessionToken, boolean accepted) implements CustomPacketPayload {
   public static final Type<NpcDialogChoicePayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("cobbleservertools", "npc_dialog_choice"));
   public static final StreamCodec<RegistryFriendlyByteBuf, NpcDialogChoicePayload> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, NpcDialogChoicePayload>() {
      public NpcDialogChoicePayload decode(RegistryFriendlyByteBuf var1) {
         return new NpcDialogChoicePayload(var1.readVarInt(), var1.readLong(), var1.readBoolean());
      }

      public void encode(RegistryFriendlyByteBuf var1, NpcDialogChoicePayload var2) {
         var1.writeVarInt(var2.entityId());
         var1.writeLong(var2.sessionToken());
         var1.writeBoolean(var2.accepted());
      }
   };

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
