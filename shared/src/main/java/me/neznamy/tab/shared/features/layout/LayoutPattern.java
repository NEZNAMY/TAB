package me.neznamy.tab.shared.features.layout;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.api.tablist.layout.Layout;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.types.Refreshable;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Getter
public class LayoutPattern extends TabFeature implements Refreshable, Layout {

    @Getter private final String featureName = "Layout";
    @Getter private final String refreshDisplayName = "Updating player groups";

    @NotNull private final LayoutManagerImpl manager;
    @NotNull private final String name;
    @Nullable private final Condition condition;
    private final Map<Integer, FixedSlot> fixedSlots = new HashMap<>();
    private final List<GroupPattern> groups = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public LayoutPattern(@NotNull LayoutManagerImpl manager, @NotNull String name, @NotNull Map<String, Object> map) {
        this.manager = manager;
        this.name = name;
        TAB.getInstance().getMisconfigurationHelper().checkLayoutMap(name, map);
        condition = Condition.getCondition((String) map.get("condition"));
        if (condition != null) manager.addUsedPlaceholders(Collections.singletonList(TabConstants.Placeholder.condition(condition.getName())));
        for (String fixedSlot : (List<String>)map.getOrDefault("fixed-slots", Collections.emptyList())) {
            addFixedSlot(fixedSlot);
        }
        Map<String, Map<String, Object>> groups = (Map<String, Map<String, Object>>) map.getOrDefault("groups", Collections.emptyMap());
        if (groups != null) {
            for (Map.Entry<String, Map<String, Object>> group : groups.entrySet()) {
                String groupName = group.getKey();
                Map<String, Object> groupData = group.getValue();
                TAB.getInstance().getMisconfigurationHelper().checkLayoutGroupMap(name, groupName, groupData);
                List<Integer> positions = new ArrayList<>();
                for (String line : (List<String>) groupData.get("slots")) {
                    String[] arr = line.split("-");
                    int from = Integer.parseInt(arr[0]);
                    int to = arr.length == 1 ? from : Integer.parseInt(arr[1]);
                    for (int i = from; i<= to; i++) {
                        positions.add(i);
                    }
                }
                addGroup(Condition.getCondition((String) groupData.get("condition")), positions.stream().mapToInt(i->i).toArray());
            }
        }
    }

    public void addFixedSlot(@NotNull String lineDefinition) {
        FixedSlot slot = FixedSlot.fromLine(lineDefinition, this, manager);
        if (slot != null) fixedSlots.put(slot.getSlot(), slot);
    }

    public void addGroup(@Nullable Condition condition, int[] slots) {
        groups.add(new GroupPattern(condition, Arrays.stream(slots).filter(slot -> !fixedSlots.containsKey(slot)).toArray()));
        if (condition != null) addUsedPlaceholders(Collections.singletonList(TabConstants.Placeholder.condition(condition.getName())));
    }

    public boolean isConditionMet(@NotNull TabPlayer p) {
        return condition == null || condition.isMet(p);
    }

    @Override
    public void refresh(@NotNull TabPlayer refreshed, boolean force) {
        manager.getViews().values().forEach(LayoutView::tick);
    }

    @Override
    public void addFixedSlot(int slot, @NonNull String text) {
        addFixedSlot(slot, text, manager.getDefaultSkin(slot), manager.getEmptySlotPing());
    }

    @Override
    public void addFixedSlot(int slot, @NonNull String text, @NonNull String skin) {
        addFixedSlot(slot, text, skin, manager.getEmptySlotPing());
    }

    @Override
    public void addFixedSlot(int slot, @NonNull String text, int ping) {
        addFixedSlot(slot, text, manager.getDefaultSkin(slot), ping);
    }

    @Override
    public void addFixedSlot(int slot, @NonNull String text, @NonNull String skin, int ping) {
        fixedSlots.put(slot, new FixedSlot(manager, slot, this, manager.getUUID(slot), text,
                "Layout-" + text + "-SLOT-" + slot, skin, "Layout-" + text + "-SLOT-" + slot+ "-skin", ping));

    }

    @Override
    public void addGroup(@Nullable String condition, int[] slots) {
        addGroup(Condition.getCondition(condition), slots);
    }
}
