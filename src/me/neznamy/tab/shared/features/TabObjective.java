package me.neznamy.tab.shared.features;

import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective.EnumScoreboardHealthDisplay;

public class TabObjective implements SimpleFeature{

	private static final String objectivename = "TAB-TabObjective";
	private static final int DisplaySlot = 0;
	
	public TabObjectiveType type;
	public static String rawValue;
	private final String title = "ms";

	public TabObjective(TabObjectiveType type) {
		this.type = type;
	}
	@Override
	public void load() {
		for (ITabPlayer p : Shared.getPlayers()){
			if (p.disabledTablistObjective) continue;
			PacketAPI.registerScoreboardObjective(p, objectivename, title, DisplaySlot, type.getDisplay());
			for (ITabPlayer all : Shared.getPlayers()) PacketAPI.setScoreboardScore(all, p.getName(), objectivename, getValue(p));
		}
		Shared.cpu.startRepeatingMeasuredTask(type.getRefresh(), "refreshing tablist objective", "Tablist Objective", new Runnable() {
			public void run(){
				for (ITabPlayer p : Shared.getPlayers()){
					if (p.disabledTablistObjective) continue;
					if (p.properties.get("tablist-objective").isUpdateNeeded()) {
						for (ITabPlayer all : Shared.getPlayers()) PacketAPI.setScoreboardScore(all, p.getName(), objectivename, getValue(p));
					}
				}
			}
		});
	}
	@Override
	public void unload() {
		for (ITabPlayer p : Shared.getPlayers()){
			if (p.disabledTablistObjective) continue;
			PacketAPI.unregisterScoreboardObjective(p, objectivename);
		}
	}
	@Override
	public void onJoin(ITabPlayer connectedPlayer) {
		if (connectedPlayer.disabledTablistObjective) return;
		PacketAPI.registerScoreboardObjective(connectedPlayer, objectivename, title, DisplaySlot, type.getDisplay());
		for (ITabPlayer all : Shared.getPlayers()){
			PacketAPI.setScoreboardScore(all, connectedPlayer.getName(), objectivename, getValue(connectedPlayer));
			PacketAPI.setScoreboardScore(connectedPlayer, all.getName(), objectivename, getValue(all));
		}
	}
	@Override
	public void onQuit(ITabPlayer disconnectedPlayer) {
	}
	@Override
	public void onWorldChange(ITabPlayer p, String from, String to) {
		if (p.disabledTablistObjective && !p.isDisabledWorld(Configs.disabledTablistObjective, from)) {
			PacketAPI.unregisterScoreboardObjective(p, objectivename);
		}
		if (!p.disabledTablistObjective && p.isDisabledWorld(Configs.disabledTablistObjective, from)) {
			onJoin(p);
		}
	}
	public int getValue(ITabPlayer p) {
		return Shared.errorManager.parseInteger(p.properties.get("tablist-objective").get(), 0, "Tablist Objective");
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