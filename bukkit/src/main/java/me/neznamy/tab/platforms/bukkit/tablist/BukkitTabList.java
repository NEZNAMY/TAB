package me.neznamy.tab.platforms.bukkit.tablist;

import lombok.NonNull;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.chat.component.TabComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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
    public BukkitTabList(@NonNull BukkitTabPlayer player) {
        super(player);
    }

    @Override
    public void removeEntry(@NonNull UUID entry) {
        // Shrug
    }

    @Override
    @SuppressWarnings("deprecation")
    public void updateDisplayName0(@NonNull UUID entry, @Nullable TabComponent displayName) {
        Player p = Bukkit.getPlayer(entry);
        if (p == null) return;
        p.setPlayerListName(displayName == null ? null : displayName.toLegacyText());
    }

    @Override
    public void updateLatency(@NonNull UUID entry, int latency) {
        // Shrug
    }

    @Override
    public void updateGameMode(@NonNull UUID entry, int gameMode) {
        // Shrug
    }

    @Override
    public void updateListed(@NonNull UUID entry, boolean listed) {
        // Shrug
    }

    @Override
    public void updateListOrder(@NonNull UUID entry, int listOrder) {
        // Shrug
    }

    @Override
    public void updateHat(@NonNull UUID entry, boolean showHat) {
        // Shrug
    }

    @Override
    public void addEntry0(@NonNull Entry entry) {
        // Shrug
    }
}
