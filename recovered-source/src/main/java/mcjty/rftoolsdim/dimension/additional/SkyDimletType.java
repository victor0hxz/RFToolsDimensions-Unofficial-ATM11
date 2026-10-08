package mcjty.rftoolsdim.dimension.additional;

import java.util.HashMap;
import java.util.Map;
import mcjty.rftoolsdim.modules.knowledge.data.KnowledgeSet;

public enum SkyDimletType {
   DEFAULT(0L, KnowledgeSet.SET1),
   END(1L, KnowledgeSet.SET1),
   INFERNAL(1024L, KnowledgeSet.SET5),
   BLACK(2048L, KnowledgeSet.SET5),
   STARS(4096L, KnowledgeSet.SET5),
   NEBULA(8192L, KnowledgeSet.SET5),
   NOCLOUDS(1073741824L, KnowledgeSet.SET4),
   THICKBLACKFOG(1099511627776L, KnowledgeSet.SET3),
   THICKREDFOG(2199023255552L, KnowledgeSet.SET3),
   THICKWHITEFOG(4398046511104L, KnowledgeSet.SET3),
   BLACKFOG(35184372088832L, KnowledgeSet.SET3),
   REDFOG(70368744177664L, KnowledgeSet.SET3),
   WHITEFOG(140737488355328L, KnowledgeSet.SET3);

   private final long mask;
   private final KnowledgeSet set;
   private static final Map<String, SkyDimletType> BY_NAME = new HashMap<>();

   private SkyDimletType(long mask, KnowledgeSet set) {
      this.mask = mask;
      this.set = set;
   }

   public static String getDescription(long skyDimletTypes) {
      String buf = "";

      for (SkyDimletType value : values()) {
         if (value.match(skyDimletTypes)) {
            buf = buf + value.name() + " ";
         }
      }

      return buf;
   }

   public long getMask() {
      return this.mask;
   }

   public KnowledgeSet getKnowledgeSet() {
      return this.set;
   }

   public boolean match(long skyMask) {
      return (skyMask & this.mask) != 0L;
   }

   public static SkyDimletType byName(String name) {
      return BY_NAME.get(name.toLowerCase());
   }

   static {
      for (SkyDimletType type : values()) {
         BY_NAME.put(type.name().toLowerCase(), type);
      }
   }
}
