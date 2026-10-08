package mcjty.rftoolsdim.setup;

import mcjty.rftoolsdim.dimension.network.PackagePropageDataToClients;
import mcjty.rftoolsdim.dimension.tools.PacketSyncDimensionListChanges;
import mcjty.rftoolsdim.modules.dimlets.network.PacketSendDimletPackages;
import mcjty.rftoolsdim.modules.workbench.network.PacketPatternToClient;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class RFToolsDimMessages {
   public static void registerMessages(RegisterPayloadHandlersEvent event) {
      PayloadRegistrar registrar = event.registrar("rftoolsdim").versioned("1.0").optional();
      registrar.playToClient(PacketSendDimletPackages.TYPE, PacketSendDimletPackages.CODEC);
      registrar.playToClient(PacketPatternToClient.TYPE, PacketPatternToClient.CODEC);
      registrar.playToClient(PackagePropageDataToClients.TYPE, PackagePropageDataToClients.CODEC);
      registrar.playToClient(PacketSyncDimensionListChanges.TYPE, PacketSyncDimensionListChanges.CODEC);
   }

   public static void registerClientMessages(RegisterClientPayloadHandlersEvent event) {
      event.register(PacketSendDimletPackages.TYPE, PacketSendDimletPackages::handle);
      event.register(PacketPatternToClient.TYPE, PacketPatternToClient::handle);
      event.register(PackagePropageDataToClients.TYPE, PackagePropageDataToClients::handle);
      event.register(PacketSyncDimensionListChanges.TYPE, PacketSyncDimensionListChanges::handle);
   }

   public static <T extends CustomPacketPayload> void sendToPlayer(T packet, Player player) {
      PacketDistributor.sendToPlayer((ServerPlayer)player, packet, new CustomPacketPayload[0]);
   }

   public static <T extends CustomPacketPayload> void sendToAll(T packet) {
      PacketDistributor.sendToAllPlayers(packet, new CustomPacketPayload[0]);
   }

   public static <T extends CustomPacketPayload> void sendToServer(T packet) {
      ClientPacketDistributor.sendToServer(packet, new CustomPacketPayload[0]);
   }
}
