package me.neznamy.tab.shared.features.layout.pattern;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.api.tablist.layout.Layout;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.layout.LayoutConfiguration.LayoutDefinition;
import me.neznamy.tab.shared.features.layout.LayoutManagerImpl;
import me.neznamy.tab.shared.features.layout.impl.common.FixedSlot;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Getter
public class LayoutPattern extends RefreshableFeature implements Layout {

    @NotNull private final LayoutManagerImpl manager;
    @NotNull private final String name;
    @Nullable private final Condition condition;
    private final int slotCount;
    @Nullable private final String defaultSkinOverride;
    @Nullable private final TabList.Skin defaultSkin;
    private final Map<Integer, FixedSlot> fixedSlots = new HashMap<>();
    private final List<GroupPattern> groups = new ArrayList<>();

    public LayoutPattern(@NotNull LayoutManagerImpl manager, @NotNull LayoutDefinition def) {
        this.manager = manager;
        name = def.getName();
        condition = TAB.getInstance().getPlaceholderManager().getConditionManager().getByNameOrExpression(def.getCondition());
        if (condition != null) manager.addUsedPlaceholder(condition.getPlaceholderIdentifier());
        slotCount = def.getSlotCount();
        defaultSkinOverride = def.getDefaultSkin();
        defaultSkin = def.getDefaultSkin() == null ? null : manager.getSkinManager().getSkin(def.getDefaultSkin());
        for (FixedSlotPattern fixed : def.getFixedSlots()) {
            FixedSlot slot = FixedSlot.fromDefinition(fixed, this, manager);
            fixedSlots.put(slot.getSlot(), slot);
        }
        for (Map.Entry<String, GroupPattern> entry : def.getGroups().entrySet()) {
            addGroup(entry.getValue().getCondition(), entry.getValue().getSlots());
        }
    }

    /**
     * Checks if condition for this layout pattern is met for given player.
     *
     * @param   p
     *          player to check for
     * @return  {@code true} if condition is met or not defined, {@code false} if not met
     */
    public boolean isConditionMet(@NotNull TabPlayer p) {
        return condition == null || condition.isMet(p);
    }

    /**
     * Returns default skin for this layout pattern. If not defined, uses default skin of specified slot.
     * If that is also not defined, uses global default skin.
     * May return {@code null} if no default skin is defined anywhere, or they are all invalid.
     *
     * @param   slot
     *          slot to get default skin for if not defined in this pattern
     * @return  default skin for this layout pattern
     */
    @Nullable
    public TabList.Skin getDefaultSkin(int slot) {
        if (defaultSkin != null) return defaultSkin;
        return manager.getSkinManager().getDefaultSkin(slot);
    }

    @NotNull
    @Override
    public String getRefreshDisplayName() {
        return "Updating player groups";
    }

    @Override
    public void refresh(@NotNull TabPlayer refreshed, boolean force) {
        manager.tickAllLayouts();
    }

    @NotNull
    @Override
    public String getFeatureName() {
        return manager.getFeatureName();
    }

    // ------------------
    // API Implementation
    // ------------------

    @Override
    public void addFixedSlot(int slot, @NonNull String text) {
        ensureActive();
        addFixedSlot(
                slot,
                text,
                defaultSkinOverride != null ? defaultSkinOverride : manager.getConfiguration().getDefaultSkin(slot),
                manager.getConfiguration().getEmptySlotPing()
        );
    }

    @Override
    public void addFixedSlot(int slot, @NonNull String text, @NonNull String skin) {
        ensureActive();
        addFixedSlot(slot, text, skin, manager.getConfiguration().getEmptySlotPing());
    }

    @Override
    public void addFixedSlot(int slot, @NonNull String text, int ping) {
        ensureActive();
        addFixedSlot(
                slot,
                text,
                defaultSkinOverride != null ? defaultSkinOverride : manager.getConfiguration().getDefaultSkin(slot),
                ping
        );
    }

    @Override
    public void addFixedSlot(int slot, @NonNull String text, @NonNull String skin, int ping) {
        ensureActive();
        if (slot < 1 || slot > slotCount) throw new IllegalArgumentException("Slot must be between 1 - " + slotCount + " (was " + slot + ")");
        fixedSlots.put(slot, new FixedSlot(manager, slot, this, manager.getUUID(slot), text, skin, null, ping));
    }

    @Override
    public void addGroup(@Nullable String condition, int[] slots) {
        ensureActive();
        groups.add(new GroupPattern(condition, Arrays.stream(slots).filter(slot -> !fixedSlots.containsKey(slot)).toArray()));
        if (condition != null) {
            Condition compiled = TAB.getInstance().getPlaceholderManager().getConditionManager().getByNameOrExpression(condition);
            addUsedPlaceholder(compiled.getPlaceholderIdentifier());
            if (compiled.hasRelationalContent()) {
                addUsedPlaceholder(compiled.getRelationalPlaceholderIdentifier());
            }
        }
    }
}