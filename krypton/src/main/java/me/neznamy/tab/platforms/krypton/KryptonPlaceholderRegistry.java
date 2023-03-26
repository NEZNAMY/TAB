package me.neznamy.tab.platforms.krypton;

import lombok.RequiredArgsConstructor;
import me.lucko.spark.api.Spark;
import me.lucko.spark.api.statistic.StatisticWindow.*;
import me.lucko.spark.api.statistic.misc.DoubleAverageInfo;
import me.lucko.spark.api.statistic.types.DoubleStatistic;
import me.lucko.spark.api.statistic.types.GenericStatistic;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.placeholder.PlaceholderManager;
import me.neznamy.tab.shared.placeholders.UniversalPlaceholderRegistry;
import org.kryptonmc.api.entity.player.Player;

@RequiredArgsConstructor
public class KryptonPlaceholderRegistry extends UniversalPlaceholderRegistry {

    private final Main plugin;

    @Override
    public void registerPlaceholders(PlaceholderManager manager) {
        // Built-in stuff
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.DISPLAY_NAME, 500, p -> ((Player) p.getPlayer()).getDisplayName());
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.HEALTH, 100, p -> Math.ceil(((Player) p.getPlayer()).getHealth()));

        // Spark placeholders, registered if Spark is present
        if (plugin.getServer().getPluginManager().isLoaded("spark")) registerSparkPlaceholders(manager);
        super.registerPlaceholders(manager);
    }

    private void registerSparkPlaceholders(PlaceholderManager manager){
        Spark spark = plugin.getServer().getServicesManager().provide(Spark.class);
        if (spark == null) return;

        // TPS
        DoubleStatistic<TicksPerSecond> tps = spark.tps();
        if (tps != null) {
            manager.registerServerPlaceholder("%tps_5s%", 1000, () -> format(tps.poll(TicksPerSecond.SECONDS_5)));
            manager.registerServerPlaceholder("%tps_10s%", 1000, () -> format(tps.poll(TicksPerSecond.SECONDS_10)));
            manager.registerServerPlaceholder("%tps_1m%", 1000, () -> format(tps.poll(TicksPerSecond.MINUTES_1)));
            manager.registerServerPlaceholder("%tps_5m%", 1000, () -> format(tps.poll(TicksPerSecond.MINUTES_5)));
            manager.registerServerPlaceholder("%tps_15m%", 1000, () -> format(tps.poll(TicksPerSecond.MINUTES_15)));
        }

        // MSPT
        GenericStatistic<DoubleAverageInfo, MillisPerTick> mspt = spark.mspt();
        if (mspt != null) {
            manager.registerServerPlaceholder("%mspt_min_10s%", 1000, () -> format(mspt.poll(MillisPerTick.SECONDS_10).min()));
            manager.registerServerPlaceholder("%mspt_max_10s%", 1000, () -> format(mspt.poll(MillisPerTick.SECONDS_10).max()));
            manager.registerServerPlaceholder("%mspt_mean_10s%", 1000, () -> format(mspt.poll(MillisPerTick.SECONDS_10).mean()));
            manager.registerServerPlaceholder("%mspt_median_10s%", 1000, () -> format(mspt.poll(MillisPerTick.SECONDS_10).median()));
            manager.registerServerPlaceholder("%mspt_percentile95_10s%", 1000, () -> format(mspt.poll(MillisPerTick.SECONDS_10).percentile95th()));

            manager.registerServerPlaceholder("%mspt_min_1m%", 1000, () -> format(mspt.poll(MillisPerTick.MINUTES_1).min()));
            manager.registerServerPlaceholder("%mspt_max_1m%", 1000, () -> format(mspt.poll(MillisPerTick.MINUTES_1).max()));
            manager.registerServerPlaceholder("%mspt_mean_1m%", 1000, () -> format(mspt.poll(MillisPerTick.MINUTES_1).mean()));
            manager.registerServerPlaceholder("%mspt_median_1m%", 1000, () -> format(mspt.poll(MillisPerTick.MINUTES_1).median()));
            manager.registerServerPlaceholder("%mspt_percentile95_1m%", 1000, () -> format(mspt.poll(MillisPerTick.SECONDS_10).percentile95th()));
        }

        // CPU process
        DoubleStatistic<CpuUsage> process= spark.cpuProcess();
        manager.registerServerPlaceholder("%cpu_process_10s%", 1000, () -> format(process.poll(CpuUsage.SECONDS_10)));
        manager.registerServerPlaceholder("%cpu_process_1m%", 1000, () -> format(process.poll(CpuUsage.MINUTES_1)));
        manager.registerServerPlaceholder("%cpu_process_15m%", 1000, () -> format(process.poll(CpuUsage.MINUTES_15)));

        // CPU system
        DoubleStatistic<CpuUsage> system = spark.cpuSystem();
        manager.registerServerPlaceholder("%cpu_system_10s%", 1000, () -> format(system.poll(CpuUsage.SECONDS_10)));
        manager.registerServerPlaceholder("%cpu_system_1m%", 1000, () -> format(system.poll(CpuUsage.MINUTES_1)));
        manager.registerServerPlaceholder("%cpu_system_15m%", 1000, () -> format(system.poll(CpuUsage.MINUTES_15)));
    }
}