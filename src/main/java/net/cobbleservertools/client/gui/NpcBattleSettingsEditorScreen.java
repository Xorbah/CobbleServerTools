package net.cobbleservertools.client.gui;

import net.cobbleservertools.compat.rct.RctBattleBridge;
import net.cobbleservertools.entity.data.BattleType;
import net.cobbleservertools.network.payload.UpdateNpcProfilePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;

public final class NpcBattleSettingsEditorScreen extends Screen {
   private final int entityId;
   private final CompoundTag original;
   private EditBox seekRange;
   private EditBox rematch;
   private EditBox money;
   private EditBox command;
   private EditBox homeX;
   private EditBox homeY;
   private EditBox homeZ;
   private EditBox entityX;
   private EditBox entityY;
   private EditBox entityZ;
   private EditBox homeYaw;
   private EditBox maxItemUses;
   private BattleType battleType;
   private boolean repeat;
   private String rewardItem;
   private String battleEngine;
   private String rctFormat;
   private boolean healPlayers;
   private boolean adjustPlayerLevels;
   private boolean adjustNpcLevels;
   private ListTag trainerBag;

   public NpcBattleSettingsEditorScreen(int var1, CompoundTag var2) {
      super(Component.literal("CobbleServerTools Battle Settings"));
      this.entityId = var1;
      this.original = var2 == null ? new CompoundTag() : var2.copy();
      this.battleType = BattleType.parse(this.original.getString("BattleType"));
      this.repeat = this.original.getBoolean("RewardResetAlways");
      this.rewardItem = this.original.getString("RewardItemId");
      this.battleEngine = RctBattleBridge.normalizeEngine(this.original.getString("RctBattleEngine"));
      this.rctFormat = RctBattleBridge.normalizeFormat(this.original.getString("RctBattleFormat"));
      this.healPlayers = this.original.getBoolean("RctHealPlayers");
      this.adjustPlayerLevels = this.original.getBoolean("RctAdjustPlayerLevels");
      this.adjustNpcLevels = this.original.getBoolean("RctAdjustNpcLevels");
      this.trainerBag = copyList(this.original.getList("RctTrainerBag", 10));
   }

