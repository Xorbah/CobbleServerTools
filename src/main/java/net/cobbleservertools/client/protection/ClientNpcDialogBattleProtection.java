package net.cobbleservertools.client.protection;

import java.util.UUID;
import net.cobbleservertools.entity.AbstractBattleNpcEntity;
import net.cobbleservertools.network.payload.BattleDialogProtectionPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ClientNpcDialogBattleProtection {
   private UUID npcUuid;
   private int ticks;

   public boolean openForEntity(int var1) {
      Minecraft var2 = Minecraft.getInstance();
      if (var2.level == null) {
         return false;
      }

      Entity var3 = var2.level.getEntity(var1);
      if (!(var3 instanceof AbstractBattleNpcEntity)) {
         return false;
      }

      this.npcUuid = var3.getUUID();
      this.ticks = 0;
      PacketDistributor.sendToServer(new BattleDialogProtectionPayload(this.npcUuid, (byte)0), new CustomPacketPayload[0]);
      return true;
   }

   public void tick() {
      if (this.npcUuid != null) {
         if (++this.ticks >= 10) {
            this.ticks = 0;
            PacketDistributor.sendToServer(new BattleDialogProtectionPayload(this.npcUuid, (byte)1), new CustomPacketPayload[0]);
         }
      }
   }

   public void close() {
      if (this.npcUuid != null) {
         PacketDistributor.sendToServer(new BattleDialogProtectionPayload(this.npcUuid, (byte)2), new CustomPacketPayload[0]);
         this.npcUuid = null;
         this.ticks = 0;
      }
   }
}
