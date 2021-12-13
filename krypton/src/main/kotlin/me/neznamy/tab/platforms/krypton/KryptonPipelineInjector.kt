package me.neznamy.tab.platforms.krypton

import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import me.neznamy.tab.api.TabPlayer
import me.neznamy.tab.shared.TAB
import me.neznamy.tab.shared.features.PipelineInjector
import org.kryptonmc.krypton.packet.out.play.PacketOutPlayerInfo
import java.util.function.Function

class KryptonPipelineInjector : PipelineInjector("handler") {

    init {
        channelFunction = Function(::KryptonChannelDuplexHandler)
    }

    inner class KryptonChannelDuplexHandler(private val player: TabPlayer) : ChannelDuplexHandler() {

        override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
            try {
                if (TAB.getInstance().featureManager.onPacketReceive(player, msg)) return
                super.channelRead(ctx, msg)
            } catch (exception: Exception) {
                TAB.getInstance().errorManager.printError("An error occurred when reading packets", exception)
            }
        }

        override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) {
            try {
                if (msg is PacketOutPlayerInfo) {
                    super.write(ctx, TAB.getInstance().featureManager.onPacketPlayOutPlayerInfo(player, msg), promise)
                    return
                }
                TAB.getInstance().featureManager.onPacketSend(player, msg)
            } catch (exception: Exception) {
                TAB.getInstance().errorManager.printError("An error occurred when reading packets", exception)
            }
            try {
                super.write(ctx, msg, promise)
            } catch (exception: Exception) {
                TAB.getInstance().errorManager.printError("Failed to forward packet ${msg.javaClass.simpleName} to ${player.name}", exception)
            }
        }
    }
}
