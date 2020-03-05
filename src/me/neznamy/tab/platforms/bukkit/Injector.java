package me.neznamy.tab.platforms.bukkit;

import java.util.UUID;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.features.CustomPacketFeature;
import me.neznamy.tab.shared.features.RawPacketFeature;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.UniversalPacketPlayOut;

public class Injector {

	public static void inject(UUID uuid) {
		Channel channel = (Channel) Shared.getPlayer(uuid).getChannel();
		if (!channel.pipeline().names().contains("packet_handler")) {
			//waterfall bug
//			Shared.error(null, "Failed to inject " + Shared.getPlayer(uuid).getName() + ", packet_handler does not exist");
			return;
		}
		if (channel.pipeline().names().contains(Shared.DECODER_NAME)) channel.pipeline().remove(Shared.DECODER_NAME);
		channel.pipeline().addBefore("packet_handler", Shared.DECODER_NAME, new ChannelDuplexHandler() {

			public void channelRead(ChannelHandlerContext context, Object packet) throws Exception {
				if (Shared.disabled) {
					super.channelRead(context, packet);
					return;
				}
				try{
					ITabPlayer player = Shared.getPlayer(uuid);
					if (player != null) {
						for (RawPacketFeature f : Shared.rawpacketfeatures.values()) {
							long time = System.nanoTime();
							try {
								if (packet != null) packet = f.onPacketReceive(player, packet);
							} catch (Throwable e) {
								Shared.errorManager.printError("Feature " + f.getCPUName() + " failed to read packet", e);
							}
							Shared.cpu.addFeatureTime(f.getCPUName(), System.nanoTime()-time);
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
						//wtf
						super.write(context, packet, channelPromise);
						return;
					}
					if (Shared.features.containsKey("nametag16") || Shared.features.containsKey("nametagx")) {
						long time = System.nanoTime();
						if (MethodAPI.PacketPlayOutScoreboardTeam.isInstance(packet)) {
							//nametag anti-override
							if (Main.killPacket(packet)) {
								Shared.cpu.addFeatureTime("Nametag anti-override", System.nanoTime()-time);
								return;
							}
						}
						Shared.cpu.addFeatureTime("Nametag anti-override", System.nanoTime()-time);
					}

					for (RawPacketFeature f : Shared.rawpacketfeatures.values()) {
						long time = System.nanoTime();
						try {
							if (packet != null) packet = f.onPacketSend(player, packet);
						} catch (Throwable e) {
							Shared.errorManager.printError("Feature " + f.getCPUName() + " failed to read packet", e);
						}
						Shared.cpu.addFeatureTime(f.getCPUName(), System.nanoTime()-time);
					}

					UniversalPacketPlayOut customPacket = null;
					customPacket = PacketPlayOutPlayerInfo.fromNMS(packet);
					if (customPacket != null) {
						for (CustomPacketFeature f : Shared.custompacketfeatures.values()) {
							long time = System.nanoTime();
							if (customPacket != null) customPacket = f.onPacketSend(player, customPacket);
							Shared.cpu.addFeatureTime(f.getCPUName(), System.nanoTime()-time);
						}
						if (customPacket != null) packet = customPacket.toNMS(player.getVersion());
						else packet = null;
					}
				} catch (Throwable e){
					Shared.errorManager.printError("An error occurred when reading packets", e);
				}
				if (packet != null) super.write(context, packet, channelPromise);
			}
		});
	}
	public static void uninject(UUID uuid) {
		Channel channel = (Channel) Shared.getPlayer(uuid).getChannel();
		if (channel.pipeline().names().contains(Shared.DECODER_NAME)) channel.pipeline().remove(Shared.DECODER_NAME);
	}
}
