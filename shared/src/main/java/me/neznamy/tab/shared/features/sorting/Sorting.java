package me.neznamy.tab.shared.features.sorting;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.shared.features.types.JoinListener;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.Refreshable;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.api.team.TeamManager;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.layout.LayoutManager;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.redis.RedisPlayer;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.features.sorting.types.Groups;
import me.neznamy.tab.shared.features.sorting.types.Permissions;
import me.neznamy.tab.shared.features.sorting.types.Placeholder;
import me.neznamy.tab.shared.features.sorting.types.PlaceholderAtoZ;
import me.neznamy.tab.shared.features.sorting.types.PlaceholderHighToLow;
import me.neznamy.tab.shared.features.sorting.types.PlaceholderLowToHigh;
import me.neznamy.tab.shared.features.sorting.types.PlaceholderZtoA;
import me.neznamy.tab.shared.features.sorting.types.SortingType;
import org.jetbrains.annotations.NotNull;

/**
 * Class for handling player sorting rules
 */
public class Sorting extends TabFeature implements JoinListener, Loadable, Refreshable {

    @Getter private final String featureName = "Team name refreshing";
    @Getter private final String refreshDisplayName = "Updating team name";

    private NameTag nameTags;
    private LayoutManager layout;
    private RedisSupport redis;
    
    //map of all registered sorting types
    private final Map<String, BiFunction<Sorting, String, SortingType>> types = new LinkedHashMap<>();
    
    //if sorting is case-sensitive or not
    @Getter private final boolean caseSensitiveSorting = TAB.getInstance().getConfiguration().getConfig().getBoolean("scoreboard-teams.case-sensitive-sorting", true);
    
    //active sorting types
    private final SortingType[] usedSortingTypes;

    //team names and notes
    private final WeakHashMap<TabPlayer, String> shortTeamNames = new WeakHashMap<>();
    private final WeakHashMap<TabPlayer, String> fullTeamNames = new WeakHashMap<>();
    private final WeakHashMap<TabPlayer, String> teamNameNotes = new WeakHashMap<>();
    
    /**
     * Constructs new instance and loads config options
     */
    public Sorting() {
        types.put("GROUPS", Groups::new);
        types.put("PERMISSIONS", Permissions::new);
        types.put("PLACEHOLDER", Placeholder::new);
        types.put("PLACEHOLDER_A_TO_Z", PlaceholderAtoZ::new);
        types.put("PLACEHOLDER_Z_TO_A", PlaceholderZtoA::new);
        types.put("PLACEHOLDER_LOW_TO_HIGH", PlaceholderLowToHigh::new);
        types.put("PLACEHOLDER_HIGH_TO_LOW", PlaceholderHighToLow::new);
        usedSortingTypes = compile(TAB.getInstance().getConfig().getStringList("scoreboard-teams.sorting-types", new ArrayList<>()));
    }
    
    @Override
    public void refresh(@NonNull TabPlayer p, boolean force) {
        String previousShortName = shortTeamNames.get(p);
        constructTeamNames(p);
        if (!shortTeamNames.get(p).equals(previousShortName)) {
            if (nameTags != null && nameTags.getForcedTeamName(p) == null && !nameTags.hasTeamHandlingPaused(p) && !nameTags.isDisabledPlayer(p)) {
                for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
                    viewer.getScoreboard().unregisterTeam(previousShortName);
                }
                nameTags.registerTeam(p);
            }
            if (layout != null) layout.updateTeamName(p, fullTeamNames.get(p));
        }
    }
    
    @Override
    public void load() {
        // All of these features are instantiated after this one, so they must be detected later
        nameTags = (NameTag) TAB.getInstance().getTeamManager();
        layout = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.LAYOUT);
        redis = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.REDIS_BUNGEE);
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            constructTeamNames(all);
        }
    }
    
    @Override
    public void onJoin(@NonNull TabPlayer connectedPlayer) {
        constructTeamNames(connectedPlayer);
    }
    
    /**
     * Compiles sorting type list into classes
     *
     * @return  list of compiled sorting types
     */
    private @NotNull SortingType[] compile(@NonNull List<String> options) {
        List<SortingType> list = new ArrayList<>();
        for (String element : options) {
            String[] arr = element.split(":");
            if (!types.containsKey(arr[0].toUpperCase())) {
                TAB.getInstance().getMisconfigurationHelper().invalidSortingTypeElement(arr[0].toUpperCase(), types.keySet());
            } else {
                list.add(types.get(arr[0].toUpperCase()).apply(this, arr.length == 1 ? "" : element.substring(arr[0].length() + 1)));
            }
        }
        return list.toArray(new SortingType[0]);
    }
    
    /**
     * Constructs short team names, both short (up to 16 characters long)
     * and full for specified player
     *
     * @param   p
     *          player to build team name for
     */
    public void constructTeamNames(@NonNull TabPlayer p) {
        teamNameNotes.put(p, "");
        StringBuilder shortName = new StringBuilder();
        for (SortingType type : usedSortingTypes) {
            shortName.append(type.getChars(p));
        }
        StringBuilder fullName = new StringBuilder(shortName);
        if (layout != null) {
            //layout is enabled, start with max character to fix compatibility with plugins
            //which add empty player into a team such as LibsDisguises
            shortName.insert(0, Character.MAX_VALUE);
        }
        if (shortName.length() > 15) {
            shortName.setLength(15);
        }
        String finalShortName = checkTeamName(p, shortName, 65);
        shortTeamNames.put(p, finalShortName);
        fullTeamNames.put(p, fullName.append(finalShortName.charAt(finalShortName.length() - 1)).toString());
    }

    /**
     * Checks if team name is available and proceeds to try new values until free name is found
     *
     * @param   p
     *          player to build team name for
     * @param   currentName
     *          current up to 15 character long team name start
     * @param   id
     *          current character to check as 16th character
     * @return  first available full team name
     */
    private @NotNull String checkTeamName(@NonNull TabPlayer p, @NonNull StringBuilder currentName, int id) {
        String potentialTeamName = currentName.toString() + (char)id;
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (all == p) continue;
            if (shortTeamNames.get(all) != null && shortTeamNames.get(all).equals(potentialTeamName)) {
                return checkTeamName(p, currentName, id+1);
            }
        }
        if (redis != null) {
            for (RedisPlayer all : redis.getRedisPlayers().values()) {
                assert redis.getRedisTeams() != null;
                if (redis.getRedisTeams().getTeamNames().get(all).equals(potentialTeamName)) {
                    return checkTeamName(p, currentName, id+1);
                }
            }
        }
        return potentialTeamName;
    }
    
    /**
     * Converts sorting types into user-friendly sorting types into /tab debug
     *
     * @return  user-friendly representation of sorting types
     */
    public @NotNull String typesToString() {
        return Arrays.stream(usedSortingTypes).map(Object::toString).collect(Collectors.joining(" -> "));
    }

    public String getShortTeamName(@NonNull TabPlayer p) {
        TeamManager teams = TAB.getInstance().getTeamManager();
        String forced = teams == null ? null : teams.getForcedTeamName(p);
        if (forced != null) return forced;
        return shortTeamNames.get(p);
    }

    public @NotNull String getFullTeamName(@NonNull TabPlayer p) {
        return fullTeamNames.get(p);
    }

    public @NotNull String getTeamNameNote(@NonNull TabPlayer p) {
        return teamNameNotes.get(p);
    }

    public void setTeamNameNote(@NonNull TabPlayer p, @NonNull String note) {
        teamNameNotes.put(p, note);
    }
}