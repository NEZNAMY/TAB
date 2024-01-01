package me.neznamy.tab.shared.features.nametags.unlimited;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.api.nametag.UnlimitedNameTagManager;
import me.neznamy.tab.shared.features.types.DisableChecker;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.util.Preconditions;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.nametags.NameTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;

public abstract class NameTagX extends NameTag implements UnlimitedNameTagManager {

    //config options
    @Getter private final boolean disableOnBoats = config().getBoolean("scoreboard-teams.unlimited-nametag-mode.disable-on-boats", true);
    @Getter private final List<String> dynamicLines = new ArrayList<>(config().getStringList("scoreboard-teams.unlimited-nametag-mode.dynamic-lines", Arrays.asList(TabConstants.Property.ABOVENAME, TabConstants.Property.NAMETAG, TabConstants.Property.BELOWNAME, "another")));
    @Getter private final Map<String, Object> staticLines = config().getConfigurationSection("scoreboard-teams.unlimited-nametag-mode.static-lines");
    @Getter private final boolean armorStandsAlwaysVisible = TAB.getInstance().getConfiguration().getSecretOption("scoreboard-teams.unlimited-nametag-mode.always-visible", false);

    @Getter protected final String featureName = "Unlimited NameTags";
    private final Set<me.neznamy.tab.api.TabPlayer> playersDisabledWithAPI = Collections.newSetFromMap(new WeakHashMap<>());
    protected final Map<TabPlayer, ArmorStandManager> armorStandManagerMap = new WeakHashMap<>();
    private final Set<TabPlayer> playersPreviewingNameTag = Collections.newSetFromMap(new WeakHashMap<>());
    private final BiFunction<NameTagX, TabPlayer, ArmorStandManager> armorStandFunction;
    @Getter private final DisableChecker unlimitedDisableChecker;

