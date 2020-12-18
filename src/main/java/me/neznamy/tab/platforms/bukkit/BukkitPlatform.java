package me.neznamy.tab.platforms.bukkit;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.clip.placeholderapi.PlaceholderAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.event.BukkitTABLoadEvent;
import me.neznamy.tab.platforms.bukkit.features.BossBar_legacy;
import me.neznamy.tab.platforms.bukkit.features.ExpansionDownloader;
import me.neznamy.tab.platforms.bukkit.features.PerWorldPlayerlist;
import me.neznamy.tab.platforms.bukkit.features.PetFix;
import me.neznamy.tab.platforms.bukkit.features.TabExpansion;
import me.neznamy.tab.platforms.bukkit.features.unlimitedtags.NameTagX;
import me.neznamy.tab.platforms.bukkit.permission.Vault;
import me.neznamy.tab.platforms.bukkit.placeholders.BukkitPlaceholderRegistry;
import me.neznamy.tab.shared.Platform;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.config.ConfigurationFile;
import me.neznamy.tab.shared.config.YamlConfigurationFile;
import me.neznamy.tab.shared.features.NameTag16;
import me.neznamy.tab.shared.features.PlaceholderManager;
import me.neznamy.tab.shared.features.bossbar.BossBar;
import me.neznamy.tab.shared.packets.UniversalPacketPlayOut;
import me.neznamy.tab.shared.permission.LuckPerms;
import me.neznamy.tab.shared.permission.None;
import me.neznamy.tab.shared.permission.PermissionPlugin;
import me.neznamy.tab.shared.permission.UltraPermissions;
import me.neznamy.tab.shared.placeholders.Placeholder;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholder;
import me.neznamy.tab.shared.placeholders.RelationalPlaceholder;
import me.neznamy.tab.shared.placeholders.ServerPlaceholder;
import me.neznamy.tab.shared.placeholders.UniversalPlaceholderRegistry;
import net.milkbowl.vault.permission.Permission;

/**
 * Bukkit implementation of Platform
 */
public class BukkitPlatform implements Platform {

	private Set<String> usedExpansions;
	private JavaPlugin plugin;

	public BukkitPlatform(JavaPlugin plugin) {
		this.plugin = plugin;
		UniversalPacketPlayOut.builder = new BukkitPacketBuilder();
	}

