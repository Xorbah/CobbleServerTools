package net.cobbleservertools.network.payload;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record UpdateNpcProfilePayload(int entityId, CompoundTag profile) implements CustomPacketPayload {
   public static final Type<UpdateNpcProfilePayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("cobbleservertools", "update_npc_profile"));
   public static final StreamCodec<RegistryFriendlyByteBuf, UpdateNpcProfilePayload> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, UpdateNpcProfilePayload>() {
      public UpdateNpcProfilePayload decode(RegistryFriendlyByteBuf var1) {
         int var2 = var1.readVarInt();
         CompoundTag var3 = var1.readNbt();
         return new UpdateNpcProfilePayload(var2, var3 == null ? new CompoundTag() : var3);
      }

      public void encode(RegistryFriendlyByteBuf var1, UpdateNpcProfilePayload var2) {
         var1.writeVarInt(var2.entityId());
         var1.writeNbt(var2.profile());
      }
   };

   public UpdateNpcProfilePayload(int entityId, CompoundTag profile) {
      profile = profile == null ? new CompoundTag() : profile.copy();
      this.entityId = entityId;
      this.profile = profile;
   }

   public CompoundTag profile() {
      return this.profile.copy();
   }

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
