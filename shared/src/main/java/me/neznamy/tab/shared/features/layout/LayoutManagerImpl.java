package me.neznamy.tab.shared.features.layout;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.api.tablist.layout.Layout;
import me.neznamy.tab.api.tablist.layout.LayoutManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.config.files.config.LayoutConfiguration;
import me.neznamy.tab.shared.config.files.config.LayoutConfiguration.LayoutDefinition;
import me.neznamy.tab.shared.features.PingSpoof;
import me.neznamy.tab.shared.features.PlayerList;
import me.neznamy.tab.shared.features.layout.skin.SkinManager;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Map.Entry;

@Getter
public class LayoutManagerImpl extends RefreshableFeature implements LayoutManager, JoinListener, QuitListener, VanishListener, Loadable,
        UnLoadable, TabListClearListener {

    private final LayoutConfiguration configuration;
    private final SkinManager skinManager;
    private final Map<Integer, UUID> uuids = new HashMap<>();
    private final Map<String, LayoutPattern> layouts = new LinkedHashMap<>();
    private final Map<TabPlayer, String> sortedPlayers = Collections.synchronizedMap(new TreeMap<>(Comparator.comparing(p -> p.layoutData.sortingString)));
    private PlayerList playerList;
    private PingSpoof pingSpoof;
    @Getter private static boolean teamsEnabled;

    /**
     * Constructs new instance.
     *
     * @param   configuration
     *          Feature configuration
     */
    public LayoutManagerImpl(@NotNull LayoutConfiguration configuration) {
        super("Layout", "Switching layouts");
        this.configuration = configuration;
        skinManager = new SkinManager(configuration.defaultSkin, configuration.defaultSkinHashMap);
        for (int slot=1; slot<=80; slot++) {
            uuids.put(slot, new UUID(0, configuration.direction.translateSlot(slot)));
        }
        for (Entry<String, LayoutDefinition> entry : configuration.layouts.entrySet()) {
            LayoutPattern pattern = new LayoutPattern(this, entry.getKey(), entry.getValue());
            layouts.put(pattern.getName(), pattern);
            TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.layout(entry.getKey()), pattern);
        }
    }

    @Override
    public void load() {
        playerList = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.PLAYER_LIST);
        pingSpoof = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.PING_SPOOF);
        teamsEnabled = TAB.getInstance().getNameTagManager() != null;
        if (pingSpoof == null) TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.LAYOUT_LATENCY, new LayoutLatencyRefresher());
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            onJoin(p);
        }
    }

    @Override
    public void onJoin(@NotNull TabPlayer p) {
        p.layoutData.sortingString = p.sortingData.fullTeamName;
        sortedPlayers.put(p, p.sortingData.fullTeamName);
        LayoutPattern highest = getHighestLayout(p);
        if (highest != null) {
            LayoutView view = new LayoutView(this, highest, p);
            view.send();
            p.layoutData.view = view;
        }
        tickAllLayouts();

        // Unformat original entries for players who can see a layout to avoid spaces due to unparsed placeholders and such
        if (highest == null) return;
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            p.getTabList().updateDisplayName(all.getTablistId(), null);
        }
    }

    @Override
    public void onQuit(@NotNull TabPlayer p) {
        sortedPlayers.remove(p);
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (all == p) continue;
            if (all.layoutData.view != null) all.layoutData.view.tick();
        }
    }

    @Override
    public void refresh(@NotNull TabPlayer p, boolean force) {
        LayoutPattern highest = getHighestLayout(p);
        String highestName = highest == null ? null : highest.getName();
        LayoutView current = p.layoutData.view;
        String currentName = current == null ? null : current.getPattern().getName();
        if (!Objects.equals(highestName, currentName)) {
            if (current != null) current.destroy();
            p.layoutData.view = null;
            if (highest != null) {
                LayoutView view = new LayoutView(this, highest, p);
                view.send();
                p.layoutData.view = view;
            }
        }
    }

    @Override
    public void unload() {
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            if (p.getVersion().getMinorVersion() < 8 || p.isBedrockPlayer()) continue;
            for (UUID id : uuids.values()) {
                p.getTabList().removeEntry(id);
            }
        }
    }

    @Override
    public void onVanishStatusChange(@NotNull TabPlayer p) {
        tickAllLayouts();
    }

    private @Nullable LayoutPattern getHighestLayout(@NotNull TabPlayer p) {
        if (p.layoutData.forcedLayout != null) return p.layoutData.forcedLayout;
        for (LayoutPattern pattern : layouts.values()) {
            if (pattern.isConditionMet(p)) return pattern;
        }
        return null;
    }

    public @NotNull UUID getUUID(int slot) {
        return uuids.get(slot);
    }

    public void updateTeamName(@NotNull TabPlayer p, @NotNull String teamName) {
        sortedPlayers.remove(p);
        p.layoutData.sortingString = teamName;
        sortedPlayers.put(p, teamName);
        tickAllLayouts();
    }

    @Override
    public void onTabListClear(@NotNull TabPlayer player) {
        if (player.layoutData.view != null) player.layoutData.view.send();
    }

    /**
     * Ticks layouts for all players.
     */
    public void tickAllLayouts() {
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (all.layoutData.view != null) all.layoutData.view.tick();
        }
    }

    // ------------------
    // API Implementation
    // ------------------

    @Override
    public Layout createNewLayout(String name) {
        ensureActive();
        return new LayoutPattern(this, name, new LayoutDefinition(null, Collections.emptyList(), new LinkedHashMap<>()));
    }

    @Override
    public void sendLayout(@NonNull me.neznamy.tab.api.TabPlayer player, @Nullable Layout layout) {
        ensureActive();
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();
        p.layoutData.forcedLayout = (LayoutPattern) layout;
        refresh(p, false);
    }

    @Override
    public void resetLayout(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();
        p.layoutData.forcedLayout = null;
        refresh(p, false);
    }

    /**
     * Class storing layout data for players.
     */
    public static class PlayerData {

        /** Merged string to sort players by */
        public String sortingString;

        /** Layout the player can currently see */
        @Nullable
        public LayoutView view;

        /** Layout forced via API */
        @Nullable
        public LayoutPattern forcedLayout;
    }
}