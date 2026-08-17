package me.neznamy.tab.api.playerlistobjective;

import lombok.NonNull;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for manipulating the playerlist objective of individual players.
 * <p>
 * Instance can be obtained using {@link TabAPI#getPlayerListObjectiveManager()}.
 * Unlike other features, this one is available even if it is disabled in config,
 * which allows enabling it for specific players using {@link #setEnabled(TabPlayer, Boolean)}.
 */
@SuppressWarnings("unused") // API class
public interface PlayerListObjectiveManager {

    /**
     * Forces the objective to be visible or hidden for specified player, overriding
     * both the config option and the disable condition. Use {@code null} to give the
     * control back to configuration.
     *
     * @param   player
     *          Player to set visibility for
     * @param   enabled
     *          Forced visibility or {@code null} to reset back to configuration
     * @see     #getCustomEnabled(TabPlayer)
     */
    void setEnabled(@NonNull TabPlayer player, @Nullable Boolean enabled);

    /**
     * Returns forced visibility assigned using {@link #setEnabled(TabPlayer, Boolean)}.
     * If no value is forced, returns {@code null}.
     *
     * @param   player
     *          Player to get forced visibility of
     * @return  Forced visibility or {@code null} if not forced
     * @see     #setEnabled(TabPlayer, Boolean)
     */
    @Nullable Boolean getCustomEnabled(@NonNull TabPlayer player);

    /**
     * Changes score of specified player to provided value for everyone who can see it.
     * This also replaces the number format with the same value. Use {@code null} to reset
     * value back to the configured one.
     *
     * @param   player
     *          Player to change score of
     * @param   value
     *          New score value or {@code null} to reset back to configuration
     */
    void setValue(@NonNull TabPlayer player, @Nullable Integer value);

    /**
     * Changes objective title of specified player to provided value. Supports placeholders,
     * as well as any supported RGB formats. Use {@code null} to reset value back to the
     * configured one.
     *
     * @param   player
     *          Player to change objective title of
     * @param   title
     *          New objective title or {@code null} to reset back to configuration
     */
    void setTitle(@NonNull TabPlayer player, @Nullable String title);

    /**
     * Changes render type of the objective for specified player. Use {@code null} to reset
     * value back to the configured one.
     *
     * @param   player
     *          Player to change render type of
     * @param   renderType
     *          New render type or {@code null} to reset back to configuration
     */
    void setRenderType(@NonNull TabPlayer player, @Nullable RenderType renderType);

    /**
     * Removes all values forced using this API for specified player and gives the control
     * back to configuration.
     *
     * @param   player
     *          Player to remove forced values of
     */
    void reset(@NonNull TabPlayer player);

    /**
     * Render type of the objective, defining how the score is displayed.
     */
    enum RenderType {

        /** Score is displayed as a number */
        INTEGER,

        /** Score is displayed as hearts */
        HEARTS
    }
}
