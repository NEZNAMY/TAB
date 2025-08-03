package me.neznamy.tab.shared.features.header;

import lombok.Getter;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.tab.api.tablist.HeaderFooterManager;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.data.Server;
import me.neznamy.tab.shared.data.World;
import me.neznamy.tab.shared.features.header.HeaderFooterConfiguration.HeaderFooterPair;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.cache.StringToComponentCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Feature handler for header and footer.
 */
public class HeaderFooter extends RefreshableFeature implements HeaderFooterManager, JoinListener, Loadable, UnLoadable,
        WorldSwitchListener, ServerSwitchListener, CustomThreaded, GroupListener {

    private final StringToComponentCache headerCache = new StringToComponentCache("Header", 1000);
    private final StringToComponentCache footerCache = new StringToComponentCache("Footer", 1000);
    @Getter private final ThreadExecutor customThread = new ThreadExecutor("TAB Header/Footer Thread");
    private final HeaderFooterConfiguration configuration;
    private final DisableChecker disableChecker;

    /**
     * Constructs new instance and registers disable condition checker to feature manager.
     *
     * @param   configuration
     *          Feature configuration
     */
    public HeaderFooter(@NotNull HeaderFooterConfiguration configuration) {
        this.configuration = configuration;
        disableChecker = new DisableChecker(this, Condition.getCondition(configuration.getDisableCondition()), this::onDisableConditionChange, p -> p.headerFooterData.disabled);
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.HEADER_FOOTER + "-Condition", disableChecker);
    }

    @Override
    public void load() {
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            onJoin(p);
        }
    }

    @Override
    public void unload() {
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            if (p.headerFooterData.disabled.get()) continue;
            sendHeaderFooter(p, "","");
        }
    }

    @Override
    public void onJoin(@NotNull TabPlayer connectedPlayer) {
        connectedPlayer.headerFooterData.header = new Property(this, connectedPlayer, getFromConfig(connectedPlayer, "header"));
        connectedPlayer.headerFooterData.footer = new Property(this, connectedPlayer, getFromConfig(connectedPlayer, "footer"));
        if (disableChecker.isDisableConditionMet(connectedPlayer)) {
            connectedPlayer.headerFooterData.disabled.set(true);
        } else {
            sendHeaderFooter(connectedPlayer, connectedPlayer.headerFooterData.header.get(), connectedPlayer.headerFooterData.footer.get());
        }
    }

    @Override
    public void onServerChange(@NotNull TabPlayer p, @NotNull Server from, @NotNull Server to) {
        // Velocity clears header/footer on server switch, resend regardless of whether values changed or not
        updateProperties(p);
        sendHeaderFooter(p, p.headerFooterData.header.get(), p.headerFooterData.footer.get());
    }

    @Override
    public void onWorldChange(@NotNull TabPlayer p, @NotNull World from, @NotNull World to) {
        if (updateProperties(p)) {
            sendHeaderFooter(p, p.headerFooterData.header.get(), p.headerFooterData.footer.get());
        }
    }

    @NotNull
    @Override
    public String getRefreshDisplayName() {
        return "Updating header/footer";
    }

    @Override
    public void refresh(@NotNull TabPlayer p, boolean force) {
        sendHeaderFooter(p, p.headerFooterData.header.updateAndGet(), p.headerFooterData.footer.updateAndGet());
    }

    @Override
    public void onGroupChange(@NotNull TabPlayer player) {
        if (updateProperties(player)) {
            sendHeaderFooter(player, player.headerFooterData.header.get(), player.headerFooterData.footer.get());
        }
    }

    /**
     * Loads all properties from config and returns {@code true} if at least
     * one of them either wasn't loaded or changed value, {@code false} otherwise.
     *
     * @param   player
     *          Player to update properties of
     * @return  {@code true} if at least one property changed, {@code false} if not
     */
    private boolean updateProperties(@NotNull TabPlayer player) {
        boolean changed = player.headerFooterData.header.changeRawValue(getFromConfig(player, "header"), null);
        if (player.headerFooterData.footer.changeRawValue(getFromConfig(player, "footer"), null)) changed = true;
        return changed;
    }

    /**
     * Processes disable condition change.
     *
     * @param   p
     *          Player who the condition has changed for
     * @param   disabledNow
     *          Whether the feature is disabled now or not
     */
    public void onDisableConditionChange(TabPlayer p, boolean disabledNow) {
        if (disabledNow) {
            p.getTabList().setPlayerListHeaderFooter(TabComponent.empty(), TabComponent.empty());
        } else {
            sendHeaderFooter(p, p.headerFooterData.header.get(), p.headerFooterData.footer.get());
        }
    }

    private String getFromConfig(TabPlayer p, String property) {
        String[] value = TAB.getInstance().getConfiguration().getUsers().getProperty(p.getName(), property, p.server, p.world);
        if (value.length > 0) {
            return value[0];
        }
        value = TAB.getInstance().getConfiguration().getUsers().getProperty(p.getUniqueId().toString(), property, p.server, p.world);
        if (value.length > 0) {
            return value[0];
        }
        value = TAB.getInstance().getConfiguration().getGroups().getProperty(p.getGroup(), property, p.server, p.world);
        if (value.length > 0) {
            return value[0];
        }
        List<String> lines = null;
        HeaderFooterPair pair = configuration.getPerWorld().get(TAB.getInstance().getConfiguration().getGroup(configuration.getPerWorld().keySet(), p.world.getName()));
        if (pair != null) {
            lines = property.equals("header") ? pair.getHeader() : pair.getFooter();
        }
        if (lines == null) {
            pair = configuration.getPerServer().get(TAB.getInstance().getConfiguration().getGroup(configuration.getPerServer().keySet(), p.server.getName()));
            if (pair != null) {
                lines = property.equals("header") ? pair.getHeader() : pair.getFooter();
            }
        }

        if (lines == null) {
            lines = property.equals("header") ? configuration.getHeader() : configuration.getFooter();
        }
        return String.join("\n", lines);
    }

    private void sendHeaderFooter(TabPlayer player, String header, String footer) {
        if (player.headerFooterData.disabled.get()) return;
        player.getTabList().setPlayerListHeaderFooter(headerCache.get(header), footerCache.get(footer));
    }

    // ------------------
    // API Implementation
    // ------------------

    @Override
    public void setHeader(@NotNull me.neznamy.tab.api.TabPlayer p, @Nullable String header) {
        ensureActive();
        customThread.execute(() -> {
            TabPlayer player = (TabPlayer) p;
            player.headerFooterData.header.setTemporaryValue(header);
            sendHeaderFooter(player, player.headerFooterData.header.updateAndGet(), player.headerFooterData.footer.updateAndGet());
        });
    }

    @Override
    public void setFooter(@NotNull me.neznamy.tab.api.TabPlayer p, @Nullable String footer) {
        ensureActive();
        customThread.execute(() -> {
            TabPlayer player = (TabPlayer) p;
            player.headerFooterData.footer.setTemporaryValue(footer);
            sendHeaderFooter(player, player.headerFooterData.header.updateAndGet(), player.headerFooterData.footer.updateAndGet());
        });
    }

    @Override
    public void setHeaderAndFooter(@NotNull me.neznamy.tab.api.TabPlayer p, @Nullable String header, @Nullable String footer) {
        ensureActive();
        customThread.execute(() -> {
            TabPlayer player = (TabPlayer) p;
            player.headerFooterData.header.setTemporaryValue(header);
            player.headerFooterData.footer.setTemporaryValue(footer);
            sendHeaderFooter(player, player.headerFooterData.header.updateAndGet(), player.headerFooterData.footer.updateAndGet());
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

        /** Player's header */
        public Property header;

        /** Player's footer */
        public Property footer;

        /** Flag tracking whether this feature is disabled for the player with condition or not */
        public final AtomicBoolean disabled = new AtomicBoolean();
    }
}
