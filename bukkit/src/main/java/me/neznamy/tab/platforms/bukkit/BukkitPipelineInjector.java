package me.neznamy.tab.platforms.bukkit;

import java.util.ArrayList;
import java.util.Collection;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.NickCompatibility;
import me.neznamy.tab.shared.features.PipelineInjector;
import org.jetbrains.annotations.NotNull;

public class BukkitPipelineInjector extends PipelineInjector {

	//nms storage
	private final NMSStorage nms = NMSStorage.getInstance();

	/**
	 * Constructs new instance
	 */
	public BukkitPipelineInjector(){
		super("packet_handler");
		channelFunction = BukkitChannelDuplexHandler::new;
	}

	/**
	 * Custom channel duplex handler override
	 */
	public class BukkitChannelDuplexHandler extends ChannelDuplexHandler {
		
		//injected player
		private final TabPlayer player;
		
		/**
		 * Constructs new instance with given player
		 * @param player - player to inject
		 */
		public BukkitChannelDuplexHandler(TabPlayer player) {
			this.player = player;
		}

		@Override
		public void channelRead(@NotNull ChannelHandlerContext context, @NotNull Object packet) {
			try {
				if (TAB.getInstance().getFeatureManager().onPacketReceive(player, packet)) return;
				super.channelRead(context, packet);
			} catch (Exception e){
				TAB.getInstance().getErrorManager().printError("An error occurred when reading packets", e);
			}
		}

		@Override
		public void write(ChannelHandlerContext context, Object packet, ChannelPromise channelPromise) {
			try {
				if (nms.PacketPlayOutPlayerInfo.isInstance(packet)) {
					super.write(context, TAB.getInstance().getFeatureManager().onPacketPlayOutPlayerInfo(player, packet), channelPromise);
					return;
				}
				if (antiOverrideTeams && nms.PacketPlayOutScoreboardTeam != null && nms.PacketPlayOutScoreboardTeam.isInstance(packet)) {
					modifyPlayers(packet);
					super.write(context, packet, channelPromise);
					return;
				}
				if (nms.PacketPlayOutScoreboardDisplayObjective.isInstance(packet)){
					TAB.getInstance().getFeatureManager().onDisplayObjective(player, packet);
				}
				if (nms.PacketPlayOutScoreboardObjective.isInstance(packet)) {
					TAB.getInstance().getFeatureManager().onObjective(player, packet);
				}
				TAB.getInstance().getFeatureManager().onPacketSend(player, packet);
			} catch (Exception e){
				TAB.getInstance().getErrorManager().printError("An error occurred when reading packets", e);
			}
			try {
				super.write(context, packet, channelPromise);
			} catch (Exception e) {
				TAB.getInstance().getErrorManager().printError("Failed to forward packet " + packet.getClass().getSimpleName() + " to " + player.getName(), e);
			}
		}
		
		/**
		 * Removes all real players from team if packet does not come from TAB and reports this to override log
		 * @param packetPlayOutScoreboardTeam - team packet
		 * @throws	ReflectiveOperationException
		 * 			nmsGameMode
		 */
		@SuppressWarnings("unchecked")
		private void modifyPlayers(Object packetPlayOutScoreboardTeam) throws ReflectiveOperationException {
			long time = System.nanoTime();
			Collection<String> players = (Collection<String>) nms.PacketPlayOutScoreboardTeam_PLAYERS.get(packetPlayOutScoreboardTeam);
			String teamName = (String) nms.PacketPlayOutScoreboardTeam_NAME.get(packetPlayOutScoreboardTeam);
			if (players == null) return;
			//creating a new list to prevent NoSuchFieldException in minecraft packet encoder when a player is removed
			Collection<String> newList = new ArrayList<>();
			for (String entry : players) {
				TabPlayer p = getPlayer(entry);
				if (p == null) {
					newList.add(entry);
					continue;
				}
				if (!((TabFeature)TAB.getInstance().getTeamManager()).isDisabledPlayer(p) && 
						!TAB.getInstance().getTeamManager().hasTeamHandlingPaused(p) && !teamName.equals(p.getTeamName())) {
					logTeamOverride(teamName, p.getName(), p.getTeamName());
				} else {
					newList.add(entry);
				}
			}
			nms.setField(packetPlayOutScoreboardTeam, nms.PacketPlayOutScoreboardTeam_PLAYERS, newList);
			TAB.getInstance().getCPUManager().addTime("NameTags", TabConstants.CpuUsageCategory.ANTI_OVERRIDE, System.nanoTime()-time);
		}
		
		private TabPlayer getPlayer(String name) {
			for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
				if (getName(p).equals(name)) return p;
			}
			return null;
		}

		private String getName(TabPlayer p) {
			return ((NickCompatibility) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.NICK_COMPATIBILITY)).getNickname(p);
		}
	}
}