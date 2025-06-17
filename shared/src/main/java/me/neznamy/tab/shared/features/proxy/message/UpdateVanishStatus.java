package me.neznamy.tab.shared.features.proxy.message;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.ToString;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.features.proxy.QueuedData;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Message sent from proxy to server to update vanish status of a player.
 */
@AllArgsConstructor
@ToString
public class UpdateVanishStatus extends ProxyMessage {

    @NotNull private final UUID playerId;
    private final boolean vanished;

    /**
     * Creates new instance and reads data from byte input.
     *
     * @param   in
     *          Input stream to read data from
     */
    public UpdateVanishStatus(@NotNull ByteArrayDataInput in) {
        playerId = readUUID(in);
        vanished = in.readBoolean();
    }

    @Override
    public void write(@NotNull ByteArrayDataOutput out) {
        writeUUID(out, playerId);
        out.writeBoolean(vanished);
    }

    @Override
    public void process(@NotNull ProxySupport proxySupport) {
        ProxyPlayer target = proxySupport.getProxyPlayers().get(playerId);
        if (target == null) {
            unknownPlayer(playerId.toString(), "vanish status update");
            QueuedData data = proxySupport.getQueuedData().computeIfAbsent(playerId, k -> new QueuedData());
            data.setVanished(vanished);
            return;
        }
        target.setVanished(vanished);
        TAB.getInstance().getFeatureManager().onVanishStatusChange(target);
    }
}
