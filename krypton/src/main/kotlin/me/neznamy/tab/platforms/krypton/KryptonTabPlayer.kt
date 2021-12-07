package me.neznamy.tab.platforms.krypton

import me.neznamy.tab.api.ProtocolVersion
import me.neznamy.tab.api.chat.IChatBaseComponent
import me.neznamy.tab.api.protocol.PacketPlayOutBoss
import me.neznamy.tab.api.protocol.PacketPlayOutBoss.Action
import me.neznamy.tab.api.protocol.PacketPlayOutChat
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerListHeaderFooter
import me.neznamy.tab.shared.ITabPlayer
import me.neznamy.tab.shared.TAB
import net.kyori.adventure.audience.MessageType
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.bossbar.BossBar.Color
import net.kyori.adventure.bossbar.BossBar.Flag
import net.kyori.adventure.bossbar.BossBar.Overlay
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.kryptonmc.api.entity.player.Player
import org.kryptonmc.api.registry.Registries
import org.kryptonmc.krypton.entity.player.KryptonPlayer
import org.kryptonmc.krypton.packet.Packet
import java.util.UUID

class KryptonTabPlayer(
    delegate: Player,
    protocolVersion: Int
) : ITabPlayer(delegate, delegate.uuid, delegate.profile.name, "N/A", delegate.world.name) {

    private val delegate = delegate as KryptonPlayer
    private val bossBars = mutableMapOf<UUID, BossBar>()

    init {
        channel = this.delegate.session.channel
        version = ProtocolVersion.fromNetworkId(protocolVersion)
    }

    override fun hasPermission(permission: String): Boolean = delegate.hasPermission(permission)

    override fun getPing(): Int {
        val latency = delegate.session.latency
        if (latency > 10000 || latency < 0) return -1
        return latency
    }

    override fun sendPacket(packet: Any?) {
        if (packet == null) return
        try {
            if (packet is Packet) {
                delegate.session.send(packet)
                return
            }
            when (packet) {
                is PacketPlayOutBoss -> handle(packet)
                is PacketPlayOutChat -> handle(packet)
                is PacketPlayOutPlayerListHeaderFooter -> handle(packet)
            }
        } catch (exception: Exception) {
            TAB.getInstance().errorManager.printError("An error occurred when sending ${packet.javaClass.simpleName}", exception)
        }
    }

    override fun hasInvisibilityPotion(): Boolean = false

    override fun isDisguised(): Boolean = false

    override fun getSkin(): Any = delegate.profile.properties

    override fun getPlayer(): Player = delegate

    override fun isOnline(): Boolean = delegate.isOnline

    override fun isVanished(): Boolean = delegate.isVanished

    override fun getGamemode(): Int = delegate.gameMode.ordinal

    private fun handle(packet: PacketPlayOutBoss) {
        when (packet.operation) {
            Action.ADD -> {
                if (bossBars.containsKey(packet.id)) return
                val bar = BossBar.bossBar(
                    GsonComponentSerializer.gson().deserialize(IChatBaseComponent.optimizedComponent(packet.name).toString(getVersion())),
                    packet.pct,
                    Color.valueOf(packet.color.toString()),
                    Overlay.valueOf(packet.overlay.toString())
                )
                if (packet.isCreateWorldFog) bar.addFlag(Flag.CREATE_WORLD_FOG)
                if (packet.isDarkenScreen) bar.addFlag(Flag.DARKEN_SCREEN)
                if (packet.isPlayMusic) bar.addFlag(Flag.PLAY_BOSS_MUSIC)
                bossBars[packet.id] = bar
                delegate.showBossBar(bar)
            }
            Action.REMOVE -> {
                delegate.hideBossBar(bossBars[packet.id] ?: return)
                bossBars.remove(packet.id)
            }
            Action.UPDATE_PCT -> bossBars[packet.id]?.progress(packet.pct)
            Action.UPDATE_NAME -> bossBars[packet.id]?.name(LegacyComponentSerializer.legacySection().deserialize(packet.name))
            Action.UPDATE_STYLE -> {
                val bar = bossBars[packet.id] ?: return
                bar.overlay(Overlay.valueOf(packet.overlay.toString()))
                bar.color(Color.valueOf(packet.color.toString()))
            }
            Action.UPDATE_PROPERTIES -> {
                val bar = bossBars[packet.id] ?: return
                processFlag(bar, packet.isCreateWorldFog, Flag.CREATE_WORLD_FOG)
                processFlag(bar, packet.isDarkenScreen, Flag.DARKEN_SCREEN)
                processFlag(bar, packet.isPlayMusic, Flag.PLAY_BOSS_MUSIC)
            }
            else -> Unit
        }
    }

    private fun handle(packet: PacketPlayOutChat) {
        val message = GsonComponentSerializer.gson().deserialize(packet.message.toString(version))
        if (packet.type === PacketPlayOutChat.ChatMessageType.GAME_INFO) {
            // Adventure recommends that we send action bars for GAME_INFO, which is why GAME_INFO isn't in MessageType
            delegate.sendActionBar(message)
            return
        }
        delegate.sendMessage(message, MessageType.valueOf(packet.type.name))
    }

    private fun handle(packet: PacketPlayOutPlayerListHeaderFooter) {
        val header = GsonComponentSerializer.gson().deserialize(packet.header.toString(version))
        val footer = GsonComponentSerializer.gson().deserialize(packet.footer.toString(version))
        delegate.sendPlayerListHeaderAndFooter(header, footer)
    }

    private fun processFlag(bar: BossBar, targetValue: Boolean, flag: Flag) {
        if (targetValue) {
            if (!bar.hasFlag(flag)) bar.addFlag(flag)
        } else {
            if (bar.hasFlag(flag)) bar.removeFlag(flag)
        }
    }
}
