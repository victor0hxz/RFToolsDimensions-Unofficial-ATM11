package mcjty.rftoolsdim.modules.dimlets.lootmodifier;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import mcjty.rftoolsdim.modules.dimlets.DimletModule;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public record LootTableCondition(Set<Identifier> tables) implements LootItemCondition {
   public static final MapCodec<LootTableCondition> CODEC = RecordCodecBuilder.mapCodec(
      instance -> instance.group(Identifier.CODEC.listOf().fieldOf("tables").forGetter(condition -> List.copyOf(condition.tables)))
         .apply(instance, list -> new LootTableCondition(ImmutableSet.copyOf(list)))
   );

   public MapCodec<LootTableCondition> codec() {
      return DimletModule.LOOT_TABLE_CONDITION.get();
   }

   public boolean test(LootContext lootContext) {
      Identifier table = lootContext.getQueriedLootTableId();
      return this.tables.contains(table);
   }
}