	@Override
	public PermissionPlugin detectPermissionPlugin() {
		if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
			return new LuckPerms(Bukkit.getPluginManager().getPlugin("LuckPerms").getDescription().getVersion());
		} else if (Bukkit.getPluginManager().isPluginEnabled("UltraPermissions")) {
			return new UltraPermissions(Bukkit.getPluginManager().getPlugin("UltraPermissions").getDescription().getVersion());
		} else if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
			return new Vault(Bukkit.getServicesManager().getRegistration(Permission.class).getProvider(), Bukkit.getPluginManager().getPlugin("Vault").getDescription().getVersion());
		} else {
			return new None();
		}
	}

	@Override
	public void loadFeatures() throws Exception {
		usedExpansions = new HashSet<String>();
		PlaceholderManager plm = new PlaceholderManager();
		Shared.featureManager.registerFeature("placeholders", plm);
		plm.addRegistry(new BukkitPlaceholderRegistry(plugin));
		plm.addRegistry(new UniversalPlaceholderRegistry());
		plm.registerPlaceholders();
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
			Shared.featureManager.registerFeature("injection", new BukkitPipelineInjector());
		}
		if (Configs.config.getBoolean("change-nametag-prefix-suffix", true)) {
			if (Configs.config.getBoolean("unlimited-nametag-prefix-suffix-mode.enabled", false) && ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
				if (Configs.config.getBoolean("classic-vanilla-belowname.enabled", true)) {
					Shared.errorManager.startupWarn("Both unlimited nametag mode and belowname features are enabled, this will result in the worst combination: belowname objective not appearing on players, only NPCs. Check wiki for more info.");
				}
				Shared.featureManager.registerFeature("nametagx", new NameTagX(plugin));
			} else {
				Shared.featureManager.registerFeature("nametag16", new NameTag16());
			}
		}
		loadUniversalFeatures();
		if (Configs.BossBarEnabled) {
			BossBar bb = new BossBar();
			Shared.featureManager.registerFeature("bossbar", bb);
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() < 9) Shared.featureManager.registerFeature("bossbar1.8", new BossBar_legacy(bb));
		}
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9 && Configs.config.getBoolean("fix-pet-names", false)) Shared.featureManager.registerFeature("petfix", new PetFix());
		if (Configs.config.getBoolean("per-world-playerlist.enabled", false)) Shared.featureManager.registerFeature("pwp", new PerWorldPlayerlist(plugin));
		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			new TabExpansion(plugin);
			new ExpansionDownloader(plugin).download(usedExpansions);
		}

		for (Player p : getOnlinePlayers()) {
			BukkitTabPlayer t = new BukkitTabPlayer(p);
			Shared.data.put(p.getUniqueId(), t);
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
		Bukkit.getConsoleSender().sendMessage(translateColors ? PlaceholderManager.color(message): message);
	}

	@Override
	public void loadConfig() throws Exception {
		Configs.config = new YamlConfigurationFile(getClass().getClassLoader().getResourceAsStream("bukkitconfig.yml"), new File(getDataFolder(), "config.yml"), Arrays.asList("# Detailed explanation of all options available at https://github.com/NEZNAMY/TAB/wiki/config.yml", ""));
		Configs.noAfk = Configs.config.getString("placeholders.afk-no", "");
		Configs.yesAfk = Configs.config.getString("placeholders.afk-yes", " &4*&4&lAFK&4*&r");
	}

	@Override
	public void registerUnknownPlaceholder(String identifier) {
		if (identifier.contains("_")) {
			PlaceholderManager pl = (PlaceholderManager) Shared.featureManager.getFeature("placeholders");
			if (identifier.startsWith("%rel_")) {
				//relational placeholder
				registerRelationalPlaceholder(identifier, pl.getRelationalRefresh(identifier));
			} else {
				//normal placeholder
				String plugin = identifier.split("_")[0].substring(1).toLowerCase();
				if (plugin.equals("some")) return;
				usedExpansions.add(plugin);
				if (pl.serverPlaceholderRefreshIntervals.containsKey(identifier)) {
					registerServerPlaceholder(identifier, pl.serverPlaceholderRefreshIntervals.get(identifier));
					return;
				}
				if (pl.playerPlaceholderRefreshIntervals.containsKey(identifier)) {
					registerPlayerPlaceholder(identifier, pl.playerPlaceholderRefreshIntervals.get(identifier));
					return;
				}
				if (identifier.startsWith("%server_")) {
					registerServerPlaceholder(identifier, pl.defaultRefresh);
				} else {
					registerPlayerPlaceholder(identifier, pl.defaultRefresh);
				}
			}
		}
	}
	
	private void registerServerPlaceholder(String identifier, int refresh) {
		((PlaceholderManager) Shared.featureManager.getFeature("placeholders")).registerPlaceholder(new ServerPlaceholder(identifier, refresh){
			
			@Override
			public String get() {
				return setPlaceholders(null, identifier);
			}
		});
	}
	
	private void registerPlayerPlaceholder(String identifier, int refresh) {
		((PlaceholderManager) Shared.featureManager.getFeature("placeholders")).registerPlaceholder(new PlayerPlaceholder(identifier, refresh) {

			@Override
			public String get(TabPlayer p) {
				return setPlaceholders((Player) p.getPlayer(), identifier);
			}
		});
	}
	
	private void registerRelationalPlaceholder(String identifier, int refresh) {
		((PlaceholderManager) Shared.featureManager.getFeature("placeholders")).registerPlaceholder(new RelationalPlaceholder(identifier, refresh) {

			@Override
			public String get(TabPlayer viewer, TabPlayer target) {
				if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) return identifier;
				try {
					return PlaceholderAPI.setRelationalPlaceholders((Player) viewer.getPlayer(), (Player) target.getPlayer(), identifier);
				} catch (Throwable t) {
					Shared.errorManager.printError("PlaceholderAPI v" + Bukkit.getPluginManager().getPlugin("PlaceholderAPI").getDescription().getVersion() + " generated an error when setting relational placeholder " + identifier + " for viewer " + viewer.getName() + " and target " + target.getName(), t, false, Configs.papiErrorFile);
				}
				return identifier;
			}
		});
	}
	
	public String setPlaceholders(Player player, String placeholder) {
		if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) return placeholder;
		try {
			return PlaceholderAPI.setPlaceholders(player, placeholder);
		} catch (Throwable t) {
			String playername = (player == null ? "<null>" : player.getName());
			Shared.errorManager.printError("PlaceholderAPI v" + Bukkit.getPluginManager().getPlugin("PlaceholderAPI").getDescription().getVersion() + " generated an error when setting placeholder " + placeholder + " for player " + playername, t, false, Configs.papiErrorFile);
			return "ERROR";
		}
	}

	@Override
	public void convertConfig(ConfigurationFile config) {
		convertUniversalOptions(config);
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
			removeOld(config, "factions-faction");
			removeOld(config, "factions-nofaction");
			removeOld(config, "date-format");
			removeOld(config, "time-format");
			removeOld(config, "relational-placeholders-refresh");
			removeOld(config, "bukkit-bridge-mode");
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
			if (config.getObject("per-world-playerlist") instanceof Boolean) {
				rename(config, "per-world-playerlist", "per-world-playerlist.enabled");
				rename(config, "allow-pwp-bypass-permission", "per-world-playerlist.allow-bypass-permission");
				rename(config, "ignore-pwp-in-worlds", "per-world-playerlist.ignore-effect-in-worlds");
				Map<String, List<String>> sharedWorlds = new HashMap<String, List<String>>();
				sharedWorlds.put("lobby", Arrays.asList("lobby1", "lobby2"));
				sharedWorlds.put("minigames", Arrays.asList("paintball", "bedwars"));
				config.set("per-world-playerlist.shared-playerlist-world-groups", sharedWorlds);
				Shared.print('2', "Converted old per-world-playerlist section to new one in advancedconfig.yml.");
			}
		}
	}

	@Override
	public String getServerVersion() {
		return Bukkit.getBukkitVersion().split("-")[0] + " (" + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + ")";
	}

	@Override
	public void suggestPlaceholders() {
		//bukkit only
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
		suggestPlaceholderSwitch("%player_name%", "%player%");
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
	
	@Override
	public String replaceAllPlaceholders(String string, TabPlayer sender) {
		if (string == null) return null;
		String replaced = string;
		for (Placeholder p : ((PlaceholderManager) Shared.featureManager.getFeature("placeholders")).getAllPlaceholders()) {
			if (replaced.contains(p.getIdentifier())) {
				if (p instanceof ServerPlaceholder) {
					((ServerPlaceholder)p).update();
				}
				if (p instanceof PlayerPlaceholder) {
					((PlayerPlaceholder)p).update(sender);
				}
				replaced = p.set(replaced, sender);
			}
		}
		replaced = setPlaceholders(sender == null ? null : (Player) sender.getPlayer(), replaced);
		return replaced;
	}

	@Override
	public void callLoadEvent() {
		Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(new BukkitTABLoadEvent()));
	}
}