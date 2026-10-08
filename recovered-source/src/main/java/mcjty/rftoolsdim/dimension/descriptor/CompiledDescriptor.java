package mcjty.rftoolsdim.dimension.descriptor;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import mcjty.lib.varia.TagTools;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsdim.RFToolsDim;
import mcjty.rftoolsdim.dimension.AdminDimletType;
import mcjty.rftoolsdim.dimension.DimensionConfig;
import mcjty.rftoolsdim.dimension.TimeType;
import mcjty.rftoolsdim.dimension.additional.SkyDimletType;
import mcjty.rftoolsdim.dimension.biomes.BiomeControllerType;
import mcjty.rftoolsdim.dimension.features.FeatureType;
import mcjty.rftoolsdim.dimension.terraintypes.AttributeType;
import mcjty.rftoolsdim.dimension.terraintypes.TerrainType;
import mcjty.rftoolsdim.modules.dimlets.data.DimletDictionary;
import mcjty.rftoolsdim.modules.dimlets.data.DimletKey;
import mcjty.rftoolsdim.modules.dimlets.data.DimletSettings;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

public class CompiledDescriptor {
   private TerrainType terrainType = null;
   private final Set<AttributeType> attributeTypes = EnumSet.noneOf(AttributeType.class);
   private BlockState baseBlock = null;
   private BlockState baseLiquid = null;
   private final Set<AdminDimletType> adminDimletTypes = EnumSet.noneOf(AdminDimletType.class);
   private final Set<CompiledFeature> features = new HashSet<>();
   private BiomeControllerType biomeControllerType = null;
   private final Set<TagKey<Biome>> biomeCategories = new HashSet<>();
   private final List<Identifier> biomes = new ArrayList<>();
   private final List<Identifier> structures = new ArrayList<>();
   private long skyDimletTypes = 0L;
   private TimeType timeType = null;
   private int createCostPerTick = 0;
   private int maintainCostPerTick = 0;
   private int randomizedCostPerTick = 0;
   private int actualTickCost = 0;

   public void compile(DimensionDescriptor descriptor, DimensionDescriptor randomizedDescriptor) throws DescriptorError {
      this.createCostPerTick = 10;
      this.maintainCostPerTick = 10;
      this.randomizedCostPerTick = 0;
      this.actualTickCost = 100;
      List<Identifier> collectedTags = new ArrayList<>();
      List<BlockState> collectedBlocks = new ArrayList<>();
      List<BlockState> collectedFluids = new ArrayList<>();
      Set<AttributeType> collectedAttributes = EnumSet.noneOf(AttributeType.class);

      for (DimletKey dimlet : descriptor.getDimlets()) {
         this.handleDimlet(collectedTags, collectedBlocks, collectedFluids, collectedAttributes, dimlet);
      }

      int originalMaintainCost = this.maintainCostPerTick;
      this.maintainCostPerTick = 0;

      for (DimletKey dimlet : randomizedDescriptor.getDimlets()) {
         this.handleDimlet(collectedTags, collectedBlocks, collectedFluids, collectedAttributes, dimlet);
      }

      this.randomizedCostPerTick = this.maintainCostPerTick;
      this.maintainCostPerTick = originalMaintainCost;
      if (this.adminDimletTypes.contains(AdminDimletType.CHEATER)) {
         this.createCostPerTick = 0;
         this.maintainCostPerTick = 0;
         this.randomizedCostPerTick = 0;
         this.actualTickCost = 1;
      }

      if (!collectedBlocks.isEmpty()) {
         throw DescriptorError.ERROR(DescriptorError.Code.DANGLING_BLOCKS);
      } else if (!collectedFluids.isEmpty()) {
         throw DescriptorError.ERROR(DescriptorError.Code.DANGLING_FLUIDS);
      } else if (!collectedAttributes.isEmpty()) {
         throw DescriptorError.ERROR(DescriptorError.Code.DANGLING_ATTRIBUTES);
      } else if (!collectedTags.isEmpty()) {
         throw DescriptorError.ERROR(DescriptorError.Code.DANGLING_TAGS);
      }
   }

   public void complete() {
      if (this.timeType == null) {
         this.timeType = TimeType.NORMAL;
      }

      if (this.biomeControllerType == null) {
         this.biomeControllerType = BiomeControllerType.SINGLE;
      }

      if (this.baseBlock == null) {
         this.baseBlock = Blocks.STONE.defaultBlockState();
      }

      if (this.baseLiquid == null) {
         this.baseLiquid = Blocks.WATER.defaultBlockState();
      }
   }

