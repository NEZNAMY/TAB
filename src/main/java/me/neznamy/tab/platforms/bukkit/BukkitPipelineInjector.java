package me.neznamy.tab.platforms.bukkit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.PipelineInjector;

public class BukkitPipelineInjector extends PipelineInjector {

	private final String INJECT_POSITION = "packet_handler";
	
	private NMSStorage nms;

	public BukkitPipelineInjector(TAB tab, NMSStorage nms) throws ClassNotFoundException {
		super(tab);
		this.nms = nms;
	}
	
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
						Object modifiedPacket = tab.getFeatureManager().onPacketReceive(player, packet);
						if (modifiedPacket != null) super.channelRead(context, modifiedPacket);
					} catch (Throwable e){
						tab.getErrorManager().printError("An error occurred when reading packets", e);
					}
				}

				@Override
				public void write(ChannelHandlerContext context, Object packet, ChannelPromise channelPromise) throws Exception {
					try {
						if (nms.PacketPlayOutPlayerInfo.isInstance(packet)) {
							super.write(context, tab.getFeatureManager().onPacketPlayOutPlayerInfo(player, packet), channelPromise);
							return;
						}
						if (tab.getFeatureManager().getNameTagFeature() != null && nms.PacketPlayOutScoreboardTeam.isInstance(packet)) {
							modifyPlayers(packet);
							super.write(context, packet, channelPromise);
							return;
						}
						if (nms.PacketPlayOutScoreboardDisplayObjective.isInstance(packet) && tab.getFeatureManager().onDisplayObjective(player, packet)) {
							return;
						}
						if (nms.PacketPlayOutPlayerListHeaderFooter.isInstance(packet) && tab.getFeatureManager().onHeaderFooter(player, packet)) {
							return;
						}
						tab.getFeatureManager().onPacketSend(player, packet);
					} catch (Throwable e){
						tab.getErrorManager().printError("An error occurred when reading packets", e);
					}
					super.write(context, packet, channelPromise);
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
	private void modifyPlayers(Object packetPlayOutScoreboardTeam) throws Exception {
		long time = System.nanoTime();
		if (nms.PacketPlayOutScoreboardTeam_SIGNATURE.getInt(packetPlayOutScoreboardTeam) != 69) {
			Collection<String> players = (Collection<String>) nms.PacketPlayOutScoreboardTeam_PLAYERS.get(packetPlayOutScoreboardTeam);
			//creating a new list to prevent NoSuchFieldException in minecraft packet encoder when a player is removed
			Collection<String> newList = new ArrayList<String>();
			for (String entry : players) {
				TabPlayer p = tab.getPlayer(entry);
				if (p == null) {
					newList.add(entry);
					continue;
				}
				if (tab.getFeatureManager().getNameTagFeature().isDisabledWorld(p.getWorldName())) {
					newList.add(entry);
				} else {
					logTeamOverride((String) nms.PacketPlayOutScoreboardTeam_NAME.get(packetPlayOutScoreboardTeam), entry);
				}
			}
			nms.PacketPlayOutScoreboardTeam_PLAYERS.set(packetPlayOutScoreboardTeam, newList);
		}
		tab.getCPUManager().addTime(TabFeature.NAMETAGS, UsageType.ANTI_OVERRIDE, System.nanoTime()-time);
	}
}