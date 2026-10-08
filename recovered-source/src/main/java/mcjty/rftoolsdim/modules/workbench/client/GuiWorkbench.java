package mcjty.rftoolsdim.modules.workbench.client;

import java.util.Objects;
import javax.annotation.Nonnull;
import mcjty.lib.base.StyleConfig;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.events.SelectionEvent;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.widgets.AbstractWidget;
import mcjty.lib.gui.widgets.BlockRender;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.Slider;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.gui.widgets.ToggleButton;
import mcjty.lib.gui.widgets.Widget;
import mcjty.lib.gui.widgets.WidgetList;
import mcjty.lib.gui.widgets.Widgets;
import mcjty.lib.network.Networking;
import mcjty.lib.network.PacketGetListFromServer;
import mcjty.lib.typed.TypedMap;
import mcjty.rftoolsdim.modules.dimlets.client.DimletClientHelper;
import mcjty.rftoolsdim.modules.dimlets.data.DimletKey;
import mcjty.rftoolsdim.modules.dimlets.data.DimletTools;
import mcjty.rftoolsdim.modules.workbench.WorkbenchModule;
import mcjty.rftoolsdim.modules.workbench.blocks.WorkbenchTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class GuiWorkbench extends GenericGuiContainer<WorkbenchTileEntity, GenericContainer> {
   public static final int WIDTH = 256;
   public static final int HEIGHT = 240;
   private static final Identifier iconLocation = Identifier.fromNamespaceAndPath("rftoolsdim", "textures/gui/dimletworkbench.png");
   private static final Identifier iconGuiElements = Identifier.fromNamespaceAndPath("rftoolsbase", "textures/gui/guielements.png");
   private TextField searchBar;
   private WidgetList itemList;
   private ToggleButton allFilter;
   private long dimletListAge = -1L;
   private static String[] pattern = null;

   public GuiWorkbench(GenericContainer container, Inventory inventory, Component title) {
      super(container, inventory, title, ((BaseBlock)WorkbenchModule.WORKBENCH.block().get()).getManualEntry(), 256, 240);
      pattern = null;
   }

   public void init() {
      super.init();
      this.searchBar = Widgets.textfield(122, 6, 123, 14).event(this::search);
      this.itemList = ((WidgetList)Widgets.list(122, 22, 120, 132).name("widgets")).event(new SelectionEvent() {
         {
            Objects.requireNonNull(GuiWorkbench.this);
         }

         public void select(int index) {
            GuiWorkbench.this.hilightPattern();
         }

         public void doubleClick(int index) {
            if (Minecraft.getInstance().player.getAbilities().instabuild) {
            }

            GuiWorkbench.this.suggestParts();
         }
      });
      Slider slider = Widgets.slider(243, 22, 8, 132).scrollableName("widgets");
      Button createButton = Widgets.button(210, 178, 40, 18, "Create").event(this::createDimlet);
      this.allFilter = ((ToggleButton)((ToggleButton)new ToggleButton().hint(210, 158, 40, 18)).text("All")).event(this::toggleAll);
      Panel toplevel = (Panel)((Panel)Widgets.positional().background(iconLocation))
         .children(new Widget[]{this.searchBar, this.itemList, slider, createButton, this.allFilter});
      toplevel.bounds(this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
      this.window = new Window(this, toplevel);
      this.dimletListAge = -1L;
      WorkbenchTileEntity te = (WorkbenchTileEntity)this.getBE();
      if (te != null) {
         Networking.sendToServer(PacketGetListFromServer.create(te.getBlockPos(), WorkbenchTileEntity.CMD_GETDIMLETS.name()));
      }
   }

   private void createDimlet() {
      this.sendServerCommandTyped(WorkbenchTileEntity.CMD_CREATE_DIMLET, TypedMap.builder().build());
   }

   private void hilightPattern() {
      int selected = this.itemList.getSelected();
      if (selected != -1) {
         Panel widget = (Panel)this.itemList.getChild(selected);
         if (widget.getUserObject() instanceof DimletClientHelper.DimletWithInfo key && key.craftable()) {
            DimletKey dimlet = key.dimlet();
            this.sendServerCommandTyped(
               WorkbenchTileEntity.CMD_HILIGHT_PATTERN,
               TypedMap.builder().put(WorkbenchTileEntity.PARAM_TYPE, dimlet.type().name()).put(WorkbenchTileEntity.PARAM_ID, dimlet.key()).build()
            );
         }
      }
   }

   public static void setPattern(String[] pattern) {
      GuiWorkbench.pattern = pattern;
   }

   private void renderHilightedPattern(GuiGraphicsExtractor graphics) {
   }

   private void cheatDimlet() {
      int selected = this.itemList.getSelected();
      if (selected != -1) {
         Panel widget = (Panel)this.itemList.getChild(selected);
         if (widget.getUserObject() instanceof DimletClientHelper.DimletWithInfo key) {
            DimletKey dimlet = key.dimlet();
            this.sendServerCommandTyped(
               WorkbenchTileEntity.CMD_CHEATDIMLET,
               TypedMap.builder().put(WorkbenchTileEntity.PARAM_TYPE, dimlet.type().name()).put(WorkbenchTileEntity.PARAM_ID, dimlet.key()).build()
            );
         }
      }
   }

   private void suggestParts() {
      int selected = this.itemList.getSelected();
      if (selected != -1) {
         Panel widget = (Panel)this.itemList.getChild(selected);
         if (widget.getUserObject() instanceof DimletClientHelper.DimletWithInfo key && key.craftable()) {
            DimletKey dimlet = key.dimlet();
            this.sendServerCommandTyped(
               WorkbenchTileEntity.CMD_SUGGESTPARTS,
               TypedMap.builder().put(WorkbenchTileEntity.PARAM_TYPE, dimlet.type().name()).put(WorkbenchTileEntity.PARAM_ID, dimlet.key()).build()
            );
         }
      }
   }

   private void toggleAll() {
      this.dimletListAge = -1L;
   }

   private void search(String filter) {
      this.dimletListAge = -1L;
   }

   private void updateList() {
      if (this.dimletListAge != DimletClientHelper.dimletListAge) {
         this.dimletListAge = DimletClientHelper.dimletListAge;
         this.itemList.removeChildren();
         String filter = this.searchBar.getText().toLowerCase();
         DimletClientHelper.dimlets.stream().filter(key -> this.dimletMatches(filter, key)).sorted().forEachOrdered(this::addItemToList);
         if (this.itemList.getFirstSelected() >= this.itemList.getChildCount()) {
            this.itemList.setFirstSelected(0);
         }
      }
   }

   private boolean dimletMatches(String filter, DimletClientHelper.DimletWithInfo key) {
      if (!this.allFilter.isPressed() && !key.craftable()) {
         return false;
      } else {
         DimletKey dimlet = key.dimlet();
         String readableName = DimletTools.getReadableName(dimlet);
         return readableName.toLowerCase().contains(filter) || dimlet.type().name().toLowerCase().contains(filter);
      }
   }

   private void addItemToList(DimletClientHelper.DimletWithInfo key) {
      Panel panel = (Panel)((Panel)((Panel)Widgets.positional().desiredWidth(113)).desiredHeight(16)).userObject(key);
      this.itemList.children(new Widget[]{panel});
      BlockRender blockRender = (BlockRender)((BlockRender)new BlockRender().renderItem(DimletTools.getDimletStack(key.dimlet())).hint(1, 0, 16, 16))
         .userObject(key);
      panel.children(new Widget[]{blockRender});
      String displayName = DimletTools.getReadableName(key.dimlet());
      AbstractWidget label = ((Label)((Label)((Label)Widgets.label(displayName)
                  .color(key.craftable() ? StyleConfig.colorTextInListNormal : StyleConfig.colorTextDisabled))
               .horizontalAlignment(HorizontalAlignment.ALIGN_LEFT))
            .hint(20, 0, 95, 16))
         .userObject(key);
      panel.children(new Widget[]{label});
   }

   public void extractBackground(@Nonnull GuiGraphicsExtractor graphics, int x, int y, float partialTicks) {
      this.updateList();
      this.drawWindow(graphics, partialTicks, x, y);
      this.renderHilightedPattern(graphics);
   }
}
