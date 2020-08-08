package me.neznamy.tab.shared.features;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.zip.GZIPOutputStream;

import javax.net.ssl.HttpsURLConnection;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.shared.Shared;

@SuppressWarnings("unchecked")
public abstract class Metrics {

	public final int B_STATS_VERSION = 1;
	private String URL;
	protected boolean logSentData;
	protected boolean enabled;
	protected String serverUUID;
	protected boolean logResponseStatusText;
	protected boolean logFailedRequests;
	protected List<CustomChart> charts = new ArrayList<CustomChart>();
	
	public Metrics(String URL) {
		this.URL = URL;
	}

	public void addCustomChart(CustomChart chart) {
		if (chart == null) {
			throw new IllegalArgumentException("Chart cannot be null!");
		}
		charts.add(chart);
	}
	
	public JSONObject getPluginData() {
		JSONObject data = new JSONObject();

		String pluginName = "TAB Reborn";
		String pluginVersion = Shared.pluginVersion;
		if (Premium.is()) pluginVersion += " Premium";

		data.put("pluginName", pluginName);
		data.put("pluginVersion", pluginVersion);
		JSONArray customCharts = new JSONArray();
		for (CustomChart customChart : charts) {
			JSONObject chart = customChart.getRequestJSONObject();
			if (chart == null) {
				continue;
			}
			customCharts.add(chart);
		}
		data.put("customCharts", customCharts);

		return data;
	}
	
	public void sendData(JSONObject data, boolean logResponseStatusText) throws Exception {
		if (data == null) {
			throw new IllegalArgumentException("Data cannot be null!");
		}
		if (logSentData) {
			System.out.println("[TAB] Sending data to bStats: " + data.toString());
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
			System.out.println("[TAB] Sent data to bStats and received response: " + builder.toString());
		}
	}
	
	protected byte[] compress(String str) throws IOException {
		if (str == null) {
			return null;
		}
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		GZIPOutputStream gzip = new GZIPOutputStream(outputStream);
		gzip.write(str.getBytes(StandardCharsets.UTF_8));
		gzip.close();
		return outputStream.toByteArray();
	}
	
	public static abstract class CustomChart {

		private final String chartId;

		public CustomChart(String chartId) {
			if (chartId == null || chartId.isEmpty()) {
				throw new IllegalArgumentException("ChartId cannot be null or empty!");
			}
			this.chartId = chartId;
		}

		public JSONObject getRequestJSONObject() {
			JSONObject chart = new JSONObject();
			chart.put("chartId", chartId);
			try {
				JSONObject data = getChartData();
				if (data == null) {
					return null;
				}
				chart.put("data", data);
			} catch (Throwable t) {
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