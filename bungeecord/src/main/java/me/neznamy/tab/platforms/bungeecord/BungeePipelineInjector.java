package me.neznamy.tab.platforms.bungeecord;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.bungeecord.redisbungee.RedisBungeeSupport;
import me.neznamy.tab.platforms.bungeecord.redisbungee.RedisPlayer;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.NickCompatibility;
import me.neznamy.tab.shared.features.PipelineInjector;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.packet.ScoreboardDisplay;
import net.md_5.bungee.protocol.packet.ScoreboardObjective;
import net.md_5.bungee.protocol.packet.Team;

public class BungeePipelineInjector extends PipelineInjector {

	//packets that must be deserialized and BungeeCord does not do it automatically
	private final Map<Class<? extends DefinedPacket>, Supplier<DefinedPacket>> extraPackets = new HashMap<>();

	/**
	 * Constructs new instance of the feature
	 */
	public BungeePipelineInjector() {
		super("inbound-boss");
		channelFunction = BungeeChannelDuplexHandler::new;
		extraPackets.put(Team.class, Team::new);
		extraPackets.put(ScoreboardDisplay.class, ScoreboardDisplay::new);
		extraPackets.put(ScoreboardObjective.class, ScoreboardObjective::new);
	}

	/**
	 * Custom channel duplex handler override
	 */
	public class BungeeChannelDuplexHandler extends ChannelDuplexHandler {

		//injected player
		private final TabPlayer player;

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
			Object modifiedPacket = packet instanceof ByteBuf && byteBufDeserialization ? deserialize((ByteBuf) packet) : packet;
			TAB.getInstance().getCPUManager().addTime("Packet deserializing", TabConstants.CpuUsageCategory.BYTE_BUF, System.nanoTime()-time);
			try {
				switch(modifiedPacket.getClass().getSimpleName()) {
				case "PlayerListItem":
					super.write(context, TAB.getInstance().getFeatureManager().onPacketPlayOutPlayerInfo(player, modifiedPacket), channelPromise);
					return;
				case "Team":
					if (antiOverrideTeams) {
						modifyPlayers((Team) modifiedPacket);
					}
					break;
				case "ScoreboardDisplay":
					TAB.getInstance().getFeatureManager().onDisplayObjective(player, modifiedPacket);
					break;
				case "ScoreboardObjective":
					TAB.getInstance().getFeatureManager().onObjective(player, modifiedPacket);
					break;
				case "Login":
					//making sure to not send own packets before login packet is actually sent
					super.write(context, modifiedPacket, channelPromise);
					TAB.getInstance().getFeatureManager().onLoginPacket(player);
					return;
				default:
					break;
				}
			} catch (Exception e){
				TAB.getInstance().getErrorManager().printError("An error occurred when analyzing packets for player " + player.getName() + " with client version " + player.getVersion().getFriendlyName(), e);
			}
			try {
				super.write(context, modifiedPacket, channelPromise);
			} catch (Exception e) {
				TAB.getInstance().getErrorManager().printError("Failed to forward packet " + modifiedPacket.getClass().getSimpleName() + " to " + player.getName(), e);
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
			for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
				if (col.contains(getName(p)) && !((TabFeature)TAB.getInstance().getTeamManager()).isDisabledPlayer(p) && 
						!TAB.getInstance().getTeamManager().hasTeamHandlingPaused(p) && !packet.getName().equals(p.getTeamName())) {
					logTeamOverride(packet.getName(), p.getName(), p.getTeamName());
					col.remove(getName(p));
				}
			}
			RedisBungeeSupport redis = (RedisBungeeSupport) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.REDIS_BUNGEE);
			if (redis != null) {
				for (RedisPlayer p : redis.getRedisPlayers().values()) {
					if (col.contains(p.getName()) && !packet.getName().equals(p.getTeamName())) {
						logTeamOverride(packet.getName(), p.getName(), p.getTeamName());
						col.remove(p.getName());
					}
				}
			}
			packet.setPlayers(col.toArray(new String[0]));
			TAB.getInstance().getCPUManager().addTime("NameTags", TabConstants.CpuUsageCategory.ANTI_OVERRIDE, System.nanoTime()-time);
		}

		private String getName(TabPlayer p) {
			NickCompatibility nick = (NickCompatibility) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.NICK_COMPATIBILITY);
			if (nick != null) {
				return nick.getNickname(p);
			}
			return p.getName();
		}

		/**
		 * Deserializes byte buf in case it is one of the tracked packets coming from backend server and returns it.
		 * If the packet is not one of them, returns input
		 * @param buf - byte buf to deserialize
		 * @return deserialized packet or input byte buf if packet is not tracked
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
				//rare OverflowPacketException or IndexOutOfBoundsException
			}
			buf.readerIndex(marker);
			return buf;
		}
	}
}