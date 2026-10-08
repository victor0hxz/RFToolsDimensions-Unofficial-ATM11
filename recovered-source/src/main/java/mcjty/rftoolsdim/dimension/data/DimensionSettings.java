package mcjty.rftoolsdim.dimension.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.rftoolsdim.RFToolsDim;
import mcjty.rftoolsdim.dimension.descriptor.CompiledDescriptor;
import mcjty.rftoolsdim.dimension.descriptor.DescriptorError;
import mcjty.rftoolsdim.dimension.descriptor.DimensionDescriptor;

public class DimensionSettings {
   public static final Codec<DimensionSettings> SETTINGS_CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            Codec.LONG.fieldOf("seed").forGetter(DimensionSettings::getSeed),
            Codec.STRING.fieldOf("dimlets").forGetter(DimensionSettings::getDimlets),
            Codec.STRING.fieldOf("randomized").forGetter(DimensionSettings::getRandomized)
         )
         .apply(instance, DimensionSettings::new)
   );
   private final long seed;
   private final String dimlets;
   private final String randomized;
   private CompiledDescriptor compiledDescriptor;

   public DimensionSettings(long seed, String dimlets, String randomized) {
      this.seed = seed;
      this.dimlets = dimlets;
      this.randomized = randomized;
   }

   public long getSeed() {
      return this.seed;
   }

   public String getDimlets() {
      return this.dimlets;
   }

   public String getRandomized() {
      return this.randomized;
   }

   public CompiledDescriptor getCompiledDescriptor() {
      if (this.compiledDescriptor == null) {
         DimensionDescriptor descriptor = new DimensionDescriptor();
         descriptor.read(this.getDimlets());
         DimensionDescriptor randomizedDescriptor = new DimensionDescriptor();
         randomizedDescriptor.read(this.getRandomized());
         this.compiledDescriptor = new CompiledDescriptor();

         try {
            this.compiledDescriptor.compile(descriptor, randomizedDescriptor);
         } catch (DescriptorError var4) {
            RFToolsDim.setup.getLogger().error("Error compiling dimension descriptor: " + var4.getMessage());
         }

         this.compiledDescriptor.complete();
      }

      return this.compiledDescriptor;
   }
}
