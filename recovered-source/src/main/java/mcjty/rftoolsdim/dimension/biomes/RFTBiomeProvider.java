package mcjty.rftoolsdim.dimension.biomes;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import mcjty.rftoolsdim.dimension.data.DimensionSettings;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.Climate.Sampler;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

public class RFTBiomeProvider extends BiomeSource {
   public static final MapCodec<RFTBiomeProvider> CODEC = RecordCodecBuilder.mapCodec(
      instance -> instance.group(
            RegistryOps.retrieveRegistryLookup(Registries.WORLD_PRESET).forGetter(RFTBiomeProvider::getWorldPresetLookup),
            RegistryOps.retrieveRegistryLookup(Registries.BIOME).forGetter(RFTBiomeProvider::getBiomeLookup),
            DimensionSettings.SETTINGS_CODEC.fieldOf("settings").forGetter(RFTBiomeProvider::getSettings)
         )
         .apply(instance, RFTBiomeProvider::new)
   );
   private final List<Holder<Biome>> biomes;
   private final Set<TagKey<Biome>> biomeCategories;
   private final Map<Identifier, Holder<Biome>> biomeMapping = new HashMap<>();
   private final RegistryLookup<WorldPreset> worldPresetLookup;
   private final RegistryLookup<Biome> biomeLookup;
   private final DimensionSettings settings;
   private final MultiNoiseBiomeSource multiNoiseBiomeSource;
   private final boolean defaultBiomes;
   private Holder<Biome> biome1 = null;
   private Holder<Biome> biome2 = null;

   public RFTBiomeProvider(RegistryLookup<WorldPreset> worldPresetLookup, RegistryLookup<Biome> biomeLookup, DimensionSettings settings) {
      this.settings = settings;
      this.biomeLookup = biomeLookup;
      this.worldPresetLookup = worldPresetLookup;
      Optional<Reference<WorldPreset>> worldPreset = worldPresetLookup.get(WorldPresets.NORMAL);
      this.multiNoiseBiomeSource = (MultiNoiseBiomeSource)((LevelStem)((WorldPreset)worldPreset.get().value()).overworld().get()).generator().getBiomeSource();
      this.biomes = this.getBiomes(biomeLookup, settings);
      this.biomeCategories = this.getBiomeCategories(settings);
      this.defaultBiomes = this.biomes.isEmpty() && this.biomeCategories.isEmpty();
      biomeLookup.listElements().forEach(this::getMappedBiome);
      this.getBiome1And2();
   }

   public RegistryLookup<WorldPreset> getWorldPresetLookup() {
      return this.worldPresetLookup;
   }

   public DimensionSettings getSettings() {
      return this.settings;
   }

   private static List<Holder<Biome>> getDefaultBiomes(RegistryLookup<Biome> biomeLookup, DimensionSettings settings) {
      List<Identifier> biomes = settings.getCompiledDescriptor().getBiomes();
      return biomes.isEmpty()
         ? biomeLookup.listElements().collect(Collectors.toList())
         : biomes.stream().map(rl -> biomeLookup.get(ResourceKey.create(Registries.BIOME, rl))).map(Optional::get).collect(Collectors.toList());
   }

   protected Stream<Holder<Biome>> collectPossibleBiomes() {
      return getDefaultBiomes(this.biomeLookup, this.settings).stream();
   }

   private boolean isCategoryMatching(Holder<Biome> biome) {
      return this.biomeCategories.isEmpty()
         ? true
         : this.biomeLookup.getOrThrow((ResourceKey)biome.unwrapKey().get()).tags().filter(this.biomeCategories::contains).findAny().isPresent();
   }

   private Holder<Biome> getMappedBiome(Holder<Biome> biome) {
      return this.defaultBiomes ? biome : this.biomeMapping.computeIfAbsent(((ResourceKey)biome.unwrapKey().get()).identifier(), resourceLocation -> {
         List<Holder<Biome>> biomes = this.getBiomes(this.biomeLookup, this.settings);
         float[] minDist = new float[]{1.0E9F};
         Holder<?>[] desired = new Holder[]{biome};
         if (biomes.isEmpty()) {
            if (!this.isCategoryMatching((Holder<Biome>)desired[0])) {
               this.biomeLookup.listElements().forEach(bx -> {
                  if (this.isCategoryMatching(bx)) {
                     float distx = this.distance(bx, biome);
                     if (distx < minDist[0]) {
                        desired[0] = bx;
                        minDist[0] = distx;
                     }
                  }
               });
            }
         } else {
            for (Holder<Biome> b : biomes) {
               if (this.biomeCategories.isEmpty() || this.isCategoryMatching(b)) {
                  float dist = this.distance(b, biome);
                  if (dist < minDist[0]) {
                     desired[0] = b;
                     minDist[0] = dist;
                  }
               }
            }
         }

         return (Holder<Biome>)desired[0];
      });
   }

