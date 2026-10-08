package mcjty.rftoolsdim.modules.dimlets.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsdim.modules.knowledge.data.DimletPattern;
import mcjty.rftoolsdim.modules.knowledge.data.KnowledgeManager;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.Pair;

public class DimletDictionary {
   private static final DimletDictionary INSTANCE = new DimletDictionary();
   private final Map<DimletKey, DimletSettings> dimlets = new HashMap<>();
   private final Map<DimletRarity, List<DimletKey>> dimletsByRarity = new HashMap<>();
   private final Map<Pair<DimletType, DimletRarity>, List<DimletKey>> dimletsByRarityAndType = new HashMap<>();

   public static DimletDictionary get() {
      return INSTANCE;
   }

   public void reset() {
      this.dimlets.clear();
      this.dimletsByRarity.clear();
      this.dimletsByRarityAndType.clear();
   }

   public boolean register(RegistryAccess access, DimletKey key, DimletSettings settings) {
      if (DimletTools.isValidDimlet(access, key)) {
         this.dimlets.put(key, settings);
         return true;
      } else {
         return false;
      }
   }

   public Set<DimletKey> getDimlets() {
      return this.dimlets.keySet();
   }

   public DimletSettings getSettings(DimletKey key) {
      return this.dimlets.get(key);
   }

   public DimletKey tryCraft(Level world, DimletType type, ItemStack memoryPart, ItemStack energyPart, ItemStack essence, DimletPattern pattern) {
      for (Entry<DimletKey, DimletSettings> entry : this.dimlets.entrySet()) {
         DimletKey key = entry.getKey();
         if (type.equals(key.type())
            && ItemStack.isSameItem(memoryPart, DimletTools.getNeededMemoryPart(key))
            && ItemStack.isSameItem(energyPart, DimletTools.getNeededEnergyPart(key))) {
            ItemStack neededEssence = DimletTools.getNeededEssence(key, entry.getValue());
            if (DimletTools.isFullEssence(essence, neededEssence, key.key())) {
               ServerLevel overworld = LevelTools.getOverworld(world);
               DimletPattern neededPattern = KnowledgeManager.get().getPattern(overworld, overworld.getSeed(), key);
               if (Objects.equals(neededPattern, pattern)) {
                  return key;
               }
            }
         }
      }

      return null;
   }

   @Nullable
   public DimletKey getRandomDimlet(DimletRarity rarity, RandomSource random) {
      List<DimletKey> keys = this.getDimletsByRarity(rarity);
      if (keys.isEmpty()) {
         return null;
      } else if (keys.size() == 1) {
         return keys.get(0);
      } else {
         DimletKey dimletKey = null;

         for (int i = 0; i < Math.max(2, Math.min(10, keys.size() / 20)); i++) {
            dimletKey = keys.get(random.nextInt(keys.size()));
            if (dimletKey.type() != DimletType.BLOCK) {
               return dimletKey;
            }
         }

         return dimletKey;
      }
   }

   @Nullable
   public DimletKey getRandomDimlet(DimletType type, Random random) {
      DimletKey key = this.getRandomDimletInternal(type, DimletRarity.COMMON, random);
      if (key == null) {
         key = this.getRandomDimletInternal(type, DimletRarity.UNCOMMON, random);
         if (key == null) {
            key = this.getRandomDimletInternal(type, DimletRarity.RARE, random);
            if (key == null) {
               key = this.getRandomDimletInternal(type, DimletRarity.LEGENDARY, random);
            }
         }
      }

      return key;
   }

   private DimletKey getRandomDimletInternal(DimletType type, DimletRarity startAt, Random random) {
      DimletRarity rarity = startAt;
      if (random.nextFloat() < 0.1F) {
         rarity = DimletRarity.UNCOMMON;
         if (random.nextFloat() < 0.1F) {
            rarity = DimletRarity.RARE;
            if (random.nextFloat() < 0.1F) {
               rarity = DimletRarity.LEGENDARY;
            }
         }
      }

      while (true) {
         List<DimletKey> keys = this.getDimletsByRarityAndType(type, rarity);
         if (!keys.isEmpty()) {
            return keys.size() == 1 ? keys.get(0) : keys.get(random.nextInt(keys.size()));
         }

         switch (rarity) {
            case COMMON:
               return null;
            case UNCOMMON:
               rarity = DimletRarity.COMMON;
               break;
            case RARE:
               rarity = DimletRarity.UNCOMMON;
               break;
            case LEGENDARY:
               rarity = DimletRarity.RARE;
         }
      }
   }

   private List<DimletKey> getDimletsByRarity(DimletRarity rarity) {
      if (!this.dimletsByRarity.containsKey(rarity)) {
         List<DimletKey> dimletKeys = new ArrayList<>();

         for (Entry<DimletKey, DimletSettings> entry : this.dimlets.entrySet()) {
            if (entry.getValue().getRarity() == rarity) {
               DimletKey key = entry.getKey();
               if (this.getSettings(key).isWorldgen()) {
                  dimletKeys.add(key);
               }
            }
         }

         this.dimletsByRarity.put(rarity, dimletKeys);
      }

      return this.dimletsByRarity.get(rarity);
   }

   private List<DimletKey> getDimletsByRarityAndType(DimletType type, DimletRarity rarity) {
      Pair<DimletType, DimletRarity> pair = Pair.of(type, rarity);
      if (!this.dimletsByRarityAndType.containsKey(pair)) {
         List<DimletKey> dimletKeys = new ArrayList<>();

         for (DimletKey key : this.getDimletsByRarity(rarity)) {
            if (key.type() == type) {
               dimletKeys.add(key);
            }
         }

         this.dimletsByRarityAndType.put(pair, dimletKeys);
      }

      return this.dimletsByRarityAndType.get(pair);
   }

   public void readPackage(RegistryAccess access, String filename) {
      DimletPackages.readPackage(filename, (key, settings) -> this.register(access, key, settings));
   }

   @Nullable
   public DimletKey getBlockDimlet(String block) {
      for (Entry<DimletKey, DimletSettings> entry : this.dimlets.entrySet()) {
         DimletKey key = entry.getKey();
         if (key.type().equals(DimletType.BLOCK) && Objects.equals(key.key(), block)) {
            return key;
         }
      }

      return null;
   }

   @Nullable
   public DimletKey getStructureDimlet(String structure) {
      for (Entry<DimletKey, DimletSettings> entry : this.dimlets.entrySet()) {
         DimletKey key = entry.getKey();
         if (key.type().equals(DimletType.STRUCTURE) && Objects.equals(key.key(), structure)) {
            return key;
         }
      }

      return null;
   }

   @Nullable
   public DimletKey getFluidDimlet(String fluid) {
      for (Entry<DimletKey, DimletSettings> entry : this.dimlets.entrySet()) {
         DimletKey key = entry.getKey();
         if (key.type().equals(DimletType.FLUID) && Objects.equals(key.key(), fluid)) {
            return key;
         }
      }

      return null;
   }

   @Nullable
   public DimletKey getBiomeDimlet(String biomeId) {
      for (Entry<DimletKey, DimletSettings> entry : this.dimlets.entrySet()) {
         DimletKey key = entry.getKey();
         if (key.type().equals(DimletType.BIOME) && Objects.equals(key.key(), biomeId)) {
            return key;
         }
      }

      return null;
   }
}
