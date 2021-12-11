package me.neznamy.tab.shared.command;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;

/**
 * Handler for "/tab ntpreview" subcommand
 */
public class NTPreviewCommand extends SubCommand{

	/**
	 * Constructs new instance
	 */
	public NTPreviewCommand() {
		super("ntpreview", TabConstants.Permission.COMMAND_NTPREVIEW);
	}

	@Override
	public void execute(TabPlayer sender, String[] args) {
		if ((TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.UNLIMITED_NAME_TAGS)) != null) {
			if (sender != null) {
				sender.toggleNametagPreview();
			} else {
				sendMessage(null, getMessages().getCommandOnlyFromGame());
			}
		} else {
			sendMessage(sender, getMessages().getUnlimitedNametagModeNotEnabled());
		}
	}
}