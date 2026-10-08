package mcjty.rftoolsdim.dimension.noisesettings;

import java.util.Collections;
import java.util.List;
import mcjty.rftoolsdim.dimension.data.DimensionSettings;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.SurfaceRules.RuleSource;

public class NoiseGeneratorSettingsBuilder {
   private BlockState baseBlock = Blocks.STONE.defaultBlockState();
   private BlockState liquidBlock = Blocks.WATER.defaultBlockState();
   private int seaLevel = -64;
   private boolean disableMobGeneration = false;
   private boolean aquifersEnabled = false;
   private boolean noiseCavesEnabled = false;
   private boolean oreVeinsEnabled = false;
   private boolean noodleCavesEnabled = false;
   private boolean legacyRandomSource = true;
   private NoiseSettings noiseSettings;
   private NoiseRouter router;
   private RuleSource ruleSource;

   public NoiseGeneratorSettingsBuilder baseBlock(BlockState baseBlock) {
      this.baseBlock = baseBlock;
      return this;
   }

   public NoiseGeneratorSettingsBuilder liquidBlock(BlockState liquidBlock) {
      this.liquidBlock = liquidBlock;
      return this;
   }

   public NoiseGeneratorSettingsBuilder seaLevel(int seaLevel) {
      this.seaLevel = seaLevel;
      return this;
   }

   public NoiseGeneratorSettingsBuilder disableMobGeneration(boolean disableMobGeneration) {
      this.disableMobGeneration = disableMobGeneration;
      return this;
   }

   public NoiseGeneratorSettingsBuilder aquifersEnabled(boolean aquifersEnabled) {
      this.aquifersEnabled = aquifersEnabled;
      return this;
   }

   public NoiseGeneratorSettingsBuilder noiseCavesEnabled(boolean noiseCavesEnabled) {
      this.noiseCavesEnabled = noiseCavesEnabled;
      return this;
   }

   public NoiseGeneratorSettingsBuilder oreVeinsEnabled(boolean oreVeinsEnabled) {
      this.oreVeinsEnabled = oreVeinsEnabled;
      return this;
   }

   public NoiseGeneratorSettingsBuilder noodleCavesEnabled(boolean noodleCavesEnabled) {
      this.noodleCavesEnabled = noodleCavesEnabled;
      return this;
   }

   public NoiseGeneratorSettingsBuilder legacyRandomSource(boolean legacyRandomSource) {
      this.legacyRandomSource = legacyRandomSource;
      return this;
   }

   public NoiseGeneratorSettingsBuilder noiseSettings(NoiseSettingsBuilder noiseSettings) {
      return this;
   }

   public NoiseGeneratorSettingsBuilder noiseSettings(NoiseSettings noiseSettings) {
      this.noiseSettings = noiseSettings;
      return this;
   }

   public NoiseGeneratorSettingsBuilder ruleSource(SurfaceRuleDataBuilder ruleSource) {
      this.ruleSource = ruleSource.build();
      return this;
   }

   public NoiseGeneratorSettingsBuilder ruleSource(RuleSource ruleSource) {
      this.ruleSource = ruleSource;
      return this;
   }

   public NoiseGeneratorSettingsBuilder router(NoiseRouter router) {
      this.router = router;
      return this;
   }

   public static NoiseGeneratorSettingsBuilder create() {
      return new NoiseGeneratorSettingsBuilder();
   }

   public static NoiseGeneratorSettingsBuilder create(NoiseGeneratorSettings settings) {
      return new NoiseGeneratorSettingsBuilder()
         .noiseSettings(settings.noiseSettings())
         .ruleSource(settings.surfaceRule())
         .aquifersEnabled(settings.isAquifersEnabled())
         .disableMobGeneration(settings.disableMobGeneration())
         .legacyRandomSource(settings.useLegacyRandomSource())
         .liquidBlock(settings.defaultFluid())
         .baseBlock(settings.defaultBlock())
         .router(settings.noiseRouter())
         .seaLevel(settings.seaLevel());
   }

   public NoiseGeneratorSettings build(DimensionSettings settings) {
      List<Identifier> structures = settings.getCompiledDescriptor().getStructures();
      return new NoiseGeneratorSettings(
         this.noiseSettings,
         this.baseBlock,
         this.liquidBlock,
         this.router,
         this.ruleSource,
         Collections.emptyList(),
         this.seaLevel,
         this.disableMobGeneration,
         this.aquifersEnabled,
         this.oreVeinsEnabled,
         this.legacyRandomSource
      );
   }

   public NoiseGeneratorSettings build() {
      return new NoiseGeneratorSettings(
         this.noiseSettings,
         this.baseBlock,
         this.liquidBlock,
         this.router,
         this.ruleSource,
         Collections.emptyList(),
         this.seaLevel,
         this.disableMobGeneration,
         this.aquifersEnabled,
         this.oreVeinsEnabled,
         this.legacyRandomSource
      );
   }
}
