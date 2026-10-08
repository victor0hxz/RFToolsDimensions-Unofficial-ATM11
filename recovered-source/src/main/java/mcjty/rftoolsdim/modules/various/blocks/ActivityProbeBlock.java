package mcjty.rftoolsdim.modules.various.blocks;

import mcjty.lib.gui.ManualEntry;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsdim.dimension.data.DimensionData;
import mcjty.rftoolsdim.dimension.data.PersistantDimensionManager;
import mcjty.rftoolsdim.dimension.terraintypes.RFToolsChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class ActivityProbeBlock extends Block implements ITooltipSettings {
   public ActivityProbeBlock(Properties properties) {
      super(properties);
   }

   public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
      if (!level.isClientSide() && newState.getBlock() != this && ((ServerLevel)level).getChunkSource().getGenerator() instanceof RFToolsChunkGenerator) {
         PersistantDimensionManager mgr = PersistantDimensionManager.get(level);
         DimensionData data = mgr.getData(level.dimension().identifier());
         data.setActivityProbes(data.getActivityProbes() - 1);
         mgr.save();
      }
   }

   public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
      if (!level.isClientSide() && ((ServerLevel)level).getChunkSource().getGenerator() instanceof RFToolsChunkGenerator) {
         PersistantDimensionManager mgr = PersistantDimensionManager.get(level);
         DimensionData data = mgr.getData(level.dimension().identifier());
         data.setActivityProbes(data.getActivityProbes() + 1);
         mgr.save();
      }
   }

   public ManualEntry getManualEntry() {
      return ManualHelper.create("rftoolsdim:dimensions/activity_probe");
   }
}
