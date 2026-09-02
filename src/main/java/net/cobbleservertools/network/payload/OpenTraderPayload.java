package net.cobbleservertools.network.payload;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record OpenTraderPayload(int entityId, long sessionToken, String title, CompoundTag view) implements CustomPacketPayload {
   public static final Type<OpenTraderPayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("cobbleservertools", "open_trader"));
   public static final StreamCodec<RegistryFriendlyByteBuf, OpenTraderPayload> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, OpenTraderPayload>() {
      public OpenTraderPayload decode(RegistryFriendlyByteBuf var1) {
         int var2 = var1.readVarInt();
         long var3 = var1.readLong();
         String var5 = var1.readUtf(128);
         CompoundTag var6 = var1.readNbt();
         return new OpenTraderPayload(var2, var3, var5, var6 == null ? new CompoundTag() : var6);
      }

      public void encode(RegistryFriendlyByteBuf var1, OpenTraderPayload var2) {
         var1.writeVarInt(var2.entityId());
         var1.writeLong(var2.sessionToken());
         var1.writeUtf(var2.title(), 128);
         var1.writeNbt(var2.view());
      }
   };

   public OpenTraderPayload(int entityId, long sessionToken, String title, CompoundTag view) {
      title = title == null ? "" : title;
      view = view == null ? new CompoundTag() : view.copy();
      this.entityId = entityId;
      this.sessionToken = sessionToken;
      this.title = title;
      this.view = view;
   }

   public CompoundTag view() {
      return this.view.copy();
   }

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
