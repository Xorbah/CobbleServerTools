package net.cobbleservertools.network.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record NpcPresetSearchRequestPayload(
   int entityId, String browserType, String query, String region, String role, String location, String trainerClass, String tag, int page, boolean reload
) implements CustomPacketPayload {
   public static final Type<NpcPresetSearchRequestPayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("cobbleservertools", "npc_preset_search"));
   public static final StreamCodec<RegistryFriendlyByteBuf, NpcPresetSearchRequestPayload> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, NpcPresetSearchRequestPayload>() {
      public NpcPresetSearchRequestPayload decode(RegistryFriendlyByteBuf var1) {
         return new NpcPresetSearchRequestPayload(
            var1.readVarInt(),
            var1.readUtf(32),
            var1.readUtf(128),
            var1.readUtf(64),
            var1.readUtf(64),
            var1.readUtf(64),
            var1.readUtf(64),
            var1.readUtf(64),
            var1.readVarInt(),
            var1.readBoolean()
         );
      }

      public void encode(RegistryFriendlyByteBuf var1, NpcPresetSearchRequestPayload var2) {
         var1.writeVarInt(var2.entityId());
         var1.writeUtf(var2.browserType(), 32);
         var1.writeUtf(var2.query(), 128);
         var1.writeUtf(var2.region(), 64);
         var1.writeUtf(var2.role(), 64);
         var1.writeUtf(var2.location(), 64);
         var1.writeUtf(var2.trainerClass(), 64);
         var1.writeUtf(var2.tag(), 64);
         var1.writeVarInt(Math.max(0, var2.page()));
         var1.writeBoolean(var2.reload());
      }
   };

   public NpcPresetSearchRequestPayload(
      int entityId, String browserType, String query, String region, String role, String location, String trainerClass, String tag, int page, boolean reload
   ) {
      browserType = safe(browserType);
      query = safe(query);
      region = safe(region);
      role = safe(role);
      location = safe(location);
      trainerClass = safe(trainerClass);
      tag = safe(tag);
      this.entityId = entityId;
      this.browserType = browserType;
      this.query = query;
      this.region = region;
      this.role = role;
      this.location = location;
      this.trainerClass = trainerClass;
      this.tag = tag;
      this.page = page;
      this.reload = reload;
   }

   private static String safe(String var0) {
      return var0 == null ? "" : var0;
   }

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
