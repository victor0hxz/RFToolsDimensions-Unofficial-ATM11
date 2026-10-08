package mcjty.rftoolsdim.setup;

import mcjty.lib.compat.MainCompatHandler;
import mcjty.lib.setup.DefaultModSetup;
import mcjty.rftoolsdim.commands.ModCommands;
import mcjty.rftoolsdim.compat.LostCityCompat;
import mcjty.rftoolsdim.compat.RFToolsUtilityCompat;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = "rftoolsdim")
public class ModSetup extends DefaultModSetup {
   public void init(FMLCommonSetupEvent e) {
      super.init(e);
      NeoForge.EVENT_BUS.register(new ForgeEventHandlers());
   }

   @SubscribeEvent
   public static void serverLoad(RegisterCommandsEvent event) {
      ModCommands.register(event.getDispatcher());
   }

   protected void setupModCompat() {
      MainCompatHandler.registerWaila();
      MainCompatHandler.registerTOP();
      RFToolsUtilityCompat.register();
      LostCityCompat.register();
   }
}
