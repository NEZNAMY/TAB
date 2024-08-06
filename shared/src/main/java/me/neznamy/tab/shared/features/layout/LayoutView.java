package me.neznamy.tab.shared.features.layout;

import lombok.Getter;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.SimpleComponent;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Getter
public class LayoutView {

    private final LayoutManagerImpl manager;
    private final LayoutPattern pattern;
    private final TabPlayer viewer;
    private final Condition displayCondition;
    private final List<Integer> emptySlots = IntStream.range(1, 81).boxed().collect(Collectors.toList());
    private final Collection<FixedSlot> fixedSlots;
    private final List<ParentGroup> groups = new ArrayList<>();

    public LayoutView(LayoutManagerImpl manager, LayoutPattern pattern, TabPlayer viewer) {
        this.manager = manager;
        this.viewer = viewer;
        this.pattern = pattern;
        fixedSlots = pattern.getFixedSlots().values();
        displayCondition = pattern.getCondition();
        for (FixedSlot slot : fixedSlots) {
            emptySlots.remove((Integer) slot.getSlot());
        }
        for (GroupPattern group : pattern.getGroups()) {
            emptySlots.removeAll(Arrays.stream(group.getSlots()).boxed().collect(Collectors.toList()));
            groups.add(new ParentGroup(this, group, viewer));
        }
    }

    public void send() {
        send(null);
    }

    public void send(@Nullable LayoutView previous) {
        if (viewer.getVersion().getMinorVersion() < 8 || viewer.isBedrockPlayer()) return;
        for (ParentGroup group : groups) {
            group.sendSlots();
        }
        final Collection<FixedSlot> previousSlots = previous == null ? null : previous.getFixedSlots();
        final List<UUID> previousSlotsUUIDS = previousSlots == null ? Collections.emptyList() : previousSlots.stream().map(x -> x.getId()).collect(Collectors.toList());
        for (FixedSlot slot : fixedSlots) {
            if (previousSlotsUUIDS.contains(slot.getId()) && viewer.getTabList().containsEntry(slot.getId())) {
                // only use updateEntry if the previous skin was the same
                FixedSlot previousSlot = previousSlots.stream().filter(x -> x.getId() == slot.getId()).findAny().orElse(null);
                if (previousSlot != null && slot.getSkinProperty().equals(previousSlot.getSkinProperty())) {
                    slot.updateEntry(viewer);
                    continue;
                }
            }
            viewer.getTabList().removeEntry(slot.getId());
            viewer.getTabList().addEntry(slot.createEntry(viewer));
        }
        // immutable
        final List<Integer> previousEmptySlots = previous == null ? Collections.emptyList() : previous.getEmptySlots();
        for (int slot : emptySlots) {
            // ignore if previous slot was an empty slot as well
            if (previousEmptySlots.contains(slot) && viewer.getTabList().containsEntry(manager.getUUID(slot))) {
                continue;
            }
            viewer.getTabList().removeEntry(manager.getUUID(slot));
            viewer.getTabList().addEntry(new TabList.Entry(
                    manager.getUUID(slot),
                    manager.getDirection().getEntryName(viewer, slot),
                    manager.getSkinManager().getDefaultSkin(slot),
                    true,
                    manager.getEmptySlotPing(),
                    0,
                    new SimpleComponent("")
            ));
        }
        tick();
    }

    public void destroy() {
        if (viewer.getVersion().getMinorVersion() < 8 || viewer.isBedrockPlayer()) return;
        for (UUID id : manager.getUuids().values()) {
            viewer.getTabList().removeEntry(id);
        }
    }

    public void tick() {
        Stream<TabPlayer> str = manager.getSortedPlayers().keySet().stream().filter(
                player -> TAB.getInstance().getPlatform().canSee(viewer, player));
        List<TabPlayer> players = str.collect(Collectors.toList());
        for (ParentGroup group : groups) {
            group.tick(players);
        }
    }

    public PlayerSlot getSlot(@NotNull TabPlayer target) {
        for (ParentGroup group : groups) {
            if (group.getPlayers().containsKey(target)) {
                return group.getPlayers().get(target);
            }
        }
        return null;
    }
}
