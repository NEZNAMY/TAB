package me.neznamy.tab.shared.features.playerlist;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.component.TabComponent;
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
@ToString(exclude = {"feature", "formatComponent"} )
@Getter
public class PlayerListProxyPlayerData extends ProxyMessage {

    /** Feature instance to use for processing */
    @NotNull private final PlayerList feature;

    /** Unique ID of this data, higher means newer, to avoid wrong packet order messing things up */
    private final long id;

    /** Player's unique ID */
    @NotNull private final UUID playerId;

    /** Player's name */
    @NotNull private final String player;

    /** Tablist format */
    @NotNull private final String format;

    /** TabComponent of the format (parsed version of {@link #format}) */
    @NotNull private final TabComponent formatComponent;

    /**
     * Creates new instance and reads data from byte input.
     *
     * @param   in
     *          Input stream to read from
     * @param   feature
     *          Feature instance to use for processing
     */
    public PlayerListProxyPlayerData(@NotNull ByteArrayDataInput in, @NotNull PlayerList feature) {
        this.feature = feature;
        id = in.readLong();
        playerId = readUUID(in);
        player = in.readUTF();
        format = in.readUTF();
        formatComponent = feature.getCache().get(format);
    }

    @Override
    public void write(@NotNull ByteArrayDataOutput out) {
        out.writeLong(id);
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
            if (data.getTabFormat() == null || data.getTabFormat().id < id)  {
                data.setTabFormat(this);
            }
            return;
        }
        if (target.getTabFormat() != null && target.getTabFormat().id > id) {
            TAB.getInstance().debug("Dropping tabformat update action for player " + target.getName() + " due to newer action already being present");
            return;
        }
        target.setTabFormat(this);
        if (target.getConnectionState() == ProxyPlayer.ConnectionState.CONNECTED) {
            feature.formatPlayerForEveryone(target);
        }
    }
}