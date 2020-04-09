package me.neznamy.tab.shared.features;

import me.neznamy.tab.shared.*;

public class NameTag16 implements SimpleFeature{

	public int refresh;

	@Override
	public void load() {
		refresh = Configs.config.getInt("nametag-refresh-interval-milliseconds", 1000);
		if (refresh < 50) Shared.errorManager.refreshTooLow("NameTags", refresh);
		for (ITabPlayer p : Shared.getPlayers()) {
			if (!p.disabledNametag) p.registerTeam();
		}
		Shared.cpu.startRepeatingMeasuredTask(refresh, "refreshing nametags", "NameTags", new Runnable() {
			public void run() {
				for (ITabPlayer p : Shared.getPlayers()) {
					if (!p.disabledNametag) p.updateTeam();
				}
			}
		});
		//fixing a 1.8.x client-sided vanilla bug on bukkit mode
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() == 8 || PluginHooks.viaversion || PluginHooks.protocolsupport)
			Shared.cpu.startRepeatingMeasuredTask(200, "refreshing nametag visibility", "NameTags - invisfix", new Runnable() {
				public void run() {
					for (ITabPlayer p : Shared.getPlayers()) p.setTeamVisible(!p.hasInvisibility());
				}
			});
	}
	public void unload() {
		for (ITabPlayer p : Shared.getPlayers()) {
			if (!p.disabledNametag) p.unregisterTeam();
		}
	}
	@Override
	public void onJoin(ITabPlayer p) {
		if (p.disabledNametag) return;
		p.registerTeam();
		for (ITabPlayer all : Shared.getPlayers()) {
			if (all == p) continue; //already registered 2 lines above
			if (!all.disabledNametag) all.registerTeam(p);
		}
	}
	@Override
	public void onQuit(ITabPlayer p) {
		if (!p.disabledNametag) p.unregisterTeam();
	}
	@Override
	public void onWorldChange(ITabPlayer p, String from, String to) {
		if (p.disabledNametag && !p.isDisabledWorld(Configs.disabledNametag, from)) {
			p.unregisterTeam();
		} else if (!p.disabledNametag && p.isDisabledWorld(Configs.disabledNametag, from)) {
			p.registerTeam();
		} else {
			p.updateTeam();
		}
	}
}