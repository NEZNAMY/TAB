package me.neznamy.tab.api.tablist.layout;

import lombok.NonNull;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
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
    Layout createNewLayout(String name);

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
