package me.neznamy.tab.platforms.krypton

import me.lucko.spark.api.Spark
import me.lucko.spark.api.statistic.StatisticWindow.*
import me.neznamy.tab.api.TabConstants
import me.neznamy.tab.api.placeholder.PlaceholderManager
import me.neznamy.tab.shared.placeholders.UniversalPlaceholderRegistry
import org.kryptonmc.api.entity.player.Player
import kotlin.math.ceil

class KryptonPlaceholderRegistry(private val plugin: Main) : UniversalPlaceholderRegistry() {

    override fun registerPlaceholders(manager: PlaceholderManager) {
        // Built-in stuff
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.DISPLAY_NAME, 500) { (it.player as Player).displayName }
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.HEALTH, 100) { ceil((it.player as Player).health) }

        // Spark placeholders, registered if Spark is present
        registerSparkPlaceholders(manager)
        super.registerPlaceholders(manager)
    }

    private fun registerSparkPlaceholders(manager: PlaceholderManager) {
        val spark = plugin.server.servicesManager.provide(Spark::class.java) ?: return

        // TPS
        val tps = spark.tps()
        if (tps != null) {
            manager.registerServerPlaceholder("%tps_5s%", 1000) { format(tps.poll(TicksPerSecond.SECONDS_5)) }
            manager.registerServerPlaceholder("%tps_10s%", 1000) { format(tps.poll(TicksPerSecond.SECONDS_10)) }
            manager.registerServerPlaceholder("%tps_1m%", 1000) { format(tps.poll(TicksPerSecond.MINUTES_1)) }
            manager.registerServerPlaceholder("%tps_5m%", 1000) { format(tps.poll(TicksPerSecond.MINUTES_5)) }
            manager.registerServerPlaceholder("%tps_15m%", 1000) { format(tps.poll(TicksPerSecond.MINUTES_15)) }
        }

        // MSPT
        val mspt = spark.mspt()
        if (mspt != null) {
            manager.registerServerPlaceholder("%mspt_min_10s%", 1000) { format(mspt.poll(MillisPerTick.SECONDS_10).min()) }
            manager.registerServerPlaceholder("%mspt_max_10s%", 1000) { format(mspt.poll(MillisPerTick.SECONDS_10).max()) }
            manager.registerServerPlaceholder("%mspt_mean_10s%", 1000) { format(mspt.poll(MillisPerTick.SECONDS_10).mean()) }
            manager.registerServerPlaceholder("%mspt_median_10s%", 1000) { format(mspt.poll(MillisPerTick.SECONDS_10).median()) }
            manager.registerServerPlaceholder("%mspt_percentile95_10s%", 1000) {
                format(mspt.poll(MillisPerTick.SECONDS_10).percentile95th())
            }

            manager.registerServerPlaceholder("%mspt_min_1m%", 1000) { format(mspt.poll(MillisPerTick.MINUTES_1).min()) }
            manager.registerServerPlaceholder("%mspt_max_1m%", 1000) { format(mspt.poll(MillisPerTick.MINUTES_1).max()) }
            manager.registerServerPlaceholder("%mspt_mean_1m%", 1000) { format(mspt.poll(MillisPerTick.MINUTES_1).mean()) }
            manager.registerServerPlaceholder("%mspt_median_1m%", 1000) { format(mspt.poll(MillisPerTick.MINUTES_1).median()) }
            manager.registerServerPlaceholder("%mspt_percentile95_1m%", 1000) {
                format(mspt.poll(MillisPerTick.SECONDS_10).percentile95th())
            }
        }

        // CPU process
        val process = spark.cpuProcess()
        manager.registerServerPlaceholder("%cpu_process_10s%", 1000) { format(process.poll(CpuUsage.SECONDS_10)) }
        manager.registerServerPlaceholder("%cpu_process_1m%", 1000) { format(process.poll(CpuUsage.MINUTES_1)) }
        manager.registerServerPlaceholder("%cpu_process_15m%", 1000) { format(process.poll(CpuUsage.MINUTES_15)) }

        // CPU system
        val system = spark.cpuSystem()
        manager.registerServerPlaceholder("%cpu_system_10s%", 1000) { format(system.poll(CpuUsage.SECONDS_10)) }
        manager.registerServerPlaceholder("%cpu_system_1m%", 1000) { format(system.poll(CpuUsage.MINUTES_1)) }
        manager.registerServerPlaceholder("%cpu_system_15m%", 1000) { format(system.poll(CpuUsage.MINUTES_15)) }
    }
}
