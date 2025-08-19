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
    private final EnumSet<NameTagInvisibilityReason> nameTagInvisibilityReasons = EnumSet.noneOf(NameTagInvisibilityReason.class);

    /** Reasons why player's nametag is hidden for specific players */
    @NotNull
    private final Map<TabPlayer, EnumSet<NameTagInvisibilityReason>> nameTagInvisibilityReasonsRelational = new WeakHashMap<>();

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
     * Returns {@code true} if teams are disabled for this player either with condition
     * or with the API, {@code false} otherwise.
     *
     * @return  {@code true} if teams are disabled for the player, {@code false} if not
     */
    public boolean isDisabled() {
        return disabled.get() || teamHandlingPaused;
    }

    /**
     * Hides nametag for a specified reason.
     *
     * @param   reason
     *          Reason for hiding nametag
     * @return  Whether the state has changed as a result of this call
     */
    public boolean hideNametag(@NotNull NameTagInvisibilityReason reason) {
        return nameTagInvisibilityReasons.add(reason);
    }

    /**
     * Shows nametag for a specified reason (removes existing hide reason).
     *
     * @param   reason
     *          Reason for showing nametag
     * @return  Whether the state has changed as a result of this call
     */
    public boolean showNametag(@NotNull NameTagInvisibilityReason reason) {
        return nameTagInvisibilityReasons.remove(reason);
    }

    /**
     * Returns {@code true} if nametag is hidden for specified reason, {@code false} if not.
     *
     * @param   reason
     *          Reason to check
     * @return  {@code true} if nametag is hidden for specified reason, {@code false} if not
     */
    public boolean hasHiddenNametag(@NotNull NameTagInvisibilityReason reason) {
        return nameTagInvisibilityReasons.contains(reason);
    }

    /**
     * Returns {@code true} if there is at least 1 reason why this nametag should be hidden, otherwise,
     * returns {@code false}.
     *
     * @return  {@code true} if nametag should be hidden, {@code false} if not
     */
    public boolean hasHiddenNametag() {
        return !nameTagInvisibilityReasons.isEmpty();
    }

    /**
     * Hides nametag for specified viewer for specified reason.
     *
     * @param   viewer
     *          Viewer to mark nametag as hidden for
     * @param   reason
     *          Reason for hiding nametag
     * @return  Whether the state has changed as a result of this call
     */
    public boolean hideNametag(@NotNull TabPlayer viewer, @NotNull NameTagInvisibilityReason reason) {
        return nameTagInvisibilityReasonsRelational.computeIfAbsent(viewer, v -> EnumSet.noneOf(NameTagInvisibilityReason.class)).add(reason);
    }

    /**
     * Shows nametag for specified player for specified reason (removes existing hide reason).
     *
     * @param   viewer
     *          Viewer to show nametag back for
     * @param   reason
     *          Reason for showing nametag
     * @return  Whether the state has changed as a result of this call
     */
    public boolean showNametag(@NotNull TabPlayer viewer, @NotNull NameTagInvisibilityReason reason) {
        return nameTagInvisibilityReasonsRelational.computeIfAbsent(viewer, v -> EnumSet.noneOf(NameTagInvisibilityReason.class)).remove(reason);
    }

    /**
     * Returns {@code true} if nametag is hidden for specified viewer for specified reason, {@code false} if not.
     *
     * @param   viewer
     *          Viewer who nametag might be hidden for
     * @param   reason
     *          Reason to check
     * @return  {@code true} if nametag is hidden for specified reason, {@code false} if not
     */
    public boolean hasHiddenNametag(@NotNull TabPlayer viewer, @NotNull NameTagInvisibilityReason reason) {
        if (!nameTagInvisibilityReasonsRelational.containsKey(viewer)) {
            return false;
        }
        return nameTagInvisibilityReasonsRelational.get(viewer).contains(reason);
    }

    /**
     * Returns {@code true} if there is at least 1 reason why this nametag should be hidden for given viewer,
     * otherwise, returns {@code false}.
     *
     * @param   viewer
     *          Viewer who nametag might be hidden for
     * @return  {@code true} if nametag should be hidden, {@code false} if not
     */
    public boolean hasHiddenNametag(@NotNull TabPlayer viewer) {
        if (!nameTagInvisibilityReasonsRelational.containsKey(viewer)) {
            return false;
        }
        return !nameTagInvisibilityReasonsRelational.get(viewer).isEmpty();
    }
}