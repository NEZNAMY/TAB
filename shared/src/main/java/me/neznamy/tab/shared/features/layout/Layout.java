package me.neznamy.tab.shared.features.layout;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.UUID;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.ConfigurationFile;
import me.neznamy.tab.shared.config.YamlConfigurationFile;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.event.JoinEventListener;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumGamemode;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.shared.placeholders.conditions.Condition;

public class Layout implements Loadable, JoinEventListener {

	private TAB tab;
	private LayoutDirection direction;
	private Map<Integer, FixedSlot> fixedSlots = new HashMap<>();
	private List<ParentGroup> parentGroups = new ArrayList<>();
	private Map<String, ChildGroup> childGroups = new HashMap<>();
	private Map<Integer, UUID> uuids = new HashMap<>();

	public Layout(TAB tab) {
		this.tab = tab;
		try {
			new File(tab.getPlatform().getDataFolder() + File.separator + "layout").mkdirs();
			ConfigurationFile file = new YamlConfigurationFile(getClass().getClassLoader().getResourceAsStream("layout/default.yml"), new File(tab.getPlatform().getDataFolder(), "layout" + File.separator + "default.yml"));
			direction = parseDirection(file.getString("direction", "COLUMNS"));
			for (int i=1; i<=80; i++) {
				fixedSlots.put(i, new FixedSlot(i, "", null));
				uuids.put(i, UUID.randomUUID());
			}
			for (String fixedSlot : file.getStringList("fixed-slots")) {
				String[] array = fixedSlot.split(":");
				int slot = Integer.parseInt(array[0]);
				String text = array[1];
				String skin = null;
				if (array.length == 3) {
					skin = array[2];
				}
				fixedSlots.put(slot, new FixedSlot(slot, text, skin));
			}
			loadGroups(file);
		} catch (Exception e) {
			tab.getErrorManager().criticalError("Failed to load layout feature", e);
		}
	}
	
	private LayoutDirection parseDirection(String value) {
		try {
			return LayoutDirection.valueOf(value);
		} catch (Exception e) {
			tab.getErrorManager().startupWarn("\"&e" + value + "&c\" is not a valid type of layout direction. Valid options are: &e" + Arrays.deepToString(LayoutDirection.values()) + ". &bUsing COLUMNS");
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
			result.put(i, new IChatBaseComponent(""));
		}

		for (FixedSlot fixed : fixedSlots.values()) {
			result.put(fixed.getSlot(), IChatBaseComponent.optimizedComponent(fixed.getText(viewer)));
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
			teamMap.put(p, p.getTeamName());
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
	public TabFeature getFeatureType() {
		return TabFeature.TABLIST_LAYOUT;
	}

	@Override
	public void onJoin(TabPlayer connectedPlayer) {
		List<PlayerInfoData> list = new ArrayList<>();
		for (FixedSlot s : fixedSlots.values()) {
			s.onJoin(connectedPlayer);
		}
		for (Entry<Integer, IChatBaseComponent> entry : doTick(connectedPlayer, sortPlayers(tab.getPlayers())).entrySet()) {
			int slot = translateSlot(entry.getKey());
			list.add(new PlayerInfoData((char)1 + "SLOT-" + (slot < 10 ? "0" + slot : String.valueOf(slot)), uuids.get(slot), null, 0, EnumGamemode.CREATIVE, entry.getValue()));
		}
		connectedPlayer.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, list), getFeatureType());
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
		tab.getCPUManager().startRepeatingMeasuredTask(500, "ticking layout", getFeatureType(), UsageType.REPEATING_TASK, () -> {

			List<TabPlayer> players = sortPlayers(tab.getPlayers());
			for (TabPlayer p : tab.getPlayers()) {
				if (!p.isLoaded()) continue;
				List<PlayerInfoData> list = new ArrayList<>();
				for (Entry<Integer, IChatBaseComponent> entry : doTick(p, new ArrayList<>(players)).entrySet()) {
					int slot = translateSlot(entry.getKey());
					list.add(new PlayerInfoData(uuids.get(slot), entry.getValue()));
				}
				p.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, list), getFeatureType());
			}
		});
		List<TabPlayer> players = sortPlayers(tab.getPlayers());
		for (TabPlayer p : tab.getPlayers()) {
			List<PlayerInfoData> list = new ArrayList<>();
			for (FixedSlot s : fixedSlots.values()) {
				s.onJoin(p);
			}
			for (Entry<Integer, IChatBaseComponent> entry : doTick(p, new ArrayList<>(players)).entrySet()) {
				int slot = translateSlot(entry.getKey());
				list.add(new PlayerInfoData((char)1 + "SLOT-" + (slot < 10 ? "0" + slot : String.valueOf(slot)), uuids.get(slot), null, 0, EnumGamemode.CREATIVE, entry.getValue()));
			}
			p.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, list), getFeatureType());
		}
	}

	@Override
	public void unload() {
		List<TabPlayer> players = sortPlayers(tab.getPlayers());
		for (TabPlayer p : tab.getPlayers()) {
			List<PlayerInfoData> list = new ArrayList<>();
			for (Entry<Integer, IChatBaseComponent> entry : doTick(p, new ArrayList<>(players)).entrySet()) {
				int slot = translateSlot(entry.getKey());
				list.add(new PlayerInfoData(uuids.get(slot)));
			}
			p.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, list), getFeatureType());
		}
	}
}