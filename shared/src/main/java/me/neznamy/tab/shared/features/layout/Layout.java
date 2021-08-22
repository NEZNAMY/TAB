package me.neznamy.tab.shared.features.layout;

import java.util.ArrayList;
import java.util.Arrays;
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
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.placeholders.conditions.Condition;

public class Layout extends TabFeature {

	private Direction direction;
	private Map<Integer, FixedSlot> fixedSlots = new HashMap<>();
	private List<Integer> emptySlots = new ArrayList<>();
	private List<ParentGroup> parentGroups = new ArrayList<>();
	private Map<Integer, UUID> uuids = new HashMap<>();
	private SkinManager skinManager;
	private Map<TabPlayer, String> sortedPlayers = new TreeMap<>((p1, p2) -> p1.getTeamName().compareTo(p2.getTeamName()));
	private String remainingPlayersText;

	public Layout() {
		super("Layout");
		direction = parseDirection(TAB.getInstance().getConfiguration().getLayout().getString("direction", "COLUMNS"));
		String defaultSkin = TAB.getInstance().getConfiguration().getLayout().getString("default-skin", "mineskin:1753261242");
		remainingPlayersText = TAB.getInstance().getConfiguration().getLayout().getString("remaining-players-text", "... and %s more");
		skinManager = new SkinManager(defaultSkin);
		for (int slot=1; slot<=80; slot++) {
			emptySlots.add(slot);
			uuids.put(slot, UUID.randomUUID());
		}
		for (String fixedSlot : TAB.getInstance().getConfiguration().getLayout().getStringList("fixed-slots")) {
			String[] array = fixedSlot.split("\\|");
			int slot = Integer.parseInt(array[0]);
			String text = array[1];
			String skin = array.length > 2 ? array[2] : defaultSkin;
			FixedSlot f = new FixedSlot(this, slot, text, skin);
			fixedSlots.put(slot, f);
			emptySlots.remove((Integer)slot);
			if (text.length() > 0) TAB.getInstance().getFeatureManager().registerFeature("layout-slot-" + slot, f);
		}
		loadGroups();
		TAB.getInstance().debug("Loaded Layout feature");
		TAB.getInstance().getFeatureManager().registerFeature("latency", new LatencyRefresher(this));
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

	@Override
	public void onJoin(TabPlayer p) {
		sortedPlayers.put(p, p.getTeamName());
		parentGroups.forEach(g -> g.onJoin(p));
		List<PlayerInfoData> list = new ArrayList<>();
		for (int slot : emptySlots) {
			list.add(new PlayerInfoData(formatSlot(slot), uuids.get(slot), skinManager.getDefaultSkin(), 0, EnumGamemode.CREATIVE, new IChatBaseComponent("")));
		}
		p.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, list), this);
		tick();
	}
	
	@Override
	public void onQuit(TabPlayer p) {
		sortedPlayers.remove(p);
		tick();
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
		tick();
	}

	private void tick() {
		List<TabPlayer> players = new ArrayList<>(sortedPlayers.keySet());
		for (ParentGroup parent : parentGroups) {
			parent.tick(players);
		}
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

	public PlayerSlot getSlot(TabPlayer p) {
		for (ParentGroup group : parentGroups) {
			if (group.getPlayers().containsKey(p)) {
				return group.getPlayers().get(p);
			}
		}
		return null;
	}

	public SkinManager getSkinManager() {
		return skinManager;
	}
	
	public void updateTeamName(TabPlayer p, String teamName) {
		sortedPlayers.remove(p);
		((ITabPlayer) p).setTeamName(teamName);
		sortedPlayers.put(p, teamName);
		tick();
	}

	public String getRemainingPlayersText() {
		return remainingPlayersText;
	}

	public enum Direction {

		COLUMNS, ROWS;
	}
}