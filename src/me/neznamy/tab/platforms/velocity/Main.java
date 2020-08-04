package me.neznamy.tab.platforms.velocity;

import java.io.File;
import java.util.UUID;

import com.google.inject.Inject;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.proxy.protocol.MinecraftPacket;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.neznamy.tab.platforms.velocity.protocol.Team;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.command.TabCommand;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.features.interfaces.PlayerInfoPacketListener;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;

@Plugin(id = "tab", name = "TAB", version = "2.8.3", description = "Change a player's tablist prefix/suffix, name tag prefix/suffix, header/footer, bossbar and more", authors = {"NEZNAMY"})
public class Main {

	public ProxyServer server;
	public static PluginMessenger plm;

	@Inject
	public Main(ProxyServer server) {
		this.server = server;
	}
	@Subscribe
	public void onProxyInitialization(ProxyInitializeEvent event) {
		if (!hasRequiredLibs()) {
			System.out.println("\u00a7c[TAB] The plugin requires Velocity 1.1.0 build #158 and up to work. Get it at https://ci.velocitypowered.com/job/velocity-1.1.0/");
			return;
		}
		if (!VelocityPacketRegistry.registerPackets()) {
			System.out.println("\u00a7c[TAB] This plugin version does not support your Velocity version. Update the plugin.");
			return;
		}
		ProtocolVersion.SERVER_VERSION = ProtocolVersion.values()[1];
		Shared.platform = new VelocityMethods(server);
		Shared.separatorType = "server";
		Configs.dataFolder = new File("plugins" + File.separatorChar + "TAB");
		Shared.command = new TabCommand();
		server.getEventManager().register(this, new VelocityEventListener());
		CommandManager cmd = server.getCommandManager();
		cmd.register(cmd.metaBuilder("btab").build(), new Command() {
			public void execute(CommandSource sender, String[] args) {
				if (Shared.disabled) {
					if (args.length == 1 && args[0].toLowerCase().equals("reload")) {
						if (sender.hasPermission("tab.reload")) {
							Shared.unload();
							Shared.load(false);
							if (Shared.disabled) {
								if (sender instanceof Player) {
									sender.sendMessage(VelocityUtils.asColoredComponent(Configs.reloadFailed.replace("%file%", Shared.brokenFile)));
								}
							} else {
								sender.sendMessage(VelocityUtils.asColoredComponent(Configs.reloaded));
							}
						} else {
							sender.sendMessage(VelocityUtils.asColoredComponent(Configs.no_perm));
						}
					} else {
						if (sender.hasPermission("tab.admin")) {
							sender.sendMessage(VelocityUtils.asColoredComponent("&m                                                                                "));
							sender.sendMessage(VelocityUtils.asColoredComponent(" &c&lPlugin is disabled due to a broken configuration file (" + Shared.brokenFile + ")"));
							sender.sendMessage(VelocityUtils.asColoredComponent(" &8>> &3&l/tab reload"));
							sender.sendMessage(VelocityUtils.asColoredComponent("      - &7Reloads plugin and config"));
							sender.sendMessage(VelocityUtils.asColoredComponent("&m                                                                                "));
						}
					}
				} else {
					Shared.command.execute(sender instanceof Player ? Shared.getPlayer(((Player)sender).getUniqueId()) : null, args);
				}
			}
/*				public List<String> suggest(CommandSource sender, String[] args) {
					List<String> sug = command.complete(sender instanceof Player ? Shared.getPlayer(((Player)sender).getUniqueId()) : null, args);
					if (sug == null) {
						sug = new ArrayList<String>();
						for (Player p : server.getAllPlayers()) {
							sug.add(p.getUsername());
						}
					}
					return sug;
				}*/
		});
		plm = new PluginMessenger(this);
		Shared.load(true);
	}

	private boolean hasRequiredLibs() {
		try {
			Class.forName("org.yaml.snakeyaml.Yaml"); //1.1.0+
			Class.forName("net.kyori.adventure.text.Component"); //1.1.0 b158+
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
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
		channel.pipeline().addBefore("handler", Shared.DECODER_NAME, new ChannelDuplexHandler() {

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
					if (packet instanceof MinecraftPacket) {
						PacketPlayOutPlayerInfo info = PacketPlayOutPlayerInfo.fromVelocity(packet);
						if (info != null) {
							for (PlayerInfoPacketListener f : Shared.playerInfoListeners) {
								long time = System.nanoTime();
								if (info != null) info = f.onPacketSend(player, info);
								Shared.featureCpu.addTime(f.getCPUName(), System.nanoTime()-time);
							}
							packet = (info == null ? null : info.toVelocity(player.getVersion()));
						}
					}
					if (packet instanceof Team && Shared.features.containsKey("nametag16")) {
						if (killPacket((Team)packet)) return;
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