package me.neznamy.tab.shared.features.nametags.unlimited;

import me.neznamy.tab.api.ArmorStandManager;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.team.UnlimitedNametagManager;
import me.neznamy.tab.api.util.Preconditions;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.shared.features.TabExpansion;
import me.neznamy.tab.shared.features.nametags.NameTag;

import java.util.*;
import java.util.function.BiFunction;

public abstract class NameTagX extends NameTag implements UnlimitedNametagManager {

    //config options
    private final boolean markerFor18x = TAB.getInstance().getConfiguration().getConfig().getBoolean("scoreboard-teams.unlimited-nametag-mode.use-marker-tag-for-1-8-x-clients", false);
    private final boolean disableOnBoats = TAB.getInstance().getConfiguration().getConfig().getBoolean("scoreboard-teams.unlimited-nametag-mode.disable-on-boats", true);
    private final List<String> disabledUnlimitedWorlds = TAB.getInstance().getConfiguration().getConfig().getStringList("scoreboard-teams.unlimited-nametag-mode.disable-in-worlds", new ArrayList<>());
    private final List<String> disabledUnlimitedServers = TAB.getInstance().getConfiguration().getConfig().getStringList("scoreboard-teams.unlimited-nametag-mode.disable-in-servers", new ArrayList<>());
    private final List<String> dynamicLines = new ArrayList<>(TAB.getInstance().getConfiguration().getConfig().getStringList("scoreboard-teams.unlimited-nametag-mode.dynamic-lines", Arrays.asList(TabConstants.Property.ABOVENAME, TabConstants.Property.NAMETAG, TabConstants.Property.BELOWNAME, "another")));
    private final Map<String, Object> staticLines = TAB.getInstance().getConfiguration().getConfig().getConfigurationSection("scoreboard-teams.unlimited-nametag-mode.static-lines");
    private final boolean armorStandsAlwaysVisible = TAB.getInstance().getConfiguration().getSecretOption("scoreboard-teams.unlimited-nametag-mode.always-visible", false);

    private final Set<TabPlayer> playersDisabledWithAPI = Collections.newSetFromMap(new WeakHashMap<>());
    private final Set<TabPlayer> disabledUnlimitedPlayers = Collections.newSetFromMap(new WeakHashMap<>());
    protected final Map<TabPlayer, ArmorStandManager> armorStandManagerMap = new WeakHashMap<>();
    private final String[] disabledUnlimitedWorldsArray = disabledUnlimitedWorlds.toArray(new String[0]);
    private final boolean unlimitedWorldWhitelistMode = disabledUnlimitedWorlds.contains("WHITELIST");
    private final String[] disabledUnlimitedServersArray = disabledUnlimitedServers.toArray(new String[0]);
    private final boolean unlimitedServerWhitelistMode = disabledUnlimitedServers.contains("WHITELIST");
    private final Set<TabPlayer> playersPreviewingNametag = Collections.newSetFromMap(new WeakHashMap<>());
    private final BiFunction<NameTagX, TabPlayer, ArmorStandManager> armorStandFunction;

    public NameTagX(BiFunction<NameTagX, TabPlayer, ArmorStandManager> armorStandFunction) {
        this.armorStandFunction = armorStandFunction;
        Collections.reverse(dynamicLines);
        if (invisibleNameTags) {
            TAB.getInstance().getErrorManager().startupWarn("Unlimited nametag mode is enabled as well as invisible nametags. These 2 options are mutually exclusive.");
            TAB.getInstance().getErrorManager().startupWarn("If you want nametags to be invisible, you don't need unlimited nametag mode at all.");
            TAB.getInstance().getErrorManager().startupWarn("If you want enhanced nametags without limits, making them invisible would defeat the purpose.");
        }
        TAB.getInstance().debug(String.format("Loaded Unlimited NameTag feature with parameters markerFor18x=%s, disableOnBoats=%s, disabledUnlimitedServers=%s, disabledUnlimitedWorlds=%s",
                markerFor18x, disableOnBoats, disabledUnlimitedServers, disabledUnlimitedWorlds));
    }

