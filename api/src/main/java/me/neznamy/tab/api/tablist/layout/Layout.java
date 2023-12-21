package me.neznamy.tab.api.tablist.layout;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface representing a layout.
 */
@SuppressWarnings("unused") // API class
public interface Layout {

    /**
     * Returns name of the layout.
     *
     * @return  name of the layout
     */
    @NotNull String getName();

    /**
     * Adds fixed slot with specified parameters
     *
     * @param   slot
     *          Slot (1-80)
     * @param   text
     *          Text to display
     */
    void addFixedSlot(int slot, @NonNull String text);

    /**
     * Adds fixed slot with specified parameters
     *
     * @param   slot
     *          Slot (1-80)
     * @param   text
     *          Text to display
     * @param   skin
     *          Skin definition like in config
     */
    void addFixedSlot(int slot, @NonNull String text, @NonNull String skin);

    /**
     * Adds fixed slot with specified parameters
     *
     * @param   slot
     *          Slot (1-80)
     * @param   text
     *          Text to display
     * @param   ping
     *          Slot's ping
     */
    void addFixedSlot(int slot, @NonNull String text, int ping);

    /**
     * Adds fixed slot with specified parameters
     *
     * @param   slot
     *          Slot (1-80)
     * @param   text
     *          Text to display
     * @param   skin
     *          Skin definition like in config
     * @param   ping
     *          Slot's ping
     */
    void addFixedSlot(int slot, @NonNull String text, @NonNull String skin, int ping);

    /**
     * Adds a player group.
     *
     * @param   condition
     *          Condition that must be met for player to appear in the group.
     *          May be null for no requirement. Syntax is like in config -
     *          either condition name or short format.
     * @param   slots
     *          Slots for this player group
     */
    void addGroup(@Nullable String condition, int[] slots);
}
