package me.neznamy.tab.bungee;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import me.neznamy.tab.premium.Premium;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

/**
 * bStats collects some data for plugin authors.
 * <p>
 * Check out https://bStats.org/ to learn more about bStats!
 */
@SuppressWarnings({"unused"})
public class Metrics {

	static {
		// You can use the property to disable the check in your test environment
		if (System.getProperty("bstats.relocatecheck") == null || !System.getProperty("bstats.relocatecheck").equals("false")) {
			// Maven's Relocate is clever and changes strings, too. So we have to use this little "trick" ... :D
			final String defaultPackage = new String(
					new byte[]{'o', 'r', 'g', '.', 'b', 's', 't', 'a', 't', 's', '.', 'b', 'u', 'n', 'g', 'e', 'e', 'c', 'o', 'r', 'd'});
			final String examplePackage = new String(new byte[]{'y', 'o', 'u', 'r', '.', 'p', 'a', 'c', 'k', 'a', 'g', 'e'});
			// We want to make sure nobody just copy & pastes the example and use the wrong package names
			if (Metrics.class.getPackage().getName().equals(defaultPackage) || Metrics.class.getPackage().getName().equals(examplePackage)) {
				throw new IllegalStateException("bStats Metrics class has not been relocated correctly!");
			}
		}
	}

	// The version of this bStats class
	public static final int B_STATS_VERSION = 1;

	// The url to which the data is sent
	private static final String URL = "https://bStats.org/submitData/bungeecord";

	// The plugin
	private final Plugin plugin;

	// Is bStats enabled on this server?
	private boolean enabled;

	// The uuid of the server
	private String serverUUID;

	// Should failed requests be logged?
	private boolean logFailedRequests = false;

	// Should the sent data be logged?
	private static boolean logSentData;

	// Should the response text be logged?
	private static boolean logResponseStatusText;

	// A list with all known metrics class objects including this one
	private static final List<Object> knownMetricsInstances = new ArrayList<Object>();

	// A list with all custom charts
	private final List<CustomChart> charts = new ArrayList<CustomChart>();

