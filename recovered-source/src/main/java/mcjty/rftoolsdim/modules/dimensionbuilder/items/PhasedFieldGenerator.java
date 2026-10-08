package mcjty.rftoolsdim.modules.dimensionbuilder.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcjty.lib.builder.InfoLine;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.gui.ManualEntry;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.IEnergyItem;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsdim.RFToolsDim;
import mcjty.rftoolsdim.dimension.DimensionConfig;
import mcjty.rftoolsdim.modules.dimensionbuilder.DimensionBuilderConfig;
import mcjty.rftoolsdim.modules.dimensionbuilder.DimensionBuilderModule;
import mcjty.rftoolsdim.modules.dimensionbuilder.data.PhasedFieldGeneratorData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class PhasedFieldGenerator extends Item implements IEnergyItem, ITooltipSettings {
   private final Lazy<TooltipBuilder> tooltipBuilder = Lazy.of(
      () -> new TooltipBuilder()
         .info(new InfoLine[]{TooltipBuilder.key("message.rftoolsdim.shiftmessage")})
         .infoShift(new InfoLine[]{TooltipBuilder.header(), TooltipBuilder.gold(), TooltipBuilder.parameter("power", this::getEnergyString)})
   );

   private String getEnergyString(ItemStack stack) {
      return Long.toString(PhasedFieldGeneratorData.getEnergy(stack));
   }

   public PhasedFieldGenerator() {
      super(RFToolsDim.setup.defaultProperties().stacksTo(1));
   }

   public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
      return oldStack.isEmpty() != newStack.isEmpty() ? true : oldStack.getItem() != newStack.getItem();
   }

   public void appendHoverText(
      @Nonnull ItemStack stack, @Nullable TooltipContext context, TooltipDisplay display, @Nonnull Consumer<Component> output, @Nonnull TooltipFlag flagIn
   ) {
      List<Component> list = new ArrayList<>();

      try {
         super.appendHoverText(stack, context, display, output, flagIn);
         ((TooltipBuilder)this.tooltipBuilder.get()).makeTooltip(Tools.getId(this), stack, list, flagIn);
      } finally {
         list.forEach(output);
      }
   }

   public long receiveEnergyL(ItemStack container, long maxReceive, boolean simulate) {
      long energy = PhasedFieldGeneratorData.getEnergy(container);
      long energyReceived = Math.min(
         this.getMaxEnergyStoredL(container) - energy, Math.min((Long)DimensionBuilderConfig.PHASEDFIELD_RECEIVEPERTICK.get(), maxReceive)
      );
      if (!simulate) {
         energy += energyReceived;
         PhasedFieldGeneratorData.setEnergy(container, energy);
      }

      return energyReceived;
   }

   public long extractEnergyL(ItemStack container, long maxExtract, boolean simulate) {
      long energy = PhasedFieldGeneratorData.getEnergy(container);
      long energyExtracted = Math.min(energy, Math.min((Long)DimensionBuilderConfig.PHASEDFIELD_CONSUMEPERTICK.get() * 10L, maxExtract));
      if (!simulate) {
         energy -= energyExtracted;
         PhasedFieldGeneratorData.setEnergy(container, energy);
      }

      return energyExtracted;
   }

   public long getEnergyStoredL(ItemStack container) {
      return PhasedFieldGeneratorData.getEnergy(container);
   }

   public long getMaxEnergyStoredL(ItemStack container) {
      return (Long)DimensionBuilderConfig.PHASEDFIELD_MAXENERGY.get();
   }

   public static boolean checkValidPhasedFieldGenerator(Player player, boolean consume, int tickCost) {
      Inventory inventory = player.getInventory();

      for (int i = 0; i < Inventory.getSelectionSize(); i++) {
         ItemStack slot = inventory.getItem(i);
         if (!slot.isEmpty() && slot.getItem() == DimensionBuilderModule.PHASED_FIELD_GENERATOR.get()) {
            PhasedFieldGenerator pfg = (PhasedFieldGenerator)slot.getItem();
            int energyStored = pfg.getEnergyStored(slot);
            int toConsume;
            if ((Boolean)DimensionConfig.ENABLE_DYNAMIC_PHASECOST.get()) {
               toConsume = (int)(10 * tickCost * (Double)DimensionConfig.DYNAMIC_PHASECOST_AMOUNT.get());
            } else {
               toConsume = (int)(10L * (Long)DimensionBuilderConfig.PHASEDFIELD_CONSUMEPERTICK.get());
            }

            if (energyStored >= toConsume) {
               if (consume) {
                  pfg.extractEnergy(slot, toConsume, false);
               }

               return true;
            }
         }
      }

      return false;
   }

   public IEnergyStorage createEnergyStorage(final ItemStack container) {
      return new IEnergyStorage() {
         {
            Objects.requireNonNull(PhasedFieldGenerator.this);
         }

         public int receiveEnergy(int maxReceive, boolean simulate) {
            return (int)PhasedFieldGenerator.this.receiveEnergyL(container, maxReceive, simulate);
         }

         public int extractEnergy(int maxExtract, boolean simulate) {
            return (int)PhasedFieldGenerator.this.extractEnergyL(container, maxExtract, simulate);
         }

         public int getEnergyStored() {
            return (int)PhasedFieldGenerator.this.getEnergyStoredL(container);
         }

         public int getMaxEnergyStored() {
            return (int)PhasedFieldGenerator.this.getMaxEnergyStoredL(container);
         }

         public boolean canExtract() {
            return false;
         }

         public boolean canReceive() {
            return true;
         }
      };
   }

   public ManualEntry getManualEntry() {
      return ManualHelper.create("rftoolsdim:dimensions/phased_field_generator");
   }
}
