package mcjty.rftoolsdim.dimension.noisesettings;

import net.minecraft.world.level.levelgen.NoiseSettings;

public class NoiseSettingsBuilder {
   int minY = 0;
   int height = 256;
   private int noiseSizeHorizontal = 2;
   private int noiseSizeVertical = 1;
   private boolean islandNoiseOverride = false;
   private boolean amplified = false;
   private boolean largeBiomes = false;

   public NoiseSettingsBuilder minY(int minY) {
      this.minY = minY;
      return this;
   }

   public NoiseSettingsBuilder height(int height) {
      this.height = height;
      return this;
   }

   public NoiseSettingsBuilder noiseSizeHorizontal(int noiseSizeHorizontal) {
      this.noiseSizeHorizontal = noiseSizeHorizontal;
      return this;
   }

   public NoiseSettingsBuilder noiseSizeVertical(int noiseSizeVertical) {
      this.noiseSizeVertical = noiseSizeVertical;
      return this;
   }

   public NoiseSettingsBuilder islandNoiseOverride(boolean islandNoiseOverride) {
      this.islandNoiseOverride = islandNoiseOverride;
      return this;
   }

   public NoiseSettingsBuilder amplified(boolean amplified) {
      this.amplified = amplified;
      return this;
   }

   public NoiseSettingsBuilder largeBiomes(boolean largeBiomes) {
      this.largeBiomes = largeBiomes;
      return this;
   }

   public NoiseSettingsBuilder samplingSettings(NoiseSamplingSettingsBuilder samplingSettings) {
      return this;
   }

   public NoiseSettingsBuilder topSlider(NoiseSliderBuilder topSlider) {
      return this;
   }

   public NoiseSettingsBuilder bottomSlider(NoiseSliderBuilder bottomSlider) {
      return this;
   }

   public static NoiseSettingsBuilder create(NoiseSettings settings) {
      return new NoiseSettingsBuilder()
         .minY(settings.minY())
         .height(settings.height())
         .noiseSizeHorizontal(settings.noiseSizeHorizontal())
         .noiseSizeVertical(settings.noiseSizeVertical());
   }
}
