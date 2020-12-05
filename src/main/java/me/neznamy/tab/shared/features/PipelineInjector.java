package me.neznamy.tab.shared.features;

import java.util.UUID;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.interfaces.JoinEventListener;
import me.neznamy.tab.shared.features.interfaces.Loadable;

/**
 * A large source of hate. Packet intercepting to secure proper functionality of some features:
 * Tablist names - anti-override (preventing other plugins from setting this value)
 * Nametags - anti-override
 * SpectatorFix - to change gamemode to something else than spectator
 * PetFix - to remove owner field from entity data
 * Unlimited nametags - replacement for bukkit events with much better accuracy and reliability
 */
public abstract class PipelineInjector implements JoinEventListener, Loadable {

	//name of the pipeline decoder injected in netty
	public static final String DECODER_NAME = "TABReader";
	
	/**
	 * Injects custom channel duplex handler to prevent other plugins from overriding this one
	 * @param uuid - player's uuid
	 */
	public abstract void inject(UUID uuid);
	
	public abstract void uninject(UUID uuid);
	
	@Override
	public void load() {
		for (TabPlayer p : Shared.getPlayers()) {
			inject(p.getUniqueId());
		}
	}

	@Override
	public void unload() {
		for (TabPlayer p : Shared.getPlayers()) {
			uninject(p.getUniqueId());
		}
	}
	
	@Override
	public void onJoin(TabPlayer connectedPlayer) {
		inject(connectedPlayer.getUniqueId());
	}
	
	@Override
	public TabFeature getFeatureType() {
		return TabFeature.PIPELINE_INJECTION;
	}
}