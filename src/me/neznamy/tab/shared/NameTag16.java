package me.neznamy.tab.shared;

public class NameTag16 {

	public static boolean enable;
	public static int refresh;
	
	public static void unload() {
		if (enable) for (ITabPlayer p : Shared.getPlayers()) p.unregisterTeam(false);
	}
	public static void load() {
		if (!enable) return;
		for (ITabPlayer p : Shared.getPlayers()) p.registerTeam();
		Shared.scheduleRepeatingTask(refresh, "refreshing nametags", "Nametags", new Runnable() {
			public void run() {
				for (ITabPlayer p : Shared.getPlayers()) p.updateTeam();
			}
		});
		//fixing a 1.8.x client-sided vanilla bug on bukkit mode
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() == 8 || PluginHooks.viaversion || PluginHooks.protocolsupport)
			Shared.scheduleRepeatingTask(200, "refreshing nametag visibility", "Nametags - invisfix", new Runnable() {
				public void run() {
					for (ITabPlayer p : Shared.getPlayers()) p.setTeamVisible(!p.hasInvisibility());
				}
			});
	}
	public static void playerJoin(ITabPlayer p) {
		if (!enable) return;
		p.registerTeam();
		for (ITabPlayer all : Shared.getPlayers()) {
			if (all == p) continue; //already registered 2 lines above
			all.registerTeam(p);
		}
	}
	public static void playerQuit(ITabPlayer p) {
		if (enable) p.unregisterTeam(false);
	}
}