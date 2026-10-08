package mcjty.rftoolsdim.modules.essences.blocks;

import javax.annotation.Nullable;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.builder.InfoLine;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tileentity.TickingTileEntity;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsdim.compat.RFToolsDimensionsTOPDriver;
import mcjty.rftoolsdim.modules.essences.EssencesConfig;
import mcjty.rftoolsdim.modules.essences.EssencesModule;
import mcjty.rftoolsdim.modules.essences.data.BiomeAbsorberData;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class BiomeAbsorberTileEntity extends TickingTileEntity {
   public BiomeAbsorberTileEntity(BlockPos pos, BlockState state) {
      super(EssencesModule.TYPE_BIOME_ABSORBER.get(), pos, state);
   }

   public static BaseBlock createBlock() {
      return new BaseBlock(
         new BlockBuilder()
            .properties(Properties.of().strength(2.0F).sound(SoundType.METAL).noOcclusion())
            .tileEntitySupplier(BiomeAbsorberTileEntity::new)
            .topDriver(RFToolsDimensionsTOPDriver.DRIVER)
            .manualEntry(ManualHelper.create("rftoolsdim:dimlets/absorbers"))
            .info(new InfoLine[]{TooltipBuilder.key("message.rftoolsdim.shiftmessage")})
            .infoShift(
               new InfoLine[]{
                  TooltipBuilder.header(),
                  TooltipBuilder.parameter("block", BiomeAbsorberTileEntity::getBiomeName),
                  TooltipBuilder.parameter("progress", BiomeAbsorberTileEntity::getProgressName)
               }
            )
      ) {
         public RotationType getRotationType() {
            return RotationType.NONE;
         }
      };
   }

   private static String getBiomeName(ItemStack stack) {
      BiomeAbsorberData data = (BiomeAbsorberData)stack.get(EssencesModule.ITEM_BIOME_ABSORBER_DATA);
      return data != null && data.biome() != null
         ? I18n.get(data.biome().toLanguageKey(Registries.BIOME.identifier().getPath()).replace('/', '.'), new Object[0])
         : "<Not Set>";
   }

   public static Identifier getBiome(ItemStack stack) {
      BiomeAbsorberData data = (BiomeAbsorberData)stack.getOrDefault(EssencesModule.ITEM_BIOME_ABSORBER_DATA, BiomeAbsorberData.DEFAULT);
      return data.biome();
   }

   private static String getProgressName(ItemStack stack) {
      BiomeAbsorberData data = (BiomeAbsorberData)stack.get(EssencesModule.ITEM_BIOME_ABSORBER_DATA);
      if (data == null) {
         return "n.a.";
      } else {
         int pct = ((Integer)EssencesConfig.maxBiomeAbsorption.get() - data.absorbing()) * 100 / (Integer)EssencesConfig.maxBiomeAbsorption.get();
         return pct + "%";
      }
   }

   public static int getProgress(ItemStack stack) {
      BiomeAbsorberData data = (BiomeAbsorberData)stack.get(EssencesModule.ITEM_BIOME_ABSORBER_DATA);
      return data == null ? -1 : ((Integer)EssencesConfig.maxBiomeAbsorption.get() - data.absorbing()) * 100 / (Integer)EssencesConfig.maxBiomeAbsorption.get();
   }

   protected void tickClient() {
      BiomeAbsorberData data = (BiomeAbsorberData)this.getData(EssencesModule.BIOME_ABSORBER_DATA);
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

   public int getAbsorbing() {
      BiomeAbsorberData data = (BiomeAbsorberData)this.getData(EssencesModule.BIOME_ABSORBER_DATA);
      return data.absorbing();
   }

   @Nullable
   public Identifier getAbsorbingBiome() {
      BiomeAbsorberData data = (BiomeAbsorberData)this.getData(EssencesModule.BIOME_ABSORBER_DATA);
      return data.biome();
   }

   protected void tickServer() {
      BiomeAbsorberData data = (BiomeAbsorberData)this.getData(EssencesModule.BIOME_ABSORBER_DATA);
      Identifier biomeId = data.biome();
      int absorbing = data.absorbing();
      if (biomeId == null) {
         Holder<Biome> biome = this.getLevel().getBiome(this.getBlockPos());
         biomeId = Tools.getId(this.level, (Biome)biome.value());
         absorbing = (Integer)EssencesConfig.maxBiomeAbsorption.get();
      }

      if (absorbing > 0) {
         Holder<Biome> biome = this.level.getBiome(this.worldPosition);
         if (!Tools.getId(this.level, (Biome)biome.value()).equals(biomeId)) {
            return;
         }

         absorbing--;
      }

      this.setData(EssencesModule.BIOME_ABSORBER_DATA, new BiomeAbsorberData(biomeId, absorbing));
   }

   protected void applyImplicitComponents(DataComponentGetter input) {
      super.applyImplicitComponents(input);
      BiomeAbsorberData data = (BiomeAbsorberData)input.get(EssencesModule.ITEM_BIOME_ABSORBER_DATA);
      if (data != null) {
         this.setData(EssencesModule.BIOME_ABSORBER_DATA, data);
      }
   }

   protected void collectImplicitComponents(Builder builder) {
      super.collectImplicitComponents(builder);
      BiomeAbsorberData data = (BiomeAbsorberData)this.getData(EssencesModule.BIOME_ABSORBER_DATA);
      builder.set((DataComponentType)EssencesModule.ITEM_BIOME_ABSORBER_DATA.get(), data);
   }
}
