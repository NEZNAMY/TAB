package me.neznamy.tab.platforms.krypton

import io.netty.channel.Channel
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import me.neznamy.tab.api.TabPlayer
import me.neznamy.tab.shared.TAB
import me.neznamy.tab.shared.features.injection.NettyPipelineInjector
import org.kryptonmc.krypton.network.NettyConnection
import org.kryptonmc.krypton.packet.out.play.PacketOutPlayerInfoRemove
import org.kryptonmc.krypton.packet.out.play.PacketOutPlayerInfoUpdate
import java.util.function.Function

class KryptonPipelineInjector : NettyPipelineInjector("handler") {

    override fun getChannelFunction(): Function<TabPlayer, ChannelDuplexHandler> = Function(::KryptonChannelDuplexHandler)

    override fun getChannel(player: TabPlayer?): Channel {
        val connection = (player as KryptonTabPlayer).connection()
        return NettyConnection::class.java.getDeclaredField("channel").apply { isAccessible = true }.get(connection) as Channel
    }

    inner class KryptonChannelDuplexHandler(private val player: TabPlayer) : ChannelDuplexHandler() {

        override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
            try {
                if (TAB.getInstance().featureManager.onPacketReceive(player, msg)) return
                super.channelRead(ctx, msg)
            } catch (exception: Throwable) {
                TAB.getInstance().errorManager.printError("An error occurred when reading packets", exception)
            }
        }

        override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) {
            try {
                if (msg is PacketOutPlayerInfoUpdate || msg is PacketOutPlayerInfoRemove) {
                    super.write(ctx, TAB.getInstance().featureManager.onPacketPlayOutPlayerInfo(player, msg), promise)
                    return
                }
                TAB.getInstance().featureManager.onPacketSend(player, msg)
            } catch (exception: Exception) {
                TAB.getInstance().errorManager.printError("An error occurred when reading packets", exception)
            }
            try {
                super.write(ctx, msg, promise)
            } catch (exception: Throwable) {
                TAB.getInstance().errorManager.printError("Failed to forward packet ${msg.javaClass.simpleName} to ${player.name}", exception)
            }
        }
    }
}
