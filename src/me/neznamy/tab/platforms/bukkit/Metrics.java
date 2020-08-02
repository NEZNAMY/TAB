package me.neznamy.tab.platforms.bukkit;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.zip.GZIPOutputStream;

import javax.net.ssl.HttpsURLConnection;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.shared.PluginHooks;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.features.PlaceholderManager;

/**
 * bStats collects some data for plugin authors.
 *
 * Check out https://bStats.org/ to learn more about bStats!
 */
@SuppressWarnings("unchecked")
public class Metrics {

	// The version of this bStats class
	public static final int B_STATS_VERSION = 1;

	// The url to which the data is sent
	private static final String URL = "https://bStats.org/submitData/bukkit";

	// Is bStats enabled on this server?
	private boolean enabled;

	// Should failed requests be logged?
	private static boolean logFailedRequests;

	// Should the sent data be logged?
	private boolean logSentData;

	// Should the response text be logged?
	private boolean logResponseStatusText;

	// The uuid of the server
	private String serverUUID;

	// The plugin
	private final Main plugin;

	// A list with all custom charts
	private final List<CustomChart> charts = new ArrayList<CustomChart>();

	public static void start(Main plugin) {
		Metrics metrics = new Metrics(plugin);
		metrics.addCustomChart(new Metrics.SimplePie("unlimited_nametag_mode_enabled", new Callable<String>() {
			public String call() {
				return Shared.features.containsKey("nametagx") ? "Yes" : "No";
			}
		}));
		metrics.addCustomChart(new Metrics.SimplePie("placeholderapi", new Callable<String>() {
			public String call() {
				return PluginHooks.placeholderAPI ? "Yes" : "No";
			}
		}));
		metrics.addCustomChart(new Metrics.SimplePie("permission_system", new Callable<String>() {
			public String call() {
				return Shared.permissionPlugin.getName();
			}
		}));
		metrics.addCustomChart(new Metrics.SimplePie("server_version", new Callable<String>() {
			public String call() {
				return "1." + ProtocolVersion.SERVER_VERSION.getMinorVersion() + ".x";
			}
		}));
		metrics.addCustomChart(new Metrics.AdvancedPie("used_unlisted_papi_placeholders_2_8_1", new Callable<Map<String, Integer>>() {
			public Map<String, Integer> call(){
				Map<String, Integer> map = new HashMap<String, Integer>();
				for (String placeholder : PlaceholderManager.getInstance().unknownPlaceholders) {
					map.put(placeholder, 1);
				}
				return map;
			}
		}));
	}
	
