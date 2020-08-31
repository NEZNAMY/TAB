package me.neznamy.tab.platforms.bukkit;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.neznamy.tab.platforms.bukkit.features.BossBar_legacy;
import me.neznamy.tab.platforms.bukkit.features.ExpansionDownloader;
import me.neznamy.tab.platforms.bukkit.features.PerWorldPlayerlist;
import me.neznamy.tab.platforms.bukkit.features.PetFix;
import me.neznamy.tab.platforms.bukkit.features.TabExpansion;
import me.neznamy.tab.platforms.bukkit.features.unlimitedtags.NameTagX;
import me.neznamy.tab.platforms.bukkit.permission.GroupManager;
import me.neznamy.tab.platforms.bukkit.permission.PermissionsEx;
import me.neznamy.tab.platforms.bukkit.permission.Vault;
import me.neznamy.tab.platforms.bukkit.placeholders.BukkitPlaceholderRegistry;
import me.neznamy.tab.premium.AlignedSuffix;
import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.premium.scoreboard.ScoreboardManager;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.PlatformMethods;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.config.ConfigurationFile;
import me.neznamy.tab.shared.config.YamlConfigurationFile;
import me.neznamy.tab.shared.features.BelowName;
import me.neznamy.tab.shared.features.GhostPlayerFix;
import me.neznamy.tab.shared.features.GroupRefresher;
import me.neznamy.tab.shared.features.HeaderFooter;
import me.neznamy.tab.shared.features.NameTag16;
import me.neznamy.tab.shared.features.PlaceholderManager;
import me.neznamy.tab.shared.features.Playerlist;
import me.neznamy.tab.shared.features.SpectatorFix;
import me.neznamy.tab.shared.features.TabObjective;
import me.neznamy.tab.shared.features.UpdateChecker;
import me.neznamy.tab.shared.features.bossbar.BossBar;
import me.neznamy.tab.shared.packets.UniversalPacketPlayOut;
import me.neznamy.tab.shared.permission.LuckPerms;
import me.neznamy.tab.shared.permission.NetworkManager;
import me.neznamy.tab.shared.permission.None;
import me.neznamy.tab.shared.permission.PermissionPlugin;
import me.neznamy.tab.shared.permission.UltraPermissions;
import me.neznamy.tab.shared.placeholders.Placeholders;
import me.neznamy.tab.shared.placeholders.UniversalPlaceholderRegistry;
import net.milkbowl.vault.permission.Permission;

/**
 * Bukkit implementation of PlatformMethods
 */
public class BukkitMethods implements PlatformMethods {

	private Set<String> usedExpansions;
	private JavaPlugin plugin;

