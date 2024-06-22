package me.neznamy.tab.api;

import java.util.Collection;
import java.util.UUID;

import lombok.NonNull;
import lombok.Setter;
import me.neznamy.tab.api.bossbar.BossBarManager;
import me.neznamy.tab.api.event.EventBus;
import me.neznamy.tab.api.placeholder.PlaceholderManager;
import me.neznamy.tab.api.scoreboard.ScoreboardManager;
import me.neznamy.tab.api.tablist.SortingManager;
import me.neznamy.tab.api.tablist.HeaderFooterManager;
import me.neznamy.tab.api.tablist.TabListFormatManager;
import me.neznamy.tab.api.nametag.NameTagManager;
import me.neznamy.tab.api.tablist.layout.LayoutManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The primary API class to get instances of other API classes
 */
public abstract class TabAPI {

    /** Instance of the API */
    @Setter private static TabAPI instance;

    /**
     * Returns API instance. If instance was not set by the plugin, throws
     * {@code IllegalStateException}. This is usually caused by shading the API
     * into own project, which is not allowed. Another option is calling the method
     * before plugin was able to load.
     *
     * @return  API instance
     * @throws  IllegalStateException
     *          If instance is {@code null}
     */
    public static @NotNull TabAPI getInstance() {
        if (instance == null) throw new IllegalStateException("The API instance is null. This can have 2 possible causes: \n" +
                "#1 - API was called before TAB was loaded. This means your plugin was loaded before TAB was. To make sure your " +
                "plugin loads after TAB, add it as a depend or soft depend of your plugin.\n" +
                "#2 - You shaded TAB's classes into your plugin, instead of only using them. This is not allowed. To verify this " +
                "is your case, unzip your plugin and check for TAB's classes. If they are there, you will need to fix your compiler " +
                "to not include them, such as scope provided for maven compilation.");
        return instance;
    }

    /**
     * Returns player object from given UUID
     *
     * @param   id
     *          Player UUID
     * @return  player object from given UUID
     */
    public abstract @Nullable TabPlayer getPlayer(@NonNull UUID id);

    /**
     * Returns player object from given name
     *
     * @param   name
     *          Player name
     * @return  player object from given name
     */
    public abstract @Nullable TabPlayer getPlayer(@NonNull String name);

    /**
     * Returns collection of all online players. Will return empty list if plugin is disabled (due to a broken configuration file for example).
     *
     * @return  collection of online players
     */
    @Deprecated
    public abstract @NotNull TabPlayer[] getOnlinePlayers();


    /**
     * Returns a stream of all online players
     *
     * @return  stream of online players
     */
    public abstract @NotNull Collection<? extends TabPlayer> onlinePlayers();

    /**
     * Return BossBar manager instance if the feature is enabled. If not, returns {@code null}.
     *
     * @return  BossBar manager
     */
    public abstract @Nullable BossBarManager getBossBarManager();

    /**
     * Returns scoreboard manager instance if the feature is enabled. If not, returns {@code null}.
     *
     * @return  scoreboard manager
     */
    public abstract @Nullable ScoreboardManager getScoreboardManager();

    /**
     * Returns team manager instance if the feature is enabled. If not, returns {@code null}.
     *
     * @return  team manager
     */
    public abstract @Nullable NameTagManager getNameTagManager();

    /**
     * Returns header/footer manager instance if the feature is enabled. If not, returns {@code null}.
     *
     * @return  Header/footer manager
     */
    public abstract @Nullable HeaderFooterManager getHeaderFooterManager();

    /**
     * Returns PlaceholderManager instance
     *
     * @return  PlaceholderManager instance
     */
    public abstract @NotNull PlaceholderManager getPlaceholderManager();

    /**
     * Returns TabList name format manager instance if the feature is enabled. If not, returns {@code null}.
     *
     * @return  TabList name format manager
     */
    public abstract @Nullable TabListFormatManager getTabListFormatManager();

    /**
     * Returns Layout manager instance if the feature is enabled. If not, returns {@code null}.
     *
     * @return  Layout manager
     */
    public abstract @Nullable LayoutManager getLayoutManager();

    /**
     * Returns Sorting manager instance if at least one feature capable of sorting players is enabled.
     * If not, returns {@code null}.
     *
     * @return  Sorting manager
     */
    public abstract @Nullable SortingManager getSortingManager();

    /**
     * Gets the event bus for registering listeners for TAB events.
     *
     * @return  the event bus
     */
    public abstract @Nullable EventBus getEventBus();
}
