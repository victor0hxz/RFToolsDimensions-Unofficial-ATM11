package mcjty.rftoolsdim.dimension.terraintypes;

import java.util.HashMap;
import java.util.Map;
import mcjty.rftoolsdim.dimension.noisesettings.TerrainPresets;
import mcjty.rftoolsdim.modules.knowledge.data.KnowledgeSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

public enum TerrainType {
   FLAT("flat", KnowledgeSet.SET1, TerrainPresets.RFTOOLSDIM_FLAT, false),
   WAVES("waves", KnowledgeSet.SET1, TerrainPresets.RFTOOLSDIM_OVERWORLD, false),
   VOID("void", KnowledgeSet.SET2, TerrainPresets.RFTOOLSDIM_OVERWORLD, true),
   NORMAL("normal", KnowledgeSet.SET3, TerrainPresets.RFTOOLSDIM_OVERWORLD, false),
   ISLANDS("islands", KnowledgeSet.SET4, TerrainPresets.RFTOOLSDIM_ISLANDS, true),
   CAVERN("cavern", KnowledgeSet.SET5, TerrainPresets.RFTOOLSDIM_CAVERN, false),
   CHAOTIC("chaotic", KnowledgeSet.SET4, TerrainPresets.RFTOOLSDIM_CHAOTIC, true),
   GRID("grid", KnowledgeSet.SET2, TerrainPresets.RFTOOLSDIM_OVERWORLD, true),
   PLATFORMS("platforms", KnowledgeSet.SET2, TerrainPresets.RFTOOLSDIM_OVERWORLD, true),
   SPIKES("spikes", KnowledgeSet.SET4, TerrainPresets.RFTOOLSDIM_OVERWORLD, false),
   MAZE("maze", KnowledgeSet.SET1, TerrainPresets.RFTOOLSDIM_OVERWORLD, false),
   RAVINE("ravine", KnowledgeSet.SET3, TerrainPresets.RFTOOLSDIM_OVERWORLD, false);

   private final String name;
   private final KnowledgeSet set;
   private final ResourceKey<NoiseGeneratorSettings> noiseSettings;
   private final boolean voidLike;
   private static final Map<String, TerrainType> TERRAIN_BY_NAME = new HashMap<>();

   private TerrainType(String name, KnowledgeSet set, ResourceKey<NoiseGeneratorSettings> noiseSettings, boolean voidLike) {
      this.name = name;
      this.set = set;
      this.noiseSettings = noiseSettings;
      this.voidLike = voidLike;
   }

   public String getName() {
      return this.name;
   }

   public boolean isVoidLike() {
      return this.voidLike;
   }

   public KnowledgeSet getSet() {
      return this.set;
   }

   public ResourceKey<NoiseGeneratorSettings> getNoiseSettings() {
      return this.noiseSettings;
   }

   public static TerrainType byName(String name) {
      return TERRAIN_BY_NAME.get(name.toLowerCase());
   }

   static {
      for (TerrainType type : values()) {
         TERRAIN_BY_NAME.put(type.getName(), type);
      }
   }
}
