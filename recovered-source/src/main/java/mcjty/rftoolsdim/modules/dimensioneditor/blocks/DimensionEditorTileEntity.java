package mcjty.rftoolsdim.modules.dimensioneditor.blocks;

import java.util.function.Function;
import javax.annotation.Nonnull;
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
import mcjty.lib.varia.Broadcaster;
import mcjty.lib.varia.LevelTools;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsdim.compat.RFToolsUtilityCompat;
import mcjty.rftoolsdim.dimension.data.DimensionData;
import mcjty.rftoolsdim.dimension.data.PersistantDimensionManager;
import mcjty.rftoolsdim.dimension.tools.DynamicDimensionManager;
import mcjty.rftoolsdim.modules.dimensionbuilder.DimensionBuilderModule;
import mcjty.rftoolsdim.modules.dimensionbuilder.blocks.DimensionBuilderTileEntity;
import mcjty.rftoolsdim.modules.dimensionbuilder.data.RealizedTabData;
import mcjty.rftoolsdim.modules.dimensioneditor.DimensionEditorConfig;
import mcjty.rftoolsdim.modules.dimensioneditor.DimensionEditorModule;
import mcjty.rftoolsdim.modules.dimlets.data.DimletDictionary;
import mcjty.rftoolsdim.modules.dimlets.data.DimletKey;
import mcjty.rftoolsdim.modules.dimlets.data.DimletSettings;
import mcjty.rftoolsdim.modules.dimlets.data.DimletTools;
import mcjty.rftoolsdim.modules.dimlets.data.DimletType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.neoforged.neoforge.common.util.Lazy;

public class DimensionEditorTileEntity extends TickingTileEntity {
   public static final EnumProperty<DimensionBuilderTileEntity.OperationType> OPERATIONTYPE = EnumProperty.create(
      "operationtype", DimensionBuilderTileEntity.OperationType.class
   );
   public static final int SLOT_INJECTINPUT = 0;
   public static final int SLOT_DIMENSIONTARGET = 1;
   public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(
      () -> new ContainerFactory(2)
         .slot(SlotDefinition.specific(DimensionEditorTileEntity::isValidInput).in().out(), 0, 64, 24)
         .slot(SlotDefinition.specific(DimensionBuilderTileEntity::isRealizedTab).in().out(), 1, 118, 24)
         .playerSlots(10, 70)
   );
   private final GenericItemHandler items = GenericItemHandler.create(this, CONTAINER_FACTORY)
      .itemValid((slot, stack) -> slot == 1 ? DimensionBuilderTileEntity.isRealizedTab(stack) : isValidInput(stack))
      .build();
   @Cap(type = CapType.ITEMS_AUTOMATION)
   private static final Function<DimensionEditorTileEntity, GenericItemHandler> ITEM_CAP = be -> be.items;
   private final GenericEnergyStorage energyStorage = new GenericEnergyStorage(
      this, true, ((Integer)DimensionEditorConfig.EDITOR_MAXENERGY.get()).intValue(), ((Integer)DimensionEditorConfig.EDITOR_RECEIVEPERTICK.get()).intValue()
   );
   @Cap(type = CapType.ENERGY)
   private static final Function<DimensionEditorTileEntity, GenericEnergyStorage> ENERGY_CAP = be -> be.energyStorage;
   @Cap(type = CapType.CONTAINER)
   private static final Function<DimensionEditorTileEntity, MenuProvider> SCREEN_CAP = be -> new DefaultContainerProvider("Dimension Editor")
      .containerSupplier(DefaultContainerProvider.container(DimensionEditorModule.CONTAINER_DIMENSION_EDITOR, CONTAINER_FACTORY, be))
      .itemHandler(() -> be.items)
      .energyHandler(() -> be.energyStorage)
      .setupSync(be);
   private final DefaultInfusable infusable = new DefaultInfusable(this);
   @Cap(type = CapType.INFUSABLE)
   private static final Function<DimensionEditorTileEntity, IInfusable> INFUSABLE_CAP = be -> be.infusable;
   @GuiValue
   private int editPercentage = 0;
   private int ticksLeft = -1;
   private int ticksCost = -1;
   private int rfPerTick = -1;
   private int state = 0;

   public DimensionEditorTileEntity(BlockPos pos, BlockState state) {
      super(DimensionEditorModule.TYPE_DIMENSION_EDITOR.get(), pos, state);
   }

