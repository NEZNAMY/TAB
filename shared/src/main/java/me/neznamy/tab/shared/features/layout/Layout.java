package me.neznamy.tab.shared.features.layout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.config.ConfigurationFile;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumGamemode;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.placeholders.conditions.Condition;

public class Layout extends TabFeature {

	private LayoutDirection direction;
	private Map<Integer, FixedSlot> fixedSlots = new HashMap<>();
	private List<ParentGroup> parentGroups = new ArrayList<>();
	private Map<String, ChildGroup> childGroups = new HashMap<>();
	private Map<Integer, UUID> uuids = new HashMap<>();

	public Layout() {
		super("Tablist layout");
		direction = parseDirection(TAB.getInstance().getConfiguration().getLayout().getString("direction", "COLUMNS"));
		for (int slot=1; slot<=80; slot++) {
			fixedSlots.put(slot, new FixedSlot(slot, ""));
			uuids.put(slot, UUID.randomUUID());
		}
		for (String fixedSlot : TAB.getInstance().getConfiguration().getLayout().getStringList("fixed-slots")) {
			String[] array = fixedSlot.split(":");
			int slot = Integer.parseInt(array[0]);
			String text = array[1];
			fixedSlots.put(slot, new FixedSlot(slot, text));
		}
		for (Entry<Integer, FixedSlot> entry : fixedSlots.entrySet()) {
			if (entry.getValue().getText().length() > 0) 
				TAB.getInstance().getFeatureManager().registerFeature("layout-slot-" + entry.getKey(), entry.getValue());
		}
		loadGroups(TAB.getInstance().getConfiguration().getLayout());
	}
	
	private LayoutDirection parseDirection(String value) {
		try {
			return LayoutDirection.valueOf(value);
		} catch (Exception e) {
			TAB.getInstance().getErrorManager().startupWarn("\"&e" + value + "&c\" is not a valid type of layout direction. Valid options are: &e" + Arrays.deepToString(LayoutDirection.values()) + ". &bUsing COLUMNS");
			return LayoutDirection.COLUMNS;
		}
	}
	
	private void loadGroups(ConfigurationFile file) {
		for (Object obj : file.getConfigurationSection("child-groups").keySet()){
			String title = file.getString("child-groups." + obj + ".title");
			Condition condition = Condition.getCondition(file.getString("child-groups." + obj + ".condition"));
			childGroups.put(obj.toString(), new ChildGroup(title, condition));
		}
		for (Object obj : file.getConfigurationSection("parent-groups").keySet()){
			Condition condition = Condition.getCondition(file.getString("parent-groups." + obj + ".condition"));
			List<Integer> positions = new ArrayList<>();
			for (String line : file.getStringList("parent-groups." + obj + ".slots")) {
				String[] arr = line.split("-");
				int from = Integer.parseInt(arr[0]);
				int to = Integer.parseInt(arr[1]);
				for (int i = from; i<= to; i++) {
					positions.add(i);
				}
			}
			List<ChildGroup> childs = new ArrayList<>();
			if (file.hasConfigOption("parent-groups." + obj + ".child-groups")) {
				for (String child : file.getStringList("parent-groups." + obj + ".child-groups")) {
					if (childGroups.containsKey(child)) {
						childs.add(childGroups.get(child));
					}
				}
			}
			parentGroups.add(new ParentGroup(condition, positions, childs));
		}
	}

	private Map<Integer, IChatBaseComponent> doTick(TabPlayer viewer, List<TabPlayer> players) {
		Map<Integer, IChatBaseComponent> result = new HashMap<>();

		for (int i=1; i<=80; i++) {
			if (fixedSlots.get(i).getText().length() == 0) result.put(i, new IChatBaseComponent(""));
		}

		for (ParentGroup parent : parentGroups) {
			List<TabPlayer> meetingCondition = new ArrayList<>();
			for (TabPlayer target : players) {
				if (parent.getCondition() == null || parent.getCondition().isMet(target)) {
					meetingCondition.add(target);
				}
			}
			players.removeAll(meetingCondition);
			result.putAll(parent.createLayout(viewer, meetingCondition));
		}
		return result;
	}

	private List<TabPlayer> sortPlayers(Collection<TabPlayer> players){
		Map<TabPlayer, String> teamMap = new HashMap<>();
		for (TabPlayer p : players) {
			teamMap.put(p, String.valueOf(p.getTeamName()));
		}
		teamMap = sortByValue(teamMap);
		return new ArrayList<>(teamMap.keySet());
	}

	private <K, V extends Comparable<V>> Map<K, V> sortByValue(Map<K, V> map) {
		Comparator<K> valueComparator =  (k1, k2) -> {
			int compare = map.get(k2).compareTo(map.get(k1));
			if (compare == 0) return 1;
			else return -compare;
		};
		Map<K, V> sortedByValues = new TreeMap<>(valueComparator);
		sortedByValues.putAll(map);
		return sortedByValues;
	}

	@Override
	public void onJoin(TabPlayer connectedPlayer) {
		List<PlayerInfoData> list = new ArrayList<>();
		for (Entry<Integer, IChatBaseComponent> entry : doTick(connectedPlayer, sortPlayers(TAB.getInstance().getOnlinePlayers())).entrySet()) {
			int slot = translateSlot(entry.getKey());
			list.add(new PlayerInfoData((char)1 + String.format("SLOT%02d", slot), uuids.get(slot), null, 0, EnumGamemode.CREATIVE, entry.getValue()));
		}
		connectedPlayer.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, list), this);
	}

	private int translateSlot(int slot) {
		if (direction == LayoutDirection.ROWS) {
			return (slot-1)%4*20+(slot-((slot-1)%4))/4+1;
		} else {
			return slot;
		}
	}

	@Override
	public void load() {
		TAB.getInstance().getCPUManager().startRepeatingMeasuredTask(500, "ticking layout", this, UsageType.REPEATING_TASK, () -> {

			List<TabPlayer> players = sortPlayers(TAB.getInstance().getOnlinePlayers());
			for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
				if (!p.isLoaded()) continue;
				List<PlayerInfoData> list = new ArrayList<>();
				for (Entry<Integer, IChatBaseComponent> entry : doTick(p, new ArrayList<>(players)).entrySet()) {
					int slot = translateSlot(entry.getKey());
					list.add(new PlayerInfoData(uuids.get(slot), entry.getValue()));
				}
				p.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, list), this);
			}
		});
		List<TabPlayer> players = sortPlayers(TAB.getInstance().getOnlinePlayers());
		for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
			List<PlayerInfoData> list = new ArrayList<>();
			for (Entry<Integer, IChatBaseComponent> entry : doTick(p, new ArrayList<>(players)).entrySet()) {
				int slot = translateSlot(entry.getKey());
				list.add(new PlayerInfoData((char)1 + String.format("SLOT%02d", slot), uuids.get(slot), null, 0, EnumGamemode.CREATIVE, entry.getValue()));
			}
			p.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, list), this);
		}
	}

	@Override
	public void unload() {
		List<TabPlayer> players = sortPlayers(TAB.getInstance().getOnlinePlayers());
		for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
			List<PlayerInfoData> list = new ArrayList<>();
			for (Entry<Integer, IChatBaseComponent> entry : doTick(p, new ArrayList<>(players)).entrySet()) {
				int slot = translateSlot(entry.getKey());
				list.add(new PlayerInfoData(uuids.get(slot)));
			}
			p.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, list), this);
		}
	}
}