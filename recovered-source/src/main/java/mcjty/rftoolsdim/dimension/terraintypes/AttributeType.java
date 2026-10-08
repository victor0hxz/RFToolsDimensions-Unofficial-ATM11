package mcjty.rftoolsdim.dimension.terraintypes;

import java.util.HashMap;
import java.util.Map;
import mcjty.rftoolsdim.modules.knowledge.data.KnowledgeSet;

public enum AttributeType {
   DEFAULT("default", KnowledgeSet.SET1),
   NOOCEANS("nooceans", KnowledgeSet.SET1),
   WATERWORLD("waterworld", KnowledgeSet.SET1),
   CITIES("cities", KnowledgeSet.SET1),
   NOBLOBS("noblobs", KnowledgeSet.SET1);

   private final String name;
   private final KnowledgeSet set;
   private static final Map<String, AttributeType> ATTRIBUTE_BY_NAME = new HashMap<>();

   private AttributeType(String name, KnowledgeSet set) {
      this.name = name;
      this.set = set;
   }

   public String getName() {
      return this.name;
   }

   public KnowledgeSet getSet() {
      return this.set;
   }

   public static AttributeType byName(String name) {
      return ATTRIBUTE_BY_NAME.get(name.toLowerCase());
   }

   static {
      for (AttributeType type : values()) {
         ATTRIBUTE_BY_NAME.put(type.getName(), type);
      }
   }
}
