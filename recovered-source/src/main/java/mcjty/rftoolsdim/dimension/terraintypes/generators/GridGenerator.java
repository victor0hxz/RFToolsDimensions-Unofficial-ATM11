package mcjty.rftoolsdim.dimension.terraintypes.generators;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import mcjty.rftoolsdim.dimension.terraintypes.RFToolsChunkGenerator;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import org.jetbrains.annotations.NotNull;

public class GridGenerator {
   public static CompletableFuture<ChunkAccess> fillFromNoise(ChunkAccess chunkAccess, RFToolsChunkGenerator generator) {
      ChunkPos chunkpos = chunkAccess.getPos();
      MutableBlockPos mpos = new MutableBlockPos();
      Heightmap hmOcean = chunkAccess.getOrCreateHeightmapUnprimed(Types.OCEAN_FLOOR_WG);
      Heightmap hmWorld = chunkAccess.getOrCreateHeightmapUnprimed(Types.WORLD_SURFACE_WG);
      BlockState defaultBlock = generator.getDefaultBlock();

      for (int x = 0; x < 16; x++) {
         for (int z = 0; z < 16; z++) {
            int realx = chunkpos.x() * 16 + x;
            int realz = chunkpos.z() * 16 + z;
            if (realx % 32 == 0 && realz % 32 == 0) {
               for (int y = chunkAccess.getMinY(); y < chunkAccess.getMinY() + chunkAccess.getHeight(); y++) {
                  chunkAccess.setBlockState(mpos.set(x, y, z), defaultBlock);
               }

               hmOcean.update(x, chunkAccess.getMinY() + chunkAccess.getHeight() - 1, z, defaultBlock);
               hmWorld.update(x, chunkAccess.getMinY() + chunkAccess.getHeight() - 1, z, defaultBlock);
            } else if (realx % 32 == 0 || realz % 32 == 0) {
               for (int y = chunkAccess.getMinY(); y < chunkAccess.getMinY() + chunkAccess.getHeight(); y += 32) {
                  chunkAccess.setBlockState(mpos.set(x, y, z), defaultBlock);
                  hmOcean.update(x, y, z, defaultBlock);
                  hmWorld.update(x, y, z, defaultBlock);
               }
            }
         }
      }

      return CompletableFuture.completedFuture(chunkAccess);
   }

   public static int getBaseHeight(int pX, int pZ, LevelHeightAccessor level) {
      if (pX % 32 == 0 && pZ % 32 == 0) {
         return level.getMinY() + level.getHeight() - 1;
      } else {
         return pX % 32 != 0 && pZ % 32 != 0 ? 0 : level.getMinY() + level.getHeight() - 32;
      }
   }

   @NotNull
   public static NoiseColumn getBaseColumn(int pX, int pZ, LevelHeightAccessor level, RFToolsChunkGenerator generator) {
      BlockState[] states = new BlockState[getBaseHeight(pX, pZ, level)];
      Arrays.fill(states, generator.getDefaultBlock());
      return new NoiseColumn(level.getMinY(), states);
   }
}
