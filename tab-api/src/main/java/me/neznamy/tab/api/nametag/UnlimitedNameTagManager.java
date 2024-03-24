package me.neznamy.tab.api.nametag;

import java.util.List;

import lombok.NonNull;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for manipulating player name tags.
 * <p>
 * Instance can be obtained using {@link TabAPI#getNameTagManager()} and then casting it.
 * This requires both Team and Unlimited name tag features to be enabled in config, otherwise the method will
 * return {@code null} or not be an instance of this class.
 */
@SuppressWarnings("unused") // API class
public interface UnlimitedNameTagManager extends NameTagManager {

    /**
     * Disables armor stands of specified player and displays vanilla name tags instead.
     *
     * @param   player
     *          Player to disable armor stands of
     * @see     #enableArmorStands(TabPlayer)
     * @see     #hasDisabledArmorStands(TabPlayer)
     */
    void disableArmorStands(@NonNull TabPlayer player);

    /**
     * Enabled back armor stands of player, who was disabled previously using
     * {@link #disableArmorStands(TabPlayer)}.
     *
     * @param   player
     *          Player to enable back
     * @see     #disableArmorStands(TabPlayer)
     * @see     #hasDisabledArmorStands(TabPlayer)
     */
    void enableArmorStands(@NonNull TabPlayer player);

    /**
     * Returns {@code true} if player has armor stands disabled using
     * {@link #disableArmorStands(TabPlayer)}, {@code false} otherwise.
     *
     * @param   player
     *          Player to check
     * @return  {@code true} if disabled, {@code false} if not.
     * @see     #disableArmorStands(TabPlayer)
     * @see     #enableArmorStands(TabPlayer)
     */
    boolean hasDisabledArmorStands(@NonNull TabPlayer player);

    /**
     * Changes name to specified value. Set to {@code null} to
     * reset back to original value.
     *
     * @param   player
     *          Player to change name of
     * @param   customName
     *          New name, or {@code null} to reset to original value
     */
    void setName(@NonNull TabPlayer player, @Nullable String customName);

    /**
     * Changes line to specified value. Set to {@code null} to reset back to original value.
     *
     * @param   player
     *          Player to change line of
     * @param   line
     *          Name of defined line
     * @param   value
     *          Value to use, or {@code null} to reset back to original value
     * @see     #getDefinedLines()
     */
    void setLine(@NonNull TabPlayer player, @NonNull String line, @Nullable String value);

    /**
     * Returns custom name value set using {@link #setName(TabPlayer, String)}.
     *
     * @param   player
     *          Player to check
     * @return  Custom name set previously or {@code null} if not set
     * @see     #setName(TabPlayer, String)
     */
    @Nullable String getCustomName(@NonNull TabPlayer player);

    /**
     * Returns custom line value set using {@link #setLine(TabPlayer, String, String)}.
     *
     * @param   player
     *          Player to check
     * @param   line
     *          Defined line
     * @return  Custom name set previously or {@code null} if not set
     * @see     #setName(TabPlayer, String)
     * @see     #getDefinedLines()
     */
    @Nullable String getCustomLineValue(@NonNull TabPlayer player, @NonNull String line);

    /**
     * Returns original name value configured in files.
     *
     * @param   player
     *          Player to check
     * @return  Original configured name
     * @see     #setName(TabPlayer, String)
     */
    @NotNull String getOriginalName(@NonNull TabPlayer player);

    /**
     * Returns original line value configured in files.
     *
     * @param   player
     *          Player to check
     * @param   line
     *          Line name
     * @return  Original configured line
     * @see     #setLine(TabPlayer, String, String)
     */
    @NotNull String getOriginalLineValue(@NonNull TabPlayer player, @NonNull String line);

    /**
     * Returns list of all defined lines in config, both static and dynamic lines.
     *
     * @return  List of all defined lines in config
     */
    @NotNull List<String> getDefinedLines();
}
