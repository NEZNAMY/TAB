package me.neznamy.tab.shared.features;

import me.neznamy.tab.shared.*;

public class NameTag16 implements SimpleFeature{

	public int refresh;

	@Override
	public void load() {
		refresh = Configs.config.getInt("nametag-refresh-interval-milliseconds", 1000);
		for (ITabPlayer p : Shared.getPlayers()) p.registerTeam();
		Shared.cpu.startRepeatingMeasuredTask(refresh, "refreshing nametags", "Nametags", new Runnable() {
			public void run() {
				for (ITabPlayer p : Shared.getPlayers()) p.updateTeam();
			}
		});
		//fixing a 1.8.x client-sided vanilla bug on bukkit mode
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() == 8 || PluginHooks.viaversion || PluginHooks.protocolsupport)
			Shared.cpu.startRepeatingMeasuredTask(200, "refreshing nametag visibility", "Nametags - invisfix", new Runnable() {
				public void run() {
					for (ITabPlayer p : Shared.getPlayers()) p.setTeamVisible(!p.hasInvisibility());
				}
			});
	}
	public void unload() {
		for (ITabPlayer p : Shared.getPlayers()) p.unregisterTeam(false);
	}
	@Override
	public void onJoin(ITabPlayer p) {
		p.registerTeam();
		for (ITabPlayer all : Shared.getPlayers()) {
			if (all == p) continue; //already registered 2 lines above
			all.registerTeam(p);
		}
	}
	@Override
	public void onQuit(ITabPlayer p) {
		p.unregisterTeam(false);
	}
	@Override
	public void onWorldChange(ITabPlayer p, String from, String to) {
		if (p.disabledNametag && !p.isDisabledWorld(Configs.disabledNametag, from)) {
			p.unregisterTeam(true);
		} else if (!p.disabledNametag && p.isDisabledWorld(Configs.disabledNametag, from)) {
			p.registerTeam();
		} else {
			p.updateTeam();
		}
	}
}