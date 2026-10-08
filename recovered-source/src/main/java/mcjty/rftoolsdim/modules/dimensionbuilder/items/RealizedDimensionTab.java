package mcjty.rftoolsdim.modules.dimensionbuilder.items;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcjty.lib.gui.ManualEntry;
import mcjty.lib.setup.RegistrationContext;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.ComponentFactory;
import mcjty.lib.varia.LevelTools;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.SafeClientTools;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsdim.dimension.data.ClientDimensionData;
import mcjty.rftoolsdim.dimension.data.DimensionData;
import mcjty.rftoolsdim.dimension.data.DimensionSettings;
import mcjty.rftoolsdim.dimension.data.PersistantDimensionManager;
import mcjty.rftoolsdim.dimension.descriptor.CompiledDescriptor;
import mcjty.rftoolsdim.dimension.descriptor.CompiledFeature;
import mcjty.rftoolsdim.dimension.descriptor.DescriptorError;
import mcjty.rftoolsdim.dimension.descriptor.DimensionDescriptor;
import mcjty.rftoolsdim.dimension.terraintypes.RFToolsChunkGenerator;
import mcjty.rftoolsdim.modules.dimensionbuilder.DimensionBuilderModule;
import mcjty.rftoolsdim.modules.dimensionbuilder.data.RealizedTabData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class RealizedDimensionTab extends Item implements ITooltipSettings {
   public RealizedDimensionTab() {
      super(RegistrationContext.prepareItemProperties(new Properties()).stacksTo(1));
   }

   @Nonnull
   public InteractionResult use(Level world, Player player, @Nonnull InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (!world.isClientSide() && player.isShiftKeyDown()) {
         RealizedTabData tab = (RealizedTabData)stack.getOrDefault(DimensionBuilderModule.ITEM_REALIZED_TAB_DATA, RealizedTabData.DEFAULT);
         Logging.message(player, tab.descriptor());
         if (tab.dimension().isPresent()) {
            Identifier dimension = tab.dimension().get();
            DimensionData data = PersistantDimensionManager.get(world).getData(dimension);
            if (data != null) {
               player.sendSystemMessage(ComponentFactory.literal(ChatFormatting.BLUE + "Energy: " + ChatFormatting.WHITE + data.getEnergy()));
               DimensionDescriptor descriptor = data.getDescriptor();
               descriptor.dump(player);
               player.sendSystemMessage(ComponentFactory.literal("-----------------------------"));
               DimensionDescriptor randomized = data.getRandomizedDescriptor();
               randomized.dump(player);
            }

            ResourceKey<Level> id = LevelTools.getId(dimension);
            ServerLevel serverWorld = ServerLifecycleHooks.getCurrentServer().getLevel(id);
            ChunkGenerator generator = serverWorld.getChunkSource().getGenerator();
            if (generator instanceof RFToolsChunkGenerator) {
               DimensionSettings settings = ((RFToolsChunkGenerator)generator).getDimensionSettings();
               player.sendSystemMessage(ComponentFactory.literal(ChatFormatting.BLUE + "Seed: " + ChatFormatting.WHITE + settings.getSeed()));
            }
         }
      }

      return InteractionResult.SUCCESS;
   }

   public void appendHoverText(
      @Nonnull ItemStack stack, @Nullable TooltipContext context, TooltipDisplay display, @Nonnull Consumer<Component> output, @Nonnull TooltipFlag flagIn
   ) {
      List<Component> list = new ArrayList<>();

      try {
         super.appendHoverText(stack, context, display, output, flagIn);
         RealizedTabData tab = (RealizedTabData)stack.get(DimensionBuilderModule.ITEM_REALIZED_TAB_DATA);
         if (tab != null) {
            Identifier dimension = tab.dimension().orElse(null);
            if (dimension != null) {
               list.add(ComponentFactory.literal("Name: " + dimension.getPath()).withStyle(ChatFormatting.BLUE));
            } else if (tab.name().isPresent()) {
               String name = tab.name().get();
               list.add(ComponentFactory.literal("Name: " + name).withStyle(ChatFormatting.BLUE));
            }

            if (SafeClientTools.isSneaking()) {
               String descriptionString = tab.descriptor();
               String randomizedString = tab.randomized();
               this.constructDescriptionHelp(list, descriptionString, randomizedString);
            } else {
               list.add(ComponentFactory.literal(ChatFormatting.GREEN + "    <Press Shift>"));
            }

            int ticksLeft = tab.ticksLeft();
            if (ticksLeft == 0) {
               long power = ClientDimensionData.get().getPower(dimension);
               long max = ClientDimensionData.get().getMaxPower(dimension);
               list.add(ComponentFactory.literal("Dimension ready!").withStyle(ChatFormatting.BLUE));
               int maintainCost = tab.rfMaintainCost();
               list.add(ComponentFactory.literal(ChatFormatting.YELLOW + "    Maintenance cost: " + maintainCost + " RF/tick"));
               list.add(ComponentFactory.literal(ChatFormatting.YELLOW + "    Current power: " + power + " (" + max + ")"));
            } else {
               int createCost = tab.rfCreateCost();
               int maintainCost = tab.rfMaintainCost();
               int tickCost = tab.tickCost();
               int percentage = 0;
               if (tickCost != 0) {
                  percentage = (tickCost - ticksLeft) * 100 / tickCost;
               }

               list.add(ComponentFactory.literal(ChatFormatting.BLUE + "Dimension progress: " + percentage + "%"));
               list.add(ComponentFactory.literal(ChatFormatting.YELLOW + "    Creation cost: " + createCost + " RF/tick"));
               list.add(ComponentFactory.literal(ChatFormatting.YELLOW + "    Maintenance cost: " + maintainCost + " RF/tick"));
               list.add(ComponentFactory.literal(ChatFormatting.YELLOW + "    Tick cost: " + tickCost + " ticks"));
            }
         }
      } finally {
         list.forEach(output);
      }
   }

   private void constructDescriptionHelp(List<Component> list, String descriptionString, String randomizedString) {
      DimensionDescriptor descriptor = new DimensionDescriptor();
      descriptor.read(descriptionString);
      DimensionDescriptor randomizedDescriptor = new DimensionDescriptor();
      if (!randomizedString.isEmpty()) {
         randomizedDescriptor.read(randomizedString);
      }

      CompiledDescriptor compiledDescriptor = new CompiledDescriptor();

      try {
         compiledDescriptor.compile(descriptor, randomizedDescriptor);
         if (compiledDescriptor.getTerrainType() != null) {
            list.add(ComponentFactory.literal(ChatFormatting.GREEN + "    Terrain: " + ChatFormatting.WHITE + compiledDescriptor.getTerrainType().getName()));
         }

         if (compiledDescriptor.getBiomeControllerType() != null) {
            list.add(
               ComponentFactory.literal(
                  ChatFormatting.GREEN + "    Biome Controller: " + ChatFormatting.WHITE + compiledDescriptor.getBiomeControllerType().getName()
               )
            );
         }

         if (compiledDescriptor.getTimeType() != null) {
            list.add(ComponentFactory.literal(ChatFormatting.GREEN + "    Time: " + ChatFormatting.WHITE + compiledDescriptor.getTimeType().getName()));
         }

         for (CompiledFeature feature : compiledDescriptor.getFeatures()) {
            list.add(ComponentFactory.literal(ChatFormatting.GREEN + "    Feature: " + ChatFormatting.WHITE + feature.getFeatureType().getName()));
         }
      } catch (DescriptorError var9) {
         list.add(ComponentFactory.literal(ChatFormatting.RED + "Parse error: " + var9.getMessage()));
      }
   }

   public ManualEntry getManualEntry() {
      return ManualHelper.create("rftoolsdim:dimensions/dimension_tabs");
   }
}
