package me.neznamy.tab.shared.command;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.bossbar.BossBarManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;

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
		BossBarManager bossBar = (BossBarManager) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.BOSS_BAR);
		if (bossBar == null) {
			sendMessage(sender, getMessages().getBossBarNotEnabled());
			return;
		}
		if (sender == null) {
			sendMessage(null, getMessages().getCommandOnlyFromGame());
			return;
		}
		if (sender.hasPermission(TabConstants.Permission.COMMAND_BOSSBAR_TOGGLE)) {
			bossBar.toggleBossBar(sender, true);
		} else {
			sender.sendMessage(getMessages().getNoPermission(), true);
		}
	}
}