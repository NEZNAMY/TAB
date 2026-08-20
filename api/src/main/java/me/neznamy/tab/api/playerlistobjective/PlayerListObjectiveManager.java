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
     * both the config option and the disable condition. This only affects what the
     * player sees, not how the player is displayed to others. Use {@code null} to
     * give the control back to configuration.
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
     * If no visibility is forced, returns {@code null}.
     *
     * @param   player
     *          Player to get forced visibility of
     * @return  Forced visibility or {@code null} if not forced
     * @see     #setEnabled(TabPlayer, Boolean)
     */
    @Nullable Boolean getCustomEnabled(@NonNull TabPlayer player);

    /**
     * Changes score value of specified player to provided value. Supports placeholders,
     * however, the value must evaluate to a number. Only 1.20.2 and lower clients display
     * this value, newer clients display the number format instead. Use {@code null} to
     * reset value back to the configured one.
     *
     * @param   player
     *          Player to change score value of
     * @param   value
     *          New score value or {@code null} to reset back to configuration
     * @see     #getCustomValue(TabPlayer)
     * @see     #setFancyValue(TabPlayer, String)
     */
    void setValue(@NonNull TabPlayer player, @Nullable String value);

    /**
     * Returns score value assigned using {@link #setValue(TabPlayer, String)}.
     * If no value is assigned, returns {@code null}.
     *
     * @param   player
     *          Player to get custom score value of
     * @return  Custom score value assigned using the API
     * @see     #setValue(TabPlayer, String)
     */
    @Nullable String getCustomValue(@NonNull TabPlayer player);

    /**
     * Changes number format of specified player to provided value. Supports placeholders,
     * as well as any supported RGB formats. Only 1.20.3+ clients display the number format,
     * older clients display the score value instead. Use {@code null} to reset value back
     * to the configured one.
     *
     * @param   player
     *          Player to change number format of
     * @param   fancyValue
     *          New number format or {@code null} to reset back to configuration
     * @see     #getCustomFancyValue(TabPlayer)
     * @see     #setValue(TabPlayer, String)
     */
    void setFancyValue(@NonNull TabPlayer player, @Nullable String fancyValue);

    /**
     * Returns number format assigned using {@link #setFancyValue(TabPlayer, String)}.
     * If no number format is assigned, returns {@code null}.
     *
     * @param   player
     *          Player to get custom number format of
     * @return  Custom number format assigned using the API
     * @see     #setFancyValue(TabPlayer, String)
     */
    @Nullable String getCustomFancyValue(@NonNull TabPlayer player);

    /**
     * Changes objective title of specified player to provided value. Supports placeholders,
     * as well as any supported RGB formats. The title is only visible on Bedrock Edition.
     * Use {@code null} to reset value back to the configured one.
     *
     * @param   player
     *          Player to change objective title of
     * @param   title
     *          New objective title or {@code null} to reset back to configuration
     * @see     #getCustomTitle(TabPlayer)
     */
    void setTitle(@NonNull TabPlayer player, @Nullable String title);

    /**
     * Returns objective title assigned using {@link #setTitle(TabPlayer, String)}.
     * If no title is assigned, returns {@code null}.
     *
     * @param   player
     *          Player to get custom objective title of
     * @return  Custom objective title assigned using the API
     * @see     #setTitle(TabPlayer, String)
     */
    @Nullable String getCustomTitle(@NonNull TabPlayer player);

    /**
     * Changes render type of the objective for specified player. Use {@code null} to reset
     * value back to the configured one.
     *
     * @param   player
     *          Player to change render type of
     * @param   renderType
     *          New render type or {@code null} to reset back to configuration
     * @see     #getCustomRenderType(TabPlayer)
     */
    void setRenderType(@NonNull TabPlayer player, @Nullable RenderType renderType);

    /**
     * Returns render type assigned using {@link #setRenderType(TabPlayer, RenderType)}.
     * If no render type is assigned, returns {@code null}.
     *
     * @param   player
     *          Player to get custom render type of
     * @return  Custom render type assigned using the API
     * @see     #setRenderType(TabPlayer, RenderType)
     */
    @Nullable RenderType getCustomRenderType(@NonNull TabPlayer player);

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
