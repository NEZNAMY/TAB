package me.neznamy.tab.platforms.forge;

import me.neznamy.tab.platforms.forge.hook.LuckPermsAPIHook;
import me.neznamy.tab.shared.backend.BackendTabPlayer;
import me.neznamy.tab.shared.chat.component.TabComponent;
import net.minecraft.SharedConstants;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import org.jetbrains.annotations.NotNull;

/**
 * TabPlayer implementation for Forge.
 */
public class ForgeTabPlayer extends BackendTabPlayer {

    /**
     * Constructs new instance with given parameters.
     *
     * @param   platform
     *          Server platform
     * @param   player
     *          Platform's player object
     */
    public ForgeTabPlayer(@NotNull ForgePlatform platform, @NotNull ServerPlayer player) {
        super(platform, player, player.getUUID(), player.getGameProfile().name(),
                ForgeTAB.getLevelName(player.level()), SharedConstants.getProtocolVersion());
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return LuckPermsAPIHook.hasPermission(getPlayer().createCommandSourceStack(), permission);
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
    public ForgePlatform getPlatform() {
        return (ForgePlatform) platform;
    }

    @Override
    public boolean isVanished0() {
        return false;
    }

    @Override
    public int getDeaths() {
        return getPlayer().getStats().getValue(Stats.CUSTOM.get(Stats.DEATHS));
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
        return getPlayer().getGameProfile().name(); // Will make it work properly if someone asks
    }
}
