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
import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;
import me.neznamy.tab.platforms.bukkit.permission.Vault;
import me.neznamy.tab.platforms.bukkit.placeholders.BukkitPlaceholderRegistry;
import me.neznamy.tab.shared.Platform;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.ConfigurationFile;
import me.neznamy.tab.shared.features.NameTag16;
import me.neznamy.tab.shared.features.PlaceholderManager;
import me.neznamy.tab.shared.features.bossbar.BossBar;
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
	private NMSStorage nms;

	public BukkitPlatform(JavaPlugin plugin, NMSStorage nms) {
		this.plugin = plugin;
		this.nms = nms;
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
		TAB tab = TAB.getInstance();
		usedExpansions = new HashSet<String>();
		tab.getPlaceholderManager().addRegistry(new BukkitPlaceholderRegistry(plugin));
		tab.getPlaceholderManager().addRegistry(new UniversalPlaceholderRegistry());
		tab.getPlaceholderManager().registerPlaceholders();
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
			tab.getFeatureManager().registerFeature("injection", new BukkitPipelineInjector(tab, nms));
		}
		if (tab.getConfiguration().config.getBoolean("change-nametag-prefix-suffix", true)) {
			if (tab.getConfiguration().config.getBoolean("unlimited-nametag-prefix-suffix-mode.enabled", false) && ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
				if (tab.getConfiguration().config.getBoolean("classic-vanilla-belowname.enabled", true)) {
					tab.getErrorManager().startupWarn("Both unlimited nametag mode and belowname features are enabled, this will result in the worst combination: belowname objective not appearing on players, only NPCs. Check wiki for more info.");
				}
				tab.getFeatureManager().registerFeature("nametagx", new NameTagX(plugin, nms, tab));
			} else {
				tab.getFeatureManager().registerFeature("nametag16", new NameTag16(tab));
			}
		}
		loadUniversalFeatures();
		if (tab.getConfiguration().BossBarEnabled) {
			BossBar bb = new BossBar(tab);
			tab.getFeatureManager().registerFeature("bossbar", bb);
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() < 9) tab.getFeatureManager().registerFeature("bossbar1.8", new BossBar_legacy(bb, tab));
		}
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9 && tab.getConfiguration().config.getBoolean("fix-pet-names", false)) tab.getFeatureManager().registerFeature("petfix", new PetFix(nms));
		if (tab.getConfiguration().config.getBoolean("per-world-playerlist.enabled", false)) tab.getFeatureManager().registerFeature("pwp", new PerWorldPlayerlist(plugin, tab));
		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			new TabExpansion(plugin);
			new ExpansionDownloader(plugin).download(usedExpansions);
		}

		for (Player p : getOnlinePlayers()) {
			BukkitTabPlayer t = new BukkitTabPlayer(p);
			tab.data.put(p.getUniqueId(), t);
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
		Bukkit.getConsoleSender().sendMessage(translateColors ? message.replace('&', '\u00a7') : message);
	}

	@Override
	public void registerUnknownPlaceholder(String identifier) {
		if (identifier.contains("_")) {
			PlaceholderManager pl = TAB.getInstance().getPlaceholderManager();
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
				registerPlayerPlaceholder(identifier, pl.defaultRefresh);
			}
		}
	}
	
	private void registerServerPlaceholder(String identifier, int refresh) {
		TAB.getInstance().getPlaceholderManager().registerPlaceholder(new ServerPlaceholder(identifier, refresh){
			
			@Override
			public String get() {
				return setPlaceholders(null, identifier);
			}
		});
	}
	
	private void registerPlayerPlaceholder(String identifier, int refresh) {
		TAB.getInstance().getPlaceholderManager().registerPlaceholder(new PlayerPlaceholder(identifier, refresh) {

			@Override
			public String get(TabPlayer p) {
				return setPlaceholders((Player) p.getPlayer(), identifier);
			}
		});
	}
	
	private void registerRelationalPlaceholder(String identifier, int refresh) {
		TAB.getInstance().getPlaceholderManager().registerPlaceholder(new RelationalPlaceholder(identifier, refresh) {

			@Override
			public String get(TabPlayer viewer, TabPlayer target) {
				if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) return identifier;
				try {
					return PlaceholderAPI.setRelationalPlaceholders((Player) viewer.getPlayer(), (Player) target.getPlayer(), identifier);
				} catch (Throwable t) {
					TAB.getInstance().getErrorManager().printError("PlaceholderAPI v" + Bukkit.getPluginManager().getPlugin("PlaceholderAPI").getDescription().getVersion() + " generated an error when setting relational placeholder " + identifier + " for viewer " + viewer.getName() + " and target " + target.getName(), t, false, TAB.getInstance().getErrorManager().papiErrorLog);
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
			TAB.getInstance().getErrorManager().printError("PlaceholderAPI v" + Bukkit.getPluginManager().getPlugin("PlaceholderAPI").getDescription().getVersion() + " generated an error when setting placeholder " + placeholder + " for player " + playername, t, false, TAB.getInstance().getErrorManager().papiErrorLog);
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
				TAB.getInstance().print('2', "Converted old tablist-objective config option to new yellow-number-in-tablist");
			}
			if (config.getObject("per-world-playerlist") instanceof Boolean) {
				rename(config, "per-world-playerlist", "per-world-playerlist.enabled");
				rename(config, "allow-pwp-bypass-permission", "per-world-playerlist.allow-bypass-permission");
				rename(config, "ignore-pwp-in-worlds", "per-world-playerlist.ignore-effect-in-worlds");
				Map<String, List<String>> sharedWorlds = new HashMap<String, List<String>>();
				sharedWorlds.put("lobby", Arrays.asList("lobby1", "lobby2"));
				sharedWorlds.put("minigames", Arrays.asList("paintball", "bedwars"));
				config.set("per-world-playerlist.shared-playerlist-world-groups", sharedWorlds);
				TAB.getInstance().print('2', "Converted old per-world-playerlist section to new one in advancedconfig.yml.");
			}
		}
	}

	@Override
	public String getServerVersion() {
		return Bukkit.getBukkitVersion().split("-")[0] + " (" + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + ")";
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
		for (Placeholder p : TAB.getInstance().getPlaceholderManager().getAllPlaceholders()) {
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

	@Override
	public int getMaxPlayers() {
		return Bukkit.getMaxPlayers();
	}
}