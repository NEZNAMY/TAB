package me.neznamy.tab.shared.features.layout.impl;

import lombok.Getter;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.features.layout.LayoutManagerImpl;
import me.neznamy.tab.shared.features.layout.impl.common.FixedSlot;
import me.neznamy.tab.shared.features.layout.impl.common.PlayerGroup;
import me.neznamy.tab.shared.features.layout.impl.common.PlayerSlot;
import me.neznamy.tab.shared.features.layout.pattern.GroupPattern;
import me.neznamy.tab.shared.features.layout.pattern.LayoutPattern;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Layout implementation using fake entries used to fill the tablist.
 * On <1.19.3, it sends all 80 slots, pushing real players out of the tablist.
 * On 1.19.3+, it hides real players using listed option, which means less than 80 slots are supported.
 */
@Getter
public class FakeEntryLayout extends LayoutBase {

    @NotNull
    private final List<Integer> emptySlots;

    @NotNull
    private final UUID[] allUUIDs;

    @NotNull
    private final Collection<FixedSlot> fixedSlots;

    @NotNull
    private final List<PlayerGroup> groups = new ArrayList<>();

    /**
     * Constructs new instance with given parameters.
     *
     * @param   manager
     *          Layout manager
     * @param   pattern
     *          Layout pattern
     * @param   viewer
     *          Viewer of the layout
     */
    public FakeEntryLayout(@NotNull LayoutManagerImpl manager, @NotNull LayoutPattern pattern, @NotNull TabPlayer viewer) {
        super(manager, pattern, viewer);
        int slotCount = pattern.getSlotCount();
        boolean supportsListed = viewer.getVersion().getNetworkId() >= ProtocolVersion.V1_19_3.getNetworkId() &&
                TAB.getInstance().getPlatform().supportsListed();
        if (!supportsListed) {
            slotCount = 80;
        }
        emptySlots = IntStream.range(1, slotCount + 1).boxed().collect(Collectors.toList());
        allUUIDs = Arrays.copyOf(manager.getUuids(), slotCount);
        fixedSlots = pattern.getFixedSlots().values();
        for (FixedSlot slot : fixedSlots) {
            emptySlots.remove((Integer) slot.getSlot());
        }
        for (GroupPattern group : pattern.getGroups()) {
            emptySlots.removeAll(Arrays.stream(group.getSlots()).boxed().collect(Collectors.toList()));
            groups.add(new PlayerGroup(this, group));
        }
    }

    @Override
    public void send() {
        for (PlayerGroup group : groups) {
            group.sendAll();
        }
        for (FixedSlot slot : fixedSlots) {
            viewer.getTabList().addEntry(slot.createEntry(viewer));
        }
        for (int slot : emptySlots) {
            viewer.getTabList().addEntry(new TabList.Entry(
                    manager.getUUID(slot),
                    manager.getConfiguration().getDirection().getEntryName(viewer, slot, LayoutManagerImpl.isTeamsEnabled()),
                    pattern.getDefaultSkin(slot),
                    true,
                    manager.getConfiguration().getEmptySlotPing(),
                    0,
                    TabComponent.empty(),
                    Integer.MAX_VALUE - manager.getConfiguration().getDirection().translateSlot(slot),
                    true
            ));
        }
        tick();
        viewer.getTabList().hideAllPlayers();
    }

    @Override
    public void destroy() {
        for (UUID id : allUUIDs) {
            viewer.getTabList().removeEntry(id);
        }
        viewer.getTabList().showAllPlayers();
    }

    @Override
    public void tick() {
        List<TabPlayer> players = manager.getSortedPlayers().keySet().stream().filter(viewer::canSee).collect(Collectors.toList());
        for (PlayerGroup group : groups) {
            group.tick(players);
        }
    }

    @Override
    @Nullable
    public PlayerSlot getSlot(@NotNull TabPlayer target) {
        for (PlayerGroup group : groups) {
            if (group.getPlayers().containsKey(target)) {
                return group.getPlayers().get(target);
            }
        }
        return null;
    }
}
