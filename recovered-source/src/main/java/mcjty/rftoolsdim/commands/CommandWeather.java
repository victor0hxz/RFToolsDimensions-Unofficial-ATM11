package mcjty.rftoolsdim.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.lib.varia.ComponentFactory;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CommandWeather implements Command<CommandSourceStack> {
   private static final CommandWeather CMD = new CommandWeather();

   public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
      return ((LiteralArgumentBuilder)Commands.literal("weather").requires(Commands.hasPermission(Commands.LEVEL_MODERATORS))).executes(CMD);
   }

   public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      ((CommandSourceStack)context.getSource()).sendSuccess(() -> ComponentFactory.translatable("commands.weather.set.clear"), true);
      return 0;
   }
}
