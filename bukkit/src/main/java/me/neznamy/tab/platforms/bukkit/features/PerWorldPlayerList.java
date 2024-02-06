package me.neznamy.tab.platforms.bukkit.features;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lombok.Getter;
import me.neznamy.tab.platforms.bukkit.BukkitUtils;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.UnLoadable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.TAB;
import org.jetbrains.annotations.NotNull;

/**
 * Per-world-PlayerList feature handler
 */
@SuppressWarnings("deprecation")
public class PerWorldPlayerList extends TabFeature implements Listener, Loadable, UnLoadable {

    @Getter private final String featureName = "Per world PlayerList";

    /** Config options */
    private final boolean allowBypass = config().getBoolean("per-world-playerlist.allow-bypass-permission", false);
    private final List<String> ignoredWorlds = config().getStringList("per-world-playerlist.ignore-effect-in-worlds", Arrays.asList("ignored_world", "build"));
    private final Map<String, List<String>> sharedWorlds = config().getConfigurationSection("per-world-playerlist.shared-playerlist-world-groups");

    /**
     * Constructs new instance and registers events
     *
     * @param   plugin
     *          Plugin instance to register events
     */
    public PerWorldPlayerList(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void load() {
        for (Player p : BukkitUtils.getOnlinePlayers()) {
            checkPlayer(p);
        }
    }

    @Override
    public void unload() {
        for (Player p : BukkitUtils.getOnlinePlayers()) {
            for (Player pl : BukkitUtils.getOnlinePlayers()) {
                p.showPlayer(pl);
            }
        }
        HandlerList.unregisterAll(this);
    }

    /**
     * Join event listener to synchronously perform hiding.
     *
     * @param   e
     *          Join event
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        long time = System.nanoTime();
        checkPlayer(e.getPlayer());
        TAB.getInstance().getCPUManager().addTime(featureName, TabConstants.CpuUsageCategory.PLAYER_JOIN, System.nanoTime()-time);
    }

    /**
     * World change listener to synchronously perform player showing/hiding.
     *
     * @param   e
     *          World change event
     */
    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        long time = System.nanoTime();
        checkPlayer(e.getPlayer());
        TAB.getInstance().getCPUManager().addTime(featureName, TabConstants.CpuUsageCategory.WORLD_SWITCH, System.nanoTime()-time);
    }

    /**
     * Performs visibility check on the player. Shows players this player should see and does not,
     * hides players the player should not see, shows the player to those who should see player and hides
     * from those, who should not see.
     *
     * @param   p
     *          Player to update
     */
    private void checkPlayer(@NotNull Player p) {
        for (Player all : BukkitUtils.getOnlinePlayers()) {
            if (all == p) continue;
            if (!shouldSee(p, all) && p.canSee(all)) p.hidePlayer(all);
            if (shouldSee(p, all) && !p.canSee(all)) p.showPlayer(all);
            if (!shouldSee(all, p) && all.canSee(p)) all.hidePlayer(p);
            if (shouldSee(all, p) && !all.canSee(p)) all.showPlayer(p);
        }
    }

    /**
     * Returns {@code true} if viewer should see target player, {@code false} if not.
     * @param   viewer
     *          Player viewing the TabList
     * @param   target
     *          Target displayed in the TabList
     * @return  {@code true} if viewer should see target, {@code false} if not.
     */
    private boolean shouldSee(@NotNull Player viewer, @NotNull Player target) {
        if (target == viewer) return true;
        if ((allowBypass && viewer.hasPermission(TabConstants.Permission.PER_WORLD_PLAYERLIST_BYPASS)) || ignoredWorlds.contains(viewer.getWorld().getName())) return true;
        String viewerWorldGroup = viewer.getWorld().getName() + "-default"; //preventing unwanted behavior when some group is called exactly like a world
        String targetWorldGroup = target.getWorld().getName() + "-default";
        for (Entry<String, List<String>> group : sharedWorlds.entrySet()) {
            if (group.getValue() != null) {
                if (group.getValue().contains(viewer.getWorld().getName())) viewerWorldGroup = group.getKey();
                if (group.getValue().contains(target.getWorld().getName())) targetWorldGroup = group.getKey();
            }
        }
        return viewerWorldGroup.equals(targetWorldGroup);
    }
}