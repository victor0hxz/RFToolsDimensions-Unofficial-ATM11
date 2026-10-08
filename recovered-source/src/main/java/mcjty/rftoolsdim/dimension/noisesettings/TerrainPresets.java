package mcjty.rftoolsdim.dimension.noisesettings;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

public class TerrainPresets {
   public static final ResourceKey<NoiseGeneratorSettings> RFTOOLSDIM_CHAOTIC = ResourceKey.create(
      Registries.NOISE_SETTINGS, Identifier.fromNamespaceAndPath("rftoolsdim", "rftoolsdim_chaotic")
   );
   public static final ResourceKey<NoiseGeneratorSettings> RFTOOLSDIM_ISLANDS = ResourceKey.create(
      Registries.NOISE_SETTINGS, Identifier.fromNamespaceAndPath("rftoolsdim", "rftoolsdim_islands")
   );
   public static final ResourceKey<NoiseGeneratorSettings> RFTOOLSDIM_CAVERN = ResourceKey.create(
      Registries.NOISE_SETTINGS, Identifier.fromNamespaceAndPath("rftoolsdim", "rftoolsdim_cavern")
   );
   public static final ResourceKey<NoiseGeneratorSettings> RFTOOLSDIM_FLAT = ResourceKey.create(
      Registries.NOISE_SETTINGS, Identifier.fromNamespaceAndPath("rftoolsdim", "rftoolsdim_flat")
   );
   public static final ResourceKey<NoiseGeneratorSettings> RFTOOLSDIM_OVERWORLD = ResourceKey.create(
      Registries.NOISE_SETTINGS, Identifier.fromNamespaceAndPath("rftoolsdim", "rftoolsdim_overworld")
   );
}