   protected void saveAdditional(CompoundTag tag, Provider provider) {
      super.saveAdditional(tag, provider);
      this.energyStorage.save(tag, "energy", provider);
      this.items.save(tag, "items", provider);
      this.infusable.save(tag, "infusable");
   }

   protected void loadAdditional(CompoundTag tag, Provider provider) {
      super.loadAdditional(tag, provider);
      this.energyStorage.load(tag, "energy", provider);
      this.items.load(tag, "items", provider);
      this.infusable.load(tag, "infusable");
   }

   protected void applyImplicitComponents(DataComponentGetter input) {
      super.applyImplicitComponents(input);
      this.energyStorage.applyImplicitComponents((ItemEnergy)input.get((DataComponentType)Registration.ITEM_ENERGY.get()));
      this.items.applyImplicitComponents((ItemInventory)input.get((DataComponentType)Registration.ITEM_INVENTORY.get()));
      this.infusable.applyImplicitComponents((ItemInfusable)input.get((DataComponentType)Registration.ITEM_INFUSABLE.get()));
   }

   protected void collectImplicitComponents(net.minecraft.core.component.DataComponentMap.Builder builder) {
      super.collectImplicitComponents(builder);
      this.energyStorage.collectImplicitComponents(builder);
      this.items.collectImplicitComponents(builder);
      this.infusable.collectImplicitComponents(builder);
   }

   public static BaseBlock createBlock() {
      return new BaseBlock(
         new BlockBuilder()
            .tileEntitySupplier(DimensionEditorTileEntity::new)
            .infusable()
            .manualEntry(ManualHelper.create("rftoolsbase:dimensions/dimension_editor"))
            .info(new InfoLine[]{TooltipBuilder.key("message.rftoolsdim.shiftmessage")})
            .infoShift(new InfoLine[]{TooltipBuilder.header(), TooltipBuilder.gold()})
      ) {
         public RotationType getRotationType() {
            return RotationType.ROTATION;
         }

         protected void createBlockStateDefinition(@Nonnull Builder<Block, BlockState> builder) {
            super.createBlockStateDefinition(builder);
            builder.add(new Property[]{DimensionEditorTileEntity.OPERATIONTYPE});
         }
      };
   }

   private static boolean isValidInput(ItemStack s) {
      return isMatterReceiver(s) ? true : (Boolean)DimensionEditorConfig.TNT_CAN_DESTROY_DIMENSION.get() && isTNT(s);
   }

   public int getEditPercentage() {
      return this.editPercentage;
   }

