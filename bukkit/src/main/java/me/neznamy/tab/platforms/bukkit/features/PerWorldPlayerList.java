package me.neznamy.tab.platforms.bukkit.features;

import me.neznamy.tab.platforms.bukkit.platform.BukkitPlatform;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.PerWorldPlayerListConfiguration;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.features.types.UnLoadable;
import me.neznamy.tab.shared.features.types.VanishListener;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map.Entry;

/**
 * Per-world-PlayerList feature handler
 */
@SuppressWarnings("deprecation")
public class PerWorldPlayerList extends TabFeature implements Listener, Loadable, UnLoadable, VanishListener {

    /** Reference to platform */
    @NotNull
    private final BukkitPlatform platform;

    /** Config options */
    @NotNull
    private final PerWorldPlayerListConfiguration configuration;

    /** Check for presence of modern (1.12.2+) methods that take plugin as argument to avoid conflict */
    private final boolean modernMethodsAvailable = ReflectionUtils.methodExists(Player.class, "hidePlayer", Plugin.class, Player.class);

    /**
     * Constructs new instance and registers events.
     *
     * @param   plugin
     *          Plugin instance to register events
     * @param   platform
     *          Platform reference
     * @param   configuration
     *          Feature configuration
     */
    public PerWorldPlayerList(@NotNull JavaPlugin plugin, @NotNull BukkitPlatform platform, @NotNull PerWorldPlayerListConfiguration configuration) {
        this.configuration = configuration;
        this.platform = platform;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void load() {
        for (Player p : platform.getOnlinePlayers()) {
            checkPlayer(p);
        }
    }

    @Override
    public void unload() {
        for (Player p : platform.getOnlinePlayers()) {
            for (Player pl : platform.getOnlinePlayers()) {
                showPlayer(p, pl);
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
        TAB.getInstance().getCPUManager().addTime(getFeatureName(), TabConstants.CpuUsageCategory.PLAYER_JOIN, System.nanoTime()-time);
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
        TAB.getInstance().getCPUManager().addTime(getFeatureName(), TabConstants.CpuUsageCategory.WORLD_SWITCH, System.nanoTime()-time);
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
        for (Player all : platform.getOnlinePlayers()) {
            if (all == p) continue;
            if (shouldSee(p, all)) {
                showPlayer(p, all);
            } else {
                hidePlayer(p, all);
            }
            if (shouldSee(all, p)) {
                showPlayer(all, p);
            } else {
                hidePlayer(all, p);
            }
        }
    }

    private void hidePlayer(@NotNull Player viewer, @NotNull Player target) {
        if (modernMethodsAvailable) {
            viewer.hidePlayer(platform.getPlugin(), target);
        } else {
            viewer.hidePlayer(target);
        }
    }

    private void showPlayer(@NotNull Player viewer, @NotNull Player target) {
        if (modernMethodsAvailable) {
            viewer.showPlayer(platform.getPlugin(), target);
        } else {
            viewer.showPlayer(target);
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
        for (MetadataValue v : target.getMetadata("vanished")) {
            if (v.asBoolean() && !viewer.hasPermission(TabConstants.Permission.SEE_VANISHED)) return false;
        }
        if ((configuration.isAllowBypassPermission() && viewer.hasPermission(TabConstants.Permission.PER_WORLD_PLAYERLIST_BYPASS)) || configuration.getIgnoredWorlds().contains(viewer.getWorld().getName())) return true;
        String viewerWorldGroup = viewer.getWorld().getName() + "-default"; //preventing unwanted behavior when some group is called exactly like a world
        String targetWorldGroup = target.getWorld().getName() + "-default";
        for (Entry<String, List<String>> group : configuration.getSharedWorlds().entrySet()) {
            if (group.getValue() != null) {
                if (group.getValue().contains(viewer.getWorld().getName())) viewerWorldGroup = group.getKey();
                if (group.getValue().contains(target.getWorld().getName())) targetWorldGroup = group.getKey();
            }
        }
        return viewerWorldGroup.equals(targetWorldGroup);
    }

    @NotNull
    @Override
    public String getFeatureName() {
        return "Per world PlayerList";
    }

    @Override
    public void onVanishStatusChange(@NotNull TabPlayer player) {
        platform.runSync((Player) player.getPlayer(), () -> checkPlayer((Player) player.getPlayer()));
    }
}