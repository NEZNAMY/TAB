package me.neznamy.tab.shared.features.layout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.placeholders.conditions.Condition;

public class ParentGroup {

	private Layout layout;
	private Condition condition;
	private int[] slots;
	private Map<Integer, PlayerSlot> playerSlots = new HashMap<>();
	private Map<TabPlayer, PlayerSlot> players = new HashMap<>();

	public ParentGroup(Layout layout, Condition condition, int[] slots) {
		this.layout = layout;
		this.condition = condition;
		if (condition != null) {
			layout.addUsedPlaceholders(Arrays.asList("%condition:" + condition.getName() + "%"));
		}
		this.slots = slots;
		for (int slot : slots) {
			playerSlots.put(slot, new PlayerSlot(layout, layout.getManager().getUUID(slot), slot));
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
	
	public void sendTo(TabPlayer p) {
		playerSlots.values().forEach(s -> s.sendSlot(p));
	}
	
	public Map<TabPlayer, PlayerSlot> getPlayers() {
		return players;
	}
}