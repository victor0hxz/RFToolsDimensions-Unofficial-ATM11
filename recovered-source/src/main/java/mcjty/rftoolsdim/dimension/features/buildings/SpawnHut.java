package mcjty.rftoolsdim.dimension.features.buildings;

import mcjty.rftoolsdim.modules.decorative.DecorativeModule;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.neoforge.common.util.Lazy;

public class SpawnHut extends BuildingTemplate {
   public static final Lazy<SpawnHut> SPAWN_HUT = Lazy.of(SpawnHut::new);

   public SpawnHut() {
      this.palette('@', Blocks.COMMAND_BLOCK);
      this.palette('#', DecorativeModule.DIMENSIONAL_SMALL_BLOCK);
      this.palette('.', DecorativeModule.DIMENSIONAL_BLANK);
      this.palette('*', Blocks.GLOWSTONE);
      this.palette('+', Blocks.GLASS_PANE);
      this.palette(' ', Blocks.AIR);
      this.palette('D', (BlockState)Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
      this.palette('d', (BlockState)Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
      this.palette('_', Blocks.STONE_PRESSURE_PLATE);
      this.slice()
         .row("###########")
         .row("#.........#")
         .row("#.*.....*.#")
         .row("#.........#")
         .row("#.........#")
         .row("#....@....#")
         .row("#.........#")
         .row("#.........#")
         .row("#.*.....*.#")
         .row("#.........#")
         .row("###########");
      this.slice()
         .row("#.+.+.+.+.#")
         .row(".         .")
         .row("+         +")
         .row(".         .")
         .row("+         +")
         .row(".         .")
         .row("+         +")
         .row(".         .")
         .row("+         +")
         .row(".    _    .")
         .row("#.+..D..+.#");
      this.slice()
         .row("#.+.+.+.+.#")
         .row(".         .")
         .row("+         +")
         .row(".         .")
         .row("+         +")
         .row(".         .")
         .row("+         +")
         .row(".         .")
         .row("+         +")
         .row(".         .")
         .row("#.+..d..+.#");
      this.slice()
         .row("#.+.+.+.+.#")
         .row(".         .")
         .row("+         +")
         .row(".         .")
         .row("+         +")
         .row(".         .")
         .row("+         +")
         .row(".         .")
         .row("+         +")
         .row(".         .")
         .row("#.+.....+.#");
      this.slice()
         .row("###########")
         .row("#.........#")
         .row("#.........#")
         .row("#.........#")
         .row("#.........#")
         .row("#.........#")
         .row("#.........#")
         .row("#.........#")
         .row("#.........#")
         .row("#.........#")
         .row("###########");
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
