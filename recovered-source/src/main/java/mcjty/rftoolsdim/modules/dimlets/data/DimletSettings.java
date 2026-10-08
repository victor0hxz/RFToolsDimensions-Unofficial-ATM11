package mcjty.rftoolsdim.modules.dimlets.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.varia.JSonTools;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class DimletSettings {
   private final DimletRarity rarity;
   private final int createCost;
   private final int maintainCost;
   private final int tickCost;
   private final boolean worldgen;
   private final boolean dimlet;
   private final ItemStack essence;

   private DimletSettings(DimletSettings.Builder builder) {
      this.rarity = builder.rarity;
      this.createCost = builder.createCost;
      this.maintainCost = builder.maintainCost;
      this.tickCost = builder.tickCost;
      this.worldgen = builder.worldgen;
      this.dimlet = builder.dimlet;
      this.essence = builder.essence;
      if (this.rarity == null) {
         throw new IllegalStateException("Dimlet without rarity!");
      }
   }

   public DimletSettings(RegistryFriendlyByteBuf buf) {
      this.rarity = DimletRarity.values()[buf.readInt()];
      this.createCost = buf.readInt();
      this.maintainCost = buf.readInt();
      this.tickCost = buf.readInt();
      this.worldgen = buf.readBoolean();
      this.dimlet = buf.readBoolean();
      this.essence = NetworkTools.readItemStack(buf);
   }

   public void toBytes(RegistryFriendlyByteBuf buf) {
      buf.writeInt(this.rarity.ordinal());
      buf.writeInt(this.createCost);
      buf.writeInt(this.maintainCost);
      buf.writeInt(this.tickCost);
      buf.writeBoolean(this.worldgen);
      buf.writeBoolean(this.dimlet);
      NetworkTools.writeItemStack(buf, this.essence);
   }

   public void buildElement(JsonObject jsonObject) {
      if (this.rarity != null) {
         jsonObject.add("rarity", new JsonPrimitive(this.rarity.name().toLowerCase()));
      }

      jsonObject.add("create", new JsonPrimitive(this.createCost));
      jsonObject.add("maintain", new JsonPrimitive(this.maintainCost));
      jsonObject.add("ticks", new JsonPrimitive(this.tickCost));
      jsonObject.add("worldgen", new JsonPrimitive(this.worldgen));
      jsonObject.add("dimlet", new JsonPrimitive(this.dimlet));
      if (!this.essence.isEmpty()) {
         JsonElement json = JSonTools.itemStackToJson(this.essence);
         jsonObject.add("essence", json);
      }
   }

   public static DimletSettings parse(JsonObject jsonObject) {
      DimletSettings.Builder builder = new DimletSettings.Builder();
      builder.rarity(
         DimletRarity.byName(
            ((JsonElement)JSonTools.getElement(jsonObject, "rarity").orElseThrow(() -> new IllegalStateException("Missing rarity"))).getAsString()
         )
      );
      JSonTools.getElement(jsonObject, "create").ifPresent(e -> builder.createCost(e.getAsInt()));
      JSonTools.getElement(jsonObject, "maintain").ifPresent(e -> builder.maintainCost(e.getAsInt()));
      JSonTools.getElement(jsonObject, "ticks").ifPresent(e -> builder.tickCost(e.getAsInt()));
      JSonTools.getElement(jsonObject, "worldgen").ifPresent(e -> builder.worldgen(e.getAsBoolean()));
      JSonTools.getElement(jsonObject, "dimlet").ifPresent(e -> builder.dimlet(e.getAsBoolean()));
      if (jsonObject.has("essence")) {
         try {
            builder.essence(JSonTools.jsonToItemStack(jsonObject.getAsJsonObject("essence")));
         } catch (Exception var3) {
            throw new RuntimeException(var3);
         }
      }

      return builder.build();
   }

   public ItemStack getEssence() {
      return this.essence;
   }

   public DimletRarity getRarity() {
      return this.rarity;
   }

   public int getCreateCost() {
      return this.createCost;
   }

   public int getMaintainCost() {
      return this.maintainCost;
   }

   public int getTickCost() {
      return this.tickCost;
   }

   public boolean isWorldgen() {
      return this.worldgen;
   }

   public boolean isDimlet() {
      return this.dimlet;
   }

   public static DimletSettings.Builder builder() {
      return new DimletSettings.Builder();
   }

   public static DimletSettings.Builder create(DimletRarity rarity, int createCost, int maintainCost, int tickCost) {
      return builder().rarity(rarity).createCost(createCost).maintainCost(maintainCost).tickCost(tickCost).worldgen(true).dimlet(true);
   }

   public static class Builder {
      private DimletRarity rarity;
      private Integer createCost;
      private Integer maintainCost;
      private Integer tickCost;
      private Boolean worldgen;
      private Boolean dimlet;
      private ItemStack essence = ItemStack.EMPTY;

      private Builder() {
      }

      public DimletSettings.Builder complete() {
         if (this.rarity == null) {
            this.rarity = DimletRarity.COMMON;
         }

         if (this.createCost == null) {
            this.createCost = 1;
         }

         if (this.maintainCost == null) {
            this.maintainCost = 1;
         }

         if (this.tickCost == null) {
            this.tickCost = 1;
         }

         if (this.worldgen == null) {
            this.worldgen = false;
         }

         if (this.dimlet == null) {
            this.dimlet = false;
         }

         return this;
      }

      public DimletSettings.Builder essence(ItemStack stack) {
         this.essence = stack;
         return this;
      }

      public DimletSettings.Builder rarity(DimletRarity rarity) {
         this.rarity = rarity;
         return this;
      }

      public DimletSettings.Builder createCost(int createCost) {
         this.createCost = createCost;
         return this;
      }

      public DimletSettings.Builder maintainCost(int maintainCost) {
         this.maintainCost = maintainCost;
         return this;
      }

      public DimletSettings.Builder tickCost(int tickCost) {
         this.tickCost = tickCost;
         return this;
      }

      public DimletSettings.Builder worldgen(boolean worldgen) {
         this.worldgen = worldgen;
         return this;
      }

      public DimletSettings.Builder dimlet(boolean dimlet) {
         this.dimlet = dimlet;
         return this;
      }

      public DimletSettings build() {
         return new DimletSettings(this);
      }
   }
}
