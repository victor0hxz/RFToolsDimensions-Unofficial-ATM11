package mcjty.rftoolsdim.dimension.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsdim.RFToolsDim;
import mcjty.rftoolsdim.compat.LostCityCompat;
import mcjty.rftoolsdim.dimension.DimensionRegistry;
import mcjty.rftoolsdim.dimension.TimeType;
import mcjty.rftoolsdim.dimension.additional.SkyDimletType;
import mcjty.rftoolsdim.dimension.biomes.RFTBiomeProvider;
import mcjty.rftoolsdim.dimension.descriptor.CompiledDescriptor;
import mcjty.rftoolsdim.dimension.descriptor.DescriptorError;
import mcjty.rftoolsdim.dimension.descriptor.DimensionDescriptor;
import mcjty.rftoolsdim.dimension.noisesettings.NoiseGeneratorSettingsBuilder;
import mcjty.rftoolsdim.dimension.terraintypes.AttributeType;
import mcjty.rftoolsdim.dimension.terraintypes.RFToolsChunkGenerator;
import mcjty.rftoolsdim.dimension.terraintypes.TerrainType;
import mcjty.rftoolsdim.dimension.tools.DynamicDimensionManager;
import mcjty.rftoolsdim.tools.Primes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.SurfaceRules.BlockRuleSource;
import net.minecraft.world.level.levelgen.SurfaceRules.RuleSource;
import net.minecraft.world.level.levelgen.SurfaceRules.SequenceRuleSource;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

public class DimensionCreator {
   private final Map<Identifier, CompiledDescriptor> compiledDescriptorMap = new HashMap<>();
   private final Map<Identifier, Integer> platformHeightMap = new HashMap<>();
   private final Map<String, DimensionCreator.ReservedName> reservedDimensionNames = new HashMap<>();
   private static final DimensionCreator instance = new DimensionCreator();

   public static DimensionCreator get() {
      return instance;
   }

   public void clear() {
      this.platformHeightMap.clear();
      this.compiledDescriptorMap.clear();
   }

   public CompiledDescriptor getCompiledDescriptor(@Nullable Level world) {
      if (world == null) {
         return null;
      } else {
         ResourceKey<Level> type = world.dimension();
         Identifier id = type.identifier();
         return this.getCompiledDescriptor(world, id);
      }
   }

   public CompiledDescriptor getCompiledDescriptor(Level overworld, Identifier id) {
      if (!this.compiledDescriptorMap.containsKey(id)) {
         ServerLevel world = LevelTools.getLevel(overworld, id);
         if (world == null) {
            return null;
         }

         if (!(world.getChunkSource().getGenerator() instanceof RFToolsChunkGenerator rftoolsGenerator)) {
            RFToolsDim.setup.getLogger().error(id.toString() + " is not a dimension managed by us!");
            return null;
         }

         CompiledDescriptor compiledDescriptor = rftoolsGenerator.getDimensionSettings().getCompiledDescriptor();
         this.compiledDescriptorMap.put(id, compiledDescriptor);
      }

      return this.compiledDescriptorMap.get(id);
   }

   public void markReservedName(Level world, BlockPos pos, String name) {
      this.reservedDimensionNames.put(name, DimensionCreator.ReservedName.create(world, pos, System.currentTimeMillis()));
   }

   public Level getDimWorld(String name) {
      Identifier id = Identifier.parse(name);
      ResourceKey<Level> type = LevelTools.getId(id);
      ServerLevel world = ServerLifecycleHooks.getCurrentServer().getLevel(type);
      if (world == null && !name.contains(":")) {
         id = Identifier.fromNamespaceAndPath("rftoolsdim", name);
         type = LevelTools.getId(id);
         return ServerLifecycleHooks.getCurrentServer().getLevel(type);
      } else {
         return world;
      }
   }

   public boolean isNameAvailable(Level world, @Nullable BlockPos pos, String name) {
      long currentTime = System.currentTimeMillis();
      DimensionCreator.ReservedName reservedName = this.reservedDimensionNames.get(name);
      if (reservedName == null
         || currentTime >= reservedName.reservationTime + 10000L
         || reservedName.pos.equals(pos) && reservedName.world.equals(world.dimension())) {
         Identifier id = Identifier.fromNamespaceAndPath("rftoolsdim", name);
         PersistantDimensionManager mgr = PersistantDimensionManager.get(world);
         DimensionData data = mgr.getData(id);
         return data == null;
      } else {
         return false;
      }
   }

