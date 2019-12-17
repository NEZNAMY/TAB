package me.neznamy.tab.shared;

import me.neznamy.tab.shared.Shared.Feature;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective.EnumScoreboardHealthDisplay;

public class BelowName{

	public static boolean enable;
	public static int refresh;
	public static String number;
	public static String text;
	private static Property textProperty;
	private static final String objectivename = "BelowName";

	public static void load() {
		if (!enable) return;
		textProperty = new Property(null, text);
		for (ITabPlayer p : Shared.getPlayers()){
			if (p.disabledBelowname) continue;
			PacketAPI.registerScoreboardObjective(p, objectivename, textProperty.get(), 2, EnumScoreboardHealthDisplay.INTEGER);
		}
		Shared.scheduleRepeatingTask(refresh, "refreshing belowname", Feature.BELOWNAME, new Runnable() {
			public void run(){
				for (ITabPlayer p : Shared.getPlayers()){
					if (p.disabledBelowname) continue;
					if (p.properties.get("belowname-number").isUpdateNeeded()) {
						for (ITabPlayer all : Shared.getPlayers()) PacketAPI.setScoreboardScore(all, p.getName(), "BelowName", getNumber(p));
					}
				}
				if (textProperty.isUpdateNeeded()) {
					for (ITabPlayer all : Shared.getPlayers()) {
						PacketAPI.changeScoreboardObjectiveTitle(all, objectivename, textProperty.get(), EnumScoreboardHealthDisplay.INTEGER);
					}
				}
			}
		});
	}
	public static void playerJoin(ITabPlayer p) {
		if (!enable || p.disabledBelowname) return;
		PacketAPI.registerScoreboardObjective(p, objectivename, textProperty.get(), 2, EnumScoreboardHealthDisplay.INTEGER);
		for (ITabPlayer all : Shared.getPlayers()){
			PacketAPI.setScoreboardScore(all, p.getName(), objectivename, getNumber(p));
			PacketAPI.setScoreboardScore(p, all.getName(), objectivename, getNumber(all));
		}
	}
	public static void unload() {
		if (!enable) return;
		for (ITabPlayer p : Shared.getPlayers()){
			if (p.disabledBelowname) continue;
			PacketAPI.unregisterScoreboardObjective(p, objectivename, textProperty.get(), EnumScoreboardHealthDisplay.INTEGER);
		}
	}
	public static void unload(ITabPlayer p) {
		if (enable) PacketAPI.unregisterScoreboardObjective(p, objectivename, textProperty.get(), EnumScoreboardHealthDisplay.INTEGER);
	}
	public static int getNumber(ITabPlayer p) {
		return Shared.parseInteger(p.properties.get("belowname-number").get(), 0, "belowname");
	}
}