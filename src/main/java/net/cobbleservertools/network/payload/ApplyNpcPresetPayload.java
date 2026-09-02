package net.cobbleservertools.network.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record ApplyNpcPresetPayload(int entityId, String browserType, String presetId) implements CustomPacketPayload {
   public static final Type<ApplyNpcPresetPayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("cobbleservertools", "apply_npc_preset"));
   public static final StreamCodec<RegistryFriendlyByteBuf, ApplyNpcPresetPayload> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, ApplyNpcPresetPayload>() {
      public ApplyNpcPresetPayload decode(RegistryFriendlyByteBuf var1) {
         return new ApplyNpcPresetPayload(var1.readVarInt(), var1.readUtf(32), var1.readUtf(256));
      }

      public void encode(RegistryFriendlyByteBuf var1, ApplyNpcPresetPayload var2) {
         var1.writeVarInt(var2.entityId());
         var1.writeUtf(var2.browserType(), 32);
         var1.writeUtf(var2.presetId(), 256);
      }
   };

   public ApplyNpcPresetPayload(int entityId, String browserType, String presetId) {
      browserType = browserType == null ? "" : browserType;
      presetId = presetId == null ? "" : presetId;
      this.entityId = entityId;
      this.browserType = browserType;
      this.presetId = presetId;
   }

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
