package me.neznamy.tab.platforms.bukkit.platform;

import lombok.NonNull;
import lombok.SneakyThrows;
import me.clip.placeholderapi.PlaceholderAPI;
import me.neznamy.tab.platforms.bukkit.features.PerWorldPlayerList;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.data.World;
import me.neznamy.tab.shared.placeholders.types.PlayerPlaceholderImpl;
import me.neznamy.tab.shared.platform.LineOfSightCheck;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;

/**
 * Platform override for Folia.
 */
public class FoliaPlatform extends BukkitPlatform {

    /** Global tick thread scheduler */
    @NotNull
    private final Object globalScheduler;

    /** FoliaGlobalRegionScheduler#execute(Plugin, Runnable) method */
    private final Method globalScheduler_execute;

    /** Location overload avoids accessing a target entity from the viewer's region. */
    private final Method hasLineOfSightLocation;

    private final AtomicBoolean lineOfSightErrorLogged = new AtomicBoolean();

    /**
     * Constructs new instance with given plugin.
     *
     * @param   plugin
     *          Plugin
     */
    @SneakyThrows
    @SuppressWarnings("JavaReflectionMemberAccess")
    public FoliaPlatform(@NotNull JavaPlugin plugin) {
        super(plugin);
        globalScheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
        globalScheduler_execute = globalScheduler.getClass().getMethod("execute", Plugin.class, Runnable.class);
        hasLineOfSightLocation = Player.class.getMethod("hasLineOfSight", Location.class);
    }

