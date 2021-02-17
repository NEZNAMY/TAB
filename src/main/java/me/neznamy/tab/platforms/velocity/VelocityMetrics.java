package me.neznamy.tab.platforms.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.PluginDescription;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;
import javax.net.ssl.HttpsURLConnection;
import org.slf4j.Logger;

public class VelocityMetrics {

  /** A factory to create new Metrics classes. */
  public static class Factory {

    private final ProxyServer server;

    private final Logger logger;

    private final Path dataDirectory;

    // The constructor is not meant to be called by the user.
    // The instance is created using Dependency Injection
    @Inject
    private Factory(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
      this.server = server;
      this.logger = logger;
      this.dataDirectory = dataDirectory;
    }

    /**
     * Creates a new Metrics class.
     *
     * @param plugin The plugin instance.
     * @param serviceId The id of the service. It can be found at <a
     *     href="https://bstats.org/what-is-my-plugin-id">What is my plugin id?</a>
     *     <p>Not to be confused with Velocity's {@link PluginDescription#getId()} method!
     * @return A Metrics instance that can be used to register custom charts.
     *     <p>The return value can be ignored, when you do not want to register custom charts.
     */
    public VelocityMetrics make(Object plugin, int serviceId) {
      return new VelocityMetrics(plugin, server, logger, dataDirectory, serviceId);
    }
  }

  private final PluginContainer pluginContainer;

  private final ProxyServer server;

  private MetricsBase metricsBase;

  private VelocityMetrics(
      Object plugin, ProxyServer server, Logger logger, Path dataDirectory, int serviceId) {
    pluginContainer =
        server
            .getPluginManager()
            .fromInstance(plugin)
            .orElseThrow(
                () -> new IllegalArgumentException("The provided instance is not a plugin"));
    this.server = server;
    File configFile = dataDirectory.getParent().resolve("bStats").resolve("config.txt").toFile();
    MetricsConfig config;
    try {
      config = new MetricsConfig(configFile, true);
    } catch (IOException e) {
      logger.error("Failed to create bStats config", e);
      return;
    }
    metricsBase =
        new MetricsBase(
            "velocity",
            config.getServerUUID(),
            serviceId,
            config.isEnabled(),
            this::appendPlatformData,
            this::appendServiceData,
            task -> server.getScheduler().buildTask(plugin, task).schedule(),
            () -> true,
            logger::warn,
            logger::info,
            config.isLogErrorsEnabled(),
            config.isLogSentDataEnabled(),
            config.isLogResponseStatusTextEnabled());
    if (!config.didExistBefore()) {
      // Send an info message when the bStats config file gets created for the first time
      logger.info(
          "Velocity and some of its plugins collect metrics and send them to bStats (https://bStats.org).");
      logger.info(
          "bStats collects some basic information for plugin authors, like how many people use");
      logger.info(
          "their plugin and their total player count. It's recommend to keep bStats enabled, but");
      logger.info(
          "if you're not comfortable with this, you can opt-out by editing the config.txt file in");
      logger.info("the '/plugins/bStats/' folder and setting enabled to false.");
    }
  }

  /**
   * Adds a custom chart.
   *
   * @param chart The chart to add.
   */
  public void addCustomChart(CustomChart chart) {
    if (metricsBase != null) {
      metricsBase.addCustomChart(chart);
    }
  }

  private void appendPlatformData(JsonObjectBuilder builder) {
    builder.appendField("playerAmount", server.getPlayerCount());
    builder.appendField("managedServers", server.getAllServers().size());
    builder.appendField("onlineMode", server.getConfiguration().isOnlineMode() ? 1 : 0);
    builder.appendField("velocityVersionVersion", server.getVersion().getVersion());
    builder.appendField("velocityVersionName", server.getVersion().getName());
    builder.appendField("velocityVersionVendor", server.getVersion().getVendor());
    builder.appendField("javaVersion", System.getProperty("java.version"));
    builder.appendField("osName", System.getProperty("os.name"));
    builder.appendField("osArch", System.getProperty("os.arch"));
    builder.appendField("osVersion", System.getProperty("os.version"));
    builder.appendField("coreCount", Runtime.getRuntime().availableProcessors());
  }

