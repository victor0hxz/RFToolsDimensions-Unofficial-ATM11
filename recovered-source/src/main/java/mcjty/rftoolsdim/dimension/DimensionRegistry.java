package mcjty.rftoolsdim.dimension;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

public class DimensionRegistry {
   public static final Identifier RFTOOLS_EFFECTS_ID = Identifier.fromNamespaceAndPath("rftoolsdim", "effects");
   public static final Identifier FIXED_DAY_ID = Identifier.fromNamespaceAndPath("rftoolsdim", "fixed_day");
   public static final Identifier FIXED_NIGHT_ID = Identifier.fromNamespaceAndPath("rftoolsdim", "fixed_night");
   public static final Identifier NORMAL_TIME_ID = Identifier.fromNamespaceAndPath("rftoolsdim", "normal_time");
   public static final Identifier CAVERN_ID = Identifier.fromNamespaceAndPath("rftoolsdim", "cavern");
   public static final Identifier HUT_LOOT_ID = Identifier.fromNamespaceAndPath("rftoolsdim", "hut_loot");
   public static final ResourceKey<LootTable> HUT_LOOT = ResourceKey.create(
      Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath("rftoolsdim", "chests/hut_loot")
   );
}
