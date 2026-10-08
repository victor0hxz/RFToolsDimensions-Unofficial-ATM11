package mcjty.rftoolsdim.dimension.features;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;

public interface IFeature {
   boolean generate(WorldGenLevel var1, ChunkGenerator var2, RandomSource var3, BlockPos var4, List<BlockState> var5, List<BlockState> var6, long var7);

   static BlockState select(List<BlockState> states, RandomSource random) {
      return states.size() == 1 ? states.get(0) : states.get(random.nextInt(states.size()));
   }
}
