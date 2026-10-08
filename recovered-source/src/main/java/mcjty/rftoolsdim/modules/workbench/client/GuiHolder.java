package mcjty.rftoolsdim.modules.workbench.client;

import javax.annotation.Nonnull;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.Widgets;
import mcjty.rftoolsdim.modules.workbench.WorkbenchModule;
import mcjty.rftoolsdim.modules.workbench.blocks.KnowledgeHolderTileEntity;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class GuiHolder extends GenericGuiContainer<KnowledgeHolderTileEntity, GenericContainer> {
   public static final int WIDTH = 256;
   public static final int HEIGHT = 240;
   private static final Identifier iconLocation = Identifier.fromNamespaceAndPath("rftoolsdim", "textures/gui/knowledgeholder.png");

   public GuiHolder(GenericContainer container, Inventory inventory, Component title) {
      super(container, inventory, title, ((BaseBlock)WorkbenchModule.HOLDER.block().get()).getManualEntry(), 256, 240);
   }

   public void init() {
      super.init();
      Panel toplevel = (Panel)Widgets.positional().background(iconLocation);
      toplevel.bounds(this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
      this.window = new Window(this, toplevel);
   }

   public void extractBackground(@Nonnull GuiGraphicsExtractor graphics, int x, int y, float partialTicks) {
      this.drawWindow(graphics, partialTicks, x, y);
   }
}
