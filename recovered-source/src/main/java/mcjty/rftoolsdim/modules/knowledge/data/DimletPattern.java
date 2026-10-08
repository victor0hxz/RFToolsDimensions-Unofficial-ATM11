package mcjty.rftoolsdim.modules.knowledge.data;

import java.util.Arrays;

public record DimletPattern(String[] pattern) {
   public static final int PATTERN_DIM = 6;

   public DimletPattern(String[] pattern) {
      System.arraycopy(pattern, 0, this.pattern, 0, 6);
   }

   public int count(char s) {
      int cnt = 0;

      for (String p : this.pattern) {
         for (int i = 0; i < p.length(); i++) {
            char c = p.charAt(i);
            if (c == s) {
               cnt++;
            }
         }
      }

      return cnt;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         DimletPattern that = (DimletPattern)o;
         return Arrays.equals((Object[])this.pattern, (Object[])that.pattern);
      } else {
         return false;
      }
   }
}