   protected void init() {
      int var1 = Math.min(600, this.width - 18);
      int var2 = (this.width - var1) / 2;
      int var3 = Math.max(4, (this.height - 458) / 2);
      int var4 = var2 + 130;
      int var5 = var1 - 148;
      this.seekRange = this.edit(var4, var3 + 30, 72, this.textInt("SeekRange", 8), 2);
      this.rematch = this.edit(var4 + 80, var3 + 30, 92, this.textInt("RematchCooldownSeconds", 0), 10);
      this.money = this.edit(var4 + 180, var3 + 30, 100, this.textInt("MoneyReward", 0), 10);
      this.addRenderableWidget(
         Button.builder(
               Component.literal("Reward item: " + disp(this.rewardItem)),
               var1x -> this.minecraft.setScreen(new SearchableCatalogScreen(this, NpcCreatorCatalog.Kind.ITEM, "", this.rewardItem, var2x -> {
                  this.rewardItem = var2x;
                  var1x.setMessage(Component.literal("Reward item: " + disp(var2x)));
               }))
            )
            .bounds(var4, var3 + 58, var5, 20)
            .build()
      );
      this.command = this.edit(var4, var3 + 84, var5, this.original.getString("OnVictoryCommand"), 2048);
      int var6 = (var5 - 12) / 3;
      this.homeX = this.edit(var4, var3 + 116, var6, this.textDouble("HomeX", 0.0), 24);
      this.homeY = this.edit(var4 + var6 + 6, var3 + 116, var6, this.textDouble("HomeY", 0.0), 24);
      this.homeZ = this.edit(var4 + (var6 + 6) * 2, var3 + 116, var6, this.textDouble("HomeZ", 0.0), 24);
      this.entityX = this.edit(var4, var3 + 148, var6, this.textDouble("EntityX", this.original.getDouble("HomeX")), 24);
      this.entityY = this.edit(var4 + var6 + 6, var3 + 148, var6, this.textDouble("EntityY", this.original.getDouble("HomeY")), 24);
      this.entityZ = this.edit(var4 + (var6 + 6) * 2, var3 + 148, var6, this.textDouble("EntityZ", this.original.getDouble("HomeZ")), 24);
      this.homeYaw = this.edit(var4, var3 + 180, 90, this.textDouble("HomeYaw", 0.0), 16);
      this.addRenderableWidget(Button.builder(Component.literal("Battle type: " + this.battleType.displayName()), var1x -> {
         BattleType[] var2x = BattleType.values();
         this.battleType = var2x[(this.battleType.ordinal() + 1) % var2x.length];
         var1x.setMessage(Component.literal("Battle type: " + this.battleType.displayName()));
      }).bounds(var4 + 98, var3 + 180, 190, 20).build());
      this.addRenderableWidget(Button.builder(Component.literal("Repeat reward: " + (this.repeat ? "Always" : "Once")), var1x -> {
         this.repeat = !this.repeat;
         var1x.setMessage(Component.literal("Repeat reward: " + (this.repeat ? "Always" : "Once")));
      }).bounds(var4, var3 + 208, 190, 20).build());
      this.addRenderableWidget(Button.builder(Component.literal(this.engineLabel()), var1x -> {
         this.battleEngine = nextEngine(this.battleEngine);
         var1x.setMessage(Component.literal(this.engineLabel()));
      }).bounds(var4 + 198, var3 + 208, var5 - 198, 20).build());
      this.addRenderableWidget(Button.builder(Component.literal(this.formatLabel()), var1x -> {
         this.rctFormat = nextFormat(this.rctFormat);
         var1x.setMessage(Component.literal(this.formatLabel()));
      }).bounds(var4, var3 + 236, 190, 20).build());
      this.maxItemUses = this.edit(
         var4 + 198, var3 + 236, 90, Integer.toString(this.original.contains("RctMaxItemUses") ? this.original.getInt("RctMaxItemUses") : -1), 5
      );
      this.addRenderableWidget(Button.builder(Component.literal("Heal player: " + onOff(this.healPlayers)), var1x -> {
         this.healPlayers = !this.healPlayers;
         var1x.setMessage(Component.literal("Heal player: " + onOff(this.healPlayers)));
      }).bounds(var4, var3 + 264, 138, 20).build());
      this.addRenderableWidget(Button.builder(Component.literal("Scale player: " + onOff(this.adjustPlayerLevels)), var1x -> {
         this.adjustPlayerLevels = !this.adjustPlayerLevels;
         var1x.setMessage(Component.literal("Scale player: " + onOff(this.adjustPlayerLevels)));
      }).bounds(var4 + 144, var3 + 264, 138, 20).build());
      this.addRenderableWidget(Button.builder(Component.literal("Scale NPC: " + onOff(this.adjustNpcLevels)), var1x -> {
         this.adjustNpcLevels = !this.adjustNpcLevels;
         var1x.setMessage(Component.literal("Scale NPC: " + onOff(this.adjustNpcLevels)));
      }).bounds(var4 + 288, var3 + 264, Math.max(120, var5 - 288), 20).build());
      this.addRenderableWidget(
         Button.builder(
               Component.literal("RCT Trainer Bag: " + this.trainerBag.size() + " item" + (this.trainerBag.size() == 1 ? "" : "s")),
               var1x -> this.minecraft.setScreen(new RctTrainerBagEditorScreen(this, this.trainerBag, var1xx -> this.trainerBag = copyList(var1xx)))
            )
            .bounds(var4, var3 + 292, var5, 20)
            .build()
      );
      this.addRenderableWidget(Button.builder(Component.literal("Save"), var1x -> this.save()).bounds(var2 + var1 / 2 - 86, var3 + 414, 80, 20).build());
      this.addRenderableWidget(
         Button.builder(Component.literal("Cancel"), var1x -> this.minecraft.setScreen(null)).bounds(var2 + var1 / 2 + 6, var3 + 414, 80, 20).build()
      );
   }

   private EditBox edit(int var1, int var2, int var3, String var4, int var5) {
      EditBox var6 = new EditBox(this.font, var1, var2, Math.max(40, var3), 20, Component.literal("value"));
      var6.setMaxLength(var5);
      var6.setValue(var4 == null ? "" : var4);
      this.addRenderableWidget(var6);
      return var6;
   }

   private String textInt(String var1, int var2) {
      return Integer.toString(this.original.contains(var1) ? this.original.getInt(var1) : var2);
   }

   private String textDouble(String var1, double var2) {
      return Double.toString(this.original.contains(var1) ? this.original.getDouble(var1) : var2);
   }

   private void save() {
      CompoundTag var1 = new CompoundTag();
      var1.putInt("SeekRange", clamp(num(this.seekRange.getValue(), 8), 1, 16));
      var1.putInt("RematchCooldownSeconds", clamp(num(this.rematch.getValue(), 0), 0, 31536000));
      var1.putInt("MoneyReward", Math.max(0, num(this.money.getValue(), 0)));
      var1.putString("RewardItemId", this.rewardItem);
      var1.putString("OnVictoryCommand", this.command.getValue().trim());
      var1.putBoolean("RewardResetAlways", this.repeat);
      var1.putString("BattleType", this.battleType.name());
      var1.putDouble("HomeX", dbl(this.homeX, 0.0));
      var1.putDouble("HomeY", dbl(this.homeY, 0.0));
      var1.putDouble("HomeZ", dbl(this.homeZ, 0.0));
      var1.putDouble("EntityX", dbl(this.entityX, var1.getDouble("HomeX")));
      var1.putDouble("EntityY", dbl(this.entityY, var1.getDouble("HomeY")));
      var1.putDouble("EntityZ", dbl(this.entityZ, var1.getDouble("HomeZ")));
      var1.putFloat("HomeYaw", (float)dbl(this.homeYaw, 0.0));
      var1.putString("RctBattleEngine", this.battleEngine);
      var1.putString("RctBattleFormat", this.rctFormat);
      var1.putInt("RctMaxItemUses", clamp(num(this.maxItemUses.getValue(), -1), -1, 99));
      var1.putBoolean("RctHealPlayers", this.healPlayers);
      var1.putBoolean("RctAdjustPlayerLevels", this.adjustPlayerLevels);
      var1.putBoolean("RctAdjustNpcLevels", this.adjustNpcLevels);
      var1.put("RctTrainerBag", copyList(this.trainerBag));
      PacketDistributor.sendToServer(new UpdateNpcProfilePayload(this.entityId, var1), new CustomPacketPayload[0]);
      this.minecraft.setScreen(null);
   }

