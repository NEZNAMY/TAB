package me.neznamy.tab.shared.proxy.message.incoming;

import com.google.common.io.ByteArrayDataInput;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import org.jetbrains.annotations.NotNull;

public class OnBoat implements IncomingMessage {

    private boolean onBoat;

    @Override
    public void read(@NotNull ByteArrayDataInput in) {
        onBoat = in.readBoolean();
    }

    @Override
    public void process(@NotNull ProxyTabPlayer player) {
        player.setOnBoat(onBoat);
    }
}
