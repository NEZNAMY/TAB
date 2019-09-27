package me.neznamy.tab.platforms.bukkit;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.Callable;

import org.anjocaido.groupmanager.GroupManager;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;
import org.yaml.snakeyaml.parser.ParserException;

import com.earth2me.essentials.Essentials;
import com.google.common.collect.Lists;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.entity.MPlayer;

import ch.soolz.xantiafk.xAntiAFKAPI;
import de.robingrether.idisguise.api.DisguiseAPI;
import me.clip.deluxetags.DeluxeTag;
import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.platforms.bukkit.unlimitedtags.NameTagLineManager;
import me.neznamy.tab.platforms.bukkit.unlimitedtags.NameTagX;
import me.neznamy.tab.premium.ScoreboardManager;
import me.neznamy.tab.shared.*;
import me.neznamy.tab.shared.Shared.Feature;
import me.neznamy.tab.shared.TabObjective.TabObjectiveType;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardTeam;
import me.neznamy.tab.shared.packets.UniversalPacketPlayOut;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

public class Main extends JavaPlugin implements Listener, MainClass{

	public static GroupManager groupManager;
	public static boolean luckPerms;
	public static boolean pex;
	public static Main instance;
	public static boolean disabled = false;
	public static Essentials essentials;
	public static Economy economy;
	public static Permission perm;
	public static boolean libsdisguises;
	public static DisguiseAPI idisguise;

