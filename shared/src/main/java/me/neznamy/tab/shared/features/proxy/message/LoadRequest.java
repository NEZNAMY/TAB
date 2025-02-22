package me.neznamy.tab.shared.features.proxy.message;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import org.jetbrains.annotations.NotNull;

public class LoadRequest extends ProxyMessage {

    @Override
    public void write(@NotNull ByteArrayDataOutput out) {
        // Nothing to write anymore
    }

    @Override
    public void read(@NotNull ByteArrayDataInput in) {
        // Nothing to read anymore
    }

    @Override
    public void process(@NotNull ProxySupport proxySupport) {
        proxySupport.sendMessage(new Load(TAB.getInstance().getOnlinePlayers()));
        TAB.getInstance().getFeatureManager().onProxyLoadRequest();
    }
}
