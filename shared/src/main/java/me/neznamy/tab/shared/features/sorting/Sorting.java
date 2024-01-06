package me.neznamy.tab.shared.features.sorting;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.api.tablist.SortingManager;
import me.neznamy.tab.shared.Limitations;
import me.neznamy.tab.shared.features.types.JoinListener;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.Refreshable;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.layout.LayoutManagerImpl;
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
public class Sorting extends TabFeature implements SortingManager, JoinListener, Loadable, Refreshable {

    @Getter private final String featureName = "Team name refreshing";
    @Getter private final String refreshDisplayName = "Updating team name";

    private NameTag nameTags;
    private LayoutManagerImpl layout;
    private RedisSupport redis;
    
    //map of all registered sorting types
    private final Map<String, BiFunction<Sorting, String, SortingType>> types = new LinkedHashMap<>();
    
    //if sorting is case-sensitive or not
    @Getter private final boolean caseSensitiveSorting = config().getBoolean("scoreboard-teams.case-sensitive-sorting", true);
    
    //active sorting types
    private final SortingType[] usedSortingTypes;

    //team names and notes
    private final WeakHashMap<TabPlayer, String> shortTeamNames = new WeakHashMap<>();
    private final WeakHashMap<TabPlayer, String> fullTeamNames = new WeakHashMap<>();
    private final WeakHashMap<TabPlayer, String> teamNameNotes = new WeakHashMap<>();
    private final WeakHashMap<me.neznamy.tab.api.TabPlayer, String> forcedTeamName = new WeakHashMap<>();
    
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
        usedSortingTypes = compile(config().getStringList("scoreboard-teams.sorting-types", new ArrayList<>()));
    }
    
    @Override
    public void refresh(@NotNull TabPlayer p, boolean force) {
        String previousShortName = shortTeamNames.get(p);
        constructTeamNames(p);
        if (!shortTeamNames.get(p).equals(previousShortName)) {
            if (nameTags != null && getForcedTeamName(p) == null && !nameTags.hasTeamHandlingPaused(p) && !nameTags.getDisableChecker().isDisabledPlayer(p)) {
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
        nameTags = TAB.getInstance().getNameTagManager();
        layout = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.LAYOUT);
        redis = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.REDIS_BUNGEE);
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            constructTeamNames(all);
        }
    }
    
    @Override
    public void onJoin(@NotNull TabPlayer connectedPlayer) {
        constructTeamNames(connectedPlayer);
    }
    
    /**
     * Compiles sorting type list into classes
     *
     * @return  array of compiled sorting types
     */
    private @NotNull SortingType[] compile(@NotNull List<String> options) {
        List<SortingType> list = new ArrayList<>();
        for (String element : options) {
            String[] arr = element.split(":");
            if (!types.containsKey(arr[0].toUpperCase())) {
                TAB.getInstance().getConfigHelper().startup().invalidSortingTypeElement(arr[0].toUpperCase(), types.keySet());
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
    public void constructTeamNames(@NotNull TabPlayer p) {
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
        if (shortName.length() >= Limitations.TEAM_NAME_LENGTH) {
            shortName.setLength(Limitations.TEAM_NAME_LENGTH-1);
        }
        String finalShortName = checkTeamName(p, shortName, 'A');
        shortTeamNames.put(p, finalShortName);
        fullTeamNames.put(p, fullName.append(finalShortName.charAt(finalShortName.length() - 1)).toString());

        // Do not randomly override note
        if (forcedTeamName.get(p) != null) {
            teamNameNotes.put(p, "Set using API");
        }
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
    private @NotNull String checkTeamName(@NotNull TabPlayer p, @NotNull StringBuilder currentName, int id) {
        String potentialTeamName = currentName.toString() + (char)id;
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (all == p) continue;
            if (shortTeamNames.get(all) != null && shortTeamNames.get(all).equals(potentialTeamName)) {
                return checkTeamName(p, currentName, id+1);
            }
        }
        if (redis != null && redis.getRedisTeams() != null) {
            for (RedisPlayer all : redis.getRedisPlayers().values()) {
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
        return Arrays.stream(usedSortingTypes).map(SortingType::getDisplayName).collect(Collectors.joining(" -> "));
    }

    /**
     * Returns short team name of specified player.
     *
     * @param   p
     *          Player to get short team name of
     * @return  short team name of specified player
     */
    @NotNull
    public String getShortTeamName(@NotNull TabPlayer p) {
        String forced = getForcedTeamName(p);
        if (forced != null) return forced;
        return shortTeamNames.get(p);
    }

    /**
     * Returns full team name of specified player.
     *
     * @param   p
     *          Player to get full team name of
     * @return  full team name of specified player
     */
    @NotNull
    public String getFullTeamName(@NotNull TabPlayer p) {
        return fullTeamNames.get(p);
    }

    /**
     * Returns team name note for player.
     *
     * @param   p
     *          Player to get team name note of
     * @return  team name note for player
     */
    @NotNull
    public String getTeamNameNote(@NotNull TabPlayer p) {
        return teamNameNotes.get(p);
    }

    /**
     * Sets team note for player.
     *
     * @param   p
     *          Player to set team name note of
     * @param   note
     *          Team name note
     */
    public void setTeamNameNote(@NotNull TabPlayer p, @NotNull String note) {
        teamNameNotes.put(p, note);
    }

    @Override
    public void forceTeamName(@NonNull me.neznamy.tab.api.TabPlayer player, String name) {
        if (Objects.equals(forcedTeamName.get(player), name)) return;
        if (name != null && name.length() > Limitations.TEAM_NAME_LENGTH) throw new IllegalArgumentException("Team name cannot be more than 16 characters long.");
        if (name != null) setTeamNameNote((TabPlayer) player, "Set using API");
        NameTag nametag = TAB.getInstance().getNameTagManager();
        if (nametag != null) nametag.unregisterTeam((TabPlayer) player, getShortTeamName((TabPlayer) player));
        forcedTeamName.put(player, name);
        if (nametag != null) nametag.registerTeam((TabPlayer) player);
        if (layout != null) layout.updateTeamName((TabPlayer) player, fullTeamNames.get(player));
        if (redis != null && nametag != null) redis.updateTeam((TabPlayer) player, getShortTeamName((TabPlayer) player),
                ((TabPlayer) player).getProperty(TabConstants.Property.TAGPREFIX).get(),
                ((TabPlayer) player).getProperty(TabConstants.Property.TAGSUFFIX).get(),
                (nametag.getTeamVisibility((TabPlayer) player, (TabPlayer) player) ? Scoreboard.NameVisibility.ALWAYS : Scoreboard.NameVisibility.NEVER));
    }

    @Override
    public String getForcedTeamName(@NonNull me.neznamy.tab.api.TabPlayer player) {
        return forcedTeamName.get(player);
    }

    @Override
    public @NotNull String getOriginalTeamName(@NonNull me.neznamy.tab.api.TabPlayer player) {
        return shortTeamNames.get((TabPlayer) player);
    }
}