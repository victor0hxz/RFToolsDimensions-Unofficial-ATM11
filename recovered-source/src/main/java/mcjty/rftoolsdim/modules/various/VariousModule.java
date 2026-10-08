package mcjty.rftoolsdim.modules.various;

import mcjty.lib.blocks.RBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.modules.IModule;
import mcjty.lib.setup.RegistrationContext;
import mcjty.rftoolsdim.modules.various.blocks.ActivityProbeBlock;
import mcjty.rftoolsdim.setup.Registration;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

public class VariousModule implements IModule {
   public static final RBlock<ActivityProbeBlock, BlockItem, BlockEntity> ACTIVITY_PROBE = Registration.registerSimpleBlock(
      "activity_probe", () -> new ActivityProbeBlock(RegistrationContext.prepareBlockProperties(BlockBuilder.standardIron()))
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
