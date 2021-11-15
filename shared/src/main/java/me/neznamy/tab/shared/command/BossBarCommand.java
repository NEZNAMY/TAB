package me.neznamy.tab.shared.command;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.bossbar.BossBarManager;
import me.neznamy.tab.shared.TAB;

/**
 * Handler for "/tab bossbar" subcommand
 */
public class BossBarCommand extends SubCommand {

	/**
	 * Constructs new instance
	 */
	public BossBarCommand() {
		super("bossbar", null);
	}

	@Override
	public void execute(TabPlayer sender, String[] args) {
		BossBarManager bossbar = (BossBarManager) TAB.getInstance().getFeatureManager().getFeature("bossbar");
		if (bossbar == null) {
			sendMessage(sender, getMessages().getBossBarNotEnabled());
			return;
		}
		if (sender == null) {
			sendMessage(sender, getMessages().getCommandOnlyFromGame());
			return;
		}
		if (sender.hasPermission("tab.togglebar")) {
			bossbar.toggleBossBar(sender, true);
		} else {
			sender.sendMessage(getMessages().getNoPermission(), true);
		}
	}
}