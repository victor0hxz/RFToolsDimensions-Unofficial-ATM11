package mcjty.rftoolsdim.modules.enscriber.blocks;

import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.blockcommands.Command;
import mcjty.lib.blockcommands.ServerCommand;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.builder.InfoLine;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericItemHandler;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.varia.ComponentFactory;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.Sync;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsdim.dimension.DimensionConfig;
import mcjty.rftoolsdim.dimension.data.DimensionCreator;
import mcjty.rftoolsdim.dimension.data.DimensionData;
import mcjty.rftoolsdim.dimension.data.PersistantDimensionManager;
import mcjty.rftoolsdim.dimension.descriptor.CompiledDescriptor;
import mcjty.rftoolsdim.dimension.descriptor.DescriptorError;
import mcjty.rftoolsdim.dimension.descriptor.DimensionDescriptor;
import mcjty.rftoolsdim.modules.dimensionbuilder.DimensionBuilderModule;
import mcjty.rftoolsdim.modules.dimensionbuilder.data.RealizedTabData;
import mcjty.rftoolsdim.modules.dimlets.data.DimletDictionary;
import mcjty.rftoolsdim.modules.dimlets.data.DimletKey;
import mcjty.rftoolsdim.modules.dimlets.data.DimletSettings;
import mcjty.rftoolsdim.modules.dimlets.data.DimletTools;
import mcjty.rftoolsdim.modules.dimlets.items.DimletItem;
import mcjty.rftoolsdim.modules.enscriber.EnscriberModule;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.Lazy;

public class EnscriberTileEntity extends GenericTileEntity {
   public static final int SLOT_DIMLETS = 0;
   public static final int SIZE_DIMLETS = 91;
   public static final int SLOT_TAB = 91;
   private DescriptorError error = DescriptorError.OK;
   private int clientErrorCode = 0;
   public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(
      () -> new ContainerFactory(92)
         .box(SlotDefinition.specific(DimletItem::isReadyDimlet), 0, 13, 7, 13, 7)
         .slot(SlotDefinition.specific(EnscriberTileEntity::isDimensionTab), 91, 13, 142)
         .playerSlots(85, 142)
   );
   private final GenericItemHandler items = GenericItemHandler.create(this, CONTAINER_FACTORY)
      .slotLimit(1)
      .itemValid((slot, stack) -> slot == 91 ? isDimensionTab(stack) : stack.getItem() instanceof DimletItem)
      .onUpdate((slot, stack) -> this.validateDimlets())
      .build();
   @Cap(type = CapType.ITEMS_AUTOMATION)
   private static final Function<EnscriberTileEntity, GenericItemHandler> ITEM_CAP = be -> be.items;
   @Cap(type = CapType.CONTAINER)
   private static final Function<EnscriberTileEntity, MenuProvider> SCREEN_CAP = be -> new DefaultContainerProvider("Enscriber")
      .containerSupplier(DefaultContainerProvider.container(EnscriberModule.CONTAINER_ENSCRIBER, CONTAINER_FACTORY, be))
      .itemHandler(() -> be.items)
      .shortListener(Sync.integer(() -> be.error.getCode().ordinal(), v -> be.clientErrorCode = v))
      .setupSync(be);
   public static final Key<String> PARAM_NAME = new Key("name", Type.STRING);
   @ServerCommand
   public static final Command<?> CMD_STORE = Command.create("enscriber.store", (te, player, params) -> te.storeDimlets(player, (String)params.get(PARAM_NAME)));
   @ServerCommand
   public static final Command<?> CMD_EXTRACT = Command.create("enscriber.extract", (te, player, params) -> te.extractDimlets());

   public EnscriberTileEntity(BlockPos pos, BlockState state) {
      super(EnscriberModule.TYPE_ENSCRIBER.get(), pos, state);
   }

   private static boolean isDimensionTab(ItemStack s) {
      return s.getItem() == DimensionBuilderModule.EMPTY_DIMENSION_TAB.get() || s.getItem() == DimensionBuilderModule.REALIZED_DIMENSION_TAB.get();
   }

   public static BaseBlock createBlock() {
      return new BaseBlock(
         new BlockBuilder()
            .tileEntitySupplier(EnscriberTileEntity::new)
            .infusable()
            .manualEntry(ManualHelper.create("rftoolsbase:dimensions/enscriber"))
            .info(new InfoLine[]{TooltipBuilder.key("message.rftoolsdim.shiftmessage")})
            .infoShift(new InfoLine[]{TooltipBuilder.header(), TooltipBuilder.gold()})
      ) {
         public RotationType getRotationType() {
            return RotationType.ROTATION;
         }
      };
   }

   public String getDimensionName() {
      ItemStack stack = this.items.getStackInSlot(91);
      if (!stack.isEmpty() && stack.getItem() == DimensionBuilderModule.REALIZED_DIMENSION_TAB.get()) {
         RealizedTabData tab = (RealizedTabData)stack.get(DimensionBuilderModule.ITEM_REALIZED_TAB_DATA);
         if (tab != null) {
            return tab.name().orElse(null);
         }
      }

      return null;
   }

   private boolean checkOwnerDimlet() {
      for (int i = 0; i < this.items.getSlots(); i++) {
         ItemStack stack = this.items.getStackInSlot(i);
         DimletKey dimletKey = DimletTools.getDimletKey(stack);
         if (DimletTools.isOwnerDimlet(dimletKey)) {
            return true;
         }
      }

      return false;
   }

