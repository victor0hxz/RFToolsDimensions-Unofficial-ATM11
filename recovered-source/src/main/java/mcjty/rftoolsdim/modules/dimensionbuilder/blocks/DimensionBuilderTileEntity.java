package mcjty.rftoolsdim.modules.dimensionbuilder.blocks;

import java.util.Random;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.api.container.ItemInventory;
import mcjty.lib.api.infusable.DefaultInfusable;
import mcjty.lib.api.infusable.IInfusable;
import mcjty.lib.api.infusable.ItemInfusable;
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
import mcjty.lib.varia.Sync;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsdim.compat.RFToolsUtilityCompat;
import mcjty.rftoolsdim.dimension.data.DimensionCreator;
import mcjty.rftoolsdim.dimension.data.DimensionData;
import mcjty.rftoolsdim.dimension.data.PersistantDimensionManager;
import mcjty.rftoolsdim.dimension.descriptor.CompiledDescriptor;
import mcjty.rftoolsdim.dimension.descriptor.DimensionDescriptor;
import mcjty.rftoolsdim.dimension.power.PowerHandler;
import mcjty.rftoolsdim.modules.dimensionbuilder.DimensionBuilderConfig;
import mcjty.rftoolsdim.modules.dimensionbuilder.DimensionBuilderModule;
import mcjty.rftoolsdim.modules.dimensionbuilder.data.RealizedTabData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.Lazy;

public class DimensionBuilderTileEntity extends TickingTileEntity {
   @GuiValue
   private int errorMode = 0;
   public static final int SLOT_DIMENSION_TAB = 0;
   public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(
      () -> new ContainerFactory(1).slot(SlotDefinition.specific(DimensionBuilderTileEntity::isRealizedTab).in().out(), 0, 28, 24).playerSlots(10, 70)
   );
   private final GenericItemHandler items = GenericItemHandler.create(this, CONTAINER_FACTORY).itemValid((slot, stack) -> isRealizedTab(stack)).build();
   @Cap(type = CapType.ITEMS_AUTOMATION)
   private static final Function<DimensionBuilderTileEntity, GenericItemHandler> ITEM_CAP = be -> be.items;
   private final GenericEnergyStorage energyStorage = new GenericEnergyStorage(
      this,
      true,
      ((Integer)DimensionBuilderConfig.BUILDER_MAXENERGY.get()).intValue(),
      ((Integer)DimensionBuilderConfig.BUILDER_RECEIVEPERTICK.get()).intValue()
   );
   @Cap(type = CapType.ENERGY)
   private static final Function<DimensionBuilderTileEntity, GenericEnergyStorage> ENERGY_CAP = be -> be.energyStorage;
   @Cap(type = CapType.CONTAINER)
   private static final Function<DimensionBuilderTileEntity, MenuProvider> SCREEN_CAP = be -> new DefaultContainerProvider("Dimension Builder")
      .containerSupplier(DefaultContainerProvider.container(DimensionBuilderModule.CONTAINER_DIMENSION_BUILDER, CONTAINER_FACTORY, be))
      .itemHandler(() -> be.items)
      .energyHandler(() -> be.energyStorage)
      .integerListener(Sync.integer(be::getBuildPercentage, v -> be.clientBuildPercentage = v))
      .setupSync(be);
   private final DefaultInfusable infusableHandler = new DefaultInfusable(this);
   @Cap(type = CapType.INFUSABLE)
   private static final Function<DimensionBuilderTileEntity, IInfusable> INFUSABLE_CAP = be -> be.infusableHandler;
   private int clientBuildPercentage = 0;
   private int state = 0;
   public static final short OK = 0;
   public static final short ERROR_NOOWNER = -1;
   public static final short ERROR_TOOMANYDIMENSIONS = -2;
   public static final short ERROR_COLLISION = -3;
   private static final Random random = new Random();

   public DimensionBuilderTileEntity(BlockPos pos, BlockState state) {
      super(DimensionBuilderModule.TYPE_DIMENSION_BUILDER.get(), pos, state);
   }

   public static BaseBlock createBlock() {
      return new BaseBlock(
         new BlockBuilder()
            .tileEntitySupplier(DimensionBuilderTileEntity::new)
            .infusable()
            .manualEntry(ManualHelper.create("rftoolsbase:dimensions/dimension_builder"))
            .info(new InfoLine[]{TooltipBuilder.key("message.rftoolsdim.shiftmessage")})
            .infoShift(new InfoLine[]{TooltipBuilder.header(), TooltipBuilder.gold()})
      ) {
         public RotationType getRotationType() {
            return RotationType.ROTATION;
         }
      };
   }

