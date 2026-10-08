package mcjty.rftoolsdim.dimension.features.buildings;

import mcjty.rftoolsdim.dimension.DimensionRegistry;
import mcjty.rftoolsdim.modules.decorative.DecorativeModule;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.neoforge.common.util.Lazy;

public class DimletHut extends BuildingTemplate {
   public static final Lazy<DimletHut> DIMLET_HUT = Lazy.of(DimletHut::new);

   public DimletHut() {
      this.palette('#', DecorativeModule.DIMENSIONAL_SMALL_BLOCK);
      this.palette('+', DecorativeModule.DIMENSIONAL_BLOCK);
      this.palette('X', DecorativeModule.DIMENSIONAL_PATTERN2_BLOCK);
      this.palette('.', DecorativeModule.DIMENSIONAL_BLANK);
      this.palette('*', Blocks.GLOWSTONE);
      this.palette('1', Blocks.LIGHT_BLUE_STAINED_GLASS);
      this.palette(' ', Blocks.AIR);
      this.palette('D', (BlockState)Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
      this.palette('d', (BlockState)Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
      this.palette('_', Blocks.STONE_PRESSURE_PLATE);
      this.palette('C', (reader, pos) -> {
         reader.setBlock(pos, Blocks.CHEST.defaultBlockState(), 0);
         if (reader.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
            chest.setLootTable(DimensionRegistry.HUT_LOOT, reader.getRandom().nextLong());
         }
      });
      this.slice()
         .row("#########")
         .row("#.......#")
         .row("#.......#")
         .row("#.......#")
         .row("#.......#")
         .row("#.......#")
         .row("#.......#")
         .row("#.......#")
         .row("#########")
         .row("   ###   ");
      this.slice()
         .row("X+++++++X")
         .row("+   C   +")
         .row("+       +")
         .row("+       +")
         .row("+       +")
         .row("+       +")
         .row("+       +")
         .row("+   _   +")
         .row("X+1+D+1+X")
         .row("    _    ");
      this.slice()
         .row("X+++1+++X")
         .row("+       +")
         .row("+       +")
         .row("+       +")
         .row("1       1")
         .row("+       +")
         .row("+       +")
         .row("+       +")
         .row("X+1+d+1+X")
         .row("         ");
      this.slice()
         .row("X+++++++X")
         .row("+       +")
         .row("+       +")
         .row("+       +")
         .row("+       +")
         .row("+       +")
         .row("+       +")
         .row("+       +")
         .row("X+1+++1+X")
         .row("         ");
      this.slice()
         .row("XXXXXXXXX")
         .row("X.......X")
         .row("X.......X")
         .row("X.......X")
         .row("X.......X")
         .row("X.......X")
         .row("X.......X")
         .row("X.......X")
         .row("XXXXXXXXX")
         .row("         ");
   }
}
