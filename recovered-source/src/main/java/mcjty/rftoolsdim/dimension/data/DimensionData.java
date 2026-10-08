package mcjty.rftoolsdim.dimension.data;

import java.util.UUID;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsbase.api.dimension.IDimensionInformation;
import mcjty.rftoolsdim.dimension.descriptor.DimensionDescriptor;
import mcjty.rftoolsdim.dimension.power.PowerHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

public class DimensionData implements IDimensionInformation {
   private final Identifier id;
   private final DimensionDescriptor descriptor;
   private final DimensionDescriptor randomizedDescriptor;
   private final UUID owner;
   private final long skyDimletTypes;
   private long energy;
   private int activityProbes;

   public DimensionData(Identifier id, DimensionDescriptor descriptor, DimensionDescriptor randomizedDescriptor, UUID owner, long skyDimletTypes) {
      this.id = id;
      this.descriptor = descriptor;
      this.randomizedDescriptor = randomizedDescriptor;
      this.owner = owner;
      this.skyDimletTypes = skyDimletTypes;
      this.activityProbes = 0;
   }

   public DimensionData(CompoundTag tag) {
      this.id = Identifier.parse(tag.getStringOr("id", ""));
      this.descriptor = new DimensionDescriptor();
      this.descriptor.read(tag.getStringOr("descriptor", ""));
      this.energy = tag.getLong("energy").orElse(0L);
      if (tag.contains("randomized")) {
         this.randomizedDescriptor = new DimensionDescriptor();
         this.randomizedDescriptor.read(tag.getStringOr("randomized", ""));
      } else {
         this.randomizedDescriptor = DimensionDescriptor.EMPTY;
      }

      this.skyDimletTypes = tag.getLong("skytypes").orElse(0L);
      this.activityProbes = tag.getInt("probes").orElse(0);
      if (tag.contains("owner")) {
         this.owner = UUID.fromString(tag.getStringOr("owner", "00000000-0000-0000-0000-000000000000"));
      } else {
         this.owner = null;
      }
   }

   public void write(CompoundTag tag) {
      tag.putString("id", this.id.toString());
      tag.putString("descriptor", this.descriptor.compact());
      tag.putString("randomized", this.randomizedDescriptor.compact());
      tag.putLong("energy", this.energy);
      tag.putLong("skytypes", this.skyDimletTypes);
      tag.putInt("probes", this.activityProbes);
      if (this.owner != null) {
         tag.putString("owner", this.owner.toString());
      }
   }

   public Identifier getId() {
      return this.id;
   }

   public DimensionDescriptor getDescriptor() {
      return this.descriptor;
   }

   public DimensionDescriptor getRandomizedDescriptor() {
      return this.randomizedDescriptor;
   }

   public UUID getOwner() {
      return this.owner;
   }

   public long getSkyTypes() {
      return this.skyDimletTypes;
   }

   public long getEnergy() {
      return this.energy;
   }

   public long getMaxEnergy(Level world) {
      return PowerHandler.calculateMaxDimensionPower(this.id, world);
   }

   public int getActivityProbes() {
      return this.activityProbes;
   }

   public void setActivityProbes(int activityProbes) {
      this.activityProbes = activityProbes;
   }

   public void setEnergy(Level overworld, long energy) {
      if (energy != this.energy) {
         long old = this.energy;
         this.energy = energy;
         if (overworld != null) {
            if (old == 0L && energy > 0L) {
               Level var7 = LevelTools.getLevel(overworld, this.id);
               if (var7 != null) {
               }
            } else if (energy == 0L) {
               Level var6 = LevelTools.getLevel(overworld, this.id);
               if (var6 != null) {
               }
            }
         }
      }
   }
}
