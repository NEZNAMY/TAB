package me.neznamy.tab.shared.features.nametags;

import java.util.*;

import me.neznamy.tab.api.Property;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardTeam;
import me.neznamy.tab.api.team.TeamManager;
import me.neznamy.tab.api.util.Preconditions;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.TabExpansion;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.features.layout.LayoutManager;
import me.neznamy.tab.shared.features.sorting.Sorting;

public class NameTag extends TabFeature implements TeamManager {

    private final boolean invisibleNameTags = TAB.getInstance().getConfiguration().getConfig().getBoolean("scoreboard-teams.invisible-nametags", false);
    private final boolean collisionRule = TAB.getInstance().getConfiguration().getConfig().getBoolean("scoreboard-teams.enable-collision", true);
    private final boolean canSeeFriendlyInvisibles = TAB.getInstance().getConfig().getBoolean("scoreboard-teams.can-see-friendly-invisibles", false);
    private final Sorting sorting = new Sorting(this);
    private final CollisionManager collisionManager = new CollisionManager(this, collisionRule);

    private final Set<TabPlayer> hiddenNameTag = Collections.newSetFromMap(new WeakHashMap<>());
    protected final Set<TabPlayer> teamHandlingPaused = Collections.newSetFromMap(new WeakHashMap<>());
    protected final WeakHashMap<TabPlayer, List<TabPlayer>> hiddenNameTagFor = new WeakHashMap<>();
    private final WeakHashMap<TabPlayer, String> forcedTeamName = new WeakHashMap<>();
    protected final Set<TabPlayer> playersWithInvisibleNameTagView = Collections.newSetFromMap(new WeakHashMap<>());

    private final boolean accepting18x = TAB.getInstance().getServerVersion() == ProtocolVersion.PROXY ||
            TAB.getInstance().getPlatform().getPluginVersion("ViaRewind") != null ||
            TAB.getInstance().getPlatform().getPluginVersion("ProtocolSupport") != null ||
            TAB.getInstance().getServerVersion().getMinorVersion() == 8;

