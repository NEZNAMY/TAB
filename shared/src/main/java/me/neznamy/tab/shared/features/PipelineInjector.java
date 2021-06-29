package me.neznamy.tab.shared.features;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.event.JoinEventListener;

/**
 * A large source of hate. Packet intercepting to secure proper functionality of some features:
 * Tablist names - anti-override
 * Nametags - anti-override
 * TabObjective & Belowname - anti-override
 * Scoreboard - disabling tab's scoreboard to prevent conflict
 * SpectatorFix - to change gamemode to something else than spectator
 * PetFix - to remove owner field from entity data
 * Unlimited nametags - replacement for bukkit events with much better accuracy and reliability
 */
public abstract class PipelineInjector implements JoinEventListener, Loadable {

	//name of the pipeline decoder injected in netty
	public static final String DECODER_NAME = "TAB";
	
	//tab instance
	protected TAB tab;
	
	//preventing spam when packet is sent to everyone
	private String lastTeamOverrideMessage;
	
	//anti-override rules
	protected boolean antiOverrideTeams;
	protected boolean antiOverrideObjectives;
	
	/**
	 * Constructs new instance
	 * @param tab
	 */
	protected PipelineInjector(TAB tab) {
		this.tab = tab;
		antiOverrideTeams = tab.getConfiguration().getConfig().getBoolean("anti-override.scoreboard-teams", true);
		antiOverrideObjectives = tab.getConfiguration().getConfig().getBoolean("anti-override.scoreboard-objectives", true);
	}
	
	/**
	 * Injects custom channel duplex handler to prevent other plugins from overriding this one
	 * @param uuid - player's uuid
	 */
	public abstract void inject(TabPlayer player);
	
	public abstract void uninject(TabPlayer player);
	
	@Override
	public void load() {
		for (TabPlayer p : tab.getPlayers()) {
			inject(p);
		}
	}

	@Override
	public void unload() {
		for (TabPlayer p : tab.getPlayers()) {
			uninject(p);
		}
	}
	
	@Override
	public void onJoin(TabPlayer connectedPlayer) {
		inject(connectedPlayer);
	}
	
	@Override
	public TabFeature getFeatureType() {
		return TabFeature.PIPELINE_INJECTION;
	}
	
	protected void logTeamOverride(String team, String player) {
		String message = "Something just tried to add player " + player + " into team " + team;
		//not logging the same message for every online player who received the packet
		if (lastTeamOverrideMessage == null || !message.equals(lastTeamOverrideMessage)) {
			lastTeamOverrideMessage = message;
			tab.getErrorManager().printError(message, null, false, tab.getErrorManager().getAntiOverrideLog());
		}
	}
}