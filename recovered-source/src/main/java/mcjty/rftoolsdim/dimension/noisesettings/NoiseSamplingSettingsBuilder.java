package mcjty.rftoolsdim.dimension.noisesettings;

public class NoiseSamplingSettingsBuilder {
   private double xzScale = 1.0;
   private double yScale = 1.0;
   private double xzFactor = 80.0;
   private double yFactor = 80.0;

   public NoiseSamplingSettingsBuilder xzScale(double xzScale) {
      this.xzScale = xzScale;
      return this;
   }

   public NoiseSamplingSettingsBuilder yScale(double yScale) {
      this.yScale = yScale;
      return this;
   }

   public NoiseSamplingSettingsBuilder xzFactor(double xzFactor) {
      this.xzFactor = xzFactor;
      return this;
   }

   public NoiseSamplingSettingsBuilder yFactor(double yFactor) {
      this.yFactor = yFactor;
      return this;
   }

   public static NoiseSamplingSettingsBuilder create() {
      return new NoiseSamplingSettingsBuilder();
   }
}
