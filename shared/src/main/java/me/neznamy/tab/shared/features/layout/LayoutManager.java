package me.neznamy.tab.shared.features.layout;

import java.util.*;
import java.util.Map.Entry;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.layout.skin.SkinManager;
import me.neznamy.tab.shared.placeholders.conditions.Condition;

public class LayoutManager extends TabFeature {

	private final Direction direction = parseDirection(TAB.getInstance().getConfiguration().getLayout().getString("direction", "COLUMNS"));
	private final String defaultSkin = TAB.getInstance().getConfiguration().getLayout().getString("default-skin", "mineskin:1753261242");
	private final boolean enableRemainingPlayersText = TAB.getInstance().getConfiguration().getLayout().getBoolean("enable-remaining-players-text", true);
	private final String remainingPlayersText = EnumChatFormat.color(TAB.getInstance().getConfiguration().getLayout().getString("remaining-players-text", "... and %s more"));
	private final SkinManager skinManager = new SkinManager(defaultSkin);
	
	private final Map<String, Layout> layouts = new LinkedHashMap<>();
	private final WeakHashMap<TabPlayer, Layout> playerViews = new WeakHashMap<>();
	private final Map<Integer, UUID> uuids = new HashMap<>();
	private final Map<TabPlayer, String> sortedPlayers = Collections.synchronizedMap(new TreeMap<>(Comparator.comparing(TabPlayer::getTeamName)));

	public LayoutManager() {
		super("Layout", "Switching layouts");
		for (int slot=1; slot<=80; slot++) {
			uuids.put(slot, new UUID(0, translateSlot(slot)));
		}
		loadLayouts();
		TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.LAYOUT_VANISH, new VanishListener(this));
		TAB.getInstance().debug("Loaded Layout feature");
	}

	private Direction parseDirection(String value) {
		try {
			return Direction.valueOf(value);
		} catch (IllegalArgumentException e) {
			TAB.getInstance().getErrorManager().startupWarn("\"&e" + value + "&c\" is not a valid type of layout direction. Valid options are: &e" + Arrays.deepToString(Direction.values()) + ". &bUsing COLUMNS");
			return Direction.COLUMNS;
		}
	}

	@SuppressWarnings("unchecked")
	private void loadLayouts() {
		for (Entry<Object, Object> layout : TAB.getInstance().getConfiguration().getLayout().getConfigurationSection("layouts").entrySet()) {
			Map<String, Object> map = (Map<String, Object>) layout.getValue();
			Condition displayCondition = Condition.getCondition((String) map.get("condition"));
			if (displayCondition != null) addUsedPlaceholders(Collections.singletonList("%condition:" + displayCondition.getName() + "%"));
			Map<Integer, FixedSlot> fixedSlots = new HashMap<>();
			List<Integer> emptySlots = new ArrayList<>();
			List<ParentGroup> parentGroups = new ArrayList<>();
			Layout l = new Layout(layout.getKey().toString(), this, displayCondition, fixedSlots, emptySlots, parentGroups);
			for (int slot=1; slot<=80; slot++) {
				emptySlots.add(slot);
			}
			for (String fixedSlot : (List<String>)map.getOrDefault("fixed-slots", Collections.emptyList())) {
				String[] array = fixedSlot.split("\\|");
				int slot = Integer.parseInt(array[0]);
				String text = array[1];
				String skin = array.length > 2 ? array[2] : defaultSkin;
				FixedSlot f = new FixedSlot(l, slot, text, skin);
				fixedSlots.put(slot, f);
				emptySlots.remove((Integer)slot);
				if (text.length() > 0) TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.layoutSlot(layout.getKey().toString(), slot), f);
			}
			for (Entry<String, Map<String, Object>> group : ((Map<String, Map<String, Object>>) map.get("groups")).entrySet()){
				Condition condition = Condition.getCondition((String) group.getValue().get("condition"));
				List<Integer> positions = new ArrayList<>();
				for (String line : (List<String>) group.getValue().get("slots")) {
					String[] arr = line.split("-");
					int from = Integer.parseInt(arr[0]);
					int to = Integer.parseInt(arr[1]);
					for (int i = from; i<= to; i++) {
						positions.add(i);
					}
				}
				parentGroups.add(new ParentGroup(l, condition, positions.stream().mapToInt(i->i).toArray()));
				emptySlots.removeAll(positions);
			}
			layouts.put(layout.getKey().toString(), l);
			TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.layout(layout.getKey().toString()), l);
		}
	}

	@Override
	public void onJoin(TabPlayer p) {
		sortedPlayers.put(p, p.getTeamName());
		Layout highest = getHighestLayout(p);
		if (highest != null) highest.sendTo(p);
		playerViews.put(p, highest);
		layouts.values().forEach(Layout::tick);
	}

	@Override
	public void onQuit(TabPlayer p) {
		sortedPlayers.remove(p);
		layouts.values().forEach(Layout::tick);
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
		for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
			onJoin(p);
		}
	}

	@Override
	public void refresh(TabPlayer p, boolean force) {
		Layout highest = getHighestLayout(p);
		Layout current = playerViews.get(p);
		if (current != highest) {
			if (current != null) current.removeFrom(p);
			if (highest != null) highest.sendTo(p);
			playerViews.put(p, highest);
		}
	}

	@Override
	public void unload() {
		List<PlayerInfoData> list = new ArrayList<>();
		for (UUID id : uuids.values()) {
			list.add(new PlayerInfoData(id));
		}
		for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
			if (p.getVersion().getMinorVersion() < 8 || p.isBedrockPlayer()) continue;
			p.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, list), this);
		}
	}

	private Layout getHighestLayout(TabPlayer p) {
		for (Layout layout : layouts.values()) {
			if (layout.isConditionMet(p)) return layout;
		}
		return null;
	}

	public UUID getUUID(int slot) {
		return uuids.get(slot);
	}

	public SkinManager getSkinManager() {
		return skinManager;
	}

	public void updateTeamName(TabPlayer p, String teamName) {
		sortedPlayers.remove(p);
		((ITabPlayer) p).setTeamName(teamName);
		sortedPlayers.put(p, teamName);
		layouts.values().forEach(Layout::tick);
	}

	public boolean isRemainingPlayersTextEnabled() {
		return enableRemainingPlayersText;
	}

	public String getRemainingPlayersText() {
		return remainingPlayersText;
	}

	public Map<TabPlayer, String> getSortedPlayers() {
		return sortedPlayers;
	}

	public Map<Integer, UUID> getUuids() {
		return uuids;
	}

	public Map<TabPlayer, Layout> getPlayerViews() {
		return playerViews;
	}

	public Map<String, Layout> getLayouts() {
		return layouts;
	}

	public enum Direction {

		COLUMNS, ROWS
	}
}