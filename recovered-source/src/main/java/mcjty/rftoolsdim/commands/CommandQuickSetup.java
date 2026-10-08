package mcjty.rftoolsdim.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsdim.modules.dimensionbuilder.DimensionBuilderModule;
import mcjty.rftoolsdim.modules.dimlets.data.DimletKey;
import mcjty.rftoolsdim.modules.dimlets.data.DimletTools;
import mcjty.rftoolsdim.modules.dimlets.data.DimletType;
import mcjty.rftoolsdim.modules.enscriber.EnscriberModule;
import mcjty.rftoolsdim.modules.workbench.WorkbenchModule;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class CommandQuickSetup implements Command<CommandSourceStack> {
   private static final CommandQuickSetup CMD = new CommandQuickSetup();

   public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
      return ((LiteralArgumentBuilder)Commands.literal("quicksetup").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).executes(CMD);
   }

   public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      ServerLevel world = ((CommandSourceStack)context.getSource()).getLevel();
      ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
      BlockPos pos = player.blockPosition();
      BlockState quartz = Blocks.QUARTZ_BLOCK.defaultBlockState();
      BlockState glass = Blocks.GLASS.defaultBlockState();
      BlockState glowstone = Blocks.GLOWSTONE.defaultBlockState();
      BlockState air = Blocks.AIR.defaultBlockState();

      for (int dx = -5; dx <= 5; dx++) {
         for (int dz = -5; dz <= 5; dz++) {
            world.setBlock(pos.offset(dx, -1, dz), quartz, 3);

            for (int y = 0; y < 5; y++) {
               if (dx != -5 && dx != 5 && dz != 5 && dz != -5) {
                  world.setBlock(pos.offset(dx, y, dz), air, 3);
               } else if (y == 4) {
                  world.setBlock(pos.offset(dx, y, dz), glowstone, 3);
               } else {
                  world.setBlock(pos.offset(dx, y, dz), glass, 3);
               }
            }
         }
      }

      Block matterTransmitter = Tools.getBlock(Identifier.fromNamespaceAndPath("rftoolsutility", "matter_transmitter"));
      Block matterReceiver = Tools.getBlock(Identifier.fromNamespaceAndPath("rftoolsutility", "matter_receiver"));
      Block dialingDevice = Tools.getBlock(Identifier.fromNamespaceAndPath("rftoolsutility", "dialing_device"));
      Block creativeCell = Tools.getBlock(Identifier.fromNamespaceAndPath("rftoolspower", "dimensionalcell_creative"));
      world.setBlock(pos.offset(-4, -1, -4), matterTransmitter.defaultBlockState(), 3);
      world.setBlock(pos.offset(-3, -1, -4), creativeCell.defaultBlockState(), 3);
      world.setBlock(pos.offset(-2, -1, -4), matterReceiver.defaultBlockState(), 3);
      world.setBlock(pos.offset(-3, -1, -3), (BlockState)dialingDevice.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.UP), 3);
      world.setBlock(pos.offset(3, 0, -4), creativeCell.defaultBlockState(), 3);
      world.setBlock(
         pos.offset(4, 0, -4),
         (BlockState)((BaseBlock)DimensionBuilderModule.DIMENSION_BUILDER.block().get())
            .defaultBlockState()
            .setValue(BlockStateProperties.FACING, Direction.SOUTH),
         3
      );
      world.setBlock(pos.offset(3, 0, 4), ((BaseBlock)WorkbenchModule.WORKBENCH.block().get()).defaultBlockState(), 3);
      world.setBlock(pos.offset(4, 0, 4), ((BaseBlock)EnscriberModule.ENSCRIBER.block().get()).defaultBlockState(), 3);
      player.addItem(new ItemStack((ItemLike)DimensionBuilderModule.EMPTY_DIMENSION_TAB.get()));
      player.addItem(DimletTools.getDimletStack(new DimletKey(DimletType.DIGIT, "1")));
      player.addItem(DimletTools.getDimletStack(new DimletKey(DimletType.DIGIT, "2")));
      player.addItem(DimletTools.getDimletStack(new DimletKey(DimletType.DIGIT, "3")));
      player.addItem(DimletTools.getDimletStack(new DimletKey(DimletType.DIGIT, "4")));
      player.addItem(DimletTools.getDimletStack(new DimletKey(DimletType.TERRAIN, "flat")));
      player.addItem(DimletTools.getDimletStack(new DimletKey(DimletType.TERRAIN, "normal")));
      Item chargedPorter = Tools.getItem(Identifier.fromNamespaceAndPath("rftoolsutility", "advanced_charged_porter"));
      player.addItem(new ItemStack(chargedPorter));
      player.addItem(new ItemStack(chargedPorter));
      return 0;
   }
}
