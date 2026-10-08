package mcjty.rftoolsdim.dimension.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import mcjty.lib.worlddata.AbstractWorldData;
import mcjty.rftoolsdim.dimension.descriptor.DimensionDescriptor;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

public class PersistantDimensionManager extends AbstractWorldData<PersistantDimensionManager> {
   private static final String NAME = "RFToolsDimensions";
   private final Map<Identifier, DimensionData> data = new HashMap<>();
   private final Map<DimensionDescriptor, DimensionData> dataByDescriptor = new HashMap<>();

   public PersistantDimensionManager() {
   }

   public PersistantDimensionManager(CompoundTag tag) {
      ListTag dimensions = (ListTag)tag.getList("dimensions").orElseGet(ListTag::new);
      this.data.clear();
      this.dataByDescriptor.clear();

      for (Tag inbt : dimensions) {
         CompoundTag dtag = (CompoundTag)inbt;
         DimensionData dd = new DimensionData(dtag);
         this.data.put(dd.getId(), dd);
         this.dataByDescriptor.put(dd.getDescriptor(), dd);
      }
   }

   @Nonnull
   public static PersistantDimensionManager get(Level world) {
      return (PersistantDimensionManager)getData(world, PersistantDimensionManager::new, PersistantDimensionManager::new, "RFToolsDimensions");
   }

   public DimensionData getData(Identifier id) {
      return this.data.get(id);
   }

   public DimensionData getData(DimensionDescriptor descriptor) {
      return this.dataByDescriptor.get(descriptor);
   }

   public Map<Identifier, DimensionData> getData() {
      return this.data;
   }

   public void register(DimensionData dd) {
      this.data.put(dd.getId(), dd);
      this.dataByDescriptor.put(dd.getDescriptor(), dd);
      this.setDirty();
   }

   public void forget(Identifier key) {
      DimensionData dd = this.data.get(key);
      this.data.remove(key);
      if (dd != null) {
         this.dataByDescriptor.remove(dd.getDescriptor());
      }

      this.setDirty();
   }

   @Nonnull
   public CompoundTag save(@Nonnull CompoundTag compound, Provider provider) {
      CompoundTag tag = new CompoundTag();
      ListTag list = new ListTag();

      for (Entry<Identifier, DimensionData> entry : this.data.entrySet()) {
         CompoundTag dtag = new CompoundTag();
         entry.getValue().write(dtag);
         list.add(dtag);
      }

      tag.put("dimensions", list);
      return tag;
   }
}
