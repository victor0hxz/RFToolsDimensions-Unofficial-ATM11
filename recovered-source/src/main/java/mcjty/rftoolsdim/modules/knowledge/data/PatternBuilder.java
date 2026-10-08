package mcjty.rftoolsdim.modules.knowledge.data;

public class PatternBuilder {
   public static final char EMPTY = ' ';
   public static final char SHARD = '*';
   public static final char LEV0 = '0';
   public static final char LEV1 = '1';
   public static final char LEV2 = '2';
   private final char[][] pattern = new char[][]{
      {' ', ' ', ' ', ' ', ' ', ' '},
      {' ', ' ', ' ', ' ', ' ', ' '},
      {' ', ' ', ' ', ' ', ' ', ' '},
      {' ', ' ', ' ', ' ', ' ', ' '},
      {' ', ' ', ' ', ' ', ' ', ' '},
      {' ', ' ', ' ', ' ', ' ', ' '}
   };

   public void set(int x, int y, char s) {
      this.pattern[x][y] = s;
   }

   public DimletPattern build() {
      String[] p = new String[6];

      for (int y = 0; y < 6; y++) {
         StringBuilder s = new StringBuilder();

         for (int x = 0; x < 6; x++) {
            s.append(this.pattern[x][y]);
         }

         p[y] = s.toString();
      }

      return new DimletPattern(p);
   }
}
