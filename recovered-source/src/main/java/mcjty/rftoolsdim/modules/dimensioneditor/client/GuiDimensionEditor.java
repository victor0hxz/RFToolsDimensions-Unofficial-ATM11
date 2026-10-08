package mcjty.rftoolsdim.modules.dimensioneditor.client;

import javax.annotation.Nonnull;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.EnergyBar;
import mcjty.lib.gui.widgets.ImageLabel;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.Widget;
import mcjty.lib.gui.widgets.Widgets;
import mcjty.rftoolsdim.modules.dimensioneditor.DimensionEditorModule;
import mcjty.rftoolsdim.modules.dimensioneditor.blocks.DimensionEditorTileEntity;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Items;

public class GuiDimensionEditor extends GenericGuiContainer<DimensionEditorTileEntity, GenericContainer> {
   public static final int WIDTH = 180;
   public static final int HEIGHT = 152;
   private EnergyBar energyBar;
   private ImageLabel arrow;
   private Label percentage;
   private Label destroy;
   private static final Identifier iconLocation = Identifier.fromNamespaceAndPath("rftoolsdim", "textures/gui/dimensioneditor.png");
   private static final Identifier iconGuiElements = Identifier.fromNamespaceAndPath("rftoolsdim", "textures/gui/guielements.png");

   public GuiDimensionEditor(GenericContainer container, Inventory inventory, Component title) {
      super(container, inventory, title, ((BaseBlock)DimensionEditorModule.DIMENSION_EDITOR.block().get()).getManualEntry(), 180, 152);
   }

   public void init() {
      super.init();
      this.energyBar = ((EnergyBar)new EnergyBar().vertical().hint(10, 7, 8, 54)).showText(false);
      this.arrow = (ImageLabel)((ImageLabel)new ImageLabel().image(iconGuiElements, 192, 0)).hint(90, 26, 16, 16);
      this.destroy = (Label)Widgets.label(30, 53, 150, 16, "Destroying dimension!").color(16711680);
      this.destroy.visible(false);
      this.percentage = Widgets.label(80, 43, 40, 16, "0%");
      Panel toplevel = (Panel)((Panel)Widgets.positional().background(iconLocation))
         .children(new Widget[]{this.energyBar, this.arrow, this.percentage, this.destroy});
      toplevel.bounds(this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
      this.window = new Window(this, toplevel);
   }

   public void extractBackground(@Nonnull GuiGraphicsExtractor graphics, int x, int y, float partialTicks) {
      DimensionEditorTileEntity te = (DimensionEditorTileEntity)this.getBE();
      int pct = te != null ? te.getEditPercentage() : 0;
      if (pct > 0) {
         this.arrow.image(iconGuiElements, 144, 0);
      } else {
         this.arrow.image(iconGuiElements, 192, 0);
      }

      this.percentage.text(pct + "%");
      this.destroy.visible(false);
      Slot slot = ((GenericContainer)this.menu).getSlot(0);
      if (slot.hasItem() && slot.getItem().getItem() == Items.TNT) {
         this.destroy.visible(true);
      }

      this.drawWindow(graphics, partialTicks, x, y);
      this.updateEnergyBar(this.energyBar);
   }
}
