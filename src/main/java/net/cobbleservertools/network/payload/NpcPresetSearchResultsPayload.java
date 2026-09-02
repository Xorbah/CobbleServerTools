package net.cobbleservertools.network.payload;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record NpcPresetSearchResultsPayload(
   int entityId, String browserType, String query, String region, String role, String location, String trainerClass, String tag, CompoundTag results
) implements CustomPacketPayload {
   public static final Type<NpcPresetSearchResultsPayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("cobbleservertools", "npc_preset_results"));
   public static final StreamCodec<RegistryFriendlyByteBuf, NpcPresetSearchResultsPayload> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, NpcPresetSearchResultsPayload>() {
      public NpcPresetSearchResultsPayload decode(RegistryFriendlyByteBuf var1) {
         CompoundTag var2 = var1.readNbt();
         return new NpcPresetSearchResultsPayload(
            var1.readVarInt(),
            var1.readUtf(32),
            var1.readUtf(128),
            var1.readUtf(64),
            var1.readUtf(64),
            var1.readUtf(64),
            var1.readUtf(64),
            var1.readUtf(64),
            var2 == null ? new CompoundTag() : var2
         );
      }

      public void encode(RegistryFriendlyByteBuf var1, NpcPresetSearchResultsPayload var2) {
         var1.writeNbt(var2.results());
         var1.writeVarInt(var2.entityId());
         var1.writeUtf(var2.browserType(), 32);
         var1.writeUtf(var2.query(), 128);
         var1.writeUtf(var2.region(), 64);
         var1.writeUtf(var2.role(), 64);
         var1.writeUtf(var2.location(), 64);
         var1.writeUtf(var2.trainerClass(), 64);
         var1.writeUtf(var2.tag(), 64);
      }
   };

   public NpcPresetSearchResultsPayload(
      int entityId, String browserType, String query, String region, String role, String location, String trainerClass, String tag, CompoundTag results
   ) {
      browserType = safe(browserType);
      query = safe(query);
      region = safe(region);
      role = safe(role);
      location = safe(location);
      trainerClass = safe(trainerClass);
      tag = safe(tag);
      results = results == null ? new CompoundTag() : results.copy();
      this.entityId = entityId;
      this.browserType = browserType;
      this.query = query;
      this.region = region;
      this.role = role;
      this.location = location;
      this.trainerClass = trainerClass;
      this.tag = tag;
      this.results = results;
   }

   public CompoundTag results() {
      return this.results.copy();
   }

   private static String safe(String var0) {
      return var0 == null ? "" : var0;
   }

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
