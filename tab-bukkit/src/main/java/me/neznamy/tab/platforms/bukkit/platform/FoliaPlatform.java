package me.neznamy.tab.platforms.bukkit.platform;

import lombok.SneakyThrows;
import me.clip.placeholderapi.PlaceholderAPI;
import me.neznamy.tab.platforms.bukkit.features.PerWorldPlayerList;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.SimpleComponent;
import me.neznamy.tab.shared.placeholders.types.PlayerPlaceholderImpl;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

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

        // Values are never updated in the API, warn users
        logWarn(new SimpleComponent("Folia never updates MSPT and TPS values in the API, making " +
                "%mspt% and %tps% return the default values (0 and 20)."));

        // Folia never calls PlayerChangedWorldEvent, this is a workaround
        TAB.getInstance().getCPUManager().startRepeatingMeasuredTask(100, "Folia compatibility", "Refreshing world", () -> {
            for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
                String bukkitWorld = ((Player)player.getPlayer()).getWorld().getName();
                if (!player.getWorld().equals(bukkitWorld)) {
                    TAB.getInstance().getFeatureManager().onWorldChange(player.getUniqueId(), bukkitWorld);
                    PerWorldPlayerList pwp = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.PER_WORLD_PLAYER_LIST);
                    if (pwp != null) {
                        runSync((Entity) player.getPlayer(), () -> pwp.onWorldChange(new PlayerChangedWorldEvent((Player) player.getPlayer(), ((Player) player.getPlayer()).getWorld())));
                    }
                }
            }
        });
    }

    @Override
    public void registerSyncPlaceholder(@NotNull String identifier, int refresh) {
        String syncedPlaceholder = "%" + identifier.substring(6);
        PlayerPlaceholderImpl[] ppl = new PlayerPlaceholderImpl[1];
        ppl[0] = TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder(identifier, refresh, p -> {
            runSync((Entity) p.getPlayer(), () -> {
                long time = System.nanoTime();
                String output = isPlaceholderAPI() ? PlaceholderAPI.setPlaceholders((Player) p.getPlayer(), syncedPlaceholder) : identifier;
                TAB.getInstance().getCPUManager().addPlaceholderTime(identifier, System.nanoTime()-time);
                TAB.getInstance().getCPUManager().runTask(() -> ppl[0].updateValue(p, output)); // To ensure player is loaded
            });
            return null;
        });
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

    @Override
    public void runEntityTask(@NotNull Entity entity, @NotNull Runnable task) {
        runSync(entity, task);
    }
}
