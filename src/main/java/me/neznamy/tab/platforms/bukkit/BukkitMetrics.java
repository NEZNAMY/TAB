package me.neznamy.tab.platforms.bukkit;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.features.Metrics;

/**
 * bStats metrics for bukkit
 */
@SuppressWarnings("unchecked")
public class BukkitMetrics extends Metrics {

	private Main plugin;

	public static void start(Main plugin) {
		BukkitMetrics metrics = new BukkitMetrics(plugin);
		metrics.addCustomChart(new SimplePie("unlimited_nametag_mode_enabled", new Callable<String>() {
			public String call() {
				return Shared.featureManager.isFeatureEnabled("nametagx") ? "Yes" : "No";
			}
		}));
		metrics.addCustomChart(new SimplePie("placeholderapi", new Callable<String>() {
			public String call() {
				return Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") ? "Yes" : "No";
			}
		}));
		metrics.addCustomChart(new SimplePie("permission_system", new Callable<String>() {
			public String call() {
				return Shared.permissionPlugin.getName();
			}
		}));
		metrics.addCustomChart(new SimplePie("server_version", new Callable<String>() {
			public String call() {
				return "1." + ProtocolVersion.SERVER_VERSION.getMinorVersion() + ".x";
			}
		}));
	}
	
	public BukkitMetrics(Main plugin) {
		super("https://bStats.org/submitData/bukkit");
		if (plugin == null) {
			throw new IllegalArgumentException("Plugin cannot be null!");
		}
		this.plugin = plugin;

		// Get the config file
		File bStatsFolder = new File(plugin.getDataFolder().getParentFile(), "bStats");
		File configFile = new File(bStatsFolder, "config.yml");
		YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
		if (!config.isSet("serverUuid")) {
			config.addDefault("enabled", true);
			config.addDefault("serverUuid", UUID.randomUUID().toString());
			config.addDefault("logFailedRequests", false);
			config.addDefault("logSentData", false);
			config.addDefault("logResponseStatusText", false);
			config.options().header(
					"bStats collects some data for plugin authors like how many servers are using their plugins.\n" +
							"To honor their work, you should not disable it.\n" +
							"This has nearly no effect on the server performance!\n" +
							"Check out https://bStats.org/ to learn more :)"
					).copyDefaults(true);
			try {
				config.save(configFile);
			} catch (IOException ignored) { }
		}

		// Load the data
		enabled = config.getBoolean("enabled", true);
		serverUUID = config.getString("serverUuid");
		logFailedRequests = config.getBoolean("logFailedRequests", false);
		logSentData = config.getBoolean("logSentData", false);
		logResponseStatusText = config.getBoolean("logResponseStatusText", false);

		if (enabled) {
			boolean found = false;
			// Search for all other bStats Metrics classes to see if we are the first one
			for (Class<?> service : Bukkit.getServicesManager().getKnownServices()) {
				try {
					service.getField("B_STATS_VERSION"); // Our identifier :)
					found = true; // We aren't the first
					break;
				} catch (NoSuchFieldException ignored) { }
			}
			// Register our service
			Bukkit.getServicesManager().register(BukkitMetrics.class, this, plugin, ServicePriority.Normal);
			if (!found) {
				// We are the first!
				startSubmitting();
			}
		}
	}

	private void startSubmitting() {
		Timer timer = new Timer(true); // We use a timer cause the Bukkit scheduler is affected by server lags
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (!plugin.isEnabled()) { // Plugin was disabled
					timer.cancel();
					return;
				}
				// Nevertheless we want our code to run in the Bukkit main thread, so we have to use the Bukkit scheduler
				// Don't be afraid! The connection to the bStats server is still async, only the stats collection is sync ;)
				Bukkit.getScheduler().runTask(plugin, new Runnable() {

					public void run() {
						submitData();
					}
				});
			}
		}, 1000*60*5, 1000*60*30);
		// Submit the data every 30 minutes, first time after 5 minutes to give other plugins enough time to start
		// WARNING: Changing the frequency has no effect but your plugin WILL be blocked/deleted!
		// WARNING: Just don't do it!
	}

	private JSONObject getServerData() {
		// Minecraft specific data
		int playerAmount = Shared.getPlayers().size();
		int onlineMode = Bukkit.getOnlineMode() ? 1 : 0;
		String bukkitVersion = Bukkit.getVersion();

		// OS/Java specific data
		String javaVersion = System.getProperty("java.version");
		String osName = System.getProperty("os.name");
		String osArch = System.getProperty("os.arch");
		String osVersion = System.getProperty("os.version");
		int coreCount = Runtime.getRuntime().availableProcessors();

		JSONObject data = new JSONObject();

		data.put("serverUUID", serverUUID);

		data.put("playerAmount", playerAmount);
		data.put("onlineMode", onlineMode);
		data.put("bukkitVersion", bukkitVersion);

		data.put("javaVersion", javaVersion);
		data.put("osName", osName);
		data.put("osArch", osArch);
		data.put("osVersion", osVersion);
		data.put("coreCount", coreCount);

		return data;
	}

	private void submitData() {
		JSONObject data = getServerData();

		JSONArray pluginData = new JSONArray();
		// Search for all other bStats Metrics classes to get their plugin data
		for (Class<?> service : Bukkit.getServicesManager().getKnownServices()) {
			try {
				service.getField("B_STATS_VERSION"); // Our identifier :)

				for (RegisteredServiceProvider<?> provider : Bukkit.getServicesManager().getRegistrations(service)) {
					try {
						pluginData.add(provider.getService().getMethod("getPluginData").invoke(provider.getProvider()));
					} catch (NullPointerException ignored) {
					} catch (NoSuchMethodException ignored) { 
					} catch (IllegalAccessException ignored) { 
					} catch (InvocationTargetException ignored) {
					}
				}
			} catch (NoSuchFieldException ignored) { }
		}

		data.put("plugins", pluginData);

		// Create a new thread for the connection to the bStats server
		new Thread(new Runnable() {

			public void run() {
				try {
					// Send the data
					sendData(data, logResponseStatusText);
				} catch (Throwable e) {
					// Something went wrong! :(
					if (logFailedRequests) {
						plugin.getLogger().log(Level.WARNING, "Could not submit plugin stats of " + plugin.getName(), e);
					}
				}
			}
		}).start();
	}
}