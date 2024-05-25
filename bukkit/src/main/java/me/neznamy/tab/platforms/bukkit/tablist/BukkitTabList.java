package me.neznamy.tab.platforms.bukkit.tablist;

import lombok.NonNull;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.shared.chat.TabComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * TabList handler using the almighty Bukkit API.
 */
public class BukkitTabList extends TabListBase<String> {

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
    public void removeEntry0(@NonNull UUID entry) {
        // Shrug
    }

    @Override
    @SuppressWarnings("deprecation")
    public void updateDisplayName(@NonNull UUID entry, @Nullable String displayName) {
        Player p = Bukkit.getPlayer(entry);
        if (p == null) return;
        p.setPlayerListName(displayName);
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
    public void addEntry(@NonNull UUID id, @NonNull String name, @Nullable Skin skin, boolean listed, int latency, int gameMode, @Nullable String displayName) {
        // Shrug
    }

    @Override
    public String toComponent(@NonNull TabComponent component) {
        return player.getPlatform().toBukkitFormat(component, true);
    }
}
