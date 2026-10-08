package mcjty.rftoolsdim.modules.dimlets.data;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsdim.compat.LostCityCompat;
import mcjty.rftoolsdim.dimension.AdminDimletType;
import mcjty.rftoolsdim.modules.dimlets.DimletModule;
import mcjty.rftoolsdim.modules.dimlets.items.DimletItem;
import mcjty.rftoolsdim.modules.essences.EssencesModule;
import mcjty.rftoolsdim.modules.essences.blocks.BiomeAbsorberTileEntity;
import mcjty.rftoolsdim.modules.essences.blocks.BlockAbsorberTileEntity;
import mcjty.rftoolsdim.modules.essences.blocks.FluidAbsorberTileEntity;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;

public class DimletTools {
   public static String getDimletDescription(ItemStack stack) {
      DimletKey key = getDimletKey(stack);
      return key == null ? "<Unknown>" : getReadableName(key);
   }

   public static String getDimletRarity(ItemStack stack) {
      DimletKey key = getDimletKey(stack);
      if (key == null) {
         return "<Unknown>";
      } else {
         DimletSettings settings = DimletDictionary.get().getSettings(key);
         if (settings != null) {
            DimletRarity rarity = settings.getRarity();
            return rarity.name();
         } else {
            return "<Unknown>";
         }
      }
   }

   public static String getDimletCost(ItemStack stack) {
      DimletKey key = getDimletKey(stack);
      if (key == null) {
         return "<Unknown>";
      } else {
         DimletSettings settings = DimletDictionary.get().getSettings(key);
         if (settings != null) {
            int createCost = settings.getCreateCost();
            int maintainCost = settings.getMaintainCost();
            int tickCost = settings.getTickCost();
            return "C " + createCost + ", M " + maintainCost + ", T " + tickCost;
         } else {
            return "<Unknown>";
         }
      }
   }

   private static DimletItem getDimletItem(DimletType type) {
      return switch (type) {
         case TERRAIN -> (DimletItem)DimletModule.TERRAIN_DIMLET.get();
         case ATTRIBUTE -> (DimletItem)DimletModule.ATTRIBUTE_DIMLET.get();
         case BIOME_CONTROLLER -> (DimletItem)DimletModule.BIOME_CONTROLLER_DIMLET.get();
         case BIOME_CATEGORY -> (DimletItem)DimletModule.BIOME_CATEGORY_DIMLET.get();
         case BIOME -> (DimletItem)DimletModule.BIOME_DIMLET.get();
         case FEATURE -> (DimletItem)DimletModule.FEATURE_DIMLET.get();
         case STRUCTURE -> (DimletItem)DimletModule.STRUCTURE_DIMLET.get();
         case TIME -> (DimletItem)DimletModule.TIME_DIMLET.get();
         case BLOCK -> (DimletItem)DimletModule.BLOCK_DIMLET.get();
         case TAG -> (DimletItem)DimletModule.TAG_DIMLET.get();
         case FLUID -> (DimletItem)DimletModule.FLUID_DIMLET.get();
         case DIGIT -> (DimletItem)DimletModule.DIGIT_DIMLET.get();
         case ADMIN -> (DimletItem)DimletModule.ADMIN_DIMLET.get();
         case SKY -> (DimletItem)DimletModule.SKY_DIMLET.get();
      };
   }

   private static DimletItem getEmptyDimletItem(DimletType type) {
      return switch (type) {
         case TERRAIN -> (DimletItem)DimletModule.EMPTY_TERRAIN_DIMLET.get();
         case ATTRIBUTE -> (DimletItem)DimletModule.EMPTY_ATTRIBUTE_DIMLET.get();
         case BIOME_CONTROLLER -> (DimletItem)DimletModule.EMPTY_BIOME_CONTROLLER_DIMLET.get();
         case BIOME_CATEGORY -> (DimletItem)DimletModule.EMPTY_BIOME_CATEGORY_DIMLET.get();
         case BIOME -> (DimletItem)DimletModule.EMPTY_BIOME_DIMLET.get();
         case FEATURE -> (DimletItem)DimletModule.EMPTY_FEATURE_DIMLET.get();
         case STRUCTURE -> (DimletItem)DimletModule.EMPTY_STRUCTURE_DIMLET.get();
         case TIME -> (DimletItem)DimletModule.EMPTY_TIME_DIMLET.get();
         case BLOCK -> (DimletItem)DimletModule.EMPTY_BLOCK_DIMLET.get();
         case TAG -> (DimletItem)DimletModule.EMPTY_TAG_DIMLET.get();
         case FLUID -> (DimletItem)DimletModule.EMPTY_FLUID_DIMLET.get();
         case DIGIT -> null;
         case ADMIN -> null;
         case SKY -> (DimletItem)DimletModule.EMPTY_SKY_DIMLET.get();
      };
   }

