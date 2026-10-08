package mcjty.rftoolsdim.modules.blob;

import java.util.function.Supplier;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.modules.IModule;
import mcjty.rftoolsdim.modules.blob.entities.DimensionalBlobEntity;
import mcjty.rftoolsdim.modules.dimlets.data.DimletRarity;
import mcjty.rftoolsdim.setup.Config;
import mcjty.rftoolsdim.setup.Registration;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.EntityType.Builder;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation;

public class BlobModule implements IModule {
   public static final Supplier<EntityType<DimensionalBlobEntity>> DIMENSIONAL_BLOB_COMMON = Registration.ENTITIES
      .register(
         "dimensional_blob_common",
         () -> Builder.of((type, world) -> new DimensionalBlobEntity(type, world, DimletRarity.COMMON), MobCategory.MONSTER)
            .sized(1.0F, 1.0F)
            .setShouldReceiveVelocityUpdates(false)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("rftoolsdim", "dimensional_blob_common")))
      );
   public static final Supplier<EntityType<DimensionalBlobEntity>> DIMENSIONAL_BLOB_RARE = Registration.ENTITIES
      .register(
         "dimensional_blob_rare",
         () -> Builder.of((type, world) -> new DimensionalBlobEntity(type, world, DimletRarity.RARE), MobCategory.MONSTER)
            .sized(1.3F, 1.3F)
            .setShouldReceiveVelocityUpdates(false)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("rftoolsdim", "dimensional_blob_rare")))
      );
   public static final Supplier<EntityType<DimensionalBlobEntity>> DIMENSIONAL_BLOB_LEGENDARY = Registration.ENTITIES
      .register(
         "dimensional_blob_legendary",
         () -> Builder.of((type, world) -> new DimensionalBlobEntity(type, world, DimletRarity.LEGENDARY), MobCategory.MONSTER)
            .sized(1.8F, 1.8F)
            .setShouldReceiveVelocityUpdates(false)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("rftoolsdim", "dimensional_blob_legendary")))
      );

   public BlobModule(IEventBus bus, Dist dist) {
      bus.addListener(this::registerEntityAttributes);
      bus.addListener(this::registerSpawnPlacements);
   }

   public void init(FMLCommonSetupEvent event) {
   }

   private void registerEntityAttributes(EntityAttributeCreationEvent event) {
      event.put(DIMENSIONAL_BLOB_COMMON.get(), DimensionalBlobEntity.registerAttributes(DimletRarity.COMMON).build());
      event.put(DIMENSIONAL_BLOB_RARE.get(), DimensionalBlobEntity.registerAttributes(DimletRarity.RARE).build());
      event.put(DIMENSIONAL_BLOB_LEGENDARY.get(), DimensionalBlobEntity.registerAttributes(DimletRarity.LEGENDARY).build());
   }

   private void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
      event.register(
         DIMENSIONAL_BLOB_COMMON.get(), SpawnPlacementTypes.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, Operation.AND
      );
      event.register(
         DIMENSIONAL_BLOB_RARE.get(), SpawnPlacementTypes.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, Operation.AND
      );
      event.register(
         DIMENSIONAL_BLOB_LEGENDARY.get(), SpawnPlacementTypes.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, Operation.AND
      );
   }

   public void initClient(FMLClientSetupEvent event) {
   }

   public void initConfig(IEventBus bus) {
      BlobConfig.init(Config.SERVER_BUILDER, Config.COMMON_BUILDER, Config.CLIENT_BUILDER);
   }

   public void initDatagen(DataGen dataGen, Provider provider) {
   }
}
