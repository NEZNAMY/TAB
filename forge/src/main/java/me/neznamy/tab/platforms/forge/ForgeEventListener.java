package me.neznamy.tab.platforms.forge;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.EventListener;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Event listener for Forge.
 */
public class ForgeEventListener implements EventListener<ServerPlayer> {

    /**
     * Registers all event listeners.
     */
    public void register() {
        PlayerEvent.PlayerLoggedInEvent.BUS.addListener(event -> join((ServerPlayer) event.getEntity()));
        PlayerEvent.PlayerLoggedOutEvent.BUS.addListener(event -> quit(event.getEntity().getUUID()));
        PlayerEvent.PlayerRespawnEvent.BUS.addListener(event -> {
            ServerPlayer player = (ServerPlayer) event.getEntity();
          replacePlayer(player.getUUID(), player);
          worldChange(player.getUUID(), ForgeTAB.getLevelName(player.level()));
        });
        PlayerEvent.PlayerChangedDimensionEvent.BUS.addListener(event -> worldChange(event.getEntity().getUUID(), ForgeTAB.getLevelName(event.getEntity().level())));
    }

    @Override
    @NotNull
    public TabPlayer createPlayer(@NotNull ServerPlayer player) {
        return new ForgeTabPlayer((ForgePlatform) TAB.getInstance().getPlatform(), player);
    }
}
