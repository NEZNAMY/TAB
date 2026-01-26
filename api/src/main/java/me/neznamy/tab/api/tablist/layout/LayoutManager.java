package me.neznamy.tab.api.tablist.layout;

import lombok.NonNull;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for working with layouts. Instance can be obtained using
 * {@link TabAPI#getLayoutManager()}. If the feature is disabled in
 * config, it will return {@code null}.
 */
@SuppressWarnings("unused") // API class
public interface LayoutManager {

    /**
     * Creates new layout
     *
     * @param   name
     *          Unique layout name
     * @return  Created layout
     */
    @NotNull
    Layout createNewLayout(@NonNull String name);

    /**
     * Creates new layout with given name and slot count (1.19.3+ only).
     *
     * @param   name
     *          Unique layout name
     * @param   slotCount
     *          Amount of slots to use for 1.19.3+ players
     * @return  Created layout
     */
    @NotNull
    Layout createNewLayout(@NonNull String name, int slotCount);

    /**
     * Returns defined layout from config by name. If no such layout is defined in config,
     * {@code null} is returned.
     *
     * @param   name
     *          Name of layout defined in config
     * @return  Layout defined in config by name or {@code null} if no such layout is defined
     */
    @Nullable
    Layout getLayout(@NonNull String name);

    /**
     * Sends layout to player. Set to {@code null} to make player not see
     * any layout, but default TabList instead.
     *
     * @param   player
     *          Player to send layout to
     * @param   layout
     *          Layout to send
     * @see     #resetLayout(TabPlayer)
     */
    void sendLayout(@NonNull TabPlayer player, @Nullable Layout layout);

    /**
     * Reset layout back to original one based on configuration.
     *
     * @param   player
     *          Player to reset layout for
     * @see     #sendLayout(TabPlayer, Layout)
     */
    void resetLayout(@NonNull TabPlayer player);
}
