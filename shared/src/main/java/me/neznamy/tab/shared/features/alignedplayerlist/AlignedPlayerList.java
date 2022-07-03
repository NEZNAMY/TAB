package me.neznamy.tab.shared.features.alignedplayerlist;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.PlayerList;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Additional code for PlayerList class to secure alignment
 */
public class AlignedPlayerList extends PlayerList {

    private final Map<TabPlayer, PlayerView> playerViews = new HashMap<>();
    private final byte[] widths = loadWidths();

    public AlignedPlayerList() {
        TAB.getInstance().getPlaceholderManager().addUsedPlaceholders(Collections.singletonList(TabConstants.Placeholder.VANISHED));
    }

    /**
     * Loads widths from included widths.txt file as well as width overrides from config
     */
    private byte[] loadWidths() {
        byte[] widths = new byte[65536];
        InputStream file = getClass().getClassLoader().getResourceAsStream("widths.txt");
        if (file == null) {
            TAB.getInstance().getErrorManager().criticalError("Failed to load widths.txt file. Is it inside the jar? Aligned suffix will not work.", null);
            return widths;
        }
        int characterId = 1;
        for (String line : new BufferedReader(new InputStreamReader(file)).lines().collect(Collectors.toList())) {
            widths[characterId++] = (byte) Float.parseFloat(line);
        }
        Map<Integer, Integer> widthOverrides = TAB.getInstance().getConfiguration().getConfig().getConfigurationSection("tablist-name-formatting.character-width-overrides");
        for (Entry<Integer, Integer> entry : widthOverrides.entrySet()) {
            widths[entry.getKey()] = entry.getValue().byteValue();
        }
        return widths;
    }

    @Override
    public void load() {
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            updateProperties(all);
            playerViews.put(all, new PlayerView(this, all));
            if (isDisabled(all.getServer(), all.getWorld())) {
                addDisabledPlayer(all);
            }
        }
        playerViews.values().forEach(PlayerView::load);
    }

    @Override
    public void onJoin(TabPlayer connectedPlayer) {
        updateProperties(connectedPlayer);
        playerViews.put(connectedPlayer, new PlayerView(this, connectedPlayer));
        playerViews.values().forEach(v -> v.playerJoin(connectedPlayer));
        if (isDisabled(connectedPlayer.getServer(), connectedPlayer.getWorld())) {
            addDisabledPlayer(connectedPlayer);
            return;
        }
        if (connectedPlayer.getVersion().getMinorVersion() < 8) return;
        Runnable r = () -> {
            playerViews.get(connectedPlayer).load();
            refresh(connectedPlayer, false);
        };
        r.run();
        //add packet might be sent after tab's refresh packet, resending again when anti-override is disabled
        if (!antiOverrideTabList || !TAB.getInstance().getFeatureManager().isFeatureEnabled(TabConstants.Feature.PIPELINE_INJECTION) ||
                connectedPlayer.getVersion().getMinorVersion() == 8)
            TAB.getInstance().getCPUManager().runTaskLater(300, this, TabConstants.CpuUsageCategory.PLAYER_JOIN, r);
    }

    @Override
    public void onQuit(TabPlayer p) {
        super.onQuit(p);
        playerViews.values().forEach(v -> v.processPlayerQuit(p));
        playerViews.remove(p);
    }

    @Override
    public void onWorldChange(TabPlayer p, String from, String to) {
        super.onWorldChange(p, from, to);
        playerViews.values().forEach(v -> v.worldChange(p));
    }

    @Override
    public void refresh(TabPlayer refreshed, boolean force) {
        if (isDisabledPlayer(refreshed)) return;
        boolean refresh;
        if (force) {
            updateProperties(refreshed);
            refresh = true;
        } else {
            boolean prefix = refreshed.getProperty(TabConstants.Property.TABPREFIX).update();
            boolean name = refreshed.getProperty(TabConstants.Property.CUSTOMTABNAME).update();
            boolean suffix = refreshed.getProperty(TabConstants.Property.TABSUFFIX).update();
            refresh = prefix || name || suffix;
        }
        if (refresh) {
            playerViews.values().forEach(v -> v.updatePlayer(refreshed));
        }
    }

    @Override
    public IChatBaseComponent getTabFormat(TabPlayer p, TabPlayer viewer) {
        if (!playerViews.containsKey(viewer)) return null;
        return playerViews.get(viewer).formatName(p);
    }

    @Override
    public void onVanishStatusChange(TabPlayer player) {
        playerViews.values().forEach(v -> v.onVanishChange(player));
    }

    public byte[] getWidths() {
        return widths;
    }
}