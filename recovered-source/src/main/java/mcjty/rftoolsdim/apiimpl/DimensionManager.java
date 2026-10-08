package mcjty.rftoolsdim.apiimpl;

import mcjty.rftoolsbase.api.dimension.IDimensionInformation;
import mcjty.rftoolsbase.api.dimension.IDimensionManager;
import mcjty.rftoolsdim.dimension.data.PersistantDimensionManager;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

public class DimensionManager implements IDimensionManager {
   public IDimensionInformation getDimensionInformation(Level world, Identifier id) {
      PersistantDimensionManager mgr = PersistantDimensionManager.get(world);
      return mgr.getData(id);
   }
}
