package me.neznamy.tab.shared;

import me.neznamy.tab.shared.Shared.Feature;

public class NameTag16 {

	public static boolean enable;
	public static int refresh;
	
	public static void unload() {
		if (enable) for (ITabPlayer p : Shared.getPlayers()) p.unregisterTeam();
	}
	public static void load() {
		if (!enable) return;
		for (ITabPlayer p : Shared.getPlayers()) p.registerTeam();
		Shared.scheduleRepeatingTask(refresh, "refreshing nametags", Feature.NAMETAG, new Runnable() {
			public void run() {
				for (ITabPlayer p : Shared.getPlayers()) p.updateTeam();
			}
		});
		//fixing a 1.8.x client-sided vanilla bug
		Shared.scheduleRepeatingTask(200, "refreshing nametag visibility", Feature.NAMETAG, new Runnable() {
			public void run() {
				for (ITabPlayer p : Shared.getPlayers()) p.setTeamVisible(!p.hasInvisibility());
			}
		});
	}
	public static void playerJoin(ITabPlayer p) {
		if (!enable) return;
		p.registerTeam();
		for (ITabPlayer all : Shared.getPlayers()) all.registerTeam(p);
	}
	public static void playerQuit(ITabPlayer p) {
		if (enable) p.unregisterTeam();
	}
}