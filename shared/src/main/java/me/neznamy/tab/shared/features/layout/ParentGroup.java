package me.neznamy.tab.shared.features.layout;

import java.util.*;

import lombok.Getter;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ParentGroup {

    @NotNull private final LayoutView layout;
    @Nullable private final Condition condition;
    @Getter private final int[] slots;
    private final TabPlayer viewer;
    @Getter private final Map<Integer, PlayerSlot> playerSlots = new HashMap<>();
    @Getter final Map<TabPlayer, PlayerSlot> players = new HashMap<>();

    public ParentGroup(@NotNull LayoutView layout, @NotNull GroupPattern pattern, @NotNull TabPlayer viewer) {
        this.layout = layout;
        condition = pattern.getCondition();
        slots = pattern.getSlots();
        this.viewer = viewer;
        for (int slot : slots) {
            playerSlots.put(slot, new PlayerSlot(slot, layout, layout.getManager().getUUID(slot)));
        }
    }

    public void tick(@NotNull List<TabPlayer> remainingPlayers) {
        players.clear();
        List<TabPlayer> meetingCondition = new ArrayList<>();
        for (TabPlayer p : remainingPlayers) {
            if (condition == null || condition.isMet(p)) meetingCondition.add(p);
        }
        remainingPlayers.removeAll(meetingCondition);
        for (int index = 0; index < slots.length; index++) {
            int slot = slots[index];
            if (layout.getManager().isRemainingPlayersTextEnabled() && index == slots.length - 1 && playerSlots.size() < meetingCondition.size()) {
                playerSlots.get(slot).setText(String.format(layout.getManager().getRemainingPlayersText(), meetingCondition.size() - playerSlots.size() + 1));
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
        playerSlots.values().forEach(s -> viewer.getTabList().addEntry(s.getSlot(viewer)));
    }
}