package mcjty.rftoolsdim.dimension.noisesettings;

import net.minecraft.data.worldgen.SurfaceRuleData;
import net.minecraft.world.level.levelgen.SurfaceRules.RuleSource;

public class SurfaceRuleDataBuilder {
   boolean what = false;
   boolean bedrockRoof = false;
   boolean bedrockFloor = false;

   public SurfaceRuleDataBuilder what(boolean what) {
      this.what = what;
      return this;
   }

   public SurfaceRuleDataBuilder bedrockRoof(boolean bedrockRoof) {
      this.bedrockRoof = bedrockRoof;
      return this;
   }

   public SurfaceRuleDataBuilder bedrockFloor(boolean bedrockFloor) {
      this.bedrockFloor = bedrockFloor;
      return this;
   }

   public static SurfaceRuleDataBuilder create() {
      return new SurfaceRuleDataBuilder();
   }

   public RuleSource build() {
      return SurfaceRuleData.overworldLike(this.what, this.bedrockRoof, this.bedrockFloor);
   }
}
