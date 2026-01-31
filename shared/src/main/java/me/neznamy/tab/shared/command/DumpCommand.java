package me.neznamy.tab.shared.command;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.TabClickEvent;
import me.neznamy.tab.shared.chat.TabTextColor;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.chat.component.TabTextComponent;
import me.neznamy.tab.shared.config.files.Config;
import me.neznamy.tab.shared.features.types.Dumpable;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.platform.TabPlayer;
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
        data.put("general-settings", dumpGeneralSettings());
        data.put("features", dumpFeatures(analyzed));
        data.put("placeholders", TAB.getInstance().getPlaceholderManager().dump(analyzed));
        data.put("tablist", analyzed.getTabList().dump());
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
    private Object dumpGeneralSettings() {
        Map<String, Object> settings = new LinkedHashMap<>();
        Config config = TAB.getInstance().getConfiguration().getConfig();
        settings.put("assign-groups-by-permissions", config.isGroupsByPermissions());
        settings.put("primary-group-finding-list", config.getPrimaryGroupFindingList());
        settings.put("permission-refresh-interval", config.getPermissionRefreshInterval());
        settings.put("debug", config.isDebugMode());
        settings.put("mysql.enabled", config.getMysql() != null);
        if (config.getProxySupport() != null) {
            settings.put("proxy-support", "type: " + config.getProxySupport().getType());
        } else {
            settings.put("proxy-support", "Disabled");
        }

        settings.put("components", config.getComponents().getSection().getMap());
        settings.put("config-version", config.getConfig().getInt("config-version", 0));
        settings.put("compensate-for-packetevents-bug", config.isPacketEventsCompensation());
        settings.put("use-bukkit-permissions-manager", config.isBukkitPermissions());
        settings.put("use-online-uuid-in-tablist", config.isOnlineUuidInTabList());
        settings.put("server-name", config.getServerName());
        return settings;
    }

    @NotNull
    private Object dumpFeatures(@NotNull TabPlayer player) {
        Map<String, Object> features = new LinkedHashMap<>();
        List<String> featureList = Arrays.asList(
                TabConstants.Feature.BELOW_NAME,
                TabConstants.Feature.BOSS_BAR,
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