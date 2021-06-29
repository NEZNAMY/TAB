package me.neznamy.tab.shared.command.level1;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.features.bossbar.BossBar;

/**
 * Handler for "/tab bossbar" subcommand
 */
public class BossBarCommand extends SubCommand{

	/**
	 * Constructs new instance
	 */
	public BossBarCommand() {
		super("bossbar", null);
	}

	@Override
	public void execute(TabPlayer sender, String[] args) {
		BossBar bossbar = (BossBar) TAB.getInstance().getFeatureManager().getFeature("bossbar");
		if (bossbar == null) {
			sendMessage(sender, "&cBossbar feature is not enabled, therefore toggle command cannot be used.");
			return;
		}
		if (sender == null) {
			sendMessage(sender, "&cThis command must be ran from the game");
			return;
		}
		if (!bossbar.isPermToToggle() || sender.hasPermission("tab.togglebar")) {
			sender.setBossbarVisible(!sender.hasBossbarVisible(), true);
		} else {
			sender.sendMessage(getTranslation("no_permission"), true);
		}
	}
}