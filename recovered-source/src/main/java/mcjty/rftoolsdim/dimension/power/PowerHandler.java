package mcjty.rftoolsdim.dimension.power;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsdim.dimension.DimensionConfig;
import mcjty.rftoolsdim.dimension.data.ClientDimensionData;
import mcjty.rftoolsdim.dimension.data.DimensionCreator;
import mcjty.rftoolsdim.dimension.data.DimensionData;
import mcjty.rftoolsdim.dimension.data.PersistantDimensionManager;
import mcjty.rftoolsdim.dimension.descriptor.CompiledDescriptor;
import mcjty.rftoolsdim.dimension.network.PackagePropageDataToClients;
import mcjty.rftoolsdim.modules.dimensionbuilder.items.PhasedFieldGenerator;
import mcjty.rftoolsdim.setup.RFToolsDimMessages;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class PowerHandler {
   public static final int MAXTICKS = 10;
   private int counter = 10;
   private static final int EFFECTS_MAX = 18;
   private int counterEffects = 18;

   public static long calculateMaxDimensionPower(Identifier id, Level overworld) {
      CompiledDescriptor descriptor = DimensionCreator.get().getCompiledDescriptor(overworld, id);
      return descriptor != null ? calculateMaxDimensionPower(descriptor) : (Long)DimensionConfig.MAX_DIMENSION_POWER_MAX.get();
   }

   public static long calculateMaxDimensionPower(CompiledDescriptor descriptor) {
      int cost = descriptor.getActualPowerCost();
      if (cost <= (Integer)DimensionConfig.MIN_POWER_THRESSHOLD.get()) {
         return (Long)DimensionConfig.MAX_DIMENSION_POWER_MIN.get();
      } else if (cost >= (Integer)DimensionConfig.MAX_POWER_THRESSHOLD.get()) {
         return (Long)DimensionConfig.MAX_DIMENSION_POWER_MAX.get();
      } else {
         long power = (Long)DimensionConfig.MAX_DIMENSION_POWER_MIN.get()
            + (cost - (Integer)DimensionConfig.MIN_POWER_THRESSHOLD.get())
               * ((Long)DimensionConfig.MAX_DIMENSION_POWER_MAX.get() - (Long)DimensionConfig.MAX_DIMENSION_POWER_MIN.get())
               / ((Integer)DimensionConfig.MAX_POWER_THRESSHOLD.get() - (Integer)DimensionConfig.MIN_POWER_THRESSHOLD.get());
         power /= DimensionConfig.POWER_MULTIPLES.get();
         return power * (Long)DimensionConfig.POWER_MULTIPLES.get();
      }
   }

   public void handlePower(Level overworld) {
      this.counter--;
      if (this.counter <= 0) {
         this.counter = 10;
         this.counterEffects--;
         boolean doEffects = false;
         if (this.counterEffects <= 0) {
            this.counterEffects = 18;
            doEffects = true;
         }

         this.handlePower(overworld, doEffects);
         this.sendOutPower(overworld);
      }
   }

   private void sendOutPower(Level overworld) {
      PersistantDimensionManager mgr = PersistantDimensionManager.get(overworld);
      Map<Identifier, ClientDimensionData.ClientData> clientDataMap = new HashMap<>();

      for (Entry<Identifier, DimensionData> entry : mgr.getData().entrySet()) {
         long energy = entry.getValue().getEnergy();
         clientDataMap.put(
            entry.getKey(), new ClientDimensionData.ClientData(energy, calculateMaxDimensionPower(entry.getKey(), overworld), entry.getValue().getSkyTypes())
         );
      }

      RFToolsDimMessages.sendToAll(new PackagePropageDataToClients(clientDataMap, ((ServerLevel)overworld).getSeed()));
   }

   private void handlePower(Level overworld, boolean doEffects) {
      PersistantDimensionManager mgr = PersistantDimensionManager.get(overworld);

      for (Entry<Identifier, DimensionData> entry : mgr.getData().entrySet()) {
         ServerLevel world = LevelTools.getLevel(overworld, entry.getKey());
         CompiledDescriptor compiledDescriptor = DimensionCreator.get().getCompiledDescriptor(world);
         if (compiledDescriptor != null) {
            boolean doPower = true;
            if (entry.getValue().getActivityProbes() > 0) {
               int chunks = world.getChunkSource().chunkMap.size();
               if (chunks == 0) {
                  doPower = false;
               }
            }

            long power;
            if (doPower) {
               power = this.handlePowerDimension(doEffects, world, entry.getValue(), compiledDescriptor);
            } else {
               power = entry.getValue().getEnergy();
            }

            if (doEffects && power > 0L) {
               this.handleEffectsForDimension(power, world, compiledDescriptor);
            }

            if (world != null && !world.players().isEmpty()) {
               this.handleRandomEffects(world, entry.getValue());
            }
         }
      }

      mgr.save();
   }

   private long handlePowerDimension(boolean doEffects, ServerLevel world, DimensionData data, CompiledDescriptor compiledDescriptor) {
      int cost = compiledDescriptor.getActualPowerCost();
      long power = data.getEnergy();
      power -= cost * 10L;
      if (power < 0L) {
         power = 0L;
      }

      this.handleLowPower(world, power, doEffects, cost);
      data.setEnergy(world, power);
      return power;
   }

   private void handleEffectsForDimension(long power, ServerLevel world, CompiledDescriptor compiledDescriptor) {
      if (world != null) {
         for (ServerPlayer player : new ArrayList(world.players())) {
            long max = calculateMaxDimensionPower(compiledDescriptor);
            int percentage = (int)(power * 100L / max);
            if (percentage < (Integer)DimensionConfig.DIMPOWER_WARN3.get()) {
               player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 360, 4, true, true));
               player.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 360, 4, true, true));
               player.addEffect(new MobEffectInstance(MobEffects.POISON, 360, 2, true, true));
               player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 360, 2, true, true));
            } else if (percentage < (Integer)DimensionConfig.DIMPOWER_WARN2.get()) {
               player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 360, 2, true, true));
               player.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 360, 2, true, true));
               player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 360, 1, true, true));
            } else if (percentage < (Integer)DimensionConfig.DIMPOWER_WARN1.get()) {
               player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 360, 0, true, true));
               player.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 360, 0, true, true));
            }
         }
      }
   }

   private void handleLowPower(ServerLevel world, long power, boolean doEffects, int phasedCost) {
      if (power <= 0L && world != null) {
         for (Player player : new ArrayList(world.players())) {
            if (!PhasedFieldGenerator.checkValidPhasedFieldGenerator(player, true, phasedCost)) {
               player.hurt(new DamageSourcePowerLow(world.registryAccess()), 1000000.0F);
            } else if (doEffects && (Boolean)DimensionConfig.PHASED_FIELD_GENERATOR_DEBUF.get()) {
               player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 180, 2, true, true));
               player.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 180, 2, true, true));
               player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 180, 2, true, true));
            }
         }
      }
   }

   private void handleRandomEffects(ServerLevel world, DimensionData information) {
   }
}