  private void appendServiceData(JsonObjectBuilder builder) {
    builder.appendField(
        "pluginVersion", pluginContainer.getDescription().getVersion().orElse("unknown"));
  }

  public static class MetricsBase {

    /** The version of the Metrics class. */
    public static final String METRICS_VERSION = "2.2.1";

    private static final ScheduledExecutorService scheduler =
        Executors.newScheduledThreadPool(1, task -> new Thread(task, "bStats-Metrics"));

    private static final String REPORT_URL = "https://bStats.org/api/v2/data/%s";

    private final String platform;

    private final String serverUuid;

    private final int serviceId;

    private final Consumer<JsonObjectBuilder> appendPlatformDataConsumer;

    private final Consumer<JsonObjectBuilder> appendServiceDataConsumer;

    private final Consumer<Runnable> submitTaskConsumer;

    private final Supplier<Boolean> checkServiceEnabledSupplier;

    private final BiConsumer<String, Throwable> errorLogger;

    private final Consumer<String> infoLogger;

    private final boolean logErrors;

    private final boolean logSentData;

    private final boolean logResponseStatusText;

    private final Set<CustomChart> customCharts = new HashSet<>();

    private final boolean enabled;

    /**
     * Creates a new MetricsBase class instance.
     *
     * @param platform The platform of the service.
     * @param serviceId The id of the service.
     * @param serverUuid The server uuid.
     * @param enabled Whether or not data sending is enabled.
     * @param appendPlatformDataConsumer A consumer that receives a {@code JsonObjectBuilder} and
     *     appends all platform-specific data.
     * @param appendServiceDataConsumer A consumer that receives a {@code JsonObjectBuilder} and
     *     appends all service-specific data.
     * @param submitTaskConsumer A consumer that takes a runnable with the submit task. This can be
     *     used to delegate the data collection to a another thread to prevent errors caused by
     *     concurrency. Can be {@code null}.
     * @param checkServiceEnabledSupplier A supplier to check if the service is still enabled.
     * @param errorLogger A consumer that accepts log message and an error.
     * @param infoLogger A consumer that accepts info log messages.
     * @param logErrors Whether or not errors should be logged.
     * @param logSentData Whether or not the sent data should be logged.
     * @param logResponseStatusText Whether or not the response status text should be logged.
     */
    public MetricsBase(
        String platform,
        String serverUuid,
        int serviceId,
        boolean enabled,
        Consumer<JsonObjectBuilder> appendPlatformDataConsumer,
        Consumer<JsonObjectBuilder> appendServiceDataConsumer,
        Consumer<Runnable> submitTaskConsumer,
        Supplier<Boolean> checkServiceEnabledSupplier,
        BiConsumer<String, Throwable> errorLogger,
        Consumer<String> infoLogger,
        boolean logErrors,
        boolean logSentData,
        boolean logResponseStatusText) {
      this.platform = platform;
      this.serverUuid = serverUuid;
      this.serviceId = serviceId;
      this.enabled = enabled;
      this.appendPlatformDataConsumer = appendPlatformDataConsumer;
      this.appendServiceDataConsumer = appendServiceDataConsumer;
      this.submitTaskConsumer = submitTaskConsumer;
      this.checkServiceEnabledSupplier = checkServiceEnabledSupplier;
      this.errorLogger = errorLogger;
      this.infoLogger = infoLogger;
      this.logErrors = logErrors;
      this.logSentData = logSentData;
      this.logResponseStatusText = logResponseStatusText;
      checkRelocation();
      if (enabled) {
        startSubmitting();
      }
    }

    public void addCustomChart(CustomChart chart) {
      this.customCharts.add(chart);
    }

