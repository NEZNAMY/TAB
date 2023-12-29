package me.neznamy.tab.shared.proxy.message.incoming;

import com.google.common.io.ByteArrayDataInput;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class UpdateGameMode implements IncomingMessage {

    private int gameMode;

    @Override
    public void read(@NotNull ByteArrayDataInput in) {
       gameMode = in.readInt();
    }

    @Override
    public void process(@NotNull ProxyTabPlayer player) {
        player.setGamemode(gameMode);
    }
}
