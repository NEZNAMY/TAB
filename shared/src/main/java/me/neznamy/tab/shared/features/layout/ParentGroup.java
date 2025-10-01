package me.neznamy.tab.shared.features.layout;

import lombok.Getter;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.layout.LayoutConfiguration.LayoutDefinition.GroupPattern;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParentGroup {

    @NotNull private final LayoutView layout;
    @Nullable private final Condition condition;
    @Getter private final int[] slots;
    private final TabPlayer viewer;
    @Getter private final Map<Integer, PlayerSlot> playerSlots = new HashMap<>();
    @Getter private final Map<TabPlayer, PlayerSlot> players = new HashMap<>();

    public ParentGroup(@NotNull LayoutView layout, @NotNull GroupPattern pattern, @NotNull TabPlayer viewer) {
        this.layout = layout;
        condition = TAB.getInstance().getPlaceholderManager().getConditionManager().getByNameOrExpression(pattern.getCondition());
        slots = pattern.getSlots();
        this.viewer = viewer;
        for (int slot : slots) {
            playerSlots.put(slot, new PlayerSlot(slot, layout, layout.getManager().getUUID(slot)));
        }
    }

    public void tick(@NotNull List<TabPlayer> remainingPlayers) {
        players.clear();
        List<TabPlayer> meetingCondition = new ArrayList<>();

        // High-performance way to filter players
        remainingPlayers.removeIf(p -> {
            boolean met = (condition == null || condition.isMet(p));
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
    
    public void sendSlots() {
        for (PlayerSlot s : playerSlots.values()) {
            viewer.getTabList().addEntry(s.getSlot(viewer));
        }
    }
}