package me.neznamy.tab.shared.command.level1;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.config.Configs;

public class ReloadCommand extends SubCommand{

	public ReloadCommand() {
		super("reload", "tab.reload");
	}

	@Override
	public void execute(ITabPlayer sender, String[] args) {
		Shared.unload();
		Shared.load(false);
		if (Shared.disabled) {
			if (sender != null) {
				sendMessage(sender, Configs.reloadFailed.replace("%file%", Shared.brokenFile));
			}
		} else {
			sendMessage(sender, Configs.reloaded);
		}
	}
}