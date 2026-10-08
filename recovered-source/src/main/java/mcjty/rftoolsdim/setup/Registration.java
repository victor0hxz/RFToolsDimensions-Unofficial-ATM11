package mcjty.rftoolsdim.setup;

import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import java.util.function.Supplier;
import mcjty.lib.blocks.RBlock;
import mcjty.lib.blocks.RBlockRegistry;
import mcjty.lib.items.BaseBlockItem;
import mcjty.lib.setup.DeferredBlocks;
import mcjty.lib.setup.DeferredItems;
import mcjty.rftoolsdim.RFToolsDim;
import mcjty.rftoolsdim.dimension.biomes.RFTBiomeProvider;
import mcjty.rftoolsdim.dimension.features.RFTFeature;
import mcjty.rftoolsdim.dimension.terraintypes.RFToolsChunkGenerator;
import mcjty.rftoolsdim.modules.dimlets.DimletModule;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.DataComponents;
import net.neoforged.neoforge.registries.NeoForgeRegistries.Keys;

public class Registration {
   public static final Supplier<Item> DIMENSIONAL_SHARD = () -> BuiltInRegistries.ITEM
      .get(Identifier.fromNamespaceAndPath("rftoolsbase", "dimensionalshard"))
      .map(holder -> (Item)holder.value())
      .orElse(Items.AIR);
   public static final RBlockRegistry RBLOCKS = new RBlockRegistry("rftoolsdim", RFToolsDim.setup::addTabItem);
   public static final DeferredBlocks BLOCKS = DeferredBlocks.create("rftoolsdim");
   public static final DeferredItems ITEMS = DeferredItems.create("rftoolsdim");
   public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, "rftoolsdim");
   public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(BuiltInRegistries.MENU, "rftoolsdim");
   public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, "rftoolsdim");
   public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, "rftoolsdim");
   public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS = DeferredRegister.create(
      Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, "rftoolsdim"
   );
   public static final DeferredRegister<MapCodec<? extends LootPoolEntryContainer>> LOOT_POOL_ENTRY_TYPES = DeferredRegister.create(
      BuiltInRegistries.LOOT_POOL_ENTRY_TYPE, "rftoolsdim"
   );
   public static final DeferredRegister<MapCodec<? extends LootItemCondition>> LOOT_CONDITIONS = DeferredRegister.create(
      BuiltInRegistries.LOOT_CONDITION_TYPE, "rftoolsdim"
   );
   public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, "rftoolsdim");
   public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "rftoolsdim");
   public static final DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, "rftoolsdim");
   public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(Keys.ATTACHMENT_TYPES, "rftoolsdim");
   public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(BuiltInRegistries.FEATURE, "rftoolsdim");
   public static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS = DeferredRegister.create(
      BuiltInRegistries.CHUNK_GENERATOR, "rftoolsdim"
   );
   public static final DeferredRegister<MapCodec<? extends BiomeSource>> BIOME_SOURCES = DeferredRegister.create(BuiltInRegistries.BIOME_SOURCE, "rftoolsdim");
   public static final Supplier<MapCodec<RFToolsChunkGenerator>> RFTCHUNKGEN = CHUNK_GENERATORS.register("rftools", () -> RFToolsChunkGenerator.CODEC);
   public static final Supplier<MapCodec<RFTBiomeProvider>> RFTBIOMESOURCE = BIOME_SOURCES.register("biomes", () -> RFTBiomeProvider.CODEC);
   public static final Supplier<RFTFeature> RFTFEATURE = FEATURES.register(
      RFTFeature.RFTFEATURE_ID.getPath(), () -> new RFTFeature(NoneFeatureConfiguration.CODEC)
   );
   public static Supplier<CreativeModeTab> TAB = TABS.register(
      "rftoolsdim",
      () -> CreativeModeTab.builder()
         .title(Component.translatable("itemGroup.rftoolsdim"))
         .icon(() -> new ItemStack((ItemLike)DimletModule.EMPTY_DIMLET.get()))
         .withTabsBefore(new ResourceKey[]{CreativeModeTabs.SPAWN_EGGS})
         .displayItems((featureFlags, output) -> RFToolsDim.setup.populateTab(output))
         .build()
   );

   public static void register(IEventBus bus) {
      RBLOCKS.register(bus);
      BLOCKS.register(bus);
      ITEMS.register(bus);
      TILES.register(bus);
      CONTAINERS.register(bus);
      SOUNDS.register(bus);
      ENTITIES.register(bus);
      LOOT_MODIFIER_SERIALIZERS.register(bus);
      LOOT_POOL_ENTRY_TYPES.register(bus);
      LOOT_CONDITIONS.register(bus);
      RECIPE_SERIALIZERS.register(bus);
      FEATURES.register(bus);
      TABS.register(bus);
      COMPONENTS.register(bus);
      ATTACHMENT_TYPES.register(bus);
      CHUNK_GENERATORS.register(bus);
      BIOME_SOURCES.register(bus);
   }

   public static Properties createStandardProperties() {
      return RFToolsDim.setup.defaultProperties();
   }

   public static <B extends Block> RBlock<B, BlockItem, BlockEntity> registerSimpleBlock(String name, Supplier<B> blockSupplier) {
      return registerSimpleBlock(name, blockSupplier, block -> new BaseBlockItem(block.get(), createStandardProperties()));
   }

   public static <B extends Block, I extends BlockItem> RBlock<B, I, BlockEntity> registerSimpleBlock(
      String name, Supplier<B> blockSupplier, Function<Supplier<? extends Block>, I> itemFactory
   ) {
      DeferredBlock<B> block = BLOCKS.register(name, blockSupplier);
      DeferredItem<I> item = ITEMS.register(name, RFToolsDim.tab(() -> itemFactory.apply(block)));
      return new RBlock(block, item, null);
   }
}
