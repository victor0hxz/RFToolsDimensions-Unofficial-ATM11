package mcjty.rftoolsdim.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.lib.varia.ComponentFactory;
import mcjty.rftoolsdim.dimension.data.PersistantDimensionManager;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.resources.Identifier;

public class CommandForget implements Command<CommandSourceStack> {
   private static final CommandForget CMD = new CommandForget();

   public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
      return ((LiteralArgumentBuilder)Commands.literal("forget").requires(Commands.hasPermission(Commands.LEVEL_MODERATORS)))
         .then(Commands.argument("name", StringArgumentType.string()).executes(CMD));
   }

   public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      SharedConstants.IS_RUNNING_IN_IDE = true;
      String name = (String)context.getArgument("name", String.class);
      PersistantDimensionManager mgr = PersistantDimensionManager.get(((CommandSourceStack)context.getSource()).getLevel());
      mgr.forget(Identifier.fromNamespaceAndPath("rftoolsdim", name));
      ((CommandSourceStack)context.getSource()).sendSuccess(() -> ComponentFactory.literal(ChatFormatting.YELLOW + "Removed '" + name + "'"), false);
      return 0;
   }
}
