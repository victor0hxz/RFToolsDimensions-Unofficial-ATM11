package mcjty.rftoolsdim.dimension.features.buildings;

import mcjty.rftoolsdim.modules.decorative.DecorativeModule;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.util.Lazy;

public class SpawnPlatform extends BuildingTemplate {
   public static final Lazy<SpawnPlatform> SPAWN_PLATFORM = Lazy.of(SpawnPlatform::new);

   public SpawnPlatform() {
      this.palette('_', Blocks.COMMAND_BLOCK);
      this.palette('#', DecorativeModule.DIMENSIONAL_SMALL_BLOCK);
      this.palette('.', DecorativeModule.DIMENSIONAL_BLANK);
      this.palette('*', Blocks.GLOWSTONE);
      this.palette(' ', Blocks.AIR);
      this.slice()
         .row("###########")
         .row("#.........#")
         .row("#.*.....*.#")
         .row("#.........#")
         .row("#.........#")
         .row("#...._....#")
         .row("#.........#")
         .row("#.........#")
         .row("#.*.....*.#")
         .row("#.........#")
         .row("###########");

      for (int i = 0; i < 3; i++) {
         this.slice()
            .row("           ")
            .row("           ")
            .row("           ")
            .row("           ")
            .row("           ")
            .row("           ")
            .row("           ")
            .row("           ")
            .row("           ")
            .row("           ")
            .row("           ");
      }
   }
}
