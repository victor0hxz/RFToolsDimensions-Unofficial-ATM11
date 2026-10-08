package mcjty.rftoolsdim.modules.workbench;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.items.BaseBlockItem;
import mcjty.lib.modules.IModule;
import mcjty.rftoolsdim.modules.workbench.blocks.KnowledgeHolderTileEntity;
import mcjty.rftoolsdim.modules.workbench.blocks.ResearcherTileEntity;
import mcjty.rftoolsdim.modules.workbench.blocks.WorkbenchTileEntity;
import mcjty.rftoolsdim.modules.workbench.client.GuiHolder;
import mcjty.rftoolsdim.modules.workbench.client.GuiResearcher;
import mcjty.rftoolsdim.modules.workbench.client.GuiWorkbench;
import mcjty.rftoolsdim.modules.workbench.client.ResearcherRenderer;
import mcjty.rftoolsdim.setup.Config;
import mcjty.rftoolsdim.setup.Registration;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class WorkbenchModule implements IModule {
   public static final RBlock<BaseBlock, BlockItem, WorkbenchTileEntity> WORKBENCH = Registration.RBLOCKS
      .registerBlock(
         "dimlet_workbench",
         WorkbenchTileEntity.class,
         WorkbenchTileEntity::createBlock,
         block -> new BaseBlockItem((Block)block.get(), Registration.createStandardProperties()),
         WorkbenchTileEntity::new
      );
   public static final Supplier<BlockEntityType<WorkbenchTileEntity>> TYPE_WORKBENCH = WORKBENCH.be();
   public static final Supplier<MenuType<GenericContainer>> CONTAINER_WORKBENCH = Registration.CONTAINERS
      .register("dimlet_workbench", GenericContainer::createContainerType);
   public static final RBlock<BaseBlock, BlockItem, KnowledgeHolderTileEntity> HOLDER = Registration.RBLOCKS
      .registerBlock(
         "knowledge_holder",
         KnowledgeHolderTileEntity.class,
         KnowledgeHolderTileEntity::createBlock,
         block -> new BaseBlockItem((Block)block.get(), Registration.createStandardProperties()),
         KnowledgeHolderTileEntity::new
      );
   public static final Supplier<BlockEntityType<KnowledgeHolderTileEntity>> TYPE_HOLDER = HOLDER.be();
   public static final Supplier<MenuType<GenericContainer>> CONTAINER_HOLDER = Registration.CONTAINERS
      .register("knowledge_holder", GenericContainer::createContainerType);
   public static final RBlock<BaseBlock, BlockItem, ResearcherTileEntity> RESEARCHER = Registration.RBLOCKS
      .registerBlock(
         "researcher",
         ResearcherTileEntity.class,
         ResearcherTileEntity::createBlock,
         block -> new BaseBlockItem((Block)block.get(), Registration.createStandardProperties()),
         ResearcherTileEntity::new
      );
   public static final Supplier<BlockEntityType<ResearcherTileEntity>> TYPE_RESEARCHER = RESEARCHER.be();
   public static final Supplier<MenuType<GenericContainer>> CONTAINER_RESEARCHER = Registration.CONTAINERS
      .register("researcher", GenericContainer::createContainerType);

   public WorkbenchModule(IEventBus bus) {
      bus.addListener(this::registerMenuScreens);
   }

   public static List<Identifier> onTextureStitch() {
      return Collections.singletonList(ResearcherRenderer.LIGHT);
   }

   public void init(FMLCommonSetupEvent event) {
   }

   public void initClient(FMLClientSetupEvent event) {
      ResearcherRenderer.register();
   }

   private void registerMenuScreens(RegisterMenuScreensEvent event) {
      event.register(CONTAINER_WORKBENCH.get(), GuiWorkbench::new);
      event.register(CONTAINER_HOLDER.get(), GuiHolder::new);
      event.register(CONTAINER_RESEARCHER.get(), GuiResearcher::new);
   }

   public void initConfig(IEventBus bus) {
      WorkbenchConfig.init(Config.SERVER_BUILDER, Config.CLIENT_BUILDER);
   }

   public void initDatagen(DataGen dataGen, Provider provider) {
   }
}
