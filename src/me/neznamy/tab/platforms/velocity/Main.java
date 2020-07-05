package me.neznamy.tab.platforms.velocity;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.Supplier;

import com.google.inject.Inject;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.StateRegistry;
import com.velocitypowered.proxy.protocol.StateRegistry.PacketMapping;
import com.velocitypowered.proxy.protocol.StateRegistry.PacketRegistry;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.neznamy.tab.platforms.velocity.protocol.ScoreboardDisplay;
import me.neznamy.tab.platforms.velocity.protocol.ScoreboardObjective;
import me.neznamy.tab.platforms.velocity.protocol.ScoreboardScore;
import me.neznamy.tab.platforms.velocity.protocol.Team;
import me.neznamy.tab.premium.AlignedSuffix;
import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.premium.ScoreboardManager;
import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ConfigurationFile;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.MainClass;
import me.neznamy.tab.shared.PluginHooks;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.command.TabCommand;
import me.neznamy.tab.shared.cpu.CPUFeature;
import me.neznamy.tab.shared.features.BelowName;
import me.neznamy.tab.shared.features.GhostPlayerFix;
import me.neznamy.tab.shared.features.GlobalPlayerlist;
import me.neznamy.tab.shared.features.GroupRefresher;
import me.neznamy.tab.shared.features.HeaderFooter;
import me.neznamy.tab.shared.features.NameTag16;
import me.neznamy.tab.shared.features.PlaceholderManager;
import me.neznamy.tab.shared.features.Playerlist;
import me.neznamy.tab.shared.features.SpectatorFix;
import me.neznamy.tab.shared.features.TabObjective;
import me.neznamy.tab.shared.features.UpdateChecker;
import me.neznamy.tab.shared.features.bossbar.BossBar;
import me.neznamy.tab.shared.features.interfaces.PlayerInfoPacketListener;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.UniversalPacketPlayOut;
import me.neznamy.tab.shared.placeholders.Placeholders;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholder;
import me.neznamy.tab.shared.placeholders.ServerConstant;
import me.neznamy.tab.shared.placeholders.ServerPlaceholder;
import net.kyori.text.Component;
import net.kyori.text.TextComponent;
import net.kyori.text.serializer.gson.GsonComponentSerializer;

@Plugin(id = "tab", name = "TAB", version = "2.8.2", description = "Change a player's tablist prefix/suffix, name tag prefix/suffix, header/footer, bossbar and more", authors = {"NEZNAMY"})
public class Main implements MainClass{

	private Method map;
	
	public ProxyServer server;
	private PluginMessenger plm;

