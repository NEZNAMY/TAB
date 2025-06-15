package me.neznamy.tab.shared.features.proxy.message;

import com.google.common.io.ByteArrayDataOutput;
import lombok.ToString;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import org.jetbrains.annotations.NotNull;

/**
 * Message sent by another server to request loading of all players connected to this server.
 */
@ToString
public class LoadRequest extends ProxyMessage {

    @Override
    public void write(@NotNull ByteArrayDataOutput out) {
        // Nothing to write anymore
    }

    @Override
    public void process(@NotNull ProxySupport proxySupport) {
        proxySupport.sendMessage(new Load(TAB.getInstance().getOnlinePlayers()));
        TAB.getInstance().getFeatureManager().onProxyLoadRequest();
    }
}
