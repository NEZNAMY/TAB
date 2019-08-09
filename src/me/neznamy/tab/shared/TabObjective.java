package me.neznamy.tab.shared;

import me.neznamy.tab.shared.Shared.Feature;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective.EnumScoreboardHealthDisplay;

public class TabObjective{

	public static TabObjectiveType type;
	public static String customValue;
	
	public static void load() {
		if (type == TabObjectiveType.NONE) return;
		for (ITabPlayer p : Shared.getPlayers()){
			if (p.disabledTablistObjective) continue;
			if (type == TabObjectiveType.HEARTS) {
				PacketAPI.registerScoreboardObjective(p, "TabObjective", "ms", 0, EnumScoreboardHealthDisplay.HEARTS);
			} else {
				PacketAPI.registerScoreboardObjective(p, "TabObjective", "ms", 0, EnumScoreboardHealthDisplay.INTEGER);
			}
		}
		int refresh = 500;
		if (type == TabObjectiveType.PING) refresh = 2050;
		if (type == TabObjectiveType.HEARTS) refresh = 100;
		Shared.scheduleRepeatingTask(refresh, "refreshing tablist objective", Feature.TABLISTOBJECTIVE, new Runnable() {
	        public void run(){
	        	for (ITabPlayer p : Shared.getPlayers()){
	        		if (p.disabledTablistObjective) continue;
	        		final int value;
	        		if (type == TabObjectiveType.PING) 			value = (int) p.getPing();
	        		else if (type == TabObjectiveType.HEARTS) 	value = p.getHealth();
	        		else if (type == TabObjectiveType.CUSTOM)	value = getCustomValue(p);
	        		else value = -1;
	        		if (p.getLastTabObjectiveValue() != value) {
	        			p.setLastTabObjectiveValue(value);
	        			for (ITabPlayer all : Shared.getPlayers()) PacketAPI.changeScoreboardScore(all, p.getName(), "TabObjective", value);
	        		}
				}
	        }
		});
	}
	public static void playerJoin(ITabPlayer p) {
		if (type == TabObjectiveType.NONE || p.disabledTablistObjective) return;
		if (type == TabObjectiveType.HEARTS){
			PacketAPI.registerScoreboardObjective(p, "TabObjective", "ms", 0, EnumScoreboardHealthDisplay.HEARTS);
		} else {
			PacketAPI.registerScoreboardObjective(p, "TabObjective", "ms", 0, EnumScoreboardHealthDisplay.INTEGER);
		}
		if (type == TabObjectiveType.PING) {
			for (ITabPlayer all : Shared.getPlayers()){
				PacketAPI.changeScoreboardScore(all, p.getName(), "TabObjective", (int) p.getPing());
				PacketAPI.changeScoreboardScore(p, all.getName(), "TabObjective", (int) all.getPing());
			}
		} 
		if (type == TabObjectiveType.HEARTS){
			for (ITabPlayer all : Shared.getPlayers()){
				PacketAPI.changeScoreboardScore(all, p.getName(), "TabObjective", p.getHealth());
				PacketAPI.changeScoreboardScore(p, all.getName(), "TabObjective", all.getHealth());
			}
		}
		if (type == TabObjectiveType.CUSTOM) {
			for (ITabPlayer all : Shared.getPlayers()){
				PacketAPI.changeScoreboardScore(all, p.getName(), "TabObjective", getCustomValue(p));
				PacketAPI.changeScoreboardScore(p, all.getName(), "TabObjective", getCustomValue(all));
			}
		} 
	}
	public static void unload() {
		if (type == TabObjectiveType.NONE) return;
		if (type == TabObjectiveType.HEARTS) {
			for (ITabPlayer p : Shared.getPlayers()){
				if (p.disabledTablistObjective) continue;
				PacketAPI.unregisterScoreboardObjective(p, "TabObjective", "ms", EnumScoreboardHealthDisplay.HEARTS);
			}
		} else {
			for (ITabPlayer p : Shared.getPlayers()){
				if (p.disabledTablistObjective) continue;
				PacketAPI.unregisterScoreboardObjective(p, "TabObjective", "ms", EnumScoreboardHealthDisplay.INTEGER);
			}
		}
	}
	public static void unload(ITabPlayer p) {
		if (type == TabObjectiveType.NONE) return;
		if (type == TabObjectiveType.HEARTS) {
			PacketAPI.unregisterScoreboardObjective(p, "TabObjective", "ms", EnumScoreboardHealthDisplay.HEARTS);
		} else {
			PacketAPI.unregisterScoreboardObjective(p, "TabObjective", "ms", EnumScoreboardHealthDisplay.INTEGER);
		}
	}
	public static int getCustomValue(ITabPlayer p) {
		String replaced = Placeholders.replace(customValue, p);
		try {
			return Integer.parseInt(replaced);
		} catch (Exception e) {
			Shared.error("Value \"" + replaced + "\" is not a valid number for tablist objective! Did you forget to download an expansion ?");
			return 0;
		}
	}
	public enum TabObjectiveType{
		PING, HEARTS, CUSTOM, NONE;
	}
}