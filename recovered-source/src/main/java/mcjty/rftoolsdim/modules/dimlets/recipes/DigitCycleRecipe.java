package mcjty.rftoolsdim.modules.dimlets.recipes;

import javax.annotation.Nonnull;
import mcjty.lib.crafting.AbstractRecipeAdaptor;
import mcjty.rftoolsdim.modules.dimlets.DimletModule;
import mcjty.rftoolsdim.modules.dimlets.data.DimletKey;
import mcjty.rftoolsdim.modules.dimlets.data.DimletTools;
import mcjty.rftoolsdim.modules.dimlets.data.DimletType;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

public class DigitCycleRecipe extends AbstractRecipeAdaptor {
   private final String input;
   private final String output;

   public DigitCycleRecipe(ShapedRecipe recipe, String input, String output) {
      super(recipe);
      this.input = input;
      this.output = output;
   }

   public String getInput() {
      return this.input;
   }

   public String getOutput() {
      return this.output;
   }

   public CraftingBookCategory category() {
      return CraftingBookCategory.MISC;
   }

   public boolean matches(@Nonnull CraftingInput inv, @Nonnull Level worldIn) {
      boolean matches = super.matches(inv, worldIn);
      if (matches) {
         for (int i = 0; i < inv.size(); i++) {
            if (!inv.getItem(i).isEmpty()) {
               DimletKey key = DimletTools.getDimletKey(inv.getItem(i));
               if (key != null) {
                  return key.key().equals(this.input);
               }
            }
         }
      }

      return false;
   }

   @Nonnull
   public ItemStack getResultItem(Provider access) {
      return DimletTools.getDimletStack(new DimletKey(DimletType.DIGIT, this.output));
   }

   @Nonnull
   public ItemStack assemble(@Nonnull CraftingInput inv) {
      return DimletTools.getDimletStack(new DimletKey(DimletType.DIGIT, this.output));
   }

   @Nonnull
   public RecipeSerializer<DigitCycleRecipe> getSerializer() {
      return DimletModule.DIMLET_CYCLE_SERIALIZER.get();
   }
}
