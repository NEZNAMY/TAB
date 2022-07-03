package me.neznamy.tab.shared.features.layout;

import java.util.*;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.placeholders.conditions.Condition;

public class ParentGroup {

    private final Layout layout;
    private final Condition condition;
    private final int[] slots;
    private final Map<Integer, PlayerSlot> playerSlots = new HashMap<>();
    private final Map<TabPlayer, PlayerSlot> players = new HashMap<>();

    public ParentGroup(Layout layout, Condition condition, int[] slots) {
        this.layout = layout;
        this.condition = condition;
        if (condition != null) {
            layout.addUsedPlaceholders(Collections.singletonList(TabConstants.Placeholder.condition(condition.getName())));
        }
        this.slots = slots;
        for (int slot : slots) {
            playerSlots.put(slot, new PlayerSlot(layout, layout.getManager().getUUID(slot)));
        }
    }

    public void tick(List<TabPlayer> remainingPlayers){
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
    
    public List<PlayerInfoData> getSlots(TabPlayer p) {
        List<PlayerInfoData> data = new ArrayList<>();
        playerSlots.values().forEach(s -> data.add(s.getSlot(p)));
        return data;
    }
    
    public Map<TabPlayer, PlayerSlot> getPlayers() {
        return players;
    }
}