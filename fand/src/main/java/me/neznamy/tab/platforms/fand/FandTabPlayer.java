package me.neznamy.tab.platforms.fand;

import io.fand.api.entity.GameMode;
import io.fand.api.entity.Player;
import me.neznamy.tab.shared.backend.BackendTabPlayer;
import me.neznamy.tab.shared.chat.component.TabComponent;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

/** TAB player backed by Fand's public player API. */
public final class FandTabPlayer extends BackendTabPlayer {

    private static final Key INVISIBILITY = Key.key("minecraft", "invisibility");
    private static final Key DEATHS = Key.key("minecraft", "deaths");

    public FandTabPlayer(@NotNull FandPlatform platform, @NotNull Player player) {
        super(platform, player, player.uniqueId(), player.name(), player.world().name(), platform.protocolVersion());
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return getPlayer().can(permission);
    }

    @Override
    public int getPing() {
        return getPlayer().ping();
    }

    @Override
    public void sendMessage(@NotNull TabComponent message) {
        getPlayer().sendMessage(message.toAdventure());
    }

    @Override
    public boolean hasInvisibilityPotion() {
        return getPlayer().effect(INVISIBILITY).isPresent();
    }

    @Override
    public boolean isDisguised() {
        return false;
    }

    @Override
    @NotNull
    public Player getPlayer() {
        return (Player) player;
    }

    @Override
    public FandPlatform getPlatform() {
        return (FandPlatform) platform;
    }

    @Override
    public boolean isVanished0() {
        return false;
    }

    @Override
    public int getDeaths() {
        return getPlayer().statistic(DEATHS);
    }

    @Override
    public int getGamemode() {
        GameMode gameMode = getPlayer().gameMode();
        return gameMode.ordinal();
    }

    @Override
    public double getHealth() {
        return getPlayer().health();
    }

    @Override
    @NotNull
    public String getDisplayName() {
        return getPlayer().name();
    }
}
