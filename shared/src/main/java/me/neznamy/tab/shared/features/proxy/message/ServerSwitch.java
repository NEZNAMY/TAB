package me.neznamy.tab.shared.features.proxy.message;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
public class ServerSwitch extends ProxyMessage {

    private UUID playerId;
    private String newServer;

    @Override
    public void write(@NotNull ByteArrayDataOutput out) {
        writeUUID(out, playerId);
        out.writeUTF(newServer);
    }

    @Override
    public void read(@NotNull ByteArrayDataInput in) {
        playerId = readUUID(in);
        newServer = in.readUTF();
    }

    @Override
    public void process(@NotNull ProxySupport proxySupport) {
        ProxyPlayer target = proxySupport.getProxyPlayers().get(playerId);
        if (target == null) {
            TAB.getInstance().getErrorManager().printError("Unable to process server switch of proxy player " + playerId + ", because no such player exists", null);
            return;
        }
        TAB.getInstance().debug("Processing server switch of proxy player " + target.getName());
        if (TAB.getInstance().isPlayerConnected(target.getUniqueId())) {
            TAB.getInstance().debug("The player " + target.getName() + " is already connected");
            return;
        }
        target.setServer(newServer);
        TAB.getInstance().getFeatureManager().onServerSwitch(target);
    }
}
