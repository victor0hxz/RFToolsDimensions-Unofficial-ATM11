package mcjty.rftoolsdim.modules.dimlets.network;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import mcjty.lib.varia.SafeClientTools;
import mcjty.rftoolsdim.modules.dimlets.data.DimletDictionary;
import mcjty.rftoolsdim.modules.dimlets.data.DimletKey;
import mcjty.rftoolsdim.modules.dimlets.data.DimletSettings;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSendDimletPackages(Map<DimletKey, DimletSettings> dimlets) implements CustomPacketPayload {
   public static final Identifier ID = Identifier.fromNamespaceAndPath("rftoolsdim", "senddimletpackages");
   public static final Type<PacketSendDimletPackages> TYPE = new Type(ID);
   public static final StreamCodec<RegistryFriendlyByteBuf, PacketSendDimletPackages> CODEC = StreamCodec.of((buf, packet) -> {
      buf.writeInt(packet.dimlets.size());

      for (Entry<DimletKey, DimletSettings> entry : packet.dimlets.entrySet()) {
         entry.getKey().toBytes(buf);
         entry.getValue().toBytes(buf);
      }
   }, buf -> {
      int size = buf.readInt();
      Map<DimletKey, DimletSettings> dimlets = new HashMap<>(size);

      for (int i = 0; i < size; i++) {
         DimletKey key = DimletKey.create(buf);
         DimletSettings settings = new DimletSettings(buf);
         dimlets.put(key, settings);
      }

      return new PacketSendDimletPackages(dimlets);
   });

   public PacketSendDimletPackages(Map<DimletKey, DimletSettings> dimlets) {
      this.dimlets = new HashMap<>(dimlets);
   }

   public static PacketSendDimletPackages create(Map<DimletKey, DimletSettings> collected) {
      return new PacketSendDimletPackages(collected);
   }

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public void handle(IPayloadContext ctx) {
      ctx.enqueueWork(() -> {
         RegistryAccess access = SafeClientTools.getClientWorld().registryAccess();
         DimletDictionary dictionary = DimletDictionary.get();

         for (Entry<DimletKey, DimletSettings> entry : this.dimlets.entrySet()) {
            dictionary.register(access, entry.getKey(), entry.getValue());
         }
      });
   }
}
