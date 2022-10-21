package me.neznamy.tab.shared.features.sorting;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.team.TeamManager;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.api.TabConstants;
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

/**
 * Class for handling player sorting rules
 */
public class Sorting extends TabFeature {

    private final NameTag nameTags;
    
    //map of all registered sorting types
    private final Map<String, BiFunction<Sorting, String, SortingType>> types = new LinkedHashMap<>();
    
    //if sorting is case-sensitive or not
    private final boolean caseSensitiveSorting = TAB.getInstance().getConfiguration().getConfig().getBoolean("scoreboard-teams.case-sensitive-sorting", true);
    
    //active sorting types
    private final SortingType[] usedSortingTypes;

    //team names and notes
    private final WeakHashMap<TabPlayer, String> shortTeamNames = new WeakHashMap<>();
    private final WeakHashMap<TabPlayer, String> fullTeamNames = new WeakHashMap<>();
    private final WeakHashMap<TabPlayer, String> teamNameNotes = new WeakHashMap<>();
    
    /**
     * Constructs new instance, loads data from configuration and starts repeating task
     *
     * @param   nameTags
     *          NameTag feature
     */
    public Sorting(NameTag nameTags) {
        super("Team name refreshing", "Refreshing team name");
        this.nameTags = nameTags;
        types.put("GROUPS", Groups::new);
        types.put("PERMISSIONS", Permissions::new);
        types.put("PLACEHOLDER", Placeholder::new);
        types.put("PLACEHOLDER_A_TO_Z", PlaceholderAtoZ::new);
        types.put("PLACEHOLDER_Z_TO_A", PlaceholderZtoA::new);
        types.put("PLACEHOLDER_LOW_TO_HIGH", PlaceholderLowToHigh::new);
        types.put("PLACEHOLDER_HIGH_TO_LOW", PlaceholderHighToLow::new);
        usedSortingTypes = compile(TAB.getInstance().getConfiguration().getConfig().getStringList("scoreboard-teams.sorting-types", new ArrayList<>()));
    }
    
    @Override
    public void refresh(TabPlayer p, boolean force) {
        if (nameTags != null && (nameTags.getForcedTeamName(p) != null || nameTags.hasTeamHandlingPaused(p))) return;
        String previousShortName = shortTeamNames.get(p);
        constructTeamNames(p);
        if (!shortTeamNames.get(p).equals(previousShortName)) {
            if (nameTags != null) nameTags.unregisterTeam(p);
            LayoutManager layout = (LayoutManager) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.LAYOUT);
            if (layout != null) layout.updateTeamName(p, fullTeamNames.get(p));
            if (nameTags != null) nameTags.registerTeam(p);
        }
    }
    
    @Override
    public void load(){
        if (nameTags != null) return; //handled by NameTag feature
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            constructTeamNames(all);
        }
    }
    
    @Override
    public void onJoin(TabPlayer connectedPlayer) {
        if (nameTags != null) return; //handled by NameTag feature
        constructTeamNames(connectedPlayer);
    }
    
    /**
     * Compiles sorting type list into classes
     *
     * @return  list of compiled sorting types
     */
    private SortingType[] compile(List<String> options){
        List<SortingType> list = new ArrayList<>();
        for (String element : options) {
            String[] arr = element.split(":");
            if (!types.containsKey(arr[0].toUpperCase())) {
                TAB.getInstance().getErrorManager().startupWarn("\"&e" + arr[0].toUpperCase() + "&c\" is not a valid sorting type element. Valid options are: &e" + types.keySet() + ".");
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
    public void constructTeamNames(TabPlayer p) {
        teamNameNotes.put(p, "");
        StringBuilder shortName = new StringBuilder();
        for (SortingType type : usedSortingTypes) {
            shortName.append(type.getChars((ITabPlayer) p));
        }
        StringBuilder fullName = new StringBuilder(shortName);
        if (TAB.getInstance().getFeatureManager().isFeatureEnabled(TabConstants.Feature.LAYOUT)) {
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
    private String checkTeamName(TabPlayer p, StringBuilder currentName, int id) {
        String potentialTeamName = currentName.toString();
        if (!caseSensitiveSorting) potentialTeamName = potentialTeamName.toLowerCase();
        potentialTeamName += (char)id;
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (all == p) continue;
            if (shortTeamNames.get(all) != null && shortTeamNames.get(all).equals(potentialTeamName)) {
                return checkTeamName(p, currentName, id+1);
            }
        }
        if (TAB.getInstance().getFeatureManager().isFeatureEnabled(TabConstants.Feature.REDIS_BUNGEE)) {
            RedisSupport redis = (RedisSupport) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.REDIS_BUNGEE);
            for (RedisPlayer all : redis.getRedisPlayers().values()) {
                if (all.getTeamName() != null && all.getTeamName().equals(potentialTeamName)) {
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
    public String typesToString() {
        return Arrays.stream(usedSortingTypes).map(Object::toString).collect(Collectors.joining(" then "));
    }

    public String getShortTeamName(TabPlayer p) {
        TeamManager teams = TAB.getInstance().getTeamManager();
        if (teams != null && teams.getForcedTeamName(p) != null) return teams.getForcedTeamName(p);
        return shortTeamNames.get(p);
    }

    public String getFullTeamName(TabPlayer p) {
        return fullTeamNames.get(p);
    }

    public String getTeamNameNote(TabPlayer p) {
        return teamNameNotes.get(p);
    }

    public void setTeamNameNote(TabPlayer p, String note) {
        teamNameNotes.put(p, note);
    }
}