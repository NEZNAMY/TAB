package me.neznamy.tab.shared.features;

import lombok.Getter;
import me.neznamy.tab.api.tablist.HeaderFooterManager;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.features.types.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Feature handler for header and footer
 */
public class HeaderFooter extends TabFeature implements HeaderFooterManager, JoinListener, Loadable, UnLoadable,
        WorldSwitchListener, ServerSwitchListener, Refreshable {

    @Getter private final String featureName = "Header/Footer";
    @Getter private final String refreshDisplayName = "Updating header/footer";
    private final List<Object> worldGroups = new ArrayList<>(TAB.getInstance().getConfig().getConfigurationSection("header-footer.per-world").keySet());
    private final List<Object> serverGroups = new ArrayList<>(TAB.getInstance().getConfig().getConfigurationSection("header-footer.per-server").keySet());
    private final DisableChecker disableChecker;

    public HeaderFooter() {
        Condition disableCondition = Condition.getCondition(TAB.getInstance().getConfig().getString("header-footer.disable-condition"));
        disableChecker = new DisableChecker(featureName, disableCondition, this::onDisableConditionChange);
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.HEADER_FOOTER + "-Condition", disableChecker);
        TAB.getInstance().getMisconfigurationHelper().checkHeaderFooterForRedundancy(TAB.getInstance().getConfig().getConfigurationSection("header-footer"));
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
            if (disableChecker.isDisabledPlayer(p)) continue;
            sendHeaderFooter(p, "","");
        }
    }

    @Override
    public void onJoin(@NotNull TabPlayer connectedPlayer) {
        if (disableChecker.isDisableConditionMet(connectedPlayer)) {
            disableChecker.addDisabledPlayer(connectedPlayer);
        }
        refresh(connectedPlayer, true);
    }

    @Override
    public void onServerChange(@NotNull TabPlayer p, @NotNull String from, @NotNull String to) {
        updateProperties(p);
        // Velocity clears header/footer on server switch, which can be a problem without placeholders that change often
        // Resend immediately instead of the next time a placeholder changes value
        sendHeaderFooter(p, p.getProperty(TabConstants.Property.HEADER).get(), p.getProperty(TabConstants.Property.FOOTER).get());
    }

    @Override
    public void onWorldChange(@NotNull TabPlayer p, @NotNull String from, @NotNull String to) {
        updateProperties(p);
    }

    private void updateProperties(TabPlayer p) {
        boolean refresh = p.setProperty(this, TabConstants.Property.HEADER, getProperty(p, TabConstants.Property.HEADER));
        if (p.setProperty(this, TabConstants.Property.FOOTER, getProperty(p, TabConstants.Property.FOOTER))) {
            refresh = true;
        }
        if (refresh) {
            sendHeaderFooter(p, p.getProperty(TabConstants.Property.HEADER).get(), p.getProperty(TabConstants.Property.FOOTER).get());
        }
    }

    @Override
    public void refresh(@NotNull TabPlayer p, boolean force) {
        if (force) {
            p.setProperty(this, TabConstants.Property.HEADER, getProperty(p, TabConstants.Property.HEADER));
            p.setProperty(this, TabConstants.Property.FOOTER, getProperty(p, TabConstants.Property.FOOTER));
        }
        sendHeaderFooter(p, p.getProperty(TabConstants.Property.HEADER).updateAndGet(), p.getProperty(TabConstants.Property.FOOTER).updateAndGet());
    }

    public void onDisableConditionChange(TabPlayer p, boolean disabledNow) {
        if (disabledNow) {
            if (p.getVersion().getMinorVersion() < 8) return;
            p.getTabList().setPlayerListHeaderFooter(new IChatBaseComponent(""), new IChatBaseComponent(""));
        } else {
            sendHeaderFooter(p, p.getProperty(TabConstants.Property.HEADER).get(), p.getProperty(TabConstants.Property.FOOTER).get());
        }
    }

    private String getProperty(TabPlayer p, String property) {
        String append = getFromConfig(p, property + "append");
        if (append.length() > 0) append = "\n" + append;
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
        List<String> lines = TAB.getInstance().getConfiguration().getConfig().getStringList("header-footer.per-world." + TAB.getInstance().getConfiguration().getGroup(worldGroups, p.getWorld()) + "." + property);
        if (lines == null) {
            lines = TAB.getInstance().getConfiguration().getConfig().getStringList("header-footer.per-server." + TAB.getInstance().getConfiguration().getGroup(serverGroups, p.getServer()) + "." + property);
        }
        if (lines == null) {
             lines = TAB.getInstance().getConfiguration().getConfig().getStringList("header-footer." + property);
        }
        if (lines == null) lines = new ArrayList<>();
        return String.join("\n", lines);
    }

    private void sendHeaderFooter(TabPlayer player, String header, String footer) {
        if (player.getVersion().getMinorVersion() < 8 || disableChecker.isDisabledPlayer(player)) return;
        player.getTabList().setPlayerListHeaderFooter(IChatBaseComponent.optimizedComponent(header), IChatBaseComponent.optimizedComponent(footer));
    }

    @Override
    public void setHeader(@NotNull me.neznamy.tab.api.TabPlayer p, @Nullable String header) {
        TabPlayer player = (TabPlayer) p;
        player.getProperty(TabConstants.Property.HEADER).setTemporaryValue(header);
        sendHeaderFooter(player, player.getProperty(TabConstants.Property.HEADER).updateAndGet(),
                player.getProperty(TabConstants.Property.FOOTER).updateAndGet());
    }

    @Override
    public void setFooter(@NotNull me.neznamy.tab.api.TabPlayer p, @Nullable String footer) {
        TabPlayer player = (TabPlayer) p;
        player.getProperty(TabConstants.Property.FOOTER).setTemporaryValue(footer);
        sendHeaderFooter(player, player.getProperty(TabConstants.Property.HEADER).updateAndGet(),
                player.getProperty(TabConstants.Property.FOOTER).updateAndGet());
    }

    @Override
    public void setHeaderAndFooter(@NotNull me.neznamy.tab.api.TabPlayer p, @Nullable String header, @Nullable String footer) {
        TabPlayer player = (TabPlayer) p;
        player.getProperty(TabConstants.Property.HEADER).setTemporaryValue(header);
        player.getProperty(TabConstants.Property.FOOTER).setTemporaryValue(footer);
        sendHeaderFooter(player, player.getProperty(TabConstants.Property.HEADER).updateAndGet(),
                player.getProperty(TabConstants.Property.FOOTER).updateAndGet());
    }
}