	public void onEnable(){
		ProtocolVersion.SERVER_VERSION = ProtocolVersion.fromServerString(Bukkit.getBukkitVersion().split("-")[0]);
		ProtocolVersion.packageName = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
		Shared.init(this);
		Shared.print("§7", "Server version: " + Bukkit.getBukkitVersion().split("-")[0] + " (" + ProtocolVersion.packageName + ")");
		if (ProtocolVersion.SERVER_VERSION.isSupported()){
			long total = System.currentTimeMillis();
			instance = this;
			Bukkit.getPluginManager().registerEvents(this, this);
			Bukkit.getPluginCommand("tab").setExecutor(new CommandExecutor() {
				public boolean onCommand(CommandSender sender, Command c, String cmd, String[] args){
					TabCommand.execute(sender instanceof Player ? Shared.getPlayer(sender.getName()) : null, args);
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
					return Placeholders.placeholderAPI ? "Yes" : "No";
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
			if (!disabled) Shared.print("§a", "Enabled in " + (System.currentTimeMillis()-total) + "ms");
		} else {
			sendConsoleMessage("§c[TAB] Your server version (" + ProtocolVersion.SERVER_VERSION.getFriendlyName() + ") is not supported. Disabling..");
			Bukkit.getPluginManager().disablePlugin(this);
		}
	}
	public void onDisable() {
		if (!disabled) {
			for (ITabPlayer p : Shared.getPlayers()) {
				if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
					Injector.uninject(p.getUniqueId());
				} else if (ProtocolVersion.SERVER_VERSION.getMinorVersion() == 7) {
					Injector1_7.uninject(p.getUniqueId());
				}
			}
			unload();
		}
	}
	public void unload() {
		try {
			if (disabled) return;
			long time = System.currentTimeMillis();
			Shared.cancelAllTasks();
			Configs.animations = new ArrayList<Animation>();
			PerWorldPlayerlist.unload();
			HeaderFooter.unload();
			TabObjective.unload();
			BelowName.unload();
			Playerlist.unload();
			NameTag16.unload();
			NameTagX.unload();
			BossBar.unload();
			ScoreboardManager.unload();
			Shared.data.clear();
			if (Placeholders.placeholderAPI) PlaceholderAPIExpansion.unregister();
			Shared.print("§a", "Disabled in " + (System.currentTimeMillis()-time) + "ms");
		} catch (Throwable e) {
			Shared.error("Failed to unload the plugin", e);
		}
	}
	public void load(boolean broadcastTime, boolean inject) {
		try {
			long time = System.currentTimeMillis();
			disabled = false;
			Shared.startupWarns = 0;
			Configs.loadFiles();
			registerPlaceholders();
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
		} catch (ParserException e) {
			Shared.print("§c", "Did not enable due to a broken configuration file.");
			disabled = true;
		} catch (Throwable e) {
			Shared.print("§c", "Failed to enable");
			sendConsoleMessage("§c" + e.getClass().getName() +": " + e.getMessage());
			for (StackTraceElement ste : e.getStackTrace()) {
				sendConsoleMessage("§c       at " + ste.toString());
			}
			disabled = true;
		}
	}
	@EventHandler(priority = EventPriority.LOWEST)
	public void a(PlayerJoinEvent e) {
		try {
			if (disabled) return;
			ITabPlayer p = new TabPlayer(e.getPlayer());
			Shared.data.put(e.getPlayer().getUniqueId(), p);
			try {
				inject(e.getPlayer().getUniqueId());
			} catch (NoSuchElementException ignored) {
				Shared.error("Failed to inject player " + e.getPlayer().getName() + " (online=" + e.getPlayer().isOnline() + ") - " + ignored.getClass().getSimpleName() +": " + ignored.getMessage());
			}
			ITabPlayer pl = p;
			PerWorldPlayerlist.trigger(e.getPlayer());
			Shared.runTask("player joined the server", Feature.OTHER, new Runnable() {

				public void run() {
					Placeholders.recalculateOnlineVersions();
					HeaderFooter.playerJoin(pl);
					TabObjective.playerJoin(pl);
					BelowName.playerJoin(pl);
					NameTag16.playerJoin(pl);
					NameTagX.playerJoin(pl);
					BossBar.playerJoin(pl);
					ScoreboardManager.register(pl);
				}
			});
		} catch (Throwable ex) {
			Shared.error("An error occured when player joined the server", ex);
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public void a(PlayerQuitEvent e){
		try {
			if (disabled) return;
			ITabPlayer disconnectedPlayer = Shared.getPlayer(e.getPlayer().getUniqueId());
			if (disconnectedPlayer == null) {
				Shared.error("Data of " + disconnectedPlayer + " did not exist when player left");
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
			Shared.data.remove(e.getPlayer().getUniqueId());
		} catch (Throwable t) {
			Shared.error("An error occured when player left server", t);
		}
	}
	@EventHandler(priority = EventPriority.LOWEST)
	public void a(PlayerChangedWorldEvent e){
		try {
			if (disabled) return;
			ITabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
			if (p == null) return;
			PerWorldPlayerlist.trigger(e.getPlayer());
			String from = e.getFrom().getName();
			String to = p.world = e.getPlayer().getWorld().getName();
			p.onWorldChange(from, to);
		} catch (Throwable ex) {
			Shared.error("An error occured when processing PlayerChangedWorldEvent", ex);
		}
	}
	@EventHandler
	public void a(PlayerCommandPreprocessEvent e) {
		if (disabled) return;
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
	public Object createComponent(String text) {
		if (text == null) return null;
		if (text.length() == 0) return MethodAPI.getInstance().ICBC_fromString("{\"translate\":\"\"}");
		JSONObject object = new JSONObject();
		object.put("text", text);
		return MethodAPI.getInstance().ICBC_fromString(object.toString());
	}
	public void sendConsoleMessage(String message) {
		Bukkit.getConsoleSender().sendMessage(message);
	}
	public String getPermissionPlugin() {
		if (pex) return "PermissionsEx";
		if (groupManager != null) return "GroupManager";
		if (luckPerms) return "LuckPerms";
		if (perm != null) return perm.getName() + " (detected by Vault)";
		return "Unknown/None";
	}
	public String getSeparatorType() {
		return "world";
	}
	public boolean isDisabled() {
		return disabled;
	}
	public void reload(ITabPlayer sender) {
		unload();
		load(true, false);
		if (!disabled) TabCommand.sendMessage(sender, Configs.reloaded);
	}
	@SuppressWarnings("unchecked")
	public boolean killPacket(Object packetPlayOutScoreboardTeam) throws Exception{
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
	public Object toNMS(UniversalPacketPlayOut packet, ProtocolVersion protocolVersion) throws Exception {
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
		Configs.removeStrings = Configs.config.getStringList("placeholders.remove-strings", Lists.newArrayList("[] ", "< > "));
		Configs.advancedconfig = new ConfigurationFile("advancedconfig.yml", Configs.advancedconfigComments);
		PerWorldPlayerlist.enabled = Configs.advancedconfig.getBoolean("per-world-playerlist", false);
		PerWorldPlayerlist.allowBypass = Configs.advancedconfig.getBoolean("allow-pwp-bypass-permission", false);
		PerWorldPlayerlist.ignoredWorlds = Configs.advancedconfig.getList("ignore-pwp-in-worlds", Lists.newArrayList("ignoredworld", "spawn"));
		Configs.sortByPermissions = Configs.advancedconfig.getBoolean("sort-players-by-permissions", false);
		Configs.fixPetNames = Configs.advancedconfig.getBoolean("fix-pet-names", false);
		Configs.usePrimaryGroup = Configs.advancedconfig.getBoolean("use-primary-group", true);
		Configs.primaryGroupFindingList = Configs.advancedconfig.getList("primary-group-finding-list", Lists.newArrayList("Owner", "Admin", "Helper", "default"));
	}
	public static void registerPlaceholders() {
		if (Bukkit.getPluginManager().isPluginEnabled("Vault")){
			RegisteredServiceProvider<Economy> rsp1 = Bukkit.getServicesManager().getRegistration(Economy.class);
			if (rsp1 != null) economy = rsp1.getProvider();
			RegisteredServiceProvider<Permission> rsp2 = Bukkit.getServicesManager().getRegistration(Permission.class);
			if (rsp2 != null) perm = rsp2.getProvider();
		}
		if (Bukkit.getPluginManager().isPluginEnabled("iDisguise")) {
			idisguise = Bukkit.getServicesManager().getRegistration(DisguiseAPI.class).getProvider();
		}
		luckPerms = Bukkit.getPluginManager().isPluginEnabled("LuckPerms");
		groupManager = (GroupManager) Bukkit.getPluginManager().getPlugin("GroupManager");
		Placeholders.placeholderAPI = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
		if (Placeholders.placeholderAPI) PlaceholderAPIExpansion.register();
		pex = Bukkit.getPluginManager().isPluginEnabled("PermissionsEx");
		libsdisguises = Bukkit.getPluginManager().isPluginEnabled("LibsDisguises");
		essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");

		Placeholders.playerPlaceholders = new ArrayList<Placeholder>();
		Placeholders.serverPlaceholders = new ArrayList<Placeholder>();

		Shared.registerUniversalPlaceholders();

		Placeholders.playerPlaceholders.add(new Placeholder("%money%") {
			public String get(ITabPlayer p) {
				return p.getMoney();
			}
		});
		Placeholders.playerPlaceholders.add(new Placeholder("%xPos%") {
			public String get(ITabPlayer p) {
				return (((TabPlayer)p).player).getLocation().getBlockX()+"";
			}
		});
		Placeholders.playerPlaceholders.add(new Placeholder("%yPos%") {
			public String get(ITabPlayer p) {
				return (((TabPlayer)p).player).getLocation().getBlockY()+"";
			}
		});
		Placeholders.playerPlaceholders.add(new Placeholder("%zPos%") {
			public String get(ITabPlayer p) {
				return (((TabPlayer)p).player).getLocation().getBlockZ()+"";
			}
		});
		Placeholders.playerPlaceholders.add(new Placeholder("%displayname%") {
			public String get(ITabPlayer p) {
				return (((TabPlayer)p).player).getDisplayName();
			}
		});
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 7) Placeholders.playerPlaceholders.add(new Placeholder("%deaths%") {
			public String get(ITabPlayer p) {
				return (((TabPlayer)p).player).getStatistic(Statistic.DEATHS)+"";
			}
		});
		Placeholders.playerPlaceholders.add(new Placeholder("%essentialsnick%") {
			public String get(ITabPlayer p) {
				return p.getNickname();
			}
		});
		if (Bukkit.getPluginManager().isPluginEnabled("DeluxeTags")) {
			Placeholders.playerPlaceholders.add(new Placeholder("%deluxetag%") {
				public String get(ITabPlayer p) {
					String tag = DeluxeTag.getPlayerDisplayTag(((TabPlayer)p).player);
					if (tag == null || tag.equals("")) {
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
		Placeholders.playerPlaceholders.add(new Placeholder("%faction%") {

			public String factionsType;
			public boolean factionsInitialized;

			public String get(ITabPlayer p) {
				if (!factionsInitialized) {
					try {
						Class.forName("com.massivecraft.factions.FPlayers");
						factionsType = "UUID";
					} catch (Throwable e) {}
					try {
						Class.forName("com.massivecraft.factions.entity.MPlayer");
						factionsType = "MCore";
					} catch (Throwable e) {}
					factionsInitialized = true;
				}
				if (factionsType == null) return Configs.noFaction;
				String name = null;
				if (factionsType.equals("UUID")) name = FPlayers.getInstance().getByPlayer(((TabPlayer)p).player).getFaction().getTag();
				if (factionsType.equals("MCore")) name = MPlayer.get(((TabPlayer)p).player).getFactionName();
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
		Placeholders.playerPlaceholders.add(new Placeholder("%health%") {
			public String get(ITabPlayer p) {
				return p.getHealth()+"";
			}
		});
		Placeholders.serverPlaceholders.add(new Placeholder("%tps%") {
			public String get(ITabPlayer p) {
				return Shared.round(Math.min(MethodAPI.getInstance().getTPS(), 20));
			}
		});
		if (Bukkit.getPluginManager().isPluginEnabled("xAntiAFK")) {
			Placeholders.playerPlaceholders.add(new Placeholder("%afk%") {
				public String get(ITabPlayer p) {
					return xAntiAFKAPI.isAfk(((TabPlayer)p).player)?Configs.yesAfk:Configs.noAfk;
				}
				@Override
				public String[] getChilds(){
					return new String[] {Configs.yesAfk, Configs.noAfk};
				}
			});
		} else if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
			Placeholders.playerPlaceholders.add(new Placeholder("%afk%") {

				private Essentials essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");

				public String get(ITabPlayer p) {
					boolean afk = (essentials.getUser(p.getUniqueId()) != null && essentials.getUser(p.getUniqueId()).isAfk());
					return afk?Configs.yesAfk:Configs.noAfk;
				}
				@Override
				public String[] getChilds(){
					return new String[] {Configs.yesAfk, Configs.noAfk};
				}
			});
		} else {
			Placeholders.playerPlaceholders.add(new Placeholder("%afk%") {
				public String get(ITabPlayer p) {
					return "";
				}
				@Override
				public String[] getChilds(){
					return new String[] {Configs.yesAfk, Configs.noAfk};
				}
			});
		}
		Placeholders.playerPlaceholders.add(new Placeholder("%canseeonline%") {
			public String get(ITabPlayer p) {
				int var = 0;
				for (ITabPlayer all : Shared.getPlayers()){
					if ((((TabPlayer)p).player).canSee(((TabPlayer)all).player)) var++;
				}
				return var+"";
			}
		});
		Placeholders.playerPlaceholders.add(new Placeholder("%canseestaffonline%") {
			public String get(ITabPlayer p) {
				int var = 0;
				for (ITabPlayer all : Shared.getPlayers()){
					if (all.isStaff() && (((TabPlayer)p).player).canSee(((TabPlayer)all).player)) var++;
				}
				return var+"";
			}
		});
		Placeholders.playerPlaceholders.add(new Placeholder("%vault-prefix%") {

			private boolean vault = Bukkit.getPluginManager().isPluginEnabled("Vault");
			private RegisteredServiceProvider<Chat> rsp = vault ? Bukkit.getServicesManager().getRegistration(Chat.class) : null;
			private Chat chat = rsp != null ? rsp.getProvider() : null;

			public String get(ITabPlayer p) {
				if (chat != null) {
					String prefix = chat.getPlayerPrefix(((TabPlayer)p).player);
					return prefix != null ? prefix : "";
				}
				return "";
			}
		});
		Placeholders.playerPlaceholders.add(new Placeholder("%vault-suffix%") {

			private boolean vault = Bukkit.getPluginManager().isPluginEnabled("Vault");
			private RegisteredServiceProvider<Chat> rsp = vault ? Bukkit.getServicesManager().getRegistration(Chat.class) : null;
			private Chat chat = rsp != null ? rsp.getProvider() : null;

			public String get(ITabPlayer p) {
				if (chat != null) {
					String suffix = chat.getPlayerSuffix(((TabPlayer)p).player);
					return suffix != null ? suffix : "";
				}
				return "";
			}
		});
		Placeholders.serverPlaceholders.add(new Placeholder("%maxplayers%") {
			public String get(ITabPlayer p) {
				return Bukkit.getMaxPlayers()+"";
			}
		});
	}
	public static Player[] getOnlinePlayers() {
		try {
			Method onlinePlayersMethod = Class.forName("org.bukkit.Server").getMethod("getOnlinePlayers");
			return onlinePlayersMethod.getReturnType().equals(Collection.class)
					? ((Collection<?>) onlinePlayersMethod.invoke(Bukkit.getServer())).toArray(new Player[0])
							: ((Player[]) onlinePlayersMethod.invoke(Bukkit.getServer()));
		} catch (Exception e) {
			Shared.error("Failed to get players", e);
			return new Player[0];
		}
	}
}