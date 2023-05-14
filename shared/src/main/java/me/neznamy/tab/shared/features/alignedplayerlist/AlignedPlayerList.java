package me.neznamy.tab.shared.features.alignedplayerlist;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.features.types.QuitListener;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PlayerList;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Additional code for PlayerList class to secure alignment
 */
public class AlignedPlayerList extends PlayerList implements QuitListener {

    private final Map<TabPlayer, PlayerView> playerViews = new HashMap<>();
    @Getter private final byte[] widths = loadWidths();
    @Getter private final Map<String, Integer> multiCharWidths = loadMultiCharWidths();

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
        Map<Object, Integer> widthOverrides = TAB.getInstance().getConfiguration().getConfig().getConfigurationSection("tablist-name-formatting.character-width-overrides");
        List<Integer> redundant = new ArrayList<>();
        for (Entry<Object, Integer> entry : widthOverrides.entrySet()) {
            if (entry.getKey() instanceof Integer) {
                Integer key = (Integer) entry.getKey();
                if (widths[key] == entry.getValue().byteValue()) {
                    redundant.add(key);
                } else {
                    widths[key] = entry.getValue().byteValue();
                }
            }
        }
        redundant.forEach(widthOverrides::remove);
        if (!redundant.isEmpty()) TAB.getInstance().getConfig().save();
        return widths;
    }

    private Map<String, Integer> loadMultiCharWidths() {
        Map<String, Integer> multiCharWidths = new HashMap<>();
        Map<Object, Integer> widthOverrides = TAB.getInstance().getConfiguration().getConfig().getConfigurationSection("tablist-name-formatting.character-width-overrides");
        for (Entry<Object, Integer> entry : widthOverrides.entrySet()) {
            if (entry.getKey() instanceof String) {
                multiCharWidths.put((String) entry.getKey(), entry.getValue());
            }
        }
        return multiCharWidths;
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
    public void onJoin(@NonNull TabPlayer connectedPlayer) {
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
    public void onQuit(@NonNull TabPlayer p) {
        playerViews.values().forEach(v -> v.processPlayerQuit(p));
        playerViews.remove(p);
    }

    @Override
    public void onWorldChange(@NonNull TabPlayer p, @NonNull String from, @NonNull String to) {
        super.onWorldChange(p, from, to);
        playerViews.values().forEach(v -> v.worldChange(p));
    }

    @Override
    public void refresh(@NonNull TabPlayer refreshed, boolean force) {
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
    public IChatBaseComponent getTabFormat(@NonNull TabPlayer p, @NonNull TabPlayer viewer) {
        PlayerView view = playerViews.get(viewer);
        if (view == null) return null;
        return view.formatName(p);
    }

    @Override
    public void onVanishStatusChange(@NonNull TabPlayer player) {
        playerViews.values().forEach(v -> v.onVanishChange(player));
    }
}