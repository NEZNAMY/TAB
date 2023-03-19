package me.neznamy.tab.platforms.krypton

import me.neznamy.tab.api.BossBarHandler
import me.neznamy.tab.api.bossbar.BarColor
import me.neznamy.tab.api.bossbar.BarStyle
import net.kyori.adventure.bossbar.BossBar
import java.util.*

class KryptonBossBarHandler(private val player: KryptonTabPlayer) : BossBarHandler {

    private val bossBars = mutableMapOf<UUID, BossBar>()

    override fun create(id: UUID, title: String, progress: Float, color: BarColor, style: BarStyle) {
        if (bossBars.containsKey(id)) return
        val bar = BossBar.bossBar(
            KryptonPacketBuilder.toComponent(title, player.version),
            progress,
            BossBar.Color.valueOf(color.toString()),
            BossBar.Overlay.valueOf(style.toString())
        )
        bossBars[id] = bar
        player.player.showBossBar(bar)
    }

    override fun update(id: UUID, title: String) {
        bossBars[id]?.name(KryptonPacketBuilder.toComponent(title, player.version))
    }

    override fun update(id: UUID, progress: Float) {
        bossBars[id]?.progress(progress)
    }

    override fun update(id: UUID, style: BarStyle) {
        val bar = bossBars[id] ?: return
        bar.overlay(BossBar.Overlay.valueOf(style.toString()))
    }

    override fun update(id: UUID, color: BarColor) {
        val bar = bossBars[id] ?: return
        bar.color(BossBar.Color.valueOf(color.toString()))
    }

    override fun remove(id: UUID) {
        player.player.hideBossBar(bossBars[id] ?: return)
        bossBars.remove(id)
    }
}