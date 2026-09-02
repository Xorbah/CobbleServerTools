package net.cobbleservertools.client.gui;

import java.util.function.Consumer;
import java.util.function.Supplier;
import net.cobbleservertools.network.payload.UpdateNpcProfilePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;

public final class NpcProfileEditorScreen extends Screen {
   private final int entityId;
   private final CompoundTag original;
   private final String kind;
   private EditBox name;
   private EditBox dialog;
   private EditBox skin;
   private EditBox size;
   private EditBox extraNumber;
   private EditBox command;
   private boolean slim;
   private boolean sitting;
   private boolean seek;
   private boolean locked;
   private boolean look;
   private boolean serviceToggle;
   private boolean heal;
   private String speciesA = "";
   private String speciesB = "";
   private String selectedItem = "";
   private String selectedMove = "";
   private String gender = "random";

   public NpcProfileEditorScreen(int var1, CompoundTag var2) {
      super(Component.literal("CobbleServerTools NPC Creator"));
      this.entityId = var1;
      this.original = var2 == null ? new CompoundTag() : var2.copy();
      this.kind = this.original.getString("NpcKind");
      this.slim = "SLIM".equalsIgnoreCase(this.original.getString("NpcType"));
      this.sitting = this.original.getBoolean("Sitting");
      this.seek = !this.original.contains("SeekPlayer") || this.original.getBoolean("SeekPlayer");
      this.locked = this.original.getBoolean("LockPos");
      this.look = this.original.getBoolean("LookAtPlayer");
      this.serviceToggle = this.original.getBoolean("unlimitedTrade");
      this.heal = this.original.getBoolean("HealAfterDialog");
      this.speciesA = this.original.getString("tradeRequestId");
      this.speciesB = this.original.getString("tradeOfferId");
      this.gender = first(this.original.getString("tradeOfferGender"), "random");
      this.selectedItem = this.original.getString("RewardItemId");
      this.selectedMove = this.original.getString("TutorMoveId");
   }

   protected void init() {
      int var1 = Math.min(620, this.width - 18);
      int var2 = (this.width - var1) / 2;
      int var3 = Math.max(6, (this.height - 410) / 2);
      int var4 = var2 + 132;
      int var5 = var1 - 152;
      this.name = this.edit(var4, var3 + 32, var5, this.original.getString("NpcName"), 128, "NPC name");
      this.dialog = this.edit(var4, var3 + 58, var5, this.original.getString("DialogId"), 128, "Dialogue ID");
      this.skin = this.edit(var4, var3 + 84, var5, this.original.getString("NpcSkin"), 256, "Skin resource");
      this.size = this.edit(var4, var3 + 110, 76, this.original.contains("NpcSize") ? Float.toString(this.original.getFloat("NpcSize")) : "0.9375", 8, "Scale");
      this.size.setFilter(var0 -> var0.matches("\\d{0,1}(?:\\.\\d{0,4})?"));
      int var6 = var3 + 140;
      this.addToggle(var2 + 18, var6, 104, "Model", () -> this.slim ? "Slim" : "Wide", () -> this.slim = !this.slim);
      this.addToggle(var2 + 130, var6, 104, "Sitting", () -> on(this.sitting), () -> this.sitting = !this.sitting);
      this.addToggle(var2 + 242, var6, 104, "Seek", () -> on(this.seek), () -> this.seek = !this.seek);
      this.addToggle(var2 + 354, var6, 104, "Locked", () -> on(this.locked), () -> this.locked = !this.locked);
      this.addToggle(var2 + 466, var6, 128, "Look", () -> on(this.look), () -> this.look = !this.look);
      this.initTypeSpecific(var2, var3, var1, var4, var5);
      this.addRenderableWidget(Button.builder(Component.literal("Save NPC"), var1x -> this.save()).bounds(var2 + var1 / 2 - 100, var3 + 372, 94, 20).build());
      this.addRenderableWidget(Button.builder(Component.literal("Cancel"), var1x -> this.onClose()).bounds(var2 + var1 / 2 + 6, var3 + 372, 94, 20).build());
      SkinUiHooks.install(this);
   }

