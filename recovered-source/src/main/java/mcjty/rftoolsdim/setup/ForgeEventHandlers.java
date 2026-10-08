package mcjty.rftoolsdim.setup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import mcjty.rftoolsdim.RFToolsDim;
import mcjty.rftoolsdim.dimension.data.DimensionData;
import mcjty.rftoolsdim.dimension.data.PersistantDimensionManager;
import mcjty.rftoolsdim.dimension.descriptor.CompiledDescriptor;
import mcjty.rftoolsdim.dimension.power.PowerHandler;
import mcjty.rftoolsdim.dimension.terraintypes.AttributeType;
import mcjty.rftoolsdim.dimension.terraintypes.RFToolsChunkGenerator;
import mcjty.rftoolsdim.modules.blob.entities.DimensionalBlobEntity;
import mcjty.rftoolsdim.modules.blob.tools.Spawner;
import mcjty.rftoolsdim.modules.dimlets.DimletConfig;
import mcjty.rftoolsdim.modules.dimlets.data.DimletDictionary;
import mcjty.rftoolsdim.modules.dimlets.data.DimletKey;
import mcjty.rftoolsdim.modules.dimlets.data.DimletSettings;
import mcjty.rftoolsdim.modules.dimlets.network.PacketSendDimletPackages;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.level.BlockEvent.NeighborNotifyEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent.Pre;

public class ForgeEventHandlers {
   private final Random random = new Random();
   private final PowerHandler powerHandler = new PowerHandler();
   private static int counter = 50;

   @SubscribeEvent
   public void onNeighborNotify(NeighborNotifyEvent event) {
      if (event.getLevel() instanceof ServerLevel serverLevel && serverLevel.getChunkSource().getGenerator() instanceof RFToolsChunkGenerator) {
         counter--;
         if (counter < 0) {
            counter = 50;
            StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
            if (stacktrace.length > 400) {
               RFToolsDim.setup.getLogger().warn("Canceled a possible stackoverflow: " + stacktrace.length);
               event.setCanceled(true);
               counter = 1;
            }
         }
      }
   }

   @SubscribeEvent
   public void onWorldTick(Pre event) {
      if (!event.getLevel().isClientSide()) {
         this.handleSpawning(event);
         if (event.getLevel().dimension() == Level.OVERWORLD) {
            this.powerHandler.handlePower(event.getLevel());
         }
      }
   }

   private void handleSpawning(Pre event) {
      if (!event.getLevel().isClientSide()) {
         ServerLevel serverWorld = (ServerLevel)event.getLevel();
         if (!serverWorld.players().isEmpty()) {
            if (serverWorld.getChunkSource().getGenerator() instanceof RFToolsChunkGenerator chunkGenerator && this.random.nextInt(20) == 10) {
               CompiledDescriptor compiledDescriptor = chunkGenerator.getDimensionSettings().getCompiledDescriptor();
               DimensionData data = PersistantDimensionManager.get(serverWorld).getData(serverWorld.dimension().identifier());
               if (!compiledDescriptor.getAttributeTypes().contains(AttributeType.NOBLOBS)) {
                  int count = 0;

                  for (Entity entity : serverWorld.getEntities().getAll()) {
                     if (entity instanceof DimensionalBlobEntity) {
                        count++;
                     }
                  }

                  if (count < 20) {
                     for (ServerPlayer player : serverWorld.players()) {
                        for (int i = 0; i < 5; i++) {
                           Spawner.spawnOne(serverWorld, player, compiledDescriptor, data, this.random);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   public void onServerStarted(ServerStartedEvent event) {
      RFToolsDim.setup.getLogger().info("Reading dimlet packages: ");
      DimletDictionary.get().reset();

      for (String file : (List)DimletConfig.DIMLET_PACKAGES.get()) {
         DimletDictionary.get().readPackage(event.getServer().registryAccess(), file);
      }
   }

   @SubscribeEvent
   public void onPlayerLoggedIn(PlayerLoggedInEvent event) {
      RFToolsDim.setup.getLogger().info("Client logged in: sending dimlet packages");
      Map<DimletKey, DimletSettings> collected = new HashMap<>();
      DimletDictionary dictionary = DimletDictionary.get();

      for (DimletKey key : dictionary.getDimlets()) {
         collected.put(key, dictionary.getSettings(key));
         if (collected.size() >= 100) {
            RFToolsDimMessages.sendToPlayer(PacketSendDimletPackages.create(collected), event.getEntity());
            collected.clear();
         }
      }

      if (!collected.isEmpty()) {
         RFToolsDimMessages.sendToPlayer(PacketSendDimletPackages.create(collected), event.getEntity());
      }
   }
}
