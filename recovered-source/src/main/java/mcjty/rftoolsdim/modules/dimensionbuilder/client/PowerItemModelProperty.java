package mcjty.rftoolsdim.modules.dimensionbuilder.client;

import com.mojang.serialization.MapCodec;
import mcjty.rftoolsdim.dimension.data.ClientDimensionData;
import mcjty.rftoolsdim.modules.dimensionbuilder.DimensionBuilderConfig;
import mcjty.rftoolsdim.modules.dimensionbuilder.data.PhasedFieldGeneratorData;
import mcjty.rftoolsdim.modules.dimensionbuilder.items.DimensionMonitorItem;
import mcjty.rftoolsdim.modules.dimensionbuilder.items.PhasedFieldGenerator;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record PowerItemModelProperty() implements RangeSelectItemModelProperty {
   public static final MapCodec<PowerItemModelProperty> MAP_CODEC = MapCodec.unit(new PowerItemModelProperty());

   public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
      if (stack.getItem() instanceof PhasedFieldGenerator) {
         long power = Math.max(0L, PhasedFieldGeneratorData.getEnergy(stack));
         long max = Math.max(1L, (Long)DimensionBuilderConfig.PHASEDFIELD_MAXENERGY.get());
         long scaled = Math.max(0L, Math.min(8L, 9L * power / max));
         return (float)(8L - scaled);
      } else if (stack.getItem() instanceof DimensionMonitorItem) {
         if (level == null) {
            return 8.0F;
         } else {
            Identifier id = level.dimension().identifier();
            long power = ClientDimensionData.get().getPower(id);
            long max = ClientDimensionData.get().getMaxPower(id);
            if (max > 0L && power >= 0L) {
               long scaled = Math.max(0L, Math.min(8L, 9L * power / max));
               return (float)(8L - scaled);
            } else {
               return 8.0F;
            }
         }
      } else {
         return 8.0F;
      }
   }

   public MapCodec<PowerItemModelProperty> type() {
      return MAP_CODEC;
   }
}
