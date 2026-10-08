package mcjty.rftoolsdim.modules.dimensionbuilder.items;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcjty.lib.builder.InfoLine;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.gui.ManualEntry;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsdim.RFToolsDim;
import mcjty.rftoolsdim.modules.dimensionbuilder.client.ClientHelpers;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.neoforge.common.util.Lazy;

public class DimensionMonitorItem extends Item implements ITooltipSettings {
   private final Lazy<TooltipBuilder> tooltipBuilder = Lazy.of(
      () -> new TooltipBuilder()
         .info(new InfoLine[]{TooltipBuilder.key("message.rftoolsdim.shiftmessage"), TooltipBuilder.parameter("power", ClientHelpers::getPowerString)})
         .infoShift(
            new InfoLine[]{
               TooltipBuilder.header(),
               TooltipBuilder.parameter("power", ClientHelpers::getPowerString),
               TooltipBuilder.parameter("name", ClientHelpers::getDimensionName)
            }
         )
   );

   public DimensionMonitorItem() {
      super(RFToolsDim.setup.defaultProperties().stacksTo(1));
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

   public ManualEntry getManualEntry() {
      return ManualHelper.create("rftoolsdim:dimensions/dimension_monitor");
   }
}