	public BukkitMethods(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public PermissionPlugin detectPermissionPlugin() {
		if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
			return new LuckPerms(Bukkit.getPluginManager().getPlugin("LuckPerms").getDescription().getVersion());
		} else if (Bukkit.getPluginManager().isPluginEnabled("PermissionsEx")) {
			return new PermissionsEx();
		} else if (Bukkit.getPluginManager().isPluginEnabled("GroupManager")) {
			return new GroupManager();
		} else if (Bukkit.getPluginManager().isPluginEnabled("UltraPermissions")) {
			return new UltraPermissions();
		} else if (Bukkit.getPluginManager().isPluginEnabled("NetworkManager")) {
			return new NetworkManager(Bukkit.getPluginManager().getPlugin("NetworkManager"));
		} else if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
			return new Vault(Bukkit.getServicesManager().getRegistration(Permission.class).getProvider());
		} else {
			return new None();
		}
	}

	@Override
	public void loadFeatures(boolean inject) throws Exception{
		Main.detectPlugins();
		usedExpansions = new HashSet<String>();
		PlaceholderManager plm = new PlaceholderManager();
		plm.addRegistry(new BukkitPlaceholderRegistry());
		plm.addRegistry(new UniversalPlaceholderRegistry());
		plm.registerPlaceholders();
		Shared.featureManager.registerFeature("placeholders", plm);
		if (Configs.config.getBoolean("change-nametag-prefix-suffix", true)) {
			if (Configs.config.getBoolean("unlimited-nametag-prefix-suffix-mode.enabled", false) && ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
				if (Configs.config.getBoolean("classic-vanilla-belowname.enabled", true)) {
					Shared.errorManager.startupWarn("Both unlimited nametag mode and belowname features are enabled, this will result in the worst combination: belowname objective not appearing on players, only NPCs. Check wiki for more info.");
				}
				Shared.featureManager.registerFeature("nametagx", new NameTagX(plugin));
			} else {
				Shared.featureManager.registerFeature("nametag16", new NameTag16(ProtocolVersion.SERVER_VERSION.getMinorVersion() == 8 || Bukkit.getPluginManager().isPluginEnabled("ViaVersion") || Bukkit.getPluginManager().isPluginEnabled("ProtocolSupport")));
			}
		}
		if (Configs.config.getBoolean("classic-vanilla-belowname.enabled", true)) Shared.featureManager.registerFeature("belowname", new BelowName());
		if (Configs.BossBarEnabled) {
			BossBar bb = new BossBar();
			Shared.featureManager.registerFeature("bossbar", bb);
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() < 9) Shared.featureManager.registerFeature("bossbar1.8", new BossBar_legacy(bb, plugin));
		}
		if (Configs.config.getBoolean("enable-header-footer", true)) Shared.featureManager.registerFeature("headerfooter", new HeaderFooter());
		if (Configs.config.getString("yellow-number-in-tablist", "%ping%").length() > 0) 												Shared.featureManager.registerFeature("tabobjective", new TabObjective());
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8 && Configs.config.getBoolean("change-tablist-prefix-suffix", true)) 	{
			Playerlist playerlist = new Playerlist();
			Shared.featureManager.registerFeature("playerlist", playerlist);
			if (Premium.alignTabsuffix) Shared.featureManager.registerFeature("alignedsuffix", new AlignedSuffix(playerlist));

		}
		int version = ProtocolVersion.SERVER_VERSION.getMinorVersion();
		//on 1.16 server cats and parrots do not listen to sit/stand commands, but dogs do
		//this is probably caused by 1.16 server requiring additional packets from client which are not sent when player does not see correct data
		//disabling the feature until the issue is resolved
		if (version >= 9 && version < 16 && Configs.advancedconfig.getBoolean("fix-pet-names", false)) 		Shared.featureManager.registerFeature("petfix", new PetFix());
		if (Configs.config.getBoolean("do-not-move-spectators", false)) 									Shared.featureManager.registerFeature("spectatorfix", new SpectatorFix());
		if (Premium.is() && Premium.premiumconfig.getBoolean("scoreboard.enabled", false)) 					Shared.featureManager.registerFeature("scoreboard", new ScoreboardManager());
		if (Configs.advancedconfig.getBoolean("per-world-playerlist.enabled", false)) 						Shared.featureManager.registerFeature("pwp", new PerWorldPlayerlist(plugin));
		if (Configs.SECRET_remove_ghost_players) 															Shared.featureManager.registerFeature("ghostplayerfix", new GhostPlayerFix());
		if (PluginHooks.placeholderAPI) {
			new TabExpansion(plugin);
			new ExpansionDownloader(plugin).download(usedExpansions);
		}
		new GroupRefresher();
		new UpdateChecker();

		for (Player p : getOnlinePlayers()) {
			ITabPlayer t = new TabPlayer(p);
			Shared.data.put(p.getUniqueId(), t);
			Shared.entityIdMap.put(p.getEntityId(), t);
			if (inject) Main.inject(t.getUniqueId());
		}
	}

	@SuppressWarnings("unchecked")
	private Player[] getOnlinePlayers() throws Exception {
		Object players = Bukkit.class.getMethod("getOnlinePlayers").invoke(null);
		if (players instanceof Player[]) {
			//1.5.x - 1.7.x
			return (Player[]) players;
		} else {
			//1.8+
			return ((Collection<Player>)players).toArray(new Player[0]); 
		}
	}

	@Override
	public void sendConsoleMessage(String message, boolean translateColors) {
		if (translateColors) message = Placeholders.color(message);
		Bukkit.getConsoleSender().sendMessage(message);
	}

	@Override
	public Object buildPacket(UniversalPacketPlayOut packet, ProtocolVersion protocolVersion) throws Exception {
		return packet.toNMS(protocolVersion);
	}

	@Override
	public void loadConfig() throws Exception {
		Configs.config = new YamlConfigurationFile(getDataFolder(), "bukkitconfig.yml", "config.yml", Arrays.asList("# Detailed explanation of all options available at https://github.com/NEZNAMY/TAB/wiki/config.yml", ""));
		Configs.noAfk = Configs.config.getString("placeholders.afk-no", "");
		Configs.yesAfk = Configs.config.getString("placeholders.afk-yes", " &4*&4&lAFK&4*&r");
		Configs.advancedconfig = new YamlConfigurationFile(getDataFolder(), "advancedconfig.yml", Arrays.asList("# Detailed explanation of all options available at https://github.com/NEZNAMY/TAB/wiki/advancedconfig.yml", ""));
		Configs.usePrimaryGroup = Configs.advancedconfig.getBoolean("use-primary-group", true);
		Configs.primaryGroupFindingList = Configs.advancedconfig.getStringList("primary-group-finding-list", Arrays.asList("Owner", "Admin", "Helper", "default"));
		Configs.groupsByPermissions = Configs.advancedconfig.getBoolean("assign-groups-by-permissions", false);
	}

	@Override
	public void registerUnknownPlaceholder(String identifier) {
		if (identifier.contains("_")) {
			String plugin = identifier.split("_")[0].replace("%", "").toLowerCase();
			if (plugin.equals("some")) return;
			if (!usedExpansions.contains(plugin) && !plugin.equals("rel")) {
				usedExpansions.add(plugin);
			}
			PlaceholderManager.getInstance().registerPAPIPlaceholder(identifier);
		}
	}

	@Override
	public void convertConfig(ConfigurationFile config) {
		if (config.getName().equals("config.yml")) {
			removeOld(config, "nametag-refresh-interval-ticks");
			removeOld(config, "tablist-refresh-interval-ticks");
			removeOld(config, "header-footer-refresh-interval-ticks");
			removeOld(config, "belowname.refresh-interval-ticks");
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
			removeOld(config, "nametag-refresh-interval-milliseconds");
			removeOld(config, "tablist-refresh-interval-milliseconds");
			removeOld(config, "header-footer-refresh-interval-milliseconds");
			removeOld(config, "classic-vanilla-belowname.refresh-interval-milliseconds");
			removeOld(config, "relational-placeholders-refresh");
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
			rename(config, "papi-placeholder-cooldowns", "placeholderapi-refresh-intervals");
			if (!config.hasConfigOption("placeholderapi-refresh-intervals")) {
				Map<String, Object> map = new LinkedHashMap<String, Object>();
				map.put("default-refresh-interval", 100);
				Map<String, Integer> server = new HashMap<String, Integer>();
				server.put("%server_uptime%", 1000);
				server.put("%server_tps_1_colored%", 1000);
				map.put("server", server);
				Map<String, Integer> player = new HashMap<String, Integer>();
				player.put("%player_health%", 200);
				player.put("%player_ping%", 1000);
				player.put("%vault_prefix%", 1000);
				map.put("player", player);
				Map<String, Integer> relational = new HashMap<String, Integer>();
				relational.put("%rel_factionsuuid_relation_color%", 500);
				map.put("relational", relational);
				config.set("placeholderapi-refresh-intervals", map);
				Shared.print('2', "Added new missing \"placeholderapi-refresh-intervals\" config.yml section.");
			}
			rename(config, "safe-team-register", "unregister-before-register");
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
		if (config.getName().equals("advancedconfig.yml") && config.getObject("per-world-playerlist") instanceof Boolean) {
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
		if (config.getName().equals("bossbar.yml")) {
			removeOld(config, "refresh-interval-milliseconds");
		}
	}

	@Override
	public String getServerVersion() {
		return Bukkit.getBukkitVersion().split("-")[0] + " (" + Main.serverPackage + ")";
	}

	@Override
	public void suggestPlaceholders() {
		//bukkit only
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
		suggestPlaceholderSwitch("%uperms_prefix%", "%vault-prefix%");
		suggestPlaceholderSwitch("%uperms_suffix%", "%vault-suffix%");
		suggestPlaceholderSwitch("%vault_eco_balance%", "%money%");
		suggestPlaceholderSwitch("%vault_prefix%", "%vault-prefix%");
		suggestPlaceholderSwitch("%vault_rank%", "%rank%");
		suggestPlaceholderSwitch("%vault_suffix%", "%vault-suffix%");

		//both
		suggestPlaceholderSwitch("%player_ping%", "%ping%");
		suggestPlaceholderSwitch("%cmi_user_ping%", "%ping%");
		suggestPlaceholderSwitch("%viaversion_player_protocol_version%", "%player-version%");
		suggestPlaceholderSwitch("%player_name%", "%nick%");
		suggestPlaceholderSwitch("%uperms_rank%", "%rank%");
	}

	@Override
	public String getSeparatorType() {
		return "world";
	}

	@Override
	public File getDataFolder() {
		return plugin.getDataFolder();
	}
}