    public NameTag() {
        super("NameTags", "Updating prefix/suffix", "scoreboard-teams");
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.SORTING, sorting);
        if (accepting18x) TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.NAME_TAGS_VISIBILITY, new VisibilityRefresher(this));
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.NAME_TAGS_COLLISION, collisionManager);
        TAB.getInstance().debug(String.format("Loaded NameTag feature with parameters collisionRule=%s, disabledWorlds=%s, disabledServers=%s, invisibleNameTags=%s",
                collisionRule, Arrays.toString(disabledWorlds), Arrays.toString(disabledServers), invisibleNameTags));
    }

    @Override
    public void load(){
        TabExpansion expansion = TAB.getInstance().getPlaceholderManager().getTabExpansion();
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            sorting.constructTeamNames(all);
            updateProperties(all);
            hiddenNameTagFor.put(all, new ArrayList<>());
            if (isDisabled(all.getServer(), all.getWorld())) {
                addDisabledPlayer(all);
                continue;
            }
            registerTeam(all);
            if (expansion != null) expansion.setNameTagVisibility(all, true);
        }
    }

    @Override
    public void unload() {
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            if (!isDisabledPlayer(p)) unregisterTeam(p);
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
        if (refresh) updateTeam(refreshed);
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
        TabExpansion expansion = TAB.getInstance().getPlaceholderManager().getTabExpansion();
        if (expansion != null) {
            expansion.setNameTagVisibility(connectedPlayer, true);
        }
        if (isDisabled(connectedPlayer.getServer(), connectedPlayer.getWorld())) {
            addDisabledPlayer(connectedPlayer);
            return;
        }
        registerTeam(connectedPlayer);
    }

    @Override
    public void onQuit(TabPlayer disconnectedPlayer) {
        if (!isDisabledPlayer(disconnectedPlayer) && !hasTeamHandlingPaused(disconnectedPlayer)) {
            PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam(sorting.getShortTeamName(disconnectedPlayer));
            for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
                if (viewer == disconnectedPlayer) continue; //player who just disconnected
                viewer.sendCustomPacket(packet, TabConstants.PacketCategory.NAMETAGS_TEAM_UNREGISTER);
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
            unregisterTeam(p);
        } else if (!disabledNow && disabledBefore) {
            registerTeam(p);
        } else if (changed) {
            updateTeam(p);
        }
    }

    @Override
    public void hideNametag(TabPlayer player) {
        if (hiddenNameTag.contains(player)) return;
        hiddenNameTag.add(player);
        updateTeamData(player);
    }
    
    @Override
    public void hideNametag(TabPlayer player, TabPlayer viewer) {
        if (hiddenNameTagFor.get(player).contains(viewer)) return;
        hiddenNameTagFor.get(player).add(viewer);
        updateTeamData(player, viewer);
    }

    @Override
    public void showNametag(TabPlayer player) {
        if (!hiddenNameTag.contains(player)) return;
        hiddenNameTag.remove(player);
        updateTeamData(player);
    }
    
    @Override
    public void showNametag(TabPlayer player, TabPlayer viewer) {
        if (!hiddenNameTagFor.get(player).contains(viewer)) return;
        hiddenNameTagFor.get(player).remove(viewer);
        updateTeamData(player, viewer);
    }

    @Override
    public boolean hasHiddenNametag(TabPlayer player) {
        return hiddenNameTag.contains(player);
    }

    @Override
    public boolean hasHiddenNametag(TabPlayer player, TabPlayer viewer) {
        return hiddenNameTagFor.containsKey(player) && hiddenNameTagFor.get(player).contains(viewer);
    }

    @Override
    public void pauseTeamHandling(TabPlayer player) {
        if (teamHandlingPaused.contains(player)) return;
        if (!isDisabledPlayer(player)) unregisterTeam(player);
        teamHandlingPaused.add(player); //adding after, so unregisterTeam method runs
    }

    @Override
    public void resumeTeamHandling(TabPlayer player) {
        if (!teamHandlingPaused.contains(player)) return;
        teamHandlingPaused.remove(player); //removing before, so registerTeam method runs
        if (!isDisabledPlayer(player)) registerTeam(player);
    }

    @Override
    public boolean hasTeamHandlingPaused(TabPlayer player) {
        return teamHandlingPaused.contains(player);
    }

    @Override
    public void forceTeamName(TabPlayer player, String name) {
        if (Objects.equals(forcedTeamName.get(player), name)) return;
        if (name != null && name.length() > 16) throw new IllegalArgumentException("Team name cannot be more than 16 characters long.");
        unregisterTeam(player);
        forcedTeamName.put(player, name);
        registerTeam(player);
        if (name != null) sorting.setTeamNameNote(player, "Set using API");
        RedisSupport redis = (RedisSupport) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.REDIS_BUNGEE);
        if (redis != null) redis.updateTeamName(player, sorting.getShortTeamName(player));
    }

    @Override
    public String getForcedTeamName(TabPlayer player) {
        return forcedTeamName.get(player);
    }

    @Override
    public void setCollisionRule(TabPlayer player, Boolean collision) {
        collisionManager.setCollisionRule(player, collision);
    }

    @Override
    public Boolean getCollisionRule(TabPlayer player) {
        return collisionManager.getCollisionRule(player);
    }
    
    @Override
    public void updateTeamData(TabPlayer p) {
        Property tagPrefix = p.getProperty(TabConstants.Property.TAGPREFIX);
        Property tagSuffix = p.getProperty(TabConstants.Property.TAGSUFFIX);
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            String currentPrefix = tagPrefix.getFormat(viewer);
            String currentSuffix = tagSuffix.getFormat(viewer);
            boolean visible = getTeamVisibility(p, viewer);
            viewer.sendCustomPacket(new PacketPlayOutScoreboardTeam(sorting.getShortTeamName(p), currentPrefix, currentSuffix, translate(visible), translate(collisionManager.getCollision(p)), getTeamOptions()), TabConstants.PacketCategory.NAMETAGS_TEAM_UPDATE);
        }
        RedisSupport redis = (RedisSupport) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.REDIS_BUNGEE);
        if (redis != null) redis.updateNameTag(p, p.getProperty(TabConstants.Property.TAGPREFIX).get(), p.getProperty(TabConstants.Property.TAGSUFFIX).get());
    }

    public void updateTeamData(TabPlayer p, TabPlayer viewer) {
        boolean visible = getTeamVisibility(p, viewer);
        String currentPrefix = p.getProperty(TabConstants.Property.TAGPREFIX).getFormat(viewer);
        String currentSuffix = p.getProperty(TabConstants.Property.TAGSUFFIX).getFormat(viewer);
        viewer.sendCustomPacket(new PacketPlayOutScoreboardTeam(sorting.getShortTeamName(p), currentPrefix, currentSuffix, translate(visible), translate(collisionManager.getCollision(p)), getTeamOptions()), TabConstants.PacketCategory.NAMETAGS_TEAM_UPDATE);
    }

    public void unregisterTeam(TabPlayer p) {
        if (hasTeamHandlingPaused(p) || sorting.getShortTeamName(p) == null) return;
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            viewer.sendCustomPacket(new PacketPlayOutScoreboardTeam(sorting.getShortTeamName(p)), TabConstants.PacketCategory.NAMETAGS_TEAM_UNREGISTER);
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
        viewer.sendCustomPacket(new PacketPlayOutScoreboardTeam(sorting.getShortTeamName(p), replacedPrefix, replacedSuffix, translate(getTeamVisibility(p, viewer)),
                translate(collisionManager.getCollision(p)), Collections.singletonList(p.getNickname()), getTeamOptions()), TabConstants.PacketCategory.NAMETAGS_TEAM_REGISTER);
    }

    private void updateTeam(TabPlayer p) {
        if (sorting.getShortTeamName(p) == null) return; //player not loaded yet
        String oldName = getSorting().getShortTeamName(p);
        getSorting().constructTeamNames(p);
        if (oldName.equals(sorting.getShortTeamName(p))) {
            updateTeamData(p);
        } else {
            unregisterTeam(p);
            LayoutManager layout = (LayoutManager) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.LAYOUT);
            if (layout != null) layout.updateTeamName(p, sorting.getFullTeamName(p));
            registerTeam(p);
            RedisSupport redis = (RedisSupport) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.REDIS_BUNGEE);
            if (redis != null) redis.updateTeamName(p, sorting.getShortTeamName(p));
        }
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

    public Sorting getSorting() {
        return sorting;
    }

    public CollisionManager getCollisionManager() {
        return collisionManager;
    }

    public int getTeamOptions() {
        return canSeeFriendlyInvisibles ? 2 : 0;
    }

    @Override
    public void setPrefix(TabPlayer player, String prefix) {
        Preconditions.checkLoaded(player);
        player.getProperty(TabConstants.Property.TAGPREFIX).setTemporaryValue(prefix);
        updateTeamData(player);
    }

    @Override
    public void setSuffix(TabPlayer player, String suffix) {
        Preconditions.checkLoaded(player);
        player.getProperty(TabConstants.Property.TAGSUFFIX).setTemporaryValue(suffix);
        updateTeamData(player);
    }

    @Override
    public void resetPrefix(TabPlayer player) {
        Preconditions.checkLoaded(player);
        player.getProperty(TabConstants.Property.TAGPREFIX).setTemporaryValue(null);
        updateTeamData(player);
    }

    @Override
    public void resetSuffix(TabPlayer player) {
        Preconditions.checkLoaded(player);
        player.getProperty(TabConstants.Property.TAGSUFFIX).setTemporaryValue(null);
        updateTeamData(player);
    }

    @Override
    public String getCustomPrefix(TabPlayer player) {
        Preconditions.checkLoaded(player);
        return player.getProperty(TabConstants.Property.TAGPREFIX).getTemporaryValue();
    }

    @Override
    public String getCustomSuffix(TabPlayer player) {
        Preconditions.checkLoaded(player);
        return player.getProperty(TabConstants.Property.TAGSUFFIX).getTemporaryValue();
    }

    @Override
    public String getOriginalPrefix(TabPlayer player) {
        Preconditions.checkLoaded(player);
        return player.getProperty(TabConstants.Property.TAGPREFIX).getOriginalRawValue();
    }

    @Override
    public String getOriginalSuffix(TabPlayer player) {
        Preconditions.checkLoaded(player);
        return player.getProperty(TabConstants.Property.TAGSUFFIX).getOriginalRawValue();
    }

    @Override
    public void toggleNameTagVisibilityView(TabPlayer player, boolean sendToggleMessage) {
        if (playersWithInvisibleNameTagView.contains(player)) {
            playersWithInvisibleNameTagView.remove(player);
            if (sendToggleMessage) player.sendMessage(TAB.getInstance().getConfiguration().getMessages().getNameTagsShown(), true);
        } else {
            playersWithInvisibleNameTagView.add(player);
            if (sendToggleMessage) player.sendMessage(TAB.getInstance().getConfiguration().getMessages().getNameTagsHidden(), true);
        }
        TabExpansion expansion = TAB.getInstance().getPlaceholderManager().getTabExpansion();
        if (expansion != null) {
            expansion.setNameTagVisibility(player, !playersWithInvisibleNameTagView.contains(player));
        }
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            updateTeamData(all, player);
        }
    }

    @Override
    public boolean hasHiddenNameTagVisibilityView(TabPlayer player) {
        return playersWithInvisibleNameTagView.contains(player);
    }
}