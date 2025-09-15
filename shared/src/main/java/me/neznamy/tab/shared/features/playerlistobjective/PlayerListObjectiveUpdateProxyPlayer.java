package me.neznamy.tab.shared.features.playerlistobjective;

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
 * Proxy message to update playerlist objective data of a player.
 */
@AllArgsConstructor
@ToString
public class PlayerListObjectiveUpdateProxyPlayer extends ProxyMessage {

    @NotNull private final YellowNumber feature;
    @NotNull private final UUID playerId;
    private final int value;
    @NotNull private final String fancyValue;

    /**
     * Creates new instance and reads data from byte input.
     *
     * @param   feature
     *          Feature instance
     * @param   in
     *          Input stream to read from
     */
    public PlayerListObjectiveUpdateProxyPlayer(@NotNull YellowNumber feature, @NotNull ByteArrayDataInput in) {
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
            unknownPlayer(playerId.toString(), "playerlist objective update");
            QueuedData data = proxySupport.getQueuedData().computeIfAbsent(playerId, k -> new QueuedData());
            data.setPlayerlistNumber(value);
            data.setPlayerlistFancy(feature.getCache().get(fancyValue));
            return;
        }
        target.setPlayerlistNumber(value);
        target.setPlayerlistFancy(feature.getCache().get(fancyValue));
        if (target.getConnectionState() == ProxyPlayer.ConnectionState.CONNECTED) {
            feature.updatePlayer(target);
        }
    }
}