package mcjty.rftoolsdim.modules.dimlets.lootmodifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import mcjty.rftoolsdim.modules.dimlets.data.DimletDictionary;
import mcjty.rftoolsdim.modules.dimlets.data.DimletKey;
import mcjty.rftoolsdim.modules.dimlets.data.DimletRarity;
import mcjty.rftoolsdim.modules.dimlets.data.DimletTools;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer.Builder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class DimletLootEntry extends LootPoolSingletonContainer {
   public static final MapCodec<DimletLootEntry> CODEC = RecordCodecBuilder.mapCodec(
      instance -> singletonFields(instance).and(rarityCodec().fieldOf("rarity").forGetter(DimletLootEntry::getRarity)).apply(instance, DimletLootEntry::new)
   );
   private final DimletRarity rarity;
   private final RandomSource random = RandomSource.create(546L);

   public MapCodec<DimletLootEntry> codec() {
      return CODEC;
   }

   private DimletLootEntry(int weightIn, int qualityIn, List<LootItemCondition> conditionsIn, List<LootItemFunction> functionsIn, DimletRarity rarity) {
      super(weightIn, qualityIn, conditionsIn, functionsIn);
      this.rarity = rarity;
   }

   protected void createItemStack(Consumer<ItemStack> stackConsumer, LootContext context) {
      DimletKey dimlet = DimletDictionary.get().getRandomDimlet(this.rarity, this.random);
      if (dimlet != null) {
         stackConsumer.accept(DimletTools.getDimletStack(dimlet));
      }
   }

   public DimletRarity getRarity() {
      return this.rarity;
   }

   public static Builder<?> builder(DimletRarity rarity) {
      return simpleBuilder((weight, quality, conditions, functions) -> new DimletLootEntry(weight, quality, conditions, functions, rarity));
   }

   private static Codec<DimletRarity> rarityCodec() {
      return Codec.STRING.comapFlatMap(name -> {
         DimletRarity rarity = DimletRarity.byName(name);
         return rarity != null ? DataResult.success(rarity) : DataResult.error(() -> "Unknown rarity '" + name + "'");
      }, rarity -> rarity.name().toLowerCase(Locale.ROOT));
   }
}
