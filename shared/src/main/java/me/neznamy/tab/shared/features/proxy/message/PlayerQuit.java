package me.neznamy.tab.shared.features.proxy.message;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.ToString;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Message sent by another server to notify that a player has quit.
 */
@AllArgsConstructor
@ToString
public class PlayerQuit extends ProxyMessage {

    @NotNull private final UUID playerId;

    /**
     * Creates new instance and reads data from byte input.
     *
     * @param   in
     *          Input stream to read from
     */
    public PlayerQuit(@NotNull ByteArrayDataInput in) {
        playerId = readUUID(in);
    }

    @Override
    public void write(@NotNull ByteArrayDataOutput out) {
        writeUUID(out, playerId);
    }

    @Override
    public void process(@NotNull ProxySupport proxySupport) {
        ProxyPlayer target = proxySupport.getProxyPlayers().get(playerId);
        if (target == null) {
            // This will not actually properly handle it, let's hope no one will ever run into this
            unknownPlayer(playerId.toString(), "disconnect");
            proxySupport.getQueuedData().remove(playerId);
            return;
        }
        TAB.getInstance().getFeatureManager().onQuit(target);
        proxySupport.getProxyPlayers().remove(target.getUniqueId());
    }
}
