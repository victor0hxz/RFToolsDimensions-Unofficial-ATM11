package mcjty.rftoolsdim.modules.dimlets;

import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.modules.IModule;
import mcjty.rftoolsdim.RFToolsDim;
import mcjty.rftoolsdim.modules.dimlets.data.DimletData;
import mcjty.rftoolsdim.modules.dimlets.data.DimletType;
import mcjty.rftoolsdim.modules.dimlets.items.DimensionManualItem;
import mcjty.rftoolsdim.modules.dimlets.items.DimletItem;
import mcjty.rftoolsdim.modules.dimlets.items.PartItem;
import mcjty.rftoolsdim.modules.dimlets.lootmodifier.DimletLootEntry;
import mcjty.rftoolsdim.modules.dimlets.lootmodifier.EndermanLootModifier;
import mcjty.rftoolsdim.modules.dimlets.lootmodifier.LootTableCondition;
import mcjty.rftoolsdim.modules.dimlets.recipes.DigitCycleRecipe;
import mcjty.rftoolsdim.modules.dimlets.recipes.DimletCycleRecipeSerializer;
import mcjty.rftoolsdim.modules.dimlets.recipes.DimletRecipe;
import mcjty.rftoolsdim.modules.dimlets.recipes.DimletRecipeSerializer;
import mcjty.rftoolsdim.setup.Config;
import mcjty.rftoolsdim.setup.Registration;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public class DimletModule implements IModule {
   public static final DeferredItem<DimletItem> EMPTY_DIMLET = Registration.ITEMS.register("empty_dimlet", RFToolsDim.tab(() -> new DimletItem(null, false)));
   public static final DeferredItem<DimletItem> EMPTY_TERRAIN_DIMLET = Registration.ITEMS
      .register("empty_terrain_dimlet", RFToolsDim.tab(() -> new DimletItem(DimletType.TERRAIN, false)));
   public static final DeferredItem<DimletItem> EMPTY_ATTRIBUTE_DIMLET = Registration.ITEMS
      .register("empty_attribute_dimlet", RFToolsDim.tab(() -> new DimletItem(DimletType.ATTRIBUTE, false)));
   public static final DeferredItem<DimletItem> EMPTY_FEATURE_DIMLET = Registration.ITEMS
      .register("empty_feature_dimlet", RFToolsDim.tab(() -> new DimletItem(DimletType.FEATURE, false)));
   public static final DeferredItem<DimletItem> EMPTY_STRUCTURE_DIMLET = Registration.ITEMS
      .register("empty_structure_dimlet", RFToolsDim.tab(() -> new DimletItem(DimletType.STRUCTURE, false)));
   public static final DeferredItem<DimletItem> EMPTY_BIOME_DIMLET = Registration.ITEMS
      .register("empty_biome_dimlet", RFToolsDim.tab(() -> new DimletItem(DimletType.BIOME, false)));
   public static final DeferredItem<DimletItem> EMPTY_BIOME_CONTROLLER_DIMLET = Registration.ITEMS
      .register("empty_biome_controller_dimlet", RFToolsDim.tab(() -> new DimletItem(DimletType.BIOME_CONTROLLER, false)));
   public static final DeferredItem<DimletItem> EMPTY_BIOME_CATEGORY_DIMLET = Registration.ITEMS
      .register("empty_biome_category_dimlet", RFToolsDim.tab(() -> new DimletItem(DimletType.BIOME_CATEGORY, false)));
   public static final DeferredItem<DimletItem> EMPTY_BLOCK_DIMLET = Registration.ITEMS
      .register("empty_block_dimlet", RFToolsDim.tab(() -> new DimletItem(DimletType.BLOCK, false)));
   public static final DeferredItem<DimletItem> EMPTY_FLUID_DIMLET = Registration.ITEMS
      .register("empty_fluid_dimlet", RFToolsDim.tab(() -> new DimletItem(DimletType.FLUID, false)));
   public static final DeferredItem<DimletItem> EMPTY_TIME_DIMLET = Registration.ITEMS
      .register("empty_time_dimlet", RFToolsDim.tab(() -> new DimletItem(DimletType.TIME, false)));
   public static final DeferredItem<DimletItem> EMPTY_TAG_DIMLET = Registration.ITEMS
      .register("empty_tag_dimlet", RFToolsDim.tab(() -> new DimletItem(DimletType.TAG, false)));
   public static final DeferredItem<DimletItem> EMPTY_SKY_DIMLET = Registration.ITEMS
      .register("empty_sky_dimlet", RFToolsDim.tab(() -> new DimletItem(DimletType.SKY, false)));
   public static final DeferredItem<DimletItem> TERRAIN_DIMLET = Registration.ITEMS
      .register("terrain_dimlet", RFToolsDim.tab(() -> new DimletItem(DimletType.TERRAIN, true)));
   public static final DeferredItem<DimletItem> ATTRIBUTE_DIMLET = Registration.ITEMS
      .register("attribute_dimlet", RFToolsDim.tab(() -> new DimletItem(DimletType.ATTRIBUTE, true)));
   public static final DeferredItem<DimletItem> FEATURE_DIMLET = Registration.ITEMS
      .register("feature_dimlet", RFToolsDim.tab(() -> new DimletItem(DimletType.FEATURE, true)));
   public static final DeferredItem<DimletItem> STRUCTURE_DIMLET = Registration.ITEMS
      .register("structure_dimlet", RFToolsDim.tab(() -> new DimletItem(DimletType.STRUCTURE, true)));
   public static final DeferredItem<DimletItem> BIOME_DIMLET = Registration.ITEMS
      .register("biome_dimlet", RFToolsDim.tab(() -> new DimletItem(DimletType.BIOME, true)));
   public static final DeferredItem<DimletItem> BIOME_CONTROLLER_DIMLET = Registration.ITEMS
      .register("biome_controller_dimlet", RFToolsDim.tab(() -> new DimletItem(DimletType.BIOME_CONTROLLER, true)));
   public static final DeferredItem<DimletItem> BIOME_CATEGORY_DIMLET = Registration.ITEMS
      .register("biome_category_dimlet", RFToolsDim.tab(() -> new DimletItem(DimletType.BIOME_CATEGORY, true)));
   public static final DeferredItem<DimletItem> BLOCK_DIMLET = Registration.ITEMS
      .register("block_dimlet", RFToolsDim.tab(() -> new DimletItem(DimletType.BLOCK, true)));
   public static final DeferredItem<DimletItem> FLUID_DIMLET = Registration.ITEMS
      .register("fluid_dimlet", RFToolsDim.tab(() -> new DimletItem(DimletType.FLUID, true)));
   public static final DeferredItem<DimletItem> TIME_DIMLET = Registration.ITEMS
      .register("time_dimlet", RFToolsDim.tab(() -> new DimletItem(DimletType.TIME, true)));
   public static final DeferredItem<DimletItem> DIGIT_DIMLET = Registration.ITEMS
      .register("digit_dimlet", RFToolsDim.tab(() -> new DimletItem(DimletType.DIGIT, true)));
   public static final DeferredItem<DimletItem> TAG_DIMLET = Registration.ITEMS
      .register("tag_dimlet", RFToolsDim.tab(() -> new DimletItem(DimletType.TAG, true)));
   public static final DeferredItem<DimletItem> SKY_DIMLET = Registration.ITEMS
      .register("sky_dimlet", RFToolsDim.tab(() -> new DimletItem(DimletType.SKY, true)));
   public static final DeferredItem<DimletItem> ADMIN_DIMLET = Registration.ITEMS
      .register("admin_dimlet", RFToolsDim.tab(() -> new DimletItem(DimletType.ADMIN, true)));
   public static final DeferredItem<PartItem> PART_ENERGY_0 = Registration.ITEMS.register("part_energy_0", RFToolsDim.tab(PartItem::new));
   public static final DeferredItem<PartItem> PART_ENERGY_1 = Registration.ITEMS.register("part_energy_1", RFToolsDim.tab(PartItem::new));
   public static final DeferredItem<PartItem> PART_ENERGY_2 = Registration.ITEMS.register("part_energy_2", RFToolsDim.tab(PartItem::new));
   public static final DeferredItem<PartItem> PART_ENERGY_3 = Registration.ITEMS.register("part_energy_3", RFToolsDim.tab(PartItem::new));
   public static final DeferredItem<PartItem> PART_MEMORY_0 = Registration.ITEMS.register("part_memory_0", RFToolsDim.tab(PartItem::new));
   public static final DeferredItem<PartItem> PART_MEMORY_1 = Registration.ITEMS.register("part_memory_1", RFToolsDim.tab(PartItem::new));
   public static final DeferredItem<PartItem> PART_MEMORY_2 = Registration.ITEMS.register("part_memory_2", RFToolsDim.tab(PartItem::new));
   public static final DeferredItem<PartItem> PART_MEMORY_3 = Registration.ITEMS.register("part_memory_3", RFToolsDim.tab(PartItem::new));
   public static final DeferredItem<Item> COMMON_ESSENCE = Registration.ITEMS
      .register("common_essence", RFToolsDim.tab(() -> new DimensionManualItem("rftoolsdim:dimlets/essences")));
   public static final DeferredItem<Item> RARE_ESSENCE = Registration.ITEMS
      .register("rare_essence", RFToolsDim.tab(() -> new DimensionManualItem("rftoolsdim:dimlets/essences")));
   public static final DeferredItem<Item> LEGENDARY_ESSENCE = Registration.ITEMS
      .register("legendary_essence", RFToolsDim.tab(() -> new DimensionManualItem("rftoolsdim:dimlets/essences")));
   public static final Supplier<MapCodec<? extends IGlobalLootModifier>> ENDERMAN_LOOT_MODIFIER = Registration.LOOT_MODIFIER_SERIALIZERS
      .register("enderman_extra", () -> EndermanLootModifier.CODEC);
   public static final Supplier<RecipeSerializer<DimletRecipe>> DIMLET_RECIPE_SERIALIZER = Registration.RECIPE_SERIALIZERS
      .register("dimlet_recipe", () -> DimletRecipeSerializer.SERIALIZER);
   public static final Supplier<RecipeSerializer<DigitCycleRecipe>> DIMLET_CYCLE_SERIALIZER = Registration.RECIPE_SERIALIZERS
      .register("dimlet_cycle_recipe", () -> DimletCycleRecipeSerializer.SERIALIZER);
   public static final Supplier<MapCodec<DimletLootEntry>> DIMLET_LOOT_ENTRY = Registration.LOOT_POOL_ENTRY_TYPES
      .register("dimlet_loot", () -> DimletLootEntry.CODEC);
   public static final Supplier<MapCodec<LootTableCondition>> LOOT_TABLE_CONDITION = Registration.LOOT_CONDITIONS
      .register("check_tables", () -> LootTableCondition.CODEC);
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<DimletData>> ITEM_DIMLET_DATA = Registration.COMPONENTS
      .registerComponentType("dimlet_data", builder -> builder.persistent(DimletData.CODEC).networkSynchronized(DimletData.STREAM_CODEC));

   public void init(FMLCommonSetupEvent event) {
   }

   public void initClient(FMLClientSetupEvent event) {
   }

   public void initConfig(IEventBus bus) {
      DimletConfig.init(Config.SERVER_BUILDER, Config.CLIENT_BUILDER);
   }

   public void initDatagen(DataGen dataGen, Provider provider) {
   }
}
