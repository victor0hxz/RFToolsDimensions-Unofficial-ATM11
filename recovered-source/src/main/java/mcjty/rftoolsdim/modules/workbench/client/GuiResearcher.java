package mcjty.rftoolsdim.modules.workbench.client;

import javax.annotation.Nonnull;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.EnergyBar;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.Widget;
import mcjty.lib.gui.widgets.Widgets;
import mcjty.rftoolsdim.modules.workbench.WorkbenchModule;
import mcjty.rftoolsdim.modules.workbench.blocks.ResearcherTileEntity;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class GuiResearcher extends GenericGuiContainer<ResearcherTileEntity, GenericContainer> {
   public static final int WIDTH = 180;
   public static final int HEIGHT = 152;
   private EnergyBar energyBar;
   private Label progress;
   private static final Identifier iconLocation = Identifier.fromNamespaceAndPath("rftoolsdim", "textures/gui/researcher.png");

   public GuiResearcher(GenericContainer container, Inventory inventory, Component title) {
      super(container, inventory, title, ((BaseBlock)WorkbenchModule.RESEARCHER.block().get()).getManualEntry(), 180, 152);
   }

   public void init() {
      super.init();
      this.energyBar = ((EnergyBar)((EnergyBar)new EnergyBar().name("energybar")).vertical().hint(10, 7, 8, 54)).showText(false);
      this.progress = Widgets.label(64, 44, 70, 18, "");
      Panel toplevel = (Panel)((Panel)Widgets.positional().background(iconLocation)).children(new Widget[]{this.energyBar, this.progress});
      toplevel.bounds(this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
      this.window = new Window(this, toplevel);
      this.initializeFields();
   }

   private void initializeFields() {
      this.energyBar = (EnergyBar)this.window.findChild("energybar");
   }

   private void updateFields() {
      if (this.window != null) {
         this.updateEnergyBar(this.energyBar);
         ResearcherTileEntity te = (ResearcherTileEntity)this.getBE();
         this.progress.text(te != null ? te.getProgressPercentage() + "%" : "");
      }
   }

   public void extractBackground(@Nonnull GuiGraphicsExtractor graphics, int x, int y, float partialTicks) {
      this.updateFields();
      this.drawWindow(graphics, partialTicks, x, y);
   }
}
