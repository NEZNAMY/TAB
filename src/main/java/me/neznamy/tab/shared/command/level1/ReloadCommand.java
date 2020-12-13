package me.neznamy.tab.shared.command.level1;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.config.Configs;

/**
 * Handler for "/tab reload" subcommand
 */
public class ReloadCommand extends SubCommand {

	public ReloadCommand() {
		super("reload", "tab.reload");
	}

	@Override
	public void execute(TabPlayer sender, String[] args) {
		Shared.unload();
		Shared.load();
		if (Shared.disabled) {
			if (sender != null) {
				sendMessage(sender, Configs.reloadFailed.replace("%file%", Shared.brokenFile));
			}
		} else {
			sendMessage(sender, Configs.reloaded);
		}
	}
}