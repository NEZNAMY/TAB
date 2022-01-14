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
public class SendBarCommand extends SubCommand {

	/**
	 * Constructs new instance
	 */
	public SendBarCommand() {
		super("bar", TabConstants.Permission.COMMAND_BOSSBAR_SEND);
	}

	@Override
	public void execute(TabPlayer sender, String[] args) {
		BossBarManager feature = (BossBarManager) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.BOSS_BAR);
		if (feature == null) {
			sendMessage(sender, getMessages().getBossBarNotEnabled());
			return;
		}
		if (args.length != 3) {
			sendMessage(sender, getMessages().getSendBarCommandUsage());
			return;
		}
		TabPlayer target = TAB.getInstance().getPlayer(args[0]);
		if (target == null) {
			sendMessage(sender, getMessages().getPlayerNotFound(args[0]));
			return;
		}
		String barName = args[1];
		int duration;
		try {
			duration = Integer.parseInt(args[2]);
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
		feature.sendBossBarTemporarily(target, bar.getName(), duration);
		sendMessage(sender, getMessages().getBossBarSendSuccess(target.getName(), bar.getName(), duration));
	}

	@Override
	public List<String> complete(TabPlayer sender, String[] arguments) {
		BossBarManager b = (BossBarManager) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.BOSS_BAR);
		if (b == null) return new ArrayList<>();
		if (arguments.length == 1) return getOnlinePlayers(arguments[0]);
		if (arguments.length == 2) return getStartingArgument(b.getRegisteredBossBars().keySet(), arguments[1]);
		if (arguments.length == 3 && b.getBossBar(arguments[1]) != null) return getStartingArgument(Arrays.asList("5", "10", "30", "60", "120"), arguments[2]);
		return new ArrayList<>();
	}
}