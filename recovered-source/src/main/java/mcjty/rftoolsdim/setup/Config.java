package mcjty.rftoolsdim.setup;

import mcjty.lib.modules.Modules;
import mcjty.rftoolsdim.dimension.DimensionConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;

public class Config {
   public static final String CATEGORY_GENERAL = "general";
   public static final Builder SERVER_BUILDER = new Builder();
   public static final Builder COMMON_BUILDER = new Builder();
   public static final Builder CLIENT_BUILDER = new Builder();
   public static ModConfigSpec SERVER_CONFIG;
   public static ModConfigSpec COMMON_CONFIG;
   public static ModConfigSpec CLIENT_CONFIG;

   public static void register(ModContainer mod, IEventBus bus, Modules modules) {
      setupGeneralConfig();
      DimensionConfig.init();
      modules.initConfig(bus);
      SERVER_CONFIG = SERVER_BUILDER.build();
      COMMON_CONFIG = COMMON_BUILDER.build();
      CLIENT_CONFIG = CLIENT_BUILDER.build();
      mod.registerConfig(Type.CLIENT, CLIENT_CONFIG);
      mod.registerConfig(Type.COMMON, COMMON_CONFIG);
      mod.registerConfig(Type.SERVER, SERVER_CONFIG);
   }

   private static void setupGeneralConfig() {
      SERVER_BUILDER.comment("General settings").push("general");
      CLIENT_BUILDER.comment("General settings").push("general");
      SERVER_BUILDER.pop();
      CLIENT_BUILDER.pop();
   }
}
