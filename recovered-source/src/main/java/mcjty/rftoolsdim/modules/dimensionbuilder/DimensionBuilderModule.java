package mcjty.rftoolsdim.modules.dimensionbuilder;

import java.util.function.Supplier;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.items.BaseBlockItem;
import mcjty.lib.modules.IModule;
import mcjty.rftoolsdim.RFToolsDim;
import mcjty.rftoolsdim.modules.dimensionbuilder.blocks.DimensionBuilderTileEntity;
import mcjty.rftoolsdim.modules.dimensionbuilder.client.ClientHelpers;
import mcjty.rftoolsdim.modules.dimensionbuilder.client.DimensionBuilderRenderer;
import mcjty.rftoolsdim.modules.dimensionbuilder.client.GuiDimensionBuilder;
import mcjty.rftoolsdim.modules.dimensionbuilder.data.PhasedFieldGeneratorData;
import mcjty.rftoolsdim.modules.dimensionbuilder.data.RealizedTabData;
import mcjty.rftoolsdim.modules.dimensionbuilder.items.DimensionMonitorItem;
import mcjty.rftoolsdim.modules.dimensionbuilder.items.EmptyDimensionTab;
import mcjty.rftoolsdim.modules.dimensionbuilder.items.PhasedFieldGenerator;
import mcjty.rftoolsdim.modules.dimensionbuilder.items.RealizedDimensionTab;
import mcjty.rftoolsdim.setup.Config;
import mcjty.rftoolsdim.setup.Registration;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public class DimensionBuilderModule implements IModule {
   public static final RBlock<BaseBlock, BlockItem, DimensionBuilderTileEntity> DIMENSION_BUILDER = Registration.RBLOCKS
      .registerBlock(
         "dimension_builder",
         DimensionBuilderTileEntity.class,
         DimensionBuilderTileEntity::createBlock,
         block -> new BaseBlockItem((Block)block.get(), Registration.createStandardProperties()),
         DimensionBuilderTileEntity::new
      );
   public static final Supplier<BlockEntityType<DimensionBuilderTileEntity>> TYPE_DIMENSION_BUILDER = DIMENSION_BUILDER.be();
   public static final Supplier<MenuType<GenericContainer>> CONTAINER_DIMENSION_BUILDER = Registration.CONTAINERS
      .register("dimension_builder", GenericContainer::createContainerType);
   public static final DeferredItem<EmptyDimensionTab> EMPTY_DIMENSION_TAB = Registration.ITEMS
      .register("empty_dimension_tab", RFToolsDim.tab(EmptyDimensionTab::new));
   public static final DeferredItem<RealizedDimensionTab> REALIZED_DIMENSION_TAB = Registration.ITEMS
      .register("realized_dimension_tab", RFToolsDim.tab(RealizedDimensionTab::new));
   public static final DeferredItem<DimensionMonitorItem> DIMENSION_MONITOR = Registration.ITEMS
      .register("dimension_monitor", RFToolsDim.tab(DimensionMonitorItem::new));
   public static final DeferredItem<PhasedFieldGenerator> PHASED_FIELD_GENERATOR = Registration.ITEMS
      .register("phased_field_generator", RFToolsDim.tab(PhasedFieldGenerator::new));
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<RealizedTabData>> ITEM_REALIZED_TAB_DATA = Registration.COMPONENTS
      .registerComponentType("realized_tab_data", builder -> builder.persistent(RealizedTabData.CODEC).networkSynchronized(RealizedTabData.STREAM_CODEC));
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<PhasedFieldGeneratorData>> ITEM_PHASED_FIELD_GENERATOR_DATA = Registration.COMPONENTS
      .registerComponentType(
         "phased_field_generator_data",
         builder -> builder.persistent(PhasedFieldGeneratorData.CODEC).networkSynchronized(PhasedFieldGeneratorData.STREAM_CODEC)
      );

   public DimensionBuilderModule(IEventBus bus) {
      bus.addListener(this::registerMenuScreens);
   }

   public void init(FMLCommonSetupEvent event) {
   }

   public void initClient(FMLClientSetupEvent event) {
      event.enqueueWork(() -> {
         ClientHelpers.initOverrides((DimensionMonitorItem)DIMENSION_MONITOR.get());
         ClientHelpers.initOverrides((PhasedFieldGenerator)PHASED_FIELD_GENERATOR.get());
      });
      DimensionBuilderRenderer.register();
   }

   private void registerMenuScreens(RegisterMenuScreensEvent event) {
      event.register(CONTAINER_DIMENSION_BUILDER.get(), GuiDimensionBuilder::new);
   }

   public void initConfig(IEventBus bus) {
      DimensionBuilderConfig.init(Config.SERVER_BUILDER, Config.CLIENT_BUILDER);
   }

   public void initDatagen(DataGen dataGen, Provider provider) {
   }
}