    private void startSubmitting() {
      final Runnable submitTask =
          () -> {
            if (!enabled || !checkServiceEnabledSupplier.get()) {
              // Submitting data or service is disabled
              scheduler.shutdown();
              return;
            }
            if (submitTaskConsumer != null) {
              submitTaskConsumer.accept(this::submitData);
            } else {
              this.submitData();
            }
          };
      // Many servers tend to restart at a fixed time at xx:00 which causes an uneven distribution
      // of requests on the
      // bStats backend. To circumvent this problem, we introduce some randomness into the initial
      // and second delay.
      // WARNING: You must not modify and part of this Metrics class, including the submit delay or
      // frequency!
      // WARNING: Modifying this code will get your plugin banned on bStats. Just don't do it!
      long initialDelay = (long) (1000 * 60 * (3 + Math.random() * 3));
      long secondDelay = (long) (1000 * 60 * (Math.random() * 30));
      scheduler.schedule(submitTask, initialDelay, TimeUnit.MILLISECONDS);
      scheduler.scheduleAtFixedRate(
          submitTask, initialDelay + secondDelay, 1000 * 60 * 30, TimeUnit.MILLISECONDS);
    }

    private void submitData() {
      final JsonObjectBuilder baseJsonBuilder = new JsonObjectBuilder();
      appendPlatformDataConsumer.accept(baseJsonBuilder);
      final JsonObjectBuilder serviceJsonBuilder = new JsonObjectBuilder();
      appendServiceDataConsumer.accept(serviceJsonBuilder);
      JsonObjectBuilder.JsonObject[] chartData =
          customCharts.stream()
              .map(customChart -> customChart.getRequestJsonObject(errorLogger, logErrors))
              .filter(Objects::nonNull)
              .toArray(JsonObjectBuilder.JsonObject[]::new);
      serviceJsonBuilder.appendField("id", serviceId);
      serviceJsonBuilder.appendField("customCharts", chartData);
      baseJsonBuilder.appendField("service", serviceJsonBuilder.build());
      baseJsonBuilder.appendField("serverUUID", serverUuid);
      baseJsonBuilder.appendField("metricsVersion", METRICS_VERSION);
      JsonObjectBuilder.JsonObject data = baseJsonBuilder.build();
      scheduler.execute(
          () -> {
            try {
              // Send the data
              sendData(data);
            } catch (Exception e) {
              // Something went wrong! :(
              if (logErrors) {
                errorLogger.accept("Could not submit bStats metrics data", e);
              }
            }
          });
    }

    private void sendData(JsonObjectBuilder.JsonObject data) throws Exception {
      if (logSentData) {
        infoLogger.accept("Sent bStats metrics data: " + data.toString());
      }
      String url = String.format(REPORT_URL, platform);
      HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
      // Compress the data to save bandwidth
      byte[] compressedData = compress(data.toString());
      connection.setRequestMethod("POST");
      connection.addRequestProperty("Accept", "application/json");
      connection.addRequestProperty("Connection", "close");
      connection.addRequestProperty("Content-Encoding", "gzip");
      connection.addRequestProperty("Content-Length", String.valueOf(compressedData.length));
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setRequestProperty("User-Agent", "Metrics-Service/1");
      connection.setDoOutput(true);
      try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
        outputStream.write(compressedData);
      }
      StringBuilder builder = new StringBuilder();
      try (BufferedReader bufferedReader =
          new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
        String line;
        while ((line = bufferedReader.readLine()) != null) {
          builder.append(line);
        }
      }
      if (logResponseStatusText) {
        infoLogger.accept("Sent data to bStats and received response: " + builder);
      }
    }

    /** Checks that the class was properly relocated. */
    private void checkRelocation() {
      // You can use the property to disable the check in your test environment
      if (System.getProperty("bstats.relocatecheck") == null
          || !System.getProperty("bstats.relocatecheck").equals("false")) {
        // Maven's Relocate is clever and changes strings, too. So we have to use this little
        // "trick" ... :D
        final String defaultPackage =
            new String(new byte[] {'o', 'r', 'g', '.', 'b', 's', 't', 'a', 't', 's'});
        final String examplePackage =
            new String(new byte[] {'y', 'o', 'u', 'r', '.', 'p', 'a', 'c', 'k', 'a', 'g', 'e'});
        // We want to make sure no one just copy & pastes the example and uses the wrong package
        // names
        if (MetricsBase.class.getPackage().getName().startsWith(defaultPackage)
            || MetricsBase.class.getPackage().getName().startsWith(examplePackage)) {
          throw new IllegalStateException("bStats Metrics class has not been relocated correctly!");
        }
      }
    }

