package net.cobbleservertools.client.gui;

import java.util.Locale;
import net.cobbleservertools.network.payload.UpdateNpcProfilePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;

public final class NpcBattleTeamEditorScreen extends Screen {
   private static final String[] BRANCHES = new String[]{"Fire", "Water", "Grass"};
   final int entityId;
   final CompoundTag working;
   final boolean rival;
   final int branch;
   private final NpcBattleTeamEditorScreen.PokemonDraft[] team = new NpcBattleTeamEditorScreen.PokemonDraft[6];

   public NpcBattleTeamEditorScreen(int var1, CompoundTag var2) {
      this(var1, var2, 0);
   }

   private NpcBattleTeamEditorScreen(int var1, CompoundTag var2, int var3) {
      super(Component.literal("CobbleServerTools Team Builder"));
      this.entityId = var1;
      this.working = var2 == null ? new CompoundTag() : var2.copy();
      this.rival = "rival".equalsIgnoreCase(this.working.getString("NpcKind"));
      this.branch = this.rival ? Math.floorMod(var3, 3) : 0;
      this.read();
   }

   protected void init() {
      int var1 = Math.min(560, this.width - 18);
      int var2 = (this.width - var1) / 2;
      int var3 = Math.max(8, (this.height - 300) / 2);
      if (this.rival) {
         this.addRenderableWidget(Button.builder(Component.literal("<"), var1x -> this.switchBranch(-1)).bounds(var2 + 12, var3 + 22, 24, 20).build());
         this.addRenderableWidget(Button.builder(Component.literal(">"), var1x -> this.switchBranch(1)).bounds(var2 + var1 - 36, var3 + 22, 24, 20).build());
      }

      for (int var4 = 0; var4 < 6; var4++) {
         int var5 = var4;
         this.addRenderableWidget(
            Button.builder(Component.literal(this.teamLabel(var4)), var2x -> this.minecraft.setScreen(new NpcPokemonEditorScreen(this, var5, this.team[var5])))
               .bounds(var2 + 24, var3 + 54 + var4 * 31, var1 - 116, 24)
               .build()
         );
         this.addRenderableWidget(Button.builder(Component.literal("Clear"), var2x -> {
            this.team[var5] = null;
            this.store();
            this.minecraft.setScreen(new NpcBattleTeamEditorScreen(this.entityId, this.working, this.branch));
         }).bounds(var2 + var1 - 84, var3 + 56 + var4 * 31, 60, 20).build());
      }

      this.addRenderableWidget(Button.builder(Component.literal("Save Team"), var1x -> this.save()).bounds(var2 + var1 / 2 - 92, var3 + 250, 86, 20).build());
      this.addRenderableWidget(
         Button.builder(Component.literal("Cancel"), var1x -> this.minecraft.setScreen(null)).bounds(var2 + var1 / 2 + 6, var3 + 250, 86, 20).build()
      );
   }

   private String teamLabel(int var1) {
      NpcBattleTeamEditorScreen.PokemonDraft var2 = this.team[var1];
      if (var2 != null && !var2.species.isBlank()) {
         String var3 = "";
         int var4 = 0;

         for (String var8 : var2.moves) {
            if (var8 != null && !var8.isBlank()) {
               var4++;
            }
         }

         return var1
            + 1
            + ". "
            + NpcCreatorCatalog.pretty(var2.species)
            + "  Lv."
            + var2.level
            + "  "
            + NpcCreatorCatalog.pretty(var2.gender)
            + "  • "
            + var4
            + " moves";
      } else {
         return var1 + 1 + ". [ Empty — click to add Pokémon ]";
      }
   }

   void applyPokemon(int var1, NpcBattleTeamEditorScreen.PokemonDraft var2) {
      this.team[var1] = var2 == null ? null : var2.copy();
   }

   private void switchBranch(int var1) {
      this.store();
      this.minecraft.setScreen(new NpcBattleTeamEditorScreen(this.entityId, this.working, this.branch + var1));
   }

   private void store() {
      if (this.rival) {
         CompoundTag var1 = this.working.contains("RivalTeams", 10) ? this.working.getCompound("RivalTeams").copy() : new CompoundTag();
         var1.put(BRANCHES[this.branch], this.writeRival());
         this.working.put("RivalTeams", var1);
      } else {
         this.working.put("NpcPokemons", this.writeBattle());
      }
   }

