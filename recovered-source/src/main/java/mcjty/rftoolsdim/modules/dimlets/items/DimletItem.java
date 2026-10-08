package mcjty.rftoolsdim.modules.dimlets.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import mcjty.lib.builder.InfoLine;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.gui.ManualEntry;
import mcjty.lib.items.BaseItem;
import mcjty.lib.tooltips.ITooltipExtras;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.SafeClientTools;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsdim.dimension.data.ClientDimensionData;
import mcjty.rftoolsdim.modules.dimlets.DimletModule;
import mcjty.rftoolsdim.modules.dimlets.data.DimletDictionary;
import mcjty.rftoolsdim.modules.dimlets.data.DimletKey;
import mcjty.rftoolsdim.modules.dimlets.data.DimletSettings;
import mcjty.rftoolsdim.modules.dimlets.data.DimletTools;
import mcjty.rftoolsdim.modules.dimlets.data.DimletType;
import mcjty.rftoolsdim.modules.knowledge.data.DimletPattern;
import mcjty.rftoolsdim.modules.knowledge.data.KnowledgeManager;
import mcjty.rftoolsdim.setup.Registration;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.util.Lazy;
import org.apache.commons.lang3.tuple.Pair;

public class DimletItem extends BaseItem implements ITooltipSettings, ITooltipExtras {
   private final Lazy<TooltipBuilder> tooltipBuilder = Lazy.of(
      () -> new TooltipBuilder()
         .info(
            new InfoLine[]{
               TooltipBuilder.key("message.rftoolsdim.shiftmessage"),
               TooltipBuilder.parameter("key", DimletItem::isReadyDimlet, DimletTools::getDimletDescription)
            }
         )
         .infoShift(
            new InfoLine[]{
               TooltipBuilder.header(),
               TooltipBuilder.gold(),
               TooltipBuilder.parameter("key", DimletItem::isReadyDimlet, DimletTools::getDimletDescription),
               TooltipBuilder.parameter("rarity", DimletItem::isReadyDimlet, DimletTools::getDimletRarity),
               TooltipBuilder.parameter("cost", DimletItem::isReadyDimlet, DimletTools::getDimletCost)
            }
         )
   );
   private final DimletType type;
   private final boolean isReady;

   public DimletItem(DimletType type, boolean isReady) {
      super(Registration.createStandardProperties());
      this.type = type;
      this.isReady = isReady;
   }

   public static boolean isReadyDimlet(ItemStack stack) {
      Item item = stack.getItem();
      return isReadyDimlet(item);
   }

   private static boolean isReadyDimlet(Item item) {
      return item instanceof DimletItem ? ((DimletItem)item).isReady : false;
   }

   public static boolean isEmptyDimlet(ItemStack stack) {
      if (stack.getItem() instanceof DimletItem) {
         return stack.getItem() == DimletModule.EMPTY_DIMLET.get() ? false : !((DimletItem)stack.getItem()).isReady;
      } else {
         return false;
      }
   }

   public DimletType getType() {
      return this.type;
   }

   public static DimletType getType(ItemStack stack) {
      return stack.getItem() instanceof DimletItem ? ((DimletItem)stack.getItem()).getType() : null;
   }

   public void appendHoverText(
      @Nonnull ItemStack itemStack, TooltipContext context, TooltipDisplay display, @Nonnull Consumer<Component> output, @Nonnull TooltipFlag flags
   ) {
      List<Component> list = new ArrayList<>();

      try {
         super.appendHoverText(itemStack, context, display, output, flags);
         ((TooltipBuilder)this.tooltipBuilder.get()).makeTooltip(Tools.getId(this), itemStack, list, flags);
      } finally {
         list.forEach(output);
      }
   }

