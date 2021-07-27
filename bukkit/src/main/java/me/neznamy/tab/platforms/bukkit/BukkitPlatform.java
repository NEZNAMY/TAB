package me.neznamy.tab.platforms.bukkit;

import java.io.File;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import me.clip.placeholderapi.PlaceholderAPI;
import me.neznamy.tab.api.PermissionPlugin;
import me.neznamy.tab.api.Platform;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.event.BukkitTABLoadEvent;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.api.placeholder.PlayerPlaceholder;
import me.neznamy.tab.api.placeholder.RelationalPlaceholder;
import me.neznamy.tab.api.placeholder.ServerPlaceholder;
import me.neznamy.tab.api.protocol.PacketBuilder;
import me.neznamy.tab.platforms.bukkit.features.WitherBossBar;
import me.neznamy.tab.platforms.bukkit.features.PerWorldPlayerlist;
import me.neznamy.tab.platforms.bukkit.features.PetFix;
import me.neznamy.tab.platforms.bukkit.features.TabExpansion;
import me.neznamy.tab.platforms.bukkit.features.unlimitedtags.NameTagX;
import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;
import me.neznamy.tab.platforms.bukkit.permission.Vault;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.BelowName;
import me.neznamy.tab.shared.features.NameTag;
import me.neznamy.tab.shared.features.PingSpoof;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.features.SpectatorFix;
import me.neznamy.tab.shared.features.YellowNumber;
import me.neznamy.tab.shared.features.bossbar.BossBarManagerImpl;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardManagerImpl;
import me.neznamy.tab.shared.permission.LuckPerms;
import me.neznamy.tab.shared.permission.None;
import me.neznamy.tab.shared.permission.UltraPermissions;
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
	
	private BukkitPacketBuilder packetBuilder;
	
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
		packetBuilder = new BukkitPacketBuilder(nms);
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
		new BukkitPlaceholderRegistry(plugin).registerPlaceholders(tab.getPlaceholderManager());
		new UniversalPlaceholderRegistry().registerPlaceholders(tab.getPlaceholderManager());
		if (nms.getMinorVersion() >= 8 && tab.getConfiguration().isPipelineInjection()) {
			tab.getFeatureManager().registerFeature("injection", new BukkitPipelineInjector(nms));
		}
		loadNametagFeature(tab);
		tab.loadUniversalFeatures();
		if (tab.getConfiguration().getConfig().getBoolean("ping-spoof.enabled", false)) tab.getFeatureManager().registerFeature("pingspoof", new PingSpoof());
		if (tab.getConfiguration().getConfig().getBoolean("yellow-number-in-tablist.enabled", true)) tab.getFeatureManager().registerFeature("tabobjective", new YellowNumber());
		if (tab.getConfiguration().getConfig().getBoolean("prevent-spectator-effect.enabled", false)) tab.getFeatureManager().registerFeature("spectatorfix", new SpectatorFix());
		if (tab.getConfiguration().getConfig().getBoolean("belowname-objective.enabled", true)) tab.getFeatureManager().registerFeature("belowname", new BelowName());
		if (tab.getConfiguration().getConfig().getBoolean("scoreboard.enabled", false)) tab.getFeatureManager().registerFeature("scoreboard", new ScoreboardManagerImpl());
		if (tab.getConfiguration().getConfig().getBoolean("bossbar.enabled", false)) {
			if (nms.getMinorVersion() < 9) {
				tab.getFeatureManager().registerFeature("bossbar", new WitherBossBar(plugin));
			} else {
				tab.getFeatureManager().registerFeature("bossbar", new BossBarManagerImpl());
			}
		}
		if (nms.getMinorVersion() >= 9 && tab.getConfiguration().getConfig().getBoolean("fix-pet-names.enabled", false)) tab.getFeatureManager().registerFeature("petfix", new PetFix(nms));
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
		if (tab.getConfiguration().getConfig().getBoolean("scoreboard-teams.enabled", true)) {
			if (tab.getConfiguration().getConfig().getBoolean("scoreboard-teams.unlimited-nametag-mode.enabled", false) && nms.getMinorVersion() >= 8) {
				tab.getFeatureManager().registerFeature("nametagx", new NameTagX(plugin, nms));
			} else {
				tab.getFeatureManager().registerFeature("nametag16", new NameTag());
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
	public Placeholder registerUnknownPlaceholder(String identifier) {
		PlaceholderManagerImpl pl = (PlaceholderManagerImpl) TAB.getInstance().getPlaceholderManager();
		if (identifier.startsWith("%rel_")) {
			//relational placeholder
			return registerRelationalPlaceholder(identifier, pl.getRelationalRefresh(identifier));
		} else {
			//normal placeholder
			if (pl.getServerPlaceholderRefreshIntervals().containsKey(identifier)) {
				return registerServerPlaceholder(identifier, pl.getServerPlaceholderRefreshIntervals().get(identifier));
			}
			if (pl.getPlayerPlaceholderRefreshIntervals().containsKey(identifier)) {
				return registerPlayerPlaceholder(identifier, pl.getPlayerPlaceholderRefreshIntervals().get(identifier));
			}
			return registerPlayerPlaceholder(identifier, pl.getDefaultRefresh());
		}
	}
	
	/**
	 * Registers server placeholder
	 * @param identifier - placeholder identifier
	 * @param refresh - refresh interval in milliseconds
	 */
	private ServerPlaceholder registerServerPlaceholder(String identifier, int refresh) {
		BukkitPlatform pl = this;
		ServerPlaceholder p = new ServerPlaceholder(identifier, TAB.getInstance().getErrorManager().fixPlaceholderInterval(identifier, refresh)){
			
			@Override
			public Object get() {
				return pl.setPlaceholders(null, identifier);
			}
		};
		TAB.getInstance().getPlaceholderManager().registerServerPlaceholder(p);
		return p;
	}
	
	/**
	 * Registers player placeholder
	 * @param identifier - placeholder identifier
	 * @param refresh - refresh interval in milliseconds
	 */
	private PlayerPlaceholder registerPlayerPlaceholder(String identifier, int refresh) {
		BukkitPlatform pl = this;
		PlayerPlaceholder p = new PlayerPlaceholder(identifier, TAB.getInstance().getErrorManager().fixPlaceholderInterval(identifier, refresh)) {

			@Override
			public Object get(TabPlayer p) {
				return pl.setPlaceholders((Player) p.getPlayer(), identifier);
			}
		};
		TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder(p);
		return p;
	}
	
	/**
	 * Registers relational placeholder
	 * @param identifier - placeholder identifier
	 * @param refresh - refresh interval in milliseconds
	 */
	private RelationalPlaceholder registerRelationalPlaceholder(String identifier, int refresh) {
		RelationalPlaceholder p = new RelationalPlaceholder(identifier, TAB.getInstance().getErrorManager().fixPlaceholderInterval(identifier, refresh)) {

			@Override
			public String get(TabPlayer viewer, TabPlayer target) {
				if (placeholderAPI == null) return identifier;
				try {
					return PlaceholderAPI.setRelationalPlaceholders((Player) viewer.getPlayer(), (Player) target.getPlayer(), identifier);
				} catch (Exception t) {
					return identifier;
				}
			}
		};
		TAB.getInstance().getPlaceholderManager().registerRelationalPlaceholder(p);
		return p;
	}
	
	/**
	 * Runs PlaceholderAPI call and returns the output. Returns identifier if call throws an exception.
	 * @param player - player to set placeholder for
	 * @param placeholder - placeholder
	 * @return result from PlaceholderAPI
	 */
	public String setPlaceholders(Player player, String placeholder) {
		if (placeholderAPI == null) return placeholder;
		try {
			return PlaceholderAPI.setPlaceholders(player, placeholder);
		} catch (Exception | NoClassDefFoundError t) {
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

	@Override
	public PacketBuilder getPacketBuilder() {
		return packetBuilder;
	}
}