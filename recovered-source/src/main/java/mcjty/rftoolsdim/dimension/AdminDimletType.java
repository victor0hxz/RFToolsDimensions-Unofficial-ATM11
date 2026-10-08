package mcjty.rftoolsdim.dimension;

import java.util.HashMap;
import java.util.Map;

public enum AdminDimletType {
   OWNER("owner"),
   CHEATER("cheater");

   private final String name;
   private static final Map<String, AdminDimletType> ADMIN_BY_NAME = new HashMap<>();

   private AdminDimletType(String name) {
      this.name = name;
   }

   public String getName() {
      return this.name;
   }

   public static AdminDimletType byName(String name) {
      return ADMIN_BY_NAME.get(name.toLowerCase());
   }

   static {
      for (AdminDimletType type : values()) {
         ADMIN_BY_NAME.put(type.getName(), type);
      }
   }
}
