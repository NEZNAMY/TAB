package me.neznamy.tab.premium;

import java.util.Arrays;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.PlaceholderManager;
import me.neznamy.tab.shared.placeholders.Placeholder;

/**
 * Enum class for all supported sorting types
 */
public enum SortingType {
	
	GROUPS, GROUP_PERMISSIONS, 
	TABPREFIX_A_TO_Z,
	PLACEHOLDER_LOW_TO_HIGH, PLACEHOLDER_HIGH_TO_LOW, PLACEHOLDER_A_TO_Z, PLACEHOLDER_Z_TO_A,
	GROUPS_THEN_PLACEHOLDER_HIGH_TO_LOW, GROUPS_THEN_PLACEHOLDER_LOW_TO_HIGH, 
	GROUPS_THEN_PLACEHOLDER_A_TO_Z, GROUPS_THEN_PLACEHOLDER_Z_TO_A,
	GROUP_PERMISSIONS_THEN_PLACEHOLDER_HIGH_TO_LOW, GROUP_PERMISSIONS_THEN_PLACEHOLDER_LOW_TO_HIGH, 
	GROUP_PERMISSIONS_THEN_PLACEHOLDER_A_TO_Z, GROUP_PERMISSIONS_THEN_PLACEHOLDER_Z_TO_A;
	
	private final int DEFAULT_NUMBER = 500000000;
	public static SortingType INSTANCE;
	public String sortingPlaceholder;
	private boolean caseSensitiveSorting = true;
	private List<String> usedPlaceholders;
	
	public static void initialize() {
		if (Premium.is()) {
			String type = Premium.premiumconfig.getString("sorting-type", "GROUPS");
			try {
				INSTANCE = valueOf(type.toUpperCase());
			} catch (Throwable e) {
				Shared.errorManager.startupWarn("\"&e" + type + "&c\" is not a valid type of sorting type. Valid options are: &e" + Arrays.deepToString(values()) + ". &bUsing GROUPS");
				INSTANCE = GROUPS;
			}
			INSTANCE.sortingPlaceholder = Premium.premiumconfig.getString("sorting-placeholder", "%some_level_maybe?%");
			INSTANCE.caseSensitiveSorting = Premium.premiumconfig.getBoolean("case-sentitive-sorting", true);
			INSTANCE.usedPlaceholders = PlaceholderManager.getUsedPlaceholderIdentifiersRecursive(INSTANCE.sortingPlaceholder);
		} else {
			INSTANCE = (Configs.config.getBoolean("sort-players-by-permissions", false) ? SortingType.GROUP_PERMISSIONS : SortingType.GROUPS);
		}
		Shared.cpu.startRepeatingMeasuredTask(1000, "refreshing team names", TabFeature.NAMETAGS, UsageType.REFRESHING_TEAM_NAME, new Runnable() {

			@Override
			public void run() {
				for (TabPlayer p : Shared.getPlayers()) {
					if (!p.isLoaded()) continue;
					String newName = INSTANCE.getTeamName(p);
					if (!p.getTeamName().equals(newName)) {
						p.unregisterTeam();
						p.setTeamName(newName);
						p.registerTeam();
					}
				}
			}
		});
	}
	
