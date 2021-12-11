package me.neznamy.tab.shared.command;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.shared.PropertyImpl;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.PlayerList;
import me.neznamy.tab.shared.features.nametags.NameTag;

/**
 * Handler for "/tab debug" subcommand
 */
public class DebugCommand extends SubCommand {

	/**
	 * Constructs new instance
	 */
	public DebugCommand() {
		super("debug", TabConstants.Permission.COMMAND_DEBUG);
	}

	@Override
	public void execute(TabPlayer sender, String[] args) {
		TabPlayer analyzed = null;
		if (args.length > 0) {
			analyzed = TAB.getInstance().getPlayer(args[0]);
			if (analyzed == null) {
				sendMessage(sender, getMessages().getPlayerNotFound(args[0]));
				return;
			}
		}
		if (analyzed == null && sender != null) {
			analyzed = TAB.getInstance().getPlayer(sender.getUniqueId());
		}
		debug(sender, analyzed);
	}

	/**
	 * Performs debug on player and displays output
	 * @param sender - command sender or null if console
	 * @param analyzed - player to be analyzed
	 */
	private void debug(TabPlayer sender, TabPlayer analyzed) {
		TAB tab = TAB.getInstance();
		String separator = "&7&m>-------------------------------<";
		sendMessage(sender, "&3[TAB] &a&lShowing debug information");
		sendMessage(sender, separator);
		sendMessage(sender, "&6Server version: &b" + tab.getPlatform().getServerVersion());
		sendMessage(sender, "&6Plugin version: &b" + TAB.PLUGIN_VERSION);
		if (tab.getErrorManager().getErrorLog().exists()) {
			sendMessage(sender, "&6" + tab.getErrorManager().getErrorLog().getPath() + " size: &c" + tab.getErrorManager().getErrorLog().length()/1024 + "KB");
		}
		sendMessage(sender, "&6Permission plugin: &b" + TAB.getInstance().getGroupManager().getPlugin().getName());
		sendMessage(sender, "&6Permission group choice logic: &b" + getGroupChoiceLogic());
		sendMessage(sender, "&6Sorting system: &b" + getSortingType());
		sendMessage(sender, separator);
		if (analyzed == null) return;
		sendMessage(sender, "&ePlayer: &a" + analyzed.getName());
		sendMessage(sender, getGroup(analyzed));
		sendMessage(sender, getTeamName(analyzed));
		sendMessage(sender, getTeamNameNote(analyzed));
		if (tab.getFeatureManager().isFeatureEnabled(TabConstants.Feature.PLAYER_LIST)) {
			PlayerList playerlist = (PlayerList) tab.getFeatureManager().getFeature(TabConstants.Feature.PLAYER_LIST);
			boolean disabledPlayerlist = playerlist.isDisabled(analyzed.getServer(), analyzed.getWorld());
			showProperty(sender, analyzed, TabConstants.Property.TABPREFIX, disabledPlayerlist);
			showProperty(sender, analyzed, TabConstants.Property.TABSUFFIX, disabledPlayerlist);
			showProperty(sender, analyzed, TabConstants.Property.CUSTOMTABNAME, disabledPlayerlist);
		} else {
			sendMessage(sender, "&atabprefix: &cDisabled");
			sendMessage(sender, "&atabsuffix: &cDisabled");
			sendMessage(sender, "&acustomtabname: &cDisabled");
		}
		if (tab.getTeamManager() != null) {
			boolean disabledNametags = ((TabFeature) tab.getTeamManager()).isDisabled(analyzed.getServer(), analyzed.getWorld());
			showProperty(sender, analyzed, TabConstants.Property.TAGPREFIX, disabledNametags);
			showProperty(sender, analyzed, TabConstants.Property.TAGSUFFIX, disabledNametags);
			for (Object line : getExtraLines()) {
				showProperty(sender, analyzed, line.toString(), disabledNametags);
			}
		} else {
			sendMessage(sender, "&atagprefix: &cDisabled");
			sendMessage(sender, "&atagsuffix: &cDisabled");
		}
		sendMessage(sender, separator);
	}
	
