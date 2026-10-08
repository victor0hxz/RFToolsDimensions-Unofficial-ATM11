package mcjty.rftoolsdim.modules.knowledge.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import mcjty.lib.varia.LevelTools;
import mcjty.lib.varia.TagTools;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsdim.RFToolsDim;
import mcjty.rftoolsdim.dimension.TimeType;
import mcjty.rftoolsdim.dimension.additional.SkyDimletType;
import mcjty.rftoolsdim.dimension.biomes.BiomeControllerType;
import mcjty.rftoolsdim.dimension.features.FeatureType;
import mcjty.rftoolsdim.dimension.terraintypes.AttributeType;
import mcjty.rftoolsdim.dimension.terraintypes.TerrainType;
import mcjty.rftoolsdim.modules.dimlets.DimletModule;
import mcjty.rftoolsdim.modules.dimlets.data.DimletDictionary;
import mcjty.rftoolsdim.modules.dimlets.data.DimletKey;
import mcjty.rftoolsdim.modules.dimlets.data.DimletRarity;
import mcjty.rftoolsdim.modules.dimlets.data.DimletSettings;
import mcjty.rftoolsdim.setup.Registration;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.CommonLevelAccessor;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.Structure;

public class KnowledgeManager {
   private long worldSeed = -1L;
   private Map<KnowledgeKey, DimletPattern> patterns = null;
   private final Map<DimletRarity, List<KnowledgeKey>> knownPatterns = new HashMap<>();
   private final Map<KnowledgeKey, String> keyReasons = new HashMap<>();
   private static final KnowledgeManager INSTANCE = new KnowledgeManager();
   private final CommonTags commonTags = new CommonTags();

   public static KnowledgeManager get() {
      return INSTANCE;
   }

   public void clear() {
      this.commonTags.clear();
      this.keyReasons.clear();
      this.knownPatterns.clear();
      this.patterns = null;
      this.worldSeed = -1L;
   }

   private void resolve(long seed) {
      if (seed != this.worldSeed || this.patterns == null) {
         this.worldSeed = seed;
         this.patterns = RandomPatternCreator.createRandomPatterns(seed);
      }
   }

   public static ItemStack getPatternItem(char p) {
      return switch (p) {
         case ' ' -> ItemStack.EMPTY;
         case '*' -> new ItemStack((ItemLike)Registration.DIMENSIONAL_SHARD.get());
         case '0' -> new ItemStack((ItemLike)DimletModule.COMMON_ESSENCE.get());
         case '1' -> new ItemStack((ItemLike)DimletModule.RARE_ESSENCE.get());
         case '2' -> new ItemStack((ItemLike)DimletModule.LEGENDARY_ESSENCE.get());
         default -> ItemStack.EMPTY;
      };
   }

   public static char getPatternChar(ItemStack stack) {
      if (stack.isEmpty()) {
         return ' ';
      } else {
         Item item = stack.getItem();
         if (item == Registration.DIMENSIONAL_SHARD.get()) {
            return '*';
         } else if (item == DimletModule.COMMON_ESSENCE.get()) {
            return '0';
         } else if (item == DimletModule.RARE_ESSENCE.get()) {
            return '1';
         } else {
            return (char)(item == DimletModule.LEGENDARY_ESSENCE.get() ? '2' : ' ');
         }
      }
   }

   @Nullable
   private String getKnowledgeSetReason(CommonLevelAccessor level, DimletKey key) {
      return switch (key.type()) {
         case TERRAIN -> null;
         case ATTRIBUTE -> null;
         case BIOME_CONTROLLER -> null;
         case BIOME_CATEGORY -> null;
         case BIOME -> this.getReasonBiome(level, key);
         case STRUCTURE -> this.getReasonStructure(level, key);
         case FEATURE -> null;
         case SKY -> null;
         case TIME -> null;
         case DIGIT -> null;
         case ADMIN -> null;
         case BLOCK -> this.getReasonBlock(key);
         case TAG -> Identifier.parse(key.key()).getPath();
         case FLUID -> Identifier.parse(key.key()).getNamespace();
      };
   }