   private void storeDimlets(Player player, String name) {
      if ((Boolean)DimensionConfig.OWNER_DIMLET_REQUIRED.get() && this.checkOwnerDimlet()) {
         Logging.warn(player, "You need an owner dimlet to make a dimension!");
      } else {
         DimensionDescriptor descriptor = this.convertToDimensionDescriptor(player);
         ItemStack realizedTab = this.createRealizedTab(descriptor);
         DimensionData data = PersistantDimensionManager.get(this.level).getData(descriptor);
         if (data != null) {
            name = data.getId().getPath();
            player.sendSystemMessage(
               ComponentFactory.literal(
                     "This dimension already existed! If this is what you wanted then that's fine. Otherwise you need digit dimlets to make new unique dimensions. The dimlet sequence uniquely identifies a dimension. Names can't be changed"
                  )
                  .withStyle(ChatFormatting.YELLOW)
            );
         } else if (!DimensionCreator.get().isNameAvailable(this.level, null, name)) {
            player.sendSystemMessage(ComponentFactory.literal("This name is already used by another dimension!").withStyle(ChatFormatting.YELLOW));
            return;
         }

         for (int i = 0; i < 91; i++) {
            this.items.setStackInSlot(i, ItemStack.EMPTY);
         }

         RealizedTabData tab = (RealizedTabData)realizedTab.getOrDefault(DimensionBuilderModule.ITEM_REALIZED_TAB_DATA, RealizedTabData.DEFAULT);
         realizedTab.set(DimensionBuilderModule.ITEM_REALIZED_TAB_DATA, tab.withName(name));
         this.items.setStackInSlot(91, realizedTab);
         this.setChanged();
      }
   }

   private ItemStack createRealizedTab(DimensionDescriptor descriptor) {
      ItemStack realizedTab = new ItemStack((ItemLike)DimensionBuilderModule.REALIZED_DIMENSION_TAB.get(), 1);
      RealizedTabData tab = (RealizedTabData)realizedTab.getOrDefault(DimensionBuilderModule.ITEM_REALIZED_TAB_DATA, RealizedTabData.DEFAULT);
      String compact = descriptor.compact();
      tab = tab.withDescriptor(compact);
      PersistantDimensionManager mgr = PersistantDimensionManager.get(this.level);
      DimensionData data = mgr.getData(descriptor);
      CompiledDescriptor compiledDescriptor = new CompiledDescriptor();
      if (data != null) {
         tab = tab.withTicksLeft(0);
         tab = tab.withDimension(data.getId());

         try {
            compiledDescriptor.compile(descriptor, data.getRandomizedDescriptor());
         } catch (DescriptorError var10) {
         }
      } else {
         try {
            compiledDescriptor.compile(descriptor, DimensionDescriptor.EMPTY);
         } catch (DescriptorError var9) {
         }

         tab = tab.withTicksLeft(compiledDescriptor.getActualTickCost());
      }

      tab = tab.withTickCost(compiledDescriptor.getActualTickCost());
      tab = tab.withRfCreateCost(compiledDescriptor.getCreateCostPerTick());
      tab = tab.withRfMaintainCost(compiledDescriptor.getActualPowerCost());
      realizedTab.set(DimensionBuilderModule.ITEM_REALIZED_TAB_DATA, tab);
      return realizedTab;
   }

   private DimensionDescriptor convertToDimensionDescriptor(@Nullable Player player) {
      DimensionDescriptor descriptor = new DimensionDescriptor();
      List<DimletKey> dimlets = descriptor.getDimlets();
      long forcedSeed = 0L;

      for (int i = 0; i < 91; i++) {
         ItemStack stack = this.items.getStackInSlot(i + 0);
         if (!stack.isEmpty()) {
            DimletKey key = DimletTools.getDimletKey(stack);
            DimletSettings settings = DimletDictionary.get().getSettings(key);
            if (settings != null) {
               dimlets.add(key);
               RealizedTabData tab = (RealizedTabData)stack.get(DimensionBuilderModule.ITEM_REALIZED_TAB_DATA);
               if (tab != null && tab.forcedSeed() != 0L) {
                  forcedSeed = tab.forcedSeed();
               }
            } else if (player != null) {
               Logging.warn(player, "Dimlet " + key.type().name() + "." + key.key() + " was not included in the tab because it is blacklisted");
            }
         }
      }

      return descriptor;
   }

   private void validateDimlets() {
      DimensionDescriptor descriptor = this.convertToDimensionDescriptor(null);
      CompiledDescriptor compiledDescriptor = new CompiledDescriptor();

      try {
         compiledDescriptor.compile(descriptor, DimensionDescriptor.EMPTY);
         this.error = DescriptorError.OK;
      } catch (DescriptorError var4) {
         this.error = var4;
      }
   }

   public int getClientErrorCode() {
      return this.clientErrorCode;
   }

   private void extractDimlets() {
      ItemStack realizedTab = this.items.getStackInSlot(91);
      RealizedTabData tab = (RealizedTabData)realizedTab.get(DimensionBuilderModule.ITEM_REALIZED_TAB_DATA);
      if (tab != null) {
         String descString = tab.descriptor();
         DimensionDescriptor descriptor = new DimensionDescriptor();
         descriptor.read(descString);
         List<DimletKey> dimlets = descriptor.getDimlets();
         int idx = 0;

         for (DimletKey key : dimlets) {
            boolean hasDimension = tab.dimension().isPresent();
            if (!(Boolean)DimensionConfig.OWNER_DIMLET_REQUIRED.get() || !hasDimension || !DimletTools.isOwnerDimlet(key)) {
               ItemStack dimletStack = DimletTools.getDimletStack(key);
               this.items.setStackInSlot(idx, dimletStack);
               idx++;
            }
         }
      }

      this.items.setStackInSlot(91, new ItemStack((ItemLike)DimensionBuilderModule.EMPTY_DIMENSION_TAB.get()));
      this.setChanged();
   }
}