   private void save() {
      this.store();
      CompoundTag var1 = new CompoundTag();
      if (this.rival) {
         var1.put("RivalTeams", this.working.getCompound("RivalTeams").copy());
      } else {
         var1.put("NpcPokemons", this.working.getList("NpcPokemons", 10).copy());
      }

      PacketDistributor.sendToServer(new UpdateNpcProfilePayload(this.entityId, var1), new CustomPacketPayload[0]);
      this.minecraft.setScreen(null);
   }

   private ListTag writeBattle() {
      ListTag var1 = new ListTag();

      for (NpcBattleTeamEditorScreen.PokemonDraft var5 : this.team) {
         if (var5 != null && !var5.species.isBlank()) {
            CompoundTag var6 = new CompoundTag();
            var6.putString("Species", var5.species);
            var6.putString("Properties", var5.properties());
            var6.putInt("Level", var5.level);
            var6.putString("Gender", var5.gender);
            ListTag var7 = new ListTag();

            for (String var11 : var5.moves) {
               if (var11 != null && !var11.isBlank()) {
                  CompoundTag var12 = new CompoundTag();
                  var12.putString("MoveId", var11);
                  var7.add(var12);
               }
            }

            var6.put("Moves", var7);
            var1.add(var6);
         }
      }

      return var1;
   }

   private ListTag writeRival() {
      ListTag var1 = new ListTag();

      for (NpcBattleTeamEditorScreen.PokemonDraft var5 : this.team) {
         if (var5 != null && !var5.species.isBlank()) {
            CompoundTag var6 = new CompoundTag();
            var6.putString("Species", var5.species);
            var6.putString("Properties", var5.properties());
            var6.putInt("Level", var5.level);
            var6.putString("Gender", var5.gender);
            ListTag var7 = new ListTag();

            for (String var11 : var5.moves) {
               if (var11 != null && !var11.isBlank()) {
                  var7.add(StringTag.valueOf(var11));
               }
            }

            var6.put("Moves", var7);
            var1.add(var6);
         }
      }

      return var1;
   }

   private void read() {
      ListTag var1;
      if (this.rival) {
         CompoundTag var2 = this.working.getCompound("RivalTeams");
         String var3 = BRANCHES[this.branch];
         var1 = var2.contains(var3, 9) ? var2.getList(var3, 10) : var2.getList(var3.toLowerCase(Locale.ROOT), 10);
      } else {
         var1 = this.working.getList("NpcPokemons", 10);
      }

      for (int var4 = 0; var4 < Math.min(6, var1.size()); var4++) {
         this.team[var4] = from(var1.getCompound(var4), this.rival);
      }
   }

   private static NpcBattleTeamEditorScreen.PokemonDraft from(CompoundTag var0, boolean var1) {
      NpcBattleTeamEditorScreen.PokemonDraft var2 = new NpcBattleTeamEditorScreen.PokemonDraft();
      var2.species = first(var0.getString("Species"), var0.getString("species"));
      var2.level = var0.contains("Level") ? var0.getInt("Level") : Math.max(1, var0.getInt("level"));
      var2.gender = first(var0.getString("Gender"), var0.getString("gender"));
      if (var2.gender.isBlank()) {
         var2.gender = "random";
      }

      parseProperties(var2, var0.getString("Properties"));
      ListTag var3 = var0.getList("Moves", var1 ? 8 : 10);

      for (int var4 = 0; var4 < Math.min(4, var3.size()); var4++) {
         var2.moves[var4] = var1 ? var3.getString(var4) : var3.getCompound(var4).getString("MoveId");
      }

      return var2;
   }

   private static void parseProperties(NpcBattleTeamEditorScreen.PokemonDraft var0, String var1) {
      if (var1 != null) {
         for (String var5 : var1.trim().split("\\s+")) {
            int var6 = var5.indexOf(61);
            if (var6 >= 1) {
               String var7 = var5.substring(0, var6).toLowerCase(Locale.ROOT);
               String var8 = var5.substring(var6 + 1);
               switch (var7) {
                  case "species":
                     var0.species = var8;
                     break;
                  case "form":
                     var0.form = var8;
                     break;
                  case "nature":
                     var0.nature = var8;
                     break;
                  case "ability":
                     var0.ability = var8;
                     break;
                  case "helditem":
                  case "held_item":
                     var0.heldItem = var8;
                     break;
                  case "shiny":
                     var0.shiny = Boolean.parseBoolean(var8);
                     break;
                  default:
                     String[] var11 = new String[]{"hp", "attack", "defence", "special_attack", "special_defence", "speed"};

                     for (int var12 = 0; var12 < 6; var12++) {
                        if (var7.equals(var11[var12] + "_iv")) {
                           var0.ivs[var12] = num(var8, var0.ivs[var12]);
                        }

                        if (var7.equals(var11[var12] + "_ev")) {
                           var0.evs[var12] = num(var8, var0.evs[var12]);
                        }
                     }
               }
            }
         }
      }
   }

