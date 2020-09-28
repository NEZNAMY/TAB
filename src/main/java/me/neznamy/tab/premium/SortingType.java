package me.neznamy.tab.premium;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.interfaces.Refreshable;
import me.neznamy.tab.shared.placeholders.Placeholder;
import me.neznamy.tab.shared.placeholders.Placeholders;

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
	private boolean caseSensitiveSorting;
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
			INSTANCE.usedPlaceholders = Placeholders.detectAll(INSTANCE.sortingPlaceholder);
			Shared.featureManager.registerFeature("sorting-refresh", new Refreshable(){

				@Override
				public void refresh(TabPlayer refreshed, boolean force) {
					if (Shared.featureManager.getNameTagFeature().isDisabledWorld(refreshed.getWorldName())) return;
					refreshed.updateTeam();
				}

				@Override
				public Set<String> getUsedPlaceholders() {
					return new HashSet<>(INSTANCE.usedPlaceholders);
				}

				@Override
				public void refreshUsedPlaceholders() {
					INSTANCE.usedPlaceholders = Placeholders.detectAll(INSTANCE.sortingPlaceholder);
				}

				/**
				 * Returns name of the feature displayed in /tab cpu
				 * @return name of the feature displayed in /tab cpu
				 */
				@Override
				public TabFeature getFeatureType() {
					return TabFeature.SORTING;
				}
				
			});
		} else {
			if (Configs.advancedconfig != null) {
				INSTANCE = (Configs.advancedconfig.getBoolean("sort-players-by-permissions", false) ? SortingType.GROUP_PERMISSIONS : SortingType.GROUPS);
			} else {
				INSTANCE = GROUPS;
			}
		}
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
		String chars = Configs.sortedGroups.get(group.toLowerCase()); // 4 chars
		if (chars == null) {
			chars = "";
			if (!group.equals("null")) Shared.errorManager.oneTimeConsoleError("Group \"&e" + group + "&c\" is not defined in sorting list! This will result in players in that group not being sorted correctly. To fix this, add group \"&e" + group + "&c\" into &egroup-sorting-priority-list in config.yml&c.");
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
				Placeholder pl = Placeholders.getPlaceholder(identifier);
				if (pl != null && replaced.contains(pl.getIdentifier())) {
					replaced = pl.set(replaced, player);
				}
			}
		}
		return replaced;
	}
}