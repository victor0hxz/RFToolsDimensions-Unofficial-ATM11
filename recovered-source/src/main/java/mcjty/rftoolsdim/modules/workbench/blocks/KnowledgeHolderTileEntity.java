package mcjty.rftoolsdim.modules.workbench.blocks;

import java.util.Set;
import java.util.function.Function;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.api.container.ItemInventory;
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
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsdim.modules.knowledge.data.KnowledgeKey;
import mcjty.rftoolsdim.modules.knowledge.items.LostKnowledgeItem;
import mcjty.rftoolsdim.modules.workbench.WorkbenchModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.Lazy;

public class KnowledgeHolderTileEntity extends GenericTileEntity {
   public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(
      () -> new ContainerFactory(104)
         .box(SlotDefinition.specific(KnowledgeHolderTileEntity::isValidKnowledgeItem).in().out(), 0, 11, 10, 13, 8)
         .playerSlots(11, 158)
   );
   private final GenericItemHandler items = GenericItemHandler.create(this, CONTAINER_FACTORY).itemValid((slot, stack) -> isValidKnowledgeItem(stack)).build();
   @Cap(type = CapType.ITEMS_AUTOMATION)
   private static final Function<KnowledgeHolderTileEntity, GenericItemHandler> ITEM_CAP = be -> be.items;
   @Cap(type = CapType.CONTAINER)
   private static final Function<KnowledgeHolderTileEntity, MenuProvider> SCREEN_CAP = be -> new DefaultContainerProvider("Knowledge Holder")
      .containerSupplier(DefaultContainerProvider.container(WorkbenchModule.CONTAINER_HOLDER, CONTAINER_FACTORY, be))
      .itemHandler(() -> be.items)
      .setupSync(be);

   public KnowledgeHolderTileEntity(BlockPos pos, BlockState state) {
      super(WorkbenchModule.TYPE_HOLDER.get(), pos, state);
   }

   private static boolean isValidKnowledgeItem(ItemStack stack) {
      Item item = stack.getItem();
      return item instanceof LostKnowledgeItem;
   }

   public void addKnownKnowledgeKeys(Set<KnowledgeKey> keys) {
      for (int i = 0; i < this.items.getSlots(); i++) {
         ItemStack stack = this.items.getStackInSlot(i);
         if (!stack.isEmpty()) {
            KnowledgeKey key = LostKnowledgeItem.getKnowledgeKey(stack);
            keys.add(key);
         }
      }
   }

   public static BaseBlock createBlock() {
      return new BaseBlock(
         new BlockBuilder()
            .tileEntitySupplier(KnowledgeHolderTileEntity::new)
            .infusable()
            .manualEntry(ManualHelper.create("rftoolsbase:dimlets/knowledge_holder"))
            .info(new InfoLine[]{TooltipBuilder.key("message.rftoolsdim.shiftmessage")})
            .infoShift(new InfoLine[]{TooltipBuilder.header(), TooltipBuilder.gold()})
      ) {
         public RotationType getRotationType() {
            return RotationType.ROTATION;
         }
      };
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
      this.items.applyImplicitComponents((ItemInventory)input.get((DataComponentType)Registration.ITEM_INVENTORY.get()));
   }

   protected void collectImplicitComponents(Builder builder) {
      super.collectImplicitComponents(builder);
      this.items.collectImplicitComponents(builder);
   }
}
