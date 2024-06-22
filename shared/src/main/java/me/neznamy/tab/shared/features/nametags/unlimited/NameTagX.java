package me.neznamy.tab.shared.features.nametags.unlimited;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.api.nametag.UnlimitedNameTagManager;
import me.neznamy.tab.shared.features.types.DisableChecker;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.nametags.NameTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;


@Getter
public abstract class NameTagX extends NameTag implements UnlimitedNameTagManager {

    //config options
    private final boolean disableOnBoats = config().getBoolean("scoreboard-teams.unlimited-nametag-mode.disable-on-boats", true);
    private final List<String> dynamicLines = new ArrayList<>(config().getStringList("scoreboard-teams.unlimited-nametag-mode.dynamic-lines", Arrays.asList(TabConstants.Property.ABOVENAME, TabConstants.Property.NAMETAG, TabConstants.Property.BELOWNAME, "another")));
    private final Map<String, Object> staticLines = config().getConfigurationSection("scoreboard-teams.unlimited-nametag-mode.static-lines");
    private final boolean armorStandsAlwaysVisible = TAB.getInstance().getConfiguration().getSecretOption("scoreboard-teams.unlimited-nametag-mode.always-visible", false);

    private final BiFunction<NameTagX, TabPlayer, ArmorStandManager> armorStandFunction;
    private final DisableChecker unlimitedDisableChecker;

