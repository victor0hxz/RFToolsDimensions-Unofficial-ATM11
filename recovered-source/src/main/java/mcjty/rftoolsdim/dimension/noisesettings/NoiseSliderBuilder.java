package mcjty.rftoolsdim.dimension.noisesettings;

public class NoiseSliderBuilder {
   private double top = 1.0;
   private int size = 1;
   private int offset = 0;

   public NoiseSliderBuilder top(double top) {
      this.top = top;
      return this;
   }

   public NoiseSliderBuilder size(int size) {
      this.size = size;
      return this;
   }

   public NoiseSliderBuilder offset(int offset) {
      this.offset = offset;
      return this;
   }

   public static NoiseSliderBuilder create() {
      return new NoiseSliderBuilder();
   }
}
