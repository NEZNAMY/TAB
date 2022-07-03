package me.neznamy.tab.api;

/**
 * An interface allowing to modify tablist display names
 * of players.
 * <p>
 * Instance can be obtained using {@link TabAPI#getTablistFormatManager()}.
 * This requires the feature to be enabled in config, otherwise the method will
 * return {@code null}.
 */
public interface TablistFormatManager {

    /**
     * Changes player's prefix to provided value. Supports placeholders,
     * as well as any supported RGB formats.
     *
     * @param   player
     *          player to change prefix of
     * @param   prefix
     *          new prefix value
     * @see     #resetPrefix(TabPlayer)
     * @see     #getCustomPrefix(TabPlayer)
     * @see     #getOriginalPrefix(TabPlayer)
     */
    void setPrefix(TabPlayer player, String prefix);

    /**
     * Changes player's name to provided value. Supports placeholders,
     * as well as any supported RGB formats.
     *
     * @param   player
     *          player to change prefix of
     * @param   customName
     *          new customName value
     * @see     #resetName(TabPlayer)
     * @see     #getCustomName(TabPlayer)
     * @see     #getOriginalName(TabPlayer)
     */
    void setName(TabPlayer player, String customName);

    /**
     * Changes player's suffix to provided value. Supports placeholders,
     * as well as any supported RGB formats.
     *
     * @param   player
     *          player to change prefix of
     * @param   suffix
     *          new suffix value
     * @see     #resetSuffix(TabPlayer)
     * @see     #getCustomSuffix(TabPlayer)
     * @see     #getOriginalSuffix(TabPlayer)
     */
    void setSuffix(TabPlayer player, String suffix);

    /**
     * Resets player's prefix previously assigned using {@link #setPrefix(TabPlayer, String)}
     * method. If nothing was set, this method will not do anything.
     *
     * @param   player
     *          player to reset prefix of
     * @see     #setPrefix(TabPlayer, String)
     * @see     #getCustomPrefix(TabPlayer)
     * @see     #getOriginalPrefix(TabPlayer)
     */
    void resetPrefix(TabPlayer player);

    /**
     * Resets player's custom name previously assigned using {@link #setName(TabPlayer, String)}
     * method. If nothing was set, this method will not do anything.
     *
     * @param   player
     *          player to reset name of
     * @see     #setName(TabPlayer, String)
     * @see     #getCustomName(TabPlayer)
     * @see     #getOriginalName(TabPlayer)
     */
    void resetName(TabPlayer player);

    /**
     * Resets player's suffix previously assigned using {@link #setSuffix(TabPlayer, String)}
     * method. If nothing was set, this method will not do anything.
     *
     * @param   player
     *          player to reset suffix of
     * @see     #setSuffix(TabPlayer, String)
     * @see     #getCustomSuffix(TabPlayer)
     * @see     #getOriginalSuffix(TabPlayer)
     */
    void resetSuffix(TabPlayer player);

    /**
     * Returns prefix assigned using {@link #setPrefix(TabPlayer, String)} method.
     * If the method was not used, {@code null} is returned.
     *
     * @param   player
     *          player to get custom prefix of
     * @return  player's custom prefix or {@code null} if not defined
     * @see     #setPrefix(TabPlayer, String)
     * @see     #resetPrefix(TabPlayer)
     * @see     #getOriginalPrefix(TabPlayer)
     */
    String getCustomPrefix(TabPlayer player);

    /**
     * Returns custom name assigned using {@link #setName(TabPlayer, String)} method.
     * If the method was not used, {@code null} is returned.
     *
     * @param   player
     *          player to get custom name of
     * @return  player's custom name or {@code null} if not defined
     * @see     #setName(TabPlayer, String)
     * @see     #resetName(TabPlayer)
     * @see     #getOriginalName(TabPlayer)
     */
    String getCustomName(TabPlayer player);

    /**
     * Returns suffix assigned using {@link #setSuffix(TabPlayer, String)} method.
     * If the method was not used, {@code null} is returned.
     *
     * @param   player
     *          player to get custom suffix of
     * @return  player's custom suffix or {@code null} if not defined
     * @see     #setSuffix(TabPlayer, String)
     * @see     #resetSuffix(TabPlayer)
     * @see     #getOriginalSuffix(TabPlayer)
     */
    String getCustomSuffix(TabPlayer player);

    /**
     * Returns player's original prefix applied using plugin's internal logic.
     * This value is not affected by API calls in any way.
     *
     * @param   player
     *          player to get original prefix of
     * @return  player's original prefix
     * @see     #setPrefix(TabPlayer, String)
     * @see     #resetPrefix(TabPlayer)
     * @see     #getCustomPrefix(TabPlayer)
     */
    String getOriginalPrefix(TabPlayer player);

    /**
     * Returns player's original name applied using plugin's internal logic.
     * This value is not affected by API calls in any way.
     *
     * @param   player
     *          player to get original name of
     * @return  player's original name
     * @see     #setName(TabPlayer, String)
     * @see     #resetName(TabPlayer)
     * @see     #getCustomName(TabPlayer)
     */
    String getOriginalName(TabPlayer player);

    /**
     * Returns player's original suffix applied using plugin's internal logic.
     * This value is not affected by API calls in any way.
     *
     * @param   player
     *          player to get original suffix of
     * @return  player's original suffix
     * @see     #setSuffix(TabPlayer, String)
     * @see     #resetSuffix(TabPlayer)
     * @see     #getCustomSuffix(TabPlayer)
     */
    String getOriginalSuffix(TabPlayer player);
}
