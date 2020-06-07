package me.neznamy.tab.platforms.bungee;

import java.util.*;
import java.util.Map.Entry;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import me.neznamy.tab.premium.AlignedSuffix;
import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.premium.ScoreboardManager;
import me.neznamy.tab.shared.*;
import me.neznamy.tab.shared.command.TabCommand;
import me.neznamy.tab.shared.features.*;
import me.neznamy.tab.shared.features.BossBar;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.UniversalPacketPlayOut;
import me.neznamy.tab.shared.placeholders.*;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.*;
import net.md_5.bungee.event.*;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.packet.*;

public class Main extends Plugin implements Listener, MainClass{

	private PluginMessenger plm;
	
	public void onEnable(){
		ProtocolVersion.SERVER_VERSION = ProtocolVersion.v1_15_2;
		Shared.mainClass = this;
		Shared.separatorType = "server";
		getProxy().getPluginManager().registerListener(this, this);
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
		plm = new PluginMessenger(this);
		Shared.load(true);
		Metrics.start(this);
	}
	public void onDisable() {
		if (!Shared.disabled) {
			for (ITabPlayer p : Shared.getPlayers()) ((Channel) p.getChannel()).pipeline().remove(Shared.DECODER_NAME);
			Shared.unload();
		}
	}
	@EventHandler
	public void a(TabCompleteEvent e) {
		if (Shared.disabled) return;
		if (e.getCursor().startsWith("/btab ")) {
			String arg = e.getCursor();
			while (arg.contains("  ")) arg = arg.replace("  ", " ");
			String[] args = arg.split(" ");
			args = Arrays.copyOfRange(args, 1, args.length);
			if (arg.endsWith(" ")) {
				List<String> list = Lists.newArrayList(args);
				list.add("");
				args = list.toArray(new String[0]);
			}
			e.getSuggestions().clear();
			e.getSuggestions().addAll(Shared.command.complete(Shared.getPlayer(((ProxiedPlayer)e.getSender()).getUniqueId()), args));
		}
	}
	@EventHandler(priority = EventPriority.LOWEST)
	public void a(PlayerDisconnectEvent e){
		if (Shared.disabled) return;
		ITabPlayer disconnectedPlayer = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (disconnectedPlayer == null) return; //player connected to bungeecord successfully, but not to the bukkit server anymore ? idk the check is needed
		disconnectedPlayer.disconnect();
		Shared.data.remove(e.getPlayer().getUniqueId());
		Shared.features.values().forEach(f -> f.onQuit(disconnectedPlayer));
		for (Placeholder pl : Placeholders.getAllPlaceholders()) {
			if (pl instanceof PlayerPlaceholder) {
				((PlayerPlaceholder)pl).lastRefresh.remove(disconnectedPlayer.getName());
				((PlayerPlaceholder)pl).lastValue.remove(disconnectedPlayer.getName());
			}
		}
	}
	@EventHandler(priority = EventPriority.LOW)
	public void a(ServerSwitchEvent e){
		try{
			if (Shared.disabled) return;
			ITabPlayer p;
			if (!Shared.data.containsKey(e.getPlayer().getUniqueId())) {
				p = new TabPlayer(e.getPlayer());
				Shared.data.put(e.getPlayer().getUniqueId(), p);
				inject(p.getUniqueId());
				Shared.features.values().forEach(f -> f.onJoin(p));
			} else {
				p = Shared.getPlayer(e.getPlayer().getUniqueId());
				String from = p.getWorldName();
				String to = p.world = e.getPlayer().getServer().getInfo().getName();
				p.onWorldChange(from, to);
			}
		} catch (Throwable ex){
			Shared.errorManager.criticalError("An error occurred when player joined/changed server", ex);
		}
	}
	@EventHandler
	public void a(ChatEvent e) {
		ITabPlayer sender = Shared.getPlayer(((ProxiedPlayer)e.getSender()).getUniqueId());
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
	}
	private void inject(UUID uuid) {
		Channel channel = (Channel) Shared.getPlayer(uuid).getChannel();
		if (channel.pipeline().names().contains(Shared.DECODER_NAME)) channel.pipeline().remove(Shared.DECODER_NAME);
		channel.pipeline().addBefore("inbound-boss", Shared.DECODER_NAME, new ChannelDuplexHandler() {

			public void channelRead(ChannelHandlerContext context, Object packet) throws Exception {
				super.channelRead(context, packet);
			}
			public void write(ChannelHandlerContext context, Object packet, ChannelPromise channelPromise) throws Exception {
				ITabPlayer player = Shared.getPlayer(uuid);
				if (player == null || player.getVersion() == ProtocolVersion.UNKNOWN) {
					super.write(context, packet, channelPromise);
					return;
				}
				try{
					if (packet instanceof DefinedPacket) {
						UniversalPacketPlayOut customPacket = null;
						customPacket = PacketPlayOutPlayerInfo.fromBungee(packet, player.getVersion());
						if (customPacket != null) {
							for (CustomPacketFeature f : Shared.custompacketfeatures.values()) {
								long time = System.nanoTime();
								if (customPacket != null) customPacket = f.onPacketSend(player, customPacket);
								Shared.featureCpu.addTime(f.getCPUName(), System.nanoTime()-time);
							}
							if (customPacket != null) packet = customPacket.toBungee(player.getVersion());
							else packet = null;
						}
					}
					if (packet instanceof Team && Shared.features.containsKey("nametag16")) {
						Team team = (Team) packet;
						if (killPacket(team)) {
							return;
						}
					}
					if (packet instanceof ByteBuf && Shared.features.containsKey("nametag16")) {
						ByteBuf buf = ((ByteBuf) packet).duplicate();
						byte packetId = buf.readByte();
						Team team = null;
						if (packetId == ((TabPlayer)player).getPacketId(Team.class)) {
							team = new Team();
							team.read(buf, null, player.getVersion().getNetworkId());
						}
						if (team != null) {
							if (killPacket(team)) {
								return;
							}
						}
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
	public static void registerPlaceholders() {
		PluginHooks.premiumVanish = ProxyServer.getInstance().getPluginManager().getPlugin("PremiumVanish") != null;
		PluginHooks.luckPerms = ProxyServer.getInstance().getPluginManager().getPlugin("LuckPerms") != null;
		if (PluginHooks.luckPerms) PluginHooks.luckPermsVersion = ProxyServer.getInstance().getPluginManager().getPlugin("LuckPerms").getDescription().getVersion();
		PluginHooks.ultrapermissions = ProxyServer.getInstance().getPluginManager().getPlugin("UltraPermissions") != null;
		if (PluginHooks.premiumVanish) {
			Placeholders.registerPlaceholder(new ServerPlaceholder("%canseeonline%", 1000) {
				public String get() {
					return PluginHooks.PremiumVanish_getVisiblePlayerCount()+"";
				}
			});
			Placeholders.registerPlaceholder(new ServerPlaceholder("%canseestaffonline%", 1000) {
				public String get() {
					int count = 0;
					for (ITabPlayer all : Shared.getPlayers()) {
						if (!PluginHooks._isVanished(all) && all.isStaff()) count++;
					}
					return count+"";
				}
			});
		}
		Placeholders.registerPlaceholder(new ServerConstant("%maxplayers%") {
			public String get() {
				return ProxyServer.getInstance().getConfigurationAdapter().getListeners().iterator().next().getMaxPlayers()+"";
			}
		});
		for (Entry<String, ServerInfo> server : ProxyServer.getInstance().getServers().entrySet()) {
			Placeholders.registerPlaceholder(new ServerPlaceholder("%online_" + server.getKey() + "%", 1000) {
				public String get() {
					return server.getValue().getPlayers().size()+"";
				}
			});
			Placeholders.registerPlaceholder(new ServerPlaceholder("%canseeonline_" + server.getKey() + "%", 1000) {
				public String get() {
					int count = server.getValue().getPlayers().size();
					for (ProxiedPlayer p : server.getValue().getPlayers()) {
						if (PluginHooks._isVanished(Shared.getPlayer(p.getUniqueId()))) count--;
					}
					return count+"";
				}
			});
		}
		Placeholders.registerUniversalPlaceholders();
	}


	/*
	 *  Implementing MainClass
	 */

	public void loadFeatures(boolean inject) throws Exception{
		registerPlaceholders();
		if (Configs.config.getBoolean("classic-vanilla-belowname.enabled", true)) 			Shared.registerFeature("belowname", new BelowName());
		if (Configs.BossBarEnabled) 														Shared.registerFeature("bossbar", new BossBar());
		if (Configs.config.getBoolean("do-not-move-spectators", false)) 					Shared.registerFeature("spectatorfix", new SpectatorFix());
		if (Configs.config.getBoolean("global-playerlist.enabled", false)) 					Shared.registerFeature("globalplayerlist", new GlobalPlayerlist());
		if (Configs.config.getBoolean("enable-header-footer", true)) 						Shared.registerFeature("headerfooter", new HeaderFooter());
		if (Configs.config.getBoolean("change-nametag-prefix-suffix", true)) 				Shared.registerFeature("nametag16", new NameTag16());
		if (Configs.config.getString("yellow-number-in-tablist", "%ping%").length() > 0)	Shared.registerFeature("tabobjective", new TabObjective());
		if (Configs.config.getBoolean("change-tablist-prefix-suffix", true)) {
			Shared.registerFeature("playerlist", new Playerlist());
			if (Premium.allignTabsuffix) Shared.registerFeature("alignedsuffix", new AlignedSuffix());
		}
		if (Premium.is() && Premium.premiumconfig.getBoolean("scoreboard.enabled", false)) 	Shared.registerFeature("scoreboard", new ScoreboardManager());
		if (Configs.SECRET_remove_ghost_players) 											Shared.registerFeature("ghostplayerfix", new GhostPlayerFix());
		new UpdateChecker();
		
		for (ProxiedPlayer p : getProxy().getPlayers()) {
			ITabPlayer t = new TabPlayer(p);
			Shared.data.put(p.getUniqueId(), t);
			if (inject) inject(t.getUniqueId());
		}
	}
	@SuppressWarnings("deprecation")
	public void sendConsoleMessage(String message) {
		ProxyServer.getInstance().getConsole().sendMessage(Placeholders.color(message));
	}
	@SuppressWarnings("deprecation")
	public void sendRawConsoleMessage(String message) {
		ProxyServer.getInstance().getConsole().sendMessage(message);
	}
	public String getPermissionPlugin() {
		if (PluginHooks.luckPerms) return "LuckPerms";
		if (PluginHooks.ultrapermissions) return "UltraPermissions";
		if (ProxyServer.getInstance().getPluginManager().getPlugin("BungeePerms") != null) return "BungeePerms";
		return "Unknown/None";
	}
	public Object buildPacket(UniversalPacketPlayOut packet, ProtocolVersion protocolVersion) {
		return packet.toBungee(protocolVersion);
	}
	@SuppressWarnings("unchecked")
	public void loadConfig() throws Exception {
		Configs.config = new ConfigurationFile("bungeeconfig.yml", "config.yml", Arrays.asList("# Detailed explanation of all options available at https://github.com/NEZNAMY/TAB/wiki/config.yml", ""));
		Configs.serverAliases = Configs.config.getConfigurationSection("server-aliases");
	}
	public void registerUnknownPlaceholder(String identifier) {
		if (identifier.contains("_")) {
			Shared.debug("Detected used PlaceholderAPI placeholder " + identifier);
			Placeholders.registerPlaceholder(new PlayerPlaceholder(identifier, 49){
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
			rename(config, "belowname", "classic-vanilla-belowname");
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
		return getProxy().getVersion();
	}
	@Override
	public void suggestPlaceholders() {
		//bungee only
		suggestPlaceholderSwitch("%premiumvanish_bungeeplayercount%", "%canseeonline%");
		suggestPlaceholderSwitch("%bungee_total%", "%online%");
		for (String server : ProxyServer.getInstance().getServers().keySet()) {
			suggestPlaceholderSwitch("%bungee_" + server + "%", "%online_" + server + "%");
		}

		//both
		suggestPlaceholderSwitch("%player_ping%", "%ping%");
		suggestPlaceholderSwitch("%viaversion_player_protocol_version%", "%player-version%");
		suggestPlaceholderSwitch("%player_name%", "%nick%");
	}
}