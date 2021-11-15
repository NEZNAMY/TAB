package me.neznamy.tab.shared.command;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;

/**
 * Handler for "/tab ntpreview" subcommand
 */
public class NTPreviewCommand extends SubCommand{

	/**
	 * Constructs new instance
	 */
	public NTPreviewCommand() {
		super("ntpreview", "tab.ntpreview");
	}

	@Override
	public void execute(TabPlayer sender, String[] args) {
		if ((TAB.getInstance().getFeatureManager().getFeature("nametagx")) != null) {
			if (sender != null) {
				sender.toggleNametagPreview();
			} else {
				sendMessage(sender, getMessages().getCommandOnlyFromGame());
			}
		} else {
			sendMessage(sender, getMessages().getUnlimitedNametagModeNotEnabled());
		}
	}
}