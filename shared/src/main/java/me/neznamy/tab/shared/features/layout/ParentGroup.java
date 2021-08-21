package me.neznamy.tab.shared.features.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
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
		this.slots = slots;
		for (int slot : slots) {
			playerSlots.put(slot, new PlayerSlot(layout, layout.getUUID(slot), slot));
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
			if (index == slots.length - 1 && playerSlots.size() < meetingCondition.size()) {
				playerSlots.get(slot).setText(String.format(layout.getRemainingPlayersText(), meetingCondition.size() - playerSlots.size() + 1));
				break;
			}
			if (meetingCondition.size() > index) {
				TabPlayer p = meetingCondition.get(index);
				playerSlots.get(slot).setPlayer(p);
				players.put(p, playerSlots.get(slot));
			} else {
				playerSlots.get(slot).setPlayer(null);
				playerSlots.get(slot).setText("");
			}
		}
	}
	
	public void onJoin(TabPlayer p) {
		playerSlots.values().forEach(s -> s.onJoin(p));
	}
	
	public Map<TabPlayer, PlayerSlot> getPlayers() {
		if (players == null) {
			players = new HashMap<>();
			TAB.getInstance().getErrorManager().printError("Player map in layout group was null");
		}
		return players;
	}
}