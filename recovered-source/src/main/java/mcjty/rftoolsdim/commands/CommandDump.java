package mcjty.rftoolsdim.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.lib.varia.ComponentFactory;
import mcjty.rftoolsdim.dimension.data.DimensionData;
import mcjty.rftoolsdim.dimension.data.DimensionSettings;
import mcjty.rftoolsdim.dimension.data.PersistantDimensionManager;
import mcjty.rftoolsdim.dimension.descriptor.DimensionDescriptor;
import mcjty.rftoolsdim.dimension.terraintypes.RFToolsChunkGenerator;
import mcjty.rftoolsdim.modules.dimlets.data.DimletKey;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;

public class CommandDump implements Command<CommandSourceStack> {
   private static final CommandDump CMD = new CommandDump();
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

   public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
      return ((LiteralArgumentBuilder)Commands.literal("dump").requires(Commands.hasPermission(Commands.LEVEL_ALL))).executes(CMD);
   }

   public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      ServerLevel world = ((CommandSourceStack)context.getSource()).getLevel();
      Identifier location = world.dimension().identifier();
      this.feedback(context, ChatFormatting.BLUE + "Dimension: " + ChatFormatting.WHITE + location.toString());
      DimensionData data = PersistantDimensionManager.get(world).getData(location);
      if (data == null) {
         this.feedback(context, ChatFormatting.RED + "Not an RFTools Dimensions!");
         return 0;
      } else {
         this.feedback(context, ChatFormatting.YELLOW + "Owner: " + ChatFormatting.WHITE + data.getOwner());
         this.feedback(context, ChatFormatting.BLUE + "Energy: " + ChatFormatting.WHITE + data.getEnergy());
         ChunkGenerator generator = world.getChunkSource().getGenerator();
         if (generator instanceof RFToolsChunkGenerator) {
            DimensionSettings settings = ((RFToolsChunkGenerator)generator).getDimensionSettings();
            this.feedback(context, ChatFormatting.BLUE + "Seed: " + ChatFormatting.WHITE + settings.getSeed());
         }

         DimensionDescriptor descriptor = data.getDescriptor();
         this.feedback(context, ChatFormatting.GREEN + "Standard dimlets:");

         for (DimletKey dimlet : descriptor.getDimlets()) {
            this.feedback(context, ChatFormatting.BLUE + "    " + dimlet.type().name() + ": " + ChatFormatting.WHITE + dimlet.key());
         }

         DimensionDescriptor randomizedDescriptor = data.getRandomizedDescriptor();
         this.feedback(context, ChatFormatting.GREEN + "Randomized dimlets:");

         for (DimletKey dimlet : randomizedDescriptor.getDimlets()) {
            this.feedback(context, ChatFormatting.BLUE + "    " + dimlet.type().name() + ": " + ChatFormatting.WHITE + dimlet.key());
         }

         return 0;
      }
   }

   private void feedback(CommandContext<CommandSourceStack> context, String message) {
      ((CommandSourceStack)context.getSource()).sendSuccess(() -> ComponentFactory.literal(message), false);
   }
}
