package me.neznamy.tab.platforms.bukkit.tablist;

import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.BukkitUtils;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * TabList handler using the almighty Bukkit API.
 */
public class BukkitTabList extends TabListBase {

    /**
     * Constructs new instance with given player.
     *
     * @param   player
     *          Player this tablist will belong to.
     */
    public BukkitTabList(@NotNull BukkitTabPlayer player) {
        super(player);
    }

    @Override
    public void removeEntry(@NotNull UUID entry) {
        // Shrug
    }

    @Override
    @SuppressWarnings("deprecation")
    public void updateDisplayName(@NotNull UUID entry, @Nullable IChatBaseComponent displayName) {
        Player p = Bukkit.getPlayer(entry);
        if (p == null) return;
        p.setPlayerListName(displayName == null ? null : BukkitUtils.toBukkitFormat(displayName, true));
    }

    @Override
    public void updateLatency(@NotNull UUID entry, int latency) {
        // Shrug
    }

    @Override
    public void updateGameMode(@NotNull UUID entry, int gameMode) {
        // Shrug
    }

    @Override
    public void addEntry(@NotNull Entry entry) {
        // Shrug
    }
}