	public Metrics(Plugin plugin) {
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
			plugin.getLogger().log(Level.WARNING, "Chart cannot be null");
		}
		charts.add(chart);
	}

	/**
	 * Links an other metrics class with this class.
	 * This method is called using Reflection.
	 *
	 * @param metrics An object of the metrics class to link.
	 */
	public static void linkMetrics(Object metrics) {
		knownMetricsInstances.add(metrics);
	}

	/**
	 * Gets the plugin specific data.
	 * This method is called using Reflection.
	 *
	 * @return The plugin specific data.
	 */
	public JsonObject getPluginData() {
		JsonObject data = new JsonObject();

		String pluginName = "TAB Reborn";
		String pluginVersion = plugin.getDescription().getVersion();
		if (Premium.is()) pluginVersion += "+";

		data.addProperty("pluginName", pluginName);
		data.addProperty("pluginVersion", pluginVersion);

		JsonArray customCharts = new JsonArray();
		for (CustomChart customChart : charts) {
			// Add the data of the custom charts
			JsonObject chart = customChart.getRequestJsonObject(plugin.getLogger(), logFailedRequests);
			if (chart == null) { // If the chart is null, we skip it
				continue;
			}
			customCharts.add(chart);
		}
		data.add("customCharts", customCharts);

		return data;
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

	/**
	 * Gets the server specific data.
	 *
	 * @return The server specific data.
	 */
	@SuppressWarnings("deprecation")
	private JsonObject getServerData() {
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

		JsonObject data = new JsonObject();

		data.addProperty("serverUUID", serverUUID);

		data.addProperty("playerAmount", playerAmount);
		data.addProperty("managedServers", managedServers);
		data.addProperty("onlineMode", onlineMode);
		data.addProperty("bungeecordVersion", bungeecordVersion);

		data.addProperty("javaVersion", javaVersion);
		data.addProperty("osName", osName);
		data.addProperty("osArch", osArch);
		data.addProperty("osVersion", osVersion);
		data.addProperty("coreCount", coreCount);

		return data;
	}

	/**
	 * Collects the data and sends it afterwards.
	 */
	private void submitData() {
		final JsonObject data = getServerData();

		final JsonArray pluginData = new JsonArray();
		// Search for all other bStats Metrics classes to get their plugin data
		for (Object metrics : knownMetricsInstances) {
			try {
				Object plugin = metrics.getClass().getMethod("getPluginData").invoke(metrics);
				if (plugin instanceof JsonObject) {
					pluginData.add((JsonObject) plugin);
				}
			} catch (Exception ignored) { }
		}

		data.add("plugins", pluginData);

		try {
			// Send the data
			sendData(plugin, data);
		} catch (Throwable e) {
			// Something went wrong! :(
			if (logFailedRequests) {
				plugin.getLogger().log(Level.WARNING, "Could not submit plugin stats!", e);
			}
		}
	}

	/**
	 * Loads the bStats configuration.
	 *
	 * @throws IOException If something did not work :(
	 */
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

	/**
	 * Gets the first bStat Metrics class.
	 *
	 * @return The first bStats metrics class.
	 */
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

	/**
	 * Reads the first line of the file.
	 *
	 * @param file The file to read. Cannot be null.
	 * @return The first line of the file or <code>null</code> if the file does not exist or is empty.
	 * @throws IOException If something did not work :(
	 */
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

	/**
	 * Writes a String to a file. It also adds a note for the user,
	 *
	 * @param file The file to write to. Cannot be null.
	 * @param lines The lines to write.
	 * @throws IOException If something did not work :(
	 */
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

	/**
	 * Sends the data to the bStats server.
	 *
	 * @param plugin Any plugin. It's just used to get a logger instance.
	 * @param data The data to send.
	 * @throws Exception If the request failed.
	 */
	private static void sendData(Plugin plugin, JsonObject data) throws Exception {
		if (data == null) {
			throw new IllegalArgumentException("Data cannot be null");
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

	/**
	 * Gzips the given String.
	 *
	 * @param str The string to gzip.
	 * @return The gzipped String.
	 * @throws IOException If the compression failed.
	 */
	private static byte[] compress(final String str) throws IOException {
		if (str == null) {
			return null;
		}
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		GZIPOutputStream gzip = new GZIPOutputStream(outputStream);
		gzip.write(str.getBytes(StandardCharsets.UTF_8));
		gzip.close();
		return outputStream.toByteArray();
	}


	/**
	 * Represents a custom chart.
	 */
	public static abstract class CustomChart {

		// The id of the chart
		private final String chartId;

		/**
		 * Class constructor.
		 *
		 * @param chartId The id of the chart.
		 */
		CustomChart(String chartId) {
			if (chartId == null || chartId.isEmpty()) {
				throw new IllegalArgumentException("ChartId cannot be null or empty!");
			}
			this.chartId = chartId;
		}

		private JsonObject getRequestJsonObject(Logger logger, boolean logFailedRequests) {
			JsonObject chart = new JsonObject();
			chart.addProperty("chartId", chartId);
			try {
				JsonObject data = getChartData();
				if (data == null) {
					// If the data is null we don't send the chart.
					return null;
				}
				chart.add("data", data);
			} catch (Throwable t) {
				if (logFailedRequests) {
					logger.log(Level.WARNING, "Failed to get data for custom chart with id " + chartId, t);
				}
				return null;
			}
			return chart;
		}

		protected abstract JsonObject getChartData() throws Exception;

	}

	/**
	 * Represents a custom simple pie.
	 */
	public static class SimplePie extends CustomChart {

		private final Callable<String> callable;

		/**
		 * Class constructor.
		 *
		 * @param chartId The id of the chart.
		 * @param callable The callable which is used to request the chart data.
		 */
		public SimplePie(String chartId, Callable<String> callable) {
			super(chartId);
			this.callable = callable;
		}

		@Override
		protected JsonObject getChartData() throws Exception {
			JsonObject data = new JsonObject();
			String value = callable.call();
			if (value == null || value.isEmpty()) {
				// Null = skip the chart
				return null;
			}
			data.addProperty("value", value);
			return data;
		}
	}

	/**
	 * Represents a custom advanced pie.
	 */
	public static class AdvancedPie extends CustomChart {

		private final Callable<Map<String, Integer>> callable;

		/**
		 * Class constructor.
		 *
		 * @param chartId The id of the chart.
		 * @param callable The callable which is used to request the chart data.
		 */
		public AdvancedPie(String chartId, Callable<Map<String, Integer>> callable) {
			super(chartId);
			this.callable = callable;
		}

		@Override
		protected JsonObject getChartData() throws Exception {
			JsonObject data = new JsonObject();
			JsonObject values = new JsonObject();
			Map<String, Integer> map = callable.call();
			if (map == null || map.isEmpty()) {
				// Null = skip the chart
				return null;
			}
			boolean allSkipped = true;
			for (Map.Entry<String, Integer> entry : map.entrySet()) {
				if (entry.getValue() == 0) {
					continue; // Skip this invalid
				}
				allSkipped = false;
				values.addProperty(entry.getKey(), entry.getValue());
			}
			if (allSkipped) {
				// Null = skip the chart
				return null;
			}
			data.add("values", values);
			return data;
		}
	}

	/**
	 * Represents a custom drilldown pie.
	 */
	public static class DrilldownPie extends CustomChart {

		private final Callable<Map<String, Map<String, Integer>>> callable;

		/**
		 * Class constructor.
		 *
		 * @param chartId The id of the chart.
		 * @param callable The callable which is used to request the chart data.
		 */
		public DrilldownPie(String chartId, Callable<Map<String, Map<String, Integer>>> callable) {
			super(chartId);
			this.callable = callable;
		}

		@Override
		public JsonObject getChartData() throws Exception {
			JsonObject data = new JsonObject();
			JsonObject values = new JsonObject();
			Map<String, Map<String, Integer>> map = callable.call();
			if (map == null || map.isEmpty()) {
				// Null = skip the chart
				return null;
			}
			boolean reallyAllSkipped = true;
			for (Map.Entry<String, Map<String, Integer>> entryValues : map.entrySet()) {
				JsonObject value = new JsonObject();
				boolean allSkipped = true;
				for (Map.Entry<String, Integer> valueEntry : map.get(entryValues.getKey()).entrySet()) {
					value.addProperty(valueEntry.getKey(), valueEntry.getValue());
					allSkipped = false;
				}
				if (!allSkipped) {
					reallyAllSkipped = false;
					values.add(entryValues.getKey(), value);
				}
			}
			if (reallyAllSkipped) {
				// Null = skip the chart
				return null;
			}
			data.add("values", values);
			return data;
		}
	}

	/**
	 * Represents a custom single line chart.
	 */
	public static class SingleLineChart extends CustomChart {

		private final Callable<Integer> callable;

		/**
		 * Class constructor.
		 *
		 * @param chartId The id of the chart.
		 * @param callable The callable which is used to request the chart data.
		 */
		public SingleLineChart(String chartId, Callable<Integer> callable) {
			super(chartId);
			this.callable = callable;
		}

		@Override
		protected JsonObject getChartData() throws Exception {
			JsonObject data = new JsonObject();
			int value = callable.call();
			if (value == 0) {
				// Null = skip the chart
				return null;
			}
			data.addProperty("value", value);
			return data;
		}

	}

	/**
	 * Represents a custom multi line chart.
	 */
	public static class MultiLineChart extends CustomChart {

		private final Callable<Map<String, Integer>> callable;

		/**
		 * Class constructor.
		 *
		 * @param chartId The id of the chart.
		 * @param callable The callable which is used to request the chart data.
		 */
		public MultiLineChart(String chartId, Callable<Map<String, Integer>> callable) {
			super(chartId);
			this.callable = callable;
		}

		@Override
		protected JsonObject getChartData() throws Exception {
			JsonObject data = new JsonObject();
			JsonObject values = new JsonObject();
			Map<String, Integer> map = callable.call();
			if (map == null || map.isEmpty()) {
				// Null = skip the chart
				return null;
			}
			boolean allSkipped = true;
			for (Map.Entry<String, Integer> entry : map.entrySet()) {
				if (entry.getValue() == 0) {
					continue; // Skip this invalid
				}
				allSkipped = false;
				values.addProperty(entry.getKey(), entry.getValue());
			}
			if (allSkipped) {
				// Null = skip the chart
				return null;
			}
			data.add("values", values);
			return data;
		}

	}

	/**
	 * Represents a custom simple bar chart.
	 */
	public static class SimpleBarChart extends CustomChart {

		private final Callable<Map<String, Integer>> callable;

		/**
		 * Class constructor.
		 *
		 * @param chartId The id of the chart.
		 * @param callable The callable which is used to request the chart data.
		 */
		public SimpleBarChart(String chartId, Callable<Map<String, Integer>> callable) {
			super(chartId);
			this.callable = callable;
		}

		@Override
		protected JsonObject getChartData() throws Exception {
			JsonObject data = new JsonObject();
			JsonObject values = new JsonObject();
			Map<String, Integer> map = callable.call();
			if (map == null || map.isEmpty()) {
				// Null = skip the chart
				return null;
			}
			for (Map.Entry<String, Integer> entry : map.entrySet()) {
				JsonArray categoryValues = new JsonArray();
				categoryValues.add(new JsonPrimitive(entry.getValue()));
				values.add(entry.getKey(), categoryValues);
			}
			data.add("values", values);
			return data;
		}

	}

	/**
	 * Represents a custom advanced bar chart.
	 */
	public static class AdvancedBarChart extends CustomChart {

		private final Callable<Map<String, int[]>> callable;

		/**
		 * Class constructor.
		 *
		 * @param chartId The id of the chart.
		 * @param callable The callable which is used to request the chart data.
		 */
		public AdvancedBarChart(String chartId, Callable<Map<String, int[]>> callable) {
			super(chartId);
			this.callable = callable;
		}

		@Override
		protected JsonObject getChartData() throws Exception {
			JsonObject data = new JsonObject();
			JsonObject values = new JsonObject();
			Map<String, int[]> map = callable.call();
			if (map == null || map.isEmpty()) {
				// Null = skip the chart
				return null;
			}
			boolean allSkipped = true;
			for (Map.Entry<String, int[]> entry : map.entrySet()) {
				if (entry.getValue().length == 0) {
					continue; // Skip this invalid
				}
				allSkipped = false;
				JsonArray categoryValues = new JsonArray();
				for (int categoryValue : entry.getValue()) {
					categoryValues.add(new JsonPrimitive(categoryValue));
				}
				values.add(entry.getKey(), categoryValues);
			}
			if (allSkipped) {
				// Null = skip the chart
				return null;
			}
			data.add("values", values);
			return data;
		}

	}

}