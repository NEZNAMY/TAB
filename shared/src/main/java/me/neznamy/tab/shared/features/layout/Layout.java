package me.neznamy.tab.shared.features.layout;

import java.util.*;
import java.util.stream.Collectors;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumGamemode;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.placeholders.conditions.Condition;

public class Layout extends TabFeature {

	private final String name;
	private final LayoutManager manager;
	private final Condition displayCondition;
	private final Map<Integer, FixedSlot> fixedSlots;
	private final List<Integer> emptySlots;
	private final List<ParentGroup> groups;
	private final Set<TabPlayer> viewers = Collections.newSetFromMap(new WeakHashMap<>());

	public Layout(String name, LayoutManager manager, Condition displayCondition, Map<Integer, FixedSlot> fixedSlots, List<Integer> emptySlots, List<ParentGroup> groups) {
		super(manager.getFeatureName(), "Updating player groups");
		this.name = name;
		this.manager = manager;
		this.displayCondition = displayCondition;
		this.fixedSlots = fixedSlots;
		this.emptySlots = emptySlots;
		this.groups = groups;
	}

	public void sendTo(TabPlayer p) {
		if (viewers.contains(p)) return;
		viewers.add(p);
		List<PlayerInfoData> list = new ArrayList<>();
		groups.forEach(g -> g.sendTo(p));
		for (FixedSlot slot : fixedSlots.values()) {
			p.setProperty(slot, slot.getPropertyName(), slot.getText());
			list.add(new PlayerInfoData("", slot.getId(), slot.getSkin(), 0, EnumGamemode.CREATIVE, IChatBaseComponent.optimizedComponent(p.getProperty(slot.getPropertyName()).get())));
		}
		for (int slot : emptySlots) {
			list.add(new PlayerInfoData("", manager.getUUID(slot), manager.getSkinManager().getDefaultSkin(), 0, EnumGamemode.CREATIVE, new IChatBaseComponent("")));
		}
		if (p.getVersion().getMinorVersion() < 8 || p.isBedrockPlayer()) return;
		p.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, list), this);
	}

	public void removeFrom(TabPlayer p) {
		if (!viewers.contains(p)) return;
		viewers.remove(p);
		List<PlayerInfoData> list = new ArrayList<>();
		for (UUID id : manager.getUuids().values()) {
			list.add(new PlayerInfoData(id));
		}
		if (p.getVersion().getMinorVersion() < 8 || p.isBedrockPlayer()) return;
		p.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, list), this);
	}

	public boolean isConditionMet(TabPlayer p) {
		return displayCondition == null || displayCondition.isMet(p);
	}

	public List<ParentGroup> getGroups(){
		return groups;
	}

	public void tick() {
		List<TabPlayer> players = manager.getSortedPlayers().keySet().stream().filter(player -> !player.isVanished()).collect(Collectors.toList());
		for (ParentGroup group : groups) {
			group.tick(players);
		}
	}

	@Override
	public void refresh(TabPlayer p, boolean force) {
		tick();
	}

	public Set<TabPlayer> getViewers() {
		return viewers;
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

	@Override
	public void onServerChange(TabPlayer player, String from, String to) {
		if (TAB.getInstance().getFeatureManager().isFeatureEnabled(TabConstants.Feature.PIPELINE_INJECTION)) return;
		//velocity clearing TabList on server switch
		if (viewers.remove(player)){
			sendTo(player);
		}
	}
}