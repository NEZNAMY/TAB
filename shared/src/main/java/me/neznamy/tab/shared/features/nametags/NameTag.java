package me.neznamy.tab.shared.features.nametags;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.api.nametag.NameTagManager;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.config.MessageFile;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.data.Server;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.platform.Scoreboard.CollisionRule;
import me.neznamy.tab.shared.platform.Scoreboard.NameVisibility;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.DumpUtils;
import me.neznamy.tab.shared.util.OnlinePlayers;
import me.neznamy.tab.shared.util.cache.LastColorCache;
import me.neznamy.tab.shared.util.cache.StringToComponentCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class NameTag extends TabFeature implements NameTagManager, JoinListener, QuitListener,
        Loadable, VanishListener, ServerSwitchListener, CustomThreaded, ProxyFeature, Dumpable {

    private final ThreadExecutor customThread = new ThreadExecutor("TAB NameTag Thread");
    private OnlinePlayers onlinePlayers;
    private final TeamConfiguration configuration;
    private final StringToComponentCache prefixCache = new StringToComponentCache("NameTag prefix", 1000);
    private final StringToComponentCache lastColorCache = new LastColorCache("NameTag last prefix color", 1000);
    private final StringToComponentCache suffixCache = new StringToComponentCache("NameTag suffix", 1000);
    private final VisibilityManager visibilityManager;
    private final CollisionManager collisionManager;
    private final int teamOptions;
    private final DisableChecker disableChecker;
    @Nullable private final ProxySupport proxy = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.PROXY_SUPPORT);
    private final NameTagProxyHandler proxyHandler = new NameTagProxyHandler(this);
    private final PrefixSuffixManager prefixSuffixManager = new PrefixSuffixManager(this);

    /**
     * Constructs new instance and registers sub-features.
     *
     * @param   configuration
     *          Feature configuration
     */
    public NameTag(@NotNull TeamConfiguration configuration) {
        this.configuration = configuration;
        teamOptions = configuration.isCanSeeFriendlyInvisibles() ? 2 : 0;
        disableChecker = new DisableChecker(this, TAB.getInstance().getPlaceholderManager().getConditionManager().getByNameOrExpression(configuration.getDisableCondition()), this::onDisableConditionChange, p -> p.teamData.disabled);
        visibilityManager = new VisibilityManager(this);
        collisionManager = new CollisionManager(this);
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.NAME_TAGS + "-Condition", disableChecker);
        if (proxy != null) {
            proxy.registerMessage(NameTagProxyPlayerData.class, in -> new NameTagProxyPlayerData(in, this));
        }
    }

    @Override
    public void load() {
        onlinePlayers = new OnlinePlayers(TAB.getInstance().getOnlinePlayers());
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.NAME_TAGS_VISIBILITY, visibilityManager);
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.NAME_TAGS_COLLISION, collisionManager);
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.NAME_TAGS_PREFIX_SUFFIX, prefixSuffixManager);
        for (TabPlayer all : onlinePlayers.getPlayers()) {
            prefixSuffixManager.loadProperties(all);
            all.teamData.teamName = all.sortingData.shortTeamName; // Sorting is loaded sync before nametags
            if (disableChecker.isDisableConditionMet(all)) {
                all.teamData.disabled.set(true);
                continue;
            }
            all.expansionData.setNameTagVisibility(true);
            proxyHandler.sendProxyMessage(all);
        }
        for (TabPlayer viewer : onlinePlayers.getPlayers()) {
            for (TabPlayer target : onlinePlayers.getPlayers()) {
                if (target.isVanished() && !viewer.canSee(target)) {
                    target.teamData.vanishedFor.add(viewer.getUniqueId());
                }
                if (!target.teamData.isDisabled()) registerTeam(target, viewer);
            }
        }
        visibilityManager.load();
        collisionManager.load();
    }

    @Override
    public void onJoin(@NotNull TabPlayer connectedPlayer) {
        onlinePlayers.addPlayer(connectedPlayer);
        prefixSuffixManager.loadProperties(connectedPlayer);
        connectedPlayer.teamData.teamName = connectedPlayer.sortingData.shortTeamName; // Sorting is loaded sync before nametags
        for (TabPlayer all : onlinePlayers.getPlayers()) {
            if (all == connectedPlayer) continue; //avoiding double registration
            if (connectedPlayer.isVanished() && !all.canSee(connectedPlayer)) {
                connectedPlayer.teamData.vanishedFor.add(all.getUniqueId());
            }
            if (all.isVanished() && !connectedPlayer.canSee(all)) {
                all.teamData.vanishedFor.add(connectedPlayer.getUniqueId());
            }
            if (!all.teamData.isDisabled()) {
                registerTeam(all, connectedPlayer);
            }
        }
        connectedPlayer.expansionData.setNameTagVisibility(true);
        if (proxy != null) {
            ProxyPlayer proxyPlayer = proxy.getProxyPlayers().get(connectedPlayer.getUniqueId());
            if (proxyPlayer != null && proxyPlayer.getNametag() != null) {
                unregisterTeam(proxyPlayer);
                proxyPlayer.setNametag(null);
            }
        }
        if (disableChecker.isDisableConditionMet(connectedPlayer)) {
            connectedPlayer.teamData.disabled.set(true);
            return;
        }
        registerTeam(connectedPlayer);
        if (proxy != null) {
            for (ProxyPlayer proxied : proxy.getProxyPlayers().values()) {
                if (proxied.getNametag() == null) continue; // This proxy player is not loaded yet
                connectedPlayer.teamData.registerTeam(
                        proxied,
                        proxied.getNametag().getResolvedTeamName(),
                        prefixCache.get(proxied.getNametag().getPrefix()),
                        suffixCache.get(proxied.getNametag().getSuffix()),
                        proxied.getNametag().getNameVisibility(),
                        CollisionRule.ALWAYS,
                        Collections.singletonList(proxied.getNickname()),
                        teamOptions,
                        lastColorCache.get(proxied.getNametag().getPrefix()).getLastStyle().toEnumChatFormat()
                );
            }
            proxyHandler.sendProxyMessage(connectedPlayer);
        }
    }

    @Override
    public void onQuit(@NotNull TabPlayer disconnectedPlayer) {
        onlinePlayers.removePlayer(disconnectedPlayer);
        unregisterTeam(disconnectedPlayer);
    }

    @Override
    public void onVanishStatusChange(@NotNull TabPlayer player) {
        if (player.isVanished()) {
            for (TabPlayer viewer : onlinePlayers.getPlayers()) {
                if (viewer == player) continue;
                if (!viewer.canSee(player)) {
                    player.teamData.vanishedFor.add(viewer.getUniqueId());
                    viewer.teamData.unregisterTeam(player);
                }
            }
        } else {
            Set<UUID> ids = new HashSet<>(player.teamData.vanishedFor);
            player.teamData.vanishedFor.clear();
            if (!player.teamData.isDisabled()) {
                for (UUID id : ids) {
                    TabPlayer viewer = TAB.getInstance().getPlayer(id);
                    if (viewer != null) registerTeam(player, viewer);
                }
            }
        }
    }

    @Override
    public void onServerChange(@NotNull TabPlayer changed, @NotNull Server from, @NotNull Server to) {
        for (TabPlayer all : onlinePlayers.getPlayers()) {
            // Process changed player's team
            boolean shouldRegister = shouldRegister(changed, all);
            boolean isRegistered = all.teamData.hasTeamRegistered(changed);
            if (shouldRegister) {
                if (!isRegistered) {
                    registerTeam(changed, all);
                }
            } else {
                if (isRegistered) {
                    all.teamData.unregisterTeam(changed);
                }
            }

            if (all == changed) continue;

            // Process the other player's team
            shouldRegister = shouldRegister(all, changed);
            isRegistered = changed.teamData.hasTeamRegistered(all);
            if (shouldRegister) {
                if (!isRegistered) {
                    registerTeam(all, changed);
                }
            } else {
                if (isRegistered) {
                    changed.teamData.unregisterTeam(all);
                }
            }
        }
    }

    public void onDisableConditionChange(TabPlayer p, boolean disabledNow) {
        if (disabledNow) {
            unregisterTeam(p);
        } else {
            registerTeam(p);
        }
        if (proxy != null) {
            proxyHandler.sendProxyMessage(p);
        }
    }

    /**
     * Safely unregisters team of the given target player for everyone who can see the team.
     *
     * @param   target
     *          Player whose team should be unregistered
     */
    public void unregisterTeam(@NotNull TabPlayer target) {
        for (TabPlayer viewer : onlinePlayers.getPlayers()) {
            viewer.teamData.unregisterTeam(target);
        }
    }

    /**
     * Safely unregisters team of the given target player for everyone who can see the team.
     *
     * @param   target
     *          Player whose team should be unregistered
     */
    public void unregisterTeam(@NotNull ProxyPlayer target) {
        for (TabPlayer viewer : onlinePlayers.getPlayers()) {
            viewer.teamData.unregisterTeam(target);
        }
    }

    private void registerTeam(@NonNull TabPlayer p) {
        for (TabPlayer viewer : onlinePlayers.getPlayers()) {
            registerTeam(p, viewer);
        }
    }

    private void registerTeam(@NonNull TabPlayer p, @NonNull TabPlayer viewer) {
        if (shouldRegister(p, viewer)) {
            viewer.teamData.registerTeam(
                    p,
                    p.teamData.teamName,
                    prefixCache.get(p.teamData.prefix.getFormat(viewer)),
                    suffixCache.get(p.teamData.suffix.getFormat(viewer)),
                    p.teamData.getTeamVisibility(viewer) ? NameVisibility.ALWAYS : NameVisibility.NEVER,
                    p.teamData.getCollisionRule() ? CollisionRule.ALWAYS : CollisionRule.NEVER,
                    Collections.singletonList(p.getNickname()),
                    teamOptions,
                    lastColorCache.get(p.teamData.prefix.getFormat(viewer)).getLastStyle().toEnumChatFormat()
            );
        }
    }

    private boolean shouldRegister(@NotNull TabPlayer teamOwner, @NonNull TabPlayer viewer) {
        if (teamOwner.teamData.isDisabled()) return false;
        if (teamOwner.teamData.vanishedFor.contains(viewer.getUniqueId())) return false;
        if (!viewer.canSee(teamOwner) && teamOwner != viewer) return false;
        return viewer.server.canSee(teamOwner.server);
    }

    /**
     * Updates team name for a specified player to everyone.
     *
     * @param   player
     *          Player to change team name of
     * @param   newTeamName
     *          New team name to use
     */
    public void updateTeamName(@NonNull TabPlayer player, @NonNull String newTeamName) {
        customThread.execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            // Function ran before onJoin did (super rare), drop action since onJoin will use new team name anyway
            if (player.teamData.teamName == null) return;
            for (TabPlayer viewer : onlinePlayers.getPlayers()) {
                viewer.teamData.unregisterTeam(player);
            }
            player.teamData.teamName = newTeamName;
            for (TabPlayer viewer : onlinePlayers.getPlayers()) {
                registerTeam(player, viewer);
            }
            proxyHandler.sendProxyMessage(player);
        }, getFeatureName(), "Updating team name"));
    }

    @Override
    public void onProxyLoadRequest() {
        proxyHandler.onProxyLoadRequest();
    }

    @Override
    public void onQuit(@NotNull ProxyPlayer player) {
        proxyHandler.onQuit(player);
    }

    @Override
    public void onJoin(@NotNull ProxyPlayer player) {
        proxyHandler.onJoin(player);
    }

    @NotNull
    @Override
    public String getFeatureName() {
        return "NameTags";
    }

    @Override
    @NotNull
    public Object dump(@NotNull TabPlayer player) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("configuration", configuration.getSection().getMap());
        map.put("disabled with condition", player.teamData.disabled.get());
        map.put("team name", player.teamData.teamName.replaceAll("\\p{C}", ""));
        map.put("team handling paused with API", player.teamData.teamHandlingPaused);
        map.put("invisible nametag view", player.teamData.invisibleNameTagView);
        map.put("collision", player.teamData.getCollisionRule());
        map.put("invisible nametag", visibilityManager.getInvisibleCondition().isMet(player));
        for (Property property : Arrays.asList(player.teamData.prefix, player.teamData.suffix)) {
            Map<String, Object> propertyMap = new LinkedHashMap<>();
            propertyMap.put("configured-raw-value", property.getOriginalRawValue());
            propertyMap.put("api-forced-raw-value", property.getTemporaryValue());
            propertyMap.put("current-source", property.getSource());
            propertyMap.put("replaced-value", property.get());
            map.put(property.getName(), propertyMap);
        }

        // Table of all players
        List<String> header = Arrays.asList("Player", "tagprefix", "team color", "tagsuffix", "Disabled with condition");
        List<List<String>> players = Arrays.stream(TAB.getInstance().getOnlinePlayers()).map(p -> Arrays.asList(
                p.getName(),
                "\"" + p.teamData.prefix.get() + "\"",
                lastColorCache.get(p.teamData.prefix.getFormat(p)).getLastStyle().toEnumChatFormat().name(),
                "\"" + p.teamData.suffix.get() + "\"",
                String.valueOf(p.teamData.disabled.get())
        )).collect(Collectors.toList());
        if (proxy != null) {
            players.addAll(proxy.getProxyPlayers().values().stream().map(p -> Arrays.asList(
                    "[Proxy] " + p.getName(),
                    p.getNametag() == null ? "NULL" : "\"" + p.getNametag().getPrefix() + "\"",
                    p.getNametag() == null ? "NULL" : lastColorCache.get(p.getNametag().getPrefix()).getLastStyle().toEnumChatFormat().name(),
                    p.getNametag() == null ? "NULL" : "\"" + p.getNametag().getSuffix() + "\"",
                    p.getNametag() == null ? "NULL" : String.valueOf(p.getNametag().isDisabled())
            )).collect(Collectors.toList()));
        }
        map.put("current values for all players (without applying relational placeholders)", DumpUtils.tableToLines(header, players));
        return map;
    }

    // ------------------
    // API Implementation
    // ------------------

    @Override
    public void hideNameTag(@NonNull me.neznamy.tab.api.TabPlayer player) {
        visibilityManager.hideNameTag((TabPlayer) player, NameTagInvisibilityReason.API_HIDE, "Processing API call (hideNameTag)", false);
    }

    @Override
    public void hideNameTag(@NonNull me.neznamy.tab.api.TabPlayer player, @NonNull me.neznamy.tab.api.TabPlayer viewer) {
        visibilityManager.hideNameTag((TabPlayer) player, (TabPlayer) viewer, NameTagInvisibilityReason.API_HIDE, "Processing API call (hideNameTag)", false);
    }

    @Override
    public void showNameTag(@NonNull me.neznamy.tab.api.TabPlayer player) {
        visibilityManager.showNameTag((TabPlayer) player, NameTagInvisibilityReason.API_HIDE, "Processing API call (showNameTag)", false);
    }

    @Override
    public void showNameTag(@NonNull me.neznamy.tab.api.TabPlayer player, @NonNull me.neznamy.tab.api.TabPlayer viewer) {
        visibilityManager.showNameTag((TabPlayer) player, (TabPlayer) viewer, NameTagInvisibilityReason.API_HIDE, "Processing API call (showNameTag)", false);
    }

    @Override
    public boolean hasHiddenNameTag(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        return ((TabPlayer)player).teamData.hasHiddenNametag(NameTagInvisibilityReason.API_HIDE);
    }

    @Override
    public boolean hasHiddenNameTag(@NonNull me.neznamy.tab.api.TabPlayer player, @NonNull me.neznamy.tab.api.TabPlayer viewer) {
        ensureActive();
        return ((TabPlayer)player).teamData.hasHiddenNametag((TabPlayer) viewer, NameTagInvisibilityReason.API_HIDE);
    }

    @Override
    public void pauseTeamHandling(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        customThread.execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            TabPlayer p = (TabPlayer) player;
            p.ensureLoaded();
            if (p.teamData.teamHandlingPaused) return;
            unregisterTeam(p);
            p.teamData.teamHandlingPaused = true;
        }, getFeatureName(), "Processing API call (pauseTeamHandling)"));
    }

    @Override
    public void resumeTeamHandling(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        customThread.execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            TabPlayer p = (TabPlayer) player;
            p.ensureLoaded();
            if (!p.teamData.teamHandlingPaused) return;
            p.teamData.teamHandlingPaused = false; //setting before, so registerTeam method runs
            if (!p.teamData.isDisabled()) registerTeam(p);
        }, getFeatureName(), "Processing API call (resumeTeamHandling)"));
    }

    @Override
    public boolean hasTeamHandlingPaused(@NonNull me.neznamy.tab.api.TabPlayer player) {
        return ((TabPlayer)player).teamData.teamHandlingPaused;
    }

    @Override
    public void setCollisionRule(@NonNull me.neznamy.tab.api.TabPlayer player, Boolean collision) {
        collisionManager.setCollisionRule(player, collision);
    }

    @Override
    @Nullable
    public Boolean getCollisionRule(@NonNull me.neznamy.tab.api.TabPlayer player) {
        return collisionManager.getCollisionRule(player);
    }

    @Override
    public void setPrefix(@NonNull me.neznamy.tab.api.TabPlayer player, @Nullable String prefix) {
        ensureActive();
        customThread.execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            TabPlayer p = (TabPlayer) player;
            p.ensureLoaded();
            p.teamData.prefix.setTemporaryValue(prefix);
            prefixSuffixManager.updatePrefixSuffix(p);
        }, getFeatureName(), "Processing API call (setPrefix)"));
    }

    @Override
    public void setSuffix(@NonNull me.neznamy.tab.api.TabPlayer player, @Nullable String suffix) {
        ensureActive();
        customThread.execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            TabPlayer p = (TabPlayer) player;
            p.ensureLoaded();
            p.teamData.suffix.setTemporaryValue(suffix);
            prefixSuffixManager.updatePrefixSuffix(p);
        }, getFeatureName(), "Processing API call (setSuffix)"));
    }

    @Override
    public String getCustomPrefix(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();
        return p.teamData.prefix.getTemporaryValue();
    }

    @Override
    public String getCustomSuffix(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();
        return p.teamData.suffix.getTemporaryValue();
    }

    @Override
    @NotNull
    public String getOriginalRawPrefix(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();
        return p.teamData.prefix.getOriginalRawValue();
    }

    @Override
    @NotNull
    public String getOriginalRawSuffix(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();
        return p.teamData.suffix.getOriginalRawValue();
    }

    @Override
    @NotNull
    public String getOriginalReplacedPrefix(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();
        return p.teamData.prefix.getOriginalReplacedValue();
    }

    @Override
    @NotNull
    public String getOriginalReplacedSuffix(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();
        return p.teamData.suffix.getOriginalReplacedValue();
    }

    @Override
    @NotNull
    public String getOriginalPrefix(@NonNull me.neznamy.tab.api.TabPlayer player) {
        return getOriginalRawPrefix(player);
    }

    @Override
    @NotNull
    public String getOriginalSuffix(@NonNull me.neznamy.tab.api.TabPlayer player) {
        return getOriginalRawSuffix(player);
    }

    @Override
    public void toggleNameTagVisibilityView(@NonNull me.neznamy.tab.api.TabPlayer p, boolean sendToggleMessage) {
        setNameTagVisibilityView((TabPlayer) p, ((TabPlayer) p).teamData.invisibleNameTagView, sendToggleMessage);
    }

    @Override
    public void showNameTagVisibilityView(@NonNull me.neznamy.tab.api.TabPlayer p, boolean sendToggleMessage) {
        setNameTagVisibilityView((TabPlayer) p, true, sendToggleMessage);
    }

    @Override
    public void hideNameTagVisibilityView(@NonNull me.neznamy.tab.api.TabPlayer p, boolean sendToggleMessage) {
        setNameTagVisibilityView((TabPlayer) p, false, sendToggleMessage);
    }

    private void setNameTagVisibilityView(@NonNull TabPlayer player, boolean visible, boolean sendToggleMessage) {
        ensureActive();
        if (player.teamData.invisibleNameTagView != visible) return;
        player.teamData.invisibleNameTagView = !visible;
        if (sendToggleMessage) {
            MessageFile messageFile = TAB.getInstance().getConfiguration().getMessages();
            player.sendMessage(visible ? messageFile.getNameTagViewShown() :messageFile.getNameTagViewHidden());
        }
        player.expansionData.setNameTagVisibility(visible);
        for (TabPlayer all : onlinePlayers.getPlayers()) {
            visibilityManager.updateVisibility(all, player);
        }
    }

    @Override
    public boolean hasHiddenNameTagVisibilityView(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        return ((TabPlayer)player).teamData.invisibleNameTagView;
    }
}
