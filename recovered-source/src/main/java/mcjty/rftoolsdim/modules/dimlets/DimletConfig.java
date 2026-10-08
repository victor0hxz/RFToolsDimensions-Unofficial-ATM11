package mcjty.rftoolsdim.modules.dimlets;

import java.util.ArrayList;
import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;

public class DimletConfig {
   public static final String SUB_CATEGORY_DIMLETS = "dimlets";
   public static ConfigValue<List<? extends String>> DIMLET_PACKAGES;

   public static void init(Builder SERVER_BUILDER, Builder CLIENT_BUILDER) {
      SERVER_BUILDER.comment("Dimlets settings").push("dimlets");
      List<String> defValues = new ArrayList<>();
      defValues.add("base.json");
      defValues.add("vanilla_blocks.json");
      defValues.add("vanilla_tags.json");
      defValues.add("vanilla_fluids.json");
      defValues.add("vanilla_biomes.json");
      defValues.add("vanilla_structures.json");
      defValues.add("rftools.json");
      defValues.add("appliedenergistics2.json");
      defValues.add("biggerreactors.json");
      defValues.add("bigreactors.json");
      defValues.add("botania.json");
      defValues.add("immersiveengineering.json");
      defValues.add("mekanism.json");
      defValues.add("powah.json");
      defValues.add("quark.json");
      defValues.add("tconstruct.json");
      defValues.add("thermal.json");
      defValues.add("biomesoplenty.json");
      defValues.add("emendatusenigmatica.json");
      DIMLET_PACKAGES = SERVER_BUILDER.comment(
            "This is a list of dimlet packages that will be used. Later dimlet packages can override dimlets defined in earlier packages. You can place these packages in the 'config/rftoolsdim' folder"
         )
         .defineList("dimletPackages", defValues, o -> o instanceof String);
      SERVER_BUILDER.pop();
   }
}
