package me.neznamy.tab.platforms.velocity;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.Supplier;

import org.slf4j.Logger;

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
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.StateRegistry;
import com.velocitypowered.proxy.protocol.StateRegistry.PacketMapping;
import com.velocitypowered.proxy.protocol.StateRegistry.PacketRegistry;

import io.netty.channel.*;
import me.neznamy.tab.platforms.velocity.protocol.*;
import me.neznamy.tab.premium.AlignedSuffix;
import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.premium.ScoreboardManager;
import me.neznamy.tab.shared.*;
import me.neznamy.tab.shared.command.TabCommand;
import me.neznamy.tab.shared.features.*;
import me.neznamy.tab.shared.packets.*;
import me.neznamy.tab.shared.placeholders.*;
import net.kyori.text.Component;
import net.kyori.text.TextComponent;
import net.kyori.text.serializer.gson.GsonComponentSerializer;

@Plugin(id = "tab", name = "TAB", version = "2.7.2", description = "Change a player's tablist prefix/suffix, name tag prefix/suffix, header/footer, bossbar and more", authors = {"NEZNAMY"})
public class Main implements MainClass{

	public static ProxyServer server;
	public static Logger logger;
	private PluginMessenger plm;

	@Inject
	public Main(ProxyServer server, Logger logger) {
		Main.server = server;
		Main.logger = logger;
	}
	@Subscribe
	public void onProxyInitialization(ProxyInitializeEvent event) {
		try {
			Class.forName("org.yaml.snakeyaml.Yaml");
			me.neznamy.tab.shared.ProtocolVersion.SERVER_VERSION = me.neznamy.tab.shared.ProtocolVersion.v1_15_2;
			Shared.mainClass = this;
			Shared.separatorType = "server";
			TabCommand command = new TabCommand();
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
								sender.sendMessage(TextComponent.of(Placeholders.color(" &6&lBukkit bridge mode activated")));
								sender.sendMessage(TextComponent.of(Placeholders.color(" &8>> &3&l/tab reload")));
								sender.sendMessage(TextComponent.of(Placeholders.color("      - &7Reloads plugin and config")));
								sender.sendMessage(TextComponent.of(Placeholders.color("&m                                                                                ")));
							}
						}
					} else {
						command.execute(sender instanceof Player ? Shared.getPlayer(((Player)sender).getUniqueId()) : null, args);
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
			for (ITabPlayer p : Shared.getPlayers()) ((Channel) p.getChannel()).pipeline().remove(Shared.DECODER_NAME);
			Shared.unload();
		}
	}
	@Subscribe
	public void a(DisconnectEvent e){
		if (Shared.disabled) return;
		ITabPlayer disconnectedPlayer = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (disconnectedPlayer == null) return; //player connected to bungeecord successfully, but not to the bukkit server anymore ? idk the check is needed
		disconnectedPlayer.disconnect();
		Shared.data.remove(e.getPlayer().getUniqueId());
		Shared.features.values().forEach(f -> f.onQuit(disconnectedPlayer));
		for (Placeholder pl : Placeholders.usedPlaceholders.values()) {
			if (pl instanceof PlayerPlaceholder) {
				((PlayerPlaceholder)pl).lastRefresh.remove(disconnectedPlayer.getName());
				((PlayerPlaceholder)pl).lastValue.remove(disconnectedPlayer.getName());
			}
		}
	}
	@Subscribe
	public void a(ServerConnectedEvent e){
		try{
			if (Shared.disabled) return;
			ITabPlayer p;
			if (!Shared.data.containsKey(e.getPlayer().getUniqueId())) {
				//join
				p = new TabPlayer(e.getPlayer(), e.getServer().getServerInfo().getName());
				Shared.data.put(e.getPlayer().getUniqueId(), p);
				inject(p.getUniqueId());
				//sending custom packets with a delay, it would not work otherwise
				Shared.cpu.runTaskLater(50, "processing join", "onJoin handle", new Runnable() {

					@Override
					public void run() {
						Shared.features.values().forEach(f -> f.onJoin(p));
					}
				});
			} else {
				//server change
				p = Shared.getPlayer(e.getPlayer().getUniqueId());
				String from = p.getWorldName();
				String to = p.world = e.getServer().getServerInfo().getName();
				p.onWorldChange(from, to);
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
		if (Shared.features.containsKey("bossbar")) {
			if (((BossBar)Shared.features.get("bossbar")).onChat(sender, e.getMessage())) e.setCancelled(true);
		}
		if (Shared.features.containsKey("scoreboard")) {
			if (((ScoreboardManager)Shared.features.get("scoreboard")).onCommand(sender, e.getMessage())) e.setCancelled(true);
		}
	}*/
	private void inject(UUID uuid) {
		Channel channel = (Channel) Shared.getPlayer(uuid).getChannel();
		if (channel.pipeline().names().contains(Shared.DECODER_NAME)) channel.pipeline().remove(Shared.DECODER_NAME);
		channel.pipeline().addBefore("handler", Shared.DECODER_NAME, new ChannelDuplexHandler() {

			public void channelRead(ChannelHandlerContext context, Object packet) throws Exception {
				super.channelRead(context, packet);
			}
			public void write(ChannelHandlerContext context, Object packet, ChannelPromise channelPromise) throws Exception {
				ITabPlayer player = Shared.getPlayer(uuid);
				if (player == null || player.getVersion() == me.neznamy.tab.shared.ProtocolVersion.UNKNOWN) {
					super.write(context, packet, channelPromise);
					return;
				}
				try{
					if (packet instanceof MinecraftPacket) {
						UniversalPacketPlayOut customPacket = null;
						customPacket = PacketPlayOutPlayerInfo.fromVelocity(packet);
						if (customPacket != null) {
							for (CustomPacketFeature f : Shared.custompacketfeatures.values()) {
								long time = System.nanoTime();
								if (customPacket != null) customPacket = f.onPacketSend(player, customPacket);
								Shared.cpu.addFeatureTime(f.getCPUName(), System.nanoTime()-time);
							}
/*							PacketPlayOutPlayerInfo info = (PacketPlayOutPlayerInfo) customPacket;
							if (info.action == EnumPlayerInfoAction.ADD_PLAYER || info.action == EnumPlayerInfoAction.REMOVE_PLAYER) {
								System.out.println("[" + player.getName() + "] " + info.toString());
							}*/
							if (customPacket != null) packet = customPacket.toVelocity(player.getVersion());
							else packet = null;
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
	public static void registerPlaceholders() {
		PluginHooks.luckPerms = server.getPluginManager().getPlugin("luckperms").isPresent();
		
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
	private static Method map;
	
	public static PacketMapping map(final int id, final ProtocolVersion version, final boolean encodeOnly) throws Exception {
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
							map(0x3D, ProtocolVersion.MINECRAFT_1_8, false),
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
							map(0x3B, ProtocolVersion.MINECRAFT_1_8, false),
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
							map(0x3C, ProtocolVersion.MINECRAFT_1_8, false),
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
							map(0x3E, ProtocolVersion.MINECRAFT_1_8, false),
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
		registerPlaceholders();
		if (Configs.config.getBoolean("belowname.enabled", true)) 							Shared.registerFeature("belowname", new BelowName());
		if (Configs.BossBarEnabled) 														Shared.registerFeature("bossbar", new BossBar());
		if (Configs.config.getBoolean("do-not-move-spectators", false)) 					Shared.registerFeature("spectatorfix", new SpectatorFix());
		if (Configs.config.getBoolean("global-playerlist.enabled", false)) 					Shared.registerFeature("globalplayerlist", new GlobalPlayerlist());
		if (Configs.config.getBoolean("enable-header-footer", true)) 						Shared.registerFeature("headerfooter", new HeaderFooter());
		if (Configs.config.getBoolean("change-nametag-prefix-suffix", true))				Shared.registerFeature("nametag16", new NameTag16());
		if (Configs.config.getString("yellow-number-in-tablist", "%ping%").length() > 0) 	Shared.registerFeature("tabobjective", new TabObjective());
		if (Configs.config.getBoolean("change-tablist-prefix-suffix", true)) {
			Shared.registerFeature("playerlist", new Playerlist());
			if (Premium.allignTabsuffix) Shared.registerFeature("alignedsuffix", new AlignedSuffix());
		}
		if (Premium.is() && Premium.premiumconfig.getBoolean("scoreboard.enabled", false)) 	Shared.registerFeature("scoreboard", new ScoreboardManager());
		if (Configs.SECRET_remove_ghost_players) 											Shared.registerFeature("ghostplayerfix", new GhostPlayerFix());
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
			Placeholders.registerPlaceholder(new PlayerPlaceholder(identifier, 49){
				public String get(ITabPlayer p) {
					plm.requestPlaceholder(p, identifier);
					return lastValue.get(p.getName());
				}
			});
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
		}
		if (config.getName().equals("premiumconfig.yml")) {
			ticks2Millis(config, "scoreboard.refresh-interval-ticks", "scoreboard.refresh-interval-milliseconds");
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
		}
	}
	@Override
	public String getServerVersion() {
		return server.getVersion().getName() + " v" + server.getVersion().getVersion();
	}
}