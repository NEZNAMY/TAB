package me.neznamy.tab.platforms.bukkit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.UUID;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.cpu.CPUFeature;
import me.neznamy.tab.shared.features.interfaces.PlayerInfoPacketListener;
import me.neznamy.tab.shared.features.interfaces.RawPacketFeature;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardTeam;

public class Injector {

	public static void inject(UUID uuid) {
		Channel channel = Shared.getPlayer(uuid).channel;
		if (!channel.pipeline().names().contains("packet_handler")) {
			//fake player or waterfall bug
			return;
		}
		if (channel.pipeline().names().contains(Shared.DECODER_NAME)) channel.pipeline().remove(Shared.DECODER_NAME);
		try {
			channel.pipeline().addBefore("packet_handler", Shared.DECODER_NAME, new ChannelDuplexHandler() {

				public void channelRead(ChannelHandlerContext context, Object packet) throws Exception {
					if (Shared.disabled) {
						super.channelRead(context, packet);
						return;
					}
					try{
						ITabPlayer player = Shared.getPlayer(uuid);
						if (player != null) {
							for (RawPacketFeature f : Shared.rawpacketfeatures) {
								long time = System.nanoTime();
								try {
									if (packet != null) packet = f.onPacketReceive(player, packet);
								} catch (Throwable e) {
									Shared.errorManager.printError("Feature " + f.getCPUName() + " failed to read packet", e);
								}
								Shared.featureCpu.addTime(f.getCPUName(), System.nanoTime()-time);
							}
						}
					} catch (Throwable e){
						Shared.errorManager.printError("An error occurred when reading packets", e);
					}
					if (packet != null) super.channelRead(context, packet);
				}

				public void write(ChannelHandlerContext context, Object packet, ChannelPromise channelPromise) throws Exception {
					if (Shared.disabled) {
						super.write(context, packet, channelPromise);
						return;
					}
					try{
						ITabPlayer player = Shared.getPlayer(uuid);
						if (player == null) {
							super.write(context, packet, channelPromise);
							return;
						}
						if (Shared.features.containsKey("nametag16") || Shared.features.containsKey("nametagx")) {
							//nametag anti-override
							long time = System.nanoTime();
							if (PacketPlayOutScoreboardTeam.PacketPlayOutScoreboardTeam.isInstance(packet)) {
								modifyPlayers(packet);
							}
							Shared.featureCpu.addTime(CPUFeature.NAMETAG_ANTIOVERRIDE, System.nanoTime()-time);
						}

						for (RawPacketFeature f : Shared.rawpacketfeatures) {
							long time = System.nanoTime();
							try {
								if (packet != null) packet = f.onPacketSend(player, packet);
							} catch (Throwable e) {
								Shared.errorManager.printError("Feature " + f.getCPUName() + " failed to read packet", e);
							}
							Shared.featureCpu.addTime(f.getCPUName(), System.nanoTime()-time);
						}

						PacketPlayOutPlayerInfo info = PacketPlayOutPlayerInfo.fromNMS(packet);
						if (info != null) {
							for (PlayerInfoPacketListener f : Shared.playerInfoListeners) {
								long time = System.nanoTime();
								if (info != null) info = f.onPacketSend(player, info);
								Shared.featureCpu.addTime(f.getCPUName(), System.nanoTime()-time);
							}
							packet = (info == null ? null : info.toNMS(player.getVersion()));
						}
					} catch (Throwable e){
						Shared.errorManager.printError("An error occurred when reading packets", e);
					}
					if (packet != null) super.write(context, packet, channelPromise);
				}
			});
		} catch (NoSuchElementException e) {
			//this makes absolutely no sense, there is already a check for "packet_handler" ...
		}
	}
	public static void uninject(UUID uuid) {
		Channel channel = Shared.getPlayer(uuid).channel;
		if (channel.pipeline().names().contains(Shared.DECODER_NAME)) channel.pipeline().remove(Shared.DECODER_NAME);
	}
	
	@SuppressWarnings("unchecked")
	private static void modifyPlayers(Object packetPlayOutScoreboardTeam) throws Exception {
		if (PacketPlayOutScoreboardTeam.SIGNATURE.getInt(packetPlayOutScoreboardTeam) != 69) {
			Collection<String> players = (Collection<String>) PacketPlayOutScoreboardTeam.PLAYERS.get(packetPlayOutScoreboardTeam);
			Collection<String> newList = new ArrayList<String>();
			for (String entry : players) {
				ITabPlayer p = Shared.getPlayer(entry);
				if (p == null || p.disabledNametag) newList.add(entry);
			}
			PacketPlayOutScoreboardTeam.PLAYERS.set(packetPlayOutScoreboardTeam, newList);
		}
	}
}