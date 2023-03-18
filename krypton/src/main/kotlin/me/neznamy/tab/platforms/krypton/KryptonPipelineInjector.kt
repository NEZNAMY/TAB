package me.neznamy.tab.platforms.krypton

import io.netty.channel.Channel
import me.neznamy.tab.api.TabPlayer
import me.neznamy.tab.shared.features.injection.NettyPipelineInjector
import org.kryptonmc.krypton.network.NettyConnection
import org.kryptonmc.krypton.packet.out.play.PacketOutPlayerInfoRemove
import org.kryptonmc.krypton.packet.out.play.PacketOutPlayerInfoUpdate

class KryptonPipelineInjector : NettyPipelineInjector("handler") {

    override fun onDisplayObjective(player: TabPlayer, packet: Any) {

    }

    override fun onObjective(player: TabPlayer, packet: Any) {

    }

    override fun onPlayerInfo(receiver: TabPlayer, packet: Any) {
        TODO("Not yet implemented")
    }

    override fun getChannel(player: TabPlayer?): Channel {
        val connection = (player as KryptonTabPlayer).connection()
        return NettyConnection::class.java.getDeclaredField("channel").apply { isAccessible = true }.get(connection) as Channel
    }

    override fun isDisplayObjective(packet: Any): Boolean {
        return false
    }

    override fun isObjective(packet: Any): Boolean {
        return false
    }

    override fun isTeam(packet: Any): Boolean {
        return false
    }

    override fun isPlayerInfo(packet: Any): Boolean {
        return packet is PacketOutPlayerInfoUpdate || packet is PacketOutPlayerInfoRemove
    }

    override fun isLogin(packet: Any): Boolean {
        return false
    }

    override fun modifyPlayers(teamPacket: Any) {

    }
}
