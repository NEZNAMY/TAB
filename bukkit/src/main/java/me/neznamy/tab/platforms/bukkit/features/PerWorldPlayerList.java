package me.neznamy.tab.platforms.bukkit.features;

import me.neznamy.tab.platforms.bukkit.platform.BukkitPlatform;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.data.World;
import me.neznamy.tab.shared.features.PerWorldPlayerListConfiguration;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map.Entry;

/**
 * Per-world-PlayerList feature handler
 */
@SuppressWarnings("deprecation")
public class PerWorldPlayerList extends TabFeature implements Loadable, UnLoadable, JoinListener, WorldSwitchListener, VanishListener {

    /** Reference to platform */
    @NotNull
    private final BukkitPlatform platform;

    /** Config options */
    @NotNull
    private final PerWorldPlayerListConfiguration configuration;

    @NotNull
    private final DisableChecker disableChecker;

    /** Check for presence of modern (1.12.2+) methods that take plugin as argument to avoid conflict */
    private final boolean modernMethodsAvailable = ReflectionUtils.methodExists(Player.class, "hidePlayer", Plugin.class, Player.class);

    /**
     * Constructs new instance and registers the disable-condition checker.
     *
     * @param   platform
     *          Platform reference
     * @param   configuration
     *          Feature configuration
     */
    public PerWorldPlayerList(@NotNull BukkitPlatform platform, @NotNull PerWorldPlayerListConfiguration configuration) {
        this.configuration = configuration;
        this.platform = platform;
        Condition disableCondition = TAB.getInstance().getPlaceholderManager().getConditionManager().getByNameOrExpression(configuration.getDisableCondition());
        disableChecker = new DisableChecker(this, disableCondition, this::onDisableConditionChange,
                p -> p.perWorldPlayerListData.disabled);
        TAB.getInstance().getFeatureManager().registerFeature("PerWorldPlayerList-Condition", disableChecker);
    }

    @Override
    public void load() {
        TabPlayer[] players = TAB.getInstance().getOnlinePlayers();
        for (TabPlayer player : players) {
            player.perWorldPlayerListData.disabled.set(disableChecker.isDisableConditionMet(player));
        }
        for (TabPlayer player : players) {
            checkPlayer(player);
        }
    }

    @Override
    public void unload() {
        try {
            for (Player viewer : platform.getOnlinePlayers()) {
                platform.runSync(viewer, () -> {
                    if (!viewer.isOnline()) return;
                    for (Player target : platform.getOnlinePlayers()) {
                        if (target != viewer) showPlayer(viewer, target);
                    }
                });
            }
        } catch (IllegalPluginAccessException e) {
            // Server shutting down, doesn't matter if we don't show players to each other
        }
    }

    /**
     * Initializes the player's condition state and performs visibility checks.
     */
    @Override
    public void onJoin(@NotNull TabPlayer player) {
        player.perWorldPlayerListData.disabled.set(disableChecker.isDisableConditionMet(player));
        checkPlayer(player);
    }

    @Override
    public void onWorldChange(@NotNull TabPlayer changed, @NotNull World from, @NotNull World to) {
        checkPlayer(changed);
    }

    /**
     * Performs visibility check on the player. Shows players this player should see and does not,
     * hides players the player should not see, shows the player to those who should see player and hides
     * from those, who should not see.
     *
     * @param   player
     *          Player to update
     */
    private void checkPlayer(@NotNull TabPlayer player) {
        Player changed = (Player) player.getPlayer();
        platform.runSync(changed, () -> {
            if (!changed.isOnline()) return;
            for (Player other : platform.getOnlinePlayers()) {
                if (other == changed) continue;
                updateVisibility(changed, other);
                platform.runSync(other, () -> updateVisibility(other, changed));
            }
        });
    }

    private void updateVisibility(@NotNull Player viewer, @NotNull Player target) {
        if (!viewer.isOnline() || !target.isOnline()) return;
        if (shouldSee(viewer, target)) {
            showPlayer(viewer, target);
        } else {
            hidePlayer(viewer, target);
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
        TabPlayer tabViewer = TAB.getInstance().getPlayer(viewer.getUniqueId());
        if (tabViewer != null && tabViewer.perWorldPlayerListData.disabled.get()) return true;
        if (configuration.isAllowBypassPermission() && viewer.hasPermission(TabConstants.Permission.PER_WORLD_PLAYERLIST_BYPASS)) return true;
        return computeWorldGroup(viewer).equals(computeWorldGroup(target));
    }

    @NotNull
    private String computeWorldGroup(@NotNull Player p) {
        for (Entry<String, List<String>> group : configuration.getSharedWorlds().entrySet()) {
            List<String> worlds = group.getValue();
            if (worlds == null) continue;
            for (String world : worlds) {
                if (TAB.getInstance().getDataManager().matchesPattern(p.getWorld().getName(), world)) {
                    return group.getKey();
                }
            }
        }
        return p.getWorld().getName() + "-default"; //preventing unwanted behavior when some group is called exactly like a world
    }

    @NotNull
    @Override
    public String getFeatureName() {
        return "Per world PlayerList";
    }

    @Override
    public void onVanishStatusChange(@NotNull TabPlayer player) {
        checkPlayer(player);
    }

    private void onDisableConditionChange(@NotNull TabPlayer player, boolean disabledNow) {
        checkPlayer(player);
    }
}