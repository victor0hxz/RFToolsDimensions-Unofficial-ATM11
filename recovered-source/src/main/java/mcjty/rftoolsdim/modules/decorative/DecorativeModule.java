package mcjty.rftoolsdim.modules.decorative;

import mcjty.lib.blocks.RBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.modules.IModule;
import mcjty.lib.setup.RegistrationContext;
import mcjty.rftoolsdim.setup.Registration;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

public class DecorativeModule implements IModule {
   public static final RBlock<Block, BlockItem, BlockEntity> DIMENSIONAL_BLANK = Registration.registerSimpleBlock(
      "dimensional_blank_block", () -> new Block(RegistrationContext.prepareBlockProperties(BlockBuilder.standardIron()))
   );
   public static final RBlock<Block, BlockItem, BlockEntity> DIMENSIONAL_BLOCK = Registration.registerSimpleBlock(
      "dimensional_block", () -> new Block(RegistrationContext.prepareBlockProperties(BlockBuilder.standardIron()))
   );
   public static final RBlock<Block, BlockItem, BlockEntity> DIMENSIONAL_SMALL_BLOCK = Registration.registerSimpleBlock(
      "dimensional_small_blocks", () -> new Block(RegistrationContext.prepareBlockProperties(BlockBuilder.standardIron()))
   );
   public static final RBlock<Block, BlockItem, BlockEntity> DIMENSIONAL_CROSS_BLOCK = Registration.registerSimpleBlock(
      "dimensional_cross_block", () -> new Block(RegistrationContext.prepareBlockProperties(BlockBuilder.standardIron()))
   );
   public static final RBlock<Block, BlockItem, BlockEntity> DIMENSIONAL_CROSS2_BLOCK = Registration.registerSimpleBlock(
      "dimensional_cross2_block", () -> new Block(RegistrationContext.prepareBlockProperties(BlockBuilder.standardIron()))
   );
   public static final RBlock<Block, BlockItem, BlockEntity> DIMENSIONAL_PATTERN1_BLOCK = Registration.registerSimpleBlock(
      "dimensional_pattern1_block", () -> new Block(RegistrationContext.prepareBlockProperties(BlockBuilder.standardIron()))
   );
   public static final RBlock<Block, BlockItem, BlockEntity> DIMENSIONAL_PATTERN2_BLOCK = Registration.registerSimpleBlock(
      "dimensional_pattern2_block", () -> new Block(RegistrationContext.prepareBlockProperties(BlockBuilder.standardIron()))
   );

   public void init(FMLCommonSetupEvent event) {
   }

   public void initClient(FMLClientSetupEvent event) {
   }

   public void initConfig(IEventBus bus) {
   }

   public void initDatagen(DataGen dataGen, Provider provider) {
   }
}
