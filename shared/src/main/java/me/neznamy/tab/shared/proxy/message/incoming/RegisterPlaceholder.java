package me.neznamy.tab.shared.proxy.message.incoming;

import com.google.common.io.ByteArrayDataInput;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import org.jetbrains.annotations.NotNull;

public class RegisterPlaceholder implements IncomingMessage {

    private String identifier;

    @Override
    public void read(@NotNull ByteArrayDataInput in) {
        identifier = in.readUTF();
    }

    @Override
    public void process(@NotNull ProxyTabPlayer player) {
        TAB.getInstance().getPlaceholderManager().addUsedPlaceholder(identifier);
    }
}
