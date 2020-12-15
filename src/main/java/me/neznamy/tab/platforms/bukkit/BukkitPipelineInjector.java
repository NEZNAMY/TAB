package me.neznamy.tab.platforms.bukkit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.PipelineInjector;

public class BukkitPipelineInjector extends PipelineInjector {
	
	private static final String INJECT_POSITION = "packet_handler";
	
	@Override
	public void inject(TabPlayer player) {
		if (!player.getChannel().pipeline().names().contains(INJECT_POSITION)) {
			//fake player or waterfall bug
			return;
		}
		uninject(player);
		try {
			player.getChannel().pipeline().addBefore(INJECT_POSITION, DECODER_NAME, new ChannelDuplexHandler() {
				
				@Override
				public void channelRead(ChannelHandlerContext context, Object packet) throws Exception {
					try {
						Object modifiedPacket = Shared.featureManager.onPacketReceive(player, packet);
						if (modifiedPacket != null) super.channelRead(context, modifiedPacket);
					} catch (Throwable e){
						Shared.errorManager.printError("An error occurred when reading packets", e);
					}
				}

				@Override
				public void write(ChannelHandlerContext context, Object packet, ChannelPromise channelPromise) throws Exception {
					try {
						if (BukkitPacketBuilder.PacketPlayOutPlayerInfo.isInstance(packet)) {
							super.write(context, Shared.featureManager.onPacketPlayOutPlayerInfo(player, packet), channelPromise);
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
						super.write(context, packet, channelPromise);
					} catch (Throwable e){
						Shared.errorManager.printError("An error occurred when reading packets", e);
					}
				}
			});
		} catch (NoSuchElementException | IllegalArgumentException e) {
			//idk how does this keep happening but whatever
		}
	}
	
	@Override
	public void uninject(TabPlayer player) {
		try {
			if (player.getChannel().pipeline().names().contains(DECODER_NAME)) player.getChannel().pipeline().remove(DECODER_NAME);
		} catch (NoSuchElementException e) {
			//for whatever reason this rarely throws
			//java.util.NoSuchElementException: TABReader
		}
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