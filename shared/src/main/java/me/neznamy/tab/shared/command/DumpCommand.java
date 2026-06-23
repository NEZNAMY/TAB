package me.neznamy.tab.shared.command;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.TabClickEvent;
import me.neznamy.tab.shared.chat.TabTextColor;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.chat.component.TabTextComponent;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.features.types.Dumpable;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.hook.MiniMessageHook;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Handler for "/tab dump" subcommand
 */
public class DumpCommand extends SubCommand {

    /**
     * Constructs new instance
     */
    public DumpCommand() {
        super("dump", TabConstants.Permission.COMMAND_DUMP);
    }

    @Override
    public void execute(@Nullable TabPlayer sender, @NotNull String[] args) {
        if (args.length > 0) {
            TabPlayer analyzed = TAB.getInstance().getPlayer(args[0]);
            if (analyzed == null) {
                sendMessage(sender, getMessages().getPlayerNotFound(args[0]));
                return;
            }
            new Thread(() -> dump(sender, analyzed)).start();
        } else {
            sendMessage(sender, "&cUsage: /tab dump <player>");
        }
    }

    private void dump(@Nullable TabPlayer sender, @NotNull TabPlayer analyzed) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("platform", TAB.getInstance().getPlatform().dump());
        data.put("player", analyzed.dump());
        data.put("features", dumpFeatures(analyzed));
        data.put("minimessage", dumpMiniMessage());
        data.put("placeholders", TAB.getInstance().getPlaceholderManager().dump(analyzed));
        data.put("tablist", analyzed.getTabList().dump());
        data.put("files", dumpFiles());
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setWidth(Integer.MAX_VALUE);

        Yaml yaml = new Yaml(options);
        String output = yaml.dump(data);
        try {
            sendMessage(sender, "&eUploading dump...");
            String url = upload(output);
            TabComponent urlComponent = new TabTextComponent(url, TabTextColor.YELLOW);
            urlComponent.getModifier().setClickEvent(new TabClickEvent(TabClickEvent.Action.OPEN_URL, url));
            sendMessage(sender, new TabTextComponent("", Arrays.asList(
                    new TabTextComponent("Dump uploaded: ", TabTextColor.GREEN),
                    urlComponent
            )));
        } catch (Exception e) {
            sendMessage(sender, "&cAn error occurred while uploading the dump, check console for more info.");
            TAB.getInstance().getErrorManager().criticalError("Failed to upload dump", e);
        }
    }

    @NotNull
    private Map<String, Object> dumpFeatures(@NotNull TabPlayer player) {
        Map<String, Object> features = new LinkedHashMap<>();
        List<String> featureList = Arrays.asList(
                TabConstants.Feature.BELOW_NAME,
                TabConstants.Feature.BOSS_BAR,
                TabConstants.Feature.GLOBAL_PLAYER_LIST,
                TabConstants.Feature.HEADER_FOOTER,
                TabConstants.Feature.LAYOUT,
                TabConstants.Feature.NAME_TAGS,
                TabConstants.Feature.PING_SPOOF,
                TabConstants.Feature.PLAYER_LIST,
                TabConstants.Feature.YELLOW_NUMBER,
                TabConstants.Feature.SCOREBOARD,
                TabConstants.Feature.SORTING,
                TabConstants.Feature.SPECTATOR_FIX
        );
        for (String featureName : featureList) {
            TabFeature feature = TAB.getInstance().getFeatureManager().getFeature(featureName);
            if (feature instanceof Dumpable) {
                features.put(featureName, ((Dumpable) feature).dump(player));
            } else if (feature == null) {
                features.put(featureName, "Feature is disabled");
            } else {
                features.put(featureName, "Feature is not dumpable"); // Should never happen
            }
        }
        return features;
    }

    @NotNull
    private Map<String, Object> dumpMiniMessage() {
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("support is enabled in config", TAB.getInstance().getConfiguration().getConfig().getComponents().isMinimessageSupport());
        content.put("MiniMessage library is available", ReflectionUtils.classExists("net.kyori.adventure.text.minimessage.MiniMessage"));
        content.put("TAB's MiniMessage instance", String.valueOf(MiniMessageHook.getMiniMessage()));
        content.put("... and therefore MiniMessage works", MiniMessageHook.isAvailable());
        return content;
    }

    @NotNull
    private Map<String, Object> dumpFiles() {
        Map<String, Object> files = new LinkedHashMap<>();
        Configs configs = TAB.getInstance().getConfiguration();
        files.put("animations.yml", configs.getAnimations().getAnimationFile().getValues());
        Map<Object, Object> configClone = deepCopy(configs.getConfig().getConfig().getValues());
        censorConfig(configClone);
        files.put("config.yml", configClone);
        if (configs.getMysql() == null) {
            files.put("groups.yml", configs.getGroupsFile().getValues());
            files.put("users.yml", configs.getUsersFile().getValues());
        } else {
            files.put("groups.yml", "MySQL connection enabled, groups are in database");
            files.put("users.yml", "MySQL connection enabled, users are in database");
        }
        return files;
    }

    @SuppressWarnings("unchecked")
    private void censorConfig(@NotNull Map<Object, Object> config) {
        Map<String, Object> mysql = (Map<String, Object>) config.get("mysql");
        mysql.put("host", "*CENSORED*");
        mysql.put("database", "*CENSORED*");
        mysql.put("username", "*CENSORED*");
        mysql.put("password", "*CENSORED*");
        Map<String, Object> proxySupport = (Map<String, Object>) config.get("proxy-support");
        Map<String, Object> redis = (Map<String, Object>) proxySupport.get("redis");
        redis.put("url", "*CENSORED*");
        Map<String, Object> rabbitmq = (Map<String, Object>) proxySupport.get("rabbitmq");
        rabbitmq.put("url", "*CENSORED*");
    }

    @NotNull
    @SuppressWarnings("unchecked")
    private Map<Object, Object> deepCopy(@NotNull Map<Object, Object> original) {
        Map<Object, Object> copy = new LinkedHashMap<>();
        for (Map.Entry<Object, Object> entry : original.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map) {
                copy.put(entry.getKey(), deepCopy((Map<Object, Object>) value));
            } else if (value instanceof List) {
                copy.put(entry.getKey(), deepCopy((List<?>) value));
            } else {
                copy.put(entry.getKey(), value);
            }
        }
        return copy;
    }

    @NotNull
    @SuppressWarnings("unchecked")
    private List<?> deepCopy(@NotNull List<?> original) {
        List<Object> copy = new ArrayList<>();
        for (Object item : original) {
            if (item instanceof Map) {
                copy.add(deepCopy((Map<Object, Object>) item));
            } else if (item instanceof List) {
                copy.add(deepCopy((List<?>) item));
            } else {
                copy.add(item);
            }
        }
        return copy;
    }

    @NotNull
    private String upload(@NotNull String content) throws Exception {
        URL url = new URL("https://api.pastes.dev/post");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "text/log; charset=UTF-8");

        try (OutputStream os = connection.getOutputStream()) {
            os.write(content.getBytes(StandardCharsets.UTF_8));
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) response.append(inputLine);
        in.close();

        String responseString = response.toString();
        String id = responseString.substring(responseString.indexOf("\"key\":\"") + 7, responseString.indexOf('"', responseString.indexOf("\"key\":\"") + 7));

        return "https://pastes.dev/" + id;
    }

    @Override
    public @NotNull List<String> complete(@Nullable TabPlayer sender, @NotNull String[] arguments) {
        return arguments.length == 1 ? getOnlinePlayers(arguments[0]) : new ArrayList<>();
    }
}