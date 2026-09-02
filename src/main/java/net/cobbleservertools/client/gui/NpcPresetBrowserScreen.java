package net.cobbleservertools.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.cobbleservertools.network.payload.ApplyNpcPresetPayload;
import net.cobbleservertools.network.payload.NpcPresetSearchRequestPayload;
import net.cobbleservertools.network.payload.NpcPresetSearchResultsPayload;
import net.cobbleservertools.network.payload.OpenNpcPresetBrowserPayload;
import net.cobbleservertools.network.payload.RequestNpcSnapshotPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;

public final class NpcPresetBrowserScreen extends Screen {
   private static final int PANEL_WIDTH = 540;
   private static final int PANEL_HEIGHT = 390;
   private final int entityId;
   private final String browserType;
   private final String currentPresetId;
   private final CompoundTag results;
   private final String initialQuery;
   private String region;
   private String role;
   private String location;
   private String trainerClass;
   private String tag;
   private EditBox searchBox;
   private boolean requestedInitial;

   public NpcPresetBrowserScreen(OpenNpcPresetBrowserPayload var1) {
      this(var1.entityId(), var1.browserType(), var1.currentPresetId(), new CompoundTag(), "", "", "", "", "", "");
   }

   public NpcPresetBrowserScreen(NpcPresetSearchResultsPayload var1) {
      this(
         var1.entityId(),
         var1.browserType(),
         var1.results().getString("CurrentPresetId"),
         var1.results(),
         var1.query(),
         var1.region(),
         var1.role(),
         var1.location(),
         var1.trainerClass(),
         var1.tag()
      );
   }

   private NpcPresetBrowserScreen(
      int var1, String var2, String var3, CompoundTag var4, String var5, String var6, String var7, String var8, String var9, String var10
   ) {
      super(Component.literal("CobbleServerTools Preset Library"));
      this.entityId = var1;
      this.browserType = var2 == null ? "" : var2;
      this.currentPresetId = var3 == null ? "" : var3;
      this.results = var4 == null ? new CompoundTag() : var4.copy();
      this.initialQuery = var5 == null ? "" : var5;
      this.region = var6 == null ? "" : var6;
      this.role = var7 == null ? "" : var7;
      this.location = var8 == null ? "" : var8;
      this.trainerClass = var9 == null ? "" : var9;
      this.tag = var10 == null ? "" : var10;
   }

   protected void init() {
      int var1 = Math.min(540, this.width - 20);
      int var2 = Math.min(390, this.height - 16);
      int var3 = (this.width - var1) / 2;
      int var4 = Math.max(8, (this.height - var2) / 2);
      this.searchBox = new EditBox(this.font, var3 + 16, var4 + 34, var1 - 144, 20, Component.literal("Search presets"));
      this.searchBox.setMaxLength(128);
      this.searchBox.setValue(this.initialQuery);
      this.addRenderableWidget(this.searchBox);
      this.addRenderableWidget(
         Button.builder(Component.literal("Search"), var1x -> this.request(0, false)).bounds(var3 + var1 - 120, var4 + 34, 50, 20).build()
      );
      this.addRenderableWidget(Button.builder(Component.literal("Reload"), var1x -> this.request(0, true)).bounds(var3 + var1 - 64, var4 + 34, 48, 20).build());
      int var5 = var4 + 60;
      int var6 = Math.max(86, (var1 - 44) / 5);
      this.addFacetButton(var3 + 16, var5, var6, "Region", "Regions", () -> this.region, var1x -> this.region = var1x);
      this.addFacetButton(var3 + 20 + var6, var5, var6, "Role", "Roles", () -> this.role, var1x -> this.role = var1x);
      this.addFacetButton(var3 + 24 + var6 * 2, var5, var6, "Location", "Locations", () -> this.location, var1x -> this.location = var1x);
      this.addFacetButton(var3 + 28 + var6 * 3, var5, var6, "Class", "TrainerClasses", () -> this.trainerClass, var1x -> this.trainerClass = var1x);
      this.addFacetButton(var3 + 32 + var6 * 4, var5, var6, "Tag", "Tags", () -> this.tag, var1x -> this.tag = var1x);
      ListTag var7 = this.results.getList("Results", 10);
      int var8 = var4 + 91;
      int var9 = var1 - 32;

      for (int var10 = 0; var10 < var7.size() && var10 < 8; var10++) {
         CompoundTag var11 = var7.getCompound(var10);
         String var12 = var11.getString("Id");
         String var13 = var11.getString("DisplayName");
         String var14 = details(var11);
         String var15 = truncate(var13 + (var14.isBlank() ? "" : "  |  " + var14), 72);
         this.addRenderableWidget(Button.builder(Component.literal(var15), var2x -> this.apply(var12)).bounds(var3 + 16, var8 + var10 * 25, var9, 22).build());
      }

      int var16 = this.results.getInt("Page");
      int var17 = Math.max(1, this.results.getInt("Pages"));
      int var18 = var4 + var2 - 29;
      Button var19 = Button.builder(Component.literal("< Prev"), var2x -> this.request(Math.max(0, var16 - 1), false)).bounds(var3 + 16, var18, 64, 20).build();
      var19.active = var16 > 0;
      this.addRenderableWidget(var19);
      Button var20 = Button.builder(Component.literal("Next >"), var3x -> this.request(Math.min(var17 - 1, var16 + 1), false))
         .bounds(var3 + 86, var18, 64, 20)
         .build();
      var20.active = var16 + 1 < var17;
      this.addRenderableWidget(var20);
      this.addRenderableWidget(
         Button.builder(Component.literal("Edit manually"), var1x -> this.editManually()).bounds(var3 + var1 - 206, var18, 92, 20).build()
      );
      this.addRenderableWidget(Button.builder(Component.literal("Cancel"), var1x -> this.onClose()).bounds(var3 + var1 - 106, var18, 90, 20).build());
      if (!this.results.contains("Results") && !this.requestedInitial) {
         this.requestedInitial = true;
         this.request(0, false);
      }

      RoamingUiHooks.install(this);
   }

