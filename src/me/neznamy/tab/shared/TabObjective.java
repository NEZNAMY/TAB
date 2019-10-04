package me.neznamy.tab.shared;

import me.neznamy.tab.shared.Shared.Feature;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective.EnumScoreboardHealthDisplay;

public class TabObjective{

	public static TabObjectiveType type;
	public static String rawValue;
	private static final String objectivename = "TabObjective";
	private static final String title = "ms";

	public static void load() {
		if (type == TabObjectiveType.NONE) return;
		for (ITabPlayer p : Shared.getPlayers()){
			if (p.disabledTablistObjective) continue;
			PacketAPI.registerScoreboardObjective(p, objectivename, title, 0, type.getDisplay());
		}
		Shared.scheduleRepeatingTask(type.getRefresh(), "refreshing tablist objective", Feature.TABLISTOBJECTIVE, new Runnable() {
			public void run(){
				for (ITabPlayer p : Shared.getPlayers()){
					if (p.disabledTablistObjective) continue;
					if (p.properties.get("tablist-objective").isUpdateNeeded()) {
						for (ITabPlayer all : Shared.getPlayers()) PacketAPI.setScoreboardScore(all, p.getName(), "TabObjective", getValue(p));
					}
				}
			}
		});
	}
	public static void playerJoin(ITabPlayer p) {
		if (type == TabObjectiveType.NONE || p.disabledTablistObjective) return;
		PacketAPI.registerScoreboardObjective(p, objectivename, title, 0, type.getDisplay());
		for (ITabPlayer all : Shared.getPlayers()){
			PacketAPI.setScoreboardScore(all, p.getName(), objectivename, getValue(p));
			PacketAPI.setScoreboardScore(p, all.getName(), objectivename, getValue(all));
		}
	}
	public static void unload() {
		if (type == TabObjectiveType.NONE) return;
		for (ITabPlayer p : Shared.getPlayers()){
			if (p.disabledTablistObjective) continue;
			PacketAPI.unregisterScoreboardObjective(p, objectivename, title, type.getDisplay());
		}
	}
	public static void unload(ITabPlayer p) {
		if (type != TabObjectiveType.NONE) PacketAPI.unregisterScoreboardObjective(p, objectivename, title, type.getDisplay());
	}
	public static int getValue(ITabPlayer p) {
		String replaced = p.properties.get("tablist-objective").get();
		try {
			return Integer.parseInt(replaced);
		} catch (Throwable e) {
			return Shared.error(0, "Value \"" + replaced + "\" is not a valid number for tablist objective! Did you forget to download an expansion ?");
		}
	}
	public enum TabObjectiveType{

		PING(1000, EnumScoreboardHealthDisplay.INTEGER), 
		HEARTS(500, EnumScoreboardHealthDisplay.HEARTS), 
		CUSTOM(500, EnumScoreboardHealthDisplay.INTEGER), 
		NONE(0, null);

		private int refresh;
		private EnumScoreboardHealthDisplay display;

		TabObjectiveType(int refresh, EnumScoreboardHealthDisplay display){
			this.refresh = refresh;
			this.display = display;
		}
		public EnumScoreboardHealthDisplay getDisplay() {
			return display;
		}
		public int getRefresh() {
			return refresh;
		}
	}
}