package mcjty.rftoolsdim.dimension.terraintypes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import mcjty.rftoolsdim.compat.LostCityCompat;
import mcjty.rftoolsdim.dimension.data.DimensionSettings;
import mcjty.rftoolsdim.dimension.noisesettings.NoiseSamplingSettingsBuilder;
import mcjty.rftoolsdim.dimension.noisesettings.NoiseSettingsBuilder;
import mcjty.rftoolsdim.dimension.noisesettings.NoiseSliderBuilder;
import mcjty.rftoolsdim.dimension.terraintypes.generators.FlatGenerator;
import mcjty.rftoolsdim.dimension.terraintypes.generators.GridGenerator;
import mcjty.rftoolsdim.dimension.terraintypes.generators.MazeGenerator;
import mcjty.rftoolsdim.dimension.terraintypes.generators.PlatformsGenerator;
import mcjty.rftoolsdim.dimension.terraintypes.generators.RavineGenerator;
import mcjty.rftoolsdim.dimension.terraintypes.generators.SpikesGenerator;
import mcjty.rftoolsdim.dimension.terraintypes.generators.WavesGenerator;
import mcjty.rftoolsdim.tools.PerlinNoiseGenerator14;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;

public class RFToolsChunkGenerator extends NoiseBasedChunkGenerator {
   public static final MapCodec<RFToolsChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(
      instance -> instance.group(
            Codec.list(StructureSet.CODEC).fieldOf("structures").forGetter(ins -> ins.overrideStructures),
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(ins -> ins.biomeSource),
            Codec.LONG.fieldOf("seed").stable().forGetter(RFToolsChunkGenerator::getSeed),
            NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(NoiseBasedChunkGenerator::generatorSettings),
            DimensionSettings.SETTINGS_CODEC.fieldOf("dimsettings").forGetter(RFToolsChunkGenerator::getDimensionSettings)
         )
         .apply(instance, instance.stable(RFToolsChunkGenerator::new))
   );
   private final DimensionSettings dimensionSettings;
   private final List<Holder<StructureSet>> overrideStructures;
   private final long seed;
   private PerlinNoiseGenerator14 perlinNoise = null;

   public RFToolsChunkGenerator(
      List<Holder<StructureSet>> overrideStructures,
      BiomeSource biomeSource,
      long seed,
      Holder<NoiseGeneratorSettings> settingsSupplier,
      DimensionSettings dimensionSettings
   ) {
      super(biomeSource, settingsSupplier);
      this.dimensionSettings = dimensionSettings;
      this.overrideStructures = overrideStructures;
      this.seed = seed;
      this.featuresPerStep = Lazy.of(
         () -> FeatureSorter.buildFeaturesPerStep(
            List.copyOf(biomeSource.possibleBiomes()),
            biome -> {
               List<HolderSet<PlacedFeature>> features = ((Biome)biome.value()).getGenerationSettings().features();
               List<HolderSet<PlacedFeature>> newFeatures = new ArrayList<>();

               for (HolderSet<PlacedFeature> set : features) {
                  List<Holder<PlacedFeature>> list = set.stream()
                     .sorted(Comparator.comparing(placedFeatureHolder -> placedFeatureHolder.unwrapKey().map(ResourceKey::toString).orElse("")))
                     .toList();
                  newFeatures.add(HolderSet.direct(list));
               }

               return newFeatures;
            },
            true
         )
      );
   }

   public void changeSettings(
      Consumer<NoiseSettingsBuilder> noiseBuilderConsumer,
      Consumer<NoiseSamplingSettingsBuilder> samplingSettingsBuilderConsumer,
      Consumer<NoiseSliderBuilder> topSliderBuilderConsumer,
      Consumer<NoiseSliderBuilder> bottomSliderBuilderConsumer
   ) {
      NoiseGeneratorSettings settings = (NoiseGeneratorSettings)this.generatorSettings().value();
   }

   public NoiseGeneratorSettings getNoiseGeneratorSettings() {
      return (NoiseGeneratorSettings)this.generatorSettings().value();
   }

   public long getSeed() {
      return this.seed;
   }

   public BlockState getDefaultBlock() {
      return ((NoiseGeneratorSettings)this.generatorSettings().value()).defaultBlock();
   }

   public PerlinNoiseGenerator14 getPerlinNoise() {
      if (this.perlinNoise == null) {
         this.perlinNoise = new PerlinNoiseGenerator14(this.seed, 4);
      }

      return this.perlinNoise;
   }

