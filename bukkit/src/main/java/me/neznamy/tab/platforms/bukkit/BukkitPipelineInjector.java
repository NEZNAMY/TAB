package me.neznamy.tab.platforms.bukkit;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import java.util.ArrayList;
import java.util.Collection;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.AdapterProvider;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.NickCompatibility;
import me.neznamy.tab.shared.features.PipelineInjector;

public class BukkitPipelineInjector extends PipelineInjector {

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
				if (TAB.getInstance().getFeatureManager().onPacketReceive(player, packet)) return;
				super.channelRead(context, packet);
			} catch (Exception e){
				TAB.getInstance().getErrorManager().printError("An error occurred when reading packets", e);
			}
		}

		@Override
		public void write(ChannelHandlerContext context, Object packet, ChannelPromise channelPromise) {
			try {
				if (AdapterProvider.get().isPlayerInfoPacket(packet)) {
					super.write(context, TAB.getInstance().getFeatureManager().onPacketPlayOutPlayerInfo(player, packet), channelPromise);
					return;
				}
				if (antiOverrideTeams && AdapterProvider.get().isTeamPacket(packet)) {
					modifyPlayers(packet);
					super.write(context, packet, channelPromise);
					return;
				}
				if (AdapterProvider.get().isDisplayObjectivePacket(packet) && TAB.getInstance().getFeatureManager().onDisplayObjective(player, packet)) {
					return;
				}
				if (AdapterProvider.get().isObjectivePacket(packet)) {
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
		 * @throws ReflectiveOperationException 
		 */
		@SuppressWarnings("unchecked")
		private void modifyPlayers(Object packetPlayOutScoreboardTeam) throws ReflectiveOperationException {
			long time = System.nanoTime();
			final Collection<String> players = AdapterProvider.get().getTeamPlayers(packetPlayOutScoreboardTeam);
			final String teamName = AdapterProvider.get().getTeamName(packetPlayOutScoreboardTeam);
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
			AdapterProvider.get().setTeamPlayers(packetPlayOutScoreboardTeam, newList);
			TAB.getInstance().getCPUManager().addTime("Nametags", TabConstants.CpuUsageCategory.ANTI_OVERRIDE, System.nanoTime()-time);
		}
		
		private TabPlayer getPlayer(String name) {
			for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
				if (getName(p).equals(name)) return p;
			}
			return null;
		}

		
		private String getName(TabPlayer p) {
			return ((NickCompatibility) TAB.getInstance().getFeatureManager().getFeature("nick")).getNickname(p);
		}
	}
}
