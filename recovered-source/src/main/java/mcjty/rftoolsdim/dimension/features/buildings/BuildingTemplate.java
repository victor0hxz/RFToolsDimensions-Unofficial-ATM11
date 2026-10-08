package mcjty.rftoolsdim.dimension.features.buildings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import mcjty.lib.blocks.RBlock;
import mcjty.rftoolsdim.dimension.features.IFeature;
import mcjty.rftoolsdim.dimension.terraintypes.TerrainType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BuildingTemplate {
   private final Map<Character, BlockState> palette = new HashMap<>();
   private final Map<Character, BiConsumer<WorldGenLevel, BlockPos>> paletteSpecial = new HashMap<>();
   private final List<BuildingTemplate.Slice> slices = new ArrayList<>();

   public BuildingTemplate palette(Character key, RBlock<Block, BlockItem, BlockEntity> block) {
      return this.palette(key, block.block());
   }

   public BuildingTemplate palette(Character key, BlockState state) {
      this.palette.put(key, state);
      return this;
   }

   public BuildingTemplate palette(Character key, Block block) {
      this.palette.put(key, block.defaultBlockState());
      return this;
   }

   public BuildingTemplate palette(Character key, Supplier<Block> block) {
      this.palette.put(key, block.get().defaultBlockState());
      return this;
   }

   public BuildingTemplate palette(Character key, BiConsumer<WorldGenLevel, BlockPos> consumer) {
      this.paletteSpecial.put(key, consumer);
      return this;
   }

   public void generate(TerrainType type, WorldGenLevel reader, BlockPos pos, List<BlockState> states, BuildingTemplate.GenerateFlag flag) {
      switch (flag) {
         case PLAIN:
         default:
            break;
         case FILLDOWN:
            this.fillDown(reader, pos, states);
            break;
         case FILLDOWN_IFNOTVOID:
            if (!type.isVoidLike()) {
               this.fillDownIfNotVoid(reader, pos, states);
            }
      }

      int y = pos.getY();
      MutableBlockPos mpos = new MutableBlockPos();

      for (BuildingTemplate.Slice slice : this.slices) {
         int z = pos.getZ();

         for (String row : slice.rows) {
            for (int x = 0; x < row.length(); x++) {
               mpos.set(pos.getX() + x, y, z);
               char key = row.charAt(x);
               if (this.paletteSpecial.containsKey(key)) {
                  this.paletteSpecial.get(key).accept(reader, mpos);
               } else {
                  reader.setBlock(mpos, this.palette.get(key), 0);
               }
            }

            z++;
         }

         y++;
      }
   }

   private void fillDown(WorldGenLevel reader, BlockPos pos, List<BlockState> states) {
      int y = pos.getY();
      MutableBlockPos mpos = new MutableBlockPos();
      BuildingTemplate.Slice slice = this.slices.get(0);
      int z = pos.getZ();

      for (String row : slice.rows) {
         for (int x = 0; x < row.length(); x++) {
            for (int yy = y - 1; yy > 1; yy--) {
               mpos.set(pos.getX() + x, yy, z);
               BlockState state = reader.getBlockState(mpos);
               if (!state.isAir()) {
                  break;
               }

               BlockState blockState = IFeature.select(states, reader.getRandom());
               reader.setBlock(mpos, blockState, 0);
            }
         }

         z++;
      }
   }

   private void fillDownIfNotVoid(WorldGenLevel reader, BlockPos pos, List<BlockState> states) {
      int y = pos.getY();
      MutableBlockPos mpos = new MutableBlockPos();
      BuildingTemplate.Slice slice = this.slices.get(0);
      int z = pos.getZ();

      for (String row : slice.rows) {
         for (int x = 0; x < row.length(); x++) {
            boolean isVoid = true;

            for (int yy = y - 1; yy > reader.getMinY() + 1; yy--) {
               mpos.set(pos.getX() + x, yy, z);
               BlockState state = reader.getBlockState(mpos);
               if (!state.isAir()) {
                  isVoid = false;
                  break;
               }
            }

            if (!isVoid) {
               for (int yyx = y - 1; yyx > reader.getMinY() + 1; yyx--) {
                  mpos.set(pos.getX() + x, yyx, z);
                  BlockState state = reader.getBlockState(mpos);
                  if (!state.isAir()) {
                     break;
                  }

                  BlockState blockState = IFeature.select(states, reader.getRandom());
                  reader.setBlock(mpos, blockState, 0);
               }
            }
         }

         z++;
      }
   }

   public BuildingTemplate.Slice slice() {
      BuildingTemplate.Slice slice = new BuildingTemplate.Slice();
      this.slices.add(slice);
      return slice;
   }

   public static enum GenerateFlag {
      PLAIN,
      FILLDOWN,
      FILLDOWN_IFNOTVOID;
   }

   public static class Slice {
      private final List<String> rows = new ArrayList<>();

      public BuildingTemplate.Slice row(String r) {
         this.rows.add(r);
         return this;
      }
   }
}
