package me.neznamy.tab.platforms.bukkit;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.parser.ParserException;
import org.yaml.snakeyaml.scanner.ScannerException;

import de.robingrether.idisguise.api.DisguiseAPI;
import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.platforms.bukkit.unlimitedtags.NameTagLineManager;
import me.neznamy.tab.platforms.bukkit.unlimitedtags.NameTagX;
import me.neznamy.tab.premium.ScoreboardManager;
import me.neznamy.tab.shared.*;
import me.neznamy.tab.shared.Shared.Feature;
import me.neznamy.tab.shared.TabObjective.TabObjectiveType;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.shared.placeholders.*;
import me.neznamy.tab.shared.packets.*;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin implements Listener, MainClass{

	public static Main instance;

	public void onEnable(){
		long total = System.currentTimeMillis();
		ProtocolVersion.SERVER_VERSION = ProtocolVersion.fromServerString(Bukkit.getBukkitVersion().split("-")[0]);
		Shared.mainClass = this;
		Shared.separatorType = "world";
		Shared.print("§7", "Server version: " + Bukkit.getBukkitVersion().split("-")[0] + " (" + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + ")");
		if (ProtocolVersion.SERVER_VERSION != ProtocolVersion.UNKNOWN){
			instance = this;
			Bukkit.getPluginManager().registerEvents(this, this);
			Bukkit.getPluginCommand("tab").setExecutor(new CommandExecutor() {
				public boolean onCommand(CommandSender sender, Command c, String cmd, String[] args){
					TabCommand.execute(sender instanceof Player ? Shared.getPlayer(((Player)sender).getUniqueId()) : null, args);
					return false;
				}
			});
			load(false, true);
			Metrics metrics = new Metrics(this);
			metrics.addCustomChart(new Metrics.SimplePie("unlimited_nametag_mode_enabled", new Callable<String>() {
				public String call() {
					return Configs.unlimitedTags ? "Yes" : "No";
				}
			}));
			metrics.addCustomChart(new Metrics.SimplePie("placeholderapi", new Callable<String>() {
				public String call() {
					return PluginHooks.placeholderAPI ? "Yes" : "No";
				}
			}));
			metrics.addCustomChart(new Metrics.SimplePie("permission_system", new Callable<String>() {
				public String call() {
					if (Bukkit.getPluginManager().isPluginEnabled("UltraPermissions")) return "UltraPermissions";
					return getPermissionPlugin();
				}
			}));
			metrics.addCustomChart(new Metrics.SimplePie("protocol_hack", new Callable<String>() {
				public String call() {
					if (Bukkit.getPluginManager().isPluginEnabled("ViaVersion") && Bukkit.getPluginManager().isPluginEnabled("ProtocolSupport")) return "ViaVersion + ProtocolSupport";
					if (Bukkit.getPluginManager().isPluginEnabled("ViaVersion")) return "ViaVersion";
					if (Bukkit.getPluginManager().isPluginEnabled("ProtocolSupport")) return "ProtocolSupport";
					return "None";
				}
			}));
			metrics.addCustomChart(new Metrics.SimplePie("server_version", new Callable<String>() {
				public String call() {
					return "1." + ProtocolVersion.SERVER_VERSION.getMinorVersion() + ".x";
				}
			}));
			if (!Shared.disabled) Shared.print("§a", "Enabled in " + (System.currentTimeMillis()-total) + "ms");
		} else {
			sendConsoleMessage("§c[TAB] Your server version is not supported. Disabling..");
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
		}
	}
	public void load(boolean broadcastTime, boolean inject) {
		try {
			long time = System.currentTimeMillis();
			Shared.disabled = false;
			Shared.startupWarns = 0;
			registerPlaceholders();
			Configs.loadFiles();
			Shared.registerAnimationPlaceholders();
			registerCooldowns();
			Shared.data.clear();
			for (Player p : getOnlinePlayers()) {
				ITabPlayer t = new TabPlayer(p);
				Shared.data.put(p.getUniqueId(), t);
				if (inject) inject(t.getUniqueId());
			}
			Placeholders.recalculateOnlineVersions();
			BossBar.load();
			BossBar_legacy.load();
			NameTagX.load();
			NameTag16.load();
			Playerlist.load();
			TabObjective.load();
			BelowName.load();
			HeaderFooter.load();
			PerWorldPlayerlist.load();
			ScoreboardManager.load();
			Shared.startCPUTask();
			if (Shared.startupWarns > 0) Shared.print("§e", "There were " + Shared.startupWarns + " startup warnings.");
			if (broadcastTime) Shared.print("§a", "Enabled in " + (System.currentTimeMillis()-time) + "ms");
		} catch (ParserException | ScannerException e) {
			Shared.print("§c", "Did not enable due to a broken configuration file.");
			Shared.disabled = true;
		} catch (Throwable e) {
			Shared.print("§c", "Failed to enable");
			sendConsoleMessage("§c" + e.getClass().getName() +": " + e.getMessage());
			for (StackTraceElement ste : e.getStackTrace()) {
				sendConsoleMessage("§c       at " + ste.toString());
			}
			Shared.disabled = true;
		}
	}
	@EventHandler(priority = EventPriority.LOWEST)
	public void a(PlayerJoinEvent e) {
		try {
			if (Shared.disabled) return;
			ITabPlayer p = new TabPlayer(e.getPlayer());
			Shared.data.put(e.getPlayer().getUniqueId(), p);
			inject(e.getPlayer().getUniqueId());
			PerWorldPlayerlist.trigger(e.getPlayer());
			Shared.runTask("player joined the server", Feature.OTHER, new Runnable() {

				public void run() {
					Placeholders.recalculateOnlineVersions();
					HeaderFooter.playerJoin(p);
					TabObjective.playerJoin(p);
					BelowName.playerJoin(p);
					NameTag16.playerJoin(p);
					NameTagX.playerJoin(p);
					BossBar.playerJoin(p);
					ScoreboardManager.register(p);
				}
			});
		} catch (Throwable ex) {
			Shared.error(null, "An error occured when player joined the server", ex);
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public void a(PlayerQuitEvent e){
		try {
			if (Shared.disabled) return;
			ITabPlayer disconnectedPlayer = Shared.getPlayer(e.getPlayer().getUniqueId());
			if (disconnectedPlayer == null) {
				Shared.error(null, "Data of " + e.getPlayer().getName() + " did not exist when player left");
				return;
			}
			Placeholders.recalculateOnlineVersions();
			NameTag16.playerQuit(disconnectedPlayer);
			NameTagX.playerQuit(disconnectedPlayer);
			ScoreboardManager.unregister(disconnectedPlayer);
			for (ITabPlayer all : Shared.getPlayers()) {
				NameTagLineManager.removeFromRegistered(all, disconnectedPlayer);
			}
			NameTagLineManager.destroy(disconnectedPlayer);
			if (Configs.SECRET_remove_ghost_players) {
				Object packet = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, disconnectedPlayer.getInfoData()).toNMS(null);
				for (ITabPlayer all : Shared.getPlayers()) {
					all.sendPacket(packet);
				}
			}
			for (PlayerPlaceholder pl : Placeholders.playerPlaceholders) {
				pl.lastRefresh.remove(e.getPlayer().getName());
				pl.lastValue.remove(e.getPlayer().getName());
			}
			Shared.data.remove(e.getPlayer().getUniqueId());
		} catch (Throwable t) {
			Shared.error(null, "An error occured when player left server", t);
			Shared.data.remove(e.getPlayer().getUniqueId());
		}
	}
	@EventHandler(priority = EventPriority.LOWEST)
	public void a(PlayerChangedWorldEvent e){
		try {
			if (Shared.disabled) return;
			ITabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
			if (p == null) return;
			PerWorldPlayerlist.trigger(e.getPlayer());
			String from = e.getFrom().getName();
			String to = p.world = e.getPlayer().getWorld().getName();
			p.onWorldChange(from, to);
		} catch (Throwable ex) {
			Shared.error(null, "An error occured when processing PlayerChangedWorldEvent", ex);
		}
	}
	@EventHandler
	public void a(PlayerCommandPreprocessEvent e) {
		if (Shared.disabled) return;
		ITabPlayer sender = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (sender == null) return;
		if (e.getMessage().equalsIgnoreCase("/tab") || e.getMessage().equalsIgnoreCase("/tab:tab")) {
			Shared.sendPluginInfo(sender);
			return;
		}
		if (BossBar.onChat(sender, e.getMessage())) e.setCancelled(true);
		if (ScoreboardManager.onCommand(sender, e.getMessage())) e.setCancelled(true);
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
			PacketPlayOutScoreboardTeam.SIGNATURE.set(packetPlayOutScoreboardTeam, 0);
		}
		return false;
	}
	public static void registerPlaceholders() {
		if (Bukkit.getPluginManager().isPluginEnabled("Vault")){
			PluginHooks.Vault_loadProviders();
		}
		if (Bukkit.getPluginManager().isPluginEnabled("iDisguise")) {
			PluginHooks.idisguise = Bukkit.getServicesManager().getRegistration(DisguiseAPI.class).getProvider();
		}
		PluginHooks.luckPerms = Bukkit.getPluginManager().isPluginEnabled("LuckPerms");
		PluginHooks.groupManager = Bukkit.getPluginManager().getPlugin("GroupManager");
		PluginHooks.placeholderAPI = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
		if (PluginHooks.placeholderAPI) PlaceholderAPIExpansion.register();
		PluginHooks.permissionsEx = Bukkit.getPluginManager().isPluginEnabled("PermissionsEx");
		PluginHooks.libsDisguises = Bukkit.getPluginManager().isPluginEnabled("LibsDisguises");
		PluginHooks.essentials = Bukkit.getPluginManager().getPlugin("Essentials");

		Placeholders.playerPlaceholders = new ArrayList<PlayerPlaceholder>();
		Placeholders.serverPlaceholders = new ArrayList<ServerPlaceholder>();
		Placeholders.constants = new ArrayList<Constant>();
		Shared.registerUniversalPlaceholders();

		Placeholders.playerPlaceholders.add(new PlayerPlaceholder("%money%", 3000) {
			public String get(ITabPlayer p) {
				return p.getMoney();
			}
		});
		Placeholders.playerPlaceholders.add(new PlayerPlaceholder("%xPos%", 0) {
			public String get(ITabPlayer p) {
				return (((TabPlayer)p).player).getLocation().getBlockX()+"";
			}
		});
		Placeholders.playerPlaceholders.add(new PlayerPlaceholder("%yPos%", 0) {
			public String get(ITabPlayer p) {
				return (((TabPlayer)p).player).getLocation().getBlockY()+"";
			}
		});
		Placeholders.playerPlaceholders.add(new PlayerPlaceholder("%zPos%", 0) {
			public String get(ITabPlayer p) {
				return (((TabPlayer)p).player).getLocation().getBlockZ()+"";
			}
		});
		Placeholders.playerPlaceholders.add(new PlayerPlaceholder("%displayname%", 0) {
			public String get(ITabPlayer p) {
				return (((TabPlayer)p).player).getDisplayName();
			}
		});
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 7) Placeholders.playerPlaceholders.add(new PlayerPlaceholder("%deaths%", 5000) {
			public String get(ITabPlayer p) {
				return (((TabPlayer)p).player).getStatistic(Statistic.DEATHS)+"";
			}
		});
		Placeholders.playerPlaceholders.add(new PlayerPlaceholder("%essentialsnick%", 3000) {
			public String get(ITabPlayer p) {
				return p.getNickname();
			}
		});
		if (Bukkit.getPluginManager().isPluginEnabled("DeluxeTags")) {
			Placeholders.playerPlaceholders.add(new PlayerPlaceholder("%deluxetag%", 2000) {
				public String get(ITabPlayer p) {
					String tag = PluginHooks.DeluxeTag_getPlayerDisplayTag(p);
					if (tag == null || tag.length() == 0) {
						return Configs.noTag;
					}
					return Configs.yesTag.replace("%value%", tag);
				}
				@Override
				public String[] getChilds(){
					return new String[] {Configs.yesTag, Configs.noTag};
				}
			});
		}
		Placeholders.playerPlaceholders.add(new PlayerPlaceholder("%faction%", 3000) {

			public int type;

			{
				try {
					Class.forName("com.massivecraft.factions.FPlayers");
					type = 1;
				} catch (Throwable e) {}
				try {
					Class.forName("com.massivecraft.factions.entity.MPlayer");
					type = 2;
				} catch (Throwable e) {}
			}
			
			public String get(ITabPlayer p) {
				if (type == 0) return Configs.noFaction;
				String name = null;
				if (type == 1) name = PluginHooks.FactionsUUID_getFactionTag(p);
				if (type == 2) name = PluginHooks.FactionsMCore_getFactionName(p);
				if (name == null || name.length() == 0 || name.contains("Wilderness")) {
					return Configs.noFaction;
				}
				return Configs.yesFaction.replace("%value%", name);
			}
			@Override
			public String[] getChilds(){
				return new String[] {Configs.yesFaction, Configs.noFaction};
			}
		});
		Placeholders.playerPlaceholders.add(new PlayerPlaceholder("%health%", 100) {
			public String get(ITabPlayer p) {
				return (int) Math.ceil(((TabPlayer)p).player.getHealth())+"";
			}
		});
		Placeholders.serverPlaceholders.add(new ServerPlaceholder("%tps%", 1000) {
			public String get() {
				return Shared.decimal2.format(Math.min(20, MethodAPI.getInstance().getTPS()));
			}
		});
		if (Bukkit.getPluginManager().isPluginEnabled("xAntiAFK")) {
			Placeholders.playerPlaceholders.add(new PlayerPlaceholder("%afk%", 1000) {
				public String get(ITabPlayer p) {
					return PluginHooks.xAntiAFK_isAfk(p)?Configs.yesAfk:Configs.noAfk;
				}
				@Override
				public String[] getChilds(){
					return new String[] {Configs.yesAfk, Configs.noAfk};
				}
			});
		} else if (Bukkit.getPluginManager().isPluginEnabled("AFKPlus")) {
			Placeholders.playerPlaceholders.add(new PlayerPlaceholder("%afk%", 1000) {

				public String get(ITabPlayer p) {
					return PluginHooks.AFKPlus_isAFK(p)? Configs.yesAfk : Configs.noAfk;
				}
				@Override
				public String[] getChilds(){
					return new String[] {Configs.yesAfk, Configs.noAfk};
				}
			});
		} else if (Bukkit.getPluginManager().isPluginEnabled("AutoAFK")) {
			Placeholders.playerPlaceholders.add(new PlayerPlaceholder("%afk%", 1000) {

				public String get(ITabPlayer p) {
					return PluginHooks.AutoAFK_isAFK(p)? Configs.yesAfk : Configs.noAfk;
				}
				@Override
				public String[] getChilds(){
					return new String[] {Configs.yesAfk, Configs.noAfk};
				}
			});
		} else if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
			Placeholders.playerPlaceholders.add(new PlayerPlaceholder("%afk%", 1000) {

				public String get(ITabPlayer p) {
					return PluginHooks.Essentials_isAFK(p) ? Configs.yesAfk : Configs.noAfk;
				}
				@Override
				public String[] getChilds(){
					return new String[] {Configs.yesAfk, Configs.noAfk};
				}
			});
		} else {
			Placeholders.constants.add(new Constant("%afk%") {
				public String get() {
					return "";
				}
			});
		}
		Placeholders.playerPlaceholders.add(new PlayerPlaceholder("%canseeonline%", 2000) {
			public String get(ITabPlayer p) {
				int var = 0;
				for (ITabPlayer all : Shared.getPlayers()){
					if ((((TabPlayer)p).player).canSee(((TabPlayer)all).player)) var++;
				}
				return var+"";
			}
		});
		Placeholders.playerPlaceholders.add(new PlayerPlaceholder("%canseestaffonline%", 2000) {
			public String get(ITabPlayer p) {
				int var = 0;
				for (ITabPlayer all : Shared.getPlayers()){
					if (all.isStaff() && (((TabPlayer)p).player).canSee(((TabPlayer)all).player)) var++;
				}
				return var+"";
			}
		});
		if (Bukkit.getPluginManager().isPluginEnabled("Vault") && PluginHooks.Vault_chat != null) {
			Placeholders.playerPlaceholders.add(new PlayerPlaceholder("%vault-prefix%", 1000) {

				public String get(ITabPlayer p) {
					String prefix = ((Chat)PluginHooks.Vault_chat).getPlayerPrefix(((TabPlayer)p).player);
					return prefix != null ? prefix : "";
				}
			});
			Placeholders.playerPlaceholders.add(new PlayerPlaceholder("%vault-suffix%", 1000) {

				public String get(ITabPlayer p) {
					String suffix = ((Chat)PluginHooks.Vault_chat).getPlayerSuffix(((TabPlayer)p).player);
					return suffix != null ? suffix : "";
				}
			});
		} else {
			Placeholders.constants.add(new Constant("%vault-prefix%") {
				public String get() {
					return "";
				}
			});
			Placeholders.constants.add(new Constant("%vault-suffix%") {
				public String get() {
					return "";
				}
			});
		}
		Placeholders.constants.add(new Constant("%maxplayers%") {
			public String get() {
				return Bukkit.getMaxPlayers()+"";
			}
		});
	}
	@SuppressWarnings("unchecked")
	public void registerCooldowns() {
		Object serverPlaceholderCooldowns = Configs.config.get("papi-placeholder-cooldowns.server");
		if (serverPlaceholderCooldowns != null) {
			for (Entry<String, Integer> entry : ((Map<String,Integer>)serverPlaceholderCooldowns).entrySet()) {
				String placeholder = "%" + entry.getKey() + "%";
				int cooldown = entry.getValue();
				Placeholders.serverPlaceholders.add(new ServerPlaceholder(placeholder, cooldown, "PlaceholderAPI[" + entry.getKey() + " - " + cooldown + "]") {
					public String get() {
						return PluginHooks.PlaceholderAPI_setPlaceholders(null, placeholder, new String[] {placeholder}, false);
					}
				});
			}
		}
		Object playerPlaceholderCooldowns = Configs.config.get("papi-placeholder-cooldowns.player");
		if (playerPlaceholderCooldowns != null) {
			for (Entry<String, Integer> entry : ((Map<String,Integer>)playerPlaceholderCooldowns).entrySet()) {
				String placeholder = "%" + entry.getKey() + "%";
				int cooldown = entry.getValue();
				Placeholders.playerPlaceholders.add(new PlayerPlaceholder(placeholder, cooldown, "PlaceholderAPI[" + entry.getKey() + " - " + cooldown + "]") {
					public String get(ITabPlayer p) {
						return PluginHooks.PlaceholderAPI_setPlaceholders(p, placeholder, new String[] {placeholder}, false);
					}
				});
			}
		}
	}
	@SuppressWarnings("unchecked")
	public static Player[] getOnlinePlayers() throws Exception {
		Object players = Bukkit.class.getMethod("getOnlinePlayers").invoke(null);
		if (players instanceof Player[]) {
			//1.5.x - 1.6.x
			return (Player[]) players;
		} else {
			//1.7+
			return ((Collection<Player>)players).toArray(new Player[0]); 
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
	
	public void sendConsoleMessage(String message) {
		Bukkit.getConsoleSender().sendMessage(message);
	}
	public String getPermissionPlugin() {
		if (PluginHooks.permissionsEx) return "PermissionsEx";
		if (PluginHooks.groupManager != null) return "GroupManager";
		if (PluginHooks.luckPerms) return "LuckPerms";
		if (PluginHooks.Vault_permission != null) return PluginHooks.Vault_getPermissionPlugin() + " (detected by Vault)";
		return "Unknown/None";
	}
	public void reload(ITabPlayer sender) {
		Shared.unload();
		load(true, false);
		if (!Shared.disabled) TabCommand.sendMessage(sender, Configs.reloaded);
	}
	public Object buildPacket(UniversalPacketPlayOut packet, ProtocolVersion protocolVersion) throws Exception {
		return packet.toNMS(protocolVersion);
	}
	public void loadConfig() throws Exception {
		Configs.config = new ConfigurationFile("bukkitconfig.yml", "config.yml", Configs.configComments);
		boolean changeNameTag = Configs.config.getBoolean("change-nametag-prefix-suffix", true);
		NameTag16.refresh = NameTagX.refresh = (Configs.config.getInt("nametag-refresh-interval-ticks", 20)*50);
		Playerlist.refresh = (Configs.config.getInt("tablist-refresh-interval-ticks", 20)*50);
		boolean unlimitedTags = Configs.config.getBoolean("unlimited-nametag-prefix-suffix-mode.enabled", false);
		Configs.modifyNPCnames = Configs.config.getBoolean("unlimited-nametag-prefix-suffix-mode.modify-npc-names", true);
		HeaderFooter.refresh = (Configs.config.getInt("header-footer-refresh-interval-ticks", 1)*50);
		//resetting booleans if this is a plugin reload to avoid chance of both modes being loaded at the same time
		NameTagX.enable = false;
		NameTag16.enable = false;
		Configs.unlimitedTags = false;
		if (changeNameTag) {
			Configs.unlimitedTags = unlimitedTags;
			if (unlimitedTags && ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
				NameTagX.enable = true;
			} else {
				NameTag16.enable = true;
			}
		}
		String objective = Configs.config.getString("tablist-objective", "PING");
		try{
			TabObjective.type = TabObjectiveType.valueOf(objective.toUpperCase());
		} catch (Throwable e) {
			Shared.startupWarn("\"§e" + objective + "§c\" is not a valid type of tablist-objective. Valid options are: §ePING, HEARTS, CUSTOM §cand §eNONE §cfor disabling the feature.");
			TabObjective.type = TabObjectiveType.NONE;
		}
		TabObjective.rawValue = Configs.config.getString("tablist-objective-custom-value", "%ping%");
		if (TabObjective.type == TabObjectiveType.PING) TabObjective.rawValue = "%ping%";
		if (TabObjective.type == TabObjectiveType.HEARTS) TabObjective.rawValue = "%health%";
		BelowName.enable = Configs.config.getBoolean("belowname.enabled", true);
		BelowName.refresh = 50*Configs.config.getInt("belowname.refresh-interval-ticks", 5);
		BelowName.number = Configs.config.getString("belowname.number", "%health%");
		BelowName.text = Configs.config.getString("belowname.text", "Health");
		Configs.noFaction = Configs.config.getString("placeholders.faction-no", "&2Wilderness");
		Configs.yesFaction = Configs.config.getString("placeholders.faction-yes", "<%value%>");
		Configs.noTag = Configs.config.getString("placeholders.deluxetag-no", "&oNo Tag :(");
		Configs.yesTag = Configs.config.getString("placeholders.deluxetag-yes", "< %value% >");
		Configs.noAfk = Configs.config.getString("placeholders.afk-no", "");
		Configs.yesAfk = Configs.config.getString("placeholders.afk-yes", " &4*&4&lAFK&4*&r");
		Configs.removeStrings = Configs.config.getStringList("placeholders.remove-strings", Arrays.asList("[] ", "< > "));
		Configs.advancedconfig = new ConfigurationFile("advancedconfig.yml", Configs.advancedconfigComments);
		PerWorldPlayerlist.enabled = Configs.advancedconfig.getBoolean("per-world-playerlist", false);
		PerWorldPlayerlist.allowBypass = Configs.advancedconfig.getBoolean("allow-pwp-bypass-permission", false);
		PerWorldPlayerlist.ignoredWorlds = Configs.advancedconfig.getList("ignore-pwp-in-worlds", Arrays.asList("ignoredworld", "spawn"));
		Configs.sortByPermissions = Configs.advancedconfig.getBoolean("sort-players-by-permissions", false);
		Configs.fixPetNames = Configs.advancedconfig.getBoolean("fix-pet-names", false);
		Configs.usePrimaryGroup = Configs.advancedconfig.getBoolean("use-primary-group", true);
		Configs.primaryGroupFindingList = Configs.advancedconfig.getList("primary-group-finding-list", Arrays.asList("Owner", "Admin", "Helper", "default"));
	}
}