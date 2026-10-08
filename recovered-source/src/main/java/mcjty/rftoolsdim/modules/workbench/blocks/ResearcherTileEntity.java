package mcjty.rftoolsdim.modules.workbench.blocks;

import java.util.function.Function;
import javax.annotation.Nonnull;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.api.container.ItemInventory;
import mcjty.lib.api.infusable.DefaultInfusable;
import mcjty.lib.api.infusable.IInfusable;
import mcjty.lib.api.power.ItemEnergy;
import mcjty.lib.bindings.GuiValue;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.builder.InfoLine;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericItemHandler;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.setup.Registration;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.lib.tileentity.TickingTileEntity;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsdim.modules.dimlets.data.DimletDictionary;
import mcjty.rftoolsdim.modules.dimlets.data.DimletKey;
import mcjty.rftoolsdim.modules.dimlets.data.DimletRarity;
import mcjty.rftoolsdim.modules.dimlets.data.DimletTools;
import mcjty.rftoolsdim.modules.dimlets.items.DimletItem;
import mcjty.rftoolsdim.modules.essences.EssencesModule;
import mcjty.rftoolsdim.modules.essences.blocks.BiomeAbsorberTileEntity;
import mcjty.rftoolsdim.modules.essences.blocks.BlockAbsorberTileEntity;
import mcjty.rftoolsdim.modules.essences.blocks.FluidAbsorberTileEntity;
import mcjty.rftoolsdim.modules.essences.blocks.StructureAbsorberTileEntity;
import mcjty.rftoolsdim.modules.knowledge.data.KnowledgeKey;
import mcjty.rftoolsdim.modules.knowledge.items.LostKnowledgeItem;
import mcjty.rftoolsdim.modules.workbench.WorkbenchConfig;
import mcjty.rftoolsdim.modules.workbench.WorkbenchModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.util.Lazy;

public class ResearcherTileEntity extends TickingTileEntity {
   public static final int SLOT_IN = 0;
   public static final int SLOT_OUT = 1;
   @GuiValue
   private int progress;
   public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(
      () -> new ContainerFactory(2)
         .slot(SlotDefinition.specific(ResearcherTileEntity::isResearchable).in(), 0, 64, 24)
         .slot(SlotDefinition.generic().out(), 1, 118, 24)
         .playerSlots(10, 70)
   );
   private final IInfusable infusable = new DefaultInfusable(this);
   @Cap(type = CapType.INFUSABLE)
   private static final Function<ResearcherTileEntity, IInfusable> INFUSABLE_CAP = be -> be.infusable;
   private final GenericEnergyStorage energyStorage = new GenericEnergyStorage(
      this, true, ((Integer)WorkbenchConfig.RESEARCHER_MAXENERGY.get()).intValue(), ((Integer)WorkbenchConfig.RESEARCHER_ENERGY_INPUT_PERTICK.get()).intValue()
   );
   @Cap(type = CapType.ENERGY)
   private static final Function<ResearcherTileEntity, GenericEnergyStorage> ENERGY_CAP = be -> be.energyStorage;
   private final GenericItemHandler items = GenericItemHandler.create(this, CONTAINER_FACTORY)
      .itemValid((slot, stack) -> isResearchable(stack))
      .insertable(GenericItemHandler.slot(0))
      .onUpdate((slot, stack) -> {
         if (slot == 0) {
            this.progress = this.getMaxProgress();
         }
      })
      .build();
   @Cap(type = CapType.ITEMS_AUTOMATION)
   private static final Function<ResearcherTileEntity, GenericItemHandler> ITEM_CAP = be -> be.items;
   @Cap(type = CapType.CONTAINER)
   private static final Function<ResearcherTileEntity, MenuProvider> SCREEN_CAP = be -> new DefaultContainerProvider("Knowledge Holder")
      .containerSupplier(DefaultContainerProvider.container(WorkbenchModule.CONTAINER_RESEARCHER, CONTAINER_FACTORY, be))
      .energyHandler(() -> be.energyStorage)
      .itemHandler(() -> be.items)
      .setupSync(be);
   public static final VoxelShape SLAB = Shapes.box(0.0, 0.0, 0.0, 1.0, 0.5, 1.0);

