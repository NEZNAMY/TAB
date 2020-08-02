package me.neznamy.tab.platforms.bungee;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.command.TabCommand;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.cpu.CPUFeature;
import me.neznamy.tab.shared.features.BelowName;
import me.neznamy.tab.shared.features.TabObjective;
import me.neznamy.tab.shared.features.interfaces.PlayerInfoPacketListener;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.placeholders.Placeholders;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.packet.Login;
import net.md_5.bungee.protocol.packet.Team;

public class Main extends Plugin{

	public static PluginMessenger plm;

	public void onEnable(){
		ProtocolVersion.SERVER_VERSION = ProtocolVersion.values()[1];
		Shared.platform = new BungeeMethods();
		Shared.separatorType = "server";
		Configs.dataFolder = getDataFolder();
		getProxy().getPluginManager().registerListener(this, new BungeeEventListener());
		if (getProxy().getPluginManager().getPlugin("PremiumVanish") != null) getProxy().getPluginManager().registerListener(this, new PremiumVanishListener());
		Shared.command = new TabCommand();
		getProxy().getPluginManager().registerCommand(this, new Command("btab") {
			@SuppressWarnings("deprecation")
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
		new PluginMessenger(this);
		Shared.load(true);
		Metrics.start(this);
	}
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
				try{
					if (packet instanceof DefinedPacket) {
						PacketPlayOutPlayerInfo info = PacketPlayOutPlayerInfo.fromBungee(packet, player.getVersion());
						if (info != null) {
							for (PlayerInfoPacketListener f : Shared.playerInfoListeners) {
								long time = System.nanoTime();
								if (info != null) info = f.onPacketSend(player, info);
								Shared.featureCpu.addTime(f.getCPUName(), System.nanoTime()-time);
							}
							packet = (info == null ? null : info.toBungee(player.getVersion()));
						}
					}
					if (Shared.features.containsKey("nametag16")) {
						if (packet instanceof Team) {
							Shared.errorManager.printError("[" + player.getName() + "] Receiving team packet" + ((Team)packet).toString());
							if (killPacket((Team) packet)) {
								Shared.errorManager.printError("[" + player.getName() + "] Killing");
								return;
							} else {
								Shared.errorManager.printError("[" + player.getName() + "] Keeping");
							}
						}
						if (packet instanceof ByteBuf) {
							ByteBuf buf = ((ByteBuf) packet).duplicate();
							if (buf.readByte() == ((TabPlayer)player).getPacketId(Team.class)) {
								Team team = new Team();
								team.read(buf, null, player.getVersion().getNetworkId());
								Shared.errorManager.printError("[" + player.getName() + "] Receiving raw team packet" + team.toString());
								if (killPacket(team)) {
									Shared.errorManager.printError("[" + player.getName() + "] Killing");
									return;
								} else {
									Shared.errorManager.printError("[" + player.getName() + "] Keeping");
								}
							}
						}
						if (packet instanceof Login) {
							//registering all teams again because client reset packet is sent
							Shared.errorManager.printError("[" + player.getName() + "] Receiving login packet");
							Shared.featureCpu.runTaskLater(100, "Reapplying scoreboard components", CPUFeature.WATERFALLFIX, new Runnable() {

								@Override
								public void run() {
									if (Shared.features.containsKey("nametag16")) {
										for (ITabPlayer all : Shared.getPlayers()) {
											all.registerTeam(player);
										}
									}
									TabObjective objective = (TabObjective) Shared.features.get("tabobjective");
									if (objective != null) {
										objective.onJoin(player);
									}
									BelowName belowname = (BelowName) Shared.features.get("belowname");
									if (belowname != null) {
										belowname.onJoin(player);
									}
								}
							});
						}
					}
				} catch (Throwable e){
					Shared.errorManager.printError("An error occurred when analyzing packets for player " + player.getName() + " with client version " + player.getVersion().getFriendlyName(), e);
				}
				super.write(context, packet, channelPromise);
			}
		});
	}
	public static boolean killPacket(Team packet){
		if (packet.getFriendlyFire() != 69) {
			String[] players = packet.getPlayers();
			if (players == null) return false;
			for (ITabPlayer p : Shared.getPlayers()) {
				for (String player : players) {
					if (player.equals(p.getName()) && !p.disabledNametag) {
						return true;
					}
				}
			}
		}
		return false;
	}
}