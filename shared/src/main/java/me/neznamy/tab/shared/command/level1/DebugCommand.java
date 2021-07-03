package me.neznamy.tab.shared.command.level1;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.PropertyUtils;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.features.GroupRefresher;
import me.neznamy.tab.shared.features.NameTag;
import me.neznamy.tab.shared.features.Playerlist;
import me.neznamy.tab.shared.features.sorting.types.GroupPermission;

/**
 * Handler for "/tab debug" subcommand
 */
public class DebugCommand extends SubCommand {

	/**
	 * Constructs new instance
	 */
	public DebugCommand() {
		super("debug", "tab.debug");
	}

	@Override
	public void execute(TabPlayer sender, String[] args) {
		TabPlayer analyzed = null;
		if (args.length > 0) {
			analyzed = TAB.getInstance().getPlayer(args[0]);
			if (analyzed == null) {
				sendMessage(sender, getTranslation("player_not_found"));
				return;
			}
		}
		if (analyzed == null && sender != null) {
			analyzed = TAB.getInstance().getPlayer(sender.getUniqueId());
		}
		debug(sender, analyzed);
	}

	/**
	 * Peforms debug on player and displays output
	 * @param sender - command sender or null if console
	 * @param analyzed - player to be analyzed
	 */
	private void debug(TabPlayer sender, TabPlayer analyzed) {
		TAB tab = TAB.getInstance();
		String separator = "&7&m>-------------------------------<";
		sendMessage(sender, "&3[TAB] &a&lShowing debug information");
		sendMessage(sender, separator);
		String c2 = tab.isPremium() ? "&b" : "&a";
		sendMessage(sender, "&6Server version: " + c2 + tab.getPlatform().getServerVersion());
		sendMessage(sender, "&6Plugin version: " + c2 + tab.getPluginVersion() + (tab.isPremium() ? " Premium" : ""));
		if (tab.getErrorManager().getErrorLog().exists()) {
			sendMessage(sender, "&6" + tab.getErrorManager().getErrorLog().getPath() + " size: &c" + tab.getErrorManager().getErrorLog().length()/1024 + "KB");
		}
		sendMessage(sender, "&6Permission plugin: " + c2 + tab.getPermissionPlugin().getName());
		sendMessage(sender, "&6Permission group choice logic: " + c2 + getGroupChoiceLogic());
		sendMessage(sender, "&6Sorting system: " + c2 + getSortingType());
		sendMessage(sender, separator);
		if (analyzed == null) return;
		sendMessage(sender, "&ePlayer: &a" + analyzed.getName());
		sendMessage(sender, getGroup(analyzed));
		sendMessage(sender, getTeamName(analyzed));
		sendMessage(sender, getTeamNameNote(analyzed));
		if (tab.getFeatureManager().isFeatureEnabled("playerlist")) {
			Playerlist playerlist = (Playerlist) tab.getFeatureManager().getFeature("playerlist");
			boolean disabledPlayerlist = playerlist.isDisabledWorld(playerlist.getDisabledWorlds(), analyzed.getWorldName());
			showProperty(sender, analyzed, PropertyUtils.TABPREFIX, disabledPlayerlist);
			showProperty(sender, analyzed, PropertyUtils.TABSUFFIX, disabledPlayerlist);
			showProperty(sender, analyzed, PropertyUtils.CUSTOMTABNAME, disabledPlayerlist);
		} else {
			sendMessage(sender, "&atabprefix: &cDisabled");
			sendMessage(sender, "&atabsuffix: &cDisabled");
			sendMessage(sender, "&atabname: &cDisabled");
		}
		if (tab.getFeatureManager().getNameTagFeature() != null) {
			boolean disabledNametags = tab.getFeatureManager().getNameTagFeature().isDisabledWorld(analyzed.getWorldName());
			showProperty(sender, analyzed, PropertyUtils.TAGPREFIX, disabledNametags);
			showProperty(sender, analyzed, PropertyUtils.TAGSUFFIX, disabledNametags);
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
		GroupRefresher group = (GroupRefresher) TAB.getInstance().getFeatureManager().getFeature("group");
		if (group.isGroupsByPermissions()) {
			return "Permissions";
		} else if (group.isUsePrimaryGroup()) {
			return "Primary group";
		} else {
			return "Choose from list";
		}
	}
	
	/**
	 * Returns sorting type
	 * @return sorting type
	 */
	private String getSortingType() {
		NameTag nametag = TAB.getInstance().getFeatureManager().getNameTagFeature();
		if (nametag != null) {
			if (TAB.getInstance().isPremium()) {
				String sortingType = nametag.getSorting().typesToString();
				if (sortingType.contains("PLACEHOLDER")) {
					sortingType += " - " + nametag.getSorting().getSortingPlaceholder();
				}
				return sortingType;
			} else if (nametag.getSorting().getSorting().get(0) instanceof GroupPermission) {
				return "Permissions &c(this option was enabled by user, it is disabled by default!)";
			} else {
				return "Groups";
			}
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
		GroupRefresher group = (GroupRefresher) TAB.getInstance().getFeatureManager().getFeature("group");
		if (group.isGroupsByPermissions()) {
			return "&eHighest permission for group: &a" + analyzed.getGroup();
		} else if (group.isUsePrimaryGroup()) {
			return "&ePrimary permission group: &a" + analyzed.getGroup();
		} else {
			try {
				return "&eFull permission group list: &a" + Arrays.toString(TAB.getInstance().getPermissionPlugin().getAllGroups(analyzed)) + "\n&eChosen group: &a" + analyzed.getGroup();
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
				return "&eFull permission group list: &a[]\n&eChosen group: &a" + analyzed.getGroup();
			}
		}
	}
	
	/**
	 * Returns team name of specified player
	 * @param analyzed - player to get team name of
	 * @return team name of specified player
	 */
	private String getTeamName(TabPlayer analyzed) {
		if (TAB.getInstance().getFeatureManager().getNameTagFeature() != null) {
			if (TAB.getInstance().getFeatureManager().getNameTagFeature().isDisabledWorld(analyzed.getWorldName())) {
				return "&eTeam name: &cSorting is disabled in player's " + TAB.getInstance().getPlatform().getSeparatorType();
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
		if (TAB.getInstance().getFeatureManager().getNameTagFeature() != null && 
			!TAB.getInstance().getFeatureManager().getNameTagFeature().isDisabledWorld(analyzed.getWorldName()) && 
			analyzed.getTeamNameNote() != null)
				return "&eTeam name note: &a" + analyzed.getTeamNameNote();
		return "";
	}
	
	/**
	 * Returns list of extra properties if unlimited nametag mode is enabled
	 * @return list of extra properties
	 */
	@SuppressWarnings("unchecked")
	public List<Object> getExtraLines(){
		if (!TAB.getInstance().getFeatureManager().isFeatureEnabled("nametagx")) return new ArrayList<>();
		if (TAB.getInstance().isPremium()) {
			List<Object> lines = Lists.newArrayList((List<Object>) TAB.getInstance().getConfiguration().getPremiumConfig().getObject("unlimited-nametag-mode-dynamic-lines"));
			lines.addAll(TAB.getInstance().getConfiguration().getPremiumConfig().getConfigurationSection("unlimited-nametag-mode-static-lines").keySet());
			lines.remove(PropertyUtils.NAMETAG);
			lines.add(PropertyUtils.CUSTOMTAGNAME);
			return lines;
		} else {
			return Arrays.asList(PropertyUtils.CUSTOMTAGNAME, PropertyUtils.BELOWNAME, PropertyUtils.ABOVENAME);
		}
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
			sendMessage(sender, "&a" + property + ": &cDisabled in player's " + TAB.getInstance().getPlatform().getSeparatorType());
		} else {
			Property pr = analyzed.getProperty(property);
			String rawValue = pr.getCurrentRawValue().replace('\u00a7', '&');
			String c = TAB.getInstance().isPremium() ? "&7" : "&9";
			String value = String.format(("&a%s: &e\"&r%s&r&e\" &7(%s) " + c + "(Source: %s)").replace('&', '\u00a7'), property, rawValue, rawValue.length(), pr.getSource());
			sendRawMessage(sender, value);
		}
	}

	@Override
	public List<String> complete(TabPlayer sender, String[] arguments) {
		return arguments.length == 1 ? getPlayers(arguments[0]) : new ArrayList<>();
	}
}