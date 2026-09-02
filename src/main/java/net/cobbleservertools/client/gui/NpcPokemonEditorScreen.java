package net.cobbleservertools.client.gui;

import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class NpcPokemonEditorScreen extends Screen {
   private final NpcBattleTeamEditorScreen parent;
   private final int slot;
   private final NpcBattleTeamEditorScreen.PokemonDraft draft;
   private EditBox level;
   private final EditBox[] ivs = new EditBox[6];
   private final EditBox[] evs = new EditBox[6];

   public NpcPokemonEditorScreen(NpcBattleTeamEditorScreen var1, int var2, NpcBattleTeamEditorScreen.PokemonDraft var3) {
      super(Component.literal("Pokémon Slot " + (var2 + 1)));
      this.parent = var1;
      this.slot = var2;
      this.draft = var3 == null ? new NpcBattleTeamEditorScreen.PokemonDraft() : var3.copy();
   }

   protected void init() {
      int var1 = Math.min(610, this.width - 18);
      int var2 = (this.width - var1) / 2;
      int var3 = Math.max(5, (this.height - 430) / 2);
      int var4 = var2 + 124;
      int var5 = var1 - 142;
      this.addPick(var4, var3 + 30, var5, "Species", this.draft.species, NpcCreatorCatalog.Kind.POKEMON, "", var1x -> {
         this.draft.species = var1x;
         this.draft.form = "";
      });
      this.addPick(var4, var3 + 56, var5, "Form", this.draft.form, NpcCreatorCatalog.Kind.FORM, this.draft.species, var1x -> this.draft.form = var1x);
      this.level = this.edit(var4, var3 + 82, 72, "Level", Integer.toString(this.draft.level), 3);
      this.level.setFilter(var0 -> var0.matches("\\d{0,3}"));
      this.addRenderableWidget(Button.builder(Component.literal("Gender: " + NpcCreatorCatalog.pretty(this.draft.gender)), var1x -> {
         this.draft.gender = nextGender(this.draft.gender);
         var1x.setMessage(Component.literal("Gender: " + NpcCreatorCatalog.pretty(this.draft.gender)));
      }).bounds(var4 + 80, var3 + 82, 150, 20).build());
      this.addRenderableWidget(Button.builder(Component.literal("Shiny: " + (this.draft.shiny ? "Yes" : "No")), var1x -> {
         this.draft.shiny = !this.draft.shiny;
         var1x.setMessage(Component.literal("Shiny: " + (this.draft.shiny ? "Yes" : "No")));
      }).bounds(var4 + 238, var3 + 82, 110, 20).build());
      this.addPick(var4, var3 + 108, var5, "Nature", this.draft.nature, NpcCreatorCatalog.Kind.NATURE, "", var1x -> this.draft.nature = var1x);
      this.addPick(var4, var3 + 134, var5, "Ability", this.draft.ability, NpcCreatorCatalog.Kind.ABILITY, "", var1x -> this.draft.ability = var1x);
      this.addPick(var4, var3 + 160, var5, "Held item", this.draft.heldItem, NpcCreatorCatalog.Kind.ITEM, "", var1x -> this.draft.heldItem = var1x);

      for (int var6 = 0; var6 < 4; var6++) {
         int var7 = var6;
         this.addPick(
            var4,
            var3 + 192 + var6 * 24,
            var5,
            "Move " + (var6 + 1),
            this.draft.moves[var6],
            NpcCreatorCatalog.Kind.MOVE,
            "",
            var2x -> this.draft.moves[var7] = var2x
         );
      }

      String[] var12 = new String[]{"HP", "Atk", "Def", "SpA", "SpD", "Spe"};

      for (int var13 = 0; var13 < 6; var13++) {
         int var8 = var13 % 3;
         int var9 = var13 / 3;
         int var10 = var4 + var8 * 118;
         int var11 = var3 + 298 + var9 * 26;
         this.ivs[var13] = this.edit(var10, var11, 46, var12[var13] + " IV", Integer.toString(this.draft.ivs[var13]), 2);
         this.ivs[var13].setFilter(var0 -> var0.matches("\\d{0,2}"));
         this.evs[var13] = this.edit(var10 + 50, var11, 54, var12[var13] + " EV", Integer.toString(this.draft.evs[var13]), 3);
         this.evs[var13].setFilter(var0 -> var0.matches("\\d{0,3}"));
      }

      this.addRenderableWidget(Button.builder(Component.literal("Save Pokémon"), var1x -> this.save()).bounds(var2 + var1 / 2 - 94, var3 + 382, 90, 20).build());
      this.addRenderableWidget(
         Button.builder(Component.literal("Cancel"), var1x -> this.minecraft.setScreen(this.parent)).bounds(var2 + var1 / 2 + 4, var3 + 382, 90, 20).build()
      );
   }

   private void addPick(int var1, int var2, int var3, String var4, String var5, NpcCreatorCatalog.Kind var6, String var7, Consumer<String> var8) {
      this.addRenderableWidget(Button.builder(Component.literal(var4 + ": " + display(var5)), var6x -> {
         this.captureNumeric();
         this.minecraft.setScreen(new SearchableCatalogScreen(this, var6, var7, var5, var3xx -> {
            var8.accept(var3xx);
            var6x.setMessage(Component.literal(var4 + ": " + display(var3xx)));
         }));
      }).bounds(var1, var2, var3, 20).build());
   }

   private EditBox edit(int var1, int var2, int var3, String var4, String var5, int var6) {
      EditBox var7 = new EditBox(this.font, var1, var2, var3, 20, Component.literal(var4));
      var7.setMaxLength(var6);
      var7.setValue(var5);
      this.addRenderableWidget(var7);
      return var7;
   }

   private static String display(String var0) {
      return var0 != null && !var0.isBlank() ? NpcCreatorCatalog.pretty(var0) : "None";
   }

   private static String nextGender(String var0) {
      return switch (var0 == null ? "random" : var0.toLowerCase()) {
         case "random" -> "male";
         case "male" -> "female";
         case "female" -> "genderless";
         default -> "random";
      };
   }

   private void captureNumeric() {
      if (this.level != null) {
         this.draft.level = clamp(parse(this.level.getValue(), this.draft.level), 1, 100);
      }

      for (int var1 = 0; var1 < 6; var1++) {
         if (this.ivs[var1] != null) {
            this.draft.ivs[var1] = clamp(parse(this.ivs[var1].getValue(), this.draft.ivs[var1]), 0, 31);
         }

         if (this.evs[var1] != null) {
            this.draft.evs[var1] = clamp(parse(this.evs[var1].getValue(), this.draft.evs[var1]), 0, 252);
         }
      }
   }

   private void save() {
      this.captureNumeric();
      if (this.draft.species != null && !this.draft.species.isBlank()) {
         this.parent.applyPokemon(this.slot, this.draft);
         this.minecraft.setScreen(this.parent);
      }
   }

   private static int parse(String var0, int var1) {
      try {
         return Integer.parseInt(var0);
      } catch (Exception var3) {
         return var1;
      }
   }

   private static int clamp(int var0, int var1, int var2) {
      return Math.max(var1, Math.min(var2, var0));
   }

   public void renderBackground(GuiGraphics var1, int var2, int var3, float var4) {
      var1.fill(0, 0, this.width, this.height, -2013265920);
      int var5 = Math.min(610, this.width - 18);
      int var6 = (this.width - var5) / 2;
      int var7 = Math.max(5, (this.height - 430) / 2);
      var1.fill(var6, var7, var6 + var5, var7 + 414, -233103319);
      var1.fill(var6, var7, var6 + var5, var7 + 2, -1653151);
   }

   public void render(GuiGraphics var1, int var2, int var3, float var4) {
      super.render(var1, var2, var3, var4);
      int var5 = Math.min(610, this.width - 18);
      int var6 = (this.width - var5) / 2;
      int var7 = Math.max(5, (this.height - 430) / 2);
      var1.drawCenteredString(this.font, this.title, this.width / 2, var7 + 10, -1653151);
      String[] var8 = new String[]{"Species", "Form", "Level", "Nature", "Ability", "Held item"};
      int[] var9 = new int[]{36, 62, 88, 114, 140, 166};

      for (int var10 = 0; var10 < var8.length; var10++) {
         var1.drawString(this.font, Component.literal(var8[var10]), var6 + 14, var7 + var9[var10], -1, false);
      }

      var1.drawString(this.font, Component.literal("Moves"), var6 + 14, var7 + 198, -1, false);
      var1.drawString(this.font, Component.literal("IV / EV values"), var6 + 14, var7 + 304, -1, false);
   }

   public boolean isPauseScreen() {
      return false;
   }
}