   public boolean isDescriptorAvailable(Level world, DimensionDescriptor descriptor) {
      PersistantDimensionManager mgr = PersistantDimensionManager.get(world);
      DimensionData data = mgr.getData(descriptor);
      return data == null;
   }

   public ServerLevel createWorld(
      ServerLevel world, String name, long seed, DimensionDescriptor descriptor, DimensionDescriptor randomizedDescriptor, UUID owner
   ) {
      Identifier id = Identifier.fromNamespaceAndPath("rftoolsdim", name);
      PersistantDimensionManager mgr = PersistantDimensionManager.get(world);
      DimensionData data = mgr.getData(id);
      if (data != null) {
         RFToolsDim.setup.getLogger().error("There is already a dimension with this id: " + name);
         throw new RuntimeException("There is already a dimension with this id: " + name);
      } else {
         data = mgr.getData(descriptor);
         if (data != null) {
            RFToolsDim.setup.getLogger().error("There is already a dimension with this descriptor: " + name);
            throw new RuntimeException("There is already a dimension with this descriptor: " + name);
         } else {
            randomizedDescriptor.log("Attempting to create dimension:");
            CompiledDescriptor compiledDescriptor = new CompiledDescriptor();

            try {
               compiledDescriptor.compile(descriptor, randomizedDescriptor);
            } catch (DescriptorError var22) {
               RFToolsDim.setup.getLogger().error("Error compiling dimension descriptor: " + var22.getMessage());
               throw new RuntimeException("Error compiling dimension descriptor: " + var22.getMessage());
            }

            compiledDescriptor.complete();
            compiledDescriptor.log("Compiled Descriptor:");
            TerrainType terrainType = compiledDescriptor.getTerrainType();
            DimensionSettings settings = new DimensionSettings(seed, descriptor.compact(), randomizedDescriptor.compact());
            TimeType timeType = compiledDescriptor.getTimeType();
            ResourceKey<Level> key = LevelTools.getId(id);
            if (settings.getCompiledDescriptor().getAttributeTypes().contains(AttributeType.CITIES) && LostCityCompat.hasLostCities()) {
               LostCityCompat.registerDimension(world, key, LostCityCompat.getProfile(terrainType));
            }

            RegistryAccess registryAccess = world.getServer().registryAccess();
            Identifier dimensionType = timeType.getDimensionType();
            if (terrainType == TerrainType.CAVERN) {
               dimensionType = DimensionRegistry.CAVERN_ID;
            }

            Holder<DimensionType> type = registryAccess.lookupOrThrow(Registries.DIMENSION_TYPE)
               .getOrThrow(ResourceKey.create(Registries.DIMENSION_TYPE, dimensionType));
            ServerLevel result = DynamicDimensionManager.getOrCreateLevel(
               world.getServer(),
               key,
               (server, registryKey) -> {
                  Registry<NoiseGeneratorSettings> noiseGeneratorSettings = registryAccess.lookupOrThrow(Registries.NOISE_SETTINGS);
                  Reference<NoiseGeneratorSettings> noiseSettingsIn = noiseGeneratorSettings.getOrThrow(terrainType.getNoiseSettings());
                  NoiseGeneratorSettings noiseSettings = this.adapt((NoiseGeneratorSettings)noiseSettingsIn.value(), settings);
                  ChunkGenerator generator = new RFToolsChunkGenerator(
                     this.getStructures(server, settings),
                     new RFTBiomeProvider(registryAccess.lookupOrThrow(Registries.WORLD_PRESET), registryAccess.lookupOrThrow(Registries.BIOME), settings),
                     seed,
                     Holder.direct(noiseSettings),
                     settings
                  );
                  return new LevelStem(type, generator);
               }
            );
            long skyDimletTypes = compiledDescriptor.getSkyDimletTypes();
            if (skyDimletTypes == 0L && terrainType == TerrainType.CAVERN) {
               skyDimletTypes = SkyDimletType.BLACK.getMask() | SkyDimletType.BLACKFOG.getMask();
            }

            data = new DimensionData(id, descriptor, randomizedDescriptor, owner, skyDimletTypes);
            mgr.register(data);
            return result;
         }
      }
   }

   public static ServerLevel getOverworld() {
      MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
      return server.getLevel(Level.OVERWORLD);
   }

