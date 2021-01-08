package me.neznamy.tab.platforms.bungee;

import java.util.Collection;
import java.util.NoSuchElementException;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.PipelineInjector;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.packet.Login;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.Team;

public class BungeePipelineInjector extends PipelineInjector {

	private static final String INJECT_POSITION = "inbound-boss";

	@Override
	public void inject(TabPlayer player) {
		if (player.getVersion().getMinorVersion() < 8) return;
		uninject(player);
		try {
			player.getChannel().pipeline().addBefore(INJECT_POSITION, DECODER_NAME, new ChannelDuplexHandler() {

				public void write(ChannelHandlerContext context, Object packet, ChannelPromise channelPromise) throws Exception {
					try {
						if (packet instanceof PlayerListItem) {
							super.write(context, Shared.featureManager.onPacketPlayOutPlayerInfo(player, packet), channelPromise);
							return;
						}
						if (Shared.featureManager.isFeatureEnabled("nametag16")) {
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
									Shared.cpu.addTime(TabFeature.NAMETAGS, UsageType.ANTI_OVERRIDE, System.nanoTime()-time);
									modifyPlayers(team);
									super.write(context, team, channelPromise);
									return;
//								} else if (packetId + 128 == ((BungeeTabPlayer)player).getPacketId(Team.class)){
//									//compressed team packet when using protocolsupport, just kill it as it does not come from tab anyway
//									buf.release();
//									return;
								}
								buf.readerIndex(marker);
							}
						}
						//client reset packet
						if (packet instanceof Login) {
							//making sure to not send own packets before login packet is actually sent
							super.write(context, packet, channelPromise);
							Shared.featureManager.onLoginPacket(player);
							return;
						}
					} catch (Throwable e){
						Shared.errorManager.printError("An error occurred when analyzing packets for player " + player.getName() + " with client version " + player.getVersion().getFriendlyName(), e);
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
			for (TabPlayer p : Shared.getPlayers()) {
				if (col.contains(p.getName()) && !Shared.featureManager.getNameTagFeature().isDisabledWorld(p.getWorldName())) {
					col.remove(p.getName());
				}
			}
			packet.setPlayers(col.toArray(new String[0]));
		}
		Shared.cpu.addTime(TabFeature.NAMETAGS, UsageType.ANTI_OVERRIDE, System.nanoTime()-time);
	}
}