   private void addFacetButton(int var1, int var2, int var3, String var4, String var5, Supplier<String> var6, Consumer<String> var7) {
      List var8 = readStrings(this.results.getList(var5, 8));
      this.addRenderableWidget(Button.builder(Component.literal(var4 + ": " + display((String)var6.get())), var5x -> {
         String var6x = nextFacet((String)var6.get(), var8);
         var7.accept(var6x);
         var5x.setMessage(Component.literal(var4 + ": " + display(var6x)));
         this.request(0, false);
      }).bounds(var1, var2, var3, 20).build());
   }

   private void request(int var1, boolean var2) {
      PacketDistributor.sendToServer(
         new NpcPresetSearchRequestPayload(
            this.entityId,
            this.browserType,
            this.searchBox == null ? this.initialQuery : this.searchBox.getValue(),
            this.region,
            this.role,
            this.location,
            this.trainerClass,
            this.tag,
            Math.max(0, var1),
            var2
         ),
         new CustomPacketPayload[0]
      );
   }

   private void apply(String var1) {
      PacketDistributor.sendToServer(new ApplyNpcPresetPayload(this.entityId, this.browserType, var1), new CustomPacketPayload[0]);
      if (this.minecraft != null) {
         this.minecraft.setScreen(null);
      }
   }

   private void editManually() {
      PacketDistributor.sendToServer(new RequestNpcSnapshotPayload(this.entityId), new CustomPacketPayload[0]);
      if (this.minecraft != null) {
         this.minecraft.setScreen(null);
      }
   }

   private static List<String> readStrings(ListTag var0) {
      ArrayList var1 = new ArrayList();

      for (int var2 = 0; var2 < var0.size(); var2++) {
         String var3 = var0.getString(var2);
         if (!var3.isBlank()) {
            var1.add(var3);
         }
      }

      return var1;
   }

   private static String nextFacet(String var0, List<String> var1) {
      if (var1.isEmpty()) {
         return "";
      } else if (var0 != null && !var0.isBlank()) {
         int var2 = var1.indexOf(var0);
         return var2 >= 0 && var2 + 1 < var1.size() ? (String)var1.get(var2 + 1) : "";
      } else {
         return (String)var1.getFirst();
      }
   }

   private static String display(String var0) {
      return var0 != null && !var0.isBlank() ? var0.replace('_', ' ') : "All";
   }

   private static String details(CompoundTag var0) {
      StringBuilder var1 = new StringBuilder();
      add(var1, var0.getString("Region"));
      add(var1, var0.getString("Location"));
      add(var1, var0.getString("Role"));
      add(var1, var0.getString("TrainerClass"));
      return var1.toString();
   }

   private static void add(StringBuilder var0, String var1) {
      if (var1 != null && !var1.isBlank()) {
         if (var0.length() > 0) {
            var0.append(" / ");
         }

         var0.append(var1.replace('_', ' '));
      }
   }

   private static String truncate(String var0, int var1) {
      if (var0 == null) {
         return "";
      } else {
         return var0.length() <= var1 ? var0 : var0.substring(0, Math.max(0, var1 - 3)) + "...";
      }
   }

   public void renderBackground(GuiGraphics var1, int var2, int var3, float var4) {
      var1.fill(0, 0, this.width, this.height, -2013265920);
      int var5 = Math.min(540, this.width - 20);
      int var6 = Math.min(390, this.height - 16);
      int var7 = (this.width - var5) / 2;
      int var8 = Math.max(8, (this.height - var6) / 2);
      var1.fill(var7, var8, var7 + var5, var8 + var6, -233103319);
      var1.fill(var7, var8, var7 + var5, var8 + 2, -1653151);
      var1.fill(var7, var8 + var6 - 2, var7 + var5, var8 + var6, -1653151);
   }

   public void render(GuiGraphics var1, int var2, int var3, float var4) {
      super.render(var1, var2, var3, var4);
      int var5 = Math.min(540, this.width - 20);
      int var6 = Math.min(390, this.height - 16);
      int var7 = (this.width - var5) / 2;
      int var8 = Math.max(8, (this.height - var6) / 2);
      var1.drawCenteredString(this.font, this.title, this.width / 2, var8 + 10, -1653151);
      String var9 = "Type: " + display(this.browserType) + "   |   " + this.results.getInt("Total") + " preset(s)";
      if (!this.currentPresetId.isBlank()) {
         var9 = var9 + "   |   Current: " + this.currentPresetId;
      }

      var1.drawString(this.font, Component.literal(var9), var7 + 16, var8 + 22, -4669236, false);
      int var10 = this.results.getInt("Page");
      int var11 = Math.max(1, this.results.getInt("Pages"));
      var1.drawCenteredString(this.font, Component.literal("Page " + (var10 + 1) + " / " + var11), this.width / 2, var8 + var6 - 25, -4669236);
      if (this.results.contains("Results") && this.results.getList("Results", 10).isEmpty()) {
         var1.drawCenteredString(this.font, Component.literal("No presets match the current search and filters."), this.width / 2, var8 + 132, -6511697);
      }
   }

   public boolean isPauseScreen() {
      return false;
   }
}
