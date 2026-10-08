package mcjty.rftoolsdim.dimension.descriptor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import mcjty.lib.varia.ComponentFactory;
import mcjty.rftoolsdim.RFToolsDim;
import mcjty.rftoolsdim.modules.dimlets.data.DimletDictionary;
import mcjty.rftoolsdim.modules.dimlets.data.DimletKey;
import mcjty.rftoolsdim.modules.dimlets.data.DimletType;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Player;

public class DimensionDescriptor {
   private final List<DimletKey> dimlets = new ArrayList<>();
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
   private static final Gson GSON_COMPACT = new GsonBuilder().disableHtmlEscaping().create();
   public static final DimensionDescriptor EMPTY = new DimensionDescriptor();

   public List<DimletKey> getDimlets() {
      return this.dimlets;
   }

   public void read(String json) {
      JsonElement root = JsonParser.parseString(json);
      JsonArray object = root.getAsJsonArray();
      this.read(object);
   }

   public void read(JsonArray object) {
      this.dimlets.clear();

      for (JsonElement element : object) {
         JsonObject dimletJson = element.getAsJsonObject();
         String type;
         if (dimletJson.has("type")) {
            type = dimletJson.getAsJsonPrimitive("type").getAsString();
         } else {
            type = dimletJson.getAsJsonPrimitive("t").getAsString();
         }

         DimletType dimletType = DimletType.byName(type);
         String name;
         if (dimletJson.has("name")) {
            name = dimletJson.get("name").getAsString();
         } else {
            name = dimletJson.get("n").getAsString();
         }

         DimletKey dimletDescriptor = new DimletKey(dimletType, name);
         this.dimlets.add(dimletDescriptor);
      }
   }

   public String write() {
      JsonArray root = new JsonArray();

      for (DimletKey dimletDescriptor : this.dimlets) {
         JsonObject dimletJson = new JsonObject();
         dimletJson.addProperty("type", dimletDescriptor.type().name());
         dimletJson.addProperty("name", dimletDescriptor.key());
         root.add(dimletJson);
      }

      return GSON.toJson(root);
   }

