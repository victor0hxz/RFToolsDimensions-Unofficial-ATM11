package mcjty.rftoolsdim.dimension.network;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import mcjty.rftoolsdim.dimension.data.ClientDimensionData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PackagePropageDataToClients(Map<Identifier, ClientDimensionData.ClientData> clientDataMap, long seed) implements CustomPacketPayload {
   public static final Identifier ID = Identifier.fromNamespaceAndPath("rftoolsdim", "propagate_data_to_clients");
   public static final Type<PackagePropageDataToClients> TYPE = new Type(ID);
   public static final StreamCodec<FriendlyByteBuf, PackagePropageDataToClients> CODEC = StreamCodec.of((buf, packet) -> {
      buf.writeInt(packet.clientDataMap.size());

      for (Entry<Identifier, ClientDimensionData.ClientData> entry : packet.clientDataMap.entrySet()) {
         Identifier.STREAM_CODEC.encode(buf, entry.getKey());
         entry.getValue().writeToBuf(buf);
      }

      buf.writeLong(packet.seed);
   }, buf -> {
      int size = buf.readInt();
      Map<Identifier, ClientDimensionData.ClientData> clientDataMap = new HashMap<>(size);

      for (int i = 0; i < size; i++) {
         Identifier id = (Identifier)Identifier.STREAM_CODEC.decode(buf);
         clientDataMap.put(id, ClientDimensionData.ClientData.create(buf));
      }

      long seed = buf.readLong();
      return new PackagePropageDataToClients(clientDataMap, seed);
   });

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public void handle(IPayloadContext ctx) {
      ctx.enqueueWork(() -> ClientDimensionData.get().updateDataFromServer(this.clientDataMap, this.seed));
   }
}
