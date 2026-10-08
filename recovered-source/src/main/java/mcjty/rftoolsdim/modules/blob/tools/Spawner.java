package mcjty.rftoolsdim.modules.blob.tools;

import java.util.Random;
import javax.annotation.Nullable;
import mcjty.rftoolsdim.dimension.data.DimensionData;
import mcjty.rftoolsdim.dimension.descriptor.CompiledDescriptor;
import mcjty.rftoolsdim.modules.blob.BlobModule;
import mcjty.rftoolsdim.modules.blob.entities.DimensionalBlobEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.neoforged.neoforge.event.EventHooks;

public class Spawner {
   public static void spawnOne(ServerLevel world, Player player, CompiledDescriptor compiledDescriptor, DimensionData data, Random random) {
      double distanceX = random.nextDouble() * 100.0 - 50.0;

      double distanceZ;
      for (distanceZ = random.nextDouble() * 100.0 - 50.0; distanceX < 22.0 && distanceZ < 22.0; distanceZ = random.nextDouble() * 100.0 - 50.0) {
         distanceX = random.nextDouble() * 100.0 - 50.0;
      }

      int x = (int)(player.getX() + distanceX);
      int z = (int)(player.getZ() + distanceZ);
      EntityType<DimensionalBlobEntity> type = randomBlob(compiledDescriptor, data, random);
      BlockPos pos = getValidSpawnablePosition(random, world, x, z);
      if (pos != null) {
         boolean nocollisions = world.noCollision(type.getSpawnAABB(x, pos.getY(), z));
         boolean canSpawn = true;
         if (nocollisions && canSpawn) {
            DimensionalBlobEntity entity = (DimensionalBlobEntity)type.create(world, EntitySpawnReason.NATURAL);
            entity.snapTo(x, pos.getY(), z);
            entity.setYRot(random.nextFloat() * 360.0F);
            entity.setXRot(0.0F);
            EventHooks.finalizeMobSpawn(entity, world, world.getCurrentDifficultyAt(entity.blockPosition()), EntitySpawnReason.NATURAL, null);
            if (!entity.isSpawnCancelled()) {
               if (entity.checkSpawnRules(world, EntitySpawnReason.NATURAL) && entity.checkSpawnObstruction(world)) {
                  entity.finalizeSpawn(world, world.getCurrentDifficultyAt(entity.blockPosition()), EntitySpawnReason.NATURAL, null);
                  world.addFreshEntityWithPassengers(entity);
               }
            }
         }
      }
   }

   private static EntityType<DimensionalBlobEntity> randomBlob(CompiledDescriptor compiledDescriptor, DimensionData data, Random random) {
      float perTick = compiledDescriptor != null ? compiledDescriptor.getActualPowerCost() : 0.0F;
      perTick = Math.min(perTick, 50000.0F);
      float rareChance = 0.1F + perTick / 150000.0F;
      if (random.nextFloat() < rareChance) {
         return random.nextFloat() < rareChance ? BlobModule.DIMENSIONAL_BLOB_LEGENDARY.get() : BlobModule.DIMENSIONAL_BLOB_RARE.get();
      } else {
         return BlobModule.DIMENSIONAL_BLOB_COMMON.get();
      }
   }

   @Nullable
   private static BlockPos getValidSpawnablePosition(Random random, LevelReader worldIn, int x, int z) {
      int height = worldIn.getHeight(Types.WORLD_SURFACE, x, z);
      if (height <= 3) {
         return null;
      } else {
         height = random.nextInt(height - 3) + 3;
         BlockPos blockPos = new BlockPos(x, height - 1, z);

         while (!isValidSpawnPos(worldIn, blockPos)) {
            blockPos = blockPos.below();
            if (blockPos.getY() <= worldIn.getMinY() + 1) {
               return null;
            }
         }

         return blockPos;
      }
   }

   private static boolean isValidSpawnPos(LevelReader world, BlockPos pos) {
      return !world.getBlockState(pos).isPathfindable(PathComputationType.LAND) ? false : world.getBlockState(pos.below()).canOcclude();
   }
}
