package me.neznamy.tab.bukkit.packets;

import org.bukkit.event.Listener;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.neznamy.tab.bukkit.Main;
import me.neznamy.tab.bukkit.NameTagX;
import me.neznamy.tab.bukkit.packets.PacketPlayOutEntity.PacketPlayOutRelEntityMove;
import me.neznamy.tab.bukkit.packets.PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook;
import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.NameTag16;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardTeam;

public class Packet implements Listener {

	public static void inject(final ITabPlayer player, final PacketReader reader){
		try {
			player.getChannel().pipeline().addBefore("packet_handler", Shared.DECODER_NAME, new ChannelDuplexHandler() {

				public void channelRead(ChannelHandlerContext context, Object packet) throws Exception {
					try{
						long time = System.nanoTime();
						ITabPlayer receiver = Shared.getPlayer(player.getUniqueId());
						if (receiver == null) {
							//plugin reload and player data not loaded yet
							super.channelRead(context, packet);
							return;
						}
						if (NameTagX.enable) {
							if (PacketAPI.PacketPlayInUseEntity.isInstance(packet)) NameTagX.modifyPacketIN(packet);
						}
						Shared.nanoTimeGeneral += (System.nanoTime()-time);
					} catch (Exception e){
						Shared.error("An error occured when reading packets", e);
					}
					super.channelRead(context, packet);
				}
				public void write(ChannelHandlerContext context, Object packet, ChannelPromise channelPromise) throws Exception {
					try{
						long time = System.nanoTime();
						ITabPlayer receiver = Shared.getPlayer(player.getUniqueId());
						if (receiver == null) {
							//plugin reload and player data not loaded yet
							super.write(context, packet, channelPromise);
							return;
						}

						if (PacketPlayOutScoreboardTeam.PacketPlayOutScoreboardTeam.isInstance(packet)) {
							if (!Configs.disabledNametag.contains(receiver.getWorldName())) {
								if ((NameTag16.enable || NameTagX.enable) && Main.instance.killPacket(packet)) {
									Shared.nanoTimeGeneral += (System.nanoTime()-time);
									return;
								}
							}
						}

						time = System.nanoTime();
						if (NameTagX.enable) {
							if (PacketAPI.PacketPlayOutAnimation.isInstance(packet)) {
								if (PacketAPI.PacketPlayOutAnimation_ACTION.getInt(packet) == 2) {
									NameTagX.onBedStatusChange(PacketAPI.PacketPlayOutAnimation_ENTITY.getInt(packet), receiver, false);
								}
							}
							if (NMSClass.versionNumber < 14) {
								if (PacketAPI.PacketPlayOutBed.isInstance(packet)) {
									NameTagX.onBedStatusChange(PacketAPI.PacketPlayOutBed_ENTITY.getInt(packet), receiver, true);
								}
							}
						}


						//unlimited nametag mode
						PacketPlayOut pack = null;
						PacketSendEvent event = null;
						if ((pack = PacketPlayOutNamedEntitySpawn.read(packet)) 			!= null) event = new PacketSendEvent(receiver, pack);
						if ((pack = PacketPlayOutEntityDestroy.read(packet)) 				!= null) event = new PacketSendEvent(receiver, pack);
						if ((pack = PacketPlayOutEntityTeleport.read(packet)) 				!= null) event = new PacketSendEvent(receiver, pack);
						if ((pack = PacketPlayOutRelEntityMove.read(packet))				!= null) event = new PacketSendEvent(receiver, pack);
						if ((pack = PacketPlayOutRelEntityMoveLook.read(packet))			!= null) event = new PacketSendEvent(receiver, pack);
						if ((pack = PacketPlayOutMount.read(packet))						!= null) event = new PacketSendEvent(receiver, pack);
						if (NMSClass.versionNumber == 8 && (pack = PacketPlayOutAttachEntity_1_8_x.read(packet))!= null) event = new PacketSendEvent(receiver, pack);
						if (event != null) {
							try {
								reader.justRead(event);
							} catch (Exception e) {
								Shared.error("An error occured when reading packets (fancy reader)", e);
							}
						}


						pack = null;
						event = null;

						if ((pack = PacketPlayOutEntityMetadata.fromNMS(packet)) 			!= null) event = new PacketSendEvent(receiver, pack);
						if ((pack = PacketPlayOutPlayerInfo.fromNMS(packet)) 				!= null) event = new PacketSendEvent(receiver, pack);
						if ((pack = PacketPlayOutSpawnEntityLiving.fromNMS(packet)) 		!= null) event = new PacketSendEvent(receiver, pack);
						if (event != null) {
							try {
								reader.onPacketSend(event);
							} catch (Exception e) {
								Shared.error("An error occured when reading packets (fancy reader)", e);
							}
							pack = event.getPacket();
							if (event.isCancelled()) {
								return;
							}
						}
						if (pack != null) {
							packet = pack.toNMS();
						}
						Shared.nanoTimeGeneral += (System.nanoTime()-time);
						if (packet == null) return;
					} catch (Exception e){
						Shared.error("An error occured when reading packets", e);
					}
					super.write(context, packet, channelPromise);
				}
			});
		} catch (IllegalArgumentException e) {
			player.getChannel().pipeline().remove(Shared.DECODER_NAME);
			inject(player, reader);
		}
	}

	public static abstract class PacketReader{
		public abstract void onPacketSend(PacketSendEvent e) throws Exception;
		public abstract void justRead(PacketSendEvent e);
	}

	public static class PacketSendEvent{

		private ITabPlayer receiver;
		private PacketPlayOut packet;
		private boolean cancelled;

		public PacketSendEvent(ITabPlayer receiver, PacketPlayOut packet) {
			this.receiver = receiver;
			this.packet = packet;
		}
		public ITabPlayer getPlayer() {
			return receiver;
		}
		public boolean isCancelled() {
			return cancelled;
		}
		public PacketPlayOut getPacket() {
			return packet;
		}
		public void setCancelled(boolean cancelled) {
			this.cancelled = cancelled;
		}
		public void setPacket(PacketPlayOut packet) {
			this.packet = packet;
		}
	}
}