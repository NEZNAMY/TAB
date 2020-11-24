package me.neznamy.tab.platforms.bukkit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.UUID;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.PipelineInjector;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;

public class BukkitPipelineInjector extends PipelineInjector {
	
	private static final String INJECT_POSITION = "packet_handler";
	
	@Override
	public void inject(UUID uuid) {
		Channel channel = Shared.getPlayer(uuid).getChannel();
		if (!channel.pipeline().names().contains(INJECT_POSITION)) {
			//fake player or waterfall bug
			return;
		}
		if (channel.pipeline().names().contains(DECODER_NAME)) channel.pipeline().remove(DECODER_NAME);
		try {
			channel.pipeline().addBefore(INJECT_POSITION, DECODER_NAME, new ChannelDuplexHandler() {
				
				@Override
				public void channelRead(ChannelHandlerContext context, Object packet) throws Exception {
					try {
						TabPlayer player = Shared.getPlayer(uuid);
						if (player == null) {
							super.channelRead(context, packet);
							return;
						}
						Object modifiedPacket = Shared.featureManager.onPacketReceive(player, packet);
						if (modifiedPacket != null) super.channelRead(context, modifiedPacket);
					} catch (Throwable e){
						Shared.errorManager.printError("An error occurred when reading packets", e);
					}
				}

				@Override
				public void write(ChannelHandlerContext context, Object packet, ChannelPromise channelPromise) throws Exception {
					try {
						TabPlayer player = Shared.getPlayer(uuid);
						if (player == null) {
							super.write(context, packet, channelPromise);
							return;
						}
						if (Shared.featureManager.isFeatureEnabled("nametag16") || Shared.featureManager.isFeatureEnabled("nametagx")) {
							//nametag anti-override
							long time = System.nanoTime();
							if (BukkitPacketBuilder.PacketPlayOutScoreboardTeam.isInstance(packet)) {
								modifyPlayers(packet);
								Shared.cpu.addTime(TabFeature.NAMETAGS, UsageType.ANTI_OVERRIDE, System.nanoTime()-time);
								super.write(context, packet, channelPromise);
								return;
							}
							Shared.cpu.addTime(TabFeature.NAMETAGS, UsageType.ANTI_OVERRIDE, System.nanoTime()-time);
						}
						Shared.featureManager.onPacketSend(player, packet);
						if (BukkitPacketBuilder.PacketPlayOutPlayerInfo.isInstance(packet)) {
							PacketPlayOutPlayerInfo info = deserializeInfoPacket(packet, player);
							Shared.featureManager.onPacketPlayOutPlayerInfo(player, info);
							super.write(context, serializeInfoPacket(info, player), channelPromise);
							return;
						}
						super.write(context, packet, channelPromise);
					} catch (Throwable e){
						Shared.errorManager.printError("An error occurred when reading packets", e);
					}
				}
			});
		} catch (NoSuchElementException e) {
			//this makes absolutely no sense, there is already a check for "packet_handler" ...
		}
	}
	
	@Override
	public void uninject(UUID uuid) {
		Channel channel = Shared.getPlayer(uuid).getChannel();
		if (channel.pipeline().names().contains(DECODER_NAME)) channel.pipeline().remove(DECODER_NAME);
	}
	
	@SuppressWarnings("unchecked")
	private static void modifyPlayers(Object packetPlayOutScoreboardTeam) throws Exception {
		if (BukkitPacketBuilder.PacketPlayOutScoreboardTeam_SIGNATURE.getInt(packetPlayOutScoreboardTeam) != 69) {
			Collection<String> players = (Collection<String>) BukkitPacketBuilder.PacketPlayOutScoreboardTeam_PLAYERS.get(packetPlayOutScoreboardTeam);
			Collection<String> newList = new ArrayList<String>();
			for (String entry : players) {
				TabPlayer p = Shared.getPlayer(entry);
				if (p == null || Shared.featureManager.getNameTagFeature().isDisabledWorld(p.getWorldName())) newList.add(entry);
			}
			BukkitPacketBuilder.PacketPlayOutScoreboardTeam_PLAYERS.set(packetPlayOutScoreboardTeam, newList);
		}
	}
}