   @Nullable
   public static DimletKey getDimletKey(ItemStack stack) {
      if (stack.getItem() instanceof DimletItem) {
         DimletType type = ((DimletItem)stack.getItem()).getType();
         if (type != null) {
            DimletData data = (DimletData)stack.get(DimletModule.ITEM_DIMLET_DATA);
            if (data != null) {
               String name = data.name();
               return new DimletKey(type, name);
            }
         }
      }

      return null;
   }

   @Nonnull
   public static ItemStack getDimletStack(DimletKey key) {
      DimletItem item = getDimletItem(key.type());
      ItemStack stack = new ItemStack(item);
      stack.set(DimletModule.ITEM_DIMLET_DATA, new DimletData(key.key()));
      return stack;
   }

   @Nonnull
   public static ItemStack getEmptyDimletStack(DimletType type) {
      DimletItem item = getEmptyDimletItem(type);
      return new ItemStack(item);
   }

   public static ItemStack getNeededMemoryPart(DimletKey key) {
      DimletSettings settings = DimletDictionary.get().getSettings(key);
      if (settings == null) {
         return ItemStack.EMPTY;
      } else {
         DimletRarity rarity = settings.getRarity();
         if (rarity == null) {
            return ItemStack.EMPTY;
         } else {
            return switch (rarity) {
               case COMMON -> new ItemStack((ItemLike)DimletModule.PART_MEMORY_0.get());
               case UNCOMMON -> new ItemStack((ItemLike)DimletModule.PART_MEMORY_1.get());
               case RARE -> new ItemStack((ItemLike)DimletModule.PART_MEMORY_2.get());
               case LEGENDARY -> new ItemStack((ItemLike)DimletModule.PART_MEMORY_3.get());
            };
         }
      }
   }

   public static ItemStack getNeededEnergyPart(DimletKey key) {
      DimletSettings settings = DimletDictionary.get().getSettings(key);
      if (settings == null) {
         return ItemStack.EMPTY;
      } else {
         DimletRarity rarity = settings.getRarity();
         if (rarity == null) {
            return ItemStack.EMPTY;
         } else {
            return switch (rarity) {
               case COMMON -> new ItemStack((ItemLike)DimletModule.PART_ENERGY_0.get());
               case UNCOMMON -> new ItemStack((ItemLike)DimletModule.PART_ENERGY_1.get());
               case RARE -> new ItemStack((ItemLike)DimletModule.PART_ENERGY_2.get());
               case LEGENDARY -> new ItemStack((ItemLike)DimletModule.PART_ENERGY_3.get());
            };
         }
      }
   }

   public static ItemStack getNeededEssence(DimletKey key, @Nonnull DimletSettings settings) {
      if (!settings.getEssence().isEmpty()) {
         return settings.getEssence();
      } else {
         return switch (key.type()) {
            case TERRAIN -> ItemStack.EMPTY;
            case ATTRIBUTE -> ItemStack.EMPTY;
            case BIOME_CONTROLLER -> ItemStack.EMPTY;
            case BIOME_CATEGORY -> ItemStack.EMPTY;
            case BIOME -> new ItemStack((ItemLike)EssencesModule.BIOME_ABSORBER_ITEM.get());
            case FEATURE -> ItemStack.EMPTY;
            case STRUCTURE -> new ItemStack((ItemLike)EssencesModule.STRUCTURE_ABSORBER_ITEM.get());
            case TIME -> ItemStack.EMPTY;
            case BLOCK -> new ItemStack((ItemLike)EssencesModule.BLOCK_ABSORBER_ITEM.get());
            case TAG -> ItemStack.EMPTY;
            case FLUID -> new ItemStack((ItemLike)EssencesModule.FLUID_ABSORBER_ITEM.get());
            case DIGIT -> ItemStack.EMPTY;
            case ADMIN -> ItemStack.EMPTY;
            case SKY -> ItemStack.EMPTY;
         };
      }
   }

