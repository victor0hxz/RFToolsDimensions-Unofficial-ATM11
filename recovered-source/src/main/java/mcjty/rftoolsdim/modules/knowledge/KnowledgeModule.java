package mcjty.rftoolsdim.modules.knowledge;

import mcjty.lib.datagen.DataGen;
import mcjty.lib.modules.IModule;
import mcjty.rftoolsdim.RFToolsDim;
import mcjty.rftoolsdim.modules.dimlets.data.DimletRarity;
import mcjty.rftoolsdim.modules.knowledge.data.KnowledgeManager;
import mcjty.rftoolsdim.modules.knowledge.data.LostKnowledgeData;
import mcjty.rftoolsdim.modules.knowledge.items.LostKnowledgeItem;
import mcjty.rftoolsdim.setup.Registration;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent.Load;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public class KnowledgeModule implements IModule {
   public static final DeferredItem<LostKnowledgeItem> COMMON_LOST_KNOWLEDGE = Registration.ITEMS
      .register("common_lost_knowledge", RFToolsDim.tab(() -> new LostKnowledgeItem(DimletRarity.COMMON)));
   public static final DeferredItem<LostKnowledgeItem> UNCOMMON_LOST_KNOWLEDGE = Registration.ITEMS
      .register("uncommon_lost_knowledge", RFToolsDim.tab(() -> new LostKnowledgeItem(DimletRarity.UNCOMMON)));
   public static final DeferredItem<LostKnowledgeItem> RARE_LOST_KNOWLEDGE = Registration.ITEMS
      .register("rare_lost_knowledge", RFToolsDim.tab(() -> new LostKnowledgeItem(DimletRarity.RARE)));
   public static final DeferredItem<LostKnowledgeItem> LEGENDARY_LOST_KNOWLEDGE = Registration.ITEMS
      .register("legendary_lost_knowledge", RFToolsDim.tab(() -> new LostKnowledgeItem(DimletRarity.LEGENDARY)));
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<LostKnowledgeData>> ITEM_LOST_KNOWLEDGE_DATA = Registration.COMPONENTS
      .registerComponentType("lost_knowledge_data", builder -> builder.persistent(LostKnowledgeData.CODEC).networkSynchronized(LostKnowledgeData.STREAM_CODEC));

   public KnowledgeModule() {
      NeoForge.EVENT_BUS.addListener(this::onWorldLoad);
   }

   private void onWorldLoad(Load event) {
      KnowledgeManager.get().clear();
   }

   public void init(FMLCommonSetupEvent event) {
   }

   public void initClient(FMLClientSetupEvent event) {
   }

   public void initConfig(IEventBus bus) {
   }

   public void initDatagen(DataGen dataGen, Provider provider) {
   }
}
