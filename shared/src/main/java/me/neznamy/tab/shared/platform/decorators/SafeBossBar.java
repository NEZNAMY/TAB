package me.neznamy.tab.shared.platform.decorators;

import lombok.*;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.BossBar;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An abstract class implementing BossBar interface that contains
 * various safety checks to make sure there is no accidental NullPointerException
 * thrown or client disconnected with "Network Protocol Error" (1.20.5+) when sending
 * BossBar update to player who is switching server, but switch did not finish yet and
 * player did not receive the BossBar again (1.20.2+).
 *
 * @param   <T>
 *          Platform's BossBar class
 */
public abstract class SafeBossBar<T> implements BossBar {

    /** BossBars currently visible to the player */
    private final Map<UUID, BossBarInfo> bossBars = new ConcurrentHashMap<>();

    /** Flag tracking whether boss bars should be frozen or not */
    private boolean frozen;

    @Override
    public synchronized void create(@NotNull UUID id, @NotNull TabComponent title, float progress, @NotNull BarColor color, @NotNull BarStyle style) {
        BossBarInfo bar = new BossBarInfo(title, progress, color, style, constructBossBar(title, progress, color, style));
        bossBars.put(id, bar);
        if (frozen) return;
        create(bar);
    }

    @Override
    public synchronized void update(@NotNull UUID id, @NotNull TabComponent title) {
        BossBarInfo bar = bossBars.get(id);
        if (bar == null) return;
        bar.setTitle(title);
        if (frozen) return;
        updateTitle(bar);
    }

    @Override
    public synchronized void update(@NotNull UUID id, float progress) {
        BossBarInfo bar = bossBars.get(id);
        if (bar == null) return;
        bar.setProgress(progress);
        if (frozen) return;
        updateProgress(bar);
    }

    @Override
    public synchronized void update(@NotNull UUID id, @NotNull BarStyle style) {
        BossBarInfo bar = bossBars.get(id);
        if (bar == null) return;
        bar.setStyle(style);
        if (frozen) return;
        updateStyle(bar);
    }

    @Override
    public synchronized void update(@NotNull UUID id, @NotNull BarColor color) {
        BossBarInfo bar = bossBars.get(id);
        if (bar == null) return;
        bar.setColor(color);
        if (frozen) return;
        updateColor(bar);
    }

    @Override
    public synchronized void remove(@NotNull UUID id) {
        BossBarInfo bar = bossBars.remove(id);
        if (bar == null) return;
        if (frozen) return;
        remove(bar);
    }

    @Override
    public synchronized void clear() {
        for (UUID id : bossBars.keySet()) {
            remove(id);
        }
    }

    /**
     * Freezes the class, not letting any packets through.
     */
    public synchronized void freeze() {
        frozen = true;
    }

    /**
     * Unfreezes the class back, enabling it back and resending all BossBars to the player.
     */
    public synchronized void unfreezeAndResend() {
        frozen = false;
        for (BossBarInfo bar : bossBars.values()) {
            // Destroy previous reference due to Adventure bug
            bar.setBossBar(constructBossBar(bar.getTitle(), bar.getProgress(), bar.getColor(), bar.getStyle()));
            create(bar);
        }
    }

    /**
     * Constructs platform's BossBar object with given parameters.
     *
     * @param   title
     *          BossBar title
     * @param   progress
     *          BossBar progress (0-1)
     * @param   color
     *          BossBar color
     * @param   style
     *          BossBar style
     * @return  Platform's BossBar with given data
     */
    @NotNull
    public abstract T constructBossBar(@NotNull TabComponent title, float progress, @NotNull BarColor color, @NotNull BarStyle style);

    /**
     * Creates the BossBar and sends it to the player.
     *
     * @param   bar
     *          BossBar to create
     */
    public abstract void create(@NotNull BossBarInfo bar);

    /**
     * Updates title of a BossBar.
     *
     * @param   bar
     *          BossBar to update title of
     */
    public abstract void updateTitle(@NotNull BossBarInfo bar);

    /**
     * Updates progress of a BossBar.
     *
     * @param   bar
     *          BossBar to update progress of
     */
    public abstract void updateProgress(@NotNull BossBarInfo bar);

    /**
     * Updates style of a BossBar.
     *
     * @param   bar
     *          BossBar to update style of
     */
    public abstract void updateStyle(@NotNull BossBarInfo bar);

    /**
     * Updates color of a BossBar.
     *
     * @param   bar
     *          BossBar to update color of
     */
    public abstract void updateColor(@NotNull BossBarInfo bar);

    /**
     * Removes BossBar from the player.
     *
     * @param   bar
     *          BossBar to remove
     */
    public abstract void remove(@NotNull BossBarInfo bar);

    /**
     * Class storing raw information about a BossBar.
     */
    @AllArgsConstructor
    @Getter
    @Setter
    public class BossBarInfo {

        /** BossBar's title */
        @NonNull private TabComponent title;

        /** BossBar's progress */
        private float progress;

        /** BossBar's color */
        @NonNull private BarColor color;

        /** BossBar's style */
        @NonNull private BarStyle style;

        /** Platform's BossBar object of this BossBar */
        @NonNull private T bossBar;
    }
}