   public List<ItemStack> getItemsForTab() {
      List<ItemStack> stacks = new ArrayList<>();

      for (int i = 0; i <= 9; i++) {
         ItemStack stack = DimletTools.getDimletStack(new DimletKey(DimletType.DIGIT, String.valueOf(i)));
         stacks.add(stack);
      }

      stacks.add(DimletTools.getDimletStack(new DimletKey(DimletType.ADMIN, "owner")));
      stacks.add(DimletTools.getDimletStack(new DimletKey(DimletType.ADMIN, "cheater")));
      stacks.add(new ItemStack((ItemLike)DimletModule.EMPTY_TERRAIN_DIMLET.get()));
      stacks.add(new ItemStack((ItemLike)DimletModule.EMPTY_ATTRIBUTE_DIMLET.get()));
      stacks.add(new ItemStack((ItemLike)DimletModule.EMPTY_FEATURE_DIMLET.get()));
      stacks.add(new ItemStack((ItemLike)DimletModule.EMPTY_STRUCTURE_DIMLET.get()));
      stacks.add(new ItemStack((ItemLike)DimletModule.EMPTY_BIOME_DIMLET.get()));
      stacks.add(new ItemStack((ItemLike)DimletModule.EMPTY_BIOME_CONTROLLER_DIMLET.get()));
      stacks.add(new ItemStack((ItemLike)DimletModule.EMPTY_BIOME_CATEGORY_DIMLET.get()));
      stacks.add(new ItemStack((ItemLike)DimletModule.EMPTY_BLOCK_DIMLET.get()));
      stacks.add(new ItemStack((ItemLike)DimletModule.EMPTY_FLUID_DIMLET.get()));
      stacks.add(new ItemStack((ItemLike)DimletModule.EMPTY_TIME_DIMLET.get()));
      stacks.add(new ItemStack((ItemLike)DimletModule.EMPTY_TAG_DIMLET.get()));
      stacks.add(new ItemStack((ItemLike)DimletModule.EMPTY_SKY_DIMLET.get()));
      return stacks;
   }

   public List<Pair<ItemStack, Integer>> getItems(ItemStack stack) {
      DimletKey key = DimletTools.getDimletKey(stack);
      if (key != null) {
         List<Pair<ItemStack, Integer>> items = new ArrayList<>();
         items.add(Pair.of(DimletTools.getNeededEnergyPart(key), -2));
         items.add(Pair.of(DimletTools.getNeededMemoryPart(key), -2));
         long seed = ClientDimensionData.get().getWorldSeed();
         if (seed != -1L) {
            DimletPattern pattern = KnowledgeManager.get().getPattern(SafeClientTools.getClientWorld(), seed, key);
            addPatternItems(pattern, items);
         }

         DimletSettings settings = DimletDictionary.get().getSettings(key);
         if (settings != null) {
            ItemStack essence = DimletTools.getNeededEssence(key, settings);
            if (!essence.isEmpty()) {
               items.add(Pair.of(essence, -1));
            }
         }

         return items;
      } else {
         return Collections.emptyList();
      }
   }

   public static void addPatternItems(DimletPattern pattern, List<Pair<ItemStack, Integer>> items) {
      if (pattern != null) {
         int cnt = pattern.count('*');
         if (cnt > 0) {
            items.add(Pair.of(new ItemStack((ItemLike)Registration.DIMENSIONAL_SHARD.get(), cnt), -1));
         }

         cnt = pattern.count('0');
         if (cnt > 0) {
            items.add(Pair.of(new ItemStack((ItemLike)DimletModule.COMMON_ESSENCE.get(), cnt), -1));
         }

         cnt = pattern.count('1');
         if (cnt > 0) {
            items.add(Pair.of(new ItemStack((ItemLike)DimletModule.RARE_ESSENCE.get(), cnt), -1));
         }

         cnt = pattern.count('2');
         if (cnt > 0) {
            items.add(Pair.of(new ItemStack((ItemLike)DimletModule.LEGENDARY_ESSENCE.get(), cnt), -1));
         }
      }
   }

   public ManualEntry getManualEntry() {
      return ManualHelper.create("rftoolsdim:dimlets/dimlets");
   }
}
