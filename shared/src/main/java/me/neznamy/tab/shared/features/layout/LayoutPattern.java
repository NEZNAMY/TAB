package me.neznamy.tab.shared.features.layout;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.api.tablist.layout.Layout;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.layout.LayoutConfiguration.LayoutDefinition;
import me.neznamy.tab.shared.features.layout.LayoutConfiguration.LayoutDefinition.FixedSlotDefinition;
import me.neznamy.tab.shared.features.layout.LayoutConfiguration.LayoutDefinition.GroupPattern;
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
    @Nullable private final String defaultSkinDefinition;
    @Nullable private final TabList.Skin defaultSkin;
    private final Map<Integer, FixedSlot> fixedSlots = new HashMap<>();
    private final List<GroupPattern> groups = new ArrayList<>();

    public LayoutPattern(@NotNull LayoutManagerImpl manager, @NotNull String name, @NotNull LayoutDefinition def) {
        this.manager = manager;
        this.name = name;
        condition = TAB.getInstance().getPlaceholderManager().getConditionManager().getByNameOrExpression(def.getCondition());
        if (condition != null) manager.addUsedPlaceholder(TabConstants.Placeholder.condition(condition.getName()));
        defaultSkinDefinition = def.getDefaultSkin();
        defaultSkin = def.getDefaultSkin() == null ? null : manager.getSkinManager().getSkin(def.getDefaultSkin());
        for (FixedSlotDefinition fixed : def.getFixedSlots()) {
            addFixedSlot(fixed);
        }
        for (Map.Entry<String, GroupPattern> entry : def.getGroups().entrySet()) {
            addGroup(entry.getKey(), entry.getValue().getCondition(), entry.getValue().getSlots());
        }
    }

    public void addFixedSlot(@NotNull FixedSlotDefinition def) {
        FixedSlot slot = FixedSlot.fromDefinition(def, this, manager);
        fixedSlots.put(slot.getSlot(), slot);
    }

    public void addGroup(@NotNull String name, @Nullable String condition, int[] slots) {
        groups.add(new GroupPattern(name, condition, Arrays.stream(slots).filter(slot -> !fixedSlots.containsKey(slot)).toArray()));
        if (condition != null) addUsedPlaceholder(TabConstants.Placeholder.condition(TAB.getInstance().getPlaceholderManager().getConditionManager().getByNameOrExpression(condition).getName()));
    }

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

    // ------------------
    // API Implementation
    // ------------------

    @Override
    public void addFixedSlot(int slot, @NonNull String text) {
        ensureActive();
        addFixedSlot(
                slot,
                text,
                defaultSkinDefinition != null ? defaultSkinDefinition : manager.getConfiguration().getDefaultSkin(slot),
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
                defaultSkinDefinition != null ? defaultSkinDefinition : manager.getConfiguration().getDefaultSkin(slot),
                ping
        );
    }

    @Override
    public void addFixedSlot(int slot, @NonNull String text, @NonNull String skin, int ping) {
        ensureActive();
        if (slot < 1 || slot > 80) throw new IllegalArgumentException("Slot must be between 1 - 80 (was " + slot + ")");
        fixedSlots.put(slot, new FixedSlot(manager, slot, this, manager.getUUID(slot), text, skin, ping));
    }

    @Override
    public void addGroup(@Nullable String condition, int[] slots) {
        ensureActive();
        addGroup(UUID.randomUUID().toString(), condition, slots);
    }

    @NotNull
    @Override
    public String getFeatureName() {
        return manager.getFeatureName();
    }
}
