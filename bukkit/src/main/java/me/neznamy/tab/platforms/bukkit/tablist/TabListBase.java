package me.neznamy.tab.platforms.bukkit.tablist;

import lombok.NonNull;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.shared.platform.decorators.TrackedTabList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Base TabList class for all implementations.
 */
public abstract class TabListBase extends TrackedTabList<BukkitTabPlayer> {

    @Nullable
    protected static SkinData skinData;

    /**
     * Constructs new instance.
     *
     * @param   player
     *          Player this tablist will belong to
     */
    protected TabListBase(@NotNull BukkitTabPlayer player) {
        super(player);
    }

    @Override
    public void setPlayerListHeaderFooter(@NonNull TabComponent header, @NonNull TabComponent footer) {
        player.getPlatform().getHeaderFooter().set(player, header, footer);
    }

    @Override
    public boolean containsEntry(@NonNull UUID entry) {
        return true; // TODO?
    }

    /**
     * Returns player's skin. If NMS fields did not load or server is in
     * offline mode, returns {@code null}.
     *
     * @return  Player's skin or {@code null}.
     */
    @Nullable
    public Skin getSkin() {
        if (skinData == null) return null;
        return skinData.getSkin(player);
    }
}