   private void checkForCities(WorldGenRegion region, TerrainType terrainType) {
      if (LostCityCompat.hasLostCities() && this.dimensionSettings.getCompiledDescriptor().getAttributeTypes().contains(AttributeType.CITIES)) {
         LostCityCompat.registerDimension(region.getLevel(), region.getLevel().dimension(), LostCityCompat.getProfile(terrainType));
      }
   }

   public void buildSurface(WorldGenRegion level, StructureManager structureFeatureManager, RandomState randomState, ChunkAccess chunkAccess) {
      TerrainType terrainType = this.dimensionSettings.getCompiledDescriptor().getTerrainType();
      this.checkForCities(level, terrainType);
      if (terrainType != TerrainType.VOID && terrainType != TerrainType.FLAT) {
         super.buildSurface(level, structureFeatureManager, randomState, chunkAccess);
      }
   }

   @Nonnull
   public CompletableFuture<ChunkAccess> fillFromNoise(
      Blender blender, RandomState randomState, StructureManager structureFeatureManager, ChunkAccess chunkAccess
   ) {
      TerrainType terrainType = this.dimensionSettings.getCompiledDescriptor().getTerrainType();

      return switch (terrainType) {
         case FLAT -> FlatGenerator.fillFromNoise(chunkAccess, this);
         case VOID -> CompletableFuture.completedFuture(chunkAccess);
         case WAVES -> WavesGenerator.fillFromNoise(chunkAccess, this);
         case SPIKES -> SpikesGenerator.fillFromNoise(chunkAccess, this);
         case GRID -> GridGenerator.fillFromNoise(chunkAccess, this);
         case PLATFORMS -> PlatformsGenerator.fillFromNoise(chunkAccess, this);
         case MAZE -> MazeGenerator.fillFromNoise(chunkAccess, this);
         case RAVINE -> RavineGenerator.fillFromNoise(chunkAccess, this);
         default -> super.fillFromNoise(blender, randomState, structureFeatureManager, chunkAccess);
      };
   }

   public int getBaseHeight(int pX, int pZ, Types type, LevelHeightAccessor level, RandomState randomState) {
      TerrainType terrainType = this.dimensionSettings.getCompiledDescriptor().getTerrainType();

      return switch (terrainType) {
         case FLAT -> 119;
         case VOID -> level.getMinY();
         case WAVES -> WavesGenerator.calculateWaveHeight(pX, pZ);
         case SPIKES -> SpikesGenerator.calculateSpikeHeight(pX, pZ, this.seed);
         case GRID -> GridGenerator.getBaseHeight(pX, pZ, level);
         case PLATFORMS -> PlatformsGenerator.getBaseHeight(pX, pZ, level, this);
         case MAZE -> MazeGenerator.getBaseHeight(pX, pZ, level);
         case RAVINE -> RavineGenerator.getBaseHeight(pX, pZ, this);
         default -> super.getBaseHeight(pX, pZ, type, level, randomState);
      };
   }

   @NotNull
   public NoiseColumn getBaseColumn(int pX, int pZ, LevelHeightAccessor level, RandomState randomState) {
      TerrainType terrainType = this.dimensionSettings.getCompiledDescriptor().getTerrainType();

      return switch (terrainType) {
         case FLAT -> FlatGenerator.getBaseColumn(pX, pZ, level, this);
         case VOID -> new NoiseColumn(level.getMinY(), new BlockState[0]);
         case WAVES -> WavesGenerator.getBaseColumn(pX, pZ, level, this);
         case SPIKES -> SpikesGenerator.getBaseColumn(pX, pZ, level, this);
         case GRID -> GridGenerator.getBaseColumn(pX, pZ, level, this);
         case PLATFORMS -> PlatformsGenerator.getBaseColumn(pX, pZ, level, this);
         case MAZE -> MazeGenerator.getBaseColumn(pX, pZ, level, this);
         case RAVINE -> RavineGenerator.getBaseColumn(pX, pZ, level, this);
         default -> super.getBaseColumn(pX, pZ, level, randomState);
      };
   }

   public DimensionSettings getDimensionSettings() {
      return this.dimensionSettings;
   }

   @Nonnull
   protected MapCodec<? extends ChunkGenerator> codec() {
      return CODEC;
   }
}
