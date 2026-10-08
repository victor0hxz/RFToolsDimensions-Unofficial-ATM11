package mcjty.rftoolsdim.modules.essences.blocks;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
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
import mcjty.rftoolsdim.modules.essences.data.StructureAbsorberData;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.levelgen.structure.Structure;

public class StructureAbsorberTileEntity extends TickingTileEntity {
   public StructureAbsorberTileEntity(BlockPos pos, BlockState state) {
      super(EssencesModule.TYPE_STRUCTURE_ABSORBER.get(), pos, state);
   }

   public static BaseBlock createBlock() {
      return new BaseBlock(
         new BlockBuilder()
            .properties(Properties.of().strength(2.0F).sound(SoundType.METAL).noOcclusion())
            .tileEntitySupplier(StructureAbsorberTileEntity::new)
            .topDriver(RFToolsDimensionsTOPDriver.DRIVER)
            .manualEntry(ManualHelper.create("rftoolsdim:dimlets/absorbers"))
            .info(new InfoLine[]{TooltipBuilder.key("message.rftoolsdim.shiftmessage")})
            .infoShift(
               new InfoLine[]{
                  TooltipBuilder.header(),
                  TooltipBuilder.parameter("block", StructureAbsorberTileEntity::getStructureName),
                  TooltipBuilder.parameter("progress", StructureAbsorberTileEntity::getProgressName)
               }
            )
      ) {
         public RotationType getRotationType() {
            return RotationType.NONE;
         }
      };
   }

   private static String getStructureName(ItemStack stack) {
      StructureAbsorberData data = (StructureAbsorberData)stack.get(EssencesModule.ITEM_STRUCTURE_ABSORBER_DATA);
      return data != null && data.structure() != null
         ? I18n.get(data.structure().toLanguageKey(Registries.STRUCTURE.identifier().getPath()).replace('/', '.'), new Object[0])
         : "<Not Set>";
   }

   public static Identifier getStructure(ItemStack stack) {
      StructureAbsorberData data = (StructureAbsorberData)stack.getOrDefault(EssencesModule.ITEM_STRUCTURE_ABSORBER_DATA, StructureAbsorberData.DEFAULT);
      return data.structure();
   }

   private static String getProgressName(ItemStack stack) {
      StructureAbsorberData data = (StructureAbsorberData)stack.get(EssencesModule.ITEM_STRUCTURE_ABSORBER_DATA);
      if (data == null) {
         return "n.a.";
      } else {
         int pct = ((Integer)EssencesConfig.maxStructureAbsorption.get() - data.absorbing()) * 100 / (Integer)EssencesConfig.maxStructureAbsorption.get();
         return pct + "%";
      }
   }

   public static int getProgress(ItemStack stack) {
      StructureAbsorberData data = (StructureAbsorberData)stack.get(EssencesModule.ITEM_STRUCTURE_ABSORBER_DATA);
      return data == null
         ? -1
         : ((Integer)EssencesConfig.maxStructureAbsorption.get() - data.absorbing()) * 100 / (Integer)EssencesConfig.maxStructureAbsorption.get();
   }

   protected void tickClient() {
      StructureAbsorberData data = (StructureAbsorberData)this.getData(EssencesModule.STRUCTURE_ABSORBER_DATA);
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
      StructureAbsorberData data = (StructureAbsorberData)this.getData(EssencesModule.STRUCTURE_ABSORBER_DATA);
      return data.absorbing();
   }

   public Identifier getAbsorbingStructure() {
      StructureAbsorberData data = (StructureAbsorberData)this.getData(EssencesModule.STRUCTURE_ABSORBER_DATA);
      return data.structure();
   }

   protected void tickServer() {
      StructureAbsorberData data = (StructureAbsorberData)this.getData(EssencesModule.STRUCTURE_ABSORBER_DATA);
      Identifier structureId = data.structure();
      int absorbing = data.absorbing();
      if (structureId == null) {
         ChunkPos cp = new ChunkPos(this.worldPosition.getX() >> 4, this.worldPosition.getZ() >> 4);
         Map<Structure, LongSet> references = this.level.getChunk(cp.x(), cp.z()).getAllReferences();
         List<Identifier> structures = new ArrayList<>();

         for (Entry<Structure, LongSet> entry : references.entrySet()) {
            if (!entry.getValue().isEmpty()) {
               structures.add(Tools.getId(this.level, entry.getKey()));
            }
         }

         if (!structures.isEmpty()) {
            if (structures.size() == 1) {
               structureId = structures.get(0);
            } else {
               structureId = structures.get(this.level.getRandom().nextInt(structures.size()));
            }

            absorbing = (Integer)EssencesConfig.maxStructureAbsorption.get();
         }
      }

      if (absorbing > 0) {
         if (!this.isValidStructure()) {
            return;
         }

         absorbing--;
      }

      this.setData(EssencesModule.STRUCTURE_ABSORBER_DATA, new StructureAbsorberData(structureId, absorbing));
   }

   private boolean isValidStructure() {
      StructureAbsorberData data = (StructureAbsorberData)this.getData(EssencesModule.STRUCTURE_ABSORBER_DATA);
      ChunkPos cp = new ChunkPos(this.worldPosition.getX() >> 4, this.worldPosition.getZ() >> 4);
      Map<Structure, LongSet> references = this.level.getChunk(cp.x(), cp.z()).getAllReferences();

      for (Entry<Structure, LongSet> entry : references.entrySet()) {
         if (!entry.getValue().isEmpty() && Objects.equals(data.structure(), Tools.getId(this.level, entry.getKey()).toString())) {
            return true;
         }
      }

      return false;
   }

   protected void applyImplicitComponents(DataComponentGetter input) {
      super.applyImplicitComponents(input);
      StructureAbsorberData data = (StructureAbsorberData)input.get(EssencesModule.ITEM_STRUCTURE_ABSORBER_DATA);
      if (data != null) {
         this.setData(EssencesModule.STRUCTURE_ABSORBER_DATA, data);
      }
   }

   protected void collectImplicitComponents(Builder builder) {
      super.collectImplicitComponents(builder);
      StructureAbsorberData data = (StructureAbsorberData)this.getData(EssencesModule.STRUCTURE_ABSORBER_DATA);
      builder.set((DataComponentType)EssencesModule.ITEM_STRUCTURE_ABSORBER_DATA.get(), data);
   }
}
