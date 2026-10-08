package mcjty.rftoolsdim.modules.workbench.network;

import mcjty.rftoolsdim.modules.workbench.client.GuiWorkbench;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketPatternToClient(String[] pattern) implements CustomPacketPayload {
   public static final Identifier ID = Identifier.fromNamespaceAndPath("rftoolsdim", "pattern_to_client");
   public static final Type<PacketPatternToClient> TYPE = new Type(ID);
   public static final StreamCodec<FriendlyByteBuf, PacketPatternToClient> CODEC = StreamCodec.of((buf, packet) -> {
      buf.writeInt(packet.pattern.length);

      for (String p : packet.pattern) {
         buf.writeUtf(p);
      }
   }, buf -> {
      int size = buf.readInt();
      String[] pattern = new String[size];

      for (int i = 0; i < size; i++) {
         pattern[i] = buf.readUtf(32767);
      }

      return new PacketPatternToClient(pattern);
   });

   public static PacketPatternToClient create(String[] p) {
      return new PacketPatternToClient(p);
   }

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public void handle(IPayloadContext ctx) {
      ctx.enqueueWork(() -> GuiWorkbench.setPattern(this.pattern));
   }
}
