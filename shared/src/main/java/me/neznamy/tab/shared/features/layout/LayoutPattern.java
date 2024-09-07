package me.neznamy.tab.shared.features.layout;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.api.tablist.layout.Layout;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.config.files.config.LayoutConfiguration.LayoutDefinition;
import me.neznamy.tab.shared.config.files.config.LayoutConfiguration.LayoutDefinition.FixedSlotDefinition;
import me.neznamy.tab.shared.config.files.config.LayoutConfiguration.LayoutDefinition.GroupPattern;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Getter
public class LayoutPattern extends RefreshableFeature implements Layout {

    @NotNull private final LayoutManagerImpl manager;
    @NotNull private final String name;
    @Nullable private final Condition condition;
    private final Map<Integer, FixedSlot> fixedSlots = new HashMap<>();
    private final List<GroupPattern> groups = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public LayoutPattern(@NotNull LayoutManagerImpl manager, @NotNull String name, @NotNull LayoutDefinition def) {
        this.manager = manager;
        this.name = name;
        condition = Condition.getCondition(def.condition);
        if (condition != null) manager.addUsedPlaceholder(TabConstants.Placeholder.condition(condition.getName()));
        for (FixedSlotDefinition fixed : def.fixedSlots) {
            addFixedSlot(fixed);
        }
        for (Map.Entry<String, GroupPattern> entry : def.groups.entrySet()) {
            addGroup(entry.getKey(), entry.getValue().condition, entry.getValue().slots);
        }
    }

    public void addFixedSlot(@NotNull FixedSlotDefinition def) {
        FixedSlot slot = FixedSlot.fromDefinition(def, this, manager);
        fixedSlots.put(slot.getSlot(), slot);
    }

    public void addGroup(@NotNull String name, @Nullable String condition, int[] slots) {
        groups.add(new GroupPattern(name, condition, Arrays.stream(slots).filter(slot -> !fixedSlots.containsKey(slot)).toArray()));
        if (condition != null) addUsedPlaceholder(TabConstants.Placeholder.condition(condition));
    }

    public boolean isConditionMet(@NotNull TabPlayer p) {
        return condition == null || condition.isMet(p);
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
        addFixedSlot(slot, text, manager.getConfiguration().getDefaultSkin(slot), manager.getConfiguration().emptySlotPing);
    }

    @Override
    public void addFixedSlot(int slot, @NonNull String text, @NonNull String skin) {
        ensureActive();
        addFixedSlot(slot, text, skin, manager.getConfiguration().emptySlotPing);
    }

    @Override
    public void addFixedSlot(int slot, @NonNull String text, int ping) {
        ensureActive();
        addFixedSlot(slot, text, manager.getConfiguration().getDefaultSkin(slot), ping);
    }

    @Override
    public void addFixedSlot(int slot, @NonNull String text, @NonNull String skin, int ping) {
        ensureActive();
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