	public String getTeamName(TabPlayer p) {
		String teamName = null;
		switch(this){
		case GROUPS:
			teamName = getGroupChars(p.getGroup(), p);
			break;
		case GROUP_PERMISSIONS:
			teamName = getGroupPermissionChars(p);
			break;
		case TABPREFIX_A_TO_Z:
			teamName = p.getProperty("tabprefix").get();
			break;
		case PLACEHOLDER_LOW_TO_HIGH:
			teamName = placeholderLowToHigh(p);
			break;
		case PLACEHOLDER_HIGH_TO_LOW:
			teamName = placeholderHighToLow(p);
			break;
		case PLACEHOLDER_A_TO_Z:
			teamName = setPlaceholders(sortingPlaceholder, p);
			break;
		case PLACEHOLDER_Z_TO_A:
			teamName = placeholderZtoA(p);
			break;
		case GROUPS_THEN_PLACEHOLDER_LOW_TO_HIGH:
			teamName = getGroupChars(p.getGroup(), p) + placeholderLowToHigh(p);
			break;
		case GROUPS_THEN_PLACEHOLDER_HIGH_TO_LOW:
			teamName = getGroupChars(p.getGroup(), p) + placeholderHighToLow(p);
			break;
		case GROUPS_THEN_PLACEHOLDER_A_TO_Z:
			teamName = getGroupChars(p.getGroup(), p) + setPlaceholders(sortingPlaceholder, p);
			break;
		case GROUPS_THEN_PLACEHOLDER_Z_TO_A:
			teamName = getGroupChars(p.getGroup(), p) + placeholderZtoA(p);
			break;
		case GROUP_PERMISSIONS_THEN_PLACEHOLDER_LOW_TO_HIGH:
			teamName = getGroupPermissionChars(p) + placeholderLowToHigh(p);
			break;
		case GROUP_PERMISSIONS_THEN_PLACEHOLDER_HIGH_TO_LOW:
			teamName = getGroupPermissionChars(p) + placeholderHighToLow(p);
			break;
		case GROUP_PERMISSIONS_THEN_PLACEHOLDER_A_TO_Z:
			teamName = getGroupPermissionChars(p) + setPlaceholders(sortingPlaceholder, p);
			break;
		case GROUP_PERMISSIONS_THEN_PLACEHOLDER_Z_TO_A:
			teamName = getGroupPermissionChars(p) + placeholderZtoA(p);
			break;
		default:
			break;
		}
		if (teamName.length() > 12) {
			teamName = teamName.substring(0, 12);
		}
		teamName += p.getName();
		if (teamName.length() > 15) {
			teamName = teamName.substring(0, 15);
		}
		main:
		for (int i = 65; i <= 255; i++) {
			String potentialTeamName = teamName;
			if (!caseSensitiveSorting) potentialTeamName = potentialTeamName.toLowerCase();
			potentialTeamName += (char)i;
			for (TabPlayer all : Shared.getPlayers()) {
				if (all == p) continue;
				if (all.getTeamName() != null && all.getTeamName().equals(potentialTeamName)) {
					continue main;
				}
			}
			return potentialTeamName;
		}
		return "InvalidTeam";
	}
	private String placeholderZtoA(TabPlayer p) {
		char[] chars = setPlaceholders(sortingPlaceholder, p).toCharArray();
		for (int i=0; i<chars.length; i++) {
			char c = chars[i];
			if (c >= 65 && c <= 90) {
				chars[i] = (char) (155 - c);
			}
			if (c >= 97 && c <= 122) {
				chars[i] = (char) (219 - c);
			}
		}
		return new String(chars);
	}
	private String placeholderLowToHigh(TabPlayer p) {
		int intValue = Shared.errorManager.parseInteger(setPlaceholders(sortingPlaceholder, p), 0, "numeric sorting placeholder");
		return String.valueOf(DEFAULT_NUMBER + intValue);
	}
	private String placeholderHighToLow(TabPlayer p) {
		int intValue = Shared.errorManager.parseInteger(setPlaceholders(sortingPlaceholder, p), 0, "numeric sorting placeholder");
		return String.valueOf(DEFAULT_NUMBER - intValue);
	}
	public String getGroupChars(String group, TabPlayer p) {
		String chars = Configs.sortedGroups.get(group.toLowerCase()); // 3 chars
		if (chars == null) {
			chars = "999";
			if (!group.equals("<null>")) Shared.errorManager.oneTimeConsoleError("Group \"&e" + group + "&c\" is not defined in sorting list! This will result in players in that group not being sorted correctly. To fix this, add group \"&e" + group + "&c\" into &egroup-sorting-priority-list in config.yml&c.");
			p.setTeamNameNote("&cPlayer's primary group is not in sorting list");
		} else {
			p.setTeamNameNote("Primary group is #" + Integer.parseInt(chars) + " in sorting list");
			
		}
		return chars;
	}
	public String getGroupPermissionChars(TabPlayer p) {
		String chars = null;
		for (String localgroup : Configs.sortedGroups.keySet()) {
			if (p.hasPermission("tab.sort." + localgroup)) {
				chars = getGroupChars(localgroup, p);
				p.setTeamNameNote("Highest sorting permission: &etab.sort." + localgroup + " &a(#" + Integer.parseInt(chars) + " in sorting list)");
				if (p.hasPermission("random.permission")) {
					p.setTeamNameNote(p.getTeamNameNote() + ". &cThis user appears to have all permissions. Is he OP?");
				}
				break;
			}
		}
		if (chars == null) {
			chars = "";
			Shared.errorManager.oneTimeConsoleError("Sorting by permissions is enabled but player " + p.getName() + " does not have any sorting permission. Configure sorting permissions or disable sorting by permissions like it is by default.");
			p.setTeamNameNote("&cPlayer does not have sorting permission for any group in sorting list");
		}
		return chars;
	}
	private String setPlaceholders(String string, TabPlayer player) {
		String replaced = string;
		if (string.contains("%")) {
			for (String identifier : usedPlaceholders) {
				Placeholder pl = ((PlaceholderManager) Shared.featureManager.getFeature("placeholders")).getPlaceholder(identifier);
				if (pl != null && replaced.contains(pl.getIdentifier())) {
					replaced = pl.set(replaced, player);
				}
			}
		}
		return replaced;
	}
}