package mcjty.rftoolsdim.compat;

import java.util.HashMap;
import java.util.Map;
import mcjty.lib.compat.theoneprobe.McJtyLibTOPDriver;
import mcjty.lib.compat.theoneprobe.TOPDriver;
import mcjty.lib.varia.ComponentFactory;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsdim.modules.essences.EssencesConfig;
import mcjty.rftoolsdim.modules.essences.EssencesModule;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class RFToolsDimensionsTOPDriver implements TOPDriver {
   public static final RFToolsDimensionsTOPDriver DRIVER = new RFToolsDimensionsTOPDriver();
   private final Map<Identifier, TOPDriver> drivers = new HashMap<>();

   public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData data) {
      Identifier id = Tools.getId(blockState);
      if (!this.drivers.containsKey(id)) {
         if (blockState.getBlock() == EssencesModule.BLOCK_ABSORBER.block().get()) {
            this.drivers.put(id, new RFToolsDimensionsTOPDriver.BlockAbsorberDriver());
         } else if (blockState.getBlock() == EssencesModule.FLUID_ABSORBER.block().get()) {
            this.drivers.put(id, new RFToolsDimensionsTOPDriver.FluidAbsorberDriver());
         } else if (blockState.getBlock() == EssencesModule.BIOME_ABSORBER.block().get()) {
            this.drivers.put(id, new RFToolsDimensionsTOPDriver.BiomeAbsorberDriver());
         } else if (blockState.getBlock() == EssencesModule.STRUCTURE_ABSORBER.block().get()) {
            this.drivers.put(id, new RFToolsDimensionsTOPDriver.StructureAbsorberDriver());
         } else {
            this.drivers.put(id, new RFToolsDimensionsTOPDriver.DefaultDriver());
         }
      }

      TOPDriver driver = this.drivers.get(id);
      if (driver != null) {
         driver.addProbeInfo(mode, probeInfo, player, world, blockState, data);
      }
   }

   private static class BiomeAbsorberDriver implements TOPDriver {
      public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData data) {
         McJtyLibTOPDriver.DRIVER.addStandardProbeInfo(mode, probeInfo, player, world, blockState, data);
         Tools.safeConsume(
            world.getBlockEntity(data.getPos()),
            te -> {
               int absorbing = te.getAbsorbing();
               Identifier biome = te.getAbsorbingBiome();
               int pct = ((Integer)EssencesConfig.maxBiomeAbsorption.get() - absorbing) * 100 / (Integer)EssencesConfig.maxBiomeAbsorption.get();
               probeInfo.text(
                     ComponentFactory.literal("Biome: ")
                        .append(ComponentFactory.translatable(biome.toLanguageKey(Registries.BIOME.identifier().getPath().replace('/', '.'))))
                        .withStyle(ChatFormatting.GREEN)
                  )
                  .horizontal()
                  .progress(pct, 100, probeInfo.defaultProgressStyle().suffix("%"));
            },
            "Bad tile entity!"
         );
      }
   }

   private static class BlockAbsorberDriver implements TOPDriver {
      public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData data) {
         McJtyLibTOPDriver.DRIVER.addStandardProbeInfo(mode, probeInfo, player, world, blockState, data);
         Tools.safeConsume(
            world.getBlockEntity(data.getPos()),
            te -> {
               int absorbing = te.getAbsorbing();
               Identifier block = te.getAbsorbingBlock();
               int pct = ((Integer)EssencesConfig.maxBlockAbsorption.get() - absorbing) * 100 / (Integer)EssencesConfig.maxBlockAbsorption.get();
               ItemStack stack = new ItemStack(Tools.getBlock(block), 1);
               if (!stack.isEmpty()) {
                  probeInfo.text(
                        ComponentFactory.literal("Block: ")
                           .append(ComponentFactory.translatable(block.toLanguageKey(Registries.BLOCK.identifier().getPath()).replace('/', '.')))
                           .withStyle(ChatFormatting.GREEN)
                     )
                     .horizontal()
                     .progress(pct, 100, probeInfo.defaultProgressStyle().suffix("%"))
                     .item(stack);
               }
            },
            "Bad tile entity!"
         );
      }
   }

   private static class DefaultDriver implements TOPDriver {
      public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData data) {
         McJtyLibTOPDriver.DRIVER.addStandardProbeInfo(mode, probeInfo, player, world, blockState, data);
      }
   }

   private static class FluidAbsorberDriver implements TOPDriver {
      public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData data) {
         McJtyLibTOPDriver.DRIVER.addStandardProbeInfo(mode, probeInfo, player, world, blockState, data);
         Tools.safeConsume(
            world.getBlockEntity(data.getPos()),
            te -> {
               int absorbing = te.getAbsorbing();
               Identifier block = te.getAbsorbingBlock();
               if (block != null) {
                  int pct = ((Integer)EssencesConfig.maxFluidAbsorption.get() - absorbing) * 100 / (Integer)EssencesConfig.maxFluidAbsorption.get();
                  probeInfo.text(
                        ComponentFactory.literal("Fluid: ")
                           .append(ComponentFactory.translatable(block.toLanguageKey(Registries.FLUID.identifier().getPath().replace('/', '.'))))
                           .withStyle(ChatFormatting.GREEN)
                     )
                     .horizontal()
                     .progress(pct, 100, probeInfo.defaultProgressStyle().suffix("%"));
               }
            },
            "Bad tile entity!"
         );
      }
   }

   private static class StructureAbsorberDriver implements TOPDriver {
      public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData data) {
         McJtyLibTOPDriver.DRIVER.addStandardProbeInfo(mode, probeInfo, player, world, blockState, data);
         Tools.safeConsume(
            world.getBlockEntity(data.getPos()),
            te -> {
               int absorbing = te.getAbsorbing();
               Identifier structure = te.getAbsorbingStructure();
               if (structure != null) {
                  int pct = ((Integer)EssencesConfig.maxStructureAbsorption.get() - absorbing) * 100 / (Integer)EssencesConfig.maxStructureAbsorption.get();
                  probeInfo.text(
                        ComponentFactory.literal("Structure: ")
                           .append(ComponentFactory.translatable(structure.toLanguageKey(Registries.STRUCTURE.identifier().getPath().replace('/', '.'))))
                           .withStyle(ChatFormatting.GREEN)
                     )
                     .horizontal()
                     .progress(pct, 100, probeInfo.defaultProgressStyle().suffix("%"));
               }
            },
            "Bad tile entity!"
         );
      }
   }
}
