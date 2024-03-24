package me.neznamy.tab.shared.proxy.message.incoming;

import com.google.common.io.ByteArrayDataInput;
import me.neznamy.tab.api.placeholder.PlayerPlaceholder;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import org.jetbrains.annotations.NotNull;

public class SetGroup implements IncomingMessage {

    private String group;

    @Override
    public void read(@NotNull ByteArrayDataInput in) {
        group = in.readUTF();
    }

    @Override
    public void process(@NotNull ProxyTabPlayer player) {
        ((PlayerPlaceholder)TAB.getInstance().getPlaceholderManager().getPlaceholder(TabConstants.Placeholder.GROUP)).updateValue(player, group);
    }
}
