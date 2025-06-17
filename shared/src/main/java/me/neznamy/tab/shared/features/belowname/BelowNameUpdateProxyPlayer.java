package me.neznamy.tab.shared.features.belowname;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.ToString;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.features.proxy.QueuedData;
import me.neznamy.tab.shared.features.proxy.message.ProxyMessage;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Proxy message to update belowname data of a player.
 */
@AllArgsConstructor
@ToString
public class BelowNameUpdateProxyPlayer extends ProxyMessage {

    @NotNull private final BelowName feature;
    @NotNull private final UUID playerId;
    private final int value;
    @NotNull private final String fancyValue;

    /**
     * Creates new instance and reads data from byte input.
     *
     * @param   in
     *          Input stream to read from
     * @param   feature
     *          Feature instance to use for processing
     */
    public BelowNameUpdateProxyPlayer(@NotNull ByteArrayDataInput in, @NotNull BelowName feature) {
        this.feature = feature;
        playerId = readUUID(in);
        value = in.readInt();
        fancyValue = in.readUTF();
    }

    @NotNull
    public ThreadExecutor getCustomThread() {
        return feature.getCustomThread();
    }

    @Override
    public void write(@NotNull ByteArrayDataOutput out) {
        writeUUID(out, playerId);
        out.writeInt(value);
        out.writeUTF(fancyValue);
    }

    @Override
    public void process(@NotNull ProxySupport proxySupport) {
        ProxyPlayer target = proxySupport.getProxyPlayers().get(playerId);
        if (target == null) {
            unknownPlayer(playerId.toString(), "belowname objective update");
            QueuedData data = proxySupport.getQueuedData().computeIfAbsent(playerId, k -> new QueuedData());
            data.setBelowNameNumber(value);
            data.setBelowNameFancy(feature.getCache().get(fancyValue));
            return;
        }
        target.setBelowNameNumber(value);
        target.setBelowNameFancy(feature.getCache().get(fancyValue));
        if (target.getConnectionState() == ProxyPlayer.ConnectionState.CONNECTED) {
            feature.updatePlayer(target);
        }
    }
}