   private String engineLabel() {
      String var1 = switch (this.battleEngine) {
         case "RCT" -> "RCT Battle AI";
         case "RUN_BUN" -> "Run & Bun AI";
         default -> "CobbleServerTools Native";
      };

      String var5 = switch (this.battleEngine) {
         case "RCT" -> RctBattleBridge.rctAvailable() ? "" : " (unavailable)";
         case "RUN_BUN" -> RctBattleBridge.runBunAvailable() ? "" : " (unavailable)";
         default -> "";
      };
      return "Engine: " + var1 + var5;
   }

   private String formatLabel() {
      return "Format: " + switch (this.rctFormat) {
         case "GEN_9_DOUBLES" -> "Doubles";
         case "GEN_9_TRIPLES" -> "Triples";
         default -> "Singles";
      };
   }

   private static String nextEngine(String var0) {
      return switch (RctBattleBridge.normalizeEngine(var0)) {
         case "COBBLESERVERTOOLS" -> "RCT";
         case "RCT" -> "RUN_BUN";
         default -> "COBBLESERVERTOOLS";
      };
   }

   private static String nextFormat(String var0) {
      return switch (RctBattleBridge.normalizeFormat(var0)) {
         case "GEN_9_SINGLES" -> "GEN_9_DOUBLES";
         case "GEN_9_DOUBLES" -> "GEN_9_TRIPLES";
         default -> "GEN_9_SINGLES";
      };
   }

   private static String onOff(boolean var0) {
      return var0 ? "On" : "Off";
   }

   private static int num(String var0, int var1) {
      try {
         return Integer.parseInt(var0.trim());
      } catch (Exception var3) {
         return var1;
      }
   }

   private static double dbl(EditBox var0, double var1) {
      try {
         double var3 = Double.parseDouble(var0.getValue().trim());
         return Double.isFinite(var3) ? var3 : var1;
      } catch (Exception var5) {
         return var1;
      }
   }

   private static int clamp(int var0, int var1, int var2) {
      return Math.max(var1, Math.min(var2, var0));
   }

   private static String disp(String var0) {
      return var0 != null && !var0.isBlank() ? NpcCreatorCatalog.pretty(var0) : "None";
   }

   private static ListTag copyList(ListTag var0) {
      ListTag var1 = new ListTag();
      if (var0 == null) {
         return var1;
      }

      for (int var2 = 0; var2 < var0.size(); var2++) {
         var1.add(var0.getCompound(var2).copy());
      }

      return var1;
   }

   public void renderBackground(GuiGraphics var1, int var2, int var3, float var4) {
      var1.fill(0, 0, this.width, this.height, -2013265920);
      int var5 = Math.min(600, this.width - 18);
      int var6 = (this.width - var5) / 2;
      int var7 = Math.max(4, (this.height - 458) / 2);
      var1.fill(var6, var7, var6 + var5, var7 + 446, -233103319);
      var1.fill(var6, var7, var6 + var5, var7 + 2, -1653151);
   }

   public void render(GuiGraphics var1, int var2, int var3, float var4) {
      super.render(var1, var2, var3, var4);
      int var5 = Math.min(600, this.width - 18);
      int var6 = (this.width - var5) / 2;
      int var7 = Math.max(4, (this.height - 458) / 2);
      var1.drawCenteredString(this.font, this.title, this.width / 2, var7 + 8, -1653151);
      var1.drawString(this.font, Component.literal("Seek / Rematch / Money"), var6 + 14, var7 + 36, -1, false);
      var1.drawString(this.font, Component.literal("Victory command"), var6 + 14, var7 + 90, -1, false);
      var1.drawString(this.font, Component.literal("Home XYZ"), var6 + 14, var7 + 122, -1, false);
      var1.drawString(this.font, Component.literal("Entity XYZ"), var6 + 14, var7 + 154, -1, false);
      var1.drawString(this.font, Component.literal("Home yaw"), var6 + 14, var7 + 186, -1, false);
      var1.drawString(this.font, Component.literal("RCT max items"), var6 + 14, var7 + 242, -1, false);
      var1.drawString(this.font, Component.literal(RctBattleBridge.availabilityLabel()), var6 + 130, var7 + 322, -4669236, false);
      var1.drawString(this.font, Component.literal("RCT settings are ignored when Engine = CobbleServerTools Native."), var6 + 130, var7 + 338, -4669236, false);
   }

   public boolean isPauseScreen() {
      return false;
   }
}