   @Nullable
   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      CompoundTag nbtTag = new CompoundTag();
      this.saveClientDataToNBT(nbtTag, this.level.registryAccess());
      nbtTag.putInt("errorMode", this.errorMode);
      return ClientboundBlockEntityDataPacket.create(this);
   }

   @Nonnull
   public CompoundTag getUpdateTag(Provider provider) {
      CompoundTag updateTag = super.getUpdateTag(provider);
      updateTag.putInt("errorMode", this.errorMode);
      return updateTag;
   }

   public void tickServer() {
      RealizedTabData tab = this.hasTab();
      if (tab == null || tab.tickCost() == 0) {
         this.setState(-1);
      } else if (!this.isMachineEnabled()) {
         this.setState(-1);
      } else {
         int ticksLeft = tab.ticksLeft();
         if (ticksLeft > 0) {
            ItemStack itemStack = this.items.getStackInSlot(0);
            ticksLeft = this.createDimensionTick(itemStack, tab);
         } else {
            this.maintainDimensionTick(tab);
         }

         this.setState(ticksLeft);
      }
   }

   private void maintainDimensionTick(RealizedTabData tab) {
      if (tab.dimension().isPresent()) {
         Identifier id = tab.dimension().get();
         DimensionData data = PersistantDimensionManager.get(this.level).getData(id);
         if (data == null) {
            return;
         }

         long rf = this.energyStorage.getEnergy();
         long energy = data.getEnergy();
         long maxEnergy = PowerHandler.calculateMaxDimensionPower(id, this.level) - energy;
         if (rf > maxEnergy) {
            rf = maxEnergy;
         }

         this.energyStorage.consumeEnergy(rf);
         data.setEnergy(this.level, energy + rf);
         PersistantDimensionManager.get(this.level).save();
      }
   }

   private int createDimensionTick(ItemStack stack, RealizedTabData tab) {
      this.errorMode = 0;
      String name = tab.name().orElse("");
      DimensionCreator.get().markReservedName(this.level, this.worldPosition, name);
      int createCost = tab.rfCreateCost();
      float inf = this.infusableHandler.getInfusedFactor();
      createCost = (int)(createCost * (2.0F - inf) / 2.0F);
      if (this.isCheaterDimension(tab) || this.energyStorage.getEnergyStored() >= createCost) {
         if (!DimensionCreator.get().isNameAvailable(this.level, this.worldPosition, name)) {
            this.errorMode = -3;
            this.setChanged();
            return tab.ticksLeft();
         }

         int ticksLeft = tab.ticksLeft();
         if (this.isCheaterDimension(tab)) {
            ticksLeft = 0;
         } else {
            this.energyStorage.consumeEnergy(createCost);
            ticksLeft--;
            if (random.nextFloat() < inf) {
               if (--ticksLeft < 0) {
                  ticksLeft = 0;
               }
            }
         }

         tab = tab.withTicksLeft(ticksLeft);
         stack.set(DimensionBuilderModule.ITEM_REALIZED_TAB_DATA, tab);
         if (ticksLeft <= 0) {
            String descriptorString = tab.descriptor();
            DimensionDescriptor descriptor = new DimensionDescriptor();
            descriptor.read(descriptorString);
            DimensionDescriptor randomizedDescriptor = descriptor.createRandomizedDescriptor(random);
            if (!DimensionCreator.get().isNameAvailable(this.level, this.worldPosition, name)) {
               this.errorMode = -3;
               this.setChanged();
               return 0;
            }

            if (!DimensionCreator.get().isDescriptorAvailable(this.level, descriptor)) {
               this.errorMode = -3;
               this.setChanged();
               return 0;
            }

            long seed = random.nextLong();
            ServerLevel newworld = DimensionCreator.get()
               .createWorld((ServerLevel)this.level, name, seed, descriptor, randomizedDescriptor, this.getOwnerUUID());
            Identifier id = Identifier.fromNamespaceAndPath("rftoolsdim", name);
            RealizedTabData var15 = tab.withDimension(id);
            CompiledDescriptor compiledDescriptor = DimensionCreator.get().getCompiledDescriptor(newworld);
            tab = var15.withRfMaintainCost(compiledDescriptor.getActualPowerCost());
            stack.set(DimensionBuilderModule.ITEM_REALIZED_TAB_DATA, tab);
            this.setChanged();
            this.placeMatterReceiver(newworld, name);
         }
      }

      return tab.ticksLeft();
   }

   private void placeMatterReceiver(ServerLevel newworld, String name) {
      for (int y = newworld.getMinY() + newworld.getHeight() - 10; y >= newworld.getMinY() + 1; y--) {
         if (newworld.getBlockState(new BlockPos(8, y, 8)).getBlock() == Blocks.COMMAND_BLOCK) {
            RFToolsUtilityCompat.createTeleporter(newworld, new BlockPos(8, y, 8), name);
            return;
         }
      }

      int platformHeight = DimensionCreator.get().getPlatformHeight(newworld.dimension().identifier());
      RFToolsUtilityCompat.createTeleporter(newworld, new BlockPos(8, platformHeight, 8), name);
      newworld.setBlockAndUpdate(new BlockPos(8, platformHeight + 1, 8), Blocks.AIR.defaultBlockState());
      newworld.setBlockAndUpdate(new BlockPos(8, platformHeight + 2, 8), Blocks.AIR.defaultBlockState());
   }

   private boolean isCheaterDimension(RealizedTabData tab) {
      return false;
   }

   public DimensionBuilderTileEntity.OperationType getState() {
      return DimensionBuilderTileEntity.OperationType.values()[this.state];
   }

   private void setState(int ticksLeft) {
      int oldstate = this.state;
      if (ticksLeft == 0) {
         this.state = 0;
      } else if (ticksLeft == -1) {
         this.state = 1;
      } else if ((ticksLeft >> 2 & 1) == 0) {
         this.state = 2;
      } else {
         this.state = 3;
      }

      if (oldstate != this.state) {
         this.setChanged();
      }
   }

   public void loadClientDataFromNBT(CompoundTag tag, Provider provider) {
      this.items.load(tag, "items", provider);
   }

   public void saveClientDataToNBT(CompoundTag tag, Provider provider) {
      this.items.save(tag, "items", provider);
   }

   public RealizedTabData hasTab() {
      ItemStack itemStack = this.items.getStackInSlot(0);
      return itemStack.isEmpty() ? null : (RealizedTabData)itemStack.get(DimensionBuilderModule.ITEM_REALIZED_TAB_DATA);
   }

   public int getBuildPercentage() {
      if (this.level.isClientSide()) {
         return this.clientBuildPercentage;
      } else {
         RealizedTabData tab = this.hasTab();
         if (tab != null) {
            int ticksLeft = tab.ticksLeft();
            int tickCost = tab.tickCost();
            return tickCost == 0 ? 0 : (tickCost - ticksLeft) * 100 / tickCost;
         } else {
            return 0;
         }
      }
   }

   public int getErrorMode() {
      return this.errorMode;
   }

   public static boolean isRealizedTab(ItemStack stack) {
      return stack.getItem() == DimensionBuilderModule.REALIZED_DIMENSION_TAB.get();
   }

   protected boolean needsRedstoneMode() {
      return true;
   }

   protected void saveAdditional(CompoundTag tag, Provider provider) {
      super.saveAdditional(tag, provider);
      this.energyStorage.save(tag, "energy", provider);
      this.items.save(tag, "items", provider);
      this.infusableHandler.save(tag, "infusable");
   }

   protected void loadAdditional(CompoundTag tag, Provider provider) {
      super.loadAdditional(tag, provider);
      this.energyStorage.load(tag, "energy", provider);
      this.items.load(tag, "items", provider);
      this.infusableHandler.load(tag, "infusable");
   }

   protected void applyImplicitComponents(DataComponentGetter input) {
      super.applyImplicitComponents(input);
      this.energyStorage.applyImplicitComponents((ItemEnergy)input.get(Registration.ITEM_ENERGY));
      this.items.applyImplicitComponents((ItemInventory)input.get(Registration.ITEM_INVENTORY));
      this.infusableHandler.applyImplicitComponents((ItemInfusable)input.get(Registration.ITEM_INFUSABLE));
   }

   protected void collectImplicitComponents(Builder builder) {
      super.collectImplicitComponents(builder);
      this.energyStorage.collectImplicitComponents(builder);
      this.items.collectImplicitComponents(builder);
      this.infusableHandler.collectImplicitComponents(builder);
   }

   public static enum OperationType implements StringRepresentable {
      CHARGING("charging"),
      EMPTY("empty"),
      BUILDING1("building1"),
      BUILDING2("building2");

      private final String name;

      private OperationType(String name) {
         this.name = name;
      }

      @Nonnull
      public String getSerializedName() {
         return this.name;
      }
   }
}
