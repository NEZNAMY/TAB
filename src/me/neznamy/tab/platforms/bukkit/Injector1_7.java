package me.neznamy.tab.platforms.bukkit;

import java.util.UUID;

import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.*;
import me.neznamy.tab.shared.Shared.Feature;
import net.minecraft.util.io.netty.channel.*;

public class Injector1_7 {

	public static void inject(UUID uuid) {
		Channel channel = (Channel) Shared.getPlayer(uuid).getChannel();
		if (!channel.pipeline().names().contains("packet_handler")) {
			Shared.error(null, "Failed to inject " + Shared.getPlayer(uuid).getName() + ", packet_handler does not exist");
			return;
		}
		if (channel.pipeline().names().contains(Shared.DECODER_NAME)) channel.pipeline().remove(Shared.DECODER_NAME);
		channel.pipeline().addBefore("packet_handler", Shared.DECODER_NAME, new ChannelDuplexHandler() {

			public void channelRead(ChannelHandlerContext context, Object packet) throws Exception {
				super.channelRead(context, packet);
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
					long time = System.nanoTime();
					if (MethodAPI.PacketPlayOutScoreboardTeam.isInstance(packet)) {
						//nametag anti-override
						if (NameTag16.enable && Main.killPacket(packet)) {
							Shared.featureCPU(Feature.NAMETAGAO, System.nanoTime()-time);
							return;
						}
					}
					Shared.featureCPU(Feature.NAMETAGAO, System.nanoTime()-time);
				} catch (Throwable e){
					Shared.error(null, "An error occurred when reading packets", e);
				}
				super.write(context, packet, channelPromise);
			}
		});
	}
	public static void uninject(UUID uuid) {
		Channel channel = (Channel) Shared.getPlayer(uuid).getChannel();
		if (channel.pipeline().names().contains(Shared.DECODER_NAME)) channel.pipeline().remove(Shared.DECODER_NAME);
	}
}