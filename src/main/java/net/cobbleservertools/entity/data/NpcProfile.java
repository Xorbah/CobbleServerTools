package net.cobbleservertools.entity.data;

import java.util.List;
import java.util.Locale;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class NpcProfile {
   public static final int DATA_VERSION = 2;
   public static final String DEFAULT_SKIN = "minecraft:textures/entity/player/wide/steve.png";
   private static final int MAX_TEXT_LENGTH = 512;
   private static final int MAX_COMMAND_LENGTH = 8192;
   private static final int MAX_MONEY_REWARD = 1000000000;
   private static final int MAX_COOLDOWN_SECONDS = 31536000;
   private static final List<String> PASSTHROUGH_KEYS = List.of(
      "NpcPokemons",
      "NpcPokemonProperties",
      "Winners",
      "RematchCooldowns",
      "ShopItems",
      "SellPrices",
      "SellDescs",
      "Items",
      "ItemInfo",
      "TradeData",
      "TraderData",
      "TradedPlayers",
      "RewardedPlayers",
      "RequestedPokemon",
      "OfferedPokemon",
      "UnlimitedTrades",
      "tradeRequestId",
      "tradeOfferId",
      "tradeOfferLevel",
      "tradeOfferGender",
      "unlimitedTrade",
      "tradedPlayers",
      "rewardedPlayers",
      "requestSpecies",
      "offerSpecies",
      "requestId",
      "offerId",
      "offerLevel",
      "offerGender",
      "LockAnchorX",
      "LockAnchorY",
      "LockAnchorZ",
      "EntityX",
      "EntityY",
      "EntityZ",
      "StartDialog",
      "Moves",
      "MoveId",
      "tutorMoveId",
      "CobbleServerToolsBlankNpc",
      "CobbleServerToolsPresetId",
      "RctBattleEngine",
      "RctBattleFormat",
      "RctMaxItemUses",
      "RctHealPlayers",
      "RctAdjustPlayerLevels",
      "RctAdjustNpcLevels",
      "RctTrainerBag",
      "RctBattleTheme"
   );
   private String npcName = "";
   private float npcSize = 0.9375F;
   private NpcType npcType = NpcType.WIDE;
   private String npcSkin = "minecraft:textures/entity/player/wide/steve.png";
   private String dialogId = "admDialog";
   private boolean seekPlayer = true;
   private int seekRange = 8;
   private boolean sitting;
   private boolean lockPos;
   private Double lockX;
   private Double lockY;
   private Double lockZ;
   private boolean lookAtPlayer;
   private Double homeX;
   private Double homeY;
   private Double homeZ;
   private float homeYaw;
   private BattleType battleType = BattleType.NOT_SET;
   private String rewardItemId = "";
   private String onVictoryCommand = "";
   private boolean rewardResetAlways;
   private int moneyReward;
   private int rematchCooldownSeconds;
   private boolean healAfterDialog;
   private String tutorMoveId = "";
   private CompoundTag passthroughData = new CompoundTag();

   public static NpcProfile fromFullTag(CompoundTag var0) {
      NpcProfile var1 = new NpcProfile();
      var1.readFull(var0);
      return var1;
   }

   public void readFull(CompoundTag var1) {
      if (var1 != null) {
         this.npcName = bounded(var1.getString("NpcName"), 512);
         this.npcSize = Mth.clamp(var1.contains("NpcSize") ? var1.getFloat("NpcSize") : this.npcSize, 0.25F, 4.0F);
         this.npcType = NpcType.parse(var1.getString("NpcType"));
         this.npcSkin = sanitizeSkin(var1.getString("NpcSkin"));
         this.dialogId = bounded(orDefault(var1.getString("DialogId"), "admDialog"), 512);
         this.seekPlayer = var1.contains("SeekPlayer") ? var1.getBoolean("SeekPlayer") : this.seekPlayer;
         this.seekRange = Mth.clamp(var1.contains("SeekRange") ? var1.getInt("SeekRange") : this.seekRange, 1, 16);
         this.sitting = var1.getBoolean("Sitting");
         this.lockPos = var1.getBoolean("LockPos");
         this.lockX = readNullableDouble(var1, "LockX");
         this.lockY = readNullableDouble(var1, "LockY");
         this.lockZ = readNullableDouble(var1, "LockZ");
         this.lookAtPlayer = var1.getBoolean("LookAtPlayer");
         this.homeX = readNullableDouble(var1, "HomeX");
         this.homeY = readNullableDouble(var1, "HomeY");
         this.homeZ = readNullableDouble(var1, "HomeZ");
         this.homeYaw = var1.contains("HomeYaw") ? var1.getFloat("HomeYaw") : 0.0F;
         this.battleType = BattleType.parse(var1.getString("BattleType"));
         String var2 = var1.contains("RewardItemId") ? var1.getString("RewardItemId") : var1.getString("RewardItemRaw");
         this.rewardItemId = bounded(var2, 512);
         this.onVictoryCommand = bounded(var1.getString("OnVictoryCommand"), 8192);
         this.rewardResetAlways = var1.getBoolean("RewardResetAlways");
         this.moneyReward = Mth.clamp(var1.getInt("MoneyReward"), 0, 1000000000);
         this.rematchCooldownSeconds = Mth.clamp(var1.getInt("RematchCooldownSeconds"), 0, 31536000);
         this.healAfterDialog = var1.getBoolean("HealAfterDialog");
         this.tutorMoveId = bounded(var1.getString("TutorMoveId"), 512);
         this.passthroughData = new CompoundTag();

         for (String var4 : PASSTHROUGH_KEYS) {
            if (var1.contains(var4)) {
               Tag var5 = var1.get(var4);
               if (var5 != null) {
                  this.passthroughData.put(var4, var5.copy());
               }
            }
         }
      }
   }

   public void applyEditableTag(CompoundTag var1) {
      if (var1 != null) {
         if (var1.contains("NpcName")) {
            this.npcName = bounded(var1.getString("NpcName"), 512);
         }

         if (var1.contains("NpcSize")) {
            this.npcSize = Mth.clamp(var1.getFloat("NpcSize"), 0.25F, 4.0F);
         }

         if (var1.contains("NpcType")) {
            this.npcType = NpcType.parse(var1.getString("NpcType"));
         }

         if (var1.contains("NpcSkin")) {
            this.npcSkin = sanitizeSkin(var1.getString("NpcSkin"));
         }

         if (var1.contains("DialogId")) {
            this.dialogId = bounded(var1.getString("DialogId"), 512);
         }

         if (var1.contains("SeekPlayer")) {
            this.seekPlayer = var1.getBoolean("SeekPlayer");
         }

         if (var1.contains("SeekRange")) {
            this.seekRange = Mth.clamp(var1.getInt("SeekRange"), 1, 16);
         }

         if (var1.contains("Sitting")) {
            this.sitting = var1.getBoolean("Sitting");
         }

         if (var1.contains("LockPos")) {
            this.lockPos = var1.getBoolean("LockPos");
         }

         if (var1.contains("LockX")) {
            this.lockX = finiteOrNull(var1.getDouble("LockX"));
         }

         if (var1.contains("LockY")) {
            this.lockY = finiteOrNull(var1.getDouble("LockY"));
         }

         if (var1.contains("LockZ")) {
            this.lockZ = finiteOrNull(var1.getDouble("LockZ"));
         }

         if (var1.contains("LookAtPlayer")) {
            this.lookAtPlayer = var1.getBoolean("LookAtPlayer");
         }

         if (var1.contains("HomeX")) {
            this.homeX = finiteOrNull(var1.getDouble("HomeX"));
         }

         if (var1.contains("HomeY")) {
            this.homeY = finiteOrNull(var1.getDouble("HomeY"));
         }

         if (var1.contains("HomeZ")) {
            this.homeZ = finiteOrNull(var1.getDouble("HomeZ"));
         }

         if (var1.contains("HomeYaw")) {
            this.homeYaw = var1.getFloat("HomeYaw");
         }

         if (var1.contains("BattleType")) {
            this.battleType = BattleType.parse(var1.getString("BattleType"));
         }

         if (var1.contains("RewardItemId")) {
            this.rewardItemId = bounded(var1.getString("RewardItemId"), 512);
         }

         if (var1.contains("RewardItemRaw")) {
            this.rewardItemId = bounded(var1.getString("RewardItemRaw"), 512);
         }

         if (var1.contains("OnVictoryCommand")) {
            this.onVictoryCommand = bounded(var1.getString("OnVictoryCommand"), 8192);
         }

         if (var1.contains("RewardResetAlways")) {
            this.rewardResetAlways = var1.getBoolean("RewardResetAlways");
         }

         if (var1.contains("MoneyReward")) {
            this.moneyReward = Mth.clamp(var1.getInt("MoneyReward"), 0, 1000000000);
         }

         if (var1.contains("RematchCooldownSeconds")) {
            this.rematchCooldownSeconds = Mth.clamp(var1.getInt("RematchCooldownSeconds"), 0, 31536000);
         }

         if (var1.contains("HealAfterDialog")) {
            this.healAfterDialog = var1.getBoolean("HealAfterDialog");
         }

         if (var1.contains("TutorMoveId")) {
            this.tutorMoveId = bounded(var1.getString("TutorMoveId"), 512);
         }

         this.copyEditablePassthrough(var1, "RctBattleEngine");
         this.copyEditablePassthrough(var1, "RctBattleFormat");
         this.copyEditablePassthrough(var1, "RctMaxItemUses");
         this.copyEditablePassthrough(var1, "RctHealPlayers");
         this.copyEditablePassthrough(var1, "RctAdjustPlayerLevels");
         this.copyEditablePassthrough(var1, "RctAdjustNpcLevels");
         this.copyEditablePassthrough(var1, "RctTrainerBag");
         this.copyEditablePassthrough(var1, "RctBattleTheme");
      }
   }

   public CompoundTag writeFull(NpcKind var1) {
      CompoundTag var2 = this.passthroughData.copy();
      this.writeVisibleFields(var2, var1);
      var2.putString("DialogId", this.dialogId);
      putNullableDouble(var2, "LockX", this.lockX);
      putNullableDouble(var2, "LockY", this.lockY);
      putNullableDouble(var2, "LockZ", this.lockZ);
      putNullableDouble(var2, "HomeX", this.homeX);
      putNullableDouble(var2, "HomeY", this.homeY);
      putNullableDouble(var2, "HomeZ", this.homeZ);
      var2.putFloat("HomeYaw", this.homeYaw);
      var2.putString("BattleType", this.battleType.name());
      var2.putString("RewardItemId", this.rewardItemId);
      var2.putString("OnVictoryCommand", this.onVictoryCommand);
      var2.putBoolean("RewardResetAlways", this.rewardResetAlways);
      var2.putInt("MoneyReward", this.moneyReward);
      var2.putInt("RematchCooldownSeconds", this.rematchCooldownSeconds);
      var2.putBoolean("HealAfterDialog", this.healAfterDialog);
      var2.putString("TutorMoveId", this.tutorMoveId);
      return var2;
   }

   public CompoundTag writeEditable(NpcKind var1) {
      CompoundTag var2 = new CompoundTag();
      this.writeVisibleFields(var2, var1);
      var2.putString("DialogId", this.dialogId);
      putNullableDouble(var2, "LockX", this.lockX);
      putNullableDouble(var2, "LockY", this.lockY);
      putNullableDouble(var2, "LockZ", this.lockZ);
      putNullableDouble(var2, "HomeX", this.homeX);
      putNullableDouble(var2, "HomeY", this.homeY);
      putNullableDouble(var2, "HomeZ", this.homeZ);
      var2.putFloat("HomeYaw", this.homeYaw);
      var2.putString("BattleType", this.battleType.name());
      var2.putString("RewardItemId", this.rewardItemId);
      var2.putString("OnVictoryCommand", this.onVictoryCommand);
      var2.putBoolean("RewardResetAlways", this.rewardResetAlways);
      var2.putInt("MoneyReward", this.moneyReward);
      var2.putInt("RematchCooldownSeconds", this.rematchCooldownSeconds);
      var2.putBoolean("HealAfterDialog", this.healAfterDialog);
      var2.putString("TutorMoveId", this.tutorMoveId);
      this.copyEditorPassthrough(var2, "RctBattleEngine");
      this.copyEditorPassthrough(var2, "RctBattleFormat");
      this.copyEditorPassthrough(var2, "RctMaxItemUses");
      this.copyEditorPassthrough(var2, "RctHealPlayers");
      this.copyEditorPassthrough(var2, "RctAdjustPlayerLevels");
      this.copyEditorPassthrough(var2, "RctAdjustNpcLevels");
      this.copyEditorPassthrough(var2, "RctTrainerBag");
      this.copyEditorPassthrough(var2, "RctBattleTheme");
      return var2;
   }

   private void copyEditablePassthrough(CompoundTag var1, String var2) {
      if (var1.contains(var2)) {
         Tag var3 = var1.get(var2);
         if (var3 != null) {
            this.passthroughData.put(var2, var3.copy());
         }
      }
   }

   private void copyEditorPassthrough(CompoundTag var1, String var2) {
      if (this.passthroughData.contains(var2)) {
         Tag var3 = this.passthroughData.get(var2);
         if (var3 != null) {
            var1.put(var2, var3.copy());
         }
      }
   }

   public CompoundTag writeVisible(NpcKind var1) {
      CompoundTag var2 = new CompoundTag();
      this.writeVisibleFields(var2, var1);
      return var2;
   }

   private void writeVisibleFields(CompoundTag var1, NpcKind var2) {
      var1.putInt("CobbleServerToolsProfileVersion", 2);
      var1.putString("NpcKind", var2.serializedName());
      var1.putString("NpcName", this.npcName);
      var1.putFloat("NpcSize", this.npcSize);
      var1.putString("NpcType", this.npcType.name());
      var1.putString("NpcSkin", this.npcSkin);
      var1.putBoolean("SeekPlayer", this.seekPlayer);
      var1.putInt("SeekRange", this.seekRange);
      var1.putBoolean("Sitting", this.sitting);
      var1.putBoolean("LockPos", this.lockPos);
      var1.putBoolean("LookAtPlayer", this.lookAtPlayer);
   }

   private static void putNullableDouble(CompoundTag var0, String var1, Double var2) {
      if (var2 != null && Double.isFinite(var2)) {
         var0.putDouble(var1, var2);
      } else {
         var0.remove(var1);
      }
   }

   private static Double readNullableDouble(CompoundTag var0, String var1) {
      return var0.contains(var1) ? finiteOrNull(var0.getDouble(var1)) : null;
   }

   private static Double finiteOrNull(double var0) {
      return Double.isFinite(var0) ? var0 : null;
   }

   private static String bounded(String var0, int var1) {
      if (var0 == null) {
         return "";
      }

      String var2 = var0.replace('\u0000', ' ').trim();
      return var2.length() <= var1 ? var2 : var2.substring(0, var1);
   }

   private static String sanitizeSkin(String var0) {
      String var1 = bounded(orDefault(var0, "minecraft:textures/entity/player/wide/steve.png"), 512).replace('\\', '/');
      if (var1.indexOf(58) < 0) {
         if (var1.startsWith("textures/")) {
            var1 = "minecraft:" + var1;
         } else {
            var1 = "cobbleservertools:textures/entity/npc/" + sanitizeSkinName(var1) + ".png";
         }
      } else if (var1.startsWith("cobbleservertools:") && !var1.startsWith("cobbleservertools:textures/")) {
         String var2 = var1.substring("cobbleservertools:".length());
         if (var2.startsWith("bootstrap/")) {
            var2 = var2.substring("bootstrap/".length());
         }

         var1 = "cobbleservertools:textures/entity/npc/" + sanitizeSkinName(var2) + ".png";
      }

      return ResourceLocation.tryParse(var1) == null ? "minecraft:textures/entity/player/wide/steve.png" : var1;
   }

   private static String sanitizeSkinName(String var0) {
      String var1 = var0.toLowerCase(Locale.ROOT);
      if (var1.endsWith(".png")) {
         var1 = var1.substring(0, var1.length() - 4);
      }

      var1 = var1.replaceAll("[^a-z0-9._-]+", "_").replaceAll("^_+|_+$", "");
      return var1.isBlank() ? "steve" : var1;
   }

   private static String orDefault(String var0, String var1) {
      return var0 != null && !var0.isBlank() ? var0 : var1;
   }

   public String npcName() {
      return this.npcName;
   }

   public float npcSize() {
      return this.npcSize;
   }

   public NpcType npcType() {
      return this.npcType;
   }

   public String npcSkin() {
      return this.npcSkin;
   }

   public String dialogId() {
      return this.dialogId;
   }

   public boolean seekPlayer() {
      return this.seekPlayer;
   }

   public int seekRange() {
      return this.seekRange;
   }

   public boolean sitting() {
      return this.sitting;
   }

   public boolean lockPos() {
      return this.lockPos;
   }

   public boolean lookAtPlayer() {
      return this.lookAtPlayer;
   }

   public Double homeX() {
      return this.homeX;
   }

   public Double homeY() {
      return this.homeY;
   }

   public Double homeZ() {
      return this.homeZ;
   }

   public float homeYaw() {
      return this.homeYaw;
   }

   public boolean healAfterDialog() {
      return this.healAfterDialog;
   }

   public String tutorMoveId() {
      return this.tutorMoveId;
   }

   public String rewardItemId() {
      return this.rewardItemId;
   }

   public String onVictoryCommand() {
      return this.onVictoryCommand;
   }

   public boolean rewardResetAlways() {
      return this.rewardResetAlways;
   }

   public int moneyReward() {
      return this.moneyReward;
   }

   public int rematchCooldownSeconds() {
      return this.rematchCooldownSeconds;
   }

   public BattleType battleType() {
      return this.battleType;
   }

   public CompoundTag compatibilityData() {
      return this.passthroughData.copy();
   }

   public void replaceCompatibilityData(CompoundTag var1) {
      this.passthroughData = var1 == null ? new CompoundTag() : var1.copy();
   }
}
