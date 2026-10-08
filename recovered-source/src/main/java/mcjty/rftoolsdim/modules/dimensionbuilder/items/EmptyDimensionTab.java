package mcjty.rftoolsdim.modules.dimensionbuilder.items;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcjty.lib.gui.ManualEntry;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.ComponentFactory;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsdim.RFToolsDim;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.TooltipDisplay;

public class EmptyDimensionTab extends Item implements ITooltipSettings {
   public EmptyDimensionTab() {
      super(RFToolsDim.setup.defaultProperties().stacksTo(1));
   }

   public void appendHoverText(
      @Nonnull ItemStack stack, @Nullable TooltipContext context, TooltipDisplay display, @Nonnull Consumer<Component> output, @Nonnull TooltipFlag flagIn
   ) {
      List<Component> list = new ArrayList<>();

      try {
         super.appendHoverText(stack, context, display, output, flagIn);
         list.add(ComponentFactory.literal(ChatFormatting.YELLOW + "Put this empty dimension tab in a 'Dimension Enscriber'"));
         list.add(ComponentFactory.literal(ChatFormatting.YELLOW + "where you can construct a dimension using dimlets"));
      } finally {
         list.forEach(output);
      }
   }

   public ManualEntry getManualEntry() {
      return ManualHelper.create("rftoolsdim:dimensions/dimension_tabs");
   }
}
