package mcjty.rftoolsdim.modules.dimensioneditor;

import java.util.function.Supplier;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.items.BaseBlockItem;
import mcjty.lib.modules.IModule;
import mcjty.rftoolsdim.modules.dimensioneditor.blocks.DimensionEditorTileEntity;
import mcjty.rftoolsdim.modules.dimensioneditor.client.GuiDimensionEditor;
import mcjty.rftoolsdim.setup.Config;
import mcjty.rftoolsdim.setup.Registration;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class DimensionEditorModule implements IModule {
   public static final RBlock<BaseBlock, BlockItem, DimensionEditorTileEntity> DIMENSION_EDITOR = Registration.RBLOCKS
      .registerBlock(
         "dimension_editor",
         DimensionEditorTileEntity.class,
         DimensionEditorTileEntity::createBlock,
         block -> new BaseBlockItem((Block)block.get(), Registration.createStandardProperties()),
         DimensionEditorTileEntity::new
      );
   public static final Supplier<BlockEntityType<DimensionEditorTileEntity>> TYPE_DIMENSION_EDITOR = DIMENSION_EDITOR.be();
   public static final Supplier<MenuType<GenericContainer>> CONTAINER_DIMENSION_EDITOR = Registration.CONTAINERS
      .register("dimension_editor", GenericContainer::createContainerType);

   public DimensionEditorModule(IEventBus bus) {
      bus.addListener(this::registerMenuScreens);
   }

   public void init(FMLCommonSetupEvent event) {
   }

   public void initClient(FMLClientSetupEvent event) {
   }

   private void registerMenuScreens(RegisterMenuScreensEvent event) {
      event.register(CONTAINER_DIMENSION_EDITOR.get(), GuiDimensionEditor::new);
   }

   public void initConfig(IEventBus bus) {
      DimensionEditorConfig.init(Config.SERVER_BUILDER, Config.CLIENT_BUILDER);
   }

   public void initDatagen(DataGen dataGen, Provider provider) {
   }
}