   @Nullable
   private String getReasonBiome(CommonLevelAccessor level, DimletKey key) {
      Identifier rl = Identifier.parse(key.key());
      Biome biome = level.registryAccess().lookupOrThrow(Registries.BIOME).get(rl).<Biome>map(Holder::value).orElse(null);
      return biome != null ? this.getMostImportantIsTag(level, rl) : null;
   }

   @Nullable
   private String getMostImportantIsTag(CommonLevelAccessor level, Identifier rl) {
      return level.registryAccess()
         .lookupOrThrow(Registries.BIOME)
         .getOrThrow(ResourceKey.create(Registries.BIOME, rl))
         .tags()
         .filter(t -> t.location().getPath().startsWith("is_"))
         .sorted(Comparator.comparing(o -> o.location().getPath()))
         .findFirst()
         .map(k -> k.location().toString())
         .orElse(null);
   }

   @Nullable
   private String getReasonStructure(CommonLevelAccessor level, DimletKey key) {
      Structure structure = level.registryAccess()
         .lookupOrThrow(Registries.STRUCTURE)
         .get(Identifier.parse(key.key()))
         .<Structure>map(Holder::value)
         .orElse(null);
      return structure != null ? Identifier.parse(key.key()).getPath() : null;
   }

   @Nullable
   private String getReasonBlock(DimletKey key) {
      TagKey<Block> tagId = this.getMostCommonTagForBlock(key);
      return tagId != null ? tagId.location().getPath() : null;
   }

   private KnowledgeSet getKnowledgeSet(CommonLevelAccessor level, DimletKey key) {
      return switch (key.type()) {
         case TERRAIN -> TerrainType.byName(key.key()).getSet();
         case ATTRIBUTE -> AttributeType.byName(key.key()).getSet();
         case BIOME_CONTROLLER -> BiomeControllerType.byName(key.key()).getSet();
         case BIOME_CATEGORY -> this.getBiomeCategoryKnowledgeSet(key);
         case BIOME -> this.getBiomeKnowledgeSet(level, key);
         case STRUCTURE -> this.getStructureKnowledgeSet(level, key);
         case FEATURE -> FeatureType.byName(key.key()).getSet();
         case SKY -> this.getSkyKnowledgeSet(key);
         case TIME -> TimeType.byName(key.key()).getSet();
         case DIGIT -> KnowledgeSet.SET1;
         case ADMIN -> KnowledgeSet.SET1;
         case BLOCK -> this.getBlockKnowledgeSet(key);
         case TAG -> this.getTagKnowledgeSet(key);
         case FLUID -> this.getFluidKnowledgeSet(key);
      };
   }

   private KnowledgeSet getSkyKnowledgeSet(DimletKey key) {
      SkyDimletType skyType = SkyDimletType.byName(key.key());
      return skyType == null ? KnowledgeSet.SET1 : skyType.getKnowledgeSet();
   }

   private KnowledgeSet getFluidKnowledgeSet(DimletKey key) {
      int i = Math.abs(Identifier.parse(key.key()).getNamespace().hashCode());
      return KnowledgeSet.values()[i % KnowledgeSet.values().length];
   }

   private KnowledgeSet getBiomeCategoryKnowledgeSet(DimletKey key) {
      return KnowledgeSet.values()[Math.abs(key.key().hashCode()) % KnowledgeSet.values().length];
   }

   private KnowledgeSet getTagKnowledgeSet(DimletKey key) {
      Identifier tagId = Identifier.parse(key.key());
      int i = Math.abs(tagId.hashCode());
      return KnowledgeSet.values()[i % KnowledgeSet.values().length];
   }

