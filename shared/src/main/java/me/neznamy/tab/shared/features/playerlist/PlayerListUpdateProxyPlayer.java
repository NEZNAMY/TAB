package me.neznamy.tab.shared.features.playerlist;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.features.proxy.message.ProxyMessage;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Proxy message to update tablist format of a player.
 */
@RequiredArgsConstructor
@AllArgsConstructor
public class PlayerListUpdateProxyPlayer extends ProxyMessage {

    private final PlayerList feature;
    private UUID playerId;
    private String player;
    private String format;

    @Override
    public void write(@NotNull ByteArrayDataOutput out) {
        TAB.getInstance().debug("[Proxy Support] Sending proxy message to update tablist format of player " + player + " to " + format);
        writeUUID(out, playerId);
        out.writeUTF(player);
        out.writeUTF(format);
    }

    @Override
    public void read(@NotNull ByteArrayDataInput in) {
        playerId = readUUID(in);
        player = in.readUTF();
        format = in.readUTF();
    }

    @Override
    public void process(@NotNull ProxySupport proxySupport) {
        ProxyPlayer target = proxySupport.getProxyPlayers().get(playerId);
        if (target == null) {
            TAB.getInstance().getErrorManager().printError("[Proxy Support] Unable to process tablist format update of proxy player " + player + ", because no such player exists", null);
            return;
        }
        TAB.getInstance().debug("[Proxy Support] Processing tablist formatting update of proxy player " + player + " to " + format);
        target.setTabFormat(feature.getCache().get(format));
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            viewer.getTabList().updateDisplayName(target.getUniqueId(), target.getTabFormat());
        }
    }
}