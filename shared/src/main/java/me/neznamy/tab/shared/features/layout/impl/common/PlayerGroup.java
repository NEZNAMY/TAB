package me.neznamy.tab.shared.features.layout.impl.common;

import lombok.Getter;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.layout.impl.LayoutBase;
import me.neznamy.tab.shared.features.layout.pattern.GroupPattern;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A group of players in a layout, defined by a condition and slots.
 */
@Getter
public class PlayerGroup {

    @NotNull private final LayoutBase layout;
    @Nullable private final Condition condition;
    private final int[] slots;
    private final Map<Integer, PlayerSlot> playerSlots = new HashMap<>();
    private final Map<TabPlayer, PlayerSlot> players = new HashMap<>();

    /**
     * Constructs a new PlayerGroup with the specified layout and group pattern.
     *
     * @param   layout
     *          The layout this group belongs to
     * @param   pattern
     *          The group pattern defining the condition and slots
     */
    public PlayerGroup(@NotNull LayoutBase layout, @NotNull GroupPattern pattern) {
        this.layout = layout;
        condition = TAB.getInstance().getPlaceholderManager().getConditionManager().getByNameOrExpression(pattern.getCondition());
        slots = pattern.getSlots();
        for (int slot : slots) {
            playerSlots.put(slot, new PlayerSlot(slot, layout, layout.getManager().getUUID(slot)));
        }
    }

    /**
     * Updates the player slots in this group based on the remaining players.
     *
     * @param   remainingPlayers
     *          The list of players remaining to be assigned to slots
     */
    public void tick(@NotNull List<TabPlayer> remainingPlayers) {
        players.clear();
        List<TabPlayer> meetingCondition = new ArrayList<>();

        // High-performance way to filter players
        remainingPlayers.removeIf(p -> {
            boolean met = (condition == null || condition.isMet(layout.getViewer(), p));
            if (met) meetingCondition.add(p);
            return met;
        });

        for (int index = 0; index < slots.length; index++) {
            int slot = slots[index];
            if (layout.getManager().getConfiguration().isRemainingPlayersTextEnabled() && index == slots.length - 1 && playerSlots.size() < meetingCondition.size()) {
                playerSlots.get(slot).setText(String.format(layout.getManager().getConfiguration().getRemainingPlayersText(), meetingCondition.size() - playerSlots.size() + 1));
                break;
            }
            if (meetingCondition.size() > index) {
                TabPlayer p = meetingCondition.get(index);
                playerSlots.get(slot).setPlayer(p);
                players.put(p, playerSlots.get(slot));
            } else {
                playerSlots.get(slot).setText("");
            }
        }
    }

    /**
     * Sends all player slots in this group to the viewer's tab list.
     */
    public void sendAll() {
        for (PlayerSlot s : playerSlots.values()) {
            layout.getViewer().getTabList().addEntry(s.getSlot(layout.getViewer()));
        }
    }
}