   public static String getReadableName(DimletKey dimletKey) {
      return switch (dimletKey.type()) {
         case TERRAIN -> dimletKey.key().toLowerCase();
         case ATTRIBUTE -> dimletKey.key().toLowerCase();
         case BIOME_CONTROLLER -> dimletKey.key().toLowerCase();
         case BIOME_CATEGORY -> dimletKey.key().toLowerCase();
         case BIOME -> getReadableNameBiome(dimletKey);
         case FEATURE -> dimletKey.key().toLowerCase();
         case STRUCTURE -> Identifier.parse(dimletKey.key()).getPath();
         case TIME -> dimletKey.key().toLowerCase();
         case BLOCK -> getReadableNameBlock(dimletKey);
         case TAG -> dimletKey.key().toLowerCase();
         case FLUID -> getReadableNameFluid(dimletKey);
         case DIGIT -> dimletKey.key();
         case ADMIN -> dimletKey.key();
         case SKY -> dimletKey.key().toLowerCase();
      };
   }

   @NotNull
   private static String getReadableNameFluid(DimletKey dimletKey) {
      Fluid fluid = Tools.getFluid(Identifier.parse(dimletKey.key()));
      if (fluid != null) {
         String modName = Tools.getModName(fluid);
         return "minecraft".equalsIgnoreCase(modName)
            ? I18n.get(fluid.defaultFluidState().createLegacyBlock().getBlock().getDescriptionId(), new Object[0])
            : I18n.get(fluid.defaultFluidState().createLegacyBlock().getBlock().getDescriptionId(), new Object[0]) + " (" + modName + ")";
      } else {
         return "<Invalid " + dimletKey.key() + ">";
      }
   }

   @NotNull
   private static String getReadableNameBlock(DimletKey dimletKey) {
      Block block = Tools.getBlock(Identifier.parse(dimletKey.key()));
      if (block != null) {
         String modName = Tools.getModName(block);
         return "minecraft".equalsIgnoreCase(modName)
            ? I18n.get(block.getDescriptionId(), new Object[0])
            : I18n.get(block.getDescriptionId(), new Object[0]) + " (" + modName + ")";
      } else {
         return "<Invalid " + dimletKey.key() + ">";
      }
   }

   @NotNull
   private static String getReadableNameBiome(DimletKey dimletKey) {
      Identifier id = Identifier.parse(dimletKey.key());
      String trans = "biome." + id.getNamespace() + "." + id.getPath();
      return I18n.get(trans, new Object[0]);
   }

   public static boolean isFullEssence(ItemStack stack, ItemStack desired, String desiredKey) {
      if (ItemStack.isSameItem(stack, desired)) {
         if (stack.getItem() == EssencesModule.BIOME_ABSORBER_ITEM.get()) {
            Identifier biome = BiomeAbsorberTileEntity.getBiome(stack);
            if (Objects.equals(desiredKey, biome == null ? null : biome.toString())) {
               return BiomeAbsorberTileEntity.getProgress(stack) >= 100;
            }
         } else if (stack.getItem() == EssencesModule.BLOCK_ABSORBER_ITEM.get()) {
            Identifier block = BlockAbsorberTileEntity.getBlock(stack);
            if (Objects.equals(desiredKey, block == null ? null : block.toString())) {
               return BlockAbsorberTileEntity.getProgress(stack) >= 100;
            }
         } else {
            if (stack.getItem() != EssencesModule.FLUID_ABSORBER_ITEM.get()) {
               return true;
            }

            Identifier fluid = FluidAbsorberTileEntity.getFluid(stack);
            if (Objects.equals(desiredKey, fluid == null ? null : fluid.toString())) {
               return FluidAbsorberTileEntity.getProgress(stack) >= 100;
            }
         }
      }

      return false;
   }

   public static boolean isOwnerDimlet(DimletKey dimletKey) {
      return dimletKey != null && dimletKey.type() == DimletType.ADMIN && dimletKey.key().equals(AdminDimletType.OWNER.name().toLowerCase());
   }

   public static boolean isValidDimlet(RegistryAccess access, DimletKey key) {
      return switch (key.type()) {
         case BIOME -> isValidBiome(access, key);
         default -> true;
         case BLOCK -> isValidBlock(key);
         case FLUID -> isValidFluid(key);
         case ADMIN -> isValidAttribute(key);
      };
   }

   private static boolean isValidAttribute(DimletKey key) {
      return "cities".equals(key.key()) ? LostCityCompat.hasLostCities() : true;
   }

   private static boolean isValidBiome(RegistryAccess access, DimletKey key) {
      return access.lookupOrThrow(Registries.BIOME).containsKey(Identifier.parse(key.key()));
   }

   private static boolean isValidBlock(DimletKey key) {
      Block value = Tools.getBlock(Identifier.parse(key.key()));
      return value != null && value != Blocks.AIR;
   }

   private static boolean isValidFluid(DimletKey key) {
      Fluid fluid = Tools.getFluid(Identifier.parse(key.key()));
      return fluid != null && fluid.defaultFluidState().createLegacyBlock().getBlock() != Blocks.AIR;
   }
}
