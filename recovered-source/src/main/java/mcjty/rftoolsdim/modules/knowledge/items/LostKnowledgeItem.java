package mcjty.rftoolsdim.modules.knowledge.items;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcjty.lib.builder.InfoLine;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.gui.ManualEntry;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.LevelTools;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsdim.modules.dimlets.data.DimletDictionary;
import mcjty.rftoolsdim.modules.dimlets.data.DimletKey;
import mcjty.rftoolsdim.modules.dimlets.data.DimletRarity;
import mcjty.rftoolsdim.modules.dimlets.data.DimletSettings;
import mcjty.rftoolsdim.modules.knowledge.KnowledgeModule;
import mcjty.rftoolsdim.modules.knowledge.data.KnowledgeKey;
import mcjty.rftoolsdim.modules.knowledge.data.KnowledgeManager;
import mcjty.rftoolsdim.modules.knowledge.data.LostKnowledgeData;
import mcjty.rftoolsdim.setup.Registration;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.Lazy;

public class LostKnowledgeItem extends Item implements ITooltipSettings {
   private final Lazy<TooltipBuilder> tooltipBuilder = Lazy.of(
      () -> new TooltipBuilder()
         .info(new InfoLine[]{TooltipBuilder.key("message.rftoolsdim.shiftmessage")})
         .infoShift(
            new InfoLine[]{
               TooltipBuilder.header(),
               TooltipBuilder.gold(),
               TooltipBuilder.parameter("pattern", this::getPatternString),
               TooltipBuilder.parameter("reason", s -> this.getReasonString(s) != null, this::getReasonString)
            }
         )
   );
   private final DimletRarity rarity;

   private String getReasonString(ItemStack stack) {
      LostKnowledgeData data = (LostKnowledgeData)stack.get(KnowledgeModule.ITEM_LOST_KNOWLEDGE_DATA);
      return data != null ? data.reason() : null;
   }

   private String getPatternString(ItemStack stack) {
      LostKnowledgeData data = (LostKnowledgeData)stack.get(KnowledgeModule.ITEM_LOST_KNOWLEDGE_DATA);
      if (data != null) {
         String pattern = data.pattern();
         KnowledgeKey kkey = KnowledgeKey.create(pattern);
         return kkey.rarity().name().toLowerCase() + " " + kkey.type().name().toLowerCase();
      } else {
         return "<Unknown>";
      }
   }

   public LostKnowledgeItem(DimletRarity rarity) {
      super(Registration.createStandardProperties());
      this.rarity = rarity;
   }

   public DimletRarity getRarity() {
      return this.rarity;
   }

   @Nullable
   public static KnowledgeKey getKnowledgeKey(ItemStack stack) {
      LostKnowledgeData data = (LostKnowledgeData)stack.get(KnowledgeModule.ITEM_LOST_KNOWLEDGE_DATA);
      if (data != null) {
         String pattern = data.pattern();
         return KnowledgeKey.create(pattern);
      } else {
         return null;
      }
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

   public static ItemStack createUnresearchedLostKnowledge(DimletRarity rarity) {
      return switch (rarity) {
         case COMMON -> new ItemStack((ItemLike)KnowledgeModule.COMMON_LOST_KNOWLEDGE.get());
         case UNCOMMON -> new ItemStack((ItemLike)KnowledgeModule.UNCOMMON_LOST_KNOWLEDGE.get());
         case RARE -> new ItemStack((ItemLike)KnowledgeModule.RARE_LOST_KNOWLEDGE.get());
         case LEGENDARY -> new ItemStack((ItemLike)KnowledgeModule.LEGENDARY_LOST_KNOWLEDGE.get());
      };
   }

   public static ItemStack createLostKnowledge(Level world, DimletKey key) {
      DimletSettings settings = DimletDictionary.get().getSettings(key);
      if (settings != null) {
         ServerLevel overworld = LevelTools.getOverworld(world);
         KnowledgeKey kkey = KnowledgeManager.get().getKnowledgeKey(overworld, overworld.getSeed(), key);
         if (kkey != null) {
            DimletRarity rarity = settings.getRarity();
            return createLostKnowledgeStack(world, rarity, kkey);
         }
      }

      return ItemStack.EMPTY;
   }

   public static ItemStack createRandomLostKnowledge(Level world, DimletRarity rarity, RandomSource random) {
      List<KnowledgeKey> patterns = KnowledgeManager.get().getKnownPatterns(world, rarity);
      if (patterns.isEmpty()) {
         return ItemStack.EMPTY;
      } else {
         KnowledgeKey kkey = patterns.get(random.nextInt(patterns.size()));
         return createLostKnowledgeStack(world, rarity, kkey);
      }
   }

   private static ItemStack createLostKnowledgeStack(Level world, DimletRarity rarity, KnowledgeKey kkey) {
      LostKnowledgeItem item = getKnowledgeItem(rarity);
      ItemStack result = new ItemStack(item);
      String reason = KnowledgeManager.get().getReason(world, kkey);
      result.set(KnowledgeModule.ITEM_LOST_KNOWLEDGE_DATA, new LostKnowledgeData(kkey.serialize(), reason));
      return result;
   }

   private static LostKnowledgeItem getKnowledgeItem(DimletRarity rarity) {
      KnowledgeModule.COMMON_LOST_KNOWLEDGE.get();

      return switch (rarity) {
         case COMMON -> (LostKnowledgeItem)KnowledgeModule.COMMON_LOST_KNOWLEDGE.get();
         case UNCOMMON -> (LostKnowledgeItem)KnowledgeModule.UNCOMMON_LOST_KNOWLEDGE.get();
         case RARE -> (LostKnowledgeItem)KnowledgeModule.RARE_LOST_KNOWLEDGE.get();
         case LEGENDARY -> (LostKnowledgeItem)KnowledgeModule.LEGENDARY_LOST_KNOWLEDGE.get();
      };
   }

   public ManualEntry getManualEntry() {
      return ManualHelper.create("rftoolsdim:dimlets/lost_knowledge");
   }
}
