package mcjty.rftoolsdim.modules.enscriber.client;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.gui.widgets.Widget;
import mcjty.lib.gui.widgets.Widgets;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.ComponentFactory;
import mcjty.lib.varia.Logging;
import mcjty.rftoolsdim.dimension.descriptor.DescriptorError;
import mcjty.rftoolsdim.modules.dimensionbuilder.DimensionBuilderModule;
import mcjty.rftoolsdim.modules.enscriber.EnscriberModule;
import mcjty.rftoolsdim.modules.enscriber.blocks.EnscriberTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class GuiEnscriber extends GenericGuiContainer<EnscriberTileEntity, GenericContainer> {
   public static final int ENSCRIBER_WIDTH = 256;
   public static final int ENSCRIBER_HEIGHT = 224;
   public static final String REGEX = "[a-z0-9_\\.\\-]+";
   private Button extractButton;
   private Button storeButton;
   private TextField nameField;
   private Label validateField;
   private static final Identifier iconLocation = Identifier.fromNamespaceAndPath("rftoolsdim", "textures/gui/dimensionenscriber.png");

   public GuiEnscriber(GenericContainer container, Inventory inventory, Component title) {
      super(container, inventory, title, ((BaseBlock)EnscriberModule.ENSCRIBER.block().get()).getManualEntry(), 256, 224);
   }

   public void init() {
      super.init();
      this.extractButton = (Button)((Button)Widgets.button(12, 164, 60, 16, "Extract").name("extract"))
         .event(this::extractDimlets)
         .tooltips(new String[]{"Extract the dimlets out of", "a realized dimension tab"});
      this.storeButton = (Button)Widgets.button(13, 182, 60, 16, "Store")
         .event(this::storeDimlets)
         .tooltips(new String[]{"Store dimlets in a", "empty dimension tab"});
      this.nameField = (TextField)Widgets.textfield(13, 200, 60, 16).name("name");
      this.validateField = (Label)Widgets.label(35, 142, 38, 16, "Val").tooltips(new String[]{"Hover here for errors..."});
      this.setNameFromDimensionTab();
      Panel toplevel = (Panel)((Panel)Widgets.positional().background(iconLocation))
         .children(new Widget[]{this.extractButton, this.storeButton, this.nameField, this.validateField});
      toplevel.bounds(this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
      this.window = new Window(this, toplevel);
   }

   private void extractDimlets() {
      for (int i = 0; i < 91; i++) {
         ItemStack stack = ((Slot)((GenericContainer)this.menu).slots.get(i + 0)).getItem();
         if (!stack.isEmpty()) {
            Logging.warn(this.minecraft.player, "You cannot extract. Remove all dimlets first!");
            return;
         }
      }

      this.sendServerCommandTyped(EnscriberTileEntity.CMD_EXTRACT, TypedMap.EMPTY);
   }

   private void storeDimlets() {
      String name = this.nameField.getText();
      if (name != null && !name.trim().isEmpty()) {
         this.sendServerCommandTyped(EnscriberTileEntity.CMD_STORE, TypedMap.builder().put(EnscriberTileEntity.PARAM_NAME, name).build());
      } else {
         Minecraft.getInstance().player.sendSystemMessage(ComponentFactory.literal("Name is required!"));
      }
   }

   private void enableButtons() {
      Slot slot = (Slot)((GenericContainer)this.menu).slots.get(91);
      this.extractButton.enabled(false);
      this.storeButton.enabled(false);
      if (!slot.getItem().isEmpty()) {
         if (slot.getItem().getItem() == DimensionBuilderModule.EMPTY_DIMENSION_TAB.get()) {
            this.storeButton.enabled(true);
         } else if (slot.getItem().getItem() == DimensionBuilderModule.REALIZED_DIMENSION_TAB.get()) {
            this.extractButton.enabled(true);
         }
      }
   }

   private void validateDimlets() {
      EnscriberTileEntity te = (EnscriberTileEntity)this.getBE();
      if (te == null) {
         this.validateField.text("");
      } else {
         int errorCode = te.getClientErrorCode();
         DescriptorError.Code error = DescriptorError.Code.values()[errorCode];
         List<String> tooltips = new ArrayList<>();
         if (error == DescriptorError.Code.OK) {
            tooltips.add("Everything appears to be alright");
            this.validateField.color(34816);
            this.validateField.text("Ok");
         } else {
            tooltips.add(error.getMessage());
            this.validateField.color(16711680);
            this.validateField.text("Error");
            this.storeButton.enabled(false);
         }

         this.validateField.tooltips(tooltips.toArray(new String[tooltips.size()]));
      }
   }

   private boolean validateName(String name) {
      return name.trim().isEmpty() ? false : Pattern.matches("[a-z0-9_\\.\\-]+", name);
   }

   public void extractBackground(@Nonnull GuiGraphicsExtractor graphics, int x, int y, float partialTicks) {
      this.enableButtons();
      this.validateDimlets();
      String name = this.nameField.getText().trim();
      if (name.isEmpty()) {
         this.storeButton.enabled(false);
         this.storeButton.tooltips(new String[]{"A dimension name is needed!"});
      } else if (!this.validateName(name)) {
         this.storeButton.enabled(false);
         this.storeButton.tooltips(new String[]{"The dimension name is invalid (only lowercase, no special characters)!"});
      } else {
         this.storeButton.tooltips(new String[]{"Store dimlets in a", "empty dimension tab"});
      }

      this.setNameFromDimensionTab();
      this.drawWindow(graphics, partialTicks, x, y);
   }

   private void setNameFromDimensionTab() {
      EnscriberTileEntity te = (EnscriberTileEntity)this.getBE();
      if (te != null) {
         String dimensionName = te.getDimensionName();
         if (dimensionName != null) {
            this.nameField.text(dimensionName);
         }
      }
   }
}
