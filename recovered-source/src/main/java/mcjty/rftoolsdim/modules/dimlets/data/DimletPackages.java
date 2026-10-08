package mcjty.rftoolsdim.modules.dimlets.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.MapCodec;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import mcjty.lib.varia.TagTools;
import mcjty.rftoolsdim.RFToolsDim;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.fml.loading.FMLPaths;

public class DimletPackages {
   public static void writePackage(String filename, String modid) throws IOException {
      Path configPath = FMLPaths.CONFIGDIR.get();
      new File(configPath + File.separator + "rftoolsdim").mkdirs();
      JsonArray root = new JsonArray();
      writeStructures(root, modid);
      writeBlocks(root, modid);
      writeFluids(root, modid);
      writeBiomes(root, modid);
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      String json = gson.toJson(root);
      File file = new File(configPath + File.separator + "rftoolsdim" + File.separator + filename);

      try (FileWriter writer = new FileWriter(file)) {
         writer.write(json);
      }
   }

   private static void writeBiomes(JsonArray root, String modid) {
      Set<DimletKey> dimlets = DimletDictionary.get().getDimlets();

      for (Entry<ResourceKey<MapCodec<? extends BiomeSource>>, MapCodec<? extends BiomeSource>> entry : BuiltInRegistries.BIOME_SOURCE.entrySet()) {
         Identifier id = entry.getKey().identifier();
         if (modid.toLowerCase().equals(id.getNamespace()) && !dimlets.contains(new DimletKey(DimletType.BIOME, id.toString()))) {
            JsonObject object = new JsonObject();
            object.addProperty("type", DimletType.BIOME.name().toLowerCase());
            object.addProperty("key", id.toString());
            DimletSettings settings = DimletSettings.create(DimletRarity.COMMON, 10, 10, 1).dimlet(true).worldgen(true).build();
            settings.buildElement(object);
            root.add(object);
         }
      }
   }

   private static void writeFluids(JsonArray root, String modid) {
      Set<DimletKey> dimlets = DimletDictionary.get().getDimlets();

      for (Entry<ResourceKey<Fluid>, Fluid> entry : BuiltInRegistries.FLUID.entrySet()) {
         Identifier id = entry.getKey().identifier();
         if (modid.toLowerCase().equals(id.getNamespace())) {
            Fluid fluid = entry.getValue();
            if (fluid.defaultFluidState().createLegacyBlock().getBlock() != Blocks.AIR && !dimlets.contains(new DimletKey(DimletType.FLUID, id.toString()))) {
               JsonObject object = new JsonObject();
               object.addProperty("type", DimletType.FLUID.name().toLowerCase());
               object.addProperty("key", id.toString());
               DimletSettings settings = DimletSettings.create(DimletRarity.COMMON, 10, 10, 10).dimlet(true).worldgen(true).build();
               settings.buildElement(object);
               root.add(object);
            }
         }
      }
   }

   private static void writeBlocks(JsonArray root, String modid) {
      Set<DimletKey> dimlets = DimletDictionary.get().getDimlets();

      for (Entry<ResourceKey<Block>, Block> entry : BuiltInRegistries.BLOCK.entrySet()) {
         Identifier id = entry.getKey().identifier();
         if (modid.toLowerCase().equals(id.getNamespace())) {
            Block block = entry.getValue();
            boolean hasTileEntity = block.defaultBlockState().hasBlockEntity();
            if (!hasTileEntity && !dimlets.contains(new DimletKey(DimletType.BLOCK, id.toString()))) {
               boolean isOre = TagTools.hasTag(block, net.neoforged.neoforge.common.Tags.Blocks.ORES);
               JsonObject object = new JsonObject();
               object.addProperty("type", DimletType.BLOCK.name().toLowerCase());
               object.addProperty("key", id.toString());
               DimletSettings settings = DimletSettings.create(
                     isOre ? DimletRarity.UNCOMMON : DimletRarity.COMMON, isOre ? 100 : 10, isOre ? 100 : 10, isOre ? 100 : 10
                  )
                  .dimlet(true)
                  .worldgen(true)
                  .build();
               settings.buildElement(object);
               root.add(object);
            }
         }
      }
   }

   private static void writeStructures(JsonArray root, String modid) {
      Set<DimletKey> dimlets = DimletDictionary.get().getDimlets();

      for (Entry<ResourceKey<StructureType<?>>, StructureType<?>> entry : BuiltInRegistries.STRUCTURE_TYPE.entrySet()) {
         Identifier id = entry.getKey().identifier();
         if (modid.toLowerCase().equals(id.getNamespace()) && !dimlets.contains(new DimletKey(DimletType.STRUCTURE, id.toString()))) {
            JsonObject object = new JsonObject();
            object.addProperty("type", DimletType.STRUCTURE.name().toLowerCase());
            object.addProperty("key", id.toString());
            DimletSettings settings = DimletSettings.create(DimletRarity.UNCOMMON, 100, 100, 100).dimlet(true).worldgen(true).build();
            settings.buildElement(object);
            root.add(object);
         }
      }
   }

   public static void readPackage(String filename, BiFunction<DimletKey, DimletSettings, Boolean> consumer) {
      InputStream inputStream = null;
      Path configPath = FMLPaths.CONFIGDIR.get();
      new File(configPath + File.separator + "rftoolsdim").mkdirs();
      File file = new File(configPath + File.separator + "rftoolsdim" + File.separator + filename);
      if (file.exists()) {
         try {
            inputStream = new FileInputStream(file);
         } catch (FileNotFoundException var18) {
            throw new UncheckedIOException(var18);
         }
      }

      if (inputStream == null) {
         inputStream = RFToolsDim.class.getResourceAsStream("/data/rftoolsdim/dimletpackages/" + filename);
         if (inputStream == null) {
            RFToolsDim.setup.getLogger().error("Can't find dimlet package: " + filename);
            throw new IllegalStateException("Can't find dimlet package: " + filename);
         }
      }

      int cnt = 0;

      try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
         JsonElement root = JsonParser.parseReader(br);

         for (JsonElement element : root.getAsJsonArray()) {
            JsonObject object = element.getAsJsonObject();
            String type = object.getAsJsonPrimitive("type").getAsString();
            String key = object.getAsJsonPrimitive("key").getAsString();
            DimletKey dimletKey = new DimletKey(DimletType.byName(type), key);
            DimletSettings settings = DimletSettings.parse(object);
            Boolean success = consumer.apply(dimletKey, settings);
            if (Boolean.TRUE.equals(success)) {
               cnt++;
            }
         }
      } catch (IOException var20) {
         RFToolsDim.setup.getLogger().error("Error loading dimlet package: " + filename);
         throw new UncheckedIOException(var20);
      }

      RFToolsDim.setup.getLogger().info("Reading dimlet package: " + filename + ", " + cnt + " valid dimlets found");
   }
}