	public Metrics(Main plugin) {
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
			Bukkit.getServicesManager().register(Metrics.class, this, plugin, ServicePriority.Normal);
			if (!found) {
				// We are the first!
				startSubmitting();
			}
		}
	}

	/**
	 * Checks if bStats is enabled.
	 *
	 * @return Whether bStats is enabled or not.
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Adds a custom chart.
	 *
	 * @param chart The chart to add.
	 */
	public void addCustomChart(CustomChart chart) {
		if (chart == null) {
			throw new IllegalArgumentException("Chart cannot be null!");
		}
		charts.add(chart);
	}

	/**
	 * Starts the Scheduler which submits our data every 30 minutes.
	 */
	private void startSubmitting() {
		final Timer timer = new Timer(true); // We use a timer cause the Bukkit scheduler is affected by server lags
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

	/**
	 * Gets the plugin specific data.
	 * This method is called using Reflection.
	 *
	 * @return The plugin specific data.
	 */
	public JSONObject getPluginData() {
		JSONObject data = new JSONObject();

		String pluginName = "TAB Reborn";
		String pluginVersion = plugin.getDescription().getVersion();
		if (Premium.is()) pluginVersion += " Premium";

		data.put("pluginName", pluginName); // Append the name of the plugin
		data.put("pluginVersion", pluginVersion); // Append the version of the plugin
		JSONArray customCharts = new JSONArray();
		for (CustomChart customChart : charts) {
			// Add the data of the custom charts
			JSONObject chart = customChart.getRequestJsonObject();
			if (chart == null) { // If the chart is null, we skip it
				continue;
			}
			customCharts.add(chart);
		}
		data.put("customCharts", customCharts);

		return data;
	}

	/**
	 * Gets the server specific data.
	 *
	 * @return The server specific data.
	 */
	private JSONObject getServerData() {
		// Minecraft specific data
		int playerAmount = Main.getOnlinePlayers().length;
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

	/**
	 * Collects the data and sends it afterwards.
	 */
	private void submitData() {
		final JSONObject data = getServerData();

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
					sendData(plugin, data);
				} catch (Throwable e) {
					// Something went wrong! :(
					if (logFailedRequests) {
						plugin.getLogger().log(Level.WARNING, "Could not submit plugin stats of " + plugin.getName(), e);
					}
				}
			}
		}).start();
	}

	private void sendData(Plugin plugin, JSONObject data) throws Exception {
		if (data == null) {
			throw new IllegalArgumentException("Data cannot be null!");
		}
		if (Bukkit.isPrimaryThread()) {
			throw new IllegalAccessException("This method must not be called from the main thread!");
		}
		if (logSentData) {
			plugin.getLogger().info("Sending data to bStats: " + data.toString());
		}
		HttpsURLConnection connection = (HttpsURLConnection) new URL(URL).openConnection();

		// Compress the data to save bandwidth
		byte[] compressedData = compress(data.toString());

		// Add headers
		connection.setRequestMethod("POST");
		connection.addRequestProperty("Accept", "application/json");
		connection.addRequestProperty("Connection", "close");
		connection.addRequestProperty("Content-Encoding", "gzip"); // We gzip our request
		connection.addRequestProperty("Content-Length", String.valueOf(compressedData.length));
		connection.setRequestProperty("Content-Type", "application/json"); // We send our data in JSON format
		connection.setRequestProperty("User-Agent", "MC-Server/" + B_STATS_VERSION);

		// Send data
		connection.setDoOutput(true);
		DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
		outputStream.write(compressedData);
		outputStream.flush();
		outputStream.close();

		InputStream inputStream = connection.getInputStream();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

		StringBuilder builder = new StringBuilder();
		String line;
		while ((line = bufferedReader.readLine()) != null) {
			builder.append(line);
		}
		bufferedReader.close();
		if (logResponseStatusText) {
			plugin.getLogger().info("Sent data to bStats and received response: " + builder.toString());
		}
	}

	private static byte[] compress(final String str) throws IOException {
		if (str == null) {
			return null;
		}
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		GZIPOutputStream gzip = new GZIPOutputStream(outputStream);
		gzip.write(str.getBytes("UTF-8"));
		gzip.close();
		return outputStream.toByteArray();
	}

	public static abstract class CustomChart {

		private final String chartId;

		CustomChart(String chartId) {
			if (chartId == null || chartId.isEmpty()) {
				throw new IllegalArgumentException("ChartId cannot be null or empty!");
			}
			this.chartId = chartId;
		}

		private JSONObject getRequestJsonObject() {
			JSONObject chart = new JSONObject();
			chart.put("chartId", chartId);
			try {
				JSONObject data = getChartData();
				if (data == null) {
					return null;
				}
				chart.put("data", data);
			} catch (Throwable t) {
				if (logFailedRequests) {
					Bukkit.getLogger().log(Level.WARNING, "Failed to get data for custom chart with id " + chartId, t);
				}
				return null;
			}
			return chart;
		}

		protected abstract JSONObject getChartData() throws Exception;

	}

	public static class SimplePie extends CustomChart {

		private final Callable<String> callable;

		public SimplePie(String chartId, Callable<String> callable) {
			super(chartId);
			this.callable = callable;
		}

		@Override
		protected JSONObject getChartData() throws Exception {
			JSONObject data = new JSONObject();
			String value = callable.call();
			if (value == null || value.isEmpty()) {
				// Null = skip the chart
				return null;
			}
			data.put("value", value);
			return data;
		}
	}

	public static class AdvancedPie extends CustomChart {

		private final Callable<Map<String, Integer>> callable;

		public AdvancedPie(String chartId, Callable<Map<String, Integer>> callable) {
			super(chartId);
			this.callable = callable;
		}

		@Override
		protected JSONObject getChartData() throws Exception {
			JSONObject data = new JSONObject();
			JSONObject values = new JSONObject();
			Map<String, Integer> map = callable.call();
			if (map == null || map.isEmpty()) {
				return null;
			}
			boolean allSkipped = true;
			for (Map.Entry<String, Integer> entry : map.entrySet()) {
				if (entry.getValue() == 0) {
					continue;
				}
				allSkipped = false;
				values.put(entry.getKey(), entry.getValue());
			}
			if (allSkipped) {
				return null;
			}
			data.put("values", values);
			return data;
		}
	}

	public static class DrilldownPie extends CustomChart {

		private final Callable<Map<String, Map<String, Integer>>> callable;

		public DrilldownPie(String chartId, Callable<Map<String, Map<String, Integer>>> callable) {
			super(chartId);
			this.callable = callable;
		}

		@Override
		public JSONObject getChartData() throws Exception {
			JSONObject data = new JSONObject();
			JSONObject values = new JSONObject();
			Map<String, Map<String, Integer>> map = callable.call();
			if (map == null || map.isEmpty()) {
				return null;
			}
			boolean reallyAllSkipped = true;
			for (Map.Entry<String, Map<String, Integer>> entryValues : map.entrySet()) {
				JSONObject value = new JSONObject();
				boolean allSkipped = true;
				for (Map.Entry<String, Integer> valueEntry : map.get(entryValues.getKey()).entrySet()) {
					value.put(valueEntry.getKey(), valueEntry.getValue());
					allSkipped = false;
				}
				if (!allSkipped) {
					reallyAllSkipped = false;
					values.put(entryValues.getKey(), value);
				}
			}
			if (reallyAllSkipped) {
				return null;
			}
			data.put("values", values);
			return data;
		}
	}

	public static class SingleLineChart extends CustomChart {

		private final Callable<Integer> callable;

		public SingleLineChart(String chartId, Callable<Integer> callable) {
			super(chartId);
			this.callable = callable;
		}

		@Override
		protected JSONObject getChartData() throws Exception {
			JSONObject data = new JSONObject();
			int value = callable.call();
			if (value == 0) {
				// Null = skip the chart
				return null;
			}
			data.put("value", value);
			return data;
		}

	}

	public static class MultiLineChart extends CustomChart {

		private final Callable<Map<String, Integer>> callable;

		public MultiLineChart(String chartId, Callable<Map<String, Integer>> callable) {
			super(chartId);
			this.callable = callable;
		}

		@Override
		protected JSONObject getChartData() throws Exception {
			JSONObject data = new JSONObject();
			JSONObject values = new JSONObject();
			Map<String, Integer> map = callable.call();
			if (map == null || map.isEmpty()) {
				return null;
			}
			boolean allSkipped = true;
			for (Map.Entry<String, Integer> entry : map.entrySet()) {
				if (entry.getValue() == 0) {
					continue;
				}
				allSkipped = false;
				values.put(entry.getKey(), entry.getValue());
			}
			if (allSkipped) {
				return null;
			}
			data.put("values", values);
			return data;
		}
	}

	public static class SimpleBarChart extends CustomChart {

		private final Callable<Map<String, Integer>> callable;

		public SimpleBarChart(String chartId, Callable<Map<String, Integer>> callable) {
			super(chartId);
			this.callable = callable;
		}

		@Override
		protected JSONObject getChartData() throws Exception {
			JSONObject data = new JSONObject();
			JSONObject values = new JSONObject();
			Map<String, Integer> map = callable.call();
			if (map == null || map.isEmpty()) {
				return null;
			}
			for (Map.Entry<String, Integer> entry : map.entrySet()) {
				JSONArray categoryValues = new JSONArray();
				categoryValues.add(entry.getValue());
				values.put(entry.getKey(), categoryValues);
			}
			data.put("values", values);
			return data;
		}

	}

	public static class AdvancedBarChart extends CustomChart {

		private final Callable<Map<String, int[]>> callable;

		public AdvancedBarChart(String chartId, Callable<Map<String, int[]>> callable) {
			super(chartId);
			this.callable = callable;
		}

		@Override
		protected JSONObject getChartData() throws Exception {
			JSONObject data = new JSONObject();
			JSONObject values = new JSONObject();
			Map<String, int[]> map = callable.call();
			if (map == null || map.isEmpty()) {
				return null;
			}
			boolean allSkipped = true;
			for (Map.Entry<String, int[]> entry : map.entrySet()) {
				if (entry.getValue().length == 0) {
					continue;
				}
				allSkipped = false;
				JSONArray categoryValues = new JSONArray();
				for (int categoryValue : entry.getValue()) {
					categoryValues.add(categoryValue);
				}
				values.put(entry.getKey(), categoryValues);
			}
			if (allSkipped) {
				return null;
			}
			data.put("values", values);
			return data;
		}

	}
}