package me.neznamy.tab.shared.features;

import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.PluginHooks;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.cpu.CPUFeature;
import me.neznamy.tab.shared.features.interfaces.JoinEventListener;
import me.neznamy.tab.shared.features.interfaces.Loadable;
import me.neznamy.tab.shared.features.interfaces.QuitEventListener;
import me.neznamy.tab.shared.features.interfaces.WorldChangeListener;

public class NameTag16 implements Loadable, JoinEventListener, QuitEventListener, WorldChangeListener{
	
	@Override
	public void load() {
		int refresh = Configs.config.getInt("nametag-refresh-interval-milliseconds", 1000);
		if (refresh < 50) Shared.errorManager.refreshTooLow("NameTags", refresh);
		for (ITabPlayer p : Shared.getPlayers()) {
			if (!p.disabledNametag) p.registerTeam();
		}
		Shared.featureCpu.startRepeatingMeasuredTask(refresh, "refreshing nametags", CPUFeature.NAMETAG, new Runnable() {
			public void run() {
				for (ITabPlayer p : Shared.getPlayers()) {
					if (!p.disabledNametag) p.updateTeam(false);
				}
			}
		});
		//fixing a 1.8.x client-sided vanilla bug on bukkit mode
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() == 8 || PluginHooks.viaversion || PluginHooks.protocolsupport) {
			for (ITabPlayer p : Shared.getPlayers()) {
				p.nameTagVisible = !p.hasInvisibility();
			}
			Shared.featureCpu.startRepeatingMeasuredTask(200, "refreshing nametag visibility", CPUFeature.NAMETAG_INVISFIX, new Runnable() {
				public void run() {
					for (ITabPlayer p : Shared.getPlayers()) {
						boolean visible = !p.hasInvisibility();
						if (p.nameTagVisible != visible) {
							p.nameTagVisible = visible;
							p.updateTeam(false);
						}
					}
				}
			});
		}
	}
	public void unload() {
		for (ITabPlayer p : Shared.getPlayers()) {
			if (!p.disabledNametag) p.unregisterTeam();
		}
	}
	@Override
	public void onJoin(ITabPlayer connectedPlayer) {
		if (connectedPlayer.disabledNametag) return;
		connectedPlayer.registerTeam();
		for (ITabPlayer all : Shared.getPlayers()) {
			if (all == connectedPlayer) continue; //already registered 2 lines above
			if (!all.disabledNametag) all.registerTeam(connectedPlayer);
		}
	}
	@Override
	public void onQuit(ITabPlayer disconnectedPlayer) {
		if (!disconnectedPlayer.disabledNametag) disconnectedPlayer.unregisterTeam();
	}
	@Override
	public void onWorldChange(ITabPlayer p, String from, String to) {
		if (p.disabledNametag && !p.isDisabledWorld(Configs.disabledNametag, from)) {
			p.unregisterTeam();
		} else if (!p.disabledNametag && p.isDisabledWorld(Configs.disabledNametag, from)) {
			p.registerTeam();
		} else {
			if (Shared.separatorType.equals("server")) {
				Shared.featureCpu.runTaskLater(500, "processing server switch", CPUFeature.NAMETAG, new Runnable() {

					@Override
					public void run() {
						for (ITabPlayer all : Shared.getPlayers()) {
							all.unregisterTeam(p);
							all.registerTeam(p);
						}
					}
				});
			} else {
				for (ITabPlayer all : Shared.getPlayers()) {
					all.unregisterTeam(p);
					all.registerTeam(p);
				}
			}
		}
	}
}