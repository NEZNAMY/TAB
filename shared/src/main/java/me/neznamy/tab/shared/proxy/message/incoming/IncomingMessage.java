package me.neznamy.tab.shared.proxy.message.incoming;

import com.google.common.io.ByteArrayDataInput;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for plugin messages sent from backend to proxy.
 */
public interface IncomingMessage {

    /**
     * Reads byte input.
     *
     * @param   in
     *          Input to read from
     */
    void read(@NotNull ByteArrayDataInput in);

    /**
     * Processes this message for given player.
     *
     * @param   player
     *          Player to process this message for
     */
    void process(@NotNull ProxyTabPlayer player);
}
