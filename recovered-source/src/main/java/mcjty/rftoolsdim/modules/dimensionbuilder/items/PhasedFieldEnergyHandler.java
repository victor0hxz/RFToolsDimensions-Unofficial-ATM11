package mcjty.rftoolsdim.modules.dimensionbuilder.items;

import mcjty.rftoolsdim.modules.dimensionbuilder.DimensionBuilderConfig;
import mcjty.rftoolsdim.modules.dimensionbuilder.DimensionBuilderModule;
import mcjty.rftoolsdim.modules.dimensionbuilder.data.PhasedFieldGeneratorData;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public final class PhasedFieldEnergyHandler implements EnergyHandler {
   private final ItemAccess access;

   public PhasedFieldEnergyHandler(ItemAccess access) {
      this.access = access;
   }

   private PhasedFieldGeneratorData data(ItemResource resource) {
      return (PhasedFieldGeneratorData)resource.getOrDefault(
         (DataComponentType)DimensionBuilderModule.ITEM_PHASED_FIELD_GENERATOR_DATA.get(), PhasedFieldGeneratorData.DEFAULT
      );
   }

   public long getAmountAsLong() {
      int count = this.access.getAmount();
      return count <= 0 ? 0L : count * Math.max(0L, this.data(this.access.getResource()).energy());
   }

   public long getCapacityAsLong() {
      int count = this.access.getAmount();
      return count <= 0 ? 0L : count * Math.max(0L, (Long)DimensionBuilderConfig.PHASEDFIELD_MAXENERGY.get());
   }

   public int insert(int amount, TransactionContext transaction) {
      TransferPreconditions.checkNonNegative(amount);
      int count = this.access.getAmount();
      if (amount != 0 && count > 0) {
         ItemResource resource = this.access.getResource();
         long capacity = Math.max(0L, (Long)DimensionBuilderConfig.PHASEDFIELD_MAXENERGY.get());
         long current = Math.max(0L, Math.min(capacity, this.data(resource).energy()));
         int perItemRequest = amount / count;
         long maxReceive = Math.max(0L, (Long)DimensionBuilderConfig.PHASEDFIELD_RECEIVEPERTICK.get());
         int perItem = (int)Math.min(2147483647L, Math.min(maxReceive, Math.min((long)perItemRequest, capacity - current)));
         if (perItem <= 0) {
            return 0;
         } else {
            ItemResource updated = resource.with(
               (DataComponentType)DimensionBuilderModule.ITEM_PHASED_FIELD_GENERATOR_DATA.get(), new PhasedFieldGeneratorData(current + perItem)
            );
            if (updated.isEmpty()) {
               return 0;
            } else {
               int exchanged = this.access.exchange(updated, count, transaction);
               return perItem * exchanged;
            }
         }
      } else {
         return 0;
      }
   }

   public int extract(int amount, TransactionContext transaction) {
      TransferPreconditions.checkNonNegative(amount);
      int count = this.access.getAmount();
      if (amount != 0 && count > 0) {
         ItemResource resource = this.access.getResource();
         long current = Math.max(0L, this.data(resource).energy());
         int perItemRequest = amount / count;
         long maxExtract = Math.max(0L, (Long)DimensionBuilderConfig.PHASEDFIELD_CONSUMEPERTICK.get());
         int perItem = (int)Math.min(2147483647L, Math.min(maxExtract, Math.min((long)perItemRequest, current)));
         if (perItem <= 0) {
            return 0;
         } else {
            ItemResource updated = resource.with(
               (DataComponentType)DimensionBuilderModule.ITEM_PHASED_FIELD_GENERATOR_DATA.get(), new PhasedFieldGeneratorData(current - perItem)
            );
            if (updated.isEmpty()) {
               return 0;
            } else {
               int exchanged = this.access.exchange(updated, count, transaction);
               return perItem * exchanged;
            }
         }
      } else {
         return 0;
      }
   }
}
