package mcjty.rftoolsdim.modules.dimensionbuilder.client;

import javax.annotation.Nonnull;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.widgets.EnergyBar;
import mcjty.lib.gui.widgets.ImageChoiceLabel;
import mcjty.lib.gui.widgets.ImageLabel;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.Widget;
import mcjty.lib.gui.widgets.Widgets;
import mcjty.lib.varia.RedstoneMode;
import mcjty.rftoolsdim.modules.dimensionbuilder.DimensionBuilderModule;
import mcjty.rftoolsdim.modules.dimensionbuilder.blocks.DimensionBuilderTileEntity;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class GuiDimensionBuilder extends GenericGuiContainer<DimensionBuilderTileEntity, GenericContainer> {
   public static final int BUILDER_WIDTH = 180;
   public static final int BUILDER_HEIGHT = 152;
   private EnergyBar energyBar;
   private ImageLabel stages;
   private Label percentage;
   private Label error1;
   private Label error2;
   private static final Identifier iconLocation = Identifier.fromNamespaceAndPath("rftoolsdim", "textures/gui/dimensionbuilder.png");
   private static final Identifier iconStages = Identifier.fromNamespaceAndPath("rftoolsdim", "textures/gui/dimensionbuilderstages.png");
   private static final Identifier iconGuiElements = Identifier.fromNamespaceAndPath("rftoolsbase", "textures/gui/guielements.png");

   public GuiDimensionBuilder(GenericContainer container, Inventory inventory, Component title) {
      super(container, inventory, title, ((BaseBlock)DimensionBuilderModule.DIMENSION_BUILDER.block().get()).getManualEntry(), 180, 152);
   }

   public void init() {
      super.init();
      this.energyBar = ((EnergyBar)new EnergyBar().vertical().hint(10, 7, 8, 54)).showText(false);
      this.stages = (ImageLabel)((ImageLabel)new ImageLabel().image(iconStages, 0, 0)).hint(61, 9, 48, 48);
      this.percentage = (Label)Widgets.label(115, 25, 50, 16, "0%").horizontalAlignment(HorizontalAlignment.ALIGN_LEFT);
      this.error1 = (Label)((Label)Widgets.label(115, 15, 60, 16, "").horizontalAlignment(HorizontalAlignment.ALIGN_LEFT)).color(16711680);
      this.error2 = (Label)((Label)Widgets.label(115, 28, 60, 16, "").horizontalAlignment(HorizontalAlignment.ALIGN_LEFT)).color(16711680);
      ImageChoiceLabel redstoneMode = this.initRedstoneMode();
      Panel toplevel = (Panel)((Panel)Widgets.positional().background(iconLocation))
         .children(new Widget[]{this.energyBar, this.stages, this.percentage, this.error1, this.error2, redstoneMode});
      toplevel.bounds(this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
      this.window = new Window(this, toplevel);
      DimensionBuilderTileEntity te = (DimensionBuilderTileEntity)this.getBE();
      if (te != null) {
         this.window.bind("redstone", te, "rsmode");
      }
   }

   private ImageChoiceLabel initRedstoneMode() {
      return ((ImageChoiceLabel)Widgets.imageChoice(150, 46, 16, 16).name("redstone"))
         .choice(RedstoneMode.REDSTONE_IGNORED.getDescription(), "Redstone mode:\nIgnored", iconGuiElements, 0, 0)
         .choice(RedstoneMode.REDSTONE_OFFREQUIRED.getDescription(), "Redstone mode:\nOff to activate", iconGuiElements, 16, 0)
         .choice(RedstoneMode.REDSTONE_ONREQUIRED.getDescription(), "Redstone mode:\nOn to activate", iconGuiElements, 32, 0);
   }

   public void extractBackground(@Nonnull GuiGraphicsExtractor graphics, int x, int y, float partialTicks) {
      DimensionBuilderTileEntity te = (DimensionBuilderTileEntity)this.getBE();
      int pct = te != null ? te.getBuildPercentage() : 0;
      int error = te != null ? te.getErrorMode() : 0;
      if (error == -1) {
         this.error1.text("Builder has");
         this.error2.text("no owner!");
         this.percentage.text("");
      } else if (error == -2) {
         this.error1.text("Too many");
         this.error2.text("dimensions!");
         this.percentage.text("");
      } else if (error == -3) {
         this.error1.text("Duplicate");
         this.error2.text("name!");
         this.percentage.text("");
      } else {
         int px = (pct - 1) / 4 % 5;
         int py = (pct - 1) / 4 / 5;
         this.stages.image(iconStages, px * 48, py * 48);
         this.percentage.text(pct + "%");
         this.error1.text("");
         this.error2.text("");
      }

      this.drawWindow(graphics, partialTicks, x, y);
      this.updateEnergyBar(this.energyBar);
   }
}
