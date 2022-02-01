package me.neznamy.tab.shared.command.level2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.bossbar.BossBar;
import me.neznamy.tab.api.bossbar.BossBarManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.features.bossbar.BossBarLine;

/**
 * Handler for "/tab announce bar" subcommand
 */
public class AnnounceBarCommand extends SubCommand {

	/**
	 * Constructs new instance
	 */
	public AnnounceBarCommand() {
		super("bar", TabConstants.Permission.COMMAND_BOSSBAR_ANNOUNCE);
	}

	@Override
	public void execute(TabPlayer sender, String[] args) {
		BossBarManager feature = (BossBarManager) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.BOSS_BAR);
		if (feature == null) {
			sendMessage(sender, getMessages().getBossBarNotEnabled());
			return;
		}
		if (args.length != 2) {
			sendMessage(sender, getMessages().getBossBarAnnounceCommandUsage());
			return;
		}
		String barName = args[0];
		int duration;
		try {
			duration = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			sendMessage(sender, getMessages().getInvalidNumber(args[1]));
			return;
		}
		BossBar bar = feature.getBossBar(barName);
		if (bar == null) {
			sendMessage(sender, getMessages().getBossBarNotFound(barName));
			return;
		}
		if (!((BossBarLine)bar).isAnnouncementOnly()) {
			sendMessage(sender, getMessages().getBossBarNotMarkedAsAnnouncement());
			return;
		}
		if (feature.getAnnouncedBossBars().contains(bar)) {
			sendMessage(sender, getMessages().getBossBarAlreadyAnnounced());
			return;
		}
		feature.announceBossBar(bar.getName(), duration);
		sendMessage(sender, getMessages().getBossBarAnnouncementSuccess(bar.getName(), duration));
	}

	@Override
	public List<String> complete(TabPlayer sender, String[] arguments) {
		BossBarManager b = (BossBarManager) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.BOSS_BAR);
		if (b == null) return new ArrayList<>();
		List<String> suggestions = new ArrayList<>();
		if (arguments.length == 1) {
			for (String bar : b.getRegisteredBossBars().keySet()) {
				if (bar.toLowerCase().startsWith(arguments[0].toLowerCase())) suggestions.add(bar);
			}
		} else if (arguments.length == 2 && b.getBossBar(arguments[0]) != null){
			for (String time : Arrays.asList("5", "10", "30", "60", "120")) {
				if (time.startsWith(arguments[1])) suggestions.add(time);
			}
		}
		return suggestions;
	}
}