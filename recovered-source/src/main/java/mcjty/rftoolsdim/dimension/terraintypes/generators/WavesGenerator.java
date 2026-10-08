package mcjty.rftoolsdim.dimension.terraintypes.generators;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import mcjty.rftoolsdim.dimension.terraintypes.RFToolsChunkGenerator;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import org.jetbrains.annotations.NotNull;

public class WavesGenerator {
   public static final int WAVE_BASE = 80;

   @NotNull
   public static CompletableFuture<ChunkAccess> fillFromNoise(ChunkAccess chunkAccess, RFToolsChunkGenerator generator) {
      ChunkPos chunkpos = chunkAccess.getPos();
      MutableBlockPos mpos = new MutableBlockPos();
      Heightmap hmOcean = chunkAccess.getOrCreateHeightmapUnprimed(Types.OCEAN_FLOOR_WG);
      Heightmap hmWorld = chunkAccess.getOrCreateHeightmapUnprimed(Types.WORLD_SURFACE_WG);
      BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
      int minBuildHeight = chunkAccess.getMinY();
      BlockState defaultBlock = generator.getDefaultBlock();

      for (int x = 0; x < 16; x++) {
         for (int z = 0; z < 16; z++) {
            int realx = chunkpos.x() * 16 + x;
            int realz = chunkpos.z() * 16 + z;
            int height = calculateWaveHeight(realx, realz);

            for (int y = minBuildHeight; y < height; y++) {
               BlockState b = y < minBuildHeight + 2 ? bedrock : defaultBlock;
               chunkAccess.setBlockState(mpos.set(x, y, z), b);
               hmOcean.update(x, y, z, b);
               hmWorld.update(x, y, z, b);
            }
         }
      }

      return CompletableFuture.completedFuture(chunkAccess);
   }

   public static int calculateWaveHeight(int realx, int realz) {
      return (int)(80.0 + Math.sin(realx / 20.0F) * 10.0 + Math.cos(realz / 20.0F) * 10.0);
   }

   @NotNull
   public static NoiseColumn getBaseColumn(int pX, int pZ, LevelHeightAccessor level, RFToolsChunkGenerator generator) {
      BlockState[] states = new BlockState[calculateWaveHeight(pX, pZ) - level.getMinY()];
      Arrays.fill(states, generator.getDefaultBlock());
      states[0] = Blocks.BEDROCK.defaultBlockState();
      states[1] = Blocks.BEDROCK.defaultBlockState();
      return new NoiseColumn(level.getMinY(), states);
   }
}
