package me.neznamy.tab.shared.features.nametags;

import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class holding team data for players.
 */
public class NameTagPlayerData {

    /** Team name used for sorting */
    public String teamName;

    /** Player's tagprefix */
    public Property prefix;

    /** Player's tagsuffix */
    public Property suffix;

    /** Flag tracking whether this feature is disabled for the player with condition or not */
    public final AtomicBoolean disabled = new AtomicBoolean();

    /** Players who should not see this player's name tag */
    @Nullable
    private Set<TabPlayer> hiddenNameTagFor;

    /** Flag tracking whether team handling is paused or not */
    public boolean teamHandlingPaused;

    /** Flag tracking whether this player disabled nametags on all players or not */
    public boolean invisibleNameTagView;

    /** Players who this player is vanished for */
    public final Set<UUID> vanishedFor = new HashSet<>();

    /** Currently used collision rule */
    public boolean collisionRule;

    /** Forced collision rule using API */
    @Nullable
    public Boolean forcedCollision;

    /** Reasons why player's nametag is hidden for everyone */
    @NotNull
    public EnumSet<NameTagInvisibilityReason> nameTagInvisibilityReasons = EnumSet.noneOf(NameTagInvisibilityReason.class);

    /**
     * Returns current collision rule. If forced using API, the forced value is returned.
     * Otherwise, value assigned internally based on configuration is returned.
     *
     * @return  Current collision rule to use
     */
    public boolean getCollisionRule() {
        return forcedCollision != null ? forcedCollision : collisionRule;
    }

    /**
     * Returns {@code true} if nametag is hidden for specified viewer, {@code false} if not.
     *
     * @param   viewer
     *          Player viewing the nametag
     * @return  {@code true} if hidden for viewer, {@code false} if not
     */
    public boolean hasHiddenNameTagFor(@NotNull TabPlayer viewer) {
        if (hiddenNameTagFor == null) return false;
        return hiddenNameTagFor.contains(viewer);
    }

    /**
     * Adds player to players to hide nametag for.
     *
     * @param   viewer
     *          Player to hide nametag for
     * @return  {@code true} if player was added, {@code false} if player was already added before
     */
    public boolean addHiddenNameTagFor(@NotNull TabPlayer viewer) {
        if (hiddenNameTagFor == null) hiddenNameTagFor = Collections.newSetFromMap(new WeakHashMap<>());
        return hiddenNameTagFor.add(viewer);
    }

    /**
     * Removes player from players to hide nametag for.
     *
     * @param   viewer
     *          Player to show back nametag for
     * @return  {@code true} if player was remove, {@code false} if player was not in list
     */
    public boolean removeHiddenNameTagFor(@NotNull TabPlayer viewer) {
        if (hiddenNameTagFor != null) return hiddenNameTagFor.remove(viewer);
        return false;
    }

    /**
     * Returns {@code true} if teams are disabled for this player either with condition
     * or with the API, {@code false} otherwise.
     *
     * @return  {@code true} if teams are disabled for the player, {@code false} if not
     */
    public boolean isDisabled() {
        return disabled.get() || teamHandlingPaused;
    }
}