   public ResearcherTileEntity(BlockPos pos, BlockState state) {
      super(WorkbenchModule.TYPE_RESEARCHER.get(), pos, state);
   }

   private static boolean isResearchable(ItemStack stack) {
      Item item = stack.getItem();
      if (item instanceof LostKnowledgeItem) {
         KnowledgeKey key = LostKnowledgeItem.getKnowledgeKey(stack);
         return key == null;
      } else if (item == EssencesModule.BLOCK_ABSORBER_ITEM.get()) {
         return true;
      } else if (item == EssencesModule.FLUID_ABSORBER_ITEM.get()) {
         return true;
      } else if (item == EssencesModule.BIOME_ABSORBER_ITEM.get()) {
         return true;
      } else if (item == EssencesModule.STRUCTURE_ABSORBER_ITEM.get()) {
         return true;
      } else if (DimletItem.isReadyDimlet(stack)) {
         DimletKey dimletKey = DimletTools.getDimletKey(stack);
         return dimletKey == null ? false : dimletKey.type().usesKnowledgeSystem();
      } else {
         return false;
      }
   }

   public static BaseBlock createBlock() {
      return new BaseBlock(
         new BlockBuilder()
            .tileEntitySupplier(ResearcherTileEntity::new)
            .infusable()
            .manualEntry(ManualHelper.create("rftoolsbase:dimlets/researcher"))
            .info(new InfoLine[]{TooltipBuilder.key("message.rftoolsdim.shiftmessage")})
            .infoShift(new InfoLine[]{TooltipBuilder.header(), TooltipBuilder.gold()})
      ) {
         public RotationType getRotationType() {
            return RotationType.NONE;
         }

         @Nonnull
         public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter worldIn, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
            return ResearcherTileEntity.SLAB;
         }
      };
   }

   protected void tickServer() {
      if (this.items.getStackInSlot(1).isEmpty()) {
         long consume = (long)(((Integer)WorkbenchConfig.RESEARCHER_USE_PER_TICK.get()).intValue() / (1.0F + this.infusable.getInfusedFactor() / 3.0F));
         if (this.energyStorage.getEnergy() >= consume) {
            ItemStack stack = this.items.getStackInSlot(0);
            if (!stack.isEmpty()) {
               this.progress--;
               if (this.progress <= 0) {
                  this.progress = 0;
                  this.research();
                  this.markDirtyClient();
               }

               this.energyStorage.consumeEnergy(consume);
               this.markDirtyQuick();
            }
         }
      }
   }

   public void research() {
      ItemStack stack = this.items.getStackInSlot(0);
      if (!stack.isEmpty()) {
         Item item = stack.getItem();
         if (item instanceof LostKnowledgeItem) {
            this.researchKnowledge((LostKnowledgeItem)item);
         } else if (item == EssencesModule.BLOCK_ABSORBER_ITEM.get()) {
            this.researchBlockAbsorber(stack);
         } else if (item == EssencesModule.FLUID_ABSORBER_ITEM.get()) {
            this.researchFluidAbsorber(stack);
         } else if (item == EssencesModule.BIOME_ABSORBER_ITEM.get()) {
            this.researchBiomeAbsorber(stack);
         } else if (item == EssencesModule.STRUCTURE_ABSORBER_ITEM.get()) {
            this.researchStructureAbsorber(stack);
         } else if (DimletItem.isReadyDimlet(stack)) {
            this.researchDimlet(stack);
         }
      }
   }

   private void researchDimlet(ItemStack stack) {
      DimletKey key = DimletTools.getDimletKey(stack);
      if (key != null) {
         ItemStack researched = LostKnowledgeItem.createLostKnowledge(this.level, key);
         this.items.setStackInSlot(1, researched);
      }

      this.items.decrStackSize(0, 1);
   }

   private void researchBiomeAbsorber(ItemStack stack) {
      Identifier biomeId = BiomeAbsorberTileEntity.getBiome(stack);
      if (biomeId != null) {
         DimletKey key = DimletDictionary.get().getBiomeDimlet(biomeId.toString());
         if (key != null) {
            int absorberProgress = BiomeAbsorberTileEntity.getProgress(stack);
            if (this.level.getRandom().nextInt(100) < absorberProgress) {
               ItemStack researched = LostKnowledgeItem.createLostKnowledge(this.level, key);
               this.items.setStackInSlot(1, researched);
            }
         }
      }

      this.items.decrStackSize(0, 1);
   }

   private void researchBlockAbsorber(ItemStack stack) {
      Identifier blockId = BlockAbsorberTileEntity.getBlock(stack);
      if (blockId != null) {
         DimletKey key = DimletDictionary.get().getBlockDimlet(blockId.toString());
         if (key != null) {
            int absorberProgress = BlockAbsorberTileEntity.getProgress(stack);
            if (this.level.getRandom().nextInt(100) < absorberProgress) {
               ItemStack researched = LostKnowledgeItem.createLostKnowledge(this.level, key);
               this.items.setStackInSlot(1, researched);
            }
         }
      }

      this.items.decrStackSize(0, 1);
   }

   private void researchFluidAbsorber(ItemStack stack) {
      Identifier fluidId = FluidAbsorberTileEntity.getFluid(stack);
      if (fluidId != null) {
         DimletKey key = DimletDictionary.get().getFluidDimlet(fluidId.toString());
         if (key != null) {
            int absorberProgress = FluidAbsorberTileEntity.getProgress(stack);
            if (this.level.getRandom().nextInt(100) < absorberProgress) {
               ItemStack researched = LostKnowledgeItem.createLostKnowledge(this.level, key);
               this.items.setStackInSlot(1, researched);
            }
         }
      }

      this.items.decrStackSize(0, 1);
   }

   private void researchStructureAbsorber(ItemStack stack) {
      Identifier structureId = StructureAbsorberTileEntity.getStructure(stack);
      if (structureId != null) {
         DimletKey key = DimletDictionary.get().getStructureDimlet(structureId.toString());
         if (key != null) {
            int absorberProgress = StructureAbsorberTileEntity.getProgress(stack);
            if (this.level.getRandom().nextInt(100) < absorberProgress) {
               ItemStack researched = LostKnowledgeItem.createLostKnowledge(this.level, key);
               this.items.setStackInSlot(1, researched);
            }
         }
      }

      this.items.decrStackSize(0, 1);
   }

   private void researchKnowledge(LostKnowledgeItem item) {
      DimletRarity rarity = item.getRarity();
      ItemStack researched = LostKnowledgeItem.createRandomLostKnowledge(this.level, rarity, this.level.getRandom());
      this.items.setStackInSlot(1, researched);
      if (!this.items.getStackInSlot(0).isEmpty()) {
         this.progress = this.getMaxProgress();
      }

      this.items.decrStackSize(0, 1);
   }

   public int getProgress() {
      return this.progress;
   }

   public int getProgressPercentage() {
      int max = this.getMaxProgress();
      return (max - this.progress) * 100 / max;
   }

   private int getMaxProgress() {
      int p = (Integer)WorkbenchConfig.RESEARCH_TIME.get();
      return (int)(p / (1.0F + this.infusable.getInfusedFactor()));
   }

   public void loadAdditional(CompoundTag tag, Provider provider) {
      super.loadAdditional(tag, provider);
      this.progress = tag.getInt("progress").orElse(0);
      this.energyStorage.load(tag, "energy", provider);
      this.items.load(tag, "items", provider);
   }

   public void saveAdditional(@Nonnull CompoundTag tag, Provider provider) {
      super.saveAdditional(tag, provider);
      tag.putInt("progress", this.progress);
      this.energyStorage.save(tag, "energy", provider);
      this.items.save(tag, "items", provider);
   }

   protected void applyImplicitComponents(DataComponentGetter input) {
      super.applyImplicitComponents(input);
      this.energyStorage.applyImplicitComponents((ItemEnergy)input.get((DataComponentType)Registration.ITEM_ENERGY.get()));
      this.items.applyImplicitComponents((ItemInventory)input.get((DataComponentType)Registration.ITEM_INVENTORY.get()));
   }

   protected void collectImplicitComponents(Builder builder) {
      super.collectImplicitComponents(builder);
      this.energyStorage.collectImplicitComponents(builder);
      this.items.collectImplicitComponents(builder);
   }

   public void saveClientDataToNBT(CompoundTag tag, Provider provider) {
      this.items.save(tag, "items", provider);
   }

   public void loadClientDataFromNBT(CompoundTag tag, Provider provider) {
      this.items.load(tag, "items", provider);
   }
}
