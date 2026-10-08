package mcjty.rftoolsdim.compat;

import javax.annotation.Nonnull;
import mcjty.rftoolsdim.modules.dimlets.DimletModule;
import mcjty.rftoolsdim.modules.dimlets.data.DimletKey;
import mcjty.rftoolsdim.modules.dimlets.data.DimletTools;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@JeiPlugin
public class JeiCompatibility implements IModPlugin {
   @Nonnull
   public Identifier getPluginUid() {
      return Identifier.fromNamespaceAndPath("rftoolsdim", "jeiplugin");
   }

   public void registerItemSubtypes(ISubtypeRegistration registration) {
      registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, (Item)DimletModule.ATTRIBUTE_DIMLET.get(), JeiCompatibility.DimletInterpreter.INSTANCE);
      registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, (Item)DimletModule.TERRAIN_DIMLET.get(), JeiCompatibility.DimletInterpreter.INSTANCE);
      registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, (Item)DimletModule.FLUID_DIMLET.get(), JeiCompatibility.DimletInterpreter.INSTANCE);
      registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, (Item)DimletModule.FEATURE_DIMLET.get(), JeiCompatibility.DimletInterpreter.INSTANCE);
      registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, (Item)DimletModule.BIOME_DIMLET.get(), JeiCompatibility.DimletInterpreter.INSTANCE);
      registration.registerSubtypeInterpreter(
         VanillaTypes.ITEM_STACK, (Item)DimletModule.BIOME_CONTROLLER_DIMLET.get(), JeiCompatibility.DimletInterpreter.INSTANCE
      );
      registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, (Item)DimletModule.BLOCK_DIMLET.get(), JeiCompatibility.DimletInterpreter.INSTANCE);
      registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, (Item)DimletModule.TIME_DIMLET.get(), JeiCompatibility.DimletInterpreter.INSTANCE);
      registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, (Item)DimletModule.DIGIT_DIMLET.get(), JeiCompatibility.DimletInterpreter.INSTANCE);
      registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, (Item)DimletModule.ADMIN_DIMLET.get(), JeiCompatibility.DimletInterpreter.INSTANCE);
   }

   public static class DimletInterpreter implements ISubtypeInterpreter<ItemStack> {
      public static final JeiCompatibility.DimletInterpreter INSTANCE = new JeiCompatibility.DimletInterpreter();

      @Nullable
      public Object getSubtypeData(ItemStack ingredient, UidContext context) {
         DimletKey key = DimletTools.getDimletKey(ingredient);
         return key == null ? "null" : key.key();
      }

      public String getLegacyStringSubtypeInfo(ItemStack ingredient, UidContext context) {
         return "";
      }
   }
}
