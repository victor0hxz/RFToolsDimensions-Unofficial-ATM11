package mcjty.rftoolsdim.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.lib.varia.ComponentFactory;
import mcjty.rftoolsdim.dimension.data.DimensionCreator;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CommandCreateDim implements Command<CommandSourceStack> {
   private static final CommandCreateDim CMD = new CommandCreateDim();

   public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
      return ((LiteralArgumentBuilder)Commands.literal("create").requires(Commands.hasPermission(Commands.LEVEL_MODERATORS)))
         .then(
            Commands.argument("name", StringArgumentType.word())
               .then(Commands.argument("descriptor", StringArgumentType.string()).then(Commands.argument("seed", LongArgumentType.longArg()).executes(CMD)))
         );
   }

   public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      SharedConstants.IS_RUNNING_IN_IDE = true;
      String name = (String)context.getArgument("name", String.class);
      String descriptor = (String)context.getArgument("descriptor", String.class);
      long seed = (Long)context.getArgument("seed", Long.class);
      String error = DimensionCreator.get()
         .createDimension(
            ((CommandSourceStack)context.getSource()).getLevel(),
            name,
            seed,
            descriptor,
            ((CommandSourceStack)context.getSource()).getPlayerOrException().getUUID()
         );
      if (error != null) {
         ((CommandSourceStack)context.getSource()).sendSuccess(() -> ComponentFactory.literal(ChatFormatting.RED + error), true);
      }

      return 0;
   }
}
