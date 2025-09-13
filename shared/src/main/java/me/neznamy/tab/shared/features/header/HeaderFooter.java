package me.neznamy.tab.shared.features.header;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.tablist.HeaderFooterManager;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.data.Server;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.cache.StringToComponentCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Feature handler for header and footer.
 */
@RequiredArgsConstructor
public class HeaderFooter extends RefreshableFeature implements HeaderFooterManager, JoinListener, Loadable, UnLoadable,
        ServerSwitchListener, CustomThreaded {

    private final StringToComponentCache headerCache = new StringToComponentCache("Header", 1000);
    private final StringToComponentCache footerCache = new StringToComponentCache("Footer", 1000);
    @Getter private final ThreadExecutor customThread = new ThreadExecutor("TAB Header/Footer Thread");
    private final Map<String, HeaderFooterDesign> registeredDesigns = new LinkedHashMap<>();
    private HeaderFooterDesign[] definedDesigns;

    @NonNull private final HeaderFooterConfiguration configuration;

    @Override
    public void load() {
        for (Map.Entry<String, HeaderFooterConfiguration.HeaderFooterDesignDefinition> entry : configuration.getDesigns().entrySet()) {
            String designName = entry.getKey();
            HeaderFooterDesign design = new HeaderFooterDesign(this, designName, entry.getValue());
            registeredDesigns.put(designName, design);
            TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.design(designName), design);
        }
        definedDesigns = registeredDesigns.values().toArray(new HeaderFooterDesign[0]);
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            onJoin(p);
        }
    }

    @Override
    public void unload() {
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            if (p.headerFooterData.activeDesign == null) continue;
            sendHeaderFooter(p, "","");
        }
    }

    @Override
    public void onJoin(@NotNull TabPlayer connectedPlayer) {
        sendHighestDesign(connectedPlayer);
    }

    private void sendHighestDesign(@NotNull TabPlayer player) {
        HeaderFooterDesign highest = detectHighestDesign(player);
        HeaderFooterDesign current = player.headerFooterData.activeDesign;
        if (highest != current) {
            if (highest != null) {
                highest.sendTo(player);
            } else {
                sendHeaderFooter(player, "", "");
            }
        }
    }

    @Nullable
    private HeaderFooterDesign detectHighestDesign(@NonNull TabPlayer p) {
        for (HeaderFooterDesign design : definedDesigns) {
            if (design.isConditionMet(p)) return design;
        }
        return null;
    }

    @Override
    public void onServerChange(@NotNull TabPlayer p, @NotNull Server from, @NotNull Server to) {
        // Velocity clears header/footer on server switch
        sendHighestDesign(p);
    }

    @NotNull
    @Override
    public String getRefreshDisplayName() {
        return "Switching designs";
    }

    @Override
    public void refresh(@NotNull TabPlayer p, boolean force) {
        sendHighestDesign(p);
    }

    /**
     * Sends header and footer to player.
     *
     * @param   player
     *          Player to send header and footer to
     * @param   header
     *          Header to send
     * @param   footer
     *          Footer to send
     */
    public void sendHeaderFooter(@NotNull TabPlayer player, @NotNull String header, @NotNull String footer) {
        player.getTabList().setPlayerListHeaderFooter(headerCache.get(header), footerCache.get(footer));
    }

    /**
     * Sends header and footer to player based on currently active design or forced
     * header/footer set by the API.
     *
     * @param   player
     *          Player to send header and footer to
     */
    public void sendHeaderFooter(@NotNull TabPlayer player) {
        String header;
        String footer;
        if (player.headerFooterData.forcedHeader != null) {
            header = player.headerFooterData.forcedHeader.updateAndGet();
        } else if (player.headerFooterData.activeDesign != null) {
            Property prop = player.headerFooterData.headerProperties.get(player.headerFooterData.activeDesign);
            if (prop == null) {
                prop = new Property(player.headerFooterData.activeDesign, player, String.join("\n", player.headerFooterData.activeDesign.getDefinition().getHeader()));
                player.headerFooterData.headerProperties.put(player.headerFooterData.activeDesign, prop);
            }
            header = prop.updateAndGet();
        } else {
            header = "";
        }
        if (player.headerFooterData.forcedFooter != null) {
            footer = player.headerFooterData.forcedFooter.updateAndGet();
        } else if (player.headerFooterData.activeDesign != null) {
            Property prop = player.headerFooterData.footerProperties.get(player.headerFooterData.activeDesign);
            if (prop == null) {
                prop = new Property(player.headerFooterData.activeDesign, player, String.join("\n", player.headerFooterData.activeDesign.getDefinition().getFooter()));
                player.headerFooterData.footerProperties.put(player.headerFooterData.activeDesign, prop);
            }
            footer = prop.updateAndGet();
        } else {
            footer = "";
        }
        sendHeaderFooter(player, header, footer);
    }

    // ------------------
    // API Implementation
    // ------------------

    @Override
    public void setHeader(@NotNull me.neznamy.tab.api.TabPlayer p, @Nullable String header) {
        ensureActive();
        customThread.execute(() -> {
            TabPlayer player = (TabPlayer) p;
            if (header != null) {
                player.headerFooterData.forcedHeader = new Property(this, player, header);
            } else {
                player.headerFooterData.forcedHeader = null;
            }
            sendHeaderFooter(player);
        });
    }

    @Override
    public void setFooter(@NotNull me.neznamy.tab.api.TabPlayer p, @Nullable String footer) {
        ensureActive();
        customThread.execute(() -> {
            TabPlayer player = (TabPlayer) p;
            if (footer != null) {
                player.headerFooterData.forcedFooter = new Property(this, player, footer);
            } else {
                player.headerFooterData.forcedFooter = null;
            }
            sendHeaderFooter(player);
        });
    }

    @Override
    public void setHeaderAndFooter(@NotNull me.neznamy.tab.api.TabPlayer p, @Nullable String header, @Nullable String footer) {
        ensureActive();
        customThread.execute(() -> {
            TabPlayer player = (TabPlayer) p;
            if (header != null) {
                player.headerFooterData.forcedHeader = new Property(this, player, header);
            } else {
                player.headerFooterData.forcedHeader = null;
            }
            if (footer != null) {
                player.headerFooterData.forcedFooter = new Property(this, player, footer);
            } else {
                player.headerFooterData.forcedFooter = null;
            }
            sendHeaderFooter(player);
        });
    }

    @NotNull
    @Override
    public String getFeatureName() {
        return "Header/Footer";
    }

    /**
     * Class holding header/footer data for players.
     */
    public static class PlayerData {

        /** Forced header using the API */
        @Nullable
        public Property forcedHeader;

        /** Forced footer using the API */
        @Nullable
        public Property forcedFooter;

        /** Currently active design */
        @Nullable
        public HeaderFooterDesign activeDesign;

        /** Map of header properties for each design */
        public final Map<HeaderFooterDesign, Property> headerProperties = new IdentityHashMap<>();

        /** Map of footer properties for each design */
        public final Map<HeaderFooterDesign, Property> footerProperties = new IdentityHashMap<>();
    }
}
