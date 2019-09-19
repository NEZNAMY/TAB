package me.neznamy.tab.platforms.bukkit;

import java.util.UUID;

import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.platforms.bukkit.unlimitedtags.NameTagX;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.NameTag16;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.Shared.Feature;
import net.minecraft.util.io.netty.channel.Channel;
import net.minecraft.util.io.netty.channel.ChannelDuplexHandler;
import net.minecraft.util.io.netty.channel.ChannelHandlerContext;
import net.minecraft.util.io.netty.channel.ChannelPromise;

public class Injector1_7 {

	public static void inject(UUID uuid) {
		Channel channel = (Channel) Shared.getPlayer(uuid).getChannel();
		if (channel.pipeline().names().contains(Shared.DECODER_NAME)) channel.pipeline().remove(Shared.DECODER_NAME);
		channel.pipeline().addBefore("packet_handler", Shared.DECODER_NAME, new ChannelDuplexHandler() {

			public void channelRead(ChannelHandlerContext context, Object packet) throws Exception {
				super.channelRead(context, packet);
			}
			public void write(ChannelHandlerContext context, Object packet, ChannelPromise channelPromise) throws Exception {
				if (Main.disabled) {
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
						if ((NameTag16.enable || NameTagX.enable) && Main.instance.killPacket(packet)) {
							Shared.cpu(Feature.NAMETAGAO, System.nanoTime()-time);
							return;
						}
					}
					Shared.cpu(Feature.NAMETAGAO, System.nanoTime()-time);
				} catch (Throwable e){
					Shared.error("An error occured when reading packets", e);
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
