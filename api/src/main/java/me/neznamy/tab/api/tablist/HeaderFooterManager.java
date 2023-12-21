package me.neznamy.tab.api.tablist;

import lombok.NonNull;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Interface allowing to send header/footer to players.
 * <p>
 * Instance can be obtained using {@link TabAPI#getHeaderFooterManager()}.
 * This requires the Header/footer feature to be enabled in config, otherwise
 * the method will return {@code null}.
 */
@SuppressWarnings("unused") // API class
public interface HeaderFooterManager {

    /**
     * Sets raw value of header to specified value. This overrides plugin
     * configuration until reset is called. Has full placeholder support
     * and will send update packet whenever a placeholder used inside
     * changes its value. Use {@code null} to reset value back to original.
     *
     * @param   player
     *          Player to set header for
     * @param   header
     *          Raw header value
     */
    void setHeader(@NonNull TabPlayer player, @Nullable String header);

    /**
     * Sets raw value of footer to specified value. This overrides plugin
     * configuration until reset is called. Has full placeholder support
     * and will send update packet whenever a placeholder used inside
     * changes its value. Use {@code null} to reset value back to original.
     *
     * @param   player
     *          Player to set header for
     * @param   footer
     *          Raw footer value
     */
    void setFooter(@NonNull TabPlayer player, @Nullable String footer);

    /**
     * Sets raw value of header and footer to specified values. This overrides plugin
     * configuration until reset is called. Has full placeholder support
     * and will send update packet whenever a placeholder used inside
     * changes its value. Use {@code null} to reset value back to original.
     *
     * @param   player
     *          Player to set header for
     * @param   header
     *          Raw header value
     * @param   footer
     *          Raw footer value
     */
    void setHeaderAndFooter(@NonNull TabPlayer player, @Nullable String header, @Nullable String footer);
}