    protected NameTagX(@NonNull BiFunction<NameTagX, TabPlayer, ArmorStandManager> armorStandFunction) {
        this.armorStandFunction = armorStandFunction;
        Collections.reverse(dynamicLines);
        Condition disableCondition = Condition.getCondition(config().getString("scoreboard-teams.unlimited-nametag-mode.disable-condition"));
        unlimitedDisableChecker = new DisableChecker(getExtraFeatureName(), disableCondition, this::onUnlimitedDisableConditionChange, p -> p.disabledUnlimitedNametags);
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.UNLIMITED_NAME_TAGS + "-Condition", unlimitedDisableChecker);
    }

    public boolean isPlayerDisabled(@NonNull TabPlayer p) {
        return p.disabledNametags.get() || p.disabledUnlimitedNametags.get() || hasTeamHandlingPaused(p) || hasDisabledArmorStands(p);
    }

    @Override
    public void load() {
        if (invisibleNameTags) {
            TAB.getInstance().getConfigHelper().startup().invisibleAndUnlimitedNameTagsAreMutuallyExclusive();
        }
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            updateProperties(all);
            all.unlimitedNametagData.armorStandManager = armorStandFunction.apply(this, all);
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
        connectedPlayer.unlimitedNametagData.armorStandManager = armorStandFunction.apply(this, connectedPlayer);
        TAB.getInstance().getPlaceholderManager().getTabExpansion().setNameTagPreview(connectedPlayer, false);
    }

    public void addDisabledPlayer(@NotNull TabPlayer player) {
        player.disabledUnlimitedNametags.set(true);
    }

    @Override
    public void refresh(@NotNull TabPlayer refreshed, boolean force) {
        super.refresh(refreshed, force);
        if (isPlayerDisabled(refreshed)) return;
        refreshed.unlimitedNametagData.armorStandManager.refresh(force);
    }

    @Override
    public void unload() {
        super.unload();
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            ArmorStandManager asm = p.unlimitedNametagData.armorStandManager;
            if (asm != null) {
                asm.destroy();
            } else {
                TAB.getInstance().getErrorManager().armorStandNull(p, "unload");
            }
        }
    }

    public void toggleNameTagPreview(TabPlayer player, boolean sendToggleMessage) {
        if (player.unlimitedNametagData.previewing) {
            setNameTagPreview(player, false);
            if (sendToggleMessage) player.sendMessage(TAB.getInstance().getConfiguration().getMessages().getNametagPreviewOff(), true);
            player.unlimitedNametagData.previewing = false;
        } else {
            setNameTagPreview(player, true);
            if (sendToggleMessage) player.sendMessage(TAB.getInstance().getConfiguration().getMessages().getNametagPreviewOn(), true);
            player.unlimitedNametagData.previewing = true;
        }
        TAB.getInstance().getPlaceholderManager().getTabExpansion().setNameTagPreview(player, player.unlimitedNametagData.previewing);
    }

    public void onUnlimitedDisableConditionChange(TabPlayer p, boolean disabledNow) {
        if (!p.disabledNametags.get()) updateTeamData(p);
        p.unlimitedNametagData.armorStandManager.refresh(true);
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
        if (viewer.teamData.invisibleNameTagView) return false;
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
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();
        if (!p.teamData.hiddenNameTagFor.add((TabPlayer) viewer)) return;
        updateTeamData((TabPlayer) player, (TabPlayer) viewer);
        pauseArmorStands((TabPlayer) player);
    }

    @Override
    public void showNameTag(@NonNull me.neznamy.tab.api.TabPlayer player, @NonNull me.neznamy.tab.api.TabPlayer viewer) {
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();
        if (!p.teamData.hiddenNameTagFor.remove((TabPlayer) viewer)) return;
        updateTeamData((TabPlayer) player, (TabPlayer) viewer);
        resumeArmorStands((TabPlayer) player);
    }

    private void rebuildNameTagLine(@NonNull TabPlayer player) {
        player.setProperty(this, TabConstants.Property.NAMETAG, player.getProperty(TabConstants.Property.TAGPREFIX).getCurrentRawValue() +
                player.getProperty(TabConstants.Property.CUSTOMTAGNAME).getCurrentRawValue() + player.getProperty(TabConstants.Property.TAGSUFFIX).getCurrentRawValue());
    }

    @NotNull
    public String getExtraFeatureName() {
        return "Unlimited NameTags";
    }

    /* TeamManager override */

    @Override
    public void setPrefix(@NonNull me.neznamy.tab.api.TabPlayer player, String prefix) {
        ensureActive();
        super.setPrefix(player, prefix);
        rebuildNameTagLine((TabPlayer) player);
        ((TabPlayer) player).unlimitedNametagData.armorStandManager.refresh(true);
    }

    @Override
    public void setSuffix(@NonNull me.neznamy.tab.api.TabPlayer player, String suffix) {
        ensureActive();
        super.setSuffix(player, suffix);
        rebuildNameTagLine((TabPlayer) player);
        ((TabPlayer) player).unlimitedNametagData.armorStandManager.refresh(true);
    }

    @Override
    public void pauseTeamHandling(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();
        if (p.teamData.teamHandlingPaused) return;
        if (!p.disabledNametags.get()) unregisterTeam(p, p.sortingData.getShortTeamName());
        p.teamData.teamHandlingPaused = true; //setting after, so unregisterTeam method runs
        pauseArmorStands(p);
    }

    @Override
    public void resumeTeamHandling(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();
        if (!p.teamData.teamHandlingPaused) return;
        p.teamData.teamHandlingPaused = false; //setting before, so registerTeam method runs
        if (!p.disabledNametags.get()) registerTeam(p);
        resumeArmorStands(p);
    }

    @Override
    public void toggleNameTagVisibilityView(@NonNull me.neznamy.tab.api.TabPlayer player, boolean sendToggleMessage) {
        ensureActive();
        super.toggleNameTagVisibilityView(player, sendToggleMessage);
        updateNameTagVisibilityView((TabPlayer) player);
    }

    // --------------------------------------
    // UnlimitedNameTagManager Implementation
    // --------------------------------------

    @Override
    public void disableArmorStands(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();
        if (p.unlimitedNametagData.disabledWithAPI) return;
        p.unlimitedNametagData.disabledWithAPI = true;
        pauseArmorStands(p);
        updateTeamData(p);
    }

    @Override
    public void enableArmorStands(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();
        if (!p.unlimitedNametagData.disabledWithAPI) return;
        p.unlimitedNametagData.disabledWithAPI = false;
        resumeArmorStands(p);
        updateTeamData(p);
    }

    @Override
    public boolean hasDisabledArmorStands(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        return ((TabPlayer)player).unlimitedNametagData.disabledWithAPI;
    }

    @Override
    public void setName(@NonNull me.neznamy.tab.api.TabPlayer player, @Nullable String customName) {
        ensureActive();
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();
        p.getProperty(TabConstants.Property.CUSTOMTAGNAME).setTemporaryValue(customName);
        rebuildNameTagLine(p);
        p.unlimitedNametagData.armorStandManager.refresh(true);
    }

    @Override
    public void setLine(@NonNull me.neznamy.tab.api.TabPlayer player, @NonNull String line, @Nullable String value) {
        ensureActive();
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();
        if (!getDefinedLines().contains(line)) throw new IllegalArgumentException("\"" + line + "\" is not a defined line. Defined lines: " + getDefinedLines());
        p.getProperty(line).setTemporaryValue(value);
        p.unlimitedNametagData.armorStandManager.refresh(true);
    }

    @Override
    public String getCustomName(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();
        return p.getProperty(TabConstants.Property.CUSTOMTAGNAME).getTemporaryValue();
    }

    @Override
    public String getCustomLineValue(@NonNull me.neznamy.tab.api.TabPlayer player, @NonNull String line) {
        ensureActive();
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();
        return p.getProperty(line).getTemporaryValue();
    }

    @Override
    @NotNull
    public String getOriginalName(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();
        return p.getProperty(TabConstants.Property.CUSTOMTAGNAME).getOriginalRawValue();
    }

    @Override
    @NotNull
    public String getOriginalLineValue(@NonNull me.neznamy.tab.api.TabPlayer player, @NonNull String line) {
        ensureActive();
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();
        return p.getProperty(line).getOriginalRawValue();
    }

    @Override
    @NotNull
    public List<String> getDefinedLines() {
        ensureActive();
        List<String> lines = new ArrayList<>(dynamicLines);
        lines.addAll(staticLines.keySet());
        return lines;
    }

    /**
     * Class storing unlimited nametag data for players.
     */
    public static class PlayerData {

        /** Armor stand manager */
        public ArmorStandManager armorStandManager;

        /** Whether player is previewing armor stands or not */
        public boolean previewing;

        /** Whether armor stands are disabled via API or not */
        public boolean disabledWithAPI;

        /** Whether player is riding a boat or not */
        public boolean onBoat;
    }
}
