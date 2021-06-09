package me.neznamy.tab.platforms.bungeecord;

import java.util.Collection;
import java.util.NoSuchElementException;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.PipelineInjector;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.packet.Login;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.ScoreboardDisplay;
import net.md_5.bungee.protocol.packet.ScoreboardObjective;
import net.md_5.bungee.protocol.packet.Team;

public class BungeePipelineInjector extends PipelineInjector {

	//handler to inject before
	private final String INJECT_POSITION = "inbound-boss";

	/**
	 * Constructs new instance with given parameter
	 * @param tab - tab instance
	 */
	public BungeePipelineInjector(TAB tab) {
		super(tab);
	}

	@Override
	public void inject(TabPlayer player) {
		if (player.getVersion().getMinorVersion() < 8) return;
		uninject(player);
		try {
			player.getChannel().pipeline().addBefore(INJECT_POSITION, DECODER_NAME, new BungeeChannelDuplexHandler(player));
		} catch (NoSuchElementException | IllegalArgumentException e) {
			//idk how does this keep happening but whatever
		}
	}

	@Override
	public void uninject(TabPlayer player) {
		if (player.getVersion().getMinorVersion() < 8) return;
		if (player.getChannel().pipeline().names().contains(DECODER_NAME)) player.getChannel().pipeline().remove(DECODER_NAME);
	}

	/**
	 * Removes all real players from packet if the packet doesn't come from TAB
	 * @param packet - packet to modify
	 */
	private void modifyPlayers(Team packet){
		long time = System.nanoTime();
		if (packet.getPlayers() == null) return;
		if (packet.getFriendlyFire() != 69) {
			Collection<String> col = Lists.newArrayList(packet.getPlayers());
			for (TabPlayer p : tab.getPlayers()) {
				if (col.contains(p.getName()) && !tab.getFeatureManager().getNameTagFeature().isDisabledWorld(p.getWorldName()) && !p.hasTeamHandlingPaused()) {
					logTeamOverride(packet.getName(), p.getName());
					col.remove(p.getName());
				}
			}
			packet.setPlayers(col.toArray(new String[0]));
		}
		tab.getCPUManager().addTime(TabFeature.NAMETAGS, UsageType.ANTI_OVERRIDE, System.nanoTime()-time);
	}
	
	/**
	 * Custom channel duplex handler override
	 */
	public class BungeeChannelDuplexHandler extends ChannelDuplexHandler {
		
		//injected player
		private TabPlayer player;
		
		/**
		 * Constructs new instance with given player
		 * @param player - player to inject
		 */
		public BungeeChannelDuplexHandler(TabPlayer player) {
			this.player = player;
		}

		@Override
		public void write(ChannelHandlerContext context, Object packet, ChannelPromise channelPromise) throws Exception {
			try {
				if (packet instanceof PlayerListItem) {
					super.write(context, tab.getFeatureManager().onPacketPlayOutPlayerInfo(player, packet), channelPromise);
					return;
				}
				if (tab.getFeatureManager().isFeatureEnabled("nametag16") && antiOverrideTeams) {
					if (packet instanceof Team) {
						//team packet coming from a bungeecord plugin
						modifyPlayers((Team) packet);
						super.write(context, packet, channelPromise);
						return;
					}
					if (packet instanceof ByteBuf) {
						long time = System.nanoTime();
						ByteBuf buf = ((ByteBuf) packet);
						int marker = buf.readerIndex();
						int packetId = buf.readByte();
						if (packetId == ((BungeeTabPlayer)player).getPacketId(Team.class)) {
							//team packet sent by backend server, proxy does not deserialize those for whatever reason
							Team team = new Team();
							team.read(buf, null, ((ProxiedPlayer)player.getPlayer()).getPendingConnection().getVersion());
							buf.release();
							tab.getCPUManager().addTime(TabFeature.NAMETAGS, UsageType.ANTI_OVERRIDE, System.nanoTime()-time);
							modifyPlayers(team);
							super.write(context, team, channelPromise);
							return;
						} else if (packetId + 128 == ((BungeeTabPlayer)player).getPacketId(Team.class)){
							//compressed team packet when using protocolsupport, just kill it as it does not come from tab anyway
							buf.release();
							return;
						}
						buf.readerIndex(marker);
					}
				}
				if (packet instanceof ScoreboardDisplay && antiOverrideObjectives && tab.getFeatureManager().onDisplayObjective(player, packet)) {
					//TODO add support for serialized packets as above with teams
					return;
				}
				if (packet instanceof ScoreboardObjective && antiOverrideObjectives) {
					//TODO add support for serialized packets as above with teams
					tab.getFeatureManager().onObjective(player, packet);
				}
				//client reset packet
				if (packet instanceof Login) {
					//making sure to not send own packets before login packet is actually sent
					super.write(context, packet, channelPromise);
					tab.getFeatureManager().onLoginPacket(player);
					return;
				}
			} catch (Throwable e){
				tab.getErrorManager().printError("An error occurred when analyzing packets for player " + player.getName() + " with client version " + player.getVersion().getFriendlyName(), e);
			}
			super.write(context, packet, channelPromise);
		}
	}
}