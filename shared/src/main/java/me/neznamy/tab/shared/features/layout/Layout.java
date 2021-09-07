package me.neznamy.tab.shared.features.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumGamemode;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.placeholders.conditions.Condition;

public class Layout extends TabFeature {

	private String name;
	private LayoutManager manager;
	private Condition displayCondition;
	private Map<Integer, FixedSlot> fixedSlots = new HashMap<>();
	private List<Integer> emptySlots = new ArrayList<>();
	private List<ParentGroup> groups = new ArrayList<>();
	private Set<TabPlayer> viewers = new HashSet<>();
	private TabPlayer[] viewerArray = new TabPlayer[0];

	public Layout(String name, LayoutManager manager, Condition displayCondition, Map<Integer, FixedSlot> fixedSlots, List<Integer> emptySlots, List<ParentGroup> groups) {
		super(manager.getFeatureName());
		this.name = name;
		this.manager = manager;
		this.displayCondition = displayCondition;
		this.fixedSlots = fixedSlots;
		this.emptySlots = emptySlots;
		this.groups = groups;
		TAB.getInstance().getFeatureManager().registerFeature("latency-" + name, new LatencyRefresher(this));
	}

	public void sendTo(TabPlayer p) {
		viewers.add(p);
		viewerArray = viewers.toArray(new TabPlayer[0]);
		groups.forEach(g -> g.sendTo(p));
		fixedSlots.values().forEach(s -> s.sendTo(p));
		List<PlayerInfoData> list = new ArrayList<>();
		for (int slot : emptySlots) {
			list.add(new PlayerInfoData(manager.formatSlot(slot), manager.getUUID(slot), manager.getSkinManager().getDefaultSkin(), 0, EnumGamemode.CREATIVE, new IChatBaseComponent("")));
		}
		p.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, list), this);
	}

	public void removeFrom(TabPlayer p) {
		viewers.remove(p);
		viewerArray = viewers.toArray(new TabPlayer[0]);
		List<PlayerInfoData> list = new ArrayList<>();
		for (UUID id : manager.getUuids().values()) {
			list.add(new PlayerInfoData(id));
		}
		p.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, list), this);
	}

	public boolean isConditionMet(TabPlayer p) {
		return displayCondition == null || displayCondition.isMet(p);
	}

	public List<ParentGroup> getGroups(){
		return groups;
	}

	public void tick() {
		List<TabPlayer> players = new ArrayList<>(manager.getSortedPlayers().keySet());
		for (ParentGroup group : groups) {
			group.tick(players);
		}
	}

	@Override
	public void refresh(TabPlayer p, boolean force) {
		tick();
	}
	
	@Override
	public void onQuit(TabPlayer p) {
		if (viewers.remove(p)) {
			viewerArray = viewers.toArray(new TabPlayer[0]);
		}
	}

	public TabPlayer[] getViewers() {
		return viewerArray;
	}
	
	public boolean containsViewer(TabPlayer viewer) {
		return viewers.contains(viewer);
	}

	public LayoutManager getManager() {
		return manager;
	}

	public PlayerSlot getSlot(TabPlayer p) {
		for (ParentGroup group : groups) {
			if (group.getPlayers().containsKey(p)) {
				return group.getPlayers().get(p);
			}
		}
		return null;
	}
	
	public String getName() {
		return name;
	}
}