package mcjty.rftoolsdim.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.lib.varia.ComponentFactory;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsdim.dimension.data.DimensionData;
import mcjty.rftoolsdim.dimension.data.PersistantDimensionManager;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CommandSetPower implements Command<CommandSourceStack> {
   private static final CommandSetPower CMD = new CommandSetPower();

   public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
      return ((LiteralArgumentBuilder)Commands.literal("setpower").requires(Commands.hasPermission(Commands.LEVEL_MODERATORS)))
         .then(Commands.argument("power", LongArgumentType.longArg()).executes(CMD));
   }

   public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      SharedConstants.IS_RUNNING_IN_IDE = true;
      long power = (Long)context.getArgument("power", Long.class);
      PersistantDimensionManager mgr = PersistantDimensionManager.get(((CommandSourceStack)context.getSource()).getLevel());
      DimensionData data = mgr.getData(((CommandSourceStack)context.getSource()).getLevel().dimension().identifier());
      if (data == null) {
         ((CommandSourceStack)context.getSource()).sendFailure(ComponentFactory.literal("Not an RFTools Dimension!"));
      } else {
         data.setEnergy(LevelTools.getOverworld(((CommandSourceStack)context.getSource()).getLevel()), power);
         ((CommandSourceStack)context.getSource()).sendFailure(ComponentFactory.literal("Power set to " + power));
      }

      return 0;
   }
}
