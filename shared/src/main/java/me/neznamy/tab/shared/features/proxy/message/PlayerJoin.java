package me.neznamy.tab.shared.features.proxy.message;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.ToString;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.data.Server;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.features.proxy.QueuedData;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Message sent by another proxy when a player joins.
 */
@ToString
public class PlayerJoin extends ProxyMessage {

    @NotNull private final UUID uniqueId;
    @NotNull private final UUID tablistId;
    @NotNull private final String name;
    @NotNull private final Server server;
    private final boolean vanished;
    private final boolean staff;
    @Nullable private final TabList.Skin skin;

    /**
     * Creates new instance from given player data.
     *
     * @param   encodedPlayer
     *          Player data to encode
     */
    public PlayerJoin(@NotNull TabPlayer encodedPlayer) {
        uniqueId = encodedPlayer.getUniqueId();
        tablistId = encodedPlayer.getTablistId();
        name = encodedPlayer.getName();
        server = encodedPlayer.server;
        vanished = encodedPlayer.isVanished();
        staff = encodedPlayer.hasPermission(TabConstants.Permission.STAFF);
        skin = encodedPlayer.getTabList().getSkin();
    }

    /**
     * Creates new instance and reads data from byte input.
     *
     * @param   in
     *          Input stream to read from
     */
    public PlayerJoin(@NotNull ByteArrayDataInput in) {
        uniqueId = readUUID(in);
        tablistId = readUUID(in);
        name = in.readUTF();
        server = Server.byName(in.readUTF());
        vanished = in.readBoolean();
        staff = in.readBoolean();
        skin = readSkin(in);
    }

    @Override
    public void write(@NotNull ByteArrayDataOutput out) {
        writeUUID(out, uniqueId);
        writeUUID(out, tablistId);
        out.writeUTF(name);
        out.writeUTF(server.getName());
        out.writeBoolean(vanished);
        out.writeBoolean(staff);
        writeSkin(out, skin);
    }

    @Override
    public void process(@NotNull ProxySupport proxySupport) {
        ProxyPlayer decodedPlayer = new ProxyPlayer(uniqueId, tablistId, name, server, vanished, staff, skin);
        if (proxySupport.getProxyPlayers().containsKey(decodedPlayer.getUniqueId())) {
            TAB.getInstance().debug("[Proxy Support] The proxy player " + decodedPlayer.getName() + " is already connected, cannot process join.");
            return;
        }
        proxySupport.getProxyPlayers().put(decodedPlayer.getUniqueId(), decodedPlayer);
        QueuedData data = proxySupport.getQueuedData().remove(decodedPlayer.getUniqueId());
        if (data != null) {
            decodedPlayer.setBelowname(data.getBelowname());
            decodedPlayer.setTabFormat(data.getTabFormat());
            decodedPlayer.setNametag(data.getNametag());
            decodedPlayer.setPlayerlist(data.getPlayerlist());
            decodedPlayer.setVanished(data.isVanished());
        }
        if (TAB.getInstance().getPlayer(decodedPlayer.getUniqueId()) == null) {
            TAB.getInstance().getFeatureManager().onJoin(decodedPlayer);
        }
    }
}
