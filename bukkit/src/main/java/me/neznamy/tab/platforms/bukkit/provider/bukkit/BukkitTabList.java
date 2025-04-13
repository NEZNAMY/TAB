package me.neznamy.tab.platforms.bukkit.provider.bukkit;

import lombok.NonNull;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.shared.platform.decorators.TrackedTabList;
import me.neznamy.tab.shared.util.ReflectionUtils;
import me.neznamy.tab.shared.util.function.TriConsumer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * TabList handler using the almighty Bukkit API.
 */
@SuppressWarnings("deprecation") // Marked as deprecated by Paper to make us use their method instead
public class BukkitTabList extends TrackedTabList<BukkitTabPlayer> {

    private static final TriConsumer<BukkitTabPlayer, TabComponent, TabComponent> sendHeaderFooter;

    static {
        if (ReflectionUtils.classExists("net.kyori.adventure.text.Component") &&
                ReflectionUtils.methodExists(Player.class, "sendPlayerListHeaderAndFooter", Component.class, Component.class)) {
            sendHeaderFooter = (player, header, footer) -> player.getPlayer().sendPlayerListHeaderAndFooter(header.toAdventure(), footer.toAdventure());
        } else if (ReflectionUtils.methodExists(Player.class, "setPlayerListHeaderFooter", String.class, String.class)) {
            sendHeaderFooter = (player, header, footer) -> player.getPlayer().setPlayerListHeaderFooter(
                    player.getPlatform().toBukkitFormat(header),
                    player.getPlatform().toBukkitFormat(footer)
            );
        } else {
            sendHeaderFooter = (player, header, footer) -> {
                // Do nothing
            };
        }
    }

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
        p.setPlayerListName(displayName == null ? null : player.getPlatform().toBukkitFormat(displayName));
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

    @Override
    public boolean containsEntry(@NonNull UUID entry) {
        return true;
    }

    @Override
    public void setPlayerListHeaderFooter(@NonNull TabComponent header, @NonNull TabComponent footer) {
        sendHeaderFooter.accept(player, header, footer);
    }

    @Override
    @Nullable
    public Skin getSkin() {
        return null; // Shrug
    }
}
