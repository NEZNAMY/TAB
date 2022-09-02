package me.neznamy.tab.api;

/**
 * Interface allowing to send header/footer to players.
 * <p>
 * Instance can be obtained using {@link TabAPI#getHeaderFooterManager()}.
 * This requires the Header/footer feature to be enabled in config, otherwise
 * the method will return {@code null}.
 */
public interface HeaderFooterManager {

    /**
     * Sets raw value of header to specified value. This overrides plugin
     * configuration until reset is called. Has full placeholder support
     * and will send update packet whenever a placeholder used inside
     * changes its value.
     *
     * @param   player
     *          Player to set header for
     * @param   header
     *          Raw header value
     */
    void setHeader(TabPlayer player, String header);

    /**
     * Sets raw value of footer to specified value. This overrides plugin
     * configuration until reset is called. Has full placeholder support
     * and will send update packet whenever a placeholder used inside
     * changes its value.
     *
     * @param   player
     *          Player to set header for
     * @param   footer
     *          Raw footer value
     */
    void setFooter(TabPlayer player, String footer);

    /**
     * Sets raw value of header and footer to specified values. This overrides plugin
     * configuration until reset is called. Has full placeholder support
     * and will send update packet whenever a placeholder used inside
     * changes its value.
     *
     * @param   player
     *          Player to set header for
     * @param   header
     *          Raw header value
     * @param   footer
     *          Raw footer value
     */
    void setHeaderAndFooter(TabPlayer player, String header, String footer);

    /**
     * Resets header value previously set using {@link #setHeader(TabPlayer, String)}
     * or {@link #setHeaderAndFooter(TabPlayer, String, String)} and restores internal
     * logic using configuration files.
     *
     * @param   player
     *          Player to reset header for
     */
    void resetHeader(TabPlayer player);

    /**
     * Resets footer value previously set using {@link #setFooter(TabPlayer, String)}
     * or {@link #setHeaderAndFooter(TabPlayer, String, String)} and restores internal
     * logic using configuration files.
     *
     * @param   player
     *          Player to reset header for
     */
    void resetFooter(TabPlayer player);

    /**
     * Resets values previously set using {@link #setHeader(TabPlayer, String)},
     * {@link #setFooter(TabPlayer, String)} or {@link #setHeaderAndFooter(TabPlayer, String, String)}
     * and restores internal logic using configuration files.
     *
     * @param   player
     *          Player to reset header and footer for
     */
    void resetHeaderAndFooter(TabPlayer player);
}
