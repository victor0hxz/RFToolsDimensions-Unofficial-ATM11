package mcjty.rftoolsdim.modules.essences;

import java.util.function.Supplier;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RBlock;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.items.BaseBlockItem;
import mcjty.lib.modules.IModule;
import mcjty.rftoolsdim.modules.essences.blocks.BiomeAbsorberTileEntity;
import mcjty.rftoolsdim.modules.essences.blocks.BlockAbsorberTileEntity;
import mcjty.rftoolsdim.modules.essences.blocks.FluidAbsorberTileEntity;
import mcjty.rftoolsdim.modules.essences.blocks.StructureAbsorberTileEntity;
import mcjty.rftoolsdim.modules.essences.data.BiomeAbsorberData;
import mcjty.rftoolsdim.modules.essences.data.BlockFluidAbsorberData;
import mcjty.rftoolsdim.modules.essences.data.StructureAbsorberData;
import mcjty.rftoolsdim.setup.Config;
import mcjty.rftoolsdim.setup.Registration;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public class EssencesModule implements IModule {
   public static final RBlock<BaseBlock, BlockItem, BlockAbsorberTileEntity> BLOCK_ABSORBER = Registration.RBLOCKS
      .registerBlock(
         "block_absorber",
         BlockAbsorberTileEntity.class,
         BlockAbsorberTileEntity::createBlock,
         block -> new BaseBlockItem((Block)block.get(), Registration.createStandardProperties()),
         BlockAbsorberTileEntity::new
      );
   public static final DeferredItem<BlockItem> BLOCK_ABSORBER_ITEM = BLOCK_ABSORBER.item();
   public static final Supplier<BlockEntityType<BlockAbsorberTileEntity>> TYPE_BLOCK_ABSORBER = BLOCK_ABSORBER.be();
   public static final RBlock<BaseBlock, BlockItem, FluidAbsorberTileEntity> FLUID_ABSORBER = Registration.RBLOCKS
      .registerBlock(
         "fluid_absorber",
         FluidAbsorberTileEntity.class,
         FluidAbsorberTileEntity::createBlock,
         block -> new BaseBlockItem((Block)block.get(), Registration.createStandardProperties()),
         FluidAbsorberTileEntity::new
      );
   public static final DeferredItem<BlockItem> FLUID_ABSORBER_ITEM = FLUID_ABSORBER.item();
   public static final Supplier<BlockEntityType<FluidAbsorberTileEntity>> TYPE_FLUID_ABSORBER = FLUID_ABSORBER.be();
   public static final RBlock<BaseBlock, BlockItem, BiomeAbsorberTileEntity> BIOME_ABSORBER = Registration.RBLOCKS
      .registerBlock(
         "biome_absorber",
         BiomeAbsorberTileEntity.class,
         BiomeAbsorberTileEntity::createBlock,
         block -> new BaseBlockItem((Block)block.get(), Registration.createStandardProperties()),
         BiomeAbsorberTileEntity::new
      );
   public static final DeferredItem<BlockItem> BIOME_ABSORBER_ITEM = BIOME_ABSORBER.item();
   public static final Supplier<BlockEntityType<BiomeAbsorberTileEntity>> TYPE_BIOME_ABSORBER = BIOME_ABSORBER.be();
   public static final RBlock<BaseBlock, BlockItem, StructureAbsorberTileEntity> STRUCTURE_ABSORBER = Registration.RBLOCKS
      .registerBlock(
         "structure_absorber",
         StructureAbsorberTileEntity.class,
         StructureAbsorberTileEntity::createBlock,
         block -> new BaseBlockItem((Block)block.get(), Registration.createStandardProperties()),
         StructureAbsorberTileEntity::new
      );
   public static final DeferredItem<BlockItem> STRUCTURE_ABSORBER_ITEM = STRUCTURE_ABSORBER.item();
   public static final Supplier<BlockEntityType<StructureAbsorberTileEntity>> TYPE_STRUCTURE_ABSORBER = STRUCTURE_ABSORBER.be();
   public static final Supplier<AttachmentType<BiomeAbsorberData>> BIOME_ABSORBER_DATA = Registration.ATTACHMENT_TYPES
      .register("biome_absorber_data", () -> AttachmentType.builder(() -> BiomeAbsorberData.DEFAULT).serialize(BiomeAbsorberData.CODEC.fieldOf("data")).build());
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<BiomeAbsorberData>> ITEM_BIOME_ABSORBER_DATA = Registration.COMPONENTS
      .registerComponentType("biome_absorber_data", builder -> builder.persistent(BiomeAbsorberData.CODEC).networkSynchronized(BiomeAbsorberData.STREAM_CODEC));
   public static final Supplier<AttachmentType<BlockFluidAbsorberData>> BLOCKFLUID_ABSORBER_DATA = Registration.ATTACHMENT_TYPES
      .register(
         "blockfluid_absorber_data",
         () -> AttachmentType.builder(() -> BlockFluidAbsorberData.DEFAULT).serialize(BlockFluidAbsorberData.CODEC.fieldOf("data")).build()
      );
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockFluidAbsorberData>> ITEM_BLOCKFLUID_ABSORBER_DATA = Registration.COMPONENTS
      .registerComponentType(
         "blockfluid_absorber_data", builder -> builder.persistent(BlockFluidAbsorberData.CODEC).networkSynchronized(BlockFluidAbsorberData.STREAM_CODEC)
      );
   public static final Supplier<AttachmentType<StructureAbsorberData>> STRUCTURE_ABSORBER_DATA = Registration.ATTACHMENT_TYPES
      .register(
         "structure_absorber_data",
         () -> AttachmentType.builder(() -> StructureAbsorberData.DEFAULT).serialize(StructureAbsorberData.CODEC.fieldOf("data")).build()
      );
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<StructureAbsorberData>> ITEM_STRUCTURE_ABSORBER_DATA = Registration.COMPONENTS
      .registerComponentType(
         "structure_absorber_data", builder -> builder.persistent(StructureAbsorberData.CODEC).networkSynchronized(StructureAbsorberData.STREAM_CODEC)
      );

   public void init(FMLCommonSetupEvent event) {
   }

   public void initClient(FMLClientSetupEvent event) {
   }

   public void initConfig(IEventBus bus) {
      EssencesConfig.init(Config.SERVER_BUILDER, Config.CLIENT_BUILDER);
   }

   public void initDatagen(DataGen dataGen, Provider provider) {
   }
}
