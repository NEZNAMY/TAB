package me.neznamy.tab.shared.features.nametags;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class holding team data for players.
 */
@RequiredArgsConstructor
public class NameTagPlayerData {

    /** Player this data belongs to */
    private final TabPlayer player;

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

    /** Whether opaque nametag mode is enabled for all viewers */
    private boolean opaqueNameTagMode;

    /** Viewers with opaque nametag mode enabled only for them */
    private final Set<TabPlayer> opaqueNameTagViewers = Collections.newSetFromMap(new WeakHashMap<>());

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

    /** Teams registered to this player mapped as team owner to team name */
    private final Map<TabPlayer, String> registeredTeams = new HashMap<>();

    /** Teams of proxy players registered to this player mapped as team owner to team name */
    private final Map<ProxyPlayer, String> registeredProxyTeams = new HashMap<>();

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

    /**
     * Returns {@code true} if nametag should be visible by given viewer, {@code false} if not.
     *
     * @param   viewer
     *          Viewer to check nametag visibility for
     * @return  {@code true} if nametag should be visible by given viewer, {@code false} if not
     */
    public boolean getTeamVisibility(@NonNull TabPlayer viewer) {
        if (hasHiddenNametag()) return false; // At least 1 reason for invisible nametag exists
        if (hasHiddenNametag(viewer)) return false; // At least 1 reason for invisible nametag for this viewer exists
        if (viewer.teamData.invisibleNameTagView) return false; // Viewer does not want to see nametags
        if (viewer.getVersion() == ProtocolVersion.V1_8 && player.hasInvisibilityPotion()) return false;
        return true;
    }

    /**
     * Returns {@code true} if opaque nametag mode is enabled for specified viewer.
     *
     * @param   viewer
     *          Viewer to check
     * @return  {@code true} if enabled, {@code false} if not
     */
    public boolean isOpaqueNameTagMode(@NotNull TabPlayer viewer) {
        return opaqueNameTagMode || opaqueNameTagViewers.contains(viewer);
    }

    /**
     * Enables or disables opaque nametag mode globally or for specified viewer.
     *
     * @param   viewer
     *          Viewer to change mode for, or {@code null} for all viewers
     * @param   enabled
     *          Whether opaque nametag mode should be enabled
     */
    public void setOpaqueNameTagMode(@Nullable TabPlayer viewer, boolean enabled) {
        if (viewer == null) {
            opaqueNameTagMode = enabled;
            if (!enabled) opaqueNameTagViewers.clear();
        } else if (enabled) {
            opaqueNameTagViewers.add(viewer);
        } else {
            opaqueNameTagViewers.remove(viewer);
        }
    }

    /**
     * Returns {@code true} if opaque nametag mode is enabled globally or for at least one viewer.
     *
     * @return  {@code true} if enabled, {@code false} if not
     */
    public boolean hasOpaqueNameTagMode() {
        return opaqueNameTagMode || !opaqueNameTagViewers.isEmpty();
    }

    public void registerTeam(@NotNull TabPlayer target, @NotNull String teamName, @NotNull TabComponent prefix, @NotNull TabComponent suffix,
                                 @NotNull Scoreboard.NameVisibility visibility, @NotNull Scoreboard.CollisionRule collision,
                                 @NotNull Collection<String> players, int options, @NotNull EnumChatFormat color) {
        registeredTeams.put(target, teamName);
        player.getScoreboard().registerTeam(teamName, prefix, suffix, visibility, collision, players, options, color);
    }

    public void registerTeam(@NotNull ProxyPlayer target, @NotNull String teamName, @NotNull TabComponent prefix, @NotNull TabComponent suffix,
                                 @NotNull Scoreboard.NameVisibility visibility, @NotNull Scoreboard.CollisionRule collision,
                                 @NotNull Collection<String> players, int options, @NotNull EnumChatFormat color) {
        registeredProxyTeams.put(target, teamName);
        player.getScoreboard().registerTeam(teamName, prefix, suffix, visibility, collision, players, options, color);
    }

    /**
     * Returns {@code true} if team with given owner is registered to this player, {@code false} if not.
     *
     * @param   teamOwner
     *          Owner of the team to check
     * @return  {@code true} if team with given owner is registered to this player, {@code false} if not
     */
    public boolean hasTeamRegistered(@NotNull TabPlayer teamOwner) {
        return registeredTeams.containsKey(teamOwner);
    }

    /**
     * Returns {@code true} if team with given owner is registered to this player, {@code false} if not.
     *
     * @param   teamOwner
     *          Owner of the team to check
     * @return  {@code true} if team with given owner is registered to this player, {@code false} if not
     */
    public boolean hasTeamRegistered(@NotNull ProxyPlayer teamOwner) {
        return registeredProxyTeams.containsKey(teamOwner);
    }

    /**
     * Safely unregisters team belonging to the given owner if registered before and removes it from the map.
     * If not registered, nothing happens.
     *
     * @param   teamOwner
     *          Owner of the team to unregister
     */
    public void unregisterTeam(@NotNull TabPlayer teamOwner) {
        String teamName = registeredTeams.remove(teamOwner);
        if (teamName != null) {
            player.getScoreboard().unregisterTeam(teamName);
        }
    }

    /**
     * Safely unregisters team belonging to the given owner if registered before and removes it from the map.
     * If not registered, nothing happens.
     *
     * @param   teamOwner
     *          Owner of the team to unregister
     */
    public void unregisterTeam(@NotNull ProxyPlayer teamOwner) {
        String teamName = registeredProxyTeams.remove(teamOwner);
        if (teamName != null) {
            player.getScoreboard().unregisterTeam(teamName);
        }
    }
}
