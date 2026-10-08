package mcjty.rftoolsdim.dimension.tools;

import java.util.HashSet;
import java.util.Set;
import mcjty.rftoolsdim.setup.RFToolsDimMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSyncDimensionListChanges(Set<ResourceKey<Level>> newDimensions, Set<ResourceKey<Level>> removedDimensions) implements CustomPacketPayload {
   public static final Identifier ID = Identifier.fromNamespaceAndPath("rftoolsdim", "syncdimensionlistchanges");
   public static final Type<PacketSyncDimensionListChanges> TYPE = new Type(ID);
   public static final StreamCodec<FriendlyByteBuf, PacketSyncDimensionListChanges> CODEC = StreamCodec.of((buf, packet) -> {
      buf.writeVarInt(packet.newDimensions.size());

      for (ResourceKey<Level> key : packet.newDimensions) {
         Identifier.STREAM_CODEC.encode(buf, key.identifier());
      }

      buf.writeVarInt(packet.removedDimensions.size());

      for (ResourceKey<Level> key : packet.removedDimensions) {
         Identifier.STREAM_CODEC.encode(buf, key.identifier());
      }
   }, buf -> {
      Set<ResourceKey<Level>> newDimensions = new HashSet<>();
      Set<ResourceKey<Level>> removedDimensions = new HashSet<>();
      int newDimensionCount = buf.readVarInt();

      for (int i = 0; i < newDimensionCount; i++) {
         Identifier worldID = (Identifier)Identifier.STREAM_CODEC.decode(buf);
         newDimensions.add(ResourceKey.create(Registries.DIMENSION, worldID));
      }

      int removedDimensionCount = buf.readVarInt();

      for (int i = 0; i < removedDimensionCount; i++) {
         Identifier worldID = (Identifier)Identifier.STREAM_CODEC.decode(buf);
         removedDimensions.add(ResourceKey.create(Registries.DIMENSION, worldID));
      }

      return new PacketSyncDimensionListChanges(newDimensions, removedDimensions);
   });

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public void handle(IPayloadContext ctx) {
      ctx.enqueueWork(() -> {
         LocalPlayer player = Minecraft.getInstance().player;
         if (player != null) {
            Set<ResourceKey<Level>> commandSuggesterLevels = player.connection.levels();
            commandSuggesterLevels.addAll(this.newDimensions);

            for (ResourceKey<Level> key : this.removedDimensions) {
               commandSuggesterLevels.remove(key);
            }
         }
      });
   }

   public static void updateClientDimensionLists(Set<ResourceKey<Level>> newDimensions, Set<ResourceKey<Level>> removedDimensions) {
      RFToolsDimMessages.sendToAll(new PacketSyncDimensionListChanges(newDimensions, removedDimensions));
   }
}
