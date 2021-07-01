package me.neznamy.tab.platforms.bukkit;

import java.io.File;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import me.clip.placeholderapi.PlaceholderAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.event.BukkitTABLoadEvent;
import me.neznamy.tab.platforms.bukkit.features.WitherBossBar;
import me.neznamy.tab.platforms.bukkit.features.PerWorldPlayerlist;
import me.neznamy.tab.platforms.bukkit.features.PetFix;
import me.neznamy.tab.platforms.bukkit.features.TabExpansion;
import me.neznamy.tab.platforms.bukkit.features.unlimitedtags.NameTagX;
import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;
import me.neznamy.tab.platforms.bukkit.permission.Vault;
import me.neznamy.tab.shared.Platform;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.BelowName;
import me.neznamy.tab.shared.features.NameTag;
import me.neznamy.tab.shared.features.PingSpoof;
import me.neznamy.tab.shared.features.PlaceholderManager;
import me.neznamy.tab.shared.features.SpectatorFix;
import me.neznamy.tab.shared.features.TabObjective;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardManager;
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
	
	//plugin instance
	private JavaPlugin plugin;
	
	//nms storage
	private NMSStorage nms;
	
	//booleans to check plugin presence
	private Plugin placeholderAPI;
	private boolean viaversion;
	private boolean idisguise;
	private boolean libsdisguises;
	private boolean essentials;

	/**
	 * Constructs new instance with given parameters
	 * @param plugin - plugin instance
	 * @param nms - nms storage
	 */
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
	public void loadFeatures() {
		placeholderAPI = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
		viaversion = Bukkit.getPluginManager().isPluginEnabled("ViaVersion");
		idisguise = Bukkit.getPluginManager().isPluginEnabled("iDisguise");
		libsdisguises = Bukkit.getPluginManager().isPluginEnabled("LibsDisguises");
		essentials = Bukkit.getPluginManager().isPluginEnabled("Essentials");
		TAB tab = TAB.getInstance();
		tab.getPlaceholderManager().addRegistry(new BukkitPlaceholderRegistry(plugin));
		tab.getPlaceholderManager().addRegistry(new UniversalPlaceholderRegistry());
		tab.getPlaceholderManager().registerPlaceholders();
		if (nms.getMinorVersion() >= 8 && tab.getConfiguration().isPipelineInjection()) {
			tab.getFeatureManager().registerFeature("injection", new BukkitPipelineInjector(tab, nms));
		}
		loadNametagFeature(tab);
		loadUniversalFeatures();
		if (tab.getConfiguration().getConfig().getBoolean("ping-spoof.enabled", false)) tab.getFeatureManager().registerFeature("pingspoof", new PingSpoof());
		if (tab.getConfiguration().getConfig().getString("yellow-number-in-tablist", "%ping%").length() > 0) tab.getFeatureManager().registerFeature("tabobjective", new TabObjective(tab));
		if (tab.getConfiguration().getConfig().getBoolean("do-not-move-spectators", false)) tab.getFeatureManager().registerFeature("spectatorfix", new SpectatorFix());
		if (tab.getConfiguration().getConfig().getBoolean("classic-vanilla-belowname.enabled", true)) tab.getFeatureManager().registerFeature("belowname", new BelowName(tab));
		if (tab.getConfiguration().getPremiumConfig() != null && tab.getConfiguration().getPremiumConfig().getBoolean("scoreboard.enabled", false)) tab.getFeatureManager().registerFeature("scoreboard", new ScoreboardManager(tab));
		if (tab.getConfiguration().getBossbarConfig().getBoolean("bossbar-enabled", false) && nms.getMinorVersion() < 9) tab.getFeatureManager().registerFeature("bossbar1.8", new WitherBossBar(tab, plugin));
		if (nms.getMinorVersion() >= 9 && tab.getConfiguration().getConfig().getBoolean("fix-pet-names", false)) tab.getFeatureManager().registerFeature("petfix", new PetFix(nms));
		if (tab.getConfiguration().getConfig().getBoolean("per-world-playerlist.enabled", false)) tab.getFeatureManager().registerFeature("pwp", new PerWorldPlayerlist(plugin, tab));
		if (placeholderAPI != null) {
			new TabExpansion(plugin);
		}
		for (Player p : getOnlinePlayers()) {
			tab.addPlayer(new BukkitTabPlayer(p, Main.getProtocolVersion(p)));
		}
	}
	
	/**
	 * Loads nametag feature from config
	 * @param tab - tab instance
	 */
	private void loadNametagFeature(TAB tab) {
		if (tab.getConfiguration().getConfig().getBoolean("change-nametag-prefix-suffix", true)) {
			if (tab.getConfiguration().getConfig().getBoolean("unlimited-nametag-prefix-suffix-mode.enabled", false) && nms.getMinorVersion() >= 8) {
				tab.getFeatureManager().registerFeature("nametagx", new NameTagX(plugin, nms, tab));
			} else {
				tab.getFeatureManager().registerFeature("nametag16", new NameTag(tab));
			}
		}
	}

	/**
	 * Returns list of online players from Bukkit API
	 * @return list of online players from Bukkit API
	 */
	@SuppressWarnings("unchecked")
	private Player[] getOnlinePlayers() {
		try {
			Object players = Bukkit.class.getMethod("getOnlinePlayers").invoke(null);
			if (players instanceof Player[]) {
				//1.7.x
				return (Player[]) players;
			} else {
				//1.8+
				return ((Collection<Player>)players).toArray(new Player[0]); 
			}
		} catch (Exception e) {
			TAB.getInstance().getErrorManager().printError("Failed to get online players", e);
			return new Player[0];
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
				String expansion = identifier.split("_")[0].substring(1).toLowerCase();
				if (expansion.equals("some")) return;
				if (pl.getServerPlaceholderRefreshIntervals().containsKey(identifier)) {
					registerServerPlaceholder(identifier, pl.getServerPlaceholderRefreshIntervals().get(identifier));
					return;
				}
				if (pl.getPlayerPlaceholderRefreshIntervals().containsKey(identifier)) {
					registerPlayerPlaceholder(identifier, pl.getPlayerPlaceholderRefreshIntervals().get(identifier));
					return;
				}
				registerPlayerPlaceholder(identifier, pl.getDefaultRefresh());
			}
		}
	}
	
	/**
	 * Registers server placeholder
	 * @param identifier - placeholder identifier
	 * @param refresh - refresh interval in milliseconds
	 */
	private void registerServerPlaceholder(String identifier, int refresh) {
		BukkitPlatform pl = this;
		TAB.getInstance().getPlaceholderManager().registerPlaceholder(new ServerPlaceholder(identifier, TAB.getInstance().getErrorManager().fixPlaceholderInterval(identifier, refresh)){
			
			@Override
			public String get() {
				return pl.setPlaceholders(null, identifier);
			}
		});
	}
	
	/**
	 * Registers player placeholder
	 * @param identifier - placeholder identifier
	 * @param refresh - refresh interval in milliseconds
	 */
	private void registerPlayerPlaceholder(String identifier, int refresh) {
		BukkitPlatform pl = this;
		TAB.getInstance().getPlaceholderManager().registerPlaceholder(new PlayerPlaceholder(identifier, TAB.getInstance().getErrorManager().fixPlaceholderInterval(identifier, refresh)) {

			@Override
			public String get(TabPlayer p) {
				return pl.setPlaceholders((Player) p.getPlayer(), identifier);
			}
		});
	}
	
	/**
	 * Registers relational placeholder
	 * @param identifier - placeholder identifier
	 * @param refresh - refresh interval in milliseconds
	 */
	private void registerRelationalPlaceholder(String identifier, int refresh) {
		TAB.getInstance().getPlaceholderManager().registerPlaceholder(new RelationalPlaceholder(identifier, TAB.getInstance().getErrorManager().fixPlaceholderInterval(identifier, refresh)) {

			@Override
			public String get(TabPlayer viewer, TabPlayer target) {
				if (placeholderAPI == null) return identifier;
				try {
					return PlaceholderAPI.setRelationalPlaceholders((Player) viewer.getPlayer(), (Player) target.getPlayer(), identifier);
				} catch (Exception t) {
					TAB.getInstance().getErrorManager().printError("PlaceholderAPI v" + placeholderAPI.getDescription().getVersion() + " generated an error when setting relational placeholder " + identifier + " for viewer " + viewer.getName() + " and target " + target.getName(), t, false, TAB.getInstance().getErrorManager().getPapiErrorLog());
				}
				return identifier;
			}
		});
	}
	
	/**
	 * Runs PlaceholderAPI call and returns the output. If the task fails, error is logged and "ERROR" returned instead
	 * @param player - player to set placeholder for
	 * @param placeholder - placeholder
	 * @return result from PlaceholderAPI
	 */
	public String setPlaceholders(Player player, String placeholder) {
		if (placeholderAPI == null) return placeholder;
		try {
			return PlaceholderAPI.setPlaceholders(player, placeholder);
		} catch (Exception t) {
			String playername = (player == null ? "<null>" : player.getName());
			TAB.getInstance().getErrorManager().printError("PlaceholderAPI v" + placeholderAPI.getDescription().getVersion() + " generated an error when setting placeholder " + placeholder + " for player " + playername, t, false, TAB.getInstance().getErrorManager().getPapiErrorLog());
			return "ERROR";
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
		Bukkit.getPluginManager().callEvent(new BukkitTABLoadEvent());
	}

	@Override
	public int getMaxPlayers() {
		return Bukkit.getMaxPlayers();
	}

	@Override
	public String getConfigName() {
		return "bukkitconfig.yml";
	}

	public boolean isViaversionEnabled() {
		return viaversion;
	}

	public boolean isLibsdisguisesEnabled() {
		return libsdisguises;
	}

	public boolean isIdisguiseEnabled() {
		return idisguise;
	}

	public boolean isEssentialsEnabled() {
		return essentials;
	}
}