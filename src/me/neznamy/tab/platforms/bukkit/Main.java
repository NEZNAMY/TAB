package me.neznamy.tab.platforms.bukkit;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import de.robingrether.idisguise.api.DisguiseAPI;
import me.neznamy.tab.api.TABAPI;
import me.neznamy.tab.platforms.bukkit.features.*;
import me.neznamy.tab.platforms.bukkit.features.unlimitedtags.NameTagX;
import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.premium.AlignedSuffix;
import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.premium.ScoreboardManager;
import me.neznamy.tab.shared.*;
import me.neznamy.tab.shared.command.TabCommand;
import me.neznamy.tab.shared.features.*;
import me.neznamy.tab.shared.features.TabObjective.TabObjectiveType;
import me.neznamy.tab.shared.placeholders.*;
import me.neznamy.tab.shared.packets.*;
import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin implements Listener, MainClass{

	public static Main instance;
	@SuppressWarnings("unused")
	private PluginMessenger plm;
	public static List<String> usedExpansions;
	private TabObjectiveType objType;
	public static final String serverPackage = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

	public void onEnable(){
		ProtocolVersion.SERVER_VERSION = ProtocolVersion.fromServerString(Bukkit.getBukkitVersion().split("-")[0]);
		Shared.mainClass = this;
		Shared.separatorType = "world";
		Shared.print('7', "Server version: " + Bukkit.getBukkitVersion().split("-")[0] + " (" + serverPackage + ")");
		if (MethodAPI.getInstance() != null){
			instance = this;
			Bukkit.getPluginManager().registerEvents(this, this);
			TabCommand command = new TabCommand();
			Bukkit.getPluginCommand("tab").setExecutor(new CommandExecutor() {
				public boolean onCommand(CommandSender sender, Command c, String cmd, String[] args){
					if (Configs.bukkitBridgeMode || Shared.disabled) {
						if (args.length == 1 && args[0].toLowerCase().equals("reload")) {
							if (sender.hasPermission("tab.reload")) {
								Shared.unload();
								Shared.load(false);
								if (Shared.disabled) {
									if (sender instanceof Player) {
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
						command.execute(sender instanceof Player ? Shared.getPlayer(((Player)sender).getUniqueId()) : null, args);
					}
					return false;
				}
			});
			Bukkit.getPluginCommand("tab").setTabCompleter(new TabCompleter() {
				public List<String> onTabComplete(CommandSender sender, Command c, String cmd, String[] args) {
					if (Configs.bukkitBridgeMode) {
						return null;
					}
					return command.complete(sender instanceof Player ? Shared.getPlayer(((Player)sender).getUniqueId()) : null, args);
				}
			});
			Shared.load(true);
			Metrics.start(this);
		} else {
			Shared.disabled = true;
			sendConsoleMessage("&c[TAB] Your server version is not supported. Disabling..");
			Bukkit.getPluginManager().disablePlugin(this);
		}
	}
	public void onDisable() {
		if (!Shared.disabled) {
			for (ITabPlayer p : Shared.getPlayers()) {
				if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
					Injector.uninject(p.getUniqueId());
				} else if (ProtocolVersion.SERVER_VERSION.getMinorVersion() == 7) {
					Injector1_7.uninject(p.getUniqueId());
				}
			}
			Shared.unload();
			if (Configs.bukkitBridgeMode) Bukkit.getMessenger().unregisterIncomingPluginChannel(this);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void a(PlayerJoinEvent e) {
		try {
			if (Shared.disabled) return;
			if (Configs.bukkitBridgeMode) return;
			ITabPlayer p = new TabPlayer(e.getPlayer());
			Shared.data.put(e.getPlayer().getUniqueId(), p);
			Shared.entityIdMap.put(e.getPlayer().getEntityId(), p);
			inject(e.getPlayer().getUniqueId());
			Shared.cpu.runMeasuredTask("player joined the server", "onJoin handling", new Runnable() {

				public void run() {
					PluginHooks.DeluxeTags_onChat(p);
					Shared.features.values().forEach(f -> f.onJoin(p));
				}
			});
		} catch (Throwable ex) {
			Shared.errorManager.criticalError("An error occurred when processing PlayerJoinEvent", ex);
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public void a(PlayerQuitEvent e){
		try {
			if (Shared.disabled) return;
			if (Configs.bukkitBridgeMode) return;
			ITabPlayer disconnectedPlayer = Shared.getPlayer(e.getPlayer().getUniqueId());
			if (disconnectedPlayer == null) return;
			Shared.features.values().forEach(f -> f.onQuit(disconnectedPlayer));
			for (PlayerPlaceholder pl : Placeholders.usedPlayerPlaceholders.values()) {
				pl.lastRefresh.remove(e.getPlayer().getName());
				pl.lastValue.remove(e.getPlayer().getName());
			}
		} catch (Throwable t) {
			Shared.errorManager.printError("An error occurred when processing PlayerQuitEvent", t);
		}
		Shared.data.remove(e.getPlayer().getUniqueId());
		Shared.entityIdMap.remove(e.getPlayer().getEntityId());
	}
	@EventHandler(priority = EventPriority.LOWEST)
	public void a(PlayerChangedWorldEvent e){
		try {
			if (Shared.disabled) return;
			if (Configs.bukkitBridgeMode) return;
			ITabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
			if (p == null) return;
			String from = e.getFrom().getName();
			String to = p.world = e.getPlayer().getWorld().getName();
			p.onWorldChange(from, to);
		} catch (Throwable t) {
			Shared.errorManager.printError("An error occurred when processing PlayerChangedWorldEvent", t);
		}
	}
	@EventHandler
	public void a(PlayerCommandPreprocessEvent e) {
		if (Shared.disabled) return;
		if (Configs.bukkitBridgeMode) return;
		ITabPlayer sender = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (sender == null) return;
		if (e.getMessage().equalsIgnoreCase("/tab") || e.getMessage().equalsIgnoreCase("/tab:tab")) {
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
	private static void inject(UUID player) {
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
			Injector.inject(player);
		} else if (ProtocolVersion.SERVER_VERSION.getMinorVersion() == 7) {
			Injector1_7.inject(player);
		}
	}

	@SuppressWarnings("unchecked")
	public static boolean killPacket(Object packetPlayOutScoreboardTeam) throws Exception{
		if (PacketPlayOutScoreboardTeam.SIGNATURE.getInt(packetPlayOutScoreboardTeam) != 69) {
			Collection<String> players = (Collection<String>) PacketPlayOutScoreboardTeam.PLAYERS.get(packetPlayOutScoreboardTeam);
			for (ITabPlayer p : Shared.getPlayers()) {
				if (players.contains(p.getName()) && !p.disabledNametag) {
					return true;
				}
			}
		} else {
			//PacketPlayOutScoreboardTeam.SIGNATURE.set(packetPlayOutScoreboardTeam, 0);
		}
		return false;
	}
	public static void registerPlaceholders() {
		if (Bukkit.getPluginManager().isPluginEnabled("Vault")) PluginHooks.Vault_loadProviders();
		if (Bukkit.getPluginManager().isPluginEnabled("iDisguise")) {
			RegisteredServiceProvider<DisguiseAPI> provider = Bukkit.getServicesManager().getRegistration(DisguiseAPI.class);
			if (provider != null) PluginHooks.idisguise = provider.getProvider();
		}
		PluginHooks.luckPerms = Bukkit.getPluginManager().isPluginEnabled("LuckPerms");
		PluginHooks.groupManager = Bukkit.getPluginManager().getPlugin("GroupManager");
		PluginHooks.placeholderAPI = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
		PluginHooks.permissionsEx = Bukkit.getPluginManager().isPluginEnabled("PermissionsEx");
		PluginHooks.libsDisguises = Bukkit.getPluginManager().isPluginEnabled("LibsDisguises");
		PluginHooks.deluxetags = Bukkit.getPluginManager().isPluginEnabled("DeluxeTags");
		PluginHooks.essentials = Bukkit.getPluginManager().getPlugin("Essentials");
		PluginHooks.protocolsupport = Bukkit.getPluginManager().isPluginEnabled("ProtocolSupport");
		PluginHooks.viaversion = Bukkit.getPluginManager().isPluginEnabled("ViaVersion");
		PluginHooks.ultrapermissions = Bukkit.getPluginManager().isPluginEnabled("UltraPermissions");

		usedExpansions = new ArrayList<String>();

		TABAPI.registerPlayerPlaceholder(new PlayerPlaceholder("%money%", 1000) {
			public String get(ITabPlayer p) {
				if (PluginHooks.essentials != null) return Shared.decimal2.format(PluginHooks.Essentials_getMoney(p));
				if (PluginHooks.Vault_economy != null) return Shared.decimal2.format(PluginHooks.Vault_getMoney(p));
				return "-";
			}
		});
		TABAPI.registerPlayerPlaceholder(new PlayerPlaceholder("%xPos%", 0) {
			public String get(ITabPlayer p) {
				return (((TabPlayer)p).player).getLocation().getBlockX()+"";
			}
		});
		TABAPI.registerPlayerPlaceholder(new PlayerPlaceholder("%yPos%", 0) {
			public String get(ITabPlayer p) {
				return (((TabPlayer)p).player).getLocation().getBlockY()+"";
			}
		});
		TABAPI.registerPlayerPlaceholder(new PlayerPlaceholder("%zPos%", 0) {
			public String get(ITabPlayer p) {
				return (((TabPlayer)p).player).getLocation().getBlockZ()+"";
			}
		});
		TABAPI.registerPlayerPlaceholder(new PlayerPlaceholder("%displayname%", 0) {
			public String get(ITabPlayer p) {
				return (((TabPlayer)p).player).getDisplayName();
			}
		});
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 7) TABAPI.registerPlayerPlaceholder(new PlayerPlaceholder("%deaths%", 5000) {
			public String get(ITabPlayer p) {
				return (((TabPlayer)p).player).getStatistic(Statistic.DEATHS)+"";
			}
		});
		TABAPI.registerPlayerPlaceholder(new PlayerPlaceholder("%essentialsnick%", 1000) {
			public String get(ITabPlayer p) {
				String name = null;
				if (PluginHooks.essentials != null) {
					name = PluginHooks.Essentials_getNickname(p);
				}
				if (name == null || name.length() == 0) return p.getName();
				return Configs.SECRET_essentials_nickname_prefix + name;
			}
		});
		if (Bukkit.getPluginManager().isPluginEnabled("DeluxeTags")) {
			TABAPI.registerPlayerPlaceholder(new PlayerPlaceholder("%deluxetag%", 0) {
				public String get(ITabPlayer p) {
					return PluginHooks.DeluxeTag_getPlayerDisplayTag(p);
				}
			});
		} else {
			TABAPI.registerServerConstant(new ServerConstant("%deluxetag%") {
				public String get() {
					return "";
				}
			});
		}
		TABAPI.registerPlayerPlaceholder(new PlayerPlaceholder("%faction%", 1000) {

			private boolean init;
			private int type;

			private void init() {
				if (init) return;
				try {
					Class.forName("com.massivecraft.factions.FPlayers");
					type = 1;
				} catch (Throwable e) {}
				try {
					Class.forName("com.massivecraft.factions.entity.MPlayer");
					type = 2;
				} catch (Throwable e) {}
				init = true;
			}
			public String get(ITabPlayer p) {
				init();
				if (type == 0) return "";
				String name = null;
				if (type == 1) name = PluginHooks.FactionsUUID_getFactionTag(p);
				if (type == 2) name = PluginHooks.FactionsMCore_getFactionName(p);
				return name;
			}
		});
		TABAPI.registerPlayerPlaceholder(new PlayerPlaceholder("%health%", 100) {
			public String get(ITabPlayer p) {
				return (int) Math.ceil(((TabPlayer)p).player.getHealth())+"";
			}
		});
		TABAPI.registerServerPlaceholder(new ServerPlaceholder("%tps%", 1000) {
			public String get() {
				return Shared.decimal2.format(Math.min(20, MethodAPI.getInstance().getTPS()));
			}
		});
		if (Bukkit.getPluginManager().isPluginEnabled("xAntiAFK")) {
			TABAPI.registerPlayerPlaceholder(new PlayerPlaceholder("%afk%", 1000) {
				public String get(ITabPlayer p) {
					return PluginHooks.xAntiAFK_isAfk(p)?Configs.yesAfk:Configs.noAfk;
				}
				@Override
				public String[] getChilds(){
					return new String[] {Configs.yesAfk, Configs.noAfk};
				}
			});
		} else if (Bukkit.getPluginManager().isPluginEnabled("AFKPlus")) {
			TABAPI.registerPlayerPlaceholder(new PlayerPlaceholder("%afk%", 1000) {

				public String get(ITabPlayer p) {
					return PluginHooks.AFKPlus_isAFK(p)? Configs.yesAfk : Configs.noAfk;
				}
				@Override
				public String[] getChilds(){
					return new String[] {Configs.yesAfk, Configs.noAfk};
				}
			});
		} else if (Bukkit.getPluginManager().isPluginEnabled("AutoAFK")) {
			TABAPI.registerPlayerPlaceholder(new PlayerPlaceholder("%afk%", 1000) {

				public String get(ITabPlayer p) {
					return PluginHooks.AutoAFK_isAFK(p)? Configs.yesAfk : Configs.noAfk;
				}
				@Override
				public String[] getChilds(){
					return new String[] {Configs.yesAfk, Configs.noAfk};
				}
			});
		} else if (Bukkit.getPluginManager().isPluginEnabled("AntiAFKPlus")) {
			TABAPI.registerPlayerPlaceholder(new PlayerPlaceholder("%afk%", 1000) {

				public String get(ITabPlayer p) {
					return PluginHooks.AntiAFKPlus_isAFK(p)? Configs.yesAfk : Configs.noAfk;
				}
				@Override
				public String[] getChilds(){
					return new String[] {Configs.yesAfk, Configs.noAfk};
				}
			});
		} else if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
			TABAPI.registerPlayerPlaceholder(new PlayerPlaceholder("%afk%", 1000) {

				public String get(ITabPlayer p) {
					return PluginHooks.Essentials_isAFK(p) ? Configs.yesAfk : Configs.noAfk;
				}
				@Override
				public String[] getChilds(){
					return new String[] {Configs.yesAfk, Configs.noAfk};
				}
			});
		} else {
			TABAPI.registerServerConstant(new ServerConstant("%afk%") {
				public String get() {
					return "";
				}
			});
		}
		TABAPI.registerPlayerPlaceholder(new PlayerPlaceholder("%canseeonline%", 2000) {
			public String get(ITabPlayer p) {
				int var = 0;
				for (ITabPlayer all : Shared.getPlayers()){
					if ((((TabPlayer)p).player).canSee(((TabPlayer)all).player)) var++;
				}
				return var+"";
			}
		});
		TABAPI.registerPlayerPlaceholder(new PlayerPlaceholder("%canseestaffonline%", 2000) {
			public String get(ITabPlayer p) {
				int var = 0;
				for (ITabPlayer all : Shared.getPlayers()){
					if (all.isStaff() && (((TabPlayer)p).player).canSee(((TabPlayer)all).player)) var++;
				}
				return var+"";
			}
		});
		if (Bukkit.getPluginManager().isPluginEnabled("Vault") && PluginHooks.Vault_chat != null) {
			TABAPI.registerPlayerPlaceholder(new PlayerPlaceholder("%vault-prefix%", 1000) {

				public String get(ITabPlayer p) {
					String prefix = PluginHooks.Vault_getPrefix(p);
					return prefix != null ? prefix : "";
				}
			});
			TABAPI.registerPlayerPlaceholder(new PlayerPlaceholder("%vault-suffix%", 1000) {

				public String get(ITabPlayer p) {
					String suffix = PluginHooks.Vault_getSuffix(p);
					return suffix != null ? suffix : "";
				}
			});
		} else {
			TABAPI.registerServerConstant(new ServerConstant("%vault-prefix%") {
				public String get() {
					return "";
				}
			});
			TABAPI.registerServerConstant(new ServerConstant("%vault-suffix%") {
				public String get() {
					return "";
				}
			});
		}
		TABAPI.registerServerConstant(new ServerConstant("%maxplayers%") {
			public String get() {
				return Bukkit.getMaxPlayers()+"";
			}
		});
		Shared.registerUniversalPlaceholders();
	}
	@SuppressWarnings("unchecked")
	public static Player[] getOnlinePlayers(){
		try {
			Object players = Bukkit.class.getMethod("getOnlinePlayers").invoke(null);
			if (players instanceof Player[]) {
				//1.5.x - 1.6.x
				return (Player[]) players;
			} else {
				//1.7+
				return ((Collection<Player>)players).toArray(new Player[0]); 
			}
		} catch (Exception e) {
			return Shared.errorManager.printError(new Player[0], "Failed to get online players");
		}
	}
	public static boolean LibsDisguises_isDisguised(ITabPlayer p) {
		return me.libraryaddict.disguise.DisguiseAPI.isDisguised(((TabPlayer)p).player);
	}
	public static double Vault_getMoney(ITabPlayer p) {
		return ((Economy)PluginHooks.Vault_economy).getBalance(((TabPlayer)p).player);
	}

	/*
	 *  Implementing MainClass
	 */

	public void loadFeatures(boolean inject) throws Exception{
		if (Configs.bukkitBridgeMode) {
			PluginHooks.placeholderAPI = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
			if (!PluginHooks.placeholderAPI) {
				Shared.errorManager.startupWarn("Bukkit bridge mode is enabled but PlaceholderAPI was not found, this will not work.");
			}
			plm = new PluginMessenger(this);
		} else {
			registerPlaceholders();
			if (Configs.config.getBoolean("belowname.enabled", true)) Shared.registerFeature("belowname", new BelowName());
			if (Configs.BossBarEnabled) {
				Shared.registerFeature("bossbar", new BossBar());
				if (ProtocolVersion.SERVER_VERSION.getMinorVersion() < 9) Shared.registerFeature("bossbar1.8", new BossBar_legacy());
			}
			if (Configs.config.getBoolean("enable-header-footer", true)) Shared.registerFeature("headerfooter", new HeaderFooter());
			if (Configs.config.getBoolean("change-nametag-prefix-suffix", true)) {
				if (Configs.config.getBoolean("unlimited-nametag-prefix-suffix-mode.enabled", false) && ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
					Shared.registerFeature("nametagx", new NameTagX());
				} else {
					Shared.registerFeature("nametag16", new NameTag16());
				}
			}
			if (objType != TabObjectiveType.NONE) 																							Shared.registerFeature("tabobjective", new TabObjective(objType));
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8 && Configs.config.getBoolean("change-tablist-prefix-suffix", true)) 	{
				Shared.registerFeature("playerlist", new Playerlist());
				if (Premium.allignTabsuffix) Shared.registerFeature("alignedsuffix", new AlignedSuffix());
			}
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9 && Configs.advancedconfig.getBoolean("fix-pet-names", false)) 		Shared.registerFeature("petfix", new PetFix());
			if (Configs.config.getBoolean("do-not-move-spectators", false)) 																Shared.registerFeature("spectatorfix", new SpectatorFix());
			if (Premium.is() && Premium.premiumconfig.getBoolean("scoreboard.enabled", false)) 												Shared.registerFeature("scoreboard", new ScoreboardManager());
			if (Configs.advancedconfig.getBoolean("per-world-playerlist.enabled", false)) 													Shared.registerFeature("pwp", new PerWorldPlayerlist());
			if (Configs.SECRET_remove_ghost_players) 																						Shared.registerFeature("ghostplayerfix", new GhostPlayerFix());
			if (PluginHooks.placeholderAPI) {
				Shared.registerFeature("papihook", new TabExpansion());
				new ExpansionDownloader();
			}
			new UpdateChecker();

			for (Player p : getOnlinePlayers()) {
				ITabPlayer t = new TabPlayer(p);
				Shared.data.put(p.getUniqueId(), t);
				Shared.entityIdMap.put(p.getEntityId(), t);
				if (inject) inject(t.getUniqueId());
			}
		}
	}
	public void sendConsoleMessage(String message) {
		Bukkit.getConsoleSender().sendMessage(Placeholders.color(message));
	}
	public String getPermissionPlugin() {
		if (PluginHooks.luckPerms) return "LuckPerms";
		if (PluginHooks.permissionsEx) return "PermissionsEx";
		if (PluginHooks.groupManager != null) return "GroupManager";
		if (PluginHooks.ultrapermissions) return "UltraPermissions";
		if (PluginHooks.Vault_permission != null) return PluginHooks.Vault_getPermissionPlugin() + " (detected by Vault)";
		return "Unknown/None";
	}
	public Object buildPacket(UniversalPacketPlayOut packet, ProtocolVersion protocolVersion) throws Exception {
		return packet.toNMS(protocolVersion);
	}
	public void loadConfig() throws Exception {
		Configs.config = new ConfigurationFile("bukkitconfig.yml", "config.yml", Arrays.asList("# Detailed explanation of all options available at https://github.com/NEZNAMY/TAB/wiki/config.yml", ""));
		Configs.modifyNPCnames = Configs.config.getBoolean("unlimited-nametag-prefix-suffix-mode.modify-npc-names", true);

		String objective = Configs.config.getString("tablist-objective", "PING");
		try{
			objType = TabObjectiveType.valueOf(objective.toUpperCase());
		} catch (Throwable e) {
			Shared.errorManager.startupWarn("\"&e" + objective + "&c\" is not a valid type of tablist-objective. Valid options are: &ePING, HEARTS, CUSTOM &cand &eNONE &cfor disabling the feature.");
			objType = TabObjectiveType.NONE;
		}
		TabObjective.rawValue = Configs.config.getString("tablist-objective-custom-value", "%ping%");
		if (objType == TabObjectiveType.PING) {
			TabObjective.rawValue = "%ping%";
			Placeholders.usedPlaceholders.add("%ping%");
		}
		if (objType == TabObjectiveType.HEARTS) {
			TabObjective.rawValue = "%health%";
			Placeholders.usedPlaceholders.add("%health%");
		}
		Configs.noAfk = Configs.config.getString("placeholders.afk-no", "");
		Configs.yesAfk = Configs.config.getString("placeholders.afk-yes", " &4*&4&lAFK&4*&r");
		Configs.advancedconfig = new ConfigurationFile("advancedconfig.yml", Arrays.asList("# Detailed explanation of all options available at https://github.com/NEZNAMY/TAB/wiki/advancedconfig.yml", ""));
		Configs.sortByPermissions = Configs.advancedconfig.getBoolean("sort-players-by-permissions", false);
		Configs.usePrimaryGroup = Configs.advancedconfig.getBoolean("use-primary-group", true);
		Configs.primaryGroupFindingList = Configs.advancedconfig.getStringList("primary-group-finding-list", Arrays.asList("Owner", "Admin", "Helper", "default"));
		Configs.bukkitBridgeMode = Configs.advancedconfig.getBoolean("bukkit-bridge-mode", false);
		Configs.groupsByPermissions = Configs.advancedconfig.getBoolean("assign-groups-by-permissions", false);
	}
	public void registerUnknownPlaceholder(String identifier) {
		if (identifier.contains("_")) {
			String plugin = identifier.split("_")[0].replace("%", "").toLowerCase();
			if (!usedExpansions.contains(plugin) && !plugin.equals("some")) {
				usedExpansions.add(plugin);
				Shared.debug("&dFound used placeholderapi expansion: &e" + plugin);
			}
			int server = Configs.getSecretOption("papi-placeholder-cooldowns.server." + identifier, -1);
			if (server != -1) {
				Shared.debug("Registering SERVER PAPI placeholder " + identifier + " with cooldown " + server);
				TABAPI.registerServerPlaceholder(new ServerPlaceholder(identifier, server){
					public String get() {
						return PluginHooks.PlaceholderAPI_setPlaceholders((Player)null, identifier);
					}
				});
				return;
			}
			int player = Configs.getSecretOption("papi-placeholder-cooldowns.player." + identifier, -1);
			if (player != -1) {
				Shared.debug("Registering PLAYER PAPI placeholder " + identifier + " with cooldown " + player);
				TABAPI.registerPlayerPlaceholder(new PlayerPlaceholder(identifier, player){
					public String get(ITabPlayer p) {
						return PluginHooks.PlaceholderAPI_setPlaceholders(((TabPlayer)p).player, identifier);
					}
				});
				return;
			}
			Shared.debug("Registering PLAYER PAPI placeholder " + identifier);
			TABAPI.registerPlayerPlaceholder(new PlayerPlaceholder(identifier, 49){
				public String get(ITabPlayer p) {
					return PluginHooks.PlaceholderAPI_setPlaceholders(((TabPlayer)p).player, identifier);
				}
			});
			return;
		}
		//		Shared.print('6', "Unknown placeholder: " + identifier);
	}

	@SuppressWarnings("unchecked")
	public void convertConfig(ConfigurationFile config) {
		if (config.getName().equals("config.yml")) {
			ticks2Millis(config, "nametag-refresh-interval-ticks", "nametag-refresh-interval-milliseconds");
			ticks2Millis(config, "tablist-refresh-interval-ticks", "tablist-refresh-interval-milliseconds");
			ticks2Millis(config, "header-footer-refresh-interval-ticks", "header-footer-refresh-interval-milliseconds");
			ticks2Millis(config, "belowname.refresh-interval-ticks", "belowname.refresh-interval-milliseconds");
			removeOld(config, "placeholders.deluxetag-yes");
			removeOld(config, "placeholders.deluxetag-no");
			removeOld(config, "placeholders.faction-yes");
			removeOld(config, "placeholders.faction-no");
			removeOld(config, "staff-groups");
			removeOld(config, "use-essentials-nickname");
			removeOld(config, "deluxetag-empty-value");
			removeOld(config, "per-world-playerlist");
			removeOld(config, "factions-faction");
			removeOld(config, "factions-nofaction");
			removeOld(config, "date-format");
			removeOld(config, "time-format");
			if (Bukkit.getPluginManager().isPluginEnabled("eGlow")) {
				for (String group : ((Map<String, Object>)config.get("Groups")).keySet()) {
					String tagprefix = config.getString("Groups." + group + ".tagprefix");
					if (tagprefix != null) {
						if (!tagprefix.contains("%eglow_glowcolor%")) {
							Shared.print('2', "eGlow is installed but %eglow_glowcolor% is not used in tagprefix of group " + group + ". Adding it to make eGlow work properly");
							config.set("Groups." + group + ".tagprefix", tagprefix + "%eglow_glowcolor%");
						}
					}
				}
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
		if (config.getName().equals("advancedconfig.yml")) {
			if (config.get("per-world-playerlist") instanceof Boolean) {
				rename(config, "per-world-playerlist", "per-world-playerlist.enabled");
				rename(config, "allow-pwp-bypass-permission", "per-world-playerlist.allow-bypass-permission");
				rename(config, "ignore-pwp-in-worlds", "per-world-playerlist.ignore-effect-in-worlds");
				Map<String, List<String>> sharedWorlds = new HashMap<String, List<String>>();
				sharedWorlds.put("lobby", Arrays.asList("lobby1", "lobby2"));
				sharedWorlds.put("minigames", Arrays.asList("paintball", "bedwars"));
				sharedWorlds.put("DoNotDoThis", Arrays.asList("ThisIsASingleWorldSoThereIsNoPointInEvenCreatingGroupForIt"));
				config.set("per-world-playerlist.shared-playerlist-world-groups", sharedWorlds);
				Shared.print('2', "Converted old per-world-playerlist section to new one in advancedconfig.yml.");
			}
		}
	}
}