package net.cobbleservertools.registry;

import net.cobbleservertools.block.CeladonVendingMachineBlock;
import net.cobbleservertools.block.entity.VendingMachineBlockEntity;
import net.cobbleservertools.entity.AbstractCobbleNpcEntity;
import net.cobbleservertools.entity.BattleNpcEntity;
import net.cobbleservertools.entity.DialogNpcEntity;
import net.cobbleservertools.entity.MartNpcEntity;
import net.cobbleservertools.entity.MoveTutorNpcEntity;
import net.cobbleservertools.entity.RivalNpcEntity;
import net.cobbleservertools.entity.TraderNpcEntity;
import net.cobbleservertools.item.NpcPresetItem;
import net.cobbleservertools.item.NpcSpawnItem;
import net.cobbleservertools.item.RemoveNpcItem;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType.EntityFactory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.Blocks;
import net.neoforged.neoforge.registries.DeferredRegister.Items;

public final class KRegistries {
   public static final Blocks BLOCKS = DeferredRegister.createBlocks("cobbleservertools");
   public static final Items ITEMS = DeferredRegister.createItems("cobbleservertools");
   public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, "cobbleservertools");
   public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "cobbleservertools");
   public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, "cobbleservertools");
   public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "cobbleservertools");
   public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, "cobbleservertools");
   public static final DeferredHolder<Block, CeladonVendingMachineBlock> CELADON_VENDING_MACHINE = BLOCKS.register(
      "celadon_city_dept_store_vending_machine",
      () -> new CeladonVendingMachineBlock(Properties.of().mapColor(MapColor.METAL).strength(2.0F, 3.0F).sound(SoundType.METAL).noOcclusion())
   );
   public static final DeferredHolder<Item, BlockItem> CELADON_VENDING_MACHINE_ITEM = ITEMS.register(
      "celadon_city_dept_store_vending_machine", () -> new BlockItem((Block)CELADON_VENDING_MACHINE.get(), new net.minecraft.world.item.Item.Properties())
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VendingMachineBlockEntity>> VENDING_MACHINE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
      "vending_machine", () -> Builder.of(VendingMachineBlockEntity::new, new Block[]{(Block)CELADON_VENDING_MACHINE.get()}).build(null)
   );
   public static final DeferredHolder<EntityType<?>, EntityType<BattleNpcEntity>> BATTLE_NPC = ENTITY_TYPES.register(
      "battle_npc", () -> npcType("battle_npc", BattleNpcEntity::new)
   );
   public static final DeferredHolder<EntityType<?>, EntityType<RivalNpcEntity>> RIVAL_NPC = ENTITY_TYPES.register(
      "rival_npc", () -> npcType("rival_npc", RivalNpcEntity::new)
   );
   public static final DeferredHolder<EntityType<?>, EntityType<DialogNpcEntity>> DIALOG_NPC = ENTITY_TYPES.register(
      "dialog_npc", () -> npcType("dialog_npc", DialogNpcEntity::new)
   );
   public static final DeferredHolder<EntityType<?>, EntityType<TraderNpcEntity>> TRADER_NPC = ENTITY_TYPES.register(
      "trader_npc", () -> npcType("trader_npc", TraderNpcEntity::new)
   );
   public static final DeferredHolder<EntityType<?>, EntityType<MartNpcEntity>> MART_NPC = ENTITY_TYPES.register(
      "mart_npc", () -> npcType("mart_npc", MartNpcEntity::new)
   );
   public static final DeferredHolder<EntityType<?>, EntityType<MoveTutorNpcEntity>> MOVE_TUTOR_NPC = ENTITY_TYPES.register(
      "movetutor_npc", () -> npcType("movetutor_npc", MoveTutorNpcEntity::new)
   );
   public static final DeferredHolder<Item, NpcPresetItem> NPC_PRESET = ITEMS.register(
      "npc_preset", () -> new NpcPresetItem(new net.minecraft.world.item.Item.Properties().stacksTo(1))
   );
   public static final DeferredHolder<Item, RemoveNpcItem> REMOVE_NPC = ITEMS.register(
      "remove_npc", () -> new RemoveNpcItem(new net.minecraft.world.item.Item.Properties().stacksTo(1))
   );
   public static final DeferredHolder<Item, NpcSpawnItem<BattleNpcEntity>> SPAWN_BATTLE_NPC = ITEMS.register(
      "spawn_battle_npc", () -> new NpcSpawnItem(new net.minecraft.world.item.Item.Properties().stacksTo(16), BATTLE_NPC::get)
   );
   public static final DeferredHolder<Item, NpcSpawnItem<RivalNpcEntity>> SPAWN_RIVAL_NPC = ITEMS.register(
      "spawn_rival_npc", () -> new NpcSpawnItem(new net.minecraft.world.item.Item.Properties().stacksTo(16), RIVAL_NPC::get)
   );
   public static final DeferredHolder<Item, NpcSpawnItem<DialogNpcEntity>> SPAWN_DIALOG_NPC = ITEMS.register(
      "spawn_dialog_npc", () -> new NpcSpawnItem(new net.minecraft.world.item.Item.Properties().stacksTo(16), DIALOG_NPC::get)
   );
   public static final DeferredHolder<Item, NpcSpawnItem<TraderNpcEntity>> SPAWN_TRADER_NPC = ITEMS.register(
      "spawn_trader_npc", () -> new NpcSpawnItem(new net.minecraft.world.item.Item.Properties().stacksTo(16), TRADER_NPC::get)
   );
   public static final DeferredHolder<Item, NpcSpawnItem<MartNpcEntity>> SPAWN_MART_NPC = ITEMS.register(
      "spawn_mart_npc", () -> new NpcSpawnItem(new net.minecraft.world.item.Item.Properties().stacksTo(16), MART_NPC::get)
   );
   public static final DeferredHolder<Item, NpcSpawnItem<MoveTutorNpcEntity>> SPAWN_MOVE_TUTOR_NPC = ITEMS.register(
      "spawn_movetutor_npc", () -> new NpcSpawnItem(new net.minecraft.world.item.Item.Properties().stacksTo(16), MOVE_TUTOR_NPC::get)
   );
   public static final DeferredHolder<Item, Item> AMULET_COIN = ITEMS.register(
      "amulet_coin", () -> new Item(new net.minecraft.world.item.Item.Properties().stacksTo(1))
   );
   public static final DeferredHolder<CreativeModeTab, CreativeModeTab> COBBLE_SERVER_TOOLS_TAB = CREATIVE_TABS.register(
      "cobbleservertools",
      () -> CreativeModeTab.builder()
         .title(Component.translatable("itemGroup.cobbleservertools", new Object[0]))
         .icon(() -> new ItemStack((ItemLike)NPC_PRESET.get()))
         .displayItems((var0, var1) -> {
            var1.accept((ItemLike)NPC_PRESET.get());
            var1.accept((ItemLike)REMOVE_NPC.get());
            var1.accept((ItemLike)SPAWN_BATTLE_NPC.get());
            var1.accept((ItemLike)SPAWN_RIVAL_NPC.get());
            var1.accept((ItemLike)SPAWN_DIALOG_NPC.get());
            var1.accept((ItemLike)SPAWN_TRADER_NPC.get());
            var1.accept((ItemLike)SPAWN_MART_NPC.get());
            var1.accept((ItemLike)SPAWN_MOVE_TUTOR_NPC.get());
            var1.accept((ItemLike)AMULET_COIN.get());
            var1.accept((ItemLike)CELADON_VENDING_MACHINE_ITEM.get());
         })
         .build()
   );

   private KRegistries() {
   }

   private static <T extends AbstractCobbleNpcEntity> EntityType<T> npcType(String var0, EntityFactory<T> var1) {
      return net.minecraft.world.entity.EntityType.Builder.of(var1, MobCategory.CREATURE)
         .sized(0.6F, 1.9F)
         .clientTrackingRange(10)
         .updateInterval(3)
         .build("cobbleservertools:" + var0);
   }

   public static void register(IEventBus var0) {
      BLOCKS.register(var0);
      ITEMS.register(var0);
      ENTITY_TYPES.register(var0);
      BLOCK_ENTITY_TYPES.register(var0);
      DATA_COMPONENT_TYPES.register(var0);
      CREATIVE_TABS.register(var0);
      MENUS.register(var0);
   }
}
