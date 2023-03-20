package me.neznamy.tab.shared.features;

import lombok.Getter;
import me.neznamy.tab.api.HeaderFooterManager;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.feature.*;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.shared.TAB;

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

    public HeaderFooter() {
        super("header-footer");
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
            if (isDisabledPlayer(p) || p.getVersion().getMinorVersion() < 8) continue;
            sendHeaderFooter(p, "","");
        }
    }

    @Override
    public void onJoin(TabPlayer connectedPlayer) {
        if (isDisabled(connectedPlayer.getServer(), connectedPlayer.getWorld())) {
            addDisabledPlayer(connectedPlayer);
            return;
        }
        refresh(connectedPlayer, true);
    }

    @Override
    public void onServerChange(TabPlayer p, String from, String to) {
        onWorldChange(p, null, null);
    }

    @Override
    public void onWorldChange(TabPlayer p, String from, String to) {
        boolean disabledBefore = isDisabledPlayer(p);
        boolean disabledNow = false;
        if (isDisabled(p.getServer(), p.getWorld())) {
            disabledNow = true;
            addDisabledPlayer(p);
        } else {
            removeDisabledPlayer(p);
        }
        if (p.getVersion().getMinorVersion() < 8) return;
        if (disabledNow) {
            if (!disabledBefore) sendHeaderFooter(p, "", "");
        } else {
            boolean refresh = p.setProperty(this, TabConstants.Property.HEADER, getProperty(p, TabConstants.Property.HEADER));
            if (p.setProperty(this, TabConstants.Property.FOOTER, getProperty(p, TabConstants.Property.FOOTER))) {
                refresh = true;
            }
            if (refresh || disabledBefore) {
                sendHeaderFooter(p, p.getProperty(TabConstants.Property.HEADER).get(), p.getProperty(TabConstants.Property.FOOTER).get());
            }
        }
    }

    @Override
    public void refresh(TabPlayer p, boolean force) {
        if (force) {
            p.setProperty(this, TabConstants.Property.HEADER, getProperty(p, TabConstants.Property.HEADER));
            p.setProperty(this, TabConstants.Property.FOOTER, getProperty(p, TabConstants.Property.FOOTER));
        }
        if (isDisabledPlayer(p) || p.getVersion().getMinorVersion() < 8) return;
        sendHeaderFooter(p, p.getProperty(TabConstants.Property.HEADER).updateAndGet(), p.getProperty(TabConstants.Property.FOOTER).updateAndGet());
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
        sendHeaderFooter(player, IChatBaseComponent.optimizedComponent(header), IChatBaseComponent.optimizedComponent(footer));
    }
    
    private void sendHeaderFooter(TabPlayer player, IChatBaseComponent header, IChatBaseComponent footer) {
        player.setPlayerListHeaderFooter(header, footer);
    }

    @Override
    public void setHeader(TabPlayer player, String header) {
        player.getProperty(TabConstants.Property.HEADER).setTemporaryValue(header);
        sendHeaderFooter(player, player.getProperty(TabConstants.Property.HEADER).updateAndGet(), player.getProperty(TabConstants.Property.FOOTER).updateAndGet());
    }

    @Override
    public void setFooter(TabPlayer player, String footer) {
        player.getProperty(TabConstants.Property.FOOTER).setTemporaryValue(footer);
        sendHeaderFooter(player, player.getProperty(TabConstants.Property.HEADER).updateAndGet(), player.getProperty(TabConstants.Property.FOOTER).updateAndGet());
    }

    @Override
    public void setHeaderAndFooter(TabPlayer player, String header, String footer) {
        player.getProperty(TabConstants.Property.HEADER).setTemporaryValue(header);
        player.getProperty(TabConstants.Property.FOOTER).setTemporaryValue(footer);
        sendHeaderFooter(player, player.getProperty(TabConstants.Property.HEADER).updateAndGet(), player.getProperty(TabConstants.Property.FOOTER).updateAndGet());
    }

    @Override
    public void resetHeader(TabPlayer player) {
        player.getProperty(TabConstants.Property.HEADER).setTemporaryValue(null);
        sendHeaderFooter(player, player.getProperty(TabConstants.Property.HEADER).updateAndGet(), player.getProperty(TabConstants.Property.FOOTER).updateAndGet());
    }

    @Override
    public void resetFooter(TabPlayer player) {
        player.getProperty(TabConstants.Property.FOOTER).setTemporaryValue(null);
        sendHeaderFooter(player, player.getProperty(TabConstants.Property.HEADER).updateAndGet(), player.getProperty(TabConstants.Property.FOOTER).updateAndGet());
    }

    @Override
    public void resetHeaderAndFooter(TabPlayer player) {
        player.getProperty(TabConstants.Property.HEADER).setTemporaryValue(null);
        player.getProperty(TabConstants.Property.FOOTER).setTemporaryValue(null);
        sendHeaderFooter(player, player.getProperty(TabConstants.Property.HEADER).updateAndGet(), player.getProperty(TabConstants.Property.FOOTER).updateAndGet());
    }
}