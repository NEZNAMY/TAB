package me.neznamy.tab.shared.features;

import java.util.NoSuchElementException;
import java.util.function.Function;

import io.netty.channel.ChannelDuplexHandler;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;

/**
 * Packet intercepting to secure proper functionality of some features:
 * Tablist names - anti-override
 * Nametags - anti-override
 * Scoreboard - disabling tab's scoreboard to prevent conflict
 * SpectatorFix - to change gamemode to something else than spectator
 * PetFix - to remove owner field from entity data
 * PingSpoof - full feature functionality
 * Unlimited nametags - replacement for bukkit events with much better accuracy and reliability
 */
public abstract class PipelineInjector extends TabFeature {

	//name of the pipeline decoder injected in netty
	public static final String DECODER_NAME = "TAB";
	
	//handler to inject before
	private final String injectPosition;
	
	//preventing spam when packet is sent to everyone
	private String lastTeamOverrideMessage;
	
	//anti-override rules
	protected boolean antiOverrideTeams;
	
	protected Function<TabPlayer, ChannelDuplexHandler> channelFunction;
	
	protected boolean bytebufDeserialization;
	
	/**
	 * Constructs new instance
	 * @param tab
	 */
	protected PipelineInjector(String injectPosition) {
		super("Pipeline injection");
		this.injectPosition = injectPosition;
		antiOverrideTeams = TAB.getInstance().getConfiguration().getConfig().getBoolean("scoreboard-teams.enabled", true) && 
				TAB.getInstance().getConfiguration().getConfig().getBoolean("scoreboard-teams.anti-override", true);
		if (antiOverrideTeams) setByteBufDeserialization(true);
	}
	
	/**
	 * Injects custom channel duplex handler to prevent other plugins from overriding this one
	 * @param uuid - player's uuid
	 */
	public void inject(TabPlayer player) {
		if (player.getVersion().getMinorVersion() < 8 || player.getChannel() == null) return; //hello A248
		if (!player.getChannel().pipeline().names().contains(injectPosition)) {
			//fake player or waterfall bug
			return;
		}
		uninject(player);
		try {
			player.getChannel().pipeline().addBefore(injectPosition, DECODER_NAME, channelFunction.apply(player));
		} catch (NoSuchElementException | IllegalArgumentException e) {
			//idk how does this keep happening but whatever
		}
	}

	public void uninject(TabPlayer player) {
		if (player.getVersion().getMinorVersion() < 8 || player.getChannel() == null) return; //hello A248
		try {
			if (player.getChannel().pipeline().names().contains(DECODER_NAME)) player.getChannel().pipeline().remove(DECODER_NAME);
		} catch (NoSuchElementException e) {
			//for whatever reason this rarely throws
			//java.util.NoSuchElementException: TABReader
		}
	}
	
	@Override
	public void load() {
		for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
			inject(p);
		}
	}

	@Override
	public void unload() {
		for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
			uninject(p);
		}
	}
	
	@Override
	public void onJoin(TabPlayer connectedPlayer) {
		inject(connectedPlayer);
	}

	protected void logTeamOverride(String team, String player) {
		String message = "Something just tried to add player " + player + " into team " + team;
		//not logging the same message for every online player who received the packet
		if (lastTeamOverrideMessage == null || !message.equals(lastTeamOverrideMessage)) {
			lastTeamOverrideMessage = message;
			TAB.getInstance().getErrorManager().printError(message, null, false, TAB.getInstance().getErrorManager().getAntiOverrideLog());
		}
	}
	
	public void setByteBufDeserialization(boolean bytebufDeserialization) {
		this.bytebufDeserialization = bytebufDeserialization;
	}
}