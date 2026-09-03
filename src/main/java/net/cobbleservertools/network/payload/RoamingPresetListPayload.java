package net.cobbleservertools.network.payload;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Server-authoritative reusable roaming definitions. */
public record RoamingPresetListPayload(CompoundTag presets) implements CustomPacketPayload {
    public static final Type<RoamingPresetListPayload> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath("cobbleservertools", "roaming_presets"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RoamingPresetListPayload> STREAM_CODEC =
        new StreamCodec<>() {
            public RoamingPresetListPayload decode(RegistryFriendlyByteBuf b) {
                CompoundTag tag = b.readNbt();
                return new RoamingPresetListPayload(tag == null ? new CompoundTag() : tag);
            }
            public void encode(RegistryFriendlyByteBuf b, RoamingPresetListPayload value) {
                b.writeNbt(value.presets());
            }
        };

    public RoamingPresetListPayload { presets = presets == null ? new CompoundTag() : presets.copy(); }
    @Override public CompoundTag presets() { return presets.copy(); }
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
