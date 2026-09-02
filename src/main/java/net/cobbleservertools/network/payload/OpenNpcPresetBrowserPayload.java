package net.cobbleservertools.network.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record OpenNpcPresetBrowserPayload(int entityId, String browserType, String currentPresetId) implements CustomPacketPayload {
   public static final Type<OpenNpcPresetBrowserPayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("cobbleservertools", "open_npc_preset_browser"));
   public static final StreamCodec<RegistryFriendlyByteBuf, OpenNpcPresetBrowserPayload> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, OpenNpcPresetBrowserPayload>() {
      public OpenNpcPresetBrowserPayload decode(RegistryFriendlyByteBuf var1) {
         return new OpenNpcPresetBrowserPayload(var1.readVarInt(), var1.readUtf(32), var1.readUtf(256));
      }

      public void encode(RegistryFriendlyByteBuf var1, OpenNpcPresetBrowserPayload var2) {
         var1.writeVarInt(var2.entityId());
         var1.writeUtf(var2.browserType(), 32);
         var1.writeUtf(var2.currentPresetId(), 256);
      }
   };

   public OpenNpcPresetBrowserPayload(int entityId, String browserType, String currentPresetId) {
      browserType = browserType == null ? "" : browserType;
      currentPresetId = currentPresetId == null ? "" : currentPresetId;
      this.entityId = entityId;
      this.browserType = browserType;
      this.currentPresetId = currentPresetId;
   }

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
