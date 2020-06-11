package me.neznamy.tab.platforms.bukkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import me.neznamy.tab.platforms.bukkit.features.BossBar_legacy;
import me.neznamy.tab.platforms.bukkit.features.ExpansionDownloader;
import me.neznamy.tab.platforms.bukkit.features.PerWorldPlayerlist;
import me.neznamy.tab.platforms.bukkit.features.PetFix;
import me.neznamy.tab.platforms.bukkit.features.TabExpansion;
import me.neznamy.tab.platforms.bukkit.features.unlimitedtags.NameTagX;
import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.premium.AlignedSuffix;
import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.premium.ScoreboardManager;
import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ConfigurationFile;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.MainClass;
import me.neznamy.tab.shared.PluginHooks;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.command.TabCommand;
import me.neznamy.tab.shared.cpu.CPUFeature;
import me.neznamy.tab.shared.features.BelowName;
import me.neznamy.tab.shared.features.BossBar;
import me.neznamy.tab.shared.features.GhostPlayerFix;
import me.neznamy.tab.shared.features.HeaderFooter;
import me.neznamy.tab.shared.features.NameTag16;
import me.neznamy.tab.shared.features.PlaceholderRefresher;
import me.neznamy.tab.shared.features.Playerlist;
import me.neznamy.tab.shared.features.SpectatorFix;
import me.neznamy.tab.shared.features.TabObjective;
import me.neznamy.tab.shared.features.UpdateChecker;
import me.neznamy.tab.shared.features.interfaces.CommandListener;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardTeam;
import me.neznamy.tab.shared.packets.UniversalPacketPlayOut;
import me.neznamy.tab.shared.placeholders.Placeholder;
import me.neznamy.tab.shared.placeholders.Placeholders;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholder;
import me.neznamy.tab.shared.placeholders.ServerConstant;
import me.neznamy.tab.shared.placeholders.ServerPlaceholder;
import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin implements Listener, MainClass{

	public static Main instance;
	@SuppressWarnings("unused")
	private PluginMessenger plm;
	public static List<String> usedExpansions;
	public static final String serverPackage = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

	public void onEnable(){
		ProtocolVersion.SERVER_VERSION = ProtocolVersion.fromServerString(Bukkit.getBukkitVersion().split("-")[0]);
		Shared.mainClass = this;
		Shared.separatorType = "world";
		Shared.print('7', "Server version: " + Bukkit.getBukkitVersion().split("-")[0] + " (" + serverPackage + ")");
		if (MethodAPI.getInstance() != null){
			instance = this;
			Bukkit.getPluginManager().registerEvents(this, this);
			Shared.command = new TabCommand();
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
								if (Configs.bukkitBridgeMode) sender.sendMessage(Placeholders.color(" &6&lBukkit bridge mode activated"));
								if (Shared.disabled) sender.sendMessage(Placeholders.color(" &c&lPlugin is disabled due to a broken configuration file (" + Shared.brokenFile + ")"));
								sender.sendMessage(Placeholders.color(" &8>> &3&l/tab reload"));
								sender.sendMessage(Placeholders.color("      - &7Reloads plugin and config"));
								sender.sendMessage(Placeholders.color("&m                                                                                "));
							}
						}
					} else {
						Shared.command.execute(sender instanceof Player ? Shared.getPlayer(((Player)sender).getUniqueId()) : null, args);
					}
					return false;
				}
			});
			Bukkit.getPluginCommand("tab").setTabCompleter(new TabCompleter() {
				public List<String> onTabComplete(CommandSender sender, Command c, String cmd, String[] args) {
					if (Configs.bukkitBridgeMode) {
						return null;
					}
					return Shared.command.complete(sender instanceof Player ? Shared.getPlayer(((Player)sender).getUniqueId()) : null, args);
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
			Shared.featureCpu.runMeasuredTask("player joined the server", CPUFeature.OTHER, new Runnable() {

				public void run() {
					Shared.joinListeners.values().forEach(f -> f.onJoin(p));
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
			disconnectedPlayer.disconnect();
			Shared.quitListeners.values().forEach(f -> f.onQuit(disconnectedPlayer));
			for (Placeholder pl : Placeholders.getAllPlaceholders()) {
				if (pl instanceof PlayerPlaceholder) {
					((PlayerPlaceholder)pl).lastRefresh.remove(disconnectedPlayer.getName());
					((PlayerPlaceholder)pl).lastValue.remove(disconnectedPlayer.getName());
				}
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
			p.onWorldChange(e.getFrom().getName(), p.world = e.getPlayer().getWorld().getName());
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
		for (CommandListener listener : Shared.commandListeners.values()) {
			if (listener.onCommand(sender, e.getMessage())) e.setCancelled(true);
		}
	}
	@EventHandler
	public void a(EntityPotionEffectEvent e) {
		if (e.getEntity() instanceof Player) {
			ITabPlayer player = Shared.getPlayer(e.getEntity().getUniqueId());	
			if (player == null) return;
			if (e.getNewEffect() != null && e.getNewEffect().getType().equals(PotionEffectType.INVISIBILITY) || e.getOldEffect() != null && e.getOldEffect().getType().equals(PotionEffectType.INVISIBILITY))
				player.nameTagVisible = player.hasInvisibility();
		}	
	}
	private static void inject(UUID player) {
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
			Injector.inject(player);
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
		}
		return false;
	}
	
	public void registerPlaceholders() {
		if (Bukkit.getPluginManager().isPluginEnabled("Vault")) PluginHooks.Vault_loadProviders();
		if (Bukkit.getPluginManager().isPluginEnabled("iDisguise")) {
			try {
				RegisteredServiceProvider<?> provider = Bukkit.getServicesManager().getRegistration(Class.forName("de.robingrether.idisguise.api.DisguiseAPI"));
				if (provider != null) PluginHooks.idisguise = provider.getProvider();
			} catch (ClassNotFoundException e) {

			}
		}
		PluginHooks.luckPerms = Bukkit.getPluginManager().isPluginEnabled("LuckPerms");
		if (PluginHooks.luckPerms) PluginHooks.luckPermsVersion = Bukkit.getPluginManager().getPlugin("LuckPerms").getDescription().getVersion();
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

		Placeholders.registerPlaceholder(new PlayerPlaceholder("%money%", 1000) {
			public String get(ITabPlayer p) {
				if (PluginHooks.essentials != null) return Placeholders.decimal2.format(PluginHooks.Essentials_getMoney(p));
				if (PluginHooks.Vault_economy != null) return Placeholders.decimal2.format(PluginHooks.Vault_getMoney(p));
				return "-";
			}
		});
		Placeholders.registerPlaceholder(new PlayerPlaceholder("%xPos%", 0) {
			public String get(ITabPlayer p) {
				return (p.getBukkitEntity()).getLocation().getBlockX()+"";
			}
		});
		Placeholders.registerPlaceholder(new PlayerPlaceholder("%yPos%", 0) {
			public String get(ITabPlayer p) {
				return (p.getBukkitEntity()).getLocation().getBlockY()+"";
			}
		});
		Placeholders.registerPlaceholder(new PlayerPlaceholder("%zPos%", 0) {
			public String get(ITabPlayer p) {
				return (p.getBukkitEntity()).getLocation().getBlockZ()+"";
			}
		});
		Placeholders.registerPlaceholder(new PlayerPlaceholder("%displayname%", 0) {
			public String get(ITabPlayer p) {
				return (p.getBukkitEntity()).getDisplayName();
			}
		});
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 7) Placeholders.registerPlaceholder(new PlayerPlaceholder("%deaths%", 5000) {
			public String get(ITabPlayer p) {
				return (p.getBukkitEntity()).getStatistic(Statistic.DEATHS)+"";
			}
		});
		Placeholders.registerPlaceholder(new PlayerPlaceholder("%essentialsnick%", 1000) {
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
			Placeholders.registerPlaceholder(new PlayerPlaceholder("%deluxetag%", 0) {
				public String get(ITabPlayer p) {
					return PluginHooks.DeluxeTag_getPlayerDisplayTag(p);
				}
			});
		}
		Placeholders.registerPlaceholder(new PlayerPlaceholder("%health%", 100) {
			public String get(ITabPlayer p) {
				return (int) Math.ceil(p.getBukkitEntity().getHealth())+"";
			}
		});
		Placeholders.registerPlaceholder(new ServerPlaceholder("%tps%", 1000) {
			public String get() {
				return Placeholders.decimal2.format(Math.min(20, MethodAPI.getInstance().getTPS()));
			}
		});
		if (Bukkit.getPluginManager().isPluginEnabled("xAntiAFK")) {
			Placeholders.registerPlaceholder(new PlayerPlaceholder("%afk%", 500) {
				public String get(ITabPlayer p) {
					return PluginHooks.xAntiAFK_isAfk(p)?Configs.yesAfk:Configs.noAfk;
				}
				@Override
				public String[] getChilds(){
					return new String[] {Configs.yesAfk, Configs.noAfk};
				}
			});
		} else if (Bukkit.getPluginManager().isPluginEnabled("AFKPlus")) {
			Placeholders.registerPlaceholder(new PlayerPlaceholder("%afk%", 500) {

				public String get(ITabPlayer p) {
					return PluginHooks.AFKPlus_isAFK(p)? Configs.yesAfk : Configs.noAfk;
				}
				@Override
				public String[] getChilds(){
					return new String[] {Configs.yesAfk, Configs.noAfk};
				}
			});
		} else if (Bukkit.getPluginManager().isPluginEnabled("AutoAFK")) {
			Placeholders.registerPlaceholder(new PlayerPlaceholder("%afk%", 500) {

				public String get(ITabPlayer p) {
					return PluginHooks.AutoAFK_isAFK(p)? Configs.yesAfk : Configs.noAfk;
				}
				@Override
				public String[] getChilds(){
					return new String[] {Configs.yesAfk, Configs.noAfk};
				}
			});
		} else if (Bukkit.getPluginManager().isPluginEnabled("AntiAFKPlus")) {
			Placeholders.registerPlaceholder(new PlayerPlaceholder("%afk%", 500) {

				public String get(ITabPlayer p) {
					return PluginHooks.AntiAFKPlus_isAFK(p)? Configs.yesAfk : Configs.noAfk;
				}
				@Override
				public String[] getChilds(){
					return new String[] {Configs.yesAfk, Configs.noAfk};
				}
			});
		} else if (Bukkit.getPluginManager().isPluginEnabled("CMI")) {
			Placeholders.registerPlaceholder(new PlayerPlaceholder("%afk%", 500) {

				public String get(ITabPlayer p) {
					return PluginHooks.CMI_isAFK(p) ? Configs.yesAfk : Configs.noAfk;
				}
				@Override
				public String[] getChilds(){
					return new String[] {Configs.yesAfk, Configs.noAfk};
				}
			});
		} else if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
			Placeholders.registerPlaceholder(new PlayerPlaceholder("%afk%", 500) {

				public String get(ITabPlayer p) {
					return PluginHooks.Essentials_isAFK(p) ? Configs.yesAfk : Configs.noAfk;
				}
				@Override
				public String[] getChilds(){
					return new String[] {Configs.yesAfk, Configs.noAfk};
				}
			});
		} else {
			Placeholders.registerPlaceholder(new ServerConstant("%afk%") {
				public String get() {
					return "";
				}
			});
		}
		Placeholders.registerPlaceholder(new PlayerPlaceholder("%canseeonline%", 2000) {
			public String get(ITabPlayer p) {
				int var = 0;
				for (ITabPlayer all : Shared.getPlayers()){
					if ((p.getBukkitEntity()).canSee(all.getBukkitEntity())) var++;
				}
				return var+"";
			}
		});
		Placeholders.registerPlaceholder(new PlayerPlaceholder("%canseestaffonline%", 2000) {
			public String get(ITabPlayer p) {
				int var = 0;
				for (ITabPlayer all : Shared.getPlayers()){
					if (all.isStaff() && (p.getBukkitEntity()).canSee(all.getBukkitEntity())) var++;
				}
				return var+"";
			}
		});
		if (Bukkit.getPluginManager().isPluginEnabled("Vault") && PluginHooks.Vault_chat != null) {
			Placeholders.registerPlaceholder(new PlayerPlaceholder("%vault-prefix%", 500) {

				public String get(ITabPlayer p) {
					String prefix = PluginHooks.Vault_getPrefix(p);
					return prefix != null ? prefix : "";
				}
			});
			Placeholders.registerPlaceholder(new PlayerPlaceholder("%vault-suffix%", 500) {

				public String get(ITabPlayer p) {
					String suffix = PluginHooks.Vault_getSuffix(p);
					return suffix != null ? suffix : "";
				}
			});
		} else {
			Placeholders.registerPlaceholder(new ServerConstant("%vault-prefix%") {
				public String get() {
					return "";
				}
			});
			Placeholders.registerPlaceholder(new ServerConstant("%vault-suffix%") {
				public String get() {
					return "";
				}
			});
		}
		Placeholders.registerPlaceholder(new ServerConstant("%maxplayers%") {
			public String get() {
				return Bukkit.getMaxPlayers()+"";
			}
		});
		Placeholders.registerUniversalPlaceholders();
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
	public static double Vault_getMoney(ITabPlayer p) {
		return ((Economy)PluginHooks.Vault_economy).getBalance(p.getBukkitEntity());
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
			if (Configs.config.getBoolean("classic-vanilla-belowname.enabled", true)) Shared.registerFeature("belowname", new BelowName());
			if (Configs.BossBarEnabled) {
				BossBar bb = new BossBar();
				Shared.registerFeature("bossbar", bb);
				if (ProtocolVersion.SERVER_VERSION.getMinorVersion() < 9) Shared.registerFeature("bossbar1.8", new BossBar_legacy(bb));
			}
			if (Configs.config.getBoolean("enable-header-footer", true)) Shared.registerFeature("headerfooter", new HeaderFooter());
			if (Configs.config.getBoolean("change-nametag-prefix-suffix", true)) {
				if (Configs.config.getBoolean("unlimited-nametag-prefix-suffix-mode.enabled", false) && ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
					if (Configs.config.getBoolean("classic-vanilla-belowname.enabled", true)) {
						Shared.errorManager.startupWarn("Both unlimited nametag mode and belowname features are enabled, this will result in the worst combination: belowname objective not appearing on players, only NPCs. Check wiki for more info.");
					}
					Shared.registerFeature("nametagx", new NameTagX());
				} else {
					Shared.registerFeature("nametag16", new NameTag16());
				}
			}
			if (Configs.config.getString("yellow-number-in-tablist", "%ping%").length() > 0) 												Shared.registerFeature("tabobjective", new TabObjective());
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8 && Configs.config.getBoolean("change-tablist-prefix-suffix", true)) 	{
				if (Premium.allignTabsuffix) Shared.registerFeature("alignedsuffix", new AlignedSuffix());
				Shared.registerFeature("playerlist", new Playerlist());
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
	public void sendRawConsoleMessage(String message) {
		Bukkit.getConsoleSender().sendMessage(message);
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
			if (plugin.equals("some")) return;
			if (!usedExpansions.contains(plugin)) {
				usedExpansions.add(plugin);
				Shared.debug("&dFound used placeholderapi expansion: &e" + plugin);
			}
			PlaceholderRefresher.registerPlaceholder(identifier);
		}
	}

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
				for (Object group : config.getConfigurationSection("Groups").keySet()) {
					String tagprefix = config.getString("Groups." + group + ".tagprefix");
					if (tagprefix != null && !tagprefix.contains("%eglow_glowcolor%")) {
						Shared.print('2', "eGlow is installed but %eglow_glowcolor% is not used in tagprefix of group " + group + ". Adding it to make eGlow work properly");
						config.set("Groups." + group + ".tagprefix", tagprefix + "%eglow_glowcolor%");
					}
				}
			}
			if (config.hasConfigOption("tablist-objective")) {
				String type = config.getString("tablist-objective");
				String value;
				if (type.equals("NONE")) {
					value = "";
				} else if (type.equals("PING")){
					value = "%ping%";
				} else if (type.equals("HEARTS")) {
					value = "%health%";
				} else {
					value = config.getString("tablist-objective-custom-value");
				}
				config.set("tablist-objective", null);
				config.set("tablist-objective-custom-value", null);
				config.set("yellow-number-in-tablist", value);
				Shared.print('2', "Converted old tablist-objective config option to new yellow-number-in-tablist");
			}
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
		if (config.getName().equals("advancedconfig.yml")) {
			if (config.getObject("per-world-playerlist") instanceof Boolean) {
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
	@Override
	public String getServerVersion() {
		return Bukkit.getBukkitVersion().split("-")[0] + " (" + serverPackage + ")";
	}
	@Override
	public void suggestPlaceholders() {
		//bukkit only
		suggestPlaceholderSwitch("%cmi_user_afk%", "%afk%");
		suggestPlaceholderSwitch("%cmi_user_afk_symbol%", "%afk%");
		suggestPlaceholderSwitch("%deluxetags_tag%", "%deluxetag%");
		suggestPlaceholderSwitch("%essentials_afk%", "%afk%");
		suggestPlaceholderSwitch("%essentials_nickname%", "%essentialsnick%");
		suggestPlaceholderSwitch("%luckperms_prefix%", "%luckperms-prefix%");
		suggestPlaceholderSwitch("%luckperms_suffix%", "%luckperms-suffix%");
		suggestPlaceholderSwitch("%player_displayname%", "%displayname%");
		suggestPlaceholderSwitch("%player_health%", "%health%");
		suggestPlaceholderSwitch("%player_health_rounded%", "%health%");
		suggestPlaceholderSwitch("%player_world%", "%world%");
		suggestPlaceholderSwitch("%player_x%", "%xPos%");
		suggestPlaceholderSwitch("%player_y%", "%yPos%");
		suggestPlaceholderSwitch("%player_z%", "%zPos%");
		suggestPlaceholderSwitch("%premiumvanish_playercount%", "%canseeonline%");
		suggestPlaceholderSwitch("%server_max_players%", "%maxplayers%");
		suggestPlaceholderSwitch("%server_online%", "%online%");
		suggestPlaceholderSwitch("%server_ram_max%", "%memory-max%");
		suggestPlaceholderSwitch("%server_ram_used%", "%memory-used%");
		suggestPlaceholderSwitch("%server_tps_1%", "%tps%");
		suggestPlaceholderSwitch("%statistic_deaths%", "%deaths%");
		suggestPlaceholderSwitch("%supervanish_playercount%", "%canseeonline%");
		suggestPlaceholderSwitch("%vault_eco_balance%", "%money%");
		suggestPlaceholderSwitch("%vault_prefix%", "%vault-prefix%");
		suggestPlaceholderSwitch("%vault_rank%", "%rank%");
		suggestPlaceholderSwitch("%vault_suffix%", "%vault-suffix%");

		//both
		suggestPlaceholderSwitch("%player_ping%", "%ping%");
		suggestPlaceholderSwitch("%viaversion_player_protocol_version%", "%player-version%");
		suggestPlaceholderSwitch("%player_name%", "%nick%");
	}
}