   private void handleDimlet(
      List<Identifier> collectedTags,
      List<BlockState> collectedBlocks,
      List<BlockState> collectedFluids,
      Set<AttributeType> collectedAttributes,
      DimletKey dimlet
   ) throws DescriptorError {
      DimletSettings settings = DimletDictionary.get().getSettings(dimlet);
      if (settings != null) {
         this.createCostPerTick = this.createCostPerTick + settings.getCreateCost();
         this.actualTickCost = this.actualTickCost + settings.getTickCost();
         this.maintainCostPerTick = this.maintainCostPerTick + settings.getMaintainCost();
      }

      String name = dimlet.key();
      switch (dimlet.type()) {
         case TERRAIN:
            this.handleDimletTerrain(collectedTags, collectedBlocks, collectedFluids, collectedAttributes, name);
            break;
         case ATTRIBUTE:
            AttributeType type = AttributeType.byName(dimlet.key());
            if (type == null) {
               throw DescriptorError.ERROR(DescriptorError.Code.BAD_ATTRIBUTE, name);
            }

            collectedAttributes.add(type);
         case DIGIT:
         default:
            break;
         case ADMIN:
            AdminDimletType type = AdminDimletType.byName(dimlet.key());
            if (type == null) {
               throw DescriptorError.ERROR(DescriptorError.Code.BAD_ADMIN_TYPE, name);
            }

            this.adminDimletTypes.add(type);
            break;
         case BIOME_CONTROLLER:
            if (this.biomeControllerType != null) {
               throw DescriptorError.ERROR(DescriptorError.Code.ONLY_ONE_BIOME_CONTROLLER);
            }

            this.biomeControllerType = BiomeControllerType.byName(name);
            if (this.biomeControllerType == null) {
               throw DescriptorError.ERROR(DescriptorError.Code.BAD_BIOME_CONTROLLER, name);
            }
            break;
         case BIOME_CATEGORY:
            this.biomeCategories.add(TagKey.create(Registries.BIOME, Identifier.parse(name)));
            break;
         case BIOME:
            this.biomes.add(Identifier.parse(name));
            break;
         case STRUCTURE:
            this.structures.add(Identifier.parse(name));
            break;
         case SKY:
            SkyDimletType skyDimletType = SkyDimletType.byName(name);
            if (skyDimletType != null) {
               this.skyDimletTypes = this.skyDimletTypes | skyDimletType.getMask();
            }
            break;
         case TIME:
            if (this.timeType != null) {
               throw DescriptorError.ERROR(DescriptorError.Code.ONLY_ONE_TIME);
            }

            this.timeType = TimeType.byName(name);
            if (this.timeType == null) {
               throw DescriptorError.ERROR(DescriptorError.Code.BAD_TIME, name);
            }
            break;
         case FEATURE:
            this.handleDimletTerrain(collectedTags, collectedBlocks, collectedFluids, name);
            break;
         case TAG:
            collectedTags.add(Identifier.parse(name));
            break;
         case BLOCK:
            Block block = Tools.getBlock(Identifier.parse(name));
            if (block == null) {
               throw DescriptorError.ERROR(DescriptorError.Code.BAD_BLOCK, name);
            }

            collectedBlocks.add(block.defaultBlockState());
            break;
         case FLUID:
            Fluid fluid = Tools.getFluid(Identifier.parse(name));
            if (fluid == null) {
               throw DescriptorError.ERROR(DescriptorError.Code.BAD_FLUID, name);
            }

            BlockState blockState = fluid.defaultFluidState().createLegacyBlock();
            if (blockState == null || blockState.isAir()) {
               throw DescriptorError.ERROR(DescriptorError.Code.FLUID_HAS_NO_BLOCK, name);
            }

            collectedFluids.add(blockState);
      }
   }

   private void handleDimletTerrain(List<Identifier> collectedTags, List<BlockState> collectedBlocks, List<BlockState> collectedFluids, String name) throws DescriptorError {
      FeatureType feature = FeatureType.byName(name);
      if (feature == null) {
         throw DescriptorError.ERROR(DescriptorError.Code.BAD_FEATURE, name);
      } else {
         CompiledFeature compiledFeature = new CompiledFeature(feature);

         for (Identifier rl : collectedTags) {
            for (Holder<Block> holder : TagTools.getBlocksForTag(rl)) {
               collectedBlocks.add(((Block)holder.value()).defaultBlockState());
            }
         }

         collectedTags.clear();
         compiledFeature.getBlocks().addAll(collectedBlocks);
         collectedBlocks.clear();
         if (compiledFeature.getBlocks().isEmpty()) {
            compiledFeature.getBlocks().add(Blocks.STONE.defaultBlockState());
         }

         compiledFeature.getFluids().addAll(collectedFluids);
         collectedFluids.clear();
         if (compiledFeature.getFluids().isEmpty()) {
            compiledFeature.getFluids().add(Blocks.WATER.defaultBlockState());
         }

         this.features.add(compiledFeature);
      }
   }