    public boolean isUnlimitedDisabled(String server, String world) {
        boolean contains = contains(disabledUnlimitedServersArray, server);
        if (unlimitedServerWhitelistMode) contains = !contains;
        if (contains) return true;
        contains = contains(disabledUnlimitedWorldsArray, world);
        if (unlimitedWorldWhitelistMode) contains = !contains;
        return contains;
    }

    public ArmorStandManager getArmorStandManager(TabPlayer player) {
        return armorStandManagerMap.get(player);
    }

    public boolean isPlayerDisabled(TabPlayer p) {
        return isDisabledPlayer(p) || disabledUnlimitedPlayers.contains(p) || hasTeamHandlingPaused(p) || hasDisabledArmorStands(p);
    }

    @Override
    public void load() {
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            updateProperties(all);
            armorStandManagerMap.put(all, armorStandFunction.apply(this, all));
            if (isUnlimitedDisabled(all.getServer(), all.getWorld())) {
                disabledUnlimitedPlayers.add(all);
            }
            TabExpansion expansion = TAB.getInstance().getPlaceholderManager().getTabExpansion();
            if (expansion != null) expansion.setNameTagPreview(all, false);
        }
        super.load();
    }

    @Override
    public void onJoin(TabPlayer connectedPlayer) {
        if (isUnlimitedDisabled(connectedPlayer.getServer(), connectedPlayer.getWorld()))
            disabledUnlimitedPlayers.add(connectedPlayer);
        super.onJoin(connectedPlayer);
        armorStandManagerMap.put(connectedPlayer, armorStandFunction.apply(this, connectedPlayer));
        TabExpansion expansion = TAB.getInstance().getPlaceholderManager().getTabExpansion();
        if (expansion != null) expansion.setNameTagPreview(connectedPlayer, false);
    }

    @Override
    public void refresh(TabPlayer refreshed, boolean force) {
        super.refresh(refreshed, force);
        if (isPlayerDisabled(refreshed)) return;
        getArmorStandManager(refreshed).refresh(force);
    }

    @Override
    public void unload() {
        super.unload();
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            getArmorStandManager(p).destroy();
        }
    }

    public boolean isMarkerFor18x() {
        return markerFor18x;
    }

    public Set<TabPlayer> getDisabledUnlimitedPlayers() {
        return disabledUnlimitedPlayers;
    }

    public boolean isDisableOnBoats() {
        return disableOnBoats;
    }

    public List<String> getDynamicLines() {
        return dynamicLines;
    }

    public Map<String, Object> getStaticLines() {
        return staticLines;
    }

    public List<String> getDisabledUnlimitedWorlds() {
        return disabledUnlimitedWorlds;
    }

    public void toggleNametagPreview(TabPlayer player, boolean sendToggleMessage) {
        if (playersPreviewingNametag.contains(player)) {
            setNameTagPreview(player, false);
            if (sendToggleMessage) player.sendMessage(TAB.getInstance().getConfiguration().getMessages().getNametagPreviewOff(), true);
            playersPreviewingNametag.remove(player);
        } else {
            setNameTagPreview(player, true);
            if (sendToggleMessage) player.sendMessage(TAB.getInstance().getConfiguration().getMessages().getNametagPreviewOn(), true);
            playersPreviewingNametag.add(player);
        }
        TabExpansion expansion = TAB.getInstance().getPlaceholderManager().getTabExpansion();
        if (expansion != null) expansion.setNameTagPreview(player, isPreviewingNametag(player));
    }

    public boolean isPreviewingNametag(TabPlayer player) {
        return playersPreviewingNametag.contains(player);
    }

    @Override
    public void onWorldChange(TabPlayer p, String from, String to) {
        super.onWorldChange(p, from , to);
        if (isUnlimitedDisabled(p.getServer(), to)) {
            if (getDisabledUnlimitedPlayers().add(p)) updateTeamData(p);
        } else {
            if (getDisabledUnlimitedPlayers().remove(p)) {
                updateTeamData(p);
            }
            getArmorStandManager(p).refresh(true);
        }
    }

    /**
     * Updates raw values of properties for specified player
     *
     * @param   p
     *          player to update
     */
    @Override
    public boolean updateProperties(TabPlayer p) {
        boolean changed = super.updateProperties(p);
        if (p.loadPropertyFromConfig(this, TabConstants.Property.CUSTOMTAGNAME, p.getName())) changed = true;
        if (p.setProperty(this, TabConstants.Property.NAMETAG, p.getProperty(TabConstants.Property.TAGPREFIX).getCurrentRawValue() +
                p.getProperty(TabConstants.Property.CUSTOMTAGNAME).getCurrentRawValue() + p.getProperty(TabConstants.Property.TAGSUFFIX).getCurrentRawValue())) changed = true;
        for (String property : dynamicLines) {
            if (!property.equals(TabConstants.Property.NAMETAG) && p.loadPropertyFromConfig(this, property)) changed = true;
        }
        for (String property : staticLines.keySet()) {
            if (!property.equals(TabConstants.Property.NAMETAG) && p.loadPropertyFromConfig(this, property)) changed = true;
        }
        return changed;
    }

    @Override
    public boolean getTeamVisibility(TabPlayer p, TabPlayer viewer) {
        if (p.hasInvisibilityPotion()) return false; //1.8.x client sided bug
        if (playersWithInvisibleNameTagView.contains(viewer)) return false;
        return isOnBoat(p) || isPlayerDisabled(p);
    }

    @Override
    public String getFeatureName() {
        return "Unlimited NameTags";
    }

    public List<String> getDisabledUnlimitedServers() {
        return disabledUnlimitedServers;
    }

    public boolean isArmorStandsAlwaysVisible() {
        return armorStandsAlwaysVisible;
    }

    public abstract boolean isOnBoat(TabPlayer player);

    public abstract void setNameTagPreview(TabPlayer player, boolean status);

    public abstract void resumeArmorStands(TabPlayer player);

    public abstract void pauseArmorStands(TabPlayer player);

    public abstract void updateNameTagVisibilityView(TabPlayer player);

    /* NameTag override */

    @Override
    public void hideNametag(TabPlayer player, TabPlayer viewer) {
        if (hiddenNameTagFor.get(player).contains(viewer)) return;
        hiddenNameTagFor.get(player).add(viewer);
        updateTeamData(player, viewer);
        pauseArmorStands(player);
    }

    @Override
    public void showNametag(TabPlayer player, TabPlayer viewer) {
        if (!hiddenNameTagFor.get(player).contains(viewer)) return;
        hiddenNameTagFor.get(player).remove(viewer);
        updateTeamData(player, viewer);
        resumeArmorStands(player);
    }

    /* UnlimitedNametagManager implementation */

    @Override
    public void disableArmorStands(TabPlayer player) {
        Preconditions.checkLoaded(player);
        if (playersDisabledWithAPI.contains(player)) return;
        playersDisabledWithAPI.add(player);
        pauseArmorStands(player);
        updateTeamData(player);
    }

    @Override
    public void enableArmorStands(TabPlayer player) {
        Preconditions.checkLoaded(player);
        if (!playersDisabledWithAPI.contains(player)) return;
        playersDisabledWithAPI.remove(player);
        resumeArmorStands(player);
        updateTeamData(player);
    }

    @Override
    public boolean hasDisabledArmorStands(TabPlayer player) {
        return playersDisabledWithAPI.contains(player);
    }

    @Override
    public void setName(TabPlayer player, String customName) {
        Preconditions.checkLoaded(player);
        player.getProperty(TabConstants.Property.CUSTOMTAGNAME).setTemporaryValue(customName);
        rebuildNameTagLine(player);
        getArmorStandManager(player).refresh(true);
    }

    @Override
    public void setLine(TabPlayer player, String line, String value) {
        Preconditions.checkLoaded(player);
        if (!getDefinedLines().contains(line)) throw new IllegalArgumentException("\"" + line + "\" is not a defined line. Defined lines: " + getDefinedLines());
        player.getProperty(line).setTemporaryValue(value);
        getArmorStandManager(player).refresh(true);
    }

    @Override
    public void resetName(TabPlayer player) {
        Preconditions.checkLoaded(player);
        player.getProperty(TabConstants.Property.CUSTOMTAGNAME).setTemporaryValue(null);
        rebuildNameTagLine(player);
        getArmorStandManager(player).refresh(true);
    }

    @Override
    public void resetLine(TabPlayer player, String line) {
        Preconditions.checkLoaded(player);
        player.getProperty(line).setTemporaryValue(null);
        getArmorStandManager(player).refresh(true);
    }

    @Override
    public String getCustomName(TabPlayer player) {
        Preconditions.checkLoaded(player);
        return player.getProperty(TabConstants.Property.CUSTOMTAGNAME).getTemporaryValue();
    }

    @Override
    public String getCustomLineValue(TabPlayer player, String line) {
        Preconditions.checkLoaded(player);
        return player.getProperty(line).getTemporaryValue();
    }

    @Override
    public String getOriginalName(TabPlayer player) {
        Preconditions.checkLoaded(player);
        return player.getProperty(TabConstants.Property.CUSTOMTAGNAME).getOriginalRawValue();
    }

    @Override
    public String getOriginalLineValue(TabPlayer player, String line) {
        Preconditions.checkLoaded(player);
        return player.getProperty(line).getOriginalRawValue();
    }

    @Override
    public List<String> getDefinedLines() {
        List<String> lines = new ArrayList<>(dynamicLines);
        lines.addAll(staticLines.keySet());
        return lines;
    }

    private void rebuildNameTagLine(TabPlayer player) {
        player.setProperty(this, TabConstants.Property.NAMETAG, player.getProperty(TabConstants.Property.TAGPREFIX).getCurrentRawValue() +
                player.getProperty(TabConstants.Property.CUSTOMTAGNAME).getCurrentRawValue() + player.getProperty(TabConstants.Property.TAGSUFFIX).getCurrentRawValue());
    }

    /* TeamManager override */

    @Override
    public void setPrefix(TabPlayer player, String prefix) {
        Preconditions.checkLoaded(player);
        super.setPrefix(player, prefix);
        rebuildNameTagLine(player);
        getArmorStandManager(player).refresh(true);
    }

    @Override
    public void setSuffix(TabPlayer player, String suffix) {
        Preconditions.checkLoaded(player);
        super.setSuffix(player, suffix);
        rebuildNameTagLine(player);
        getArmorStandManager(player).refresh(true);
    }

    @Override
    public void resetPrefix(TabPlayer player) {
        Preconditions.checkLoaded(player);
        super.resetPrefix(player);
        rebuildNameTagLine(player);
        getArmorStandManager(player).refresh(true);
    }

    @Override
    public void resetSuffix(TabPlayer player) {
        Preconditions.checkLoaded(player);
        super.resetSuffix(player);
        rebuildNameTagLine(player);
        getArmorStandManager(player).refresh(true);
    }

    @Override
    public void pauseTeamHandling(TabPlayer player) {
        Preconditions.checkLoaded(player);
        if (teamHandlingPaused.contains(player)) return;
        if (!isDisabledPlayer(player)) unregisterTeam(player, getSorting().getShortTeamName(player));
        teamHandlingPaused.add(player); //adding after, so unregisterTeam method runs
        pauseArmorStands(player);
    }

    @Override
    public void resumeTeamHandling(TabPlayer player) {
        Preconditions.checkLoaded(player);
        if (!teamHandlingPaused.contains(player)) return;
        teamHandlingPaused.remove(player); //removing before, so registerTeam method runs
        if (!isDisabledPlayer(player)) registerTeam(player);
        resumeArmorStands(player);
    }

    @Override
    public void toggleNameTagVisibilityView(TabPlayer player, boolean sendToggleMessage) {
        super.toggleNameTagVisibilityView(player, sendToggleMessage);
        updateNameTagVisibilityView(player);
    }
}
