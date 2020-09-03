package me.neznamy.tab.platforms.bungee;

import java.util.Collection;
import java.util.UUID;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.BelowName;
import me.neznamy.tab.shared.features.TabObjective;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.UniversalPacketPlayOut;
import me.neznamy.tab.shared.placeholders.Placeholders;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.protocol.packet.Login;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.Team;

/**
 * Main class for BungeeCord platform
 */
public class Main extends Plugin {

	public static PluginMessenger plm;

	@SuppressWarnings("deprecation")
	@Override
	public void onEnable(){
		if (!isVersionSupported()) {
			ProxyServer.getInstance().getConsole().sendMessage("\u00a7c[TAB] The plugin requires BungeeCord build #1330 and up to work. Get it at https://ci.md-5.net/job/BungeeCord/");
			Shared.disabled = true;
			return;
		}
		ProtocolVersion.SERVER_VERSION = ProtocolVersion.values()[1];
		Shared.platform = new BungeeMethods(this);
		UniversalPacketPlayOut.builder = new BungeePacketBuilder();
		getProxy().getPluginManager().registerListener(this, new BungeeEventListener());
		if (getProxy().getPluginManager().getPlugin("PremiumVanish") != null) getProxy().getPluginManager().registerListener(this, new PremiumVanishListener());
		getProxy().getPluginManager().registerCommand(this, new Command("btab") {

			public void execute(CommandSender sender, String[] args) {
				if (Shared.disabled) {
					if (args.length == 1 && args[0].toLowerCase().equals("reload")) {
						if (sender.hasPermission("tab.reload")) {
							Shared.unload();
							Shared.load(false);
							if (Shared.disabled) {
								if (sender instanceof ProxiedPlayer) {
									sender.sendMessage(Placeholders.color(Configs.reloadFailed.replace("%file%", Shared.brokenFile)));
								}
							} else {
								sender.sendMessage(Placeholders.color(Configs.reloaded));
							}
						} else {
							sender.sendMessage(Placeholders.color(Configs.no_perm));
						}
					} else {
						if (sender.hasPermission("tab.admin")) {
							sender.sendMessage(Placeholders.color("&m                                                                                "));
							sender.sendMessage(Placeholders.color(" &c&lPlugin is disabled due to a broken configuration file (" + Shared.brokenFile + ")"));
							sender.sendMessage(Placeholders.color(" &8>> &3&l/tab reload"));
							sender.sendMessage(Placeholders.color("      - &7Reloads plugin and config"));
							sender.sendMessage(Placeholders.color("&m                                                                                "));
						}
					}
				} else {
					Shared.command.execute(sender instanceof ProxiedPlayer ? Shared.getPlayer(((ProxiedPlayer)sender).getUniqueId()) : null, args);
				}
			}
		});
		plm = new PluginMessenger(this);
		Shared.load(true);
		BungeeMetrics.start(this);
	}
	
	private boolean isVersionSupported() {
		try {
			Class.forName("net.md_5.bungee.protocol.packet.ScoreboardObjective$HealthDisplay");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
	
	@Override
	public void onDisable() {
		if (!Shared.disabled) {
			for (ITabPlayer p : Shared.getPlayers()) p.channel.pipeline().remove(Shared.DECODER_NAME);
			Shared.unload();
		}
	}
	
	public static void inject(UUID uuid) {
		Channel channel = Shared.getPlayer(uuid).channel;
		if (channel.pipeline().names().contains(Shared.DECODER_NAME)) channel.pipeline().remove(Shared.DECODER_NAME);
		channel.pipeline().addBefore("inbound-boss", Shared.DECODER_NAME, new ChannelDuplexHandler() {

			public void channelRead(ChannelHandlerContext context, Object packet) throws Exception {
				super.channelRead(context, packet);
			}
			public void write(ChannelHandlerContext context, Object packet, ChannelPromise channelPromise) throws Exception {
				ITabPlayer player = Shared.getPlayer(uuid);
				if (player == null) {
					super.write(context, packet, channelPromise);
					return;
				}
				try {
					if (packet instanceof PlayerListItem) {
						PacketPlayOutPlayerInfo info = Shared.featureManager.onPacketPlayOutPlayerInfo(player, BungeePacketBuilder.fromBungee(packet, player.getVersion()));
						packet = (info == null ? null : info.create(player.getVersion()));
					}
					if (Shared.featureManager.isFeatureEnabled("nametag16")) {
						long time = System.nanoTime();
						if (packet instanceof Team) {
							modifyPlayers((Team) packet);
						}
						if (packet instanceof ByteBuf) {
							ByteBuf buf = ((ByteBuf) packet).duplicate();
							if (buf.readByte() == ((TabPlayer)player).getPacketId(Team.class)) {
								Team team = new Team();
								team.read(buf, null, player.getVersion().getNetworkId());
								modifyPlayers(team);
								packet = team;
							}
						}
						Shared.cpu.addTime(TabFeature.NAMETAGS, UsageType.PACKET_READING, System.nanoTime()-time);
					}
					if (packet instanceof Login) {
						//registering all teams again because client reset packet is sent
						Shared.cpu.runTaskLater(100, "Reapplying scoreboard components", TabFeature.WATERFALLFIX, UsageType.PACKET_READING, new Runnable() {

							@Override
							public void run() {
								if (Shared.featureManager.isFeatureEnabled("nametag16")) {
									for (ITabPlayer all : Shared.getPlayers()) {
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
				if (packet != null) super.write(context, packet, channelPromise);
			}
		});
	}
	public static void modifyPlayers(Team packet){
		if (packet.getPlayers() == null) return;
		if (packet.getFriendlyFire() != 69) {
			Collection<String> col = Lists.newArrayList(packet.getPlayers());
			for (ITabPlayer p : Shared.getPlayers()) {
				if (col.contains(p.getName()) && !p.disabledNametag) {
					col.remove(p.getName());
				}
			}
			packet.setPlayers(col.toArray(new String[0]));
		}
	}
}