	/**
	 * Returns group choice logic
	 * @return group choice logic
	 */
	private String getGroupChoiceLogic() {
		if (TAB.getInstance().getGroupManager().isGroupsByPermissions()) {
			return "Permissions";
		}
		return "Primary group";
		
	}
	
	/**
	 * Returns sorting type
	 * @return sorting type
	 */
	private String getSortingType() {
		NameTag nametag = (NameTag) TAB.getInstance().getTeamManager();
		if (nametag != null) {
			return nametag.getSorting().typesToString();
		} else {
			return "&cDISABLED";
		}
	}
	
	/**
	 * Returns all info about player's group
	 * @param analyzed - player to check group of
	 * @return all info about player's group
	 */
	private String getGroup(TabPlayer analyzed) {
		if (TAB.getInstance().getGroupManager().isGroupsByPermissions()) {
			return "&eHighest permission for group: &a" + analyzed.getGroup();
		}
		return "&ePrimary permission group: &a" + analyzed.getGroup();
	}
	
	/**
	 * Returns team name of specified player
	 * @param analyzed - player to get team name of
	 * @return team name of specified player
	 */
	private String getTeamName(TabPlayer analyzed) {
		if (TAB.getInstance().getTeamManager() != null) {
			if (((TabFeature) TAB.getInstance().getTeamManager()).isDisabled(analyzed.getServer(), analyzed.getWorld())) {
				return "&eTeam name: &cSorting is disabled in player's world/server";
			} else {
				return "&eTeam name: &a" + analyzed.getTeamName();
			}
		}
		return "";
	}
	

	/**
	 * Returns team name note of specified player
	 * @param analyzed - player to get team name note of
	 * @return team name note of specified player
	 */
	private String getTeamNameNote(TabPlayer analyzed) {
		if (TAB.getInstance().getTeamManager() != null && 
			!((TabFeature) TAB.getInstance().getTeamManager()).isDisabled(analyzed.getServer(), analyzed.getWorld()) && 
			analyzed.getTeamNameNote() != null)
				return "&eTeam name note: &r" + analyzed.getTeamNameNote();
		return "";
	}
	
	/**
	 * Returns list of extra properties if unlimited nametag mode is enabled
	 * @return list of extra properties
	 */
	@SuppressWarnings("unchecked")
	public List<Object> getExtraLines(){
		if (!TAB.getInstance().getFeatureManager().isFeatureEnabled(TabConstants.Feature.UNLIMITED_NAME_TAGS)) return new ArrayList<>();
		List<Object> lines = Lists.newArrayList((List<Object>) TAB.getInstance().getConfiguration().getConfig().getObject("scoreboard-teams.unlimited-nametag-mode.dynamic-lines"));
		lines.addAll(TAB.getInstance().getConfiguration().getConfig().getConfigurationSection("scoreboard-teams.unlimited-nametag-mode.static-lines").keySet());
		lines.remove(TabConstants.Property.NAMETAG);
		lines.add(TabConstants.Property.CUSTOMTAGNAME);
		return lines;
	}

	/**
	 * Shows value and source of player's property
	 * @param sender - command sender or null if console
	 * @param analyzed - analyzed player
	 * @param property - property name
	 * @param disabled - if feature the property belongs to is disabled or not
	 */
	private void showProperty(TabPlayer sender, TabPlayer analyzed, String property, boolean disabled) {
		if (disabled) {
			sendMessage(sender, "&a" + property + ": &cDisabled in player's world/server");
		} else {
			PropertyImpl pr = (PropertyImpl) analyzed.getProperty(property);
			String rawValue = EnumChatFormat.decolor(pr.getCurrentRawValue());
			String value = String.format((EnumChatFormat.color("&a%s: &e\"&r%s&r&e\" &7(%s) &7(Source: %s)")), property, rawValue, rawValue.length(), pr.getSource());
			sendRawMessage(sender, value);
		}
	}

	@Override
	public List<String> complete(TabPlayer sender, String[] arguments) {
		return arguments.length == 1 ? getOnlinePlayers(arguments[0]) : new ArrayList<>();
	}
}