package me.neznamy.tab.shared.features.proxy.message;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@NoArgsConstructor
public class PlayerJoin extends ProxyMessage {

    @Getter private ProxyPlayer decodedPlayer;
    private TabPlayer encodedPlayer;

    public PlayerJoin(@NotNull TabPlayer encodedPlayer) {
        this.encodedPlayer = encodedPlayer;
    }

    @Override
    public void write(@NotNull ByteArrayDataOutput out) {
        writeUUID(out, encodedPlayer.getTablistId());
        out.writeUTF(encodedPlayer.getName());
        out.writeUTF(encodedPlayer.server);
        out.writeBoolean(encodedPlayer.isVanished());
        out.writeBoolean(encodedPlayer.hasPermission(TabConstants.Permission.STAFF));
        out.writeBoolean(encodedPlayer.getSkin() != null);

        // Load skin immediately to make global playerlist stuff not too complicated
        if (encodedPlayer.getSkin() != null) {
            out.writeUTF(encodedPlayer.getSkin().getValue());
            out.writeBoolean(encodedPlayer.getSkin().getSignature() != null);
            if (encodedPlayer.getSkin().getSignature() != null) {
                out.writeUTF(encodedPlayer.getSkin().getSignature());
            }
        }
    }

    @Override
    public void read(@NotNull ByteArrayDataInput in) {
        UUID uniqueId = readUUID(in);
        String name = in.readUTF();
        String server = in.readUTF();
        boolean vanished = in.readBoolean();
        boolean staff = in.readBoolean();
        decodedPlayer = new ProxyPlayer(uniqueId, name, name, server, vanished, staff);

        // Load skin immediately to make global playerlist stuff not too complicated
        if (in.readBoolean()) {
            String value = in.readUTF();
            String signature = null;
            if (in.readBoolean()) {
                signature = in.readUTF();
            }
            decodedPlayer.setSkin(new TabList.Skin(value, signature));
        }
    }

    @Override
    public void process(@NotNull ProxySupport proxySupport) {
        TAB.getInstance().debug("Processing join of proxy player " + decodedPlayer.getName() + " (" + decodedPlayer.getUniqueId() + ")");
        proxySupport.getProxyPlayers().put(decodedPlayer.getUniqueId(), decodedPlayer);
        TAB.getInstance().getFeatureManager().onJoin(decodedPlayer);
    }
}