   private void handleDimletTerrain(
      List<Identifier> collectedTags, List<BlockState> collectedBlocks, List<BlockState> collectedFluids, Set<AttributeType> collectedAttributes, String name
   ) throws DescriptorError {
      if (this.terrainType != null) {
         throw DescriptorError.ERROR(DescriptorError.Code.ONLY_ONE_TERRAIN);
      } else {
         this.terrainType = TerrainType.byName(name);
         if (this.terrainType == null) {
            throw DescriptorError.ERROR(DescriptorError.Code.BAD_TERRAIN_TYPE, name);
         } else if (!collectedTags.isEmpty()) {
            throw DescriptorError.ERROR(DescriptorError.Code.NO_TAGS);
         } else if (collectedBlocks.size() > 1) {
            throw DescriptorError.ERROR(DescriptorError.Code.ONLY_ONE_BLOCK);
         } else {
            if (collectedBlocks.isEmpty()) {
               this.baseBlock = Blocks.STONE.defaultBlockState();
            } else {
               this.baseBlock = collectedBlocks.iterator().next();
            }

            collectedBlocks.clear();
            this.attributeTypes.addAll(collectedAttributes);
            collectedAttributes.clear();
            if (collectedFluids.size() > 1) {
               throw DescriptorError.ERROR(DescriptorError.Code.ONLY_ONE_FLUID, name);
            } else {
               if (collectedFluids.size() == 1) {
                  this.baseLiquid = collectedFluids.get(0);
                  collectedFluids.clear();
               }
            }
         }
      }
   }

   public void log(String header) {
      header = "--------------------------------------------------\n" + header;
      header = header + "\n    TERRAIN: " + this.terrainType.getName();
      header = header + "\n    TIME: " + this.timeType.getName();
      header = header + "\n    SKY: " + SkyDimletType.getDescription(this.skyDimletTypes);
      header = header + "\n    LIQUID: " + Tools.getId(this.baseLiquid).toString();
      if (this.baseBlock != null) {
         header = header + "\n    BLOCK: " + Tools.getId(this.baseBlock).toString();
      }

      for (AdminDimletType type : this.adminDimletTypes) {
         header = header + "\n    ADMIN: " + type.getName();
      }

      for (AttributeType type : this.attributeTypes) {
         header = header + "\n    ATTR: " + type.getName();
      }

      header = header + "\n    BIOME CTRL: " + this.biomeControllerType.getName();

      for (Identifier biome : this.biomes) {
         header = header + "\n        BIOME: " + biome.toString();
      }

      for (TagKey<Biome> category : this.biomeCategories) {
         header = header + "\n        CATEGORY: " + category.location().toString();
      }

      for (Identifier structure : this.structures) {
         header = header + "\n    STRUCTURE: " + structure.toString();
      }

      for (CompiledFeature feature : this.features) {
         header = header + "\n    FEATURE: " + feature.getFeatureType().getName();

         for (BlockState block : feature.getBlocks()) {
            header = header + "\n        BLOCK: " + Tools.getId(block).toString();
         }

         for (BlockState fluid : feature.getFluids()) {
            header = header + "\n        LIQUID: " + Tools.getId(fluid).toString();
         }
      }

      header = header + "\n--------------------------------------------------";
      RFToolsDim.setup.getLogger().info(header);
   }

   public long getSkyDimletTypes() {
      return this.skyDimletTypes;
   }

   public TimeType getTimeType() {
      return this.timeType;
   }

   public TerrainType getTerrainType() {
      return this.terrainType;
   }

   public Set<AttributeType> getAttributeTypes() {
      return this.attributeTypes;
   }

   public List<Identifier> getStructures() {
      return this.structures;
   }

   public Set<AdminDimletType> getAdminDimletTypes() {
      return this.adminDimletTypes;
   }

   public BlockState getBaseLiquid() {
      return this.baseLiquid;
   }

   public int getCreateCostPerTick() {
      return this.createCostPerTick;
   }

   public int getActualTickCost() {
      return this.actualTickCost;
   }

   public int getActualPowerCost() {
      return this.maintainCostPerTick + (int)(this.randomizedCostPerTick * (Double)DimensionConfig.RANDOMIZED_DIMLET_COST_FACTOR.get());
   }

   public int getMaintainCostPerTick() {
      return this.maintainCostPerTick;
   }

   public BlockState getBaseBlock() {
      return this.baseBlock;
   }

   public Set<CompiledFeature> getFeatures() {
      return this.features;
   }

   public BiomeControllerType getBiomeControllerType() {
      return this.biomeControllerType;
   }

   public List<Identifier> getBiomes() {
      return this.biomes;
   }

   public Set<TagKey<Biome>> getBiomeCategories() {
      return this.biomeCategories;
   }
}