   private void initTypeSpecific(int var1, int var2, int var3, int var4, int var5) {
      int var6 = var2 + 184;
      if ("battle".equals(this.kind) || "rival".equals(this.kind)) {
         this.addRenderableWidget(
            Button.builder(
                  Component.literal("Open Pokémon Team Builder"), var1x -> this.captureAndOpen(new NpcBattleTeamEditorScreen(this.entityId, this.merged()))
               )
               .bounds(var4, var6, 220, 22)
               .build()
         );
         this.addRenderableWidget(
            Button.builder(
                  Component.literal("Battle / Reward Settings"), var1x -> this.captureAndOpen(new NpcBattleSettingsEditorScreen(this.entityId, this.merged()))
               )
               .bounds(var4 + 228, var6, Math.min(190, var5 - 228), 22)
               .build()
         );
      } else if ("mart".equals(this.kind)) {
         this.addRenderableWidget(
            Button.builder(
                  Component.literal("Edit Shop Inventory (All Mod Items)"),
                  var1x -> this.captureAndOpen(new NpcMartInventoryEditorScreen(this.entityId, this.merged()))
               )
               .bounds(var4, var6, Math.min(300, var5), 22)
               .build()
         );
      } else if ("trader".equals(this.kind)) {
         this.addPick(var4, var6, var5, "Requested Pokémon", this.speciesA, NpcCreatorCatalog.Kind.POKEMON, "", var1x -> this.speciesA = var1x);
         this.addPick(var4, var6 + 28, var5, "Offered Pokémon", this.speciesB, NpcCreatorCatalog.Kind.POKEMON, "", var1x -> this.speciesB = var1x);
         this.extraNumber = this.edit(
            var4, var6 + 56, 76, Integer.toString(this.original.contains("tradeOfferLevel") ? this.original.getInt("tradeOfferLevel") : 10), 3, "Level"
         );
         this.extraNumber.setFilter(var0 -> var0.matches("\\d{0,3}"));
         this.addRenderableWidget(Button.builder(Component.literal("Gender: " + NpcCreatorCatalog.pretty(this.gender)), var1x -> {
            this.gender = nextGender(this.gender);
            var1x.setMessage(Component.literal("Gender: " + NpcCreatorCatalog.pretty(this.gender)));
         }).bounds(var4 + 84, var6 + 56, 150, 20).build());
         this.addToggle(var4 + 242, var6 + 56, 130, "Unlimited", () -> on(this.serviceToggle), () -> this.serviceToggle = !this.serviceToggle);
      } else if ("move_tutor".equals(this.kind)) {
         this.addPick(var4, var6, var5, "Tutored Move", this.selectedMove, NpcCreatorCatalog.Kind.MOVE, "", var1x -> this.selectedMove = var1x);
         this.addPick(var4, var6 + 28, var5, "Item / Cost", this.selectedItem, NpcCreatorCatalog.Kind.ITEM, "", var1x -> this.selectedItem = var1x);
      } else if ("dialog".equals(this.kind)) {
         this.addToggle(var4, var6, 150, "Heal after dialog", () -> on(this.heal), () -> this.heal = !this.heal);
         this.addPick(var4, var6 + 28, var5, "Reward Item", this.selectedItem, NpcCreatorCatalog.Kind.ITEM, "", var1x -> this.selectedItem = var1x);
         this.command = this.edit(var4, var6 + 56, var5, this.original.getString("OnVictoryCommand"), 2048, "Command after dialog");
      }
   }

   private EditBox edit(int var1, int var2, int var3, String var4, int var5, String var6) {
      EditBox var7 = new EditBox(this.font, var1, var2, Math.max(40, var3), 20, Component.literal(var6));
      var7.setMaxLength(var5);
      var7.setValue(var4 == null ? "" : var4);
      this.addRenderableWidget(var7);
      return var7;
   }

   private void addToggle(int var1, int var2, int var3, String var4, Supplier<String> var5, Runnable var6) {
      this.addRenderableWidget(Button.builder(Component.literal(var4 + ": " + (String)var5.get()), var3x -> {
         var6.run();
         var3x.setMessage(Component.literal(var4 + ": " + (String)var5.get()));
      }).bounds(var1, var2, var3, 20).build());
   }

   private void addPick(int var1, int var2, int var3, String var4, String var5, NpcCreatorCatalog.Kind var6, String var7, Consumer<String> var8) {
      this.addRenderableWidget(Button.builder(Component.literal(var4 + ": " + disp(var5)), var6x -> {
         this.capture();
         this.minecraft.setScreen(new SearchableCatalogScreen(this, var6, var7, var5, var3xx -> {
            var8.accept(var3xx);
            var6x.setMessage(Component.literal(var4 + ": " + disp(var3xx)));
         }));
      }).bounds(var1, var2, var3, 20).build());
   }

   private void captureAndOpen(Screen var1) {
      this.capture();
      this.minecraft.setScreen(var1);
   }

   private void capture() {
      if (this.name != null) {
         this.applyBasic(this.original);
         this.applyType(this.original);
      }
   }

   private CompoundTag merged() {
      CompoundTag var1 = this.original.copy();
      this.applyBasic(var1);
      this.applyType(var1);
      return var1;
   }

