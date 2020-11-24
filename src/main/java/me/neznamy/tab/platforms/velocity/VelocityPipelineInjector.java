package me.neznamy.tab.platforms.velocity;

import java.util.Collection;
import java.util.UUID;

import com.google.common.collect.Lists;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.velocity.protocol.Team;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.PipelineInjector;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;

public class VelocityPipelineInjector extends PipelineInjector {
	
	private static final String INJECT_POSITION = "handler";
	
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
					if (packet.getClass().getSimpleName().equals("PlayerListItem")) {
						PacketPlayOutPlayerInfo info = deserializeInfoPacket(packet, player);
						Shared.featureManager.onPacketPlayOutPlayerInfo(player, info);
						super.write(context, serializeInfoPacket(info, player), channelPromise);
						return;
					}
					if (packet instanceof Team && Shared.featureManager.isFeatureEnabled("nametag16")) {
						long time = System.nanoTime();
						modifyPlayers((Team) packet);
						Shared.cpu.addTime(TabFeature.NAMETAGS, UsageType.ANTI_OVERRIDE, System.nanoTime()-time);
						super.write(context, packet, channelPromise);
						return;
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
		if (packet.players == null) return;
		if (packet.getFriendlyFire() != 69) {
			Collection<String> col = Lists.newArrayList(packet.getPlayers());
			for (TabPlayer p : Shared.getPlayers()) {
				if (col.contains(p.getName()) && !Shared.featureManager.getNameTagFeature().isDisabledWorld(p.getWorldName())) {
					col.remove(p.getName());
				}
			}
			packet.players = col.toArray(new String[0]);
		}
	}
}