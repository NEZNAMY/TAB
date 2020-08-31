package me.neznamy.tab.platforms.bungee;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.features.Metrics;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

/**
 * bStats metrics for bungeecord
 */
@SuppressWarnings("unchecked")
public class BungeeMetrics extends Metrics {
	
	private final Plugin plugin;
	
	private static final List<Object> knownMetricsInstances = new ArrayList<Object>();

	public static void start(Plugin plugin) {
		BungeeMetrics metrics = new BungeeMetrics(plugin);
		metrics.addCustomChart(new SimplePie("permission_system", new Callable<String>() {
			public String call() {
				return Shared.permissionPlugin.getName();
			}
		}));
		metrics.addCustomChart(new SimplePie("global_playerlist_enabled", new Callable<String>() {
			public String call() {
				return Shared.featureManager.isFeatureEnabled("globalplayerlist") ? "Yes" : "No";
			}
		}));
	}
	
	public BungeeMetrics(Plugin plugin) {
		super("https://bStats.org/submitData/bungeecord");
		this.plugin = plugin;

		try {
			loadConfig();
		} catch (Throwable e) {
			// Failed to load configuration
			plugin.getLogger().log(Level.WARNING, "Failed to load bStats config!", e);
			return;
		}

		// We are not allowed to send data about this server :(
		if (!enabled) {
			return;
		}

		Class<?> usedMetricsClass = getFirstBStatsClass();
		if (usedMetricsClass == null) {
			// Failed to get first metrics class
			return;
		}
		if (usedMetricsClass == getClass()) {
			// We are the first! :)
			linkMetrics(this);
			startSubmitting();
		} else {
			// We aren't the first so we link to the first metrics class
			try {
				usedMetricsClass.getMethod("linkMetrics", Object.class).invoke(null, this);
			} catch (Throwable e) {
				if (logFailedRequests) {
					plugin.getLogger().log(Level.WARNING, "Failed to link to first metrics class " + usedMetricsClass.getName() + "!", e);
				}
			}
		}
	}

	public static void linkMetrics(Object metrics) {
		knownMetricsInstances.add(metrics);
	}

	private void startSubmitting() {
		// The data collection is async, as well as sending the data
		// Bungeecord does not have a main thread, everything is async
		plugin.getProxy().getScheduler().schedule(plugin, new Runnable() {
			public void run() {
				submitData();
			}
		}, 2, 30, TimeUnit.MINUTES);
		// Submit the data every 30 minutes, first time after 2 minutes to give other plugins enough time to start
		// WARNING: Changing the frequency has no effect but your plugin WILL be blocked/deleted!
		// WARNING: Just don't do it!
	}

	@SuppressWarnings("deprecation")
	private JSONObject getServerData() {
		// Minecraft specific data
		int playerAmount = plugin.getProxy().getOnlineCount();
		playerAmount = playerAmount > 500 ? 500 : playerAmount;
		int onlineMode = plugin.getProxy().getConfig().isOnlineMode() ? 1 : 0;
		String bungeecordVersion = plugin.getProxy().getVersion();
		int managedServers = plugin.getProxy().getServers().size();

		// OS/Java specific data
		String javaVersion = System.getProperty("java.version");
		String osName = System.getProperty("os.name");
		String osArch = System.getProperty("os.arch");
		String osVersion = System.getProperty("os.version");
		int coreCount = Runtime.getRuntime().availableProcessors();

		JSONObject data = new JSONObject();

		data.put("serverUUID", serverUUID);

		data.put("playerAmount", playerAmount);
		data.put("managedServers", managedServers);
		data.put("onlineMode", onlineMode);
		data.put("bungeecordVersion", bungeecordVersion);

		data.put("javaVersion", javaVersion);
		data.put("osName", osName);
		data.put("osArch", osArch);
		data.put("osVersion", osVersion);
		data.put("coreCount", coreCount);

		return data;
	}

	private void submitData() {
		final JSONObject data = getServerData();

		final JSONArray pluginData = new JSONArray();
		// Search for all other bStats Metrics classes to get their plugin data
		for (Object metrics : knownMetricsInstances) {
			try {
				Object plugin = metrics.getClass().getMethod("getPluginData").invoke(metrics);
				if (plugin instanceof JSONObject) {
					pluginData.add((JSONObject) plugin);
				}
			} catch (Exception ignored) { }
		}

		data.put("plugins", pluginData);

		try {
			// Send the data
			sendData(data, logResponseStatusText);
		} catch (Throwable e) {
			// Something went wrong! :(
			if (logFailedRequests) {
				plugin.getLogger().log(Level.WARNING, "Could not submit plugin stats!", e);
			}
		}
	}

	private void loadConfig() throws IOException {
		Path configPath = plugin.getDataFolder().toPath().getParent().resolve("bStats");
		configPath.toFile().mkdirs();
		File configFile = new File(configPath.toFile(), "config.yml");
		if (!configFile.exists()) {
			writeFile(configFile,
					"#bStats collects some data for plugin authors like how many servers are using their plugins.",
					"#To honor their work, you should not disable it.",
					"#This has nearly no effect on the server performance!",
					"#Check out https://bStats.org/ to learn more :)",
					"enabled: true",
					"serverUuid: \"" + UUID.randomUUID().toString() + "\"",
					"logFailedRequests: false",
					"logSentData: false",
					"logResponseStatusText: false");
		}

		Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);

		// Load configuration
		enabled = configuration.getBoolean("enabled", true);
		serverUUID = configuration.getString("serverUuid");
		logFailedRequests = configuration.getBoolean("logFailedRequests", false);
		logSentData = configuration.getBoolean("logSentData", false);
		logResponseStatusText = configuration.getBoolean("logResponseStatusText", false);
	}

	private Class<?> getFirstBStatsClass() {
		Path configPath = plugin.getDataFolder().toPath().getParent().resolve("bStats");
		configPath.toFile().mkdirs();
		File tempFile = new File(configPath.toFile(), "temp.txt");

		try {
			String className = readFile(tempFile);
			if (className != null) {
				try {
					// Let's check if a class with the given name exists.
					return Class.forName(className);
				} catch (ClassNotFoundException ignored) { }
			}
			writeFile(tempFile, getClass().getName());
			return getClass();
		} catch (Throwable e) {
			if (logFailedRequests) {
				plugin.getLogger().log(Level.WARNING, "Failed to get first bStats class!", e);
			}
			return null;
		}
	}

	private String readFile(File file) throws IOException {
		if (!file.exists()) {
			return null;
		}
		BufferedReader bufferedReader = null;
		try {
			FileReader fileReader = new FileReader(file);
			bufferedReader = new BufferedReader(fileReader);
			return bufferedReader.readLine();
		} catch (Throwable e) {
			return null;
		} finally {
			if (bufferedReader != null) bufferedReader.close();
		}
	}
	
	private void writeFile(File file, String... lines) throws IOException {
		if (!file.exists()) {
			file.createNewFile();
		}
		try {
			FileWriter fileWriter = new FileWriter(file);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			for (String line : lines) {
				bufferedWriter.write(line);
				bufferedWriter.newLine();
			}
			bufferedWriter.close();
		} catch (Throwable e) {
			
		}
	}
}