    protected NameTagX(@NonNull BiFunction<NameTagX, TabPlayer, ArmorStandManager> armorStandFunction) {
        this.armorStandFunction = armorStandFunction;
        Collections.reverse(dynamicLines);
        Condition disableCondition = Condition.getCondition(config().getString("scoreboard-teams.unlimited-nametag-mode.disable-condition"));
        unlimitedDisableChecker = new DisableChecker(featureName, disableCondition, this::onUnlimitedDisableConditionChange);
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.UNLIMITED_NAME_TAGS + "-Condition", unlimitedDisableChecker);
    }

    public ArmorStandManager getArmorStandManager(@NonNull TabPlayer player) {
        return armorStandManagerMap.get(player);
    }

    public boolean isPlayerDisabled(@NonNull TabPlayer p) {
        return getDisableChecker().isDisabledPlayer(p) || unlimitedDisableChecker.isDisabledPlayer(p) || hasTeamHandlingPaused(p) || hasDisabledArmorStands(p);
    }

    @Override
    public void load() {
        if (invisibleNameTags) {
            TAB.getInstance().getConfigHelper().startup().invisibleAndUnlimitedNameTagsAreMutuallyExclusive();
        }
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            updateProperties(all);
            armorStandManagerMap.put(all, armorStandFunction.apply(this, all));
            if (unlimitedDisableChecker.isDisableConditionMet(all)) {
                addDisabledPlayer(all);
            }
            TAB.getInstance().getPlaceholderManager().getTabExpansion().setNameTagPreview(all, false);
        }
        super.load();
    }

    @Override
    public void onJoin(@NotNull TabPlayer connectedPlayer) {
        if (unlimitedDisableChecker.isDisableConditionMet(connectedPlayer))
            addDisabledPlayer(connectedPlayer);
        super.onJoin(connectedPlayer);
        armorStandManagerMap.put(connectedPlayer, armorStandFunction.apply(this, connectedPlayer));
        TAB.getInstance().getPlaceholderManager().getTabExpansion().setNameTagPreview(connectedPlayer, false);
    }

    public void addDisabledPlayer(@NotNull TabPlayer player) {
        unlimitedDisableChecker.addDisabledPlayer(player);
    }

    @Override
    public void refresh(@NotNull TabPlayer refreshed, boolean force) {
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

    public void toggleNameTagPreview(TabPlayer player, boolean sendToggleMessage) {
        if (playersPreviewingNameTag.contains(player)) {
            setNameTagPreview(player, false);
            if (sendToggleMessage) player.sendMessage(TAB.getInstance().getConfiguration().getMessages().getNametagPreviewOff(), true);
            playersPreviewingNameTag.remove(player);
        } else {
            setNameTagPreview(player, true);
            if (sendToggleMessage) player.sendMessage(TAB.getInstance().getConfiguration().getMessages().getNametagPreviewOn(), true);
            playersPreviewingNameTag.add(player);
        }
        TAB.getInstance().getPlaceholderManager().getTabExpansion().setNameTagPreview(player, isPreviewingNameTag(player));
    }

    public boolean isPreviewingNameTag(@NonNull TabPlayer player) {
        return playersPreviewingNameTag.contains(player);
    }

    public void onUnlimitedDisableConditionChange(TabPlayer p, boolean disabledNow) {
        if (!getDisableChecker().isDisabledPlayer(p)) updateTeamData(p);
        getArmorStandManager(p).refresh(true);
    }

    /**
     * Updates raw values of properties for specified player
     *
     * @param   p
     *          player to update
     */
    @Override
    public boolean updateProperties(@NonNull TabPlayer p) {
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
    public boolean getTeamVisibility(@NonNull TabPlayer p, @NonNull TabPlayer viewer) {
        if (p.hasInvisibilityPotion()) return false; //1.8.x client sided bug
        if (playersWithInvisibleNameTagView.contains(viewer)) return false;
        return isOnBoat(p) || isPlayerDisabled(p);
    }

    public abstract boolean isOnBoat(@NonNull TabPlayer player);

    public abstract void setNameTagPreview(@NonNull TabPlayer player, boolean status);

    public abstract void resumeArmorStands(@NonNull TabPlayer player);

    public abstract void pauseArmorStands(@NonNull TabPlayer player);

    public abstract void updateNameTagVisibilityView(@NonNull TabPlayer player);

    /* NameTag override */

    @Override
    public void hideNameTag(@NonNull me.neznamy.tab.api.TabPlayer player, @NonNull me.neznamy.tab.api.TabPlayer viewer) {
        if (hiddenNameTagFor.get(player).contains(viewer)) return;
        hiddenNameTagFor.get(player).add(viewer);
        updateTeamData((TabPlayer) player, (TabPlayer) viewer);
        pauseArmorStands((TabPlayer) player);
    }

    @Override
    public void showNameTag(@NonNull me.neznamy.tab.api.TabPlayer player, @NonNull me.neznamy.tab.api.TabPlayer viewer) {
        if (!hiddenNameTagFor.get(player).contains(viewer)) return;
        hiddenNameTagFor.get(player).remove(viewer);
        updateTeamData((TabPlayer) player, (TabPlayer) viewer);
        resumeArmorStands((TabPlayer) player);
    }

    /* UnlimitedNameTagManager implementation */

    @Override
    public void disableArmorStands(me.neznamy.tab.api.@NonNull TabPlayer player) {
        Preconditions.checkLoaded(player);
        if (playersDisabledWithAPI.contains(player)) return;
        playersDisabledWithAPI.add(player);
        pauseArmorStands((TabPlayer) player);
        updateTeamData((TabPlayer) player);
    }

    @Override
    public void enableArmorStands(me.neznamy.tab.api.@NonNull TabPlayer player) {
        Preconditions.checkLoaded(player);
        if (!playersDisabledWithAPI.contains(player)) return;
        playersDisabledWithAPI.remove(player);
        resumeArmorStands((TabPlayer) player);
        updateTeamData((TabPlayer) player);
    }

    @Override
    public boolean hasDisabledArmorStands(me.neznamy.tab.api.@NonNull TabPlayer player) {
        return playersDisabledWithAPI.contains(player);
    }

    @Override
    public void setName(@NonNull me.neznamy.tab.api.TabPlayer player, @Nullable String customName) {
        Preconditions.checkLoaded(player);
        ((TabPlayer)player).getProperty(TabConstants.Property.CUSTOMTAGNAME).setTemporaryValue(customName);
        rebuildNameTagLine((TabPlayer) player);
        getArmorStandManager((TabPlayer) player).refresh(true);
    }

    @Override
    public void setLine(@NonNull me.neznamy.tab.api.TabPlayer player, @NonNull String line, @Nullable String value) {
        Preconditions.checkLoaded(player);
        if (!getDefinedLines().contains(line)) throw new IllegalArgumentException("\"" + line + "\" is not a defined line. Defined lines: " + getDefinedLines());
        ((TabPlayer)player).getProperty(line).setTemporaryValue(value);
        getArmorStandManager((TabPlayer) player).refresh(true);
    }

    @Override
    public String getCustomName(me.neznamy.tab.api.@NonNull TabPlayer player) {
        Preconditions.checkLoaded(player);
        return ((TabPlayer)player).getProperty(TabConstants.Property.CUSTOMTAGNAME).getTemporaryValue();
    }

    @Override
    public String getCustomLineValue(me.neznamy.tab.api.@NonNull TabPlayer player, @NonNull String line) {
        Preconditions.checkLoaded(player);
        return ((TabPlayer)player).getProperty(line).getTemporaryValue();
    }

    @Override
    public @NotNull String getOriginalName(me.neznamy.tab.api.@NonNull TabPlayer player) {
        Preconditions.checkLoaded(player);
        return ((TabPlayer)player).getProperty(TabConstants.Property.CUSTOMTAGNAME).getOriginalRawValue();
    }

    @Override
    public @NotNull String getOriginalLineValue(me.neznamy.tab.api.@NonNull TabPlayer player, @NonNull String line) {
        Preconditions.checkLoaded(player);
        return ((TabPlayer)player).getProperty(line).getOriginalRawValue();
    }

    @Override
    public @NotNull List<String> getDefinedLines() {
        List<String> lines = new ArrayList<>(dynamicLines);
        lines.addAll(staticLines.keySet());
        return lines;
    }

    private void rebuildNameTagLine(@NonNull TabPlayer player) {
        player.setProperty(this, TabConstants.Property.NAMETAG, player.getProperty(TabConstants.Property.TAGPREFIX).getCurrentRawValue() +
                player.getProperty(TabConstants.Property.CUSTOMTAGNAME).getCurrentRawValue() + player.getProperty(TabConstants.Property.TAGSUFFIX).getCurrentRawValue());
    }

    /* TeamManager override */

    @Override
    public void setPrefix(@NonNull me.neznamy.tab.api.TabPlayer player, String prefix) {
        super.setPrefix(player, prefix);
        rebuildNameTagLine((TabPlayer) player);
        getArmorStandManager((TabPlayer) player).refresh(true);
    }

    @Override
    public void setSuffix(@NonNull me.neznamy.tab.api.TabPlayer player, String suffix) {
        super.setSuffix(player, suffix);
        rebuildNameTagLine((TabPlayer) player);
        getArmorStandManager((TabPlayer) player).refresh(true);
    }

    @Override
    public void pauseTeamHandling(@NonNull me.neznamy.tab.api.TabPlayer player) {
        Preconditions.checkLoaded(player);
        if (teamHandlingPaused.contains(player)) return;
        if (!getDisableChecker().isDisabledPlayer((TabPlayer) player)) unregisterTeam((TabPlayer) player, getSorting().getShortTeamName((TabPlayer) player));
        teamHandlingPaused.add(player); //adding after, so unregisterTeam method runs
        pauseArmorStands((TabPlayer) player);
    }

    @Override
    public void resumeTeamHandling(@NonNull me.neznamy.tab.api.TabPlayer player) {
        Preconditions.checkLoaded(player);
        if (!teamHandlingPaused.contains(player)) return;
        teamHandlingPaused.remove(player); //removing before, so registerTeam method runs
        if (!getDisableChecker().isDisabledPlayer((TabPlayer) player)) registerTeam((TabPlayer) player);
        resumeArmorStands((TabPlayer) player);
    }

    @Override
    public void toggleNameTagVisibilityView(@NonNull me.neznamy.tab.api.TabPlayer player, boolean sendToggleMessage) {
        super.toggleNameTagVisibilityView(player, sendToggleMessage);
        updateNameTagVisibilityView((TabPlayer) player);
    }
}
