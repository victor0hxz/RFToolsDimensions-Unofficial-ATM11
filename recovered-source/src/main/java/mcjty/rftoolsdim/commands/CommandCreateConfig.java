package mcjty.rftoolsdim.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.io.IOException;
import mcjty.lib.varia.ComponentFactory;
import mcjty.rftoolsdim.modules.dimlets.data.DimletPackages;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CommandCreateConfig implements Command<CommandSourceStack> {
   private static final CommandCreateConfig CMD = new CommandCreateConfig();

   public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
      return ((LiteralArgumentBuilder)Commands.literal("config").requires(Commands.hasPermission(Commands.LEVEL_ALL)))
         .then(Commands.argument("filename", StringArgumentType.word()).then(Commands.argument("modid", StringArgumentType.string()).executes(CMD)));
   }

   public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      String filename = (String)context.getArgument("filename", String.class);
      String modid = (String)context.getArgument("modid", String.class);

      try {
         DimletPackages.writePackage(filename, modid);
      } catch (IOException var5) {
         ((CommandSourceStack)context.getSource()).sendSuccess(() -> ComponentFactory.literal(ChatFormatting.RED + var5.getMessage()), true);
      }

      return 0;
   }
}
