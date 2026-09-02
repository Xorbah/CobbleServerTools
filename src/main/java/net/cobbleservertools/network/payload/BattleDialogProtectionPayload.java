package net.cobbleservertools.network.payload;

import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record BattleDialogProtectionPayload(UUID npcUuid, byte action) implements CustomPacketPayload {
   public static final byte OPEN = 0;
   public static final byte HEARTBEAT = 1;
   public static final byte CLOSE = 2;
   public static final Type<BattleDialogProtectionPayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("cobbleservertools", "battle_dialog_protection"));
   public static final StreamCodec<RegistryFriendlyByteBuf, BattleDialogProtectionPayload> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, BattleDialogProtectionPayload>() {
      public BattleDialogProtectionPayload decode(RegistryFriendlyByteBuf var1) {
         return new BattleDialogProtectionPayload(var1.readUUID(), var1.readByte());
      }

      public void encode(RegistryFriendlyByteBuf var1, BattleDialogProtectionPayload var2) {
         var1.writeUUID(var2.npcUuid());
         var1.writeByte(var2.action());
      }
   };

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
