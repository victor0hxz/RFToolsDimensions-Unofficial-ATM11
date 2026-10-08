package mcjty.rftoolsdim.modules.essences.blocks;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.builder.InfoLine;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tileentity.TickingTileEntity;
import mcjty.lib.varia.FakePlayerGetter;
import mcjty.lib.varia.FluidTools;
import mcjty.lib.varia.SoundTools;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsdim.compat.RFToolsDimensionsTOPDriver;
import mcjty.rftoolsdim.modules.dimlets.data.DimletDictionary;
import mcjty.rftoolsdim.modules.dimlets.data.DimletKey;
import mcjty.rftoolsdim.modules.dimlets.data.DimletSettings;
import mcjty.rftoolsdim.modules.dimlets.data.DimletType;
import mcjty.rftoolsdim.modules.essences.EssencesConfig;
import mcjty.rftoolsdim.modules.essences.EssencesModule;
import mcjty.rftoolsdim.modules.essences.data.BlockFluidAbsorberData;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public class FluidAbsorberTileEntity extends TickingTileEntity {
   private static final int ABSORB_SPEED = 2;
   private int timer = 2;
   private final Set<BlockPos> toscan = new HashSet<>();
   private final FakePlayerGetter harvester = new FakePlayerGetter(this, "rftools_fluid_absorber");

   public FluidAbsorberTileEntity(BlockPos pos, BlockState state) {
      super(EssencesModule.TYPE_FLUID_ABSORBER.get(), pos, state);
   }

   public static BaseBlock createBlock() {
      return new BaseBlock(
         new BlockBuilder()
            .properties(Properties.of().strength(2.0F).sound(SoundType.METAL).noOcclusion())
            .tileEntitySupplier(FluidAbsorberTileEntity::new)
            .manualEntry(ManualHelper.create("rftoolsdim:dimlets/absorbers"))
            .topDriver(RFToolsDimensionsTOPDriver.DRIVER)
            .info(new InfoLine[]{TooltipBuilder.key("message.rftoolsdim.shiftmessage")})
            .infoShift(
               new InfoLine[]{
                  TooltipBuilder.header(),
                  TooltipBuilder.parameter("fluid", FluidAbsorberTileEntity::getFluidName),
                  TooltipBuilder.parameter("progress", FluidAbsorberTileEntity::getProgressName)
               }
            )
      ) {
         public RotationType getRotationType() {
            return RotationType.NONE;
         }
      };
   }

   private static String getFluidName(ItemStack stack) {
      BlockFluidAbsorberData data = (BlockFluidAbsorberData)stack.get(EssencesModule.ITEM_BLOCKFLUID_ABSORBER_DATA);
      return data != null && data.block() != null
         ? I18n.get(data.block().toLanguageKey(Registries.FLUID.identifier().getPath()).replace('/', '.'), new Object[0])
         : "<Not Set>";
   }

   public static Identifier getFluid(ItemStack stack) {
      BlockFluidAbsorberData data = (BlockFluidAbsorberData)stack.getOrDefault(EssencesModule.ITEM_BLOCKFLUID_ABSORBER_DATA, BlockFluidAbsorberData.DEFAULT);
      return data.block();
   }

   private static String getProgressName(ItemStack stack) {
      BlockFluidAbsorberData data = (BlockFluidAbsorberData)stack.get(EssencesModule.ITEM_BLOCKFLUID_ABSORBER_DATA);
      if (data == null) {
         return "n.a.";
      } else {
         int pct = ((Integer)EssencesConfig.maxFluidAbsorption.get() - data.absorbing()) * 100 / (Integer)EssencesConfig.maxFluidAbsorption.get();
         return pct + "%";
      }
   }

   public static int getProgress(ItemStack stack) {
      BlockFluidAbsorberData data = (BlockFluidAbsorberData)stack.get(EssencesModule.ITEM_BLOCKFLUID_ABSORBER_DATA);
      return data == null ? -1 : ((Integer)EssencesConfig.maxFluidAbsorption.get() - data.absorbing()) * 100 / (Integer)EssencesConfig.maxFluidAbsorption.get();
   }

   protected void tickServer() {
      BlockFluidAbsorberData data = (BlockFluidAbsorberData)this.getData(EssencesModule.BLOCKFLUID_ABSORBER_DATA);
      Identifier blockId = data.block();
      int absorbing = data.absorbing();
      if (absorbing > 0 || blockId == null) {
         this.timer--;
         if (this.timer <= 0) {
            this.timer = 2;
            BlockState b = this.isValidSourceBlock(this.getBlockPos().below());
            if (b != null) {
               if (blockId == null) {
                  absorbing = (Integer)EssencesConfig.maxFluidAbsorption.get();
                  blockId = Tools.getId(b.getBlock());
                  this.toscan.clear();
               }

               this.toscan.add(this.getBlockPos().below());
            }

            if (!this.toscan.isEmpty()) {
               int r = this.level.getRandom().nextInt(this.toscan.size());
               Iterator<BlockPos> iterator = this.toscan.iterator();
               BlockPos c = null;

               for (int i = 0; i <= r; i++) {
                  c = iterator.next();
               }

               this.toscan.remove(c);
               this.checkBlock(c, Direction.DOWN);
               this.checkBlock(c, Direction.UP);
               this.checkBlock(c, Direction.EAST);
               this.checkBlock(c, Direction.WEST);
               this.checkBlock(c, Direction.SOUTH);
               this.checkBlock(c, Direction.NORTH);
               if (this.blockMatches(c)) {
                  BlockState oldState = this.level.getBlockState(c);
                  FluidState oldFluidState = this.level.getFluidState(c);
                  SoundTools.playSound(
                     this.level,
                     oldState.getBlock().getSoundType(oldFluidState.createLegacyBlock(), this.level, c, null).getBreakSound(),
                     this.getBlockPos().getX(),
                     this.getBlockPos().getY(),
                     this.getBlockPos().getZ(),
                     1.0,
                     1.0
                  );
                  BlockPos finalC = c;
                  FluidTools.pickupFluidBlock(this.level, c, s -> true, () -> this.level.setBlock(finalC, Blocks.AIR.defaultBlockState(), 2));
                  absorbing--;
                  BlockState newState = this.level.getBlockState(c);
                  this.level.sendBlockUpdated(c, oldState, newState, 3);
               }
            }
         }

         this.setData(EssencesModule.BLOCKFLUID_ABSORBER_DATA, new BlockFluidAbsorberData(blockId, absorbing));
      }
   }

   protected void tickClient() {
      BlockFluidAbsorberData data = (BlockFluidAbsorberData)this.getData(EssencesModule.BLOCKFLUID_ABSORBER_DATA);
      if (data.absorbing() > 0) {
         RandomSource rand = this.level.getRandom();
         double u = rand.nextFloat() * 2.0F - 1.0F;
         double v = (float)(rand.nextFloat() * 2.0F * Math.PI);
         double x = Math.sqrt(1.0 - u * u) * Math.cos(v);
         double y = Math.sqrt(1.0 - u * u) * Math.sin(v);
         double r = 1.0;
         this.level
            .addParticle(
               ParticleTypes.PORTAL,
               this.getBlockPos().getX() + 0.5F + x * r,
               this.getBlockPos().getY() + 0.5F + y * r,
               this.getBlockPos().getZ() + 0.5F + u * r,
               -x,
               -y,
               -u
            );
      }
   }

   private void checkBlock(BlockPos c, Direction direction) {
      BlockPos c2 = c.relative(direction);
      if (this.blockMatches(c2)) {
         this.toscan.add(c2);
      }
   }

   private boolean blockMatches(BlockPos c) {
      FluidState state = this.level.getFluidState(c);
      if (!state.isSource()) {
         return false;
      } else {
         BlockFluidAbsorberData data = (BlockFluidAbsorberData)this.getData(EssencesModule.BLOCKFLUID_ABSORBER_DATA);
         return Objects.equals(Tools.getId(this.level.getBlockState(c).getBlock()), data.block());
      }
   }

   public int getAbsorbing() {
      BlockFluidAbsorberData data = (BlockFluidAbsorberData)this.getData(EssencesModule.BLOCKFLUID_ABSORBER_DATA);
      return data.absorbing();
   }

   @Nullable
   public Identifier getAbsorbingBlock() {
      BlockFluidAbsorberData data = (BlockFluidAbsorberData)this.getData(EssencesModule.BLOCKFLUID_ABSORBER_DATA);
      return data.block();
   }

   public Fluid getAbsorbingFluid() {
      BlockFluidAbsorberData data = (BlockFluidAbsorberData)this.getData(EssencesModule.BLOCKFLUID_ABSORBER_DATA);
      return data.block() != null ? Tools.getFluid(data.block()) : null;
   }

   private BlockState isValidSourceBlock(BlockPos coordinate) {
      if (!BlockAbsorberTileEntity.allowedToBreak(this.level.getBlockState(coordinate), this.level, coordinate, this.harvester.get())) {
         return null;
      } else {
         FluidState state = this.level.getFluidState(coordinate);
         return this.isValidDimletFluid(state) ? state.createLegacyBlock() : null;
      }
   }

   private boolean isValidDimletFluid(FluidState fluidState) {
      if (fluidState == null || fluidState.isEmpty()) {
         return false;
      } else if (!fluidState.isSource()) {
         return false;
      } else {
         Fluid fluid = fluidState.getType();
         DimletKey key = new DimletKey(DimletType.FLUID, Tools.getId(fluid).toString());
         DimletSettings settings = DimletDictionary.get().getSettings(key);
         return settings != null && settings.isDimlet();
      }
   }

   public void loadAdditional(CompoundTag tagCompound, Provider provider) {
      super.loadAdditional(tagCompound, provider);
      int[] x = tagCompound.getIntArray("toscanx").orElse(new int[0]);
      int[] y = tagCompound.getIntArray("toscany").orElse(new int[0]);
      int[] z = tagCompound.getIntArray("toscanz").orElse(new int[0]);
      this.toscan.clear();

      for (int i = 0; i < x.length; i++) {
         this.toscan.add(new BlockPos(x[i], y[i], z[i]));
      }
   }

   public void saveAdditional(@Nonnull CompoundTag tagCompound, Provider provider) {
      super.saveAdditional(tagCompound, provider);
      int[] x = new int[this.toscan.size()];
      int[] y = new int[this.toscan.size()];
      int[] z = new int[this.toscan.size()];
      int i = 0;

      for (BlockPos c : this.toscan) {
         x[i] = c.getX();
         y[i] = c.getY();
         z[i] = c.getZ();
         i++;
      }

      tagCompound.putIntArray("toscanx", x);
      tagCompound.putIntArray("toscany", y);
      tagCompound.putIntArray("toscanz", z);
   }

   protected void applyImplicitComponents(DataComponentGetter input) {
      super.applyImplicitComponents(input);
      BlockFluidAbsorberData data = (BlockFluidAbsorberData)input.get(EssencesModule.ITEM_BLOCKFLUID_ABSORBER_DATA);
      if (data != null) {
         this.setData(EssencesModule.BLOCKFLUID_ABSORBER_DATA, data);
      }
   }

   protected void collectImplicitComponents(Builder builder) {
      super.collectImplicitComponents(builder);
      BlockFluidAbsorberData data = (BlockFluidAbsorberData)this.getData(EssencesModule.BLOCKFLUID_ABSORBER_DATA);
      builder.set((DataComponentType)EssencesModule.ITEM_BLOCKFLUID_ABSORBER_DATA.get(), data);
   }
}
