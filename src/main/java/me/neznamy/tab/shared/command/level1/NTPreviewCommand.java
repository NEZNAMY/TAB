package me.neznamy.tab.shared.command.level1;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.config.Configs;

/**
 * Handler for "/tab ntpreview" subcommand
 */
public class NTPreviewCommand extends SubCommand{

	public NTPreviewCommand() {
		super("ntpreview", "tab.ntpreview");
	}

	@Override
	public void execute(TabPlayer sender, String[] args) {
		if ((Shared.featureManager.getFeature("nametagx")) != null) {
			if (sender != null) {
				sender.toggleNametagPreview();
			} else {
				sendMessage(sender, "&c[TAB] This command must be ran from the game");
			}
		} else {
			sendMessage(sender, Configs.unlimited_nametag_mode_not_enabled);
		}
	}
}