package me.neznamy.tab.shared.features.playerlist;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.ToString;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.features.proxy.QueuedData;
import me.neznamy.tab.shared.features.proxy.message.ProxyMessage;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Proxy message to update tablist format of a player.
 */
@AllArgsConstructor
@ToString
public class PlayerListUpdateProxyPlayer extends ProxyMessage {

    @NotNull private final PlayerList feature;
    @NotNull private final UUID playerId;
    @NotNull private final String player;
    @NotNull private final String format;

    /**
     * Creates new instance and reads data from byte input.
     *
     * @param   in
     *          Input stream to read from
     * @param   feature
     *          Feature instance to use for processing
     */
    public PlayerListUpdateProxyPlayer(@NotNull ByteArrayDataInput in, @NotNull PlayerList feature) {
        this.feature = feature;
        playerId = readUUID(in);
        player = in.readUTF();
        format = in.readUTF();
    }

    @Override
    public void write(@NotNull ByteArrayDataOutput out) {
        writeUUID(out, playerId);
        out.writeUTF(player);
        out.writeUTF(format);
    }

    @Override
    public void process(@NotNull ProxySupport proxySupport) {
        ProxyPlayer target = proxySupport.getProxyPlayers().get(playerId);
        if (target == null) {
            unknownPlayer(playerId.toString(), "tablist format update");
            QueuedData data = proxySupport.getQueuedData().computeIfAbsent(playerId, k -> new QueuedData());
            data.setTabFormat(feature.getCache().get(format));
            return;
        }
        target.setTabFormat(feature.getCache().get(format));
        if (target.getConnectionState() == ProxyPlayer.ConnectionState.CONNECTED) {
            feature.formatPlayerForEveryone(target);
        }
    }
}