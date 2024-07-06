package me.neznamy.tab.shared.task;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import me.neznamy.tab.shared.proxy.message.incoming.IncomingMessage;

/**
 * Task for processing incoming plugin messages from backend server.
 */
@RequiredArgsConstructor
public class PluginMessageProcessTask implements Runnable {

    /** Decoded plugin message */
    private final IncomingMessage message;

    /** Player who received the plugin message */
    private final ProxyTabPlayer player;

    @Override
    public void run() {
        message.process(player);
    }
}
