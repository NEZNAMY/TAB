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

	//handler to inject before
	private static final String INJECT_POSITION = "packet_handler";
	
	//nms storage
	private NMSStorage nms;

	/**
	 * Constructs new instance with given parameters
	 * @param tab - tab instance
	 * @param nms - nms storage
	 */
	public BukkitPipelineInjector(TAB tab, NMSStorage nms){
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
			player.getChannel().pipeline().addBefore(INJECT_POSITION, DECODER_NAME, new BukkitChannelDuplexHandler(player));
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
	
	/**
	 * Custom channel duplex handler override
	 */
	public class BukkitChannelDuplexHandler extends ChannelDuplexHandler {
		
		//injected player
		private TabPlayer player;
		
		/**
		 * Constructs new instance with given player
		 * @param player - player to inject
		 */
		public BukkitChannelDuplexHandler(TabPlayer player) {
			this.player = player;
		}

		@Override
		public void channelRead(ChannelHandlerContext context, Object packet) {
			try {
				Object modifiedPacket = tab.getFeatureManager().onPacketReceive(player, packet);
				if (modifiedPacket != null) super.channelRead(context, modifiedPacket);
			} catch (Exception e){
				tab.getErrorManager().printError("An error occurred when reading packets", e);
			}
		}

		@Override
		public void write(ChannelHandlerContext context, Object packet, ChannelPromise channelPromise) {
			try {
				if (nms.getClass("PacketPlayOutPlayerInfo").isInstance(packet)) {
					super.write(context, tab.getFeatureManager().onPacketPlayOutPlayerInfo(player, packet), channelPromise);
					return;
				}
				if (tab.getFeatureManager().getNameTagFeature() != null && antiOverrideTeams && nms.getClass("PacketPlayOutScoreboardTeam").isInstance(packet)) {
					modifyPlayers(packet);
					super.write(context, packet, channelPromise);
					return;
				}
				if (nms.getClass("PacketPlayOutScoreboardDisplayObjective").isInstance(packet) && antiOverrideObjectives && tab.getFeatureManager().onDisplayObjective(player, packet)) {
					return;
				}
				if (antiOverrideObjectives && nms.getClass("PacketPlayOutScoreboardObjective").isInstance(packet)) {
					tab.getFeatureManager().onObjective(player, packet);
				}
				tab.getFeatureManager().onPacketSend(player, packet);
			} catch (Throwable e){
				tab.getErrorManager().printError("An error occurred when reading packets", e);
			}
			try {
				super.write(context, packet, channelPromise);
			} catch (Throwable e) {
				tab.getErrorManager().printError("Failed to forward packet " + packet.getClass().getSimpleName() + " to " + player.getName(), e);
			}
		}
		
		/**
		 * Removes all real players from team if packet does not come from TAB and reports this to override log
		 * @param packetPlayOutScoreboardTeam - team packet
		 * @throws IllegalAccessException 
		 */
		@SuppressWarnings("unchecked")
		private void modifyPlayers(Object packetPlayOutScoreboardTeam) throws IllegalAccessException {
			long time = System.nanoTime();
			Collection<String> players = (Collection<String>) nms.getField("PacketPlayOutScoreboardTeam_PLAYERS").get(packetPlayOutScoreboardTeam);
			String teamName = (String) nms.getField("PacketPlayOutScoreboardTeam_NAME").get(packetPlayOutScoreboardTeam);
			if (players == null) return;
			//creating a new list to prevent NoSuchFieldException in minecraft packet encoder when a player is removed
			Collection<String> newList = new ArrayList<>();
			for (String entry : players) {
				TabPlayer p = tab.getPlayer(entry);
				if (p == null) {
					newList.add(entry);
					continue;
				}
				if (!tab.getFeatureManager().getNameTagFeature().getPlayersInDisabledWorlds().contains(p) && !p.hasTeamHandlingPaused() && 
						!teamName.equals(p.getTeamName())) {
					logTeamOverride(teamName, entry);
				} else {
					newList.add(entry);
				}
			}
			nms.setField(packetPlayOutScoreboardTeam, "PacketPlayOutScoreboardTeam_PLAYERS", newList);
			tab.getCPUManager().addTime(TabFeature.NAMETAGS, UsageType.ANTI_OVERRIDE, System.nanoTime()-time);
		}
	}
}