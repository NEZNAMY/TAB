package me.neznamy.tab.shared.proxy.message.incoming;

import com.google.common.io.ByteArrayDataInput;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import org.jetbrains.annotations.NotNull;

public class SetWorld implements IncomingMessage {

    private String world;

    @Override
    public void read(@NotNull ByteArrayDataInput in) {
        world = in.readUTF();
    }

    @Override
    public void process(@NotNull ProxyTabPlayer player) {
        TAB.getInstance().getFeatureManager().onWorldChange(player.getUniqueId(), world);
    }
}
