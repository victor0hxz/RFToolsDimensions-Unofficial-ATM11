package mcjty.rftoolsdim.modules.knowledge.data;

import java.util.HashSet;
import java.util.Set;
import mcjty.lib.varia.TagTools;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags.Blocks;

public class CommonTags {
   private Set<TagKey<Block>> commonTags = null;

   private void findCommonTags() {
      if (this.commonTags == null) {
         this.commonTags = new HashSet<>();
         this.commonTags.add(BlockTags.SAND);
         this.commonTags.add(BlockTags.FENCES);
         this.commonTags.add(BlockTags.SAPLINGS);
         this.commonTags.add(BlockTags.LEAVES);
         this.commonTags.add(BlockTags.LOGS);
         this.commonTags.add(BlockTags.RAILS);
         this.commonTags.add(BlockTags.SLABS);
         this.commonTags.add(BlockTags.WOOL);
         this.commonTags.add(BlockTags.WOOL_CARPETS);
         this.commonTags.add(BlockTags.CROPS);
         this.commonTags.add(BlockTags.PLANKS);
         this.commonTags.add(BlockTags.STAIRS);
         this.commonTags.add(BlockTags.DIRT);
         this.commonTags.add(Blocks.GLASS_BLOCKS);
         this.commonTags.add(Blocks.GLASS_PANES);
         this.commonTags.add(Blocks.CHESTS);
         this.commonTags.add(Blocks.COBBLESTONES);
         this.commonTags.add(Blocks.NETHERRACKS);
         this.commonTags.add(Blocks.OBSIDIANS);
         this.commonTags.add(Blocks.GRAVELS);
         this.commonTags.add(Blocks.SANDSTONE_BLOCKS);
         this.commonTags.add(Blocks.END_STONES);
         this.commonTags.add(Blocks.STONES);
         this.commonTags.add(Blocks.ORES);
         this.commonTags.add(Blocks.ORES_COAL);
         this.commonTags.add(Blocks.ORES_DIAMOND);
         this.commonTags.add(Blocks.ORES_EMERALD);
         this.commonTags.add(Blocks.ORES_GOLD);
         this.commonTags.add(Blocks.ORES_REDSTONE);
         this.commonTags.add(Blocks.ORES_QUARTZ);
         this.commonTags.add(Blocks.ORES_IRON);
         this.commonTags.add(Blocks.ORES_LAPIS);
         this.commonTags.add(Blocks.ORES_COPPER);
         this.commonTags.add(TagTools.createBlockTagKey(Identifier.fromNamespaceAndPath("c", "ores/tin")));
         this.commonTags.add(TagTools.createBlockTagKey(Identifier.fromNamespaceAndPath("c", "ores/silver")));
         this.commonTags.add(TagTools.createBlockTagKey(Identifier.fromNamespaceAndPath("c", "ores/manganese")));
         this.commonTags.add(TagTools.createBlockTagKey(Identifier.fromNamespaceAndPath("c", "ores/platinum")));
         this.commonTags.add(Blocks.STORAGE_BLOCKS_COAL);
         this.commonTags.add(Blocks.STORAGE_BLOCKS_DIAMOND);
         this.commonTags.add(Blocks.STORAGE_BLOCKS_EMERALD);
         this.commonTags.add(Blocks.STORAGE_BLOCKS_GOLD);
         this.commonTags.add(Blocks.STORAGE_BLOCKS_REDSTONE);
         this.commonTags.add(Blocks.STORAGE_BLOCKS_IRON);
         this.commonTags.add(Blocks.STORAGE_BLOCKS_LAPIS);
         this.commonTags.add(Blocks.STORAGE_BLOCKS_COPPER);
         this.commonTags.add(TagTools.createBlockTagKey(Identifier.fromNamespaceAndPath("c", "storage_blocks/tin")));
         this.commonTags.add(TagTools.createBlockTagKey(Identifier.fromNamespaceAndPath("c", "storage_blocks/silver")));
         this.commonTags.add(TagTools.createBlockTagKey(Identifier.fromNamespaceAndPath("c", "storage_blocks/manganese")));
         this.commonTags.add(TagTools.createBlockTagKey(Identifier.fromNamespaceAndPath("c", "storage_blocks/platinum")));
      }
   }

   public boolean isCommon(TagKey<Block> id) {
      this.findCommonTags();
      return this.commonTags.contains(id);
   }

   public void clear() {
      this.commonTags = null;
   }
}
