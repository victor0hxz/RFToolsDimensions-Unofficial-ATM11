package mcjty.rftoolsdim.modules.knowledge.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Supplier;
import mcjty.rftoolsdim.modules.dimlets.data.DimletRarity;
import mcjty.rftoolsdim.modules.dimlets.data.DimletType;

public class RandomPatternCreator {
   private static final List<String[]> PATTERNS = new ArrayList<>();

   private static List<String[]> getPatterns() {
      if (PATTERNS.isEmpty()) {
         generatePattern("##", "##");
         generatePattern("# ", " #");
         generatePattern(" #", "# ");
         generatePattern("##", " #");
         generatePattern("##", "# ");
         generatePattern(" #", "##");
         generatePattern("# ", "##");
         generatePattern("###", "###");
         generatePattern("##", "##", "##");
         generatePattern("####", "####");
         generatePattern("##", "##", "##", "##");
         generatePattern("# #", "   ", "# #");
         generatePattern("###", "   ", "###");
         generatePattern("# #", "# #", "# #");
         generatePattern("###", "#  ", "###");
         generatePattern("###", "# #", "# #");
         generatePattern("###", "  #", "###");
         generatePattern("# #", "# #", "###");
         generatePattern("###", "# #", "###");
         generatePattern("###", " # ", "###");
         generatePattern("# #", "###", "# #");
         generatePattern("#  ", " # ", "  #");
         generatePattern("  #", " # ", "#  ");
         generatePattern("###", "#  ", "#  ");
         generatePattern("###", "  #", "  #");
         generatePattern("#  ", "#  ", "###");
         generatePattern("  #", "  #", "###");
         generatePattern(" ##", "#  ", "#  ");
         generatePattern("## ", "  #", "  #");
         generatePattern("#  ", "#  ", " ##");
         generatePattern("  #", "  #", "## ");
         generatePattern("# #", " # ", "# #");
         generatePattern(" # ", "###", " # ");
         generatePattern("###", "###", "###");
         generatePattern("#  #", "    ", "    ", "#  #");
         generatePattern("####", "#   ", "#   ", "#   ");
         generatePattern("####", "   #", "   #", "   #");
         generatePattern("#   ", "#   ", "#   ", "####");
         generatePattern("   #", "   #", "   #", "####");
         generatePattern(" ###", "#   ", "#   ", "#   ");
         generatePattern("### ", "   #", "   #", "   #");
         generatePattern("#   ", "#   ", "#   ", " ###");
         generatePattern("   #", "   #", "   #", "### ");
         generatePattern("####", "#  #", "#  #", "####");
         generatePattern(" ## ", "#  #", "#  #", " ## ");
         generatePattern("#  #", " ## ", " ## ", "#  #");
         generatePattern("#   #", "     ", "     ", "     ", "#   #");
         generatePattern("#   #", " # # ", "  #  ", " # # ", "#   #");
         generatePattern("#   #", " ### ", " ### ", " ### ", "#   #");
         generatePattern("## ##", "#   #", "     ", "#   #", "## ##");
         generatePattern("#####", "#   #", "#   #", "#   #", "#####");
         generatePattern("#    #", "      ", "      ", "      ", "      ", "#    #");
         generatePattern("#    #", " #  # ", "  ##  ", "  ##  ", " #  # ", "#    #");
         generatePattern("##  ##", "#    #", "      ", "      ", "#    #", "##  ##");
         generatePattern("##  ##", "##  ##", "      ", "      ", "##  ##", "##  ##");
         generatePattern("######", "#    #", "#    #", "#    #", "#    #", "######");
         System.out.println("Generated patterns: " + PATTERNS.size());
      }

      return PATTERNS;
   }

   private static void generatePattern(String... pattern) {
      for (int y = 0; y <= 6 - pattern.length; y++) {
         for (int x = 0; x <= 6 - pattern[0].length(); x++) {
            String[] ppp = new String[6];

            for (int py = 0; py < 6; py++) {
               StringBuilder pat = new StringBuilder();
               if (py < y) {
                  pat = new StringBuilder("      ");
               } else if (py >= y + pattern.length) {
                  pat = new StringBuilder("      ");
               } else {
                  for (int px = 0; px < 6; px++) {
                     if (px < x) {
                        pat.append(" ");
                     } else if (px >= x + pattern[0].length()) {
                        pat.append(" ");
                     } else {
                        pat.append(pattern[py - y].charAt(px - x));
                     }
                  }
               }

               ppp[py] = pat.toString();
            }

            PATTERNS.add(ppp);
         }
      }
   }

