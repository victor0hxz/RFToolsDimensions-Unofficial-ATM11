package mcjty.rftoolsdim.dimension.data;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import mcjty.rftoolsdim.dimension.client.RFToolsDimensionSpecialEffects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;

public class ClientDimensionData {
   private static final ClientDimensionData INSTANCE = new ClientDimensionData();
   private Map<Identifier, ClientDimensionData.ClientData> clientDataMap = new HashMap<>();
   private long worldSeed = -1L;

   public static ClientDimensionData get() {
      return INSTANCE;
   }

   public long getPower(Identifier id) {
      return this.clientDataMap.getOrDefault(id, ClientDimensionData.ClientData.NONE).power;
   }

   public long getMaxPower(Identifier id) {
      return this.clientDataMap.getOrDefault(id, ClientDimensionData.ClientData.NONE).max;
   }

   @Nonnull
   public ClientDimensionData.ClientData getClientData(Identifier id) {
      return this.clientDataMap.getOrDefault(id, ClientDimensionData.ClientData.NONE);
   }

   public long getWorldSeed() {
      return this.worldSeed;
   }

   public void updateDataFromServer(Map<Identifier, ClientDimensionData.ClientData> clientDataMap, long seed) {
      this.clientDataMap = clientDataMap;
      this.worldSeed = seed;
      RFToolsDimensionSpecialEffects.clearCache();
   }

   public void clear() {
      this.worldSeed = -1L;
      this.clientDataMap.clear();
   }

   public record ClientData(long power, long max, long skyDimletTypes) {
      public static final ClientDimensionData.ClientData NONE = new ClientDimensionData.ClientData(-1L, -1L, 0L);

      public static ClientDimensionData.ClientData create(FriendlyByteBuf buf) {
         long power = buf.readLong();
         long max = buf.readLong();
         long skyDimletTypes = buf.readLong();
         return new ClientDimensionData.ClientData(power, max, skyDimletTypes);
      }

      public void writeToBuf(FriendlyByteBuf buf) {
         buf.writeLong(this.power);
         buf.writeLong(this.max);
         buf.writeLong(this.skyDimletTypes);
      }
   }
}
