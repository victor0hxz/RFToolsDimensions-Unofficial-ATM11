package mcjty.rftoolsdim.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ModCommands {
   public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
      LiteralCommandNode<CommandSourceStack> commands = dispatcher.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                                             "rftoolsdim"
                                          )
                                          .then(CommandCreateDim.register(dispatcher)))
                                       .then(CommandListDim.register(dispatcher)))
                                    .then(CommandRefreshChunks.register(dispatcher)))
                                 .then(CommandForget.register(dispatcher)))
                              .then(CommandForgetInvalid.register(dispatcher)))
                           .then(CommandTpDim.register(dispatcher)))
                        .then(CommandDump.register(dispatcher)))
                     .then(CommandSetPower.register(dispatcher)))
                  .then(CommandWeather.register(dispatcher)))
               .then(CommandCreateConfig.register(dispatcher)))
            .then(CommandQuickSetup.register(dispatcher))
      );
      dispatcher.register((LiteralArgumentBuilder)Commands.literal("dim").redirect(commands));
   }
}
