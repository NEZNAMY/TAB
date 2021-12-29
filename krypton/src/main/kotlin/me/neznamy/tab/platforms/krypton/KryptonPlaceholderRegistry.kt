package me.neznamy.tab.platforms.krypton

import me.lucko.spark.api.statistic.StatisticWindow.CpuUsage
import me.lucko.spark.api.statistic.StatisticWindow.MillisPerTick
import me.lucko.spark.api.statistic.StatisticWindow.TicksPerSecond
import me.neznamy.tab.api.TabPlayer
import me.neznamy.tab.api.placeholder.PlaceholderManager
import me.neznamy.tab.shared.TAB
import me.neznamy.tab.shared.placeholders.PlaceholderRegistry
import org.kryptonmc.api.entity.player.Player
import java.math.RoundingMode
import java.text.NumberFormat
import kotlin.math.ceil

class KryptonPlaceholderRegistry(private val plugin: Main) : PlaceholderRegistry {

    override fun registerPlaceholders(manager: PlaceholderManager) {
        // Built-in stuff
        manager.registerPlayerPlaceholder("%displayname%", 500) { (it.player as Player).displayName }
        manager.registerPlayerPlaceholder("%health%", 100) { ceil((it.player as Player).health) }

        // TPS
        val tps = plugin.server.spark.tps()!!
        manager.registerServerPlaceholder("%tps_5s%", 1000) { format(tps.poll(TicksPerSecond.SECONDS_5)) }
        manager.registerServerPlaceholder("%tps_10s%", 1000) { format(tps.poll(TicksPerSecond.SECONDS_10)) }
        manager.registerServerPlaceholder("%tps_1m%", 1000) { format(tps.poll(TicksPerSecond.MINUTES_1)) }
        manager.registerServerPlaceholder("%tps_5m%", 1000) { format(tps.poll(TicksPerSecond.MINUTES_5)) }
        manager.registerServerPlaceholder("%tps_15m%", 1000) { format(tps.poll(TicksPerSecond.MINUTES_15)) }

        // MSPT
        val mspt = plugin.server.spark.mspt()!!
        manager.registerServerPlaceholder("%mspt_min_10s%", 1000) { formatMSPT(mspt.poll(MillisPerTick.SECONDS_10).min()) }
        manager.registerServerPlaceholder("%mspt_max_10s%", 1000) { formatMSPT(mspt.poll(MillisPerTick.SECONDS_10).max()) }
        manager.registerServerPlaceholder("%mspt_mean_10s%", 1000) { formatMSPT(mspt.poll(MillisPerTick.SECONDS_10).mean()) }
        manager.registerServerPlaceholder("%mspt_median_10s%", 1000) { formatMSPT(mspt.poll(MillisPerTick.SECONDS_10).median()) }
        manager.registerServerPlaceholder("%mspt_percentile95_10s%", 1000) {
            formatMSPT(mspt.poll(MillisPerTick.SECONDS_10).percentile95th())
        }
        manager.registerServerPlaceholder("%mspt_min_1m%", 1000) { formatMSPT(mspt.poll(MillisPerTick.MINUTES_1).min()) }
        manager.registerServerPlaceholder("%mspt_max_1m%", 1000) { formatMSPT(mspt.poll(MillisPerTick.MINUTES_1).max()) }
        manager.registerServerPlaceholder("%mspt_mean_1m%", 1000) { formatMSPT(mspt.poll(MillisPerTick.MINUTES_1).mean()) }
        manager.registerServerPlaceholder("%mspt_median_1m%", 1000) { formatMSPT(mspt.poll(MillisPerTick.MINUTES_1).median()) }
        manager.registerServerPlaceholder("%mspt_percentile95_1m%", 1000) {
            formatMSPT(mspt.poll(MillisPerTick.SECONDS_10).percentile95th())
        }

        // CPU process
        val process = plugin.server.spark.cpuProcess()
        manager.registerServerPlaceholder("%cpu_process_10s%", 1000) { format(process.poll(CpuUsage.SECONDS_10)) }
        manager.registerServerPlaceholder("%cpu_process_1m%", 1000) { format(process.poll(CpuUsage.MINUTES_1)) }
        manager.registerServerPlaceholder("%cpu_process_15m%", 1000) { format(process.poll(CpuUsage.MINUTES_15)) }

        // CPU system
        val system = plugin.server.spark.cpuSystem()
        manager.registerServerPlaceholder("%cpu_system_10s%", 1000) { format(system.poll(CpuUsage.SECONDS_10)) }
        manager.registerServerPlaceholder("%cpu_system_1m%", 1000) { format(system.poll(CpuUsage.MINUTES_1)) }
        manager.registerServerPlaceholder("%cpu_system_15m%", 1000) { format(system.poll(CpuUsage.MINUTES_15)) }

        registerOnlinePlaceholders(manager)
    }

    private fun registerOnlinePlaceholders(manager: PlaceholderManager) {
        manager.registerPlayerPlaceholder("%online%", 2000) { player ->
            TAB.getInstance().onlinePlayers.count { (player.player as Player).canSee(it.player as Player) }
        }
        manager.registerPlayerPlaceholder("%staffonline%", 2000) { player ->
            TAB.getInstance().onlinePlayers.count {
                it.hasPermission("tab.staff") && (player.player as Player).canSee(it.player as Player)
            }
        }
        manager.registerPlayerPlaceholder("%nonstaffonline%", 2000) { player ->
            TAB.getInstance().onlinePlayers.count {
                !it.hasPermission("tab.staff") && (player.player as Player).canSee(it.player as Player)
            }
        }
    }

    private fun format(value: Double): String = TWO_DECIMAL_PLACES.format(value)

    private fun formatMSPT(value: Double): String = TWO_DECIMAL_PLACES_ROUNDING_DOWN.format(value)

    companion object {

        private val TWO_DECIMAL_PLACES = NumberFormat.getInstance().apply {
            maximumFractionDigits = 2
        }
        private val TWO_DECIMAL_PLACES_ROUNDING_DOWN = NumberFormat.getInstance().apply {
            roundingMode = RoundingMode.DOWN
            maximumFractionDigits = 2
        }
    }
}
