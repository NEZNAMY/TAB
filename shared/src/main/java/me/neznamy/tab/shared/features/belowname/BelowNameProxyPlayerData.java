package me.neznamy.tab.shared.features.belowname;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import me.neznamy.tab.shared.TAB;
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
@ToString(exclude = "feature")
@Getter
public class BelowNameProxyPlayerData extends ProxyMessage {

    /** Feature instance */
    @NotNull private final BelowName feature;

    /** Unique ID of this data, higher means newer, to avoid wrong packet order messing things up */
    private final long id;

    /** Player's UUID */
    @NotNull private final UUID playerId;

    /** Belowname value (1.20.2-) */
    private final int value;

    /** Belowname fancy value (1.20.3+) */
    @NotNull private final String fancyValue;

    /**
     * Creates new instance and reads data from byte input.
     *
     * @param   in
     *          Input stream to read from
     * @param   feature
     *          Feature instance to use for processing
     */
    public BelowNameProxyPlayerData(@NotNull ByteArrayDataInput in, @NotNull BelowName feature) {
        this.feature = feature;
        id = in.readLong();
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
        out.writeLong(id);
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
            if (data.getBelowname() == null || data.getBelowname().id < id)  {
                data.setBelowname(this);
            }
            return;
        }
        if (target.getBelowname() != null && target.getBelowname().id > id) {
            TAB.getInstance().debug("Dropping belowname update action for player " + target.getName() + " due to newer action already being present");
            return;
        }
        target.setBelowname(this);
        if (target.getConnectionState() == ProxyPlayer.ConnectionState.CONNECTED) {
            feature.updatePlayer(target);
        }
    }
}
