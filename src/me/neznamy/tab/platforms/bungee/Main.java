package me.neznamy.tab.platforms.bungee;

import java.util.*;
import java.util.Map.Entry;

import java.util.UUID;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import me.neznamy.tab.api.TABAPI;
import me.neznamy.tab.premium.AlignedSuffix;
import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.premium.ScoreboardManager;
import me.neznamy.tab.shared.*;
import me.neznamy.tab.shared.command.TabCommand;
import me.neznamy.tab.shared.features.*;
import me.neznamy.tab.shared.features.BossBar;
import me.neznamy.tab.shared.features.TabObjective.TabObjectiveType;
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
	private TabObjectiveType objType;
	private TabCommand command;
	
	public void onEnable(){
		ProtocolVersion.SERVER_VERSION = ProtocolVersion.v1_15_2;
		Shared.mainClass = this;
		Shared.separatorType = "server";
		getProxy().getPluginManager().registerListener(this, this);
		if (getProxy().getPluginManager().getPlugin("PremiumVanish") != null) getProxy().getPluginManager().registerListener(this, new PremiumVanishListener());
		command = new TabCommand();
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
							sender.sendMessage(Placeholders.color(" &6&lBukkit bridge mode activated"));
							sender.sendMessage(Placeholders.color(" &8>> &3&l/tab reload"));
							sender.sendMessage(Placeholders.color("      - &7Reloads plugin and config"));
							sender.sendMessage(Placeholders.color("&m                                                                                "));
						}
					}
				} else {
					command.execute(sender instanceof ProxiedPlayer ? Shared.getPlayer(((ProxiedPlayer)sender).getUniqueId()) : null, args);
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
			e.getSuggestions().addAll(command.complete(Shared.getPlayer(((ProxiedPlayer)e.getSender()).getUniqueId()), args));
		}
	}
	@EventHandler(priority = EventPriority.LOWEST)
	public void a(PlayerDisconnectEvent e){
		if (Shared.disabled) return;
		ITabPlayer disconnectedPlayer = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (disconnectedPlayer == null) return; //player connected to bungeecord successfully, but not to the bukkit server anymore ? idk the check is needed
		Shared.data.remove(e.getPlayer().getUniqueId());
		Shared.features.values().forEach(f -> f.onQuit(disconnectedPlayer));
		for (PlayerPlaceholder pl : Placeholders.usedPlayerPlaceholders.values()) {
			pl.lastRefresh.remove(disconnectedPlayer.getName());
			pl.lastValue.remove(disconnectedPlayer.getName());
		}
	}
	@EventHandler
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
				try{
					ITabPlayer player = Shared.getPlayer(uuid);
					if (player == null || player.getVersion() == ProtocolVersion.UNKNOWN) {
						super.write(context, packet, channelPromise);
						return;
					}
					if (packet instanceof DefinedPacket) {
						UniversalPacketPlayOut customPacket = null;
						customPacket = PacketPlayOutPlayerInfo.fromBungee(packet, player.getVersion());
						if (customPacket != null) {
							for (CustomPacketFeature f : Shared.custompacketfeatures.values()) {
								long time = System.nanoTime();
								if (customPacket != null) customPacket = f.onPacketSend(player, customPacket);
								Shared.cpu.addFeatureTime(f.getCPUName(), System.nanoTime()-time);
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
						if (packetId == player.getVersion().getPacketPlayOutScoreboardTeamId()) {
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
					Shared.errorManager.printError("An error occurred when analyzing packets", e);
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
		PluginHooks.ultrapermissions = ProxyServer.getInstance().getPluginManager().getPlugin("UltraPermissions") != null;
		if (PluginHooks.premiumVanish) {
			TABAPI.registerServerPlaceholder(new ServerPlaceholder("%canseeonline%", 1000) {
				public String get() {
					return PluginHooks.PremiumVanish_getVisiblePlayerCount()+"";
				}
			});
		}
		TABAPI.registerServerConstant(new ServerConstant("%maxplayers%") {
			public String get() {
				return ProxyServer.getInstance().getConfigurationAdapter().getListeners().iterator().next().getMaxPlayers()+"";
			}
		});
		for (Entry<String, ServerInfo> server : ProxyServer.getInstance().getServers().entrySet()) {
			TABAPI.registerServerPlaceholder(new ServerPlaceholder("%online_" + server.getKey() + "%", 1000) {
				public String get() {
					return server.getValue().getPlayers().size()+"";
				}
			});
			TABAPI.registerServerPlaceholder(new ServerPlaceholder("%canseeonline_" + server.getKey() + "%", 1000) {
				public String get() {
					int count = server.getValue().getPlayers().size();
					for (ProxiedPlayer p : server.getValue().getPlayers()) {
						if (PluginHooks._isVanished(Shared.getPlayer(p.getUniqueId()))) count--;
					}
					return count+"";
				}
			});
		}
		Shared.registerUniversalPlaceholders();
	}


	/*
	 *  Implementing MainClass
	 */

	public void loadFeatures(boolean inject) throws Exception{
		registerPlaceholders();
		if (Configs.config.getBoolean("belowname.enabled", true)) 							Shared.registerFeature("belowname", new BelowName());
		if (Configs.BossBarEnabled) 														Shared.registerFeature("bossbar", new BossBar());
		if (Configs.config.getBoolean("global-playerlist", false)) 							Shared.registerFeature("globalplayerlist", new GlobalPlayerlist());
		if (Configs.config.getBoolean("enable-header-footer", true)) 						Shared.registerFeature("headerfooter", new HeaderFooter());
		if (Configs.config.getBoolean("change-nametag-prefix-suffix", true)) 				Shared.registerFeature("nametag16", new NameTag16());
		if (objType != TabObjectiveType.NONE) 												Shared.registerFeature("tabobjective", new TabObjective(objType));
		if (Configs.config.getBoolean("change-tablist-prefix-suffix", true)) {
			Shared.registerFeature("playerlist", new Playerlist());
			if (Premium.allignTabsuffix) Shared.registerFeature("alignedsuffix", new AlignedSuffix());
		}
		if (Configs.config.getBoolean("do-not-move-spectators", false)) 					Shared.registerFeature("spectatorfix", new SpectatorFix());
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
	public String getPermissionPlugin() {
		if (PluginHooks.luckPerms) return "LuckPerms";
		if (PluginHooks.ultrapermissions) return "UltraPermissions";
		if (ProxyServer.getInstance().getPluginManager().getPlugin("BungeePerms") != null) return "BungeePerms";
		return "Unknown/None";
	}
	public Object buildPacket(UniversalPacketPlayOut packet, ProtocolVersion protocolVersion) {
		return packet.toBungee(protocolVersion);
	}
	public void loadConfig() throws Exception {
		Configs.config = new ConfigurationFile("bungeeconfig.yml", "config.yml", Arrays.asList("# Detailed explanation of all options available at https://github.com/NEZNAMY/TAB/wiki/config.yml", ""));
		TabObjective.rawValue = Configs.config.getString("tablist-objective-value", "%ping%");
		objType = (TabObjective.rawValue.length() == 0) ? TabObjectiveType.NONE : TabObjectiveType.CUSTOM;
		Configs.serverAliases = Configs.config.getConfigurationSection("server-aliases");
		if (Configs.serverAliases == null) Configs.serverAliases = new HashMap<String, Object>();
	}
	public void registerUnknownPlaceholder(String identifier) {
		if (identifier.contains("_")) {
			TABAPI.registerPlayerPlaceholder(new PlayerPlaceholder(identifier, 49){
				public String get(ITabPlayer p) {
					plm.requestPlaceholder(p, identifier);
					return lastValue.get(p.getName());
				}
			});
			return;
		}
	}
	@SuppressWarnings("unchecked")
	public void convertConfig(ConfigurationFile config) {
		if (config.getName().equals("config.yml")) {
			if (config.get("belowname.refresh-interval") != null) {
				int value = (int) config.get("belowname.refresh-interval");
				convert(config, "belowname.refresh-interval", value, "belowname.refresh-interval-milliseconds", value);
			}
		}
		if (config.getName().equals("premiumconfig.yml")) {
			ticks2Millis(config, "scoreboard.refresh-interval-ticks", "scoreboard.refresh-interval-milliseconds");
			if (config.get("placeholder-output-replacements") == null) {
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
			for (String scoreboard : ((Map<String, Object>)config.get("scoreboards")).keySet()) {
				Boolean permReq = (Boolean) config.get("scoreboards." + scoreboard + ".permission-required");
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
}