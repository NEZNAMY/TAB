package me.neznamy.tab.platforms.forge;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.EventListener;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import org.jetbrains.annotations.NotNull;

/**
 * Event listener for Forge.
 */
public class ForgeEventListener implements EventListener<ServerPlayer> {

    /**
     * Registers all event listeners.
     */
    public void register() {
        IEventBus eventBus = MinecraftForge.EVENT_BUS;
        eventBus.addListener((PlayerEvent.PlayerLoggedInEvent event) -> join((ServerPlayer) event.getEntity()));
        eventBus.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> quit(event.getEntity().getUUID()));
        eventBus.addListener((PlayerEvent.PlayerRespawnEvent event) -> {
            ServerPlayer player = (ServerPlayer) event.getEntity();
          replacePlayer(player.getUUID(), player);
          worldChange(player.getUUID(), ForgeTAB.getLevelName(player.level()));
        });
        eventBus.addListener((PlayerEvent.PlayerChangedDimensionEvent event) -> worldChange(event.getEntity().getUUID(), ForgeTAB.getLevelName(event.getEntity().level())));
    }

    @Override
    @NotNull
    public TabPlayer createPlayer(@NotNull ServerPlayer player) {
        return new ForgeTabPlayer((ForgePlatform) TAB.getInstance().getPlatform(), player);
    }
}
