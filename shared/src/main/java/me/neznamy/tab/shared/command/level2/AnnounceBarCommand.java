package me.neznamy.tab.shared.command.level2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.bossbar.BossBar;
import me.neznamy.tab.api.bossbar.BossBarManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.command.SubCommand;

/**
 * Handler for "/tab announce bar" subcommand
 */
public class AnnounceBarCommand extends SubCommand {

	/**
	 * Constructs new instance
	 */
	public AnnounceBarCommand() {
		super("bar", "tab.announce.bar");
	}

	@Override
	public void execute(TabPlayer sender, String[] args) {
		BossBarManager feature = (BossBarManager) TAB.getInstance().getFeatureManager().getFeature("bossbar");
		if (feature == null) {
			sendMessage(sender, getMessages().getBossBarNotEnabled());
			return;
		}
		if (args.length != 2) {
			sendMessage(sender, getMessages().getBossBarAnnounceCommandUsage());
			return;
		}
		String barname = args[0];
		int duration;
		try {
			duration = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			sendMessage(sender, getMessages().getInvalidNumber(args[1]));
			return;
		}
		BossBar bar = feature.getBossBar(barname);
		if (bar == null) {
			sendMessage(sender, getMessages().getBossBarNotFound(barname));
			return;
		}
		if (feature.getAnnouncedBossBars().contains(bar)) {
			sendMessage(sender, getMessages().getBossBarAlreadyAnnounced());
			return;
		}
		feature.announceBossBar(bar.getName(), duration);
	}

	@Override
	public List<String> complete(TabPlayer sender, String[] arguments) {
		BossBarManager b = (BossBarManager) TAB.getInstance().getFeatureManager().getFeature("bossbar");
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