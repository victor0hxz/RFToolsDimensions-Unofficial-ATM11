package mcjty.rftoolsdim.modules.enscriber;

import java.util.function.Supplier;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.items.BaseBlockItem;
import mcjty.lib.modules.IModule;
import mcjty.rftoolsdim.modules.enscriber.blocks.EnscriberTileEntity;
import mcjty.rftoolsdim.modules.enscriber.client.GuiEnscriber;
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

public class EnscriberModule implements IModule {
   public static final RBlock<BaseBlock, BlockItem, EnscriberTileEntity> ENSCRIBER = Registration.RBLOCKS
      .registerBlock(
         "enscriber",
         EnscriberTileEntity.class,
         EnscriberTileEntity::createBlock,
         block -> new BaseBlockItem((Block)block.get(), Registration.createStandardProperties()),
         EnscriberTileEntity::new
      );
   public static final Supplier<BlockEntityType<EnscriberTileEntity>> TYPE_ENSCRIBER = ENSCRIBER.be();
   public static final Supplier<MenuType<GenericContainer>> CONTAINER_ENSCRIBER = Registration.CONTAINERS
      .register("enscriber", GenericContainer::createContainerType);

   public EnscriberModule(IEventBus bus) {
      bus.addListener(this::registerMenuScreens);
   }

   public void init(FMLCommonSetupEvent event) {
   }

   public void initClient(FMLClientSetupEvent event) {
   }

   private void registerMenuScreens(RegisterMenuScreensEvent event) {
      event.register(CONTAINER_ENSCRIBER.get(), GuiEnscriber::new);
   }

   public void initConfig(IEventBus bus) {
      EnscriberConfig.init(Config.SERVER_BUILDER, Config.CLIENT_BUILDER);
   }

   public void initDatagen(DataGen dataGen, Provider provider) {
   }
}
