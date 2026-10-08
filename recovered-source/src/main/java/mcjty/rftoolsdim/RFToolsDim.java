package mcjty.rftoolsdim;

import java.util.function.Supplier;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.modules.Modules;
import mcjty.lib.varia.LegacyCapabilities;
import mcjty.rftoolsdim.dimension.client.OverlayRenderer;
import mcjty.rftoolsdim.dimension.data.DimensionCreator;
import mcjty.rftoolsdim.modules.blob.BlobModule;
import mcjty.rftoolsdim.modules.decorative.DecorativeModule;
import mcjty.rftoolsdim.modules.dimensionbuilder.DimensionBuilderModule;
import mcjty.rftoolsdim.modules.dimensionbuilder.items.PhasedFieldEnergyHandler;
import mcjty.rftoolsdim.modules.dimensionbuilder.items.PhasedFieldGenerator;
import mcjty.rftoolsdim.modules.dimensioneditor.DimensionEditorModule;
import mcjty.rftoolsdim.modules.dimlets.DimletModule;
import mcjty.rftoolsdim.modules.enscriber.EnscriberModule;
import mcjty.rftoolsdim.modules.essences.EssencesModule;
import mcjty.rftoolsdim.modules.knowledge.KnowledgeModule;
import mcjty.rftoolsdim.modules.various.VariousModule;
import mcjty.rftoolsdim.modules.workbench.WorkbenchModule;
import mcjty.rftoolsdim.setup.ClientSetup;
import mcjty.rftoolsdim.setup.Config;
import mcjty.rftoolsdim.setup.ModSetup;
import mcjty.rftoolsdim.setup.RFToolsDimMessages;
import mcjty.rftoolsdim.setup.Registration;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.capabilities.Capabilities.Energy;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.data.event.GatherDataEvent.Client;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;

@Mod("rftoolsdim")
public class RFToolsDim {
   public static final String MODID = "rftoolsdim";
   public static RFToolsDim instance;
   private final Modules modules = new Modules();
   public static final ModSetup setup = new ModSetup();

   public RFToolsDim(ModContainer mod, IEventBus bus, Dist dist) {
      instance = this;
      this.setupModules(bus, dist);
      Config.register(mod, bus, this.modules);
      Registration.register(bus);
      bus.addListener(setup::init);
      bus.addListener(this.modules::init);
      bus.addListener(this::processIMC);
      bus.addListener(this::onDataGen);
      bus.addListener(RFToolsDimMessages::registerMessages);
      NeoForge.EVENT_BUS.addListener(this::onJoinWorld);
      bus.addListener(setup.getBlockCapabilityRegistrar(Registration.RBLOCKS));
      bus.addListener(this::onRegisterCapabilities);
      if (dist.isClient()) {
         bus.addListener(RFToolsDimMessages::registerClientMessages);
         NeoForge.EVENT_BUS.addListener(ClientSetup::onPlayerLogin);
         NeoForge.EVENT_BUS.addListener(ClientSetup::onDimensionChange);
         NeoForge.EVENT_BUS.addListener(OverlayRenderer::render);
         bus.addListener(ClientSetup::init);
         bus.addListener(ClientSetup::registerRangeItemModelProperties);
         bus.addListener(this.modules::initClient);
      }
   }

   private void processIMC(InterModProcessEvent event) {
   }

   public static <T extends Item> Supplier<T> tab(Supplier<T> supplier) {
      return setup.tab(supplier);
   }

   private void onDataGen(Client event) {
      DataGen datagen = new DataGen("rftoolsdim", event);
      this.modules.datagen(datagen, event.getLookupProvider());
      datagen.generate();
   }

   private void setupModules(IEventBus bus, Dist dist) {
      this.modules.register(new VariousModule());
      this.modules.register(new DimensionBuilderModule(bus));
      this.modules.register(new DimensionEditorModule(bus));
      this.modules.register(new DimletModule());
      this.modules.register(new EnscriberModule(bus));
      this.modules.register(new WorkbenchModule(bus));
      this.modules.register(new BlobModule(bus, dist));
      this.modules.register(new KnowledgeModule());
      this.modules.register(new EssencesModule());
      this.modules.register(new DecorativeModule());
   }

   private void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
      Item item = (Item)DimensionBuilderModule.PHASED_FIELD_GENERATOR.get();
      PhasedFieldGenerator pfg = (PhasedFieldGenerator)item;
      event.registerItem(LegacyCapabilities.ENERGY_ITEM, (stack, context) -> pfg.createEnergyStorage(stack), new ItemLike[]{item});
      event.registerItem(Energy.ITEM, (stack, context) -> context == null ? null : new PhasedFieldEnergyHandler(context), new ItemLike[]{item});
   }

   private void onJoinWorld(PlayerLoggedInEvent event) {
      DimensionCreator.get().clear();
   }
}