    @Override
    public void loadPlayers() {
        super.loadPlayers();

        // Folia never calls PlayerChangedWorldEvent, this is a workaround
        TAB.getInstance().getCpu().getProcessingThread().repeatTask(new TimedCaughtTask(TAB.getInstance().getCpu(), ()  -> {
            for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
                World actualWorld = World.byName(((Player) player.getPlayer()).getWorld().getName());
                if (player.world != actualWorld) {
                    TAB.getInstance().getFeatureManager().onWorldChange(player.getUniqueId(), actualWorld);
                    PerWorldPlayerList pwp = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.PER_WORLD_PLAYER_LIST);
                    if (pwp != null) {
                        runSync((Entity) player.getPlayer(), () -> pwp.onWorldChange(new PlayerChangedWorldEvent((Player) player.getPlayer(), ((Player) player.getPlayer()).getWorld())));
                    }
                }
            }
        }, "Folia compatibility", "Refreshing world"), 100);
    }

    @Override
    public void registerPlaceholders() {
        super.registerPlaceholders();
        DecimalFormat decimal2;
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        decimal2 = new DecimalFormat("#.##", symbols);
        registerInternalSyncPlaceholder(TabConstants.Placeholder.MSPT, p -> decimal2.format(Bukkit.getAverageTickTime()));
        registerInternalSyncPlaceholder(TabConstants.Placeholder.TPS, p -> decimal2.format(Math.min(20, Bukkit.getTPS()[0])));
    }

    @Override
    public void registerSyncPlaceholder(@NotNull String identifier) {
        String syncedPlaceholder = "%" + identifier.substring(6);
        PlayerPlaceholderImpl[] ppl = new PlayerPlaceholderImpl[1];
        ppl[0] = TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder(identifier, p -> {
            runSync((Entity) p.getPlayer(), () -> {
                long time = System.nanoTime();
                String output = isPlaceholderAPI() ? PlaceholderAPI.setPlaceholders((Player) p.getPlayer(), syncedPlaceholder) : identifier;
                long totalTime =  System.nanoTime()-time;
                TAB.getInstance().getCPUManager().addPlaceholderTime(identifier, totalTime);
                TAB.getInstance().getCpu().addTime(TAB.getInstance().getPlaceholderManager().getFeatureName(), TabConstants.CpuUsageCategory.PLACEHOLDER_REQUEST, totalTime);
                TAB.getInstance().getCPUManager().runTask(() -> ppl[0].updateValue(p, output)); // To ensure player is loaded
            });
            return null;
        });
    }

    private void registerInternalSyncPlaceholder(@NonNull String identifier, @NonNull Function<TabPlayer, String> function) {
        PlayerPlaceholderImpl[] ppl = new PlayerPlaceholderImpl[1];
        ppl[0] = TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder(identifier, p -> {
            runSync((Entity) p.getPlayer(), () -> {
                long time = System.nanoTime();
                String output = function.apply((TabPlayer) p);
                long totalTime =  System.nanoTime()-time;
                TAB.getInstance().getCPUManager().addPlaceholderTime(identifier, totalTime);
                TAB.getInstance().getCpu().addTime(TAB.getInstance().getPlaceholderManager().getFeatureName(), TabConstants.CpuUsageCategory.PLACEHOLDER_REQUEST, totalTime);
                TAB.getInstance().getCPUManager().runTask(() -> ppl[0].updateValue(p, output)); // To ensure player is loaded
            });
            return null;
        });
    }

    /**
     * Overriding the method to fix initial error caused by ServerPlaceholder implementation trying to
     * retrieve the value in constructor, but folia does not support that. Overriding this function to avoid the
     * MSPT function being called in the wrong thread.
     *
     * @return  -1
     */
    @Override
    public double getMSPT() {
        return -1;
    }

    /**
     * Overriding the method to fix initial error caused by ServerPlaceholder implementation trying to
     * retrieve the value in constructor, but folia does not support that. Overriding this function to avoid the
     * TPS function being called in the wrong thread.
     *
     * @return  -1
     */
    @Override
    public double getTPS() {
        return -1;
    }

    /**
     * Runs task using player's entity scheduler. It's using reflection, because
     * Folia uses Java 17 while TAB maintains Java 8 compatibility for compatibility
     * with MC versions older than their player base.
     *
     * @param   entity
     *          entity to run task for
     * @param   task
     *          Task to run
     */
    @Override
    @SneakyThrows
    @SuppressWarnings("JavaReflectionMemberAccess")
    public void runSync(@NotNull Entity entity, @NotNull Runnable task) {
        if (!getPlugin().isEnabled()) return; // Server shutdown, no one cares anymore, everyone is about to be kicked
        Object entityScheduler = Entity.class.getMethod("getScheduler").invoke(entity);
        Consumer<?> consumer = $ -> task.run(); // Reflection and lambdas don't go together
        entityScheduler.getClass().getMethod("run", Plugin.class, Consumer.class, Runnable.class)
                .invoke(entityScheduler, getPlugin(), consumer, null);
    }

    /**
     * Runs task in global tick thread. It's using reflection, because
     * Folia uses Java 17 while TAB maintains Java 8 compatibility for compatibility
     * with MC versions older than their player base.
     *
     * @param   task
     *          Task to run
     */
    @Override
    @SneakyThrows
    public void runSyncGlobal(@NotNull Runnable task) {
        if (!getPlugin().isEnabled()) return; // Server shutdown, no one cares anymore, everyone is about to be kicked
        globalScheduler_execute.invoke(globalScheduler, getPlugin(), task);
    }

    /**
     * Captures target positions on their owning regions, then ray traces from each viewer's
     * owning region. No target entity is read while running a viewer's check.
     */
    @Override
    public void requestLineOfSight(@NotNull List<LineOfSightCheck> checks, @NotNull Consumer<boolean[]> callback) {
        boolean[] visible = new boolean[checks.size()];
        if (checks.isEmpty() || !getPlugin().isEnabled()) {
            callback.accept(visible);
            return;
        }

        Map<TabPlayer, CompletableFuture<Location>> targetLocations = new IdentityHashMap<>();
        for (LineOfSightCheck check : checks) {
            targetLocations.computeIfAbsent(check.getTarget(), target -> {
                CompletableFuture<Location> location = new CompletableFuture<>();
                scheduleEntity((Player) target.getPlayer(), () -> {
                    try {
                        Player player = (Player) target.getPlayer();
                        location.complete(player.isOnline() ? player.getEyeLocation() : null);
                    } catch (RuntimeException e) {
                        location.complete(null);
                        logLineOfSightError(e);
                    }
                }, () -> location.complete(null));
                return location;
            });
        }

        CompletableFuture.allOf(targetLocations.values().toArray(new CompletableFuture[0])).whenComplete((ignored, error) -> {
            Map<TabPlayer, List<Integer>> checksByViewer = new IdentityHashMap<>();
            for (int i = 0; i < checks.size(); i++) {
                checksByViewer.computeIfAbsent(checks.get(i).getViewer(), viewer -> new ArrayList<>()).add(i);
            }

            List<CompletableFuture<Void>> viewerTasks = new ArrayList<>(checksByViewer.size());
            for (Map.Entry<TabPlayer, List<Integer>> entry : checksByViewer.entrySet()) {
                CompletableFuture<Void> finished = new CompletableFuture<>();
                viewerTasks.add(finished);
                scheduleEntity((Player) entry.getKey().getPlayer(), () -> {
                    try {
                        Player viewer = (Player) entry.getKey().getPlayer();
                        if (viewer.isOnline()) {
                            for (int index : entry.getValue()) {
                                Location target = targetLocations.get(checks.get(index).getTarget()).getNow(null);
                                if (target == null || target.getWorld() != viewer.getWorld()) continue;
                                try {
                                    visible[index] = (boolean) hasLineOfSightLocation.invoke(viewer, target);
                                } catch (ReflectiveOperationException | RuntimeException e) {
                                    visible[index] = false;
                                    logLineOfSightError(e);
                                }
                            }
                        }
                    } catch (RuntimeException e) {
                        for (int index : entry.getValue()) visible[index] = false;
                        logLineOfSightError(e);
                    } finally {
                        finished.complete(null);
                    }
                }, () -> finished.complete(null));
            }
            CompletableFuture.allOf(viewerTasks.toArray(new CompletableFuture[0]))
                    .whenComplete((done, failure) -> callback.accept(visible));
        });
    }

    private void scheduleEntity(@NotNull Player player, @NotNull Runnable task, @NotNull Runnable retired) {
        if (!getPlugin().isEnabled()) {
            retired.run();
            return;
        }
        try {
            Object scheduler = Entity.class.getMethod("getScheduler").invoke(player);
            Consumer<?> consumer = ignored -> task.run();
            Object scheduled = scheduler.getClass().getMethod("run", Plugin.class, Consumer.class, Runnable.class)
                    .invoke(scheduler, getPlugin(), consumer, retired);
            if (scheduled == null) retired.run();
        } catch (ReflectiveOperationException | RuntimeException e) {
            retired.run();
            logLineOfSightError(e);
        }
    }

    private void logLineOfSightError(@NotNull Exception error) {
        if (lineOfSightErrorLogged.compareAndSet(false, true)) {
            getPlugin().getLogger().log(Level.WARNING, "Unable to check opaque nametag line of sight on Folia", error);
        }
    }
}
