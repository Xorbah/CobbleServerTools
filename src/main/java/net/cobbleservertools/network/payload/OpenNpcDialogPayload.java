package net.cobbleservertools.network.payload;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record OpenNpcDialogPayload(int entityId, long sessionToken, String title, List<String> lines, boolean choices) implements CustomPacketPayload {
   private static final int MAX_TITLE_LENGTH = 128;
   private static final int MAX_LINE_LENGTH = 512;
   private static final int MAX_LINES = 128;
   public static final Type<OpenNpcDialogPayload> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("cobbleservertools", "open_npc_dialog"));
   public static final StreamCodec<RegistryFriendlyByteBuf, OpenNpcDialogPayload> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, OpenNpcDialogPayload>() {
      public OpenNpcDialogPayload decode(RegistryFriendlyByteBuf var1) {
         int var2 = var1.readVarInt();
         long var3 = var1.readLong();
         String var5 = var1.readUtf(128);
         int var6 = var1.readVarInt();
         if (var6 >= 0 && var6 <= 128) {
            ArrayList var7 = new ArrayList(var6);

            for (int var8 = 0; var8 < var6; var8++) {
               var7.add(var1.readUtf(512));
            }

            return new OpenNpcDialogPayload(var2, var3, var5, var7, var1.readBoolean());
         } else {
            throw new IllegalArgumentException("Invalid dialogue line count: " + var6);
         }
      }

      public void encode(RegistryFriendlyByteBuf var1, OpenNpcDialogPayload var2) {
         var1.writeVarInt(var2.entityId());
         var1.writeLong(var2.sessionToken());
         var1.writeUtf(var2.title(), 128);
         int var3 = Math.min(var2.lines().size(), 128);
         var1.writeVarInt(var3);

         for (int var4 = 0; var4 < var3; var4++) {
            var1.writeUtf(var2.lines().get(var4), 512);
         }

         var1.writeBoolean(var2.choices());
      }
   };

   public OpenNpcDialogPayload(int entityId, long sessionToken, String title, List<String> lines, boolean choices) {
      title = bounded(title, 128);
      ArrayList var7 = new ArrayList();
      if (lines != null) {
         for (String var9 : lines) {
            if (var7.size() >= 128) {
               break;
            }

            var7.add(bounded(var9, 512));
         }
      }

      lines = List.copyOf(var7);
      this.entityId = entityId;
      this.sessionToken = sessionToken;
      this.title = title;
      this.lines = lines;
      this.choices = choices;
   }

   private static String bounded(String var0, int var1) {
      String var2 = var0 == null ? "" : var0.replace('\u0000', ' ').strip();
      return var2.length() <= var1 ? var2 : var2.substring(0, var1);
   }

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
