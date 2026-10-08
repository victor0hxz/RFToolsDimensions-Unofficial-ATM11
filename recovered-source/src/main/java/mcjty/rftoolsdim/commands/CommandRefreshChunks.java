package mcjty.rftoolsdim.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.function.Consumer;
import mcjty.rftoolsdim.dimension.noisesettings.NoiseSamplingSettingsBuilder;
import mcjty.rftoolsdim.dimension.noisesettings.NoiseSettingsBuilder;
import mcjty.rftoolsdim.dimension.noisesettings.NoiseSliderBuilder;
import mcjty.rftoolsdim.dimension.terraintypes.RFToolsChunkGenerator;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import org.apache.commons.lang3.StringUtils;

public class CommandRefreshChunks implements Command<CommandSourceStack> {
   private static final CommandRefreshChunks CMD = new CommandRefreshChunks();
   public static final String XZSCALE = "xzscale";
   public static final String YSCALE = "yscale";
   public static final String XZFACTOR = "xzfactor";
   public static final String YFACTOR = "yfactor";
   public static final String TOPOFFSET = "topoffset";
   public static final String TOPSIZE = "topsize";
   public static final String TOPTARGET = "toptarget";
   public static final String BOTTOMOFFSET = "bottomoffset";
   public static final String BOTTOMSIZE = "bottomsize";
   public static final String BOTTOMTARGET = "bottomtarget";
   public static final String HORIZONTAL = "horizontal";
   public static final String VERTICAL = "vertical";

   public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
      return ((LiteralArgumentBuilder)Commands.literal("refreshchunks").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)))
         .then(Commands.argument("par", StringArgumentType.string()).executes(CMD));
   }

   public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      SharedConstants.IS_RUNNING_IN_IDE = true;
      ServerLevel level = ((CommandSourceStack)context.getSource()).getLevel();
      if (level.getChunkSource().getGenerator() instanceof RFToolsChunkGenerator chunkGenerator) {
         String par = (String)context.getArgument("par", String.class);
         String[] split = StringUtils.split(par, '=');
         String cmd = split[0].toLowerCase();
         NoiseGeneratorSettings settings = chunkGenerator.getNoiseGeneratorSettings();
         if ("list".equals(cmd)) {
            this.dump(settings);
         } else if (split.length <= 1) {
            double result = switch (cmd) {
               case "horizontal" -> settings.noiseSettings().noiseSizeHorizontal();
               case "vertical" -> settings.noiseSettings().noiseSizeVertical();
               default -> 0.0;
            };
            System.out.println(cmd + " = " + result);
         } else {
            double doubleValue = asDouble(split[1]);
            int intValue = asInt(split[1]);

            Consumer<NoiseSettingsBuilder> noiseBuilder = switch (cmd) {
               case "horizontal" -> builder -> builder.noiseSizeHorizontal(intValue);
               case "vertical" -> builder -> builder.noiseSizeVertical(intValue);
               default -> builder -> {};
            };

            Consumer<NoiseSamplingSettingsBuilder> samplingBuilder = switch (cmd) {
               case "xzscale" -> builder -> builder.xzScale(doubleValue);
               case "yscale" -> builder -> builder.yScale(doubleValue);
               case "xzfactor" -> builder -> builder.xzFactor(doubleValue);
               case "yfactor" -> builder -> builder.yFactor(doubleValue);
               default -> builder -> {};
            };

            Consumer<NoiseSliderBuilder> topSliderBuilder = switch (cmd) {
               case "topoffset" -> builder -> builder.offset(intValue);
               case "topsize" -> builder -> builder.size(intValue);
               case "toptarget" -> builder -> builder.top(doubleValue);
               default -> builder -> {};
            };

            Consumer<NoiseSliderBuilder> bottomSliderBuilder = switch (cmd) {
               case "bottomoffset" -> builder -> builder.offset(intValue);
               case "bottomsize" -> builder -> builder.size(intValue);
               case "bottomtarget" -> builder -> builder.top(doubleValue);
               default -> builder -> {};
            };
            chunkGenerator.changeSettings(noiseBuilder, samplingBuilder, topSliderBuilder, bottomSliderBuilder);
         }
      }

      return 0;
   }

   private void dump(NoiseGeneratorSettings settings) {
   }

   private static double asDouble(String d) {
      try {
         return Double.parseDouble(d);
      } catch (NumberFormatException var2) {
         return 0.0;
      }
   }

   private static int asInt(String d) {
      try {
         return Integer.parseInt(d);
      } catch (NumberFormatException var2) {
         return 0;
      }
   }
}