    /**
     * Gzips the given string.
     *
     * @param str The string to gzip.
     * @return The gzipped string.
     */
    private static byte[] compress(final String str) throws IOException {
      if (str == null) {
        return null;
      }
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      try (GZIPOutputStream gzip = new GZIPOutputStream(outputStream)) {
        gzip.write(str.getBytes(StandardCharsets.UTF_8));
      }
      return outputStream.toByteArray();
    }
  }

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
    protected JsonObjectBuilder.JsonObject getChartData() throws Exception {
      JsonObjectBuilder valuesBuilder = new JsonObjectBuilder();
      Map<String, int[]> map = callable.call();
      if (map == null || map.isEmpty()) {
        // Null = skip the chart
        return null;
      }
      boolean allSkipped = true;
      for (Map.Entry<String, int[]> entry : map.entrySet()) {
        if (entry.getValue().length == 0) {
          // Skip this invalid
          continue;
        }
        allSkipped = false;
        valuesBuilder.appendField(entry.getKey(), entry.getValue());
      }
      if (allSkipped) {
        // Null = skip the chart
        return null;
      }
      return new JsonObjectBuilder().appendField("values", valuesBuilder.build()).build();
    }
  }

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
    protected JsonObjectBuilder.JsonObject getChartData() throws Exception {
      JsonObjectBuilder valuesBuilder = new JsonObjectBuilder();
      Map<String, Integer> map = callable.call();
      if (map == null || map.isEmpty()) {
        // Null = skip the chart
        return null;
      }
      for (Map.Entry<String, Integer> entry : map.entrySet()) {
        valuesBuilder.appendField(entry.getKey(), new int[] {entry.getValue()});
      }
      return new JsonObjectBuilder().appendField("values", valuesBuilder.build()).build();
    }
  }

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
    protected JsonObjectBuilder.JsonObject getChartData() throws Exception {
      JsonObjectBuilder valuesBuilder = new JsonObjectBuilder();
      Map<String, Integer> map = callable.call();
      if (map == null || map.isEmpty()) {
        // Null = skip the chart
        return null;
      }
      boolean allSkipped = true;
      for (Map.Entry<String, Integer> entry : map.entrySet()) {
        if (entry.getValue() == 0) {
          // Skip this invalid
          continue;
        }
        allSkipped = false;
        valuesBuilder.appendField(entry.getKey(), entry.getValue());
      }
      if (allSkipped) {
        // Null = skip the chart
        return null;
      }
      return new JsonObjectBuilder().appendField("values", valuesBuilder.build()).build();
    }
  }

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
    protected JsonObjectBuilder.JsonObject getChartData() throws Exception {
      JsonObjectBuilder valuesBuilder = new JsonObjectBuilder();
      Map<String, Integer> map = callable.call();
      if (map == null || map.isEmpty()) {
        // Null = skip the chart
        return null;
      }
      boolean allSkipped = true;
      for (Map.Entry<String, Integer> entry : map.entrySet()) {
        if (entry.getValue() == 0) {
          // Skip this invalid
          continue;
        }
        allSkipped = false;
        valuesBuilder.appendField(entry.getKey(), entry.getValue());
      }
      if (allSkipped) {
        // Null = skip the chart
        return null;
      }
      return new JsonObjectBuilder().appendField("values", valuesBuilder.build()).build();
    }
  }

  public abstract static class CustomChart {

    private final String chartId;

    protected CustomChart(String chartId) {
      if (chartId == null) {
        throw new IllegalArgumentException("chartId must not be null");
      }
      this.chartId = chartId;
    }

    public JsonObjectBuilder.JsonObject getRequestJsonObject(
        BiConsumer<String, Throwable> errorLogger, boolean logErrors) {
      JsonObjectBuilder builder = new JsonObjectBuilder();
      builder.appendField("chartId", chartId);
      try {
        JsonObjectBuilder.JsonObject data = getChartData();
        if (data == null) {
          // If the data is null we don't send the chart.
          return null;
        }
        builder.appendField("data", data);
      } catch (Throwable t) {
        if (logErrors) {
          errorLogger.accept("Failed to get data for custom chart with id " + chartId, t);
        }
        return null;
      }
      return builder.build();
    }

    protected abstract JsonObjectBuilder.JsonObject getChartData() throws Exception;
  }

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
    protected JsonObjectBuilder.JsonObject getChartData() throws Exception {
      int value = callable.call();
      if (value == 0) {
        // Null = skip the chart
        return null;
      }
      return new JsonObjectBuilder().appendField("value", value).build();
    }
  }

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
    protected JsonObjectBuilder.JsonObject getChartData() throws Exception {
      String value = callable.call();
      if (value == null || value.isEmpty()) {
        // Null = skip the chart
        return null;
      }
      return new JsonObjectBuilder().appendField("value", value).build();
    }
  }

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
    public JsonObjectBuilder.JsonObject getChartData() throws Exception {
      JsonObjectBuilder valuesBuilder = new JsonObjectBuilder();
      Map<String, Map<String, Integer>> map = callable.call();
      if (map == null || map.isEmpty()) {
        // Null = skip the chart
        return null;
      }
      boolean reallyAllSkipped = true;
      for (Map.Entry<String, Map<String, Integer>> entryValues : map.entrySet()) {
        JsonObjectBuilder valueBuilder = new JsonObjectBuilder();
        boolean allSkipped = true;
        for (Map.Entry<String, Integer> valueEntry : map.get(entryValues.getKey()).entrySet()) {
          valueBuilder.appendField(valueEntry.getKey(), valueEntry.getValue());
          allSkipped = false;
        }
        if (!allSkipped) {
          reallyAllSkipped = false;
          valuesBuilder.appendField(entryValues.getKey(), valueBuilder.build());
        }
      }
      if (reallyAllSkipped) {
        // Null = skip the chart
        return null;
      }
      return new JsonObjectBuilder().appendField("values", valuesBuilder.build()).build();
    }
  }

  /**
   * An extremely simple JSON builder.
   *
   * <p>While this class is neither feature-rich nor the most performant one, it's sufficient enough
   * for its use-case.
   */
  public static class JsonObjectBuilder {

    private StringBuilder builder = new StringBuilder();

    private boolean hasAtLeastOneField = false;

    public JsonObjectBuilder() {
      builder.append("{");
    }

    /**
     * Appends a null field to the JSON.
     *
     * @param key The key of the field.
     * @return A reference to this object.
     */
    public JsonObjectBuilder appendNull(String key) {
      appendFieldUnescaped(key, "null");
      return this;
    }

    /**
     * Appends a string field to the JSON.
     *
     * @param key The key of the field.
     * @param value The value of the field.
     * @return A reference to this object.
     */
    public JsonObjectBuilder appendField(String key, String value) {
      if (value == null) {
        throw new IllegalArgumentException("JSON value must not be null");
      }
      appendFieldUnescaped(key, "\"" + escape(value) + "\"");
      return this;
    }

    /**
     * Appends an integer field to the JSON.
     *
     * @param key The key of the field.
     * @param value The value of the field.
     * @return A reference to this object.
     */
    public JsonObjectBuilder appendField(String key, int value) {
      appendFieldUnescaped(key, String.valueOf(value));
      return this;
    }

    /**
     * Appends an object to the JSON.
     *
     * @param key The key of the field.
     * @param object The object.
     * @return A reference to this object.
     */
    public JsonObjectBuilder appendField(String key, JsonObject object) {
      if (object == null) {
        throw new IllegalArgumentException("JSON object must not be null");
      }
      appendFieldUnescaped(key, object.toString());
      return this;
    }

    /**
     * Appends a string array to the JSON.
     *
     * @param key The key of the field.
     * @param values The string array.
     * @return A reference to this object.
     */
    public JsonObjectBuilder appendField(String key, String[] values) {
      if (values == null) {
        throw new IllegalArgumentException("JSON values must not be null");
      }
      String escapedValues =
          Arrays.stream(values)
              .map(value -> "\"" + escape(value) + "\"")
              .collect(Collectors.joining(","));
      appendFieldUnescaped(key, "[" + escapedValues + "]");
      return this;
    }

    /**
     * Appends an integer array to the JSON.
     *
     * @param key The key of the field.
     * @param values The integer array.
     * @return A reference to this object.
     */
    public JsonObjectBuilder appendField(String key, int[] values) {
      if (values == null) {
        throw new IllegalArgumentException("JSON values must not be null");
      }
      String escapedValues =
          Arrays.stream(values).mapToObj(String::valueOf).collect(Collectors.joining(","));
      appendFieldUnescaped(key, "[" + escapedValues + "]");
      return this;
    }

    /**
     * Appends an object array to the JSON.
     *
     * @param key The key of the field.
     * @param values The integer array.
     * @return A reference to this object.
     */
    public JsonObjectBuilder appendField(String key, JsonObject[] values) {
      if (values == null) {
        throw new IllegalArgumentException("JSON values must not be null");
      }
      String escapedValues =
          Arrays.stream(values).map(JsonObject::toString).collect(Collectors.joining(","));
      appendFieldUnescaped(key, "[" + escapedValues + "]");
      return this;
    }

    /**
     * Appends a field to the object.
     *
     * @param key The key of the field.
     * @param escapedValue The escaped value of the field.
     */
    private void appendFieldUnescaped(String key, String escapedValue) {
      if (builder == null) {
        throw new IllegalStateException("JSON has already been built");
      }
      if (key == null) {
        throw new IllegalArgumentException("JSON key must not be null");
      }
      if (hasAtLeastOneField) {
        builder.append(",");
      }
      builder.append("\"").append(escape(key)).append("\":").append(escapedValue);
      hasAtLeastOneField = true;
    }

    /**
     * Builds the JSON string and invalidates this builder.
     *
     * @return The built JSON string.
     */
    public JsonObject build() {
      if (builder == null) {
        throw new IllegalStateException("JSON has already been built");
      }
      JsonObject object = new JsonObject(builder.append("}").toString());
      builder = null;
      return object;
    }

    /**
     * Escapes the given string like stated in https://www.ietf.org/rfc/rfc4627.txt.
     *
     * <p>This method escapes only the necessary characters '"', '\'. and '\u0000' - '\u001F'.
     * Compact escapes are not used (e.g., '\n' is escaped as "\u000a" and not as "\n").
     *
     * @param value The value to escape.
     * @return The escaped value.
     */
    private static String escape(String value) {
      final StringBuilder builder = new StringBuilder();
      for (int i = 0; i < value.length(); i++) {
        char c = value.charAt(i);
        if (c == '"') {
          builder.append("\\\"");
        } else if (c == '\\') {
          builder.append("\\\\");
        } else if (c <= '\u000F') {
          builder.append("\\u000").append(Integer.toHexString(c));
        } else if (c <= '\u001F') {
          builder.append("\\u00").append(Integer.toHexString(c));
        } else {
          builder.append(c);
        }
      }
      return builder.toString();
    }

    /**
     * A super simple representation of a JSON object.
     *
     * <p>This class only exists to make methods of the {@link JsonObjectBuilder} type-safe and not
     * allow a raw string inputs for methods like {@link JsonObjectBuilder#appendField(String,
     * JsonObject)}.
     */
    public static class JsonObject {

      private final String value;

      private JsonObject(String value) {
        this.value = value;
      }

      @Override
      public String toString() {
        return value;
      }
    }
  }

  /**
   * A simple config for bStats.
   *
   * <p>This class is not used by every platform.
   */
  public static class MetricsConfig {

    private final File file;

    private final boolean defaultEnabled;

    private String serverUUID;

    private boolean enabled;

    private boolean logErrors;

    private boolean logSentData;

    private boolean logResponseStatusText;

    private boolean didExistBefore = true;

    public MetricsConfig(File file, boolean defaultEnabled) throws IOException {
      this.file = file;
      this.defaultEnabled = defaultEnabled;
      setupConfig();
    }

    public String getServerUUID() {
      return serverUUID;
    }

    public boolean isEnabled() {
      return enabled;
    }

    public boolean isLogErrorsEnabled() {
      return logErrors;
    }

    public boolean isLogSentDataEnabled() {
      return logSentData;
    }

    public boolean isLogResponseStatusTextEnabled() {
      return logResponseStatusText;
    }

    /**
     * Checks whether the config file did exist before or not.
     *
     * @return If the config did exist before.
     */
    public boolean didExistBefore() {
      return didExistBefore;
    }

    /** Creates the config file if it does not exist and read its content. */
    private void setupConfig() throws IOException {
      if (!file.exists()) {
        // Looks like it's the first time we create it (or someone deleted it).
        didExistBefore = false;
        writeConfig();
      }
      readConfig();
      if (serverUUID == null) {
        // Found a malformed config file with no UUID. Let's recreate it.
        writeConfig();
        readConfig();
      }
    }

    /** Creates a config file with teh default content. */
    private void writeConfig() throws IOException {
      List<String> configContent = new ArrayList<>();
      configContent.add(
          "# bStats (https://bStats.org) collects some basic information for plugin authors, like");
      configContent.add(
          "# how many people use their plugin and their total player count. It's recommended to keep");
      configContent.add(
          "# bStats enabled, but if you're not comfortable with this, you can turn this setting off.");
      configContent.add(
          "# There is no performance penalty associated with having metrics enabled, and data sent to");
      configContent.add("# bStats is fully anonymous.");
      configContent.add("enabled=" + defaultEnabled);
      configContent.add("server-uuid=" + UUID.randomUUID().toString());
      configContent.add("log-errors=false");
      configContent.add("log-sent-data=false");
      configContent.add("log-response-status-text=false");
      writeFile(file, configContent);
    }

    /** Reads the content of the config file. */
    private void readConfig() throws IOException {
      List<String> lines = readFile(file);
      if (lines == null) {
        throw new AssertionError("Content of newly created file is null");
      }
      enabled = getConfigValue("enabled", lines).map("true"::equals).orElse(true);
      serverUUID = getConfigValue("server-uuid", lines).orElse(null);
      logErrors = getConfigValue("log-errors", lines).map("true"::equals).orElse(false);
      logSentData = getConfigValue("log-sent-data", lines).map("true"::equals).orElse(false);
      logResponseStatusText =
          getConfigValue("log-response-status-text", lines).map("true"::equals).orElse(false);
    }

    /**
     * Gets a config setting from the given list of lines of the file.
     *
     * @param key The key for the setting.
     * @param lines The lines of the file.
     * @return The value of the setting.
     */
    private Optional<String> getConfigValue(String key, List<String> lines) {
      return lines.stream()
          .filter(line -> line.startsWith(key + "="))
          .map(line -> line.replaceFirst(Pattern.quote(key + "="), ""))
          .findFirst();
    }

    /**
     * Reads the text content of the given file.
     *
     * @param file The file to read.
     * @return The lines of the given file.
     */
    private List<String> readFile(File file) throws IOException {
      if (!file.exists()) {
        return null;
      }
      try (FileReader fileReader = new FileReader(file);
          BufferedReader bufferedReader = new BufferedReader(fileReader)) {
        return bufferedReader.lines().collect(Collectors.toList());
      }
    }

    /**
     * Writes the given lines to the given file.
     *
     * @param file The file to write to.
     * @param lines The lines to write.
     */
    private void writeFile(File file, List<String> lines) throws IOException {
      if (!file.exists()) {
        file.getParentFile().mkdirs();
        file.createNewFile();
      }
      try (FileWriter fileWriter = new FileWriter(file);
          BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
        for (String line : lines) {
          bufferedWriter.write(line);
          bufferedWriter.newLine();
        }
      }
    }
  }
}