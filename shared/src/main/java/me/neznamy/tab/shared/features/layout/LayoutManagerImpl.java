package me.neznamy.tab.shared.features.layout;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.tablist.layout.Layout;
import me.neznamy.tab.api.tablist.layout.LayoutManager;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.layout.LayoutConfiguration.LayoutDefinition;
import me.neznamy.tab.shared.features.layout.impl.FakeEntryLayout;
import me.neznamy.tab.shared.features.layout.impl.LayoutBase;
import me.neznamy.tab.shared.features.layout.impl.common.FixedSlot;
import me.neznamy.tab.shared.features.layout.pattern.LayoutPattern;
import me.neznamy.tab.shared.features.pingspoof.PingSpoof;
import me.neznamy.tab.shared.features.playerlist.PlayerList;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Map.Entry;

@Getter
public class LayoutManagerImpl extends RefreshableFeature implements LayoutManager, JoinListener, QuitListener, VanishListener, Loadable,
        UnLoadable, TabListClearListener, Dumpable {

    private final LayoutConfiguration configuration;
    private final LayoutSkinManager skinManager;
    private final UUID[] uuids = new UUID[80];
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
        this.configuration = configuration;
        skinManager = new LayoutSkinManager(TAB.getInstance().getConfiguration().getSkinManager(), configuration.getDefaultSkin(), configuration.getDefaultSkinHashMap());
        for (int slot=1; slot<=80; slot++) {
            uuids[slot-1] = new UUID(0, configuration.getDirection().translateSlot(slot));
        }
        for (Entry<String, LayoutDefinition> entry : configuration.getLayouts().entrySet()) {
            LayoutPattern pattern = new LayoutPattern(this, entry.getValue());
            layouts.put(pattern.getName(), pattern);
            TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.layout(entry.getKey()), pattern);
        }
    }

    @Override
    public void load() {
        playerList = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.PLAYER_LIST);
        pingSpoof = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.PING_SPOOF);
        teamsEnabled = TAB.getInstance().getNameTagManager() != null && TAB.getInstance().getPlatform().supportsScoreboards();
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
            sendLayout(p, highest);
        }
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (all.layoutData.currentLayout != null) all.layoutData.currentLayout.view.onJoin(p);
        }

        // Unformat original entries for players who can see a layout to avoid spaces due to unparsed placeholders and such
        if (highest == null) return;
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            p.getTabList().updateDisplayName(all, null);
        }
    }

    @Override
    public void onQuit(@NotNull TabPlayer p) {
        sortedPlayers.remove(p);
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (all == p) continue;
            if (all.layoutData.currentLayout != null) all.layoutData.currentLayout.view.tick();
        }
    }

    @NotNull
    @Override
    public String getRefreshDisplayName() {
        return "Switching layouts";
    }

    @Override
    public void refresh(@NotNull TabPlayer p, boolean force) {
        LayoutPattern highest = getHighestLayout(p);
        LayoutPattern current = p.layoutData.currentLayout == null ? null : p.layoutData.currentLayout.view.getPattern();
        if (highest != current) {
            if (current != null) p.layoutData.currentLayout.view.destroy();
            p.layoutData.currentLayout = null;
            if (highest != null) {
                sendLayout(p, highest);
            }
        }
    }

    private void sendLayout(@NotNull TabPlayer player, @NotNull LayoutPattern pattern) {
        LayoutBase view = new FakeEntryLayout(this, pattern, player);
        player.layoutData.currentLayout = new LayoutData(view);
        view.send();
    }

    @Override
    public void unload() {
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            if (p.layoutData.currentLayout != null) {
                p.layoutData.currentLayout.view.destroy();
            }
        }
    }

    @Override
    public void onVanishStatusChange(@NotNull TabPlayer p) {
        tickAllLayouts();
    }

    @Nullable
    private LayoutPattern getHighestLayout(@NotNull TabPlayer p) {
        if (p.getVersion().getMinorVersion() < 8 || p.isBedrockPlayer()) return null; // Ignore these players entirely
        if (p.layoutData.forcedLayout != null) return p.layoutData.forcedLayout;
        for (LayoutPattern pattern : layouts.values()) {
            if (pattern.isConditionMet(p)) return pattern;
        }
        return null;
    }

    @NotNull
    public UUID getUUID(int slot) {
        return uuids[slot-1];
    }

    public void updateTeamName(@NotNull TabPlayer p, @NotNull String teamName) {
        sortedPlayers.remove(p);
        p.layoutData.sortingString = teamName;
        sortedPlayers.put(p, teamName);
        tickAllLayouts();
    }

    @Override
    public void onTabListClear(@NotNull TabPlayer player) {
        if (player.layoutData.currentLayout != null) player.layoutData.currentLayout.view.send();
    }

    /**
     * Ticks layouts for all players.
     */
    public void tickAllLayouts() {
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (all.layoutData.currentLayout != null) all.layoutData.currentLayout.view.tick();
        }
    }

    // ------------------
    // API Implementation
    // ------------------

    @Override
    @NotNull
    public Layout createNewLayout(@NonNull String name) {
        ensureActive();
        return new LayoutPattern(this, new LayoutDefinition(name, null, null, 80, Collections.emptyList(), new LinkedHashMap<>()));
    }

    @Override
    @Nullable
    public Layout getLayout(@NonNull String name) {
        return layouts.get(name);
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

    @NotNull
    @Override
    public String getFeatureName() {
        return "Layout";
    }

    @Override
    @NotNull
    public Object dump(@NotNull TabPlayer player) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("configuration", configuration.getSection().getMap());
        map.put("chain", new LinkedHashMap<String, Object>() {{
            for (LayoutPattern pattern : layouts.values()) {
               put(pattern.getName(), pattern.dump(player, player.layoutData.currentLayout == null ? null : player.layoutData.currentLayout.view.getPattern()));
            }
        }});
        if (player.layoutData.currentLayout != null) {
            map.put("currently displayed layout", new LinkedHashMap<String, Object>() {{
                put("name", player.layoutData.currentLayout.view.getPattern().getName());
            }});
        } else {
            map.put("currently displayed layout", null);
        }
        return map;
    }

    /**
     * Class storing layout data for players.
     */
    public static class PlayerData {

        /** Merged string to sort players by */
        public String sortingString;

        /** Layout the player can currently see */
        @Nullable
        public LayoutData currentLayout;

        /** Layout forced via API */
        @Nullable
        public LayoutPattern forcedLayout;
    }

    /**
     * Data about a displayed layout.
     */
    @RequiredArgsConstructor
    public static class LayoutData {

        /** Layout view this data belongs to */
        @NotNull
        public final LayoutBase view;

        /** Player's properties for fixed slot texts */
        @NotNull
        public final Map<FixedSlot, Property> fixedSlotTexts = new IdentityHashMap<>();

        /** Player's properties for fixed slot skins */
        @NotNull
        public final Map<FixedSlot, Property> fixedSlotSkins = new IdentityHashMap<>();

        /** Player's properties for fixed slot ping values */
        @NotNull
        public final Map<FixedSlot, Property> fixedSlotPings = new IdentityHashMap<>();
    }
}