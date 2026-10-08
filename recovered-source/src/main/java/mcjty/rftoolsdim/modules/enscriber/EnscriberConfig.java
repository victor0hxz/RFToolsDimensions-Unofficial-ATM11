package mcjty.rftoolsdim.modules.enscriber;

import net.neoforged.neoforge.common.ModConfigSpec.Builder;

public class EnscriberConfig {
   public static final String SUB_CATEGORY_ENSCRIBER = "enscriber";

   public static void init(Builder SERVER_BUILDER, Builder CLIENT_BUILDER) {
      SERVER_BUILDER.comment("Enscriber settings").push("enscriber");
      SERVER_BUILDER.pop();
   }
}
