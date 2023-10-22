package me.neznamy.tab.platforms.fabric;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.EventListener;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class FabricEventListener extends EventListener<ServerPlayer> {

    public void register() {
        ServerPlayConnectionEvents.DISCONNECT.register((connection, $) -> quit(connection.player.getUUID()));
        ServerPlayConnectionEvents.JOIN.register((connection, $, $$) -> join(connection.player));
        FabricMultiVersion.registerEntityEvents(this::replacePlayer, this::worldChange);
        //TODO command preprocess
    }

    @Override
    @NotNull
    public TabPlayer createPlayer(@NotNull ServerPlayer player) {
        return new FabricTabPlayer((FabricPlatform) TAB.getInstance().getPlatform(), player);
    }
}
