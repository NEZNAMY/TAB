package me.neznamy.tab.platforms.velocity;

import java.util.Collection;

import com.google.common.collect.Lists;
import com.velocitypowered.proxy.protocol.packet.ScoreboardDisplay;
import com.velocitypowered.proxy.protocol.packet.Team;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.PipelineInjector;

public class VelocityPipelineInjector extends PipelineInjector {

	private final String INJECT_POSITION = "handler";

	public VelocityPipelineInjector(TAB tab) {
		super(tab);
	}

	@Override
	public void inject(TabPlayer player) {
		if (player.getVersion().getMinorVersion() < 8) return;
		uninject(player);
		player.getChannel().pipeline().addBefore(INJECT_POSITION, DECODER_NAME, new ChannelDuplexHandler() {

			public void write(ChannelHandlerContext context, Object packet, ChannelPromise channelPromise) throws Exception {
				try {
					if (packet.getClass().getSimpleName().equals("PlayerListItem")) {
						super.write(context, tab.getFeatureManager().onPacketPlayOutPlayerInfo(player, packet), channelPromise);
						return;
					}
					if (packet instanceof Team && tab.getFeatureManager().isFeatureEnabled("nametag16")) {
						modifyPlayers((Team) packet);
						super.write(context, packet, channelPromise);
						return;
					}
					if (packet.getClass().getSimpleName().equals("JoinGame")) {
						//making sure to not send own packets before join packet is actually sent
						super.write(context, packet, channelPromise);
						tab.getFeatureManager().onLoginPacket(player);
						return;
					}
					if (packet instanceof ScoreboardDisplay && tab.getFeatureManager().onDisplayObjective(player, packet)) {
						return;
					}
				} catch (Throwable e){
					tab.getErrorManager().printError("An error occurred when analyzing packets for player " + player.getName() + " with client version " + player.getVersion().getFriendlyName(), e);
				}
				super.write(context, packet, channelPromise);
			}
		});
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
		if (packet.players == null) return;
		if (packet.getFriendlyFire() != 69) {
			Collection<String> col = Lists.newArrayList(packet.getPlayers());
			for (TabPlayer p : tab.getPlayers()) {
				if (col.contains(p.getName()) && !tab.getFeatureManager().getNameTagFeature().isDisabledWorld(p.getWorldName())) {
					logTeamOverride(packet.name, p.getName());
					col.remove(p.getName());
				}
			}
			packet.players = col.toArray(new String[0]);
		}
		tab.getCPUManager().addTime(TabFeature.NAMETAGS, UsageType.ANTI_OVERRIDE, System.nanoTime()-time);
	}
}