   public void tickServer() {
      if (this.ticksLeft == -1) {
         this.editPercentage = 0;
      } else {
         this.editPercentage = (this.ticksCost - this.ticksLeft) * 100 / this.ticksCost;
      }

      ItemStack injectableItemStack = this.validateInjectableItemStack();
      if (!injectableItemStack.isEmpty()) {
         ItemStack dimensionItemStack = this.validateDimensionItemStack();
         if (!dimensionItemStack.isEmpty()) {
            if (this.ticksLeft == -1) {
               RealizedTabData tab = (RealizedTabData)dimensionItemStack.getOrDefault(DimensionBuilderModule.ITEM_REALIZED_TAB_DATA, RealizedTabData.DEFAULT);
               Identifier id = tab.dimension().orElse(null);
               DimensionData data = PersistantDimensionManager.get(this.level).getData(id);
               if (isMatterReceiver(injectableItemStack)) {
                  this.ticksCost = 1000;
                  this.rfPerTick = 200;
               } else if (isTNT(injectableItemStack)) {
                  this.ticksCost = 600;
                  this.rfPerTick = 10;
               } else {
                  DimletKey key = DimletTools.getDimletKey(injectableItemStack);
                  DimletSettings settings = DimletDictionary.get().getSettings(key);
                  this.ticksCost = 1000 + settings.getTickCost();
                  this.rfPerTick = 200 + settings.getCreateCost();
               }

               this.ticksLeft = this.ticksCost;
            } else {
               long rf = this.energyStorage.getEnergyStored();
               int rfpt = this.rfPerTick;
               rfpt = (int)(rfpt * (2.0F - this.infusable.getInfusedFactor()) / 2.0F);
               if (rf >= rfpt) {
                  this.energyStorage.consumeEnergy(rfpt);
                  this.ticksLeft--;
                  if (this.ticksLeft <= 0) {
                     RealizedTabData tab = (RealizedTabData)dimensionItemStack.getOrDefault(
                        DimensionBuilderModule.ITEM_REALIZED_TAB_DATA, RealizedTabData.DEFAULT
                     );
                     Identifier id = tab.dimension().orElse(null);
                     if (isMatterReceiver(injectableItemStack)) {
                        ServerLevel dimWorld = LevelTools.getLevel(this.level, LevelTools.getId(id));
                        if (dimWorld == null) {
                           Broadcaster.broadcast(
                              this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), "Dimension does not exist!", 10.0F
                           );
                        } else {
                           int y = this.findGoodReceiverLocation(dimWorld);
                           if (y == -1) {
                              y = dimWorld.getHeight() / 2;
                           }

                           if (injectableItemStack.getItem() instanceof BlockItem itemBlock) {
                              BlockState state = itemBlock.getBlock().defaultBlockState();
                              BlockPos pos = new BlockPos(8, y, 8);
                              dimWorld.setBlock(pos, state, 1);
                              Block block = dimWorld.getBlockState(pos).getBlock();
                              String name = RFToolsUtilityCompat.getReceiverName(injectableItemStack);
                              ItemEnergy itemEnergy = (ItemEnergy)injectableItemStack.get((DataComponentType)Registration.ITEM_ENERGY.get());
                              long energy = itemEnergy == null ? 0L : itemEnergy.energy();
                              RFToolsUtilityCompat.createTeleporter(dimWorld, pos, name, (int)energy);
                              block.setPlacedBy(dimWorld, pos, state, null, injectableItemStack);
                              dimWorld.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 1);
                              dimWorld.setBlock(pos.above(2), Blocks.AIR.defaultBlockState(), 1);
                           }
                        }
                     } else if (isTNT(injectableItemStack)) {
                        this.safeDeleteDimension(id, dimensionItemStack);
                     } else {
                        DimletKey var24 = DimletTools.getDimletKey(injectableItemStack);
                     }

                     this.items.decrStackSize(0, 1);
                     this.stopInjecting();
                  }
               }
            }

            this.setChanged();
            this.setState();
         }
      }
   }

   private void safeDeleteDimension(Identifier id, ItemStack dimensionTab) {
      DimensionData data = PersistantDimensionManager.get(this.level).getData(id);
      if (data != null) {
         ServerLevel dimension = LevelTools.getLevel(this.level, id);
         int chunks = dimension.getChunkSource().chunkMap.size();
         if (chunks > 0) {
            Broadcaster.broadcast(
               this.level,
               this.worldPosition.getX(),
               this.worldPosition.getY(),
               this.worldPosition.getZ(),
               "Dimension cannot be deleted. It is still in use!",
               10.0F
            );
         } else {
            if (data.getOwner() != null) {
               if (this.getOwnerUUID() == null) {
                  Broadcaster.broadcast(
                     this.level,
                     this.worldPosition.getX(),
                     this.worldPosition.getY(),
                     this.worldPosition.getZ(),
                     "This machine has no proper owner and cannot delete dimensions!",
                     10.0F
                  );
                  return;
               }

               if (!this.getOwnerUUID().equals(data.getOwner())) {
                  Broadcaster.broadcast(
                     this.level,
                     this.worldPosition.getX(),
                     this.worldPosition.getY(),
                     this.worldPosition.getZ(),
                     "This machine's owner differs from the dimensions owner!",
                     10.0F
                  );
                  return;
               }

               RFToolsUtilityCompat.teleportationManager.removeReceiverDestinations(this.level, LevelTools.getId(id));
               PersistantDimensionManager mgr = PersistantDimensionManager.get(this.level);
               mgr.forget(id);
               DynamicDimensionManager.markDimensionForUnregistration(this.level.getServer(), LevelTools.getId(id));
               Broadcaster.broadcast(
                  this.level,
                  this.worldPosition.getX(),
                  this.worldPosition.getY(),
                  this.worldPosition.getZ(),
                  "Removed dimension '" + id.toString() + "'!",
                  10.0F
               );
               RealizedTabData tab = (RealizedTabData)dimensionTab.getOrDefault(DimensionBuilderModule.ITEM_REALIZED_TAB_DATA, RealizedTabData.DEFAULT);
               int tickCost = tab.tickCost();
               tab = tab.withTicksLeft(tickCost);
               dimensionTab.set(DimensionBuilderModule.ITEM_REALIZED_TAB_DATA, tab);
            }
         }
      }
   }

   private int findGoodReceiverLocation(Level dimWorld) {
      int y = findSuitableEmptySpot(dimWorld, 8, 8);
      return y + 1;
   }

   public static int findSuitableEmptySpot(Level world, int x, int z) {
      int y = world.getHeight(Types.WORLD_SURFACE_WG, x, z);
      if (y == -1) {
         return -1;
      } else {
         if (y == 0) {
            y = 65;
         }

         if (--y > world.getHeight() - 5) {
            y = world.getHeight() / 2;
         }

         for (BlockState state = world.getBlockState(new BlockPos(x, y + 1, z)); state.liquid(); state = world.getBlockState(new BlockPos(x, y + 1, z))) {
            if (++y > world.getHeight() - 10) {
               return -1;
            }
         }

         return y;
      }
   }

   private ItemStack validateInjectableItemStack() {
      ItemStack itemStack = this.items.getStackInSlot(0);
      if (itemStack.isEmpty()) {
         this.stopInjecting();
         return ItemStack.EMPTY;
      } else if (isMatterReceiver(itemStack)) {
         return itemStack;
      } else if (isTNT(itemStack)) {
         return this.canDeleteDimension(itemStack);
      } else {
         DimletKey key = DimletTools.getDimletKey(itemStack);
         DimletType type = key.type();
         return ItemStack.EMPTY;
      }
   }

   private ItemStack canDeleteDimension(ItemStack itemStack) {
      if (!(Boolean)DimensionEditorConfig.TNT_CAN_DESTROY_DIMENSION.get()) {
         Broadcaster.broadcast(
            this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), "Players cannot delete dimensions!", 10.0F
         );
         return ItemStack.EMPTY;
      } else {
         ItemStack dimensionStack = this.items.getStackInSlot(1);
         if (dimensionStack.isEmpty()) {
            return ItemStack.EMPTY;
         } else {
            RealizedTabData tab = (RealizedTabData)dimensionStack.getOrDefault(DimensionBuilderModule.ITEM_REALIZED_TAB_DATA, RealizedTabData.DEFAULT);
            Identifier id = tab.dimension().orElse(null);
            DimensionData data = PersistantDimensionManager.get(this.level).getData(id);
            if (data == null) {
               Broadcaster.broadcast(
                  this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), "Not a valid dimension!", 10.0F
               );
               return ItemStack.EMPTY;
            } else if (this.getOwnerUUID() != null && this.getOwnerUUID().equals(data.getOwner())) {
               return itemStack;
            } else {
               Broadcaster.broadcast(
                  this.level,
                  this.worldPosition.getX(),
                  this.worldPosition.getY(),
                  this.worldPosition.getZ(),
                  "This machine's owner differs from the dimensions owner!",
                  10.0F
               );
               return ItemStack.EMPTY;
            }
         }
      }
   }

   private static boolean isMatterReceiver(ItemStack itemStack) {
      return "rftoolsutility:matter_receiver".equals(Tools.getId(itemStack).toString());
   }

   private static boolean isTNT(ItemStack itemStack) {
      return itemStack.getItem() == Items.TNT;
   }

   private ItemStack validateDimensionItemStack() {
      ItemStack itemStack = this.items.getStackInSlot(1);
      if (itemStack.isEmpty()) {
         this.stopInjecting();
         return ItemStack.EMPTY;
      } else {
         RealizedTabData tab = (RealizedTabData)itemStack.getOrDefault(DimensionBuilderModule.ITEM_REALIZED_TAB_DATA, RealizedTabData.DEFAULT);
         Identifier id = tab.dimension().orElse(null);
         DimensionData data = PersistantDimensionManager.get(this.level).getData(id);
         if (data == null) {
            this.stopInjecting();
            return ItemStack.EMPTY;
         } else {
            return itemStack;
         }
      }
   }

   private void stopInjecting() {
      this.setState();
      this.ticksLeft = -1;
      this.ticksCost = -1;
      this.rfPerTick = -1;
      this.setChanged();
   }

   private void setState() {
      int oldstate = this.state;
      if (this.ticksLeft == 0) {
         this.state = 0;
      } else if (this.ticksLeft == -1) {
         this.state = 1;
      } else if ((this.ticksLeft >> 2 & 1) == 0) {
         this.state = 2;
      } else {
         this.state = 3;
      }

      if (oldstate != this.state) {
         this.level
            .setBlock(
               this.worldPosition, (BlockState)this.getBlockState().setValue(OPERATIONTYPE, DimensionBuilderTileEntity.OperationType.values()[this.state]), 3
            );
         this.setChanged();
      }
   }
}
