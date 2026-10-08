package mcjty.rftoolsdim.modules.workbench.blocks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.api.container.ItemInventory;
import mcjty.lib.blockcommands.Command;
import mcjty.lib.blockcommands.ListCommand;
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
import mcjty.lib.varia.LevelTools;
import mcjty.lib.varia.OrientationTools;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsdim.modules.dimlets.DimletModule;
import mcjty.rftoolsdim.modules.dimlets.client.DimletClientHelper;
import mcjty.rftoolsdim.modules.dimlets.data.DimletDictionary;
import mcjty.rftoolsdim.modules.dimlets.data.DimletKey;
import mcjty.rftoolsdim.modules.dimlets.data.DimletTools;
import mcjty.rftoolsdim.modules.dimlets.data.DimletType;
import mcjty.rftoolsdim.modules.dimlets.items.DimletItem;
import mcjty.rftoolsdim.modules.dimlets.items.PartItem;
import mcjty.rftoolsdim.modules.knowledge.data.DimletPattern;
import mcjty.rftoolsdim.modules.knowledge.data.KnowledgeKey;
import mcjty.rftoolsdim.modules.knowledge.data.KnowledgeManager;
import mcjty.rftoolsdim.modules.workbench.WorkbenchModule;
import mcjty.rftoolsdim.modules.workbench.network.PacketPatternToClient;
import mcjty.rftoolsdim.setup.RFToolsDimMessages;
import mcjty.rftoolsdim.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public class WorkbenchTileEntity extends GenericTileEntity {
   public static final int SLOT_EMPTY_DIMLET = 0;
   public static final int SLOT_MEMORY_PART = 1;
   public static final int SLOT_ENERGY_PART = 2;
   public static final int SLOT_ESSENCE = 3;
   public static final int SLOT_OUTPUT = 4;
   public static final int SLOT_PATTERN = 5;
   public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(
      () -> new ContainerFactory(41)
         .slot(SlotDefinition.specific(DimletItem::isEmptyDimlet).in().out(), 0, 11, 7)
         .slot(SlotDefinition.specific(s -> s.getItem() instanceof PartItem).in().out(), 1, 33, 7)
         .slot(SlotDefinition.specific(s -> s.getItem() instanceof PartItem).in().out(), 2, 55, 7)
         .slot(SlotDefinition.generic().in().out(), 3, 77, 7)
         .slot(SlotDefinition.generic().out(), 4, 232, 216)
         .box(SlotDefinition.specific(WorkbenchTileEntity::isValidPatternItem).in().out(), 5, 11, 28, 6, 6)
         .playerSlots(11, 158)
   );
   private final GenericItemHandler items = GenericItemHandler.create(this, CONTAINER_FACTORY).itemValid((slot, stack) -> {
      return switch (slot) {
         case 0 -> DimletItem.isEmptyDimlet(stack);
         case 1 -> stack.getItem() instanceof PartItem;
         case 2 -> stack.getItem() instanceof PartItem;
         case 3 -> true;
         case 4 -> DimletItem.isReadyDimlet(stack);
         default -> isValidPatternItem(stack);
      };
   }).insertable(GenericItemHandler.notslot(4)).build();
   @Cap(type = CapType.ITEMS_AUTOMATION)
   private static final Function<WorkbenchTileEntity, GenericItemHandler> ITEM_CAP = be -> be.items;
   @Cap(type = CapType.CONTAINER)
   private static final Function<WorkbenchTileEntity, MenuProvider> SCREEN_CAP = be -> new DefaultContainerProvider("Dimlet Workbench")
      .containerSupplier(DefaultContainerProvider.container(WorkbenchModule.CONTAINER_WORKBENCH, CONTAINER_FACTORY, be))
      .itemHandler(() -> be.items)
      .setupSync(be);
   public static final Key<String> PARAM_TYPE = new Key("type", Type.STRING);
   public static final Key<String> PARAM_ID = new Key("id", Type.STRING);
   @ServerCommand
   public static final Command<?> CMD_SUGGESTPARTS = Command.create(
      "workbench.suggestParts",
      (te, player, params) -> te.suggestParts(player, new DimletKey(DimletType.byName((String)params.get(PARAM_TYPE)), (String)params.get(PARAM_ID)))
   );
   @ServerCommand
   public static final Command<?> CMD_CHEATDIMLET = Command.create(
      "workbench.cheatDimlet",
      (te, player, params) -> te.cheatDimlet(player, new DimletKey(DimletType.byName((String)params.get(PARAM_TYPE)), (String)params.get(PARAM_ID)))
   );
   @ServerCommand
   public static final Command<?> CMD_HILIGHT_PATTERN = Command.create(
      "workbench.hilightPattern",
      (te, player, params) -> te.hilightPattern(player, new DimletKey(DimletType.byName((String)params.get(PARAM_TYPE)), (String)params.get(PARAM_ID)))
   );
   @ServerCommand
   public static final Command<?> CMD_CREATE_DIMLET = Command.create("workbench.createDimlet", (te, player, params) -> te.createDimlet());
   @ServerCommand(type = DimletClientHelper.DimletWithInfo.class, serializer = DimletClientHelper.DimletWithInfo.Serializer.class)
   public static final ListCommand<?, ?> CMD_GETDIMLETS = ListCommand.create(
      "rftoolsdim.workbench.getdimlets", (te, player, params) -> te.getDimlets(), (te, player, params, list) -> DimletClientHelper.setDimletsOnGui(list)
   );

   public WorkbenchTileEntity(BlockPos pos, BlockState state) {
      super(WorkbenchModule.TYPE_WORKBENCH.get(), pos, state);
   }

   private static boolean isValidPatternItem(ItemStack stack) {
      Item item = stack.getItem();
      return item == Registration.DIMENSIONAL_SHARD.get()
         || item == DimletModule.COMMON_ESSENCE.get()
         || item == DimletModule.RARE_ESSENCE.get()
         || item == DimletModule.LEGENDARY_ESSENCE.get();
   }

   public static BaseBlock createBlock() {
      return new BaseBlock(
         new BlockBuilder()
            .tileEntitySupplier(WorkbenchTileEntity::new)
            .infusable()
            .manualEntry(ManualHelper.create("rftoolsbase:dimlets/dimlet_workbench"))
            .info(new InfoLine[]{TooltipBuilder.key("message.rftoolsdim.shiftmessage")})
            .infoShift(new InfoLine[]{TooltipBuilder.header(), TooltipBuilder.gold()})
      ) {
         public RotationType getRotationType() {
            return RotationType.ROTATION;
         }
      };
   }

   private boolean createDimlet() {
      ItemStack emptyDimlet = this.items.getStackInSlot(0);
      ItemStack memoryPart = this.items.getStackInSlot(1);
      ItemStack energyPart = this.items.getStackInSlot(2);
      ItemStack essenceStack = this.items.getStackInSlot(3);
      DimletType type = DimletItem.getType(emptyDimlet);
      if (type == null) {
         return false;
      } else if (!type.usesKnowledgeSystem()) {
         return false;
      } else if (memoryPart.isEmpty()) {
         return false;
      } else if (energyPart.isEmpty()) {
         return false;
      } else {
         String[] pattern = new String[6];
         int slot = 5;

         for (int y = 0; y < 6; y++) {
            StringBuilder p = new StringBuilder();

            for (int x = 0; x < 6; x++) {
               ItemStack stack = this.items.getStackInSlot(slot);
               char c = KnowledgeManager.getPatternChar(stack);
               p.append(c);
               slot++;
            }

            pattern[y] = p.toString();
         }

         DimletKey key = DimletDictionary.get().tryCraft(this.level, type, memoryPart, energyPart, essenceStack, new DimletPattern(pattern));
         if (key == null) {
            return false;
         } else {
            ItemStack dimletStack = DimletTools.getDimletStack(key);
            if (dimletStack.isEmpty()) {
               return false;
            } else {
               this.items.decrStackSize(0, 1);
               this.items.decrStackSize(1, 1);
               this.items.decrStackSize(2, 1);
               this.items.decrStackSize(3, 1);

               for (int i = 5; i <= 41; i++) {
                  this.items.decrStackSize(i, 1);
               }

               this.items.setStackInSlot(4, dimletStack);
               return true;
            }
         }
      }
   }

   private void cheatDimlet(Player player, DimletKey key) {
      ItemStack dimlet = DimletTools.getDimletStack(key);
      ItemHandlerHelper.giveItemToPlayer(player, dimlet);
   }

   private void hilightPattern(Player player, DimletKey key) {
      if (this.isCraftable(this.getSupportedKnowledgeKeys(), key)) {
         ServerLevel overworld = LevelTools.getOverworld(player.level());
         DimletPattern pattern = KnowledgeManager.get().getPattern(overworld, overworld.getSeed(), key);
         if (pattern != null) {
            String[] p = pattern.pattern();
            RFToolsDimMessages.sendToPlayer(PacketPatternToClient.create(p), player);
         }
      }
   }

   private void suggestParts(Player player, DimletKey key) {
      if (key.type().usesKnowledgeSystem()) {
         if (this.isCraftable(this.getSupportedKnowledgeKeys(), key)) {
            this.tryFindAndFitItem(player, s -> ItemStack.isSameItem(s, DimletTools.getEmptyDimletStack(key.type())), 0);
            this.tryFindAndFitItem(player, s -> ItemStack.isSameItem(s, DimletTools.getNeededMemoryPart(key)), 1);
            this.tryFindAndFitItem(player, s -> ItemStack.isSameItem(s, DimletTools.getNeededEnergyPart(key)), 2);
            ItemStack essence = DimletTools.getNeededEssence(key, DimletDictionary.get().getSettings(key));
            if (!essence.isEmpty()) {
               this.tryFindAndFitItem(player, s -> DimletTools.isFullEssence(s, essence, key.key()), 3);
            } else {
               this.tryFindAndFitItem(player, s -> false, 3);
            }

            ServerLevel overworld = LevelTools.getOverworld(this.level);
            DimletPattern pattern = KnowledgeManager.get().getPattern(overworld, overworld.getSeed(), key);
            if (pattern != null) {
               String[] p = pattern.pattern();
               int slotNumber = 5;

               for (String value : p) {
                  for (int x = 0; x < value.length(); x++) {
                     ItemStack neededPattern = KnowledgeManager.getPatternItem(value.charAt(x));
                     if (!neededPattern.isEmpty()) {
                        this.tryFindAndFitItem(player, s -> ItemStack.isSameItem(s, neededPattern), slotNumber);
                     } else {
                        this.tryFindAndFitItem(player, s -> false, slotNumber);
                     }

                     slotNumber++;
                  }
               }
            }

            player.inventoryMenu.broadcastChanges();
         }
      }
   }

   private void tryFindAndFitItem(Player player, Predicate<ItemStack> desired, int slotNumber) {
      if (!desired.test(this.items.getStackInSlot(slotNumber))) {
         if (!this.items.getStackInSlot(slotNumber).isEmpty()) {
            ItemHandlerHelper.giveItemToPlayer(player, this.items.getStackInSlot(slotNumber));
            this.items.setStackInSlot(slotNumber, ItemStack.EMPTY);
         }

         for (ItemStack itemStack : player.getInventory().getNonEquipmentItems()) {
            if (desired.test(itemStack)) {
               ItemStack copy = itemStack.copy();
               copy.setCount(1);
               this.items.setStackInSlot(slotNumber, copy);
               itemStack.shrink(1);
               return;
            }
         }
      }
   }

   protected void saveAdditional(CompoundTag tag, Provider provider) {
      super.saveAdditional(tag, provider);
      this.items.save(tag, "items", provider);
   }

   protected void loadAdditional(CompoundTag tag, Provider provider) {
      super.loadAdditional(tag, provider);
      this.items.load(tag, "items", provider);
   }

   protected void applyImplicitComponents(DataComponentGetter input) {
      super.applyImplicitComponents(input);
      this.items.applyImplicitComponents((ItemInventory)input.get((DataComponentType)mcjty.lib.setup.Registration.ITEM_INVENTORY.get()));
   }

   protected void collectImplicitComponents(Builder builder) {
      super.collectImplicitComponents(builder);
      this.items.collectImplicitComponents(builder);
   }

   private Set<KnowledgeKey> getSupportedKnowledgeKeys() {
      Set<KnowledgeKey> knownKeys = new HashSet<>();

      for (Direction direction : OrientationTools.DIRECTION_VALUES) {
         BlockEntity tileEntity = this.level.getBlockEntity(this.worldPosition.relative(direction));
         if (tileEntity instanceof KnowledgeHolderTileEntity) {
            ((KnowledgeHolderTileEntity)tileEntity).addKnownKnowledgeKeys(knownKeys);
         }
      }

      return knownKeys;
   }

   public List<DimletClientHelper.DimletWithInfo> getDimlets() {
      Set<KnowledgeKey> knownKeys = this.getSupportedKnowledgeKeys();
      List<DimletClientHelper.DimletWithInfo> dimlets = new ArrayList<>();

      for (DimletKey dimlet : DimletDictionary.get().getDimlets()) {
         boolean craftable = this.isCraftable(knownKeys, dimlet);
         dimlets.add(new DimletClientHelper.DimletWithInfo(dimlet, craftable));
      }

      return dimlets;
   }

   private boolean isCraftable(Set<KnowledgeKey> knownKeys, DimletKey dimlet) {
      ServerLevel overworld = LevelTools.getOverworld(this.level);
      KnowledgeKey kkey = KnowledgeManager.get().getKnowledgeKey(overworld, overworld.getSeed(), dimlet);
      return knownKeys.contains(kkey);
   }
}
