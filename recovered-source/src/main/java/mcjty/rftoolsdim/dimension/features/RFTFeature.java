package mcjty.rftoolsdim.dimension.features;

import com.mojang.serialization.Codec;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import mcjty.rftoolsdim.dimension.DimensionConfig;
import mcjty.rftoolsdim.dimension.data.DimensionCreator;
import mcjty.rftoolsdim.dimension.descriptor.CompiledDescriptor;
import mcjty.rftoolsdim.dimension.descriptor.CompiledFeature;
import mcjty.rftoolsdim.dimension.features.buildings.BuildingTemplate;
import mcjty.rftoolsdim.dimension.features.buildings.DimletHut;
import mcjty.rftoolsdim.dimension.features.buildings.SpawnHut;
import mcjty.rftoolsdim.dimension.features.buildings.SpawnPlatform;
import mcjty.rftoolsdim.dimension.terraintypes.RFToolsChunkGenerator;
import mcjty.rftoolsdim.dimension.terraintypes.TerrainType;
import mcjty.rftoolsdim.tools.Primes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class RFTFeature extends Feature<NoneFeatureConfiguration> {
   public static final Identifier RFTFEATURE_ID = Identifier.fromNamespaceAndPath("rftoolsdim", "rftfeature");

   public RFTFeature(Codec<NoneFeatureConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
      ChunkGenerator generator = context.chunkGenerator();
      BlockPos pos = context.origin();
      RandomSource rand = context.random();
      WorldGenLevel reader = context.level();
      if (!(generator instanceof RFToolsChunkGenerator chunkGenerator)) {
         return false;
      } else {
         CompiledDescriptor compiledDescriptor = chunkGenerator.getDimensionSettings().getCompiledDescriptor();
         Set<CompiledFeature> features = compiledDescriptor.getFeatures();
         if (features.stream().anyMatch(f -> f.getFeatureType().equals(FeatureType.NONE))) {
            return false;
         } else {
            boolean generatedSomething = false;
            int primeIndex = 0;

            for (CompiledFeature feature : features) {
               if (feature.getFeatureType()
                  .getFeature()
                  .generate(reader, generator, rand, pos, feature.getBlocks(), feature.getFluids(), Primes.PRIMES[primeIndex % Primes.PRIMES.length])) {
                  generatedSomething = true;
               }

               primeIndex++;
            }

            ChunkPos cp = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
            TerrainType terrainType = compiledDescriptor.getTerrainType();
            List<BlockState> baseBlocks = Collections.singletonList(compiledDescriptor.getBaseBlock());
            if (cp.x() == 0 && cp.z() == 0) {
               int floorHeight = this.getFloorHeight(terrainType, reader, cp);
               ChunkAccess chunk = reader.getChunk(cp.x(), cp.z());
               DimensionCreator.get().registerPlatformHeight(reader.getLevel().dimension().identifier(), floorHeight);
               if (floorHeight >= generator.getSeaLevel() && !this.needsHut(chunk, floorHeight)) {
                  ((SpawnPlatform)SpawnPlatform.SPAWN_PLATFORM.get())
                     .generate(terrainType, reader, new BlockPos(3, floorHeight, 3), baseBlocks, BuildingTemplate.GenerateFlag.PLAIN);
               } else {
                  ((SpawnHut)SpawnHut.SPAWN_HUT.get())
                     .generate(terrainType, reader, new BlockPos(3, floorHeight, 3), baseBlocks, BuildingTemplate.GenerateFlag.PLAIN);
               }

               generatedSomething = true;
            } else if (rand.nextFloat() < (Double)DimensionConfig.DIMLET_HUT_CHANCE.get()) {
               ((DimletHut)DimletHut.DIMLET_HUT.get())
                  .generate(
                     terrainType,
                     reader,
                     new BlockPos(cp.getMinBlockX() + 4, this.getFloorHeight(terrainType, reader, cp), cp.getMinBlockZ() + 4),
                     baseBlocks,
                     BuildingTemplate.GenerateFlag.FILLDOWN_IFNOTVOID
                  );
               generatedSomething = true;
            }

            return generatedSomething;
         }
      }
   }

   private boolean needsHut(ChunkAccess chunk, int height) {
      MutableBlockPos mpos = new MutableBlockPos();

      for (int y = 1; y < 2; y++) {
         for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
               if (!chunk.getBlockState(mpos.set(x, height + y, z)).isAir()) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private int getFloorHeight(TerrainType type, WorldGenLevel reader, ChunkPos cp) {
      int height0 = this.getHeightAt(type, reader, cp, 8, 8);
      int height1 = this.getHeightAt(type, reader, cp, 4, 4);
      int height2 = this.getHeightAt(type, reader, cp, 12, 4);
      int height3 = this.getHeightAt(type, reader, cp, 4, 12);
      int height4 = this.getHeightAt(type, reader, cp, 12, 12);
      return (height0 + height1 + height2 + height3 + height4) / 5;
   }

   private int getHeightAt(TerrainType type, WorldGenLevel reader, ChunkPos cp, int dx, int dz) {
      int height;
      if (type == TerrainType.CAVERN) {
         height = 64;
         ChunkAccess chunk = reader.getChunk(cp.x(), cp.z());
         MutableBlockPos mpos = new MutableBlockPos(dx, 0, dz);

         while (height > reader.getMinY() && chunk.getBlockState(mpos.setY(height)).isAir()) {
            height--;
         }
      } else {
         height = reader.getHeight(Types.WORLD_SURFACE, cp.getMinBlockX() + dx, cp.getMinBlockZ() + dz);
      }

      if (height <= reader.getMinY() + 2 || height > reader.getMinY() + reader.getHeight() - 10) {
         height = 65;
      }

      return height;
   }
}
