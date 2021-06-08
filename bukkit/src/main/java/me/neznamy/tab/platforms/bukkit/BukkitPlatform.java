package me.neznamy.tab.platforms.bukkit;

import java.io.File;
import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.World;
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
import me.neznamy.tab.shared.Platform;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.NameTag;
import me.neznamy.tab.shared.features.PlaceholderManager;
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

	//list of used expansions
	private Set<String> usedExpansions;
	
	//plugin instance
	private JavaPlugin plugin;
	
	//nms storage
	private NMSStorage nms;
	
	//booleans to check plugin presence
	public boolean placeholderAPI;
	public boolean viaversion;
	public boolean idisguise;
	public boolean libsdisguises;
	public boolean essentials;

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
	public void loadFeatures() throws Exception {
		placeholderAPI = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
		viaversion = Bukkit.getPluginManager().isPluginEnabled("ViaVersion");
		idisguise = Bukkit.getPluginManager().isPluginEnabled("iDisguise");
		libsdisguises = Bukkit.getPluginManager().isPluginEnabled("LibsDisguises");
		essentials = Bukkit.getPluginManager().isPluginEnabled("Essentials");
		TAB tab = TAB.getInstance();
		usedExpansions = new HashSet<String>();
		tab.getPlaceholderManager().addRegistry(new BukkitPlaceholderRegistry(plugin));
		tab.getPlaceholderManager().addRegistry(new UniversalPlaceholderRegistry());
		tab.getPlaceholderManager().registerPlaceholders();
		if (nms.minorVersion >= 8 && tab.getConfiguration().pipelineInjection) {
			tab.getFeatureManager().registerFeature("injection", new BukkitPipelineInjector(tab, nms));
		}
		loadNametagFeature(tab);
		loadUniversalFeatures();
		if (tab.getConfiguration().bossbar.getBoolean("bossbar-enabled", false) && nms.minorVersion < 9) tab.getFeatureManager().registerFeature("bossbar1.8", new BossBar_legacy(tab, plugin));
		if (nms.minorVersion >= 9 && tab.getConfiguration().config.getBoolean("fix-pet-names", false)) tab.getFeatureManager().registerFeature("petfix", new PetFix(nms));
		if (tab.getConfiguration().config.getBoolean("per-world-playerlist.enabled", false)) tab.getFeatureManager().registerFeature("pwp", new PerWorldPlayerlist(plugin, tab));
		if (placeholderAPI) {
			new TabExpansion(plugin);
			new ExpansionDownloader(plugin).download(usedExpansions);
		}

		for (Player p : getOnlinePlayers()) {
			tab.addPlayer(new BukkitTabPlayer(p));
		}
	}
	
	/**
	 * Loads nametag feature from config
	 * @param tab - tab instance
	 */
	private void loadNametagFeature(TAB tab) {
		if (tab.getConfiguration().config.getBoolean("change-nametag-prefix-suffix", true)) {
			if (tab.getConfiguration().config.getBoolean("unlimited-nametag-prefix-suffix-mode.enabled", false) && nms.minorVersion >= 8) {
				tab.getFeatureManager().registerFeature("nametagx", new NameTagX(plugin, nms, tab));
			} else {
				tab.getFeatureManager().registerFeature("nametag16", new NameTag(tab));
			}
		}
	}

	/**
	 * Returns list of online players from Bukkit API
	 * @return list of online players from Bukkit API
	 * @throws Exception - if reflection fails
	 */
	@SuppressWarnings("unchecked")
	private Player[] getOnlinePlayers() throws Exception {
		Object players = Bukkit.class.getMethod("getOnlinePlayers").invoke(null);
		if (players instanceof Player[]) {
			//1.7.x
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
	
	/**
	 * Registers server placeholder
	 * @param identifier - placeholder identifier
	 * @param refresh - refresh interval in milliseconds
	 */
	private void registerServerPlaceholder(String identifier, int refresh) {
		TAB.getInstance().getPlaceholderManager().registerPlaceholder(new ServerPlaceholder(identifier, TAB.getInstance().getErrorManager().fixPlaceholderInterval(identifier, refresh)){
			
			@Override
			public String get() {
				return setPlaceholders(null, identifier);
			}
		});
	}
	
	/**
	 * Registers player placeholder
	 * @param identifier - placeholder identifier
	 * @param refresh - refresh interval in milliseconds
	 */
	private void registerPlayerPlaceholder(String identifier, int refresh) {
		TAB.getInstance().getPlaceholderManager().registerPlaceholder(new PlayerPlaceholder(identifier, TAB.getInstance().getErrorManager().fixPlaceholderInterval(identifier, refresh)) {

			@Override
			public String get(TabPlayer p) {
				return setPlaceholders((Player) p.getPlayer(), identifier);
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
				if (!placeholderAPI) return identifier;
				try {
					return PlaceholderAPI.setRelationalPlaceholders((Player) viewer.getPlayer(), (Player) target.getPlayer(), identifier);
				} catch (Throwable t) {
					TAB.getInstance().getErrorManager().printError("PlaceholderAPI v" + Bukkit.getPluginManager().getPlugin("PlaceholderAPI").getDescription().getVersion() + " generated an error when setting relational placeholder " + identifier + " for viewer " + viewer.getName() + " and target " + target.getName(), t, false, TAB.getInstance().getErrorManager().papiErrorLog);
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
		if (!placeholderAPI) return placeholder;
		try {
			return PlaceholderAPI.setPlaceholders(player, placeholder);
		} catch (Throwable t) {
			String playername = (player == null ? "<null>" : player.getName());
			TAB.getInstance().getErrorManager().printError("PlaceholderAPI v" + Bukkit.getPluginManager().getPlugin("PlaceholderAPI").getDescription().getVersion() + " generated an error when setting placeholder " + placeholder + " for player " + playername, t, false, TAB.getInstance().getErrorManager().papiErrorLog);
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
	public List<String> getWorldNames() {
		List<String> list = new ArrayList<>();

		for (World world : Bukkit.getWorlds()) {
			list.add(world.getName());
		}

		return list;
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
}