package me.neznamy.tab.shared.features;

import lombok.Getter;
import me.neznamy.tab.api.tablist.HeaderFooterManager;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.SimpleComponent;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Feature handler for header and footer.
 */
public class HeaderFooter extends RefreshableFeature implements HeaderFooterManager, JoinListener, Loadable, UnLoadable,
        WorldSwitchListener, ServerSwitchListener, CustomThreaded {

    @Getter
    private final ScheduledExecutorService customThread = TAB.getInstance().getCpu().newExecutor("TAB Header/Footer Thread");
    private final String HEADER = "header";
    private final String FOOTER = "footer";
    private final List<Object> worldGroups = new ArrayList<>(config().getConfigurationSection("header-footer.per-world").keySet());
    private final List<Object> serverGroups = new ArrayList<>(config().getConfigurationSection("header-footer.per-server").keySet());
    private final DisableChecker disableChecker;

    /**
     * Constructs new instance and registers disable condition checker to feature manager.
     */
    public HeaderFooter() {
        super("Header/Footer", "Updating header/footer");
        Condition disableCondition = Condition.getCondition(config().getString("header-footer.disable-condition"));
        disableChecker = new DisableChecker(getFeatureName(), disableCondition, this::onDisableConditionChange, p -> p.headerFooterData.disabled);
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.HEADER_FOOTER + "-Condition", disableChecker);
        TAB.getInstance().getConfigHelper().hint().checkHeaderFooterForRedundancy(config().getConfigurationSection("header-footer"));
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
        connectedPlayer.headerFooterData.header = new Property(this, connectedPlayer, getProperty(connectedPlayer, HEADER));
        connectedPlayer.headerFooterData.footer = new Property(this, connectedPlayer, getProperty(connectedPlayer, FOOTER));
        if (disableChecker.isDisableConditionMet(connectedPlayer)) {
            connectedPlayer.headerFooterData.disabled.set(true);
        } else {
            sendHeaderFooter(connectedPlayer, connectedPlayer.headerFooterData.header.get(), connectedPlayer.headerFooterData.footer.get());
        }
    }

    @Override
    public void onServerChange(@NotNull TabPlayer p, @NotNull String from, @NotNull String to) {
        // Velocity clears header/footer on server switch, resend regardless of whether values changed or not
        refresh(p, true);
    }

    @Override
    public void onWorldChange(@NotNull TabPlayer p, @NotNull String from, @NotNull String to) {
        if (p.headerFooterData.setHeaderFooter(getProperty(p, HEADER), getProperty(p, FOOTER))) {
            sendHeaderFooter(p, p.headerFooterData.header.get(), p.headerFooterData.footer.get());
        }
    }

    @Override
    public void refresh(@NotNull TabPlayer p, boolean force) {
        if (force) {
            p.headerFooterData.setHeaderFooter(getProperty(p, HEADER), getProperty(p, FOOTER));
        }
        sendHeaderFooter(p, p.headerFooterData.header.updateAndGet(), p.headerFooterData.footer.updateAndGet());
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
            p.getTabList().setPlayerListHeaderFooter(new SimpleComponent(""), new SimpleComponent(""));
        } else {
            sendHeaderFooter(p, p.headerFooterData.header.get(), p.headerFooterData.footer.get());
        }
    }

    private String getProperty(TabPlayer p, String property) {
        String append = getFromConfig(p, property + "append");
        if (!append.isEmpty()) append = "\n" + append;
        return getFromConfig(p, property) + append;
    }

    private String getFromConfig(TabPlayer p, String property) {
        String[] value = TAB.getInstance().getConfiguration().getUsers().getProperty(p.getName(), property, p.getServer(), p.getWorld());
        if (value.length > 0) {
            return value[0];
        }
        value = TAB.getInstance().getConfiguration().getUsers().getProperty(p.getUniqueId().toString(), property, p.getServer(), p.getWorld());
        if (value.length > 0) {
            return value[0];
        }
        value = TAB.getInstance().getConfiguration().getGroups().getProperty(p.getGroup(), property, p.getServer(), p.getWorld());
        if (value.length > 0) {
            return value[0];
        }
        List<String> lines = config().getStringList("header-footer.per-world." + TAB.getInstance().getConfiguration().getGroup(worldGroups, p.getWorld()) + "." + property);
        if (lines == null) {
            lines = config().getStringList("header-footer.per-server." + TAB.getInstance().getConfiguration().getServerGroup(serverGroups, p.getServer()) + "." + property);
        }
        if (lines == null) {
            lines = config().getStringList("header-footer." + property);
        }
        if (lines == null) lines = new ArrayList<>();
        return String.join("\n", lines);
    }

    private void sendHeaderFooter(TabPlayer player, String header, String footer) {
        if (player.headerFooterData.disabled.get()) return;
        player.getTabList().setPlayerListHeaderFooter(TabComponent.optimized(header), TabComponent.optimized(footer));
    }

    // ------------------
    // API Implementation
    // ------------------

    @Override
    public void setHeader(@NotNull me.neznamy.tab.api.TabPlayer p, @Nullable String header) {
        ensureActive();
        customThread.submit(() -> {
            TabPlayer player = (TabPlayer) p;
            player.headerFooterData.header.setTemporaryValue(header);
            sendHeaderFooter(player, player.headerFooterData.header.updateAndGet(), player.headerFooterData.footer.updateAndGet());
        });
    }

    @Override
    public void setFooter(@NotNull me.neznamy.tab.api.TabPlayer p, @Nullable String footer) {
        ensureActive();
        customThread.submit(() -> {
            TabPlayer player = (TabPlayer) p;
            player.headerFooterData.footer.setTemporaryValue(footer);
            sendHeaderFooter(player, player.headerFooterData.header.updateAndGet(), player.headerFooterData.footer.updateAndGet());
        });
    }

    @Override
    public void setHeaderAndFooter(@NotNull me.neznamy.tab.api.TabPlayer p, @Nullable String header, @Nullable String footer) {
        ensureActive();
        customThread.submit(() -> {
            TabPlayer player = (TabPlayer) p;
            player.headerFooterData.header.setTemporaryValue(header);
            player.headerFooterData.footer.setTemporaryValue(footer);
            sendHeaderFooter(player, player.headerFooterData.header.updateAndGet(), player.headerFooterData.footer.updateAndGet());
        });
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

        /**
         * Sets header and footer to new values and returns {@code true} if some property's
         * raw value has changed, {@code false} if not.
         *
         * @param   rawHeader
         *          New raw header to use
         * @param   rawFooter
         *          New raw footer to use
         * @return  Whether at least one raw value changed or not

         */
        public boolean setHeaderFooter(@NotNull String rawHeader, @NotNull String rawFooter) {
            boolean changed = header.changeRawValue(rawHeader, null);
            if (footer.changeRawValue(rawFooter, null)) changed = true;
            return changed;
        }
    }
}
