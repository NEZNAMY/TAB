package me.neznamy.tab.platforms.bungeecord;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

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
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.packet.Login;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.ScoreboardDisplay;
import net.md_5.bungee.protocol.packet.ScoreboardObjective;
import net.md_5.bungee.protocol.packet.Team;

public class BungeePipelineInjector extends PipelineInjector {

	//handler to inject before
	private static final String INJECT_POSITION = "inbound-boss";
	
	//packets that must be deserialized and bungeecord does not do it automatically
	private Map<Class<? extends DefinedPacket>, Supplier<DefinedPacket>> extraPackets = new HashMap<>();

	/**
	 * Constructs new instance with given parameter
	 * @param tab - tab instance
	 */
	public BungeePipelineInjector(TAB tab) {
		super(tab);
		extraPackets.put(Team.class, Team::new);
		extraPackets.put(ScoreboardDisplay.class, ScoreboardDisplay::new);
		extraPackets.put(ScoreboardObjective.class, ScoreboardObjective::new);
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
		public void write(ChannelHandlerContext context, Object packet, ChannelPromise channelPromise) {
			long time = System.nanoTime();
			Object modifiedPacket = packet instanceof ByteBuf ? deserialize((ByteBuf) packet) : packet;
			tab.getCPUManager().addTime(TabFeature.PACKET_DESERIALIZING, UsageType.PACKET_READING_OUT, System.nanoTime()-time);
			try {
				if (modifiedPacket instanceof PlayerListItem) {
					super.write(context, tab.getFeatureManager().onPacketPlayOutPlayerInfo(player, modifiedPacket), channelPromise);
					return;
				}
				if (tab.getFeatureManager().isFeatureEnabled("nametag16") && antiOverrideTeams) {
					time = System.nanoTime();
					if (modifiedPacket instanceof Team) {
						modifyPlayers((Team) modifiedPacket);
					}
					tab.getCPUManager().addTime(TabFeature.NAMETAGS, UsageType.ANTI_OVERRIDE, System.nanoTime()-time);
				}
				if (modifiedPacket instanceof ScoreboardDisplay && antiOverrideObjectives && tab.getFeatureManager().onDisplayObjective(player, modifiedPacket)) {
					return;
				}
				if (modifiedPacket instanceof ScoreboardObjective && antiOverrideObjectives) {
					tab.getFeatureManager().onObjective(player, modifiedPacket);
				}
				//client reset packet
				if (modifiedPacket instanceof Login) {
					//making sure to not send own packets before login packet is actually sent
					super.write(context, modifiedPacket, channelPromise);
					tab.getFeatureManager().onLoginPacket(player);
					return;
				}
			} catch (Throwable e){
				tab.getErrorManager().printError("An error occurred when analyzing packets for player " + player.getName() + " with client version " + player.getVersion().getFriendlyName(), e);
			}
			try {
				super.write(context, modifiedPacket, channelPromise);
			} catch (Throwable e) {
				tab.getErrorManager().printError("Failed to forward packet " + modifiedPacket.getClass().getSimpleName() + " to " + player.getName(), e);
			}
		}

		/**
		 * Removes all real players from packet if the packet doesn't come from TAB
		 * @param packet - packet to modify
		 */
		private void modifyPlayers(Team packet){
			long time = System.nanoTime();
			if (packet.getPlayers() == null) return;
			Collection<String> col = Lists.newArrayList(packet.getPlayers());
			for (TabPlayer p : tab.getPlayers()) {
				if (col.contains(p.getName()) && !tab.getFeatureManager().getNameTagFeature().getPlayersInDisabledWorlds().contains(p) && 
						!p.hasTeamHandlingPaused() && !packet.getName().equals(p.getTeamName())) {
					logTeamOverride(packet.getName(), p.getName());
					col.remove(p.getName());
				}
			}
			packet.setPlayers(col.toArray(new String[0]));
			tab.getCPUManager().addTime(TabFeature.NAMETAGS, UsageType.ANTI_OVERRIDE, System.nanoTime()-time);
		}
		
		/**
		 * Deserializes bytebuf in case it is one of the tracked packets coming from backend server and returns it.
		 * If the packet is not one of them, returns input
		 * @param buf - bytebuf to deserialize
		 * @return deserialized packet or input bytebuf if packet is not tracked
		 */
		private Object deserialize(ByteBuf buf) {
			int marker = buf.readerIndex();
			try {
				int packetId = buf.readByte();
				for (Entry<Class<? extends DefinedPacket>, Supplier<DefinedPacket>> e : extraPackets.entrySet()) {
					if (packetId == ((BungeeTabPlayer)player).getPacketId(e.getKey())) {
						DefinedPacket packet = e.getValue().get();
						packet.read(buf, null, ((ProxiedPlayer)player.getPlayer()).getPendingConnection().getVersion());
						buf.release();
						return packet;
					}
				}
			} catch (Exception e) {
				//OverflowPacketException someone got, no idea why
			}
			buf.readerIndex(marker);
			return buf;
		}
	}
}