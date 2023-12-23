package me.neznamy.tab.api.nametag;

import lombok.NonNull;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for manipulating player name tags.
 * <p>
 * Instance can be obtained using {@link TabAPI#getNameTagManager()}.
 * This requires the Team feature to be enabled in config, otherwise the method will
 * return {@code null}.
 */
@SuppressWarnings("unused") // API class
public interface NameTagManager {

    /**
     * Makes player's NameTag globally invisible
     *
     * @param   player
     *          player to hide name tag of
     * @see     #showNameTag(TabPlayer)
     * @see     #hasHiddenNameTag(TabPlayer)
     */
    void hideNameTag(@NonNull TabPlayer player);

    /**
     * Hides player's NameTag for specified player until it's shown again
     *
     * @param   player
     *          player to hide name tag of
     * @param   viewer
     *          player to hide name tag for
     */
    void hideNameTag(@NonNull TabPlayer player, @NonNull TabPlayer viewer);

    /**
     * Makes player's NameTag visible again
     *
     * @param   player
     *          player to show name tag of
     * @see     #hideNameTag(TabPlayer)
     * @see     #hasHiddenNameTag(TabPlayer)
     */
    void showNameTag(@NonNull TabPlayer player);

    /**
     * Shows player's NameTag for specified viewer if it was hidden before
     *
     * @param   player
     *          player to show name tag of
     * @param   viewer
     *          player to show NameTag back for
     */
    void showNameTag(@NonNull TabPlayer player, @NonNull TabPlayer viewer);

    /**
     * Return whether player has hidden NameTag or not
     *
     * @param   player
     *          player to check name tag visibility status of
     * @return  Whether player has hidden NameTag or not
     * @see     #hideNameTag(TabPlayer)
     * @see     #showNameTag(TabPlayer)
     */
    boolean hasHiddenNameTag(@NonNull TabPlayer player);

    /**
     * Returns true if NameTag is hidden for specified viewer, false if not
     *
     * @param   player
     *          player to check visibility status of
     * @param   viewer
     *          player to check visibility status for
     * @return  true if hidden, false if not
     */
    boolean hasHiddenNameTag(@NonNull TabPlayer player, @NonNull TabPlayer viewer);

    /**
     * Unregisters player's team and no longer handles it, as well as disables anti-override for teams.
     * This can be resumed using resumeTeamHandling(). If team handling was paused already, nothing happens.
     *
     * @param   player
     *          player to pause team handling of
     */
    void pauseTeamHandling(@NonNull TabPlayer player);

    /**
     * Resumes team handling if it was before paused using pauseTeamHandling(), if not, nothing happens
     *
     * @param   player
     *          player to resume team handling of
     */
    void resumeTeamHandling(@NonNull TabPlayer player);

    /**
     * Returns true if team handling is paused for this player using pauseTeamHandling(), false if not, or
     * it was resumed already using resumeTeamHandling
     *
     * @param   player
     *          player to check handling status of
     * @return  true if paused, false if not
     */
    boolean hasTeamHandlingPaused(@NonNull TabPlayer player);

    /**
     * Forces collision rule for the player. Setting it to null will remove forced value
     *
     * @param   player
     *          player to set collision rule of
     * @param   collision
     *          forced collision rule
     */
    void setCollisionRule(@NonNull TabPlayer player, @Nullable Boolean collision);

    /**
     * Returns forced collision rule or null if collision is not forced using setCollisionRule
     *
     * @param   player
     *          player to get forced collision of
     * @return  forced value or null if not forced
     */
    @Nullable Boolean getCollisionRule(@NonNull TabPlayer player);

    /**
     * Changes player's prefix to provided value. Supports placeholders,
     * as well as any supported RGB formats. Use {@code null} to reset
     * value back to original.
     *
     * @param   player
     *          player to change prefix of
     * @param   prefix
     *          new prefix value
     * @see     #getCustomPrefix(TabPlayer)
     * @see     #getOriginalPrefix(TabPlayer)
     */
    void setPrefix(@NonNull TabPlayer player, @Nullable String prefix);

    /**
     * Changes player's suffix to provided value. Supports placeholders,
     * as well as any supported RGB formats. Use {@code null} to reset
     * value back to original.
     *
     * @param   player
     *          player to change prefix of
     * @param   suffix
     *          new suffix value
     * @see     #getCustomSuffix(TabPlayer)
     * @see     #getOriginalSuffix(TabPlayer)
     */
    void setSuffix(@NonNull TabPlayer player, @Nullable String suffix);

    /**
     * Returns custom prefix assigned using {@link #setPrefix(TabPlayer, String)}.
     * If no custom prefix is set, returns {@code null}.
     *
     * @param   player
     *          Player to get custom prefix of
     * @return  Custom prefix assigned using the API.
     * @see     #setPrefix(TabPlayer, String)
     * @see     #getOriginalPrefix(TabPlayer)
     */
    @Nullable
    String getCustomPrefix(@NonNull TabPlayer player);

    /**
     * Returns custom suffix assigned using {@link #setSuffix(TabPlayer, String)}.
     * If no custom suffix is set, returns {@code null}.
     *
     * @param   player
     *          Player to get custom suffix of
     * @return  Custom suffix assigned using the API.
     * @see     #setSuffix(TabPlayer, String)
     * @see     #getOriginalSuffix(TabPlayer)
     */
    @Nullable
    String getCustomSuffix(@NonNull TabPlayer player);

    /**
     * Returns original prefix assigned by the plugin using internal logic.
     * @param   player
     *          Player to get original prefix of
     * @return  Original prefix assigned by the plugin's internal logic.
     * @see     #getCustomPrefix(TabPlayer)
     */
    @NonNull
    String getOriginalPrefix(@NonNull TabPlayer player);

    /**
     * Returns original suffix assigned by the plugin using internal logic.
     * @param   player
     *          Player to get original suffix of
     * @return  Original suffix assigned by the plugin's internal logic.
     * @see     #getCustomSuffix(TabPlayer)
     */
    @NonNull
    String getOriginalSuffix(@NonNull TabPlayer player);

    /**
     * Toggles name tag visibility view on all players for specified player.
     * On first call, name tags of all players will become invisible for specified
     * player. On second call, they will become visible again.
     *
     * @param   player
     *          player to toggle name tag visibility view for
     * @param   sendToggleMessage
     *          {@code true} if configured toggle message should be sent, {@code false} if not
     */
    void toggleNameTagVisibilityView(@NonNull TabPlayer player, boolean sendToggleMessage);

    /**
     * Returns {@code true} if player has hidden name tags by either calling
     * {@link #toggleNameTagVisibilityView(TabPlayer, boolean)} or using a command,
     * {@code false} if not.
     *
     * @param   player
     *          player to check
     * @return  {@code true} if hidden, {@code false} if not
     */
    boolean hasHiddenNameTagVisibilityView(@NonNull TabPlayer player);
}
