package mcjty.rftoolsdim.modules.dimlets.recipes;

import javax.annotation.Nonnull;
import mcjty.lib.crafting.AbstractRecipeAdaptor;
import mcjty.rftoolsdim.modules.dimlets.DimletModule;
import mcjty.rftoolsdim.modules.dimlets.data.DimletKey;
import mcjty.rftoolsdim.modules.dimlets.data.DimletTools;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

public class DimletRecipe extends AbstractRecipeAdaptor {
   private final DimletKey key;

   public DimletRecipe(ShapedRecipe recipe, DimletKey key) {
      super(recipe);
      this.key = key;
   }

   public CraftingBookCategory category() {
      return CraftingBookCategory.MISC;
   }

   public DimletKey getKey() {
      return this.key;
   }

   @Nonnull
   public ItemStack getResultItem(Provider access) {
      return DimletTools.getDimletStack(this.key);
   }

   @Nonnull
   public ItemStack assemble(@Nonnull CraftingInput inv) {
      return DimletTools.getDimletStack(this.key);
   }

   @Nonnull
   public RecipeSerializer<DimletRecipe> getSerializer() {
      return DimletModule.DIMLET_RECIPE_SERIALIZER.get();
   }
}
