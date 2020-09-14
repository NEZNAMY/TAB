package me.neznamy.tab.shared.command.level1;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.features.bossbar.BossBar;
import me.neznamy.tab.shared.features.bossbar.BossBarLine;

/**
 * Handler for "/tab bossbar" subcommand
 */
public class BossBarCommand extends SubCommand{

	public BossBarCommand() {
		super("bossbar", null);
	}

	@Override
	public void execute(TabPlayer sender, String[] args) {
		BossBar bossbar = (BossBar) Shared.featureManager.getFeature("bossbar");
		if (bossbar == null) {
			sender.sendMessage("&cBossbar feature is not enabled, therefore toggle command cannot be used.", true);
			return;
		}
		if (!bossbar.permToToggle || sender.hasPermission("tab.togglebar")) {
			sender.setBossbarVisible(!sender.hasBossbarVisible());
			if (sender.hasBossbarVisible()) {
				if (sender != null) bossbar.detectBossBarsAndSend(sender);
				sender.sendMessage(Configs.bossbar_on, true);
				if (bossbar.remember_toggle_choice) {
					bossbar.bossbar_off_players.remove(sender.getName());
					Configs.playerdata.set("bossbar-off", bossbar.bossbar_off_players);
				}
			} else {
				for (BossBarLine line : sender.getActiveBossBars()) {
					line.remove(sender);
				}
				sender.getActiveBossBars().clear();
				sender.sendMessage(Configs.bossbar_off, true);
				if (bossbar.remember_toggle_choice && !bossbar.bossbar_off_players.contains(sender.getName())) {
					bossbar.bossbar_off_players.add(sender.getName());
					Configs.playerdata.set("bossbar-off", bossbar.bossbar_off_players);
				}
			}
		} else {
			sender.sendMessage(Configs.no_perm, true);
		}
	}
}