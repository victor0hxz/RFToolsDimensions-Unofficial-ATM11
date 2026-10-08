package mcjty.rftoolsdim.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.lib.varia.TeleportationTools;
import mcjty.rftoolsdim.RFToolsDim;
import mcjty.rftoolsdim.dimension.data.DimensionCreator;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class CommandTpDim implements Command<CommandSourceStack> {
   private static final CommandTpDim CMD = new CommandTpDim();

   public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
      return ((LiteralArgumentBuilder)Commands.literal("tp").requires(Commands.hasPermission(Commands.LEVEL_MODERATORS)))
         .then(Commands.argument("name", StringArgumentType.string()).executes(CMD));
   }

   public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      SharedConstants.IS_RUNNING_IN_IDE = true;
      String name = (String)context.getArgument("name", String.class);
      ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
      int x = player.blockPosition().getX();
      int z = player.blockPosition().getZ();
      Level world = DimensionCreator.get().getDimWorld(name);
      if (world == null) {
         RFToolsDim.setup.getLogger().error("Can't find dimension '" + name + "'!");
         return 0;
      } else {
         ResourceKey<Level> id = world.dimension();
         TeleportationTools.teleport(player, id, x, 200.0, z, Direction.NORTH);
         return 0;
      }
   }
}
