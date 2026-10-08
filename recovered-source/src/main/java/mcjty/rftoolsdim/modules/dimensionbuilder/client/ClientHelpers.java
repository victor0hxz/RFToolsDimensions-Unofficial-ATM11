package mcjty.rftoolsdim.modules.dimensionbuilder.client;

import mcjty.lib.varia.SafeClientTools;
import mcjty.rftoolsdim.dimension.data.ClientDimensionData;
import mcjty.rftoolsdim.modules.dimensionbuilder.items.DimensionMonitorItem;
import mcjty.rftoolsdim.modules.dimensionbuilder.items.PhasedFieldGenerator;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ClientHelpers {
   public static String getDimensionName(ItemStack stack) {
      Level world = SafeClientTools.getClientWorld();
      return world == null ? "" : world.dimension().identifier().getPath();
   }

   public static String getPowerString(ItemStack stack) {
      Level world = SafeClientTools.getClientWorld();
      if (world == null) {
         return "";
      } else {
         Identifier id = world.dimension().identifier();
         long power = ClientDimensionData.get().getPower(id);
         long max = ClientDimensionData.get().getMaxPower(id);
         return power == -1L ? "<n.a.>" : power + " (" + max + ")";
      }
   }

   public static void initOverrides(DimensionMonitorItem item) {
   }

   public static void initOverrides(PhasedFieldGenerator item) {
   }
}