   private static int num(String var0, int var1) {
      try {
         return Integer.parseInt(var0);
      } catch (Exception var3) {
         return var1;
      }
   }

   private static String first(String var0, String var1) {
      return var0 != null && !var0.isBlank() ? var0 : (var1 == null ? "" : var1);
   }

   public void renderBackground(GuiGraphics var1, int var2, int var3, float var4) {
      var1.fill(0, 0, this.width, this.height, -2013265920);
      int var5 = Math.min(560, this.width - 18);
      int var6 = (this.width - var5) / 2;
      int var7 = Math.max(8, (this.height - 300) / 2);
      var1.fill(var6, var7, var6 + var5, var7 + 282, -233103319);
      var1.fill(var6, var7, var6 + var5, var7 + 2, -1653151);
   }

   public void render(GuiGraphics var1, int var2, int var3, float var4) {
      super.render(var1, var2, var3, var4);
      int var5 = Math.max(8, (this.height - 300) / 2);
      var1.drawCenteredString(
         this.font, Component.literal(this.rival ? "Rival " + BRANCHES[this.branch] + " Team" : "Battle NPC Team"), this.width / 2, var5 + 10, -1653151
      );
      var1.drawCenteredString(this.font, Component.literal("Click a slot to open the searchable Pokémon builder"), this.width / 2, var5 + 36, -4669236);
   }

   public boolean isPauseScreen() {
      return false;
   }

   public static final class PokemonDraft {
      String species = "";
      String form = "";
      String gender = "random";
      String nature = "";
      String ability = "";
      String heldItem = "";
      int level = 5;
      boolean shiny;
      final String[] moves = new String[]{"", "", "", ""};
      final int[] ivs = new int[]{31, 31, 31, 31, 31, 31};
      final int[] evs = new int[]{0, 0, 0, 0, 0, 0};

      NpcBattleTeamEditorScreen.PokemonDraft copy() {
         NpcBattleTeamEditorScreen.PokemonDraft var1 = new NpcBattleTeamEditorScreen.PokemonDraft();
         var1.species = this.species;
         var1.form = this.form;
         var1.gender = this.gender;
         var1.nature = this.nature;
         var1.ability = this.ability;
         var1.heldItem = this.heldItem;
         var1.level = this.level;
         var1.shiny = this.shiny;
         System.arraycopy(this.moves, 0, var1.moves, 0, 4);
         System.arraycopy(this.ivs, 0, var1.ivs, 0, 6);
         System.arraycopy(this.evs, 0, var1.evs, 0, 6);
         return var1;
      }

      String properties() {
         StringBuilder var1 = new StringBuilder();
         if (!this.species.isBlank()) {
            var1.append("species=").append(this.species);
         }

         append(var1, "form", this.form);
         append(var1, "nature", this.nature);
         append(var1, "ability", this.ability);
         append(var1, "held_item", this.heldItem);
         if (this.shiny) {
            append(var1, "shiny", "true");
         }

         String[] var2 = new String[]{"hp", "attack", "defence", "special_attack", "special_defence", "speed"};

         for (int var3 = 0; var3 < 6; var3++) {
            append(var1, var2[var3] + "_iv", Integer.toString(this.ivs[var3]));
            if (this.evs[var3] > 0) {
               append(var1, var2[var3] + "_ev", Integer.toString(this.evs[var3]));
            }
         }

         return var1.toString();
      }

      static void append(StringBuilder var0, String var1, String var2) {
         if (var2 != null && !var2.isBlank()) {
            if (var0.length() > 0) {
               var0.append(' ');
            }

            var0.append(var1).append('=').append(var2);
         }
      }
   }
}
