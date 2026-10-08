package mcjty.rftoolsdim.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.HashSet;
import java.util.Map.Entry;
import mcjty.lib.varia.ComponentFactory;
import mcjty.rftoolsdim.dimension.data.DimensionCreator;
import mcjty.rftoolsdim.dimension.data.DimensionData;
import mcjty.rftoolsdim.dimension.data.PersistantDimensionManager;
import mcjty.rftoolsdim.dimension.descriptor.CompiledDescriptor;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;

public class CommandForgetInvalid implements Command<CommandSourceStack> {
   private static final CommandForgetInvalid CMD = new CommandForgetInvalid();

   public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
      return ((LiteralArgumentBuilder)Commands.literal("forgetinvalid").requires(Commands.hasPermission(Commands.LEVEL_MODERATORS))).executes(CMD);
   }

   public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      SharedConstants.IS_RUNNING_IN_IDE = true;
      ServerLevel world = ((CommandSourceStack)context.getSource()).getLevel();
      PersistantDimensionManager mgr = PersistantDimensionManager.get(world);

      for (Entry<Identifier, DimensionData> entry : new HashSet<>(mgr.getData().entrySet())) {
         CompiledDescriptor descriptor = DimensionCreator.get().getCompiledDescriptor(world, entry.getKey());
         if (descriptor == null) {
            mgr.forget(entry.getKey());
            ((CommandSourceStack)context.getSource())
               .sendSuccess(() -> ComponentFactory.literal(ChatFormatting.YELLOW + "Removed '" + entry.getKey() + "'"), false);
         }
      }

      return 0;
   }
}
