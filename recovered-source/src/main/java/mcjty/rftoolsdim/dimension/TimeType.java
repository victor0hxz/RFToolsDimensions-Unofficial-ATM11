package mcjty.rftoolsdim.dimension;

import java.util.HashMap;
import java.util.Map;
import mcjty.rftoolsdim.modules.knowledge.data.KnowledgeSet;
import net.minecraft.resources.Identifier;

public enum TimeType {
   NORMAL("normal", KnowledgeSet.SET1, DimensionRegistry.NORMAL_TIME_ID),
   DAY("day", KnowledgeSet.SET2, DimensionRegistry.FIXED_DAY_ID),
   NIGHT("night", KnowledgeSet.SET2, DimensionRegistry.FIXED_NIGHT_ID);

   private final String name;
   private final KnowledgeSet set;
   private final Identifier dimensionType;
   private static final Map<String, TimeType> TYPE_BY_NAME = new HashMap<>();

   private TimeType(String name, KnowledgeSet set, Identifier dimensionType) {
      this.name = name;
      this.set = set;
      this.dimensionType = dimensionType;
   }

   public String getName() {
      return this.name;
   }

   public KnowledgeSet getSet() {
      return this.set;
   }

   public Identifier getDimensionType() {
      return this.dimensionType;
   }

   public static TimeType byName(String name) {
      return TYPE_BY_NAME.get(name.toLowerCase());
   }

   static {
      for (TimeType type : values()) {
         TYPE_BY_NAME.put(type.getName(), type);
      }
   }
}
