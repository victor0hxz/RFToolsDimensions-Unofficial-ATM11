package mcjty.rftoolsdim.modules.dimensioneditor;

import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

public class DimensionEditorConfig {
   public static final String SUB_CATEGORY_DIMENSION_EDITOR = "dimensioneditor";
   public static IntValue EDITOR_MAXENERGY;
   public static IntValue EDITOR_RECEIVEPERTICK;
   public static BooleanValue TNT_CAN_DESTROY_DIMENSION;

   public static void init(Builder SERVER_BUILDER, Builder CLIENT_BUILDER) {
      SERVER_BUILDER.comment("Dimension Editor settings").push("dimensioneditor");
      EDITOR_MAXENERGY = SERVER_BUILDER.comment("Maximum RF storage that the dimension editor can hold")
         .defineInRange("generatorMaxRF", 10000000, 0, Integer.MAX_VALUE);
      EDITOR_RECEIVEPERTICK = SERVER_BUILDER.comment("Maximum RF storage that the dimension editor can receive per side")
         .defineInRange("generatorMaxRF", 20000, 0, Integer.MAX_VALUE);
      TNT_CAN_DESTROY_DIMENSION = SERVER_BUILDER.comment("Set to true to allow the dimension editor to destroy dimensions using tnt")
         .define("tntCanDestroyDimension", true);
      SERVER_BUILDER.pop();
   }
}
