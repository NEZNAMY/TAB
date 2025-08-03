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
import me.neznamy.tab.shared.platform.TabPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Platform override for Folia.
 */
public class FoliaPlatform extends BukkitPlatform {

    /**
     * Constructs new instance with given plugin.
     *
     * @param   plugin
     *          Plugin
     */
    public FoliaPlatform(@NotNull JavaPlugin plugin) {
        super(plugin);
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
        registerInternalSyncPlaceholder(TabConstants.Placeholder.MSPT, 1000, p -> decimal2.format(Bukkit.getAverageTickTime()));
        registerInternalSyncPlaceholder(TabConstants.Placeholder.TPS, 1000, p -> decimal2.format(Math.min(20, Bukkit.getTPS()[0])));
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

    private void registerInternalSyncPlaceholder(@NonNull String identifier, int refresh, @NonNull Function<TabPlayer, String> function) {
        PlayerPlaceholderImpl[] ppl = new PlayerPlaceholderImpl[1];
        ppl[0] = TAB.getInstance().getPlaceholderManager().registerInternalPlayerPlaceholder(identifier, refresh, p -> {
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
        Object entityScheduler = Entity.class.getMethod("getScheduler").invoke(entity);
        Consumer<?> consumer = $ -> task.run(); // Reflection and lambdas don't go together
        entityScheduler.getClass().getMethod("run", Plugin.class, Consumer.class, Runnable.class)
                .invoke(entityScheduler, getPlugin(), consumer, null);
    }
}
