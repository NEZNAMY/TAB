package me.neznamy.tab.shared.features.nametags;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.feature.*;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.team.TeamManager;
import me.neznamy.tab.api.util.Preconditions;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.features.sorting.Sorting;

import java.util.*;

public class NameTag extends TabFeature implements TeamManager, JoinListener, QuitListener, LoginPacketListener,
        Loadable, UnLoadable, WorldSwitchListener, ServerSwitchListener, Refreshable {

    @Getter private final String featureName = "NameTags";
    @Getter private final String refreshDisplayName = "Updating prefix/suffix";
    protected final boolean invisibleNameTags = TAB.getInstance().getConfiguration().getConfig().getBoolean("scoreboard-teams.invisible-nametags", false);
    private final boolean collisionRule = TAB.getInstance().getConfiguration().getConfig().getBoolean("scoreboard-teams.enable-collision", true);
    private final boolean canSeeFriendlyInvisibles = TAB.getInstance().getConfig().getBoolean("scoreboard-teams.can-see-friendly-invisibles", false);
    @Getter private final Sorting sorting = (Sorting) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.SORTING);
    @Getter private final CollisionManager collisionManager = new CollisionManager(this, collisionRule);
    @Getter private final int teamOptions = canSeeFriendlyInvisibles ? 2 : 0;

    private final Set<TabPlayer> hiddenNameTag = Collections.newSetFromMap(new WeakHashMap<>());
    protected final Set<TabPlayer> teamHandlingPaused = Collections.newSetFromMap(new WeakHashMap<>());
    protected final WeakHashMap<TabPlayer, List<TabPlayer>> hiddenNameTagFor = new WeakHashMap<>();
    private final WeakHashMap<TabPlayer, String> forcedTeamName = new WeakHashMap<>();
    protected final Set<TabPlayer> playersWithInvisibleNameTagView = Collections.newSetFromMap(new WeakHashMap<>());

    private RedisSupport redis;

    private final boolean accepting18x = TAB.getInstance().getServerVersion() == ProtocolVersion.PROXY ||
            TAB.getInstance().getPlatform().getPluginVersion(TabConstants.Plugin.VIAREWIND) != null ||
            TAB.getInstance().getPlatform().getPluginVersion(TabConstants.Plugin.PROTOCOL_SUPPORT) != null ||
            TAB.getInstance().getServerVersion().getMinorVersion() == 8;

    public NameTag() {
        super("scoreboard-teams");
    }

    @Override
    public void load() {
        // RedisSupport is instantiated after NameTags, so must be loaded after
        redis = (RedisSupport) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.REDIS_BUNGEE);
        if (accepting18x) TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.NAME_TAGS_VISIBILITY, new VisibilityRefresher(this));
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.NAME_TAGS_COLLISION, collisionManager);
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            updateProperties(all);
            hiddenNameTagFor.put(all, new ArrayList<>());
            if (isDisabled(all.getServer(), all.getWorld())) {
                addDisabledPlayer(all);
                continue;
            }
            TAB.getInstance().getPlaceholderManager().getTabExpansion().setNameTagVisibility(all, true);
        }
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            for (TabPlayer target : TAB.getInstance().getOnlinePlayers()) {
                if (!isDisabledPlayer(target)) registerTeam(target, viewer);
            }
        }
    }

    @Override
    public void unload() {
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            for (TabPlayer target : TAB.getInstance().getOnlinePlayers()) {
                if (hasTeamHandlingPaused(target)) return;
                viewer.getScoreboard().unregisterTeam(sorting.getShortTeamName(target));
            }
        }
    }

    @Override
    public void onLoginPacket(TabPlayer packetReceiver) {
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (!all.isLoaded()) continue;
            if (!isDisabledPlayer(all)) registerTeam(all, packetReceiver);
        }
    }

    @Override
    public void refresh(TabPlayer refreshed, boolean force) {
        if (isDisabledPlayer(refreshed)) return;
        boolean refresh;
        if (force) {
            updateProperties(refreshed);
            refresh = true;
        } else {
            boolean prefix = refreshed.getProperty(TabConstants.Property.TAGPREFIX).update();
            boolean suffix = refreshed.getProperty(TabConstants.Property.TAGSUFFIX).update();
            refresh = prefix || suffix;
        }
        if (refresh) updateTeamData(refreshed);
    }

    @Override
    public void onJoin(TabPlayer connectedPlayer) {
        sorting.constructTeamNames(connectedPlayer);
        updateProperties(connectedPlayer);
        hiddenNameTagFor.put(connectedPlayer, new ArrayList<>());
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (all == connectedPlayer) continue; //avoiding double registration
            if (!isDisabledPlayer(all)) {
                registerTeam(all, connectedPlayer);
            }
        }
        TAB.getInstance().getPlaceholderManager().getTabExpansion().setNameTagVisibility(connectedPlayer, true);
        if (isDisabled(connectedPlayer.getServer(), connectedPlayer.getWorld())) {
            addDisabledPlayer(connectedPlayer);
            return;
        }
        registerTeam(connectedPlayer);
    }

    @Override
    public void onQuit(TabPlayer disconnectedPlayer) {
        if (!isDisabledPlayer(disconnectedPlayer) && !hasTeamHandlingPaused(disconnectedPlayer)) {
            for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
                if (viewer == disconnectedPlayer) continue; //player who just disconnected
                viewer.getScoreboard().unregisterTeam(sorting.getShortTeamName(disconnectedPlayer));
            }
        }
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (all == disconnectedPlayer) continue;
            List<TabPlayer> list = hiddenNameTagFor.get(all);
            if (list != null) list.remove(disconnectedPlayer); //clearing memory from API method
        }
    }

    @Override
    public void onServerChange(TabPlayer p, String from, String to) {
        onWorldChange(p, null, null);
    }

    @Override
    public void onWorldChange(TabPlayer p, String from, String to) {
        boolean disabledBefore = isDisabledPlayer(p);
        boolean disabledNow = false;
        if (isDisabled(p.getServer(), p.getWorld())) {
            disabledNow = true;
            addDisabledPlayer(p);
        } else {
            removeDisabledPlayer(p);
        }
        boolean changed = updateProperties(p);
        if (disabledNow && !disabledBefore) {
            unregisterTeam(p, sorting.getShortTeamName(p));
        } else if (!disabledNow && disabledBefore) {
            registerTeam(p);
        } else if (changed) {
            updateTeamData(p);
        }
    }

    @Override
    public void hideNametag(@NonNull TabPlayer player) {
        if (hiddenNameTag.contains(player)) return;
        hiddenNameTag.add(player);
        updateTeamData(player);
    }
    
    @Override
    public void hideNametag(@NonNull TabPlayer player, @NonNull TabPlayer viewer) {
        if (hiddenNameTagFor.get(player).contains(viewer)) return;
        hiddenNameTagFor.get(player).add(viewer);
        updateTeamData(player, viewer);
    }

    @Override
    public void showNametag(@NonNull TabPlayer player) {
        if (!hiddenNameTag.contains(player)) return;
        hiddenNameTag.remove(player);
        updateTeamData(player);
    }
    
    @Override
    public void showNametag(@NonNull TabPlayer player, @NonNull TabPlayer viewer) {
        if (!hiddenNameTagFor.get(player).contains(viewer)) return;
        hiddenNameTagFor.get(player).remove(viewer);
        updateTeamData(player, viewer);
    }

    @Override
    public boolean hasHiddenNametag(@NonNull TabPlayer player) {
        return hiddenNameTag.contains(player);
    }

    @Override
    public boolean hasHiddenNametag(@NonNull TabPlayer player, @NonNull TabPlayer viewer) {
        return hiddenNameTagFor.containsKey(player) && hiddenNameTagFor.get(player).contains(viewer);
    }

    @Override
    public void pauseTeamHandling(@NonNull TabPlayer player) {
        if (teamHandlingPaused.contains(player)) return;
        if (!isDisabledPlayer(player)) unregisterTeam(player, sorting.getShortTeamName(player));
        teamHandlingPaused.add(player); //adding after, so unregisterTeam method runs
    }

    @Override
    public void resumeTeamHandling(@NonNull TabPlayer player) {
        if (!teamHandlingPaused.contains(player)) return;
        teamHandlingPaused.remove(player); //removing before, so registerTeam method runs
        if (!isDisabledPlayer(player)) registerTeam(player);
    }

    @Override
    public boolean hasTeamHandlingPaused(@NonNull TabPlayer player) {
        return teamHandlingPaused.contains(player);
    }

    @Override
    public void forceTeamName(@NonNull TabPlayer player, String name) {
        if (Objects.equals(forcedTeamName.get(player), name)) return;
        if (name != null && name.length() > 16) throw new IllegalArgumentException("Team name cannot be more than 16 characters long.");
        unregisterTeam(player, sorting.getShortTeamName(player));
        forcedTeamName.put(player, name);
        registerTeam(player);
        if (name != null) sorting.setTeamNameNote(player, "Set using API");
        if (redis != null) redis.updateTeamName(player, sorting.getShortTeamName(player));
    }

    @Override
    public String getForcedTeamName(@NonNull TabPlayer player) {
        return forcedTeamName.get(player);
    }

    @Override
    public void setCollisionRule(@NonNull TabPlayer player, Boolean collision) {
        collisionManager.setCollisionRule(player, collision);
    }

    @Override
    public Boolean getCollisionRule(@NonNull TabPlayer player) {
        return collisionManager.getCollisionRule(player);
    }
    
    @Override
    public void updateTeamData(@NonNull TabPlayer p) {
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            updateTeamData(p, viewer);
        }
        if (redis != null) redis.updateNameTag(p, p.getProperty(TabConstants.Property.TAGPREFIX).get(), p.getProperty(TabConstants.Property.TAGSUFFIX).get());
    }

    public void updateTeamData(TabPlayer p, TabPlayer viewer) {
        boolean visible = getTeamVisibility(p, viewer);
        String currentPrefix = p.getProperty(TabConstants.Property.TAGPREFIX).getFormat(viewer);
        String currentSuffix = p.getProperty(TabConstants.Property.TAGSUFFIX).getFormat(viewer);
        viewer.getScoreboard().updateTeam(sorting.getShortTeamName(p), currentPrefix, currentSuffix,
                translate(visible), translate(collisionManager.getCollision(p)), getTeamOptions());
    }

    public void unregisterTeam(TabPlayer p, String teamName) {
        if (hasTeamHandlingPaused(p)) return;
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            viewer.getScoreboard().unregisterTeam(teamName);
        }
    }

    public void registerTeam(TabPlayer p) {
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            registerTeam(p, viewer);
        }
    }

    private void registerTeam(TabPlayer p, TabPlayer viewer) {
        if (hasTeamHandlingPaused(p)) return;
        String replacedPrefix = p.getProperty(TabConstants.Property.TAGPREFIX).getFormat(viewer);
        String replacedSuffix = p.getProperty(TabConstants.Property.TAGSUFFIX).getFormat(viewer);
        viewer.getScoreboard().registerTeam(sorting.getShortTeamName(p), replacedPrefix, replacedSuffix, translate(getTeamVisibility(p, viewer)),
                translate(collisionManager.getCollision(p)), Collections.singletonList(p.getNickname()), getTeamOptions());
    }

    public String translate(boolean b) {
        return b ? "always" : "never";
    }
    
    protected boolean updateProperties(TabPlayer p) {
        boolean changed = p.loadPropertyFromConfig(this, TabConstants.Property.TAGPREFIX);
        if (p.loadPropertyFromConfig(this, TabConstants.Property.TAGSUFFIX)) changed = true;
        return changed;
    }

    public boolean getTeamVisibility(TabPlayer p, TabPlayer viewer) {
        return !hasHiddenNametag(p) && !hasHiddenNametag(p, viewer) && !invisibleNameTags
                && (!accepting18x || !p.hasInvisibilityPotion()) && !playersWithInvisibleNameTagView.contains(viewer);
    }

    @Override
    public void setPrefix(@NonNull TabPlayer player, String prefix) {
        Preconditions.checkLoaded(player);
        player.getProperty(TabConstants.Property.TAGPREFIX).setTemporaryValue(prefix);
        updateTeamData(player);
    }

    @Override
    public void setSuffix(@NonNull TabPlayer player, String suffix) {
        Preconditions.checkLoaded(player);
        player.getProperty(TabConstants.Property.TAGSUFFIX).setTemporaryValue(suffix);
        updateTeamData(player);
    }

    @Override
    public void resetPrefix(@NonNull TabPlayer player) {
        Preconditions.checkLoaded(player);
        player.getProperty(TabConstants.Property.TAGPREFIX).setTemporaryValue(null);
        updateTeamData(player);
    }

    @Override
    public void resetSuffix(@NonNull TabPlayer player) {
        Preconditions.checkLoaded(player);
        player.getProperty(TabConstants.Property.TAGSUFFIX).setTemporaryValue(null);
        updateTeamData(player);
    }

    @Override
    public String getCustomPrefix(@NonNull TabPlayer player) {
        Preconditions.checkLoaded(player);
        return player.getProperty(TabConstants.Property.TAGPREFIX).getTemporaryValue();
    }

    @Override
    public String getCustomSuffix(@NonNull TabPlayer player) {
        Preconditions.checkLoaded(player);
        return player.getProperty(TabConstants.Property.TAGSUFFIX).getTemporaryValue();
    }

    @Override
    public @NonNull String getOriginalPrefix(@NonNull TabPlayer player) {
        Preconditions.checkLoaded(player);
        return player.getProperty(TabConstants.Property.TAGPREFIX).getOriginalRawValue();
    }

    @Override
    public @NonNull String getOriginalSuffix(@NonNull TabPlayer player) {
        Preconditions.checkLoaded(player);
        return player.getProperty(TabConstants.Property.TAGSUFFIX).getOriginalRawValue();
    }

    @Override
    public void toggleNameTagVisibilityView(@NonNull TabPlayer player, boolean sendToggleMessage) {
        if (playersWithInvisibleNameTagView.contains(player)) {
            playersWithInvisibleNameTagView.remove(player);
            if (sendToggleMessage) player.sendMessage(TAB.getInstance().getConfiguration().getMessages().getNameTagsShown(), true);
        } else {
            playersWithInvisibleNameTagView.add(player);
            if (sendToggleMessage) player.sendMessage(TAB.getInstance().getConfiguration().getMessages().getNameTagsHidden(), true);
        }
        TAB.getInstance().getPlaceholderManager().getTabExpansion().setNameTagVisibility(player, !playersWithInvisibleNameTagView.contains(player));
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            updateTeamData(all, player);
        }
    }

    @Override
    public boolean hasHiddenNameTagVisibilityView(@NonNull TabPlayer player) {
        return playersWithInvisibleNameTagView.contains(player);
    }
}