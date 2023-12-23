package me.neznamy.tab.shared.cpu;

import lombok.Getter;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Getter
public class CpuReport {

    private static final int LINE_CHAR = 9553;

    /** Active time in % from features */
    @NotNull private final Map<String, Map<String, Float>> featureUsage;

    /** Total usage of all features in % */
    private final double featureUsageTotal;

    /** Active time in % from placeholders */
    @NotNull private final Map<String, Float> placeholderUsage;

    /** Total usage of all placeholders in % */
    private final double placeholderUsageTotal;

    /** Timestamp when this report was made */
    private final long timeStamp = System.currentTimeMillis();

    /**
     * Constructs new instance with given parameters and performs calculation and ordering
     *
     * @param   updateRateSeconds
     *          How often is a new report made
     * @param   features
     *          Feature usage map
     * @param   placeholders
     *          Placeholder usage map
     */
    public CpuReport(int updateRateSeconds, @NotNull Map<String, Map<String, Long>> features, @NotNull Map<String, Long> placeholders) {
        long TIME_PERCENT = TimeUnit.SECONDS.toNanos(1) / updateRateSeconds;
        TreeMap<Long, Map.Entry<String, Map<String, Float>>> sorted = new TreeMap<>((o1, o2) -> Long.compare(o2, o1));
        features.forEach((key, val) -> {
            Map<String, Float> percent = new LinkedHashMap<>(val.size());
            long sum = val.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue((o1, o2) -> Long.compare(o2, o1)))
                    .peek(e -> percent.put(e.getKey(), (float) e.getValue() / TIME_PERCENT))
                    .mapToLong(Map.Entry::getValue)
                    .sum();
            sorted.put(sum, new AbstractMap.SimpleImmutableEntry<>(key, percent));
        });
        featureUsage = sorted.values().stream().collect(() -> new LinkedHashMap<>(features.size()),
                (m, e) -> m.put(e.getKey(), e.getValue()), Map::putAll);

        // No, it's not empty
        featureUsageTotal = featureUsage.values().stream().mapToDouble(map -> map.values().stream().mapToDouble(Float::floatValue).sum()).sum();

        placeholderUsage = placeholders.entrySet().stream().sorted(Map.Entry.comparingByValue((o1, o2) -> Long.compare(o2, o1)))
                .collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), (float) e.getValue() / TIME_PERCENT), Map::putAll);

        placeholderUsageTotal = placeholderUsage.values().stream().mapToDouble(Float::floatValue).sum();
    }

    /**
     * Prints this report into the console.
     * 
     * @param   timeDiff
     *          How long this report took to create (expected 10000ms)
     */
    public void printToConsole(long timeDiff) {
        message("Took " + timeDiff + "ms to create a CPU report, but the period is set to 10000ms. This means the plugin is overloaded. " +
                "Printing CPU report.");
        DecimalFormat decimal3 = new DecimalFormat("#.###");
        message(" ");
        message(LINE_CHAR + "             [ TAB CPU Stats ]             ");
        message(LINE_CHAR + " CPU stats from the last 10 seconds");
        message(LINE_CHAR + "                                                    ");
        
        // Placeholders
        message(LINE_CHAR + " Top 5 placeholders:");
        int printCounter = 0;
        for (Map.Entry<String, Float> entry : placeholderUsage.entrySet()) {
            if (printCounter++ == 5) break;
            String refresh = "";
            Placeholder p = TAB.getInstance().getPlaceholderManager().getPlaceholder(entry.getKey());
            if (p.getRefresh() != -1) refresh = " (" + p.getRefresh() + ")";
            message(String.format("%s %s - %s%%", LINE_CHAR, entry.getKey() + refresh, decimal3.format(entry.getValue())));
        }
        
        // Features
        message(LINE_CHAR + "                                                    ");
        message(LINE_CHAR + " Features:");
        for (Map.Entry<String, Map<String, Float>> entry : featureUsage.entrySet()) {
            double featureTotal = entry.getValue().values().stream().mapToDouble(Float::floatValue).sum();
            message(String.format("%s %s (%s%%):", LINE_CHAR, entry.getKey(), decimal3.format(featureTotal)));
            for (Map.Entry<String, Float> type : entry.getValue().entrySet()) {
                message(String.format("%s     %s - %s%%", LINE_CHAR, type.getKey(), decimal3.format(type.getValue())));
            }
        }
        
        // Totals
        message(LINE_CHAR + "                                                    ");
        message(String.format("%s Placeholders Total: %s%%", LINE_CHAR, decimal3.format(placeholderUsageTotal)));
        message(String.format("%s Plugin internals: %s%%", LINE_CHAR, decimal3.format(featureUsageTotal - placeholderUsageTotal)));
        message(String.format("%s Total: %s%%", LINE_CHAR, decimal3.format(featureUsageTotal)));
        message(LINE_CHAR + "             [ TAB CPU Stats ]             ");
        message(" ");
    }

    private void message(@NotNull String message) {
        TAB.getInstance().getPlatform().logWarn(IChatBaseComponent.fromColoredText(message));
    }
}
