package me.neznamy.tab.platforms.neoforge;

import me.neznamy.chat.component.TabComponent;
import me.neznamy.tab.shared.backend.BackendTabPlayer;
import net.minecraft.SharedConstants;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * TabPlayer implementation for NeoForge.
 */
public class NeoForgeTabPlayer extends BackendTabPlayer {

    /**
     * Constructs new instance with given parameters.
     *
     * @param   platform
     *          Server platform
     * @param   player
     *          Platform's player object
     */
    public NeoForgeTabPlayer(@NotNull NeoForgePlatform platform, @NotNull ServerPlayer player) {
        super(platform, player, player.getUUID(), player.getGameProfile().getName(),
                NeoForgeTAB.getLevelName(player.level()), SharedConstants.getProtocolVersion());
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return true; // PermissionsAPIHook.hasPermission(getPlayer(), permission);
    }

    @Override
    public int getPing() {
        return getPlayer().connection.latency();
    }

    @Override
    public void sendMessage(@NotNull TabComponent message) {
        getPlayer().sendSystemMessage(message.convert());
    }

    @Override
    public boolean hasInvisibilityPotion() {
        return false;
    }

    @Override
    public boolean isDisguised() {
        return false;
    }

    @Override
    @NotNull
    public ServerPlayer getPlayer() {
        return (ServerPlayer) player;
    }

    @Override
    public NeoForgePlatform getPlatform() {
        return (NeoForgePlatform) platform;
    }

    @Override
    public boolean isVanished0() {
        return false;
    }

    @Override
    public int getGamemode() {
        return getPlayer().gameMode.getGameModeForPlayer().getId();
    }

    @Override
    public double getHealth() {
        return getPlayer().getHealth();
    }

    @Override
    @NotNull
    public String getDisplayName() {
        return getPlayer().getGameProfile().getName(); // Will make it work properly if someone asks
    }
}
