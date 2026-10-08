package mcjty.rftoolsdim.dimension.power;

import javax.annotation.Nonnull;
import mcjty.lib.varia.ComponentFactory;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.LivingEntity;

public class DamageSourcePowerLow extends DamageSource {
   public DamageSourcePowerLow(RegistryAccess access) {
      super(new DamageSources(access).genericKill().typeHolder());
   }

   @Nonnull
   public Component getLocalizedDeathMessage(LivingEntity entity) {
      String s = "death.dimension.powerfailure";
      return ComponentFactory.translatable(s, new Object[]{entity.getName()});
   }
}
