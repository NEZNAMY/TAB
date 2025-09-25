package me.neznamy.tab.shared.features.sorting;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.api.tablist.SortingManager;
import me.neznamy.tab.shared.Limitations;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.layout.LayoutManagerImpl;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.features.sorting.types.*;
import me.neznamy.tab.shared.features.types.JoinListener;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Class for handling player sorting rules
 */
public class Sorting extends RefreshableFeature implements SortingManager, JoinListener, Loadable {

    private NameTag nameTags;
    private LayoutManagerImpl layout;
    private ProxySupport proxy;
    
    //map of all registered sorting types
    private final Map<String, BiFunction<Sorting, String, SortingType>> types = new LinkedHashMap<>();

    @Getter
    @NotNull
    private final SortingConfiguration configuration;
    
    //active sorting types
    private final SortingType[] usedSortingTypes;
    
    /**
     * Constructs new instance.
     *
     * @param   configuration
     *          Feature configuration
     */
    public Sorting(@NotNull SortingConfiguration configuration) {
        this.configuration = configuration;
        types.put("GROUPS", Groups::new);
        types.put("PERMISSIONS", Permissions::new);
        types.put("PLACEHOLDER", (sorting, value) -> {
            Placeholder.PlaceholderSplitResult split = Placeholder.splitValue(value);
            return split == null ? null : new Placeholder(sorting, split);
        });
        types.put("PLACEHOLDER_A_TO_Z", PlaceholderAtoZ::new);
        types.put("PLACEHOLDER_Z_TO_A", PlaceholderZtoA::new);
        types.put("PLACEHOLDER_LOW_TO_HIGH", PlaceholderLowToHigh::new);
        types.put("PLACEHOLDER_HIGH_TO_LOW", PlaceholderHighToLow::new);
        usedSortingTypes = compile(configuration.getSortingTypes());
    }

    @NotNull
    @Override
    public String getRefreshDisplayName() {
        return "Updating team names";
    }

    @Override
    public void refresh(@NotNull TabPlayer p, boolean force) {
        String previousShortName = p.sortingData.shortTeamName;
        constructTeamNames(p);
        if (!p.sortingData.shortTeamName.equals(previousShortName)) {
            if (nameTags != null) nameTags.updateTeamName(p, p.sortingData.getShortTeamName());
            if (layout != null) layout.updateTeamName(p, p.sortingData.getFullTeamName());
        }
    }

    @Override
    public void load() {
        // All of these features are instantiated after this one, so they must be detected later
        nameTags = TAB.getInstance().getNameTagManager();
        layout = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.LAYOUT);
        proxy = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.PROXY_SUPPORT);
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            onJoin(all);
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
                SortingType type = types.get(arr[0].toUpperCase()).apply(this, arr.length == 1 ? "" : element.substring(arr[0].length() + 1));
                if (type != null) {
                    list.add(type);
                }
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
        p.sortingData.teamNameNote = "";
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
        String finalShortName = checkTeamName(p, shortName);
        p.sortingData.shortTeamName = finalShortName;
        p.sortingData.fullTeamName = fullName.append(finalShortName.charAt(finalShortName.length() - 1)).toString();

        // Do not randomly override note
        if (p.sortingData.forcedTeamName != null) {
            p.sortingData.teamNameNote = "Set using API";
        }
    }

    /**
     * Checks if team name is available and proceeds to try new values until free name is found
     *
     * @param   p
     *          player to build team name for
     * @param   currentName
     *          current up to 15 character long team name start
     * @return  first available full team name
     */
    @NotNull
    private String checkTeamName(@NotNull TabPlayer p, @NotNull StringBuilder currentName) {
        char id = 'A';
        while (true) {
            String potentialTeamName = currentName.toString() + id;
            boolean nameTaken = false;
            for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                if (all == p) continue;
                if (potentialTeamName.equals(all.sortingData.shortTeamName)) {
                    nameTaken = true;
                    break;
                }
            }
            if (!nameTaken && proxy != null && nameTags != null) {
                for (ProxyPlayer all : proxy.getProxyPlayers().values()) {
                    if (all.getNametag() != null && potentialTeamName.equals(all.getNametag().getResolvedTeamName())) {
                        nameTaken = true;
                        break;
                    }
                }
            }
            if (!nameTaken) {
                return potentialTeamName;
            }
            id++;
        }
    }
    
    /**
     * Converts sorting types into user-friendly sorting types into /tab debug
     *
     * @return  user-friendly representation of sorting types
     */
    @NotNull
    public String typesToString() {
        return Arrays.stream(usedSortingTypes).map(SortingType::getDisplayName).collect(Collectors.joining(" -> "));
    }

    @NotNull
    @Override
    public String getFeatureName() {
        return "Sorting";
    }

    // ------------------
    // API Implementation
    // ------------------

    @Override
    public void forceTeamName(@NonNull me.neznamy.tab.api.TabPlayer player, String name) {
        ensureActive();
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();
        if (Objects.equals(p.sortingData.forcedTeamName, name)) return;
        if (name != null) {
            if (name.length() > Limitations.TEAM_NAME_LENGTH) throw new IllegalArgumentException("Team name cannot be more than 16 characters long.");
            p.sortingData.teamNameNote = "Set using API";
        }
        p.sortingData.forcedTeamName = name;
        if (layout != null) layout.updateTeamName(p, p.sortingData.getFullTeamName());
        if (nameTags != null) nameTags.updateTeamName(p, p.sortingData.getShortTeamName());
    }

    @Override
    @Nullable
    public String getForcedTeamName(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        return ((TabPlayer)player).sortingData.forcedTeamName;
    }

    @Override
    @NotNull
    public String getOriginalTeamName(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        ((TabPlayer)player).ensureLoaded();
        return ((TabPlayer)player).sortingData.shortTeamName;
    }
}