   private void save() {
      CompoundTag var1 = new CompoundTag();
      this.applyBasic(var1);
      this.applyType(var1);
      PacketDistributor.sendToServer(new UpdateNpcProfilePayload(this.entityId, var1), new CustomPacketPayload[0]);
      this.minecraft.setScreen(null);
   }

   private void applyBasic(CompoundTag var1) {
      float var2 = 0.9375F;

      try {
         var2 = Float.parseFloat(this.size.getValue());
      } catch (Exception var4) {
      }

      var2 = Math.max(0.25F, Math.min(4.0F, var2));
      var1.putString("NpcName", this.name.getValue());
      var1.putString("DialogId", this.dialog.getValue());
      var1.putString("NpcSkin", this.skin.getValue());
      var1.putFloat("NpcSize", var2);
      var1.putString("NpcType", this.slim ? "SLIM" : "WIDE");
      var1.putBoolean("Sitting", this.sitting);
      var1.putBoolean("SeekPlayer", this.seek);
      var1.putBoolean("LockPos", this.locked);
      var1.putBoolean("LookAtPlayer", this.look);
   }

   private void applyType(CompoundTag var1) {
      if ("trader".equals(this.kind)) {
         var1.putString("tradeRequestId", this.speciesA);
         var1.putString("tradeOfferId", this.speciesB);
         var1.putInt("tradeOfferLevel", clamp(num(this.extraNumber == null ? "10" : this.extraNumber.getValue(), 10), 1, 100));
         var1.putString("tradeOfferGender", this.gender);
         var1.putBoolean("unlimitedTrade", this.serviceToggle);
      } else if ("move_tutor".equals(this.kind)) {
         var1.putString("TutorMoveId", this.selectedMove);
         var1.putString("RewardItemId", this.selectedItem);
      } else if ("dialog".equals(this.kind)) {
         var1.putBoolean("HealAfterDialog", this.heal);
         var1.putString("RewardItemId", this.selectedItem);
         var1.putString("OnVictoryCommand", this.command == null ? "" : this.command.getValue());
      }
   }

   private static int num(String var0, int var1) {
      try {
         return Integer.parseInt(var0);
      } catch (Exception var3) {
         return var1;
      }
   }

   private static int clamp(int var0, int var1, int var2) {
      return Math.max(var1, Math.min(var2, var0));
   }

   private static String nextGender(String var0) {
      return switch (var0 == null ? "random" : var0.toLowerCase()) {
         case "random" -> "male";
         case "male" -> "female";
         case "female" -> "genderless";
         default -> "random";
      };
   }

   private static String first(String var0, String var1) {
      return var0 != null && !var0.isBlank() ? var0 : var1;
   }

   private static String on(boolean var0) {
      return var0 ? "On" : "Off";
   }

   private static String disp(String var0) {
      return var0 != null && !var0.isBlank() ? NpcCreatorCatalog.pretty(var0) : "None";
   }

   public void renderBackground(GuiGraphics var1, int var2, int var3, float var4) {
      var1.fill(0, 0, this.width, this.height, -2013265920);
      int var5 = Math.min(620, this.width - 18);
      int var6 = (this.width - var5) / 2;
      int var7 = Math.max(6, (this.height - 410) / 2);
      var1.fill(var6, var7, var6 + var5, var7 + 402, -233103319);
      var1.fill(var6, var7, var6 + var5, var7 + 2, -1653151);
   }

   public void render(GuiGraphics var1, int var2, int var3, float var4) {
      super.render(var1, var2, var3, var4);
      int var5 = Math.min(620, this.width - 18);
      int var6 = (this.width - var5) / 2;
      int var7 = Math.max(6, (this.height - 410) / 2);
      var1.drawCenteredString(
         this.font, Component.literal("CobbleServerTools " + NpcCreatorCatalog.pretty(this.kind) + " Creator"), this.width / 2, var7 + 10, -1653151
      );
      var1.drawString(this.font, Component.literal("Name"), var6 + 18, var7 + 38, -1, false);
      var1.drawString(this.font, Component.literal("Dialogue"), var6 + 18, var7 + 64, -1, false);
      var1.drawString(this.font, Component.literal("Skin"), var6 + 18, var7 + 90, -1, false);
      var1.drawString(this.font, Component.literal("Scale"), var6 + 18, var7 + 116, -1, false);
      var1.drawString(this.font, Component.literal("Type-specific configuration"), var6 + 18, var7 + 174, -1653151, false);
      var1.drawString(
         this.font,
         Component.literal("Search selectors read the registries synchronized by Minecraft/Cobblemon, including addons."),
         var6 + 18,
         var7 + 342,
         -6511697,
         false
      );
      SkinUiHooks.renderPreview(this, var1);
   }

   public boolean isPauseScreen() {
      return false;
   }
}
