package me.neznamy.tab.platforms.bungee;

import java.util.Collection;
import java.util.UUID;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.BelowName;
import me.neznamy.tab.shared.features.PipelineInjector;
import me.neznamy.tab.shared.features.TabObjective;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.packet.Login;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.Team;

public class BungeePipelineInjector extends PipelineInjector {
	
	private static final String INJECT_POSITION = "inbound-boss";
	
	@Override
	public void inject(UUID uuid) {
		TabPlayer p = Shared.getPlayer(uuid);
		if (p.getVersion().getMinorVersion() < 8) return;
		if (p.getChannel().pipeline().names().contains(DECODER_NAME)) p.getChannel().pipeline().remove(DECODER_NAME);
		p.getChannel().pipeline().addBefore(INJECT_POSITION, DECODER_NAME, new ChannelDuplexHandler() {

			public void write(ChannelHandlerContext context, Object packet, ChannelPromise channelPromise) throws Exception {
				TabPlayer player = Shared.getPlayer(uuid);
				if (player == null) {
					super.write(context, packet, channelPromise);
					return;
				}
				try {
					if (packet instanceof PlayerListItem) {
						super.write(context, Shared.featureManager.onPacketPlayOutPlayerInfo(player, packet), channelPromise);
						return;
					}
					if (Shared.featureManager.isFeatureEnabled("nametag16")) {
						long time = System.nanoTime();
						if (packet instanceof Team) {
							modifyPlayers((Team) packet);
							Shared.cpu.addTime(TabFeature.NAMETAGS, UsageType.ANTI_OVERRIDE, System.nanoTime()-time);
							super.write(context, packet, channelPromise);
							return;
						}
						if (packet instanceof ByteBuf) {
                            // No need to clone the packet, if this errors it doesn't get flushed anyway
							ByteBuf buf = ((ByteBuf) packet);
                            // Mark where the reader index is at
							int marker = buf.readerIndex();
							if (buf.readByte() == ((BungeeTabPlayer)player).getPacketId(Team.class)) {
								Team team = new Team();
								team.read(buf, null, ((ProxiedPlayer)player.getPlayer()).getPendingConnection().getVersion());
                                // From here on this buffers usage has ended, it needs to be released. Refer to
                                // io.netty.util.ReferenceCounted for an explanation.
                                // Side-note: Netty ByteBufs should always be read with try{} finally {buf.release()}
                                // to ensure proper freeing of the buffer all the time.
								buf.release();
								modifyPlayers(team);
								Shared.cpu.addTime(TabFeature.NAMETAGS, UsageType.ANTI_OVERRIDE, System.nanoTime()-time);
								super.write(context, team, channelPromise);
								return;
							}
                            // Reset the reader back if the condition doesnt end the method
							buf.readerIndex(marker);
						}
						Shared.cpu.addTime(TabFeature.NAMETAGS, UsageType.ANTI_OVERRIDE, System.nanoTime()-time);
					}
					if (packet instanceof Login) {
						//registering all teams again because client reset packet is sent
						Shared.cpu.runTaskLater(100, "Reapplying scoreboard components", TabFeature.WATERFALLFIX, UsageType.PACKET_READING, new Runnable() {

							@Override
							public void run() {
								if (Shared.featureManager.isFeatureEnabled("nametag16")) {
									for (TabPlayer all : Shared.getPlayers()) {
										all.registerTeam(player);
									}
								}
								TabObjective objective = (TabObjective) Shared.featureManager.getFeature("tabobjective");
								if (objective != null) {
									objective.onJoin(player);
								}
								BelowName belowname = (BelowName) Shared.featureManager.getFeature("belowname");
								if (belowname != null) {
									belowname.onJoin(player);
								}
							}
						});
					}
				} catch (Throwable e){
					Shared.errorManager.printError("An error occurred when analyzing packets for player " + player.getName() + " with client version " + player.getVersion().getFriendlyName(), e);
				}
				super.write(context, packet, channelPromise);
			}
		});
	}
	
	@Override
	public void uninject(UUID uuid) {
		TabPlayer p = Shared.getPlayer(uuid);
		if (p.getVersion().getMinorVersion() < 8) return;
		if (p.getChannel().pipeline().names().contains(DECODER_NAME)) p.getChannel().pipeline().remove(DECODER_NAME);
	}
	
	/**
	 * Removes all real players from packet if the packet doesn't come from TAB
	 * @param packet - packet to modify
	 */
	private void modifyPlayers(Team packet){
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
	}
}