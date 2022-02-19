package me.neznamy.tab.shared.command;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.team.TeamManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.nametags.unlimited.NameTagX;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Handler for "/tab nametag" subcommand
 */
public class NameTagCommand extends SubCommand {

	/**
	 * Constructs new instance
	 */
	public NameTagCommand() {
		super("nametag", null);
	}

	@Override
	public void execute(TabPlayer sender, String[] args) {
		if (args.length != 1) {
			sendMessages(sender, getMessages().getNameTagHelpMenu());
			return;
		}
		switch (args[0].toLowerCase(Locale.US)) {
			case "preview":
				preview(sender);
				break;
			case "toggle":
				toggle(sender);
				break;
			default:
				sendMessages(sender, getMessages().getNameTagHelpMenu());
				break;
		}
	}

	private void preview(TabPlayer sender) {
		if (sender == null) {
			sendMessage(null, getMessages().getCommandOnlyFromGame());
			return;
		}
		if (!hasPermission(sender, TabConstants.Permission.COMMAND_NAMETAG_PREVIEW)) {
			sendMessage(sender, getMessages().getNoPermission());
			return;
		}
		NameTagX nameTagX = (NameTagX) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.UNLIMITED_NAME_TAGS);
		if (nameTagX == null) {
			sendMessage(sender, getMessages().getUnlimitedNametagModeNotEnabled());
			return;
		}
		nameTagX.toggleNametagPreview(sender);
	}

	private void toggle(TabPlayer sender) {
		if (sender == null) {
			sendMessage(null, getMessages().getCommandOnlyFromGame());
			return;
		}
		if (!hasPermission(sender, TabConstants.Permission.COMMAND_NAMETAG_TOGGLE)) {
			sendMessage(sender, getMessages().getNoPermission());
			return;
		}
		TeamManager teams = TAB.getInstance().getTeamManager();
		if (teams == null) {
			sendMessage(sender, getMessages().getNameTagFeatureNotEnabled());
			return;
		}
		teams.toggleNameTagVisibilityView(sender, true);
	}

	@Override
	public List<String> complete(TabPlayer sender, String[] arguments) {
		return getStartingArgument(Arrays.asList("preview", "toggle"), arguments[0]);
	}
}