   private static RandomPatternCreator.SelectedPattern findUnusedPattern(
      Set<RandomPatternCreator.SelectedPattern> alreadyUsed, Supplier<RandomPatternCreator.SelectedPattern> generator
   ) {
      RandomPatternCreator.SelectedPattern pattern = generator.get();

      while (alreadyUsed.contains(pattern)) {
         pattern = generator.get();
      }

      alreadyUsed.add(pattern);
      return pattern;
   }

   private static void applyPattern(PatternBuilder builder, String[] pattern, char c) {
      for (int y = 0; y < pattern.length; y++) {
         for (int x = 0; x < pattern[y].length(); x++) {
            char p = pattern[y].charAt(x);
            if (p == '#') {
               builder.set(x, y, c);
            }
         }
      }
   }

   private static DimletPattern buildPattern(RandomPatternCreator.SelectedPattern selectedPattern) {
      PatternBuilder builder = new PatternBuilder();
      List<String[]> patterns = getPatterns();
      int i1 = selectedPattern.i1();
      applyPattern(builder, patterns.get(i1), '*');
      int i2 = selectedPattern.i2();
      if (i2 != -1) {
         applyPattern(builder, patterns.get(i2), '0');
      }

      int i3 = selectedPattern.i3();
      if (i3 != -1) {
         applyPattern(builder, patterns.get(i3), '1');
      }

      int i4 = selectedPattern.i4();
      if (i4 != -1) {
         applyPattern(builder, patterns.get(i4), '2');
      }

      return builder.build();
   }

   public static Map<KnowledgeKey, DimletPattern> createRandomPatterns(long seed) {
      Set<RandomPatternCreator.SelectedPattern> selectedPatterns = new HashSet<>();
      Map<KnowledgeKey, RandomPatternCreator.SelectedPattern> patternMap = new HashMap<>();
      Random random = new Random(seed);
      random.nextInt();
      random.nextInt();

      for (KnowledgeSet set : KnowledgeSet.values()) {
         for (DimletType type : DimletType.values()) {
            RandomPatternCreator.SelectedPattern pattern0 = findUnusedPattern(
               selectedPatterns, () -> new RandomPatternCreator.SelectedPattern(r(random), -1, -1, -1)
            );
            patternMap.put(new KnowledgeKey(type, DimletRarity.COMMON, set), pattern0);
            RandomPatternCreator.SelectedPattern pattern1 = findUnusedPattern(
               selectedPatterns, () -> new RandomPatternCreator.SelectedPattern(r(random), r(random), -1, -1)
            );
            patternMap.put(new KnowledgeKey(type, DimletRarity.UNCOMMON, set), pattern1);
            RandomPatternCreator.SelectedPattern pattern2 = findUnusedPattern(
               selectedPatterns, () -> new RandomPatternCreator.SelectedPattern(r(random), r(random), r(random), -1)
            );
            patternMap.put(new KnowledgeKey(type, DimletRarity.RARE, set), pattern2);
            RandomPatternCreator.SelectedPattern pattern3 = findUnusedPattern(
               selectedPatterns, () -> new RandomPatternCreator.SelectedPattern(r(random), r(random), r(random), r(random))
            );
            patternMap.put(new KnowledgeKey(type, DimletRarity.LEGENDARY, set), pattern3);
         }
      }

      Map<KnowledgeKey, DimletPattern> patterns = new HashMap<>();

      for (Entry<KnowledgeKey, RandomPatternCreator.SelectedPattern> entry : patternMap.entrySet()) {
         RandomPatternCreator.SelectedPattern selectedPattern = entry.getValue();
         patterns.put(entry.getKey(), buildPattern(selectedPattern));
      }

      return patterns;
   }

   private static int r(Random random) {
      return random.nextInt(getPatterns().size());
   }

   public static void main(String[] args) {
      int idx = 1;

      for (String[] pattern : getPatterns()) {
         System.out.println("Pattern " + idx);

         for (String s : pattern) {
            System.out.println("    " + s.replace(' ', '.'));
         }

         idx++;
      }
   }

   private record SelectedPattern(int i1, int i2, int i3, int i4) {
   }
}