   private float distance(Holder<Biome> biome1, Holder<Biome> biome2) {
      if (Objects.equals(biome1, biome2)) {
         return -1.0F;
      } else if (Objects.equals(biome1.value(), biome2.value())) {
         return -1.0F;
      } else {
         Set<TagKey<Biome>> tags1 = biome1.tags().collect(Collectors.toSet());
         Set<TagKey<Biome>> tags2 = biome2.tags().collect(Collectors.toSet());
         tags1.removeAll(tags2);
         tags1 = biome1.tags().collect(Collectors.toSet());
         tags2.removeAll(tags1);
         float d1 = Math.max(tags1.size(), tags2.size());
         float d2 = Math.abs(((Biome)biome1.value()).getBaseTemperature() - ((Biome)biome2.value()).getBaseTemperature());
         return d1 + d2 * d2;
      }
   }

   private List<Holder<Biome>> getBiomes(RegistryLookup<Biome> holderLookup, DimensionSettings settings) {
      List<Identifier> biomes = settings.getCompiledDescriptor().getBiomes();
      return biomes.stream().map(rl -> this.biomeLookup.get(ResourceKey.create(Registries.BIOME, rl))).map(Optional::get).collect(Collectors.toList());
   }

   private Set<TagKey<Biome>> getBiomeCategories(DimensionSettings settings) {
      return settings.getCompiledDescriptor().getBiomeCategories();
   }

   public RegistryLookup<Biome> getBiomeLookup() {
      return this.biomeLookup;
   }

   @Nonnull
   protected MapCodec<? extends BiomeSource> codec() {
      return CODEC;
   }

   private void getBiome1And2() {
      if (this.biome1 == null) {
         if (this.biomes.isEmpty()) {
            List<Holder<Biome>> list = this.biomeLookup.listElements().filter(this::isCategoryMatching).collect(Collectors.toList());
            if (list.isEmpty()) {
               this.biome1 = this.biome2 = (Holder<Biome>)this.biomeLookup.get(Biomes.PLAINS).get();
            } else {
               this.biome1 = list.get(0);
               if (list.size() > 1) {
                  this.biome2 = list.get(1);
               } else {
                  this.biome2 = this.biome1;
               }
            }
         } else {
            this.biome1 = this.biomes.get(0);
            if (this.biomes.size() > 1) {
               this.biome2 = this.biomes.get(1);
            } else {
               this.biome2 = this.biome1;
            }
         }

         this.biome1 = this.getMappedBiome(this.biome1);
         if (this.biome1 == null) {
            this.biome1 = (Holder<Biome>)this.biomeLookup.get(Biomes.PLAINS).get();
         }

         this.biome2 = this.getMappedBiome(this.biome2);
         if (this.biome2 == null) {
            this.biome2 = this.biome1;
         }
      }
   }

   @Nonnull
   public Holder<Biome> getNoiseBiome(int x, int y, int z, Sampler climate) {
      return switch (this.settings.getCompiledDescriptor().getBiomeControllerType()) {
         case CHECKER -> this.getCheckerBiome(x, z);
         case SINGLE -> this.getSingleBiome();
         default -> this.getDefaultBiome(x, y, z, climate);
      };
   }

   private Holder<Biome> getDefaultBiome(int x, int y, int z, Sampler climate) {
      return this.defaultBiomes
         ? this.multiNoiseBiomeSource.getNoiseBiome(x, y, z, climate)
         : this.getMappedBiome(this.multiNoiseBiomeSource.getNoiseBiome(x, y, z, climate));
   }

   private Holder<Biome> getSingleBiome() {
      return this.biome1;
   }

   private Holder<Biome> getCheckerBiome(int x, int z) {
      return ((x >> 3) + (z >> 3)) % 2 == 0 ? this.biome1 : this.biome2;
   }
}
