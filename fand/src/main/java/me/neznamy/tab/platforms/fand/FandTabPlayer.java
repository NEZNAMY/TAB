package me.neznamy.tab.platforms.fand;

import io.fand.api.entity.GameMode;
import io.fand.api.entity.Player;
import io.fand.api.visibility.DisguiseService;
import io.fand.api.visibility.VanishService;
import me.neznamy.tab.shared.backend.BackendTabPlayer;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.TabPlayer;
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
        DisguiseService service = getPlatform().disguiseService();
        return service != null && service.disguised(getPlayer());
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
        VanishService service = getPlatform().vanishService();
        return service != null && service.vanished(getPlayer());
    }

    @Override
    public boolean canSee(@NotNull TabPlayer target) {
        if (target == this) {
            return true;
        }
        VanishService service = getPlatform().vanishService();
        if (service != null && target instanceof FandTabPlayer fandTarget) {
            return service.canSee(getPlayer(), fandTarget.getPlayer());
        }
        return super.canSee(target);
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
