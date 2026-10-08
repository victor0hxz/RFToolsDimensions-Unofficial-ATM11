package mcjty.rftoolsdim.compat;

import java.util.function.Function;
import mcjty.rftoolsbase.api.teleportation.ITeleportationManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.WorldGenLevel;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModList;

public class RFToolsUtilityCompat {
   private static boolean registered = false;
   public static ITeleportationManager teleportationManager = null;

   public static void register() {
      if (ModList.get().isLoaded("rftoolsutility")) {
         registerInternal();
      }
   }

   private static void registerInternal() {
      if (!registered) {
         registered = true;
         InterModComms.sendTo("rftoolsutility", "getTeleportationManager", RFToolsUtilityCompat.GetTeleportationManager::new);
      }
   }

   public static void createTeleporter(WorldGenLevel reader, BlockPos pos, String name) {
      if (teleportationManager != null) {
         teleportationManager.createReceiver(reader.getLevel(), pos, name, -1);
      }
   }

   public static void createTeleporter(WorldGenLevel reader, BlockPos pos, String name, int power) {
      if (teleportationManager != null) {
         teleportationManager.createReceiver(reader.getLevel(), pos, name, power);
      }
   }

   public static String getReceiverName(ItemStack stack) {
      return teleportationManager != null ? teleportationManager.getReceiverName(stack) : "";
   }

   public static class GetTeleportationManager implements Function<ITeleportationManager, Void> {
      public Void apply(ITeleportationManager tm) {
         RFToolsUtilityCompat.teleportationManager = tm;
         return null;
      }
   }
}
