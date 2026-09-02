package net.cobbleservertools.network.payload;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record NpcSnapshotPayload(int entityId, boolean editorView, CompoundTag profile) implements CustomPacketPayload {
   public static final Type<NpcSnapshotPayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("cobbleservertools", "npc_snapshot"));
   public static final StreamCodec<RegistryFriendlyByteBuf, NpcSnapshotPayload> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, NpcSnapshotPayload>() {
      public NpcSnapshotPayload decode(RegistryFriendlyByteBuf var1) {
         int var2 = var1.readVarInt();
         boolean var3 = var1.readBoolean();
         CompoundTag var4 = var1.readNbt();
         return new NpcSnapshotPayload(var2, var3, var4 == null ? new CompoundTag() : var4);
      }

      public void encode(RegistryFriendlyByteBuf var1, NpcSnapshotPayload var2) {
         var1.writeVarInt(var2.entityId());
         var1.writeBoolean(var2.editorView());
         var1.writeNbt(var2.profile());
      }
   };

   public NpcSnapshotPayload(int entityId, boolean editorView, CompoundTag profile) {
      profile = profile == null ? new CompoundTag() : profile.copy();
      this.entityId = entityId;
      this.editorView = editorView;
      this.profile = profile;
   }

   public CompoundTag profile() {
      return this.profile.copy();
   }

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
