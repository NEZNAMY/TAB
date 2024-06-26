package me.neznamy.tab.shared.command;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.Map.Entry;

import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.cpu.CpuReport;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Handler for "/tab cpu" subcommand
 */
public class CpuCommand extends SubCommand {

    private final DecimalFormat decimal3 = new DecimalFormat("#.###");
    private final char LINE_CHAR = 9553;

    /**
     * Constructs new instance
     */
    public CpuCommand() {
        super("cpu", TabConstants.Permission.COMMAND_CPU);
    }

    @Override
    public void execute(@Nullable TabPlayer sender, @NotNull String[] args) {
        CpuReport report = TAB.getInstance().getCPUManager().getLastReport();
        if (report == null) {
            if (TAB.getInstance().getCPUManager().enableTracking()) {
                sendMessage(sender, "&aCPU usage tracking has been enabled. Run the command again in 10 seconds to see the first results.");
            } else {
                sendMessage(sender, "&cPlease wait at least 10 seconds since running the command for the first time.");
            }
            return;
        }
        Map<String, Map<String, Float>> features = report.getFeatureUsage();
        sendMessage(sender, " ");
        sendMessage(sender, "&8&l" + LINE_CHAR + "&8&m             &r&8&l[ &bTAB CPU Stats &8&l]&r&8&l&m             ");
        sendMessage(sender, "&8&l" + LINE_CHAR + " &6CPU stats from the last 10 seconds");
        sendMessage(sender, "&8&l" + LINE_CHAR + "&8&m                                                    ");
        sendMessage(sender, "&8&l" + LINE_CHAR + " &6Top 5 placeholders:");
        printPlaceholders(sender, report.getPlaceholderUsage());
        sendMessage(sender, "&8&l" + LINE_CHAR + "&8&m                                                    ");
        if (sender != null) {
            sendToPlayer(sender, features);
        } else {
            sendToConsole(features);
        }
        sendMessage(sender, "&8&l" + LINE_CHAR + "&8&m                                                    ");
        sendMessage(sender, String.format("&8&l%s &6&lPlaceholders Total: &a&l%s%%", LINE_CHAR, colorize(decimal3.format(report.getPlaceholderUsageTotal()), 10, 5)));
        sendMessage(sender, String.format("&8&l%s &6&lPlugin internals: &a&l%s%%", LINE_CHAR, colorize(decimal3.format(report.getFeatureUsageTotal()-report.getPlaceholderUsageTotal()), 10, 5)));
        sendMessage(sender, String.format("&8&l%s &6&lTotal: &e&l%s%%", LINE_CHAR, colorize(decimal3.format(report.getFeatureUsageTotal()), 10, 5)));
        sendMessage(sender, "&8&l" + LINE_CHAR + "&8&m             &r&8&l[ &bTAB CPU Stats &8&l]&r&8&l&m             ");
        sendMessage(sender, " ");
    }

    private void printPlaceholders(@Nullable TabPlayer sender, @NotNull Map<String, Float> map) {
        int printCounter = 0;
        for (Entry<String, Float> entry : map.entrySet()) {
            if (printCounter++ == 5) break;
            String refresh = "";
            Placeholder p = TAB.getInstance().getPlaceholderManager().getPlaceholder(entry.getKey());
            if (p.getRefresh() != -1) refresh = " &8(" + p.getRefresh() + ")&7";
            String colorized = entry.getKey().startsWith("%sync:") ? "&c" + decimal3.format(entry.getValue()) : colorize(decimal3.format(entry.getValue()), 1, 0.3f);
            sendMessage(sender, String.format("&8&l%s &7%s - %s%%", LINE_CHAR, entry.getKey() + refresh, colorized));
        }
    }

    public void sendToConsole(@NotNull Map<String, Map<String, Float>> features) {
        TAB.getInstance().getPlatform().logInfo(TabComponent.fromColoredText(EnumChatFormat.color("&8&l" + LINE_CHAR + " &6Features:")));
        for (Entry<String, Map<String, Float>> entry : features.entrySet()) {
            TAB.getInstance().getPlatform().logInfo(TabComponent.fromColoredText(EnumChatFormat.color(
                    String.format("&8&l%s &7%s &7(%s%%&7):", LINE_CHAR, entry.getKey(),
                            colorize(decimal3.format(entry.getValue().values().stream().mapToDouble(Float::floatValue).sum()), 5, 1)))));
            for (Entry<String, Float> type : entry.getValue().entrySet()) {
                TAB.getInstance().getPlatform().logInfo(TabComponent.fromColoredText(EnumChatFormat.color(
                        String.format("&8&l%s     &7%s - %s%%", LINE_CHAR, type.getKey(), colorize(decimal3.format(type.getValue()), 5, 1)))));
            }
        }
    }

    public void sendToPlayer(@NotNull TabPlayer sender, @NotNull Map<String, Map<String, Float>> features) {
        sendMessage(sender, "&8&l" + LINE_CHAR + " &6Features (execute from console for more info):");
        for (Entry<String, Map<String, Float>> entry : features.entrySet()) {
            double featureTotal = entry.getValue().values().stream().mapToDouble(Float::floatValue).sum();
            String core = String.format("&8&l%s &7%s &7(%s%%&7):", LINE_CHAR, entry.getKey(), colorize(decimal3.format(featureTotal), 5, 1));
            sender.sendMessage(TabComponent.fromColoredText(EnumChatFormat.color(core)));
        }
    }

    /**
     * Returns colored usage from provided usage
     *
     * @param   usage
     *          usage
     * @return  colored usage
     */
    private String colorize(@NotNull String usage, float threshold1, float threshold2) {
        float percent = Float.parseFloat(usage.replace(",", "."));
        if (percent > threshold1) return "&c" + usage;
        if (percent > threshold2) return "&e" + usage;
        return "&a" + usage;
    }
}