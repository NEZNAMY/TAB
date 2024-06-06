package me.neznamy.tab.shared.task;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants.CpuUsageCategory;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import me.neznamy.tab.shared.proxy.message.outgoing.OutgoingMessage;

/**
 * Task for encoding and sending plugin message to a player.
 */
@RequiredArgsConstructor
public class PluginMessageEncodeTask implements Runnable {

    /** Player to send plugin message to */
    private final ProxyTabPlayer player;

    /** Plugin message to encode and send */
    private final OutgoingMessage message;

    @Override
    public void run() {
        long time = System.nanoTime();
        byte[] msg = message.write().toByteArray();
        TAB.getInstance().getCpu().addTime("Plugin message handling", CpuUsageCategory.PLUGIN_MESSAGE_ENCODE, System.nanoTime() - time);
        time = System.nanoTime();
        player.sendPluginMessage(msg);
        TAB.getInstance().getCpu().addTime("Plugin message handling", CpuUsageCategory.PLUGIN_MESSAGE_SEND, System.nanoTime() - time);
    }
}