   public String compact() {
      JsonArray root = new JsonArray();

      for (DimletKey dimletDescriptor : this.dimlets) {
         JsonObject dimletJson = new JsonObject();
         dimletJson.addProperty("t", dimletDescriptor.type().getShortName());
         dimletJson.addProperty("n", dimletDescriptor.key());
         root.add(dimletJson);
      }

      return GSON_COMPACT.toJson(root);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         DimensionDescriptor that = (DimensionDescriptor)o;
         return Objects.equals(this.dimlets, that.dimlets);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.dimlets);
   }

   public void dump(Player player) {
      for (DimletKey dimlet : this.dimlets) {
         player.sendSystemMessage(
            ComponentFactory.literal(dimlet.type().name() + ": ")
               .withStyle(ChatFormatting.AQUA)
               .append(ComponentFactory.literal(dimlet.key()).withStyle(ChatFormatting.WHITE))
         );
      }
   }

   public void log(String header) {
      StringBuilder headerBuilder = new StringBuilder("--------------------------------------------------\n" + header);

      for (DimletKey dimlet : this.dimlets) {
         headerBuilder.append("\n    ").append(dimlet.type().name()).append(": ").append(dimlet.key());
      }

      header = headerBuilder.toString();
      header = header + "\n--------------------------------------------------";
      RFToolsDim.setup.getLogger().info(header);
   }

   public DimensionDescriptor createRandomizedDescriptor(Random random) {
      DimensionDescriptor randomizedDescriptor = new DimensionDescriptor();
      List<DimletKey> dimlets = this.getDimlets();
      List<DimletKey> randomized = randomizedDescriptor.getDimlets();
      if (!this.has(dimlets, DimletType.TERRAIN)) {
         DimletKey terrainDimlet = DimletDictionary.get().getRandomDimlet(DimletType.TERRAIN, random);
         if (terrainDimlet != null) {
            this.addRandomDimlets(DimletType.BLOCK, randomized, random, Math.min(1, random.nextInt(5) - 2));
            this.addAttributeDimlets(randomized, random);
            this.addFluidDimlets(randomized, random, 1);
            randomized.add(terrainDimlet);
         }
      }

      if (!this.has(dimlets, DimletType.STRUCTURE)) {
         if (random.nextFloat() < 0.05) {
            randomized.add(new DimletKey(DimletType.STRUCTURE, "none"));
         }

         if (random.nextFloat() < 0.6) {
            randomized.add(new DimletKey(DimletType.STRUCTURE, "default"));
         } else {
            this.addRandomDimlets(DimletType.STRUCTURE, randomized, random, random.nextInt(4) + 1);
         }
      }

      if (!this.has(dimlets, DimletType.SKY)) {
         this.addRandomDimlets(DimletType.SKY, randomized, random, Math.max(0, random.nextInt(9) - 5));
      }

      if (!this.has(dimlets, DimletType.FEATURE)) {
         int cnt = random.nextInt(4);

         for (int i = 0; i < cnt - 1; i++) {
            DimletKey featureDimlet = DimletDictionary.get().getRandomDimlet(DimletType.FEATURE, random);
            if (featureDimlet != null) {
               if (random.nextFloat() < 0.2F) {
                  this.addTagDimlets(randomized, random);
               } else {
                  this.addBlockDimlets(randomized, random);
               }

               randomized.add(featureDimlet);
            }
         }
      }

      boolean addedBiomeCategories = false;
      if (!this.has(dimlets, DimletType.BIOME_CATEGORY) && !this.has(dimlets, DimletType.BIOME)) {
         int cnt = random.nextInt(4);

         for (int ix = 0; ix < cnt - 2; ix++) {
            DimletKey categoryDimlet = DimletDictionary.get().getRandomDimlet(DimletType.BIOME_CATEGORY, random);
            if (categoryDimlet != null) {
               randomized.add(categoryDimlet);
               addedBiomeCategories = true;
            }
         }
      }

      if (!this.has(dimlets, DimletType.BIOME_CONTROLLER)) {
         DimletKey controllerDimlet = DimletDictionary.get().getRandomDimlet(DimletType.BIOME_CONTROLLER, random);
         if (controllerDimlet != null) {
            if (!addedBiomeCategories) {
               this.addBiomeDimlets(randomized, random);
            }

            randomized.add(controllerDimlet);
         }
      }

      if (!this.has(dimlets, DimletType.TIME)) {
         DimletKey timeDimlet = DimletDictionary.get().getRandomDimlet(DimletType.TIME, random);
         if (timeDimlet != null) {
            randomized.add(timeDimlet);
         }
      }

      return randomizedDescriptor;
   }

   private void addAttributeDimlets(List<DimletKey> randomized, Random random) {
      this.addRandomDimlets(DimletType.ATTRIBUTE, randomized, random, random.nextInt(3));
   }

   private void addBiomeDimlets(List<DimletKey> randomized, Random random) {
      this.addRandomDimlets(DimletType.BIOME, randomized, random, random.nextInt(8) + 1);
   }

   private void addFluidDimlets(List<DimletKey> randomized, Random random, int max) {
      this.addRandomDimlets(DimletType.FLUID, randomized, random, random.nextInt(Math.min(3, max + 1)));
   }

   private void addTagDimlets(List<DimletKey> randomized, Random random) {
      this.addRandomDimlets(DimletType.TAG, randomized, random, Math.max(1, random.nextInt(4) - 2));
   }

   private void addBlockDimlets(List<DimletKey> randomized, Random random) {
      this.addRandomDimlets(DimletType.BLOCK, randomized, random, Math.max(1, random.nextInt(6) - 2));
   }

   private void addRandomDimlets(DimletType type, List<DimletKey> randomized, Random random, int cnt) {
      for (int i = 0; i < cnt; i++) {
         DimletKey dimlet = DimletDictionary.get().getRandomDimlet(type, random);
         if (dimlet != null) {
            randomized.add(dimlet);
         }
      }
   }

   private boolean has(List<DimletKey> dimlets, DimletType type) {
      return dimlets.stream().anyMatch(key -> key.type() == type);
   }
}
