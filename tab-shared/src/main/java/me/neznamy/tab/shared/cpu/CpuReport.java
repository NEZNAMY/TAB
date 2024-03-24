package me.neznamy.tab.shared.cpu;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Getter
public class CpuReport {

    /** Active time in % from features */
    @NotNull private final Map<String, Map<String, Float>> featureUsage;

    /** Total usage of all features in % */
    private final double featureUsageTotal;

    /** Active time in % from placeholders */
    @NotNull private final Map<String, Float> placeholderUsage;

    /** Total usage of all placeholders in % */
    private final double placeholderUsageTotal;

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
    public CpuReport(int updateRateSeconds, @NotNull Map<String, Map<String, AtomicLong>> features, @NotNull Map<String, AtomicLong> placeholders) {
        long TIME_PERCENT = TimeUnit.SECONDS.toNanos(1) / updateRateSeconds;
        TreeMap<Long, Map.Entry<String, Map<String, Float>>> sorted = new TreeMap<>((o1, o2) -> Long.compare(o2, o1));
        features.forEach((key, val) -> {
            Map<String, Float> percent = new LinkedHashMap<>(val.size());
            long sum = val.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue((o1, o2) -> Long.compare(o2.get(), o1.get())))
                    .peek(e -> percent.put(e.getKey(), (float) e.getValue().get() / TIME_PERCENT))
                    .mapToLong(e -> e.getValue().get())
                    .sum();
            sorted.put(sum, new AbstractMap.SimpleImmutableEntry<>(key, percent));
        });
        featureUsage = sorted.values().stream().collect(() -> new LinkedHashMap<>(features.size()),
                (m, e) -> m.put(e.getKey(), e.getValue()), Map::putAll);

        // No, it's not empty
        featureUsageTotal = featureUsage.values().stream().mapToDouble(map -> map.values().stream().mapToDouble(Float::floatValue).sum()).sum();

        placeholderUsage = placeholders.entrySet().stream().sorted(Map.Entry.comparingByValue((o1, o2) -> Long.compare(o2.get(), o1.get())))
                .collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), (float) e.getValue().get() / TIME_PERCENT), Map::putAll);

        placeholderUsageTotal = placeholderUsage.values().stream().mapToDouble(Float::floatValue).sum();
    }
}
