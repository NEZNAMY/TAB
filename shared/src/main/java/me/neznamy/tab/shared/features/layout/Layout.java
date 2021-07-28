package me.neznamy.tab.shared.features.layout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumGamemode;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.YellowNumber;
import me.neznamy.tab.shared.placeholders.conditions.Condition;

public class Layout extends TabFeature {

	private Direction direction;
	private Map<Integer, FixedSlot> fixedSlots = new HashMap<>();
	private List<Integer> emptySlots = new ArrayList<>();
	private List<ParentGroup> parentGroups = new ArrayList<>();
	private Map<Integer, UUID> uuids = new HashMap<>();
	private YellowNumberFix yellowNumberFix;

	public Layout() {
		super("Tablist layout");
		direction = parseDirection(TAB.getInstance().getConfiguration().getLayout().getString("direction", "COLUMNS"));
		for (int slot=1; slot<=80; slot++) {
			emptySlots.add(slot);
			uuids.put(slot, UUID.randomUUID());
		}
		for (String fixedSlot : TAB.getInstance().getConfiguration().getLayout().getStringList("fixed-slots")) {
			String[] array = fixedSlot.split("\\|");
			int slot = Integer.parseInt(array[0]);
			String text = array[1];
			FixedSlot f = new FixedSlot(this, uuids.get(slot), slot, text);
			fixedSlots.put(slot, f);
			emptySlots.remove((Integer)slot);
			if (text.length() > 0) TAB.getInstance().getFeatureManager().registerFeature("layout-slot-" + slot, f);
		}
		loadGroups();
		TAB.getInstance().debug("Loaded Layout feature");
		TAB.getInstance().getFeatureManager().registerFeature("latency", new LatencyRefresher(this));
		if (TAB.getInstance().getFeatureManager().isFeatureEnabled("tabobjective")) {
			yellowNumberFix = new YellowNumberFix(this, (YellowNumber) TAB.getInstance().getFeatureManager().getFeature("tabobjective"));
			TAB.getInstance().getFeatureManager().registerFeature("yellownumberfix", yellowNumberFix);
		}
	}

	private Direction parseDirection(String value) {
		try {
			return Direction.valueOf(value);
		} catch (Exception e) {
			TAB.getInstance().getErrorManager().startupWarn("\"&e" + value + "&c\" is not a valid type of layout direction. Valid options are: &e" + Arrays.deepToString(Direction.values()) + ". &bUsing COLUMNS");
			return Direction.COLUMNS;
		}
	}

	public String formatSlot(int slot) {
		return (char)1 + String.format("SLOT%02d", translateSlot(slot));
	}

	private void loadGroups() {
		for (Object obj : TAB.getInstance().getConfiguration().getLayout().getConfigurationSection("groups").keySet()){
			Condition condition = Condition.getCondition(TAB.getInstance().getConfiguration().getLayout().getString("groups." + obj + ".condition"));
			List<Integer> positions = new ArrayList<>();
			for (String line : TAB.getInstance().getConfiguration().getLayout().getStringList("groups." + obj + ".slots")) {
				String[] arr = line.split("-");
				int from = Integer.parseInt(arr[0]);
				int to = Integer.parseInt(arr[1]);
				for (int i = from; i<= to; i++) {
					positions.add(i);
				}
			}
			parentGroups.add(new ParentGroup(this, condition, positions.stream().mapToInt(i->i).toArray()));
			emptySlots.removeAll(positions);
		}
	}

	private List<TabPlayer> sortPlayers(Collection<TabPlayer> players){
		Map<TabPlayer, String> teamMap = new HashMap<>();
		for (TabPlayer p : players) {
			if (!p.isLoaded()) continue;
			teamMap.put(p, String.valueOf(p.getTeamName()));
		}
		teamMap = sortByValue(teamMap);
		return new ArrayList<>(teamMap.keySet());
	}

	private <K, V extends Comparable<V>> Map<K, V> sortByValue(Map<K, V> map) {
		Comparator<K> valueComparator = (k1, k2) -> {
			int compare = map.get(k2).compareTo(map.get(k1));
			if (compare == 0) return 1;
			else return -compare;
		};
		Map<K, V> sortedByValues = new TreeMap<>(valueComparator);
		sortedByValues.putAll(map);
		return sortedByValues;
	}

	@Override
	public void onJoin(TabPlayer p) {
		parentGroups.forEach(g -> g.onJoin(p));
		List<PlayerInfoData> list = new ArrayList<>();
		for (int slot : emptySlots) {
			list.add(new PlayerInfoData(formatSlot(slot), uuids.get(slot), null, 0, EnumGamemode.CREATIVE, new IChatBaseComponent("")));
		}
		p.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, list), this);
	}

	private int translateSlot(int slot) {
		if (direction == Direction.ROWS) {
			return (slot-1)%4*20+(slot-((slot-1)%4))/4+1;
		} else {
			return slot;
		}
	}

	@Override
	public void load() {
		TAB.getInstance().getOnlinePlayers().forEach(this::onJoin);
		TAB.getInstance().getCPUManager().startRepeatingMeasuredTask(100, "ticking layout", this, UsageType.REPEATING_TASK, () -> {
			
			List<TabPlayer> players = sortPlayers(TAB.getInstance().getOnlinePlayers());
			for (ParentGroup parent : parentGroups) {
				parent.tick(players);
			}
		});
	}

	@Override
	public void unload() {
		List<PlayerInfoData> list = new ArrayList<>();
		for (UUID id : uuids.values()) {
			list.add(new PlayerInfoData(id));
		}
		for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
			p.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, list), this);
		}
	}

	public UUID getUUID(int slot) {
		return uuids.get(slot);
	}
	
	public List<ParentGroup> getGroups(){
		return parentGroups;
	}

	public YellowNumberFix getYellowNumberFix() {
		return yellowNumberFix;
	}

	public enum Direction {

		COLUMNS, ROWS;
	}
}