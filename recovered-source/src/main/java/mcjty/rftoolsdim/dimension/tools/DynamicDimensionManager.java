package mcjty.rftoolsdim.dimension.tools;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.serialization.Lifecycle;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess.ImmutableRegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayer.RespawnConfig;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent.Load;
import net.neoforged.neoforge.event.level.LevelEvent.Unload;

public class DynamicDimensionManager {
   private static final Set<ResourceKey<Level>> VANILLA_WORLDS = ImmutableSet.of(Level.OVERWORLD, Level.NETHER, Level.END);
   private static Set<ResourceKey<Level>> pendingLevelsToUnregister = new HashSet<>();

   public static ServerLevel getOrCreateLevel(
      MinecraftServer server, ResourceKey<Level> levelKey, BiFunction<MinecraftServer, ResourceKey<LevelStem>, LevelStem> dimensionFactory
   ) {
      Map<ResourceKey<Level>, ServerLevel> map = server.forgeGetWorldMap();
      ServerLevel existingLevel = map.get(levelKey);
      return existingLevel != null ? existingLevel : createAndRegisterWorldAndDimension(server, map, levelKey, dimensionFactory);
   }

   public static void markDimensionForUnregistration(MinecraftServer server, ResourceKey<Level> levelToRemove) {
      if (!VANILLA_WORLDS.contains(levelToRemove)) {
         pendingLevelsToUnregister.add(levelToRemove);
      }
   }

   public static Set<ResourceKey<Level>> getWorldsPendingUnregistration() {
      return Collections.unmodifiableSet(pendingLevelsToUnregister);
   }

   @Deprecated
   public static void unregisterScheduledDimensions(MinecraftServer server) {
      Set<ResourceKey<Level>> keysToRemove = pendingLevelsToUnregister;
      pendingLevelsToUnregister = new HashSet<>();
      Set<ResourceKey<Level>> removedLevelKeys = new HashSet<>();
      ServerLevel overworld = server.getLevel(Level.OVERWORLD);

      for (ResourceKey<Level> levelKeyToRemove : keysToRemove) {
         ServerLevel removedLevel = (ServerLevel)server.forgeGetWorldMap().remove(levelKeyToRemove);
         if (removedLevel != null) {
            for (ServerPlayer player : Lists.newArrayList(removedLevel.players())) {
               RespawnConfig respawnConfig = player.getRespawnConfig();
               ServerLevel destinationLevel = server.findRespawnDimension();
               BlockPos destinationPos = server.getRespawnData().pos();
               float respawnAngle = server.getRespawnData().yaw();
               if (respawnConfig != null && !keysToRemove.contains(respawnConfig.respawnData().dimension())) {
                  ServerLevel configuredLevel = server.getLevel(respawnConfig.respawnData().dimension());
                  if (configuredLevel != null) {
                     destinationLevel = configuredLevel;
                     destinationPos = respawnConfig.respawnData().pos();
                     respawnAngle = respawnConfig.respawnData().yaw();
                  }
               } else if (respawnConfig != null) {
                  player.setRespawnPosition(null, false);
               }

               player.teleportTo(
                  destinationLevel,
                  destinationPos.getX() + 0.5,
                  destinationPos.getY(),
                  destinationPos.getZ() + 0.5,
                  Collections.emptySet(),
                  respawnAngle,
                  0.0F,
                  true
               );
            }

            removedLevel.save(null, false, removedLevel.noSave());
            NeoForge.EVENT_BUS.post(new Unload(removedLevel));
            removedLevelKeys.add(levelKeyToRemove);
         }
      }

      if (!removedLevelKeys.isEmpty()) {
         LayeredRegistryAccess<RegistryLayer> registries = server.registries();
         ImmutableRegistryAccess composite = (ImmutableRegistryAccess)registries.compositeAccess();
         Map<ResourceKey<?>, Registry<?>> hashMap = new HashMap<>();
         ResourceKey<?> key = ResourceKey.create(ResourceKey.createRegistryKey(Identifier.parse("root")), Identifier.parse("dimension"));
         Registry<LevelStem> oldRegistry = (Registry<LevelStem>)hashMap.get(key);
         Lifecycle oldLifecycle = null;
         Registry<LevelStem> newRegistry = new MappedRegistry(Registries.LEVEL_STEM, oldLifecycle, false);

         for (Entry<ResourceKey<LevelStem>, LevelStem> entry : oldRegistry.entrySet()) {
            ResourceKey<LevelStem> oldKey = entry.getKey();
            ResourceKey<Level> oldLevelKey = ResourceKey.create(Registries.DIMENSION, oldKey.identifier());
            LevelStem dimension = entry.getValue();
            if (oldKey != null && dimension != null && !removedLevelKeys.contains(oldLevelKey)) {
               if (newRegistry instanceof MappedRegistry<LevelStem> mappedRegistry) {
                  mappedRegistry.unfreeze(false);
               }

               Registry.register(newRegistry, oldKey, dimension);
            }
         }

         hashMap.replace(key, newRegistry);
         server.markWorldsDirty();
         PacketSyncDimensionListChanges.updateClientDimensionLists(ImmutableSet.of(), removedLevelKeys);
      }
   }