   @NotNull
   private List<Holder<StructureSet>> getStructures(MinecraftServer server, DimensionSettings settings) {
      List<Identifier> structures = settings.getCompiledDescriptor().getStructures();
      List<Holder<StructureSet>> list = new ArrayList<>();
      Primes primes = new Primes();

      for (Identifier structure : structures) {
         if (structure.getPath().equals("none")) {
            list.clear();
            break;
         }

         if (structure.getPath().equals("default")) {
            return Collections.emptyList();
         }

         ResourceKey<Registry<Structure>> registryName = Registries.STRUCTURE;
         Registry<Structure> registry = getOverworld().registryAccess().lookupOrThrow(registryName);
         TagKey<Structure> tagKey = TagKey.create(registryName, structure);
         Optional<Named<Structure>> tag = registry.get(tagKey);
         if (tag.isPresent() && tag.get().size() != 0) {
            tag.get().forEach(st -> {
               StructureSet set = new StructureSet(st, new RandomSpreadStructurePlacement(12, 5, RandomSpreadType.LINEAR, primes.nextIntUnsigned()));
               list.add(Holder.direct(set));
            });
         } else {
            registry.get(ResourceKey.create(Registries.STRUCTURE, structure)).ifPresent(cfg -> {
               StructureSet set = new StructureSet(cfg, new RandomSpreadStructurePlacement(12, 5, RandomSpreadType.LINEAR, primes.nextIntUnsigned()));
               list.add(Holder.direct(set));
            });
         }

         server.registryAccess().lookupOrThrow(Registries.STRUCTURE_SET).get(ResourceKey.create(Registries.STRUCTURE_SET, structure)).ifPresent(list::add);
      }

      return list;
   }

   private NoiseGeneratorSettings adapt(NoiseGeneratorSettings in, DimensionSettings settings) {
      NoiseGeneratorSettingsBuilder builder = NoiseGeneratorSettingsBuilder.create(in);
      CompiledDescriptor compiledDescriptor = settings.getCompiledDescriptor();
      if (compiledDescriptor.getAttributeTypes().contains(AttributeType.NOOCEANS)) {
         builder.seaLevel(-64);
      }

      if (compiledDescriptor.getAttributeTypes().contains(AttributeType.WATERWORLD)) {
         builder.seaLevel(200);
      }

      if (compiledDescriptor.getTerrainType().isVoidLike()) {
         builder.seaLevel(-64);
      }

      if (compiledDescriptor.getBaseBlock() != null) {
         builder.baseBlock(compiledDescriptor.getBaseBlock());
         RuleSource adapted = this.adaptSurfaceRule(in.surfaceRule(), compiledDescriptor.getBaseBlock());
         builder.ruleSource(adapted);
      }

      builder.liquidBlock(compiledDescriptor.getBaseLiquid());
      return builder.build(settings);
   }

   private RuleSource adaptSurfaceRule(RuleSource input, BlockState baseBlock) {
      if (input instanceof BlockRuleSource) {
         return new BlockRuleSource(baseBlock);
      } else if (!(input instanceof SequenceRuleSource sequenceRuleSource)) {
         return input;
      } else {
         SequenceRuleSource output = new SequenceRuleSource(new ArrayList());

         for (RuleSource source : sequenceRuleSource.sequence()) {
            output.sequence().add(this.adaptSurfaceRule(source, baseBlock));
         }

         return output;
      }
   }

   public String createDimension(ServerLevel world, String name, long seed, String filename, UUID owner) {
      ResourceKey<Level> id = LevelTools.getId(Identifier.fromNamespaceAndPath("rftoolsdim", name));
      if (world.getServer().getLevel(id) != null) {
         return "Dimension already exists!";
      } else {
         DimensionDescriptor descriptor = new DimensionDescriptor();
         if (!filename.endsWith(".json")) {
            filename = filename + ".json";
         }

         try (
            InputStream inputstream = RFToolsDim.class.getResourceAsStream("/data/rftoolsdim/rftdim/" + filename);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8));
         ) {
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(br);
            descriptor.read(element.getAsJsonArray());
         } catch (IOException var17) {
            throw new UncheckedIOException(var17);
         }

         this.createWorld(world, name, seed, descriptor, DimensionDescriptor.EMPTY, owner);
         return null;
      }
   }

   public void registerPlatformHeight(Identifier location, int floorHeight) {
      this.platformHeightMap.put(location, floorHeight);
   }

   public int getPlatformHeight(Identifier location) {
      return this.platformHeightMap.getOrDefault(location, 65);
   }

   private record ReservedName(long reservationTime, BlockPos pos, ResourceKey<Level> world) {
      public static DimensionCreator.ReservedName create(Level world, BlockPos pos, long reservationTime) {
         return new DimensionCreator.ReservedName(reservationTime, pos, world.dimension());
      }
   }
}
