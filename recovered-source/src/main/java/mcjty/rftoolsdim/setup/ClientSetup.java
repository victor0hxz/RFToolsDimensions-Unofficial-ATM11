package mcjty.rftoolsdim.setup;

import mcjty.rftoolsdim.dimension.client.RFToolsDimensionSpecialEffects;
import mcjty.rftoolsdim.dimension.data.ClientDimensionData;
import mcjty.rftoolsdim.modules.dimensionbuilder.client.PowerItemModelProperty;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.Clone;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingIn;

public class ClientSetup {
   public static void init(FMLClientSetupEvent event) {
   }

   public static void registerRangeItemModelProperties(RegisterRangeSelectItemModelPropertyEvent event) {
      event.register(Identifier.fromNamespaceAndPath("rftoolsdim", "power"), PowerItemModelProperty.MAP_CODEC);
   }

   public static void onPlayerLogin(LoggingIn event) {
      ClientDimensionData.get().clear();
   }

   public static void onDimensionChange(Clone event) {
      RFToolsDimensionSpecialEffects.clearCache();
   }
}
