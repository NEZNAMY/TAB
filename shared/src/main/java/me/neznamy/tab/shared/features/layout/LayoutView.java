package me.neznamy.tab.shared.features.layout;

import lombok.Getter;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.SimpleComponent;
import me.neznamy.tab.shared.config.files.config.LayoutConfiguration.LayoutDefinition.GroupPattern;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

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
            emptySlots.removeAll(Arrays.stream(group.slots).boxed().collect(Collectors.toList()));
            groups.add(new ParentGroup(this, group, viewer));
        }
    }

    public void send() {
        if (viewer.getVersion().getMinorVersion() < 8 || viewer.isBedrockPlayer()) return;
        for (ParentGroup group : groups) {
            group.sendSlots();
        }
        for (FixedSlot slot : fixedSlots) {
            viewer.getTabList().addEntry(slot.createEntry(viewer));
        }
        for (int slot : emptySlots) {
            viewer.getTabList().addEntry(new TabList.Entry(
                    manager.getUUID(slot),
                    manager.getConfiguration().direction.getEntryName(viewer, slot, LayoutManagerImpl.isTeamsEnabled()),
                    manager.getSkinManager().getDefaultSkin(slot),
                    true,
                    manager.getConfiguration().emptySlotPing,
                    0,
                    new SimpleComponent(""),
                    Integer.MAX_VALUE - manager.getConfiguration().direction.translateSlot(slot)
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