   private static ServerLevel createAndRegisterWorldAndDimension(
      MinecraftServer server,
      Map<ResourceKey<Level>, ServerLevel> map,
      ResourceKey<Level> worldKey,
      BiFunction<MinecraftServer, ResourceKey<LevelStem>, LevelStem> dimensionFactory
   ) {
      ServerLevel overworld = server.getLevel(Level.OVERWORLD);
      ResourceKey<LevelStem> dimensionKey = ResourceKey.create(Registries.LEVEL_STEM, worldKey.identifier());
      LevelStem dimension = dimensionFactory.apply(server, dimensionKey);
      Executor executor = server.executor;
      LevelStorageAccess anvilConverter = server.storageSource;
      WorldData worldData = server.getWorldData();
      DerivedLevelData derivedLevelData = new DerivedLevelData(worldData, worldData.overworldData());
      LayeredRegistryAccess<RegistryLayer> registries = server.registries();
      ImmutableRegistryAccess composite = (ImmutableRegistryAccess)registries.compositeAccess();
      Map<ResourceKey<? extends Registry<?>>, Registry<?>> regmap = new HashMap<>(composite.registries);
      ResourceKey<? extends Registry<?>> key = ResourceKey.create(ResourceKey.createRegistryKey(Identifier.parse("root")), Identifier.parse("dimension"));
      MappedRegistry<LevelStem> oldRegistry = (MappedRegistry<LevelStem>)regmap.get(key);
      Lifecycle oldLifecycle = oldRegistry.registryLifecycle();
      MappedRegistry<LevelStem> newRegistry = new MappedRegistry(Registries.LEVEL_STEM, oldLifecycle, false);

      for (Entry<ResourceKey<LevelStem>, LevelStem> entry : oldRegistry.entrySet()) {
         ResourceKey<LevelStem> oldKey = entry.getKey();
         ResourceKey<Level> oldLevelKey = ResourceKey.create(Registries.DIMENSION, oldKey.identifier());
         LevelStem dim = entry.getValue();
         if (dim != null && oldLevelKey != worldKey) {
            Registry.register(newRegistry, oldKey, dim);
         }
      }

      Registry.register(newRegistry, dimensionKey, dimension);
      regmap.replace(key, newRegistry);
      composite.registries = regmap;
      ServerLevel newWorld = new ServerLevel(
         server,
         executor,
         anvilConverter,
         derivedLevelData,
         worldKey,
         dimension,
         false,
         BiomeManager.obfuscateSeed(overworld.getSeed()),
         ImmutableList.of(),
         false
      );
      map.put(worldKey, newWorld);
      server.markWorldsDirty();
      NeoForge.EVENT_BUS.post(new Load(newWorld));
      PacketSyncDimensionListChanges.updateClientDimensionLists(ImmutableSet.of(worldKey), ImmutableSet.of());
      return newWorld;
   }
}
