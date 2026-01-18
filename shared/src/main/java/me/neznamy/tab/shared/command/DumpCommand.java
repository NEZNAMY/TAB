package me.neznamy.tab.shared.command;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
            dump(sender, analyzed);
        } else {
            sendMessage(sender, "&cUsage: /tab dump <player>");
        }
    }

    private void dump(@Nullable TabPlayer sender, @NotNull TabPlayer analyzed) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("tablist", analyzed.getTabList().dump());
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setWidth(Integer.MAX_VALUE);

        Yaml yaml = new Yaml(options);
        String output = yaml.dump(data);
        try {
            String url = upload(output);
            sendMessage(sender, "&aDump uploaded: &e" + url);
        } catch (Exception e) {
            sendMessage(sender, "&cAn error occurred while uploading the dump, check console for more info.");
            e.printStackTrace();
        }
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