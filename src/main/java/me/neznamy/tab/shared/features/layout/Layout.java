package me.neznamy.tab.shared.features.layout;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.premium.conditions.Condition;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.ConfigurationFile;
import me.neznamy.tab.shared.config.YamlConfigurationFile;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.interfaces.JoinEventListener;
import me.neznamy.tab.shared.features.interfaces.Loadable;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumGamemode;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.PlayerInfoData;

public class Layout implements Loadable, JoinEventListener {

	private LayoutDirection direction;
	private Map<Integer, FixedSlot> fixedSlots = new HashMap<Integer, FixedSlot>();
	private List<ParentGroup> parentGroups = new ArrayList<ParentGroup>();
	private Map<String, ChildGroup> childGroups = new HashMap<String, ChildGroup>();
	private Map<Integer, UUID> uuids = new HashMap<Integer, UUID>();
	
	public Layout() {
		try {
			new File(Shared.platform.getDataFolder() + File.separator + "layout").mkdirs();
			ConfigurationFile file = new YamlConfigurationFile(Shared.platform.getDataFolder(), "layout/default.yml", "layout" + File.separator + "default.yml", null);
			String type = file.getString("direction", "COLUMNS");
			try {
				direction = LayoutDirection.valueOf(type);
			} catch (Throwable e) {
				Shared.errorManager.startupWarn("\"&e" + type + "&c\" is not a valid type of layout direction. Valid options are: &e" + Arrays.deepToString(LayoutDirection.values()) + ". &bUsing COLUMNS");
				direction = LayoutDirection.COLUMNS;
			}
			for (int i=1; i<=80; i++) {
				fixedSlots.put(i, new FixedSlot(i, "", null));
			}
			for (int i=1; i<=80; i++) {
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
			for (Object obj : file.getConfigurationSection("child-groups").keySet()){
				String title = file.getString("child-groups." + obj + ".title");
				Condition condition = Premium.conditions.get(file.getString("child-groups." + obj + ".condition"));
				childGroups.put(obj.toString(), new ChildGroup(title, condition));
			}
			for (Object obj : file.getConfigurationSection("parent-groups").keySet()){
				Condition condition = Premium.conditions.get(file.getString("parent-groups." + obj + ".condition"));
				List<Integer> positions = new ArrayList<Integer>();
				for (String line : file.getStringList("parent-groups." + obj + ".slots")) {
					String[] arr = line.split("-");
					int from = Integer.parseInt(arr[0]);
					int to = Integer.parseInt(arr[1]);
					for (int i = from; i<= to; i++) {
						positions.add(i);
					}
				}
				List<ChildGroup> childs = new ArrayList<ChildGroup>();
				if (file.hasConfigOption("parent-groups." + obj + ".child-groups")) {
					for (String child : file.getStringList("parent-groups." + obj + ".child-groups")) {
						if (childGroups.containsKey(child)) {
							childs.add(childGroups.get(child));
						}
					}
				}
				parentGroups.add(new ParentGroup(condition, positions, childs));
			}
			Shared.cpu.startRepeatingMeasuredTask(500, "crashing server", TabFeature.TABLIST_LAYOUT, UsageType.REPEATING_TASK, new Runnable() {

				@Override
				public void run() {
					for (TabPlayer p : Shared.getPlayers()) {
						List<PlayerInfoData> list = new ArrayList<PlayerInfoData>();
						for (Entry<Integer, IChatBaseComponent> entry : doTick(p).entrySet()) {
							int slot = translateSlot(entry.getKey());
							list.add(new PlayerInfoData((char)1 + "SLOT-" + (slot < 10 ? "0" + slot : slot + ""), uuids.get(slot), null, 0, EnumGamemode.CREATIVE, entry.getValue()));
						}
						p.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, list));
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private Map<Integer, IChatBaseComponent> doTick(TabPlayer viewer) {
		Map<Integer, IChatBaseComponent> result = new HashMap<Integer, IChatBaseComponent>();
		
		for (int i=1; i<=80; i++) {
			result.put(i, new IChatBaseComponent(""));
		}
		
		for (FixedSlot fixed : fixedSlots.values()) {
			result.put(fixed.getSlot(), IChatBaseComponent.optimizedComponent(fixed.getText(viewer)));
		}
		
		List<TabPlayer> players = new ArrayList<TabPlayer>(Shared.getPlayers());
		for (ParentGroup parent : parentGroups) {
			List<TabPlayer> meetingCondition = new ArrayList<TabPlayer>();
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
	
	@Override
	public TabFeature getFeatureType() {
		return TabFeature.OTHER;
	}

	@Override
	public void onJoin(TabPlayer connectedPlayer) {
		List<PlayerInfoData> list = new ArrayList<PlayerInfoData>();
		for (FixedSlot s : fixedSlots.values()) {
			s.onJoin(connectedPlayer);
		}
		for (Entry<Integer, IChatBaseComponent> entry : doTick(connectedPlayer).entrySet()) {
			int slot = translateSlot(entry.getKey());
			list.add(new PlayerInfoData((char)1 + "SLOT-" + (slot < 10 ? "0" + slot : slot + ""), uuids.get(slot), null, 0, EnumGamemode.CREATIVE, entry.getValue()));
		}
		connectedPlayer.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, list));
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
		for (TabPlayer p : Shared.getPlayers()) {
			List<PlayerInfoData> list = new ArrayList<PlayerInfoData>();
			for (FixedSlot s : fixedSlots.values()) {
				s.onJoin(p);
			}
			for (Entry<Integer, IChatBaseComponent> entry : doTick(p).entrySet()) {
				int slot = translateSlot(entry.getKey());
				list.add(new PlayerInfoData((char)1 + "SLOT-" + (slot < 10 ? "0" + slot : slot + ""), uuids.get(slot), null, 0, EnumGamemode.CREATIVE, entry.getValue()));
			}
			p.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, list));
		}
	}

	@Override
	public void unload() {
		for (TabPlayer p : Shared.getPlayers()) {
			List<PlayerInfoData> list = new ArrayList<PlayerInfoData>();
			for (Entry<Integer, IChatBaseComponent> entry : doTick(p).entrySet()) {
				int slot = translateSlot(entry.getKey());
				list.add(new PlayerInfoData(uuids.get(slot)));
			}
			p.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, list));
		}
	}
}