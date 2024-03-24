package me.neznamy.tab.shared.proxy.message.incoming;

import com.google.common.io.ByteArrayDataInput;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.api.placeholder.PlayerPlaceholder;
import me.neznamy.tab.api.placeholder.RelationalPlaceholder;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import org.jetbrains.annotations.NotNull;

public class UpdatePlaceholder implements IncomingMessage {

    private String identifier;
    private String target; // rel only
    private String value;

    @Override
    public void read(@NotNull ByteArrayDataInput in) {
        identifier = in.readUTF();
        if (identifier.startsWith("%rel_")) target = in.readUTF();
        value = in.readUTF();
    }

    @Override
    public void process(@NotNull ProxyTabPlayer player) {
        if (!TAB.getInstance().getPlaceholderManager().isPlaceholderRegistered(identifier)) return;
        Placeholder placeholder = TAB.getInstance().getPlaceholderManager().getPlaceholder(identifier);
        if (placeholder instanceof RelationalPlaceholder) {
            TabPlayer other = TAB.getInstance().getPlayer(target);
            if (other != null) { // Backend player did not connect via this proxy if null
                ((RelationalPlaceholder)placeholder).updateValue(player, other, value);
            }
        } else {
            ((PlayerPlaceholder)placeholder).updateValue(player, value);
        }
    }
}