	@Inject
	public Main(ProxyServer server) {
		this.server = server;
	}
	@Subscribe
	public void onProxyInitialization(ProxyInitializeEvent event) {
		try {
			Class.forName("org.yaml.snakeyaml.Yaml");
			me.neznamy.tab.shared.ProtocolVersion.SERVER_VERSION = me.neznamy.tab.shared.ProtocolVersion.values()[1];
			Shared.mainClass = this;
			Shared.separatorType = "server";
			Shared.command = new TabCommand();
			server.getCommandManager().register("btab", new Command() {
				public void execute(CommandSource sender, String[] args) {
					if (Shared.disabled) {
						if (args.length == 1 && args[0].toLowerCase().equals("reload")) {
							if (sender.hasPermission("tab.reload")) {
								Shared.unload();
								Shared.load(false);
								if (Shared.disabled) {
									if (sender instanceof Player) {
										sender.sendMessage(TextComponent.of(Placeholders.color(Configs.reloadFailed.replace("%file%", Shared.brokenFile))));
									}
								} else {
									sender.sendMessage(TextComponent.of(Placeholders.color(Configs.reloaded)));
								}
							} else {
								sender.sendMessage(TextComponent.of(Placeholders.color(Configs.no_perm)));
							}
						} else {
							if (sender.hasPermission("tab.admin")) {
								sender.sendMessage(TextComponent.of(Placeholders.color("&m                                                                                ")));
								sender.sendMessage(TextComponent.of(Placeholders.color(" &c&lPlugin is disabled due to a broken configuration file (" + Shared.brokenFile + ")")));
								sender.sendMessage(TextComponent.of(Placeholders.color(" &8>> &3&l/tab reload")));
								sender.sendMessage(TextComponent.of(Placeholders.color("      - &7Reloads plugin and config")));
								sender.sendMessage(TextComponent.of(Placeholders.color("&m                                                                                ")));
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
			registerPackets();
			plm = new PluginMessenger(this);
			Shared.load(true);
		} catch (ClassNotFoundException e) {
			sendConsoleMessage("&c[TAB] The plugin requires Velocity 1.1.0 and up to work ! Get it at https://ci.velocitypowered.com/job/velocity-1.1.0/");
		}
	}
	public void onDisable() {
		if (!Shared.disabled) {
			for (ITabPlayer p : Shared.getPlayers()) p.channel.pipeline().remove(Shared.DECODER_NAME);
			Shared.unload();
		}
	}
	@Subscribe
	public void a(DisconnectEvent e){
		if (Shared.disabled) return;
		ITabPlayer disconnectedPlayer = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (disconnectedPlayer == null) return; //player connected to bungeecord successfully, but not to the bukkit server anymore ? idk the check is needed
		Shared.data.remove(e.getPlayer().getUniqueId());
		Shared.quitListeners.forEach(f -> f.onQuit(disconnectedPlayer));
	}
	@Subscribe
	public void a(ServerConnectedEvent e){
		try{
			if (Shared.disabled) return;
			if (!Shared.data.containsKey(e.getPlayer().getUniqueId())) {
				//join
				ITabPlayer p = new TabPlayer(e.getPlayer(), e.getServer().getServerInfo().getName());
				Shared.data.put(e.getPlayer().getUniqueId(), p);
				inject(p.getUniqueId());
				//sending custom packets with a delay, it would not work otherwise
				Shared.featureCpu.runTaskLater(50, "processing join", CPUFeature.OTHER, new Runnable() {

					@Override
					public void run() {
						Shared.joinListeners.forEach(f -> f.onJoin(p));
						p.onJoinFinished = true;
					}
				});
			} else {
				//server change
				ITabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
				p.onWorldChange(p.getWorldName(), p.world = e.getServer().getServerInfo().getName());
			}
		} catch (Throwable ex){
			Shared.errorManager.criticalError("An error occurred when player joined/changed server", ex);
		}
	}
/*	@Subscribe
	public void a(PlayerChatEvent e) {
		ITabPlayer sender = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (e.getMessage().equalsIgnoreCase("/btab")) {
			Shared.sendPluginInfo(sender);
			return;
		}
		for (CommandListener listener : Shared.commandListeners.values()) {
			if (listener.onCommand(sender, e.getMessage())) e.setCancelled(true);
		}
	}*/
	private void inject(UUID uuid) {
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
	public boolean killPacket(Team packet){
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
	//java class loader is `intelligent` and throws NoClassDefFoundError in inactive code (PacketPlayOutPlayerInfo#toVelocity)
	//making it return Object and then casting fixes it
	public static Object componentFromString(String json) {
		if (json == null) return null;
		return GsonComponentSerializer.INSTANCE.deserialize(json);
	}
	public static String componentToString(Component component) {
		if (component == null) return null;
		return GsonComponentSerializer.INSTANCE.serialize(component);
	}
	public void registerPlaceholders() {
		PluginHooks.luckPerms = server.getPluginManager().getPlugin("luckperms").isPresent();
		if (PluginHooks.luckPerms) PluginHooks.luckPermsVersion = server.getPluginManager().getPlugin("luckperms").get().getDescription().getVersion().get();
		Placeholders.registerPlaceholder(new ServerConstant("%maxplayers%") {
			public String get() {
				return server.getConfiguration().getShowMaxPlayers()+"";
			}
		});
		for (Entry<String, String> servers : server.getConfiguration().getServers().entrySet()) {
			Placeholders.registerPlaceholder(new ServerPlaceholder("%online_" + servers.getKey() + "%", 1000) {
				public String get() {
					return server.getServer(servers.getKey()).get().getPlayersConnected().size()+"";
				}
			});
		}
		Placeholders.registerUniversalPlaceholders();
	}
	
	public PacketMapping map(final int id, final ProtocolVersion version, final boolean encodeOnly) throws Exception {
		return (PacketMapping) map.invoke(null, id, version, encodeOnly);
	}
	public void registerPackets() {
		try {
			Method register = null;
			for (Method m : PacketRegistry.class.getDeclaredMethods()) {
				if (m.getName().equals("register")) register = m;
			}
			register.setAccessible(true);
			map = StateRegistry.class.getDeclaredMethod("map", int.class, ProtocolVersion.class, boolean.class);
			map.setAccessible(true);

			Supplier<ScoreboardDisplay> display = ScoreboardDisplay::new;
			register.invoke(StateRegistry.PLAY.clientbound, ScoreboardDisplay.class, display, 
					new PacketMapping[] {
							map(0x3D, ProtocolVersion.MINECRAFT_1_7_2, false),
							map(0x38, ProtocolVersion.MINECRAFT_1_9, false),
							map(0x3A, ProtocolVersion.MINECRAFT_1_12, false),
							map(0x3B, ProtocolVersion.MINECRAFT_1_12_1, false),
							map(0x3E, ProtocolVersion.MINECRAFT_1_13, false),
							map(0x42, ProtocolVersion.MINECRAFT_1_14, false),
							map(0x43, ProtocolVersion.MINECRAFT_1_15, false)
			});
			Supplier<ScoreboardObjective> objective = ScoreboardObjective::new;
			register.invoke(StateRegistry.PLAY.clientbound, ScoreboardObjective.class, objective, 
					new PacketMapping[] {
							map(0x3B, ProtocolVersion.MINECRAFT_1_7_2, false),
							map(0x3F, ProtocolVersion.MINECRAFT_1_9, false),
							map(0x41, ProtocolVersion.MINECRAFT_1_12, false),
							map(0x42, ProtocolVersion.MINECRAFT_1_12_1, false),
							map(0x45, ProtocolVersion.MINECRAFT_1_13, false),
							map(0x49, ProtocolVersion.MINECRAFT_1_14, false),
							map(0x4A, ProtocolVersion.MINECRAFT_1_15, false)
			});
			Supplier<ScoreboardScore> score = ScoreboardScore::new;
			register.invoke(StateRegistry.PLAY.clientbound, ScoreboardScore.class, score, 
					new PacketMapping[] {
							map(0x3C, ProtocolVersion.MINECRAFT_1_7_2, false),
							map(0x42, ProtocolVersion.MINECRAFT_1_9, false),
							map(0x44, ProtocolVersion.MINECRAFT_1_12, false),
							map(0x45, ProtocolVersion.MINECRAFT_1_12_1, false),
							map(0x48, ProtocolVersion.MINECRAFT_1_13, false),
							map(0x4C, ProtocolVersion.MINECRAFT_1_14, false),
							map(0x4D, ProtocolVersion.MINECRAFT_1_15, false)
			});
			Supplier<Team> team = Team::new;
			register.invoke(StateRegistry.PLAY.clientbound, Team.class, team, 
					new PacketMapping[] {
							map(0x3E, ProtocolVersion.MINECRAFT_1_7_2, false),
							map(0x41, ProtocolVersion.MINECRAFT_1_9, false),
							map(0x43, ProtocolVersion.MINECRAFT_1_12, false),
							map(0x44, ProtocolVersion.MINECRAFT_1_12_1, false),
							map(0x47, ProtocolVersion.MINECRAFT_1_13, false),
							map(0x4B, ProtocolVersion.MINECRAFT_1_14, false),
							map(0x4C, ProtocolVersion.MINECRAFT_1_15, false)
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 *  Implementing MainClass
	 */

	public void loadFeatures(boolean inject) throws Exception{
		Shared.registerFeature("placeholders", new PlaceholderManager());
		registerPlaceholders();
		if (Configs.config.getBoolean("classic-vanilla-belowname.enabled", true)) 			Shared.registerFeature("belowname", new BelowName());
		if (Configs.BossBarEnabled) 														Shared.registerFeature("bossbar", new BossBar());
		if (Configs.config.getBoolean("do-not-move-spectators", false)) 					Shared.registerFeature("spectatorfix", new SpectatorFix());
		if (Configs.config.getBoolean("global-playerlist.enabled", false)) 					Shared.registerFeature("globalplayerlist", new GlobalPlayerlist());
		if (Configs.config.getBoolean("enable-header-footer", true)) 						Shared.registerFeature("headerfooter", new HeaderFooter());
		if (Configs.config.getBoolean("change-nametag-prefix-suffix", true))				Shared.registerFeature("nametag16", new NameTag16());
		if (Configs.config.getString("yellow-number-in-tablist", "%ping%").length() > 0) 	Shared.registerFeature("tabobjective", new TabObjective());
		if (Configs.config.getBoolean("change-tablist-prefix-suffix", true)) {
			Playerlist playerlist = new Playerlist();
			Shared.registerFeature("playerlist", playerlist);
			if (Premium.alignTabsuffix) Shared.registerFeature("alignedsuffix", new AlignedSuffix(playerlist));
		}
		if (Premium.is() && Premium.premiumconfig.getBoolean("scoreboard.enabled", false)) 	Shared.registerFeature("scoreboard", new ScoreboardManager());
		if (Configs.SECRET_remove_ghost_players) 											Shared.registerFeature("ghostplayerfix", new GhostPlayerFix());
		Shared.registerFeature("group-refresh", new GroupRefresher());
		new UpdateChecker();
		
		for (Player p : server.getAllPlayers()) {
			ITabPlayer t = new TabPlayer(p, p.getCurrentServer().get().getServerInfo().getName());
			Shared.data.put(p.getUniqueId(), t);
			if (inject) inject(t.getUniqueId());
		}
	}
	public void sendConsoleMessage(String message) {
		server.getConsoleCommandSource().sendMessage(TextComponent.of(Placeholders.color(message)));
	}
	public void sendRawConsoleMessage(String message) {
		server.getConsoleCommandSource().sendMessage(TextComponent.of(message));
	}
	public String getPermissionPlugin() {
		if (PluginHooks.luckPerms) return "luckperms";
		return "Unknown/None";
	}
	public Object buildPacket(UniversalPacketPlayOut packet, me.neznamy.tab.shared.ProtocolVersion protocolVersion) {
		return packet.toVelocity(protocolVersion);
	}
	@SuppressWarnings("unchecked")
	public void loadConfig() throws Exception {
		Configs.config = new ConfigurationFile("bungeeconfig.yml", "config.yml", Arrays.asList("# Detailed explanation of all options available at https://github.com/NEZNAMY/TAB/wiki/config.yml", ""));
		Configs.serverAliases = Configs.config.getConfigurationSection("server-aliases");
	}
	public void registerUnknownPlaceholder(String identifier) {
		if (identifier.contains("_")) {
			String plugin = identifier.split("_")[0].replace("%", "").toLowerCase();
			if (plugin.equals("some")) return;
			Shared.debug("Detected used PlaceholderAPI placeholder " + identifier);
			PlaceholderManager pl = PlaceholderManager.getInstance();
			int cooldown = pl.DEFAULT_COOLDOWN;
			if (pl.playerPlaceholderRefreshIntervals.containsKey(identifier)) cooldown = pl.playerPlaceholderRefreshIntervals.get(identifier);
			if (pl.serverPlaceholderRefreshIntervals.containsKey(identifier)) cooldown = pl.serverPlaceholderRefreshIntervals.get(identifier);
			if (pl.serverConstantList.contains(identifier)) cooldown = 9999999;
			Placeholders.registerPlaceholder(new PlayerPlaceholder(identifier, cooldown){
				public String get(ITabPlayer p) {
					plm.requestPlaceholder(p, identifier);
					String name;
					if (p == null) {
						name = "null";
					} else {
						name = p.getName();
					}
					return lastValue.get(name);
				}
			}, true);
			return;
		}
	}
	public void convertConfig(ConfigurationFile config) {
		if (config.getName().equals("config.yml")) {
			if (config.hasConfigOption("belowname.refresh-interval")) {
				int value = config.getInt("belowname.refresh-interval");
				convert(config, "belowname.refresh-interval", value, "belowname.refresh-interval-milliseconds", value);
			}
			if (config.getObject("global-playerlist") instanceof Boolean) {
				rename(config, "global-playerlist", "global-playerlist.enabled");
				config.set("global-playerlist.spy-servers", Arrays.asList("spyserver1", "spyserver2"));
				Map<String, List<String>> serverGroups = new HashMap<String, List<String>>();
				serverGroups.put("lobbies", Arrays.asList("lobby1", "lobby2"));
				serverGroups.put("group2", Arrays.asList("server1", "server2"));
				config.set("global-playerlist.server-groups", serverGroups);
				config.set("global-playerlist.display-others-as-spectators", false);
				Shared.print('2', "Converted old global-playerlist section to new one in config.yml.");
			}
			rename(config, "tablist-objective-value", "yellow-number-in-tablist");
			rename(config, "belowname", "classic-vanilla-belowname");
			removeOld(config, "nametag-refresh-interval-milliseconds");
			removeOld(config, "tablist-refresh-interval-milliseconds");
			removeOld(config, "header-footer-refresh-interval-milliseconds");
			removeOld(config, "classic-vanilla-belowname.refresh-interval-milliseconds");
		}
		if (config.getName().equals("premiumconfig.yml")) {
			removeOld(config, "scoreboard.refresh-interval-ticks");
			if (!config.hasConfigOption("placeholder-output-replacements")) {
				Map<String, Map<String, String>> replacements = new HashMap<String, Map<String, String>>();
				Map<String, String> essVanished = new HashMap<String, String>();
				essVanished.put("Yes", "&7| Vanished");
				essVanished.put("No", "");
				replacements.put("%essentials_vanished%", essVanished);
				Map<String, String> tps = new HashMap<String, String>();
				tps.put("20", "&aPerfect");
				replacements.put("%tps%", tps);
				config.set("placeholder-output-replacements", replacements);
				Shared.print('2', "Added new missing \"placeholder-output-replacements\" premiumconfig.yml section.");
			}
			boolean scoreboardsConverted = false;
			for (Object scoreboard : config.getConfigurationSection("scoreboards").keySet()) {
				Boolean permReq = config.getBoolean("scoreboards." + scoreboard + ".permission-required");
				if (permReq != null) {
					if (permReq) {
						config.set("scoreboards." + scoreboard + ".display-condition", "permission:tab.scoreboard." + scoreboard);
					}
					config.set("scoreboards." + scoreboard + ".permission-required", null);
					scoreboardsConverted = true;
				}
				String childBoard = config.getString("scoreboards." + scoreboard + ".if-permission-missing");
				if (childBoard != null) {
					config.set("scoreboards." + scoreboard + ".if-permission-missing", null);
					config.set("scoreboards." + scoreboard + ".if-condition-not-met", childBoard);
					scoreboardsConverted = true;
				}
			}
			if (scoreboardsConverted) {
				Shared.print('2', "Converted old premiumconfig.yml scoreboard display condition system to new one.");
			}
			removeOld(config, "scoreboard.refresh-interval-milliseconds");
		}
		if (config.getName().equals("bossbar.yml")) {
			removeOld(config, "refresh-interval-milliseconds");
		}
	}
	@Override
	public String getServerVersion() {
		return server.getVersion().getName() + " v" + server.getVersion().getVersion();
	}
	@Override
	public void suggestPlaceholders() {
		//bungee only
		suggestPlaceholderSwitch("%premiumvanish_bungeeplayercount%", "%canseeonline%");
		suggestPlaceholderSwitch("%bungee_total%", "%online%");
		for (RegisteredServer server : server.getAllServers()) {
			suggestPlaceholderSwitch("%bungee_" + server.getServerInfo().getName() + "%", "%online_" + server.getServerInfo().getName() + "%");
		}

		//both
		suggestPlaceholderSwitch("%cmi_user_ping%", "%ping%");
		suggestPlaceholderSwitch("%player_ping%", "%ping%");
		suggestPlaceholderSwitch("%viaversion_player_protocol_version%", "%player-version%");
		suggestPlaceholderSwitch("%player_name%", "%nick%");
		suggestPlaceholderSwitch("%uperms_rank%", "%rank%");
	}
}