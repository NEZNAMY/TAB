package me.neznamy.tab.shared.features;

import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective.EnumScoreboardHealthDisplay;

public class BelowName implements SimpleFeature{

	private static final String objectivename = "TAB-BelowName";
	private static final int DisplaySlot = 2;
	
	private int refresh;
	public static String number;
	public static String text;
	private Property textProperty;
	
	@Override
	public void load() {
		this.refresh =  Configs.config.getInt("belowname.refresh-interval-milliseconds", 200);
		textProperty = new Property(null, text);
		for (ITabPlayer p : Shared.getPlayers()){
			if (p.disabledBelowname) continue;
			PacketAPI.registerScoreboardObjective(p, objectivename, textProperty.get(), DisplaySlot, EnumScoreboardHealthDisplay.INTEGER);
		}
		Shared.cpu.startRepeatingMeasuredTask(refresh, "refreshing belowname", "Belowname", new Runnable() {
			public void run(){
				for (ITabPlayer p : Shared.getPlayers()){
					if (p.disabledBelowname) continue;
					if (p.properties.get("belowname-number").isUpdateNeeded()) {
						for (ITabPlayer all : Shared.getPlayers()) PacketAPI.setScoreboardScore(all, p.getName(), objectivename, getNumber(p));
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
	@Override
	public void unload() {
		for (ITabPlayer p : Shared.getPlayers()){
			if (p.disabledBelowname) continue;
			PacketAPI.unregisterScoreboardObjective(p, objectivename, textProperty.get(), EnumScoreboardHealthDisplay.INTEGER);
		}
	}
	@Override
	public void onJoin(ITabPlayer p) {
		if (p.disabledBelowname) return;
		PacketAPI.registerScoreboardObjective(p, objectivename, textProperty.get(), DisplaySlot, EnumScoreboardHealthDisplay.INTEGER);
		for (ITabPlayer all : Shared.getPlayers()){
			PacketAPI.setScoreboardScore(all, p.getName(), objectivename, getNumber(p));
			PacketAPI.setScoreboardScore(p, all.getName(), objectivename, getNumber(all));
		}
	}
	@Override
	public void onQuit(ITabPlayer p) {
		PacketAPI.unregisterScoreboardObjective(p, objectivename, textProperty.get(), EnumScoreboardHealthDisplay.INTEGER);
	}
	public void onWorldChange(ITabPlayer p, String from, String to) {
		if (p.disabledBelowname && !p.isDisabledWorld(Configs.disabledBelowname, from)) {
			onQuit(p);
		}
		if (!p.disabledBelowname && p.isDisabledWorld(Configs.disabledBelowname, from)) {
			onJoin(p);
		}
	}
	private int getNumber(ITabPlayer p) {
		return Shared.errorManager.parseInteger(p.properties.get("belowname-number").get(), 0, objectivename);
	}
}