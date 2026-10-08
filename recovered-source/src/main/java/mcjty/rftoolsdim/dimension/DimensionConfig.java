package mcjty.rftoolsdim.dimension;

import mcjty.rftoolsdim.setup.Config;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;
import net.neoforged.neoforge.common.ModConfigSpec.LongValue;

public class DimensionConfig {
   public static final String SUB_CATEGORY_DIMENSION = "dimensions";
   public static LongValue POWER_MULTIPLES;
   public static IntValue MIN_POWER_THRESSHOLD;
   public static IntValue MAX_POWER_THRESSHOLD;
   public static LongValue MAX_DIMENSION_POWER_MIN;
   public static LongValue MAX_DIMENSION_POWER_MAX;
   public static BooleanValue ENABLE_DYNAMIC_PHASECOST;
   public static DoubleValue DYNAMIC_PHASECOST_AMOUNT;
   public static BooleanValue PHASED_FIELD_GENERATOR_DEBUF;
   public static IntValue DIMPOWER_WARN0;
   public static IntValue DIMPOWER_WARN1;
   public static IntValue DIMPOWER_WARN2;
   public static IntValue DIMPOWER_WARN3;
   public static BooleanValue OWNER_DIMLET_REQUIRED;
   public static DoubleValue RANDOMIZED_DIMLET_COST_FACTOR;
   public static DoubleValue DIMLET_HUT_CHANCE;

   public static void init() {
      Config.SERVER_BUILDER.comment("Dimension settings").push("dimensions");
      RANDOMIZED_DIMLET_COST_FACTOR = Config.SERVER_BUILDER
         .comment("The maintenance cost of randomized dimlets is multiplied with this value before applying to the dimension")
         .defineInRange("randomizedDimletCostFactor", 0.1, 0.0, 10.0);
      DIMLET_HUT_CHANCE = Config.SERVER_BUILDER.comment("The chance of a dimlet hut for a given chunk").defineInRange("dimletHutChance", 0.005, 0.0, 1.0);
      MIN_POWER_THRESSHOLD = Config.SERVER_BUILDER
         .comment("At this maintenance cost thresshold and below the minimum dimension power (dimensionPowerMinimum is used")
         .defineInRange("minPowerThresshold", 100, 0, Integer.MAX_VALUE);
      MAX_POWER_THRESSHOLD = Config.SERVER_BUILDER
         .comment("At this maintenance cost thresshold and above the maximum dimension power (dimensionPowerMaximum is used")
         .defineInRange("maxPowerThresshold", 5000, 0, Integer.MAX_VALUE);
      MAX_DIMENSION_POWER_MIN = Config.SERVER_BUILDER
         .comment("Maximum power in a dimension. This is the minimum value used by dimensions that don't consume a lot of power")
         .defineInRange("dimensionPowerMinimum", 20000000L, 0L, Long.MAX_VALUE);
      MAX_DIMENSION_POWER_MAX = Config.SERVER_BUILDER
         .comment("Maximum power in a dimension. This is the maximum value used by dimensions that consume a lot of power")
         .defineInRange("dimensionPowerMaximum", 40000000L, 0L, Long.MAX_VALUE);
      POWER_MULTIPLES = Config.SERVER_BUILDER
         .comment("Maximum power of a dimension is always a multiple of this value")
         .defineInRange("powerMultiples", 500000L, 1L, Long.MAX_VALUE);
      DIMPOWER_WARN0 = Config.SERVER_BUILDER
         .comment(
            "The zero power percentage at which power warning signs are starting to happen. This is only used for lighting level. No other debuffs occur at this level."
         )
         .defineInRange("dimensionPowerWarn0", 12, 0, 100);
      DIMPOWER_WARN1 = Config.SERVER_BUILDER
         .comment("The first power percentage at which power warning signs are starting to happen")
         .defineInRange("dimensionPowerWarn1", 9, 0, 100);
      DIMPOWER_WARN2 = Config.SERVER_BUILDER
         .comment("The second power percentage at which power warning signs are starting to become worse")
         .defineInRange("dimensionPowerWarn2", 2, 0, 100);
      DIMPOWER_WARN3 = Config.SERVER_BUILDER
         .comment("The third power percentage at which power warning signs are starting to be very bad")
         .defineInRange("dimensionPowerWarn3", 1, 0, 100);
      ENABLE_DYNAMIC_PHASECOST = Config.SERVER_BUILDER
         .comment("Enable dynamic scaling of the Phase Field Generator cost based on world tick cost")
         .define("enableDynamicPhaseCost", false);
      DYNAMIC_PHASECOST_AMOUNT = Config.SERVER_BUILDER
         .comment("How much of the tick cost of the world is applied to the PFG cost, as a ratio from 0 to 1")
         .defineInRange("dynamicPhaseCostAmount", 0.05F, 0.0, 1.0);
      PHASED_FIELD_GENERATOR_DEBUF = Config.SERVER_BUILDER
         .comment("If true you will get some debufs when the PFG is in use. If false there will be no debufs")
         .define("phasedFieldGeneratorDebuf", true);
      OWNER_DIMLET_REQUIRED = Config.SERVER_BUILDER.comment("If true creating dimensions requires an owner dimlet").define("ownerDimletRequired", false);
      Config.SERVER_BUILDER.pop();
   }
}