   private KnowledgeSet getBlockKnowledgeSet(DimletKey key) {
      TagKey<Block> tagId = this.getMostCommonTagForBlock(key);
      if (tagId == null) {
         return KnowledgeSet.SET1;
      } else {
         int i = Math.abs(tagId.hashCode());
         return KnowledgeSet.values()[i % KnowledgeSet.values().length];
      }
   }

   private TagKey<Block> getMostCommonTagForBlock(DimletKey key) {
      TagKey<Block> mostImportant = null;
      Block block = Tools.getBlock(Identifier.parse(key.key()));
      if (block == null) {
         RFToolsDim.setup.getLogger().error("Block '" + key.key() + "' is missing!");
      } else {
         Collection<TagKey<Block>> tags = TagTools.getTags(block);
         int maxAmount = -1;

         for (TagKey<Block> tag : tags) {
            List<Block> elements = new ArrayList<>();
            TagTools.getBlocksForTag(tag).forEach(h -> elements.add((Block)h.value()));
            int size = elements.size();
            if (this.commonTags.isCommon(tag)) {
               size += 10;
            }

            if (size > maxAmount) {
               mostImportant = tag;
               maxAmount = size;
            }
         }
      }

      return mostImportant;
   }

   private KnowledgeSet getStructureKnowledgeSet(CommonLevelAccessor level, DimletKey key) {
      Identifier id = Identifier.parse(key.key());
      return KnowledgeSet.values()[Math.abs(id.hashCode()) % KnowledgeSet.values().length];
   }

   private KnowledgeSet getBiomeKnowledgeSet(CommonLevelAccessor level, DimletKey key) {
      Identifier rl = Identifier.parse(key.key());
      Biome biome = level.registryAccess().lookupOrThrow(Registries.BIOME).get(rl).<Biome>map(Holder::value).orElse(null);
      if (biome == null) {
         RFToolsDim.setup.getLogger().error("Biome '" + key.key() + "' is missing!");
         return KnowledgeSet.SET1;
      } else {
         return KnowledgeSet.values()[Math.abs(key.key().hashCode()) % KnowledgeSet.values().length];
      }
   }

   @Nullable
   public KnowledgeKey getKnowledgeKey(CommonLevelAccessor level, long seed, DimletKey key) {
      this.resolve(seed);
      DimletSettings settings = DimletDictionary.get().getSettings(key);
      if (settings == null) {
         return null;
      } else {
         KnowledgeSet set = this.getKnowledgeSet(level, key);
         return new KnowledgeKey(key.type(), settings.getRarity(), set);
      }
   }

   @Nullable
   public DimletPattern getPattern(CommonLevelAccessor level, long seed, DimletKey key) {
      KnowledgeKey kkey = this.getKnowledgeKey(level, seed, key);
      return kkey == null ? null : this.patterns.get(kkey);
   }

   public String getReason(Level world, KnowledgeKey key) {
      this.getKnownPatterns(world, key.rarity());
      return this.keyReasons.get(key);
   }

   public List<KnowledgeKey> getKnownPatterns(Level world, DimletRarity rarity) {
      if (!this.knownPatterns.containsKey(rarity)) {
         List<KnowledgeKey> set = new ArrayList<>();

         for (DimletKey key : DimletDictionary.get().getDimlets()) {
            DimletSettings settings = DimletDictionary.get().getSettings(key);
            if (settings != null && Objects.equals(settings.getRarity(), rarity)) {
               ServerLevel overworld = LevelTools.getOverworld(world);
               KnowledgeKey kkey = this.getKnowledgeKey(overworld, overworld.getSeed(), key);
               if (kkey != null) {
                  set.add(kkey);
                  String reason = this.getKnowledgeSetReason(world, key);
                  if (reason != null) {
                     this.keyReasons.put(kkey, reason);
                  }
               }
            }
         }

         RFToolsDim.setup.getLogger().info("Patterns for rarity " + rarity.name() + ": " + set.size());
         this.knownPatterns.put(rarity, set);
      }

      return this.knownPatterns.get(rarity);
   }
}
